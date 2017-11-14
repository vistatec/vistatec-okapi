package net.sf.okapi.lib.search.lucene.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.okapi.lib.search.lucene.scorer.TmFuzzyScorer;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

@SuppressWarnings("serial")
public class TmFuzzyQuery extends Query {
	float threshold;
	List<Term> terms;
	String termCountField;

	public TmFuzzyQuery(float threshold, String termCountField) {
		this.threshold = threshold;
		this.terms = new ArrayList<Term>();
		this.termCountField = termCountField;
	}

	public void add(Term term) {
		terms.add(term);
	}

	@Override
	public Weight createWeight(Searcher searcher) throws IOException {
		return new TmFuzzyWeight(searcher);
	}

	@Override
	public void extractTerms(Set<Term> terms) {
		terms.addAll(terms);
	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		return this;
	}

	@Override
	public String toString(String field) {
		return terms.toString();
	}

	protected class TmFuzzyWeight extends Weight {
		Similarity similarity;

		public TmFuzzyWeight(Searcher searcher) throws IOException {
			super();
			this.similarity = searcher.getSimilarity();
		}

		@Override
		public Explanation explain(IndexReader reader, int doc)
				throws IOException {
			return new Explanation(getValue(), toString());
		}

		@Override
		public Query getQuery() {
			return TmFuzzyQuery.this;
		}

		@Override
		public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder,
				boolean topScorer) throws IOException {

			// optimize zero-term or no match case
			if (terms.size() == 0)
				return null;

			return new TmFuzzyScorer(threshold, similarity, terms, reader, termCountField);
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
}
