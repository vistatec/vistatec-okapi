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

package net.sf.okapi.common.filterwriter;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;

/**
 * Provides a way to access the parameters for {@link net.sf.okapi.common.filterwriter.XLIFFWriter XLIFFWriter} in a single object.
 */
public class XLIFFWriterParameters extends StringParameters {
	
	private static final String USESOURCEFORTRANSLATED = "useSourceForTranslated";
	private static final String ESCAPEGT = "escapeGt";
	private static final String PLACEHOLDERMODE = "placeholderMode";
	private static final String INCLUDENOTRANSLATE = "includeNoTranslate";
	private static final String SETAPPROVEDASNOTRANSLATE = "setApprovedAsNoTranslate";
	private static final String COPYSOURCE = "copySource";
	private static final String INCLUDEALTTRANS = "includeAltTrans";
	private static final String INCLUDECODEATTRS = "includeCodeAttrs";
	private static final String INCLUDEITS = "includeIts";
	
	// tool information
	private static final String TOOL_ID = "toolId";
	private static final String TOOL_NAME = "toolName";
	private static final String TOOL_VERSION = "toolVersion";
	private static final String TOOL_COMPANY = "toolCompany";
	
	// custom namespace xml snippet written in <tool>
	// Used for company specific metadata
	private static final String CUSTOM_TOOL_XML_SNIPPET = "customToolXmlSnippet";

	public XLIFFWriterParameters () {
		super();
	}

	public boolean getUseSourceForTranslated () {
		return getBoolean(USESOURCEFORTRANSLATED);
	}

	/**
	 * Sets the flag indicating if the source text is used in the target, even if
	 * a target is available.
	 * <p>This is for the tools where we trust the target will be obtained by the tool
	 * from the TMX from original. This is to allow editing of pre-translated items in XLIFF
	 * editors that use directly the &lt;target&gt; element.
	 * @param useSourceForTranslated true to use the source in the target even if a target text
	 * is available.
	 */
	public void setUseSourceForTranslated (boolean useSourceForTranslated) {
		setBoolean(USESOURCEFORTRANSLATED, useSourceForTranslated);
	}

	public boolean getEscapeGt () {
		return getBoolean(ESCAPEGT);
	}

	/**
	 * Sets the flag indicating if '&gt;' should be escaped or not.
	 * @param escapeGt true to always escape '&gt;', false to not escape it.
	 */
	public void setEscapeGt (boolean escapeGt) {
		setBoolean(ESCAPEGT, escapeGt);
	}

	/**
	 * Gets the flag indicating if the inline code should use the place-holder notation (g and x elements).
	 * @return true if the inline code should use the place-holder notation.
	 */
	public boolean getPlaceholderMode () {
		return getBoolean(PLACEHOLDERMODE);
	}

	/**
	 * Sets the flag indicating if the inline code should use the place-holder notation (g and x elements).
	 * @param placeholderMode true if the inline code should use the place-holder notation.
	 */
	public void setPlaceholderMode(boolean placeholderMode) {
		setBoolean(PLACEHOLDERMODE, placeholderMode);
	}

	public boolean getIncludeNoTranslate () {
		return getBoolean(INCLUDENOTRANSLATE);
	}

	/**
	 * Sets the flag indicating if non-translatable text units should be output or not.
	 * @param includeNoTranslate true to include non-translatable text unit in the output.
	 */
	public void setIncludeNoTranslate (boolean includeNoTranslate) {
		setBoolean(INCLUDENOTRANSLATE, includeNoTranslate);
	}

	public boolean getSetApprovedAsNoTranslate () {
		return getBoolean(SETAPPROVEDASNOTRANSLATE);
	}

	/**
	 * Sets the flag indicating to mark as not translatable all entries that are approved.
	 * @param setApprovedAsNoTranslate true to mark approved entries as not translatable.
	 */
	public void setSetApprovedAsNoTranslate(boolean setApprovedAsNoTranslate) {
		setBoolean(SETAPPROVEDASNOTRANSLATE, setApprovedAsNoTranslate);
	}

	public boolean getCopySource () {
		return getBoolean(COPYSOURCE);
	}

	/**
	 * Sets the copySource flag indicating to copy the source at the target spot if there is no target defined.
	 * @param copySource true to copy the source at the target spot if there is no target defined.
	 */
	public void setCopySource (boolean copySource) {
		setBoolean(COPYSOURCE, copySource);
	}

	public boolean getIncludeAltTrans () {
		return getBoolean(INCLUDEALTTRANS);
	}

	/**
	 * Sets the flag indicating if alt-trans elements should be output or not.
	 * @param includeAltTrans true to include alt-trans element in the output.
	 */
	public void setIncludeAltTrans (boolean includeAltTrans) {
		setBoolean(INCLUDEALTTRANS, includeAltTrans);
	}

	public boolean getIncludeCodeAttrs () {
		return getBoolean(INCLUDECODEATTRS);
	}

	/**
	 * Sets the flag indicating if extended code attributes should be output or not.
	 * @param includeCodeAttrs true to include extended code attributes in the output.
	 */
	public void setIncludeCodeAttrs(boolean includeCodeAttrs) {
		setBoolean(INCLUDECODEATTRS, includeCodeAttrs);
	}

	/**
	 * Gets the flag indicating if ITS markup should be output or not.
	 * @return true if ITS markup should be output, false otherwise.
	 */
	public boolean getIncludeIts () {
		return getBoolean(INCLUDEITS);
	}

	/**
	 * Sets the flag indicating if ITS markup should be output or not.
	 * @param includeIts true to include ITS markup in the output.
	 */
	public void setIncludeIts (boolean includeIts) {
		setBoolean(INCLUDEITS, includeIts);
	}
	
	public String getToolId () {
		return getString(TOOL_ID);
	}
	
	public void setToolId (String toolId) {
		setString(TOOL_ID, toolId);
	}
	
	public String getToolName () {
		return getString(TOOL_NAME);
	}
	
	public void setToolName (String toolName) {
		setString(TOOL_NAME, toolName);
	}
	
	public String getToolVersion () {
		return getString(TOOL_VERSION);
	}
	
	public void setToolVersion (String toolVersion) {
		setString(TOOL_VERSION, toolVersion);
	}
	
	public String getToolCompany () {
		return getString(TOOL_COMPANY);
	}
	
	public void setToolCompany (String toolCompany) {
		setString(TOOL_COMPANY, toolCompany);
	}
	
	public String getToolXmlSnippet () {
		return getString(CUSTOM_TOOL_XML_SNIPPET);
	}
	
	public void setToolXmlSnippet (String snippet) {
		setString(CUSTOM_TOOL_XML_SNIPPET, snippet);
	}

	@Override
	public void reset() {
		super.reset();
		setUseSourceForTranslated(false);
		setEscapeGt(false);
		setPlaceholderMode(true);
		setIncludeNoTranslate(true);
		setSetApprovedAsNoTranslate(false);
		setCopySource(true);
		setIncludeAltTrans(true);
		setIncludeCodeAttrs(false);
		setIncludeIts(true);
		
		setToolId(null);
		setToolName(null);
		setToolVersion(null);
		setToolCompany(null);
		
		setToolXmlSnippet(null);
	}

	@Override
	public ParametersDescription getParametersDescription() {
		ParametersDescription desc = new ParametersDescription(this);

		desc.add(USESOURCEFORTRANSLATED, "Use the source text in the target, even if a target is available", null);
		desc.add(ESCAPEGT, "Escape the greater-than characters as &gt;", null);
		desc.add(PLACEHOLDERMODE, "Inline code should use the place-holder notation (g and x elements)", null);
		desc.add(INCLUDENOTRANSLATE, "Output non-translatable text units", null);
		desc.add(SETAPPROVEDASNOTRANSLATE, "Mark as not translatable all entries that are approved", null);
		desc.add(COPYSOURCE, "Copy the source as target if there is no target defined", null);
		desc.add(INCLUDEALTTRANS, "Output alt-trans elements", null);
		desc.add(INCLUDECODEATTRS, "Output extended code attributes", null);
		desc.add(INCLUDEITS, "Output ITS markup", null);
		
		
		desc.add(TOOL_ID, 
				"Xliff Tool Id Value", 
				"Tool Id is used in the xliff tool element found in the header");
		desc.add(TOOL_NAME, 
				"Xliff Tool Name Value", 
				"Tool Name is used in the xliff tool element found in the header");
		desc.add(TOOL_VERSION, 
				"Xliff Tool Version Value", 
				"Tool Version is used in the xliff tool element found in the header");
		desc.add(TOOL_COMPANY, 
				"Xliff Tool Company Name Value", 
				"Tool Company Name is used in the xliff tool element found in the header");
		
		desc.add(CUSTOM_TOOL_XML_SNIPPET, 
				"Custom Tool XML Snippet", 
				"Custom Tool XML snippet containing company or tool specific metadata. "
				+ "Written as part of the Xliff tool element (tool-id and tool-name must be defined!). "
				+ "Must be well-formed XML with a valid namespace URI");

		return desc;
	}

}
