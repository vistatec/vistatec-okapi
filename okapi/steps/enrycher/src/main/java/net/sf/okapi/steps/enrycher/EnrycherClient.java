/*===========================================================================
  Copyright (C) 2012-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.enrycher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Attribute;
import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.QueryUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a wrapper to easily call the Enrycher web service.
 */
public class EnrycherClient {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private static final Pattern HTML_STARTSPAN = Pattern.compile("\\<span\\s(.*?)>",
		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	private static final Pattern HTML_ENDSPAN = Pattern.compile("\\</span>",
		Pattern.CASE_INSENSITIVE);

	private Parameters params;
	private String lang;
	private QueryUtil util;

	/**
	 * Creates a default client object with "en" as the default locale, and the default paameters.
	 */
	public EnrycherClient () {
		params = new Parameters();
		lang = "en";
		util = new QueryUtil();
	}
	
	/**
	 * Gets the current parameters for this client.
	 * @return the current parameters for this client.
	 */
	public IParameters getParameters () {
		return params;
	}
	
	/**
	 * Sets the parameters for this client.
	 * @param params the new parameters to use.
	 */
	public void setParameters (Parameters params) {
		this.params = params;
	}
	
	/**
	 * Sets the locale to use when invoking the Enrycher service.
	 * @param locId the locale to use.
	 */
	public void setLocale (LocaleId locId) {
		lang = locId.getLanguage();
	}

	/**
	 * Sends an HTML string to the service and get back the same string with Enrycher's ITS annotations.
	 * @param text the HTML string to process.
	 * @return the annotated HTML string.
	 */
	public String processContent (String text) {
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		try {
			// Prepare the request
			URL url = new URL(params.getBaseUrl()+lang+"/run.html5its2");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "text/html");
			wr = new OutputStreamWriter(conn.getOutputStream());
	    
			// Post the request
			wr.write(text);
			wr.flush();

			// Get the response
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ( (line = rd.readLine()) != null ) {
				sb.append(line + "\n");
			}
		    return sb.toString();
		}
		catch ( MalformedURLException e ) {
			throw new OkapiException("Invalid URL:\n"+e.getMessage());
		}
		catch ( IOException e ) {
			throw new OkapiException("Input/Output error:\n"+e.getMessage());
		}
		finally {
			try {
				if ( wr != null ) wr.close();
				if ( rd != null ) rd.close();
			}
			catch ( IOException e ) {
				// Skip this one
			}			
		}
	}	

	/**
	 * Call the Enrycher service on a list of text units.
	 * they can be segmented or not.
	 * Only the source content is annotated. the target content is not touched.
	 * @param list the list to process.
	 */
	public void processList (LinkedList<ITextUnit> list) {
		// Check if there is anything to process
		if ( Util.isEmpty(list) ) return;

		// Convert each segment into an HTML paragraph with a unique ID
		StringBuilder sb = new StringBuilder();
		for ( ITextUnit tu : list ) {
			if ( !tu.isTranslatable() ) continue;
			ISegments segs = tu.getSource().getSegments();
			for ( int i=0; i<segs.count(); i++ ) {
				sb.append("<p id='"+tu.getId()+"_"+i+"'>");
				sb.append(util.toCodedHTML(segs.get(i).getContent()));
				sb.append("</p>");
			}
		}
		
		// Call the service. this gets back an annotated HTML string
		String res = processContent(sb.toString());
		parseHTML(res, list);
	}
	
	protected void parseHTML (String htmlResult,
		LinkedList<ITextUnit> list)
	{
		// Parse the returned string
		Source source = new Source(htmlResult);
		
		// Transfer back the information into each segment
		for ( ITextUnit tu : list ) {
			try {
				if ( !tu.isTranslatable() ) continue;
				ISegments segs = tu.getSource().getSegments();
				for ( int i=0; i<segs.count(); i++ ) {
					// Get the paragraph for the given segment and convert it back to coded text
					// but leave the ITS span elements
					Element p = source.getElementById(tu.getId()+"_"+i);
					
					TextFragment tf = segs.get(i).getContent();
					String ct = util.fromCodedHTML(p.getContent().toString(), tf, false, false);
					// Need to unwrap the content if needed (because Enrycher adds line breaks and spaces in some cases)
					if ( !tu.preserveWhitespaces() ) {
						TextFragment.unwrap(tf);
						ct = unwrap(ct);
					}
	
					// Only spans are left: inline codes have been replaced by coded-text markers
					// We can gather the spans
					// We need to do this after the inline elements have been removed to get the positions in the coded text
					
					StringBuilder sb = new StringBuilder(ct);
					int markersLength = 0;
					int nestedMarkersLength = 0;
					Matcher m = HTML_ENDSPAN.matcher(sb.toString());
					
					while ( m.find() ) {	
						// Ending tag found
						int nestedCount = -1;
						int endBlock = m.end();
						int startBlock = -1;
						int n = 0;
						int correctionStart = 0;
						int endLength = m.group(0).length();
						int correctionEnd = endLength;
						int startLength = 0;
						// Now look for corresponding starting tag (the nearest on the left)
						m = HTML_STARTSPAN.matcher(sb.toString());
						while ( m.find(n) ) {
							n = m.end();
							startBlock = m.start();
							startLength = m.group(0).length();
							correctionStart += startLength;
							nestedCount++;
							m = HTML_STARTSPAN.matcher(sb.substring(0, endBlock));
						}
						correctionEnd += correctionStart;
						correctionStart -= startLength;
						if ( startBlock == -1 ) {
							LOGGER.warn("Missing <span> in entry '{}'.", tu.getId()+"_"+i);
							continue;
						}
						
						Source spanSource = new Source(sb.substring(startBlock, endBlock));
						Element span = spanSource.getFirstElement();
	
						GenericAnnotations anns = readAnnotation(span);
						if ( anns != null ) {
							/* When working with nested spans, we add the added length for the annotation markers
							 * only on the closing one, since the starting one is always before any inner annotation.
							 * But on non-nested spans we need to add the total added length on both positions.
							 */
							int diff = tf.annotate((startBlock-correctionStart) + (nestedCount>0 ? markersLength : markersLength-nestedMarkersLength),
								(endBlock-correctionEnd)+markersLength, GenericAnnotationType.GENERIC, anns);
							nestedMarkersLength += diff;
							markersLength += diff;
						}
	
						// Remove the span tags
						sb.delete(endBlock-endLength, endBlock);
						sb.delete(startBlock, startBlock+startLength);
	
						m = HTML_ENDSPAN.matcher(sb.toString());
	
						if ( nestedCount == 0 ) {
							// Reset for next group of nested spans
							nestedMarkersLength = 0;
						}
					}
				}
			}
			catch ( Throwable e ) {
				throw new OkapiIOException(
					String.format("Error when placing ITS markup (TU id='%s').\n"+e.getMessage(),
						tu.getId()));
			}
		}
	}
	
	GenericAnnotations readAnnotation (Element itsSpan) {
		GenericAnnotation ann = new GenericAnnotation(GenericAnnotationType.TA);
		Attributes attributes = itsSpan.getAttributes();
		for ( Attribute attr : attributes ) {
			if ( attr.getKey().equals("its-ta-class-ref") ) {
				ann.setString(GenericAnnotationType.TA_CLASS, GenericAnnotationType.REF_PREFIX+attr.getValue());
			}
			else if ( attr.getKey().equals("its-ta-source") ) {
				ann.setString(GenericAnnotationType.TA_SOURCE, attr.getValue());
			}
			else if ( attr.getKey().equals("its-ta-ident") ) {
				ann.setString(GenericAnnotationType.TA_IDENT, attr.getValue());
			}
			else if ( attr.getKey().equals("its-ta-ident-ref") ) {
				ann.setString(GenericAnnotationType.TA_IDENT, GenericAnnotationType.REF_PREFIX+attr.getValue());
			}
			else if ( attr.getKey().equals("its-ta-confidence") ) {
				ann.setDouble(GenericAnnotationType.TA_CONFIDENCE, Double.parseDouble(attr.getValue()));
			}
		}

		if ( ann.getFieldCount() == 0 ) return null;
		else return new GenericAnnotations(ann);
	}

	public String unwrap (String text) {
		StringBuilder tmp = new StringBuilder(text.length());
		boolean wasWS = true; // Removes leading white-spaces
		// Process the text
		for ( int i=0; i<text.length(); i++ ) {
			switch ( text.charAt(i) ) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				tmp.append(text.charAt(i));
				tmp.append(text.charAt(++i));
				wasWS = false;
				break;
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				if ( wasWS ) continue;
				wasWS = true;
				tmp.append(' ');
				break;
			default:
				wasWS = false;
				tmp.append(text.charAt(i));
				break;
			}
		}
		return tmp.toString();
	}

}
