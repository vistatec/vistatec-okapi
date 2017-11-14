/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ZipSkeleton;

/**
 * Base class for part handlers that wrap an OpenXMLContentFilter.
 */
abstract class ContentFilterBasedPartHandler extends GenericPartHandler {
	protected OpenXMLContentFilter contentFilter;

	ContentFilterBasedPartHandler(OpenXMLContentFilter contentFilter, ConditionalParameters cparams,
				OpenXMLZipFile zipFile, ZipEntry entry) {
		super(cparams, entry.getName());
		this.zipFile = zipFile;
		this.entry = entry;
		this.contentFilter = contentFilter;
	}

	@Override
	public boolean hasNext() {
		return contentFilter.hasNext();
	}

	@Override
	public Event next() {
		return contentFilter.next();
	}

	@Override
	public void close() {
		contentFilter.close();
	}

	/**
	 * Open the nested {@link OpenXMLContentFilter} instance on the specified InputStream,
	 * and convert a START_SUBDOCUMENT event for it.
	 * @param is input stream
	 * @param documentId document identifier
	 * @param subDocumentId sub-document identifier
	 * @param srcLang the source language
	 * @return the START_SUBDOCUMENT Event
	 */
	protected Event openContentFilter(InputStream is, String documentId, String subDocumentId, LocaleId srcLang) {
		contentFilter.open(new RawDocument(is, StandardCharsets.UTF_8.name(), srcLang));
		Event startDocEvent = contentFilter.next();
		// Change the START_DOCUMENT event to START_SUBDOCUMENT
		StartSubDocument sd = new StartSubDocument(documentId, subDocumentId);
		sd.setName(entry.getName());
		ConditionalParameters clonedParams = params.clone();
		clonedParams.nFileType = contentFilter.getParseType();
		sd.setFilterParameters(clonedParams);
		ZipSkeleton skel = new ZipSkeleton((GenericSkeleton)startDocEvent.getStartDocument().getSkeleton(),
										   zipFile.getZip(), entry);
		return new Event(EventType.START_SUBDOCUMENT, sd, skel);
	}

	@Override
	public void logEvent(Event e) {
		contentFilter.displayOneEvent(e);
	}
}
