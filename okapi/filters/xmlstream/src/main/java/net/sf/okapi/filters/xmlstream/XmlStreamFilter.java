/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xmlstream;

import java.io.File;
import java.net.URL;

import net.htmlparser.jericho.EndTag;
import net.htmlparser.jericho.StartTag;
import net.htmlparser.jericho.Tag;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.filters.abstractmarkup.AbstractMarkupEventBuilder;
import net.sf.okapi.filters.abstractmarkup.AbstractMarkupFilter;
import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration;

@UsingParameters(Parameters.class)
public class XmlStreamFilter extends AbstractMarkupFilter {

	private Parameters parameters;
	
	public XmlStreamFilter() {
		super();			
		setMimeType(MimeTypeMapper.XML_MIME_TYPE);
		setFilterWriter(createFilterWriter());
		setParameters(new Parameters());
		setName("okf_xmlstream"); //$NON-NLS-1$
		setDisplayName("XML Stream Filter"); //$NON-NLS-1$
		addConfiguration(new FilterConfiguration(getName(), MimeTypeMapper.XML_MIME_TYPE,
				getClass().getName(), "XML Stream", "Large XML Documents", //$NON-NLS-1$
				Parameters.DEFAULT_PARAMETERS));
		addConfiguration(new FilterConfiguration(getName()+"-dita", MimeTypeMapper.XML_MIME_TYPE,
				getClass().getName(), "DITA", "DITA XML", //$NON-NLS-1$
				Parameters.DITA_PARAMETERS, ".dita;.ditamap;"));
		addConfiguration(new FilterConfiguration(getName()+"-JavaPropertiesHTML", MimeTypeMapper.XML_MIME_TYPE,
				getClass().getName(), "Java Properties XML + HTML", "Java Properties XML with Embedded HTML", //$NON-NLS-1$
				Parameters.PROPERTY_XML_PARAMETERS));
	}

	/**
	 * Initialize rule state and parser. Called before processing of each input.
	 */
	@Override
	protected void startFilter() {
		super.startFilter();
		getEventBuilder().initializeCodeFinder(getConfig().isUseCodeFinder(), 
				getConfig().getCodeFinderRules());
	}

	@Override
	protected void handleStartTag(StartTag startTag) {
		super.handleStartTag(startTag);
	}

	@Override
	protected void handleEndTag(EndTag endTag) {
		super.handleEndTag(endTag);
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.markupfilter.BaseMarkupFilter#normalizeName(java. lang.String)
	 */
	@Override
	protected String normalizeAttributeName(String attrName, String attrValue, Tag tag) {
		// normalize values for XML
		String normalizedName = attrName;

		// <x xml:lang="en">
		if (attrName.equalsIgnoreCase("xml:lang")) {
			normalizedName = Property.LANGUAGE;
		}

		return normalizedName;
	}

	@Override
	protected TaggedFilterConfiguration getConfig() {
		return parameters.getTaggedConfig();
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.filters.IFilter#setParameters(net.sf.okapi.common .IParameters)
	 */
	public void setParameters(IParameters params) {
		this.parameters = (Parameters) params;
	}

	/*
	 * (non-Javadoc)
	 * @see net.sf.okapi.common.filters.IFilter#getParameters()
	 */
	public IParameters getParameters() {
		return parameters;
	}

	/**
	 * Initialize filter parameters from a URL.
	 * 
	 * @param config
	 */
	public void setParametersFromURL(URL config) {
		parameters = new Parameters(config);
	}

	/**
	 * Initialize filter parameters from a Java File.
	 * 
	 * @param config
	 */
	public void setParametersFromFile(File config) {
		parameters = new Parameters(config);
	}

	/**
	 * Initialize filter parameters from a String.
	 * 
	 * @param config
	 */
	public void setParametersFromString(String config) {
		parameters = new Parameters(config);
	}
	
	/**
	 * @return the {@link AbstractMarkupEventBuilder}
	 */
	public AbstractMarkupEventBuilder getEventBuilder() {
		return (AbstractMarkupEventBuilder)super.getEventBuilder();
	}
}
