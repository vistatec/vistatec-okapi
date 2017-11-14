package net.sf.okapi.filters.doxygen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParameterExtractor {
	
	private DoxygenParameter paramData;
	
	private String param = "";
	private String remainder = "";
	private String frontWhitespace = "";
	
	private boolean hasParameter = false;
	
	private static final String NEXT_WORD = "^[^\\s\\[\\(]*[^\\s.,|/\\[\\]\\(\\)\\{\\}]+(?:\\([^\\s\\)]*\\))?";
	private static final Pattern NEXT_WORD_PATTERN = Pattern.compile(NEXT_WORD);
	private static final String NEXT_LINE = "^.*?(?:\\r?\\n|\\z)";
	private static final Pattern NEXT_LINE_PATTERN = Pattern.compile(NEXT_LINE);
	private static final String NEXT_PHRASE = "\"[^\"]*\"";
	private static final Pattern NEXT_PHRASE_PATTERN = Pattern.compile(NEXT_PHRASE);
	private static final String NEXT_PARAGRAPH = "^.*?(?=(?:\\s*?\\n\\s*){2,}|\\z)";
	private static final Pattern NEXT_PARAGRAPH_PATTERN = Pattern.compile(NEXT_PARAGRAPH, Pattern.DOTALL);
	private static final String FRONT_WHITESPACE = "^\\s*";
	public static final Pattern FRONT_WHITESPACE_PATTERN = Pattern.compile(FRONT_WHITESPACE);
	
	public ParameterExtractor (DoxygenParameter paramData, String text)
	{
		this.paramData = paramData;
		extract(text);
	}
	
	private void extract(String text)
	{		
		// First inspect leading whitespace for linebreaks.
		// If the parameter is required then we ignore a linebreak;
		// if the parameter is optional then we consider a linebreak to indicate
		// that no parameter is supplied.
		
		Matcher m = FRONT_WHITESPACE_PATTERN.matcher(text);
		m.find();
		
		frontWhitespace = m.group();
		String body = text.substring(m.end());
		
		if (frontWhitespace.contains("\n") && !paramData.isRequired()) {
			remainder = body;
			return;
		}
		
		// Extract text of the appropriate length.
		switch (paramData.length()) {
		case WORD:
			m = NEXT_WORD_PATTERN.matcher(body);
			break;
		case LINE:
			m = NEXT_LINE_PATTERN.matcher(body);
			break;
		case PHRASE:
			m = NEXT_PHRASE_PATTERN.matcher(body);
			break;
		case PARAGRAPH:
			m = NEXT_PARAGRAPH_PATTERN.matcher(body);
			break;
		}
				
		if (!m.find()) {
			remainder = text;
			return;
		}
		
		hasParameter = true;
		param = frontWhitespace + m.group();
		remainder = body.substring(m.end());
	}
	
	public String parameter()
	{
		return param;
	}
	
	public String remainder()
	{
		return remainder;
	}

	public boolean hasParameter() {
		return hasParameter;
	}

	public String frontWhitespace() {
		return frontWhitespace;
	}
}
