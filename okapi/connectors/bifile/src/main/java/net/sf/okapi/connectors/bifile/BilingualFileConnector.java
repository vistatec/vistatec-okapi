/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.bifile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.connectors.pensieve.PensieveTMConnector;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.tmimport.TMImportStep;

/**
 * This connector extends {@link PensieveTMConnector} to allow one-step leveraging 
 * from a bilingual file. Upon initialization it imports the specified input file 
 * into a temporary Pensieve TM, and points its superclass to it. All actual queries
 * are handled by the superclass.
 */
public class BilingualFileConnector extends PensieveTMConnector {
	
	private Parameters params;
	private Path tmpTm;
	private LocaleId sourceLocale;
	private LocaleId targetLocale;
	private boolean inited;
	
	public BilingualFileConnector () {
		params = new Parameters();
		inited = false;
	}

	@Override
	public String getName () {
		return "Bilingual File";
	}

	@Override
	public String getSettingsDisplay () {
		return "File: " + (Util.isEmpty(params.getBiFile())
				? "<To be specified>"
				: params.getBiFile());
	}
	
	@Override
	public int query (String plainText) {
		if (!inited) {
			init();
		}
		return super.query(plainText);
	}
	
	@Override
	public int query (TextFragment text) {
		if (!inited) {
			init();
		}
		return super.query(text);
	}
	
	/**
	 * Init must be deferred until the sourceLocale and targetLocale are
	 * known. When used with LeveragingStep, this is not until START_DOCUMENT.
	 */
	private void init () {
		makeTempTM();
		
		net.sf.okapi.connectors.pensieve.Parameters p =
				new net.sf.okapi.connectors.pensieve.Parameters();
		p.setDbDirectory(tmpTm.toString());
		
		super.setParameters(p);
		
		super.open();
		
		inited = true;
	}
	
	private void makeTempTM () {
		// Make pipeline.
		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
		// This outputs a bunch of warnings if the default filters aren't on the classpath.
        DefaultFilters.setMappings(fcMapper, false, true);
        
        // TODO: Allow custom filter configs. This would allow bilingual
        // extraction from arbitrary files via the okf_regex filter.
        //fcMapper.setCustomConfigurationsDirectory(filterConfigPath);
        
		PipelineDriver driver = new PipelineDriver();
		driver.setFilterConfigurationMapper(fcMapper);
		
		// Step 1: Raw Docs to Filter Events
		driver.addStep(new RawDocumentToFilterEventsStep());
		
		// Step 2: Generate Pensieve TM
		TMImportStep tmImport = new TMImportStep();
		driver.addStep(tmImport);
		
		try {
			tmpTm = Files.createTempDirectory("okapi-connector-bitext").toAbsolutePath();
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Could not create temp file for Pensieve TM.", e);
		}
		net.sf.okapi.steps.tmimport.Parameters p =
				(net.sf.okapi.steps.tmimport.Parameters) tmImport.getParameters();
		p.setTmDirectory(tmpTm.toString());
		
		File file = new File(params.getBiFile());
		if ( !file.exists() ) {
			throw new OkapiBadStepInputException("Bilingual file for leveraging not found.");
		}
		
		FilterConfiguration fc = fcMapper.getDefaultConfigurationFromExtension(
			Util.getExtension(file.getName()));
		
		if ( fc == null ) {
			throw new OkapiBadStepInputException("Could not auto-detect filter configuration "
				+ "for bilingual input file.");
		}
		
		try (RawDocument rawDoc = new RawDocument(file.toURI(), params.getInputEncoding(),
			sourceLocale, targetLocale, fc.configId);) {
			driver.addBatchItem(rawDoc, null, null);
			
			driver.processBatch();
			driver.clearItems();
		}
	}
	
	@Override
	public void open () {
		// Defer opening of PensieveTMConnector until query() is called
		// because by then we'll have received the sourceLocale and targetLocale.
	}

	@Override
	public void close () {
		super.close();
		
		if (!inited || tmpTm == null) {
			return;
		}
		
		File dir = tmpTm.toFile();
		if ( !dir.exists() ) {
			return;
		}
		
		// Delete temp TM.
		try {
			for (File f : dir.listFiles()) {
				f.delete();
			}
			dir.delete();
		}
		catch (Exception e) {
			throw new OkapiIOException("Could not clean up temporary TM.", e);
		}
	}
	
	@Override
	public void setLanguages (LocaleId sourceLocale,
		LocaleId targetLocale)
	{
		super.setLanguages(sourceLocale, targetLocale);
		
		this.sourceLocale = sourceLocale;
		this.targetLocale = targetLocale;
	}
	
	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}
}
