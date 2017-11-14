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
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.lib.persistence.json.jackson.JSONUtil;

public class VersionManager {

	public static final String CONFIG = "/versions.json";
	private Versions versions; // List is sorted by date
	private static final VersionInfo EMPTY_INFO = new VersionInfo();
	private VersionInfo activeVersion = EMPTY_INFO;
	private ClassLoader classLoader;
	private String path;
	
	public VersionManager() {
		super();
		versions = new Versions();
	}
	
	/**
	 * 
	 * @param date date from a rainbow t-kit manifest file (manifest.rkm). 
	 * Expected date format is "yyyy-MM-dd HH:mm:ssZ" ("2014-04-08 19:12:23+0200").
	 * @see /okapi-filter-rainbowkit/src/main/java/net/sf/okapi/filters/rainbowkit/Manifest.java  
	 * @return
	 */
	public String getPathByDate(String date) {
		return getVersionInfoByDate(date).getPath();
	}
	
	public String getLabelByDate(String date) {
		return getVersionInfoByDate(date).getLabel();
	}
	
	private boolean isDateRegistered(String date) {
		for (VersionInfo vi : versions) {
			if (vi.getDate().equals(date)) return true;
		}
		return false;
	}
	
	private VersionInfo getVersionInfoByDate(String date) {		
		VersionInfo info = null;
		Date d = null;
		try {
			d = VersionInfo.parseLongDate(date);
		} catch (ParseException e) {
			try {
				d = VersionInfo.parseShortDate(date);
			} catch (ParseException e1) {
				return EMPTY_INFO;
			}
		}
		
		long time = d.getTime();
		
		// Versions are sorted by date
		for (VersionInfo vi : versions) {
			try {
				Date viDate = VersionInfo.parseShortDate(vi.getDate());
				long viTime = viDate.getTime();
				if (time < viTime) {					
					break;
				}
				else {
					info = vi;
				}
			} catch (ParseException e) {
				continue;
			}			
		}
		
		return info == null ? EMPTY_INFO : info;
	}
	
	public String getPathByLabel(String label) {
		for (VersionInfo vi : versions) {
			if (vi.getLabel().equals(label)) {
				return vi.getPath();
			}
		}
		return "";
	}
	
	private VersionInfo getVersionInfoByPath(String path) {
		for (VersionInfo vi : versions) {
			if (vi.getPath().equals(path)) {
				return vi;
			}
		}
		return EMPTY_INFO;
	}
	
	public Versions getVersions() {
		return versions;
	}

	public void setVersions(Versions versions) {
		this.versions = versions;
	}
	
	public void add(VersionInfo versionInfo) {
		if (isDateRegistered(versionInfo.getDate()))
			throw new OkapiException("Date is already bound to another version");
		getVersions().add(versionInfo);
	}
	
	public void add(String label, String path, String startDate) {
		this.add(new VersionInfo(label, path, startDate));
	}
	
	public void load(String path) {
		versions = JSONUtil.fromJSON(StringUtil.readString(new File(path)));
	}
	
	public void load(URL url) {
		versions = JSONUtil.fromJSON(StringUtil.readString(url));
	}
	
	public void load(URI uri) throws MalformedURLException {
		versions = JSONUtil.fromJSON(StringUtil.readString(uri.toURL()));
	}
	
	public void load(InputStream in) {
		versions = JSONUtil.fromJSON(StreamUtil.streamUtf8AsString(in));
	}
	
	public void store(String path) {
		String json = JSONUtil.toJSON(versions, true);
		StringUtil.writeString(json, new File(path));
	}
	
	public void store(OutputStream os) {
		String json = JSONUtil.toJSON(versions, true);
		StringUtil.writeString(json, os);
	}
	
	@Override
	public String toString() {
		return JSONUtil.toJSON(versions, true);
	}

	public ClassLoader setClassLoader(String path) {
		return setClassLoader(FileUtil.fileToUrl(new File(path)));
	}
	
	public ClassLoader setClassLoader(URL url) {
		URL[] urls = null;
				
		if (url.getFile().endsWith(".jar")) {
			urls = new URL[] {url}; 
		}
		else {
			FilenameFilter jarFilter = new FilenameFilter() {
				@Override public boolean accept(File dir, String name) {
					return name.toLowerCase(Locale.US).endsWith(".jar");
				}
			};
			// Directory, scan for all jars in it to pass them to the constructor
			Collection<File> jars = FileUtil.getFilteredFiles(FileUtil.urlToFile(url), jarFilter, true);
			List<URL> urlList = new ArrayList<URL>(jars.size() + 1);
			
			urlList.add(url);
			for (File jar : jars) {
				urlList.add(FileUtil.fileToUrl(jar));
			}
			urls = urlList.toArray(new URL[urlList.size()]);
		}		
					
//		ClassLoader parentLoader = this.getClass().getClassLoader();
//		Class<? extends ClassLoader> plClass = this.getClass().getClassLoader().getClass();
//		plClass.getMethod(name, parameterTypes);
		
		// Falls back to the initial class loader		
//		classLoader = URLClassLoader.newInstance(urls, this.getClass().getClassLoader());
//		classLoader = VMClassLoader.newInstance(urls, this.getClass().getClassLoader());
//		classLoader = new VMClassLoader(urls, this.getClass().getClassLoader());
				
		ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
		
		// If system CL is set from outside as JVM param
		if (sysClassLoader instanceof VMClassLoader) {
			// As the system CL (VMClassLoader) is instantiated by the system, it has no URLs yet, we need to set them here
			for (URL u : urls) {
				((VMClassLoader) sysClassLoader).addURL(u);
//				System.out.println("   " + u);
			}
			classLoader = sysClassLoader;
		}
		else {
			classLoader = new VMClassLoader(urls); // Parent CL is the system CL
		}
//		classLoader = URLClassLoader.newInstance(urls);
//		Thread.currentThread().setContextClassLoader(classLoader);
//		AccessController.doPrivileged(				
//		        new PrivilegedAction<Void>() {		        	
//		        	public Void run() {
//		        		Thread.currentThread().setContextClassLoader(classLoader);
//				    	return null;
//				    }
//		        }
//		);
		return classLoader;
	}
	
	/**
	 * Version to use is selected based on a given version label.
	 * Possible labels are defined in the configuration file.
	 * @param label predefined label of the version to use.
	 */
	public void loadVersion(String label) {
		checkVersions();
		path = getPathByLabel(label);
		activeVersion = getVersionInfoByPath(path);
		setClassLoader(path);
	}
	
	/**
	 * Version to use is selected based on a given file creation date matched against version dates in the configuration file.
	 * @param file file which creation date will determine the version selection.
	 */
	public void loadVersion(File file) {
		checkVersions();
		String date = VersionInfo.getFileCreationDate(file);
		path = getPathByDate(date);
		activeVersion = getVersionInfoByPath(path);
		setClassLoader(path);
	}
	
	/**
	 * Version to use is located at a given URL.
	 * @param url URL of a jar file or directory.
	 */
	public void loadVersion(URL url) {
		checkVersions();
		path = url.getPath();
		activeVersion = EMPTY_INFO;		
		setClassLoader(url);
	}

	private void checkVersions() {
		if (versions.size() == 0)
			this.load(VersionManager.class.getResource(CONFIG));
	}
	
	public String getActiveVersionLabel() {
		return activeVersion.getLabel();
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String getPath() {
		return path;
	}
}
