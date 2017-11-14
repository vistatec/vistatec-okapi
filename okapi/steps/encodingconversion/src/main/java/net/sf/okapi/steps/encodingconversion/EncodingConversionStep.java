/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.encodingconversion;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.HTMLCharacterEntities;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class EncodingConversionStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final int MAXBUF = 1024;

	private Parameters params;
	private String outFormat;
	private CharsetEncoder outputEncoder;
	private boolean useCER;
	private CharBuffer buffer;
	private Pattern pattern;
	private Pattern xmlEncDecl;
	private Pattern xmlDecl;
	private Pattern htmlEncDecl;
	private Pattern htmlDecl;
	private Pattern htmlHead;
	private String prevBuf;
	private boolean isXML;
	private boolean isHTML;
	private URI outputURI;
	private URI inputURI;
	private String outputEncoding;
	private HTMLCharacterEntities entities;

	public EncodingConversionStep () {
		params = new Parameters();
		entities = new HTMLCharacterEntities();
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_URI)
	public void setInputURI (URI inputURI) {
		this.inputURI = inputURI;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_ENCODING)
	public void setOutputEncoding (String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}
	
	public String getDescription () {
		return "Convert the character set encoding of a text-based file."
			+ " Expects: raw document. Sends back: raw document.";
	}

	public String getName () {
		return "Encoding Conversion";
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
		buffer = CharBuffer.allocate(MAXBUF);
		// Pre-compile the patterns for declaration detection
		xmlEncDecl = Pattern.compile("((<\\?xml)(.*?)(encoding(\\s*?)=(\\s*?)(\\'|\\\")))", Pattern.DOTALL);
		xmlDecl = Pattern.compile("((<\\?xml)(.*?)(version(\\s*?)=(\\s*?)(\\'|\\\")))", Pattern.DOTALL);
		htmlEncDecl = Pattern.compile("(<meta)([^>]*?)(content)(\\s*?)=(\\s*?)[\\'|\\\"](\\s*?)text/html(\\s*?);(\\s*?)charset(\\s*?)=(\\s*?)([^\\s]+?)(\\s|\\\"|\\')",
			Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		htmlDecl = Pattern.compile("(<html)", Pattern.CASE_INSENSITIVE);
		htmlHead = Pattern.compile("<head>", Pattern.CASE_INSENSITIVE);
		
		// Pre-compile pattern for un-escaping
		String tmp = "";
		if ( params.getUnescapeNCR() ) {
			tmp += "&#([0-9]*?);|&#[xX]([0-9a-fA-F]*?);";
		}
		if ( params.getUnescapeCER() ) {
			if ( tmp.length() > 0 ) tmp += "|";
			tmp += "(&\\w*?;)";
		}
		if ( params.getUnescapeJava() ) {
			if ( tmp.length() > 0 ) tmp += "|";
			tmp += "(\\\\[Uu]([0-9a-fA-F]{1,4}))";
		}
		if ( tmp.length() > 0 ) {
			pattern = Pattern.compile(tmp, Pattern.CASE_INSENSITIVE);
			entities.ensureInitialization(false);
		}
		else pattern = null;
        		
		useCER = false;
		switch ( params.getEscapeNotation() ) {
		case Parameters.ESCAPE_CER:
			useCER = true;
			entities.ensureInitialization(false);
			outFormat = "&#x%X;"; // Here outFormat is used only if no CER can be used
			break;
		case Parameters.ESCAPE_JAVAL:
			outFormat = "\\u%04x";
			break;
		case Parameters.ESCAPE_JAVAU:
			outFormat = "\\u%04X";
			break;
		case Parameters.ESCAPE_NCRDECI:
			outFormat = "&#%d;";
			break;
		case Parameters.ESCAPE_NCRHEXAL:
			outFormat = "&#x%x;";
			break;
		case Parameters.ESCAPE_USERFORMAT:
			outFormat = params.getUserFormat();
			break;
		case Parameters.ESCAPE_NCRHEXAU:
		default:
			outFormat = "&#x%X;";
			break;
		}
		
		return event;
	}
	
	@Override
	protected Event handleRawDocument (Event event) {
		RawDocument rawDoc = (RawDocument)event.getResource();
		BufferedReader reader = null;
		OutputStreamWriter writer = null;
		try {
			// Try to detect the type of file from extension
			isXML = false;
			isHTML = false;
			String ext = Util.getExtension(inputURI.getPath());
			if ( !Util.isEmpty(ext) ) {
				isHTML = (ext.toLowerCase().indexOf(".htm")==0);
				isXML = ext.equalsIgnoreCase(".xml");
			}
			
			//=== Try to detect the encoding
			
			InputStream is = rawDoc.getStream();
			// First: guess from a possible BOM
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(is, rawDoc.getEncoding());
			detector.detectAndRemoveBom();
			rawDoc.setEncoding(detector.getEncoding());

			String inputEncoding = rawDoc.getEncoding();
			// Then try internal detection for XML/HTML type files
			if ( !detector.isAutodetected() ) {
				reader = new BufferedReader(rawDoc.getReader());
				reader.read(buffer);
				String detectedEncoding = checkDeclaration(inputEncoding);
				if ( !detectedEncoding.equalsIgnoreCase(inputEncoding) ) {
					inputEncoding = detectedEncoding;
				}
				reader.close();
			}

			// Open the input document 
			//TODO: Where did we reset the reader - can't call this twice unless we reset it
			reader = new BufferedReader(rawDoc.getReader());
			logger.info("Input encoding: {}", inputEncoding);
			
			// Open the output document
			File outFile;
			if ( isLastOutputStep() ) {
				outFile = rawDoc.createOutputFile(outputURI);
			}
			else {
				try {
					outFile = File.createTempFile("~okapi-40_okp-enc_", ".tmp");
				}
				catch ( Throwable e ) {
					throw new OkapiIOException("Cannot create temporary output.", e);
				}
			}
			writer = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(outFile)), outputEncoding);
			outputEncoder = Charset.forName(outputEncoding).newEncoder();
			logger.info("Output encoding: {}", outputEncoding);
			Util.writeBOMIfNeeded(writer, params.getBOMonUTF8(), outputEncoding);
			
			int n;
			CharBuffer tmpBuf = CharBuffer.allocate(1);
			ByteBuffer encBuf;
			boolean canEncode;
			boolean checkDeclaration = true;

			while ( true ) {
				buffer.clear();
				// Start with previous buffer remains if needed
				if ( prevBuf != null ) {
					buffer.append(prevBuf);
				}
				// Read the next block
				n = reader.read(buffer);
				// Check if we need to stop here
				boolean needSplitCheck = true;
				if ( n == -1 ) {
					// Make sure we do not start an endless loop by
					// re-checking the last previous buffer
					if ( prevBuf != null ) {
						needSplitCheck = false;
						prevBuf = null;
						buffer.limit(buffer.position());
					}
					else break; // No previous, no read: Done
				}
				
				if ( checkDeclaration ) {
					checkDeclaration(inputEncoding);
					checkDeclaration = false;
				}

				// Un-escape if requested
				if ( pattern != null ) {
					if ( needSplitCheck ) checkSplitSequence();
					unescape();
				}
				
				// Output
				n = buffer.position();
				buffer.position(0);
				for ( int i=0; i<n; i++ ) {
					if ( !(canEncode = outputEncoder.canEncode(buffer.get(i))) ) {
						if ( params.getReportUnsupported() ) {
							logger.warn(String.format("Un-supported character: U+%04X ('%c')",
								(int)buffer.get(i), buffer.get(i)));
						}
					}
					
					if (( params.getEscapeAll() && ( buffer.get(i) > 127 )) || !canEncode ) {
						boolean fallBack = false;
						// Write escape form
						if ( useCER ) {
							String tmp = entities.getName(buffer.get(i));
							if ( tmp == null ) fallBack = true;
							else writer.write("&"+tmp+";");
						}
						else {
							if ( params.getUseBytes() ) { // Escape bytes
								if ( canEncode ) {
									tmpBuf.put(0, buffer.get(i));
									tmpBuf.position(0);
									encBuf = outputEncoder.encode(tmpBuf);
									for ( int j=0; j<encBuf.limit(); j++ ) {
										writer.write(String.format(outFormat,
											(encBuf.get(j)<0 ? (0xFF^~encBuf.get(j)) : encBuf.get(j)) ));
									}
								}
								else fallBack = true;
							}
							else { // Escape character
								writer.write(String.format(outFormat, (int)buffer.get(i)));
							}
						}
						if ( fallBack ) { // Default escaping when nothing else works
							writer.write(String.format("&#x%X;", (int)buffer.get(i)));
						}
					}
					else { // Normal raw forms
						writer.write(buffer.get(i));
					}
				}
			}
			
			// Done: close the files
			reader.close(); reader = null; 
			writer.close(); writer = null;
			rawDoc.finalizeOutput();
			
			// Set the new raw-document URI and the encoding (in case one was auto-detected)
			// Other info stays the same
			RawDocument newDoc = new RawDocument(outFile.toURI(), outputEncoding,
				rawDoc.getSourceLocale(), rawDoc.getTargetLocale());
			event.setResource(newDoc);
			
		}
		catch ( FileNotFoundException e) {
			throw new OkapiException(e);
		}
		catch ( IOException e) {
			throw new OkapiException(e);
		}
		finally {
			try {
				if ( writer != null ) {
					writer.close();
					writer = null;
				}
				if ( reader != null ) {
					reader.close();
					reader = null;
				}
			}
			catch ( IOException e) {
				throw new OkapiException(e);
			}
		}
		
		return event;
	}

	private String checkDeclaration (String defEncoding) {
		// Convert the CharBuffer to a string
		buffer.limit(buffer.position());
		buffer.position(0);
		StringBuffer text = new StringBuffer(buffer.toString());
		
		// Look for XML encoding declaration
		String encoding = defEncoding;
		Matcher m = xmlEncDecl.matcher(text);
		if ( m.find() ) { // We have an XML encoding declaration
			isXML = true;
			// Get the declared encoding
			String delim = String.valueOf(text.charAt(m.end()-1));
			int end = text.indexOf(delim, m.end());
			if ( end != -1 ) {
				encoding = text.substring(m.end(), end);
				// End replace the current declaration by the new one
				text.replace(m.end(), end, outputEncoding);
			}
		}
		else { // No XML encoding declaration found: Check if it is XML
			m = xmlDecl.matcher(text);
			if ( m.find() ) { // It is XML without encoding declaration
				isXML = true;
				// Encoding should UTF-8 or UTF-16/32, we will detect those later
				encoding = "UTF-8";
				// Add the encoding after the version
				String delim = String.valueOf(text.charAt(m.end()-1));
				int end = text.indexOf(delim, m.end());
				if ( end != -1 ) {
					text.insert(end+1, " encoding=\""+outputEncoding+"\"");
				}
			}
			else { // No XML declaration found, maybe it's an XML without one
				if ( isXML ) { // Was a .xml extension, assume UTF-8
					encoding = "UTF-8";
					text.insert(0, "<?xml version=\"1.0\" encoding=\""+outputEncoding+"\" ?>");
				}
			}
		}

		// Look for HTML declarations
		m = htmlEncDecl.matcher(text);
		if ( m.find() ) {
			isHTML = true;
			// Group 11 contains the encoding name
			encoding = m.group(11);
			// Replace it by the new encoding
			int n = text.indexOf(encoding, m.start());
			text.replace(n, n+encoding.length(), outputEncoding);
		}
		else if ( isHTML ) { // No HTML encoding found, but try to update if it was seen as HTML from extension 
			// Try to place it after <head>
			m = htmlHead.matcher(text);
			if ( m.find() ) {
				text.insert(m.end(), String.format(
					"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=%s\"></meta>",
					outputEncoding));
			}
			else { // If no <head>, try <html>
				m = htmlDecl.matcher(text);
				if ( m.find() ) {
					int n = text.indexOf(">", m.end());
					if ( n != -1 ) {
						text.insert(n+1, String.format(
							"<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=%s\"></meta></head>",
							outputEncoding));
					}
				}
			}
		}
		
		// Convert the string back to a CharBuffer
		int len = text.length();
		// Make sure we have room for added characters
		if ( len > buffer.capacity() ) {
			buffer = CharBuffer.allocate(len);
		}
		else {
			buffer.clear();
		}
		buffer.append(text.toString());
		buffer.limit(len);
		return encoding;
	}
	
	private void checkSplitSequence () {
		int len = buffer.position();
		buffer.position(0);
		// Search for the first & or \ in the last 10 (or less) characters
		prevBuf = null;
		int j = 0;
		for ( int i=len-1; ((i>=0) && (j<10)); i-- ) {
			if (( buffer.charAt(i) == '&' ) || ( buffer.charAt(i) == '\\' )) {
				prevBuf = buffer.subSequence(i, len).toString();
				len = i;
				break;
			}
			j++;
		}
		buffer.position(len);
		buffer.limit(len);
	}
	
	private void unescape () {
		int len = buffer.position();
		buffer.position(0);
		Matcher m = pattern.matcher(buffer);
		int pos = 0;
		StringBuilder tmp = new StringBuilder(len);
		String seq = null;
		while ( m.find(pos) ) {
			// Copy any previous text
			if ( m.start() > pos ) {
				// Get text before
				tmp.append(buffer.subSequence(pos, m.start()));
			}
			pos = m.end();

			// Treat the escape sequence
			seq = m.group();
			int value = -1;
			int uIndex = seq.indexOf('u');
			if ( seq.indexOf('x') == 2 ) {
				// Hexadecimal NCR "&#xHHH;"
				value = Integer.parseInt(seq.substring(3, seq.length()-1), 16);
			}
			else if (( uIndex == 1 ) && ( seq.charAt(uIndex-1) == '\\' )) {
				// Java style "\ and uHHH"
				value = Integer.parseInt(seq.substring(2), 16);
			}
			else if ( seq.indexOf('#') == 1 ) {
				// Decimal NCR "&#DDD;"
				value = Integer.parseInt(seq.substring(2, seq.length()-1));
			}
			else {
				// Character entity reference: &NAME;
				seq = seq.substring(1, seq.length()-1);
				// Unidentified is -1: leave it like that
				value = entities.lookupName(seq);
			}

			// Append the parsed escape
			switch ( value ) {
			case -1: // Unidentified (includes e.g. &apos;)
			case 0x22: // "
			case 0x27: // '
			case 0x26: // &
			case 0x3C: // <
			case 0x3E: // >
				tmp.append(m.group()); // Keep those escaped
				break;
			default:
				// Un-escape all others
				tmp.append((char)value);
			}
		}
		
		// Copy last part and re-build the buffer
		if ( seq != null ) { // We had at least one match
			if ( pos < len ) {
				// Get text before
				tmp.append(buffer.subSequence(pos, len));
			}
			// Reset the buffer
			buffer.clear();
			buffer.append(tmp.toString(), 0, tmp.length());
		}
		else { // Else: nothing to un-escape
			buffer.position(len);
		}
	}
	
}
