package net.sf.okapi.common.pipeline;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FilebasedPipelineTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void runPipeline() throws URISyntaxException {
		IPipeline pipeline = new Pipeline();
		pipeline.addStep(new ConsumerProducer());
		pipeline.addStep(new Consumer());

		assertEquals(PipelineReturnValue.PAUSED, pipeline.getState());
		pipeline.process(new RawDocument("<b>Test this resource</b>",
			LocaleId.fromString("en")));	
		pipeline.destroy();
		assertEquals(PipelineReturnValue.DESTROYED, pipeline.getState());
	}
}
