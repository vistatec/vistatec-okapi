package net.sf.okapi.filters.doxygen;

import java.util.Map;

public class DoxygenParameter
{
	
	public enum ParameterLength {
		WORD, LINE, PHRASE, PARAGRAPH
	}
	
	private Map<String, Object> data;
	
	public DoxygenParameter(Map<String, Object> map)
	{
		data = map;
	}
	
	public boolean isTranslatable()
	{
		Boolean bool = (Boolean)data.get("translatable");
		return bool != null ? bool.booleanValue() : true;
	}
	
	public boolean isRequired()
	{
		Boolean bool = (Boolean)data.get("required");
		return bool != null ? bool.booleanValue() : true;
	}

	public ParameterLength length()
	{
		String str = (String)data.get("length");
		return str != null ? ParameterLength.valueOf(str) : ParameterLength.WORD;
	}
	
	public String getName()
	{
		return (String)data.get("name");
	}
	
}
