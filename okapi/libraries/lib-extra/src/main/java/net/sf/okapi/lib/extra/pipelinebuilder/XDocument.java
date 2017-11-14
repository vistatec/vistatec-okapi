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

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipelinedriver.DocumentData;
import net.sf.okapi.common.resource.RawDocument;

public class XDocument  {

	private DocumentData documentData;
	private FilterConfigurationMapper fcMapper;
		
	{
		fcMapper = new FilterConfigurationMapper();
	
		// Used in XLIFFKitReaderTest
		// TODO Registration of filter configs in the FilterConfigurationMapper, not here
		fcMapper.addConfigurations("net.sf.okapi.filters.xml.XMLFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.openoffice.OpenOfficeFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.openxml.OpenXMLFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.properties.PropertiesFilter");						
		fcMapper.addConfigurations("net.sf.okapi.filters.plaintext.PlainTextFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.table.TableFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.xliff.XLIFFFilter");
	}
	
	public XDocument(URI inputURI, String defaultEncoding, String filterConfigId,
			URI outputURI, String outputEncoding, LocaleId sourceLocale, LocaleId targetLocale) {
		this(new RawDocument(inputURI, defaultEncoding, sourceLocale,	targetLocale), outputURI, outputEncoding);
		getRawDocument().setFilterConfigId(filterConfigId);
	}
	
	public XDocument(URI inputURI,
			String defaultEncoding,
			LocaleId sourceLocale,
			LocaleId targetLocale,
			String filterConfigId) {
		this(new RawDocument(inputURI, defaultEncoding, sourceLocale, targetLocale));
		getRawDocument().setFilterConfigId(filterConfigId);
	}
	
	public XDocument(RawDocument rawDocument) {
		setRawDocument(rawDocument);
	}
	
	public XDocument(DocumentData documentData) {
		setDocumentData(documentData);
	}
	
	public XDocument(RawDocument rawDoc,
			URI outputURI,
			String outputEncoding) {
		setRawDocument(rawDoc);
		documentData.outputURI = outputURI;
		documentData.outputEncoding = outputEncoding;
	}
	
	public XDocument(CharSequence inputCharSequence, LocaleId sourceLocale) {
		setRawDocument(new RawDocument(inputCharSequence, sourceLocale));
	}
	
	public XDocument(URI inputURI, String defaultEncoding, LocaleId sourceLocale) {
		setRawDocument(new RawDocument(inputURI, defaultEncoding, sourceLocale));
	}

	public XDocument(URI inputURI, String defaultEncoding, URI outputURI, String outputEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		this(inputURI, defaultEncoding, null, outputURI, outputEncoding, sourceLocale, targetLocale);
	}
	
	public XDocument(URI inputURI, String defaultEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		setRawDocument(new RawDocument(inputURI, defaultEncoding, sourceLocale,	targetLocale));
	}
	
	public XDocument(URL inputURL, String defaultEncoding, LocaleId sourceLocale) {
		setRawDocument(new RawDocument(Util.URLtoURI(inputURL), defaultEncoding, sourceLocale));
	}

	public XDocument(URL inputURL, String defaultEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		setRawDocument(new RawDocument(Util.URLtoURI(inputURL), defaultEncoding, sourceLocale,	targetLocale));
	}
	
	public XDocument(URL inputURL, String defaultEncoding, URL outputURL, String outputEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		setRawDocument(new RawDocument(Util.URLtoURI(inputURL), defaultEncoding, sourceLocale,	targetLocale));
		documentData.outputURI = (outputURL == null) ? null : Util.URLtoURI(outputURL);
		documentData.outputEncoding = outputEncoding;
	}
	
	public XDocument(URL inputURL, String defaultEncoding, String outputPath, String outputEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		setRawDocument(new RawDocument(Util.URLtoURI(inputURL), defaultEncoding, sourceLocale,	targetLocale));
		documentData.outputURI = new File(outputPath).toURI();
		documentData.outputEncoding = outputEncoding;			
	}
	
	public XDocument(InputStream inputStream, String defaultEncoding, LocaleId sourceLocale) {
		setRawDocument(new RawDocument(inputStream, defaultEncoding, sourceLocale));
	}
	
	public XDocument(InputStream inputStream, String defaultEncoding, String outputPath, String outputEncoding, LocaleId sourceLocale, LocaleId targetLocale) {
		setRawDocument(new RawDocument(inputStream, defaultEncoding, sourceLocale, targetLocale));
		documentData.outputURI = Util.toURI(outputPath);
		documentData.outputEncoding = outputEncoding;
	}
	
	public XDocument(InputStream inputStream, String defaultEncoding, LocaleId sourceLocale,
			LocaleId targetLocale) {
		setRawDocument(new RawDocument(inputStream, defaultEncoding, sourceLocale, targetLocale));
	}
	
	protected XDocument() {
		super();
	}

	public DocumentData getDocumentData() {
		return documentData;
	}

	public void setDocumentData(DocumentData documentData) {
		this.documentData = documentData;
		validateFilterConfigId();
	}

	public RawDocument getRawDocument() {
		return (documentData != null) ? documentData.rawDocument : null;
	}

	public void setRawDocument(RawDocument rawDocument) {
		if (documentData == null)
			documentData = new DocumentData();
		
		documentData.rawDocument = rawDocument;
		validateFilterConfigId();
	}
	
	private void validateFilterConfigId() {
		RawDocument rd = getRawDocument();
		if (rd == null) return;
		if (!Util.isEmpty(rd.getFilterConfigId())) return; // Already set
		if (rd.getInputURI() == null) return; 
		
		String ext = Util.getExtension(rd.getInputURI().toString());
		if (Util.isEmpty(ext)) return;
		
		ext = ext.substring(1); // Exclude leading dot
		String mimeType = MimeTypeMapper.getMimeType(ext);
		
		FilterConfiguration cfg = fcMapper.getDefaultConfiguration(mimeType);
		if (cfg == null) return;
		
		rd.setFilterConfigId(cfg.configId);
	}
	
}
