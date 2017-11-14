/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.observer.IObservable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MultiEventTest {

	@Test
	public void pipelineObserverWithMultiEvent() {		
		IPipeline p = new Pipeline();
		
		// add our event observer
		EventObserver o = new EventObserver();
		((IObservable)p).addObserver(o);
		
		p.addStep(new DummyMultiCustomEventStep());
		
		p.startBatch();		
		p.process(new Event(EventType.CUSTOM));				
		p.endBatch();
		
		// test we observed the correct events
		List<Event> el = o.getResult();  
		assertEquals(EventType.START_BATCH, el.remove(0).getEventType());
		assertEquals(EventType.START_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.CUSTOM, el.remove(0).getEventType());
		assertEquals(EventType.CUSTOM, el.remove(0).getEventType());		
		assertEquals(EventType.END_BATCH_ITEM, el.remove(0).getEventType());
		assertEquals(EventType.END_BATCH, el.remove(0).getEventType());
	}
}
