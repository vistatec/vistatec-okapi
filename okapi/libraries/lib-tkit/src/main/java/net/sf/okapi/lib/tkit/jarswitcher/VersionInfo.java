/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

package net.sf.okapi.lib.tkit.jarswitcher;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sf.okapi.common.exceptions.OkapiIOException;

public class VersionInfo implements Comparable<VersionInfo> {

	private final static SimpleDateFormat LONG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
	private final static SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	
	private String label; // Unique label of this version
	private String path;  // Path to the version root jar
	private String date;  // date specifies the start date the jars of this version started being used for t-kit creation.

	public VersionInfo() {
		super();
	}
	
	/**
	 * 
	 * @param label
	 * @param path
	 * @param date the start date the jars of this version started being used for t-kit creation. 
	 * Expected date format is "yyyy-MM-dd" ("2014-04-08").
	 */
	public VersionInfo(String label, String path, String date) {
		super();
		this.label = label;
		this.path = path;
		this.date = date;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getDate() {
		return date;
	}
	
	/**
	 * 
	 * @param date the start date the jars of this version started being used for t-kit creation. 
	 * Expected date format is "yyyy-MM-dd" ("2014-04-08").
	 */
	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public int compareTo(VersionInfo vi) {
		try {
			Date d = parseShortDate(date);		
			Date d2 = parseShortDate(vi.getDate());
			return d.compareTo(d2);
		} catch (ParseException e) {
			return 0;
		}		
	}

	public static Date parseLongDate(String date) throws ParseException {
		return LONG_DATE_FORMAT.parse(date);
	}
	
	public static Date parseShortDate(String date) throws ParseException {
		return SHORT_DATE_FORMAT.parse(date);
	}
	
	public static String getFileCreationDate(URL url) {
		try {
			return getFileCreationDate(new File(url.toURI()));
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	public static String getFileCreationDate(String path) {
		return getFileCreationDate(new File(path));
	}
	
	public static String getFileCreationDate(File file) {
		Path path = file.toPath();
		try {
			BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);		
			FileTime creationTime = attr.creationTime();
			long mils = creationTime.toMillis();
			
			Date d = new Date(mils);
			String date = SHORT_DATE_FORMAT.format(d);
			return date;
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}		
	}

}
