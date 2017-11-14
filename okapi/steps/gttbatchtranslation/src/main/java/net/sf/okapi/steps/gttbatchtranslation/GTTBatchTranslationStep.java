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

package net.sf.okapi.steps.gttbatchtranslation;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IWaitDialog;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.gtt.GTTClient;
import net.sf.okapi.lib.translation.QueryUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class GTTBatchTranslationStep extends BasePipelineStep {

	private final static long MAXSIZE =(1020*1024); // Less than 1 MB 
		
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Parameters params;
	private QueryUtil qutil;
	private XMLWriter htmlWriter;
	private TMXWriter tmxWriter;
	private ArrayList<File> blocks;
	private GTTClient gtt;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private String tmId;
	private String rootDir;
	private int subDocId;
	private Map<String, String> attributes;
	private IWaitDialog waitDlg;
	private long count;
	private CharsetEncoder encoder;

	public GTTBatchTranslationStep () {
		params = new Parameters();
	}
	
	private void closeAndClean () {
		if ( tmxWriter != null ) {
			tmxWriter.writeEndDocument();
			tmxWriter.close();
			tmxWriter = null;
		}
		if (( gtt != null ) && ( tmId != null )) {
			gtt.deleteTM(tmId);
		}
	}
	
	@Override
	public String getDescription () {
		return "Creates a TM from the input documents using the Google Translator Toolkit."
			+ " Expects: filter events. Sends back: filter events.";
	}

	@Override
	public String getName () {
		return "GTT Batch Translation";
	}

	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale (LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}
	
	@Override
	public IParameters getParameters () {
		return params;
	}
	
	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}
	
	@Override
	protected Event handleStartBatch (Event event) {
		blocks = new ArrayList<File>();
		qutil = new QueryUtil();

		try {
			waitDlg = (IWaitDialog)Class.forName(params.getWaitClass()).newInstance();
		}
		catch ( Throwable e ) {
			throw new OkapiException("Could not create waiting dialog.");
		}

		gtt = new GTTClient(params.getEmail()+"-GTTBTrans-1");
		gtt.setCredentials(params.getEmail(), params.getPassword());
		gtt.setLanguages(sourceLocale, targetLocale);
		
		// Create a temporary empty TM
		tmId = gtt.createTM("tempTM");
		if ( tmId == null ) {
			throw new OkapiException("Could not create the initial temporary TM on GTT.");
		}
		
		// Create the TMX output
		String tmxOutputPath = Util.fillRootDirectoryVariable(params.getTmxPath(), rootDir);
		tmxOutputPath = LocaleId.replaceVariables(tmxOutputPath, sourceLocale, targetLocale);
		tmxWriter = new TMXWriter(tmxOutputPath);
		tmxWriter.writeStartDocument(sourceLocale, targetLocale, getClass().getCanonicalName(),
			"1", "sentence", null, "unknown");
		
		attributes = new Hashtable<String, String>();
		if ( params.getMarkAsMT() ) {
			attributes.put("creationid", Util.MTFLAG);
		}
		attributes.put("Txt::Origin", "Google-GTT");
	
		// Create the encoder to use to compute the file size 
		encoder = Charset.forName("UTF-8").newEncoder();
		
		return event;
	}

	@Override
	protected Event handleEndBatch (Event event) {
		// Finish to process all remaining blocks
		if ( !blocks.isEmpty() ) {
			processExtractionBlocks();
		}
		
		// Close and clean things
		closeAndClean();
		
		return event;
	}
	
	@Override
	protected Event handleStartDocument (Event event) {
		subDocId = 0;
		startExtractedBlock();
		return event;
	}
	
	@Override
	protected Event handleEndDocument (Event event) {
		endExtractedBlock();
		return event;
	}
	
	@Override
	protected Event handleStartSubDocument (Event event) {
		subDocId++;
		return event;
	}
	
	@Override
	protected Event handleEndSubDocument (Event event) {
		return event;
	}

	@Override
	protected Event handleTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();
		// Skip non-translatable entries
		if ( !tu.isTranslatable() ) return event;
	
		ISegments segs = tu.getSourceSegments();
		for ( Segment seg : segs ) {
			// Skip segment with no text
			if ( !seg.text.hasText() ) continue;
			// Create the HTML entry
			htmlWriter.writeStartElement("p");
			String out = String.format("%d:%s:%s", subDocId, tu.getId(), seg.id);
			count += (out.length() + 10);
			htmlWriter.writeAttributeString("id", out);
			out = qutil.toCodedHTML(seg.text);
			try {
				count += encoder.encode(CharBuffer.wrap(out)).array().length;
			}
			catch ( CharacterCodingException e ) {
				// Swallow this one
			}
			htmlWriter.writeRawXML(out);
			htmlWriter.writeEndElementLineBreak(); // p
		}
		
		// Check the size of the extraction file
		if ( count >= MAXSIZE ) {
			endExtractedBlock();
			startExtractedBlock();
		}

		return event;
	}

	private void startExtractedBlock () {
		try {
			File tmpFile = File.createTempFile("~okapi-45_gttbt_", ".html");
			blocks.add(tmpFile);
			
			// Create the HTML file
			htmlWriter = new XMLWriter(tmpFile.getPath());
			// Start building the source file
			htmlWriter.writeStartElement("html");
			htmlWriter.writeStartElement("meta");
			htmlWriter.writeAttributeString("http-equiv", "Content-Type");
			htmlWriter.writeAttributeString("content", "text/html; charset=UTF-8");
			htmlWriter.writeEndElementLineBreak();
			count = 100; // Roughly
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error creating extraction file.", e);
		}
	}
	
	private void endExtractedBlock () {
		// Close the extraction file
		htmlWriter.writeEndElementLineBreak(); // html
		htmlWriter.close();
		// Check if we need to process 
		if ( blocks.size() >= 4 ) {
			processExtractionBlocks();
		}
	}
	
	private void processExtractionBlocks () {
		// Upload the blocks
		// Get a document id for each one
		ArrayList<String> docIds = new ArrayList<String>();
		for ( File inputFile : blocks ) {
			String docId = gtt.uploadDocument(inputFile.getPath(),
				Util.getFilename(inputFile.getPath(), true), tmId);
			if ( docId == null ) {
				throw new OkapiException(String.format("Could not upload block %s.", inputFile.getPath()));
			}
			docIds.add(docId);
			if ( params.getOpenGttPages() ) {
				Util.openURL("http://translate.google.com/toolkit/workbench?did="+docId);
			}
		}
		
		// Wait for the user to trigger the download
		int res = waitDlg.waitForUserInput("Please click Continue when you have open and save each uploaded block of text in GTT.", "Continue");
		if ( res == 0 ) {
			cancel();
			return;
		}
		
		// Download the translated blocks
		// And for each one: go through the entries and create the aligned pairs
		int i = 0;
		for ( String docId : docIds ) {
			// Download the translation
			File inputFile = blocks.get(i);
			File outputFile = new File(inputFile.getPath() + ".out");
			gtt.downloadDocument(docId, outputFile);
			
			// Remove the document in GTT: we do not need it any more
			gtt.deleteDocument(docId, true);
			
			// Align the source input and the target output
			alignTranslations(inputFile, outputFile);
			// Delete temporary files
			inputFile.delete();
			outputFile.delete();
			
			// Next block
			i++;
		}

		// Reset the blocks
		blocks.clear();
	}
	
	private void alignTranslations (File inputFile,
		File outputFile)
	{
		try {
			// Open the translated file
			Source trgHtml = new Source(outputFile.toURI().toURL());
			trgHtml.fullSequentialParse();
			List<Element> trgEntries = trgHtml.getAllElements(HTMLElementName.P);

			// Read the source file
			Source srcHtml = new Source(inputFile.toURI().toURL());
			srcHtml.fullSequentialParse();
			List<Element> srcEntries = srcHtml.getAllElements(HTMLElementName.P);
			
			TextFragment srcFrag;
			TextFragment trgFrag;
			int i = 0;
			for ( Element srcElem : srcEntries ) {
				Element trgElem = trgEntries.get(i);
				
				// Make sure we are synchronized
				String id = srcElem.getAttributeValue("id");
				if ( !id.equals(trgElem.getAttributeValue("id")) ) {
					throw new OkapiIOException(String.format("Source and target mismatched for %s", id));
				}

				// Create the source and target fragment
				try {
					srcFrag = qutil.fromCodedHTMLToFragment(srcElem.getContent().toString(), null);
					trgFrag = qutil.fromCodedHTMLToFragment(trgElem.getContent().toString(), null);
					tmxWriter.writeTU(srcFrag, trgFrag, null, attributes);
				}
				catch ( Throwable e ) {
					// Catch issues with inline codes
					logger.warn("Skipping entry '{}'.\n{}", id, e.getMessage());
					continue; // Skip this entry
				}

				i++;
			}
		}
		catch ( MalformedURLException e ) {
			throw new OkapiIOException("Bad URL for temporary HTML", e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Cannot open temporary HTML", e);
		}
	}
}
