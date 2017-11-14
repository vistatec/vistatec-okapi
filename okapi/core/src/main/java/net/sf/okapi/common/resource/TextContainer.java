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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;

/**
 * Provides methods for storing the content of a paragraph-type unit, to handle
 * its properties, annotations and segmentation.
 * <p>
 * The TextContainer is made of a collection of parts: Some are simple {@link TextPart} objects,
 * others are special {@link TextPart} objects called {@link Segment}.
 * <p>
 * A TextContainer has always at least one {@link Segment} part.
 */
public class TextContainer implements INameable, Iterable<TextPart> {

	// Separators for storage
	private static final String PARTSEP1 = "\uE091";
	private static final String PARTSEP2 = "\uE092";
	private static final char PARTSEP1CHAR = PARTSEP1.charAt(0);

	private LinkedHashMap<String, Property> properties;
	private Annotations annotations;
	private List<TextPart> parts;
	private boolean segApplied;

	private Segments segments = new Segments(this);

	/**
	 * Creates a new {@link ISegments} object to access the segments of this container.
	 * @return a new {@link ISegments} object.
	 */
	public ISegments getSegments () {
		return segments;
	}
	
	/**
	 * Creates a string that stores the content of a given container.
	 * Use {@link #stringToContent(String)} to create the container back from the string.
	 * <p>
         * <b>IMPORTANT:</b> Only the content is saved (not the properties, annotations, etc.).
         *
	 * @param tc the container holding the content to store. 
	 * @return a string representing the content of the given container.
	 */
	public static String contentToString (TextContainer tc) {
		StringBuilder tmp = new StringBuilder();
		tmp.append(tc.hasBeenSegmented() ? '1' : '0');
		for ( TextPart part : tc ) {
			// part to string
			tmp.append(part.isSegment() ? '1' : '0');
			tmp.append(part.text.getCodedText());
			tmp.append(PARTSEP1);
			tmp.append(Code.codesToString(part.text.getCodes()));
			tmp.append(PARTSEP1);
			if ( part.isSegment() ) {
				tmp.append(((Segment)part).id);
			}
			// end of part to string: next part
			tmp.append(PARTSEP2);
		}
		return tmp.toString();
	}
	
	/**
	 * Converts a string created by {@link #contentToString(TextContainer)}
         * back into a TextContainer.
         *
	 * @param data the string to process.
	 * @return a new TextConatiner with the stored content re-created.
	 */
	public static TextContainer stringToContent (String data) {
		TextContainer tc = new TextContainer();
		return tc.setContentFromString(data);
	}
	
	/**
	 * Sets content of this TextContainer from a string created by {@link #contentToString(TextContainer)}.
	 * @param data the string to process.
	 * @return this TextConatiner.
	 */
	public TextContainer setContentFromString (String data) {
		this.setHasBeenSegmentedFlag((data.charAt(0)=='1'));
		String[] chunks = data.substring(1).split(PARTSEP2, 0);
		this.parts.clear();
		for ( String chunk : chunks ) {
			int n1 = chunk.indexOf(PARTSEP1CHAR);
			int n2 = chunk.indexOf(PARTSEP1CHAR, n1+1);
			TextFragment tf = new TextFragment(chunk.substring(1, n1),
				Code.stringToCodes(chunk.substring(n1+1, n2)));
			this.parts.add((chunk.charAt(0)=='1')
				? new Segment(chunk.substring(n2+1), tf)
				: new TextPart(tf));
		}
		return this;
	}

	/**
	 * Create two storage strings to serialize a given {@link TextContainer}.
	 * Use {@link #splitStorageToContent(String, String)} to create the container back from the strings.
	 * <p>
         * <b>IMPORTANT:</b> Only the content is saved (not the properties, annotations, etc.).
	 *
         * @param tc the text container to store.
	 * @return An array of two {@link String} objects: The first one contains the coded text
	 * parts, the second one contains the codes.
	 * @see #splitStorageToContent(String, String)
	 */
	static public String[] contentToSplitStorage(TextContainer tc) {
		String res[] = new String[2];
		StringBuilder tmp1 = new StringBuilder();
		StringBuilder tmp2 = new StringBuilder();
		tmp1.append(tc.hasBeenSegmented() ? '1' : '0');
		for ( TextPart part : tc ) {
			tmp1.append(part.isSegment() ? '1' : '0');
			tmp1.append(part.text.getCodedText());
			tmp1.append(PARTSEP1);
			if ( part.isSegment() ) {
				tmp1.append(((Segment)part).id);
			}
			// end of part to string: next part
			tmp1.append(PARTSEP2);
			// Store the corresponding codes
			tmp2.append(Code.codesToString(part.text.getCodes()));
			tmp2.append(PARTSEP2);
		}
		res[0] = tmp1.toString();
		res[1] = tmp2.toString(); 
		return res;
	}

	/**
	 * Creates a new {@link TextContainer} object from two strings generated
         * with {@link #contentToSplitStorage(TextContainer)}.
         *
	 * @param ctext the string holding the coded text parts.
	 * @param codes the string holding the codes.
	 * @return a new {@link TextContainer} object created from the strings.
	 * @see #contentToSplitStorage(TextContainer)
	 */
	static public TextContainer splitStorageToContent(String ctext,
                                                          String codes) {
		TextContainer tc = new TextContainer();
		tc.parts.clear(); // Make sure the default empty part is removed
		// Un-encode the codes
		String[] codesParts = codes.split(PARTSEP2, -2);
		// Un-code the coded text and the segment info
		// <segmented?0|1>(<segment?0|1><codedtext><sep1>[<segId>]<sep2>)*
		tc.setHasBeenSegmentedFlag((ctext.charAt(0)=='1'));
		String[] chunks = ctext.substring(1).split(PARTSEP2, 0);
		// Now we have: <segment?0|1><codedtext><sep1>[<segId>]
		int i = 0;
		for ( String chunk : chunks ) {
			int n = chunk.indexOf(PARTSEP1CHAR);
			try {
			TextFragment tf = new TextFragment(chunk.substring(1, n), Code.stringToCodes(codesParts[i]));
			if ( (chunk.charAt(0)=='1') ) { // It is a segment
				tc.parts.add(new Segment(chunk.substring(n+1), tf));
			}
			else {
				tc.parts.add(new TextPart(tf));
			}
			}
			catch ( Throwable e ) {
				e.printStackTrace();
			}
			i++;
		}
		return tc;
	}

	/**
	 * Creates a new empty TextContainer object.
	 */
	public TextContainer() {
		createSingleSegment(null);
	}

	/**
	 * Creates a new TextContainer object with some initial text.
         *
	 * @param text the initial text.
	 */
	public TextContainer(String text) {
		createSingleSegment(text);
	}

	/**
	 * Creates a new TextContainer object with an initial TextFragment.
         *
	 * @param fragment the initial TextFragment.
	 */
	public TextContainer(TextFragment fragment) {
		setContent(fragment);
	}
	
	/**
	 * Creates a new TextContainer object with initial {@link TextPart}s (segment or non-segment) appended.
	 * @param parts the given initial parts.
	 */
	public TextContainer(TextPart... parts) {
		setParts(parts);
	}
	
	/**
	 * Creates a new TextContainer object with an initial segment.
	 * If the id of the segment is null it will be set automatically.
         *
	 * @param segment the initial segment.
	 */
	public TextContainer (Segment segment) {
		if ( segment.text == null ) {
			segment.text = new TextFragment();
		}
		resetParts();
		parts.add(segment);
		segments.validateSegmentId(segment);
	}

	/**
	 * Creates a new TextContainer object with optional text.
         *
	 * @param text the text, or null for not text.
	 */
	private void createSingleSegment (String text) {
		resetParts();
		// Note: don't use appendSegment() as it uses createSingleSegment().
		Segment seg = new Segment("0", new TextFragment(text));
		parts.add(seg);
		segApplied = false;
	}
	
        /**
         * Sets the parts variable to a new empty array list, and updates the
         * reference in segments
         */
        private void resetParts() {
            parts = new ArrayList<TextPart>();
            segments.setParts(parts);
        }

	/**
	 * Gets the string representation of this container.
	 * If the container is segmented, the representation shows the merged
	 * segments. Inline codes are also included.
         *
	 * @return the string representation of this container.
	 */
	@Override
	public String toString () {
		if ( parts.size() == 1 ) {
			return parts.get(0).getContent().toText();
		}
		// Else: merge to a temporary content
		return createJoinedContent().toText();
	}
	
	/**
	 * Creates an iterator to loop through the parts (segments and
         * non-segments) of this container.
         *
	 * @return a new iterator all for the parts of this container.
	 */
	public Iterator<TextPart> iterator () {
		return new Iterator<TextPart>() {
			int current = 0;
			
			@Override
			public void remove () {
				throw new UnsupportedOperationException("The method remove() not supported.");
			}
			
			@Override
			public TextPart next () {
				if ( current >= parts.size() ) {
					throw new NoSuchElementException("No more content parts.");
				}
				return parts.get(current++);
			}
			
			@Override
			public boolean hasNext () {
				return (current<parts.size());
			}
		};
	}

	/**
	 * Compares this container with another one. Note: This is a costly operation if
	 * the two containers have segments and no text differences.
	 *
	 * @param cont the other container to compare this one with.
	 * @param codeSensitive true if the codes need to be compared as well.
	 * @return a value 0 if the objects are equals.
	 */
	public int compareTo (TextContainer cont, boolean codeSensitive) {
		int res = 0;
		if ( cont.contentIsOneSegment() ) {
			if ( contentIsOneSegment() ) {
				// No ranges to compare
				return getFirstContent().compareTo(cont.getFirstContent(), codeSensitive);
			}
			else {
				res = getUnSegmentedContentCopy().compareTo(cont.getFirstContent(), codeSensitive);
			}
		}
		else {
			if ( contentIsOneSegment() ) {
				res = getFirstContent().compareTo(cont.getUnSegmentedContentCopy(), codeSensitive);
			}
			else {
				res = getUnSegmentedContentCopy().compareTo(cont.getUnSegmentedContentCopy(), codeSensitive);
			}
		}
		if ( res != 0 ) return res;
		
		// If the content is the same, check the segment boundaries and ids
		StringBuilder tmp1 = new StringBuilder();
		for ( Range range : segments.getRanges() ) {
			tmp1.append(range.toString());
		}
		StringBuilder tmp2 = new StringBuilder();
		for ( Range range : cont.getSegments().getRanges() ) {
			tmp2.append(range.toString());
		}
		return tmp1.toString().compareTo(tmp2.toString());
	}
	
	/**
	 * Indicates if a segmentation has been applied to this container. Note that it does not
	 * mean there is more than one segment or one part. Use {@link #contentIsOneSegment()} to
	 * check if the container counts only one segment (whether is is the result of a segmentation
	 * or simply the default single segment).
	 * <p>
         * This method return true if any method that may cause the content to be segmented
	 * has been called, and no operation has resulted in un-segmenting the content since that call,
	 * or if the content has more than one part.
         *
	 * @return true if a segmentation has been applied to this container.
	 * @see #setHasBeenSegmentedFlag(boolean)
	 */
	public boolean hasBeenSegmented() {
		return segApplied;
	}
	
	/**
	 * Sets the flag indicating if the content of this container has been segmented.
         *
	 * @param hasBeenSegmented true to flag the content has having been segmented, false to set it
	 * has not having been segmented.
	 * @see #hasBeenSegmented()
	 */
	public void setHasBeenSegmentedFlag(boolean hasBeenSegmented) {
		segApplied = hasBeenSegmented;
	}
	
	/**
	 * Indicates if this container is made of a single segment that holds the
	 * whole content (i.e. there is no other parts).
	 * <p>
         * When this method returns true, the methods {@link #getFirstContent()},
	 * {@link ISegments#getFirstContent()}, {@link #getLastContent()} and
	 * {@link ISegments#getLastContent()} return the same result.
         *
	 * @return true if the whole content of this container is in a single segment.
	 * @see #count()
	 * @see ISegments#count()
	 */
	public boolean contentIsOneSegment() {
		return (( parts.size() == 1 ) && parts.get(0).isSegment() );
	}
	
	/**
	 * Changes the type of a given part.
	 * If the part was a segment this makes it a non-segment (except if this is the only part
	 * in the content. In that case the part remains unchanged). If this part was not a segment
	 * this makes it a segment (with its identifier automatically set).
         *
	 * @param partIndex the index of the part to change. Note that even if the part is a segment
	 * this index must be the part index not the segment index.
	 */
	public void changePart(int partIndex) {
		if ( parts.get(partIndex).isSegment() ) {
			// If it's a segment, make it a non-segment
			if ( hasOnlyOneSegment() ) {
				// Except if it's the only segment, to ensure at-least-1-segment
				return; 
			}
			parts.set(partIndex, new TextPart(parts.get(partIndex).text));
		}
		else {
			// If it's a non-segment, make it a segment (with auto-id)
			Segment seg = new Segment(null, parts.get(partIndex).text);
			segments.validateSegmentId(seg);
			parts.set(partIndex, seg);
			segApplied = true;
		}
	}
	
	/**
	 * Inserts a given part (segment or non-segment) at a given position.
	 * If the position is already occupied that part and all the parts to
	 * it right are shifted to the right.
	 * <p>
         * If the part to insert is a segment, its id is validated.
         *
	 * @param partIndex the position where to insert the new part.
	 * @param part the part to insert.
	 */
	public void insert (int partIndex, TextPart part) {
		parts.add(partIndex, part);
		if ( part.isSegment() ) {
			segments.validateSegmentId((Segment)part);
		}
		segApplied = true;
	}
	
	/**
	 * Removes the part at s given position.
	 * <p>
         * If the selected part is the last segment in the content, the part
	 * is only cleared, not removed.
         *
	 * @param partIndex the position of the part to remove. 
	 */
	public void remove(int partIndex) {
		if ( parts.get(partIndex).isSegment() && hasOnlyOneSegment() ){
			// If it's the last segment, just clear it, don't remove it.
			parts.get(partIndex).text.clear();
		}
		else {
			parts.remove(partIndex);
		}
	}
	
	/**
	 * Appends a part at the end of this container.
	 * <p>
         * If collapseIfPreviousEmpty and if the current last part (segment or non-segment)
	 * is empty, the text fragment is appended to the last part.
	 * Otherwise the text fragment is appended to the content as a new non-segment part.
	 * <p>
         * Important: If the container is empty, the appended part becomes
	 * a segment, as the container has always at least one segment.
         *
	 * @param fragment the text fragment to append.
	 * @param collapseIfPreviousEmpty true to collapse the previous part if it is empty.
	 */
	public void append(TextFragment fragment,
                           boolean collapseIfPreviousEmpty) {
		append(fragment, collapseIfPreviousEmpty, false);
	}
	
	/**
	 * Appends a part at the end of this container.
	 * <p>
         * If collapseIfPreviousEmpty and if the current last part (segment or non-segment)
	 * is empty, the text fragment is appended to the last part.
	 * Otherwise the text fragment is appended to the content as a new non-segment part.
	 * <p>
         * Important: If the container is empty, the appended part becomes
	 * a segment, as the container has always at least one segment.
         *
	 * @param fragment the text fragment to append.
	 * @param collapseIfPreviousEmpty true to collapse the previous part if it is empty.
	 * @param keepCodeIds true to block code balancing.
	 */
	public void append(TextFragment fragment,
                           boolean collapseIfPreviousEmpty, boolean keepCodeIds) {
		if ( collapseIfPreviousEmpty ) {
			// If the last part is empty we append to it
			if ( parts.get(parts.size()-1).getContent().isEmpty() ) {
				// Append the fragment to the segment or non-segment part
				parts.get(parts.size()-1).text.append(fragment, keepCodeIds);
			}
			else { // Else: like appending a TextPart
				append(new TextPart(fragment), false);
			}
		}
		else {
			append(new TextPart(fragment), false);
		}
	}

	/**
	 * Appends a part at the end of this container.
	 * <p>
         * This call is the same as calling {@link #append(TextFragment, boolean)} with collapseIfPreviousEmpty
	 * set to true.
         *
	 * @param fragment the text fragment to append.
	 */
	public void append(TextFragment fragment) {
		append(fragment, true);
	}
	
	/**
	 * Appends a part with a given text at the end of this container.
	 * <p>
         * If collapseIfPreviousEmpty is true and if the current last part (segment or non-segment)
	 * is empty, the new text is appended to the last part part.
	 * Otherwise the text is appended to the content as a new non-segment part.
         *
	 * @param text the text to append.
	 * @param collapseIfPreviousEmpty true to collapse the previous part if it is empty.
	 */
	public void append (String text,
		boolean collapseIfPreviousEmpty)
	{
		append(new TextPart(text), collapseIfPreviousEmpty);
	}

	/**
	 * Appends a part with a given text at the end of this container.
	 * <p>
         * This call is the same as calling {@link #append(String, boolean)}
         * with collapseIfPreviousEmpty set to true.
         *
	 * @param text the text to append.
	 */
	public void append (String text) {
		append(text, true);
	}
	
	/**
	 * Appends a {@link TextPart} (segment or non-segment) at the end of this container.
	 * <p>If collapseiIfPreviousEmpty is true and if the current last part (segment or non-segment)
	 * is empty, the new part replaces the last part.
	 * Otherwise the part is appended to the container as it.
	 * If the result of the operation would result in a container without segment, the
	 * first part is automatically converted to a fragment.
	 * @param part the TextPart to append.
	 * @param collapseIfPreviousEmpty true to collapse the previous part if it is empty.
	 */
	public void append (TextPart part, boolean collapseIfPreviousEmpty) {
		// Use the segment method if it is a segemnt
		if ( part.isSegment() ) {
			getSegments().append((Segment)part, collapseIfPreviousEmpty);
			return;
		}
		
		// Else: do the normal append
		// If the last part is empty we append to it
		if ( collapseIfPreviousEmpty ) {
			if ( parts.get(parts.size()-1).getContent().isEmpty() ) {
				parts.set(parts.size()-1, part);
			}
			else {
				parts.add(part);
			}
		}
		else {
			parts.add(part);
		}

		if ( segments.count() == 0 ) {
			// We need to ensure there is at least one segment
			changePart(0);
		}
	}

	/**
	 * Appends a {@link TextPart} (segment or non-segment) at the end of this container.
	 * <p>
         * This call is the same as calling {@link #append(TextPart, boolean)}
         * with collapseIfPreviousEmpty set to true.
         *
	 * @param part the TextPart to append.
	 */
	public void append(TextPart part) {
		append(part, true);
	}
	
	/**
	 * Gets the coded text of the whole content (segmented or not).
	 * Use this method to compute segment boundaries that will be applied using
	 * {@link ISegments#create(int, int)} or {@link ISegments#create(List)} or other methods.
         *
	 * @return the coded text of the whole content to use for segmentation template.
	 * @see ISegments#create(int, int)
	 * @see ISegments#create(List)
	 */
	public String getCodedText() {
		if ( parts.size() == 1 ) {
			return parts.get(0).getContent().getCodedText();
		}
		else {
			return createJoinedContent().getCodedText();
		}
	}

	/**
	 * Splits a given part into two or three parts.
	 * <ul>
	 * <li>If end == start or end or -1 : A new part is created on the right side of the position.
	 * It has the same type as the original part.
	 * <li>If start == 0: A new part is created on the left side of the original part.
	 * <li>If the specified span is empty at either end of the part, or if it is equals to the
	 * whole length of the part: No change (it would result in an empty part).
	 * It has the type specified by spannedPartIsSegment.
	 * </ul>
         *
	 * @param partIndex index of the part to split.
	 * @param start start of the middle part to create.
	 * @param end position just after the last character of the middle part to create.
	 * @param spannedPartIsSegment true if the new middle part should be a segment,
	 * false if it should be a non-segment.
	 */
	public void split (int partIndex,
                           int start,
                           int end,
                           boolean spannedPartIsSegment) {
		// Get the part and adjust the end==-1 if needed
		TextPart part = parts.get(partIndex);
		if ( end == -1 ) {
			end = part.text.text.length();
		}
		if ( end < start ) {
			throw new InvalidPositionException(String.format(
				"Invalid segment boundaries: start=%d, end=%d.", start, end));
		}
		// If span is empty and at either ends
		if (( end-start == 0 ) && (( start == 0 ) || ( end == part.text.text.length() ))) {
			return; // Nothing to do
		}
		// If span is the same as the part
		if ( end-start >= part.text.text.length() ) {
			return; // Nothing to do
		}

		// Determine the index where to insert the new part
		int newPartIndex = partIndex+1;
		if ( start == 0 ) {
			newPartIndex = partIndex;
		}
		// Determine the type of the new part
		boolean newPartIsSegment = spannedPartIsSegment;
		if ( start == end ) {
			newPartIsSegment = part.isSegment();
			// And it's like inserting on the right
			end = part.text.text.length();
		}
		
		// If span starts at 0, or ends at fragment ends:
		// We need only to split in two parts
		if (( start == 0 ) || ( end == part.text.text.length() )) { 
			// Create the new part and copy the relevant content
			if ( newPartIsSegment ) {
				parts.add(newPartIndex, new Segment(null, part.text.subSequence(start, end)));
				segments.validateSegmentId((Segment)parts.get(newPartIndex));
			}
			else {
				parts.add(newPartIndex, new TextPart(part.text.subSequence(start, end)));
			}
			// Removes from the given part the content that was copied into the new part
			part.text.remove(start, end);
		}
		// Else: Span with content: A middle part (the new part) and a right part are to be created 
		else {
			// Create the middle part and copy the relevant content
			if ( newPartIsSegment ) {
				parts.add(newPartIndex, new Segment(null, part.text.subSequence(start, end)));
				segments.validateSegmentId((Segment)parts.get(newPartIndex));
			}
			else {
				parts.add(newPartIndex, new TextPart(part.text.subSequence(start, end)));
			}
			// Then create the additional new part:
			// On the right of the new part, and of the type of the old part
			if ( part.isSegment() ) {
				parts.add(newPartIndex+1, new Segment(null, part.text.subSequence(end, -1)));
				segments.validateSegmentId((Segment)parts.get(newPartIndex+1));
			}
			else {
				parts.add(newPartIndex+1, new TextPart(part.text.subSequence(end, -1)));
			}
			// Removes from the given part the content that was copied into the two new parts
			part.text.remove(start, -1);
		}
		segApplied = true;
	}

	/**
	 * Unwraps the content of this container.
	 * <p>This method replaces any sequences of white-spaces by a single space character.
	 * It also removes leading and trailing white-spaces if the parameter
	 * trimEnds is set to true.
	 * <p>White spaces in this context are #x9, #xA and #x20. #xD is not considered a whitespace as the
	 * content of a text container must have its line-breaks normalized to #xA.
	 * <p>If the container has more than one segment and if collapseMode mode is set:
	 * non-segments parts are normalized and removed if they end up empty. If the option
	 * is not set: the method preserve at least one space between segments, even if the
	 * segments are empty. 
	 * <p>Empty segments are always left.
	 * <p>Currently there is no provision to not unwrap a given span of the content.
     * @param trimEnds true to remove leading and trailing white-spaces.
     * @param collapseMode true to remove non-segments parts that end up empty after the unwrapping.
	 */
	public void unwrap (boolean trimEnds,
		boolean collapseMode)
	{
		boolean wasWS = trimEnds; // Removes leading white-spaces
		
		for ( int i=0; i<parts.size(); i++ ) {
			StringBuilder text = parts.get(i).getContent().text;
			
			// Normalize the part
			for ( int j=0; j<text.length(); j++ ) {
				switch ( text.charAt(j) ) {
				case TextFragment.MARKER_OPENING:
				case TextFragment.MARKER_CLOSING:
				case TextFragment.MARKER_ISOLATED:
					j++;
					wasWS = false;
					//TODO: Do we need to do something for inline between WS?
					break;
				//case '\r': We should not have \r in text as line-break in a TextContainer.
				// If it's there it's a literal e.g. from XML #xD;
				case ' ':
				case '\t':
				case '\n':
					if ( wasWS ) {
						text.deleteCharAt(j);
						j--; // Adjust
					}
					else {
						text.setCharAt(j, ' ');
						wasWS = true;
					}
					break;
				default:
					wasWS = false;
					break;
				}
			}

			// Adjust WS flag for next part in non-collapse mode:
			if ( parts.get(i).isSegment() ) {
				if (( parts.size() > i+1 ) && !parts.get(i+1).isSegment() ) {
					if ( text.toString().trim().length() == 0 ) {
						wasWS = collapseMode;
					}
				}
			}
			
			// Remove the part if it's empty and not a segment
			if ( text.length() == 0 ) {
				if ( !parts.get(i).isSegment() ) {
					parts.remove(i);
					i--; // Adjust
				}
			}
		}

		// Trim the tail parts
		if ( trimEnds ) {
			for ( int i=parts.size()-1; i>=0; i-- ) {
				TextPart part = parts.get(i);
				if ( part.text.getCodedText().endsWith(" ") ) {
					// Remove the trailing space
					part.text.text.deleteCharAt(part.text.text.length()-1);
					// Stop if not empty, or remove empty non-segment
					if ( part.text.text.length() == 0 ) {
						if ( !parts.get(i).isSegment() ) {
							parts.remove(i);
							// No need to adjust when going backward
						}
					}
					else break;
				}
				else break;
			}
		}
	}
	
	/**
	 * Gets the content of the first part (segment or non-segment) of this container.
	 * <p>This method always returns the same result as {@link ISegments#getFirstContent()}
	 * if {@link #contentIsOneSegment()} is true.
	 * @return the content of the first part (segment or non-segment) of this container.
	 * @see ISegments#getFirstContent()
	 * @see #getLastContent()
	 * @see ISegments#getLastContent()
	 */
	public TextFragment getFirstContent() {
		return parts.get(0).text;
	}
	
	/**
	 * Returns the first {@link Segment} of this container.
	 * @return the first {@link Segment} of this container or null if there is no {@link Segment}
	 */
	public Segment getFirstSegment() {
		for ( TextPart part : parts ) {
			if (part.isSegment()) {
				return (Segment) part;
			}
		}
		return null;
	}	
	
	/**
	 * Gets the content of the last part (segment or non-segment) of this container. 
	 * <p>This method always returns the same result as {@link ISegments#getLastContent()} if {@link #contentIsOneSegment()}.
     * @return the content of the last part (segment or non-segment) of this container.
	 * @see ISegments#getLastContent()
	 * @see #getFirstContent()
	 * @see ISegments#getFirstContent()
	 */
	public TextFragment getLastContent () {
		return parts.get(parts.size()-1).text;
	}
	
	/**
	 * Clones this TextContainer, including the properties.
	 *
         * @return A new TextContainer object that is a copy of this one.
	 */
	@Override
	public TextContainer clone () {
		return clone(true);
	}

	/**
	 * Clones this container, with or without its properties. 
	 * @param cloneProperties indicates if the properties should be cloned.
	 * @return A new TextContainer object that is a copy of this one.
	 */
	public TextContainer clone (boolean cloneProperties) {
		TextContainer newCont = new TextContainer();
		// Clone segments
		newCont.resetParts();
		for ( TextPart part : parts ) {
			newCont.parts.add(part.clone());
		}
		newCont.segApplied = segApplied; 
		newCont.segments.setAlignmentStatus(segments.getAlignmentStatus());
		// Clone the properties
		if ( cloneProperties && ( properties != null )) {
			newCont.properties = new LinkedHashMap<String, Property>();
			for ( Property prop : properties.values() ) {
				newCont.properties.put(prop.getName(), prop.clone()); 
			}
		}
		// Clone the annotations
		if ( annotations != null ) {
			newCont.annotations = annotations.clone();
		}
		// Returns the new container
		return newCont;
	}
	
	/**
	 * Gets a new TextFragment representing the un-segmented content of this container.
	 * <p>
         * <b>Important:</b> This is an expensive method.
         *
	 * @return an un-segmented copy of the content of this container.
	 */
	public TextFragment getUnSegmentedContentCopy() {
		return createJoinedContent();
	}

	/**
	 * Sets the content of this TextContainer.
	 * Any existing segmentation is removed.
	 * The content becomes a single segment content.
         *
	 * @param content the new content to set.
	 */
	public void setContent(TextFragment content) {
		createSingleSegment(null);
		((Segment)parts.get(0)).text = content;
	}
	
	public void setParts(TextPart... parts) {
		resetParts();
		for (TextPart part : parts) {
			this.parts.add(part);
		}
		segApplied = segments.count() > 0;
		if ( segments.count() == 0 ) {
			if (this.parts.size() > 0) {
				changePart(0);
			}
			else {
				createSingleSegment(null);
			}
		}
	}

	/**
	 * Clears this TextContainer, removes any existing segments.
	 * The content becomes a single empty segment content.
	 * Keeps annotations.
	 */
	public void clear() {
		createSingleSegment(null);
	}
	
	/**
	 * Indicates if this container contains at least one character.
	 * Inline codes and annotation markers do not count as characters.
	 * <ul>
	 * <li>If the whole content is a single segment the check is performed on that
	 * content and the option lookInSegments is ignored.
	 * <li>If the content has several segments or if the single segment is not
	 * the whole content, each segment is checked only if lookInSegment is set.
	 * <li>The holder is always checked if no text is found in the segments.
	 * </ul>
         *
	 * @param lookInSegments indicates if the possible segments in this containers should be
	 * looked at. If this parameter is set to false, the segment marker are treated as codes.
	 * @param whiteSpacesAreText indicates if whitespaces should be considered 
	 * text characters or not.
	 * @return true if this container contains at least one character according the
	 * given options.
	 */
	public boolean hasText (boolean lookInSegments,
		boolean whiteSpacesAreText)
	{
		for ( TextPart part : parts ) {
			if ( part.isSegment() ) {
				if ( lookInSegments ) {
					if ( part.getContent().hasText(whiteSpacesAreText) ) return true;
				}
			}
			else {
				if ( part.getContent().hasText(whiteSpacesAreText) ) return true;
			}
		}
		return false; // No text
	}

	/**
	 * Indicates if this container contains at least one character that is not a whitespace.
	 * All parts (segments and non-segments) are checked.
         *
	 * @param whiteSpacesAreText indicates if whitespaces should be considered 
	 * text characters or not.
	 * @return true if this container contains at least one character that is not a whitespace.
	 */
	public boolean hasText (boolean whiteSpacesAreText) {
		for ( TextPart part : parts ) {
			if ( part.getContent().hasText(whiteSpacesAreText) ) return true;
		}
		return false;
	}
	
	/**
	 * Indicates if this fragment contains at least one character that is 'text' 
	 * (inline codes, segment markers, and annotation markers do not count as 'text' characters).
	 * This method has the same result as calling {@link #hasText(boolean, boolean)}
         * with the parameters true and false.
         *
	 * @return true if this container contains at least one character that is not a whitespace.
	 */
	public boolean hasText() {
		return hasText(false);
	}
	
	/**
	 * Indicates if this container is empty (no text and no codes).
         *
	 * @return true if this container is empty.
	 */
	public boolean isEmpty() {
		for ( TextPart part : parts ) {
			if ( !part.getContent().isEmpty() ) return false;
		}
		return true;
	}
	
	@Override
	public boolean hasProperty (String name) {
		return (getProperty(name) != null);
	}
	
	@Override
	public Property getProperty (String name) {
		if ( properties == null ) return null;
		return properties.get(name);
	}

	@Override
	public Property setProperty (Property property) {
		if ( properties == null ) properties = new LinkedHashMap<String, Property>();
		properties.put(property.getName(), property);
		return property;
	}
	
	@Override
	public void removeProperty (String name) {
		if ( properties != null ) {
			properties.remove(name);
		}
	}
	
	@Override
	public Set<String> getPropertyNames () {
		if ( properties == null ) properties = new LinkedHashMap<String, Property>();
		return properties.keySet();
	}

	@Override
	public Annotations getAnnotations() {
		return (annotations == null) ? new Annotations() : annotations;
	}

	@Override
	public <A extends IAnnotation> A getAnnotation (Class<A> type) {
		if ( annotations == null ) return null;
		return annotations.get(type);
	}

	@Override
	public void setAnnotation (IAnnotation annotation) {
		if ( annotations == null ) annotations = new Annotations();
		annotations.set(annotation);
	}
	
	/**
	 * Gets the part (segment or non-segment) for a given part index.
         *
	 * @param index the index of the part to retrieve. the first part has the index 0,
	 * the second has the index 1, etc.
	 * @return the part (segment or non-segment) for the given index.
	 * @throws IndexOutOfBoundsException if the index is out of bounds.
	 * @see ISegments#get(int)
	 */
	public TextPart get (int index) {
		return parts.get(index);
	}
	
	/**
	 * Gets the number of parts (segments and non-segments) in this container.
	 * This method always returns at least 1.
         *
	 * @return the number of parts (segments and non-segments) in this container.
	 * @see ISegments#count()
	 */
	public int count () {
		return parts.size();
	}
	
	/**
	 * Indicates if this container has a single segment. Note that it may have also non-segment parts.
	 * Use {@link #contentIsOneSegment()} to check if the container is made of a asingle segment without
	 * any additional non-segment parts.
         *
	 * @return true if this container has a single segment.
	 * @see #contentIsOneSegment()
	 */
	private boolean hasOnlyOneSegment () {
		return (segments.count() == 1);
	}
	
	public TextFragment createJoinedContent () {
		// Join all segment into a new TextFragment
		TextFragment tf = new TextFragment();
		for ( TextPart part : parts ) {
			tf.append(part.getContent());
		}
		return tf;
	}

	/**
	 * Merges back together all parts (segments and non-segments) of this container,
	 * and clear the list of segments. The content becomes a single segment content.
	 */
	public void joinAll () {
		//setContent(createJoinedContent(null));
            segments.joinAll();
	}

	/**
	 * Joins a given part with a specified number of its following parts.
	 * <p>
         * If the resulting part is the only part in the container and is not a segment,
	 * it is set automatically changed into a segment. 
	 * <p>
         * joinWithNext(0, -1) has the same effect as joinAll();
	 *
         * @param partIndex the index of the part where to append the following parts.
	 * @param partCount the number of parts to join. You can use -1 to indicate all the parts
	 * after the initial one. 
	 * @return the number of parts joined to the given part (and removed from the list of parts). 
	 */
	public int joinWithNext (int partIndex,
		int partCount)
	{
		if ( parts.size() == 1 ) {
			return 0; // Nothing to do
		}
		
		TextFragment tf = parts.get(partIndex).getContent();
		int max = (parts.size()-partIndex)-1;
		if (( partCount == -1 ) || ( partCount > max )) {
			partCount = max;
		}
		int i = 0;
		while ( i < partCount ) {
			tf.append(parts.get(partIndex+1).getContent());
			parts.remove(partIndex+1);
			i++;
		}

		// Check single part case
		if ( parts.size() == 1 ) {
			if ( !parts.get(0).isSegment() ) {
				// Ensure we have always at least one segment
				parts.set(0, new Segment(null, parts.get(0).text));
			}
			// Do not reset segApplied if one part only: keep the info that is was segmented
		}
		return i;
	}

	public List<TextPart> getParts() {
		return parts;
	}

	
	//////////////////////////////////////////////////
	// for serialization only
	//////////////////////////////////////////////////

	protected LinkedHashMap<String, Property> getProperties() {
		return properties;
	}

	protected void setProperties(LinkedHashMap<String, Property> properties) {
		this.properties = properties;
	}

	protected void setSegments(Segments segments) {
		this.segments = segments;
	}

	protected void setAnnotations(Annotations annotations) {
		this.annotations = annotations;
	}

	
	//////////////////////////////////////////////////
	// Intended to not be implemented
	//////////////////////////////////////////////////

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public String getId () {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public void setId (String id) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public ISkeleton getSkeleton () {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public void setSkeleton (ISkeleton skeleton) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method always returns null.
	 * It is intentionally not supported with TextContainer.
	 */
	@Override
	public String getName () {
		return null;
		//throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public void setName (String name) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public String getType () {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public void setType (String value) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method always returns null.
	 * It is intentionally not supported with TextContainer.
	 */
	@Override
	public String getMimeType () {
		return null;
		//throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public void setMimeType (String value) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public Property getSourceProperty (String name) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public Property setSourceProperty (Property property) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public void removeSourceProperty (String name) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public Set<String> getSourcePropertyNames () {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public boolean hasSourceProperty (String name) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public Property getTargetProperty (LocaleId locId, String name) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public Property setTargetProperty (LocaleId locId, Property property) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public void removeTargetProperty (LocaleId locId, String name) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public Set<String> getTargetPropertyNames (LocaleId locId) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public Set<LocaleId> getTargetLocales () {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public boolean hasTargetProperty (LocaleId locId, String name) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public Property createTargetProperty (LocaleId locId,
		String name,
		boolean overwriteExisting,
		int creationOptions)
	{
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public boolean isTranslatable () {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public void setIsTranslatable (boolean value) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public boolean preserveWhitespaces () {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}

	/**
	 * This method is intentionally not supported with TextContainer.
	 */
	@Override
	public void setPreserveWhitespaces (boolean value) {
		throw new UnsupportedOperationException("This method is intentionally not implemented for TextContainer.");
	}
	
}
