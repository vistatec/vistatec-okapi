/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.rtf;

import net.sf.okapi.common.Event;
import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.MergingInfo;
import net.sf.okapi.steps.rainbowkit.common.BasePackageWriter;

public class RTFPackageWriter extends BasePackageWriter {

	private RTFLayerWriter layerWriter;
	private String rawDocPath;
	private String encoding;

	public RTFPackageWriter () {
		super(Manifest.EXTRACTIONTYPE_RTF);
	}
	
	@Override
	protected void processStartBatch () {
		manifest.setSubDirectories("original", "work", "work", "done", null, null, true);
		setTMXInfo(true, null, false, false, false);
		super.processStartBatch();
	}
	
	@Override
	protected void processStartDocument (Event event) {
		super.processStartDocument(event);
		MergingInfo item = manifest.getItem(docId);
		rawDocPath = manifest.getTempSourceDirectory() + item.getRelativeInputPath() + ".rtf";
		encoding = item.getTargetEncoding();
		layerWriter = new RTFLayerWriter(skelWriter, rawDocPath, manifest.getTargetLocale(), encoding);
		layerWriter.writeEvent(event);
	}
	
	@Override
	protected Event processEndDocument (Event event) {
		layerWriter.writeEvent(event);
		close();
		
		if ( params.getSendOutput() ) {
			return super.creatRawDocumentEventSet(rawDocPath, encoding,
				manifest.getSourceLocale(), manifest.getTargetLocale());
		}
		else {
			return event;
		}
	}

	@Override
	protected void processStartSubDocument (Event event) {
		layerWriter.writeEvent(event);
	}
	
	@Override
	protected void processEndSubDocument (Event event) {
		layerWriter.writeEvent(event);
	}
	
	@Override
	protected void processTextUnit (Event event) {
		layerWriter.writeEvent(event);
		writeTMXEntries(event.getTextUnit());
	}

	@Override
	protected void processStartGroup (Event event) {
		layerWriter.writeEvent(event);
	}

	@Override
	protected void processEndGroup (Event event) {
		layerWriter.writeEvent(event);
	}

	protected void processDocumentPart (Event event) {
		layerWriter.writeEvent(event);
	}

	@Override
	public void close () {
		if ( layerWriter != null ) {
			layerWriter.close();
			layerWriter = null;
		}
	}

	@Override
	public String getName () {
		return getClass().getName();
	}

}
