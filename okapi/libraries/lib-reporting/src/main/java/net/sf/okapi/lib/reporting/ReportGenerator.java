/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.reporting;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.RegexUtil;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportGenerator {

	private static final String FIELD_REGEX = "\\[([A-Z_]+[0-9]*)\\]"; // Only uppercase A to Z optionally followed by numbers allowed in field names to not interfere with array[i], array[0], etc. of templates in Java and others
	private static final String FIELD_LEAD_REGEX = "([ \\t]*)\\[([A-Z_]+)\\]";
	private static final String TABLE_REGEX = "([ \\t]*)\\[(.*\\[.+\\][\\r\\n \\t,;]*)\\]";
	private static final String WS_REGEX = "[ \\t]+";
	private String template;
	private String lineBreak = "\n";
	
	private Pattern fieldPattern = RegexUtil.getPattern(FIELD_REGEX);
	private Pattern fieldLeadPattern = RegexUtil.getPattern(FIELD_LEAD_REGEX);
	private Pattern tablePattern = RegexUtil.getPattern(TABLE_REGEX);
	private Pattern wsPattern = RegexUtil.getPattern(WS_REGEX);
	
	private Hashtable<String, String> simpleFields = new Hashtable<String, String>();
	private Hashtable<String, String> simpleFieldLeads = new Hashtable<String, String>();
	private Hashtable<String, LinkedList<String>> multiFields = new Hashtable<String, LinkedList<String>>();
	private StringBuilder sb;
	private boolean multiItemReport;
	private boolean htmlReport;
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public ReportGenerator(String template) {
		super();
		setTemplate(template);				
		sb = new StringBuilder(); 
	}
	
	public ReportGenerator(InputStream templateStream) {
		this(StreamUtil.streamUtf8AsString(templateStream));
	}
	
	private boolean isSimpleField(String fieldName) {
		return simpleFields.containsKey(fieldName);
	}
	
	private boolean isMultiField(String fieldName) {
		return multiFields.containsKey(fieldName);
	}
	
	private boolean isCompoundField(String fieldName) {
		return isSimpleField(fieldName) && (fieldName.indexOf("[") != -1 && fieldName.indexOf("]") != -1);
	}
	
	public void setField(String fieldName, String value) {
		// Logging
		if (!"0".equals(value)) {
			sb.append(fieldName);
			sb.append(" = ");
			sb.append(value);
			sb.append("\n");
		}		
		
		if (isSimpleField(fieldName)) {
			simpleFields.put(fieldName, value);
		}			
		
		else if (isMultiField(fieldName)) {
			LinkedList<String> list = multiFields.get(fieldName);
			if (list == null)
				list = new LinkedList<String>();
			list.add(value);
		}
	}
	
	public void setField(String fieldName, long value) {
		setField(fieldName, Long.toString(value));
	}
	
	public void setField(String fieldName, double value) {
		DecimalFormat df = new DecimalFormat();
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		df.setDecimalFormatSymbols(dfs);
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(2);
		setField(fieldName, df.format(value));
	}
	
	public void setField(String fieldName, int value) {
		setField(fieldName, Integer.toString(value));
	}
	
	public void setField(String fieldName, boolean value) {
		setField(fieldName, Boolean.toString(value));
	}

	private void registerFields(String st, boolean isMultiple) {
		while (true) {
		    Matcher matcher = fieldPattern.matcher(st);
		    
		    if (matcher.find()) {			    
		        int start = matcher.start(0);
		        int end = matcher.end(0);
		        
		        int start2 = matcher.start(1);
		        int end2 = matcher.end(1);
		        
		        String field = st.substring(start, end);
		        String fieldName = st.substring(start2, end2);
		        
		        st = RegexUtil.replaceAll(st, RegexUtil.escape(field), 0, "");
		        registerField(fieldName, isMultiple);
		    }
		    else 
		    	break;
	    }
	}
	
	private void registerFields() {
		String st = template;
		
		while (true) {
		    Matcher matcher = tablePattern.matcher(st);
		    
		    if (matcher.find()) {			    
		        int start = matcher.start(0);
		        int end = matcher.end(0);
		        
		        int start1 = matcher.start(1);
		        int end1 = matcher.end(1);
		        
		        int start2 = matcher.start(2);
		        int end2 = matcher.end(2);
		        
		        String tableField = st.substring(start, end);
		        String table = st.substring(start1, end1) + st.substring(start2, end2);
		        
		        registerField(table, false);
		        st = RegexUtil.replaceAll(st, RegexUtil.escape(tableField), 0, ""); // remove table not to get in the way of simple fields search
		        registerFields(table, true);
		    }
		    else 
		    	break;
	    }
		registerFields(st, false); // register simple fields
		collectSimpleFieldLeads();
	}
	
	private void collectSimpleFieldLeads() {
		String st = template;
		while (true) {
		    Matcher matcher = fieldLeadPattern.matcher(st);
		    
		    if (matcher.find()) {			    
		        int start = matcher.start(0);
		        int end = matcher.end(0);
		        
		        int start1 = matcher.start(1);
		        int end1 = matcher.end(1);
		        
		        int start2 = matcher.start(2);
		        int end2 = matcher.end(2);
		        
		        String field = st.substring(start, end);
		        String fieldLead = st.substring(start1, end1);
		        String fieldName = st.substring(start2, end2);
		        
		        st = RegexUtil.replaceAll(st, RegexUtil.escape(field), 0, "");
		        if (simpleFields.containsKey(fieldName)) {
		        	simpleFieldLeads.put(fieldName, fieldLead);
		        }
		    }
		    else 
		    	break;
	    }
	}

	private void registerField(String fieldName, boolean isMultiple) {
		if (isMultiple)
			multiFields.put(fieldName, new LinkedList<String>());
		else
			simpleFields.put(fieldName, "");
	}

	public String generate() {		
		String st = template;
		
		// Table fields
		while (true) {
		    Matcher matcher = tablePattern.matcher(st);
		    
		    if (matcher.find()) {			    
		        int start = matcher.start(0);
		        int end = matcher.end(0);
		        
		        int start1 = matcher.start(1);
		        int end1 = matcher.end(1);
		        
		        int start2 = matcher.start(2);
		        int end2 = matcher.end(2);
		        
		        String field = st.substring(start, end);
		        String fieldName = st.substring(start1, end1) + st.substring(start2, end2);
		        String value = getData(fieldName);
		        if (! Util.isEmpty(value))
		        	st = RegexUtil.replaceAll(st, RegexUtil.escape(field), 0, value);
		        else {		        	
		        	st = RegexUtil.replaceAll(st, RegexUtil.escape(field), 0, st.substring(start1, end1) + 
		        			"{?" + st.substring(start2, end2) + "}");
		        }		        	
		    }
		    else 
		    	break;
	    }
		
		// Simple fields
		while (true) {
		    Matcher matcher = fieldPattern.matcher(st);
		    
		    if (matcher.find()) {			    
		        int start = matcher.start(0);
		        int end = matcher.end(0);
		        
		        int start2 = matcher.start(1);
		        int end2 = matcher.end(1);
		        
		        String field = st.substring(start, end);
		        String fieldName = st.substring(start2, end2);
		        String value = getData(fieldName);
		        if (!Util.isEmpty(value)) {
		        	value = value.replaceAll("\n", "\n" + simpleFieldLeads.get(fieldName));
		        	st = RegexUtil.replaceAll(st, RegexUtil.escape(field), 0, value);
		        }		        	
		        else {
		        	//break;
		        	st = RegexUtil.replaceAll(st, RegexUtil.escape(field), 0, "[?" + fieldName + "]");
		        }		        	
		    }
		    else 
		    	break;
	    }
		logger.debug(sb.toString());
		return st;
	}
	
	public void reset() {
		sb = new StringBuilder(); 
		for (String fieldName : simpleFields.keySet()) {
			simpleFields.put(fieldName, "");
		}
		
		for (String fieldName : multiFields.keySet()) {
			List<String> list = multiFields.get(fieldName);
			list.clear();
		}
	}
		
	private String getData(String fieldName) {		
		String st = "";
		
		// !!! The sequence is crucial
		if (isCompoundField(fieldName)) {
			st = buildTable(fieldName);
		}
		else if (simpleFields.containsKey(fieldName))
			st = simpleFields.get(fieldName);
				
		return st;
	}
	
	public String getField(String fieldName) {
		return simpleFields.get(fieldName);
	}
	
	public List<String> getMultiField(String multiFieldName) {
		return multiFields.get(multiFieldName);
	}
	
	public Set<String> getFieldNames() {
		return simpleFields.keySet();
	}
	
	public Set<String> getMultiFieldNames() {
		return multiFields.keySet();
	}
	
	private String buildTable(String tableTemplate) {
		String[] columnNames = extractFields(tableTemplate);
		//String[] columnValues = new String[columnNames.length];		
		List<List<String>> columns = new LinkedList<List<String>>();
		
		int numRows = 0; //Integer.MAX_VALUE;
		
		for (String columnName : columnNames) {
			List<String> column = multiFields.get(columnName);
			columns.add(column);
			numRows = Math.max(numRows, column.size());
		}
		
		// Make all columns equally sized (if a column has less values than expected (numRows), pad it with "[?columnName]")
		for (String columnName : columnNames) {
			List<String> column = multiFields.get(columnName);
			for (int i = column.size(); i < numRows; i++) {
				column.add("[?" + columnName + "]");
			} 
		}
		
		String st = "";
		
		if (numRows > 0) {
			st = fillRow(tableTemplate, 0);
			
			for (int i = 1; i < numRows; i++) {
				st += lineBreak + fillRow(tableTemplate, i);
			}
		}
				
		//return Util.trimEnd(st, " \t\n");
		return st;
	}

	private String fillRow(String tableTemplate, int rowIndex) {
		String st = tableTemplate;
		Matcher m = wsPattern.matcher(st);
		String leadingWhitespace = null;
		if (m.find()) { // Find the 1-st whitespace
			leadingWhitespace = m.group();
		}
		
		while (true) {
		    Matcher matcher = fieldPattern.matcher(st);
		    
		    if (matcher.find()) {			    
		        int start = matcher.start(0);
		        int end = matcher.end(0);
		        
		        int start2 = matcher.start(1);
		        int end2 = matcher.end(1);
		        
		        String field = st.substring(start, end);
		        String fieldName = st.substring(start2, end2);
		        
		        List<String> column = multiFields.get(fieldName);
		        st = RegexUtil.replaceAll(st, RegexUtil.escape(field), 0, column.get(rowIndex));
		        st = st.replaceAll("\n", "\n" + leadingWhitespace);
		    }
		    else 
		    	break;
	    }
		return st;
	}

	private String[] extractFields(String tableTemplate) {
		List<String> list = new LinkedList<String>();
		String st = tableTemplate;

		while (true) {
		    Matcher matcher = fieldPattern.matcher(st);
		    
		    if (matcher.find()) {			    
		        int start = matcher.start(0);
		        int end = matcher.end(0);
		        
		        int start2 = matcher.start(1);
		        int end2 = matcher.end(1);
		        
		        String field = st.substring(start, end);
		        String fieldName = st.substring(start2, end2);
		        
		        st = RegexUtil.replaceAll(st, RegexUtil.escape(field), 0, "");
		        list.add(fieldName);
		    }
		    else 
		    	break;
	    }
		return ListUtil.stringListAsArray(list);
	}

	public String getLineBreak() {
		return lineBreak;
	}

	public void setLineBreak(String lineBreak) {
		this.lineBreak = lineBreak;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
		if (Util.isEmpty(template)) {
			logger.warn("Scoping Report template is not set.");
			return;
		}
		registerFields();
		multiItemReport = multiFields.size() > 0; 
		htmlReport = template.indexOf("<html") != -1 && template.indexOf("<body") != -1;
		if (htmlReport)	setLineBreak("<br>");
	}
	
	/**
	 * Report contains at least one table with counts for individual project items
	 * @return
	 */
	protected boolean isMultiItemReport() {
		return multiItemReport;
	}

	protected boolean isHtmlReport() {
		return htmlReport;
	}
}
