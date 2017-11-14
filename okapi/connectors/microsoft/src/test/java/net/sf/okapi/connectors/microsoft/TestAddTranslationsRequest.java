package net.sf.okapi.connectors.microsoft;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.custommonkey.xmlunit.XMLAssert;

public class TestAddTranslationsRequest {

	@Test
	public void test() throws Exception {
		List<String> sources = new ArrayList<>();
		sources.add("source1");
		sources.add("source2");
		List<String> translations = new ArrayList<>();
		translations.add("translation1");
		translations.add("translation2");
		List<Integer> ratings = new ArrayList<>();
		ratings.add(8);
		ratings.add(7);
		AddTranslationsRequest options = new AddTranslationsRequest(sources, translations, ratings,
							"en", "fr", "test-category");
		XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("/AddTranslationsArrayOptions.xml"), StandardCharsets.UTF_8),
                                 new StringReader(options.toXML()));
	}
}
