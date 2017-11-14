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

package net.sf.okapi.lib.beans.v0;

import java.util.zip.ZipEntry;

import net.sf.okapi.common.skeleton.ZipSkeleton;

@Deprecated
public class ZipSkeletonBean implements IPersistenceBean {

	private String zipFileName;
	//private byte[] bytes = new byte[] {3, 5, 120, 127};
	//private TextContainerBean testBean = new TextContainerBean();
	
	@Override
	public void init(IPersistenceSession session) {
	}
	
	@Override
	public IPersistenceBean set(Object obj) {
		// TODO Auto-generated method stub
		return this;
	}

	@Override
	public <T> T get(Class<T> classRef) {
		ZipSkeleton skel = new ZipSkeleton(null, new ZipEntry(""));
		
		return classRef.cast(skel);
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public String getZipFileName() {
		return zipFileName;
	}

//	public byte[] getBytes() {
//		return bytes;
//	}
//
//	public void setBytes(byte[] bytes) {
//		this.bytes = bytes;
//	}
//
//	public void setTestBean(TextContainerBean testBean) {
//		this.testBean = testBean;
//	}
//
//	public TextContainerBean getTestBean() {
//		return testBean;
//	}	

}
