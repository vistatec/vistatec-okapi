/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ZipSkeleton;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class ZipSkeletonBean extends GenericSkeletonBean {
	
	//private ZipFileBean original = new ZipFileBean();
	private FactoryBean original = new FactoryBean();
	private String entry;

	@Override
	protected GenericSkeleton createObject(IPersistenceSession session) {
		ZipFile zipFile = null;
		ZipEntry zipEntry = null;
		//List<GenericSkeletonPartBean> parts = super.getParts();
		
		zipFile = original.get(ZipFile.class, session);		
		if (zipFile != null && !Util.isEmpty(entry))
			zipEntry = zipFile.getEntry(entry);
		
		//return new ZipSkeleton(this.get(GenericSkeleton.class, session), zipFile, zipEntry);  
		return new ZipSkeleton(super.createObject(session), zipFile, zipEntry);
	}

	@Override
	protected void fromObject(GenericSkeleton obj, IPersistenceSession session) {		
		super.fromObject(obj, session);
		
		if (obj instanceof ZipSkeleton) {
			ZipSkeleton zs = (ZipSkeleton) obj;
			
			original.set(zs.getOriginal(), session);
			//entry.set(zs.getEntry());
			ZipEntry ze = zs.getEntry();
			if (ze != null)
				entry = ze.getName();
		}
	}

	@Override
	protected void setObject(GenericSkeleton obj, IPersistenceSession session) {
		super.setObject(obj, session);
	}
	
	public FactoryBean getOriginal() {
		return original;
	}

	public void setOriginal(FactoryBean original) {
		this.original = original;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}
}
