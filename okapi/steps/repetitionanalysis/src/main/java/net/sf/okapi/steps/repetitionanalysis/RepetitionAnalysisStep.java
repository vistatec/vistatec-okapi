/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.repetitionanalysis;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.tm.pensieve.common.MetadataType;
import net.sf.okapi.tm.pensieve.common.TmHit;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.common.TranslationUnitVariant;
import net.sf.okapi.tm.pensieve.seeker.ITmSeeker;
import net.sf.okapi.tm.pensieve.seeker.PensieveSeeker;
import net.sf.okapi.tm.pensieve.writer.PensieveWriter;
import net.sf.okapi.tm.pensieve.writer.TmWriterFactory;

/**
 * The step analyzes repetitions in input documents. Either exact or configurable fuzzy search is performed.
 * <p>
 * 2 types of annotations are created for found repetitive segments -- RepetitiveSegmentAnnotation and AltTranslationsAnnotation.  
 * RepetitiveSegmentAnnotation's are attached to all repetitive source segments. 
 * AltTranslationsAnnotation's are attached to target segments, corresponding to repetitive source segments.
 * AltTranslationsAnnotation is not attached for the first repetitive segment not to be counted by counting steps
 * twice as repetitive with itself.
 */
public class RepetitionAnalysisStep extends BasePipelineStep {

	private Parameters params;
	private boolean searchExact;
	/**
	 * Counter of non-empty segments within a TU (local segment counter).
	 */
	private int segCounter;
	/**
	 * Counter of all non-empty segments within the document (global segment counter). 
	 */
	private long tuCounter;	
	/**
	 * Counter of TUs that have at least one non-empty segment (global TU counter).
	 */
	private long groupCounter;	
	private String tmDir;
	private PensieveWriter tmWriter;
	private ITmSeeker currentTm;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;

	public RepetitionAnalysisStep() {
		super();
		params = new Parameters();
		//tmDir = Util.ensureSeparator(Util.getTempDirectory(), true) + "tm/";
	}
	
	@Override
	public String getName() {
		return "Repetition Analysis";
	}

	@Override
	public String getDescription() {
		return "Analyzes repetitions in input documents. Adds AltTranslationsAnnotation and RepetitiveSegmentAnnotation to " +
				"a repetitive segment."
		+ " Expects: filter events. Sends back: filter events.";
	}
	
	@Override
	public IParameters getParameters () {
		return params;
	}
	
	@Override
	public void cancel() {
		close();
	}
	
	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale (LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	private void close() {
		if (tmWriter != null) {
			tmWriter.close();
			tmWriter = null;
		}
		if (currentTm != null) {
			currentTm.close();
			currentTm = null;
		}
		if (tmDir != null) Util.deleteDirectory(tmDir, false);
	}
	
	@Override
	protected Event handleStartDocument(Event event) {
		close();
		// For concurrent pipelines 
		tmDir = String.format("%s~okapi-step-repetitionanalysis-%s/", 
				Util.ensureSeparator(Util.getTempDirectory(), true), 
				UUID.randomUUID().toString());
		Util.createDirectories(tmDir);
		searchExact = params.getFuzzyThreshold() >= 100;
		
		tuCounter = 0;
		groupCounter = 1;
		
		tmWriter = (PensieveWriter) TmWriterFactory.createFileBasedTmWriter(tmDir, true);
		currentTm = new PensieveSeeker(tmWriter.getIndexWriter());
		
		return super.handleStartDocument(event);
	}
	
	@Override
	protected Event handleEndDocument(Event event) {
		close();		
		return super.handleEndDocument(event);
	}
	
	public static boolean checkSegments(Segment sseg, Segment tseg) {
		// tseg is allowed to be null
		return	sseg != null && 
				(sseg.getContent().hasText() || 
				(tseg != null && tseg.getContent().hasText()));
	}
	
	@Override
	protected Event handleTextUnit(Event event) {
		ITextUnit tu = event.getTextUnit();
		if (tu.isTranslatable()) {
			ISegments ssegments = tu.getSource().getSegments();
			ISegments tsegments = null;
			
			if (targetLocale != null) {
				tsegments = tu.getTargetSegments(targetLocale);
//				TextContainer ttc = tu.getTarget(targetLocale);				
//				if (ttc != null) tsegments = ttc.getSegments();
			}
			
			segCounter = 0;
			boolean hasTranslationUnits = false;
			for (Segment seg : ssegments) {
				segCounter++;
				Segment tseg = null;
				if (tsegments != null) {
					tseg = tsegments.get(seg.getId());
				}
				if (!checkSegments(seg, tseg)) continue;
				tuCounter++;
				hasTranslationUnits = true;
				
				// We don't take codes into account when analizing repetitive text
				TextFragment content = seg.getContent();
				if (content.isEmpty()) continue;
				
				TextFragment tf = new TextFragment(content.getText());
				
				String tuid = Long.toString(tuCounter);
				String groupId = Long.toString(groupCounter);
				String segId = Long.toString(segCounter);
				
				SegmentInfo info = new SegmentInfo(tuid, groupId, segId); 
				
				List<TmHit> hits = new ArrayList<TmHit>();
				hits.addAll(currentTm.searchExact(tf, null));
				if (!searchExact) {
					hits.addAll(currentTm.searchFuzzy(tf, params.getFuzzyThreshold(), params.getMaxHits(), null));
				}
								
				if (hits.size() > 0) {
					RepetitiveSegmentAnnotation ann =
							new RepetitiveSegmentAnnotation(info, hits);
					seg.setAnnotation(ann);
					
					for (TmHit hit : hits) {
						TranslationUnit hitTu = hit.getTu();
						
						if (tsegments != null) {
							TextFragment otf = new TextFragment(tf.getText());
							TextFragment stf = new TextFragment(hitTu.getSource().getContent().getText());
							TextFragment ttf = new TextFragment(hitTu.getTarget().getContent().getText());
							
							// For word counts
							AltTranslationsAnnotation ata = tseg.getAnnotation(AltTranslationsAnnotation.class);
							if (ata == null) {
								ata = new AltTranslationsAnnotation();
								tseg.setAnnotation(ata);
							}
							ata.add(new AltTranslation(sourceLocale, targetLocale == null ? sourceLocale : targetLocale, 
									otf, stf, ttf, MatchType.EXACT_DOCUMENT_CONTEXT,
									//tf, stf, ttf, MatchType.EXACT_DOCUMENT_CONTEXT,
									//Math.round(hit.getScore() * 100), ""));
									(int) Math.floor(hit.getScore()), ""));							
						}
					}					
				}
				
				TranslationUnit ntu = new TranslationUnit(
						new TranslationUnitVariant(sourceLocale, tf),
						new TranslationUnitVariant(targetLocale == null ? sourceLocale : targetLocale, 
								new TextFragment(tuid))); // To have a unique target
				ntu.setMetadataValue(MetadataType.ID, tuid);
				
				// TODO create real MetadataTypes for these
				ntu.setMetadataValue(MetadataType.GROUP_NAME, groupId);
				ntu.setMetadataValue(MetadataType.FILE_NAME, segId);
				
				// The segment can be referenced from the maps in RSA of other segments, so we create a RSA for it
				if (seg.getAnnotation(RepetitiveSegmentAnnotation.class) == null) {
					RepetitiveSegmentAnnotation ann = 
							new RepetitiveSegmentAnnotation(info, (List<TmHit>) null);
					seg.setAnnotation(ann);
				}
				
				tmWriter.indexTranslationUnit(ntu);
				
				// Should be called here after every segment addition to the TM for the situations 
				// of repetitive segments within a tu
				tmWriter.commit();
			}
			if (hasTranslationUnits) groupCounter++;
		}
		return super.handleTextUnit(event);
	}
}
