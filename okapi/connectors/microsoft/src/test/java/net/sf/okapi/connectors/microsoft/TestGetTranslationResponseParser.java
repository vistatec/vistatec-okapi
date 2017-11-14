package net.sf.okapi.connectors.microsoft;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import net.sf.okapi.common.StreamUtil;

import java.util.List;

@RunWith(JUnit4.class)
public class TestGetTranslationResponseParser {

	private GetTranslationsResponseParser parser;

	@Before
	public void setup() {
		parser = new GetTranslationsResponseParser();
	}

	private static final String EXPECTED_TRANSLATION =
			"<br id='p1'>Chats et<br id='p2'>chiens<br id='p3'>&amp;<br id='p4'>mouffettes<br id='p5'><br id='p6'>";
	@Test
	public void testParseGetTranslationsResponse() throws Exception {
		String xml = StreamUtil.streamUtf8AsString(
				getClass().getResourceAsStream("/GetTranslations-Response-Coded.xml"));
		List<TranslationResponse> responses = parser.parseGetTranslationsResponse(xml, 10, 50);
		assertEquals(1, responses.size());
		TranslationResponse r = responses.get(0);
		assertNull(r.sourceText);
		assertEquals(EXPECTED_TRANSLATION, r.translatedText);
		assertEquals(5, r.rating);
		assertEquals(100, r.matchDegree);
	}

	@Test
	public void testParseGetTranslationsArrayResponse() throws Exception {
		String xml = StreamUtil.streamUtf8AsString(
				getClass().getResourceAsStream("/GetTranslationsArray-Response.xml"));
		List<List<TranslationResponse>> responses = parser.parseGetTranslationsArrayResponse(xml, 10, 50);
		assertEquals(3, responses.size());
		List<TranslationResponse> r = responses.get(0);
		assertNull(r.get(0).sourceText);
		assertEquals("Cuando él siente casi.", r.get(0).translatedText);
		assertEquals(5, r.get(0).rating);
		assertEquals(100, r.get(0).matchDegree);
		// Cuando él salto que volar casi.
		// Él no consiguió ningún sentido apenas.
	}
}
