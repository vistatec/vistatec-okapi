/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xliff;

import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.ITSLQIAnnotations;
import net.sf.okapi.common.annotation.ITSProvenanceAnnotations;
import net.sf.okapi.common.annotation.XLIFFNote;
import net.sf.okapi.common.annotation.XLIFFNoteAnnotation;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filterwriter.ITSContent;
import net.sf.okapi.common.filterwriter.XLIFFContent;
import net.sf.okapi.common.filterwriter.XLIFFWriter;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.IReferenceable;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.StorageList;
import net.sf.okapi.filters.xliff.its.ITSStandoffManager;

public class XLIFFSkeletonWriter extends GenericSkeletonWriter {

	public static final String SEGSOURCEMARKER = "[@#$SEGSRC$#@]";
	public static final String ALTTRANSMARKER = "[@#$ALTTRANS$#@]";
	public static final String ITSNSDECL = "[@#ITSNSDECL#@]";
	public static final String NOTEMARKER = "[@#$NOTE$#@]";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Parameters params;
	private XLIFFContent fmt;
	private ITSContent itsCont;
	private ITSStandoffManager itsStandoffManager;
	private Map<String, GenericAnnotations> lqiStandoff = new HashMap<String, GenericAnnotations>();
	private Map<String, GenericAnnotations> provStandoff = new HashMap<String, GenericAnnotations>();
	private CharsetEncoder chsEnc;

	// For serialization
	public XLIFFSkeletonWriter () {
		params = new Parameters();
		fmt = new XLIFFContent();
	}
	
	public XLIFFSkeletonWriter (Parameters params) {
		this.params = params;
		fmt = new XLIFFContent();
	}		
	
	public XLIFFSkeletonWriter(Parameters params, XLIFFContent fmt,
			ITSContent itsCont, ITSStandoffManager itsStandoffManager,
			Map<String, GenericAnnotations> lqiStandoff,
			Map<String, GenericAnnotations> provStandoff,
			CharsetEncoder chsEnc) {
		super();
		this.params = params;
		this.fmt = fmt;
		this.itsCont = itsCont;
		setITSStandoffManager(itsStandoffManager);
		this.lqiStandoff = lqiStandoff;
		this.provStandoff = provStandoff;
		this.chsEnc = chsEnc;
	}

	@Override
	public String processStartDocument (LocaleId outputLocale,
		String outputEncoding,
		ILayerProvider layer,
		EncoderManager encoderManager,
		StartDocument resource)
	{
		String res = super.processStartDocument(outputLocale, outputEncoding, layer, encoderManager, resource);
		chsEnc = encoderManager.getCharsetEncoder();
		fmt.setCharsetEncoder(chsEnc);
		return res;
	}

	@Override
	public String processTextUnit (ITextUnit resource) {
		addLQIAnnotations(resource);
		addProvAnnotations(resource);
		addLQIAnnotations(resource.getSource());
		addProvAnnotations(resource.getSource());
		Set<LocaleId> targetLocales = resource.getTargetLocales();
		if ( targetLocales.size() > 1 ) {
			logger.debug("More than one target locale "+targetLocales);
		}
		else if ( targetLocales.size() == 1 ) {
			for (LocaleId tgt : targetLocales) {
				addLQIAnnotations(resource.getTarget(tgt));
				addProvAnnotations(resource.getTarget(tgt));
			}
		}
		return super.processTextUnit(resource);
	}

	@Override
	public String processStartSubDocument (StartSubDocument resource) {
		addProvAnnotations(resource);
		return super.processStartSubDocument(resource);
	}

	@Override
	public String processStartGroup (StartGroup resource) {
		addProvAnnotations(resource);
		return super.processStartGroup(resource);
	}

	private void addProvAnnotations (INameable referent) {
		ITSProvenanceAnnotations anns = referent.getAnnotation(ITSProvenanceAnnotations.class);
		if ( anns != null ) {
			String itsProvRef = anns.getData();
			if ( !provStandoff.containsKey(itsProvRef) ) {
				if ( itsProvRef == null ) {
					if ( anns.size() == 1 ) {
						ITSContent itsContent = new ITSContent(encoderManager.getCharsetEncoder(), false, true);
						String provAttr = itsContent.writeAttributeProvenance(anns.getFirstAnnotation(GenericAnnotationType.PROV));
						referent.setProperty(new Property(Property.ITS_PROV, provAttr));
					}
					else if ( anns.size() > 1 ) {
						logger.warn("Multiple provenance annotations found without standoff reference on "+referent.getName());
					}
				}
				else {
					if ( anns.size() == 0 ) {
						logger.warn("its:provenanceRecordsRef set on " + referent.getName() + " but no annotations found");
					}
					if ( XLIFFITSFilterExtension.getResourcePart(itsProvRef).equals("") ) {
						GenericAnnotations newSet = new GenericAnnotations();
						newSet.setData(XLIFFITSFilterExtension.getFragmentIdPart(itsProvRef));
						newSet.addAll(anns.getAnnotations(GenericAnnotationType.PROV));
						provStandoff.put(itsProvRef, newSet);
					}
				}
			}
		}
	}

	private void addLQIAnnotations (INameable referent) {
		ITSLQIAnnotations anns = referent.getAnnotation(ITSLQIAnnotations.class);
		if ( anns != null ) {
			String itsLQIRef = anns.getData();
			if ( !lqiStandoff.containsKey(itsLQIRef) ) {
				if ( itsLQIRef == null ) {
					if ( anns.size() == 1 ) {
						ITSContent itsContent = new ITSContent(encoderManager.getCharsetEncoder(), false, true);
						String lqiAttr = itsContent.writeAttributesLQI(anns.getFirstAnnotation(GenericAnnotationType.LQI));
						referent.setProperty(new Property(Property.ITS_LQI, lqiAttr));
					}
					else if ( anns.size() > 1 ) {
						logger.warn("Multiple locQualityIssue annotations found without standoff reference on "+referent.getName());
					}
				}
				else {
					if ( anns.size() == 0 ) {
						logger.warn("its:locQualityIssuesRef set on " + referent.getName() + " but no annotations found");
					}
					if ( XLIFFITSFilterExtension.getResourcePart(itsLQIRef).equals("") ) {
						GenericAnnotations newSet = new GenericAnnotations();
						newSet.setData(XLIFFITSFilterExtension.getFragmentIdPart(itsLQIRef));
						newSet.addAll(anns.getAnnotations(GenericAnnotationType.LQI));
						lqiStandoff.put(itsLQIRef, newSet);
					}
				}
			}
		}
	}

	@Override
	public String processEndDocument (Ending resource) {
		StringBuilder endDoc = new StringBuilder();
		ITSContent itsContent = new ITSContent(encoderManager.getCharsetEncoder(), false, true);
		Comparator<String> nullKeyComparator = Util.createComparatorHandlingNullKeys(String.class);

		if ( !lqiStandoff.isEmpty() ) {
			final Map<String, GenericAnnotations> sortedlqiStandoff = new TreeMap<String, GenericAnnotations>(nullKeyComparator);
			sortedlqiStandoff.putAll(lqiStandoff);
			Collection<GenericAnnotations> c = sortedlqiStandoff.values();
			endDoc.append(itsContent.writeStandoffLQI(new ArrayList<GenericAnnotations>(c)));
		}
// Why should we output unused annotation?		
//		if ( getITSStandoffManager() != null ) {
//			addUnusedAnnotations(endDoc, itsContent,
//				getITSStandoffManager().getStoredLQIRefs(),
//				lqiStandoff, ITSLQIAnnotations.class);
//		}

		if ( !provStandoff.isEmpty() ) {
			final Map<String, GenericAnnotations> sortedProvStandoff = new TreeMap<String, GenericAnnotations>(nullKeyComparator);
			sortedProvStandoff.putAll(provStandoff);
			Collection<GenericAnnotations> c = sortedProvStandoff.values();
			endDoc.append(itsContent.writeStandoffProvenance(new ArrayList<GenericAnnotations>(c)));
		}
// Why should we output unused annotation?
//		if ( getITSStandoffManager() != null ) {
//			addUnusedAnnotations(endDoc, itsContent,
//				getITSStandoffManager().getStoredProvRefs(),
//				provStandoff, ITSProvenanceAnnotations.class);
//		}

		endDoc.append(super.processEndDocument(resource));
		return endDoc.toString();
	}

	public void addUnusedAnnotations (StringBuilder endDoc,
		ITSContent itsContent,
		Collection<String> storedAnns,
		HashMap<String, GenericAnnotations> standoff,
		Class<? extends GenericAnnotations> type)
	{
		ArrayList<GenericAnnotations> unusedAnns = new ArrayList<GenericAnnotations>();
		if ( getITSStandoffManager() != null ) {
			for ( String stored : storedAnns ) {
				// Write out only unused Annotations that reside within the parsed document.
				if ( !standoff.containsKey(stored)
					&& XLIFFITSFilterExtension.getResourcePart(stored).equals("") ) {
					logger.warn(type.toString()+" ref \"" + stored
						+ "\" not referred to/used");
					GenericAnnotations ann = new GenericAnnotations();
					if ( type.equals(ITSLQIAnnotations.class) ) {
						getITSStandoffManager().addLQIAnnotation(ann, stored);
					}
					else if ( type.equals(ITSProvenanceAnnotations.class) ) {
						getITSStandoffManager().addProvAnnotation(ann, stored);
					}
					ann.setData(XLIFFITSFilterExtension.getFragmentIdPart(stored));
					unusedAnns.add(ann);
				}
			}

			if ( type.equals(ITSLQIAnnotations.class) ) {
				endDoc.append(itsContent.writeStandoffLQI(unusedAnns));
			}
			else if ( type.equals(ITSProvenanceAnnotations.class) ) {
				endDoc.append(itsContent.writeStandoffProvenance(unusedAnns));
			}
		}
	}
	
	@Override
	protected String getString (GenericSkeletonPart part,
		EncoderContext context)
	{
		// Check for the seg-source special case
		if ( part.toString().startsWith(SEGSOURCEMARKER) ) {
			// There is normally a text unit associated
			return getSegSourceOutput((ITextUnit)part.getParent());
		}
		else if ( part.toString().startsWith(ALTTRANSMARKER) ) {
			return getNewAltTransOutput((ITextUnit)part.getParent());
		}
		else if ( part.toString().startsWith(NOTEMARKER) ) {
		    return getNewNoteOutput((ITextUnit)part.getParent());
		}
		
		// Check for marker indicating that we need to add ITS namespace declaration
		// If we have ITS properties
		boolean needITSDecl = false;
		if ( part.toString().startsWith(ITSNSDECL) ) {
			needITSDecl = true;
			part.getData().delete(0, ITSNSDECL.length());
		}
		
		// If it is not a reference marker, just use the data
		if ( !part.toString().startsWith(TextFragment.REFMARKER_START) ) {
			if ( getLayer() == null ) {
				return part.toString();
			}
			else {
				return getLayer().encode(part.toString(), context);
			}
		}
		
		// Get the reference info
		Object[] marker = TextFragment.getRefMarker(part.getData());
		// Check for problem
		if ( marker == null ) {			
			logger.error("Invalid ref marker for resource {}\nIn GenericSkeletonPart: {}", 
					(part.getParent() == null) ? "null" : part.getParent().toString(), part.getData());
			return "-ERR:INVALID-REF-MARKER-";
		}
		String propName = (String)marker[3];

		// If we have a property name: It's a reference to a property of 
		// the resource holding this skeleton
		if ( propName != null ) { // Reference to the content of the referent
			String value = getString((INameable)part.getParent(), propName, part.getLocale(), context);
			if ( needITSDecl && !Util.isEmpty(value) ) {
				if ( value.startsWith(" its:") ) {
					value = " xmlns:its=\""+Namespaces.ITS_NS_URI+"\" its:version=\"2.0\""+value;
				}
			}
			return value;
		}

		// Set the locToUse and the contextToUse parameters
		// If locToUse==null: it's source, so use output locale for monolingual
		LocaleId locToUse = (part.getLocale()==null) ? outputLoc : part.getLocale();
		EncoderContext contextToUse = context;
		if ( isMultilingual ) {
			locToUse = part.getLocale();
			// If locToUse==null: it's source, so not text in multilingual
			contextToUse = (locToUse==null) ? EncoderContext.TEXT : context;
		}
		
		// If a parent if set, it's a reference to the content of the resource
		// holding this skeleton. And it's always a TextUnit
		if ( part.getParent() != null ) {
			if ( part.getParent() instanceof ITextUnit ) {
				return getContent((ITextUnit)part.getParent(), locToUse, contextToUse);
			}
			else {
				throw new OkapiException("The self-reference to this skeleton part must be a text-unit.");
			}
		}
		
		// Else this is a true reference to a referent
		IReferenceable ref = getReference((String)marker[0]);
		if ( ref == null ) {
			logger.warn("Reference '{}' not found.", (String)marker[0]);
			return "-ERR:REF-NOT-FOUND-";
		}
		if ( ref instanceof ITextUnit ) {
			return getString((ITextUnit)ref, locToUse, contextToUse);
		}
		if ( ref instanceof GenericSkeletonPart ) {
			return getString((GenericSkeletonPart)ref, contextToUse);
		}
		if ( ref instanceof StorageList ) { // == StartGroup
			return getString((StorageList)ref, locToUse, contextToUse);
		}
		// Else: DocumentPart, StartDocument, StartSubDocument 
		return getString((GenericSkeleton)((IResource)ref).getSkeleton(), context);
	}
	
	@Override
	protected String getContent (ITextUnit tu,
		LocaleId locToUse,
		EncoderContext context)
	{
		// Update the encoder from the TU's MIME type
		if ( encoderManager != null ) {
			encoderManager.updateEncoder(tu.getMimeType());
		}
		
		if ( !tu.isTranslatable() ) {
			context = EncoderContext.TEXT; // Keep skeleton context
		}
		
		// Get the source container
		TextContainer srcCont = tu.getSource();
		
		// Process the case of an output for the source
		if ( locToUse == null ) {
			return getUnsegmentedOutput(srcCont, locToUse, context);
		}
		
		// Else: Case of a target output

		// Determine whether we output segmentation or not
		boolean doSegments = doSegments(tu);
		
		TextContainer trgCont = tu.getTarget(locToUse);
		if ( trgCont == null || trgCont.isEmpty()) {
			if (params.getAllowEmptyTargets()) {
				trgCont = new TextContainer(); // Empty target
			}
			else {
				// If there is no target available
				// We fall back to source
				trgCont = srcCont;
			}			
		}

		if (XLIFFFilter.isUnsegmentedTextUnit(tu, params)) {
			// Treat as containing no segments
			return getUnsegmentedOutput(trgCont, locToUse, context);
		}

		// Process the target content: either with or without segments
		// With layers: treat non-segmented translatable entries with existing target as segmented
		if ( doSegments || (( getLayer() != null ) && tu.isTranslatable() && !trgCont.equals(srcCont) )) {
			return getSegmentedOutput(srcCont, trgCont, locToUse, context);
		}
		else {
			return getUnsegmentedOutput(trgCont, locToUse, context);
		}
	}

	private String getSegSourceOutput (ITextUnit tu) {
		if ( !doSegments(tu) ) {
			return ""; // No segmentation: no seg-source element
		}
		
		TextContainer srcCont = tu.getSource();
//JEH 1/13/2015 Caused failing roundtrip if seg-source not written, even if empty
//		if ( srcCont.isEmpty() ) return ""; // No segmented entry if it's empty
//		if ( srcCont.isEmpty() || !srcCont.hasBeenSegmented() ) return ""; // No segmented entry if it's empty
		
		// Else: output new seg-source
		StringBuilder tmp = new StringBuilder("<seg-source>");
		
		if (XLIFFFilter.isUnsegmentedTextUnit(tu, params)) {
			// Append source without doing <mrk> formatting
			tmp.append(getContent(tu.getSource().getFirstSegment().text, null, EncoderContext.SKELETON));
		}
		else {
			for ( TextPart part : srcCont ) {
				if ( part.isSegment() ) {
					Segment srcSeg = (Segment)part;
					// Opening marker
					tmp.append(String.format("<mrk mid=\"%s\" mtype=\"seg\">", srcSeg.id));
					// Write the segment (note: srcSeg can be null)
					// If no layer or layer: just write the target
					tmp.append(getContent(srcSeg.text, null, EncoderContext.SKELETON));
					// Closing marker
					tmp.append("</mrk>");
				}
				else { // Normal text fragment
					// Target fragment is used
					tmp.append(getContent(part.text, null, EncoderContext.SKELETON));
				}
			}
		}
		
		tmp.append("</seg-source>");
		// Add line-break only if the seg-source was not in the original
		if ( tu.getProperty(XLIFFFilter.PROP_WASSEGMENTED) == null ) {
			tmp.append(encoderManager.getLineBreak());
		}
		
		return tmp.toString();
	}
	
	private String getNewAltTransOutput (ITextUnit tu) {
		if ( !params.getAddAltTrans() ) {
			return "";
		}
		// Empty?
		if ( tu.getSource().isEmpty() ) {
			return ""; // Empty
		}
		if ( !tu.hasTarget(outputLoc) ) {
			return ""; // Nothing to output
		}
		
		StringBuilder tmp = new StringBuilder();
		TextContainer tc = tu.getTarget(outputLoc);
		// From the target container
		formatAltTranslations(tc.getAnnotation(AltTranslationsAnnotation.class), null, tmp);
		// From the segments
		for ( Segment seg : tc.getSegments() ) {
			formatAltTranslations(seg.getAnnotation(AltTranslationsAnnotation.class), seg, tmp);
		}
		
		return tmp.toString();
	}

	private String getNewNoteOutput (ITextUnit tu) {
		XLIFFNoteAnnotation notes = tu.getAnnotation(XLIFFNoteAnnotation.class);
	    if (notes == null) {
	        return "";
	    }

    	StringBuilder tmp = new StringBuilder();
	    for (XLIFFNote n : notes) {
	    	tmp.append("<note");
	    	
		    // annotates
		    if (n.getAnnotates() != null) {
		    	tmp.append(" ").append("annotates").append("=\"");
		    	tmp.append(encoderManager.encode(n.getAnnotates().toString(), EncoderContext.INLINE));
		    	tmp.append("\"");
		    }
		    
		    // from
		    if (n.getFrom() != null) {
		    	tmp.append(" ").append("from").append("=\"");
		    	tmp.append(encoderManager.encode(n.getFrom(), EncoderContext.INLINE));
		    	tmp.append("\"");
		    }
		    
		    // priority
		    if (n.getPriority() != null) {
		    	tmp.append(" ").append("priority").append("=\"");
		    	tmp.append(encoderManager.encode(n.getPriority().toString(), EncoderContext.INLINE));
		    	tmp.append("\"");
		    }
		    
		    tmp.append(">");
		    tmp.append(encoderManager.encode(n.getNoteText(), EncoderContext.TEXT));
		    tmp.append("</note>");
	    }
	    return tmp.toString();
	}

	private void formatAltTranslations (AltTranslationsAnnotation ann,
		Segment segment,
		StringBuilder sb)
	{
		if ( ann == null ) {
			return; // No annotation
		}
		for ( AltTranslation alt : ann ) {
			//if ( alt.getCombinedScore() <= 0 ) continue;
			if ( alt.getFromOriginal() ) {
				// Writer only new alt-trans (i.e. not from original) only if they cannot be modified
				// if we can modify the alt-trans we need to re-write them here (they should not be in the skeleton)
				if ( !params.getEditAltTrans() ) continue; // New alt-trans only
			}
			
			sb.append("<alt-trans");
			if ( segment != null ) {
				sb.append(String.format(" mid=\"%s\"", Util.escapeToXML(segment.getId(), 0, false, chsEnc)));
			}
			if ( alt.getCombinedScore() > 0 ) {
				sb.append(String.format(" match-quality=\"%d\"", alt.getCombinedScore()));
			}
			if (alt.getTool() != null && alt.getTool().getId() != null) {
				sb.append(String.format(" tool-id=\"%s\"", Util.escapeToXML(alt.getTool().getId(), 0, false, chsEnc)));
			}
			if ( !Util.isEmpty(alt.getOrigin()) && !alt.getOrigin().equals(AltTranslation.ORIGIN_SOURCEDOC) ) {
				sb.append(String.format(" origin=\"%s\"", Util.escapeToXML(alt.getOrigin(), 0, false, chsEnc)));
			}
			
			if ( params.getIncludeExtensions() ) {
				// Include extensions only if requested
				if ( alt.getType() != MatchType.UKNOWN ) {
					sb.append(" xmlns:okp=\""+Namespaces.NS_XLIFFOKAPI+"\"");
					sb.append(String.format(" okp:"+XLIFFWriter.OKP_MATCHTYPE+"=\"%s\"", alt.getType().toString()));
				}
			}
			
			sb.append(">"+encoderManager.getLineBreak());
			TextContainer cont = alt.getSource();
			if ( !cont.isEmpty() ) {
				sb.append(String.format("<source xml:lang=\"%s\">", alt.getSourceLocale().toString()));
				// Write full source content (never with segments markers)
				sb.append(fmt.toSegmentedString(cont, 0, false, false, params.getAddAltTransGMode(), false, params.getIncludeIts(), outputLoc));
				sb.append("</source>"+encoderManager.getLineBreak()); // source
			}
			sb.append(String.format("<target xml:lang=\"%s\"", alt.getTargetLocale().toString()));
			// Output ITS attributes
			outputITSAttributes(alt.getTarget().getAnnotation(GenericAnnotations.class),
				params.getEscapeGT(), false, sb, outputLoc, true);
			sb.append(">");
			sb.append(fmt.toSegmentedString(alt.getTarget(), 0, false, false, params.getAddAltTransGMode(), false, params.getIncludeIts(), outputLoc));
			sb.append("</target>"+encoderManager.getLineBreak()); // target
			sb.append("</alt-trans>"+encoderManager.getLineBreak()); // alt-trans
		}
	}
	
	private String getUnsegmentedOutput (TextContainer cont,
		LocaleId locToUse,
		EncoderContext context)
	{
		TextFragment tf = null;
		if ( cont.contentIsOneSegment() ) {
			// One part that is a segment, just get the content
			tf = cont.getFirstContent();
		}
		else { // Else: get a copy of the un-segmented entry
			tf = cont.getUnSegmentedContentCopy();
		}
		// Apply the layer if there is one
		if ( getLayer() == null ) {
			return getContent(tf, locToUse, context);
		}
		else {
			switch ( context ) {
			case SKELETON:
				return getLayer().endCode()
					+ getContent(tf, locToUse, EncoderContext.TEXT)
					+ getLayer().startCode();
			case INLINE:
				return getLayer().endInline()
					+ getContent(tf, locToUse, EncoderContext.TEXT)
					+ getLayer().startInline();
			default:
				return getContent(tf, locToUse, context);
			}
		}
	}
	
	@Override
	protected String expandCodeContent (Code code,
		LocaleId locToUse,
		EncoderContext context)
	{
		// Handle mrk for modifiable attributes
		if ( code.hasOnlyAnnotation()) {
			if ( code.getTagType() == TagType.OPENING ) {
				boolean mtypeNeeded = true;
				StringBuilder tmp = new StringBuilder(code.getOuterData());
				// Existing annotation have outer data
				if ( tmp.length() > 0 ) {
					// So we remove the closing bracket
					tmp.delete(tmp.length()-1, tmp.length());
					mtypeNeeded = false;
				}
				else { // New annotation have no outer data
					// So we create it here
					tmp.append("<mrk");
				}
				// Output the live attributes
				outputITSAttributes(code.getGenericAnnotations(), params.getEscapeGT(),
					mtypeNeeded, tmp, locToUse, false);
				tmp.append(">");
				return tmp.toString();
			}
			else {
				return "</mrk>";
			}
		}
		else { // Normal inline code
			return super.expandCodeContent(code, locToUse, context);
		}
	}
	
	private void outputITSAttributes (GenericAnnotations anns,
		boolean escapeGT,
		boolean mtypeNeeded,
		StringBuilder output,
		LocaleId trgLocId,
		boolean writeMTConfAnnotatorsRef)
	{
		if ( itsCont == null ) {
			itsCont = new ITSContent(chsEnc, false, true);
		}
		itsCont.outputAnnotations(anns, output, true, true, mtypeNeeded, trgLocId);
		if ( writeMTConfAnnotatorsRef ) {
			if ( anns == null ) return;
			GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.MTCONFIDENCE);
			if ( ga == null ) return;
			String ref = ga.getString(GenericAnnotationType.ANNOTATORREF);
			if ( ref == null ) return;
			output.append(" its:annotatorsRef=\"mt-confidence|"+ref+"\"");
		}
	}
	
	// This method assumes bi-lingual pairs are 1-1 and in the same order
	private String getSegmentedOutput (TextContainer srcCont,
		TextContainer trgCont,
		LocaleId locToUse,
		EncoderContext context)
	{
		StringBuilder tmp = new StringBuilder();

		// The output is driven by the target, not the source, so the interstices parts
		// are the ones of the target, no the one of the source
		ISegments srcSegs = srcCont.getSegments();
		for ( TextPart part : trgCont ) {
			if ( part.isSegment() ) {
				int lev = 0; //TODO: score values for RTF
				Segment trgSeg = (Segment)part;
				Segment srcSeg = srcSegs.get(trgSeg.id);
				if ( srcSeg == null ) {
					// A target segment without a corresponding source: give warning
					logger.warn("No source segment found for target segment id='{}':\n\"{}\".",
						trgSeg.id, trgSeg.text.toText());
				}

				// Opening marker
				tmp.append(String.format("<mrk mid=\"%s\" mtype=\"seg\"", trgSeg.id));
				// Output the live attributes
				outputITSAttributes(trgSeg.getAnnotation(GenericAnnotations.class), params.getEscapeGT(),
					false, tmp, locToUse, true);
				tmp.append(">");
				// Write the segment (note: srcSeg can be null)
				if ( getLayer() == null ) {
					// If no layer: just write the target
					tmp.append(getContent(trgSeg.text, locToUse, context));
				}
				else { // If layer: write the bilingual entry
					switch ( context ) {
					case SKELETON:
						tmp.append(getLayer().endCode()
							+ getLayer().startSegment()
							+ ((srcSeg==null) ? "" : getContent(srcSeg.text, locToUse, EncoderContext.TEXT))
							+ getLayer().midSegment(lev)
							+ getContent(trgSeg.text, locToUse, EncoderContext.TEXT)
							+ getLayer().endSegment()
							+ getLayer().startCode());
						break;
					case INLINE:
						tmp.append(getLayer().endInline()
							+ getLayer().startSegment()
							+ ((srcSeg==null) ? "" : getContent(srcSeg.text, locToUse, EncoderContext.TEXT))
							+ getLayer().midSegment(lev)
							+ getContent(trgSeg.text, locToUse, EncoderContext.TEXT)
							+ getLayer().endSegment()
							+ getLayer().startInline());
						break;
					default:
						tmp.append(getLayer().startSegment()
							+ ((srcSeg==null) ? "" : getContent(srcSeg.text, locToUse, EncoderContext.TEXT))
							+ getLayer().midSegment(lev)
							+ getContent(trgSeg.text, locToUse, EncoderContext.TEXT)
							+ getLayer().endSegment());
						break;
					}
				}
				// Closing marker
				tmp.append("</mrk>");
			}
			else { // Normal text fragment
				// Target fragment is used
				tmp.append(getContent(part.text, locToUse, context));
			}
		}

		return tmp.toString();
	}
	
	private boolean doSegments (ITextUnit tu) {
		// Do we always segment?
		switch ( params.getOutputSegmentationType() ) {
		case Parameters.SEGMENTATIONTYPE_SEGMENTED:
			return true;
		case Parameters.SEGMENTATIONTYPE_NOTSEGMENTED:
			return false;
		// As needed: segment only if it's segmented regardless what was there before
		case Parameters.SEGMENTATIONTYPE_ASNEEDED:
			return !tu.getSource().contentIsOneSegment();
		}
		// Otherwise: SEGMENTATIONTYPE_ORIGINAL (do as in the input)
		// So check the property with the info on whether it was segmented or not
		Property prop = tu.getProperty(XLIFFFilter.PROP_WASSEGMENTED);
		if ( prop != null ) {
			return prop.getValue().equals("true");
		}
		return false;
	}

	@Override
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
		Property prop = null;
		String value = null;
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
			else {
				if ( Property.APPROVED.equals(name) ) {
					// Default for approved is 'no' that is no value
					value = "";
				}
				else {
					// Fall back to source if there is no target
					prop = resource.getSourceProperty(name);
				}
			}
		}
		// Check the property we got
		if ( value == null ) {
			if ( prop == null ) {
				logger.warn("Property '{}' not found.", name);
				return "-ERR:PROP-NOT-FOUND-";
			}
			// Else process the value
			value = prop.getValue();
		}
		
		// Now look at the value
		if ( value == null ) {
			logger.warn("Property value for '{}' is null.", name);
			return "-ERR:PROP-VALUE-NULL-";
		}
		// Else: We got the property value
		// Check if it needs to be auto-modified
		if ( Property.LANGUAGE.equals(name) ) {
			// If it is the input locale, we change it with the output locale
			//TODO: Do we need an option to be region-insensitive? (en==en-gb)
			LocaleId locId = LocaleId.fromString(value);
			if ( locId.equals(inputLoc) ) {
				value = outputLoc.toString();
			}
		}
		else if ( Property.ENCODING.equals(name) ) {
			value = outputEncoding;
		}
		else if ( Property.APPROVED.equals(name) ) {
			if ( !value.isEmpty() ) {
				value = String.format(" approved=\"%s\"", value);
			}
		}
		else if ( Property.ITS_LQI.equals(name)) {
			ITSLQIAnnotations anns = resource.getAnnotation(ITSLQIAnnotations.class);
			if (anns == null) {
				value = "";
			}
		}
		else if ( Property.ITS_PROV.equals(name)) {
			ITSProvenanceAnnotations anns = resource.getAnnotation(ITSProvenanceAnnotations.class);
			if (anns == null) {
				value = "";
			}
		}
		else if ( Property.ITS_MTCONFIDENCE.equals(name)) {
			GenericAnnotations anns = resource.getAnnotation(GenericAnnotations.class);
			if ( anns != null ) {
				GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.MTCONFIDENCE);
				if ( ga != null ) {
					value = " its:mtConfidence=\"" + ga.getDouble(GenericAnnotationType.MTCONFIDENCE_VALUE) + "\""
						+ " its:annotatorsRef=\"mt-confidence|" + ga.getString(GenericAnnotationType.ANNOTATORREF) + "\"";
				}
				else {
					value = "";
				}
			}
		}
		
		// Return the native value if possible
		if ( encoderManager == null ) {
			if ( getLayer() == null ) return value;
			else return getLayer().encode(value, context); //TODO: context correct??
		}
		else {
			if ( getLayer() == null ) return encoderManager.toNative(name, value);
			else return getLayer().encode(encoderManager.toNative(name, value), context);
		}
	}

	public ITSStandoffManager getITSStandoffManager () {
		return this.itsStandoffManager;
	}

	public void setITSStandoffManager (ITSStandoffManager itsStandoffManager) {
		this.itsStandoffManager = itsStandoffManager;
	}

	public Parameters getParams() {
		return params;
	}

	public final XLIFFContent getFmt() {
		return fmt;
	}

	public final ITSContent getItsCont() {
		return itsCont;
	}

	public final Map<String, GenericAnnotations> getLqiStandoff() {
		return lqiStandoff;
	}

	public final Map<String, GenericAnnotations> getProvStandoff() {
		return provStandoff;
	}

	public final CharsetEncoder getCharsetEncoder() {
		return chsEnc;
	}

	// For serialization
	protected void setParams(Parameters params) {
		this.params = params;
	}

//	@Override
//	public String getContent (TextFragment tf,
//		LocaleId locToUse,
//		EncoderContext context)
//	{
//		// Output simple text
//		if ( !tf.hasCode() ) {
//			if ( encoderManager == null ) {
//				if ( layer == null ) {
//					return tf.toText();
//				}
//				else {
//					return layer.encode(tf.toText(), context);
//				}
//			}
//			else {
//				if ( layer == null ) {
//					return encoderManager.encode(tf.toText(), context);
//				}
//				else {
//					return layer.encode(
//						encoderManager.encode(tf.toText(), context), context);
//				}
//			}
//		}
//
//		// Output text with in-line codes
//		List<Code> codes = tf.getCodes();
//		StringBuilder tmp = new StringBuilder();
//		String text = tf.getCodedText();
//		Code code;
//		char ch;
//		for ( int i=0; i<text.length(); i++ ) {
//			ch = text.charAt(i);
//			switch ( ch ) {
//			case TextFragment.MARKER_OPENING:
//				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
//				if ( code.hasOnlyAnnotation() ) {
//					if ( itsCont == null ) itsCont = new ITSContent(encoderManager.getCharsetEncoder(), false);
//					tmp.append("<mrk");
//					itsCont.outputAnnotations(code, tmp);
//					tmp.append(">");
//				}
//				else {
//					tmp.append(expandCodeContent(code, locToUse, context));
//				}
//				break;
//			case TextFragment.MARKER_CLOSING:
//				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
//				if ( code.hasOnlyAnnotation() ) {
//					tmp.append("</mrk>");
//				}
//				else {
//					tmp.append(expandCodeContent(code, locToUse, context));
//				}
//				break;
//			case TextFragment.MARKER_ISOLATED:
//				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
//				tmp.append(expandCodeContent(code, locToUse, context));
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
	
}
