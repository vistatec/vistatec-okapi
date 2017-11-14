/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.lib.segmentation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextContainer;

@RunWith(JUnit4.class)
public class TestIcu4jRules {

	private SRXSegmenter segmenter;
	private SRXDocument doc;
	private ArrayList<Rule> rules;
	
	@Before
	public void startUp() {
		doc = new SRXDocument();
		doc.setUseICU4JBreakRules(true);
		segmenter = new SRXSegmenter(); 
		rules = new ArrayList<Rule>();
	}

	@Test
	public void testMetachars() {						
		testBreak("Sentence 1. Sentence 2.", "\\.", "\\s|<br/?>", "Sentence 1.", " Sentence 2.");
		testBreak("Sentence 1. Sentence 2.", null, null, "Sentence 1.", " Sentence 2.");
		testBreak("Sentence 1.<br>Sentence 2.", "\\.", "\\s|<br/?>", "Sentence 1.", "<br>Sentence 2.");
		testBreak("Sentence 1.<br/>Sentence 2.", "\\.", "\\s|<br/?>", "Sentence 1.", "<br/>Sentence 2.");		
	}
	
	@Test
	public void testMetachars2() {
		testBreak("Mr. Holmes is from the U.K. not the U.S. Is Dr. Watson from there too? Yes: both are.", 
				null, 
				null, 
				"Mr.", " Holmes is from the U.K. not the U.S.", 5);
		
		testBreak("The First Darlek Empire has written: \"The simplest statement we know " +
			"of is the statement of Davross himself, namely, that the members of the " +
			"empire should destroy 'all life forms,' which is understood to mean universal " +
			"destruction. No one is justified in making any other statement than this\" " +
			"(First Darlek Empire letter, Mar. 12, 3035; see also DE 11:4).",
			
			null,
			null,		
			
			"The First Darlek Empire has written: \"The simplest statement we know " +
			"of is the statement of Davross himself, namely, that the members of the " +
			"empire should destroy 'all life forms,' which is understood to mean universal " +
			"destruction.",
			
			" No one is justified in making any other statement than this\" (First Darlek Empire letter, Mar. 12, 3035; see also DE 11:4).");
	}
	
	private void testBreak(String text, String bbr, String abr, String beforeBreak,
			String afterBreak) {		
		rules.clear();
		if (bbr != null && abr != null) {
			rules.add(new Rule(bbr, abr, true));
		}
		doc.addLanguageRule("default", rules);
		doc.addLanguageMap(new LanguageMap(".*", "default"));
		segmenter.setLanguage(null); // Force rules recompile 
		doc.compileLanguageRules(LocaleId.ENGLISH, segmenter);
		assertEquals(2, segmenter.computeSegments(text));
		TextContainer tc = new TextContainer(text);
		tc.getSegments().create(segmenter.getRanges());
		assertEquals(beforeBreak, tc.getSegments().get(0).toString());
		assertEquals(afterBreak, tc.getSegments().get(1).toString());
	}
	
	private void testBreak(String text, String bbr, String abr, String beforeBreak,
			String afterBreak, int numSeg) {		
		rules.clear();
		if (bbr != null && abr != null) {
			rules.add(new Rule(bbr, abr, true));
		}
		doc.addLanguageRule("default", rules);
		doc.addLanguageMap(new LanguageMap(".*", "default"));
		segmenter.setLanguage(null); // Force rules recompile 
		doc.compileLanguageRules(LocaleId.ENGLISH, segmenter);
		assertEquals(numSeg, segmenter.computeSegments(text));
		TextContainer tc = new TextContainer(text);
		tc.getSegments().create(segmenter.getRanges());
		assertEquals(beforeBreak, tc.getSegments().get(0).toString());
		assertEquals(afterBreak, tc.getSegments().get(1).toString());
	}
}
