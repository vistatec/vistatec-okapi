package net.sf.okapi.filters.yaml.parser;

import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Util;

public class IndentedBlock {
	public List<Line> lines;
	public int firstIndent = -1;
	
	// duplicated from the parent Scalar but may need standalone
	public YamlScalarTypes type = YamlScalarTypes.UNKOWN;
	
	public IndentedBlock() {
		lines = new LinkedList<>();
	}
	
	public boolean isEmpty() {
		return lines.isEmpty();
	}
	
	public void addLine(Line line) {
		if (line != null && !line.isEmpty()) {
			// always remember the first real text line indent
			if (!line.isSkeleton && firstIndent == -1) {
				firstIndent = line.indent;
			}
			
			lines.add(line);
		}
	}
	
	/**
	 * Create a list of lines that represent TextUnits
	 * and skeleton (e.g., newlines) between them
	 * decode the strings before saving
	 * @return list of translatable lines and skeleton.
	 */
	public List<Line> getTranslatableStrings() {
		if (lines.isEmpty()) {
			return null;
		}
		
		List<Line> indentedLines = new LinkedList<>();
		List<Line> linesWithoutThroway = new LinkedList<>();
		StringBuilder t = new StringBuilder();
		
		// copy lines that are not "throw away". Means whitespace unimportant
		// to translation. LITERAL type keeps everything
		for (Line l : lines) {
			if (type == YamlScalarTypes.LITERAL || !l.isThrowAway) linesWithoutThroway.add(l);
		}
		
		// special handling for last line (it can never be skeleton)
		Line lastLine = linesWithoutThroway.remove(linesWithoutThroway.size()-1);
		String last = adjustWhiteSpace(lastLine.line, lastLine.indent-firstIndent, true, null);
		
		int i = -1;
		int size = linesWithoutThroway.size();
		for (Line l : linesWithoutThroway) {
			i++;
			if (l.isSkeleton) {
				// finish any text lines preceding the skeleton (newline)
				if (t.length() > 0) {
					indentedLines.add(new Line(t.toString(), false));
					t.setLength(0);
				}
				indentedLines.add(l);
			} else {
				Line next = null;
				if (i < size-1) {
					next = linesWithoutThroway.get(i+1);
				}
				t.append(adjustWhiteSpace(l.line, l.indent-firstIndent, false, next));
			}
		}
		
		// add any remaining text and lastline
		indentedLines.add(new Line(t.toString()+last, false));
		
		return indentedLines;
	}
		
	/*
	 * Apply whitespace rules based on type. Ignore chomp and keep
	 * markers as the newlines would just be skeleton anyway if kept
	 */
	private String adjustWhiteSpace(String line, int indent, boolean lastLine, Line next) {
		switch (type) {
		case PLAIN:
			// newline is turned into single space
			// any trailing whitespace is trimmed
			// unless its the last line
			line = Util.trimEnd(line, "\r\n");
			if (lastLine) return line;
			// if the next line is a newline then don't auto add the space.
			if (next != null && next.isSkeleton) return line; 
			
			line += " ";
			return line;
		case FOLDED:
			// newline turned in space, but keep trailing whitespace
			// also prepend any extra whitespace because of extra indents
			StringBuilder t = new StringBuilder(line);
			t = new StringBuilder(line);
			
			// NOTE: for FOLDED whitespace is added as codes (includes indent), do not add them here
			// but depending on the application we leave this code 
			// commented out here per the YAML spec
			//t.insert(0, Line.prependWhitespace(indent));
			
			// if the next line is a newline then don't auto add the space.
			if (next != null && next.isSkeleton) return line; 
						
			if (!lastLine) t.append(" ");
			return t.toString();
		case LITERAL:
			// newline maintained, but keep trailing whitespace
			// also prepend any extra whitespace because of extra indents
			
			t = new StringBuilder(line);
			// NOTE: for LITERAL whitespace is in the skeleton, do not add them here
			// but depending on the application we leave this code 
			// commented out here per the YAML spec
			//t.insert(0, Line.prependWhitespace(indent));
			//if (!lastLine) t.append("\n");
			return t.toString();
		default:
			return line;
		}
	}
	
	/**
	 * get original string with full indentation and whitspace
	 * @return original string
	 */
	public String getOriginalString() {
		StringBuilder s = new StringBuilder();
		for (Line l : lines) {
			s.append(l.line);
		}
		return s.toString();
	}

	@Override
	public String toString() {
		return lines.toString();
	}
}
