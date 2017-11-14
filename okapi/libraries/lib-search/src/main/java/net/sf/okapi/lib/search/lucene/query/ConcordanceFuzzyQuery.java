/*===========================================================================
 Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.search.lucene.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.lib.search.lucene.scorer.ConcordanceFuzzyScorer;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultipleTermPositions;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

/**
 * 
 * @author HargraveJE
 */
@SuppressWarnings("serial")
public class ConcordanceFuzzyQuery extends Query {
	private String field;
	float threshold;
	private List<Term> terms;
	private int slop = 0;

	/**
	 * Creates a new instance of ConcordanceFuzzyQuery
	 * 
	 * @param threshold
	 */
	public ConcordanceFuzzyQuery(float threshold) {
		terms = new ArrayList<Term>();
		this.threshold = threshold;
	}

	public void setSlop(int slop) {
		this.slop = slop;
	}

	/** Returns the slop. See setSlop(). */
	public int getSlop() {
		return slop;
	}

	/** Adds a term to the end of the query phrase. */
	public void add(Term term) {
		if (terms.size() == 0) {
			field = term.field();
		} else if (term.field() != field) {
			throw new IllegalArgumentException("All phrase terms must be in the same field: "
					+ term);
		}
		terms.add(term);
	}

	/** Returns the set of terms in this phrase. */
	public Term[] getTerms() {
		return (Term[]) terms.toArray(new Term[0]);
	}

	protected class ConcordanceFuzzyWeight extends Weight {
		Similarity similarity;

		public ConcordanceFuzzyWeight(Searcher searcher) throws IOException {
			super();
			this.similarity = searcher.getSimilarity();
		}

		@Override
		public Explanation explain(IndexReader reader, int doc) throws IOException {
			return new Explanation(getValue(), toString());
		}

		@Override
		public Query getQuery() {
			return ConcordanceFuzzyQuery.this;
		}

		@Override
		public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder, boolean topScorer)
				throws IOException {

			// optimize zero-term or no match case
			if (terms.size() == 0)
				return null;

			Term[] termArray = new Term[terms.size()];
			termArray = terms.toArray(termArray);
			TermPositions[] termPositions = new TermPositions[terms.size()];
			for (int i = 0; i < termArray.length; i++) {
				TermPositions p = reader.termPositions(termArray[i]);
				if (p == null)
					return null;
				termPositions[i] = p;
			}
			MultipleTermPositions multipleTermPositions = new MultipleTermPositions(reader,
					termArray);

			return new ConcordanceFuzzyScorer(threshold, similarity, terms, termPositions,
					multipleTermPositions, reader);
		}

		@Override
		public float getValue() {
			return 1.0f;
		}

		@Override
		public void normalize(float norm) {
		}

		@Override
		public float sumOfSquaredWeights() throws IOException {
			return 1.0f;
		}
	}

	@Override
	public Weight createWeight(Searcher searcher) throws IOException {
		return new ConcordanceFuzzyWeight(searcher);
	}

	@Override
	public String toString(String field) {
		return terms.toString();
	}
}
