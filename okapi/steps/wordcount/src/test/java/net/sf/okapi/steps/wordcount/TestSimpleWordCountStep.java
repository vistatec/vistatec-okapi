/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.wordcount;

import static org.junit.Assert.assertEquals;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestSimpleWordCountStep {

	@Test
	public void testTextUnitCounts() {
		ITextUnit tu = new TextUnit("tu");
		TextContainer tc = tu.getSource();
		ISegments segments = tc.getSegments();
		segments.append(new TextFragment("The number of words in this segment is 9."));
		segments.append(new TextFragment("The number of words in this second segment is 10."));
		segments.append(new TextFragment("And the number of words in this third segment is 11."));
		
		SimpleWordCountStep step = new SimpleWordCountStep();	
		step.setSourceLocale(LocaleId.ENGLISH);
		StartDocument sd = new StartDocument("sd");
		sd.setLocale(LocaleId.ENGLISH);
		step.handleEvent(new Event(EventType.START_DOCUMENT, sd));
		step.handleEvent(new Event(EventType.TEXT_UNIT, tu));
		
		assertEquals(30, WordCounter.getCount(tu.getSource()));		
	}
}
