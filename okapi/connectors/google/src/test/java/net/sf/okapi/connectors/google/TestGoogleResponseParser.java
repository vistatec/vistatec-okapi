package net.sf.okapi.connectors.google;

import org.junit.Test;

import net.sf.okapi.common.StreamUtil;

import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestGoogleResponseParser {
    private GoogleResponseParser parser = new GoogleResponseParser();

    @Test
    public void testTwoResponses() throws Exception {
        List<String> responses = parser.parseResponse(r("/2translations.json"));
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Hallo Welt", responses.get(0));
        assertEquals("Mein Name ist Jeff", responses.get(1));
    }

    @Test
    public void testResponseWithTags() throws Exception {
        List<String> responses = parser.parseResponse(r("/translation_with_tags.json"));
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Hallo <span />Welt</span>", responses.get(0));
    }

    @Test
    public void testError() throws Exception {
        GoogleMTErrorException error = parser.parseError(403,
                StreamUtil.streamUtf8AsString(getClass().getResourceAsStream("/error.json")), "test");
        assertEquals(403, error.getCode());
        assertEquals("Daily Limit Exceeded", error.getMessage());
        assertEquals("usageLimits", error.getDomain());
        assertEquals("dailyLimitExceeded", error.getReason());
        assertEquals("test", error.getQuery());
    }

    @Test
    public void testEscaping() throws Exception {
        List<String> responses = parser.parseResponse(r("/escaped.json"));
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Bonjour> & \"'<", responses.get(0));
    }

    @Test
    public void testLanguages() throws Exception {
        List<String> langs = parser.parseLanguagesResponse(r("/languages.json"));
        assertNotNull(langs);
        assertEquals(4, langs.size());
        assertEquals("de", langs.get(0));
        assertEquals("fr", langs.get(1));
        assertEquals("zh", langs.get(2));
        assertEquals("zh-TW", langs.get(3));
    }

    private Reader r(String resource) {
        return new InputStreamReader(getClass().getResourceAsStream(resource), StandardCharsets.UTF_8);
    }
}
