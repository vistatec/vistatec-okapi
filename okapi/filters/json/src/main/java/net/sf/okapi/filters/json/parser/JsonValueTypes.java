package net.sf.okapi.filters.json.parser;

public enum JsonValueTypes {
	SINGLE_QUOTED_STRING("'"),
	DOUBLE_QUOTED_STRING("\""),
	SYMBOL(""),
	NUMBER(""),
	BOOLEAN(""),
	NULL(""),
	DEFAULT("");
	
	private String quoteChar;
	
	private JsonValueTypes(String quoteChar)
	{
		this.quoteChar = quoteChar;
	} 
	
	public String getQuoteChar()
	{
		return this.quoteChar;
	}
}
