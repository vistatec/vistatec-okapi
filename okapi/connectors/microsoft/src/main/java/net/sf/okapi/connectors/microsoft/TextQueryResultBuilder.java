package net.sf.okapi.connectors.microsoft;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;

class TextQueryResultBuilder extends QueryResultBuilder<String> {
	TextQueryResultBuilder(Parameters params, int weight) {
		super(params, weight);
	}

	@Override
	List<QueryResult> convertResponses(List<TranslationResponse> responses, String text) {
		List<QueryResult> results = new ArrayList<>();
		for (TranslationResponse response : responses) {
			QueryResult qr = createQueryResult(response);
			qr.source = (response.sourceText == null) ? new TextFragment(text) : new TextFragment(response.sourceText);
			qr.target = new TextFragment(response.translatedText);
			results.add(qr);
		}
		return results;
	}
}
