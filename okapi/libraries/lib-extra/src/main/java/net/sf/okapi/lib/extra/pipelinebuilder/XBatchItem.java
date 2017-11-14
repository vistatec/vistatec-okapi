/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.extra.pipelinebuilder;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.DocumentData;
import net.sf.okapi.common.pipelinedriver.IBatchItemContext;
import net.sf.okapi.common.resource.RawDocument;

public class XBatchItem  {

	private BatchItemContext bic = new BatchItemContext();
//	private List<XDocument> documents = new ArrayList<XDocument>();

	public XBatchItem(XDocument... documents) {
		for (XDocument document : documents)
			addDocument(document);
	}

	private void addDocument(XDocument document) {
		if (document == null) return;		
		bic.add(document.getDocumentData());
	}
	
	public XBatchItem(URI inputURI, String defaultEncoding, String filterConfigId,
			URI outputURI, String outputEncoding, LocaleId sourceLocale, LocaleId targetLocale) {
		addDocument(new XDocument(inputURI, defaultEncoding, filterConfigId,
			outputURI, outputEncoding, sourceLocale, targetLocale));		
	}
	
	public XBatchItem(RawDocument rawDocument) {
		addDocument(new XDocument(rawDocument));
	}
	
	public XBatchItem(DocumentData documentData) {
		addDocument(new XDocument(documentData));
	}
	
	public XBatchItem(RawDocument rawDoc,
			URI outputURI,
			String outputEncoding) {
		addDocument(new XDocument(rawDoc, outputURI, outputEncoding));
	}
	
	public XBatchItem(CharSequence inputCharSequence, LocaleId sourceLocale) {
		addDocument(new XDocument(inputCharSequence, sourceLocale));
	}
	
	public XBatchItem(URI inputURI, String defaultEncoding, LocaleId sourceLocale) {
		addDocument(new XDocument(inputURI, defaultEncoding, sourceLocale));
	}

	public XBatchItem(URI inputURI, String defaultEncoding, URI outputURI, String outputEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		addDocument(new XDocument(inputURI, defaultEncoding, outputURI, outputEncoding, sourceLocale,
				targetLocale));
	}
	
	public XBatchItem(URI inputURI, String defaultEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		addDocument(new XDocument(inputURI, defaultEncoding, sourceLocale, targetLocale));
	}
	
	public XBatchItem(URL inputURL, String defaultEncoding, LocaleId sourceLocale) {
		addDocument(new XDocument(inputURL, defaultEncoding, sourceLocale));
	}

	public XBatchItem(URL inputURL, String defaultEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		addDocument(new XDocument(inputURL, defaultEncoding, sourceLocale, targetLocale));
	}
	
	public XBatchItem(URL inputURL, String defaultEncoding, URL outputURL, String outputEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		addDocument(new XDocument(inputURL, defaultEncoding, outputURL, outputEncoding, sourceLocale,
				targetLocale));
	}
	
	public XBatchItem(URL inputURL, String defaultEncoding, String outputPath, String outputEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		addDocument(new XDocument(inputURL, defaultEncoding, outputPath, outputEncoding, sourceLocale,
				targetLocale));
	}
	
	public XBatchItem(InputStream inputStream, String defaultEncoding, LocaleId sourceLocale) {
		addDocument(new XDocument(inputStream, defaultEncoding, sourceLocale));
	}
	
	public XBatchItem(InputStream inputStream, String defaultEncoding, String outputPath, String outputEncoding, LocaleId sourceLocale, LocaleId targetLocale) {
		addDocument(new XDocument(inputStream, defaultEncoding, outputPath, outputEncoding, sourceLocale, targetLocale));
	}
	
	public XBatchItem(InputStream inputStream, String defaultEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		addDocument(new XDocument(inputStream, defaultEncoding, sourceLocale,
				targetLocale));
	}
	
	public IBatchItemContext getContext() {
		return bic;
	}
}
