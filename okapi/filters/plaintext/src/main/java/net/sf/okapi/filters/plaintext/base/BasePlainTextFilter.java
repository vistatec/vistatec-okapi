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

package net.sf.okapi.filters.plaintext.base;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.Event;
//import java.util.List;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
//import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.filters.SubFilter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.SkeletonUtil;
import net.sf.okapi.lib.extra.filters.AbstractLineFilter;
import net.sf.okapi.lib.extra.filters.TextProcessingResult;

/**
 * <code>PlainTextFilter</code> extracts lines of input text, separated by line terminators.
 * 
 * @version 0.1, 09.06.2009
 */
public class BasePlainTextFilter extends AbstractLineFilter {	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String FILTER_NAME				= "okf_plaintext";
	public static final String FILTER_MIME				= MimeTypeMapper.PLAIN_TEXT_MIME_TYPE;	
	public static final String FILTER_CONFIG			= "okf_plaintext";
	public static final String FILTER_CONFIG_TRIM_TRAIL	= "okf_plaintext_trim_trail";
	public static final String FILTER_CONFIG_TRIM_ALL	= "okf_plaintext_trim_all";
	
	private Parameters params; // Base Plain Text Filter parameters
	private InlineCodeFinder codeFinder;
	private IdGenerator idGenerator;
	private IFilter subFilter;
	private int subfilterIndex;
	private FilterConfigurationMapper fcmapper;
		
	public BasePlainTextFilter() {
		
		codeFinder = new InlineCodeFinder();
		
		setName(FILTER_NAME);
		setDisplayName("Plain Text Filter (BETA)");
		setMimeType(FILTER_MIME);
		setMultilingual(false);
		
		addConfiguration(true, 
				FILTER_CONFIG,
				"Plain Text",
				"Plain text files.", 
				null,
				".txt;");
		
		addConfiguration(false, 
				FILTER_CONFIG_TRIM_TRAIL,
				"Plain Text (Trim Trail)",
				"Text files; trailing spaces and tabs removed from extracted lines.", 
				"okf_plaintext_trim_trail.fprm");
		
		addConfiguration(false, 
				FILTER_CONFIG_TRIM_ALL,
				"Plain Text (Trim All)",
				"Text files; leading and trailing spaces and tabs removed from extracted lines.", 
				"okf_plaintext_trim_all.fprm");
		
		setParameters(new Parameters());	// Base Plain Text Filter parameters
	}
	
	@Override
	protected void component_init() {
		
		// Commons, should be included in all descendants introducing own params
		params = getParameters(Parameters.class);	// Throws OkapiBadFilterParametersException		

		super.component_init();
		
		// Initialization
		if ( params.useCodeFinder && ( codeFinder != null )) {
			codeFinder.fromString(params.codeFinderRules);
			codeFinder.compile();
		}
		
		idGenerator = new IdGenerator(null, IdGenerator.TEXT_UNIT);
		idGenerator.createId(IdGenerator.TEXT_UNIT);
		
		subfilterIndex = 0;		
		if ( params.subfilter != null ) {
			fcmapper = new FilterConfigurationMapper();
			DefaultFilters.setMappings(fcmapper, true, true);
			subFilter = fcmapper.createFilter(params.subfilter, subFilter);
		}
	}
	
	protected final TextProcessingResult sendAsSource(ITextUnit textUnit) {
		return sendAsSource(textUnit, true);
	}
	
	protected final TextProcessingResult sendAsSource(ITextUnit textUnit, boolean rejectEmpty) {
		
		if (textUnit == null) return TextProcessingResult.REJECTED;
		TextUnitUtil.forceSkeleton(textUnit);
		
		if (!processTU(textUnit)) return TextProcessingResult.REJECTED;
		
		if (rejectEmpty && isEmpty(textUnit)) return TextProcessingResult.REJECTED;
		
		if (params.subfilter != null) {
			List<Event> events = callSubfilter(textUnit);	
			GenericSkeleton[] beforeAndAfter = SkeletonUtil.splitSkeleton((GenericSkeleton) textUnit.getSkeleton());
			for (Event e : events) {				
				switch(e.getEventType()) {
					case START_SUBFILTER:
						// add before reference i.e. ", ' etc..
						e.getStartSubfilter().setSkeleton(beforeAndAfter[0]);
						break;
					case END_SUBFILTER:
						// add after reference i.e. ", ' etc..
						e.getEndSubfilter().setSkeleton(beforeAndAfter[1]);
						break;
					case TEXT_UNIT:
						// copy over the previous TU's source properties
						Set<String> properties = textUnit.getSourcePropertyNames();
						for (String p : properties) {
							e.getTextUnit().setSourceProperty(textUnit.getSourceProperty(p));
						}
						break;
					default:
						break;
				}
				sendEvent(e.getEventType(), e.getResource());
			}				
			textUnit.setSkeleton(null);
			return TextProcessingResult.SUBFILTER;
		}
		
		sendEvent(EventType.TEXT_UNIT, textUnit);
		
		return TextProcessingResult.ACCEPTED;
	}
	
	protected final TextProcessingResult sendAsSource(TextContainer textContainer) {
		if (textContainer == null) return TextProcessingResult.REJECTED;
		return sendAsSource(TextUnitUtil.buildTU(null, "", textContainer, null, LocaleId.EMPTY, ""));
	}
	
	protected final TextProcessingResult sendAsTarget(ITextUnit target,
		ITextUnit source,
		LocaleId language)
	{
		if ( target == null ) return TextProcessingResult.REJECTED;
		if ( source == null ) return TextProcessingResult.REJECTED;
		if ( language == null ) return TextProcessingResult.REJECTED;
		
		GenericSkeleton skel = getActiveSkeleton();
		if (skel == null) return TextProcessingResult.REJECTED;
				
		GenericSkeleton targetSkel = TextUnitUtil.forceSkeleton(target);
		if ( targetSkel == null ) return TextProcessingResult.REJECTED;
		if ( !processTU(target) ) return TextProcessingResult.REJECTED;
		
		source.setTarget(language, target.getSource());
	
		int index = SkeletonUtil.findTuRefInSkeleton(targetSkel);
		
		if ( index != -1 ) {
			GenericSkeleton tempSkel = new GenericSkeleton();
			tempSkel.addContentPlaceholder(source, language);
			SkeletonUtil.replaceSkeletonPart(targetSkel, index, tempSkel);
		}
		skel.add(targetSkel);
		
		if (params.subfilter != null) {
			logger.error("When a target column is defined a subfilter is not supported.");
		}
		
		return TextProcessingResult.ACCEPTED;
	}
	
	protected final TextProcessingResult sendAsSkeleton(ITextUnit textUnit) {
		GenericSkeleton parentSkeleton = getActiveSkeleton();
		if (parentSkeleton == null) return TextProcessingResult.REJECTED;
		
		parentSkeleton.add(TextUnitUtil.convertToSkeleton(textUnit));
		return TextProcessingResult.ACCEPTED;
	}
	
	protected final TextProcessingResult sendAsSkeleton(GenericSkeleton skelPart) {
		
		if (skelPart == null) return TextProcessingResult.REJECTED;
		
		GenericSkeleton activeSkel = getActiveSkeleton();
		if (activeSkel == null) return TextProcessingResult.REJECTED;
		
		activeSkel.add(skelPart);
		
		return TextProcessingResult.ACCEPTED;
	}
	
	protected final TextProcessingResult sendAsSkeleton(String skelPart) {
		
		if (skelPart == null) return TextProcessingResult.REJECTED;
		
		GenericSkeleton activeSkel = getActiveSkeleton();
		if (activeSkel == null) return TextProcessingResult.REJECTED;
		
		activeSkel.add(skelPart);
		
		return TextProcessingResult.ACCEPTED;
	}

	protected boolean processTU(ITextUnit textUnit) {
		
		if (textUnit == null) return false;
		TextContainer source = textUnit.getSource();
		if (source == null) return false;		
				
		if (!checkTU(textUnit)) return false;
		
		// We can use getFirstPartContent() because nothing is segmented yet
		if (params.unescapeSource) _unescape(source.getFirstContent());
		
		//------------------------------
		// The cell can already have something in the skeleton (for instance, a gap after the source)
		TextUnitUtil.trimTU(textUnit, params.trimLeading, params.trimTrailing);
		

		textUnit.setMimeType(getMimeType());
		textUnit.setPreserveWhitespaces(params.preserveWS);
		
		if (!params.preserveWS ) {
			// Unwrap the content
			source.unwrap(true, true);			
		}
		
		// Automatically replace text fragments with in-line codes (based on regex rules of codeFinder)
		// codefinder has priority over subfilter
		if (params.useCodeFinder && codeFinder != null && params.subfilter == null) {
			// We can use getFirstPartContent() because nothing is segmented yet
			codeFinder.process(source.getFirstContent());			
		}
		
		return true;
	}
	
	@SuppressWarnings("resource")
	protected List<Event> callSubfilter(ITextUnit parent) {				
		String parentId = parent.getId();
		if (Util.isEmpty(parentId)) { 
			parentId = idGenerator.getLastId();
		}
		
		// force creation of the parent encoder
		SubFilter sf = new SubFilter(subFilter,
				getEncoderManager().getEncoder(),
				++subfilterIndex, parentId, parent.getName());
		
		// RawDocument closed inside the subfilter call
		return sf.getEvents(new RawDocument(parent.getSource().getFirstContent().getText(), srcLang));
	}
	
	protected boolean checkTU(ITextUnit textUnit) {
		// Can be overridden in descendant classes		
		return true;
	}	
	
	protected boolean isEmpty(ITextUnit textUnit) {
		// Can be overridden in descendant classes
		//return false;
		return TextUnitUtil.isEmpty(textUnit);
	}
	
	@Override
	protected TextProcessingResult component_exec(TextContainer lineContainer) {
				
		return sendAsSource(lineContainer);		
	}
	
// Helpers	

	/**
	 * Unescapes slash-u+HHHH characters in a TextFragment.
	 * @param textFrag the TextFragment to convert.
	 */
	private void _unescape (TextFragment textFrag) {
		// Cannot be static because of the logger
		
		final String INVALID_UESCAPE = "Invalid Unicode escape sequence '{}'";
		
		if (textFrag == null) return;
		
		String text = textFrag.getCodedText(); 
		if (Util.isEmpty(text)) return;
		
		if ( text.indexOf('\\') == -1 ) return; // Nothing to unescape
		
		StringBuilder tmpText = new StringBuilder();
		
		for ( int i = 0; i < text.length(); i++ ) {
			if ( text.charAt(i) == '\\' ) {
				switch (Util.getCharAt(text, i+1)) {
				
				case 'u':
					if ( i+5 < text.length() ) {
						try {
							int nTmp = Integer.parseInt(text.substring(i+2, i+6), 16);
							tmpText.append((char)nTmp);
						}
						catch ( Exception e ) {
							logger.warn(INVALID_UESCAPE, text.substring(i+2, i+6));
						}
						i += 5;
						continue;
					}
					else {
						logger.warn(INVALID_UESCAPE, text.substring(i+2));
					}
					break;
				case '\\':
					tmpText.append("\\\\");
					i++;
					continue;
				default: // Stand-alone "\"
					tmpText.append(text.charAt(i));
				}				
			}
			else tmpText.append(text.charAt(i));
		}
		
		textFrag.setCodedText(tmpText.toString());
	}

	@Override
	protected void component_done() {
		
	}
}	
