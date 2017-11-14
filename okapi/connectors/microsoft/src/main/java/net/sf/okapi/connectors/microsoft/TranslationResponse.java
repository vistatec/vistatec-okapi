package net.sf.okapi.connectors.microsoft;

class TranslationResponse {
	public int matchDegree;
	public int rating;
	public int combinedScore;
	public String sourceText;
	public String translatedText;

	TranslationResponse(String sourceText, String translatedText, int rating, int matchDegree) {
		this.sourceText = sourceText;
		this.translatedText = translatedText;
		this.rating = rating;
		this.matchDegree = matchDegree;
		this.combinedScore = calculateCombinedScore();
	}
	private int calculateCombinedScore() {
		int combinedScore = matchDegree;
		if ( combinedScore > 90 ) {
			combinedScore += (rating - 10);
			// Ideally we would want a composite value for the score
		}
		return combinedScore;
	}
}