package net.sf.okapi.common.pipeline;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SimplePipelineTest {
	
	@Test
	public void runPipeline() throws URISyntaxException {
		IPipeline pipeline = new Pipeline();
		pipeline.addStep(new Producer());
		pipeline.addStep(new ConsumerProducer());
		pipeline.addStep(new Consumer());

		pipeline.startBatch();
		pipeline.process(new RawDocument("DUMMY", LocaleId.fromString("en")));
		pipeline.endBatch();
		
		assertEquals(PipelineReturnValue.SUCCEDED, pipeline.getState());
		pipeline.destroy();
		assertEquals(PipelineReturnValue.DESTROYED, pipeline.getState());
	}
}
