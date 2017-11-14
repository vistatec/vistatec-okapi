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

package net.sf.okapi.common.pipelinedriver;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.BaseContext;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;

/**
 * Default implementation of the {@link IBatchItemContext} interface.
 */
public class BatchItemContext extends BaseContext implements IBatchItemContext {

	private static final int INITIAL_CAPACITY = 2;
	
	private List<DocumentData> list;

	/**
	 * Creates a new empty BatchItemContext object.
	 */
	public BatchItemContext () {
		super();
		list = new ArrayList<DocumentData>(INITIAL_CAPACITY);
	}
	
	/**
	 * Creates a new BatchItemContext object and initializes it with a given
	 * {@link RawDocument} and additional arguments.
	 * @param rawDoc the {@link RawDocument} to use as the main input document.
	 * @param outputURI the output URI of the input document (can be null if not used).
	 * @param outputEncoding the output encoding (can be null if not used).
	 */
	public BatchItemContext (RawDocument rawDoc,
		URI outputURI,
		String outputEncoding)
	{
		this();
		add(rawDoc, outputURI, outputEncoding);
	}
	
	/**
	 * Creates a new BatchItemContext object and initializes it with a given
	 * {@link RawDocument} and additional arguments.
	 * @param rawDoc the {@link RawDocument} to use as the main input document.
	 * @param outputURI the output URI of the input document (can be null if not used).
	 * @param outputEncoding the output encoding (can be null if not used).
	 * @param rawDocs one or more RawDocuments to include in this item.
	 */
	public BatchItemContext (RawDocument rawDoc,
		URI outputURI,
		String outputEncoding,
		RawDocument... rawDocs)
	{
		this();
		add(rawDoc, outputURI, outputEncoding);
		for ( RawDocument rd : rawDocs ) {
			DocumentData ddi = new DocumentData();
			ddi.rawDocument = rd;
			list.add(ddi);
		}
	}
	
	/**
	 * Creates a new BatchItemContext object and initializes it based on a given
	 * input URI and additional arguments.
	 * @param inputURI the URI of the main input document
	 * @param defaultEncoding the default encoding of the input document.
	 * @param filterConfigId the filter configuration ID (can be null if not used)
	 * @param outputURI the output URI (can be null if not used).
	 * @param outputEncoding the output encoding (can be null if not used)
	 * @param sourceLocale the source locale.
	 * @param targetLocale the target locale.
	 */
	public BatchItemContext (URI inputURI,
		String defaultEncoding,
		String filterConfigId,
		URI outputURI,
		String outputEncoding,
		LocaleId sourceLocale,
		LocaleId targetLocale)
	{
		this();
		DocumentData ddi = new DocumentData();
		ddi.rawDocument = new RawDocument(inputURI, defaultEncoding, sourceLocale, targetLocale);
		ddi.rawDocument.setFilterConfigId(filterConfigId);
		ddi.outputURI = outputURI;
		ddi.outputEncoding = outputEncoding;
		list.add(ddi);
	}
	
	/**
	 * Adds a document to the list of inputs for this batch item.
	 * @param data the data of the document.
	 */
	public void add (DocumentData data) {
		list.add(data);
	}
	
	/**
	 * Adds a document to the list of inputs for this batch item 
	 * using the provided arguments. 
	 * @param rawDoc the {@link RawDocument} to use as the main input document.
	 * @param outputURI the output URI of the input document (can be null if not used).
	 * @param outputEncoding the output encoding (can be null if not used).
	 */
	public void add (RawDocument rawDoc,
		URI outputURI,
		String outputEncoding)
	{
		DocumentData dd = new DocumentData();
		dd.rawDocument = rawDoc;
		dd.outputURI = outputURI;
		dd.outputEncoding = outputEncoding;
		list.add(dd);
	}
		
	public String getFilterConfigurationId (int index) {
		if ( list.size() <= index ) return null;
		return list.get(index).rawDocument.getFilterConfigId();
	}
	
	public String getOutputEncoding (int index) {
		if ( list.size() <= index ) return null;
		return list.get(index).outputEncoding;
	}
	
	public URI getOutputURI (int index) {
		if ( list.size() <= index ) return null;
		return list.get(index).outputURI;
	}
	
	public RawDocument getRawDocument (int index) {
		if ( list.size() <= index ) return null;
		return list.get(index).rawDocument;
	}
	
	public LocaleId getSourceLocale (int index) {
		if ( list.size() <= index ) return null;
		return list.get(index).rawDocument.getSourceLocale();
	}

	public LocaleId getTargetLocale (int index) {
		if ( list.size() <= index ) return null;
		return list.get(index).rawDocument.getTargetLocale();
	}

}
