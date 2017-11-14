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

package net.sf.okapi.steps.wordcount.common;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.ParametersString;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.lib.extra.AbstractParameters;

@EditorFor(Parameters.class)
public class Parameters extends AbstractParameters implements IEditorDescriptionProvider {

	private static final String COUNTINTEXTUNITS = "countInTextUnits";
	private static final String COUNTINBATCH = "countInBatch";
	private static final String COUNTINBATCHITEMS = "countInBatchItems";
	private static final String COUNTINDOCUMENTS = "countInDocuments";
	private static final String COUNTINSUBDOCUMENTS = "countInSubDocuments";
	private static final String COUNTINGROUPS = "countInGroups";
	private static final String BUFFERSIZE = "bufferSize";
	
	private boolean countInBatch;
	private boolean countInBatchItems;
	private boolean countInDocuments;
	private boolean countInSubDocuments;
	private boolean countInGroups;
	private int bufferSize;
	
	public boolean getCountInBatch () {
		return countInBatch;
	}
	
	public void setCountInBatch (boolean countInBatch) {
		this.countInBatch = countInBatch;
	}
	
	public boolean getCountInBatchItems () {
		return countInBatchItems;
	}
	
	public void setCountInBatchItems (boolean countInBatchItems) {
		this.countInBatchItems = countInBatchItems;
	}
	
	public boolean getCountInDocuments () {
		return countInDocuments;
	}
	
	public void setCountInDocuments (boolean countInDocuments) {
		this.countInDocuments = countInDocuments;
	}
	
	public boolean getCountInSubDocuments () {
		return countInSubDocuments;
	}
	
	public void setCountInSubDocuments (boolean countInSubDocuments) {
		this.countInSubDocuments = countInSubDocuments;
	}
	
	public boolean getCountInGroups () {
		return countInGroups;
	}
	
	public void setCountInGroups (boolean countInGroups) {
		this.countInGroups = countInGroups;
	}
	
	public boolean getCountInTextUnits () {
		return true; // Always
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getBufferSize() {
		return bufferSize;
	}
	
	@Override
	protected void parameters_init() {
	}

	@Override
	protected void parameters_load (ParametersString buffer) {
		countInBatch = buffer.getBoolean(COUNTINBATCH, countInBatch);
		countInBatchItems = buffer.getBoolean(COUNTINBATCHITEMS, countInBatchItems);
		countInDocuments = buffer.getBoolean(COUNTINDOCUMENTS, countInDocuments);
		countInSubDocuments = buffer.getBoolean(COUNTINSUBDOCUMENTS, countInSubDocuments);
		countInGroups = buffer.getBoolean(COUNTINGROUPS, countInGroups);
		bufferSize = buffer.getInteger(BUFFERSIZE, bufferSize);
	}

	@Override
	protected void parameters_reset () {
		countInBatch = true; // Defaults for the scoping report step
		countInBatchItems = true; // Defaults for the scoping report step
		countInDocuments = false;
		countInSubDocuments = false;
		countInGroups = false;
		bufferSize = 0;
	}

	@Override
	protected void parameters_save (ParametersString buffer) {
		buffer.setBoolean(COUNTINBATCH, countInBatch);
		buffer.setBoolean(COUNTINBATCHITEMS, countInBatchItems);
		buffer.setBoolean(COUNTINDOCUMENTS, countInDocuments);
		buffer.setBoolean(COUNTINSUBDOCUMENTS, countInSubDocuments);
		buffer.setBoolean(COUNTINGROUPS, countInGroups);
		buffer.setInteger(BUFFERSIZE, bufferSize);
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(COUNTINTEXTUNITS, "Text units", null);
		desc.add(COUNTINBATCH, "Batches", null);
		desc.add(COUNTINBATCHITEMS, "Batch items", null);
		desc.add(COUNTINDOCUMENTS, "Documents", null);
		desc.add(COUNTINSUBDOCUMENTS, "Sub-documents", null);
		desc.add(COUNTINGROUPS, "Groups", null);
		desc.add(BUFFERSIZE, "Size of text buffer:", null);
		return desc;
	}	

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Word or Character Count", true, false);
		desc.addTextLabelPart("Create a word or character count annotation for each of the following resources:");
		desc.addCheckboxPart(paramsDesc.get(COUNTINTEXTUNITS));
		desc.addCheckboxPart(paramsDesc.get(COUNTINBATCH));
		desc.addCheckboxPart(paramsDesc.get(COUNTINBATCHITEMS));
		desc.addCheckboxPart(paramsDesc.get(COUNTINDOCUMENTS));
		desc.addCheckboxPart(paramsDesc.get(COUNTINSUBDOCUMENTS));
		desc.addCheckboxPart(paramsDesc.get(COUNTINGROUPS));
		desc.addSpinInputPart(paramsDesc.get(BUFFERSIZE));
		return desc;
	}

}
