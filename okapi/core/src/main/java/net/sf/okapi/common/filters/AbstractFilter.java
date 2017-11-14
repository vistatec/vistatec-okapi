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

package net.sf.okapi.common.filters;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.BOMNewlineEncodingDetector.NewlineType;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Basic abstract implementation of {@link IFilter}.
 */
public abstract class AbstractFilter implements IFilter {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	List<FilterConfiguration> configList = new ArrayList<FilterConfiguration>();
	private IdGenerator documentId;
	private boolean canceled = false;
	private String documentName;
	private String newlineType;
	private String encoding = StandardCharsets.UTF_8.name();
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private String mimeType;
	private IFilterWriter filterWriter;
	private boolean generateSkeleton;
	private boolean multilingual;
	private String name;
	private String displayName;
	private EncoderManager encoderManager;
	private IFilterConfigurationMapper fcMapper;
	private String parentId;

	/**
	 * Default constructor
	 */
	public AbstractFilter() {
		// defaults
		setNewlineType("\n"); //$NON-NLS-1$
		setMultilingual(false);
		setFilterConfigurationMapper(new FilterConfigurationMapper());
		documentId = new IdGenerator(null, IdGenerator.START_DOCUMENT);
	}

	/**
	 * Each {@link IFilter} has a small set of options beyond normal configuration that gives the
	 * {@link IFilter} the needed information to properly parse the content.
	 * 
	 * @param sourceLocale
	 *            - source locale of the input document
	 * @param targetLocale
	 *            - target locale if the input document is multilingual.
	 * @param defaultEncoding
	 *            - assumed encoding of the input document. May be overriden if a different encoding
	 *            is detected.
	 * @param generateSkeleton
	 *            - store skeleton (non-translatable parts of the document) along with the extracted
	 *            text.
	 */
	public void setOptions(LocaleId sourceLocale, LocaleId targetLocale, String defaultEncoding,
			boolean generateSkeleton) {
		setEncoding(defaultEncoding);
		setTrgLoc(targetLocale);
		setSrcLoc(sourceLocale);
		setGenerateSkeleton(generateSkeleton);
	}

	/**
	 * create a START_DOCUMENT {@link Event}
	 * @return the newly created {@link StartDocument} event.
	 */
	protected Event createStartFilterEvent() {
		StartDocument startDocument = new StartDocument(
				documentId.createId(IdGenerator.START_DOCUMENT));
		startDocument.setEncoding(getEncoding(), isUtf8Encoding() && isUtf8Bom());
		startDocument.setLocale(getSrcLoc());
		startDocument.setMimeType(getMimeType());
		startDocument.setLineBreak(getNewlineType());
		startDocument.setFilterParameters(getParameters());
		startDocument.setFilterWriter(getFilterWriter());
		startDocument.setName(getDocumentName());
		startDocument.setMultilingual(isMultilingual());
		LOGGER.debug("Start Document for " + startDocument.getId()); //$NON-NLS-1$
		return new Event(EventType.START_DOCUMENT, startDocument);
	}

	/**
	 * create a END_DOCUMENT {@link Event}
	 * @return the newly created {@link Ending} event.
	 */
	protected Event createEndFilterEvent() {
		Ending endDocument = new Ending(documentId.getLastId());
		LOGGER.debug("End Document for " + endDocument.getId()); //$NON-NLS-1$
		return new Event(EventType.END_DOCUMENT, endDocument);
	}

	public boolean addConfigurations(List<FilterConfiguration> configs) {
		if (configList == null)
			return false;

		return configList.addAll(configs);
	}

	public FilterConfiguration getConfiguration(String configId) {
		if (Util.isEmpty(configList))
			return null;

		for (FilterConfiguration config : configList) {

			if (config == null)
				continue;
			if (config.configId.equalsIgnoreCase(configId))
				return config;
		}

		return null;
	}

	public boolean removeConfiguration(String configId) {
		return configList.remove(getConfiguration(configId));
	}

	@Override
	public List<FilterConfiguration> getConfigurations() {
		List<FilterConfiguration> configs = new ArrayList<FilterConfiguration>();

		for (FilterConfiguration fc : configList)
			configs.add(new FilterConfiguration(fc.configId, getMimeType(), getClass().getName(),
					fc.name, fc.description, fc.parametersLocation, fc.extensions));

		return configs;
	}

	@Override
	public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	@Override
	public EncoderManager getEncoderManager() {
		if (encoderManager == null) {
			encoderManager = new EncoderManager();
			// By default we set all known mapping.
			// It's up to each implementation to set up their own.
			encoderManager.setAllKnownMappings();
			encoderManager.setDefaultOptions(getParameters(), "UTF-8", getNewlineType());
		}
		return encoderManager;
	}

	public void addConfiguration(FilterConfiguration configuration) {
		configList.add(configuration);
	}

	@Override
	public void cancel() {
		canceled = true;
	}

	@Override
	public void close() {
	}

	@Override
	public void open(RawDocument input, boolean generateSkeleton) {
		// defaults
		setNewlineType("\n"); //$NON-NLS-1$
		setMultilingual(false);
		documentId = new IdGenerator(null, IdGenerator.START_DOCUMENT);
		this.parentId = null;
		this.canceled = false;
		this.multilingual = false;
		this.generateSkeleton = generateSkeleton;
	}

	/**
	 * Gets the filter configuration mapper if available. This mapper can be used to instantiate
	 * sub-filters based on filter configurations.
	 * 
	 * @return the filter configuration mapper.
	 */
	protected IFilterConfigurationMapper getFilterConfigurationMapper() {
		return fcMapper;
	}

	/**
	 * Allows implementers to set the START_DOCUMENT name for the current input.
	 * 
	 * @param documentName
	 *            the input document name or path
	 */
	protected void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	/**
	 * Gets the START_DOCUMENT name for the current input.
	 * 
	 * @return the document name or path of the current input.
	 */
	public String getDocumentName() {
		return documentName;
	}

	/**
	 * Get the newline type used in the input.
	 * 
	 * @return the {@link NewlineType} one of '\n', '\r' or '\r\n'
	 */
	public String getNewlineType() {
		return newlineType;
	}

	/**
	 * Sets the newline type.
	 * 
	 * @param newlineType
	 *            one of '\n', '\r' or '\r\n'.
	 */
	protected void setNewlineType(String newlineType) {
		this.newlineType = newlineType;
	}

	/**
	 * Gets the input document encoding.
	 * 
	 * @return the encoding
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Sets the input document encoding.
	 * 
	 * @param encoding
	 *            the new encoding
	 */
	protected void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	/**
	 * Gets the input document source locale.
	 * 
	 * @return the source locale
	 */
	public LocaleId getSrcLoc() {
		return srcLoc;
	}

	/**
	 * Sets the input document source locale.
	 * 
	 * @param srcLoc
	 *            the new source locale
	 */
	public void setSrcLoc(LocaleId srcLoc) {
		this.srcLoc = srcLoc;
	}

	/**
	 * @param trgLoc
	 *            the target locale to set
	 */
	public void setTrgLoc(LocaleId trgLoc) {
		this.trgLoc = trgLoc;
	}

	/**
	 * @return the trgLoc
	 */
	public LocaleId getTrgLoc() {
		return trgLoc;
	}

	/**
	 * Gets the input document mime type.
	 * 
	 * @return the mime type
	 */
	@Override
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Sets the input document mime type.
	 * 
	 * @param mimeType
	 *            the new mime type
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Checks if the {@link IFilter} has been canceled.
	 * 
	 * @return true, if is canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * Gets the filter writer for this filter.
	 * 
	 * @return the filter writer.
	 */
	public IFilterWriter getFilterWriter() {
		return filterWriter;
	}

	/**
	 * Sets the filter writer for this filter.
	 * 
	 * @param filterWriter
	 *            the filter writer to set.
	 */
	public void setFilterWriter(IFilterWriter filterWriter) {
		this.filterWriter = filterWriter;
	}

	@Override
	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	@Override
	public IFilterWriter createFilterWriter() {
		if (filterWriter != null) {
			return filterWriter;
		}
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	/**
	 * @param generateSkeleton
	 *            the generateSkeleton to set
	 */
	protected void setGenerateSkeleton(boolean generateSkeleton) {
		this.generateSkeleton = generateSkeleton;
	}

	/**
	 * @return the generateSkeleton
	 */
	public boolean isGenerateSkeleton() {
		return generateSkeleton;
	}

	/**
	 * Is the input encoded as UTF-8?
	 * 
	 * @return true if the document is in utf8 encoding.
	 */
	abstract protected boolean isUtf8Encoding();

	/**
	 * Does the input have a UTF-8 Byte Order Mark?
	 * 
	 * @return true if the document has a utf-8 byte order mark.
	 */
	abstract protected boolean isUtf8Bom();

	/**
	 * @param multilingual
	 *            the multilingual to set
	 */
	protected void setMultilingual(boolean multilingual) {
		this.multilingual = multilingual;
	}

	/**
	 * @return the multilingual
	 */
	public boolean isMultilingual() {
		return multilingual;
	}

	protected void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	protected void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public IdGenerator getDocumentId() {
		return documentId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	
}
