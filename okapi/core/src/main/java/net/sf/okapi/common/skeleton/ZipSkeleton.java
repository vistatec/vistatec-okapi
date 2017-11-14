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

package net.sf.okapi.common.skeleton;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Extension of the {@link GenericSkeleton} skeleton implementation that allow
 * ZipFile and ZipEntry objects to be passed along with skeleton parts. 
 */
public class ZipSkeleton extends GenericSkeleton {

	private ZipFile original;
	private ZipEntry entry;
	private String modifiedContents;

	public ZipSkeleton (ZipFile original, ZipEntry entry) {
		this.original = original;
		this.entry = entry;
	}
	
	public ZipSkeleton (GenericSkeleton skel, ZipFile original, ZipEntry entry) {
		this(original, entry);
		add(skel);		
	}
	
	public ZipFile getOriginal () {
		return original;
	}
	
	public ZipEntry getEntry () {
		return entry;
	}

	public String getModifiedContents() {
		return modifiedContents;
	}

	public void setModifiedContents(String modifiedContents) {
		this.modifiedContents = modifiedContents;
	}

	@Override
	public ZipSkeleton clone() {
		ZipSkeleton newSkel = new ZipSkeleton(original, entry);
		newSkel.setModifiedContents(modifiedContents);
		super.copyFields(newSkel);
    	return newSkel;
	}
}
