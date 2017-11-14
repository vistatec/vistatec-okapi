package net.sf.okapi.connectors.kantan;

import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.params.HttpParams;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KantanMTConnectorTest {

    @Mock
    private HttpClient httpClient;

    @InjectMocks
    private KantanMTConnector connector;

    @Before
    public void setup() {
        initConnector();
        initMockHttpClient();
    }

    @Test
    public void testBatchQuery() throws IOException {
        final HttpResponse mockResponse = mockHttpResponse(200, kantanJSONResponse("translation", translationDataJSON(
                Arrays.asList("First fragment", "Second fragment", "Third fragment"),
                Arrays.asList("Translated first fragment", "Translated second fragment", "Translated third fragment")
        )));

        when(httpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);
        List<List<QueryResult>> r = connector.batchQuery(generateTextFragments("First fragment", "Second fragment", "Third fragment"));
        assertEquals(3, r.size());
        assertEquals("Translated first fragment", r.get(0).get(0).target.toString());
        assertEquals("Translated second fragment", r.get(1).get(0).target.toString());
        assertEquals("Translated third fragment", r.get(2).get(0).target.toString());
    }

    @Test
    public void testQueryTextFragment() throws IOException {
        final HttpResponse mockResponse = mockHttpResponse(200, kantanJSONResponse("translation", translationDataJSON(
             Arrays.asList("First fragment"),
             Arrays.asList("Translated first fragment")
        )));
        when(httpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);
        int i = connector.query(new TextFragment("First fragment"));
        assertEquals(1, i);
        assertTrue(connector.hasNext());
        assertEquals("Translated first fragment", connector.next().target.toString());
    }

    @Test
    public void testQuery() throws IOException {
        final HttpResponse mockResponse = mockHttpResponse(200, kantanJSONResponse("translation", translationDataJSON(
                Arrays.asList("First fragment"),
                Arrays.asList("Translated first fragment")
        )));
        when(httpClient.execute(any(HttpPost.class))).thenReturn(mockResponse);
        int i = connector.query("First fragment");
        assertEquals(1, i);
        assertTrue(connector.hasNext());
        assertEquals("Translated first fragment", connector.next().target.toString());
    }

    private void initConnector() {
        ((KantanMTConnectorParameters) connector.getParameters()).setApiToken("fake_token");
        ((KantanMTConnectorParameters) connector.getParameters()).setProfileName("fake_profile");
    }

    private void initMockHttpClient() {
        when(httpClient.getConnectionManager()).thenReturn(mock(ClientConnectionManager.class));
        when(httpClient.getParams()).thenReturn(mock(HttpParams.class));
    }

    private HttpResponse mockHttpResponse(int statusCode, String body) throws IOException {
        HttpResponse response = mock(HttpResponse.class);

        StatusLine statusLine = mock(StatusLine.class);
        when(statusLine.getStatusCode()).thenReturn(statusCode);

        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));

        when(response.getStatusLine()).thenReturn(statusLine);
        when(response.getEntity()).thenReturn(httpEntity);

        return response;
    }

    private List<TextFragment> generateTextFragments(String ... src) {
        List<TextFragment> fragments = new ArrayList<TextFragment>();
        for (String s : src) {
            fragments.add(new TextFragment(s));
        }
        return fragments;
    }

    private String translationDataJSON(List<String> srcStrings, List<String> tgtStrings) {
        String templateTranslationData = "\"translationData\": [%s]";
        String templateTranslationItem = "{\"src\": \"%s\", \"trg\":\"%s\", \"id\": %d}";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < srcStrings.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(String.format(templateTranslationItem, srcStrings.get(i), tgtStrings.get(i), i));
        }
        return String.format(templateTranslationData, sb.toString());
    }

    private String kantanJSONResponse(String type, String body) {
        return String.format("{\"response\": {\"type\": \"%s\", \"body\": { %s }}}", type, body);
    }
}
