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

/**
 * This class compares two zip files to see if they have
 * the same contents.  The filesExactlyTheSame method takes
 * two files specified by their file paths and indicates
 * by calling FileCompare whether all files in the zip
 * are exactly the same as each other.  This can be used
 * to compare zip file output with a gold standard zip file.  
 */


public class ZipFileCompare {

	//static enum DocumentLocation {TS, CONTEXT, MESSAGE};
	
	private FileCompare fc=null;
	public ZipFileCompare()
	{
		fc = new FileCompare();
	}
	
	public boolean compareFiles (String type,
		String out, 
		String gold, 
		String encoding,
		boolean ignoreEmtpyLines)
	{
		ZipFile goldZipFile = null;
		ZipFile outZipFile = null;
		Enumeration<? extends ZipEntry> outEntries=null;
		Enumeration<? extends ZipEntry> goldEntries=null;
		
		try {
			HashMap<String, ZipEntry> outZipMap = new HashMap<String, ZipEntry>();
			HashMap<String, ZipEntry> goldZipMap = new HashMap<String, ZipEntry>();
			
			try {
				File outZip = new File(out);
				outZipFile = new ZipFile(outZip);
				outEntries = outZipFile.entries();
			}catch(Exception e) {
				System.err.println("ZipCompare:  Output file "+out+" not found.\n");
				System.err.println(Util.getFilename(out, true));
				return false;
			}
	
			try {
				File goldZip = new File(gold);
				goldZipFile = new ZipFile(goldZip);
				goldEntries = goldZipFile.entries();
			} catch(Exception e) {
				System.err.println("ZipCompare:  Gold file "+gold+" not found.\n");
				System.err.println(Util.getFilename(out, true));
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
				System.err.println("Difference in number of files:");
				System.err.println(" out: "+outZipMap.keySet().size());
				System.err.println("gold: "+goldZipMap.keySet().size()+"\n");
				System.err.println(Util.getFilename(out, true));
				return false;
			}
	
			if( !outZipMap.keySet().equals(goldZipMap.keySet()) ){
				System.err.println("Filenames do not match between the zipfiles\n");
				System.err.println(Util.getFilename(out, true));
				return false;
			}
	
			boolean failure = false;
			int identicals = 0;
			
			try {
				for (String filename: outZipMap.keySet()) {
	
					ZipEntry oze= outZipMap.get(filename);
					ZipEntry gze= goldZipMap.get(filename);
						
					InputStream ois = outZipFile.getInputStream(oze);
					InputStream gis = goldZipFile.getInputStream(gze);
	
					boolean same;
					if(type.equals("PerLine")){
						same = fc.compareFilesPerLines (ois, gis, "UTF-8");
					}else if(type.equals("PerLineIgnoreEmpty")){
						same = fc.compareFilesPerLines (ois, gis, "UTF-8", ignoreEmtpyLines );
					}else if(type.equals("PerLineIgnoreEmptyIC")){
						same = fc.compareFilesPerLines (ois, gis, "UTF-8", ignoreEmtpyLines, true );
					}else{ 
						same = fc.filesExactlyTheSame(ois,gis);
					}
						
					if (same){
						identicals++;
					}else{
						System.err.println("Output and Gold Entry "+filename+" differ\n");
						System.err.println(Util.getFilename(out, true));
						if(! failure){
							failure = true;
						}
					}
				}
			}catch(Exception e) {
				System.err.println("Error opening/reading file\n");
				System.err.println(Util.getFilename(out, true));
				return false;
			}
	
			if( !failure ){
				return true;
			}else{
				return false;
			}
		}
		finally {
			if ( outZipFile != null ) {
				try {
					outZipFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if ( goldZipFile != null ) {
				try {
					goldZipFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean compareFilesPerLines(String out, String gold, String encoding){
		return compareFiles("PerLine", out, gold, encoding, false);
	}
	
	public boolean compareFilesPerLines(String out, String gold, String encoding, boolean ignoreEmtpyLines){
		return compareFiles("PerLineIgnoreEmpty", out, gold, encoding, ignoreEmtpyLines);
	}
	
	public boolean compareFilesPerLinesIgnoreCase(String out, String gold, String encoding, boolean ignoreEmtpyLines){
		return compareFiles("PerLineIgnoreEmptyIC", out, gold, encoding, ignoreEmtpyLines);
	}
	
	public boolean filesExactlyTheSame (String out, String gold){
		return compareFiles("ExactlyTheSame", out, gold, null, false);
	}
}
