package net.sf.okapi.connectors.microsoft;

import java.util.List;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;

abstract class QueryResultBuilder<T> {
	protected Parameters params;
	protected int weight;

	QueryResultBuilder(Parameters params, int weight) {
		this.params = params;
		this.weight = weight;
	}

	abstract List<QueryResult> convertResponses(List<TranslationResponse> responses, T sourceContent);

	protected QueryResult createQueryResult(TranslationResponse response) {
		QueryResult qr = new QueryResult();
		qr.setQuality(Util.normalizeRange(-10, 10, response.rating));
		qr.setFuzzyScore(response.matchDegree); // Score from the system
		qr.setCombinedScore(response.combinedScore); // Adjusted score
		// Else: continue with that result
		qr.weight = weight;
		qr.origin = "Microsoft-Translator";
		if ( !Util.isEmpty(params.getCategory()) ) {
			qr.engine = params.getCategory();
		}
		qr.matchType = MatchType.MT;
		return qr;
	}
}
