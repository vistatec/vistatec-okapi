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

package net.sf.okapi.filters.dtd;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.DTDEncoder;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDComment;
import com.wutka.dtd.DTDEntity;
import com.wutka.dtd.DTDOutput;
import com.wutka.dtd.DTDParser;

/**
 * Implements the IFilter interface for DTD files.
 */
@UsingParameters(Parameters.class)
public class DTDFilter implements IFilter {

	private Parameters params;
	private String encoding;
	private boolean canceled;
	private int parseState;
	private boolean hasUTF8BOM;
	private String docName;
	private Enumeration<?> items;
	private String lineBreak;
	private LocaleId srcLang;
	private int tuId;
	private int otherId;
	private PrintWriter writer;
	private StringWriter strWriter;
	private GenericSkeleton skel;
	private Pattern pattern;
	private Hashtable<String, Character> charEntities;
	private DTDEncoder encoder;
	private EncoderManager encoderManager;
	private RawDocument input;
	
	public DTDFilter () {
		params = new Parameters();

		// Pre-compile pattern for un-escaping
		String tmp = "&#([0-9]*?);|&#[xX]([0-9a-f]*?);|(&\\w*?;)|(%\\w*?;)";
		pattern = Pattern.compile(tmp, Pattern.CASE_INSENSITIVE);
		createCharEntitiesTable();
		encoder = new DTDEncoder();
	}
	
	public void cancel () {
		canceled = true;
	}

	public void close () {
		if (input != null) {
			input.close();
		}
		
		parseState = 0;
		if ( strWriter != null ) {
			strWriter = null;
		}
		if ( writer != null ) {
			writer.close();
			writer = null;
		}
	}

	public String getName () {
		return "okf_dtd";
	}
	
	public String getDisplayName () {
		return "DTD Filter";
	}

	public String getMimeType () {
		return MimeTypeMapper.DTD_MIME_TYPE;
	}

	public Parameters getParameters () {
		return params;
	}

	public boolean hasNext () {
		return (parseState > 0);
	}

	public Event next () {
		// Cancel if requested
		if ( canceled ) {
			parseState = 0;
			return new Event(EventType.CANCELED);
		}
		if ( parseState == 1 ) return start();
		else if (parseState == 2 && !Util.isEmpty(((Parameters)getParameters()).getSimplifierRules())) {
			parseState = 3;
			// load simplifier rules and send as an event							
			Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
			return cs;							
		} else {
			Event event = parse();
			if ( event.getEventType() == EventType.END_DOCUMENT ) {
				parseState = 0;
			}
			return event;
		}
	}

	public void open (RawDocument input) {
		open(input, true);
	}
	
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration("okf_dtd",
			MimeTypeMapper.DTD_MIME_TYPE,
			getClass().getName(),
			"DTD (Document Type Definition)",
			"Configuration for XML DTD documents (entities content)",
			null,
			".dtd;"));
		return list;
	}

	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.DTD_MIME_TYPE, "net.sf.okapi.common.encoder.DTDEncoder");
		}
		return encoderManager;
	}

	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		// keep a reference so we can clean up
		this.input = input;
		
		parseState = 1;
		canceled = false;

		tuId = 0;
		otherId = 0;		
		strWriter = new StringWriter();
		writer = new PrintWriter(strWriter);

		// Compile code finder rules
		if ( params.getUseCodeFinder() ) {
			params.codeFinder.compile();
		}

		Reader rdr = null;
		try {
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
			detector.detectBom();
			input.setEncoding(detector.getEncoding());
			encoding = detector.getEncoding();
			//TODO: check if DTDParser is BOM-aware
			DTDParser parser = new DTDParser(input.getReader());
			
			srcLang = input.getSourceLocale();
			hasUTF8BOM = detector.hasUtf8Bom();
			lineBreak = detector.getNewlineType().toString();
			if ( input.getInputURI() != null ) {
				docName = input.getInputURI().getPath();
			}

			DTD dtd = parser.parse();
			items = dtd.items.elements();
			encoder.setOptions(null, encoding, lineBreak);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error parsing the DTD", e);
		}
		finally {
			if ( rdr != null ) {
				try {
					rdr.close();
				}
				catch ( IOException e ) {
					throw new OkapiIOException("Error when closing the DTD reader.", e);
				}
			}
		}
	}

	private Event start () {
		StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
		startDoc.setFilterParameters(params);
		startDoc.setFilterWriter(createFilterWriter());
		startDoc.setEncoding(encoding, hasUTF8BOM);
		startDoc.setLineBreak(lineBreak);
		startDoc.setLocale(srcLang);
		startDoc.setMimeType(MimeTypeMapper.DTD_MIME_TYPE);
		startDoc.setName(docName);
		parseState = 2;
		return new Event(EventType.START_DOCUMENT, startDoc);
	}
	
	private Event parse () {
		StringBuilder note = null;
		skel = null;
		while ( items.hasMoreElements() ) {
			Object obj = items.nextElement();
			if ( obj instanceof DTDEntity ) {
				DTDEntity tmp = (DTDEntity)obj;
				if ( tmp.isParsed || ( tmp.value == null )) { // Not to extract
					addToSkeleton(tmp, null);
					continue;
				}
				// Otherwise we have value
				ITextUnit tu = new TextUnit(String.valueOf(++tuId));
				tu.setMimeType(MimeTypeMapper.DTD_MIME_TYPE);
				tu.setName(tmp.name);

				TextFragment tf = process(tmp.value);
				if ( params.getUseCodeFinder() ) {
					params.codeFinder.process(tf);
					// Now escape any parts that was changed to code
					List<Code> codes = tf.getCodes();
					for ( Code code : codes ) {
						if ( code.getType().equals(InlineCodeFinder.TAGTYPE) ) {
							code.setData(encoder.encode(code.getData(), EncoderContext.TEXT));
						}
					}
				}
				tu.setSourceContent(tf);
			
				if ( note != null ) {
					tu.setProperty(new Property(Property.NOTE, note.toString()));
				}
				if ( skel == null ) {
					skel = new GenericSkeleton();
				}
				addToSkeleton(tmp, tu);
				tu.setSkeleton(skel);
			
				return new Event(EventType.TEXT_UNIT, tu);
			}
			else if ( obj instanceof DTDComment ) {
				if ( note == null ) note = new StringBuilder();
				else note.append("\n");
				note.append(((DTDComment)obj).text);
				addToSkeleton((DTDOutput)obj);
			}
			else {
				addToSkeleton((DTDOutput)obj);
			}
		}
		// No more entries: end of document
		Ending ending = new Ending(String.valueOf(++otherId));
		if ( skel != null ) {
			ending.setSkeleton(skel);
		}
		return new Event(EventType.END_DOCUMENT, ending);
	}

	private void addToSkeleton (DTDEntity entity,
		ITextUnit tu)
	{
		skel.append("<!ENTITY ");
        if ( entity.isParsed ) {
        	skel.append(" % ");
        }
        skel.append(entity.name);
        skel.append(" ");

        if ( entity.value == null ) {
        	StringWriter sr = new StringWriter();
        	PrintWriter pr = new PrintWriter(sr);
            try {
				entity.externalID.write(pr);
            }
			catch ( IOException e ) {
				throw new OkapiIOException("Error writing externalID of entity.", e);
			}
            skel.append(sr.toString());
            if ( entity.ndata != null ) {
            	skel.append(" NDATA ");
            	skel.append(entity.ndata);
            }
        }
        else {
        	skel.append("\"");
        	if ( tu == null ) {
        		skel.append(entity.value);
        	}
        	else {
        		skel.addContentPlaceholder(tu);
        	}
        	skel.append("\"");
        }
		skel.append(">"+lineBreak);
	}

	private void addToSkeleton (DTDOutput obj) {
		try {
			strWriter.getBuffer().setLength(0);
			obj.write(writer);
			if ( skel == null ) {
				skel = new GenericSkeleton(strWriter.toString());
			}
			else {
				skel.append(strWriter.toString());
			}
		} 
		catch ( IOException e ) {
			throw new OkapiIOException("Error writing skeleton.", e);
		}
	}

	private TextFragment process (String text) {
		int len = text.length();
		Matcher m = pattern.matcher(text);
		int pos = 0;
		String seq = null;
		TextFragment tf = new TextFragment();
		while ( m.find(pos) ) {
			// Copy any previous text
			if ( m.start() > pos ) {
				// Get text before
				tf.append(text.substring(pos, m.start()));
			}
			pos = m.end();

			// Treat the escape sequence
			seq = m.group();
			int value = -1;
			if ( seq.indexOf('x') == 2 ) {
				// Hexadecimal NCR "&#xHHH;"
				value = Integer.parseInt(seq.substring(3, seq.length()-1), 16);
			}
			else if ( seq.indexOf('#') == 1 ) {
				// Decimal NCR "&#DDD;"
				value = Integer.parseInt(seq.substring(2, seq.length()-1));
			}
			else {
				// Character entity reference: &NAME;
				seq = seq.substring(1, seq.length()-1);
				if ( charEntities.containsKey(seq) ) {
					value = (int)charEntities.get(seq);
				}
				else { // Unidentified: leave it like that
					value = -1;
				}
			}
			if ( value > -1 ) tf.append((char)value);
			else {
				tf.append(TagType.PLACEHOLDER, "x-ref", m.group());
			}
		}
		
		if ( seq == null ) { // No codes
			tf.append(text);
		}
		else { // We had at least one match
			if ( pos < len ) {
				// Get text before
				tf.append(text.substring(pos, len));
			}
		}
		
		return tf;
	}
	
	private void createCharEntitiesTable () {
		charEntities = new Hashtable<String, Character>();
		charEntities.put("amp", '&');
		charEntities.put("lt", '<');
		charEntities.put("gt", '>');
		charEntities.put("quot", '\"');
		charEntities.put("apos", '\'');
	}

}
