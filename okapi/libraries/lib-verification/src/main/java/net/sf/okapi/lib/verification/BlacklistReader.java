/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.lib.verification;

import com.ibm.icu.lang.UCharacter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import net.sf.okapi.common.BOMAwareInputStream;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiIOException;

public class BlacklistReader {
	
	private BlackTerm nextEntry;
	private BufferedReader reader;
	private LocaleId locale = new LocaleId(Locale.getDefault());

	public BlacklistReader() {
		reset();
	}

	public BlacklistReader(LocaleId loc) {
		this();
		this.locale = loc;
	}

	public void reset() {
		nextEntry = null;
		reader = null;
	}
	
	@SuppressWarnings("resource")
	public void open(File file) {
		try {
			open(new FileInputStream(file));
		}
		catch (Throwable e) {
			throw new OkapiIOException("Error opening the URI.\n" + e.getLocalizedMessage());
		}
	}
	
	public void open(InputStream input) {
		try {
			close();
			
			// Deal with potential BOM
			String encoding = "UTF-8";
			// bis is closed when reader is closed by Ifilter.close()
			BOMAwareInputStream bis = new BOMAwareInputStream(input, encoding);
			encoding = bis.detectEncoding();
			reader = new BufferedReader(new InputStreamReader(bis, encoding));
			
			// Read input document
			readNext();
		}
		catch (Throwable e) {
			throw new OkapiIOException("Error opening the URI.\n" + e.getLocalizedMessage());
		}
	}
	
	public void close() {
		nextEntry = null;
		try {
			if (reader != null) {
				reader.close();
				reader = null;
			}
		}
		catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	public boolean hasNext() {
		return (nextEntry != null);
	}
	
	public BlackTerm next() {
		BlackTerm currentEntry = nextEntry;
		readNext();
		return currentEntry;
	}
	
	private void readNext() {
		try {
			nextEntry = null;
			String parts[];
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					return;
				}
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				parts = line.split("\\t");
				if (!parts[0].isEmpty()) {
					break;
				}
			}

			BlackTerm bterm = populateBlackTerm(parts);
			nextEntry = bterm;
		}
		catch (Throwable e) {
			throw new OkapiIOException("Error opening the URI.\n" + e.getLocalizedMessage());
		}
	}

	// We try to figure out if the intention was to fix casing by comparing column 0 and 1.
	// If the two columns are the same when we ignore case, then the intention was to make a case correction. 
	// Example:
	//   Wordpress    WordPress  ==> the intention was to fix casing, so the validation will be done case-insensitive
	//   client       customer  ==> the intention was to fix terminology, so we do case-insensitive check (like before)
	// So there is no need to add another column to specify case sensitive behavior, and to manually maintain that info 
	private BlackTerm populateBlackTerm(String parts[]) {
		BlackTerm bterm = new BlackTerm();
		bterm.text = parts[0];
		bterm.suggestion = (parts.length == 1) ? "" : parts[1];
		bterm.comment = (parts.length < 3) ? "" : parts[2];
		// Case conversion is locale sensitive, and toUpperCase is better than toLowercase
		bterm.searchTerm = UCharacter.toUpperCase(locale.toIcuLocale(), bterm.text);
		String suggestionUpperCase = UCharacter.toUpperCase(locale.toIcuLocale(), bterm.suggestion);
		if (bterm.searchTerm.equals(suggestionUpperCase)) {
			bterm.doCaseSensitiveMatch = true;
			bterm.searchTerm = bterm.text;
		}
		return bterm;
	}

}
