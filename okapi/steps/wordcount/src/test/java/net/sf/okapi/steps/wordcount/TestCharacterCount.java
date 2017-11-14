package net.sf.okapi.steps.wordcount;

import static org.junit.Assert.assertEquals;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.steps.wordcount.CharacterCounter.Counts;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestCharacterCount {

	private LocaleId locEN = LocaleId.ENGLISH;
	private LocaleId locFR = LocaleId.FRENCH;
	private LocaleId locES005 = LocaleId.fromString("es-005");
	
	@Test
	public void testStatics () {
		assertEquals(22, CharacterCounter.count("Test word count is correct.", locEN));
		assertEquals(37, CharacterCounter.count("The quick (\"brown\") fox can't jump 32.3 feet, right?", locEN));
		assertEquals(37, CharacterCounter.count("The quick (\u201Cbrown\u201D) fox can\u2019t jump 32.3 feet, right?", locEN));

		assertEquals(16, CharacterCounter.count("Words in a sentence", locEN));
		assertEquals(16, CharacterCounter.count("Words in a sentence", locES005));
		
		assertEquals(Counts.of(22, 4, 1), CharacterCounter.fullCount("Test word count is correct.", locEN));
		assertEquals(Counts.of(37, 8, 7), CharacterCounter.fullCount("The quick (\"brown\") fox can't jump 32.3 feet, right?", locEN));
		assertEquals(Counts.of(37, 8, 7), CharacterCounter.fullCount("The quick (\u201Cbrown\u201D) fox can\u2019t jump 32.3 feet, right?", locEN));

		assertEquals(Counts.of(16, 3, 0), CharacterCounter.fullCount("Words in a sentence", locEN));
		assertEquals(Counts.of(16, 3, 0), CharacterCounter.fullCount("Words in a sentence", locES005));
	}	

	@Test
	public void testCountApostrophe () {
		// Should be 19 per www.ttt.org/oscarstandards/gmx-v/gmx-v.html#PunctuationCharacters
		assertEquals(19, CharacterCounter.count("L'objectif est defini.", locFR));
		assertEquals(19, CharacterCounter.count("L\u2019objectif est defini.", locFR));
		
		assertEquals(48, CharacterCounter.count("Elle a \u00E9t\u00E9 la " +
				"premi\u00E8re Fran\u00E7aise d'une famille d'\u00E9migr\u00E9s.", locFR));
		assertEquals(48, CharacterCounter.count("Elle a \u00E9t\u00E9 la " +
				"premi\u00E8re Fran\u00E7aise d\u2019une famille d\u2019\u00E9migr\u00E9s.", locFR));

		assertEquals(18, CharacterCounter.count("He can't eat that fast.", locEN));
		assertEquals(18, CharacterCounter.count("He can\u2019t eat that fast.", locEN));
		
		assertEquals(Counts.of(19, 2, 1), CharacterCounter.fullCount("L'objectif est defini.", locFR));
		assertEquals(Counts.of(19, 2, 1), CharacterCounter.fullCount("L\u2019objectif est defini.", locFR));
		
		assertEquals(Counts.of(48, 8, 1), CharacterCounter.fullCount("Elle a \u00E9t\u00E9 la " +
				"premi\u00E8re Fran\u00E7aise d'une famille d'\u00E9migr\u00E9s.", locFR));
		assertEquals(Counts.of(48, 8, 1), CharacterCounter.fullCount("Elle a \u00E9t\u00E9 la " +
				"premi\u00E8re Fran\u00E7aise d\u2019une famille d\u2019\u00E9migr\u00E9s.", locFR));

		assertEquals(Counts.of(18, 4, 1), CharacterCounter.fullCount("He can't eat that fast.", locEN));
		assertEquals(Counts.of(18, 4, 1), CharacterCounter.fullCount("He can\u2019t eat that fast.", locEN));
	}
	
	@Test
	public void testCountDecomposed () {
		assertEquals(Counts.of(48, 8, 1), CharacterCounter.fullCount("Elle a \u00E9t\u00E9 la " +
				"premi\u00E8re Fran\u00E7aise d'une famille d'\u00E9migr\u00E9s.", locFR));
		assertEquals(Counts.of(48, 8, 1), CharacterCounter.fullCount("Elle a e\u0301te\u0301 la " +
				"premie\u0300re Franc\u0327aise d'une famille d'e\u0301migre\u0301s.", locFR));
	}
	
	@Test
	public void testCountHyphen () {
		assertEquals(29, CharacterCounter.count("  Al Capone was an Italian-American.  ", locEN));
		assertEquals(Counts.of(29, 8, 1), CharacterCounter.fullCount("  Al Capone was an Italian-American.  ", locEN));
	}
	
	@Test
	public void testAstral() {
		// U+D83C U+DF81 -> U+1F381 WRAPPED PRESENT
		assertEquals(21, CharacterCounter.count("Here is a present for you: \uD83C\uDF81", locEN));
		assertEquals(Counts.of(21, 6, 1), CharacterCounter.fullCount("Here is a present for you: \uD83C\uDF81", locEN));
		assertEquals(21, CharacterCounter.count("\uD83C\uDF81 <- Here is a present for you", locEN));
		assertEquals(Counts.of(21, 7, 2),
				CharacterCounter.fullCount("\uD83C\uDF81 <- Here is a present for you", locEN));
	}

	@Test
	public void testCountGMXExamples () {
		assertEquals(33, CharacterCounter.count("This sentence has a word count of 9 words.", locEN));
		assertEquals(42, CharacterCounter.count("This sentence/text unit has a word count of 11 words.", locEN));
		// The standard claims the following should be 91 characters, but that can't be right unless
		// it's including the period, which the standard says should not be included.
		assertEquals(90, CharacterCounter.count("In this example the in-line codes do not form\n"
				+ "part of the word or character counts but are counted separately.", locEN));
		// The following actually from GMX-V 2.0
		assertEquals(12, CharacterCounter.count("Start Text end.", locEN));
		assertEquals(15, CharacterCounter.count("The black cat eats.", locEN));
		
		assertEquals(Counts.of(33, 8, 1), CharacterCounter.fullCount("This sentence has a word count of 9 words.", locEN));
		assertEquals(Counts.of(42, 9, 2), CharacterCounter.fullCount("This sentence/text unit has a word count of 11 words.", locEN));
		// The standard claims the following should be 91 characters, but that can't be right unless
		// it's including the period, which the standard says should not be included.
		assertEquals(Counts.of(90, 19, 1), CharacterCounter.fullCount("In this example the in-line codes do not form\n"
				+ "part of the word or character counts but are counted separately.", locEN));
		// The following actually from GMX-V 2.0
		assertEquals(Counts.of(12, 2, 1), CharacterCounter.fullCount("Start Text end.", locEN));
		assertEquals(Counts.of(15, 3, 1), CharacterCounter.fullCount("The black cat eats.", locEN));
	}
	
	@Test
	public void testCountTokens () {
		assertEquals(11, CharacterCounter.count("123 123.4 123,5", locEN));
		assertEquals(0, CharacterCounter.count("( ) \" \' { } [ ] / % $ @ # ? ! * _ -", locEN));
		
		assertEquals(Counts.of(11, 2, 2), CharacterCounter.fullCount("123 123.4 123,5", locEN));
		assertEquals(Counts.of(0, 17, 18), CharacterCounter.fullCount("( ) \" \' { } [ ] / % $ @ # ? ! * _ -", locEN));
	}
	
	@Test
	public void testCountEmpty () {
		assertEquals(0, CharacterCounter.count("", locEN));
		assertEquals(0, CharacterCounter.count(" \t\n\f\r ", locEN));
		
		assertEquals(Counts.of(0, 0, 0), CharacterCounter.fullCount("", locEN));
		assertEquals(Counts.of(0, 6, 0), CharacterCounter.fullCount(" \t\n\f\r ", locEN));

		TextFragment tf = new TextFragment();
		tf.append(TagType.PLACEHOLDER, "b", "[b]");
		assertEquals(0, CharacterCounter.count(tf, locEN));
		assertEquals(Counts.of(0, 0, 0), CharacterCounter.fullCount(tf, locEN));
	}
	
	@Test
	public void testCountFragments () {
		TextFragment tf = new TextFragment("abc");
		tf.append(TagType.PLACEHOLDER, "b", "[b]");
		tf.append("def");
		assertEquals(6, CharacterCounter.count(tf, locEN));
		assertEquals(Counts.of(6, 0, 0), CharacterCounter.fullCount(tf, locEN));
	}
}
