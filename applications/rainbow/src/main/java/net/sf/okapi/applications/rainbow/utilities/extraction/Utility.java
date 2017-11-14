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

package net.sf.okapi.applications.rainbow.utilities.extraction;

import java.io.File;

import net.sf.okapi.applications.rainbow.packages.IWriter;
import net.sf.okapi.applications.rainbow.utilities.BaseFilterDrivenUtility;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.lib.segmentation.SRXDocument;
import net.sf.okapi.lib.translation.QueryManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utility extends BaseFilterDrivenUtility {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Parameters params;
	private IWriter writer;
	private int id;
	private ISegmenter sourceSeg;
	private ISegmenter targetSeg;
	private QueryManager qm;
	private String resolvedOutputDir;
	private HTMLReporter htmlRpt;
	private boolean downgradeIdenticalBestMatches;
	
	private static final String HTML_REPORT_NAME = "report.html";
	
	public Utility () {
		params = new Parameters();
		needsSelfOutput = false;
	}
	
	public String getName () {
		return "oku_extraction";
	}
	
	public void preprocess () {
		// Load SRX file(s) and create segmenters if required
		if ( params.getPreSegment() ) {
			String src = params.getSourceSRX().replace(VAR_PROJDIR, projectDir);
			SRXDocument doc = new SRXDocument();
			doc.loadRules(src);
			if ( doc.hasWarning() ) logger.warn(doc.getWarning());
			sourceSeg = doc.compileLanguageRules(srcLang, null);

			// Load the target only if needed
			if ( !Util.isEmpty(params.getTargetSRX()) ) {
				String trg = params.getTargetSRX().replace(VAR_PROJDIR, projectDir);
				//TODO: This is not working cross-platform!
				if ( !src.equalsIgnoreCase(trg) ) {
					doc.loadRules(trg);
					if ( doc.hasWarning() ) logger.warn(doc.getWarning());
				}
			}
			targetSeg = doc.compileLanguageRules(trgLang, null);
		}

		downgradeIdenticalBestMatches = false;
		if ( params.getPkgType().equals("xliff") ) {
			writer = new net.sf.okapi.applications.rainbow.packages.xliff.Writer();
			writer.setParameters(params.getXliffOptions());
		}
		else if ( params.getPkgType().equals("omegat") )
			writer = new net.sf.okapi.applications.rainbow.packages.omegat.Writer();
		else if ( params.getPkgType().equals("rtf") ) {
			writer = new net.sf.okapi.applications.rainbow.packages.rtf.Writer();
			downgradeIdenticalBestMatches = true;
		}
		else {
			throw new OkapiException("Unknown package type: " + params.getPkgType());
		}
		
		if ( params.getPreTranslate() ) {
			qm = new QueryManager();
			qm.setLanguages(srcLang, trgLang);
			qm.setThreshold(params.getThreshold());
			qm.setRootDirectory(projectDir);
			qm.addAndInitializeResource(params.getTransResClass(), null, params.getTransResParams());
			if ( params.getUseTransRes2() ) {
				qm.addAndInitializeResource(params.getTransResClass2(), null, params.getTransResParams2());
				// TODO: We now accomplish this via connector weights  
				// qm.setReorder(false); // Keep results grouped by resources
			}
			qm.setOptions(params.getThreshold(), false, false, downgradeIdenticalBestMatches, null, 0, false);
		}
		
		resolvedOutputDir = params.getOutputFolder() + File.separator + params.getPkgName();
		resolvedOutputDir = resolvedOutputDir.replace(VAR_PROJDIR, projectDir);
		Util.deleteDirectory(resolvedOutputDir, false);
		
		id = 0;
		String pkgId = params.makePackageID();
		// Use the hash code of the input root for project ID, just to have one
		writer.setInformation(srcLang, trgLang, Util.makeId(inputRoot),
			resolvedOutputDir, pkgId, inputRoot, params.getPreSegment(),
			"rainbow");
		writer.writeStartPackage();

		htmlRpt = new HTMLReporter();
		htmlRpt.create(resolvedOutputDir+File.separator+HTML_REPORT_NAME);
	}

	public void postprocess () {
		if ( writer != null ) {
			writer.writeEndPackage(params.getCreateZip());
			writer = null;
		}
		if ( qm != null ) {
			qm.close();
			qm = null;
		}
		if ( htmlRpt != null ) {
			htmlRpt.close();
			htmlRpt = null;
		}
	}
	
	public IParameters getParameters () {
		return params;
	}

	public boolean hasParameters () {
		return true;
	}

	public boolean needsRoots () {
		return true;
	}

	public void setParameters (IParameters paramsObject) {
		params = (Parameters)paramsObject;
	}

	public boolean isFilterDriven () {
		return true;
	}

	public int requestInputCount () {
		return 1;
	}

	@Override
	public String getFolderAfterProcess () {
		return resolvedOutputDir;
	}

	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			processStartDocument((StartDocument)event.getResource());
			break;
		case END_DOCUMENT:
			htmlRpt.endDocument();
			break;
		case TEXT_UNIT:
			processTextUnit(event.getTextUnit());
			break;
		case RAW_DOCUMENT:
			processFileResource((RawDocument)event.getResource());
			break;
		default:
			break;
		}
		// All events then go to the actual writer
		return writer.handleEvent(event);
	}

	/**
	 * Handles files without any associated filter settings (.png, etc.)
	 * @param fr The file resource to process.
	 */
	private void processFileResource (RawDocument fr) {
		String relativeInput = getInputPath(0).substring(inputRoot.length()+1);
		writer.createCopies(++id, relativeInput);
	}
	
    private void processStartDocument (StartDocument resource) {
		htmlRpt.startDocument(getInputPath(0));
		if (( qm != null ) && params.getUseFileName() ) {
			qm.setAttribute("FileName", Util.getFilename(getInputPath(0), true));
		}
		String relativeInput = getInputPath(0).substring(inputRoot.length()+1);
		String relativeOutput = getOutputPath(0).substring(outputRoot.length()+1);
		String res[] = FilterConfigurationMapper.splitFilterFromConfiguration(getInputFilterSettings(0));
		
		if ( params.getPkgType().equals("rtf") ) {
			((net.sf.okapi.applications.rainbow.packages.rtf.Writer)writer).setSkeletonWriter(
				filter.createSkeletonWriter());
		}

		writer.createOutput(++id, relativeInput, relativeOutput,
			getInputEncoding(0), getOutputEncoding(0),
			res[0], resource.getFilterParameters(), resource.getFilterWriter().getEncoderManager());
    }
	
    private void processTextUnit (ITextUnit tu) {
    	// Do not process non-translatable text units
    	if ( !tu.isTranslatable() ) return;
    	
    	boolean approved = false;
    	Property prop = tu.getTargetProperty(trgLang, Property.APPROVED);
    	if ( prop != null ) {
    		if ( "yes".equals(prop.getValue()) ) approved = true;
    	}

    	TextContainer cont = null;
		// Segment if requested
		if ( params.getPreSegment() && !approved ) {
			try {
				cont = tu.getSource();
				if ( !cont.hasBeenSegmented() ) {
					sourceSeg.computeSegments(cont);
					cont.getSegments().create(sourceSeg.getRanges());
				}
				if ( tu.hasTarget(trgLang) ) {
					cont = tu.getTarget(trgLang);
					if ( !cont.hasBeenSegmented() ) {
						targetSeg.computeSegments(cont);
						cont.getSegments().create(targetSeg.getRanges());
					}
				}
			}
			catch ( Throwable e ) {
				logger.error("Error segmenting text unit id={}: {}", tu.getId(), e.getMessage());
			}
		}
		
		// Compute the statistics
		int n = tu.getSource().getSegments().count();
		htmlRpt.addSegmentCount(n==0 ? 1 : n);

		// Leverage if requested
		if (( qm != null ) && !approved ) {
			if ( params.getUseGroupName() && ( tu.getName() != null )) {
				qm.setAttribute("GroupName", tu.getName());
			}
			
			qm.leverage(tu); //, params.threshold, downgradeIdenticalBestMatches, null, 0);
			
			// Compute statistics
			cont = tu.getTarget(trgLang);
			if ( cont != null ) {
				tallyResults(cont.getAnnotation(AltTranslationsAnnotation.class));
				for ( Segment seg : cont.getSegments() ) {
					tallyResults(seg.getAnnotation(AltTranslationsAnnotation.class));
				}
			}
		}
	}

    private void tallyResults (AltTranslationsAnnotation atAnn) {
    	if ( atAnn == null ) {
    		return;
    	}
    	// Counting only the top matches, so we can get percentages
    	AltTranslation best = atAnn.getFirst();
    	if ( best != null ) {
    		if ( best.getCombinedScore() > 99 ) htmlRpt.addExactMatch(1);
    		else if ( best.getCombinedScore() != 0 ) htmlRpt.addFuzzyMatch(1);
    	}
    }
}
