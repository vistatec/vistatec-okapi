/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.batchtranslation;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.FolderInputPart;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.PathInputPart;
import net.sf.okapi.common.uidescription.SpinInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String COMMAND = "command";
	private static final String ORIGIN = "origin";
	private static final String MARKASMT = "markAsMT";
	private static final String MAKETM = "makeTM";
	private static final String TMDIRECTORY = "tmDirectory";
	private static final String MAKETMX = "makeTMX";
	private static final String TMXPATH = "tmxPath";
	private static final String SENDTMX = "sendTMX";
	private static final String BLOCKSIZE = "blockSize";
	private static final String CHECKEXISTINGTM = "checkExistingTm";
	private static final String EXISTINGTM = "existingTm";
	private static final String SEGMENT = "segment";
	private static final String SRXPATH = "srxPath";
	
	public Parameters () {
		super();
	}
	
	public String getCommand () {
		return getString(COMMAND);
	}

	public void setCommand (String command) {
		setString(COMMAND, command);
	}

	public String getOrigin () {
		return getString(ORIGIN);
	}

	public void setOrigin (String origin) {
		setString(ORIGIN, origin);
	}

	public boolean getMarkAsMT () {
		return getBoolean(MARKASMT);
	}

	public void setMarkAsMT (boolean markAsMT) {
		setBoolean(MARKASMT, markAsMT);
	}

	public boolean getMakeTMX () {
		return getBoolean(MAKETMX);
	}

	public void setMakeTMX (boolean makeTMX) {
		setBoolean(MAKETMX, makeTMX);
	}

	public String getTmxPath () {
		return getString(TMXPATH);
	}

	public void setTmxPath (String tmxPath) {
		setString(TMXPATH, tmxPath);
	}

	public boolean getSendTMX () {
		return getBoolean(SENDTMX);
	}

	public void setSendTMX (boolean sendTMX) {
		setBoolean(SENDTMX, sendTMX);
	}

	public boolean getMakeTM () {
		return getBoolean(MAKETM);
	}

	public void setMakeTM (boolean makeTM) {
		setBoolean(MAKETM, makeTM);
	}

	public String getTmDirectory () {
		return getString(TMDIRECTORY);
	}

	public void setTmDirectory (String tmDirectory) {
		setString(TMDIRECTORY, tmDirectory);
	}

	public int getBlockSize () {
		return getInteger(BLOCKSIZE);
	}

	public void setBlockSize (int blockSize) {
		setInteger(BLOCKSIZE, blockSize);
	}

	public boolean getCheckExistingTm () {
		return getBoolean(CHECKEXISTINGTM);
	}

	public void setCheckExistingTm (boolean checkExistingTm) {
		setBoolean(CHECKEXISTINGTM, checkExistingTm);
	}

	public String getExistingTm () {
		return getString(EXISTINGTM);
	}

	public void setExistingTm (String existingTm) {
		setString(EXISTINGTM, existingTm);
	}
	
	public boolean getSegment () {
		return getBoolean(SEGMENT);
	}

	public void setSegment (boolean segment) {
		setBoolean(SEGMENT, segment);
	}
	
	public String getSrxPath () {
		return getString(SRXPATH);
	}

	public void setSrxPath (String srxPath) {
		setString(SRXPATH, srxPath);
	}

	public void reset() {
		super.reset();
		setCommand("");
		setOrigin("");
		setMarkAsMT(true);
		setMakeTM(false);
		setTmDirectory("");
		setMakeTMX(false);
		setTmxPath("");
		setSendTMX(false);
		setBlockSize(1000);
		setCheckExistingTm(false);
		setExistingTm("");
		setSegment(false);
		setSrxPath("");
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(COMMAND, "Command line", "Command line to execute the batch translation");
		desc.add(ORIGIN, "Origin identifier", "String that identifies the origin of the translation");
		desc.add(BLOCKSIZE, "Block size", "Maximum number of text units to process together");
		desc.add(MARKASMT, "Mark the generated translation as machine translation results", null);
		desc.add(SEGMENT, "Segment the text units, using the following SRX rules:", null);
		desc.add(SRXPATH, "", "Full path of the segmentation rules file to use");
		desc.add(MAKETM, "Import into the following Pensieve TM:", null);
		desc.add(TMDIRECTORY, "", "Location of the TM to create or use");
		desc.add(MAKETMX, "Create the following TMX document:", null);
		desc.add(TMXPATH, "", "Full path of the new TMX document to create");
		desc.add(SENDTMX, "Send the TMX document to the next step", null);
		desc.add(CHECKEXISTINGTM, "Check for existing entries in an existing Pensieve TM:", null);
		desc.add(EXISTINGTM, "", "Location of the TM to lookup");
		return desc;
	}

	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Batch Translation", true, false);

		desc.addTextInputPart(paramDesc.get(COMMAND));

		SpinInputPart sip = desc.addSpinInputPart(paramDesc.get(BLOCKSIZE));
		sip.setVertical(false);
		sip.setRange(1, Integer.MAX_VALUE);
		
		TextInputPart tip = desc.addTextInputPart(paramDesc.get(ORIGIN));
		tip.setAllowEmpty(true);
		tip.setVertical(false);
		
		desc.addCheckboxPart(paramDesc.get(MARKASMT));
		
		desc.addSeparatorPart();
		
		CheckboxPart cbp = desc.addCheckboxPart(paramDesc.get(SEGMENT));
		PathInputPart pip = desc.addPathInputPart(paramDesc.get(SRXPATH), "SRX Path", false);
		pip.setBrowseFilters("SRX Documents (*.srx)\tAll Files (*.*)", "*.srx\t*.*");
		pip.setMasterPart(cbp, true);
		pip.setWithLabel(false);

		cbp = desc.addCheckboxPart(paramDesc.get(MAKETM));
		FolderInputPart fip = desc.addFolderInputPart(paramDesc.get(TMDIRECTORY), "TM Directory");
		fip.setMasterPart(cbp, true);
		fip.setWithLabel(false);

		cbp = desc.addCheckboxPart(paramDesc.get(MAKETMX));
		pip = desc.addPathInputPart(paramDesc.get(TMXPATH), "TMX Path", true);
		pip.setBrowseFilters("TMX Documents (*.tmx)\tAll Files (*.*)", "*.tmx\t*.*");
		pip.setMasterPart(cbp, true);
		pip.setWithLabel(false);
		CheckboxPart cbp2 = desc.addCheckboxPart(paramDesc.get(SENDTMX));
		cbp2.setMasterPart(cbp, true);
		
		cbp = desc.addCheckboxPart(paramDesc.get(CHECKEXISTINGTM));
		fip = desc.addFolderInputPart(paramDesc.get(EXISTINGTM), "TM Directory");
		fip.setMasterPart(cbp, true);
		fip.setWithLabel(false);
		
		return desc;
	}

}
