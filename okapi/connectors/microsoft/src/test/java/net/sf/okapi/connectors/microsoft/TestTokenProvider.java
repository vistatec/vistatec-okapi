package net.sf.okapi.connectors.microsoft;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestTokenProvider {
	@Mock private HttpClient httpClient;
	@Mock private HttpResponse response;
	@Mock private StatusLine statusLine;

	@Test(expected = AzureAuthenticationException.class)
	public void testNoAzureKeySpecified() {
		TokenProvider provider = new TokenProvider(httpClient, new Parameters());
		provider.get();
	}

	@Test(expected = AzureAuthenticationException.class)
	public void testIncorrectAzureKeySpecified() throws IOException {
		when(statusLine.getStatusCode()).thenReturn(403);
		when(statusLine.getReasonPhrase()).thenReturn("Invalid key");
		when(response.getStatusLine()).thenReturn(statusLine);
		when(httpClient.execute(any(HttpPost.class))).thenReturn(response);
		TokenProvider provider = new TokenProvider(httpClient, new Parameters());
		provider.get();
	}
}
