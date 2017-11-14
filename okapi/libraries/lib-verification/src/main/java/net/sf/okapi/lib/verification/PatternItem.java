/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.okapi.common.exceptions.OkapiIOException;

public class PatternItem {
	
	public static final String SAME = "<same>";

	public String source;
	public String target;
	public boolean enabled;
	public String description;
	public int severity;
	public boolean fromSource;
	
	private Pattern srcPat;
	private Pattern trgPat;

	public static List<PatternItem> loadFile (String path) {
		ArrayList<PatternItem> list = new ArrayList<PatternItem>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
			String line = br.readLine();
			while ( line != null ) {
				if ( line.trim().length() == 0 ) continue;
				if ( line.startsWith("#") ) continue;
				String[] parts = line.split("\t", -2);
				if ( parts.length < 6 ) {
					throw new OkapiIOException("Missing one or more tabs in line:\n"+line);
				}
				int severity = Issue.DISPSEVERITY_MEDIUM;
				try {
					severity = Integer.valueOf(parts[2]);
				}
				catch ( Throwable e ) {
					// Just use medium
				}
				list.add(new PatternItem(parts[3], parts[4], parts[0].equals("1"), severity, parts[1].equals("1"), parts[5]));
				line = br.readLine();
			}
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error reading pattern file.", e);
		}
		finally {
			if ( br != null ) {
				try {
					br.close();
				}
				catch ( IOException e ) {
					throw new OkapiIOException("Error closing pattern file.", e);
				}
			}
		}
		return list;
	}
	
	public static List<PatternItem> saveFile (String path,
		List<PatternItem> list)
	{
//TODO: UTF-8		
		// Format:
		// Use?<t>fromSource?<t>severity<t>source<t>target<t>decsription
		PrintWriter pr = null;
		final String lineBreak = System.getProperty("line.separator");
		try {
			pr = new PrintWriter(path);
			for ( PatternItem item : list ) {
				pr.write((item.enabled ? "1" : "0")
					+ "\t" + (item.fromSource ? "1" : "0")
					+ "\t" + String.valueOf(item.severity)
					+ "\t" + item.source
					+ "\t" + item.target
					+ "\t" + item.description
					+ lineBreak);
			}
			pr.flush();
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error reading pattern file.", e);
		}
		finally {
			if ( pr != null ) {
				pr.close();
			}
		}
		return list;
	}
	
	public PatternItem (String source,
		String target,
		boolean enabled,
		int severity)
	{
		create(source, target, enabled, severity, true, null);
	}

	public PatternItem (String source,
		String target,
		boolean enabled,
		int severity,
		String message)
	{
		create(source, target, enabled, severity, true, message);
	}

	public PatternItem (String source,
		String target,
		boolean enabled,
		int severity,
		boolean fromSource,
		String message)
	{
		create(source, target, enabled, severity, fromSource, message);
	}

	private void create (String source,
		String target,
		boolean enabled,
		int severity,
		boolean fromSource,
		String message)
	{
		this.source = source;
		this.target = target;
		this.enabled = enabled;
		this.description = message;
		this.severity = severity;
		this.fromSource = fromSource;
	}

	public void compile () {
		if ( !source.equals(SAME) ) {
			srcPat = Pattern.compile(source);
		}
		if ( !target.equals(SAME) ) {
			trgPat = Pattern.compile(target);
		}
	}

	public Pattern getSourcePattern () {
		return srcPat; 
	}

	public Pattern getTargetPattern () {
		return trgPat; 
	}

}
