/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.skeleton;

import java.util.List;
import java.util.regex.Pattern;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.RegexUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Helper methods to manipulate skeleton objects. 
 */
public class SkeletonUtil {

	private static final String TU_REF = TextFragment.makeRefMarker("$self$");
	
	private static final Pattern PROPERTY_REGEX = Pattern.compile(String.format("%s%s%s.+%s",
			RegexUtil.escape(TextFragment.REFMARKER_START),
			RegexUtil.escape("$self$"),
			RegexUtil.escape(TextFragment.REFMARKER_SEP),
			RegexUtil.escape(TextFragment.REFMARKER_END)));
	
	private static final Pattern REF_REGEX = Pattern.compile(String.format("%s.+%s",
			RegexUtil.escape(TextFragment.REFMARKER_START),
			RegexUtil.escape(TextFragment.REFMARKER_END)));
	
	private static final Pattern SEG_REF_REGEX = Pattern.compile(String.format("%s.+%s%s%s",
			RegexUtil.escape(TextFragment.REFMARKER_START),
			RegexUtil.escape(TextFragment.REFMARKER_SEP),
			RegexUtil.escape(Segment.REF_MARKER),
			RegexUtil.escape(TextFragment.REFMARKER_END)));
	
	/**
	 * Finds source reference in the skeleton.
	 * @param skel the skeleton being sought for the reference
	 * @return index in the list of skeleton parts for the skeleton part containing the reference 
	 */
	public static int findTuRefInSkeleton(GenericSkeleton skel) {
		return findTuRefInSkeleton(skel, null);
	}
	
	/**
	 * Finds either source or target reference in the skeleton. If locId is specified, then its target reference is sought for.
	 * @param skel the skeleton being sought for the reference.
	 * @param locId the locale to search the reference for.
	 * @return index in the list of skeleton parts for the skeleton part containing the reference. 
	 */
	public static int findTuRefInSkeleton(GenericSkeleton skel,
		LocaleId locId)
	{
		if ( skel == null ) return -1; 		
		List<GenericSkeletonPart> list = skel.getParts();		
		
		for ( int i=0; i<list.size(); i++ ) {
			GenericSkeletonPart part = list.get(i);				
			String st = part.toString();
			if ( Util.isEmpty(st) ) continue;
			if ( st.equalsIgnoreCase(TU_REF) ) {
				if ( Util.isNullOrEmpty(locId) ) {
					return i;
				}
				else if ( locId.equals(part.getLocale()) ) {
					return i;
				}
			}
		}
		return -1;
	}
		
	/**
	 * Determines if a given skeleton contains a source reference in it.
	 * @param skel the skeleton being sought for the reference. 
	 * @return true if the given skeleton contains such a reference.
	 */
	public static boolean hasTuRef (GenericSkeleton skel) {
		return findTuRefInSkeleton(skel) != -1;
	}
	
	/**
	 * Determines if a given skeleton contains a target reference in a given locale.
	 * @param skel the skeleton being sought for the reference. 
	 * @param locId the locale of the target part being sought.
	 * @return true if the given skeleton contains such a reference.
	 */
	public static boolean hasTuRef (GenericSkeleton skel, LocaleId locId) {
		return findTuRefInSkeleton(skel, locId) != -1;
	}

	/**
	 * Splits a given {@link GenericSkeleton} into 2 parts: before and after the
	 * content placeholder (self-marker).
	 * @param skel the given {@link GenericSkeleton}.
	 * @return array of 2 {@link GenericSkeleton}s before and after self-marker. 
	 */
	public static GenericSkeleton[] splitSkeleton(GenericSkeleton skel) {
		GenericSkeleton[] res = new GenericSkeleton[2];
		int index = findTuRefInSkeleton(skel);
		if (index == -1) {
			res[0] = skel;
			res[1] = new GenericSkeleton(); // Empty skeleton, not null
		}
		else {
			List<GenericSkeletonPart> parts = skel.getParts();
			res[0] = new GenericSkeleton();
			res[0].getParts().addAll(parts.subList(0, index));
			
			res[1] = new GenericSkeleton();
			res[1].getParts().addAll(parts.subList(index + 1, parts.size()));
		}
		return res;
	}
			
	/**
	 * Replaces a part of a given skeleton with another given skeleton part.
	 * @param skel the skeleton which part is being replaced.
	 * @param index the index of the skeleton part to be replaced.
	 * @param replacement the given new skeleton part to replace the existing one.
	 * @return true if replacement succeeded.
	 */
	public static boolean replaceSkeletonPart (GenericSkeleton skel,
		int index,
		GenericSkeleton replacement)
	{
		if ( skel == null ) return false;
		if ( replacement == null ) return false;
		
		List<GenericSkeletonPart> list = skel.getParts();
		if ( !Util.checkIndex(index, list) ) return false;

		List<GenericSkeletonPart> list2 = (List<GenericSkeletonPart>) ListUtil.moveItems(list); // clears the original list
		for (int i = 0; i < list2.size(); i++) {
			if ( i == index )						
				skel.add(replacement);
			else
				list.add(list2.get(i));
		}
		return true;
	}

	public static int getNumParts(GenericSkeleton skel) {
		return skel.getParts().size();
	}
	
	public static GenericSkeletonPart getPart(GenericSkeleton skel, int index) {
		List<GenericSkeletonPart> parts = skel.getParts();
		if (!Util.checkIndex(index, parts)) return null;
		return parts.get(index);
	}
	
	public static boolean isTuRef(GenericSkeletonPart part) {
		return TU_REF.equals(part.toString());		
	}
	
	public static boolean isRef(GenericSkeletonPart part) {
		String st = part.toString();
//		return !isTuRef(part) && RegexUtil.matches(st, REF_REGEX);		
		return !isTuRef(part) && RegexUtil.contains(st, REF_REGEX);
	}
	
	public static boolean isPropRef(GenericSkeletonPart part) {
		String st = part.toString();
		return !isTuRef(part) && RegexUtil.matches(st, PROPERTY_REGEX);
	}
	
	public static boolean isSegRef(GenericSkeletonPart part) {
		String st = part.toString();
		return !isTuRef(part) && RegexUtil.matches(st, SEG_REF_REGEX);
	}
	
	public static boolean isSourcePlaceholder(GenericSkeletonPart part, IResource resource) {
		if (resource == null || part == null) return false;
		return isTuRef(part) && part.getParent() == resource && part.getLocale() == null;		
	}
	
	public static boolean isTargetPlaceholder(GenericSkeletonPart part, IResource resource) {
		if (resource == null || part == null) return false;
		return isTuRef(part) && part.getParent() == resource && part.getLocale() != null;		
	}
	
	public static boolean isPropValuePlaceholder(GenericSkeletonPart part, IResource resource) {		 
		if (resource == null || part == null) return false;
//		if (!(resource instanceof INameable)) return false; // Ending can also have a skeleton with a property ref part
		return isPropRef(part) && part.getParent() == resource;		
	}
	
	public static boolean isSegmentPlaceholder(GenericSkeletonPart part, IResource resource) {		 
		if (resource == null || part == null) return false;
		return isSegRef(part) && part.getParent() == resource;		
	}
	
	public static boolean isExtSourcePlaceholder(GenericSkeletonPart part, IResource resource) {
		if (resource == null || part == null) return false;
		return isTuRef(part) && part.getParent() != resource && part.getParent() != null && part.getLocale() == null;		
	}
	
	public static boolean isExtTargetPlaceholder(GenericSkeletonPart part, IResource resource) {
		if (resource == null || part == null) return false;
		return isTuRef(part) && part.getParent() != resource && part.getParent() != null && part.getLocale() != null;		
	}
	
	public static boolean isExtPropValuePlaceholder(GenericSkeletonPart part, IResource resource) {
		if (resource == null || part == null) return false;
//		if (!(resource instanceof INameable)) return false; // Ending can also have a skeleton with a property ref part
		return isPropRef(part) && part.getParent() != resource && part.getParent() != null;
	}
	
	public static boolean isExtSegmentPlaceholder(GenericSkeletonPart part, IResource resource) {
		if (resource == null || part == null) return false;
		return isSegRef(part) && part.getParent() != resource && part.getParent() != null;
	}
	
	public static boolean isReference(GenericSkeletonPart part) {
		if (part == null) return false;
		return isRef(part) && part.getParent() == null && part.getLocale() == null;
	}
	
	public static boolean isText(GenericSkeletonPart part) {
		if (part == null) return false;
		return !isTuRef(part) && !isPropRef(part) && !isRef(part) && !isSegRef(part);
	}	
	
	public static String getRefId(GenericSkeletonPart part) {
		Object[] marker = TextFragment.getRefMarker(part.getData());
		return (String) marker[0];
	}

	public static void changeParent(ISkeleton skel, IResource curParent,
			IResource newParent) {
		if (skel instanceof GenericSkeleton) {
			GenericSkeleton gs = (GenericSkeleton) skel;
			
			if (gs.getParent() == curParent)
				gs.setParent(newParent);
			
			for (GenericSkeletonPart part : gs.getParts()) {
				if (part.getParent() == curParent)
					part.setParent(newParent);
			}
		}		
	}
}
