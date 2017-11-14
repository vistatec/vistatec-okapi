/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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
import java.util.List;
import java.util.NoSuchElementException;

import net.sf.okapi.common.Range;
import net.sf.okapi.common.Util;

public class Segments implements ISegments {
	// assume segments are always aligned when created
    private AlignmentStatus alignmentStatus = AlignmentStatus.ALIGNED;
    private TextContainer parent;
    private List<TextPart> parts;

    public Segments() {
	}
    
    /**
     * Creates an uninitialised Segments object.
     * <p>
     * <b>IMPORTANT:</b> setParts() must be called with
     * a non-null argument before calling any other methods.
     * @param parent the parent {@link TextContainer}.
     */
    public Segments(TextContainer parent) {
        this.parent = parent;

    }

    /**
     * Sets the list of TextPart objects in which the segments for this Segments
     * object are located. Parts must be set after construction before any other
     * methods are invoked.
     *
     * @param parts the list of {@link TextPart}s where the segments are stored.
     */
    public void setParts(List<TextPart> parts) {
        this.parts = parts;
    }

    public Iterator<Segment> iterator() {
            return new Iterator<Segment>() {
                    int current = foundNext(-1);
                    private int foundNext (int start) {
                            for ( int i=start+1; i<parts.size(); i++ ) {
                                    if ( parts.get(i).isSegment() ) {
                                            return i;
                                    }
                            }
                            return -1;
                    }

                    @Override
                    public void remove () {
                            throw new UnsupportedOperationException("The method remove() not supported.");
                    }

                    @Override
                    public Segment next () {
                            if ( current == -1 ) {
                                    throw new NoSuchElementException("No more content parts.");
                            }
                            int n = current;
                            // Get next here because hasNext() could be called several times
                            current = foundNext(current);
                            // Return 'previous' current
                            return (Segment)parts.get(n);
                    }

                    @Override
                    public boolean hasNext () {
                            return (current != -1);
                    }
            };
    };

    @Override
    public List<Segment> asList() {
            ArrayList<Segment> segments = new ArrayList<Segment>();
            for ( TextPart part : parts ) {
                    if ( part.isSegment() ) {
                            segments.add((Segment)part);
                    }
            }
            return segments;
    }

    @Override
    public void swap(int segIndex1, int segIndex2) {
            int partIndex1 = getPartIndex(segIndex1);
            int partIndex2 = getPartIndex(segIndex2);
            if (( partIndex1 == -1 ) || ( partIndex2 == -1 )) {
                    return; // At least one index is wrong: do nothing
            }
            TextPart tmp = parts.get(partIndex1);
            parts.set(partIndex1, parts.get(partIndex2));
            parts.set(partIndex2, tmp);
    }


    @Override
    public void append(Segment segment, boolean collapseIfPreviousEmpty) {
            append(segment, null, collapseIfPreviousEmpty);
    }

    @Override
    public void append(Segment segment) {
            append(segment, true);
    }

    @Override
    public void append(Segment segment,
                       String textBefore,
                       boolean collapseIfPreviousEmpty) {
            // Add the text before if needed
            if ( !Util.isEmpty(textBefore) ) {
                    if (( parts.get(parts.size()-1).getContent().isEmpty() )
                            && !parts.get(parts.size()-1).isSegment() )
                    {
                            parts.set(parts.size()-1, new TextPart(textBefore));
                    }
                    else {
                            parts.add(new TextPart(textBefore));
                    }
            }

            // If the last segment is empty and at the end of the content: re-use it
            if ( collapseIfPreviousEmpty ) {
                    if (( parts.get(parts.size()-1).getContent().isEmpty() )
                            && parts.get(parts.size()-1).isSegment() )
                    {
                            parts.set(parts.size()-1, segment);
                    }
                    else {
                            parts.add(segment);
                    }
            }
            else {
                    parts.add(segment);
            }

            validateSegmentId(segment);
            parent.setHasBeenSegmentedFlag(true);
    }

    @Override
    public void append(Segment segment, String textBefore) {
            append(segment, textBefore, true);
    }

    @Override
    public void append(TextFragment fragment, boolean collapseIfPreviousEmpty) {
            append(new Segment(null, fragment), collapseIfPreviousEmpty);
    }

    @Override
    public void append(TextFragment fragment) {
            append(fragment, true);
    }

    @Override
    public void set(int index, Segment seg) {
            int n = getPartIndex(index);
            if ( n < -1 ) {
                    throw new IndexOutOfBoundsException("Invalid segment index: "+index);
            }
            parts.set(n, seg);
            validateSegmentId(seg);
    }

    @Override
    public void insert(int index, Segment seg) {
            // If the index is the one after the last segment: we append
            if ( index == count() ) {
                    append(seg, true);
                    return;
            }
            // Otherwise it has to exist
            int n = getPartIndex(index);
            if ( n < -1 ) {
                    throw new IndexOutOfBoundsException("Invalid segment index: "+index);
            }
            parts.add(n, seg);
            validateSegmentId(seg);
    }

    @Override
    public int create (List<Range> ranges) {
    	return create(ranges, false);
    }
    
    @Override
    public int create (List<Range> ranges,
    	boolean allowEmptySegments)
    {
            // Do nothing if null or empty
            if (( ranges == null ) || ranges.isEmpty() ) return 0;

            // If the current content is a single segment we start from it
            TextFragment holder;
            if ( parts.size() == 1  ) {
                    holder = parts.get(0).getContent();
            }
            else {
                    holder = createJoinedContent(null);
            }

            // Reset the segments
            parts.clear();

            // Extract the segments using the ranges
            int start = 0;
            int id = 0;
            for ( Range range : ranges ) {
                    if ( range.end == -1 ) {
                            range.end = holder.text.length();
                    }
                    // Check boundaries
                    if ( range.end < range.start ) {
                            throw new InvalidPositionException(String.format(
                                    "Invalid segment boundaries: start=%d, end=%d.", range.start, range.end));
                    }
                    if ( start > range.start ) {
                            throw new InvalidPositionException("Invalid range order.");
                    }
                    if ( range.end == range.start ) {
                    	// If empty segments are not allowed, we skip this one
                    	if ( !allowEmptySegments ) continue;
                    	// Otherwise we proceed
                    }
                    // If there is an interstice: creates the corresponding part
                    if ( start < range.start ) {
                            parts.add(new TextPart(holder.subSequence(start, range.start)));
                    }
                    // Create the part for the segment
                    // Use existing id if possible, otherwise use local counter
                    Segment seg = new Segment(((range.id == null) ? String.valueOf(id++) : range.id),
                            holder.subSequence(range.start, range.end));
                    parts.add(seg);
                    validateSegmentId(seg);
                    start = range.end;
                    parent.setHasBeenSegmentedFlag(true);
            }

            // Check if we have remaining text after the last segment
            if ( start < holder.text.length() ) {
                    if ( start == 0 ) { // If the remain is the whole content: make it a segment
                    	if ( parts.size() > 0 ) {
                    		parts.add(new TextPart(holder.subSequence(start, -1)));
                    	}
                    	else {
                            parts.add(new Segment(String.valueOf(id), holder));
                    	}
                            // That is the only segment: no need to validate the id
                    }
                    else { // Otherwise: make it an interstice
                    	parts.add(new TextPart(holder.subSequence(start, -1)));
                    }
            }

            return parts.size();
    }

    @Override
    public int create(int start, int end) {
            ArrayList<Range> range = new ArrayList<Range>();
            range.add(new Range(start, end));
            return create(range);
    }

    @Override
    public int count() {
            int count = 0;
            for ( TextPart part : parts ) {
                    if ( part.isSegment() ) {
                            count++;
                    }
            }
            return count;
    }

    @Override
    public TextFragment getFirstContent() {
            for ( TextPart part : parts ) {
                    if ( part.isSegment() ) {
                            return part.getContent();
                    }
            }
            // Should never occur
            return null;
    }

    @Override
    public TextFragment getLastContent() {
            for ( int i=parts.size()-1; i>=0; i-- ) {
                    if ( parts.get(i).isSegment() ) {
                            return parts.get(i).getContent();
                    }
            }
            // Should never occur
            return null;
    }

    @Override
    public Segment getLast() {
            for ( int i=parts.size()-1; i>=0; i-- ) {
                    if ( parts.get(i).isSegment() ) {
                            return (Segment)parts.get(i);
                    }
            }
            // Should never occur
            return null;
    }

    @Override
    public Segment get(String id) {
            for ( TextPart part : parts ) {
                    if ( part.isSegment() ) {
                            if ( ((Segment)part).id.equals(id) ) return (Segment)part;
                    }
            }
            // Should never occur
            return null;
    }

    @Override
    public Segment get(int index) {
        int tmp = -1;
        for ( TextPart part : parts ) {
            if ( part.isSegment() ) {
                if ( ++tmp == index ) {
                    return (Segment)part;
                }
            }
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + ++tmp);
    }

    @Override
    public void joinAll() {
            // Merge but don't remember the ranges
            //parent.setContent(createJoinedContent(null));
            joinAll(null);
    }

    @Override
    public void joinAll(List<Range> ranges) {
            parent.setContent(createJoinedContent(ranges));
    }

    @Override
    public List<Range> getRanges() {
            List<Range> ranges = new ArrayList<Range>();
            createJoinedContent(ranges);
            return ranges;
    }

    @Override
    public int joinWithNext(int segmentIndex) {
            // Check if we have something to join to
            if ( parts.size() == 1 ) {
                    return 0; // Nothing to do
            }

            // Find the part for the segment index
            int start = getPartIndex(segmentIndex);
            // Check if we have a segment at such index
            if ( start == -1 ) {
                    return 0; // Not found
            }

            // Find the next segment
            int end = -1;
            for ( int i=start+1; i<parts.size(); i++ ) {
                    if ( parts.get(i).isSegment() ) {
                            end = i;
                            break;
                    }
            }

            // Check if we have a next segment
            if ( end == -1 ) {
                    // No more segment to join
                    return 0;
            }

            TextFragment tf = parts.get(start).getContent();
            int count = (end-start);
            int i = 0;
            while ( i < count ) {
                    tf.append(parts.get(start+1).getContent());
                    parts.remove(start+1);
                    i++;
            }

            // Do not reset segApplied if one part only: keep the info that is was segmented
            return count;
    }

    @Override
    public int getPartIndex(int segIndex) {
            int n = -1;
            for ( int i=0; i<parts.size(); i++ ) {
                    if ( parts.get(i).isSegment() ) {
                            n++;
                            if ( n == segIndex ) return i;
                    }
            }
            return -1; // Not found
    }

    @Override
    public int getIndex(String segId) {
            int n = 0;
            for ( int i=0; i<parts.size(); i++ ) {
                    if ( parts.get(i).isSegment() ) {
                            if ( segId.equals(((Segment)parts.get(i)).id) ) return n;
                            // Else, move to the next
                            n++;
                    }
            }
            return -1; // Not found
    }

    @Override
    public AlignmentStatus getAlignmentStatus() {
            return alignmentStatus;
    }

    @Override
    public void setAlignmentStatus(AlignmentStatus alignmentStatus) {
            this.alignmentStatus = alignmentStatus;
    }

    /**
     * Checks if the id of a given segment is empty, null or a duplicate. If it is, the id
     * is automatically set to a new value auto-generated.
     * @param seg the segment to verify and possibly modify.
     */
    public void validateSegmentId(Segment seg) {
            if ( !Util.isEmpty(seg.id) ) {
                    // If not null or empty: check if it is a duplicate
                    boolean duplicate = false;
                    for ( TextPart tmp : parts ) {
                            if ( !tmp.isSegment() ) continue;
                            if ( seg == tmp ) continue;
                            if ( seg.id.equals(((Segment)tmp).id) ) {
                                    duplicate = true;
                                    break;
                            }
                    }
                    if ( !duplicate ) return; // Not a duplicate, nothing to do
            }

            // If duplicate or empty or null: assign a default id
            int value = 0;
            for ( TextPart tmp : parts ) {
                    if ( tmp == seg ) continue; // Skip over the actual segment
                    if ( !tmp.isSegment() ) continue; // Skip over non-segment
                    // If it starts with a digit, it's probably a number
                    if ( Character.isDigit(((Segment)tmp).id.charAt(0)) ) {
                            // try to convert the id to a integer
                            try {
                                    int val = Integer.parseInt(((Segment)tmp).id);
                                    // Make the new id the same +1
                                    if ( value <= val ) value = val+1;
                            }
                            catch ( NumberFormatException ignore ) {
                                    // Not really an error, just a non-numeric id
                            }
                    }
            }
            // Set the auto-value
            seg.id = String.valueOf(value);
    }

    private TextFragment createJoinedContent(List<Range> ranges) {
        // Clear the ranges if needed
        if ( ranges != null ) {
            ranges.clear();
        }
        // Join all segment into a new TextFragment
        int start = 0;
        TextFragment tf = new TextFragment();
        for ( TextPart part : parts ) {
            if (( ranges != null ) && part.isSegment() ) {
                ranges.add(new Range(start, start+part.text.text.length(), ((Segment)part).id));
            }
            start += part.text.text.length();
            tf.append(part.getContent());
        }
        return tf;
    }

	public TextContainer getParent() {
		return parent;
	}

	public List<TextPart> getParts() {
		return parts;
	}
}
