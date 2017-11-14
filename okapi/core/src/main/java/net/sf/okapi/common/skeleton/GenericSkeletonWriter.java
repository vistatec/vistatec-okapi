/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.common.skeleton;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ReversedIterator;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.SubFilter;
import net.sf.okapi.common.filters.SubFilterSkeletonWriter;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.IReferenceable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements ISkeletonWriter for the GenericSkeleton skeleton. 
 */
public class GenericSkeletonWriter implements ISkeletonWriter {

	public static final String ALLOWEMPTYOUTPUTTARGET = "allowEmptyOutputTarget";
	
	protected LocaleId inputLoc;
	protected LocaleId outputLoc;
	protected ILayerProvider layer;
	protected EncoderManager encoderManager;
	protected Stack<StorageList> storageStack;
	protected boolean isMultilingual;
	// A few formats may require to allow empty target in output.
	// They must be multilingual
	protected boolean allowEmptyOutputTarget = false;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, Referent> referents;
	protected String outputEncoding;
	private int referentCopies = 1; // Number of copies to have for the referents (min=1)
	private SubFilterSkeletonWriter sfWriter; // sub-filter skeleton writer
	private boolean sfDirectOutput;

//	private boolean segmentReferents = false;
	
	public GenericSkeletonWriter() {
	}
	
	public GenericSkeletonWriter(LocaleId inputLoc, LocaleId outputLoc,
			ILayerProvider layer, EncoderManager encoderManager,
			boolean isMultilingual, boolean allowEmptyOutputTarget,
			Map<String, Referent> referents, String outputEncoding,
			int referentCopies, SubFilterSkeletonWriter sfWriter) {
		super();
		this.inputLoc = inputLoc;
		this.outputLoc = outputLoc;
		this.layer = layer;
		this.encoderManager = encoderManager;
		this.isMultilingual = isMultilingual;
		this.allowEmptyOutputTarget = allowEmptyOutputTarget;
		this.referents = referents;
		this.outputEncoding = outputEncoding;
		this.referentCopies = referentCopies;
		this.sfWriter = sfWriter;
	}

	protected IReferenceable getReference (String id) {
		if ( referents == null ) return null;
		Referent ref = referents.get(id);
		if ( ref == null ) return null;
		// Remove the object found from the list
		if ( (--ref.count) == 0 ) {
			referents.remove(id);
		}
		return ref.ref;
	}

	@Override
	public void close () {
		sfWriter = null;
		
		if ( referents != null ) {
			referents.clear();
			referents = null;
		}
		if ( storageStack != null ) {
			storageStack.clear();
			storageStack = null;
		}
	}
	
	/**
	 * Sets the number of copies to keep for a referent. By default one copy is kept
	 * and discarded after it is referenced. Some layout may need to refer to the referent
	 * more than once, for example when they output both source and target.
	 * @param referentCopies the number of copies to hold (must be at least 1).
	 */
	public void setReferentCopies (int referentCopies) {
		if ( referentCopies < 1 ) this.referentCopies = 1; 
		else this.referentCopies = referentCopies;
	}

	@Override
	public String processStartDocument (LocaleId outputLocale,
		String outputEncoding,
		ILayerProvider layer,
		EncoderManager encoderManager,
		StartDocument resource)
	{		
		if (isSubfilterActive()) {
			return sfWriter.processStartDocument(outputLocale, outputEncoding, layer, encoderManager, resource);
		}
		referents = new LinkedHashMap<String, Referent>();
		storageStack = new Stack<StorageList>();

		this.inputLoc = resource.getLocale();
		this.outputLoc = outputLocale;
		this.encoderManager = encoderManager;
		this.outputEncoding = outputEncoding;
		this.layer = layer;
		isMultilingual = resource.isMultilingual();
		IParameters prm = resource.getFilterParameters();
		if ( this.encoderManager != null ) {
			this.encoderManager.setDefaultOptions(prm, outputEncoding,
				resource.getLineBreak());
			this.encoderManager.updateEncoder(resource.getMimeType());
		}
		// By default do not allow empty target in output
		allowEmptyOutputTarget = false;
		// Check if there is a parameter for allowing empty targets (only if the format is multilingual)
		if (( prm != null ) && isMultilingual ) {
			allowEmptyOutputTarget = prm.getBoolean(ALLOWEMPTYOUTPUTTARGET);
		}
		
		return getString((GenericSkeleton)resource.getSkeleton(), EncoderContext.SKELETON);
	}

	@Override
	public String processEndDocument (Ending resource) {
		if (isSubfilterActive()) {
			return sfWriter.processEndDocument(resource);
		}
		return getString((GenericSkeleton)resource.getSkeleton(), EncoderContext.SKELETON);
	}

	@Override
	public String processStartSubDocument (StartSubDocument resource) {
		if (isSubfilterActive()) {
			return sfWriter.processStartSubDocument(resource);
		}
		if ( storageStack.size() > 0 ) {
			storageStack.peek().add(resource);
			return "";
		}
		return getString((GenericSkeleton)resource.getSkeleton(), EncoderContext.SKELETON);
	}

	@Override
	public String processEndSubDocument (Ending resource) {
		if (isSubfilterActive()) {
			return sfWriter.processEndSubDocument(resource);
		}
		if ( storageStack.size() > 0 ) {
			storageStack.peek().add(resource);
			return "";
		}
		return getString((GenericSkeleton)resource.getSkeleton(), EncoderContext.SKELETON);
	}

	private boolean isSubfilterActive() {
		return sfWriter != null;
	}

	@Override
	public String processStartGroup (StartGroup resource) {
		if (isSubfilterActive()) {
			return sfWriter.processStartGroup(resource);
		}
		return _processStartGroup(resource);
	}

	// update the referent stack
	protected String _processStartGroup (StartGroup resource) {
		if ( resource.isReferent() ) {
			StorageList sl = new StorageList(resource);
			referents.put(sl.getId(), new Referent(sl, referentCopies));
			storageStack.push(sl);
			return "";
		}
		if ( storageStack.size() > 0 ) {
			StorageList sl = new StorageList(resource);
			storageStack.peek().add(sl);
			storageStack.push(sl);
			return "";
		}
		return getString((GenericSkeleton)resource.getSkeleton(), EncoderContext.SKELETON);
	}
	
	@Override
	public String processEndGroup (Ending resource) {
		if (isSubfilterActive()) {
			return sfWriter.processEndGroup(resource);
		}
		return _processEndGroup(resource);
	}

	protected String _processEndGroup(Ending resource) {
		if ( storageStack.size() > 0 ) {
			storageStack.peek().add(resource);
			storageStack.pop();
			return "";
		}
		return getString((GenericSkeleton)resource.getSkeleton(), EncoderContext.SKELETON);
	}
	
	@Override
	public String processStartSubfilter (StartSubfilter resource) {
		if (isSubfilterActive()) {
			return sfWriter.processStartSubfilter(resource);
		}
		SubFilterSkeletonWriter sfsw = resource.createSkeletonWriter(resource, outputLoc, outputEncoding, layer);
				
		if (sfsw.getSkelWriter() != this)
			sfWriter = sfsw;
		else 
			sfWriter = null; // Prevent recursive references of skeleton writers
		
		// If sfDirectOutput is true, then output the subfilter events with END_SUBFILTER, not when a reference to the SSF is resolved
		sfDirectOutput = 
				sfWriter != null && 
				!resource.isReferent();
		
		return _processStartGroup(resource);
	}
	
	@Override
	public String processEndSubfilter (EndSubfilter resource) {
		// If the subfilter that's ending isn't *this* subfilter, keep digging.
		if (isSubfilterActive() && !SubFilter.resourceIdsMatch(sfWriter.getStartResourceId(), resource.getId())) {
			return sfWriter.processEndSubfilter(resource);
		}
		String res = "";
		if (sfDirectOutput) {
			res = sfWriter.getEncodedOutput();
		}
		res += _processEndGroup(resource);
		sfWriter = null;
		return res;
	}
	
	@Override
	public String processTextUnit (ITextUnit resource) {
		if (isSubfilterActive()) {
			return sfWriter.processTextUnit(resource);
		}
		if ( resource.isReferent() ) {
			referents.put(resource.getId(), new Referent(resource, referentCopies));
			return "";
		}
		if ( storageStack.size() > 0 ) {
			storageStack.peek().add(resource);
			return "";
		}
		return getString(resource, outputLoc, EncoderContext.TEXT);
	}

	@Override
	public String processDocumentPart (DocumentPart resource) {
		if (isSubfilterActive()) {
			return sfWriter.processDocumentPart(resource);
		}
		if ( resource.isReferent() ) {
			referents.put(resource.getId(), new Referent(resource, referentCopies));
			return "";
		}
		if ( storageStack.size() > 0 ) {
			storageStack.peek().add(resource);
			return "";
		}
		return getString((GenericSkeleton)resource.getSkeleton(), EncoderContext.SKELETON);
	}
	
	protected String getString (ISkeleton skeleton,
			EncoderContext context)
	{
		if ( skeleton == null ) return "";
		StringBuilder tmp = new StringBuilder();
		for ( GenericSkeletonPart part : ((GenericSkeleton)skeleton).getParts() ) {
			tmp.append(getString(part, context));
		}
		return tmp.toString();
	}
	
	protected String getString (GenericSkeletonPart part,
		EncoderContext context)
	{
		// If it is not a reference marker, just use the data
		//if ( !part.data.toString().startsWith(TextFragment.REFMARKER_START) ) {
		if ( !part.data.toString().contains(TextFragment.REFMARKER_START) ) {
			if ( layer == null ) {
				return part.data.toString();
			}
			else {
				return layer.encode(part.data.toString(), context);
			}
		}
		
		StringBuilder sb = new StringBuilder(part.data); 
		while (sb.indexOf(TextFragment.REFMARKER_START) != -1) {
			// Get the reference info
			Object[] marker = TextFragment.getRefMarker(sb);
			// Check for problem
			if ( marker == null ) {
				return "-ERR:INVALID-REF-MARKER-";
			}
			String propName = (String)marker[3];

			// If we have a property name: It's a reference to a property of 
			// the resource holding this skeleton
			if ( propName != null ) { // Reference to the content of the referent
				if (Segment.REF_MARKER.equals(propName)) {
					String segId = (String) marker[0];
					ITextUnit tu = (ITextUnit)part.getParent();
					LocaleId locId = part.getLocale();
					TextContainer tc = null;
					
					if ( locId == null ) { // Source
						tc = tu.getSource();
					}
					else { // Target
						tc = tu.getTarget(locId);
					}
					Segment seg = null;
					if ( tc != null ) {
						seg = tc.getSegments().get(segId);
					}
					if (seg == null) {
						logger.warn("Segment reference '{}' not found.", (String)marker[0]);
						return "-ERR:INVALID-SEGMENT-REF-";
					}
					
					return getContent(seg.getContent(), locId, context);
				}
				else
					return getString((INameable)part.parent, propName, part.locId, context);
			}

			// Set the locToUse and the contextToUse parameters
			// If locToUse==null: it's source, so use output locale for monolingual
			LocaleId locToUse = (part.locId==null) ? outputLoc : part.locId;
			EncoderContext contextToUse = context;
			if ( isMultilingual ) {
				locToUse = part.locId;
				// If locToUse==null: it's source, so not text in multilingual
				contextToUse = (locToUse==null) ? EncoderContext.TEXT : context;
			}
			
			// If a parent if set, it's a reference to the content of the resource
			// holding this skeleton. And it's always a TextUnit
			if ( part.parent != null ) {
				if ( part.parent instanceof ITextUnit ) {
					return getContent((ITextUnit)part.parent, locToUse, contextToUse);
				}
				else {
					throw new OkapiException("The self-reference to this skeleton part must be a text-unit.");
				}
			}
			
			// Else this is a true reference to a referent
			IReferenceable ref = getReference((String)marker[0]);
			
			String refData = null;
//			int start = part.data.indexOf(TextFragment.REFMARKER_START);
//			int end = part.data.indexOf(TextFragment.REFMARKER_END, start) + 
//					TextFragment.REFMARKER_END.length();
			int start = (Integer) marker[1];
			int end = (Integer) marker[2];
			
			if ( ref == null ) {
				logger.warn("Reference '{}' not found.", (String)marker[0]);
				refData = "-ERR:REF-NOT-FOUND-";
			}
			else if ( ref instanceof ITextUnit ) {
				refData = getString((ITextUnit)ref, locToUse, contextToUse); //TODO: Test locToUse
			}
			// FIXME GenericSkeletonPart does not implement IReferenceable, so this condition seems to never turn true 
			else if ( ref instanceof GenericSkeletonPart ) {
				refData = getString((GenericSkeletonPart)ref, contextToUse);
			}
			else if ( ref instanceof StorageList ) { // == StartGroup
				refData = getString((StorageList)ref, locToUse, contextToUse); //TODO: Test locToUse
			}
			else // DocumentPart, StartDocument, StartSubDocument		
				refData = getString((GenericSkeleton)((IResource)ref).getSkeleton(), context);
			
			sb.replace(start, end, refData);
		}
		return sb.toString();
	}

	protected String getString (INameable ref,
		String propName,
		LocaleId locToUse,
		EncoderContext context)
	{
		if ( ref == null ) {
			logger.warn("Null reference for '{}'.", propName);
			return "-ERR:NULL-REF-";
		}
		if ( propName != null ) {
			return getPropertyValue((INameable)ref, propName, locToUse, context);
		}
		if ( ref instanceof ITextUnit ) {
			return getString((ITextUnit)ref, locToUse, context);
		}
		if ( ref instanceof DocumentPart ) {
			return getString((GenericSkeleton)((IResource)ref).getSkeleton(), context);
		}
		if ( ref instanceof StorageList ) {
			return getString((StorageList)ref, locToUse, context);
		}
		logger.warn("Invalid reference type for '{}'.", propName);
		return "-ERR:INVALID-REFTYPE-";
	}

	/**
	 * Gets the skeleton and the original content of a given text unit.
	 * @param tu The text unit to process.
	 * @param locToUse locale to output. Use null for the source, or a LocaleId
	 * object for the target locales.
	 * @param context Context flag: 0=text, 1=skeleton, 2=in-line.
	 * @return The string representation of the text unit. 
	 */
	protected String getString (ITextUnit tu,
		LocaleId locToUse,
		EncoderContext context)
	{
		GenericSkeleton skel = (GenericSkeleton)tu.getSkeleton();
		if ( skel == null ) { // No skeleton
			return getContent(tu, locToUse, context);
		}
		// Else: process the skeleton parts, one of them should
		// refer to the text-unit content itself
		StringBuilder tmp = new StringBuilder();
		for ( GenericSkeletonPart part : skel.getParts() ) {
			tmp.append(getString(part, context));
		}
		return tmp.toString();
	}

	/**
	 * Gets the original content of a given text unit.
	 * @param tu The text unit to process.
	 * @param locToUse locale to output. Use null for the source, or the locale
	 * for the target locales.
	 * @param context Context flag: 0=text, 1=skeleton, 2=inline.
	 * @return The string representation of the text unit content.
	 */
	protected String getContent (ITextUnit tu,
		LocaleId locToUse,
		EncoderContext context)
	{
		// Update the encoder from the TU's MIME type
		if ( encoderManager != null ) {
			encoderManager.updateEncoder(tu.getMimeType());
		}
		
		// Get the right text container
		TextContainer srcCont = tu.getSource();
		TextContainer trgCont = null;
		if ( locToUse != null ) { // Expects a target output
			trgCont = tu.getTarget(locToUse);
			// If we do not have target
			// or if the target is empty (regardless the source)
			if (( trgCont == null ) || trgCont.isEmpty() ) {
				// If there is no target available
				if ( allowEmptyOutputTarget && ( layer == null )) {
					// If empty targets are allowed and we don't have one: create a temporary one
					if ( trgCont == null ) {
						trgCont = tu.createTarget(locToUse, false, IResource.CREATE_EMPTY);
					}
				}
				else { // Fall back to the source
					trgCont = srcCont;
				}
			}
		}
		else { // Use the source
			// Set trgCont to it because that's the one driving the output
			trgCont = srcCont;
		}
		// Now trgCont is either the available target or the source (fall-back case)

		if ( !tu.isTranslatable() ) {
			context = EncoderContext.TEXT; // Keep skeleton context
		}
		
		if ( srcCont.hasBeenSegmented() || !srcCont.contentIsOneSegment()
			|| trgCont.hasBeenSegmented() || !trgCont.contentIsOneSegment() 
			|| ( trgCont.getAnnotation(AltTranslationsAnnotation.class) != null ))
		{
			return getSegmentedText(srcCont, trgCont, locToUse, context, tu.isReferent(), tu.getId());
		}

		// Else: We have only one segment
		// Use trgCont, even if locToUse == null because then it's the source
		TextFragment tf = trgCont.getFirstContent();

		// Apply the layer if there is one
		if ( layer == null ) {
			return getContent(tf, locToUse, context);
		}
		else {
			switch ( context ) {
			case SKELETON:
				return layer.endCode()
					+ getContent(tf, locToUse, EncoderContext.TEXT)
					+ layer.startCode();
			case INLINE:
				return layer.endInline()
					+ getContent(tf, locToUse, EncoderContext.TEXT)
					+ layer.startInline();
			default:
				return getContent(tf, locToUse, context);
			}
		}
	}

	private String getSegmentedText (TextContainer srcCont,
		TextContainer trgCont,
		LocaleId locToUse,
		EncoderContext context,
		boolean isReferent,
		String tuId)
	{
		StringBuilder tmp = new StringBuilder();

		// Get the alternate-translations if available
		AltTranslationsAnnotation atAnn = null;
//		atAnn = trgCont.getAnnotation(AltTranslationsAnnotation.class);
		
		// The output is driven by the target, not the source, so the interstices parts
		// are the ones of the target, no the one of the source
		for ( TextPart part : trgCont ) {
			if ( part.isSegment() ) {
				Segment trgSeg = (Segment)part;
				TextFragment trgFrag = trgSeg.text;

				// Compute the leverage score
				int lev = 0;
				AltTranslation at = null;
				atAnn = trgSeg.getAnnotation(AltTranslationsAnnotation.class);
				if ( atAnn != null ) {
					at = atAnn.getFirst();
					if ( at != null ) {
						lev = at.getCombinedScore();
					}
				}
				
				// Fall-back on the source if needed
				Segment srcSeg = srcCont.getSegments().get(trgSeg.id);
				if ( srcSeg == null ) {
					// A target segment without a corresponding source: give warning
					logger.warn("No source segment found for target segment seg-id='{}' (TU id='{}'):\n\"{}\".",
						trgSeg.id, tuId, trgFrag.toText());
				}
				else {
					if ( trgFrag.isEmpty() && !srcSeg.text.isEmpty() ) {
						trgFrag = srcSeg.text;
						lev = 0; // Nothing leverage (target was not copied apparently)
					}
				}

				// Write the segment (note: srcSeg can be null)
				if ( layer == null ) {
					// If no layer: just write the target
					tmp.append(getContent(trgFrag, locToUse, context));
				}
				else { // If layer: write the bilingual entry
					switch ( context ) {
					case SKELETON:
						tmp.append(layer.endCode()
							+ layer.startSegment()
							+ ((srcSeg==null) ? "" : getContent(srcSeg.text, locToUse, EncoderContext.TEXT))
							+ layer.midSegment(lev)
							+ getContent(trgFrag, locToUse, EncoderContext.TEXT)
							+ layer.endSegment()
							+ layer.startCode());
						break;
					case INLINE:
						tmp.append(layer.endInline()
							+ layer.startSegment()
							+ ((srcSeg==null) ? "" : getContent(srcSeg.text, locToUse, EncoderContext.TEXT))
							+ layer.midSegment(lev)
							+ getContent(trgFrag, locToUse, EncoderContext.TEXT)
							+ layer.endSegment()
							+ layer.startInline());
						break;
					default:
						tmp.append(layer.startSegment()
							+ ((srcSeg==null) ? "" : getContent(srcSeg.text, locToUse, EncoderContext.TEXT))
							+ layer.midSegment(lev)
							+ getContent(trgFrag, locToUse, EncoderContext.TEXT)
							+ layer.endSegment());
						break;
					}
				}
			}
			else { // Normal text fragment
				// Target fragment is used
				tmp.append(getContent(part.text, locToUse, context));
			}
		}

		return tmp.toString();
	}

	// This method assumes bi-lingual pairs are 1-1 and in the same order
//	private String getSegmentedText_OLD (TextContainer srcCont,
//		TextContainer trgCont,
//		LocaleId locToUse,
//		int context,
//		boolean isReferent)
//	{
//		StringBuilder tmp = new StringBuilder();
//
//		// Get the scores if they are available
//		ScoresAnnotation scores = null;
//		if ( trgCont != null ) {
//			scores = trgCont.getAnnotation(ScoresAnnotation.class);
//		}
//		
//		// The output is driven by the target, not the source, so the interstices parts
//		// are the ones of the target, no the one of the source
//		int scoreIndex = -1;
//		for ( TextPart part : trgCont ) {
//			if ( part.isSegment() ) {
//				scoreIndex++;
//				int lev = (( scores != null ) ? scores.getScore(scoreIndex) : 0 );
//				Segment trgSeg = (Segment)part;
//				Segment srcSeg = srcCont.getSegments().get(trgSeg.id);
//				if ( srcSeg == null ) {
//					// A target segment without a corresponding source: give warning
//					logger.warn(String.format("No source segment found for target segment id='%s':\n\"%s\".",
//						trgSeg.id, trgSeg.text.toText()));
//				}
//
//				// Write the segment (note: srcSeg can be null)
//				if ( layer == null ) {
//					// If no layer: just write the target
//					tmp.append(getContent(trgSeg.text, locToUse, context));
//				}
//				else { // If layer: write the bilingual entry
//					switch ( context ) {
//					case 1:
//						tmp.append(layer.endCode()
//							+ layer.startSegment()
//							+ ((srcSeg==null) ? "" : getContent(srcSeg.text, locToUse, 0))
//							+ layer.midSegment(lev)
//							+ getContent(trgSeg.text, locToUse, 0)
//							+ layer.endSegment()
//							+ layer.startCode());
//						break;
//					case 2:
//						tmp.append(layer.endInline()
//							+ layer.startSegment()
//							+ ((srcSeg==null) ? "" : getContent(srcSeg.text, locToUse, 0))
//							+ layer.midSegment(lev)
//							+ getContent(trgSeg.text, locToUse, 0)
//							+ layer.endSegment()
//							+ layer.startInline());
//						break;
//					default:
//						tmp.append(layer.startSegment()
//							+ ((srcSeg==null) ? "" : getContent(srcSeg.text, locToUse, 0))
//							+ layer.midSegment(lev)
//							+ getContent(trgSeg.text, locToUse, 0)
//							+ layer.endSegment());
//						break;
//					}
//				}
//			}
//			else { // Normal text fragment
//				// Target fragment is used
//				tmp.append(getContent(part.text, locToUse, context));
//			}
//		}
//
//		return tmp.toString();
//	}

//	/**
//	 * Gets the original content of a given text unit.
//	 * @param tu The text unit to process.
//	 * @param locToUse locale to output. Use null for the source, or the locale
//	 * for the target locales.
//	 * @param context Context flag: 0=text, 1=skeleton, 2=inline.
//	 * @return The string representation of the text unit content.
//	 */
//	protected String getContent (TextUnit tu,
//		LocaleId locToUse,
//		int context) // protected for OpenXML
//	{
//		// Update the encoder from the TU's MIME type
//		if ( encoderManager != null ) {
//			encoderManager.updateEncoder(tu.getMimeType());
//		}
//		
//		// Get the right text container
//		TextContainer srcCont = tu.getSource();
//		TextContainer trgCont = null;
//		if ( locToUse != null ) {
//			if ( (trgCont = tu.getTarget(locToUse)) == null ) {
//				if ( !srcCont.isSegmented() ) {
//					// Fall back to source, except when the source is segmented
//					trgCont = tu.getSource();
//				}
//			}
//		}
//		// Now trgCont is null only if we have segments and no target is available
//		// Otherwise trgCont is either the available target or the source (fall-back case)
//
//		if ( !tu.isTranslatable() ) {
//			context = 0; // Keep skeleton context
//		}
//		// Check for segmentation
//		if ( srcCont.isSegmented() ) {
//			// Special case of segmented entry: source + target
//			return getSegmentedText(tu.getSource(), trgCont, locToUse, context, tu.isReferent());
//		}
//		else { // Normal case: use the calculated target
//			TextContainer cont;
//			if ( locToUse == null ) cont = srcCont;
//			else cont = trgCont;
//			
//			// Apply the layer if there is one
//			if ( layer == null ) {
//				return getContent(cont, locToUse, context);
//			}
//			else {
//				switch ( context ) {
//				case 1:
//					return layer.endCode()
//						+ getContent(cont, locToUse, 0)
//						+ layer.startCode();
//				case 2:
//					return layer.endInline()
//						+ getContent(cont, locToUse, 0)
//						+ layer.startInline();
//				default:
//					return getContent(cont, locToUse, context);
//				}
//			}
//		}
//	}
	
//	private String getSegmentedText (TextContainer srcCont,
//		TextContainer trgCont,
//		LocaleId locToUse,
//		int context,
//		boolean isReferent)
//	{
//		StringBuilder tmp = new StringBuilder();
//		List<Segment> srcSegs = srcCont.getSegments();
//		List<Segment> trgSegs = null;
//		ScoresAnnotation scores = null;
//		if ( trgCont != null ) {
//			trgSegs = trgCont.getSegments();
//			scores = trgCont.getAnnotation(ScoresAnnotation.class);
//		}
//		
//		boolean markSegments = ( segmentReferents || !isReferent );
//		
//		String text = srcCont.getCodedText();
//		Code code;
//		char ch;
//		for ( int i=0; i<text.length(); i++ ) {
//			ch = text.charAt(i);
//			switch ( ch ) {
//			case TextFragment.MARKER_OPENING:
//			case TextFragment.MARKER_CLOSING:
//			case TextFragment.MARKER_ISOLATED:
//				//TODO: Handle codes outside the segments!!!
//				code = srcCont.getCode(text.charAt(++i));
//				tmp.append(expandCodeContent(code, locToUse, context));
//				break;
//			case TextFragment.MARKER_SEGMENT:
//				code = srcCont.getCode(text.charAt(++i));
//				int n = Integer.valueOf(code.getData());
//				// Check segment source/target
//				TextFragment trgFrag = null;
//				int lev = 0;
//				if (( trgSegs != null ) && ( n < trgSegs.size() )) {
//					trgFrag = trgSegs.get(n).text;
//					if ( scores != null ) lev = scores.getScore(n);
//				}
//				if ( trgFrag == null ) { // No target available: use the source
//					trgFrag = srcSegs.get(n).text;
//				}
//				// Write it
//				if ( layer == null ) {
////TODO: deal with not-in segment leading text
////TODO: deal with not-in-segment codes
//					// Get the inter-segment characters at the end of the segment
//					// So derived writers can treat all chars in getContent()
//					// i currently points to the index of the segment marker
//					int j; // Move forward until we found a marker or the end of the text
//					for ( j=1; i+j<text.length(); j++ ) {
//						if ( TextFragment.isMarker(text.charAt(i+j)) ) {
//							break;
//						}
//					} // Now j-1 should be the number of characters to add
//					if ( j > 1 ) {
//						trgFrag = trgFrag.clone(); // Make sure we don't change the original
//						trgFrag.append(text.substring(i+1, i+j));
//						i += (j-1); // Move the pointer at the last char we put in the segment
//					}
//					// Now get the content for the segment
//					tmp.append(getContent(trgFrag, locToUse, context));
//				}
//				else {
//					switch ( context ) {
//					case 1:
//						tmp.append(layer.endCode()
//							+ layer.startSegment()
//							+ getContent(srcSegs.get(n).text, null, 0)
//							+ layer.midSegment(lev)
//							+ ((trgFrag==null) ? "" : getContent(trgFrag, locToUse, 0))
//							+ layer.endSegment()
//							+ layer.startCode());
//						break;
//					case 2:
//						tmp.append(layer.endInline()
//							+ layer.startSegment()
//							+ getContent(srcSegs.get(n).text, null, 0)
//							+ layer.midSegment(lev)
//							+ ((trgFrag==null) ? "" : getContent(trgFrag, locToUse, 0))
//							+ layer.endSegment()
//							+ layer.startInline());
//						break;
//					default:
//						tmp.append(layer.startSegment()
//							+ getContent(srcSegs.get(n).text, null, context)
//							+ layer.midSegment(lev)
//							+ ((trgFrag==null) ? "" : getContent(trgFrag, locToUse, context))
//							+ layer.endSegment());
//						break;
//					}
//				}
//				break;
//			default:
//				if ( Character.isHighSurrogate(ch) ) {
//					int cp = text.codePointAt(i);
//					i++; // Skip low-surrogate
//					if ( encoderManager == null ) {
//						if ( layer == null ) {
//							tmp.append(new String(Character.toChars(cp)));
//						}
//						else {
//							tmp.append(layer.encode(cp, context));
//						}
//					}
//					else {
//						if ( layer == null ) {
//							tmp.append(encoderManager.encode(cp, context));
//						}
//						else {
//							tmp.append(layer.encode(
//								encoderManager.encode(cp, context),
//								context));
//						}
//					}
//				}
//				else { // Non-supplemental case
//					if ( encoderManager == null ) {
//						if ( layer == null ) {
//							tmp.append(ch);
//						}
//						else {
//							tmp.append(layer.encode(ch, context));
//						}
//					}
//					else {
//						if ( layer == null ) {
//							tmp.append(encoderManager.encode(ch, context));
//						}
//						else {
//							tmp.append(layer.encode(
//								encoderManager.encode(ch, context),
//								context));
//						}
//					}
//				}
//				break;
//			}
//		}
//		return tmp.toString();
//	}

	/**
	 * Gets the original content of a TextFragment.
	 * @param tf the TextFragment to process.
	 * @param locToUse locale to output. Use null for the source, or the locale
	 * for the target locales. This is used for referenced content in inline codes.
	 * @param context Context flag: 0=text, 1=skeleton, 2=inline.
	 * @return The string representation of the text unit content.
	 */
	public String getContent (TextFragment tf,
		LocaleId locToUse,
		EncoderContext context)
	{
		// Output simple text
		if ( !tf.hasCode() ) {
			if ( encoderManager == null ) {
				if ( layer == null ) {
					return tf.toText();
				}
				else {
					return layer.encode(tf.toText(), context);
				}
			}
			else {
				if ( layer == null ) {
					return encoderManager.encode(tf.toText(), context);
				}
				else {
					return layer.encode(
						encoderManager.encode(tf.toText(), context), context);
				}
			}
		}

		// Output text with in-line codes
		List<Code> codes = tf.getCodes();
		StringBuilder tmp = new StringBuilder();
		String text = tf.getCodedText();
		boolean inlineCdata = false;
		Code code;
		char ch;
		for ( int i=0; i<text.length(); i++ ) {
			ch = text.charAt(i);
			switch ( ch ) {
			case TextFragment.MARKER_OPENING:
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				if (code.getType().equals(Code.TYPE_CDATA)) {
					inlineCdata = true;
				}
				tmp.append(expandCodeContent(code, locToUse, context));
				break;
			case TextFragment.MARKER_CLOSING:
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				if (code.getType().equals(Code.TYPE_CDATA)) {
					inlineCdata = false;
				}
				tmp.append(expandCodeContent(code, locToUse, context));
				break;
			case TextFragment.MARKER_ISOLATED:
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				tmp.append(expandCodeContent(code, locToUse, context));
				break;
			default:
				if (inlineCdata) {
					tmp.append(ch);
					break;
				}
				if ( Character.isHighSurrogate(ch) ) {
					int cp = text.codePointAt(i);
					i++; // Skip low-surrogate
					if ( encoderManager == null ) {
						if ( layer == null ) {
							tmp.append(new String(Character.toChars(cp)));
						}
						else {
							tmp.append(layer.encode(cp, context));
						}
					}
					else {
						if ( layer == null ) {
							tmp.append(encoderManager.encode(cp, context));
						}
						else {
							tmp.append(layer.encode(
								encoderManager.encode(cp, context),
								context));
						}
					}
				}
				else { // Non-supplemental case
					if ( encoderManager == null ) {
						if ( layer == null ) {
							tmp.append(ch);
						}
						else {
							tmp.append(layer.encode(ch, context));
						}
					}
					else {
						if ( layer == null ) {
							tmp.append(encoderManager.encode(ch, context));
						}
						else {
							tmp.append(layer.encode(
								encoderManager.encode(ch, context),
								context));
						}
					}
				}
				break;
			}
		}
		return tmp.toString();
	}
	
	protected String expandCodeContent (Code code,
		LocaleId locToUse,
		EncoderContext context)
	{ // this needs to be protected, not private, for OpenXML
		String codeTmp = code.getOuterData();
		if ( layer != null ) {
			codeTmp = layer.startInline() 
				+ layer.encode(codeTmp, EncoderContext.INLINE)
				+ layer.endInline();
		}
		if ( !code.hasReference() ) {
			return codeTmp;
		}
		// Else: look for place-holders
		StringBuilder tmp = new StringBuilder(codeTmp);
		Object[] marker = null;
		while ( (marker = TextFragment.getRefMarker(tmp)) != null ) {
			int start = (Integer)marker[1];
			int end = (Integer)marker[2];
			String propName = (String)marker[3];
			IReferenceable ref = getReference((String)marker[0]);
			if ( ref == null ) {
				logger.warn("Reference '{}' not found.", (String)marker[0]);
				tmp.replace(start, end, "-ERR:REF-NOT-FOUND-");
			}
			else if ( propName != null ) {
				tmp.replace(start, end,
					getPropertyValue((INameable)ref, propName, locToUse, EncoderContext.INLINE));
			}
			else if ( ref instanceof ITextUnit ) {
				tmp.replace(start, end, getString((ITextUnit)ref, locToUse, EncoderContext.INLINE));
			}
			// TODO seems GenericSkeletonPart is not IReferenceable, others are
			else if ( ref instanceof GenericSkeletonPart ) {
				tmp.replace(start, end, getString((GenericSkeletonPart)ref, EncoderContext.INLINE));
			}
			else if ( ref instanceof StorageList ) { // == StartGroup or StartSubfilter
				tmp.replace(start, end, getString((StorageList)ref, locToUse, EncoderContext.INLINE));
			}
			else { // DocumentPart, StartDocument, StartSubDocument 
				tmp.replace(start, end, getString((GenericSkeleton)((IResource)ref).getSkeleton(), EncoderContext.INLINE));
			}
		}
		return tmp.toString();
	}
	
	protected String getString (StorageList list,
		LocaleId locToUse,
		EncoderContext context)
	{
		StringBuilder tmp = new StringBuilder();
		
		if (list.getStartGroup() instanceof StartSubfilter) {
			tmp.append(getString((GenericSkeleton)list.getSkeleton(), context));
			
			StartSubfilter ssf = (StartSubfilter) list.getStartGroup();
			tmp.append(ssf.getSkeletonWriter().getEncodedOutput());
			
			// Looking for the last Ending to write its skeleton
			for ( IResource res : new ReversedIterator<IResource>(list) ) {
				if ( res instanceof Ending ) {
					tmp.append(getString((GenericSkeleton)res.getSkeleton(), context));
					break;
				}
			}
		}
		else {
			// Treat the skeleton of this list
			tmp.append(getString((GenericSkeleton)list.getSkeleton(), context));		
			// Then treat the list itself
			for ( IResource res : list ) {
				if ( res instanceof ITextUnit ) {
					tmp.append(getString((ITextUnit)res, locToUse, context));
				}
				else if ( res instanceof StorageList ) {
					tmp.append(getString((StorageList)res, locToUse, context));
				}
				else if ( res instanceof DocumentPart ) {
					tmp.append(getString((GenericSkeleton)res.getSkeleton(), context));
				}
				else if ( res instanceof Ending ) {
					tmp.append(getString((GenericSkeleton)res.getSkeleton(), context));
				}
			}
		}
		
		return tmp.toString();
	}
	
	protected String getPropertyValue (INameable resource,
		String name,
		LocaleId locToUse,
		EncoderContext context)
	{
		// Update the encoder from the TU's MIME type
		if ( encoderManager != null ) {
			encoderManager.updateEncoder(resource.getMimeType());
		}

		// Get the value based on the output locale
		Property prop;
		if ( locToUse == null ) { // Use the source
			prop = resource.getSourceProperty(name);
		}
		else if ( locToUse.equals(LocaleId.EMPTY) ) { // Use the resource-level properties
			prop = resource.getProperty(name);
		}
		else { // Use the given target locale if possible
			if ( resource.hasTargetProperty(locToUse, name) ) {
				prop = resource.getTargetProperty(locToUse, name);
			}
			else { // Fall back to source if there is no target
				prop = resource.getSourceProperty(name);				
			}
		}
		// Check the property we got
		if ( prop == null ) {
			logger.warn("Property '{}' not found.", name);
			return "-ERR:PROP-NOT-FOUND-";
		}
		// Else process the value
		String value = prop.getValue();
		if ( value == null ) {
			logger.warn("Property value for '{}' is null.", name);
			return "-ERR:PROP-VALUE-NULL-";
		}
		
		// Else: We got the property value
		// Check if it needs to be auto-modified
		if ( Property.LANGUAGE.equals(name) ) {
			// If it is the input locale, we change it with the output locale
			LocaleId locId = LocaleId.fromString(value);
			if ( locId.sameLanguageAs(inputLoc) ) {
				value = outputLoc.toString();
			}
		}
		else if ( Property.ENCODING.equals(name) ) {
			value = outputEncoding;
		}
		// Return the native value if possible
		if ( encoderManager == null ) {
			if ( layer == null ) return value;
			else return layer.encode(value, context); //TODO: context correct??
		}
		else {
			if ( layer == null ) return encoderManager.toNative(name, value);
			else return layer.encode(encoderManager.toNative(name, value), context);
		}
	}
	
	public void addToReferents (Event event) { // for OpenXML, so referents can stay private
		IResource resource;
		if ( event != null ) {
			if ( referents == null ) {
				referents = new LinkedHashMap<String, Referent>();
				storageStack = new Stack<StorageList>();
			}
			resource = event.getResource();
			if ( resource != null ) {
				switch( event.getEventType() ) {
				case TEXT_UNIT:
					if ( ((ITextUnit)resource).isReferent() ) {
						referents.put(resource.getId(), new Referent((ITextUnit)resource, referentCopies));
					}
					break;
				case DOCUMENT_PART:
					if ( ((DocumentPart)resource).isReferent() ) {
						referents.put(resource.getId(), new Referent((DocumentPart)resource, referentCopies));
					}
					break;
				case START_GROUP:
				case START_SUBFILTER:
					if ( ((StartGroup)resource).isReferent() ) {
						StorageList sl = new StorageList((StartGroup)resource);
						referents.put(sl.getId(), new Referent(sl, referentCopies));
					}
					break;
				default:
					break;
				}
			}
		}
	}

	public ILayerProvider getLayer() {
		return layer;
	}
	
	public final LocaleId getInputLoc() {
		return inputLoc;
	}

	public final LocaleId getOutputLoc() {
		return outputLoc;
	}

	public final EncoderManager getEncoderManager() {
		return encoderManager;
	}

	public final boolean isMultilingual() {
		return isMultilingual;
	}

	public final boolean isAllowEmptyOutputTarget() {
		return allowEmptyOutputTarget;
	}

	public final Map<String, Referent> getReferents() {
		return referents;
	}

	public void setReferents(Map<String, Referent> referents) {
		this.referents = referents;
	}

	public final String getOutputEncoding() {
		return outputEncoding;
	}

	public final int getReferentCopies() {
		return referentCopies;
	}

	public final ISkeletonWriter getSfWriter() {
		return sfWriter;
	}
	
	//////////////////////////////////
	// For serialization only
	//////////////////////////////////
	
	public void setOutputLoc(LocaleId outputLoc) {
		this.outputLoc = outputLoc;
	}

	public void setLayer(ILayerProvider layer) {
		this.layer = layer;
	}

	public void setEncoderManager(EncoderManager encoderManager) {
		this.encoderManager = encoderManager;
	}

	public void setMultilingual(boolean isMultilingual) {
		this.isMultilingual = isMultilingual;
	}

	public void setAllowEmptyOutputTarget(boolean allowEmptyOutputTarget) {
		this.allowEmptyOutputTarget = allowEmptyOutputTarget;
	}

	public void setOutputEncoding(String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public void setSfWriter(SubFilterSkeletonWriter sfWriter) {
		this.sfWriter = sfWriter;
	}

	public void setInputLoc(LocaleId inputLoc) {
		this.inputLoc = inputLoc;
	}
}
