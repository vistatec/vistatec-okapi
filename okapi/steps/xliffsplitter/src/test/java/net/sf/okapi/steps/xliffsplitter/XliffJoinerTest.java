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
public class XliffJoinerTest {
	private Pipeline pipeline;
	private String xlfRoot;
	private String joinPath;
	private String[] xlfFileList;
	private XliffJoinerStep joiner;
	
	@Before
	public void setUp() throws Exception {
		
		xlfRoot = Util.getDirectoryName(this.getClass().getResource("to_join/tasks_Test_SDL_XLIFF_18961_es_ES_xliff_singleFile_PART0001.xlf").toURI().getPath())
					+ File.separator;
		xlfFileList = Util.getFilteredFiles(xlfRoot, ".xlf");
		joinPath = xlfRoot + "out" + File.separator;

		Util.createDirectories(joinPath);
		
		// create pipeline
		pipeline = new Pipeline();
		
		// add filter step
		joiner = new XliffJoinerStep();

		pipeline.addStep(joiner);				
	}
	
	@After
	public void tearDown() throws Exception {
		pipeline.destroy();
	}
	
	@Test
	public void joinXliffWithMultipleInputFiles() {
		pipeline.startBatch();		
		for (String file : xlfFileList) {
			joiner.setOutputURI(Util.toURI(joinPath +  file));
			pipeline.process(new RawDocument(Util.toURI(xlfRoot + file), "UTF-8", LocaleId.ENGLISH));
		}			
		pipeline.endBatch();
	}
}
