package net.sf.okapi.connectors.microsoft;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

public class TestGetTranslationsArrayRequest {

	@Test
	public void test() throws Exception {
		List<String> texts = new ArrayList<>();
		texts.add("string1");
		texts.add("string2");
		texts.add("string3");
		GetTranslationsArrayRequest data = new GetTranslationsArrayRequest(texts, "en", "fr", 1, "test-category");
		XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("/GetTranslationsArrayRequest.xml"),
				StandardCharsets.UTF_8), new StringReader(data.toXML()));
	}
}
