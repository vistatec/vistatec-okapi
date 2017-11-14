/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.connectors.google;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.InvalidContentException;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.QueryUtil;

class FragmentQueryResultBuilder extends QueryResultBuilder<TextFragment> {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private QueryUtil util = new QueryUtil();

    FragmentQueryResultBuilder(GoogleMTv2Parameters params, String name, int weight) {
        super(params, name, weight);
    }

    @Override
    QueryResult createDummyResponse(TextFragment sourceContent) {
        QueryResult qr = new QueryResult();
        qr.setFuzzyScore(0);
        qr.setCombinedScore(0);
        qr.weight = weight;
        qr.origin = name;
        qr.matchType = MatchType.MT;
        qr.source = sourceContent;
        qr.target = sourceContent.clone();
        qr.setQuality(QueryResult.QUALITY_UNDEFINED);
        return qr;
    }

    @Override
    List<QueryResult> convertResponses(List<TranslationResponse> responses, TextFragment frag) {
        List<QueryResult> results = new ArrayList<>();
        for (TranslationResponse response : responses) {
            QueryResult qr = createQueryResult(response);
            try {
                qr.source = (response.getSource() == null) ? frag :
                            makeFragment(response.getSource(), frag);
                qr.target = makeFragment(response.getTarget(), frag);
            }
            catch ( InvalidContentException e ) {
                // Something went wrong in the resulting MT candidate
                // We fall back on no candidate with a zero score
                logger.error("This MT candidate will be ignored.\n{}\n{}", frag.toString(), e.getMessage());
                qr = createDummyResponse(frag);
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
