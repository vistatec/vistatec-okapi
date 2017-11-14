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

package net.sf.okapi.filters.archive;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ISimplifierRulesParameters;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider, ISimplifierRulesParameters {

	/**
	 * MIME type of the filter's container format
	 */
	private static final String MIMETYPE = "mimeType";

	/**
	 * Comma-delimited list of file names (masks with ? and * wildcards are allowed). Elements of the list correspond to elements in configIds. 
	 * If the container includes a file which name fits one of the masks or a filename, the corresponding config Id is looked up in 
	 * the fileExtensions string, and the container's filter instantiates a sub-filter to process that internal file.
	 * <p> If the container includes several files with the same name located in different internal ZIP folders, all those files will be processed;
	 * if you want to process only some of them, prefix those file names with path info.
	 * <p> If fileNames is empty, then no contained files are processed, and all content is sent as document part events.  
	 * <p> Examples of fileNames:
	 * <p> document.xml, styles.xml, *notes.xml, word/fontTable.xml, word/theme/theme?.xml  
	 */
	private static final String FILENAMES = "fileNames";

	/**
	 * Comma-delimited list of configuration Ids corresponding to the extension
	 */
	private static final String CONFIGIDS = "configIds";
		
	public Parameters () {
		super();
	}
	
	@Override
	public void reset() {
		super.reset();
		setMimeType(ArchiveFilter.MIME_TYPE);
		setFileNames("*.tmx,*.xlf,*.xlff");
		setConfigIds("okf_tmx,okf_xliff,okf_xliff");
		setSimplifierRules(null);
	}
		
	public void setFileNames(String fileNames) {
		setString(FILENAMES, fileNames);
	}

	public String getFileNames() {
		return getString(FILENAMES);
	}

	public void setConfigIds(String configIds) {
		setString(CONFIGIDS, configIds);
	}

	public String getConfigIds() {
		return getString(CONFIGIDS);
	}

	public void setMimeType(String mimeType) {
		setString(MIMETYPE, mimeType);
	}

	public String getMimeType() {
		return getString(MIMETYPE);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add("mimeType", "MIME type of the filter's container format", null);
		desc.add("fileNames", "File names", "Comma-delimited list of file names to be processed (wildcards are allowed) in the same order as configuration ids");
		desc.add("configIds", "Filter configuration ids", "Comma-delimited list of configuration ids corresponding to the file names");
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription(
			ParametersDescription parametersDescription) {
		EditorDescription desc = new EditorDescription("Archive Filter Parameters", true, false);
		
		desc.addTextInputPart(parametersDescription.get("mimeType"));
		desc.addTextInputPart(parametersDescription.get("fileNames"));
		desc.addTextInputPart(parametersDescription.get("configIds"));
		
		return desc;
	}

	@Override
	public String getSimplifierRules() {
		return getString(SIMPLIFIERRULES);
	}

	@Override
	public void setSimplifierRules(String rules) {
		setString(SIMPLIFIERRULES, rules);		
	}

	@Override
	public void validateSimplifierRules() throws ParseException {
		SimplifierRules r = new SimplifierRules(getSimplifierRules(), new Code());
		r.parse();
	}
}