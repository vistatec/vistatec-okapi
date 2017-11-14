package net.sf.okapi.connectors.microsoft;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.QueryUtil;

import java.util.Collections;
import java.util.List;

@RunWith(JUnit4.class)
public class TestQueryResultBuilder {
	private QueryUtil util = new QueryUtil();

	@Test
	public void testConvertTextData() {
		TextQueryResultBuilder qrBuilder = new TextQueryResultBuilder(new Parameters(), 1);
		String text = "Hello <br/> goodbye";
		String translated = "Bonjour <br/> au revoir";
		TranslationResponse response = new TranslationResponse(text, translated, 6, 80);
		List<QueryResult> results = qrBuilder.convertResponses(Collections.singletonList(response), text);
		assertEquals(1, results.size());
		QueryResult qr = results.get(0);
		assertEquals(new TextFragment(text), qr.source);
		assertEquals(new TextFragment(translated), qr.target);
		assertEquals(1, qr.weight);
		assertEquals(80, qr.getCombinedScore());
		assertEquals(80, qr.getQuality());
		assertEquals(80, qr.getFuzzyScore());
	}

	@Test
	public void testConvertFragmentData() throws Exception {
		FragmentQueryResultBuilder qrBuilder = new FragmentQueryResultBuilder(new Parameters(), 1);
		TextFragment frag = SampleTextFragments.makeSourceTextFragment();
		TranslationResponse response = new TranslationResponse(util.toCodedHTML(frag),
				"<u id='1'>Chats et <br id='b2'/>chiens<br id='e4'/> &amp; <br id='b3'/>mouffettes<br id='e5'/>.</u>",
				6, 80);
		List<QueryResult> results = qrBuilder.convertResponses(Collections.singletonList(response), frag);
		assertEquals(1, results.size());
		QueryResult qr = results.get(0);
		assertEquals(frag, qr.source);
		assertEquals(SampleTextFragments.makeTargetTextFragment(), qr.target);
		assertEquals(1, qr.weight);
		assertEquals(80, qr.getCombinedScore());
		assertEquals(80, qr.getQuality());
		assertEquals(80, qr.getFuzzyScore());
	}

	@Test
	public void testConvertFragmentDataWithoutCodes() throws Exception {
		FragmentQueryResultBuilder qrBuilder = new FragmentQueryResultBuilder(new Parameters(), 1);
		TextFragment frag = new TextFragment("Cats and dogs.");
		TranslationResponse response = new TranslationResponse(util.toCodedHTML(frag), "Chats et chiens.", 6, 80);
		List<QueryResult> results = qrBuilder.convertResponses(Collections.singletonList(response), frag);
		assertEquals(1, results.size());
		QueryResult qr = results.get(0);
		assertEquals(frag, qr.source);
		assertEquals(new TextFragment("Chats et chiens."), qr.target);
	}
}
