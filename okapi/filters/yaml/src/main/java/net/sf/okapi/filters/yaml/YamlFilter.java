/*===========================================================================
  Copyright (C) 2009-2014 by the Okapi Framework contributors
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

package net.sf.okapi.filters.yaml;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
import net.sf.okapi.common.filters.AbstractFilter;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.SubFilter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.yaml.parser.IYamlHandler;
import net.sf.okapi.filters.yaml.parser.Key;
import net.sf.okapi.filters.yaml.parser.Line;
import net.sf.okapi.filters.yaml.parser.ParseException;
import net.sf.okapi.filters.yaml.parser.Scalar;
import net.sf.okapi.filters.yaml.parser.StreamProvider;
import net.sf.okapi.filters.yaml.parser.TokenMgrException;
import net.sf.okapi.filters.yaml.parser.YamlNodeTypes;
import net.sf.okapi.filters.yaml.parser.YamlParser;
import net.sf.okapi.filters.yaml.parser.YamlScalarTypes;

/**
 * Implements the IFilter interface for YAML files.
 */
@UsingParameters(Parameters.class)
public class YamlFilter extends AbstractFilter implements IYamlHandler {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String YAML_SCALAR_TYPE_PROPERTY_NAME = "Scalar Type";
	public static final String YAML_PARENT_INDENT_PROPERTY_NAME = "Parent Scalar Indent";
	public static final String YAML_SCALAR_FLOW_PROPERTY_NAME = "Scalar Flow";	
	
	/*
	 * Typical whitespace space (U+0020) tab (U+0009) form feed (U+000C) line feed
	 * (U+000A) carriage return (U+000D) zero-width space (U+200B) (IE6 does not
	 * recognize these, they are treated as unprintable characters)
	 */
	private static final String WHITESPACE_REGEX = "[ \t\r\n\f\u200B\u000C]+";
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile(WHITESPACE_REGEX);

	private boolean hasUtf8Bom;
	private boolean hasUtf8Encoding;
	private Parameters params;
	private YamlEventBuilder eventBuilder;
	private EncoderManager encoderManager;
	private IFilter subFilter;
	private Stack<Key> keyStack;
	private YamlNodeTypes currentNodeType;
	private Key currentKey;
	private Pattern exceptions;
	private int subfilterIndex;
	private RawDocument input;
	private YamlParser parser;
	private int extraIndent;
	
	public YamlFilter() {
		setMimeType(MimeTypeMapper.YAML_MIME_TYPE);
		setName("okf_yaml");
		setDisplayName("YAML Filter");
		addConfiguration(new FilterConfiguration(getName(), MimeTypeMapper.YAML_MIME_TYPE,
				getClass().getName(), "YAML",
				"YAML files", null, ".yml;.yaml"));	
		
		setParameters(new Parameters());
		// must be called *after* parameters is initialized
		setFilterWriter(createFilterWriter());
	}

	@Override
	public void close() {
		super.close();	
		hasUtf8Bom = false;
		hasUtf8Encoding = false;
		if (input != null) {
			input.close();
		}
		if (keyStack != null) {
			keyStack.clear();
		}
		currentNodeType = YamlNodeTypes.UNKOWN;
		currentKey = null;
	}
	
	@Override
	public boolean hasNext() {
		return eventBuilder.hasNext();
	}

	@Override
	public Event next () {
		return eventBuilder.next();
	}

	@Override
	public void open (RawDocument input) {
		currentNodeType = YamlNodeTypes.UNKOWN;
		open(input, true);
	}

	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		// save reference for clean up
		this.input = input;
		
		super.open(input, generateSkeleton);
		
		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
		detector.detectAndRemoveBom();
		String encoding = detector.getEncoding();
		String linebreak = detector.getNewlineType().toString();
		hasUtf8Bom = detector.hasUtf8Bom();
		hasUtf8Encoding = detector.hasUtf8Encoding();
		input.setEncoding(encoding);
		setEncoding(encoding);
		setNewlineType(linebreak);
		setOptions(input.getSourceLocale(), input.getTargetLocale(), encoding, generateSkeleton);
		
		BufferedReader reader = null;		
		try {
			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), encoding));
		}
		catch (UnsupportedEncodingException e) {
			throw new OkapiUnsupportedEncodingException(
				String.format("The encoding '%s' is not supported.", encoding), e);
		}
		
		if (input.getInputURI() != null) {
			setDocumentName(input.getInputURI().getPath());
		}				
		
		// Pre-compile exceptions or set them to null
		if (Util.isEmpty(params.getExceptions())) {
			exceptions = null;
		}
		else {
			exceptions = Pattern.compile(params.getExceptions());
		}
						
		// create EventBuilder with document name as rootId
		if (eventBuilder == null) {
			eventBuilder = new YamlEventBuilder(getParentId(), this);
		} else {
			eventBuilder.reset(getParentId(), this);
		}
		eventBuilder.setMimeType(MimeTypeMapper.YAML_MIME_TYPE);
		eventBuilder.setPreserveWhitespace(true);
		
		// Compile code finder rules
		if (params.getUseCodeFinder()) {
			params.getCodeFinder().compile();
			eventBuilder.setCodeFinder(params.getCodeFinder());
		}
		
		// Initialize the subfilter
		if (!params.getUseCodeFinder()) {
			String subFilterName = params.getSubfilter();
			if (subFilterName != null && !"".equals(subFilterName)) {
				subFilter = getFilterConfigurationMapper().createFilter(subFilterName, subFilter);
			}
		}
		subfilterIndex = 0;
		
		keyStack = new Stack<Key>();
		currentKey = null;
		
		parser = new YamlParser(new StreamProvider(reader));
		parser.setHandler(this);		
		try {
			parser.parse();
		} catch (ParseException|TokenMgrException e) {
			throw new OkapiBadFilterInputException(String.format("Error parsing YAML file: %s", e.getMessage()), e);
		}
	}

	@Override
	public Parameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
		// may be new parameter options for skeleton writer and encoder
		createSkeletonWriter();
		getEncoderManager();
	}
	
	@Override
	public EncoderManager getEncoderManager() {
		if (encoderManager == null) {			
			encoderManager = super.getEncoderManager();
			encoderManager.setMapping(MimeTypeMapper.YAML_MIME_TYPE, "net.sf.okapi.filters.yaml.YamlEncoder");
			encoderManager.setDefaultOptions(getParameters(), getEncoding(), getNewlineType());
		}
		encoderManager.setOptions(getParameters(), getEncoding(), getNewlineType());
		encoderManager.updateEncoder(MimeTypeMapper.YAML_MIME_TYPE);
		return encoderManager;
	}
	
	@Override
	public ISkeletonWriter createSkeletonWriter() {
		return new YamlSkeletonWriter(params.isWrap());
	}
	
	@Override
	protected boolean isUtf8Encoding() {
		return hasUtf8Encoding;
	}

	@Override
	protected boolean isUtf8Bom() {
		return hasUtf8Bom;
	}	

	@Override
	public void handleStart() {
		// add StarDocument event	
		setFilterWriter(createFilterWriter());
		eventBuilder.addFilterEvent(createStartFilterEvent());	
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(params.getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(params.getSimplifierRules());
			eventBuilder.addFilterEvent(cs);
		}	
	}
	
	@Override
	public void handleEnd() {
		// clear out all temp events
		eventBuilder.flushRemainingTempEvents();		
		// add the final endDocument event
		eventBuilder.addFilterEvent(createEndFilterEvent());
		keyStack.clear();
		currentNodeType = YamlNodeTypes.UNKOWN;
		currentKey = null;
	}

	@Override
	public void handleComment(String c, boolean insideScalar) {
		if (c == null) return;
		eventBuilder.addToDocumentPart(c);
	}

	@Override
	public void handleKey(Key key) {		
		extraIndent = 0;
		eventBuilder.addToDocumentPart(key.key);
		key.nodeType = currentNodeType;
		currentKey = key;
		keyStack.push(currentKey == null ? new Key() : currentKey);
	}

	@Override
	public void handleWhitespace(String whitespace, boolean isInsideScalar) {
		if (whitespace == null) return;
		eventBuilder.addToDocumentPart(whitespace);
	}

	@Override
	public void handleScalar(Scalar scalar) {
		// use local values and reset fields
		// cleaner to do it once here
		Key key = currentKey == null ? new Key() : currentKey;
		currentKey = null;
		
		if (!params.getExtractStandalone() && key == null) {
			eventBuilder.addDocumentPart(scalar.getOriginalString());
			return;
		}
		
		String resName = getKeyNames(key);
		boolean extract = params.getExtractAllPairs();
		if ( exceptions != null ) {
			if ( exceptions.matcher(resName).find() ) {
				// It's an exception, so we reverse the extraction flag
				extract = !extract;
			}
		}
				
		if ( !extract ) { // Not to extract
			eventBuilder.addToDocumentPart(scalar.getOriginalString());		
			return;
		}
		
		// This is for subfilter as well to avoid duplication below
		eventBuilder.addDocumentPart(scalar.type.getQuoteChar());
		
		// call the subfilter 
		if (subFilter != null) {
			// we handle LITERAL differently by processing each line as its own TextUnit.
			if (scalar.type == YamlScalarTypes.LITERAL) {
				for (Line l : scalar.getTranslatableStrings()) {
					if (l.isSkeleton) {
						eventBuilder.addToDocumentPart(l.line);			
					} else if (l.line != null && !l.line.isEmpty()) {	
						callSubfilter(l.line, scalar, key, resName);
					}
				}
			} else {
				// set the preserve whitespace based on the scalar type
				String normalize = StringUtil.removeAnyQualifiers(Line.decode(scalar.getOriginalString()));
				normalize = collapseWhitespace(normalize);
				callSubfilter(normalize, scalar, key, resName);
			}
			eventBuilder.addDocumentPart(scalar.type.getQuoteChar());
			return;
		}
		
		// loop over the lines in the Yaml scalar
		// any lines with whitespace only are skeleton
		// decoding and whitespace adjustments have already
		// been done in the parser		
		int fullIndent = key.indent+extraIndent;
		if (scalar.indentedBlock != null && !scalar.indentedBlock.isEmpty()) {
			fullIndent = scalar.indentedBlock.firstIndent;
		}				
		Property scalarType =  new Property(YAML_SCALAR_TYPE_PROPERTY_NAME, scalar.type.name());
		Property flow = new Property(YAML_SCALAR_FLOW_PROPERTY_NAME, Boolean.toString(scalar.flow));
		Property indent = new Property(YAML_PARENT_INDENT_PROPERTY_NAME, Integer.toString(fullIndent));

		// scalar types keep the newlines as inline codes
		eventBuilder.startTextUnit();
		eventBuilder.setTextUnitName(resName);
		for (Line l : scalar.getTranslatableStrings()) {
			if (l.isSkeleton) {
				// remove spaces as these will be readded by skeleton writer, just keep newlines
				eventBuilder.addToTextUnit(new Code(TagType.PLACEHOLDER, Code.TYPE_LB, l.line));
			} else if (l.line != null && !l.line.isEmpty()) {
				eventBuilder.addToTextUnit(Util.normalizeNewlines(l.line));
			}
		}
		ITextUnit tu = eventBuilder.peekMostRecentTextUnit();
		tu.setProperty(scalarType);
		tu.setProperty(flow);
		tu.setProperty(indent);
		eventBuilder.endTextUnit();
		eventBuilder.addDocumentPart(scalar.type.getQuoteChar());
		extraIndent = 0;
		logger.debug("KEYNAME: " + resName + ": " + scalar.getOriginalString());	
	}
	
	@SuppressWarnings("resource")
	private void callSubfilter(String subtext, Scalar scalar, Key key, String parentName) {				
		String parentId = eventBuilder.findMostRecentParentId();
		if (parentId == null) { 
			parentId = getDocumentId().getLastId();
		}
		
		// force creation of the parent encoder
		YamlEncoder parentEncoder = new YamlEncoder();
		parentEncoder.setScalarType(scalar.type);
		
		// create subfilter with the updated Yaml encoder
		SubFilter sf = new SubFilter(subFilter,
				parentEncoder,
				++subfilterIndex, parentId, parentName);
		
		int fullIndent = key.indent+extraIndent;
		if (scalar.indentedBlock != null && !scalar.indentedBlock.isEmpty()) {
			fullIndent = scalar.indentedBlock.firstIndent;
		}
		Property scalarType =  new Property(YAML_SCALAR_TYPE_PROPERTY_NAME, scalar.type.name());
		Property flow = new Property(YAML_SCALAR_FLOW_PROPERTY_NAME, Boolean.toString(scalar.flow));
		Property indent = new Property(YAML_PARENT_INDENT_PROPERTY_NAME, Integer.toString(fullIndent));
				
		List<Event> subFilterEvents = sf.getEvents(new RawDocument(subtext, getSrcLoc())); 
		for (Event event : subFilterEvents) {
			if (event.isTextUnit()) {
				event.getTextUnit().setProperty(scalarType);
				event.getTextUnit().setProperty(flow);
				event.getTextUnit().setProperty(indent);
				if (scalar.type == YamlScalarTypes.LITERAL) {
					event.getTextUnit().setPreserveWhitespaces(true);
				} else {
					event.getTextUnit().setPreserveWhitespaces(false);
				}
			}
		}
		eventBuilder.addFilterEvents(subFilterEvents);							
		eventBuilder.addToDocumentPart(sf.createRefCode().toString());
	}
	
	private String collapseWhitespace(String text) {
		return WHITESPACE_PATTERN.matcher(text).replaceAll(" ");
	}

	@Override
	public void handleMapStart(boolean flow) {
		eventBuilder.startGroup(new GenericSkeleton(flow ? "{" : ""), "Yaml Map Start");		
		currentNodeType = flow ? YamlNodeTypes.FLOW_MAP : YamlNodeTypes.BLOCK_MAP;
		extraIndent = 0;
	}

	@Override
	public void handleMapEnd(boolean flow) {
		eventBuilder.endGroup(new GenericSkeleton(flow ? "}" : ""));
	}

	@Override
	public void handleSequenceStart(boolean flow) {
		eventBuilder.startGroup(new GenericSkeleton(flow ? "[" : ""), "Yaml List Start");
		currentNodeType = flow ? YamlNodeTypes.FLOW_SEQUENCE : YamlNodeTypes.BLOCK_SEQUENCE;
		extraIndent = 0;
	}

	@Override
	public void handleSequenceEnd(boolean flow) {
		eventBuilder.endGroup(new GenericSkeleton(flow ? "]" : ""));		
	}

	@Override
	public void handleMarker(String marker) {
		if (marker == null) return;
		eventBuilder.addToDocumentPart(marker);		
	}
	
	private String getKeyNames(Key key) {
		StringBuilder keyPath = new StringBuilder();
		
		if (!params.getUseKeyAsName()) {
			return null;
		}
		
		if (!params.getUseFullKeyPath()) {
			return key.getKeyName();
		}
		
		if (!keyStack.isEmpty()) {
			Iterator<Key> it = keyStack.listIterator();
			while (it.hasNext()) {
				Key k = it.next();
				if (k != null && !k.isEmpty()) {
					if (keyPath.length() > 0) {
						keyPath.append("/");
					}
					keyPath.append(k.getKeyName());
				}
			}			
		}
		
		return keyPath.toString();
	}

	@Override
	public void handleOther(String other) {
		if (other == null) return;
		eventBuilder.addToDocumentPart(other);
	}

	@Override
	public void handleDocumentStart(String start) {	
		keyStack.clear();
		currentNodeType = YamlNodeTypes.UNKOWN;
		currentKey = null;
		eventBuilder.addToDocumentPart(start);
		extraIndent = 0;
	}

	@Override
	public void handleDocumentEnd(String end) {
		keyStack.clear();
		currentNodeType = YamlNodeTypes.UNKOWN;
		currentKey = null;
		eventBuilder.addToDocumentPart(end);
		extraIndent = 0;
	}

	@Override
	public void handleMappingElementEnd() {
		keyStack.pop();				
	}

	@Override
	public void handleBlockSequenceNodeStart(String dash, int indent) {
		eventBuilder.addToDocumentPart(dash);
		extraIndent = indent;
	}
}
