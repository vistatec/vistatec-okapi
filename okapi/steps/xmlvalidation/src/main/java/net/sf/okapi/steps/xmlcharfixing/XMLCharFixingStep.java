/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xmlcharfixing;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class XMLCharFixingStep extends BasePipelineStep {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final Pattern pattern = Pattern.compile("&#(x?)([0-9a-fA-F]+);");
	
	private Parameters params;
	private URI outputURI;
	private int count;

	public XMLCharFixingStep () {
		params = new Parameters();
	}
	
	public String getDescription () {
		return "Fixes invalid characters in XML documents."
			+ " Expects: raw document. Sends back: raw document.";
	}

	public String getName () {
		return "XML Characters Fixing";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}
 
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
	public URI getOutputURI() {
		return outputURI;
	}
	
	@Override
	protected Event handleStartBatch (Event event) {
		count = 0;
		return event;
	}
	
	@Override
	protected Event handleEndBatch (Event event) {
		LOGGER.info("Number of invalid characters replaced = {}", count);
		return event;
	}
	
	@Override
	protected Event handleRawDocument (Event event) {
		FileOutputStream output = null;
		BufferedReader reader = null;
		OutputStreamWriter writer = null;
		
		try {
			RawDocument rd = event.getRawDocument();

			// Open the input
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(rd.getStream(), rd.getEncoding());
			detector.detectAndRemoveBom();
			rd.setEncoding(detector.getEncoding());
			String lineBreak = detector.getNewlineType().toString();
			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), rd.getEncoding()));
			
			// Open the output
			File outFile;
			if ( isLastOutputStep() ) {
				outFile = rd.createOutputFile(outputURI);
			}
			else {
				try {
					outFile = File.createTempFile("~okapi-72_okp-xcf_", ".tmp");
				}
				catch ( Throwable e ) {
					throw new OkapiIOException("Cannot create temporary output.", e);
				}
			}
			output = new FileOutputStream(outFile);
			writer = new OutputStreamWriter(new BufferedOutputStream(output), rd.getEncoding());
			// Write BOM if there was one
			Util.writeBOMIfNeeded(writer, detector.hasUtf8Bom(), rd.getEncoding());

			// In XML 1.0 the valid characters are:
			// #x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]

			// Process
			StringBuilder tmp = new StringBuilder();
			String line;
			Matcher m = null;
			
			while ( (line = reader.readLine()) != null ) {;
				// Process that line
				tmp.setLength(0);
				tmp.append(line);
				int ch;
				for ( int i=0; i<tmp.length(); i++ ) {
					switch ( (ch = tmp.codePointAt(i)) ) {
					case 0x0009: // Tab is allowed
					case 0x000A: // Line-feed is allowed
					case 0x000D: // Carriage-return is allowed
						continue;
					default:
						if (( ch >= 0x0020 ) && ( ch <= 0xD7FF )) {
							continue; // Valid
						}
						if (( ch >= 0xE000 ) && ( ch <= 0xFFFF )) {
							continue; // Valid
						}
						if (( ch >= 0x10000 ) && ( ch <= 0x10FFFF )) {
							i++; // Skip extra character for the code-point
							continue; // Valid
						}
						// Else: it's an invalid character
						String repl = String.format(params.getReplacement(), ch);
						tmp.replace(i, i+(ch>0xFFFF ? 2 : 1), repl);
						i += (repl.length()-1); // Move cursor just before next
						count++;
						continue;
					}
				}
				
				// Check for decimal NCRs
				int start = 0;
				do {
					m = pattern.matcher(tmp.toString());
					if ( !m.find(start) ) break; // Done
					try {
						int n = Integer.parseInt(m.group(2), m.group(1).isEmpty() ? 10 : 16);
						start = m.start();
						if ( !isValid(n) ) {
							String repl = String.format(params.getReplacement(), n);
							tmp.replace(start, m.end(), repl);
							start += (repl.length()-m.group().length()); // Move cursor on next
							count++;
						}
						else start = m.end();
					}
					catch ( NumberFormatException e ) {
						LOGGER.error("Invalid NCR: '{}'", m.group());
					}
					
				}
				while ( true );
				
				// Line has been processed, write it back
				writer.write(tmp.toString()+lineBreak);
			}

			// Done: close the files
			reader.close(); reader = null;
			writer.close(); writer = null;
			rd.finalizeOutput();
			
			// Creates the new RawDocument
			event.setResource(new RawDocument(outFile.toURI(), rd.getEncoding(), 
				rd.getSourceLocale(), rd.getTargetLocale()));
		}
		catch ( IllegalFormatException e ) {
			LOGGER.error("Invalid replacement format: '{}'", params.getReplacement());
		}
		catch ( Exception e ) {
			LOGGER.error("Error while processing XML for invalid characters.");
		}
		finally {
			try {
				if ( writer != null ) {
					writer.close();
				}
				if ( reader != null ) {
					reader.close();
				}
			}
			catch ( IOException e ) {
				throw new OkapiIOException("IO error while closing.", e);
			}
		}

		return event;
	}
	
	private boolean isValid (int value ) {
		switch ( value ) {
		case 0x0009: // Tab is allowed
		case 0x000A: // Line-feed is allowed
		case 0x000D: // Carriage-return is allowed
			return true;
		default:
			if (( value >= 0x0020 ) && ( value <= 0xD7FF )) {
				return true; // Valid
			}
			if (( value >= 0xE000 ) && ( value <= 0xFFFF )) {
				return true; // Valid
			}
			if (( value >= 0x10000 ) && ( value <= 0x10FFFF )) {
				return true; // Valid
			}
			// Else: it's an invalid character
			return false;
		}

	}

}
