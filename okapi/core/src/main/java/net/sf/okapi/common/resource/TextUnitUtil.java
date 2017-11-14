/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.RegexUtil;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;
import net.sf.okapi.common.skeleton.SkeletonUtil;

/**
 * Helper methods to manipulate {@link TextFragment}, {@link TextContainer}, and {@link TextUnit} objects.
 */
public class TextUnitUtil {

	// Segment markers
	private static final String SEG_START = "$seg_start$";
	private static final String SEG_END = "$seg_end$";
	
	// Text Part markers
	private static final String TP_START = "$tp_start$";
	private static final String TP_END = "$tp_end$";
	
	// Regex patterns for marker search
	private static final Pattern SEG_REGEX = Pattern.compile("\\[#\\$([A-Za-z_\\-0-9]+?)\\@\\%\\$seg_start\\$\\](.*?)\\[#\\$(\\1)\\@\\%\\$seg_end\\$\\]");
	private static final Pattern SEG_START_REGEX = Pattern.compile("\\[#\\$([A-Za-z_\\-0-9]+?)\\@\\%\\$seg_start\\$\\]");
	private static final Pattern SEG_END_REGEX = Pattern.compile("\\[#\\$([A-Za-z_\\-0-9]+?)\\@\\%\\$seg_end\\$\\]");
	
	private static final Pattern TP_REGEX = Pattern.compile("\\$tp_start\\$(.*?)\\$tp_end\\$");
	private static final Pattern TP_START_REGEX = Pattern.compile("\\$tp_start\\$");
	private static final Pattern TP_END_REGEX = Pattern.compile("\\$tp_end\\$");	
	
	private static final Pattern ANY_SEG_TP_REGEX = 
		Pattern.compile("\\[#\\$([A-Za-z_\\-0-9]+?)\\@\\%\\$seg_start\\$\\]|\\[#\\$([A-Za-z_\\-0-9]+?)\\@\\%\\$seg_end\\$\\]|\\$tp_start\\$|\\$tp_end\\$");
	
	private static final Pattern EXTERNAL_REF_REGEX = Pattern.compile("\\[\\#\\$((tu)|(sg))[0-9]+\\]");
	
	private static final char FOO = '\u0001';
	private static final Pattern PLAIN_TEXT_REGEX = Pattern.compile(String.format("[^%s]+", FOO));
	private static String testMarkersSt;
	
	/**
	 * Removes leading whitespaces from a given text fragment.
	 * 
	 * @param textFragment
	 *            the text fragment which leading whitespaces are to be removed.
	 */
	public static void trimLeading (TextFragment textFragment) {
		trimLeading(textFragment, null);
	}

	/**
	 * Copies the aligned inline codes of the source to the corresponding target codes.
	 * <b>WARNING: This method assumes that the source and target {@link TextFragment}'s codes are
	 * already id aligned.
	 * If they are not then call {@link TextFragment#alignCodeIds(TextFragment)} to align the codes
	 * based on their native data</b>
	 * <p>
	 * This method compares an original source with a new target, and transfer the codes of the
	 * original source at their equivalent places in the new target. The text of the new target is
	 * left untouched.
	 * </p>
	 * <p>
	 * If the option alwaysCopyCodes is false, the codes are copied only if it the original source
	 * codes have references or if the new target codes are empty.
	 * 
	 * @param oriSrc
	 *            the original source text fragment.
	 * @param newTrg
	 *            the new target text fragment (This is the fragment that will be adjusted).
	 * @param alwaysCopyCodes
	 *            indicates the adjustment of the codes is always done.
	 * @param addMissingCodes
	 *            indicates if codes that are in the original source but not in the new target
	 *            should be
	 *            automatically added at the end of the new target copy (even if they are removable)
	 *            if there are references in the original source and/or empty codes in the new
	 *            target.
	 * @param newSrc
	 *            the new source text fragment (Can be null). When available to speed up the inline
	 *            code
	 *            processing in some cases.
	 * @param parent
	 *            the parent text unit (Can be null. Used for error information only).
	 * @return the newTrg parameter with its inline codes adjusted
	 */
	public static TextFragment copySrcCodeDataToMatchingTrgCodes(TextFragment oriSrc,
			TextFragment newTrg,
			boolean alwaysCopyCodes,
			boolean addMissingCodes,
			TextFragment newSrc,
			ITextUnit parent)
	{
		Logger localLogger = LoggerFactory.getLogger(TextUnitUtil.class);
		// If it's the same object, there is no need to transfer
		if (newTrg == oriSrc) {
			return newTrg;
		}

		List<Code> newCodes = newTrg.getCodes();
		List<Code> oriCodes = oriSrc.getCodes();

		// If not alwaysCopyCodes: no reason to adjust anything: use the target as-is
		// This allows targets with only code differences to be used as-is
		boolean needAdjustment = false;
		if (!alwaysCopyCodes) {
			// Check if we need to adjust regardless of copying the codes or not
			// For example: when we have empty codes in the destination target
			for (Code code : newCodes) {
				if (!code.hasData()) {
					needAdjustment = true;
					break;
				}
			}
			// Or when the original has references
			if (!needAdjustment) {
				for (Code code : oriCodes) {
					if (code.hasReference()) {
						needAdjustment = true;
						break;
					}
				}
			}
			if (!needAdjustment) {
				return newTrg;
			}
		}
		// If both new and original have no code, return the new fragment
		if (!newTrg.hasCode() && !oriSrc.hasCode()) {
			return newTrg;
		}

		// If the codes of the original sources and the matched one are the same: no need to adjust
		if (newSrc != null) {
			if (!needAdjustment && oriCodes.toString().equals(newSrc.getCodes().toString())) {
				return newTrg;
			}
		}

		// Else: try to adjust
		int[] oriIndices = new int[oriCodes.size()];
		for (int i = 0; i < oriIndices.length; i++)
			oriIndices[i] = i;

		int done = 0;
		Code newCode, oriCode;

		for (int i = 0; i < newCodes.size(); i++) {
			newCode = newCodes.get(i);
			newCode.setOuterData(null); // Remove XLIFF outer codes if needed

			if (newCode.hasOnlyAnnotation()) {
				continue; // Skip annotation-only codes
			}

			// Get the data from the original code (match on id)
			oriCode = null;
			for (int j = 0; j < oriIndices.length; j++) {
				// Do we have the same id?
				if (oriCodes.get(j).getId() == newCode.getId()) {
					// Do we have the same tag type?
					if (oriCodes.get(j).getTagType() == newCode.getTagType()) {
						if (oriIndices[j] == -1) {
							// Was used already: this is a clone
							if (!oriCodes.get(j).isCloneable()) {
								String place = null;
								if (parent != null) {
									place = String.format(" (item id='%s', name='%s')",
											parent.getId(),
											(parent.getName() == null ? "" : parent.getName()));
								}
								// until we figure out duplicate id's in sdlxliff make this a debug (vs warn)
								localLogger.debug(String.format("The extra code id='%d' cannot be cloned.",
										newCode.getId()) + ((place == null) ? "" : place));
							}							
						}
					} else {
						// Same id but not the same tag-type
						// probably a ending matching on its starting
						continue; // Keep on searching
					}
					// Original code found, use it
					oriCode = oriCodes.get(j);
					oriIndices[j] = -1; // Mark it has used
					done++;
					break;
				}
			}

			if (oriCode == null) { // Not found in original (extra in target)
				if ((newCode.getData() == null) || (newCode.getData().length() == 0)) {
					// Leave it like that
					String place = null;
					if (parent != null) {
						place = String.format(" (item id='%s', name='%s')",
								parent.getId(), (parent.getName() == null ? "" : parent.getName()));
					}
					localLogger.warn(String.format(
							"The extra target code id='%d' does not have corresponding data.",
							newCode.getId()) + ((place == null) ? "" : place));
				}
				// Else: This is a new code: keep it
			} else { 
				// A code with same ID existed in the original
				// Get the data from the original
				newCode.setData(oriCode.getData());
				newCode.setOuterData(oriCode.getOuterData());
				newCode.setReferenceFlag(oriCode.hasReference());
				newCode.setType(oriCode.getType());
			}
		}

		// If needed, check for missing codes in new fragment
		if (oriCodes.size() > done) {
			// Any index > -1 in source means it was was deleted in target
			TextFragment leadingCodes = new TextFragment();
			for (int i = 0; i < oriIndices.length; i++) {
				if (oriIndices[i] != -1) {
					Code code = oriCodes.get(oriIndices[i]);
					if (addMissingCodes) {
						if (isLeadingCode(code, oriSrc)) {
							leadingCodes.append(code.clone());
						} else {
							newTrg.append(code.clone());
						}
					} else {
						if (!code.isDeleteable()) {
							String msg = String.format("The code id='%d' (%s) is missing in target.",
									code.getId(), code.getData());
							if (parent != null) {
								msg += String.format(" (item id='%s', name='%s')", parent.getId(),
										(parent.getName() == null ? "" : parent.getName()));
							}
							// we don't really handle deletable codes yet, but may still want to debug issues
							localLogger.debug(msg);
							localLogger.debug(String.format("Source='%s'\nTarget='%s'", oriSrc.toText(),
									newTrg.toText()));
						}
					}
				}
			}
			
			if (addMissingCodes) {
				newTrg.insert(0, leadingCodes, true);
			}
		}
		return newTrg;
	}

	private static boolean isLeadingCode(Code code, TextFragment oriSrc) {		
		int index = oriSrc.getCodes().indexOf(code);
		if (index == -1) return false;
		
		String ctext = oriSrc.getCodedText();
		int pos = ctext.indexOf(String.valueOf(TextFragment.toChar(index)), 0);
		if (pos == -1) return false;
		
		// Remove all codes from the beginning of the string before the pos and see if any text remains
		String substr = ctext.substring(0, pos - 1);
		substr = TextFragment.MARKERS_REGEX.matcher(substr).replaceAll("");
		return substr.trim().length() == 0;
	}
	
	/**
	 * Removes leading whitespaces from a given text fragment, puts removed whitespaces to the given skeleton.
	 * 
	 * @param textFragment
	 *            the text fragment which leading whitespaces are to be removed.
	 * @param skel
	 *            the skeleton to put the removed whitespaces.
	 */
	public static void trimLeading (TextFragment textFragment,
		GenericSkeleton skel)
	{
		if (textFragment == null)
			return;
		String st = textFragment.getCodedText();
		TextFragment skelTF;

		int pos = TextFragment.indexOfFirstNonWhitespace(st, 0, -1, false, false, false, true);
		if (pos == -1) { // Whole string is whitespaces
			skelTF = new TextFragment(st);
			textFragment.setCodedText("");
		} else {
			skelTF = textFragment.subSequence(0, pos);
			textFragment.setCodedText(st.substring(pos));
		}

		if (skel == null)
			return;
		if (skelTF == null)
			return;

		st = skelTF.toText();
		if (!Util.isEmpty(st))
			skel.append(st); // Codes get removed
	}

	/**
	 * Removes trailing whitespaces from a given text fragment.
	 * 
	 * @param textFragment
	 *            the text fragment which trailing whitespaces are to be removed.
	 */
	public static void trimTrailing (TextFragment textFragment) {
		trimTrailing(textFragment, null);
	}

	/**
	 * Removes trailing whitespaces from a given text fragment, puts removed whitespaces to the given skeleton.
	 * 
	 * @param textFragment
	 *            the text fragment which trailing whitespaces are to be removed.
	 * @param skel
	 *            the skeleton to put the removed whitespaces.
	 */
	public static void trimTrailing (TextFragment textFragment,
		GenericSkeleton skel)
	{
		if (textFragment == null)
			return;

		String st = textFragment.getCodedText();
		TextFragment skelTF;

		int pos = TextFragment.indexOfLastNonWhitespace(st, -1, 0, false, false, false, true);
		if (pos == -1) { // Whole string is whitespaces
			skelTF = new TextFragment(st);
			textFragment.setCodedText("");
		} else {
			skelTF = textFragment.subSequence(pos + 1, st.length());
			textFragment.setCodedText(st.substring(0, pos + 1));
		}

		if (skel == null)
			return;
		if (skelTF == null)
			return;

		st = skelTF.toText();
		if (!Util.isEmpty(st))
			skel.append(st); // Codes get removed
	}

	/**
	 * Indicates if a given text fragment ends with a given sub-string. <b>Trailing spaces are not counted</b>.
	 * 
	 * @param textFragment
	 *            the text fragment to examine.
	 * @param substr
	 *            the text to lookup.
	 * @return true if the given text fragment ends with the given sub-string.
	 */
	public static boolean endsWith (TextFragment textFragment,
		String substr)
	{
		if (textFragment == null)
			return false;
		if (Util.isEmpty(substr))
			return false;

		String st = textFragment.getCodedText();

		int pos = TextFragment.indexOfLastNonWhitespace(st, -1, 0, true, true, true, true);
		if (pos == -1)
			return false;

		return st.lastIndexOf(substr) == pos - substr.length() + 1;
	}

	/**
	 * Indicates if a given text unit resource is null, or its source part is null or empty.
	 * 
	 * @param textUnit
	 *            the text unit to check.
	 * @return true if the given text unit resource is null, or its source part is null or empty.
	 */
	public static boolean isEmpty (ITextUnit textUnit) {
		return ((textUnit == null) || textUnit.getSource().isEmpty());
	}

	/**
	 * Indicates if a given text unit resource is null, or its source part is null or empty. Whitespaces are not taken
	 * into account, e.g. if the text unit contains only whitespaces, it's considered empty.
	 * 
	 * @param textUnit
	 *            the text unit to check.
	 * @return true if the given text unit resource is null, or its source part is null or empty.
	 */
	public static boolean hasSource (ITextUnit textUnit) {
		return !isEmpty(textUnit, true);
	}

	/**
	 * Indicates if a given text unit resource is null, or its source part is null or empty. Whitespaces are not taken
	 * into account, if ignoreWS = true, e.g. if the text unit contains only whitespaces, it's considered empty.
	 * 
	 * @param textUnit
	 *            the text unit to check.
	 * @param ignoreWS
	 *            if true and the text unit contains only whitespaces, then the text unit is considered empty.
	 * @return true if the given text unit resource is null, or its source part is null or empty.
	 */
	public static boolean isEmpty (ITextUnit textUnit,
		boolean ignoreWS)
	{
		return ((textUnit == null) || Util.isEmpty(getSourceText(textUnit), ignoreWS));
	}

	/**
	 * Gets the coded text of the first part of the source of a given text unit resource.
	 * 
	 * @param textUnit
	 *            the text unit resource which source text should be returned.
	 * @return the source part of the given text unit resource.
	 */
	public static String getSourceText (ITextUnit textUnit) {
		// if ( textUnit == null ) return "";
		// return getCodedText(textUnit.getSourceContent());
		return textUnit.getSource().getFirstContent().getCodedText();
	}

	/**
	 * Gets the coded text of the first part of a source part of a given text unit resource.
	 * If removeCodes = false, and the text contains inline codes,
	 * then the codes will be removed.
	 * 
	 * @param textUnit
	 *            the text unit resource which source text should be returned.
	 * @param removeCodes
	 *            true if possible inline codes should be removed.
	 * @return the source part of the given text unit resource.
	 */
	public static String getSourceText (ITextUnit textUnit,
		boolean removeCodes)
	{
		if (textUnit == null)
			return "";
		if (removeCodes) {
			return getText(textUnit.getSource().getFirstContent());
		} else {
			return textUnit.getSource().getFirstContent().getCodedText();
		}
	}

	/**
	 * Gets text of the first part of the target of a given text unit resource in the given locale.
	 * 
	 * @param textUnit
	 *            the text unit resource which source text should be returned.
	 * @param locId
	 *            the locale the target part being sought.
	 * @return the target part of the given text unit resource in the given loacle, or an empty string if the text unit
	 *         doesn't contain one.
	 */
	public static String getTargetText (ITextUnit textUnit,
		LocaleId locId)
	{
		if (textUnit == null)
			return "";
		if (Util.isNullOrEmpty(locId))
			return "";

		return getCodedText(textUnit.getTarget(locId).getFirstContent());
	}

	/**
	 * Gets text of a given text fragment object possibly containing inline codes.
	 * 
	 * @param textFragment
	 *            the given text fragment object.
	 * @return the text of the given text fragment object possibly containing inline codes.
	 */
	public static String getCodedText (TextFragment textFragment) {
		if (textFragment == null)
			return "";
		return textFragment.getCodedText();
	}

	/**
	 * Extracts text from the given text fragment. Used to create a copy of the original string but without code
	 * markers. The original string is not stripped of code markers, and remains intact.
	 * 
	 * @param textFragment
	 *            TextFragment object with possible codes inside
	 * @param markerPositions
	 *            List to store initial positions of removed code markers. use null to not store the markers.
	 * @return The copy of the string, contained in TextFragment, but without code markers
	 */
	public static String getText (TextFragment textFragment,
		List<Integer> markerPositions)
	{
		if ( textFragment == null ) {
			return "";
		}

		String res = textFragment.getCodedText();
		if ( markerPositions != null ) {
			markerPositions.clear();
		}

		// No need to parse the text if there are no codes
		if ( !textFragment.hasCode() ) {
			return res;
		}
		
		// Collect marker positions & remove markers
		StringBuilder sb = new StringBuilder();
		int startPos = -1;
		for (int i = 0; i < res.length(); i++) {
			switch (res.charAt(i)) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				if (markerPositions != null) {
					markerPositions.add(i);
				}
				if (i > startPos && startPos >= 0) {
					sb.append(res.substring(startPos, i));
				}
				i += 1;
				startPos = -1;
				break;
			default:
				if (startPos < 0)
					startPos = i;
			}
		}

		if (startPos < 0 && sb.length() == 0) // Whole string 
			startPos = 0; 		
		else			
			if (startPos > -1 && startPos < res.length()) {
				sb.append(res.substring(startPos));
			}

		return sb.toString();
	}
	
	public static String printMarkerIndexes (TextFragment textFragment) {
		return (new GenericContent(textFragment)).printMarkerIndexes();
	}
	
	public static String printMarkers (TextFragment textFragment) {
		return (new GenericContent(textFragment)).toString();
	}

	/**
	 * Extracts text from the given text fragment. Used to create a copy of the original string but without code
	 * markers. The original string is not stripped of code markers, and remains intact.
	 * 
	 * @param textFragment
	 *            TextFragment object with possible codes inside
	 * @return The copy of the string, contained in TextFragment, but w/o code markers
	 */
	public static String getText (TextFragment textFragment) {
		return getText(textFragment, null);
	}

	/**
	 * Gets the last character of a given text fragment.
	 * 
	 * @param textFragment
	 *            the text fragment to examin.
	 * @return the last character of the given text fragment, or '\0'.
	 */
	public static char getLastChar (TextFragment textFragment) {
		if (textFragment == null)
			return '\0';

		String st = textFragment.getCodedText();

		int pos = TextFragment.indexOfLastNonWhitespace(st, -1, 0, true, true, true, true);
		if (pos == -1)
			return '\0';

		return st.charAt(pos);
	}

	/**
	 * Deletes the last non-whitespace and non-code character of a given text fragment.
	 * 
	 * @param textFragment
	 *            the text fragment to examine.
	 */
	public static void deleteLastChar (TextFragment textFragment) {
		if (textFragment == null)
			return;
		String st = textFragment.getCodedText();

		int pos = TextFragment.indexOfLastNonWhitespace(st, -1, 0, true, true, true, true);
		if (pos == -1)
			return;

		textFragment.remove(pos, pos + 1);
	}

	/**
	 * Returns the index (within a given text fragment object) of the rightmost occurrence of the specified substring.
	 * 
	 * @param textFragment
	 *            the text fragment to examine.
	 * @param findWhat
	 *            the substring to search for.
	 * @return if the string argument occurs one or more times as a substring within this object, then the index of the
	 *         first character of the last such substring is returned. If it does not occur as a substring,
	 *         <code>-1</code> is returned.
	 */
	public static int lastIndexOf (TextFragment textFragment,
		String findWhat)
	{
		if (textFragment == null)
			return -1;
		if (Util.isEmpty(findWhat))
			return -1;
		if (Util.isEmpty(textFragment.getCodedText()))
			return -1;

		return (textFragment.getCodedText()).lastIndexOf(findWhat);
	}

	/**
	 * Indicates if a given text fragment object is null, or the text it contains is null or empty.
	 * 
	 * @param textFragment
	 *            the text fragment to examine.
	 * @return true if the given text fragment object is null, or the text it contains is null or empty.
	 */
	public static boolean isEmpty (TextFragment textFragment) {
		return (textFragment == null || (textFragment != null && textFragment.isEmpty()));
	}

	/**
	 * Creates a new text unit resource based on a given text container object becoming the source part of the text
	 * unit.
	 * 
	 * @param source
	 *            the given text container becoming the source part of the text unit.
	 * @return a new text unit resource with the given text container object being its source part.
	 */
	public static ITextUnit buildTU (TextContainer source) {
		return buildTU(null, "", source, null, LocaleId.EMPTY, "");
	}

	/**
	 * Creates a new text unit resource based a given string becoming the source text of the text unit.
	 * 
	 * @param source
	 *            the given string becoming the source text of the text unit.
	 * @return a new text unit resource with the given string being its source text.
	 */
	public static ITextUnit buildTU (String source) {
		return buildTU(new TextContainer(source));
	}

	/**
	 * Creates a new text unit resource based on a given string becoming the source text of the text unit, and a
	 * skeleton string, which gets appended to the new text unit's skeleton.
	 * 
	 * @param srcPart
	 *            the given string becoming the source text of the created text unit.
	 * @param skelPart
	 *            the skeleton string appended to the new text unit's skeleton.
	 * @return a new text unit resource with the given string being its source text, and the skeleton string in the
	 *         skeleton.
	 */
	public static ITextUnit buildTU (String srcPart,
		String skelPart)
	{
		ITextUnit res = buildTU(srcPart);
		if (res == null)
			return null;

		GenericSkeleton skel = (GenericSkeleton) res.getSkeleton();
		if (skel == null)
			return null;

		skel.addContentPlaceholder(res);
		skel.append(skelPart);

		return res;
	}

	/**
	 * Creates a new text unit resource or updates the one passed as the parameter. You can use this method to create a
	 * new text unit or modify existing one (adding or modifying its fields' values).
	 * 
	 * @param textUnit
	 *            the text unit to be modified, or null to create a new text unit.
	 * @param name
	 *            name of the new text unit, or a new name for the existing one.
	 * @param source
	 *            the text container object becoming the source part of the text unit.
	 * @param target
	 *            the text container object becoming the target part of the text unit.
	 * @param locId
	 *            the locale of the target part (passed in the target parameter).
	 * @param comment
	 *            the optional comment becoming a NOTE property of the text unit.
	 * @return a reference to the original or newly created text unit.
	 */
	public static ITextUnit buildTU (ITextUnit textUnit,
		String name,
		TextContainer source,
		TextContainer target,
		LocaleId locId,
		String comment)
	{
		if (textUnit == null) {
			textUnit = new TextUnit("");
		}

		if (textUnit.getSkeleton() == null) {
			GenericSkeleton skel = new GenericSkeleton();
			textUnit.setSkeleton(skel);
		}

		if (!Util.isEmpty(name))
			textUnit.setName(name);

		if (source != null)
			textUnit.setSource(source);

		if (target != null && !Util.isNullOrEmpty(locId))
			textUnit.setTarget(locId, target);

		if (!Util.isEmpty(comment))
			textUnit.setProperty(new Property(Property.NOTE, comment));

		return textUnit;
	}

	/**
	 * Makes sure that a given text unit contains a skeleton. If there's no skeleton already attached to the text unit,
	 * a new skeleton object is created and attached to the text unit.
	 * 
	 * @param tu
	 *            the given text unit to have a skeleton.
	 * @return the skeleton of the text unit.
	 */
	public static GenericSkeleton forceSkeleton (ITextUnit tu) {
		if (tu == null)
			return null;

		GenericSkeleton skel = (GenericSkeleton) tu.getSkeleton();
		if (skel == null) {

			skel = new GenericSkeleton();			
			tu.setSkeleton(skel);
		}

		if (!SkeletonUtil.hasTuRef(skel))
			skel.addContentPlaceholder(tu);

		return skel;
	}

	/**
	 * Copies source and target text of a given text unit into a newly created skeleton. The original text unit remains
	 * intact, and plays a role of a pattern for a newly created skeleton's contents.
	 * 
	 * @param textUnit
	 *            the text unit to be copied into a skeleton.
	 * @return the newly created skeleton, which contents reflect the given text unit.
	 */
	public static GenericSkeleton convertToSkeleton (ITextUnit textUnit) {
		if (textUnit == null)
			return null;

		GenericSkeleton skel = (GenericSkeleton) textUnit.getSkeleton();

		if (skel == null)
			return new GenericSkeleton(textUnit.toString());

		List<GenericSkeletonPart> list = skel.getParts();
		if (list.size() == 0)
			return new GenericSkeleton(textUnit.toString());

		String tuRef = TextFragment.makeRefMarker("$self$");

		GenericSkeleton res = new GenericSkeleton();

		List<GenericSkeletonPart> list2 = res.getParts();

		for (GenericSkeletonPart part : list) {

			String st = part.toString();

			if (Util.isEmpty(st))
				continue;

			if (st.equalsIgnoreCase(tuRef)) {

				LocaleId locId = part.getLocale();
				if (Util.isNullOrEmpty(locId))
					res.add(TextUnitUtil.getSourceText(textUnit));
				else
					res.add(TextUnitUtil.getTargetText(textUnit, locId));

				continue;
			}

			list2.add(part);
		}

		return res;
	}

	/**
	 * Gets an annotation attached to the source part of a given text unit resource.
	 * 
	 * @param <A> a class implementing IAnnotation
	 * @param textUnit
	 *            the given text unit resource.
	 * @param type
	 *            reference to the requested annotation type.
	 * @return the annotation or null if not found.
	 */
	public static <A extends IAnnotation> A getSourceAnnotation (ITextUnit textUnit,
		Class<A> type)
	{
		if (textUnit == null)
			return null;
		if (textUnit.getSource() == null)
			return null;

		return textUnit.getSource().getAnnotation(type);
	}

	/**
	 * Attaches an annotation to the source part of a given text unit resource.
	 * 
	 * @param textUnit
	 *            the given text unit resource.
	 * @param annotation
	 *            the annotation to be attached to the source part of the text unit.
	 */
	public static void setSourceAnnotation (ITextUnit textUnit,
		IAnnotation annotation)
	{
		if (textUnit == null)
			return;
		if (textUnit.getSource() == null)
			return;

		textUnit.getSource().setAnnotation(annotation);
	}

	/**
	 * Gets an annotation attached to the target part of a given text unit resource in a given locale.
	 * 
	 * @param <A> a class implementing IAnnotation
	 * @param textUnit
	 *            the given text unit resource.
	 * @param locId
	 *            the locale of the target part being sought.
	 * @param type
	 *            reference to the requested annotation type.
	 * @return the annotation or null if not found.
	 */
	public static <A extends IAnnotation> A getTargetAnnotation (ITextUnit textUnit,
		LocaleId locId,
		Class<A> type)
	{
		if ( textUnit == null ) return null;
		if ( Util.isNullOrEmpty(locId) ) return null;
		if ( textUnit.getTarget(locId) == null ) return null;
		return textUnit.getTarget(locId).getAnnotation(type);
	}

	/**
	 * Attaches an annotation to the target part of a given text unit resource in a given language.
	 * 
	 * @param textUnit
	 *            the given text unit resource.
	 * @param locId
	 *            the locale of the target part being attached to.
	 * @param annotation
	 *            the annotation to be attached to the target part of the text unit.
	 */
	public static void setTargetAnnotation (ITextUnit textUnit,
		LocaleId locId,
		IAnnotation annotation)
	{
		if ( textUnit == null ) return;
		if ( Util.isNullOrEmpty(locId) ) return;
		if ( textUnit.getTarget(locId) == null ) return;
		textUnit.getTarget(locId).setAnnotation(annotation);
	}

	/**
	 * Sets the coded text of the un-segmented source of a given text unit resource.
	 * 
	 * @param textUnit
	 *            the given text unit resource.
	 * @param text
	 *            the text to be set.
	 */
	public static void setSourceText (ITextUnit textUnit,
		String text)
	{
		TextFragment source = textUnit.getSource().getFirstContent();
		source.setCodedText(text);
	}

	/**
	 * Sets the coded text of the the target part of a given text unit resource in a given language.
	 * 
	 * @param textUnit
	 *            the given text unit resource.
	 * @param locId
	 *            the locale of the target part being set.
	 * @param text
	 *            the text to be set.
	 */
	public static void setTargetText (ITextUnit textUnit,
		LocaleId locId,
		String text)
	{
		TextFragment target = textUnit.getTarget(locId).getFirstContent();
		target.setCodedText(text);
	}

	/**
	 * Removes leading and/or trailing whitespaces from the source part of a given text unit resource.
	 * 
	 * @param textUnit
	 *            the given text unit resource.
	 * @param trimLeading
	 *            true to remove leading whitespaces if there are any.
	 * @param trimTrailing
	 *            true to remove trailing whitespaces if there are any.
	 */
	public static void trimTU (ITextUnit textUnit,
		boolean trimLeading,
		boolean trimTrailing)
	{
		if (textUnit == null)
			return;
		if (!trimLeading && !trimTrailing)
			return;

		TextContainer source = textUnit.getSource();
		GenericSkeleton tuSkel = TextUnitUtil.forceSkeleton(textUnit);
		GenericSkeleton skel = new GenericSkeleton();

		if (trimLeading) {
			trimLeading(source.getFirstContent(), skel);
		}
		skel.addContentPlaceholder(textUnit);

		if (trimTrailing) {
			trimTrailing(source.getFirstContent(), skel);
		}

		int index = SkeletonUtil.findTuRefInSkeleton(tuSkel);
		if (index != -1) {
			SkeletonUtil.replaceSkeletonPart(tuSkel, index, skel);
		} else {
			tuSkel.add(skel);
		}
	}

	/**
	 * Adds to the skeleton of a given text unit resource qualifiers (quotation marks etc.) to appear around text. 
	 * This method is useful when the starting and ending qualifiers are different.
	 * @param textUnit
	 *            the given text unit resource
	 * @param startQualifier
	 *            the qualifier to be added before text
	 * @param endQualifier
	 *            the qualifier to be added after text
	 */
	public static void addQualifiers (ITextUnit textUnit,
			String startQualifier,
			String endQualifier) {
		if (textUnit == null) return;
		if (Util.isEmpty(startQualifier)) return;
		if (Util.isEmpty(endQualifier)) return;
		
		GenericSkeleton tuSkel = TextUnitUtil.forceSkeleton(textUnit);
		GenericSkeleton skel = new GenericSkeleton();

		skel.add(startQualifier);
		skel.addContentPlaceholder(textUnit);
		skel.add(endQualifier);

		int index = SkeletonUtil.findTuRefInSkeleton(tuSkel);
		if (index != -1)
			SkeletonUtil.replaceSkeletonPart(tuSkel, index, skel);
		else
			tuSkel.add(skel);
	}
	
	/**
	 * Adds to the skeleton of a given text unit resource qualifiers (quotation marks etc.) to appear around text. 
	 * @param textUnit
	 *            the given text unit resource
	 * @param qualifier
	 *            the qualifier to be added before and after text
	 */
	public static void addQualifiers (ITextUnit textUnit,
			String qualifier) {
		addQualifiers(textUnit, qualifier, qualifier);
	}
	
	/**
	 * Removes from the source part of a given un-segmented text unit resource qualifiers (parenthesis, quotation marks
	 * etc.) around text. This method is useful when the starting and ending qualifiers are different.
	 * 
	 * @param textUnit
	 *            the given text unit resource.
	 * @param startQualifier
	 *            the qualifier to be removed before source text.
	 * @param endQualifier
	 *            the qualifier to be removed after source text.
	 * @return true if the qualifiers were found and removed
	 */
	public static boolean removeQualifiers (ITextUnit textUnit,
		String startQualifier,
		String endQualifier)
	{
		if (textUnit == null)
			return false;
		if (Util.isEmpty(startQualifier))
			return false;
		if (Util.isEmpty(endQualifier))
			return false;

		String st = getSourceText(textUnit);
		if (st == null)
			return false;

		boolean res = false;
		int startQualifierLen = startQualifier.length();
		int endQualifierLen = endQualifier.length();

		if (st.startsWith(startQualifier) && st.endsWith(endQualifier) && 
				st.length() >= 2) {

			GenericSkeleton tuSkel = TextUnitUtil.forceSkeleton(textUnit);
			GenericSkeleton skel = new GenericSkeleton();

			skel.add(startQualifier);
			skel.addContentPlaceholder(textUnit);
			skel.add(endQualifier);

			res = true;
			setSourceText(textUnit, st.substring(startQualifierLen, Util.getLength(st)
					- endQualifierLen));

			int index = SkeletonUtil.findTuRefInSkeleton(tuSkel);
			if (index != -1)
				SkeletonUtil.replaceSkeletonPart(tuSkel, index, skel);
			else
				tuSkel.add(skel);
		}
		return res;
	}
	
	/**
	 * Simplifies all possible tags in the source part of a given text unit resource.
	 * @param textUnit the given text unit
	 * @param rules rules for the data-driven simplification
	 * @param removeLeadingTrailingCodes true to remove leading and/or trailing codes
	 * of the source part and place their text in the skeleton.
	 */
	public static void simplifyCodes (ITextUnit textUnit, String rules, boolean removeLeadingTrailingCodes) {
		simplifyCodes (textUnit, rules, removeLeadingTrailingCodes, true);
	}
	
	/**
	 * Simplifies all possible tags in the source part of a given text unit resource.
	 * @param textUnit the given text unit
	 * @param rules rules for the data-driven simplification
	 * @param removeLeadingTrailingCodes true to remove leading and/or trailing codes
	 * @param mergeCodes true to merge adjacent codes, false to leave as-is
	 * of the source part and place their text in the skeleton.
	 */
	public static void simplifyCodes (ITextUnit textUnit, String rules, boolean removeLeadingTrailingCodes, boolean mergeCodes) {
		Logger localLogger = LoggerFactory.getLogger(TextUnitUtil.class);
		if (textUnit == null) {
			localLogger.warn("Text unit is null.");
			return;
		}
		
		if (textUnit.getTargetLocales().size() > 0) {
			localLogger.warn(String.format("Text unit %s has one or more targets, " +
					"desynchronization of codes in source and targets is possible.", textUnit.getId()));
		}
		
		TextContainer tc = textUnit.getSource();
		TextFragment[] res = null;
		
		if (textUnit.getSource().hasBeenSegmented()) {
			res = simplifyCodes(tc, rules, removeLeadingTrailingCodes, mergeCodes);			
		}
		else {
			TextFragment tf = tc.getUnSegmentedContentCopy();  			
			res = simplifyCodes(tf, rules, removeLeadingTrailingCodes, mergeCodes);
			textUnit.setSourceContent(tf); // Because we modified a copy
		}
			
		// Move the codes found themselves outside the container/fragment, to the TU skeleton
		if (removeLeadingTrailingCodes && res != null) {
			GenericSkeleton tuSkel = TextUnitUtil.forceSkeleton(textUnit);
			GenericSkeleton skel = new GenericSkeleton();
			
			skel.add(TextUnitUtil.isEmpty(res[0]) ? null : TextFragmentUtil.toText(res[0]));
			skel.addContentPlaceholder(textUnit);
			skel.add(TextUnitUtil.isEmpty(res[1]) ? null : TextFragmentUtil.toText(res[1]));
		
			int index = SkeletonUtil.findTuRefInSkeleton(tuSkel);
			if (index != -1)
				SkeletonUtil.replaceSkeletonPart(tuSkel, index, skel);
			else
				tuSkel.add(skel);
		}
	}

	/**
	 * Simplifies all possible tags in the source part of a given text unit resource. If the
	 * TextUnit has a target then skip simplification.
	 * @param textUnit the given text unit
	 * @param rules rules for the data-driven simplification
	 * @param removeLeadingTrailingCodes true to remove leading and/or trailing codes
	 * of the source part and place their text in the corresponding inter-segment TextPart.
	 * @param mergeCodes true to merge adjacent codes, false to leave as-is
	 */
	public static void simplifyCodesPostSegmentation(ITextUnit textUnit, String rules, boolean removeLeadingTrailingCodes, boolean mergeCodes) {
		if (textUnit == null || TextUnitUtil.isEmpty(textUnit) || !textUnit.isTranslatable()) {
			return;
		}

		// source 
		simplifyCodesPostSegmentation(textUnit.getSource(), rules, removeLeadingTrailingCodes, mergeCodes);
		
		// codes can become desynchronized in target - depend on TextUnitMerger
		// to align codes and reset matching id's
		for (LocaleId tl : textUnit.getTargetLocales()) {
			simplifyCodesPostSegmentation(textUnit.getTarget(tl), rules, removeLeadingTrailingCodes, mergeCodes);
		}		
	}
	
	/**
	 * Simplifies all possible tags in the source part of a given text unit resource. If the
	 * TextUnit has a target then skip simplification.
	 * @param tc the given text container
	 * @param rules rules for the data-driven simplification
	 * @param removeLeadingTrailingCodes true to remove leading and/or trailing codes
	 * of the source part and place their text in the corresponding inter-segment TextPart.
	 * @param mergeCodes true to merge adjacent codes, false to leave as-is
	 */
	public static void simplifyCodesPostSegmentation(TextContainer tc, String rules, boolean removeLeadingTrailingCodes, boolean mergeCodes) {
		if (tc.hasBeenSegmented()) {
			List<TextPart> newParts = new LinkedList<>();
			for (TextPart p : tc.getParts()) {
				if (p.isSegment()) {
					TextFragment[] res = simplifyCodes(p.text, rules, removeLeadingTrailingCodes, mergeCodes, true);
					if (removeLeadingTrailingCodes && res != null) {
						// add the left trimmed codes as a TextPart
						if (res[0] != null) {
							newParts.add(new TextPart(expandCodes(res[0])));
						}					
						
						// add the segment with merged codes, minus trimmed parts
						newParts.add(p);
						
						// add the right trimmed codes as a TextPart
						if (res[1] != null) {
							newParts.add(new TextPart(expandCodes(res[1])));
						}
					} else {
						// add the segment with merged codes, there are no trimmed codes
						newParts.add(p);
					}
				} else {
					// add the inter-segment TextPart created from the Segmenter
					newParts.add(p);
				}
			}
			
			// update TextContainer with newParts
			// newParts holds the trimmed codes as new TextParts
			tc.setParts(newParts.toArray(new TextPart[newParts.size()]));
		} else {
			// not segmented but that probably means there is nothing to simplify
			return;
		}
	}
	
	/**
	 * Expand codes that have been previously merged.
	 * @param tf The original {@link TextFragment} with possibly merged codes.
	 * @return new {@link TextFragment} with expanded codes or original if there are no codes
	 * or they have not been merged.
	 */
	public static TextFragment expandCodes(TextFragment tf) {	
		if (tf == null || !tf.hasCode()) return tf;
		
		TextFragment expandedCodes = new TextFragment();	
		
		for (int i=0; i<tf.length(); i++) {
			switch (tf.charAt(i)) {
				case TextFragment.MARKER_OPENING:
				case TextFragment.MARKER_CLOSING:
				case TextFragment.MARKER_ISOLATED:
					int index = TextFragment.toIndex(tf.charAt(i+1));
					// avoid calling getCodes() to avoid premature balancing
					Code code = tf.codes.get(index);
					// if the code has been merged then unmerge it
					// we don't want trimmed codes to be merged
					// so that we preserve the original code count
					if (code.isMerged()) {
						for (Code c : Code.stringToCodes(code.getMergedData())) {
							expandedCodes.append(c);
						}
					} else {
						expandedCodes.append(code);
					}
					 // skip index marker
					i++;
					break;
				default:
					expandedCodes.append(tf.charAt(i));
					break;
			}
		}
		expandedCodes.balanceMarkers();
		return expandedCodes;
	}
	
	public static boolean hasMergedCode(TextFragment tf) {
		if (tf == null || tf.isEmpty()) {
			return false;
		}
		
		for (Code c : tf.getCodes()) {
			if (c.isMerged()) {
				return true;
			}
		}		
		return false;
	}	
	
	/**
	 * Removes all inline tags in the source (or optionally the target) text unit resource.
	 * @param textUnit the given text unit
	 * @param removeTargetCodes - remove target codes?
	 */
	public static void removeCodes(ITextUnit textUnit, boolean removeTargetCodes) {
		Logger localLogger = LoggerFactory.getLogger(TextUnitUtil.class);
		if (textUnit == null) {
			localLogger.warn("Text unit is null.");
			return;
		}
		
		// remove source inline codes
		TextContainer stc = textUnit.getSource();
		removeCodes(stc);
		
		// if requested and if targets exist remove inline codes for all targets
		if (removeTargetCodes && !textUnit.getTargetLocales().isEmpty()) {				
			for (LocaleId locale: textUnit.getTargetLocales()) {
				TextContainer ttc = textUnit.getTarget(locale);
				removeCodes(ttc); 
			}
		}
	}

	/**
	 * Removes all inline tags from the given {@link TextContainer}
	 * @param tc the given text container
	 */
	public static void removeCodes(TextContainer tc) {
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
				
		StringBuilder tmp = new StringBuilder();
		StringBuilder text = new StringBuilder(tf.getText());
		for (int i=0; i<text.length(); i++) {
			switch (text.charAt(i)) {
				case TextFragment.MARKER_OPENING:
				case TextFragment.MARKER_CLOSING:
				case TextFragment.MARKER_ISOLATED:
					i++; // skip index marker as well
					break;
				default:
					tmp.append(text.charAt(i));
					break;
			}
		}
		tc.setContent(new TextFragment(tmp.toString()));
	}
	
	/**
	 * Removes all inline tags from the given {@link TextFragment}
	 * @param tf the given text fragment
	 */
	public static void removeCodes(TextFragment tf) {				
		StringBuilder tmp = new StringBuilder();
		StringBuilder text = new StringBuilder(tf.getText());
		for (int i=0; i<text.length(); i++) {
			switch (text.charAt(i)) {
				case TextFragment.MARKER_OPENING:
				case TextFragment.MARKER_CLOSING:
				case TextFragment.MARKER_ISOLATED:
					i++; // skip index marker as well
					break;
				default:
					tmp.append(text.charAt(i));
					break;
			}
		}
		tf.clear();
		tf.setCodedText(tmp.toString());				
	}
	
	/**
	 * Removes all inline tags from a given coded text. 
	 * @param codedText the given coded text string
	 * @return the string without code markers
	 */
	public static String removeCodes (String codedText) {
		StringBuilder tmp = new StringBuilder();
		for (int i=0; i<codedText.length(); i++) {
			switch (codedText.charAt(i)) {
				case TextFragment.MARKER_OPENING:
				case TextFragment.MARKER_CLOSING:
				case TextFragment.MARKER_ISOLATED:
					i++; // skip index marker as well
					break;
				default:
					tmp.append(codedText.charAt(i));
					break;
			}
		}
		return tmp.toString();
	}

	/**
	 * Removes the opening and closing codes and replaces the isolated codes in text with the specified string.
	 *
	 * @param codedText               The given coded text string
	 * @param isolatedCodeReplacement The isolated code replacement
	 *
	 * @return The string without code markers
	 */
	public static String removeAndReplaceCodes(String codedText, String isolatedCodeReplacement) {
		StringBuilder tmp = new StringBuilder();

		for (int i = 0; i < codedText.length(); i++) {
			switch (TextFragment.Marker.asEnum(codedText.charAt(i))) {
				case OPENING:
				case CLOSING:
					i++; // skip index marker
					break;
				case ISOLATED:
					i++; // skip index marker
					tmp.append(isolatedCodeReplacement);
					break;
				default:
					tmp.append(codedText.charAt(i));
					break;
			}
		}

		return tmp.toString();				
	}

	/**
	 * Simplifies all possible tags in a given text fragment.
	 * @param tf the given text fragment
	 * @param rules rules for the data-driven simplification
	 * @param removeLeadingTrailingCodes true to remove leading and/or trailing codes
	 * of the source part and place their text in the skeleton.
	 * @return Null (no leading or trailing code removal was) or a string array with the
	 * original data of the codes removed. The first string if there was a leading code, the second string
	 * if there was a trailing code. Both or either can be null
	 */
	public static TextFragment[] simplifyCodes (TextFragment tf, String rules, boolean removeLeadingTrailingCodes) {
		return simplifyCodes(tf, rules, removeLeadingTrailingCodes, true);
	}
	
	/**
	 * Simplifies all possible tags in a given text fragment.
	 * @param tf the given text fragment
	 * @param rules rules for the data-driven simplification
	 * @param removeLeadingTrailingCodes true to remove leading and/or trailing codes
	 * of the source part and place their text in the skeleton.
	 * @param mergeCodes true to merge adjacent codes, false to leave as-is
	 * @return Null (no leading or trailing code removal was) or a string array with the
	 * original data of the codes removed. The first string if there was a leading code, the second string
	 * if there was a trailing code. Both or either can be null
	 */
	public static TextFragment[] simplifyCodes (TextFragment tf, String rules, boolean removeLeadingTrailingCodes, boolean mergeCodes) {
		CodeSimplifier simplifier = new CodeSimplifier();
		simplifier.setRules(rules);
		return simplifier.simplifyAll(tf, removeLeadingTrailingCodes, mergeCodes);
	}
	
	private static TextFragment[] simplifyCodes (TextFragment tf, String rules, boolean removeLeadingTrailingCodes, boolean mergeCodes, boolean postSegmentation) {
		CodeSimplifier simplifier = new CodeSimplifier();
		simplifier.setRules(rules);
		simplifier.postSegmentation = postSegmentation;
		return simplifier.simplifyAll(tf, removeLeadingTrailingCodes, mergeCodes);
	}
	
	/**
	 * Simplifies all possible tags in a given text container.
	 * @param tc the given text container
	 * @param rules rules for the data-driven simplification
	 * @param removeLeadingTrailingCodes true to remove leading and/or trailing codes
	 * of the source part and place their text in the skeleton.
	 * @return Null (no leading or trailing code removal was) or a string array with the
	 * original data of the codes removed. The first string if there was a leading code, the second string
	 * if there was a trailing code. Both or either can be null
	 */
	public static TextFragment[] simplifyCodes (TextContainer tc, String rules, boolean removeLeadingTrailingCodes) {
		return simplifyCodes (tc, rules, removeLeadingTrailingCodes, true);
	}
	
	/**
	 * Simplifies all possible tags in a given text container.
	 * @param tc the given text container
	 * @param rules rules for the data-driven simplification
	 * @param removeLeadingTrailingCodes true to remove leading and/or trailing codes
	 * of the source part and place their text in the skeleton.
	 * @param mergeCodes true to merge adjacent codes, false to leave as-is
	 * @return Null (no leading or trailing code removal was) or a string array with the
	 * original data of the codes removed. The first string if there was a leading code, the second string
	 * if there was a trailing code. Both or either can be null
	 */
	public static TextFragment[] simplifyCodes (TextContainer tc, String rules, boolean removeLeadingTrailingCodes, boolean mergeCodes) {
		CodeSimplifier simplifier = new CodeSimplifier();
		simplifier.setRules(rules);
		TextFragment[] res = simplifier.simplifyAll(tc, removeLeadingTrailingCodes, mergeCodes);
		trimSegments(tc);
		return res;
	}

	/**
	 * Removes from the source part of a given text unit resource qualifiers (quotation marks etc.) around text.
	 * 
	 * @param textUnit
	 *            the given text unit resource.
	 * @param qualifier
	 *            the qualifier to be removed before and after source text.
	 * @return true if the qualifiers were found and removed
	 */
	public static boolean removeQualifiers (ITextUnit textUnit,
		String qualifier)
	{
		return removeQualifiers(textUnit, qualifier, qualifier);
	}
	
	/**
	 * Adds an {@link AltTranslation} object to a given {@link TextContainer}. The {@link AltTranslationsAnnotation}
	 * annotation is created if it does not exist already.
	 * @param targetContainer the container where to add the object.
	 * @param alt alternate translation to add.
	 * @return the annotation where the object was added,
	 * it may be a new annotation or the one already associated with the container.  
	 */
	public static AltTranslationsAnnotation addAltTranslation (TextContainer targetContainer,
		AltTranslation alt)
	{
		AltTranslationsAnnotation altTrans = targetContainer.getAnnotation(AltTranslationsAnnotation.class);
		if ( altTrans == null ) {
			altTrans = new AltTranslationsAnnotation();
			targetContainer.setAnnotation(altTrans);
		}
		altTrans.add(alt);
		return altTrans;
	}
	
	/**
	 * Adds an {@link AltTranslation} object to a given {@link Segment}.
	 * The {@link AltTranslationsAnnotation} annotation is created if it does not exist already.
	 * @param seg the segment where to add the object.
	 * @param alt alternate translation to add.
	 * @return the annotation where the object was added,
	 * it may be a new annotation or the one already associated with the segment.  
	 */
	public static AltTranslationsAnnotation addAltTranslation (Segment seg,
		AltTranslation alt)
	{
		AltTranslationsAnnotation altTrans = seg.getAnnotation(AltTranslationsAnnotation.class);
		if ( altTrans == null ) {
			altTrans = new AltTranslationsAnnotation();
			seg.setAnnotation(altTrans);
		}
		altTrans.add(alt);
		return altTrans;
	}
	
	public static TextFragment storeSegmentation (TextContainer tc) {
		// Join all segment and text parts into a new TextFragment
		
		// We need to have markers for both segments and text parts, because if we have segment markers only,
		// then adjacent text parts in an inter-segment space will get restored as one text part
		
		// Cannot store seg ids in Code.type, because if 2 codes are merged, the type of one of them is lost 
		
		// tf accumulates several segments/text parts
		TextFragment tf = new TextFragment();
		int lastCodeId = 0;
		Code code;
		for ( TextPart part : tc ) {
			// We work with ptf, not tf that can contain previous parts
			TextFragment ptf = part.getContent().clone();
			lastCodeId = ptf.renumberCodes(++lastCodeId);
			
			if (part.isSegment()) {
				Segment seg = (Segment) part;				
				
				int startPos = getStartPosition(ptf);				
				TextFragment ctf = new TextFragment();
				
				// If we move the segment start, we need to create a leading text part
				if (startPos > 0) {
					code = new Code(TagType.PLACEHOLDER, "tp", TP_START);
					code.setId(++lastCodeId);
					ctf.append(code); // No re-balancing is happening as code Id <> -1
					ptf.insert(0, ctf, true); // append keeping code IDs
					startPos += 2; // adjust for just inserted 2-char code marker
					
					ctf = new TextFragment();
					code = new Code(TagType.PLACEHOLDER, "tp", TP_END);
					code.setId(++lastCodeId);
					// Will be combined and inserted with the following seg end marker
					ctf.append(code); // No re-balancing is happening as code Id <> -1
				}
				int markerId = ++lastCodeId;
				code = new Code(TagType.OPENING, "seg", TextFragment.makeRefMarker(seg.getId(), SEG_START));
				code.setId(markerId);				
				ctf.append(code); // No re-balancing is happening as code Id <> -1
				ptf.insert(startPos, ctf, true); // insert keeping code IDs
				
				ctf = new TextFragment();
				int endPos = getEndPosition(ptf);				
				
				code = new Code(TagType.CLOSING, "seg", TextFragment.makeRefMarker(seg.getId(), SEG_END));
				code.setId(markerId);				
				ctf.append(code); // No re-balancing is happening as code Id <> -1
				
				// If we move the segment end, we need to create a trailing text part
				boolean insertTrailingPart = endPos < ptf.length();
				if (insertTrailingPart) {
					code = new Code(TagType.PLACEHOLDER, "tp", TP_START);
					code.setId(++lastCodeId);
					ctf.append(code); // No re-balancing is happening as code Id <> -1
				}
				
				ptf.insert(endPos, ctf, true); // insert keeping code IDs
				
				if (insertTrailingPart) {
					ctf = new TextFragment();
					code = new Code(TagType.PLACEHOLDER, "tp", TP_END);
					code.setId(++lastCodeId);
					ctf.append(code); // No re-balancing is happening as code Id <> -1
					ptf.insert(-1, ctf, true); // insert keeping code IDs
				}				
				
				tf.insert(-1, ptf, true); // append keeping code IDs
			}
			else {
				code = new Code(TagType.PLACEHOLDER, "tp", TP_START);
				code.setId(++lastCodeId);
				tf.append(code); // No re-balancing is happening as code Id <> -1
				
//				TextFragment ptf = part.getContent().clone();
//				lastCodeId = ptf.renumberCodes(++lastCodeId);
				tf.insert(-1, ptf, true); // append keeping code IDs

				code = new Code(TagType.PLACEHOLDER, "tp", TP_END);
				code.setId(++lastCodeId);
				tf.append(code); // No re-balancing is happening as code Id <> -1
			}
		}
		tf.renumberCodes();
		return tf;
	}

	private static String buildSearchString(TextFragment tf) {		
		String text = tf.getCodedText(); // balances codes
		StringBuilder sb = new StringBuilder();
		
		for ( int i=0; i<text.length(); i++ ) {
			switch ( text.charAt(i) ) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
				sb.append("11");
				i++;
				continue;
			case TextFragment.MARKER_ISOLATED:
				sb.append("00");
				i++;
				continue;
			default:
				if (Character.isWhitespace(text.charAt(i)))
					sb.append("0");
				else
					sb.append("1");
			}			
		}
		return sb.toString();
	}
	
	private static int getStartPosition(TextFragment tf) {
		String st = buildSearchString(tf);
		int pos = st.indexOf('1');
		return pos == -1 ? 0 : pos;
	}
	
	private static int getEndPosition(TextFragment tf) {
		String st = buildSearchString(tf);
		int pos = StringUtil.mirrorString(st).indexOf('1');
		return pos == -1 ? st.length() : st.length() - pos;
	}

	/**
	 * Trims segments of a given text container that contains leading or trailing whitespaces.  
	 * Removed whitespaces are placed in newly created whitespace-only text parts before and after a trimmed segment. 
	 * @param tc the given text container
	 * @param trimLeading true to remove leading whitespaces of a segment
	 * @param trimTrailing true to remove trailing whitespaces of a segment
	 */
	public static void trimSegments (TextContainer tc, boolean trimLeading, boolean trimTrailing) {
		if (!trimLeading && !trimTrailing) return; // Nothing to do
		
		int index = 0;
		while (index < tc.count()) {
			TextPart part = tc.get(index);
			if (part.isSegment()) { // Trimming only segments
				TextFragment tf = part.getContent();
				
				if (trimLeading) {
					GenericSkeleton skel1 = new GenericSkeleton();
					trimLeading(tf, skel1);
					if (!skel1.isEmpty()) {
						tc.insert(index, new TextPart(skel1.toString()));
						index++; // Segment was moved right 
					}
				}
				
				if (trimTrailing) {
					GenericSkeleton skel2 = new GenericSkeleton();
					trimTrailing(tf, skel2);
					if (!skel2.isEmpty()) {
						tc.insert(index + 1, new TextPart(skel2.toString()));
						index++; // Skip the inserted part
					}
				}				
			}
			index++; // Move on
		}
	}
	
	public static void trimSegments (TextContainer tc) {
		trimSegments(tc, true, true);
	}
	
	private enum MarkerType {
		M_SEG_START,
		M_SEG_END,
		M_TP_START,
		M_TP_END
	}
	
	private static final class Marker {
		private MarkerType type;
		private String id;
		private int position; // position in coded text, if context == Code then position of the code + 1 (code's 2-nd char) in coded text
		private int relPos; // if context == null then relPos = 0, if context == Code pos in the code's data
		private Code context; // a Code ref or null if context is the text
		
		private Marker(MarkerType type, int position, int relPos, Code context) {
			this(type, null, position, relPos, context);
		}
		
		private Marker(MarkerType type, String id, int position, int relPos, Code context) {
			super();
			this.type = type;
			this.id = id;
			this.position = position;
			this.relPos = relPos;
			this.context = context;
		}
	}
	
	/**
	 * Extracts segment and text part markers from a given string, creates codes (place-holder type) for those markers, 
	 * and appends them to a given text fragment.
	 * @param tf the given text fragment to append extracted codes
	 * @param original the given string
	 * @param removeFromOriginal remove found markers from the given string
	 * @return the given string if removeFromOriginal == false, or the modified original string with markers removed otherwise
	 */
	public static TextFragment extractSegMarkers(TextFragment tf, TextFragment original, boolean removeFromOriginal) {
		Logger localLogger = LoggerFactory.getLogger(TextUnitUtil.class);
		if (tf == null) {
			localLogger.warn("Text fragment is null, no codes are added");
		}
		if (original == null) {
			localLogger.warn("Original string is null, no processing was performed");
			return null;
		}
		
		Matcher matcher = ANY_SEG_TP_REGEX.matcher(original.toText());
		while (tf != null && matcher.find()) {
			tf.append(new Code(TagType.PLACEHOLDER, null, matcher.group()));
		}
		
		if (removeFromOriginal) {
			List<Code> removedCodes = new LinkedList<>();
			for (Code c : original.getCodes()) {
				matcher = ANY_SEG_TP_REGEX.matcher(c.getOuterData());
				String d = matcher.replaceAll("");
				// remove the code if it was only a seg and/or textpart marker
				if (!c.getOuterData().isEmpty() && d.isEmpty()) {
					removedCodes.add(c);
				} else if (!c.getData().isEmpty()) {
					c.setData(d);
				} else if (!c.getOuterData().isEmpty()) {
					c.setOuterData(d);
				}
			}	
			
			// remove all the empty codes
			for (Code c : removedCodes) {
				original.removeCode(c);
			}
			return original;
		} else {
			return original;
		}
	}
	
	public static boolean hasSegOrTpMarker(Code code) {
		return ANY_SEG_TP_REGEX.matcher(code.data).find();
	}
	
	public static boolean hasSegStartMarker(Code code) {
		return SEG_START_REGEX.matcher(code.data).find();
	}
	
	public static boolean hasSegEndMarker(Code code) {
		return SEG_END_REGEX.matcher(code.data).find();
	}
	
	public static boolean hasTpStartMarker(Code code) {
		return TP_START_REGEX.matcher(code.data).find();
	}
	
	public static boolean hasTpEndMarker(Code code) {
		return TP_END_REGEX.matcher(code.data).find();
	}
	
	public static boolean hasExternalRefMarker(Code code) {
		return EXTERNAL_REF_REGEX.matcher(code.data).find();
	}
	
	private enum TokenType {
		SEG,
		TP,
		SEG_START,
		SEG_END,
		TP_START,
		TP_END
	}
	
	private static final class Token {
		TokenType type;
		String id;
		Range range;
		Range textRange;
		
		private Token(TokenType type, Range range, Range textRange, String id, String match) {
			super();
			this.type = type;
			this.range = range;
			this.textRange = textRange == null ? range : textRange;
			this.id = id;
		}
	}
	
	/**
	 * Restores original segmentation of a given text container from a given text fragment created with storeSegmentation().
	 * @param tc the given text container
	 * @param segStorage the text fragment created with storeSegmentation() and containing the original segmentation info
	 * @return a test string containing a sequence of markers created by the internal algorithm. Used for tests only. 
	 */
	public static String restoreSegmentation(TextContainer tc, TextFragment segStorage) {
		Logger localLogger = LoggerFactory.getLogger(TextUnitUtil.class);
		
		// Empty tc
		tc.clear();
		
		// Scan the tf, create segments and text parts, and add them to tc
		TextFragment tf = segStorage;	 	
		String ctext = tf.getCodedText(); // tf.toText() TextUnitUtil.printMarkerIndexes(tf)
		List<Code> codes = tf.getCodes();
		Matcher matcher;
				
		List<Marker> markers = new ArrayList<Marker> ();
		
		for (int i = 0; i < ctext.length(); i++){
			if ( TextFragment.isMarker(ctext.charAt(i)) ) {
				int codeIndex = TextFragment.toIndex(ctext.charAt(i + 1));
				Code code = codes.get(codeIndex);
				String data = code.getData();				

				// Tokenize code data
				List<Token> tokens = new ArrayList<Token>();
				
				matcher = SEG_REGEX.matcher(data); // Whole whitespace-only segment in code
				// Group(1) - id of start seg marker
				// Group(2) - text between markers
				// Group(3) - id of end seg marker (regex provides equality to Group(1))				
				while (matcher.find()) {
					tokens.add(new Token(TokenType.SEG, new Range(matcher.start(), matcher.end()), 
							new Range(matcher.start(2), matcher.end(2)), matcher.group(1), matcher.group()));
				}
				
				matcher = TP_REGEX.matcher(data); // Whole whitespace-only text part in code
				// Group(1) - text between markers
				while (matcher.find()) {
					tokens.add(new Token(TokenType.TP, new Range(matcher.start(), matcher.end()), 
							new Range(matcher.start(1), matcher.end(1)), null, matcher.group()));
				}				
				
				// Pad found ranges with FOO not to find parts of previously found fragments
				for (Token token : tokens) {
					data = StringUtil.padString(data, token.range.start, token.range.end, FOO);
				}
				
				matcher = SEG_START_REGEX.matcher(data);
				// Group(1) - id of start seg marker
				while (matcher.find()) {
					tokens.add(new Token(TokenType.SEG_START, new Range(matcher.start(), matcher.end()), 
							null, matcher.group(1), matcher.group()));
				}
				
				matcher = SEG_END_REGEX.matcher(data);
				// Group(1) - id of end seg marker
				while (matcher.find()) {
					tokens.add(new Token(TokenType.SEG_END, new Range(matcher.start(), matcher.end()), null, 
							matcher.group(1), matcher.group()));
				}
				
				matcher = TP_START_REGEX.matcher(data);
				while (matcher.find()) {
					tokens.add(new Token(TokenType.TP_START, new Range(matcher.start(), matcher.end()), 
							null, null, matcher.group()));
				}
				
				matcher = TP_END_REGEX.matcher(data);
				while (matcher.find()) {
					tokens.add(new Token(TokenType.TP_END, new Range(matcher.start(), matcher.end()), 
							null, null, matcher.group()));
				}
				
				if (tokens.size() == 0) { // No tokens were created, it's a regular code
					i++; // Skip the pair
					continue; // The code remains as is, it hasn't been merged with seg/tp markers
				}
				
				// Pad found ranges with FOO not to find parts of previously found fragments
				for (Token token : tokens) {
					data = StringUtil.padString(data, token.range.start, token.range.end, FOO);
				}
				
				matcher = PLAIN_TEXT_REGEX.matcher(data);
				while (matcher.find()) {
					tokens.add(new Token(TokenType.TP, new Range(matcher.start(), matcher.end()), 
							new Range(matcher.start(), matcher.end()), null, matcher.group()));
				}
								
				Collections.sort(tokens, new Comparator<Token>() {

					@Override
					public int compare(Token t1, Token t2) {
						// If a text part (from leading plain text) appears before seg end, move it forward behind the seg end
						if (t1.type == TokenType.SEG_END && t2.type == TokenType.TP)
							return -1;
						if (t2.type == TokenType.SEG_END && t1.type == TokenType.TP)
							return 1;
						// If a text part (from trailing plain text) appears after seg start, move it backwards before seg start
						if (t1.type == TokenType.SEG_START && t2.type == TokenType.TP)
							return 1;
						if (t2.type == TokenType.SEG_START && t1.type == TokenType.TP)
							return -1;
						// Otherwise text parts and other type combinations are sorted by start position
						if (t1.textRange.start < t2.textRange.start)
							return -1;
						else if (t1.textRange.start > t2.textRange.start)
							return 1;
						else
							return 0;
					}
					
				});
				
				// Translate tokens to markers
				for (Token token : tokens) {
					switch (token.type) {
					
					case SEG:
						markers.add(new Marker(MarkerType.M_SEG_START, token.id, i, token.textRange.start, code));
						markers.add(new Marker(MarkerType.M_SEG_END, token.id, i, token.textRange.end, code));
						break;
						
					case TP:
						markers.add(new Marker(MarkerType.M_TP_START, i, token.textRange.start, code));
						markers.add(new Marker(MarkerType.M_TP_END, i, token.textRange.end, code));
						break;
						
					case SEG_START:
						markers.add(new Marker(MarkerType.M_SEG_START, token.id, i + 2, 0, null));
						break;
						
					case SEG_END:
						markers.add(new Marker(MarkerType.M_SEG_END, token.id, i, 0, null));
						break;
						
					case TP_START:
						markers.add(new Marker(MarkerType.M_TP_START, i + 2, 0, null));
						break;
						
					case TP_END:
						markers.add(new Marker(MarkerType.M_TP_END, i, 0, null));
						break;
					}
				}
				
				//code.setData(data);
				i++; // Skip the pair
			}
		}
		
		Collections.sort(markers, new Comparator<Marker>() {
			
			@Override
			public int compare(Marker m1, Marker m2) {
					if (m1.position < m2.position) 
						return -1;
					else if (m1.position > m2.position) 
						return 1;
					else { // equal positions, markers from a merged code (same context), or one from the text and the other from a code
						if (m1.context != null && m2.context != null) {
							if (m1.relPos < m2.relPos)
								return -1;
							else if (m1.relPos > m2.relPos)
								return 1;
							else
								return 0;
						}
						else if (m1.context != null && m2.context == null) {
							return 1; // Move the marker with a relPos (code context) forward
						}
						else if (m1.context == null && m2.context != null) {
							return -1; // Move the marker with an absolute position (text context) backwards
						}
						else
							return 0;
					}
			}						
		});
		
		// Create segments and text parts in tc
		StringBuilder markersSb = new StringBuilder();
		ArrayList<TextPart> list = new ArrayList<TextPart>(); 		
		
		int start = -1;
		for (Marker d : markers) {
			switch (d.type) {
			case M_SEG_START:
				start = d.context == null ? d.position : d.relPos;
				if (d.context == null)
					markersSb.append(String.format("(%d: %s %s) ", d.position, "seg_start", d.id));
				else
					markersSb.append(String.format("(%d-%d: %s %s) ", d.position, d.relPos, "seg_start", d.id));
				break;

			case M_SEG_END:
				if (start > -1) {
					if (d.context == null) {
						if (start <= d.position)
							list.add(new Segment(d.id, tf.subSequence(start, d.position)));
						else
							localLogger.warn(String.format("Cannot create the segment %s - incorrect range: (%d - %d)", 
									d.id, start, d.position));
					}
					else {
						if (start <= d.relPos) {
							//list.add(new Segment(d.id, new TextFragment(d.context.getData().substring(start, d.relPos))));
							TextFragment tf2 = new TextFragment();
							tf2.append(new Code(d.context.tagType, d.context.type, d.context.getData().substring(start, d.relPos)));
							Segment newSeg = new Segment(d.id, tf2);							
							list.add(newSeg);
						}							
						else
							localLogger.warn(String.format("Cannot create the segment %s - incorrect range: (%d - %d)", 
									d.id, start, d.relPos));
					}
				}
				start = -1;
				if (d.context == null)
					markersSb.append(String.format("(%d: %s %s) ", d.position, "seg_end", d.id));
				else
					markersSb.append(String.format("(%d-%d: %s %s) ", d.position, d.relPos, "seg_end", d.id));
				break;
				
			case M_TP_START:
				start = d.context == null ? d.position : d.relPos;
				if (d.context == null)
					markersSb.append(String.format("(%d: %s) ", d.position, "tp_start"));
				else
					markersSb.append(String.format("(%d-%d: %s) ", d.position, d.relPos, "tp_start"));
				break;
				
			case M_TP_END:
				if (start > -1) {
					if (d.context == null) {
						if (start <= d.position)
							list.add(new TextPart(tf.subSequence(start, d.position)));
						else
							localLogger.warn(String.format("Cannot create a text part - incorrect range: (%d - %d)", start, d.position));
					}
					else {
						if (start <= d.relPos) {
							// list.add(new TextPart(new TextFragment(d.context.getData().substring(start, d.relPos))));
							TextFragment tf2 = new TextFragment();
							tf2.append(new Code(d.context.tagType, d.context.type, d.context.getData().substring(start, d.relPos)));
							TextPart newTp = new TextPart(tf2);							
							list.add(newTp);
						}							
						else
							localLogger.warn(String.format("Cannot create a text part - incorrect range: (%d - %d)", start, d.relPos));
					}						
				}
				start = -1;
				if (d.context == null)
					markersSb.append(String.format("(%d: %s) ", d.position, "tp_end"));
				else
					markersSb.append(String.format("(%d-%d: %s) ", d.position, d.relPos, "tp_end"));
				break;
			}			
		}
		
		testMarkersSt = markersSb.toString().trim();
		setParts(tc, list.toArray(new TextPart[list.size()]));
		return testMarkersSt; 
	}

	public static String testMarkers() {
		return testMarkersSt;
	}
	
	private static void setParts(TextContainer tc, TextPart... parts) {
		tc.setParts(parts);
	}
	
	/**
	 * Returns the content of a given text fragment, including the original codes whenever
	 * possible. Codes are decorated with '[' and ']' to tell them from regular text.
	 * @param tf the given text fragment 
	 * @return the content of the given fragment
	 */
	public static String toText (TextFragment tf) {
		List<Code> codes = tf.getCodes();
		String text = tf.getCodedText();
		
		if (( codes == null ) || ( codes.size() == 0 )) return text.toString();
		
		StringBuilder tmp = new StringBuilder();
		Code code;
		for ( int i=0; i<text.length(); i++ ) {
			switch ( text.charAt(i) ) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				tmp.append(String.format("[%s]", code.data));
				break;
			default:
				tmp.append(text.charAt(i));
				break;
			}
		}
		return tmp.toString();
	}
	
	/**
	 * Returns representation of a given coded text with code data enclosed in brackets.
	 * @param text the given coded text
	 * @param codes the given list of codes
	 * @return content of the given coded text
	 */
	public static String toText (String text, List<Code> codes) {
		
		if (( codes == null ) || ( codes.size() == 0 )) return text.toString();
		
		StringBuilder tmp = new StringBuilder();
		Code code;
		for ( int i=0; i<text.length(); i++ ) {
			switch ( text.charAt(i) ) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				int index = TextFragment.toIndex(text.charAt(++i));
				try {
					code = codes.get(index);
					tmp.append(String.format("[%s]", code.data));
				} catch (Exception e) {
					tmp.append(String.format("[-ERR:UNKNOWN-CODE- %d]", index));
				}				
				break;
			default:
				tmp.append(text.charAt(i));
				break;
			}
		}
		return tmp.toString();
	}

	public static boolean isApproved(ITextUnit tu, LocaleId targetLocale) {
		if ( !tu.isTranslatable() ) return false;
		
		Property prop = tu.getTargetProperty(targetLocale, Property.APPROVED);
    	if ( prop != null ) {
    		if ( "yes".equals(prop.getValue()) ) return true;
    	}
		
    	return false;
	}
	
	/**
	 * Convert all TextParts (not Segments) in a given TextContainer to each contain 
	 * a single code with the part's text. Needed to protect the text of 
	 * text part (e.g. created from original codes) against being escaped by 
	 * an encoder.
	 * @param tc the given TextContainer
	 */
	public static void convertTextPartsToCodes(TextContainer tc) {
		for (TextPart textPart : tc) {
			convertTextPartToCode(textPart);
		}
	}
	
	/**
	 * Create a single code with a given TextPart's text.
	 * Needed to protect the text of the text part from being escaped by 
	 * an encoder. If the TextPart already has codes, no conversion
	 * is performed.
	 * @param textPart the given TextPart
	 */
	public static void convertTextPartToCode(TextPart textPart) {
		if (!textPart.isSegment()) {
			TextFragment tf = textPart.getContent();
			if (tf.hasCode()) return;
			
			// Move the whole text of text part to a single code
			tf.changeToCode(0, tf.getCodedText().length(), 
					TagType.PLACEHOLDER, null);
		}
	}
	
	public static void convertTextParts_whitespaceCodesToText(TextContainer tc) {
		for (TextPart textPart : tc) {
			convertTextPart_whitespaceCodesToText(textPart);
		}
	}
	
	public static void convertTextPart_whitespaceCodesToText(TextPart textPart) {
		if (textPart.isSegment()) return;
		
		TextFragment tf = textPart.getContent();
		if (tf.hasText()) return;
		
		if (Util.isEmpty(tf.toText().trim())) {
			// Move all codes into text
			textPart.setContent(new TextFragment(tf.toText()));
		}
	}

	public static boolean isStandalone(ITextUnit tu) {
		if (tu == null) {
			return true;
		}
		
		if (tu.isReferent()) {
			return false;
		}
		
		TextFragment tf = tu.getSource().getUnSegmentedContentCopy();
		for (Code code : tf.getCodes()) {
			if (code.hasReference()) { 
				return false;
			}
		}
		
		ISkeleton skel = tu.getSkeleton();
		if (skel == null) {
			return true;
		}		
		
		// FIXME: other skeleton types may have TU references
		// We need a better ISkeleton interface!
		if (!(skel instanceof GenericSkeleton)) {
			return true;
		}
		
		List<GenericSkeletonPart> parts = ((GenericSkeleton) skel).getParts();
		for (GenericSkeletonPart part : parts) {
			// if it is a self reference then its still standalone (return true)
			if (SkeletonUtil.isReference(part)) { 
				return false;
			}
		}
		
		return true;
	}

	public static void renumberCodes(TextContainer tc) {
		for (TextPart textPart : tc) {
			textPart.getContent().renumberCodes(1);
		}		
	}
	
	/**
	 * Detects if a given TextContainer contains whitespace characters to
	 * be preserved in XML. Single space 0x20 doesn't need to be preserved, other whitespace
	 * characters, also a sequence of 2 or more single spaces do.
	 * @param tc the given TextContainer object.
	 * @return true if the given TextContainer has whitespace sequences that need to be preserved.
	 */
	public static boolean needsPreserveWhitespaces(TextContainer tc) {
		return !Util.isEmpty(RegexUtil.find(tc.toString(),
				// Not [non-whitespace or single space]
				// or [two or more single spaces]
				"[^\\S ]| {2,}", 0));		
	}
	
	public static boolean needsPreserveWhitespaces(ITextUnit tu) {
		if (needsPreserveWhitespaces(tu.getSource())) return true;
		
		for (LocaleId trgLoc : tu.getTargetLocales()) {
			if (needsPreserveWhitespaces(tu.getTarget(trgLoc))) 
				return true;
		}
		return false;
	}

	public static boolean isWellformed(TextFragment tf) {		
		Stack<Integer> idStack = new Stack<Integer>();
		idStack.push(-1);
		
		String text = tf.getCodedText();
		List<Code> codes = tf.getCodes();
		
		// Process the markers
		for ( int i=0; i<text.length(); i++ ) {
			switch ( text.charAt(i) ) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				int index = TextFragment.toIndex(text.charAt(i+1));
				Code code = codes.get(index);
				
				switch ( code.tagType ) {
				
				case OPENING:
					idStack.push(code.getId());
					break;
					
				case CLOSING:
					if (idStack.pop() != code.getId()) 
						return false;
					break;
					
				default:
					break;
				}
				i++; // Skip index part of the code marker
				break;
			}
		}
		return true;
	}
	
	public static boolean isWellformed(TextContainer tc) {
		for (TextPart textPart : tc) {
			if (!isWellformed(textPart.text)) return false;
		}
		return true;
	}

	public static void unsegmentTU(ITextUnit tu) {
		tu.getSource().joinAll();
		for (LocaleId trgLoc : tu.getTargetLocales()) {
			tu.getTarget(trgLoc).joinAll();
		}
	}
}
