package net.sf.okapi.steps.xliffsplitter;

import java.io.File;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XliffSplitterTest {
	private Pipeline pipeline;
	private String xlfRoot;
	private String splitPath;
	private String[] xlfFileList;
	private XliffSplitterStep splitter;
	
	@Before
	public void setUp() throws Exception {
		xlfRoot = Util.getDirectoryName(this.getClass().getResource("tasks_Test_SDL_XLIFF_18961_es_ES_xliff.xlf").toURI().getPath()) 
					+ File.separator;
		xlfFileList = Util.getFilteredFiles(xlfRoot, ".xlf");
		splitPath = xlfRoot + "/split";

		Util.createDirectories(splitPath+"/");
		
		// create pipeline
		pipeline = new Pipeline();
		
		// add filter step
		splitter = new XliffSplitterStep();		
		pipeline.addStep(splitter);				
	}
	
	@After
	public void tearDown() throws Exception {
		pipeline.destroy();
	}
	
	@Test
	public void splitXliffWithOneFile() {
			pipeline.startBatch();		
			String file = "tasks_Test_SDL_XLIFF_18961_es_ES_xliff_singleFile.xlf";
			splitter.setOutputURI(Util.toURI(splitPath + "/" + file));
			pipeline.process(new RawDocument(Util.toURI(xlfRoot + file), "UTF-8", LocaleId.ENGLISH));
			pipeline.endBatch();
	}
	
	@Test
	public void splitXliffWithMultipleFiles() {
			pipeline.startBatch();		
			String file = "tasks_Test_SDL_XLIFF_18961_es_ES_xliff.xlf";
			splitter.setOutputURI(Util.toURI(splitPath + "/" + file));
			pipeline.process(new RawDocument(Util.toURI(xlfRoot + file), "UTF-8", LocaleId.ENGLISH));
			pipeline.endBatch();
	}
	
	@Test
	public void splitXliffWithMultipleInputFiles() {
			pipeline.startBatch();		
			for (String file : xlfFileList) {
				splitter.setOutputURI(Util.toURI(splitPath + "/" + file));
				pipeline.process(new RawDocument(Util.toURI(xlfRoot + file), "UTF-8", LocaleId.ENGLISH));
			}			
			pipeline.endBatch();
	}
	
	@Test
	public void splitBigXliffWithOneFile() {
		XliffSplitterParameters params = new XliffSplitterParameters();
		params.setBigFile(true);
		splitter.setParameters(params);
		pipeline.startBatch();		
		String file = "tasks_Test_SDL_XLIFF_18961_es_ES_xliff_singleFile.xlf";
		splitter.setOutputURI(Util.toURI(splitPath + "/" + file));
		pipeline.process(new RawDocument(Util.toURI(xlfRoot + file), "UTF-8", LocaleId.ENGLISH));
		pipeline.endBatch();
	}
	
	@Test
	public void splitBigXliffWithMultipleFiles() {
		XliffSplitterParameters params = new XliffSplitterParameters();
		params.setBigFile(true);
		splitter.setParameters(params);
		pipeline.startBatch();		
		String file = "tasks_Test_SDL_XLIFF_18961_es_ES_xliff.xlf";
		splitter.setOutputURI(Util.toURI(splitPath + "/" + file));
		pipeline.process(new RawDocument(Util.toURI(xlfRoot + file), "UTF-8", LocaleId.ENGLISH));
		pipeline.endBatch();
	}
	
	@Test
	public void splitBigXliffWithMultipleInputFiles() {
		XliffSplitterParameters params = new XliffSplitterParameters();
		params.setBigFile(true);
		splitter.setParameters(params);
		pipeline.startBatch();		
		
		for (String file : xlfFileList) {
			splitter.setOutputURI(Util.toURI(splitPath + "/" + file));
			pipeline.process(new RawDocument(Util.toURI(xlfRoot + file), "UTF-8", LocaleId.ENGLISH));
		}			
		pipeline.endBatch();
	}
	
}
