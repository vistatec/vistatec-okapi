/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.plaintext.regex;

import java.net.URISyntaxException;
import java.net.URL;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.regex.RegexFilter;
import net.sf.okapi.filters.regex.Rule;
import net.sf.okapi.lib.extra.filters.AbstractBaseFilter;
import net.sf.okapi.lib.extra.filters.AbstractLineFilter;

/**
 * <code>PlainTextFilter</code> extracts lines of input text, separated by line terminators.
 * The filter is aware of the following line terminators:
 * <ul><li>Carriage return character followed immediately by a newline character ("\r\n")
 * <li>Newline (line feed) character ("\n")
 * <li>Stand-alone carriage return character ("\r")
 * <li>Next line character ("\u0085")
 * <li>Line separator character ("\u2028")
 * <li>Paragraph separator character ("\u2029").</ul><p>
 * @version 0.1, 09.06.2009  
 */
public class RegexPlainTextFilter extends AbstractBaseFilter {

	public static final String FILTER_NAME				= "okf_plaintext_regex";
	public static final String FILTER_MIME				= MimeTypeMapper.PLAIN_TEXT_MIME_TYPE;	
	public static final String FILTER_CONFIG			= "okf_plaintext_regex";
	public static final String FILTER_CONFIG_LINES		= "okf_plaintext_regex_lines";
	public static final String FILTER_CONFIG_PARAGRAPHS	= "okf_plaintext_regex_paragraphs";
		
	private InlineCodeFinder codeFinder;
	private RegexFilter regex;			// Regex aggregate
	private Parameters params;			// Regex Plain Text Filter's parameters
	private int lineNumber = 0;
	private RawDocument input;

		
//	public void component_create() {
	public RegexPlainTextFilter() {			
		codeFinder = new InlineCodeFinder();
		
		// Create the regex aggregate and its parameters 
		regex = new RegexFilter(); 
		setParameters(new Parameters());	// Regex Plain Text Filter parameters
		
		addConfiguration(true,
				FILTER_CONFIG,
				"Plain Text (Regex)",
				"Plain Text Filter using regex-based linebreak search. Detects a wider range of " + 
				"linebreaks at the price of lower speed and extra memory usage.", 
				"okf_plaintext_regex.fprm"); // Default, the same as FILTER_CONFIG_LINES
		
		addConfiguration(false,
				FILTER_CONFIG_LINES,
				"Plain Text (Regex, Line=Paragraph)",
				"Plain Text Filter using regex-based linebreak search. Extracts by lines.", 
				"okf_plaintext_regex_lines.fprm");
		
		addConfiguration(false,
				FILTER_CONFIG_PARAGRAPHS,
				"Plain Text (Regex, Block=Paragraph)",
				"Plain Text Filter using regex-based linebreak search. Extracts by paragraphs.", 
				"okf_plaintext_regex_paragraphs.fprm");
		
		net.sf.okapi.filters.regex.Parameters regexParams = new net.sf.okapi.filters.regex.Parameters();
		regex.setParameters(regexParams);
		
		// Load the default line extraction rule from a file to regexParams
		URL url = RegexPlainTextFilter.class.getResource("def_line_extraction_rule.fprm");
        if (url == null) return;
        regexParams.load(url, false);
	}

	/**
	 * Configures an internal line extractor. 
	 * If you want to set a custom rule, call this method with a modified rule.<p> 
	 * @param rule - Java regex rule used to extract lines of text. Default: "^(.*?)$".
	 * @param sourceGroup - regex capturing group denoting text to be extracted. Default: 1.  
	 * @param regexOptions - Java regex options. Default: Pattern.MULTILINE.
	 */
	public void setRule(String rule, int sourceGroup, int regexOptions) {		
		if (rule == null) return;
		if (rule.isEmpty()) return;
		
		Rule regexRule = _getFirstRegexRule();
		if (regexRule == null) return;
		
		regexRule.setExpression(rule);
		regexRule.setSourceGroup(sourceGroup);
		
		net.sf.okapi.filters.regex.Parameters rp = (net.sf.okapi.filters.regex.Parameters) _getRegexParams();
		if (rp == null) return;
		
		rp.setRegexOptions(regexOptions);
		rp.compileRules();
		
		if (this.params == null) return;
		
		this.params.rule = rule;
		this.params.sourceGroup = sourceGroup;
		this.params.regexOptions = regexOptions;
	}
			
	/**
	 * Provides access to the internal line extractor's {@link Parameters} object. 
	 * @return {@link Parameters} object; with this object you can access the line extraction rule, source group, regex options, etc.
	 */
	public net.sf.okapi.filters.regex.Parameters getRegexParameters() {		
		return _getRegexParams();
	}

// IFilter	

	public void cancel() {
		if (input != null) {
			input.close();
		}
		
		if (regex != null) regex.cancel();
	}

	public void close() {		
		if (input != null) {
			input.close();
		}
		
		if (regex != null) regex.close();
	}

	public IFilterWriter createFilterWriter() {		
		return (regex != null) ? regex.createFilterWriter() : null;
	}

	public ISkeletonWriter createSkeletonWriter() {
		
		return (regex != null) ? regex.createSkeletonWriter() : null;
	}

	public String getMimeType() {		
		return FILTER_MIME;
	}
	
	public String getName() {		
		return FILTER_NAME;
	}

	public IParameters getParameters() {		
		return params;
	}

	public boolean hasNext() {		
		return (regex != null) ? regex.hasNext() : null;
	}

	public Event next() {
		// Change the mime type by regex filter ("text/x-regex") to "text/plain"
		
		Event event = regex.next();
		if (event == null) return event; // Returns null
		
		IResource res = event.getResource();
		if (res == null) return event; // Do not change event
		
		if (event.getEventType() == EventType.TEXT_UNIT) {
			ITextUnit textUnit = event.getTextUnit();
			
			// Change mime type
			textUnit.setMimeType(this.getMimeType());
			
			// Lines are what the regex considers the lines, so line numbering is actually TU numbering 
			((ITextUnit)res).setSourceProperty(new Property(AbstractLineFilter.LINE_NUMBER, String.valueOf(++lineNumber), true));
			
			// Automatically replace text fragments with in-line codes (based on regex rules of codeFinder)
			if (params.useCodeFinder && codeFinder != null) {
				// We can use getFirstPartContent() because nothing is segmented yet
				TextContainer source = textUnit.getSource();
				if (source == null) return event;		
				codeFinder.process(source.getFirstContent());
			}
		}
				
		return event;
	}

	public void open(RawDocument input) {		
		open(input, true);
	}

	public void open(RawDocument input, boolean generateSkeleton) {	
		this.input = input;
		
		lineNumber = 0;
		
		if (input == null) 
			throw new OkapiBadFilterInputException("Input RawDocument is not defined.");
		else
			if (regex != null) regex.open(input, generateSkeleton);
		
		// Initialization
		if ( this.params.useCodeFinder && ( codeFinder != null )) {
			codeFinder.fromString(this.params.codeFinderRules);
			codeFinder.compile();
		}
	}

	public void setParameters(IParameters params) {
		super.setParameters(params);
		
		if (params instanceof Parameters) {	// Also checks for null
			this.params = (Parameters)params;
			
			if (this.params != null)
				setRule(this.params.rule, this.params.sourceGroup, this.params.regexOptions); // To compile rules
		}		
	}

	@Override
	protected void component_done() {
	}

	@Override
	protected void component_init() {		
	}
	

// Helpers 
	
	private net.sf.okapi.filters.regex.Parameters _getRegexParams() {		
		IParameters punk;
		
		if (regex == null) return null;
		punk = regex.getParameters();
		
		return (punk instanceof net.sf.okapi.filters.regex.Parameters) ? (net.sf.okapi.filters.regex.Parameters)punk : null; 
	}
	
	private Rule _getFirstRegexRule() {					
		net.sf.okapi.filters.regex.Parameters regexParams = _getRegexParams();
		
		if (regexParams == null) return null;
		if (regexParams.getRules() == null) return null;
		if (regexParams.getRules().isEmpty()) return null;
		
		return regexParams.getRules().get(0);
	}

}	

