package net.sf.okapi.connectors.microsoft;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.any;

@RunWith(MockitoJUnitRunner.class)
public class TestMicrosoftMTAPIImpl {

    @Mock private HttpClient httpClient;
    @Mock private TokenProvider tokenProvider;

    @SuppressWarnings("unchecked")
    @Test
    public void testGetTranslationsFailureShouldProduceNullResponse() throws Exception {
        when(tokenProvider.get()).thenReturn("test-token");
        when(httpClient.execute(any(HttpUriRequest.class))).thenThrow(HttpHostConnectException.class);
        MicrosoftMTAPIImpl api = new MicrosoftMTAPIImpl(new Parameters(), httpClient, tokenProvider);
        List<TranslationResponse> responses = api.getTranslations("test", "en", "fr", 1, 50);
        assertNull(responses);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetTranslationsArrayFailureShouldProduceNullResponse() throws Exception {
        when(tokenProvider.get()).thenReturn("test-token");
        when(httpClient.execute(any(HttpUriRequest.class))).thenThrow(HttpHostConnectException.class);
        MicrosoftMTAPIImpl api = new MicrosoftMTAPIImpl(new Parameters(), httpClient, tokenProvider);
        GetTranslationsArrayRequest data = new GetTranslationsArrayRequest(
                Collections.singletonList("Hello"), "en", "fr", 1, "");
        List<List<TranslationResponse>> responses = api.getTranslationsArray(data, "en", "fr", 1, 50);
        assertNull(responses);
    }
}
