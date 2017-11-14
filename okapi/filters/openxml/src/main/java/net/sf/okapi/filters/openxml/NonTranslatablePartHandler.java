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

import java.io.IOException;
import java.util.zip.ZipEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.skeleton.ZipSkeleton;

import javax.xml.stream.XMLStreamException;

public class NonTranslatablePartHandler implements OpenXMLPartHandler {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private OpenXMLZipFile zipFile;
	private ZipEntry entry;

	NonTranslatablePartHandler(OpenXMLZipFile zipFile, ZipEntry entry) {
		this.zipFile = zipFile;
		this.entry = entry;
	}

	protected OpenXMLZipFile getZipFile() {
		return zipFile;
	}

	protected ZipEntry getEntry() {
		return entry;
	}

	@Override
	public Event open(String documentId, String subDocumentId, LocaleId srcLang) throws IOException, XMLStreamException {
		DocumentPart dp = new DocumentPart(entry.getName(), false);
		ZipSkeleton skel = new ZipSkeleton(zipFile.getZip(), entry);
		skel.setModifiedContents(getModifiedContent());
		return new Event(EventType.DOCUMENT_PART, dp, skel);
	}

	/**
	 * For subclasses that need to rewrite the content, despite it not
	 * being translatable.  The default behavior returns null, which
	 * means that the original content will be copied to the target.
	 * @return the modified content.
	 */
	protected String getModifiedContent() {
		return null;
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Event next() {
		throw new IllegalStateException();
	}

	@Override
	public void close() {
	}

	@Override
	public void logEvent(Event e) {
		LOGGER.trace("[[ " + getClass().getSimpleName() + ": " + entry.getName() + " ]]");
	}

}
