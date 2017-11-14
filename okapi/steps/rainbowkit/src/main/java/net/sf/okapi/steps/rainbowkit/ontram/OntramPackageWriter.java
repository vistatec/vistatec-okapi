/*===========================================================================
  Copyright (C) 2011-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.ontram;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.MergingInfo;
import net.sf.okapi.filters.xini.rainbowkit.XINIRainbowkitWriter;
import net.sf.okapi.steps.rainbowkit.common.BasePackageWriter;

public class OntramPackageWriter extends BasePackageWriter {

	private XINIRainbowkitWriter writer;

	public OntramPackageWriter () {
		super(Manifest.EXTRACTIONTYPE_ONTRAM);
		writer = new XINIRainbowkitWriter();
		setSupporstOneOutputPerInput(false);
	}

	@Override
	protected void processStartBatch () {
		manifest.setSubDirectories("original", "xini", "xini", "translated", null, null, false);
		setTMXInfo(false, null, false, false, false);

		writer = new XINIRainbowkitWriter();
		writer.setOutputPath(manifest.getTempSourceDirectory() + "contents.xini");
		
		writer.init();
		
		super.processStartBatch();
	}

	@Override
	protected void processStartDocument (Event event) {
		super.processStartDocument(event);
		
		MergingInfo info = manifest.getItem(docId);
		String inputPath = info.getRelativeInputPath();
		writer.setNextPageName(inputPath);
		
		writer.handleEvent(event);
	}
	
	@Override
	protected void processEndBatch () {
		super.processEndBatch();

		writer.writeXINI();
		close();
	}

	@Override
	protected void processTextUnit (Event event) {
		// Skip non-translatable
		ITextUnit tu = event.getTextUnit();
		if ( !tu.isTranslatable() ) return;
		
		writer.handleEvent(event);
	}
	
	@Override
	public void close () {
		if ( writer != null ) {
			writer.close();
			writer = null;
		}
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	protected Event processEndDocument (Event event) {
		// Does not support one output per input
		return event;
	}

	@Override
	protected void processStartGroup(Event event) {
		writer.handleEvent(event);
	}

	@Override
	protected void processEndGroup(Event event) {
		writer.handleEvent(event);
	}
}
