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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;

/**
 * The default part handler for OpenXMLContentFilter-based processing.
 */
public class StandardPartHandler extends ContentFilterBasedPartHandler {

	public StandardPartHandler(OpenXMLContentFilter contentFilter, ConditionalParameters cparams,
				OpenXMLZipFile zipFile, ZipEntry entry) {
		super(contentFilter, cparams, zipFile, entry);
	}

	@Override
	public Event open(String documentId, String subDocumentId, LocaleId srcLang) throws IOException {
		InputStream isInputStream = new BufferedInputStream(zipFile.getInputStream(entry));
		return openContentFilter(isInputStream, documentId, subDocumentId, srcLang);
	}

	@Override
	public Event next() {
		Event e = super.next();
		if (e.isEndDocument() && !params.getTranslateWordHidden()) {
			// Update the mined styles - this is hacky
			params.tsExcludeWordStyles.addAll(contentFilter.getTsExcludeWordStyles());
		}
		return e;
	}
}
