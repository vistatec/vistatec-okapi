package net.sf.okapi.steps.fullwidthconversion;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FullWidthConversionStepTest {

	@Test
	public void convertToHalfWidthTest() {
		Parameters params = new Parameters();
		params.setToHalfWidth(true);
		
		String text = "Foo 123 " // ASCII (not affected)
				+ "\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13 " // Full-width alphanumerics
				+ "\uFF01\uFF1F\uFF08\uFF09 " // Full-width punctuation
				+ "\u3371 " // Squared Latin Abbreviations
				+ "\u2100 " // Letter-Like Symbols
				+ "\u30AC\u30D1\u30AA " // Katakana
				+ "\uD55C\uAD6D\uC5B4 " // Full-width Hangul (not affected because they are decomposed
										// into positional Jamo, which are not considered to have
										// half-width forms; however they will be decomposed if normalization
										// is disabled)
				+ "\u314E\u314F\u3134"; // Full-width Jamo

		assertEquals("Foo 123 Foo 123 !?() \u3371 \u2100 \u30AC\u30D1\u30AA \uD55C\uAD6D\uC5B4 \uFFBE\u314F\uFFA4",
				FullWidthConversionStep.changeWidth(text, params));

		params.setIncludeSLA(true);
		assertEquals("Foo 123 Foo 123 !?() hPa \u2100 \u30AC\u30D1\u30AA \uD55C\uAD6D\uC5B4 \uFFBE\u314F\uFFA4",
				FullWidthConversionStep.changeWidth(text, params));

		params.setIncludeLLS(true);
		assertEquals("Foo 123 Foo 123 !?() hPa a/c \u30AC\u30D1\u30AA \uD55C\uAD6D\uC5B4 \uFFBE\u314F\uFFA4",
				FullWidthConversionStep.changeWidth(text, params));

		params.setIncludeKatakana(true);
		assertEquals("Foo 123 Foo 123 !?() hPa a/c \uFF76\uFF9E\uFF8A\uFF9F\uFF75 \uD55C\uAD6D\uC5B4 \uFFBE\u314F\uFFA4",
				FullWidthConversionStep.changeWidth(text, params));
		
		params.setNormalizeOutput(false);
		assertEquals("Foo 123 Foo 123 !?() hPa a/c \uFF76\uFF9E\uFF8A\uFF9F\uFF75 \u1112\u1161\u11AB\u1100\u116E\u11A8\u110B\u1165 \uFFBE\u314F\uFFA4",
				FullWidthConversionStep.changeWidth(text, params));

		// Input is decomposed to NFD when converting to half-width, but
		// if there were no convertible characters then the output should not
		// change in composition.
		text = "A\u0308"; // Should remain decomposed
		params.setNormalizeOutput(true);
		assertEquals(text, FullWidthConversionStep.changeWidth(text, params));
		params.setNormalizeOutput(false);
		assertEquals(text, FullWidthConversionStep.changeWidth(text, params));
		text = "\uD55C\uAD6D\uC5B4"; // Should remain composed
		params.setNormalizeOutput(true);
		assertEquals(text, FullWidthConversionStep.changeWidth(text, params));
		params.setNormalizeOutput(false);
		assertEquals(text, FullWidthConversionStep.changeWidth(text, params));
	}
	
	@Test
	public void convertToFullWidthTest() {
		Parameters params = new Parameters();
		params.setToHalfWidth(false);
		
		String text = "\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13 " // Full-width alphanumerics (not affected)
				+ "Foo 123 !?() " // ASCII
				+ "\uFF76\uFF9E\uFF8A\uFF9F\uFF75 " // Half-width Katakana
				+ "\uFFBE\uFFC2\uFFA4"; // Half-width Jamo
		
		assertEquals("\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13\u3000\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13\u3000"
				+ "\uFF01\uFF1F\uFF08\uFF09\u3000\u30AC\u30D1\u30AA\u3000\u314E\uFFC2\u3134",
				FullWidthConversionStep.changeWidth(text, params));

		params.setAsciiOnly(true);
		assertEquals("\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13\u3000\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13\u3000"
				+ "\uFF01\uFF1F\uFF08\uFF09\u3000\uFF76\uFF9E\uFF8A\uFF9F\uFF75\u3000\uFFBE\uFFC2\uFFA4",
				FullWidthConversionStep.changeWidth(text, params));

		params.setKatakanaOnly(true);
		assertEquals("\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13 Foo 123 !?() \u30AC\u30D1\u30AA \uFFBE\uFFC2\uFFA4",
				FullWidthConversionStep.changeWidth(text, params));
		
		params.setKatakanaOnly(false);
		params.setNormalizeOutput(false);
		assertEquals("\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13\u3000\uFF26\uFF4F\uFF4F\u3000\uFF11\uFF12\uFF13\u3000"
				+ "\uFF01\uFF1F\uFF08\uFF09\u3000\u30AB\u3099\u30CF\u309A\u30AA\u3000\u314E\uFFC2\u3134",
				FullWidthConversionStep.changeWidth(text, params));

		params.setKatakanaOnly(true);
		text = "A\u0308"; // Should remain decomposed
		params.setNormalizeOutput(true);
		assertEquals(text, FullWidthConversionStep.changeWidth(text, params));
		params.setNormalizeOutput(false);
		assertEquals(text, FullWidthConversionStep.changeWidth(text, params));
	}
}
