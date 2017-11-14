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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;

class BlacklistTB {
	
	private List<BlackTerm> entries;
	private LocaleId locale = new LocaleId(Locale.getDefault());

	public BlacklistTB() {
		reset();
	}

	public BlacklistTB(LocaleId loc) {
		this();
		this.locale = loc;
	}

	private void reset() {
		entries = new ArrayList<BlackTerm>();
	}

	public void guessAndImport(File file) {
		String ext = Util.getExtension(file.getPath());
		if (ext.equalsIgnoreCase(".xyz")){
			// not supported yet
		} else { // Try tab-delimited
			importTSV(file);
		}
	}
	
	public void removeAll() {
		entries.clear();
	}
	
	public List<BlackTerm> getBlacklistStrings() {
		return entries;
	}
	
	private void importTSV(File file) {
		importBlacklist(new BlacklistReader(locale), file);
	}

	private void importBlacklist(BlacklistReader reader, File file) {
		try {
			reader.open(file);
			importBlacklist(reader);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private void importBlacklist(BlacklistReader reader, InputStream input) {
		try {
			reader.open(input);
			importBlacklist(reader);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	private void importBlacklist(BlacklistReader reader) {
		while (reader.hasNext()) {
			BlackTerm bterm = reader.next();
			entries.add(bterm);
		}
	}

	void loadBlacklistStream(InputStream input) {
		if (input != null) {
			importBlacklist(new BlacklistReader(locale), input);
		}
	}

}
