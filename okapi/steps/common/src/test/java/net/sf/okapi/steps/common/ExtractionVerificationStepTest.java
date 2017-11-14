/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common;

import java.io.IOException;
import java.net.URISyntaxException;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ExtractionVerificationStepTest {
	
	private Pipeline pipeline;
	private ExtractionVerificationStep verifier;
	private final FileLocation pathBase = FileLocation.fromClass(this.getClass());

	@Before
	public void setUp() throws Exception {
		
		// create pipeline
		pipeline = new Pipeline();

		// add ExtractionVerificationStep
		verifier = new ExtractionVerificationStep();

		pipeline.addStep(verifier);
		
	}
	
	public void setUpFilter(boolean compareSkeleton, String configurationId){

		ExtractionVerificationStepParameters p = new ExtractionVerificationStepParameters();
		p.setCompareSkeleton(compareSkeleton);
		verifier.setParameters(p);

		FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();

		fcMapper.addConfigurations("net.sf.okapi.filters.html.HtmlFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.xliff.XLIFFFilter");
		fcMapper.addConfigurations("net.sf.okapi.filters.tmx.TmxFilter");
		
		verifier.setFilterConfigurationMapper(fcMapper);
		verifier.setFilterConfigurationId(configurationId);		
	}

	@After
	public void tearDown() throws Exception {
		pipeline.destroy();
	}
	
	@Test
	public void testExtractionVerificationTmx () throws URISyntaxException, IOException {

		setUpFilter(true, "okf_tmx");
		
		pipeline.startBatch();

		pipeline.process(new RawDocument(pathBase.in("html_test.tmx").asUri(), "UTF-8", LocaleId.fromBCP47("en-US"), LocaleId.fromBCP47("fr-FR")));
		pipeline.process(new RawDocument(pathBase.in("ImportTest2A.tmx").asUri(), "UTF-8", LocaleId.fromBCP47("en-US"), LocaleId.fromBCP47("fr-CA")));
		pipeline.process(new RawDocument(pathBase.in("ImportTest2B.tmx").asUri(), "UTF-8", LocaleId.fromBCP47("en-US"), LocaleId.fromBCP47("fr-CA")));
		pipeline.process(new RawDocument(pathBase.in("ImportTest2C.tmx").asUri(), "UTF-8", LocaleId.fromBCP47("en-US"), LocaleId.fromBCP47("fr-FR")));
		
		pipeline.endBatch();
	}

	
	@Test
	public void testExtractionVerificationHtml () throws URISyntaxException, IOException {

		setUpFilter(true, "okf_html");
		
		pipeline.startBatch();

		pipeline.process(new RawDocument(pathBase.in("aa324.html").asUri(), "UTF-8", LocaleId.ENGLISH));
		pipeline.process(new RawDocument(pathBase.in("form.html").asUri(), "UTF-8", LocaleId.ENGLISH));
		pipeline.process(new RawDocument(pathBase.in("W3CHTMHLTest1.html").asUri(), "UTF-8", LocaleId.ENGLISH));
		
		pipeline.endBatch();
	}
	
	@Test
	public void testExtractionVerificationXlf () throws URISyntaxException, IOException {

		setUpFilter(false, "okf_xliff");
		
		pipeline.startBatch();

		pipeline.process(new RawDocument(pathBase.in("test1_es.xlf").asUri(), "UTF-8", LocaleId.ENGLISH, LocaleId.SPANISH));
		pipeline.process(new RawDocument(pathBase.in("test2_es.xlf").asUri(), "UTF-8", LocaleId.fromBCP47("en-US"), LocaleId.fromBCP47("es-ES")));

		pipeline.process(new RawDocument(pathBase.in("RB-11-Test01.xlf").asUri(), "UTF-8", LocaleId.ENGLISH, LocaleId.SPANISH));
		pipeline.process(new RawDocument(pathBase.in("SF-12-Test01.xlf").asUri(), "UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH));
		pipeline.process(new RawDocument(pathBase.in("SF-12-Test02.xlf").asUri(), "UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH));
		pipeline.process(new RawDocument(pathBase.in("SF-12-Test03.xlf").asUri(), "UTF-8", LocaleId.ENGLISH, LocaleId.SPANISH));		
		pipeline.process(new RawDocument(pathBase.in("BinUnitTest01.xlf").asUri(), "UTF-8", LocaleId.ENGLISH, LocaleId.SPANISH));
		pipeline.process(new RawDocument(pathBase.in("JMP-11-Test01.xlf").asUri(), "UTF-8", LocaleId.ENGLISH, LocaleId.SPANISH));
		pipeline.process(new RawDocument(pathBase.in("Manual-12-AltTrans.xlf").asUri(), "UTF-8", LocaleId.ENGLISH, LocaleId.SPANISH));
		
		/*
		FYI: getResourceAsStream does not allow reopening a filter
		pipeline.process(new RawDocument(this.getClass().getResourceAsStream("aa324.html"), "UTF-8", LocaleId.ENGLISH));
		pipeline.process(new RawDocument(this.getClass().getResourceAsStream("form.html"), "UTF-8", LocaleId.ENGLISH));
		pipeline.process(new RawDocument(this.getClass().getResourceAsStream("W3CHTMHLTest1.html"), "UTF-8", LocaleId.ENGLISH));*/
		
		pipeline.endBatch();
	}
}
