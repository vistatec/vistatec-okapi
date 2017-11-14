package net.sf.okapi.filters.tmx;

import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.skeleton.GenericSkeleton;

public class TmxUtils {
	
	static void copyXMLNSToBuffer(StringBuilder sb,
			String prefix, String namespaceURI) {
		sb.append(" xmlns");
		if (prefix != null && !"".equals(prefix)) {
			sb.append(":");
			sb.append(prefix);
		}
		sb.append("=\"");
		sb.append(namespaceURI);
		sb.append("\"");
		
	}
	
	static void copyXMLNSToSkeleton(GenericSkeleton skel,
			String prefix, String namespaceURI) {
		StringBuilder sb = new StringBuilder();
		copyXMLNSToBuffer(sb, prefix, namespaceURI);
		skel.append(sb.toString());
	}

	/**
	 * Copy a parsed attribute to the provided buffer, escaping the value.  The
	 * attribute will be preceded by a whitespace character.
	 * 
	 * @param sb target buffer
	 * @param reader XMLStreamReader
	 * @param attrIndex the index of the attribute to copy from the reader
	 * @param lineBreak for escaping purposes
	 * @param escapeGT whether or not to escape GT 
	 */
	static void copyAttributeToBuffer(StringBuilder sb,
			XMLStreamReader reader, int attrIndex, String lineBreak,
			boolean escapeGT) {
		String prefix = reader.getAttributePrefix(attrIndex);
		sb.append(" ");
		if (prefix != null && !"".equals(prefix)) {
			sb.append(prefix).append(":");
		}
		sb.append(reader.getAttributeLocalName(attrIndex));
		sb.append("=\"");
		sb.append(Util.escapeToXML(reader.getAttributeValue(attrIndex).replace("\n", lineBreak), 3, escapeGT, null));
		sb.append('\"');
	}
	
	/**
	 * Copy a parsed attribute to the provided skeleton, escaping the value.  The
	 * attribute will be preceded by a whitespace character.
	 * 
	 * @param skel Skeleton to which to write the attribute
	 * @param reader XMLStreamReader
	 * @param attrIndex the index of the attribute to copy from the reader
	 * @param lineBreak for escaping purposes
	 * @param escapeGT whether or not to escape GT 
	 */
	static void copyAttributeToSkeleton(GenericSkeleton skel,
			XMLStreamReader reader, int attrIndex, String lineBreak,
			boolean escapeGT) {
		StringBuilder sb = new StringBuilder();
		copyAttributeToBuffer(sb, reader, attrIndex, lineBreak, escapeGT);
		skel.append(sb.toString());
	}
}
