package net.sf.okapi.tm.pensieve.server.model;

public class QueryResult {
	
	private float score;
	private String source;
	private String target;
	
	public QueryResult() {
	}
	
	public float getScore() {
		return score;
	}
	
	public void setScore(float score) {
		this.score = score;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getTarget() {
		return target;
	}
	
	public void setTarget(String target) {
		this.target = target;
	}
}
