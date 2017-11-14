package net.sf.okapi.connectors.microsoft;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.InvalidContentException;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.QueryUtil;

class FragmentQueryResultBuilder extends QueryResultBuilder<TextFragment> {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private QueryUtil util = new QueryUtil();

	FragmentQueryResultBuilder(Parameters params, int weight) {
		super(params, weight);
	}

	@Override
	List<QueryResult> convertResponses(List<TranslationResponse> responses, TextFragment frag) {
		List<QueryResult> results = new ArrayList<>();
		for (TranslationResponse response : responses) {
			QueryResult qr = createQueryResult(response);
			try {
				qr.source = (response.sourceText == null) ? frag :
							makeFragment(response.sourceText, frag);
				qr.target = makeFragment(response.translatedText, frag);
			}
			catch ( InvalidContentException e ) {
				// Something went wrong in the resulting MT candidate
				// We fall back on no candidate with a zero score
				logger.error("This MT candidate will be ignored.\n{}\n{}", frag.toString(), e.getMessage());
				qr.setQuality(QueryResult.QUALITY_UNDEFINED);
				qr.setFuzzyScore(0);
				qr.setCombinedScore(0);
				qr.source = frag;
				qr.target = frag.clone();
			}

			results.add(qr);
		}
		return results;
	}

	private TextFragment makeFragment(String codedHtml, TextFragment sourceFragment) {
		return sourceFragment.hasCode() ?
				new TextFragment(util.fromCodedHTML(codedHtml, sourceFragment, false), sourceFragment.getClonedCodes()) :
				new TextFragment(util.fromCodedHTML(codedHtml, sourceFragment, false));
	}
}
