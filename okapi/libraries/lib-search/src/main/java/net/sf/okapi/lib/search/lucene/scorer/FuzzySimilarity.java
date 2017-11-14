package net.sf.okapi.lib.search.lucene.scorer;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.Similarity;

/**
 * @author HARGRAVEJE
 *
 */
public class FuzzySimilarity extends Similarity {
	private static final long serialVersionUID = -6299053650170316232L;
	
	@Override
	public float coord(int overlap, int maxOverlap) {
		return 1.0f;
	}

	@Override
	public float idf(int docFreq, int numDocs) {
		return 1.0f;
	}

	@Override
	public float queryNorm(float sumOfSquaredWeights) {
		return 1.0f;
	}

	@Override
	public float sloppyFreq(int distance) {
		return 1.0f;
	}

	@Override
	public float tf(float freq) {
		return 1.0f;
	}

	@Override
	public float computeNorm(String field, FieldInvertState state) {
		return 1.0f;
	}
}
