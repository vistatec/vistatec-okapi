/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
-----------------------------------------------------------------------------
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

package net.sf.okapi.common.filterwriter;

import java.nio.charset.CharsetEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Namespaces;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IssueAnnotation;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.IWithAnnotations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for writing out ITS markup.
 */
public class ITSContent {

	/**
	 * Marker used in a skeleton part to indicate where standoff markup can be inserted when merging.
	 */
	public static final String STANDOFFMARKER = "$#@StandOff@#$";
	public static final String REF_PREFIX = "REF:";

	private static final Logger LOGGER = LoggerFactory.getLogger(ITSContent.class);

	private final String ITSXLF_PREF = " "+Namespaces.ITSXLF_NS_PREFIX+":";
	
	private CharsetEncoder encoder;
	private boolean isHTML5;
	private boolean isXLIFF;
	private List<GenericAnnotations> standoff;
	private String prefix;
	
	/**
	 * Indicates if a given language tag matches at least one item of a list of extended language ranges.
	 * <p>Based on the algorithm described at: http://tools.ietf.org/html/rfc4647#section-3.3.2
	 * @param langRanges the list of extended language ranges (with optional '!' prefix for 'exclude')
	 * @param langTag the language tag.
	 * @return true if the language tag results in inclusion, false if it results in exclusion.
	 */
	public static boolean isExtendedMatch (String langRanges,
		String langTag)
	{
		boolean result = true;
		if ( langRanges.startsWith("!") ) {
			langRanges = langRanges.substring(1);
			result = false;
		}
		for ( String langRange : ListUtil.stringAsArray(langRanges.toLowerCase()) ) {
			if ( doesLangTagMacthesLangRange(langRange, langTag) ) return result;
		}
		return !result;
	}

	/**
	 * Compares an extended language range with a language tag.
	 * @param langRange the extended language range.
	 * @param langTag the language tag.
	 * @return true if the language tag matches the language range.
	 */
	private static boolean doesLangTagMacthesLangRange (String langRange,
		String langTag)
	{
		String[] lrParts = langRange.toLowerCase().split("-", 0);
		String[] ltParts = langTag.toLowerCase().split("-", 0);
		
		int i = 0;
		int j = 0;
		String lrst = lrParts[i];
		String ltst = ltParts[j]; j++;
		if ( !lrst.equals(ltst) && !lrst.equals("*") ) return false;

		i = 1;
		j = 1;
		while ( i<lrParts.length) {
			lrst = lrParts[i];
			if ( lrst.equals("*") ) {
				i++;
				continue;
			}
			else if ( j >= ltParts.length ) {
				return false;
			}
			else if ( ltParts[j].equals(lrst) ) {
				i++; j++;
				continue;
			}
			else if ( ltParts[j].length() == 1 ) {
				return false;
			}
			else {
				j++;
			}
		}
		return true;
	}
	
	/**
	 * Extract a map of the ITS annotatorsRef value in its string format.
	 * @param data the string with the annotatorsRef value to process (can be null).
	 * @return a map with the keys being the data category names and the values each corresponding annotator's URI.
	 * (Can be empty but never returns null).
	 * @see #mapToAnnotatorsRef(Map)
	 */
	public static Map<String, String> annotatorsRefToMap (String data) {
		TreeMap<String, String> map = new TreeMap<String, String>();
		if ( Util.isEmpty(data) ) return map; // Empty map
		// Else: fill the map
		String[] list = data.split("\\s", 0);
		for ( String tmp : list ) {
			tmp = tmp.trim();
			if ( tmp.isEmpty() ) continue;
			int n = tmp.indexOf('|');
			if (( n == -1 ) || ( n == tmp.length()-1 )) {
				LOGGER.warn("Invalid annotatorsRef value '{}'", tmp);
				continue;
			}
			map.put(tmp.substring(0, n).toLowerCase(), tmp.substring(n+1));
		}
		return map;
	}
	
	/**
	 * Creates an ITS annotatorsRef value in a string form from a map.
	 * @param map the map holding the key/values pairs.
	 * @return A string in the ITS annotatorsRef format.
	 * @see #annotatorsRefToMap(String)
	 */
	public static String mapToAnnotatorsRef (Map<String, String> map) {
		if (( map == null ) || map.isEmpty() ) return null;
		StringBuilder sb = new StringBuilder();
		for ( String dc : map.keySet() ) {
			if ( sb.length() > 0 ) sb.append(' ');
			sb.append(dc+"|"+map.get(dc));
		}
		return sb.toString();
	}
	
	/**
	 * Gets the value of the ITS AnnotatorsRef information if it is present.
	 * That value is a list of annotators references.
	 * @param nameable the object to query.
	 * @return the annotatorsRef value for the given object or null if it's not present.
	 * @see #getAnnotatorRef(String, INameable)
	 */
	public static String getAnnotatorsRef (IWithAnnotations nameable) {
		GenericAnnotations anns = nameable.getAnnotation(GenericAnnotations.class);
		if ( anns != null ) {
			GenericAnnotation ann = anns.getFirstAnnotation(GenericAnnotationType.ANNOT);
			if ( ann != null ) {
				return ann.getString(GenericAnnotationType.ANNOT_VALUE);
			}
		}
		return null;
	}
	
	/**
	 * Gets the ITS annotator reference for a given data category on a given object. 
	 * @param dataCategory the data category identifier.
	 * @param nameable the nameable object where to look for the annotator.
	 * @return the value of the annotator reference for the given data category,
	 * or null if there is none declared.
	 * @see #getAnnotatorsRef(IWithAnnotations)
	 * @see #getAnnotatorRef(String, String)
	 */
	public static String getAnnotatorRef (String dataCategory,
		INameable nameable)
	{
		return getAnnotatorRef(dataCategory, getAnnotatorsRef(nameable));
	}
		
	/**
	 * Gets the ITS annotator reference for a given data category from a given annotators list. 
	 * @param dataCategory the data category identifier.
	 * @param pairs the list of annotator reference pairs to look up.
	 * @return the value of the annotator reference for the given data category,
	 * or null if there is none declared.
	 * @see #getAnnotatorRef(String, INameable)
	 */
	public static String getAnnotatorRef (String dataCategory,
		String pairs)
	{
		if ( pairs != null ) {
			int pos1 = pairs.indexOf(dataCategory);
			if ( pos1 != -1 ) {
				int pos2 = pairs.indexOf('|', pos1);
				if ( pos2 != -1 ) {
					int pos3 = pairs.indexOf(' ', pos2);
					if ( pos3 != -1 ) {
						return pairs.substring(pos2+1, pos3);
					}
					else {
						return pairs.substring(pos2+1);
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Updates a set of annotator references with new values.
	 * @param oldValues the set of old values (can be null).
	 * @param newValues the set of new values (can be null).
	 * @return the updated new set of values.
	 * If the given set of new values is null, the values returned are the old ones.
	 */
	public static String updateAnnotatorsRef (String oldValues,
		String newValues)
	{
		// if there are no new values provided, we don't change anything
		if ( newValues == null ) return oldValues;
		// Update each data category modified by this annotators reference attribute
		Map<String, String> oldMap = ITSContent.annotatorsRefToMap(oldValues);
		Map<String, String> newMap = ITSContent.annotatorsRefToMap(newValues);
		oldMap.putAll(newMap); // Add or override if needed
		return ITSContent.mapToAnnotatorsRef(oldMap);
	}
	
	/**
	 * Creates an ITSContent object with a given character set encoder.
	 * @param encoder the character set encoder to use (can be null for UTF-8)
	 * @param isHTML5 true to generate markup for HTML5, false for XML.
	 * @param isXLIFF true if the XML output is XLIFF, false for generic ITS.
	 * This parameter is ignored if <code>isHTML5</code> is true.
	 */
	public ITSContent (CharsetEncoder encoder,
		boolean isHTML5,
		boolean isXLIFF)
	{
		if ( isHTML5 && isXLIFF ) {
			throw new InvalidParameterException("You can have both isHTML5 and isXLIFF true at the same time");
		}
		this.encoder = encoder;
		this.isHTML5 = isHTML5;
		this.isXLIFF = isXLIFF;
		this.prefix = (isHTML5 ? "its-" : "its:");
	}

	/**
	 * Output the standoff markup for this object and clear it afterward.
	 * This is the same as calling <code>this.writeStandoffLQI(this.getStandoff());</code> then <code>this.clearStandoff()</code>
	 * @return the generated output.
	 */
	public String writeStandoffLQI () {
		String res = writeStandoffLQI(getStandoff());
		clearStandoff();
		return res;
	}
	
	/**
	 * Output all the Localization Quality issue annotation groups in a given list.
	 * The given standoff items are not cleared automatically. 
	 * @param annotations the list of annotation set to process.
	 * @return the generated output.
	 */
	public String writeStandoffLQI (List<GenericAnnotations> annotations) {
		if ( annotations == null ) return "";
		StringBuilder sb = new StringBuilder();
		for ( GenericAnnotations anns : annotations ) {
			// Check if we have something to output
			List<GenericAnnotation> list = anns.getAnnotations(GenericAnnotationType.LQI);
			if ( list.isEmpty() ) continue;
			// Output
			if ( isHTML5 ) {
				sb.append("<script id=\""+anns.getData()+"\" type=\"application/its+xml\">\n");
			}
			// List of issues
			sb.append("<its:locQualityIssues xmlns:its=\"" + Namespaces.ITS_NS_URI +"\"");
			// Add the okp namespace if needed
			for ( GenericAnnotation ann : list ) {
				if ( ann instanceof IssueAnnotation ) {
					sb.append(" xmlns:okp=\"" + Namespaces.NS_XLIFFOKAPI +"\"");
					break;
				}
			}
			// Version and ID
			sb.append(" version=\"2.0\" xml:id=\"" + anns.getData() + "\">\n");
			// then each annotation
			for ( GenericAnnotation ann : list ) {
				sb.append("<its:locQualityIssue");
				String strVal = ann.getString(GenericAnnotationType.LQI_COMMENT);
				if ( strVal != null ) {
					sb.append(" locQualityIssueComment=\"" + Util.escapeToXML(strVal, 3, false, encoder) + "\"");
				}
				Boolean booVal = ann.getBoolean(GenericAnnotationType.LQI_ENABLED);
				if (( booVal != null ) && !booVal ) {
					sb.append(" locQualityIssueEnabled=\"no\"");
				}
				strVal = ann.getString(GenericAnnotationType.LQI_PROFILEREF);
				if ( strVal != null ) {
					sb.append(" locQualityIssueProfileRef=\"" + Util.escapeToXML(strVal, 3, false, encoder) + "\"");
				}
				Double dblVal = ann.getDouble(GenericAnnotationType.LQI_SEVERITY);
				if ( dblVal != null ) {
					sb.append(" locQualityIssueSeverity=\"" + Util.formatDouble(dblVal) + "\"");
				}
				strVal = ann.getString(GenericAnnotationType.LQI_TYPE);
				if ( strVal != null ) {
					sb.append(" locQualityIssueType=\"" + strVal + "\"");
				}
				// Extended data
				if ( ann instanceof IssueAnnotation ) {
					IssueAnnotation iann = (IssueAnnotation)ann;
					sb.append(" okp:lqiType=\"" + iann.getIssueType().toString() + "\"");
					sb.append(String.format(" okp:lqiPos=\"%d %d %d %d\"",
						iann.getSourceStart(), iann.getSourceEnd(), iann.getTargetStart(), iann.getTargetEnd()));
					strVal = iann.getCodes();
					if ( strVal != null ) {
						sb.append(" okp:lqiCodes=\"" + Util.escapeToXML(strVal, 3, false, encoder) + "\"");
					}
					strVal = iann.getSegId();
					if ( strVal != null ) {
						sb.append(" okp:lqiSegId=\"" + Util.escapeToXML(strVal, 3, false, encoder) + "\"");
					}
				}
				else { // Annotation from original with just the start/end
					Integer ss = ann.getInteger(GenericAnnotationType.LQI_XSTART);
					Integer ts = ann.getInteger(GenericAnnotationType.LQI_XTRGSTART);
					if (( ss != null ) || ( ts != null )) {
						sb.append(String.format(" okp:lqiPos=\"%d %d %d %d\"",
							(ss==null ? 0 : ss), (ss==null ? -1 : ann.getInteger(GenericAnnotationType.LQI_XEND)),
							(ts==null ? 0 : ts), (ts==null ? -1 : ann.getInteger(GenericAnnotationType.LQI_XTRGEND))));
					}
				}
				// End
				sb.append("/>\n");
			}
			sb.append("</its:locQualityIssues>\n");
			if ( isHTML5 ) {
				sb.append("</script>\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Writes the data attributes for a given LQI annotation (not locQualityIssuesRef).
	 * @param ann the annotation to output.
	 * @return a string with the written attributes.
	 */
	public String writeAttributesLQI (GenericAnnotation ann) {
		StringBuilder output = new StringBuilder();
		printITSStringAttribute(ann.getString(GenericAnnotationType.LQI_COMMENT),
			(isHTML5 ? "loc-quality-issue-comment" : "locQualityIssueComment"), output);
		Boolean booVal = ann.getBoolean(GenericAnnotationType.LQI_ENABLED);
		if ((booVal != null) && !booVal) { // Output only non-default value (if one is set)
			printITSBooleanAttribute(booVal,
				(isHTML5 ? "loc-quality-issue-enabled" : "locQualityIssueEnabled"), output);
		}
		printITSStringAttribute(ann.getString(GenericAnnotationType.LQI_PROFILEREF),
			// Use -ref/Ref here because the value is always a reference (as opposed to locNote/locNoteRef)
			(isHTML5 ? "loc-quality-issue-profile-ref" : "locQualityIssueProfileRef"), output);
		printITSDoubleAttribute(ann.getDouble(GenericAnnotationType.LQI_SEVERITY),
			(isHTML5 ? "loc-quality-issue-severity" : "locQualityIssueSeverity"), output);
		printITSStringAttribute(ann.getString(GenericAnnotationType.LQI_TYPE),
			(isHTML5 ? "loc-quality-issue-type" : "locQualityIssueType"), output);

		// Extended data
		if (ann instanceof IssueAnnotation) {
			IssueAnnotation iann = (IssueAnnotation) ann;
			output.append(" okp:lqiType=\"" + iann.getIssueType().toString() + "\"");
			output.append(String.format(" okp:lqiPos=\"%d %d %d %d\"",
				iann.getSourceStart(), iann.getSourceEnd(), iann.getTargetStart(), iann.getTargetEnd()));
			String strVal = iann.getCodes();
			if (strVal != null) {
				output.append(" okp:lqiCodes=\"" + Util.escapeToXML(strVal, 3, false, encoder) + "\"");
			}
			strVal = iann.getSegId();
			if (strVal != null) {
				output.append(" okp:lqiSegId=\"" + Util.escapeToXML(strVal, 3, false, encoder) + "\"");
			}
		}
		else { // Annotation from original with just the start/end
			Integer ss = ann.getInteger(GenericAnnotationType.LQI_XSTART);
			Integer ts = ann.getInteger(GenericAnnotationType.LQI_XTRGSTART);
			if ((ss != null) || (ts != null)) {
				output.append(String.format(" okp:lqiPos=\"%d %d %d %d\"",
					(ss == null ? 0 : ss), (ss == null ? -1 : ann.getInteger(GenericAnnotationType.LQI_XEND)),
					(ts == null ? 0 : ts), (ts == null ? -1 : ann.getInteger(GenericAnnotationType.LQI_XTRGEND))));
			}
		}

		return output.toString();
	}

	public String writeStandoffProvenance (List<GenericAnnotations> annotations) {
		StringBuilder sb = new StringBuilder();
		for ( GenericAnnotations anns : annotations ) {
			// Check if we have something to output
			List<GenericAnnotation> list = anns.getAnnotations(GenericAnnotationType.PROV);
			if ( list.isEmpty() ) continue;
			// Output
			if ( isHTML5 ) {
				sb.append("<script id=\""+anns.getData()+"\" type=\"application/its+xml\">");
			}
			sb.append("<its:provenanceRecords xmlns:its=\""+Namespaces.ITS_NS_URI+"\" version=\"2.0\" ");
			sb.append("xml:id=\""+anns.getData()+"\">");
			for ( GenericAnnotation ann : list ) {
				sb.append("<its:provenanceRecord");
				String strVal = ann.getString(GenericAnnotationType.PROV_PERSON);
				if ( strVal != null ) sb.append(outputRefOrValue(" person", strVal, false));
				strVal = ann.getString(GenericAnnotationType.PROV_ORG);
				if ( strVal != null ) sb.append(outputRefOrValue(" org", strVal, false));
				strVal = ann.getString(GenericAnnotationType.PROV_TOOL);
				if ( strVal != null ) sb.append(outputRefOrValue(" tool", strVal, false));
				strVal = ann.getString(GenericAnnotationType.PROV_REVPERSON);
				if ( strVal != null ) sb.append(outputRefOrValue(" revPerson", strVal, false));
				strVal = ann.getString(GenericAnnotationType.PROV_REVORG);
				if ( strVal != null ) sb.append(outputRefOrValue(" revOrg", strVal, false));
				strVal = ann.getString(GenericAnnotationType.PROV_REVTOOL);
				if ( strVal != null ) sb.append(outputRefOrValue(" revTool", strVal, false));
				strVal = ann.getString(GenericAnnotationType.PROV_PROVREF);
				if ( strVal != null ) sb.append(" provRef=\"" + Util.escapeToXML(strVal, 3, false, encoder) + "\"");
				sb.append("/>");
			}
			sb.append("</its:provenanceRecords>");
			if ( isHTML5 ) {
				sb.append("</script>");
			}
		}
		return sb.toString();
	}

	public String writeAttributeProvenance(GenericAnnotation ann) {
		StringBuilder output = new StringBuilder();
		writeProvAttrHelper("org", ann.getString(GenericAnnotationType.PROV_ORG), output);
		writeProvAttrHelper("person", ann.getString(GenericAnnotationType.PROV_PERSON), output);
		writeProvAttrHelper("tool", ann.getString(GenericAnnotationType.PROV_TOOL), output);
		writeProvAttrHelper((isHTML5 ? "rev-org" : "revOrg"), ann.getString(GenericAnnotationType.PROV_REVORG), output);
		writeProvAttrHelper((isHTML5 ? "rev-person" : "revPerson"), ann.getString(GenericAnnotationType.PROV_REVPERSON), output);
		writeProvAttrHelper((isHTML5 ? "rev-tool" : "revTool"), ann.getString(GenericAnnotationType.PROV_REVTOOL), output);
		writeProvAttrHelper((isHTML5 ? "prov-ref" : "provRef"), ann.getString(GenericAnnotationType.PROV_PROVREF), output);
		return output.toString();
	}

	private void writeProvAttrHelper(String attrName, String attrValue, StringBuilder output) {
		if (attrName != null && attrValue != null) {
			if (attrValue.startsWith(GenericAnnotationType.REF_PREFIX)) {
				attrValue = attrValue.substring(GenericAnnotationType.REF_PREFIX.length());
				attrName += "Ref";
			}
			printITSStringAttribute(attrValue, attrName, output);
		}
	}

	private String outputRefOrValue (String partialName,
		String value,
		boolean useHTML5Notation)
	{
		if ( value.startsWith(GenericAnnotationType.REF_PREFIX) ) {
			value = value.substring(GenericAnnotationType.REF_PREFIX.length());
			partialName = partialName + (useHTML5Notation ? "-ref" : "Ref");
		}
		return partialName+"=\""+Util.escapeToXML(value, 3, false, encoder)+"\"";
	}

	/**
	 * Generates the markup for the ITS attributes for a given annotation set.
	 * @param anns the annotations set (can be null).
	 * @param output the buffer where to append the output.
	 * @param inline true if the element is an inline element.
	 * @param mrk true if the element is an XLIFF mrk element.
	 * @param mtypeNeeded true if the mtype attribute should be output (if needed), false to not output it.
	 * @param trgLocId target locale (can be null). This is use with mrk and Locale Filter.
	 */
	public void outputAnnotations (GenericAnnotations anns,
		StringBuilder output,
		boolean inline,
		boolean mrk,
		boolean mtypeNeeded,
		LocaleId trgLocId)
	{
		if ( anns == null ) return;
		
		boolean hasTerm = false;
		boolean hasTA = false;
		boolean hasProtected = false;
		boolean protectedValue = false;
		for ( GenericAnnotation ann : anns ) {
			// Text Analysis
			if ( ann.getType().equals(GenericAnnotationType.TA) ) {
				hasTA = true;
				printITSStringAttribute(ann.getString(GenericAnnotationType.TA_CLASS),
					(isHTML5 ? "ta-class" : "taClass"), output);
				printITSDoubleAttribute(ann.getDouble(GenericAnnotationType.TA_CONFIDENCE),
					(isHTML5 ? "ta-confidence" : "taConfidence"), output);
				printITSStringAttribute(ann.getString(GenericAnnotationType.TA_IDENT),
					(isHTML5 ? "ta-ident" : "taIdent"), output);
				printITSStringAttribute(ann.getString(GenericAnnotationType.TA_SOURCE),
					(isHTML5 ? "ta-source" : "taSource"), output);
			}
			
			// Terminology
			else if ( ann.getType().equals(GenericAnnotationType.TERM) ) {
				hasTerm = true;
				if ( !(isXLIFF && inline) ) {
					printITSBooleanAttribute(true, "term", output);
				}
				if ( isXLIFF ) {
					printITSExtDoubleAttribute(ann.getDouble(GenericAnnotationType.TERM_CONFIDENCE),
						"termConfidence", output);
				}
				else {
					printITSDoubleAttribute(ann.getDouble(GenericAnnotationType.TERM_CONFIDENCE),
						(isHTML5 ? "term-confidence" : "termConfidence"), output);
				}
				// If it's not a Ref info, we must used a user-define attribute because there is no local ITS termInfo attribute
				String value = ann.getString(GenericAnnotationType.TERM_INFO);
				if ( value != null ) {
					String ref = "";
					if ( value.startsWith(REF_PREFIX) ) {
						ref = (isHTML5 ? "-ref" : "Ref");
						value = value.substring(REF_PREFIX.length());
					}
					if ( isXLIFF ) {
						printITSExtStringAttribute(value, "termInfo"+ref, output);
					}
					else {
						printITSStringAttribute(value, " "+prefix+(isHTML5 ? "term-info" : "termInfo")+ref, output);
					}
				}
			}
			
			// Allowed Characters
			else if ( ann.getType().equals(GenericAnnotationType.ALLOWEDCHARS) ) {
				printITSStringAttribute(ann.getString(GenericAnnotationType.ALLOWEDCHARS_VALUE),
					(isHTML5 ? "allowed-characters" : "allowedCharacters"), output);
			}
			
			// Storage Size
			else if ( ann.getType().equals(GenericAnnotationType.STORAGESIZE) ) {
				printITSIntegerAttribute(ann.getInteger(GenericAnnotationType.STORAGESIZE_SIZE),
					(isHTML5 ? "storage-size" : "storageSize"), output);
				String tmp = ann.getString(GenericAnnotationType.STORAGESIZE_ENCODING);
				// Handles null for default "UTF-8"
				if (( tmp != null ) && !tmp.equals("UTF-8") ) printITSStringAttribute(tmp,
					(isHTML5 ? "storage-encoding" : "storageEncoding"), output);
				tmp = ann.getString(GenericAnnotationType.STORAGESIZE_LINEBREAK);
				// Handles null as default "lf"
				if (( tmp != null ) && !tmp.equals("lf") ) printITSStringAttribute(tmp,
					(isHTML5 ? "storage-linebreak" : "storageLinebreak"), output);
			}
			
			// Translate
			else if ( ann.getType().equals(GenericAnnotationType.TRANSLATE) ) {
				hasProtected = true;
				protectedValue = !ann.getBoolean(GenericAnnotationType.TRANSLATE_VALUE);
			}
			
			// Localization Note
			else if ( ann.getType().equals(GenericAnnotationType.LOCNOTE) ) {
				if ( mrk ) {
					// in mrk element
					output.append(" comment=\""+Util.escapeToXML(ann.getString(GenericAnnotationType.LOCNOTE_VALUE), 3, false, encoder)+"\"");
					printITSExtStringAttribute(ann.getString(GenericAnnotationType.LOCNOTE_TYPE), "locNoteType", output);
				}
				else {
					printITSStringAttribute(ann.getString(GenericAnnotationType.LOCNOTE_VALUE),
						(isHTML5 ? "loc-note" : "locNote"), output);
					printITSIntegerAttribute(ann.getInteger(GenericAnnotationType.LOCNOTE),
						(isHTML5 ? "loc-note" : "locNote"), output);
					printITSStringAttribute(ann.getString(GenericAnnotationType.LOCNOTE_TYPE),
						(isHTML5 ? "loc-note-type" : "locNoteType"), output);
				}
			}
			
			// Language Information
			else if ( ann.getType().equals(GenericAnnotationType.LANG) ) {
				if ( mrk ) {
					output.append(" xml:lang=\""+ann.getString(GenericAnnotationType.LANG_VALUE)+"\"");
				}
			}
			
			// Preserve Space
			else if ( ann.getType().equals(GenericAnnotationType.PRESERVEWS) ) {
				if ( mrk ) {
					output.append(" xml:space=\""+ann.getString(GenericAnnotationType.PRESERVEWS_INFO)+"\"");
				}
			}

			// Domain
			else if ( ann.getType().equals(GenericAnnotationType.DOMAIN) ) {
				printITSExtStringAttribute(ann.getString(GenericAnnotationType.DOMAIN_VALUE), "domains", output);
			}
			
			// External Resource
			else if ( ann.getType().equals(GenericAnnotationType.EXTERNALRES) ) {
				printITSExtStringAttribute(ann.getString(GenericAnnotationType.EXTERNALRES_VALUE), "externalResourceRef", output);
			}
			
			// Localization Quality issue
			else if ( ann.getType().equals(GenericAnnotationType.LQI) ) {
				continue; // LQI are dealt with separately
			}
			
			// Provenance
			else if ( ann.getType().equals(GenericAnnotationType.PROV) ) {
				continue; // Provenance are dealt with separately
			}
			
			// Locale Filter
			else if ( ann.getType().equals(GenericAnnotationType.LOCFILTER) ) {
				String value = ann.getString(GenericAnnotationType.LOCFILTER_VALUE);
				// Check if we need to protect or not
				if ( trgLocId != null ) {
					hasProtected = true;
					protectedValue = !ITSContent.isExtendedMatch(value, trgLocId.toBCP47());
				}
				boolean exclude = false;
				if ( value.charAt(0) == '!' ) {
					exclude = true;
					value = value.substring(1);
				}
				printITSStringAttribute(value, (isHTML5 ? "locale-filter-list" : "localeFilterList"), output);
				if ( exclude ) {
					printITSStringAttribute("exclude", (isHTML5 ? "locale-filter-type" : "localeFilterType"), output);
				}
			}

			// MT Confidence
			else if ( ann.getType().equals(GenericAnnotationType.MTCONFIDENCE) ) {
				printITSDoubleAttribute(ann.getDouble(GenericAnnotationType.MTCONFIDENCE_VALUE),
					(isHTML5 ? "mt-confidence" : "mtConfidence"), output);
			}

			// Localization quality rating
			else if ( ann.getType().equals(GenericAnnotationType.LQR) ) {
				Double val1 = ann.getDouble(GenericAnnotationType.LQR_SCORE);
				if ( val1 != null ) {
					printITSDoubleAttribute(val1,
						(isHTML5 ? "loc-quality-rating-score" : "locQualityRatingScore"), output);
					printITSDoubleAttribute(ann.getDouble(GenericAnnotationType.LQR_SCORETHRESHOLD),
						(isHTML5 ? "loc-quality-rating-score-threshold" : "locQualityRatingScoreThreshold"), output);
				}
				else {
					Integer val2 = ann.getInteger(GenericAnnotationType.LQR_VOTE);
					if ( val2 != null ) {
						printITSIntegerAttribute(val2,
							(isHTML5 ? "loc-quality-rating-vote" : "locQualityRatingVote"), output);
						printITSIntegerAttribute(ann.getInteger(GenericAnnotationType.LQR_VOTETHRESHOLD),
							(isHTML5 ? "loc-quality-rating-vote-threshold" : "locQualityRatingVoteThreshold"), output);
					}
				}
				printITSStringAttribute(ann.getString(GenericAnnotationType.LQR_PROFILEREF),
					// Use -ref/Ref here because the info is always a reference (as oppose to locNote/locNoteRef)
					(isHTML5 ? "loc-quality-rating-profile-ref" : "locQualityRatingProfileRef"), output);
			}
		}
			
		// Deal with LQI information
		List<GenericAnnotation> list = anns.getAnnotations(GenericAnnotationType.LQI);
		if ( list.size() == 1 ) {
			output.append(writeAttributesLQI(list.get(0)));
		}
		else if ( list.size() > 1 ) {
			// If there are 2 or more: the items need to be output as standoff markup.
			// Inside a ,script> at the end of the document.
			String refId = anns.getData(); // ID to use should already be set
			if ( refId == null ) { // But check and possibly fix anyway
				anns.setData(Util.makeId(UUID.randomUUID().toString()));
				refId = anns.getData();
			}
			if ( isHTML5 ) output.append(" its-loc-quality-issues-ref=\"#"+refId+"\"");
			else output.append(" its:locQualityIssuesRef=\"#"+refId+"\"");
			// Create a standoff list and copy the items
			GenericAnnotations newSet = new GenericAnnotations();
			newSet.setData(refId);
			newSet.addAll(list);
			if ( standoff == null ) standoff = new ArrayList<GenericAnnotations>();
			standoff.add(newSet);
		}

		// Deal with Provenance information
		list = anns.getAnnotations(GenericAnnotationType.PROV);
		if ( list.size() == 1 ) {
			output.append(writeAttributeProvenance(list.get(0)));
		}
		else if ( list.size() > 0 ) { // For now all as standoff
			// If there are 2 or more: the items need to be output as standoff markup.
			// Inside a ,script> at the end of the document.
			String refId = anns.getData(); // ID to use should already be set
			if ( refId == null ) { // But check and possibly fix anyway
				anns.setData(Util.makeId(UUID.randomUUID().toString()));
				refId = anns.getData();
			}
			if ( isHTML5 ) output.append(" its-provenance-records-ref=\"#"+refId+"\"");
			else output.append(" its:provenanceRecordsRef=\"#"+refId+"\"");
			GenericAnnotations newSet = new GenericAnnotations();
			newSet.setData(refId);
			newSet.addAll(list);
			if ( standoff == null ) standoff = new ArrayList<GenericAnnotations>();
			standoff.add(newSet);
		}
		
		// Output mtype if needed
		if ( mrk && mtypeNeeded ) {
			String value = "x-its";
			if ( hasTerm ) value = "term";
			else if ( hasTA ) value = "phrase";
			else if ( hasProtected ) value = (protectedValue ? "protected" : "x-its-translate-yes");
			output.append(" mtype=\"" + value + "\"");
		}
	}
	
	/**
	 * Gets the current standoff markup.
	 * @return the current standoff markup (can be null)
	 */
	public List<GenericAnnotations> getStandoff () {
		return standoff;
	}
	
	/**
	 * Indicates if this object has at least standoff item.
	 * @return true if this object has at least standoff item, false otherwise.
	 */
	public boolean hasStandoff () {
		return !Util.isEmpty(standoff);
	}
	
	/**
	 * Clears the standoff markup.
	 */
	public void clearStandoff () {
		standoff = null;
	}

	private void printITSStringAttribute (String value,
		String attrName,
		StringBuilder output)
	{
		if ( value != null ) {
			String ref = "";
			if ( value.startsWith(REF_PREFIX) ) {
				ref = (isHTML5 ? "-ref" : "Ref");
				value = value.substring(REF_PREFIX.length());
			}
			output.append(" "+prefix+attrName+ref+"=\""+Util.escapeToXML(value, 3, false, encoder)+"\"");
		}
	}

	private void printITSExtStringAttribute (String value,
		String attrName,
		StringBuilder output)
	{
		if ( value != null ) {
			output.append((isHTML5 ? " data-" : ITSXLF_PREF)+attrName+"=\""+Util.escapeToXML(value, 3, false, encoder)+"\"");
		}
	}

	private void printITSDoubleAttribute (Double value,
		String attrName,
		StringBuilder output)
	{
		if ( value != null ) {
			output.append(" "+prefix+attrName+"=\""+Util.formatDouble(value)+"\"");
		}
	}

	private void printITSExtDoubleAttribute (Double value,
		String attrName,
		StringBuilder output)
	{
		if ( value != null ) {
			output.append((isHTML5 ? " data-" : ITSXLF_PREF)+attrName+"=\""+Util.formatDouble(value)+"\"");
		}
	}

	private void printITSIntegerAttribute (Integer value,
		String attrName,
		StringBuilder output)
	{
		if ( value != null ) {
			output.append(" "+prefix+attrName+"=\""+value+"\"");
		}
	}

	private void printITSBooleanAttribute (Boolean value,
		String attrName,
		StringBuilder output)
	{
		if ( value != null ) {
			output.append(" "+prefix+attrName+"=\""+(value ? "yes" : "no")+"\"");
		}
	}

	public CharsetEncoder getEncoder() {
		return encoder;
	}

	public boolean isHTML5() {
		return isHTML5;
	}

	public boolean isXLIFF() {
		return isXLIFF;
	}

}
