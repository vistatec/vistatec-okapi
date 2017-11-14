/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.ui.segmentation;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

public class FileProcessor {

	private Pattern patternOpening;
	private Pattern patternClosing;
	private Pattern patternPlaceholder;
	private GenericContent sampleOutput;
	
	public FileProcessor () {
		patternOpening = Pattern.compile("\\<(\\w+[^\\>]*)\\>"); //$NON-NLS-1$
		patternClosing = Pattern.compile("\\</(\\w+[^\\>]*)\\>"); //$NON-NLS-1$
		patternPlaceholder = Pattern.compile("\\<(\\w+[^\\>]*)/\\>"); //$NON-NLS-1$
		sampleOutput = new GenericContent();
	}
	
	/**
	 * Puts a simple text string into a TextContainer object. If the string contains
	 * XML-like tags they are converted as in-line codes. 
	 * @param text The string to put into the TextContainer object.
	 * @param textCont The TextContainer object where to put the string
	 * (it must be NOT null).
	 */
	public void populateTextContainer (String text,
		TextContainer textCont)
	{
		int n;
		int start = 0;
		int diff = 0;

		TextFragment tf = new TextFragment();
		
		// If the given text contains code markers, but the corresponding codes are missing,
		// create pseudo-codes for those markers before creating new codes from tags in the text,
		// otherwise TextFragment#balanceCodes() will fail.
		for ( int i=0; i<text.length(); i++ ) {
			switch ( text.charAt(i) ) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				try {					
					Code code = null;
					switch ( text.charAt(i) ) {
					case TextFragment.MARKER_OPENING:
						code = new Code(TagType.OPENING, "");
						break;
					case TextFragment.MARKER_CLOSING:
						code = new Code(TagType.CLOSING, "");
						break;
					case TextFragment.MARKER_ISOLATED:
						code = new Code(TagType.PLACEHOLDER, "");
						break;
					}
					int id = TextFragment.toIndex(text.charAt(++i));
					code.setId(id);
					tf.append(code);					
				}
				catch ( IndexOutOfBoundsException e ) {
					// Do nothing
				}
				break;
				
			default:
				tf.append(text.charAt(i));
			}			
		}
		
		Matcher m = patternOpening.matcher(text);
		while ( m.find(start) ) {
			n = m.start();
			diff += tf.changeToCode(n+diff, (n+diff)+m.group().length(),
				TagType.OPENING, m.group(1));
			start = (n+m.group().length());
		}
		
		text = tf.getCodedText();
		start = diff = 0;
		m = patternClosing.matcher(text);
		while ( m.find(start) ) {
			n = m.start();
			diff += tf.changeToCode(n+diff, (n+diff)+m.group().length(),
				TagType.CLOSING, m.group(1));
			start = (n+m.group().length());
		}
		
		text = tf.getCodedText();
		start = diff = 0;
		m = patternPlaceholder.matcher(text);
		while ( m.find(start) ) {
			n = m.start();
			diff += tf.changeToCode(n+diff, (n+diff)+m.group().length(),
				TagType.PLACEHOLDER, null);
			start = (n+m.group().length());
		}

		textCont.clear();
		textCont.setContent(tf);
	}
	
	public void process (String inputPath,
		String outputPath,
		boolean htmlOutput,
		ISegmenter segmenter)
		throws IOException
	{
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(inputPath), "UTF-8")); //$NON-NLS-1$

			Util.createDirectories(outputPath);
			writer = new BufferedWriter(new OutputStreamWriter(
				new BufferedOutputStream(new FileOutputStream(outputPath)), "UTF-8")); //$NON-NLS-1$

			if ( htmlOutput ) {
				writer.write("<html><head><meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>"); //$NON-NLS-1$
				writer.write("<style>p {white-space: pre; font-family: monospace; border: 1px solid; padding: 4; margin-top: 0; margin-bottom: -1;}</style></head><body>"); //$NON-NLS-1$
			}
			
			// Read the whole file into one string
			StringBuilder tmp = new StringBuilder();
			char[] buf = new char[1024];
			int count = 0;
			while (( count = reader.read(buf)) != -1 ) {
				tmp.append(buf, 0, count);
			}
			
			TextContainer textCont = new TextContainer();
			populateTextContainer(tmp.toString(), textCont);
			// Segment
			segmenter.computeSegments(textCont);
			textCont.getSegments().create(segmenter.getRanges());
			if ( htmlOutput ) {
				for ( Segment seg : textCont.getSegments() ) {
					writer.write("<p>"); //$NON-NLS-1$
					writer.write(Util.escapeToXML(sampleOutput.setContent(seg.text).toString(true), 0, false, null));
					writer.write("</p>"); //$NON-NLS-1$
				}
			}
			else {
				writer.write(sampleOutput.printSegmentedContent(textCont, true, true));
			}

			if ( htmlOutput ) {
				writer.write("</body></html>"); //$NON-NLS-1$
			}
		}
		finally {
			if ( writer != null ) {
				writer.close();
			}
			if ( reader != null ) {
				reader.close();
			}
		}
	}

}
