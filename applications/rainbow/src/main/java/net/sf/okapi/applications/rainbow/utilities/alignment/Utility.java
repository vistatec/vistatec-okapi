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

package net.sf.okapi.applications.rainbow.utilities.alignment;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.okapi.applications.rainbow.utilities.BaseFilterDrivenUtility;
import net.sf.okapi.common.ConfigurationString;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.lib.segmentation.SRXDocument;
import net.sf.okapi.tm.simpletm.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utility extends BaseFilterDrivenUtility {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Parameters params;
	private String fileName;
	private DbStoreBuilder dbStoreBuilder;
	private DbStore dbStore;
	private TMXWriter tmxWriter = null;
	private TMXWriter tmxWriterForUnknown = null;
	private Database simpleTm = null;
	private IFilter trgFilter;
	private ISegmenter srcSeg;
	private ISegmenter trgSeg;
	private int aligned;
	private int alignedTotal;
	private int noText;
	private int noTextTotal;
	private int count;
	private int countTotal;
	private int manual;
	private int manualTotal;
	private Aligner aligner;
	private boolean stopProcess;
	private int targetCount;
	private Map<String, String> originalAttributes;
	private Map<String, String> assignedAttributes;

	public Utility () {
		params = new Parameters();
		needsSelfOutput = false;
	}
	
	public String getName () {
		return "oku_alignment";
	}
	
	public void preprocess () {
		// Load the segmentation rules
		String trgSrxPath = params.getTargetSrxPath().replace(VAR_PROJDIR, projectDir);
		if ( params.getSegment()) {
			String srcSrxPath = params.getSourceSrxPath().replace(VAR_PROJDIR, projectDir);
			SRXDocument doc = new SRXDocument();
			doc.loadRules(srcSrxPath);
			if ( doc.hasWarning() ) logger.warn(doc.getWarning());
			srcSeg = doc.compileLanguageRules(srcLang, null);
			if ( !srcSrxPath.equals(trgSrxPath) ) {
				doc.loadRules(trgSrxPath);
				if ( doc.hasWarning() ) logger.warn(doc.getWarning());
			}
			trgSeg = doc.compileLanguageRules(trgLang, null);
		}
		
		// Prepare the TMX output if requested
		if ( params.getCreateTMX() ) {
			if ( tmxWriter != null ) {
				tmxWriter.close();
				tmxWriter = null;
			}
            tmxWriter = new TMXWriter(params.getTmxPath().replace(VAR_PROJDIR, projectDir));
			tmxWriter.setTradosWorkarounds(params.getUseTradosWorkarounds());
			tmxWriter.writeStartDocument(srcLang, trgLang,
				getName(), null, (params.getSegment() ? "sentence" : "paragraph"),
				null, null);
		}
		if ( params.getCreateTMXForUnknown() ) {
			if ( tmxWriterForUnknown != null ) {
				tmxWriterForUnknown.close();
				tmxWriterForUnknown = null;
			}
            tmxWriterForUnknown = new TMXWriter(params.getTmxForUnknownPath().replace(VAR_PROJDIR, projectDir));
			tmxWriterForUnknown.setTradosWorkarounds(params.getUseTradosWorkarounds());
			tmxWriterForUnknown.writeStartDocument(srcLang, trgLang,
				getName(), null, (params.getSegment() ? "sentence" : "paragraph"),
				null, null);
		}
		
		// Prepare the simpletm database
		if ( params.getCreateTM() ) {
			simpleTm = new Database();
			simpleTm.create(params.getTmPath().replace(VAR_PROJDIR, projectDir), true, trgLang);
		}
		
		// Prepare the attributes if needed
		if ( params.getCreateAttributes() ) {
			ConfigurationString cfgString = new ConfigurationString(
				params.getAttributes());
			originalAttributes = cfgString.toMap();
			assignedAttributes = new LinkedHashMap<String, String>();
		}
		
		// Prepare exclusion pattern if needed
		if ( params.getUseExclusion() ) {
			if ( tmxWriter != null ) {
				tmxWriter.setExclusionOption(params.getExclusion());
			}
			if ( tmxWriterForUnknown != null ) {
				tmxWriterForUnknown.setExclusionOption(params.getExclusion());
			}
		}
		else {
			if ( tmxWriter != null ) {
				tmxWriter.setExclusionOption(null);
			}
			if ( tmxWriterForUnknown != null ) {
				tmxWriterForUnknown.setExclusionOption(null);
			}
		}
		
		// Prepare the db store
		dbStoreBuilder = new DbStoreBuilder();
		dbStoreBuilder.setSegmenter(trgSeg);
		dbStoreBuilder.setOptions(trgLang, null);
		
		if ( aligner == null ) {
			aligner = new Aligner(shell, help);
			aligner.setInfo(trgSrxPath, params.getCheckSingleSegUnit(),
				params.getUseAutoCorrection(), srcLang, trgLang, params.getMtKey());
		}
		
		alignedTotal = 0;
		noTextTotal = 0;
		countTotal = 0;
		manualTotal = 0;
	}
	
	public void postprocess () {
		logger.info("Total translatable text units = {}", countTotal);
		logger.info("Total without text = {}", noTextTotal);
		logger.info("Total aligned = {} (manually modified = {})", alignedTotal, manualTotal);
    	
		if ( aligner != null ) {
			aligner.closeWithoutWarning();
			aligner = null;
		}
		if ( tmxWriter != null ) {
			tmxWriter.writeEndDocument();
			tmxWriter.close();
			tmxWriter = null;
		}
		if ( tmxWriterForUnknown != null ) {
			tmxWriterForUnknown.writeEndDocument();
			tmxWriterForUnknown.close();
			tmxWriterForUnknown = null;
		}
		if ( simpleTm != null ) {
			simpleTm.close();
			simpleTm = null;
		}
		if ( dbStore != null ) {
			dbStore.close();
			dbStore = null;
		}
		srcSeg = null;
		trgSeg = null;
	}
	
	public IParameters getParameters () {
		return params;
	}

	public boolean hasParameters () {
		return true;
	}

	public boolean needsRoots () {
		return false;
	}
	
	public void setParameters (IParameters paramsObject) {
		params = (Parameters)paramsObject;
	}

	public boolean isFilterDriven () {
		return true;
	}

	public int inputCountRequested () {
		// Source and possibly target
		return 2;
	}

	@Override
	public String getFolderAfterProcess () {
		if ( params.getCreateTMX() ) {
			return Util.getDirectoryName(params.getTmxPath().replace(VAR_PROJDIR, projectDir));
		}
		if ( params.getCreateTM() ) {
			return Util.getDirectoryName(params.getTmPath().replace(VAR_PROJDIR, projectDir));
		}
		// Else
		return Util.getDirectoryName(params.getTmxForUnknownPath().replace(VAR_PROJDIR, projectDir));
	}

	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			processStartDocument((StartDocument)event.getResource());
			break;
		case TEXT_UNIT:
			processTextUnit(event.getTextUnit());
			break;
		case END_DOCUMENT:
			processEndDocument();
			break;
		default:
			break;
		}
		return event;
	}

    private void processStartDocument (StartDocument resource) {
		// fileName is the value we set as attribute in the TM.
    	// Get it from the document name
		fileName = Util.getFilename(resource.getName(), true);
		
		readTargetToDb();
		
		dbStore = dbStoreBuilder.getDbStore();
		targetCount = dbStore.getTextUnitCount();
		aligned = 0;
		noText = 0;
		count = 0;
		manual = 0;
		
		aligner.setDocumentName(resource.getName());
    }
	
    private void readTargetToDb () {
    	try {
			// Initialize the filter for the target
			trgFilter = mapper.createFilter(getInputFilterSettings(1), trgFilter);
			
			// Open the file with the translations
			File f = new File(getInputPath(1));
			RawDocument res = new RawDocument(f.toURI(), getInputEncoding(1), srcLang, trgLang);
			trgFilter.open(res, false);
			
			// Fill the database with the target file
			while ( trgFilter.hasNext() ) {
				dbStoreBuilder.handleEvent(trgFilter.next());
			}
    	}
    	finally {
    		if ( trgFilter != null ) trgFilter.close();
    	}
		
    }
    
    private void processEndDocument () {
    	alignedTotal += aligned;
    	noTextTotal += noText;
    	countTotal += count;
    	manualTotal += manual;
    	logger.info("Translatable text units = {}", count);
    	logger.info("Without text = {}", noText);
    	logger.info("Aligned = {} (manually modified = {})", aligned, manual);
    }

	private void processTextUnit (ITextUnit tu) {
		//TODO: Find a way to stop the filter
		if ( stopProcess ) return;
		
		// Skip non-translatable
		if ( !tu.isTranslatable() ) return;
		count++;
		// Segment the source if needed
		if ( params.getSegment() ) {
			srcSeg.computeSegments(tu.getSource());
			tu.getSource().getSegments().create(srcSeg.getRanges());
			if ( !tu.getSource().hasBeenSegmented() ) {
				if ( !tu.getSource().hasText(false) ) {
					noText++;
					return;
				}
			}
		}
		// Retrieve the corresponding target(s)
		TextContainer trgTC = dbStore.findEntry(tu.getName());
		if ( trgTC != null ) {
			// Check alignment and fix it if needed
			tu.setTarget(trgLang, trgTC);
			// If source has no segment, merge all the one of the target ("#" -> "Num." case)
			if ( !tu.getSource().hasBeenSegmented() && ( trgTC.count() > 0 )) {
				trgTC.getSegments().joinAll();
			}
			
			switch ( aligner.align(tu, count, targetCount) ) {
			case 1:
				aligned++;
				if ( aligner.wasModifiedManually() ) manual++;
				// Prepare the attributes if needed
				if ( params.getCreateAttributes() ) {
					String value;
					for ( String key : originalAttributes.keySet() ) {
						value = originalAttributes.get(key);
						if ( "${filename}".equals(value) ) {
							assignedAttributes.put(key, fileName);
						}
						else if ( "${resname}".equals(value) ) {
							assignedAttributes.put(key, tu.getName());
						}
						else {
							assignedAttributes.put(key, value);
						}
					}
				}
				// Check for 'to-review' mark
				if ( tu.hasTargetProperty(trgLang, Aligner.ALIGNSTATUS_KEY) ) { 
					assignedAttributes.put(Aligner.ALIGNSTATUS_KEY,
						tu.getTargetProperty(trgLang, Aligner.ALIGNSTATUS_KEY).getValue());
				}
				else {
					assignedAttributes.remove(Aligner.ALIGNSTATUS_KEY);
				}
				
				// Output to TMX
				if ( params.getCreateTMX() ) {
					tmxWriter.writeItem(tu, assignedAttributes);
				}
				// Output to SimpleTM
				if ( params.getCreateTM() ) {
					simpleTm.addEntry(tu, tu.getName(), fileName);
				}
				return;
			case 2:
				// Do nothing (skip entry)
				break;
			case 0:
				stopProcess = true;
				cancel();
				break;
			}
		}
		
		// Else: track the item not aligned
		if ( !stopProcess ) {
			logger.info("Not aligned: {}", tu.getName());
			if ( tmxWriterForUnknown != null ) {
				tu.removeTarget(trgLang); // Write empty target
				tmxWriterForUnknown.writeItem(tu, assignedAttributes);
			}
		}
		
	}
	
}
