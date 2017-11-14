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

package net.sf.okapi.lib.search.lucene.scorer;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultipleTermPositions;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;

public class ConcordanceFuzzyScorer extends Scorer {
	private static float ROUGH_THRSHOLD = 0.50f;

	private static int max(int x1, int x2) {
		return (x1 > x2 ? x1 : x2);
	}

	private static int max(int x1, int x2, int x3, int x4) {
		return max(max(x1, x2), max(x3, x4));
	}

	private MultipleTermPositions multiTermPositions;
	private TermPositions[] termPositions;
	private IndexReader reader;
	private float score;
	private int[] query;
	private int matches = 0;
	private float threshold;
	private int currentDoc;
	private List<Term> terms;

	public ConcordanceFuzzyScorer(float threshold, Similarity similarity, List<Term> terms,
			TermPositions[] termPositions, MultipleTermPositions multiTermPositions,
			IndexReader reader) throws IOException {
		super(similarity);
		this.threshold = threshold;
		this.multiTermPositions = multiTermPositions;
		this.termPositions = termPositions;
		this.reader = reader;
		this.currentDoc = -1;
		this.terms = terms;

		// initialize term symbols for local alignment
		query = new int[terms.size()];
		for (int i = 1; i < terms.size(); i++) {
			query[i] = i;
		}
	}

	private int findNext() throws IOException {
		// calculateSimpleFilter() is a quick and dirty measure for bad matches
		while (true) {
			if (calculateSimpleFilter() >= ROUGH_THRSHOLD
					&& (calculateScore(multiTermPositions.doc())) >= threshold) {
				break; // we found a match
			} else {
				if (multiTermPositions.next())
					continue;
				else
					return NO_MORE_DOCS;
			}
		}

		return multiTermPositions.doc();
	}

	@Override
	public float score() throws IOException {
		return score;
	}

	private float calculateSimpleFilter() {
		return (float) multiTermPositions.freq() / (float) terms.size();
	}

	private float calculateScore(int d) throws IOException {
		int[] hit = null;
		int[] query = new int[terms.size()];
		int maxpos = 0;
		int minpos = 0;

		// find maximum position in the hit
		minpos = multiTermPositions.nextPosition();
		for (int i = 1; i < multiTermPositions.freq(); i++) {
			maxpos = multiTermPositions.nextPosition();
		}

		int size = (maxpos - minpos) + 1;
		if (maxpos <= 0) {
			size = 1;
		}
		hit = new int[size];

		for (int i = 0; i < terms.size(); i++) {
			query[i] = i + 1;
			TermPositions tp = termPositions[i];
			if (tp == null)
				continue; // already reached the end
			if (!tp.skipTo(d)) // end of the line, no more docs for this term
			{
				termPositions[i].close();
				termPositions[i] = null;
				continue;
			}

			if (tp.doc() != d) {
				// start over
				termPositions[i] = reader.termPositions(terms.get(i));
				continue; // this term must not be in our document
			}

			for (int j = 0; j < tp.freq(); j++) {
				int p = tp.nextPosition();
				hit[p - minpos] = i + 1;
			}
		}
		score = editDistance(hit, query);
		return score;
	}

	private class TraceBack {
		public int i;
		public int j;

		public TraceBack(int i, int j) {
			this.i = i;
			this.j = j;
		}
	}

	// TODO: JEH this LCS algorithm could be optimized in both time and space complexity
	private float editDistance(int[] seq1, int[] seq2) {
		int d = 1;
		int n = seq1.length, m = seq2.length;
		int[][] F = new int[n + 1][m + 1]; // acummulate scores
		TraceBack[][] T = new TraceBack[n + 1][m + 1]; // path traceback
		int s = 0;
		int maxi = n, maxj = m;
		int maxval = Integer.MIN_VALUE;
		TraceBack start;

		for (int i = 1; i <= n; i++) {
			for (int j = 1; j <= m; j++) {
				s = 0;
				if (seq1[i - 1] == seq2[j - 1])
					s = 2;

				int val = max(0, F[i - 1][j - 1] + s, F[i - 1][j] - d, F[i][j - 1] - d);
				F[i][j] = val;
				if (val == 0)
					T[i][j] = null;
				else if (val == F[i - 1][j - 1] + s)
					T[i][j] = new TraceBack(i - 1, j - 1);
				else if (val == F[i - 1][j] - d)
					T[i][j] = new TraceBack(i - 1, j);
				else if (val == F[i][j - 1] - d)
					T[i][j] = new TraceBack(i, j - 1);
				if (val > maxval) {
					maxval = val;
					maxi = i;
					maxj = j;
				}
			}
		}
		start = new TraceBack(maxi, maxj);

		// retrace the optimal path and calculate score
		matches = 0;
		TraceBack tb = start;
		int i = tb.i;
		int j = tb.j;
		while ((tb = next(tb, T)) != null) {
			i = tb.i;
			j = tb.j;
			if (seq1[i] == seq2[j])
				matches++;
		}
		return (float) matches / (float) terms.size();
	}

	private TraceBack next(TraceBack tb, TraceBack[][] tba) {
		TraceBack tb2 = tb;
		return tba[tb2.i][tb2.j];
	}

	@Override
	public int advance(int target) throws IOException {
		if (target == NO_MORE_DOCS) {
			currentDoc = NO_MORE_DOCS;
			return NO_MORE_DOCS;
		}

		while ((currentDoc = nextDoc()) < target) {
		}

		return currentDoc;
	}

	@Override
	public int docID() {
		return currentDoc;
	}

	@Override
	public int nextDoc() throws IOException {
		if (!multiTermPositions.next()) {
			multiTermPositions.close();
			return NO_MORE_DOCS;
		}

		currentDoc = findNext();
		return currentDoc;
	}
}