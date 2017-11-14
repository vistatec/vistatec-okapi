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

package net.sf.okapi.lib.beans.v1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ZipFileBean extends PersistenceBean<ZipFile> {

	private String name;  // ZIP file short name
	private List<ZipEntryBean> entries = new ArrayList<ZipEntryBean>(); // enumeration of the ZIP file entries
	private boolean empty = true;	

	@Override
	protected ZipFile createObject(IPersistenceSession session) {
		if (Util.isEmpty(name) || empty) return null;
		
		File tempZip = null;
		try {
			tempZip = File.createTempFile("~okapi-32_ZipFileBean_", ".zip");
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
		if (tempZip.exists()) tempZip.delete();			
		
		try {
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tempZip.getAbsolutePath()));
			for (ZipEntryBean entryBean : entries) {
				ZipEntry entry = entryBean.get(ZipEntry.class, session);
				zipOut.putNextEntry(entry);
				if (entryBean.getInputStream() != null)
					zipOut.write(entryBean.getInputStream().getData()); // entryBean.getInputStream().getData().length 
				zipOut.closeEntry();				
			}
			zipOut.close();
		} catch (FileNotFoundException e1) {
			throw new OkapiException(e1);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
		
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(tempZip);
		} catch (ZipException e) {
			throw new OkapiException(e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
		return zipFile;
	}

	@Override
	protected void fromObject(ZipFile obj, IPersistenceSession session) {
		if (obj == null) return;
		
		name = Util.getFilename(obj.getName(), true);
		// We make sure that the ZipFile is open, which is not the case when we
		// serialize from a set of collected filter events which are serialized
		// after the filter has finished and closed the file
		try {
//			obj.close();
			
//			File inFile = new File(obj.getName());
//			File tempZip = null;
//			try {
//				tempZip = File.createTempFile("~temp", ".zip");
//				if (tempZip.exists()) tempZip.delete();			
//				tempZip.deleteOnExit();				
//			} catch (IOException e) {
//				throw new OkapiIOException(e);
//			}			
//						
//			Util.copyFile(inFile, tempZip);
			final ZipFile zipFile = new ZipFile(obj.getName());
			obj = zipFile;
			
			session.registerEndTask(new Runnable() {
				@Override
				public void run() {
					try {
						zipFile.close();
					} catch (IOException e) {
						throw new OkapiIOException(e);
					}					
				}				
			});
			
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
		
		for (Enumeration<? extends ZipEntry> e = obj.entries(); e.hasMoreElements();) {
			ZipEntry entry = e.nextElement();			
			ZipEntryBean entryBean = new ZipEntryBean();
			entryBean.set(entry, session);
			InputStreamBean isBean = entryBean.getInputStream();
			try {
				isBean.set(obj.getInputStream(entry), session);
			} catch (IOException e1) {
				throw new OkapiIOException(e1);
			}
			entries.add(entryBean);
		}
		empty = Util.isEmpty(name) || Util.isEmpty(entries);
	}

	@Override
	protected void setObject(ZipFile obj, IPersistenceSession session) {		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ZipEntryBean> getEntries() {
		return entries;
	}

	public void setEntries(List<ZipEntryBean> entries) {
		this.entries = entries;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
}
