package net.sf.okapi.steps.wordcount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestWordCount {

	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;
	private LocaleId locES005 = LocaleId.fromString("es-005");
	
	@Test
	public void testStatics () {
		assertEquals(5, WordCounter.count("Test word count is correct.", locEN));
		assertEquals(9, WordCounter.count("The quick (\"brown\") fox can't jump 32.3 feet, right?", locEN));
		assertEquals(9, WordCounter.count("The quick (\u201Cbrown\u201D) fox can\u2019t jump 32.3 feet, right?", locEN));

		assertEquals(4, WordCounter.count("Words in a sentence", locEN));
		assertEquals(4, WordCounter.count("Words in a sentence", locES005));
	}	

	@Test
	public void testCountApostrophe () {
		//Should be 4 per http://www.lisa.org/fileadmin/standards/GMX-V.html#Words "L'objectif" is 2 words in FR
		assertEquals(4, WordCounter.count("L'objectif est defini.", locFR));
		assertEquals(4, WordCounter.count("L\u2019objectif est defini.", locFR));
		
		assertEquals(11, WordCounter.count("Elle a \u00E9t\u00E9 la " +
				"premi\u00E8re Fran\u00E7aise d'une famille d'\u00E9migr\u00E9s.", locFR));
		assertEquals(11, WordCounter.count("Elle a \u00E9t\u00E9 la " +
				"premi\u00E8re Fran\u00E7aise d\u2019une famille d\u2019\u00E9migr\u00E9s.", locFR));

		assertEquals(5, WordCounter.count("He can't eat that fast.", locEN));
		assertEquals(5, WordCounter.count("He can\u2019t eat that fast.", locEN));
	}
	
	@Test
	public void testCountHyphen () {
		assertEquals(5, WordCounter.count("  Al Capone was an Italian-American.  ", locEN));
	}
	
	@Test
	public void testCountGMXExamples () {
		assertEquals(9, WordCounter.count("This sentence has a word count of 9 words.", locEN));
		assertEquals(11, WordCounter.count("This sentence/text unit has a word count of 11 words.", locEN));
	}
	
	@Test
	public void testCountTokens () {
		//TODO: GMX "words" are really "tokens" this is a problem 
		assertEquals(3, WordCounter.count("123 123.4 123,5", locEN));
		//TODO: Not quite "tokens"
		assertEquals(0, WordCounter.count("( ) \" \' { } [ ] / % $ @ # ? ! * _ -", locEN));
	}
	
	@Test
	public void testCountEmpty () {
		assertEquals(0, WordCounter.count("", locEN));
		assertEquals(0, WordCounter.count(" \t\n\f\r ", locEN));

		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "b", "[b]");
		assertEquals(0, WordCounter.count(tf, locEN));
	}
	
	@Test
	public void testCountFragments () {
		TextFragment tf = new TextFragment("abc");
		tf.append(TagType.PLACEHOLDER, "b", "[b]");
		tf.append("def");
		assertEquals(1, WordCounter.count(tf, locEN));
	}
	
	@Test
	public void testCharCountFactorLanguages() {
		assertEquals(1, WordCounter.count("\u65E5\u672C\u8A9E", LocaleId.JAPANESE));
		assertEquals(1, WordCounter.count("\uD55C\uAD6D\uC5B4", LocaleId.KOREAN));
		assertEquals(1, WordCounter.count("\u4F60\u597D\u5417", LocaleId.CHINA_CHINESE));
		assertEquals(1, WordCounter.count("\u0E20\u0E32\u0E29\u0E32\u0E44\u0E17\u0E22",
				LocaleId.fromString("th")));
		// Laotian, Khmer, and Burmese do not have character count factors defined,
		// so word counts cannot be determined (thus they are 0).
		assertEquals(0, WordCounter.count("\u0E9E\u0EB2\u0EAA\u0EB2\u0EA5\u0EB2\u0EA7",
				LocaleId.fromString("lo")));
		assertEquals(0, WordCounter.count("\u1797\u17B6\u179F\u17B6\u1781\u17D2\u1798\u17C2\u179A",
				LocaleId.fromString("km")));
		assertEquals(0, WordCounter.count("\u1019\u103C\u1014\u103A\u1019\u102C\u1018\u102C\u101E\u102C",
				LocaleId.fromString("my")));
		try {
			WordCounter.countLogographicScript("Hello", LocaleId.ENGLISH);
			fail();
		} catch (IllegalArgumentException ex) {
			// Can't do WordCounter.countLogographicScript on a non-logographic script.
		}
	}
}
