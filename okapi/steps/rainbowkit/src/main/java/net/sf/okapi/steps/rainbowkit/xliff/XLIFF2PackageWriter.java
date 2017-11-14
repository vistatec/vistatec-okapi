/*===========================================================================
  Copyright (C) 2011-2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.xliff;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.MergingInfo;
import net.sf.okapi.lib.xliff2.Const;
import net.sf.okapi.lib.xliff2.core.CTag;
import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.Note;
import net.sf.okapi.lib.xliff2.core.Part;
import net.sf.okapi.lib.xliff2.core.StartFileData;
import net.sf.okapi.lib.xliff2.core.StartXliffData;
import net.sf.okapi.lib.xliff2.core.Store;
import net.sf.okapi.lib.xliff2.core.Unit;
import net.sf.okapi.lib.xliff2.its.Domain;
import net.sf.okapi.lib.xliff2.its.ITSWriter;
import net.sf.okapi.lib.xliff2.matches.Match;
import net.sf.okapi.lib.xliff2.writer.XLIFFWriter;
import net.sf.okapi.steps.rainbowkit.common.BasePackageWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XLIFF2PackageWriter extends BasePackageWriter {

	public static final String POBJECTS_DIR = "pobjects";

	private static final String TU_PREFIX = "$tu$";
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private XLIFFWriter writer;
	private LinkedHashMap<String, String> referents;
	private XLIFF2Options options;
	private String rawDocPath;
	private LocaleId trgLoc;

	public XLIFF2PackageWriter () {
		super(Manifest.EXTRACTIONTYPE_XLIFF2);
	}

	@Override
	protected void processStartBatch () {
		// Get the options from the parameters
		options = new XLIFF2Options();
		if ( !Util.isEmpty(params.getWriterOptions()) ) {
			options.fromString(params.getWriterOptions());
		}

		if ( options.getCreateTipPackage() ) {
			manifest.setGenerateTIPManifest(true);
			manifest.setSubDirectories(POBJECTS_DIR+"/input", POBJECTS_DIR+"/bilingual", POBJECTS_DIR+"/bilingual",
				POBJECTS_DIR+"/output", POBJECTS_DIR+"/tm", POBJECTS_DIR+"/skeleton", false);
		}
		else {
			manifest.setSubDirectories("original", "work", "work", "done", null, "skeleton", false);
		}

		// Create TM only for TIP package
		setTMXInfo(options.getCreateTipPackage(), null, false, false, false);
		super.processStartBatch();
	}
	
	// For final zip 
	public boolean getCreeatTipPackage () {
		return options.getCreateTipPackage();
	}
	
	@Override
	protected void processEndBatch () {
		// Base process
		super.processEndBatch();
		
		// TIP-specific process
		if ( options.getCreateTipPackage() ) {
			// Gather the list of TMs created
			ArrayList<String> tms = new ArrayList<String>();
			if ( tmxWriterApproved != null ) {
				if ( tmxWriterApproved.getItemCount() > 0 ) {
					tms.add(Util.getFilename(tmxPathApproved, true));
				}
			}
			if ( tmxWriterAlternates != null ) {
				if ( tmxWriterAlternates.getItemCount() > 0 ) {
					tms.add(Util.getFilename(tmxPathAlternates, true));
				}
			}
			if ( tmxWriterLeverage != null ) {
				if ( tmxWriterLeverage.getItemCount() > 0 ) {
					tms.add(Util.getFilename(tmxPathLeverage, true));
				}
			}
			if ( tmxWriterUnApproved != null ) {
				if ( tmxWriterUnApproved.getItemCount() > 0 ) {
					tms.add(Util.getFilename(tmxPathUnApproved, true));
				}
			}

			// Save the TIP manifest
			manifest.saveTIPManifest(manifest.getTempPackageRoot(), tms);

			// Zip the project files
			String dir = manifest.getTempPackageRoot()+POBJECTS_DIR;

			FileUtil.zipDirectory(dir, ".zip");
			
			// Delete the original
			Util.deleteDirectory(dir, false);
			// The creation of the .tipp file is done at the step level
			// otherwise to be done after the directory is freed from locks
		}
	}
	
	@Override
	protected void processStartDocument (Event event) {
		super.processStartDocument(event);
		
		writer = new XLIFFWriter();
		referents = new LinkedHashMap<String, String>();

		MergingInfo item = manifest.getItem(docId);
		rawDocPath = manifest.getTempSourceDirectory() + item.getRelativeInputPath() + ".xlf";
		// Set the writer's options
		writer.setWithOriginalData(options.getwithOriginalData());
		writer.setUseIndentation(true);
		// Create the writer
		trgLoc = manifest.getTargetLocale();
		Util.createDirectories(rawDocPath); //TODO: This should be done by the writer. To change when it's implemented properly.
		writer.create(new File(rawDocPath), manifest.getSourceLocale().toBCP47(), trgLoc.toBCP47());
		StartXliffData sxd = new StartXliffData(null);
		ITSWriter.addDeclaration(sxd);
		writer.writeStartDocument(sxd, null);
		// Original: use the document name if there is one (null is allowed)
		// For now we don't set ID for the files, the writer will generate them 
		StartFileData sfd = new StartFileData(null);
		sfd.setOriginal(event.getStartDocument().getName());
		writer.setStartFileData(sfd);
	}
	
	@Override
	protected Event processEndDocument (Event event) {
		writer.writeEndDocument();
		writer.close();
		writer = null;
		referents.clear();
		referents = null;
		
		if ( params.getSendOutput() ) {
			return super.creatRawDocumentEventSet(rawDocPath, "UTF-8",
				manifest.getSourceLocale(), manifest.getTargetLocale());
		}
		else {
			return event;
		}
	}

	@Override
	protected void processStartSubDocument (Event event) {
		// Do not start one explicitly
		// Let the first unit to trigger the start of the file
		// otherwise we may get empty file elements
		// One thing to do: set the original (case of the DOCX-type documents with sub-documents)
		StartFileData sfd = new StartFileData(null);
		sfd.setOriginal(event.getStartSubDocument().getName());
		writer.setStartFileData(sfd);
	}
	
	@Override
	protected void processEndSubDocument (Event event) {
		// Safe to call even if writestartFile() was not called
		writer.writeEndFile();
	}
	
	@Override
	protected void processStartGroup (Event event) {
		writer.writeStartGroup(null);
	}
	
	@Override
	protected void processEndGroup (Event event) {
		writer.writeEndGroup();
	}
	
	@Override
	protected void processTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();
		if ( tu.isReferent() ) {
			storeReferent(tu);
		}
		Unit unit = toXLIFF2Unit(tu);
		writer.writeUnit(unit);
		writeTMXEntries(event.getTextUnit());
	}
	
	@Override
	protected void processDocumentPart (Event event) {
		DocumentPart dp = event.getDocumentPart();
		if ( dp.isReferent() ) {
			storeReferent(dp);
		}
	}

	private void storeReferent (IResource res) {
		ISkeleton skel = res.getSkeleton();
		if ( skel == null ) return;
		if ( res instanceof ITextUnit ) {
			referents.put(res.getId(), TU_PREFIX+skel.toString());
		}
		else {
			referents.put(res.getId(), skel.toString());
		}
	}

	/**
	 * Gets the text unit id of the referenced objects.
	 * @param text the initial skeleton string.
	 * @return a list of IDs or empty
	 */
	private String getReferences (String text) {
		if ( text == null ) return null;
		StringBuilder tmp = new StringBuilder();
		StringBuilder data = new StringBuilder(text);
		Object[] res = null;
		do {
			// Check if that data has a reference marker
			res = TextFragment.getRefMarker(data);
			if ( res != null ) {
				String refId = (String)res[0];
				if ( !refId.equals("$self$") ) {
					String skel = referents.get(refId);
					if ( skel != null ) {
						if ( !skel.startsWith(TU_PREFIX) ) {
							String refs = getReferences(skel);
							if ( refs != null ) {
								tmp.append(refs+" ");
							}
						}
						else { // text unit
							tmp.append(refId+" ");
						}
					}
					else {
						tmp.append(refId+" ");
					}
				}
				// Remove this and check for next
				data.delete((Integer)res[1], (Integer)res[2]);
			}
		}
		while ( res != null );
		return tmp.toString().trim(); 
	}
	
	@Override
	public void close () {
		if ( writer != null ) {
			writer.close();
			writer = null;
		}
	}

	@Override
	public String getName () {
		return getClass().getName();
	}

	protected Unit toXLIFF2Unit (ITextUnit tu) {
		Unit unit = new Unit(tu.getId());
		boolean doEliminateEmptyTargetsWithNonEmptySource = options.getEliminateEmptyTargetsWithNonEmptySource();
		
		TextContainer srcTc = tu.getSource();
		TextContainer trgTc = null;
		if ( tu.hasTarget(manifest.getTargetLocale()) ) {
			trgTc = tu.getTarget(manifest.getTargetLocale());
			if ( trgTc.getSegments().count() != srcTc.getSegments().count() ) {
				// Use un-segmented entry if we have different number of segments
				LOGGER.warn("Text unit id='{}' has different number of segments in source and target.\n"
					+"This entry will be output un-segmented.", tu.getId());
				srcTc = tu.getSource().clone(); srcTc.joinAll();
				trgTc = tu.getTarget(manifest.getTargetLocale()).clone(); trgTc.joinAll();
			}
		}
		
		if ( !Util.isEmpty(tu.getType()) ) {
			unit.setType("okp:"+tu.getType().replace(':', '-'));
		}
		if ( !Util.isEmpty(tu.getName()) ) {
			unit.setName(tu.getName());
		}
		unit.setTranslate(tu.isTranslatable());
		
		// Add trans-unit level note if needed

		boolean noteDone = false;
		GenericAnnotations anns = tu.getAnnotation(GenericAnnotations.class);
		if ( anns != null ) {
			GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.LOCNOTE);
			if ( ga != null ) {
				Note note = new Note(ga.getString(GenericAnnotationType.LOCNOTE_VALUE), Note.AppliesTo.UNDEFINED);
				if ( !"alert".equals(ga.getString(GenericAnnotationType.LOCNOTE_TYPE)) ) note.setPriority(2);
				unit.addNote(note);
				noteDone = true;
			}
		}
		if ( !noteDone && tu.hasProperty(Property.NOTE) ) {
			Note note = new Note(tu.getProperty(Property.NOTE).getValue(), Note.AppliesTo.UNDEFINED);
			note.setPriority(2);
			unit.addNote(note);
		}
		// Add trans-unit level translator note if needed 
		if ( tu.hasProperty(Property.TRANSNOTE) ) {
			Note note = new Note("From Translator: "+tu.getProperty(Property.TRANSNOTE).getValue(), Note.AppliesTo.UNDEFINED);
			note.setPriority(2);
			unit.addNote(note);
		}
		// Add source notes
		if ( tu.hasSourceProperty(Property.NOTE) ) {
			Note note = new Note(tu.getSourceProperty(Property.NOTE).getValue(), Note.AppliesTo.SOURCE);
			note.setPriority(2);
			unit.addNote(note);
		}
		// Add target notes
		if ( tu.hasTargetProperty(manifest.getTargetLocale(), Property.NOTE) ) {
			Note note = new Note(tu.getTargetProperty(manifest.getTargetLocale(), Property.NOTE).getValue(), Note.AppliesTo.TARGET);
			note.setPriority(2);
			unit.addNote(note);
		}
		
		if ( anns != null ) {
			// Storage Size
//			GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.STORAGESIZE);
//			if ( ga != null ) {
//				unit.getExtAttributes().setAttribute(Names.NS_ITS, "storageSize",
//					ga.getString(GenericAnnotationType.STORAGESIZE_SIZE));
//				String tmp = ga.getString(GenericAnnotationType.STORAGESIZE_ENCODING);
//				if ( !tmp.equals("UTF-8") ) {
//					unit.getExtAttributes().setAttribute(Names.NS_ITS, "storageEncoding", tmp);
//				}
//				tmp = ga.getString(GenericAnnotationType.STORAGESIZE_LINEBREAK);
//				if ( !tmp.equals("lf") ) {
//					unit.getExtAttributes().setAttribute(Names.NS_ITS, "lineBreakType", tmp);
//				}
//			}
			
			// Domain
			GenericAnnotation ga = anns.getFirstAnnotation(GenericAnnotationType.DOMAIN);
			if ( ga != null ) {
				unit.getITSItems().add(new Domain(ga.getString(GenericAnnotationType.DOMAIN_VALUE)));
			}
			// Allowed characters
			ga = anns.getFirstAnnotation(GenericAnnotationType.ALLOWEDCHARS);
			if ( ga != null ) {
				unit.getExtAttributes().setAttribute(Const.NS_ITS, "allowedCharacters",
					ga.getString(GenericAnnotationType.ALLOWEDCHARS_VALUE));
			}
//			// External Resource reference
//			ga = anns.getFirstAnnotation(GenericAnnotationType.EXTERNALRES);
//			if ( ga != null ) {
//				unit.getExtAttributes().setAttribute(Namespaces.ITSXLF_NS_URI, "externalResourceRef",
//					ga.getString(GenericAnnotationType.EXTERNALRES_VALUE));
//			}
		}

		unit.setTranslate(tu.isTranslatable());
		// Go through the parts: Use the source to drive the order
		// But match on segment ids
		TextPart part;
		ISegments trgSegs = null;
		if ( trgTc != null ) {
			trgSegs = trgTc.getSegments();
		}
		int srcSegIndex = -1;
		for ( int i=0; i<srcTc.count(); i++ ) {
			part = srcTc.get(i);
			
			if ( part.isSegment() ) {
				Segment srcSeg = (Segment)part;
				srcSegIndex++;
				net.sf.okapi.lib.xliff2.core.Segment xSeg = unit.appendSegment();
				xSeg.setSource(toXLIFF2Fragment(srcSeg.text, unit.getStore(), false));
				// Copy the segment id, but only after checking (it may conflict with inline marker's id)
				// We should do this after all segments are created
				// if ( !unit.isIdUsed(srcSeg.getId()) ) xSeg.setId(srcSeg.getId());
				
				// Applies TU-level white space
				xSeg.setPreserveWS(tu.preserveWhitespaces());
				
				// Target
				if ( trgSegs != null ) {
					Segment trgSeg = trgSegs.get(srcSeg.getId());

					if ( trgSeg != null ) {
						// Eliminate target that are empty if source is not
						if ( doEliminateEmptyTargetsWithNonEmptySource && trgSeg.getContent().isEmpty() && !srcSeg.getContent().isEmpty() )
						    trgSeg = null;
					}
					if ( trgSeg != null ) {
						
						xSeg.setTarget(toXLIFF2Fragment(trgSeg.text, unit.getStore(), true));
						// Check if the order is the same as the source
						int trgSegIndex = trgSegs.getIndex(srcSeg.getId());
						if ( srcSegIndex != trgSegIndex ) {
							// Target is cross-aligned
							int trgPartIndex = trgSegs.getPartIndex(trgSegIndex);
							xSeg.setTargetOrder(trgPartIndex+1);
						}
						
						// We cannot set the annotations here because additional parts with inline codes 
						// may still need to be added and setting annotation would use their ID values.
					}
				}
			}
			else { // Non-segment part
				Part xPart = unit.appendIgnorable();
				// Applies TU-level white space
				xPart.setPreserveWS(tu.preserveWhitespaces());
				xPart.setSource(toXLIFF2Fragment(part.text, unit.getStore(), false));
				// Target
				if ( trgTc != null ) {
//todo				trcTc.get
				}
			}
		}

		// Add the annotations after converting all segments
		// To allow auto-IDs to be set with values not colliding with inline codes
		trgSegs = null;
		if ( trgTc != null ) {
			trgSegs = trgTc.getSegments();
		}
		srcSegIndex = -1;
		for ( int i=0; i<srcTc.count(); i++ ) {
			part = srcTc.get(i);
			if ( part.isSegment() ) {
				Segment srcSeg = (Segment)part;
				srcSegIndex++;
				// Target
				if ( trgSegs != null ) {
					Segment trgSeg = trgSegs.get(srcSeg.getId());
					if ( trgSeg != null ) {
						// Eliminate target that are empty if source is not
						if ( doEliminateEmptyTargetsWithNonEmptySource && trgSeg.getContent().isEmpty() && !srcSeg.getContent().isEmpty() ) 
							trgSeg = null;
					}
					if ( trgSeg != null ) {
						net.sf.okapi.lib.xliff2.core.Segment xSeg = unit.getSegment(srcSegIndex);
						// Alt-trans annotation?
						AltTranslationsAnnotation ann = trgSeg.getAnnotation(AltTranslationsAnnotation.class);
						if ( ann != null ) {
							for ( AltTranslation alt : ann ) {
								setMatch(alt, xSeg);
							}
						}
						ann = trgTc.getAnnotation(AltTranslationsAnnotation.class);
						if ( ann != null ) {
							for ( AltTranslation alt : ann ) {
								setMatch(alt, xSeg);
							}
						}
					}
				}
			}
		}
		
		return unit;
	}
	
	private void setMatch (AltTranslation alt,
		net.sf.okapi.lib.xliff2.core.Segment xSeg)
	{
		// Create an annotation
		Match match = Match.annotate(xSeg.getSource(), 0, -1, new Match());
		// Set the content
		match.setSource(toXLIFF2Fragment(
			alt.getEntry().getSource().getFirstContent(), match.getStore(), false));
		match.setTarget(toXLIFF2Fragment(
			alt.getEntry().getTarget(trgLoc).getFirstContent(), match.getStore(), true));
		// Copy the information
		match.setSimilarity(1.0*alt.getFuzzyScore());
		int cs = alt.getCombinedScore();
		if ( cs != QueryResult.COMBINEDSCORE_UNDEFINED ) {
			match.setMatchSuitability(1.0*cs);
		}
		int qs = alt.getQualityScore();
		if ( qs != QueryResult.QUALITY_UNDEFINED ) {
			match.setMatchQuality(1.0*qs);
		}
		match.setOrigin(alt.getOrigin());
		alt.getTool();
		switch ( alt.getType() ) {
		case CONCORDANCE:
			break;
		case EXACT:
			break;
		case EXACT_DOCUMENT_CONTEXT:
			break;
		case EXACT_LOCAL_CONTEXT:
			match.setType("icm");
			break;
		case EXACT_PREVIOUS_VERSION:
			break;
		case EXACT_STRUCTURAL:
			break;
		case EXACT_TEXT_ONLY:
			break;
		case EXACT_TEXT_ONLY_PREVIOUS_VERSION:
			break;
		case EXACT_TEXT_ONLY_UNIQUE_ID:
		case EXACT_UNIQUE_ID:
		case FUZZY_UNIQUE_ID:
			match.setType("idm");
			break;
		case FUZZY:
			break;
		case FUZZY_PREVIOUS_VERSION:
			break;
		case HUMAN_RECOMMENDED:
			match.setType("other");
			break;
		case MT:
			match.setType("mt");
			break;
		case PHRASE_ASSEMBLED:
		case FUZZY_REPAIRED:
		case EXACT_REPAIRED:
			match.setType("am");
			break;
		case UKNOWN:
		default:
			match.setType("other");
			break;
		}
		if ( alt.getType() != MatchType.UKNOWN ) {
			match.setSubType("okp:"+alt.getType());
		}
	}
	
	private Fragment toXLIFF2Fragment (TextFragment tf,
		Store store,
		boolean isTarget)
	{
		// Fast track for content without codes
		if ( !tf.hasCode() ) {
			return new Fragment(store, isTarget, tf.getCodedText());
		}
		
		// Otherwise: we map the codes
		Fragment xFrag = new Fragment(store, isTarget);
		String ctext = tf.getCodedText();
		List<Code> codes = tf.getCodes();

		int index;
		Code code;
		boolean mayOverlapDefault = false; // Most spanning codes may not overlap
		for ( int i=0; i<ctext.length(); i++ ) {
			if ( TextFragment.isMarker(ctext.charAt(i)) ) {
				index = TextFragment.toIndex(ctext.charAt(++i));
				code = codes.get(index);
				
				GenericAnnotations anns = code.getGenericAnnotations();
				if ( anns != null ) {
					
				}
				
				CTag xCode;
				switch ( code.getTagType() ) {
				case OPENING:
					xCode = xFrag.append(net.sf.okapi.lib.xliff2.core.TagType.OPENING,
						String.valueOf(code.getId()), code.getData(), mayOverlapDefault);
					break;
				case CLOSING:
					xCode = xFrag.append(net.sf.okapi.lib.xliff2.core.TagType.CLOSING,
						String.valueOf(code.getId()), code.getData(), mayOverlapDefault);
					break;
				case PLACEHOLDER:
				default:
					xCode = xFrag.appendCode(String.valueOf(code.getId()), code.getData());
					break;
				}
				if ( code.hasReference() ) {
					String data = getReferences(code.getData());
					if ( !Util.isEmpty(data) ) {
						xCode.setSubFlows(data);
					}
				}
				xCode.setDisp(code.getDisplayText());
				xCode.setCanCopy(code.isCloneable());
				xCode.setCanDelete(code.isDeleteable());
			}
			else {
				xFrag.append(ctext.charAt(i));
			}
		}

		return xFrag;
	}

}
