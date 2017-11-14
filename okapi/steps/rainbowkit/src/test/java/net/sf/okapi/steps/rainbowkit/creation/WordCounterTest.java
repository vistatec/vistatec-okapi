package net.sf.okapi.steps.rainbowkit.creation;

import static org.junit.Assert.assertEquals;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.steps.rainbowkit.common.WordCounter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class WordCounterTest {
	
	private LocaleId locEN = LocaleId.fromString("en");
	private LocaleId locFR = LocaleId.fromString("FR-CA");
	
	@Test
	public void testWordCount () {
		WordCounter wc = new WordCounter(locEN);
		assertEquals(5, wc.getWordCount("This is a (simple) test"));
		assertEquals(4, wc.getWordCount("He is an Italian-American"));
		assertEquals(0, wc.getWordCount("- \u2010 \u30a0"));
		assertEquals(4, wc.getWordCount("Born in the U.S.A."));
		// TODO: URL should be one word?
		assertEquals(6, wc.getWordCount("The link is: http://okapi.opentag.com/snapshots"));
		
		wc = new WordCounter(locFR);
		assertEquals(4, wc.getWordCount("L'objectif est atteint."));
		assertEquals(4, wc.getWordCount("L\u2019objectif est atteint."));
		
		wc = new WordCounter(LocaleId.RUSSIAN);
		assertEquals(2, wc.getWordCount("\u042d\u0442\u043e \u0442\u0435\u0441\u0442"));
	}
	
	@Test
	public void testWordCountFragmentWithInlineCodes () {
		WordCounter wc = new WordCounter(locEN);
		TextFragment tf = new TextFragment("Uppercase is ");
		tf.append(TagType.OPENING, "b", "<b>");
		tf.append("B");
		tf.append(TagType.CLOSING, "b", "</b>");
		tf.append("old and there are");
		tf.append(TagType.PLACEHOLDER, Code.TYPE_LB, "<br/>");
		tf.append("two lines.");
		assertEquals(8, wc.getWordCount(tf));
	}
	
}
