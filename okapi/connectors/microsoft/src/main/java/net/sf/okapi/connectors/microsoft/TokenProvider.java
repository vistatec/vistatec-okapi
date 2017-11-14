package net.sf.okapi.connectors.microsoft;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.exceptions.OkapiException;

class TokenProvider {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final int TOKENRETRIES = 3;
	private int SLEEPPAUSE_MS = 300;
	private static final String TOKEN_URL = "https://api.cognitive.microsoft.com/sts/v1.0/issueToken";
	private static final String KEY_HEADER = "Ocp-Apim-Subscription-Key";
	// According to docs, the token lasts 10 minutes.  We'll refresh after 7.
	private int TOKEN_EXPIRATION_MS = 7 * 60 * 1000;

	private HttpClient httpClient;
	private Parameters params;
	private Token token = expiredToken();

	public TokenProvider(HttpClient httpClient, Parameters params) {
		this.httpClient = httpClient;
		this.params = params;
	}

	/**
	 * Holds the access token and an expiration time, in the form of a raw Date value.
	 */
	public static class Token {
		final String token;
		final long expiresOn;
		public Token(String token, long expiresOn) {
			this.token = token;
			this.expiresOn = expiresOn;
		}
		public boolean isExpired() {
			return System.currentTimeMillis() >= expiresOn;
		}
	}

	public static Token expiredToken() {
		return new Token("", 0);
	}

	public String get() {
		if (token.isExpired() || isExpiring()) {
			for (int tries = 0; tries < TOKENRETRIES; tries++) {
				if (getAccesstoken()) {
					return token.token;
				}
				else {
					if ( tries < TOKENRETRIES - 1 ) {
						sleep(SLEEPPAUSE_MS);
					}
				}
			}
			throw new OkapiException(String.format(
					"Failed to get Microsoft Translator access token after %d tries.",
					TOKENRETRIES));
		}
		return token.token;
	}

	// If less than half a second is left, let it expire
	private boolean isExpiring() {
		long now = System.currentTimeMillis();
		if (now > token.expiresOn - 500) {
			sleep(token.expiresOn - now);
			return true;
		}
		return false;
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		}
		catch ( InterruptedException e ) {
			throw new OkapiException("Sleep interrupted while attempting to get Azure Marketplace Token" + e.getMessage(), e);
		}
	}

	private boolean getAccesstoken () {
		try {
			String tokenRes = getRawToken();
			if ( tokenRes != null ) {
				token = new Token(tokenRes, System.currentTimeMillis() + TOKEN_EXPIRATION_MS);
				return true;
			}
		}
		catch ( OkapiException e ) {
			throw e;
		}
		catch (Exception e) {
			// Log and retry
			logger.error("Error in getAccestoken: {}", e.getMessage());
		}
		return false;
	}

	private String getRawToken() {
		if (params.getAzureKey() == null || "".equals(params.getAzureKey())) {
			throw new AzureAuthenticationException("You must specify an Azure authentication key.");
		}
		HttpResponse response = null;
		try {
			HttpPost post = new HttpPost(TOKEN_URL);
			post.addHeader(KEY_HEADER, params.getAzureKey());
			response = httpClient.execute(post);
			StatusLine status = response.getStatusLine();
			try (InputStream is = response.getEntity().getContent()) {
				String responseBody = StreamUtil.streamUtf8AsString(is);
				if (status.getStatusCode() == 200) {
					return responseBody;
				}
				logger.error("Failed to get token: status {}: {}\nBody: {}", status.getStatusCode(),
						status.getReasonPhrase(), responseBody);
				throw new AzureAuthenticationException("Failed to get token: status " +
						status.getStatusCode() + ", " + status.getReasonPhrase());
			}
		}
		catch (Exception e) {
			logger.error("Failed to fetch token", e);
			throw new AzureAuthenticationException("Failed to get Microsoft Translator token", e);
		}
		finally {
            if (response != null) {
                try {
                    response.getEntity().getContent().close();
                }
                catch (IOException e) {}
            }
        }
	}
}
