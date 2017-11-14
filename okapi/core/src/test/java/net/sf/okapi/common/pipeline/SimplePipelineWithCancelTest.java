/*===========================================================================
  Copyright (C) 2008 Jim Hargrave
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

package net.sf.okapi.common.pipeline;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SimplePipelineWithCancelTest {
	
	@Test
	public void runPipelineAndCancel() throws URISyntaxException, InterruptedException {
		final IPipeline pipeline = new Pipeline();
		
		Runnable runnable = new Runnable() {
			public void run() {
				pipeline.addStep(new Producer());
				pipeline.addStep(new ConsumerProducer());
				pipeline.addStep(new Consumer());				
				
				pipeline.process(new RawDocument("DUMMY", LocaleId.fromString("en")));
			}
		};

		ExecutorService e = Executors.newSingleThreadExecutor();
		e.execute(runnable);
		Thread.sleep(500);
		pipeline.cancel();
		assertEquals(PipelineReturnValue.CANCELLED, pipeline.getState());
		pipeline.destroy();
		e.shutdownNow();
		assertEquals(PipelineReturnValue.DESTROYED, pipeline.getState());
	}
}
