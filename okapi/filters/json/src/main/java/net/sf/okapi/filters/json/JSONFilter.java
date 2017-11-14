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

package net.sf.okapi.filters.json;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.encoder.JSONEncoder;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;
import net.sf.okapi.common.filters.AbstractFilter;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.SubFilter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.json.parser.IJsonHandler;
import net.sf.okapi.filters.json.parser.JsonKeyTypes;
import net.sf.okapi.filters.json.parser.JsonParser;
import net.sf.okapi.filters.json.parser.JsonValueTypes;
import net.sf.okapi.filters.json.parser.ParseException;
import net.sf.okapi.filters.json.parser.StreamProvider;
import net.sf.okapi.filters.json.parser.TokenMgrException;

/**
 * Implements the IFilter interface for JSON files.
 */
@UsingParameters(Parameters.class)
public class JSONFilter extends AbstractFilter implements IJsonHandler {
	private static final String MIMETYPE = "application/json";
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private boolean hasUtf8Bom;
	private boolean hasUtf8Encoding;
	private Parameters params;
	private JsonEventBuilder eventBuilder;
	private EncoderManager encoderManager;
	private IFilter subFilter;
	private Stack<KeyAndType> keyNames;
	private String currentKeyName;
	private JsonKeyTypes currentKeyType;
	private Pattern exceptions;
	private int subfilterIndex;
	private RawDocument input;
	
	private class KeyAndType {
		public KeyAndType(String name, JsonKeyTypes type) {
			this.name = name;
			this.type = type;
		}
		String name;
		JsonKeyTypes type;
	}
	
	public JSONFilter () {
		super();
		setMimeType(MIMETYPE);
		setMultilingual(false);
		setName("okf_json"); //$NON-NLS-1$
		setDisplayName("Json Filter"); //$NON-NLS-1$
		addConfiguration(new FilterConfiguration(
				getName(),
				MIMETYPE,
				getClass().getName(),
				"JSON (JavaScript Object Notation)",
				"Configuration for JSON files",
				null,
				".json;"));
		setParameters(new Parameters());
	}

	@Override
	public void close() {
		super.close();	
		hasUtf8Bom = false;
		hasUtf8Encoding = false;
		if (input != null) {
			input.close();
		}
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
			eventBuilder = new JsonEventBuilder(getParentId(), this);
		} else {
			eventBuilder.reset(getParentId(), this);
		}
		eventBuilder.setMimeType(MIMETYPE);
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
		
		keyNames = new Stack<KeyAndType>();
		currentKeyName = null;
		currentKeyType = JsonKeyTypes.DEFAULT;
		
		JsonParser parser = new JsonParser(new StreamProvider(reader));
		parser.setHandler(this);		
		try {
			parser.parse();
		} catch (ParseException|TokenMgrException e) {
			throw new OkapiBadFilterInputException(String.format("Error parsing JSON file: %s", e.getMessage()), e);
		}
	}

	@Override
	public Parameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}
	
	@Override
	public EncoderManager getEncoderManager () {
		if (encoderManager == null) {			
			encoderManager = super.getEncoderManager();
			encoderManager.setMapping(MIMETYPE, "net.sf.okapi.common.encoder.JSONEncoder");
		}
		return encoderManager;
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
	}

	@Override
	public void handleComment(String c) {
		eventBuilder.addDocumentPart(c);
	}

	@Override
	public void handleKey(String key, JsonValueTypes valueType, JsonKeyTypes keyType) {		
		eventBuilder.addDocumentPart(String.format("%s%s%S", 
				valueType.getQuoteChar(), key, valueType.getQuoteChar()));
			currentKeyName = key;
			currentKeyType = keyType;
	}

	@Override
	public void handleWhitespace(String whitespace) {
		eventBuilder.addDocumentPart(whitespace);
	}

	@Override
	public void handleValue(String value, JsonValueTypes valueType) {
		// use local values and reset fields
		// cleaner to do it once here
		String key = currentKeyName;
		// Not used: JsonKeyTypes keyType = currentKeyType;		
		currentKeyName = null;
		currentKeyType = JsonKeyTypes.DEFAULT;		

		if (!params.getExtractStandalone() && key == null) {
			eventBuilder.addDocumentPart(String.format("%s%s%s", 
					valueType.getQuoteChar(), value, valueType.getQuoteChar()));
			return;
		}
		
		// Skip by these value types by default
		switch (valueType) {		
		case BOOLEAN:
		case NULL:
		case NUMBER:
		case SYMBOL:
			eventBuilder.addDocumentPart(value);
			return;
		default:
			break;
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
			eventBuilder.addDocumentPart(String.format("%s%s%s", 
					valueType.getQuoteChar(), value, valueType.getQuoteChar()));		
			return;
		}
		
		if (subFilter != null) {
			callSubfilter(value, valueType, resName);
			return;
		}
		
		switch (valueType) {
		case DOUBLE_QUOTED_STRING:							
		case SINGLE_QUOTED_STRING:
			eventBuilder.startTextUnit(new GenericSkeleton(valueType.getQuoteChar()));
			eventBuilder.addToTextUnit(value);			
			eventBuilder.setTextUnitName(resName);
			eventBuilder.endTextUnit(new GenericSkeleton(valueType.getQuoteChar()));
			break;
		case SYMBOL:
		case NUMBER:
			eventBuilder.startTextUnit(value);
			eventBuilder.setTextUnitName(resName);
			eventBuilder.endTextUnit();
			break;		
		default:
			break;
		}		
		logger.debug("KEYNAME: " + resName + " : " + value);	
	}
	
	@SuppressWarnings("resource")
	private void callSubfilter(String value, JsonValueTypes valueType, String parentName) {				
		String parentId = eventBuilder.findMostRecentParentId();
		if (parentId == null) { 
			parentId = getDocumentId().getLastId();
		}
		
		// force creation of the parent encoder
		JSONEncoder subEncoder = new JSONEncoder();
		subEncoder.setOptions(params, this.getEncoding(), this.getNewlineType());
		SubFilter sf = new SubFilter(subFilter,
				subEncoder,
				++subfilterIndex, parentId, parentName);
		
		// RawDocument closed inside the subfilter call
		eventBuilder.addFilterEvents(sf.getEvents(
				new RawDocument(eventBuilder.decode(value), getSrcLoc(), getTrgLoc())));
		// Now write out the json skeleton
		eventBuilder.addToDocumentPart(valueType.getQuoteChar());
		eventBuilder.addToDocumentPart(sf.createRefCode().toString());
		eventBuilder.addToDocumentPart(valueType.getQuoteChar());
	}

	@Override
	public void handleObjectStart() {
		eventBuilder.startGroup(new GenericSkeleton("{"), "Json Object Start");
		keyNames.push(new KeyAndType(currentKeyName, currentKeyType));
		currentKeyName = null;
		currentKeyType = JsonKeyTypes.DEFAULT;
	}

	@Override
	public void handleObjectEnd() {
		eventBuilder.endGroup(new GenericSkeleton("}"));
		keyNames.pop();
	}

	@Override
	public void handleListStart() {
		eventBuilder.startGroup(new GenericSkeleton("["), "Json List Start");
		keyNames.push(new KeyAndType(currentKeyName, currentKeyType));
		currentKeyName = null;
		currentKeyType = JsonKeyTypes.DEFAULT;
	}

	@Override
	public void handleListEnd() {
		eventBuilder.endGroup(new GenericSkeleton("]"));		
		keyNames.pop();		
	}

	@Override
	public void handleSeparator(String separator) {
		eventBuilder.addDocumentPart(separator);		
	}
	
	private String getKeyNames(String key) {
		StringBuilder keyPath = new StringBuilder();
		
		if (!params.getUseKeyAsName()) {
			return null;
		}
		
		if (!params.getUseFullKeyPath()) {
			// all values in a list use the immediate parent list key name
			if (!keyNames.isEmpty() && keyNames.peek().type == JsonKeyTypes.LIST) {
				return keyNames.peek().name;
			}
			return key;
		}
		
		if (!keyNames.isEmpty()) {
			Iterator<KeyAndType> it = keyNames.listIterator();
			while (it.hasNext()) {
				KeyAndType k = it.next();
				if (k != null && k.name != null) {
					keyPath.append("/").append(k.name);
				}
			}			
		}
		
		if (key != null && !key.isEmpty()) {
			keyPath.append("/").append(key);
		}
		
		if ( !params.getUseLeadingSlashOnKeyPath() ) {
			if ( keyPath.charAt(0) == '/' ) {
				keyPath.deleteCharAt(0);
			}
		}
		
		return keyPath.toString();
	}
}
