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

import net.sf.okapi.common.ISegmenter;
import net.sf.okapi.common.LocaleId;

/** Interface for the basic unit of extraction from a filter and also the resource
 * associated with the filter event TEXT_UNIT.
 * <p>
 * The TextUnit object holds the extracted source text, all its properties and
 * annotations, and any target corresponding data.</p>
 * <p>
 * Where adjusted sources exist, methods in this interface should have
 * their effects on the default source, however a specific implementation may
 * provide a method to select one of the variant sources that methods would then
 * use. The main source should always be used before such a method is called.</p>
 */
public interface ITextUnit extends INameable, IReferenceable {

	/**
	 * Resource type value for a paragraph.
	 */
	public static final String TYPE_PARA = "paragraph";
	/**
	 * Resource type value for a list.
	 */
	public static final String TYPE_LIST_ELEMENT = "list_element";
	/**
	 * Resource type value for a title.
	 */
	public static final String TYPE_TITLE = "title";
	/**
	 * Resource type value for a header.
	 */
	public static final String TYPE_HEADER = "header";
	/**
	 * Resource type value for a cdata section.
	 */
	public static final String TYPE_CDATA = "cdata";
    
	/**
	 * Clones this TextUnit.
	 * @return A new TextUnit object that is a copy of this one. 
	 */
	public ITextUnit clone ();
	
    /**
     * Indicates if the source text of this TextUnit is empty.
     *
     * @return true if the source text of this TextUnit is empty, false otherwise.
     */
    public boolean isEmpty ();

    /**
     * Gets the source object for this text unit (a {@link TextContainer} object).
     *
     * @return the source object for this text unit.
     */
    public TextContainer getSource ();

    /**
     * Sets the source object for this TextUnit. Any existing source object is overwritten.
     *
     * @param textContainer the source object to set.
     * @return the source object that has been set.
     */
    public TextContainer setSource (TextContainer textContainer);

    /**
     * Gets the target object for this text unit for a given locale.
     * If the target does not exists a null is retruned.
     *
     * @param locId the locale to query.
     * @return the target object for this text unit for the given locale, or null
     * if the target does not exist.
     * @see #createTarget(LocaleId, boolean, int)
     */
    public TextContainer getTarget (LocaleId locId);
    
    /**
     * Gets the target object for this text unit for a given locale.
     * If the target does not exists one is created if the parameter createIfNeeded is true.
     * <p>
     *
     * @param locId the locale to query.
     * @param createIfNeeded true to create the target if it does not exists yet.
     * @return the target object for this text unit for the given locale, or returns null if the
     * target does not exits and no new target was created.
     * @see #createTarget(LocaleId, boolean, int)
     */
//    public TextContainer getTarget (LocaleId locId,
//    	boolean createIfNeeded);

    /**
     * Sets the target object for this text unit for a given locale.
     * <p>
     * If the target does not exists, one is created.
     * Any existing content for the given locale is overwritten.
     * To set a target object based on the source, use the
     * {@link #createTarget(LocaleId, boolean, int)} method.</p>
     *
     * @param locId the target locale.
     * @param text the target content to set.
     * @return the target content that has been set.
     */
	public TextContainer setTarget (LocaleId locId,
		TextContainer text);

    /**
     * Removes a given target object from this text unit.
     * If the given locale does not exist in this text unit nothing happens.
     * Variant source for this target locale will not be affected.
     *
     * @param locId the target locale to remove.
     */
    public void removeTarget (LocaleId locId);

    /**
     * Indicates if there is a target object for a given locale for this text unit.
     *
     * @param locId the locale to query.
     * @return true if a target object exists for the given locale, false otherwise.
     */
    public boolean hasTarget (LocaleId locId);

    /**
     * Creates or get the target for this TextUnit.
     * <p>
     * If a variant source exists for the target locale, creationOptions apply
     * to the variant source.</p>
     *
     * @param locId the target locale.
     * @param overwriteExisting true to overwrite any existing target for the given locale.
     * False to not create a new target object if one already exists for the given locale.
     * @param creationOptions creation options:
     * <ul><li>CREATE_EMPTY: Create an empty target object.</li>
     * <li>COPY_PROPERTIES: Copy the source properties.</li>
     * <li>COPY_CONTENT: Copy the text of the source (and any associated in-line code), but not the segmenation.</li>
     * <li>COPY_SEGMENTATION: Copy the source segmentation.</li>
     * <li>COPY_SEGMENTED_CONTENT: Same as (COPY_CONTENT|COPY_SEGMENTATION).</li>
     * <li>COPY_ALL: Same as (COPY_SEGMENTED_CONTENT|COPY_PROPERTIES).</li></ul>
     * @return the target object that was created, or retrieved.
     */
    public TextContainer createTarget (LocaleId locId,
    	boolean overwriteExisting,
    	int creationOptions);

    /**
     * Sets the content of the source for this TextUnit.
     *
     * @param content the new content to set.
     * @return the new content of the source for this TextUnit.
     */
    public TextFragment setSourceContent (TextFragment content);

    /**
     * Sets the content of the target for a given locale for this TextUnit.
     * <p>If the target does not exists, one is created.
     * Any existing content for the given locale is overwritten.
     * To set a target object based on the source, use the
     * {@link #createTarget(LocaleId, boolean, int)} method.</p>
     *
     * @param locId the locale to set.
     * @param content the new content to set.
     * @return the new content for the given target locale for this text unit.
     */
    public TextFragment setTargetContent (LocaleId locId,
    	TextFragment content);

    /**
     * Creates a new {@link IAlignedSegments} object to access and
     * manipulate the segments of this text unit.
     *
     * @return a new {@link IAlignedSegments} object.
     */
    IAlignedSegments getAlignedSegments ();
	
    /**
     * Gets the segments for the source. Un-segmented content return a single segment.
     *
     * @return an object implementing ISegments for the source content.
     */
    public ISegments getSourceSegments ();

    /**
     * Get the segments for a given target. Un-segmented content return a single segment.
     * If the target does not exists, one is created, with the same segments as the source, but empty.
     *
     * @param trgLoc the locale of the target to retrieve.
     * @return an object implementing ISegments for the given target content.
     */
    public ISegments getTargetSegments (LocaleId trgLoc);

	/**
	 * Removes all segmentations (source and targets) in this text unit.
	 * All entries are converted to non-segmented entries.
	 */
	public void removeAllSegmentations ();

	/**
	 * Segments the default source content based on the rules provided by a given ISegmenter.
	 * @param segmenter the segmenter to use to create the segments.
	 */
	public void createSourceSegmentation (ISegmenter segmenter);
	
	/**
	 * Segments the specified target content based on the rules provided by a given ISegmenter.
	 * @param segmenter the segmenter to use to create the segments.
	 * @param targetLocale {@link LocaleId} of the target we want to segment.
	 */
	public void createTargetSegmentation (ISegmenter segmenter,
		LocaleId targetLocale);
	
	/**
     * Gets the source segment for a given segment id.
     * <p>
     * If the segment does not exists, one is created if <code>createIfNeeded</code> is true.</p>
     *
     * @param segId the id of the segment to retrieve.
     * @param createIfNeeded true to append a segment at the end of the content
     *                       and return it if the segment does not exist yet.
     *                       False to return null when the segment does not exists.
     * @return the found or created segment, or null.
     */
    public Segment getSourceSegment (String segId,
    	boolean createIfNeeded);
	
    /**
     * Gets the segment for a given segment id in a given target.
     * <p>
     * If the target does not exists, one is created.</p>
     * <p>
     * If the segment does not exists, one is created if <code>createIfNeeded</code> is true.</p>
     *
     * @param trgLoc the target locale to look up.
     * @param segId the id of the segment to retrieve.
     * @param createIfNeeded true to append a segment at the end of the target content and
     * return it if the segment does not exist yet. False to return null when the segment
     * does not exists.
     * @return the found or created segment, or null.
     */
    public Segment getTargetSegment (LocaleId trgLoc,
    	String segId,
    	boolean createIfNeeded);

}
