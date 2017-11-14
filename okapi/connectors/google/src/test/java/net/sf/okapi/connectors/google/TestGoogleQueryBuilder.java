package net.sf.okapi.connectors.google;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestGoogleQueryBuilder {

    @Test
    public void testUsePBMT() throws Exception {
        GoogleMTv2Parameters params = new GoogleMTv2Parameters();
        params.setApiKey("123");
        params.setUsePBMT(true);
        GoogleQueryBuilder<Object> builder = new GoogleQueryBuilder<>("http://localhost", params, "en", "de");
        assertEquals("http://localhost?key=123&source=en&target=de&model=base", builder.getQuery());
    }

    @Test
    public void testDontUsePBMT() throws Exception {
        GoogleMTv2Parameters params = new GoogleMTv2Parameters();
        params.setApiKey("123");
        GoogleQueryBuilder<Object> builder = new GoogleQueryBuilder<>("http://localhost", params, "en", "de");
        assertEquals("http://localhost?key=123&source=en&target=de", builder.getQuery());
    }
}
