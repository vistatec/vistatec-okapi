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

import static net.sf.okapi.common.IResource.COPY_ALL;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ReversedIterator;
import net.sf.okapi.common.exceptions.OkapiMisAlignmentException;

/**
 * Provides a standard implementation of the IAlignedSegments interface that
 * works with variant sources.
 * <p>
 * Currently tightly coupled to ITextUnit.
 */
public class AlignedSegments implements IAlignedSegments {
	private ITextUnit parent;

	public AlignedSegments(ITextUnit parent) {
		this.parent = parent;
	}

	@Override
	public void append(Segment srcSeg, Segment trgSeg, LocaleId trgLoc) {

		if (srcSeg == null && trgSeg == null)
			throw new IllegalArgumentException("srcSeg and trgSeg cannot both be null");

		insertOrAppend(true, -1, srcSeg, trgSeg, trgLoc);
	}

	@Override
	public void insert(int index, Segment srcSeg, Segment trgSeg, LocaleId trgLoc) {

		if (srcSeg == null)
			throw new IllegalArgumentException("srcSeg cannot be null");

		insertOrAppend(false, index, srcSeg, trgSeg, trgLoc);
	}

	private void insertOrAppend(boolean append, int index, Segment srcSeg, Segment trgSeg, LocaleId trgLoc) {
		Segment sourceSeg = (srcSeg != null ? srcSeg : new Segment(null, new TextFragment("")));
		Segment targetSeg = (trgSeg != null ? trgSeg : new Segment(null, new TextFragment("")));

		String originalId = null;
		String insertedId = null;

		ContainerIterator ci = new ContainerIterator(trgLoc);

		if (append) {
			targetSeg.id = sourceSeg.id; // make sure ids match

			if (ci.hasSource())
				ci.getSource().getSegments().append(sourceSeg);

			if (ci.hasTarget())
				ci.getTarget().getSegments().append(targetSeg);

		} else { // insert
			// get id at insertion location
			originalId = getSource(trgLoc).getSegments().get(index).id;

			if (ci.hasSource())
				insertedId = doInsert(ci.getSource(), index, null, null, sourceSeg);

			if (ci.hasTarget())
				insertedId = doInsert(ci.getTarget(), index, originalId, insertedId, targetSeg);
		}
	}

	/*
	 * @param originalId use null for source of target locale
	 * 
	 * @param insertedId use null if a new inserted id is needed
	 * 
	 * @returns null id or inserted id
	 */
	private String doInsert(TextContainer container, int index, String originalId, String insertedId, Segment seg) {
		ISegments segs = container.getSegments();
		Segment currentSeg;

		// handle source for target locale
		if (originalId == null) {
			// must be the first source
			segs.insert(index, seg);
			return seg.id; // return validated Id
		}
		// handle insertion for any other container
		currentSeg = segs.get(originalId);
		if (currentSeg != null) {
			segs.insert(segs.getIndex(originalId), seg);
			if (insertedId != null)
				seg.id = insertedId;
			return seg.id;
		}
		// append if unable to insert
		segs.append(seg);
		return insertedId; // return the most up-to-date insertedId
	}

	@Override
	public void setSegment(int index, Segment seg, LocaleId trgLoc) {

		ISegments segs = getSource(trgLoc).getSegments();
		String oldId = segs.get(index).id;
		String newId = seg.id;
		boolean idChanged = !newId.equals(oldId);
		int theIndex = index;

		Segment tempSeg;

		ContainerIterator ci = new ContainerIterator(trgLoc);

		if (ci.hasSource())
			ci.getSource().getSegments().set(theIndex, seg.clone());
		if (ci.hasTarget()) {
			segs = ci.getTarget().getSegments();
			segs.set(segs.getIndex(oldId), seg.clone());
		}

		// update the ids
		if (idChanged) {
			ci = new ContainerIterator(trgLoc);

			if (ci.hasSource()) {
				tempSeg = ci.getSource().getSegments().get(oldId);
				if (tempSeg != null)
					tempSeg.id = newId;
			}
			if (ci.hasTarget()) {
				tempSeg = ci.getTarget().getSegments().get(oldId);
				if (tempSeg != null)
					tempSeg.id = newId;
			}
		}
	}

	@Override
	public boolean remove(Segment seg, LocaleId trgLoc) {
		int count = 0;
		ContainerIterator ci = new ContainerIterator(trgLoc);

		if (ci.hasSource())
			count += removeSegment(ci.getSource(), seg.id);
		if (ci.hasTarget())
			count += removeSegment(ci.getTarget(), seg.id);

		return (count > 0);
	}

	/*
	 * Removes a segment witht he given id from the given container. Returns 1
	 * if a segment was removed, 0 otherwise.
	 */
	private int removeSegment(TextContainer container, String segId) {
		ISegments segs = container.getSegments();
		int segIndex = segs.getIndex(segId);
		if (segIndex > -1) {
			container.remove(segs.getPartIndex(segIndex));
			return 1;
		}
		return 0;
	}

	@Override
	public Segment getSource(int index, LocaleId trgLoc) {
		return getSource(trgLoc).getSegments().get(index);
	}

	@Override
	public Segment getCorrespondingTarget(Segment srcSeg, LocaleId trgLoc) {
		ISegments trgSegs = parent.getTargetSegments(trgLoc);
		return trgSegs.get(srcSeg.id);
	}

	@Override
	public Segment getCorrespondingSource(Segment trgSeg, LocaleId trgLoc) {
		ISegments srcSegs = getSource(trgLoc).getSegments();
		return srcSegs.get(trgSeg.id);
	}

	@Override
	public void align(List<AlignedPair> alignedSegmentPairs, LocaleId trgLoc) {
		// Based on TextUnitUtil.createMultilingualTextUnit(...)

		// Note: this implementation will wipe out any content that exists for
		// this locale
		// TODO check that this is the desired behavior for this method.

		TextContainer src;
		TextContainer trg;
		String srcSegId;

		// add source and target if required
		// no check required, see method description
		parent.createTarget(trgLoc, false, COPY_ALL);

		src = getSource(trgLoc);
		trg = parent.getTarget(trgLoc);

		// clear content ready for new segments
		src.clear();
		trg.clear();

		// iterate through the aligned pairs, adding content to both containers
		for (AlignedPair alignedPair : alignedSegmentPairs) {
			// use the id from the source as the id for the target
			srcSegId = appendPartsToContainer(alignedPair.getSourceParts(), src, null);
			appendPartsToContainer(alignedPair.getTargetParts(), trg, srcSegId);
		}

		// We now consider the source and target content to be segmented
		// if nothing else we need to prevent re-segmentation as that
		// will break the alignments
		src.setHasBeenSegmentedFlag(true);
		trg.setHasBeenSegmentedFlag(true);

		// the target and source should now be aligned since their content is
		// all from aligned pairs.
		trg.getSegments().setAlignmentStatus(AlignmentStatus.ALIGNED);
	}

	/**
	 * Appends the given {@link TextPart}s to the given {@link TextContainer}.
	 *
	 * @param parts
	 * @param container
	 * @param segId
	 *            the id to use for the segment component of parts
	 * @return the identifier for the segment in parts
	 */
	private String appendPartsToContainer(List<TextPart> parts, TextContainer container, String segId) {
		// make a shallow copy because we may modify the list elements
		List<TextPart> partsCopy = new LinkedList<TextPart>(parts);

		// calculate indexes of the source before and after inter-segment
		// TextParts
		int beforeIndex = 0;
		int afterIndex = partsCopy.size();
		for (TextPart part : partsCopy) {
			if (part.isSegment()) {
				break;
			}
			beforeIndex++;
		}
		ReversedIterator<TextPart> ri = new ReversedIterator<TextPart>(partsCopy);
		for (TextPart part : ri) {
			if (part.isSegment()) {
				break;
			}
			afterIndex--;
		}

		// append the before inter-segment TextParts
		for (TextPart part : partsCopy.subList(0, beforeIndex)) {
			container.append(part);
		}

		// append segment parts
		TextFragment frag = new TextFragment();
		for (TextPart part : partsCopy.subList(beforeIndex, afterIndex)) {
			frag.append(part.getContent());
		}
		Segment seg = new Segment(segId, frag);
		container.getSegments().append(seg);

		// append the after inter-segment TextParts
		for (TextPart part : partsCopy.subList(afterIndex, partsCopy.size())) {
			container.append(part);
		}

		return seg.getId();
	}

	/**
	 * Force one to one alignment. Assume that both source and target have the
	 * same number of segments.
	 *
	 * @param trgLoc
	 *            target locale used to align with the source
	 */
	@Override
	public void align(LocaleId trgLoc) {
		Iterator<Segment> srcSegsIt = getSource(trgLoc).getSegments().iterator();
		Iterator<Segment> trgSegsIt = parent.createTarget(trgLoc, false, IResource.COPY_SEGMENTATION).getSegments()
				.iterator();
		while (srcSegsIt.hasNext()) {
			try {
				Segment srcSeg = srcSegsIt.next();
				Segment trgSeg = trgSegsIt.next();
				trgSeg.id = srcSeg.id;
			} catch (NoSuchElementException e) {
				throw new OkapiMisAlignmentException("Different number of source and target segments", e);
			}
		}

		// these target segments are now aligned with their source counterparts
		parent.getTargetSegments(trgLoc).setAlignmentStatus(AlignmentStatus.ALIGNED);
	}

	@Override
	public void alignCollapseAll(LocaleId trgLoc) {

		ContainerIterator ci = new ContainerIterator(trgLoc);

		// keeping track of collapsed containers to check which to set to
		// ALIGNED
		LinkedList<TextContainer> collapsed = new LinkedList<TextContainer>();

		if (ci.hasSource()) {
			ci.getSource().joinAll();
			ci.getSource().setHasBeenSegmentedFlag(false);
			collapsed.add(ci.getSource());
		}
		if (ci.hasTarget()) {
			ci.getTarget().joinAll();
			ci.getTarget().setHasBeenSegmentedFlag(false);
			collapsed.add(ci.getTarget());
		}

		// mark target/source pairs aligned if both have been collapsed
		TextContainer src, trg;
		for (LocaleId loc : parent.getTargetLocales()) {
			src = getSource(loc);
			if (collapsed.contains(src)) {
				trg = parent.getTarget(loc);
				if (collapsed.contains(trg)) {
					trg.getSegments().setAlignmentStatus(AlignmentStatus.ALIGNED);
					// TODO: check that this is the desired behavior
					trg.getFirstSegment().id = src.getFirstSegment().getId();
				}
			}
		}

	}

	@Override
	public Segment splitSource(LocaleId trgLoc, Segment srcSeg, int splitPos) {
		TextContainer theSource = getSource(trgLoc);
		ISegments srcSegs = theSource.getSegments();
		int segIndex = srcSegs.getIndex(srcSeg.id);
		if (segIndex == -1)
			return null; // segment id not found in the container
		int partIndex = srcSegs.getPartIndex(segIndex);

		// split the source
		theSource.split(partIndex, splitPos, splitPos, false);

		Segment newSeg = srcSegs.get(segIndex + 1);
		ISegments currentSegs;
		Segment currentSeg;

		ContainerIterator ci = new ContainerIterator(trgLoc);

		// inserting new segments in all the places they go
		if (ci.hasTarget()) {
			currentSegs = ci.getTarget().getSegments();
			currentSeg = currentSegs.get(srcSeg.id);
			if (currentSeg != null)
				currentSegs.insert(currentSegs.getIndex(srcSeg.id) + 1, new Segment(srcSeg.id, new TextFragment("")));
		}

		return newSeg;
	}

	@Override
	public Segment splitTarget(LocaleId trgLoc, Segment trgSeg, int splitPos) {

		TextContainer theTarget = parent.createTarget(trgLoc, false, IResource.COPY_SEGMENTATION);
		ISegments trgSegs = theTarget.getSegments();
		int segIndex = trgSegs.getIndex(trgSeg.id);
		if (segIndex == -1)
			return null; // segment id not found in the container
		int partIndex = trgSegs.getPartIndex(segIndex);

		// split the source
		theTarget.split(partIndex, splitPos, splitPos, false);

		Segment newSeg = trgSegs.get(segIndex + 1);

		ISegments currentSegs;
		Segment currentSeg;

		ContainerIterator ci = new ContainerIterator(trgLoc);

		// inserting new segments in all the places they go
		if (ci.hasSource()) {
			currentSegs = ci.getSource().getSegments();
			currentSeg = currentSegs.get(trgSeg.id);
			if (currentSeg != null)
				currentSegs.insert(currentSegs.getIndex(trgSeg.id) + 1, new Segment(trgSeg.id, new TextFragment("")));
		}

		return newSeg;
	}

	@Override
	public void joinWithNext(Segment seg, LocaleId trgLoc) {
		ContainerIterator ci = new ContainerIterator(trgLoc);

		if (ci.hasSource())
			doJoinWithNext(ci.getSource(), seg.id);
		if (ci.hasTarget())
			doJoinWithNext(ci.getTarget(), seg.id);
	}

	private void doJoinWithNext(TextContainer cont, String segId) {
		int segIndex;
		ISegments segs = cont.getSegments();
		segIndex = segs.getIndex(segId);
		if (segIndex != -1)
			segs.joinWithNext(segIndex);
	}

	@Override
	public void joinAll(LocaleId trgLoc) {

		ContainerIterator ci = new ContainerIterator(trgLoc);
		if (ci.hasSource())
			ci.getSource().joinAll();
		if (ci.hasTarget())
			ci.getTarget().joinAll();
	}

	@Override
	public AlignmentStatus getAlignmentStatus() {
		for (LocaleId loc : parent.getTargetLocales()) {
			ISegments trgSegs = parent.getTargetSegments(loc);
			if (trgSegs.getAlignmentStatus() == AlignmentStatus.NOT_ALIGNED) {
				return AlignmentStatus.NOT_ALIGNED;
			}
		}
		return AlignmentStatus.ALIGNED;
	}

	@Override
	public AlignmentStatus getAlignmentStatus(LocaleId trgLoc) {
		return parent.getTargetSegments(trgLoc).getAlignmentStatus();
	}

	@Override
	public void segmentSource(ISegmenter segmenter, LocaleId targetLocale) {
		TextContainer theSource = getSource(targetLocale);
		segmenter.computeSegments(theSource);
		theSource.getSegments().create(segmenter.getRanges());
	}

	@Override
	public void segmentTarget(ISegmenter segmenter, LocaleId targetLocale) {
		TextContainer theTarget = parent.createTarget(targetLocale, false, IResource.COPY_SEGMENTATION);
		segmenter.computeSegments(theTarget);
		theTarget.getSegments().create(segmenter.getRanges());
		// TODO: invalidate source and other targets? or this one.
		// but then there is no way to call segmentTarget and get all in synch
	}

	@Override
	public Iterator<Segment> iterator() {
		return parent.getSource().getSegments().iterator();
	}

	@Override
	public Iterator<Segment> iterator(LocaleId trgLoc) {
		return getSource(trgLoc).getSegments().iterator();
	}

	/*
	 * Returns the source {@link TextContainer} for the given locale
	 * 
	 * @param loc the locale id from which to the source is to be returned.
	 * 
	 * @return the source text container for the given locale.
	 */
	private TextContainer getSource(LocaleId loc) {
		return parent.getSource();
	}

	/**
	 * Used to easily access the TextContainer fields source and target
	 */
	private class ContainerIterator {

		private TextContainer theSource = null;
		private TextContainer theTarget = null;

		/**
		 *
		 * @param trgLoc
		 *            used to determine which TextContainers are in which
		 *            categories
		 */
		public ContainerIterator(LocaleId trgLoc) {
			theSource = AlignedSegments.this.getSource(trgLoc);
			theTarget = parent.getTarget(trgLoc);
		}

		public boolean hasSource() {
			return theSource != null;
		}

		public TextContainer getSource() {
			if (theSource == null)
				throw new IllegalStateException("this method can only be called after hasSource() returns true");
			return theSource;
		}

		public boolean hasTarget() {
			return theTarget != null;
		}

		public TextContainer getTarget() {
			if (theTarget == null)
				throw new IllegalStateException("this method can only be called after hasTarget() returns true");
			return theTarget;
		}
	}

	public final ITextUnit getParent() {
		return parent;
	}

};