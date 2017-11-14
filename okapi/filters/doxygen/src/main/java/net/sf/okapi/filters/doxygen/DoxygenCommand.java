package net.sf.okapi.filters.doxygen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import net.sf.okapi.common.resource.TextFragment.TagType;

@SuppressWarnings({"rawtypes", "unchecked"})
public class DoxygenCommand implements Iterable<DoxygenParameter> {

	private Map<String, Object> data;
	private String name;
	private String rawCommand;
	private Parameters filterParams;
	
	
	public DoxygenCommand(Map<String, Object> data, String commandName,
			String rawCommand, Parameters filterParams) {
		
		this.data = data;
		this.name = commandName != null ? commandName : "";
		this.rawCommand = rawCommand != null ? rawCommand : "";
		this.filterParams = filterParams;
	}

	public boolean isInline()
	{
		Boolean inline = (Boolean) data.get("inline");
		return inline != null ? inline.booleanValue() : false;
	}
	
	public TagType getTagType()
	{
		
		// Always return CLOSING for closing HTML tags.
		if (isFinalHtmlTag()) return TagType.CLOSING;
		
		String type = (String) data.get("type");
		return type != null ? TagType.valueOf(type) : null;
	}
	
	public boolean hasParameters()
	{
		return !isFinalHtmlTag() && data.get("parameters") != null;
	}
	
	public boolean hasTranslatableParameters()
	{
		if (!hasParameters()) return false;
		
		for (DoxygenParameter param : this)
			if (param.isTranslatable()) return true;
		
		return false;
	}
	
	
	public ParameterIterator iterator()
	{
		Object params = data.get("parameters");
		
		if (isFinalHtmlTag()) params = null;
		
		return new ParameterIterator((ArrayList) params);
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getCanonicalName()
	{
		if (getTagType() == TagType.CLOSING) {
			String pair = getPair();
			if (pair != null) return pair;
		}
		
		return name;
	}
	
	public boolean hasPair()
	{
		return getPair() != null;
	}
	
	public String getPair()
	{
		if (isHtmlTag()) return name;
		
		return (String) data.get("pair");
	}

	public boolean isTranslatable()
	{
		Boolean translatable = (Boolean) data.get("translatable");
		
		return translatable != null ? translatable.booleanValue() : true;
	}
	
	public boolean isPreserveWhitespace()
	{
		Boolean preserve = (Boolean) data.get("preserve_whitespace");
		
		return preserve != null ? preserve.booleanValue() : filterParams.isPreserveWhitespace();
	}
	
	private boolean isHtmlTag()
	{
		return rawCommand.startsWith("<");
	}
	
	private boolean isFinalHtmlTag()
	{
		return rawCommand.startsWith("</");
	}
	
	public class ParameterIterator implements Iterator<DoxygenParameter> {
		
		private ArrayList<Map<String, Object>> data;
		private int i = 0;
		
		public ParameterIterator(ArrayList v)
		{
			data = v;
			i = 0;
		}
		
		@Override
		public boolean hasNext() {
			return data != null && i < data.size();
		}

		@Override
		public DoxygenParameter next() {
			return new DoxygenParameter((Map<String, Object>) data.get(i++));
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		public int getLength() {
			return data.size();
		}
	};

}
