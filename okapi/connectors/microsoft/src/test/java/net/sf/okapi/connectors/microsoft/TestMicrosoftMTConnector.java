package net.sf.okapi.connectors.microsoft;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.QueryUtil;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class TestMicrosoftMTConnector {
	private MicrosoftMTConnector connector;
	private QueryUtil util = new QueryUtil();

	@Rule
    public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setup() {
		connector = new MicrosoftMTConnector();
		connector.setLanguages(LocaleId.ENGLISH, LocaleId.FRENCH);
	}

	@Test
	public void testQueryText() throws Exception {
		MicrosoftMTAPI api = Mockito.mock(MicrosoftMTAPI.class);
		List<TranslationResponse> responses = new ArrayList<>();
		responses.add(new TranslationResponse("Hello world", "Translation 1", 5, 90));
		responses.add(new TranslationResponse("Hello world", "Translation 2", 5, 80));
		connector.setMaximumHits(10);
		Mockito.when(api.getTranslations("Hello world", connector.getSourceLanguage().getLanguage(),
										 connector.getTargetLanguage().getLanguage(),
								 		 connector.getMaximumHits(), connector.getThreshold()))
			   .thenReturn(responses);
		connector.open(api);
		int count = connector.query("Hello world");
		assertEquals(2, count);
		assertTrue(connector.hasNext());
		QueryResult qr = connector.next();
		assertEquals(new TextFragment("Translation 1"), qr.target);
		assertEquals(90, qr.getCombinedScore());
		qr = connector.next();
		assertEquals(new TextFragment("Translation 2"), qr.target);
		assertEquals(80, qr.getCombinedScore());
	}

	@Test
	public void testQueryTextFragment() throws Exception {
		MicrosoftMTAPI api = Mockito.mock(MicrosoftMTAPI.class);
		List<TranslationResponse> responses = new ArrayList<>();
		TextFragment src = SampleTextFragments.makeSourceTextFragment();
		TextFragment tgt = SampleTextFragments.makeTargetTextFragment();
		responses.add(new TranslationResponse(util.toCodedHTML(src), util.toCodedHTML(tgt), 5, 90));
		Mockito.when(api.getTranslations(util.toCodedHTML(src), connector.getSourceLanguage().getLanguage(),
										 connector.getTargetLanguage().getLanguage(),
								 		 connector.getMaximumHits(), connector.getThreshold()))
			   .thenReturn(responses);
		connector.open(api);
		int count = connector.query(src);
		assertEquals(1, count);
		assertTrue(connector.hasNext());
		QueryResult qr = connector.next();
		assertEquals(tgt, qr.target);
		assertEquals(90, qr.getCombinedScore());
	}

	@Test
	public void testBatchQueryText() throws Exception {
		MicrosoftMTAPI api = Mockito.mock(MicrosoftMTAPI.class);
		List<List<TranslationResponse>> responses = new ArrayList<>();
		responses.add(Collections.singletonList(new TranslationResponse("Source 1", "Translation 1", 5, 90)));
		responses.add(Collections.singletonList(new TranslationResponse("Source 2", "Translation 2", 5, 90)));
		List<String> sources = new ArrayList<>();
		sources.add("Source 1");
		sources.add("Source 2");
		GetTranslationsArrayRequest request = new GetTranslationsArrayRequest(sources,
				connector.getSourceLanguage().getLanguage(),connector.getTargetLanguage().getLanguage(),
		 		connector.getMaximumHits(), "");
		Mockito.when(api.getTranslationsArray(request, connector.getSourceLanguage().getLanguage(),
				connector.getTargetLanguage().getLanguage(), connector.getMaximumHits(), connector.getThreshold()))
				.thenReturn(responses);
		connector.open(api);
		List<List<QueryResult>> results = connector.batchQueryText(sources);
		assertEquals(2, results.size());
		List<QueryResult> qrs = results.get(0);
		assertEquals(1, qrs.size());
		assertEquals(new TextFragment("Translation 1"), qrs.get(0).target);
		assertEquals(90, qrs.get(0).getCombinedScore());
		qrs = results.get(1);
		assertEquals(1, qrs.size());
		assertEquals(new TextFragment("Translation 2"), qrs.get(0).target);
		assertEquals(90, qrs.get(0).getCombinedScore());
	}

	@Test
	public void testFilterLargeQueries() throws Exception {
		MicrosoftMTAPI api = Mockito.mock(MicrosoftMTAPI.class);
		List<List<TranslationResponse>> responses = new ArrayList<>();
		responses.add(Collections.singletonList(new TranslationResponse("Source 1", "Translation 1", 5, 90)));
		responses.add(Collections.singletonList(new TranslationResponse("Source 2", "Translation 2", 5, 90)));
		List<String> sources = new ArrayList<>();
		sources.add("Source 1");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 1100; i++) {
			sb.append("0123456789 ");
		}
		sources.add(sb.toString());
		assertTrue(sources.get(1).length() > 10000);
		sources.add("Source 2");
		List<String> filteredSources = new ArrayList<>();
		filteredSources.add("Source 1");
		filteredSources.add("Source 2");
		GetTranslationsArrayRequest request = new GetTranslationsArrayRequest(filteredSources,
				connector.getSourceLanguage().getLanguage(),connector.getTargetLanguage().getLanguage(),
		 		connector.getMaximumHits(), "");
		Mockito.when(api.getTranslationsArray(request, connector.getSourceLanguage().getLanguage(),
				connector.getTargetLanguage().getLanguage(), connector.getMaximumHits(), connector.getThreshold()))
				.thenReturn(responses);
		connector.open(api);
		List<List<QueryResult>> results = connector.batchQueryText(sources);
		assertEquals(2, results.size());
		List<QueryResult> qrs = results.get(0);
		assertEquals(1, qrs.size());
		assertEquals(new TextFragment("Translation 1"), qrs.get(0).target);
		assertEquals(90, qrs.get(0).getCombinedScore());
		qrs = results.get(1);
		assertEquals(1, qrs.size());
		assertEquals(new TextFragment("Translation 2"), qrs.get(0).target);
		assertEquals(90, qrs.get(0).getCombinedScore());
	}

	@Test
	public void testBatchQueryFragment() throws Exception {
		MicrosoftMTAPI api = Mockito.mock(MicrosoftMTAPI.class);
		List<List<TranslationResponse>> responses = new ArrayList<>();
		responses.add(Collections.singletonList(new TranslationResponse("Source 1", "Translation 1", 5, 90)));
		responses.add(Collections.singletonList(new TranslationResponse("Source 2", "Translation 2", 5, 90)));
		List<TextFragment> sources = new ArrayList<>();
		sources.add(new TextFragment("Source 1"));
		sources.add(new TextFragment("Source 2"));
		GetTranslationsArrayRequest request = new GetTranslationsArrayRequest(util.toCodedHTML(sources),
				connector.getSourceLanguage().getLanguage(),connector.getTargetLanguage().getLanguage(),
		 		connector.getMaximumHits(), "");
		Mockito.when(api.getTranslationsArray(request, connector.getSourceLanguage().getLanguage(),
				connector.getTargetLanguage().getLanguage(), connector.getMaximumHits(), connector.getThreshold()))
				.thenReturn(responses);
		connector.open(api);
		List<List<QueryResult>> results = connector.batchQuery(sources);
		assertEquals(2, results.size());
		List<QueryResult> qrs = results.get(0);
		assertEquals(1, qrs.size());
		assertEquals(new TextFragment("Translation 1"), qrs.get(0).target);
		assertEquals(90, qrs.get(0).getCombinedScore());
		qrs = results.get(1);
		assertEquals(1, qrs.size());
		assertEquals(new TextFragment("Translation 2"), qrs.get(0).target);
		assertEquals(90, qrs.get(0).getCombinedScore());
	}

	private static final int NUM_STRINGS = 50;

	@Test
	public void testMediumBatch() throws Exception {
		MicrosoftMTAPI api = Mockito.mock(MicrosoftMTAPI.class);
		List<List<TranslationResponse>> r = new ArrayList<>();
		r.add(Collections.singletonList(new TranslationResponse("Source", "Target", 5, 99)));
		when(api.getTranslationsArray(any(GetTranslationsArrayRequest.class),
		                any(String.class), any(String.class), 
		                eq(connector.getMaximumHits()), eq(connector.getThreshold())))
		    .thenReturn(r);
		List<List<TranslationResponse>> responses = new ArrayList<>();
		List<String> sources = new ArrayList<>();
		for (int i = 0; i < 11; i++) {
			responses.add(Collections.singletonList(new TranslationResponse("Source " + i, "Translation " + i, 5, 90)));
			sources.add("Source " + i);
		}

		connector.open(api);
		connector.batchQueryText(sources);
		verify(api, times(2)).getTranslationsArray(any(GetTranslationsArrayRequest.class),
				eq(connector.getSourceLanguage().getLanguage()),
				eq(connector.getTargetLanguage().getLanguage()), 
				eq(connector.getMaximumHits()),
				eq(connector.getThreshold()));
	}

	@Test
	public void testLargeBatchQueryText() throws Exception {
		MicrosoftMTAPI api = Mockito.mock(MicrosoftMTAPI.class);
		List<List<TranslationResponse>> r = new ArrayList<>();
		r.add(Collections.singletonList(new TranslationResponse("Source", "Target", 5, 99)));
		when(api.getTranslationsArray(any(GetTranslationsArrayRequest.class),
		                any(String.class), any(String.class), 
		                eq(connector.getMaximumHits()), eq(connector.getThreshold())))
		    .thenReturn(r);
		List<List<TranslationResponse>> responses = new ArrayList<>();
		List<String> sources = new ArrayList<>();
		for (int i = 0; i < NUM_STRINGS; i++) {
			responses.add(Collections.singletonList(new TranslationResponse("Source " + i, "Translation " + i, 5, 90)));
			sources.add("Source " + i);
		}

		connector.open(api);
		connector.batchQueryText(sources);
		verify(api, times(5)).getTranslationsArray(any(GetTranslationsArrayRequest.class),
				eq(connector.getSourceLanguage().getLanguage()),
				eq(connector.getTargetLanguage().getLanguage()), 
				eq(connector.getMaximumHits()),
				eq(connector.getThreshold()));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testQueryTextFailure() throws Exception {
		MicrosoftMTAPI api = Mockito.mock(MicrosoftMTAPI.class);
		Mockito.when(api.getTranslations("Hello world", connector.getSourceLanguage().getLanguage(),
										 connector.getTargetLanguage().getLanguage(),
								 		 connector.getMaximumHits(), connector.getThreshold()))
			   .thenThrow(IOException.class);
		connector.open(api);
		thrown.expect(OkapiException.class);
		connector.query("Hello world");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testQueryTextFragmentFailure() throws Exception {
		MicrosoftMTAPI api = Mockito.mock(MicrosoftMTAPI.class);
		Mockito.when(api.getTranslations("Hello world", connector.getSourceLanguage().getLanguage(),
										 connector.getTargetLanguage().getLanguage(),
								 		 connector.getMaximumHits(), connector.getThreshold()))
			   .thenThrow(IOException.class);
		connector.open(api);
		thrown.expect(OkapiException.class);
		connector.query(new TextFragment("Hello world"));
	}

	@Test
	public void testToInternalCode() {
		MSMTConnectorForTest test = new MSMTConnectorForTest();
		assertEquals("zh-CHT", test.toInternalCode(new LocaleId("zh", "TW")));
		assertEquals("zh-CHT", test.toInternalCode(new LocaleId("zh", "hk")));
		assertEquals("zh-CHS", test.toInternalCode(new LocaleId("zh")));
		assertEquals("fr", test.toInternalCode(new LocaleId("fr", "FR")));
		assertEquals("id", test.toInternalCode(new LocaleId("in", "ID")));
	}

	class MSMTConnectorForTest extends MicrosoftMTConnector {
		@Override
		public String toInternalCode (LocaleId locale) {
			return super.toInternalCode(locale);
		}
	}
}
