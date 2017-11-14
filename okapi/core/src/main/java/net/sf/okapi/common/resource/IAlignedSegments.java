/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiMisAlignmentException;

/**
 * Provides the methods to access all the source and target {@link Segment}s of
 * a {@link ITextUnit}.
 * <p>
 * To create an instance of this interface, use the method
 * {@link ITextUnit#getAlignedSegments()}.
 * </p>
 */
public interface IAlignedSegments extends Iterable<Segment> {

	/**
	 * Gets an iterator for the default source segments of this text unit. This
	 * iterator does not iterate through non-segment parts of the content.
	 *
	 * @return an iterator for the source segments of this text unit.
	 */
	@Override
	public Iterator<Segment> iterator();

	/**
	 * Gets an iterator for the source of the specified target locale. This
	 * iterator does not iterate through non-segment parts of the content.
	 *
	 * @param trgLoc
	 *            the target locale for the source to iterate over.
	 * @return an iterator for the source segments used for trgLoc.
	 */
	public Iterator<Segment> iterator(LocaleId trgLoc);

	/**
	 * Adds given source and target segments to the end of the content.
	 * <p>
	 * If srcSeg is non-null, the content of srcSeg will be used for any new
	 * segments that are not empty, otherwise the content of trgSeg will be
	 * used. srcSeg and trgSeg cannot both be null.
	 * </p>
	 *
	 * @param srcSeg
	 *            the source segment to add.
	 * @param trgSeg
	 *            the target segment to add. Null to use a clone of srcSeg
	 *            instead.
	 * @param trgLoc
	 *            the target locale for which to append segments
	 *
	 * @throws IllegalArgumentException
	 *             if srcSeg and trgSeg are both null
	 */
	public void append(Segment srcSeg, Segment trgSeg, LocaleId trgLoc);

	/**
	 * Inserts given source and target segments at the specified position in the
	 * list of segments.
	 * <p>
	 * The validated id (after insertion) of srcSeg will be applied to all other
	 * inserted segments, including trgSeg.
	 * </p>
	 *
	 * @param index
	 *            the segment index position.
	 * @param srcSeg
	 *            the source segment to insert.
	 * @param trgSeg
	 *            the target segment to insert. Null to use srcSeg instead.
	 * @param trgLoc
	 *            the target locale for which to insert the segment
	 *
	 * @throws IllegalArgumentException
	 *             if srcSeg is null
	 */
	public void insert(int index, Segment srcSeg, Segment trgSeg, LocaleId trgLoc);

	/**
	 * Replaces a segment at a given position with a clone of the given segment.
	 * <p>
	 * The segment id is determined by the segment at the position of index in
	 * the source for trgLoc, the segment id is then used to locate the segments
	 * in other sources and targets to replace.
	 * </p>
	 *
	 * @param index
	 *            the segment index position
	 * @param seg
	 *            the new segment to place at the position
	 * @param trgLoc
	 *            the target locale 
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 * @throws IllegalArgumentException
	 *             if seg is null
	 */
	public void setSegment(int index, Segment seg, LocaleId trgLoc);

	/**
	 * Removes the given segment and any segments with the same id from the
	 * specified sources and targets.
	 *
	 * @param seg
	 *            the segment to remove.
	 * @param trgLoc
	 *            the locale used in specifying which sources and targets to
	 *            use.
	 * @return true if remove success
	 */
	public boolean remove(Segment seg, LocaleId trgLoc);

	/**
	 * Gets the source segment for the given target locale at a given position.
	 * <p>
	 * The first segment has the index 0, the second has the index 1, etc.
	 * </p>
	 *
	 * @param index
	 *            the segment index of the segment to retrieve.
	 * @param trgLoc
	 *            the target locale for the source from which to retrieve the
	 *            indicated segment.
	 * @return the segment at the given position.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of bounds.
	 */
	public Segment getSource(int index, LocaleId trgLoc);

	/**
	 * Gets the target segment corresponding to a given source segment
	 *
	 * @param seg
	 *            the source (or other target) segment for which a corresponding
	 *            target segment is requested.
	 * @param trgLoc
	 *            the target to look up.
	 * @return the corresponding target {@link Segment} (may be empty).
	 */
	public Segment getCorrespondingTarget(Segment seg, LocaleId trgLoc);

	/**
	 * Gets the source segment corresponding to a given target segment
	 *
	 * @param trgSeg
	 *            the target segment for which the corresponding source segment
	 *            is requested.
	 * @param trgLoc
	 *            the target locale of the source to look up.
	 * @return the source segment.
	 */
	public Segment getCorrespondingSource(Segment trgSeg, LocaleId trgLoc);

	/**
	 * Aligns all the segments listed in the aligned pairs for given locale.
	 * <p>
	 * This will replace any content in the target and variant source for the
	 * given target locale (if either are not present they will be created).
	 * </p>
	 * <p>
	 * The target for the given locale will be considered aligned with its
	 * source when this operation is complete.
	 * </p>
	 *
	 * @param alignedSegmentPairs
	 *            the list of pairs to align
	 * @param trgLoc
	 *            the target locale to work with.
	 */
	public void align(List<AlignedPair> alignedSegmentPairs, LocaleId trgLoc);

	/**
	 * Aligns all the target segments with the source segments for the given
	 * locale.
	 * <p>
	 * Assumes the same number of source and target segments otherwise an
	 * exception is thrown.
	 * </p>
	 *
	 * @param trgLoc
	 *            the locale of the target to work with.
	 * @throws OkapiMisAlignmentException
	 *             if there are a different number of source and target
	 *             segments.
	 */
	public void align(LocaleId trgLoc);

	/**
	 * Aligns all the segments for the specified sources and targets by
	 * collapsing all segments into one.
	 *
	 * @param trgLoc
	 *            the target locale of the target (and its corresponding source)
	 *            to collapse.
	 */
	public void alignCollapseAll(LocaleId trgLoc);

	/**
	 * Splits a given source segment into two.
	 * <p>
	 * Alignment statuses are updated for the locales that have been modified. 
	 * <b>May cause a misalignment</b>.
	 * </p>
	 *
	 * @param trgLoc
	 *            the target locale that uses the source in which the segment is
	 *            to be split
	 * @param srcSeg
	 *            the source segment to split.
	 * @param splitPos
	 *            the position where to split.
	 * @return the new source segment created, or null if none was created.
	 */
	public Segment splitSource(LocaleId trgLoc, Segment srcSeg, int splitPos);

	/**
	 * Splits a given target segment into two.
	 * <p>
	 * Alignment statuses are updated for the locales that have been modified. 
	 * <b>May cause a misalignment</b>.
	 * </p>
	 *
	 * @param trgLoc
	 *            the target locale to work on.
	 * @param trgSeg
	 *            the targets segment.
	 * @param splitPos
	 *            the position where to split.
	 * @return the new target segment created, or null if none was created.
	 */
	public Segment splitTarget(LocaleId trgLoc, Segment trgSeg, int splitPos);

	/**
	 * Joins the segment for a given segment's id to the next segment, including
	 * all the parts between the two segments.
	 *
	 * @param seg
	 *            a segment holding the id to use for the join.
	 * @param trgLoc
	 *            the target locale
	 */
	public void joinWithNext(Segment seg, LocaleId trgLoc);

	/**
	 * Joins all segments for the specified sources and targets. The content
	 * becomes a single segment.
	 *
	 * @param trgLoc
	 *            the target locale
	 */
	public void joinAll(LocaleId trgLoc);

	/**
	 * Gets the status of the alignment for this entire text unit. The status
	 * will be NOT_ALIGNED if any of the targets in the parent TextUnit have a
	 * status of NOT_ALIGNED
	 *
	 * @return the status of the alignment for this text unit.
	 */
	public AlignmentStatus getAlignmentStatus();

	/**
	 * Gets the status of the alignment the given target locale in this text
	 * unit.
	 *
	 * @param trgLoc
	 *            the target locale for which to get the alignment status.
	 * @return the status of the alignment for this text unit.
	 */
	public AlignmentStatus getAlignmentStatus(LocaleId trgLoc);

	/**
	 * Segments the source content used for the given target locale based on the
	 * rules provided by a given {@link ISegmenter}.
	 * <p>
	 * No associated targets are modified.
	 * </p>
	 *
	 * @param segmenter
	 *            the segmenter to use to create the segments.
	 * @param targetLocale
	 *            the target locale that uses the source to segment.
	 */
	public void segmentSource(ISegmenter segmenter, LocaleId targetLocale);

	/**
	 * Segments the specified target content based on the rules provided by a
	 * given {@link ISegmenter}.
	 * <p>
	 * If the given target does not exist one is created.
	 * </p>
	 *
	 * @param segmenter
	 *            the segmenter to use to create the segments.
	 * @param targetLocale
	 *            {@link LocaleId} of the target we want to segment.
	 */
	public void segmentTarget(ISegmenter segmenter, LocaleId targetLocale);

}
