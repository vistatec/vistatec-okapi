package net.sf.okapi.connectors.microsoft;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.StreamUtil;

class MicrosoftMTAPIImpl implements MicrosoftMTAPI {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private HttpClient httpClient;
	private TokenProvider tokenProvider;

	public static final String MSMT_BASE_URL = "http://api.microsofttranslator.com/v2/Http.svc";

	MicrosoftMTAPIImpl(Parameters params, HttpClient httpClient, TokenProvider tokenProvider) {
		this.params = params;
		this.httpClient = httpClient;
		this.tokenProvider = tokenProvider;
	}

	HttpClient getHttpClient() {
		return httpClient;
	}

	void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	@Override
	public List<TranslationResponse> getTranslations(String query, String srcLang, String trgLang, int maxHits, int threshold) {
		String options = new GetTranslateOptions(params.getCategory()).toXML();
		HttpEntity body = new StringEntity(options, StandardCharsets.UTF_8);
		HttpUriRequest request = RequestBuilder.post(MSMT_BASE_URL + "/GetTranslations")
					  .addParameter("text", query) // XXX handles UTF-8 escaping?
					  .addParameter("from", srcLang)
					  .addParameter("to", trgLang)
					  .addParameter("maxTranslations", String.valueOf(maxHits))
					  .addHeader("Content-Type", "text/xml")
					  .addHeader("Authorization", "Bearer " + tokenProvider.get())
					  .setEntity(body)
					  .build();
		if (logger.isDebugEnabled()) {
			logger.debug("getTranslation options: " + options);
		}
		String xml = getXmlResponse(request, options);
		return (xml != null) ?
		        new GetTranslationsResponseParser().parseGetTranslationsResponse(xml, maxHits, threshold) :
	            null;
	}

	@Override
	public List<List<TranslationResponse>> getTranslationsArray(GetTranslationsArrayRequest data,
							String srcLang, String trgLang, int maxHits, int threshold) {
		String queryXml = data.toXML();
		HttpEntity body = new StringEntity(queryXml, StandardCharsets.UTF_8);
		HttpUriRequest request = RequestBuilder.post(MSMT_BASE_URL + "/GetTranslationsArray")
					.addParameter("from", srcLang)
					.addParameter("to", trgLang)
					.addParameter("maxTranslations", String.valueOf(maxHits))
					.addHeader("Content-Type", "text/xml")
					.addHeader("Authorization", "Bearer " + tokenProvider.get())
					.setEntity(body)
					.build();
		if (logger.isDebugEnabled()) {
			logger.debug("getTranslationsArray options: " + queryXml);
		}
		String xml = getXmlResponse(request, queryXml);
		return (xml != null) ?
		        new GetTranslationsResponseParser().parseGetTranslationsArrayResponse(xml, maxHits, threshold) :
	            null;
	}

	private String getXmlResponse(HttpUriRequest request, String queryXml) {
		HttpResponse response = null;
		try {
			response = execute(request);
			StatusLine status = response.getStatusLine();
			try (InputStream is = response.getEntity().getContent()) {
				String responseBody = StreamUtil.streamUtf8AsString(is);
				if (status.getStatusCode() == 200) {
					return responseBody;
				}
				logger.error("Query response code: {}: {}\nBody: {}\nFor query {} with body {}", status.getStatusCode(),
						status.getReasonPhrase(), responseBody, request.toString(), queryXml);
				if (response.containsHeader("X-MS-Trans-Info")) {
					for (Header h : response.getHeaders("X-MS-Trans-Info")) {
						logger.error("X-MS-Trans-Info Header: {}", h.getValue());
					}
				}
			}
		}
		catch (IOException e) {
			logger.error("Query failed: {}, for query {} with body {}", e.getMessage(), request.toString(), queryXml);
			logger.error("Full failure trace: ", e);
		}
		finally {
			// Make sure that we always close the inputstream for the response -- otherwise
			// httpclient will leak the connection!
			if (response != null) {
				try {
					response.getEntity().getContent().close();
				}
				catch (IOException e) {}
			}
		}
		return null;
	}

	@Override
	public int addTranslation(String original, String translation, String srcLang, String trgLang,
								 int rating) throws IOException {
		HttpUriRequest request = RequestBuilder.get(MSMT_BASE_URL + "/AddTranslation")
				.addParameter("originaltext", original)
				.addParameter("translatedtext", translation)
				.addParameter("from", srcLang)
				.addParameter("to", trgLang)
				.addParameter("user", "defaultUser")
				.addParameter("rating", String.valueOf(rating))
				.addParameter("category", params.getCategory())
				.addHeader("Content-Type", "text/xml")
				.addHeader("Authorization", "Bearer " + tokenProvider.get())
				.build();
		HttpResponse response = execute(request);
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() != 200) {
			logger.error("Error adding translation to the server: {}: {}, for query {}", status.getStatusCode(),
					 status.getReasonPhrase(), request.toString());
		}
		return status.getStatusCode();
	}

	@Override
	public int addTranslationArray(List<String> sources, List<String> translations, List<Integer> ratings,
								  String srcLang, String trgLang) throws IOException {
		String xml = new AddTranslationsRequest(sources, translations, ratings, srcLang, trgLang,
												params.getCategory()).toXML();
		HttpEntity body = new StringEntity(xml, StandardCharsets.UTF_8);
		HttpUriRequest request = RequestBuilder.post(MSMT_BASE_URL + "/AddTranslationArray")
				.addHeader("Content-Type", "text/xml")
				.addHeader("Authorization", "Bearer " + tokenProvider.get())
				.setEntity(body)
				.build();
		if (logger.isDebugEnabled()) {
			logger.debug("addTranslationList options: " + xml);
		}
		HttpResponse response = execute(request);
		StatusLine status = response.getStatusLine();
		if (status.getStatusCode() != 200) {
			logger.error("Error adding translations to the server: {}: {}, for query {}", status.getStatusCode(),
					 status.getReasonPhrase(), request.toString());
		}
		return status.getStatusCode();
	}

	private HttpResponse execute(HttpUriRequest request) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug(request.toString());
		}
		HttpResponse response = httpClient.execute(request);
		if (logger.isDebugEnabled()) {
			logger.debug(response.toString());
		}
		return response;
	}	
}
