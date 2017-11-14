package net.sf.okapi.filters.yaml.parser;

import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Util;

public class QuotedScalar {
	
	public YamlScalarTypes type;
	private String firstLine;
	public boolean firstLineHasContinuation = false;
	private List<Line> additionalLines;
	
	public QuotedScalar() {
		additionalLines = new LinkedList<>();
	}
	
	/**
	 * Set the first quoted line and configure SnakeYaml dump options
	 * WARNING: type variable must already be set.
	 * @param line
	 */
	public void setFirstLine(String line) {
		firstLine = line;
		// remove starting quote (" or ') left over from parsing process
		if (line.startsWith("\"") || line.startsWith("'")) {
			firstLine = line.substring(1);
		}
	}
	
	public void addLine(Line line) {
		if (!line.line.isEmpty()) { 
			additionalLines.add(line);
		}
	}
	
	public boolean isEmpty() {
		return firstLine.isEmpty();
	}
	
	/**
	 * Create a list of lines that represent TextUnits
	 * and skeleton (e.g., newlines) between them
	 * decode the strings based on the type
	 * @return list of translatable lines and skeleton.
	 */
	public List<Line> getTranslatableStrings() {
		if (isEmpty()) {
			return null;
		}
		
		List<Line> lines = new LinkedList<>();
		StringBuilder t = new StringBuilder();
		Line lastLine = null;
		
		if (!additionalLines.isEmpty()) {
			// add space except when continuation char was used
			if (!firstLineHasContinuation) { 
				firstLine = Util.trimEnd(firstLine, " ");
				firstLine = firstLine + " ";
			}
			lastLine = additionalLines.remove(additionalLines.size()-1);
		}
		
		String f = Line.decode(type.getQuoteChar() + firstLine + type.getQuoteChar());
		t.append(f);

		for (Line l : additionalLines) {
			if (l.isSkeleton) {
				// whitespace recreated by skeleton writer
				if (l.isThrowAway) continue;
				// finish any text lines preceding the skeleton (newline)
				if (t.length() > 0) {
					lines.add(new Line(t.toString(), false));
					t.setLength(0);
				}
				lines.add(l);
			} else {
				String a = Line.decode(type.getQuoteChar() + adjustWhiteSpace(l, false) + type.getQuoteChar());
				t.append(a);
			}
		}
		
		// add any remaining text and lastline
		t.append( Line.decode(type.getQuoteChar() + adjustWhiteSpace(lastLine, true) + type.getQuoteChar()));
		lines.add(new Line(t.toString(), false));
		
		// add back the last line in case we use this object again
		if (lastLine != null) {
			additionalLines.add(lastLine);
		}
		
		return lines;
	}
	
	/*
	 * Apply whitespace rules based on type. Take care to obey
	 * continuation chars in double quotes
	 */
	private String adjustWhiteSpace(Line line, boolean lastLine) {
		if (line == null) {
			return "";
		}
		
		String currentLine = line.line;
		
		switch (type) {
		case DOUBLE:
			if (lastLine) {
				if (line.startContinuation) {
					// preserve all whitespace
					return currentLine;
				} else {
					return Util.trimStart(currentLine, " ");
				}
			} else {
				if (!line.startContinuation)
					currentLine = Util.trimStart(currentLine, " ");
				if (!line.endContinuation) {
					currentLine = Util.trimEnd(currentLine, " ");
					currentLine += " ";
				}	
			}
			return currentLine;
		case SINGLE:
			if (lastLine) {
				currentLine = Util.trimStart(currentLine, " ");
			} else {
				currentLine = currentLine.trim();
				currentLine += " ";
			}
			return currentLine;
		default:
			return currentLine;
		}
	}
	
	/**
	 * get original string with full indentation for skeleton
	 * @return originl string
	 */
	public String getOriginalString() {
		StringBuilder s = new StringBuilder();
		s.append(type.getQuoteChar());
		s.append(firstLine);
		for (Line l : additionalLines) {
			s.append(l.line);
		}
		s.append(type.getQuoteChar());
		return s.toString();
	}

	@Override
	public String toString() {
		return firstLine + "," + additionalLines.toString();
	}
}
