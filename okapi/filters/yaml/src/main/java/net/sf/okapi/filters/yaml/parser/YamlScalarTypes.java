package net.sf.okapi.filters.yaml.parser;

public enum YamlScalarTypes {
	PLAIN(""),
	SINGLE("'"),
	DOUBLE("\""),
	LITERAL(""),
	FOLDED(""),
	UNKOWN("");
	
	private String quoteChar;	
	private YamlScalarTypes(String quoteChar)
	{
		this.quoteChar = quoteChar;
	} 
	
	public String getQuoteChar()
	{
		return this.quoteChar;
	}	
}