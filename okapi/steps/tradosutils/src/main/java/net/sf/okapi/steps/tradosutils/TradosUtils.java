/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tradosutils;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LCIDUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;

class TradosUtils {

	/**
	 * Verifies that the java.library.path system variable includes the location
	 * from where the step is run. this allows the jacob DLL to be found.
	 * @param file the class file
	 */
	static public void verifyJavaLibPath (File file) {
    	try {
        	// Get the location of the main class
    		String pluginRoot = ";" + URLDecoder.decode(file.getAbsolutePath(),"utf-8"); //$NON-NLS-1$

    		if ( pluginRoot.endsWith(".jar") ) {
        		// If it's a jar we have to remove that last part and we get the true directory
        		pluginRoot = Util.getDirectoryName(pluginRoot);
        		// Get the library path
        		String  javaLibPath = System.getProperty("java.library.path");
        		if ( !javaLibPath.contains(pluginRoot) ) {
        			// Add the location of the jar to the path
        			javaLibPath += pluginRoot;
        			System.setProperty("java.library.path", javaLibPath);
        			// Hack to force the VM to reset the internal cache
        			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        			fieldSysPath.setAccessible(true);
        			fieldSysPath.set(null, null);
        		}
        	}
    		// If it's not a jar, then we are in debug mode under eclipse,
    		// and no java.lib.path fix is needed
		}
    	catch ( Throwable e ) {
    		throw new OkapiException("Error checking/setting the java library path\n"+e.getMessage(), e);
		}
	}
	
	/**
	 * Verifies if a given TM exists.
	 * @param tm the path of the TM file.
	 * @return true if the TM exists, false otherwise.
	 */
	static public boolean tmExists (String tm) {
    	File f = new File(tm);
		return f.exists();
	}
	
    /**
     * Deletes all the files associated with a given Trados .tmw TM.
     * @param tm the full path of the Trados TM.
     */
    static public void deleteTM (String tm) {
    	File f = new File(tm);
		if ( f.exists() ) f.delete();
		
    	f = new File(tm.replace(".tmw", ".mwf"));
		if ( f.exists() ) f.delete();
		
    	f = new File(tm.replace(".tmw", ".mtf"));
		if ( f.exists() ) f.delete();
		
    	f = new File(tm.replace(".tmw", ".mdf"));
		if ( f.exists() ) f.delete();
		
    	f = new File(tm.replace(".tmw", ".iix"));
		if ( f.exists() ) f.delete();
    }
    
    /**
     * Deletes the log files associated with a Trados batch process.
     * @param log the full path of the log file.
     */
    static public void deleteLog (String log) {
		File f = new File(log);
		if ( f.exists() ) f.delete();
		// Delete the CSV if needed
		// It seems the csv actually does not get appended, but clear it anyway
		String csvPath = log;
		String ext = Util.getExtension(log);
		if ( !ext.isEmpty() ) {
			csvPath = log.replace(ext, ".csv");
			f = new File(csvPath);
			if ( f.exists() ) f.delete();
		}
    }
    
    /**
     * Deletes the log files associated with a Trados batch process.
     * @param log the full path of the log file.
     */
    static public void deleteLogIfRequested (Boolean requested, String log) {
		if ( requested ) {
			deleteLog (log);
		}
    }

    /**
     * Creates a TM from scratch.
     * @param com ActiveXComponent variable.
     * @param tm path of the TM to create.
     * @param sourceLocale source locale for the new TM.
     * @param targetLocale target locale for the new TM.
     * @param logger logger to report warnings.
     */
    static public void createTM (ActiveXComponent com,
    	String tm,
    	LocaleId sourceLocale,
    	LocaleId targetLocale,
    	Logger logger)
    {
    	//--switch to make no-no to nb-no for TM creation--
    	if(sourceLocale.equals(new LocaleId("no", "no"))){
    		sourceLocale = new LocaleId("nb", "no");
    	}
    	if(targetLocale.equals(new LocaleId("no", "no"))){
    		targetLocale = new LocaleId("nb", "no");
    	}
    	
    	String srcLcid = LCIDUtil.getLCID_asString(sourceLocale);
    	String trgLcid = LCIDUtil.getLCID_asString(targetLocale);

    	//--default src to English if not found--
    	if ( srcLcid.equals("0") ) {
    		logger.warn("Could not map Okapi source locale to Trados locale. Using default EN-US.");
    		srcLcid = "1033";
    	}

    	//--default trg to German if not found--
    	if ( trgLcid.equals("0") ) {
    		logger.warn("Could not map Okapi target locale to Trados locale. Using default DE-DE.");
    		trgLcid = "1031";
    	}
    	
    	//--switch to make es-es match trados es-es--
    	//--es-es creates Spanish International in Trados wich has es-em
    	//--es-ES_tradnl creates Spanish Traditional in Trados which has es-es
    	if(srcLcid.equals("3082")){
    		srcLcid = "1034";
    	}
    	if(trgLcid.equals("3082")){
    		trgLcid = "1034";
    	}
    	
    	Variant[] variants = new Variant[5];
    	variants[0] = new Variant(tm);
    	variants[1] = new Variant("");
    	variants[2] = new Variant(srcLcid);
    	variants[3] = new Variant("");
    	variants[4] = new Variant(trgLcid);

    	com.invoke("CreateTMEx", variants);
    }
    
    /**
     * Generates a path for a temporary Trados TM.
     * @return the path of the temporary TM. 
     */
    static public String generateTempTmName () {
    	return System.getProperty("java.io.tmpdir") + UUID.randomUUID().toString()+".tmw"; 
    }
    
    /**
     * Generates the alternative output used by several Trados Steps
     * @param outFilePath path of the new input file. 
     * @param defaultEncoding default encoding of the new file.
     * @param sourceLocale source locale.
     * @param targetLocale target locale.
     * @param filterConfigId filter configuration id of the new file.
     * @return the event created.
     */
    static public Event generateAltOutput (String outFilePath,
    	String defaultEncoding,
    	LocaleId sourceLocale,
    	LocaleId targetLocale,
    	String filterConfigId )
    {
		List<Event> list = new ArrayList<Event>();
		// Change the pipeline parameters for the raw-document-related data
		PipelineParameters pp = new PipelineParameters();
		RawDocument rawDoc = new RawDocument(new File(outFilePath).toURI(), defaultEncoding, sourceLocale, targetLocale, filterConfigId);
		pp.setOutputURI(rawDoc.getInputURI()); // Use same name as this output for now
		pp.setSourceLocale(rawDoc.getSourceLocale());
		pp.setTargetLocale(rawDoc.getTargetLocale());
		pp.setOutputEncoding(rawDoc.getEncoding()); // Use same as the output document
		pp.setInputRawDocument(rawDoc);
		pp.setFilterConfigurationId(rawDoc.getFilterConfigId());
		pp.setBatchInputCount(1);
		// Add the event to the list
		list.add(new Event(EventType.PIPELINE_PARAMETERS, pp));
		// Add raw-document related events
		list.add(new Event(EventType.START_BATCH_ITEM));
		list.add(new Event(EventType.RAW_DOCUMENT, rawDoc));
		list.add(new Event(EventType.END_BATCH_ITEM));
		// Return the list as a multiple-event event
		return new Event(EventType.MULTI_EVENT, new MultiEvent(list));
    }
    
}
