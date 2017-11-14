/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class compares two zip files containing XML entries to see if they have
 * the same contents. This can be used to compare zip file output with a gold 
 * standard zip file.  
 */
public class ZipXMLFileCompare {
	private final static Logger LOGGER = LoggerFactory.getLogger(ZipXMLFileCompare.class);
	
	private XMLFileCompare fc;
	
	public ZipXMLFileCompare() {
		fc = new XMLFileCompare();
	}
	
	public boolean compareFiles (String out, String gold)
	{
		ZipFile goldZipFile = null;
		ZipFile outZipFile = null;
		Enumeration<? extends ZipEntry> outEntries=null;
		Enumeration<? extends ZipEntry> goldEntries=null;
		File tempFileOut = null;
		File tempFileGold = null;
		
		try {
			HashMap<String, ZipEntry> outZipMap = new HashMap<String, ZipEntry>();
			HashMap<String, ZipEntry> goldZipMap = new HashMap<String, ZipEntry>();
			
			try {
				File outZip = new File(out);
				outZipFile = new ZipFile(outZip);
				outEntries = outZipFile.entries();
			}catch(Exception e) {
				LOGGER.trace("ZipCompare:  Output file "+out+" not found.\n");
				LOGGER.trace(Util.getFilename(out, true));
				return false;
			}
	
			try {
				File goldZip = new File(gold);
				goldZipFile = new ZipFile(goldZip);
				goldEntries = goldZipFile.entries();
			} catch(Exception e) {
				LOGGER.trace("ZipCompare:  Gold file "+gold+" not found.\n");
				LOGGER.trace(Util.getFilename(out, true));
				return false;
			}
			
			while( outEntries.hasMoreElements() ){
				ZipEntry ze = outEntries.nextElement();
				outZipMap.put(ze.getName(), ze);
			}
			
			while( goldEntries.hasMoreElements() ){
				ZipEntry ze = goldEntries.nextElement();
				goldZipMap.put(ze.getName(), ze);
			}
			
			if( outZipMap.keySet().size() != goldZipMap.keySet().size() ){
				LOGGER.trace("Difference in number of files:");
				LOGGER.trace(" out: "+outZipMap.keySet().size());
				LOGGER.trace("gold: "+goldZipMap.keySet().size()+"\n");
				LOGGER.trace(Util.getFilename(out, true));
				return false;
			}
	
			if( !outZipMap.keySet().equals(goldZipMap.keySet()) ){
				LOGGER.trace("Filenames do not match between the zipfiles\n");
				LOGGER.trace(Util.getFilename(out, true));
				return false;
			}
	
			boolean failure = false;
						
			try {
				for (String filename: outZipMap.keySet()) {
	
					ZipEntry oze= outZipMap.get(filename);
					ZipEntry gze= goldZipMap.get(filename);
	
					if (!oze.getName().toLowerCase().endsWith(".xml")) continue;
					if (!gze.getName().toLowerCase().endsWith(".xml")) continue;
					
					// some formats have zero byte xml files (openoffice)
					if (oze.getSize() <= 0 && gze.getSize() <= 0) {
						continue;
					}
										
					InputStream ois = outZipFile.getInputStream(oze);
					InputStream gis = goldZipFile.getInputStream(gze);
	
					tempFileOut = FileUtil.createTempFile("~okapi-17_" + Util.getFilename(oze.getName(), true) + "_");
					tempFileGold = FileUtil.createTempFile("~okapi-18_" + Util.getFilename(gze.getName(), true) + "_");
					
					String tempOut = tempFileOut.getAbsolutePath();
					String tempGold = tempFileGold.getAbsolutePath();
										
					try {
						TestUtil.writeString(TestUtil.inputStreamAsString(ois), tempOut, "UTF-8");
						TestUtil.writeString(TestUtil.inputStreamAsString(gis), tempGold, "UTF-8");
					} catch (IOException e) {
						LOGGER.trace("Error writing files\n");
						LOGGER.trace(Util.getFilename(out, true));
						LOGGER.trace(Util.getFilename(gold, true));
						return false;
					}
					
					boolean same = fc.compareFilesPerLines(tempOut, tempGold);
						
					if ( !same ){
						LOGGER.trace("Output and Gold Entry "+filename+" differ\n");
						LOGGER.trace(Util.getFilename(out, true));
						if(! failure){
							failure = true;
						}
					}
				}
			}catch(Exception e) {
				LOGGER.trace("Error opening/reading file\n");
				LOGGER.trace(Util.getFilename(out, true));
				return false;
			}
	
			if( !failure ){
				return true;
			}else{
				return false;
			}
		}
		finally {
			if (tempFileGold != null) tempFileGold.delete();
			if (tempFileOut != null) tempFileOut.delete();
			
			if ( outZipFile != null ) {
				try {
					outZipFile.close();
				} catch (IOException e) {
					LOGGER.error("Error closing zip file", e);
				}
			}
			if ( goldZipFile != null ) {
				try {
					goldZipFile.close();
				} catch (IOException e) {
					LOGGER.error("Error closing zip file", e);
				}
			}
		}
	}
}
