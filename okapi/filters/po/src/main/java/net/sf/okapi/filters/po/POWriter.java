/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.po;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Implementation of {@link IFilterWriter} for PO. This class is not
 * designed to be used with the PO Filter, but as a standalone writer that
 * can be driven by filter events.
 * <p>In extraction/merging mode the context line holds a trace of the current sub-document,
 * group and text unit.
 * okpctx: sd=ID:gp=ID:gp=ID...:tuID
 */
public class POWriter implements IFilterWriter {

	public static final String CRUMBS_PREFIX = "okpCtx";
	public static final String SUBDOCUMENT_CRUMB = "sd=";
	public static final String GROUP_CRUMB = "gp=";
	public static final String TEXTUNIT_CRUMB = "tu=";

	private static final String ESCAPEABLE = "\\\"abfnrtv";

	private Parameters params;
	private OutputStream output;
	private String outputPath;
	private OutputStreamWriter writer;
	private File tempFile;
	private LocaleId language;
	private String encoding;
	private int group;
	private int pluralGroup;
	private String linebreak;
	private GenericContent fmt;
	private ArrayList<ITextUnit> plurals;
	private boolean forExtractMerge;
	private boolean makePOT;
	private boolean transFuzzy; // Used when forExtractMerge == true 
	private String crumbs;
	
	public POWriter () {
		params = new Parameters();
		fmt = new GenericContent();
		plurals = new ArrayList<ITextUnit>();
		makePOT = false;
		forExtractMerge = false;
		transFuzzy = true;
	}
	
	@Override
	public void cancel () {
		//TODO: Implement cancel()
	}

	/**
	 * Sets the creation mode for this writer.
	 * @param forExtractMerge true to create a file that can be merged back into its original format.
	 * @param makePOT true to create a PO template file.
	 * @param transFuzzy true to set existing translations as fuzzy (only when forExtractMerge is true)
	 */
	public void setMode (boolean forExtractMerge,
		boolean makePOT,
		boolean transFuzzy)
	{
		this.forExtractMerge = forExtractMerge;
		this.makePOT = makePOT;
		this.transFuzzy = transFuzzy;
	}
	
	@Override
	public void close () {
		if ( writer == null ) return;
		IOException err = null;
		InputStream orig = null;
		OutputStream dest = null;
		try {
			// Close the output
			writer.close();
			writer = null;
			output.close();
			output = null;

			// If it was in a temporary file, copy it over the existing one
			// If the IFilter.close() is called before IFilterWriter.close()
			// this should allow to overwrite the input.
			if ( tempFile != null ) {
				dest = new FileOutputStream(outputPath);
				orig = new FileInputStream(tempFile); 
				byte[] buffer = new byte[2048];
				int len;
				while ( (len = orig.read(buffer)) > 0 ) {
					dest.write(buffer, 0, len);
				}
			}
		}
		catch ( IOException e ) {
			err = e;
		}
		finally {
			// Make sure we close both files
			if ( dest != null ) {
				try {
					dest.close();
				}
				catch ( IOException e ) {
					err = e;
				}
				dest = null;
			}
			if ( orig != null ) {
				try {
					orig.close();
				}
				catch ( IOException e ) {
					err = e;
				}
				orig = null;
				if ( err != null ) throw new OkapiException(err);
				else {
					if ( tempFile != null ) {
						tempFile.delete();
						tempFile = null;
					}
				}
			}
		}
	}

	@Override
	public String getName () {
		return "POFilterWriter";
	}

	@Override
	public EncoderManager getEncoderManager () {
		// This writer does not use skeleton
		return null;
	}
	
	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return null;
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			processStartDocument(event);
			break;
		case END_DOCUMENT:
			processEndDocument();
			break;
		case START_SUBDOCUMENT:
			processStartSubDocument(event);
			break;
		case END_SUBDOCUMENT:
			processEndSubDocument();
			break;
		case START_GROUP:
		case START_SUBFILTER:
			processStartGroup(event);
			break;
		case END_GROUP:
		case END_SUBFILTER:
			processEndGroup(event);
			break;
		case TEXT_UNIT:
			if ( pluralGroup > -1 ) { // Store until we reach the end of the group
				plurals.add(event.getTextUnit());
			}
			else {
				processTextUnit(event);
			}
			break;
		default:
			break;
		}
		return event;
	}
	
	@Override
	public void setOptions (LocaleId language,
		String defaultEncoding)
	{
		this.language = language;
		this.encoding = defaultEncoding;
	}

	@Override
	public void setOutput (String path) {
		close(); // Make sure previous is closed
		this.outputPath = path;
	}

	@Override
	public void setOutput (OutputStream output) {
		close(); // Make sure previous is closed
		this.outputPath = null; // If we use the stream, we can't use the path
		this.output = output; // then assign the new stream
	}

	@Override
	public void setParameters (IParameters params) {
		params = (Parameters)params;
	}

	private void processStartDocument (Event event) {
		try {
			StartDocument sd = (StartDocument)event.getResource();
			// Create the output
			createWriter(sd);
			// Writer header
			writer.write("# "+linebreak);
			if ( forExtractMerge ) {
				writer.write("# This file is intended to be merged back. "+linebreak);
				writer.write("# Please preserve the msgctxt lines and the order of the entries."+linebreak);
				writer.write("# "+linebreak);
			}
			writer.write("msgid \"\""+linebreak);
			writer.write("msgstr \"\""+linebreak);
			writer.write("\"Content-Type: text/plain; charset="+encoding+"\\n\""+linebreak);
			writer.write("\"Content-Transfer-Encoding: 8bit\\n\""+linebreak);
			writer.write("\"Language: "+language.toPOSIXLocaleId()+"\\n\""+linebreak);
			writer.write("\"Plural-Forms: "+PluralForms.getExpression(language));
			writer.write("\\n\""+linebreak+linebreak);
			crumbs = CRUMBS_PREFIX;
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error writing the header.", e);
		}
	}

	private void processEndDocument () {
		close();
	}

	private void processStartSubDocument (Event event) {
		if ( forExtractMerge ) {
			StartSubDocument ssd = (StartSubDocument)event.getResource();
			pushCrumb(SUBDOCUMENT_CRUMB+ssd.getId());
		}
	}
	
	private void processEndSubDocument () {
		if ( forExtractMerge ) {
			popCrumb();
		}
	}

	private void processStartGroup (Event event) {
		group++;
		StartGroup sg = (StartGroup)event.getResource();
		if (( sg.getType() != null ) && sg.getType().equals("x-gettext-plurals") ) {
			pluralGroup = group;
			plurals.clear();
		}
		if ( forExtractMerge ) {
			pushCrumb(GROUP_CRUMB+sg.getId());
		}
	}

	private void processEndGroup (Event event) {
		if ( pluralGroup == group ) {
			writePluralForms();
			pluralGroup = -1;
		}
		group--;
		if ( forExtractMerge ) {
			popCrumb();
		}
	}

	private void popCrumb () {
		int n = crumbs.lastIndexOf(':');
		crumbs = crumbs.substring(0, n);
	}
	
	private void pushCrumb (String crumb) {
		crumbs += (":"+crumb);
	}
	
	private void writePluralForms () {
		try {
			if ( plurals.size() < 2 ) {
				throw new OkapiIOException("PO connot have less than two entries for a plural form.");
			}

			// Fuzzy
			TextContainer tc = plurals.get(0).getTarget(language);
			if ( tc != null ) {
				Property prop = tc.getProperty(Property.APPROVED);
				if ( prop != null ) {
					if ( !prop.getValue().equals("yes") ) {
						writer.write("#, fuzzy"+linebreak);
					}
				}
			}
			
			// msgid
			writer.write("msgid ");
			writeQuotedContent(plurals.get(0).getSource());
			writer.write(linebreak);
	
			writer.write("msgid_plural ");
			writeQuotedContent(plurals.get(1).getSource());
			writer.write(linebreak);
	
			int count = 0;
			for ( ITextUnit tu : plurals ) {
				writer.write(String.format("msgstr[%d] ", count));
				if ( tu.hasTarget(language) && !makePOT ) {
					writeQuotedContent(tu.getTarget(language));
				}
				else {
					writer.write("\"\"");
				}
				writer.write(linebreak);
				count++;
			}
			writer.write(linebreak);
		}		
		catch ( IOException e ) {
			throw new OkapiIOException("Error writing a plural group.", e);
		}
	}
	
	private void processTextUnit (Event event) {
		try {
			ITextUnit tu = event.getTextUnit();
			if ( tu.isEmpty() ) return; // Do not write out entries with empty source
			
			TextContainer tc = tu.getTarget(language);
			Property prop = null;
			if ( tc != null ) {
				prop = tc.getProperty(Property.APPROVED);
			}
			
			// Fuzzy
			if ( !makePOT ) {
				if ( prop != null ) {
					if ( !prop.getValue().equals("yes") ) {
						writer.write("#, fuzzy"+linebreak);
					}
				}
				else if ( forExtractMerge && (( tc != null ) && !tc.isEmpty() )) {
					if ( transFuzzy ) writer.write("#, fuzzy"+linebreak);
				}
			}
			// ID reference to allow merging back and duplication of msgid text
			if ( forExtractMerge ) {
				writer.write("msgctxt \"" + crumbs + ":" + TEXTUNIT_CRUMB+tu.getId() + "\"" + linebreak);
			}
			// msgid
			writer.write("msgid ");
			writeQuotedContent(tu.getSource());
			writer.write(linebreak);
			// msgstr
			writer.write("msgstr ");
			if (( tc != null ) && !makePOT ) {
				writeQuotedContent(tc);
			}
			else {
				writer.write("\"\"");
			}
			writer.write(linebreak+linebreak);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error writing a text unit.", e);
		}
	}

	private void writeQuotedContent (TextContainer tc) {
		try {
			if ( tc == null ) {
				writer.write("\"\"");
				return;
			}

			String tmp;
			if ( forExtractMerge ) {
				if ( tc.contentIsOneSegment() ) {
					tmp = GenericContent.fromFragmentToLetterCoded(tc.getFirstContent(), true);
				}
				else { // If the container is segmented
					// We replace content made of only inter-segments by an empty one
					// (because no segment has really text)
					boolean hasSomeText = false;
					for ( Segment seg : tc.getSegments() ) {
						if ( seg.text.hasText() ) {
							hasSomeText = true;
							break;
						}
					}
					if ( hasSomeText ) {
						tmp = GenericContent.fromFragmentToLetterCoded(tc.getUnSegmentedContentCopy(), true);
					}
					else {
						tmp = "";
					}
				}
			}
			else {
				tmp = fmt.printSegmentedContent(tc, false, !params.getOutputGeneric());
			}
			tmp = escapeIfNeeded(tmp);
			
			if ( !params.getWrapContent() || ( tmp.indexOf("\\n") == -1 )) {
				writer.write("\"");
				writer.write(tmp); // No wrapping needed
				writer.write("\"");
			}
			else { // Wrap at "\n" markers
				int n = 0;
				int start = 0;
				writer.write("\"\""); // First line is empty
				while ( (n = tmp.indexOf("\\n", start)) > -1 ) {
					n += 2;
					writer.write(linebreak+"\"");
					writer.write(tmp.substring(start, n));
					writer.write("\"");
					start = n;
				}
				if (( n == -1 ) && ( start < tmp.length() )) {
					writer.write(linebreak+"\"");
					writer.write(tmp.substring(start));
					writer.write("\"");
				}
			}
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error writing a quoted text.", e);
		}
	}

	// We assume that if a bslash id followed by a valid escapeable char
	// it is a valid escape.
	// This means unescaped paths won't be escaped in some cases:
	// c:\abc should be c:\\abc but won't because \a is valid escape
	private String escapeIfNeeded (String in) {
		char prev = '\0';
		StringBuilder tmp = new StringBuilder(in.length());
		for ( int i=0; i<in.length(); i++ ) {
			switch ( in.charAt(i) ) {
			case '\\':
				if (( i < in.length()-1 ) && ( ESCAPEABLE.indexOf(in.charAt(i+1)) != -1 )) {
					// We assume it's an escape
					tmp.append('\\');
					tmp.append(in.charAt(i+1));
					i++;
				}
				else { // It's an isolated '\'
					tmp.append("\\\\");
				}
				prev = '\0';
				continue;
			case '"':
				if ( prev != '\\' ) {
					tmp.append('\\');
				}
				tmp.append(in.charAt(i));
				break;
			case '\n':
				tmp.append("\\n");
				break;
			default:
				tmp.append(in.charAt(i));
				break;
			}
			prev = in.charAt(i);
		}
		return tmp.toString();
	}

	private void createWriter (StartDocument startDoc) {
		group = 0;
		pluralGroup = -1;
		linebreak = startDoc.getLineBreak();
		try {
			tempFile = null;
			// If needed, create the output stream from the path provided
			if ( output == null ) {
				boolean useTemp = false;
				File f = new File(outputPath);
				if ( f.exists() ) {
					// If the file exists, try to remove
					useTemp = !f.delete();
				}
				if ( useTemp ) {
					// Use a temporary output if we can overwrite for now
					// If it's the input file, IFilter.close() will free it before we
					// call close() here (that is if IFilter.close() is called correctly
					tempFile = File.createTempFile("~okapi-26_pofwTmp_", null);
					output = new BufferedOutputStream(new FileOutputStream(tempFile.getAbsolutePath()));
				}
				else { // Make sure the directory exists
					Util.createDirectories(outputPath);
					output = new BufferedOutputStream(new FileOutputStream(outputPath));
				}
			}
			
			// Get the encoding of the original document
			String originalEnc = startDoc.getEncoding();
			// If it's undefined, assume it's the default of the system
			if ( originalEnc == null ) {
				originalEnc = Charset.defaultCharset().name();
			}
			// Check if the output encoding is defined
			if ( encoding == null ) {
				// if not: Fall back on the encoding of the original
				encoding = originalEnc;
			}
			// Create the output
			writer = new OutputStreamWriter(output, encoding);
			// Set default UTF-8 BOM usage
			boolean useUTF8BOM = false; // On all platforms
			// Check if the output encoding is UTF-8
			if ( "utf-8".equalsIgnoreCase(encoding) ) {
				// If the original was UTF-8 too
				if ( "utf-8".equalsIgnoreCase(originalEnc) ) {
					// Check whether it had a BOM or not
					// Most PO-aware tools are Linux and do not like BOM
					useUTF8BOM = false; // startDoc.hasUTF8BOM();
				}
			}
			// Write out the BOM if needed
			Util.writeBOMIfNeeded(writer, useUTF8BOM, encoding);
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiFileNotFoundException(e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		}
	}

}
