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

import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;

class TextQueryResultBuilder extends QueryResultBuilder<String> {
    TextQueryResultBuilder(GoogleMTv2Parameters params, String name, int weight) {
        super(params, name, weight);
    }

    @Override
    List<QueryResult> convertResponses(List<TranslationResponse> responses, String text) {
        List<QueryResult> results = new ArrayList<>();
        for (TranslationResponse response : responses) {
            QueryResult qr = createQueryResult(response);
            qr.source = (response.getSource() == null) ? new TextFragment(text) : new TextFragment(response.getSource());
            qr.target = new TextFragment(response.getTarget());
            results.add(qr);
        }
        return results;
    }

    @Override
    QueryResult createDummyResponse(String sourceContent) {
        QueryResult qr = new QueryResult();
        qr.setFuzzyScore(0);
        qr.setCombinedScore(0);
        qr.weight = weight;
        qr.origin = name;
        qr.matchType = MatchType.MT;
        qr.source = new TextFragment(sourceContent);
        qr.target = qr.source.clone();
        qr.setQuality(QueryResult.QUALITY_UNDEFINED);
        return qr;
    }
}