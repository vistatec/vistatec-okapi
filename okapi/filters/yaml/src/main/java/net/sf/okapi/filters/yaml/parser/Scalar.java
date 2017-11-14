package net.sf.okapi.filters.yaml.parser;

import java.util.LinkedList;
import java.util.List;

public class Scalar {
	public int parentIndent = -1;
	public YamlScalarTypes type = YamlScalarTypes.UNKOWN;
	public YamlChompTypes chomp = YamlChompTypes.NONE;
	// either null of | or > or |+, etc.. for skeleton
	public String blockType;
	public String scalar = null;
	public QuotedScalar quoted = null;
	public IndentedBlock indentedBlock = null;
	public boolean flow = false;
	
	public void setChomp(String blockType) {
		this.blockType = blockType;
		if (blockType.contains("+")) {
			chomp = YamlChompTypes.PLUS;
		} else if (blockType.contains("-")) {
			chomp = YamlChompTypes.MINUS;
		} else {
			chomp = YamlChompTypes.NONE;
		}
	}
	
	public boolean isEmpty() {
		return (scalar == null && indentedBlock.isEmpty() && quoted.isEmpty()); 
	}
	
	public List<Line> getTranslatableStrings() {
		List<Line> lines = new LinkedList<>();
		
		// if quoted then no other types
		if (quoted != null && !quoted.isEmpty()) {
			return quoted.getTranslatableStrings();
		}
		
		if (indentedBlock == null || indentedBlock.isEmpty()) {
			lines.add(new Line(scalar, false));
			return lines;
		}
		
		// indented or additional indented lines
		if (indentedBlock != null && !indentedBlock.isEmpty()) {
			lines.addAll(indentedBlock.getTranslatableStrings());
		}
		
		// plain scalar add it to the following indented lines
		// add the needed whitespace
		if (scalar != null && !lines.isEmpty()) {
			// this should be a non-skeleton line			
			Line f = lines.get(0);
			assert(!f.isSkeleton);
			f.line = scalar + " " + f.line;
		}
		
		return lines;
	}
	
	/**
	 * get original string with full indentation for skeleton
	 * @return fully indented string
	 */
	public String getOriginalString() {
		StringBuilder s = new StringBuilder();
		
		// if quoted then no other types
		if (quoted != null) {
			return quoted.getOriginalString();
		}
		
		// otherwise we may have an unindented scalar
		if (scalar != null) {
			s.append(scalar);
		} 
		
		// or all indented or additional indented lines
		if (indentedBlock != null) {
			s.append(indentedBlock.getOriginalString());
		}
		return s.toString();
	}
}
