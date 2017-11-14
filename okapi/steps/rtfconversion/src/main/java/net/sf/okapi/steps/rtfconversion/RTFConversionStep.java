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

package net.sf.okapi.steps.rtfconversion;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.rtf.RTFFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class RTFConversionStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final Pattern reXML;
	private final String strXMLReplace;
	private final Pattern reXMLVersion;
	private final Pattern reHTML;
	private final String strHTMLReplace;

	private Parameters params;
	private URI outputURI;
	private String outputEncoding;
	private RTFFilter filter;
	private CharsetEncoder encoder;
	
	public RTFConversionStep () {
		params = new Parameters();
		reXML = Pattern.compile("<\\?xml(.*?)encoding(\\s*?)=(\\s*?)(\"|')(.*?)(\"|')(.*?)\\?>");
		strXMLReplace = "<?xml$1encoding$2=$3$4ZZZ$6$7?>";
		reXMLVersion = Pattern.compile("<\\?xml(.*?)version(\\s*?)=(\\s*?)(\"|')(.*?)(\"|')");
		reHTML = Pattern.compile("(content)(\\s*?)=(.*?)(charset)(\\s*?)=(.*?)(\\s|\"|')", Pattern.CASE_INSENSITIVE);
		strHTMLReplace = "$1$2=$3$4$5=ZZZ$7";
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_ENCODING)
	public void setOutputEncoding (String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}
	
	public String getDescription () {
		return "Remove the RTF layer from a text-based file."
			+ " Expects: raw document. Sends back: raw document.";
	}

	public String getName () {
		return "RTF Conversion";
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
		// Create the RTF reader
		if ( filter != null ) filter.close();
		filter = new RTFFilter();
		return event;
	}
	
	@Override
	protected Event handleRawDocument (Event event) {
		RawDocument rawDoc = event.getRawDocument();
		OutputStreamWriter writer = null;
		try {
			// Open the RTF input
			filter.open(rawDoc);
			
			// Open the output document
			File outFile;
			if ( isLastOutputStep() ) {
				outFile = rawDoc.createOutputFile(outputURI);
			}
			else {
				try {
					outFile = File.createTempFile("~okapi-49_okp-rtf_", ".tmp");
				}
				catch ( Throwable e ) {
					throw new OkapiIOException("Cannot create temporary output.", e);
				}
			}
			
			encoder = Charset.forName(outputEncoding).newEncoder();
			
			writer = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outFile)), outputEncoding);
			Util.writeBOMIfNeeded(writer, params.getBomOnUTF8(), outputEncoding);
			
			String xmlReplace = strXMLReplace.replace("ZZZ", outputEncoding);
			String htmlReplace = strHTMLReplace.replace("ZZZ", outputEncoding);
			boolean declDone = !params.getUpdateEncoding(); // Set to done if we don't have do it
			int lines = 0;
			int declState = 0;
			Matcher m;
			
			// Process
			StringBuilder buf = new StringBuilder();
			while ( filter.getTextUntil(buf, -1, 0) == 0 ) {
				
				if ( !declDone  ) {
					String sTmp = buf.toString();
					switch ( declState ) {
					case 0:
						if ( sTmp.indexOf("<?xml ") != -1 ) { // XML is case-sensitive
							declState = 1;
							m = reXML.matcher(sTmp);
							if ( m.find() ) {
								buf.setLength(0);
								buf.append(m.replaceFirst(xmlReplace));
							}
							else { // Check for cases when there is no encoding declaration
								m = reXMLVersion.matcher(sTmp);
								if ( m.find()) {
									buf.insert(m.end(), " encoding=\"" + outputEncoding + "\"");
								}
							}
						}
						if ( sTmp.toLowerCase().indexOf("<html") != -1 ) {
							declState = 2;
						}
						break;

					case 1:
						// No support for XML declaration on several lines
						// Look for HTML (XHTML cases)
						if ( sTmp.toLowerCase().indexOf("<html") != -1 ) {
							declState = 2;
						}
						break;

					case 2:
						m = reHTML.matcher(sTmp);
						if ( m.find() ) {
							buf.setLength(0);
							buf.append(m.replaceFirst(htmlReplace));
							if ( !sTmp.equals(buf.toString()) ) declDone = true;
						}
						break;
					}
					if ( ++lines > 20 ) declDone = true;
				}

				if ( !encoder.canEncode(buf.toString()) ) {
					logger.warn("At least one character cannot be encoded in '{}' in '{}'.", outputEncoding, buf.toString());
				}
				
				writer.write(buf.toString());
				writer.write(params.getLineBreak());
			}

			// Done: close the output
			filter.close();
			writer.close(); writer = null;
			rawDoc.finalizeOutput();
			
			// Set the new raw-document URI and the encoding, other info stays the same
			RawDocument newDoc = new RawDocument(outFile.toURI(), outputEncoding,
				rawDoc.getSourceLocale(), rawDoc.getTargetLocale());
			event.setResource(newDoc);
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error during RTF conversion.\n" + e.getMessage(), e);
		}
		finally {
			if ( writer != null ) {
				try {
					writer.close();
				}
				catch ( IOException e ) {
					// Do nothing
				}
			}
			if ( filter != null ) {
				filter.close();
			}
		}

		return event;
	}

}
