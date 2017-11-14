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

import java.util.List;

import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;

abstract class QueryResultBuilder<T> {
    protected GoogleMTv2Parameters params;
    protected String name;
    protected int weight;

    QueryResultBuilder(GoogleMTv2Parameters params, String name, int weight) {
        this.params = params;
        this.name = name;
        this.weight = weight;
    }

    abstract QueryResult createDummyResponse(T sourceContent);

    abstract List<QueryResult> convertResponses(List<TranslationResponse> responses, T sourceContent);

    protected QueryResult createQueryResult(TranslationResponse response) {
        QueryResult qr = new QueryResult();
        qr.setFuzzyScore(95); // Arbitrary result for MT
        qr.setCombinedScore(95);
        qr.weight = weight;
        qr.origin = name;
        qr.matchType = MatchType.MT;
        return qr;
    }
}