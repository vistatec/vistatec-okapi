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

package net.sf.okapi.filters.openxml;

import java.io.File;
import java.util.zip.ZipEntry;

import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Code to parse the _rels files present in Office OpenXML documents.
 */
public class OpenXMLSubDoc {

	private ZipEntry subDocEntry;
	private IFilterWriter subDocWriter;
	private ISkeletonWriter subSkelWriter;
	private File subDocTempFile;
	
	public OpenXMLSubDoc() {
	}
	public OpenXMLSubDoc(ZipEntry subDocEntry, IFilterWriter subDocWriter, 
			OpenXMLContentSkeletonWriter subSkelWriter, File subDocTempFile) {
		this.subDocEntry = subDocEntry; 
		this.subDocWriter = subDocWriter; 
		this.subSkelWriter = subSkelWriter;
		this.subDocTempFile = subDocTempFile;
	}
	
	public void setSubDocEntry(ZipEntry subDocEntry) {
		this.subDocEntry = subDocEntry; 
	}
	
	public ZipEntry getSubDocEntry() {
		return subDocEntry;
	}
	
	public void setSubDocWriter(IFilterWriter subDocWriter) {
		this.subDocWriter = subDocWriter; 
	}
	
	public IFilterWriter getSubDocWriter() {
		return subDocWriter;
	}
	
	public void setSubDocSkelWriter(ISkeletonWriter subSkelWriter) {
		this.subSkelWriter = subSkelWriter; 
	}
	
	public ISkeletonWriter getSubSkelWriter() {
		return subSkelWriter;
	}
	
	
	public void setSubTempFile(File subDocTempFile) {
		this.subDocTempFile = subDocTempFile; 
	}
	
	public File getSubDocTempFile() {
		return subDocTempFile;
	}	
}
