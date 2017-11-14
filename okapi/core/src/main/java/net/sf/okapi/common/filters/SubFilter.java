/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.common.filters;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonPart;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.common.skeleton.SkeletonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter that converts any {@link IFilter} into a subfilter (a filter called from another {@link IFilter}). 
 * Specific implementations can implement this class and override any needed methods to transform {@link Event}s
 * as they are produced.
 * This class should be used to wrap filters that use {@link GenericSkeleton} and its subclasses. 
 * If a different type of skeleton is used or id/name generation logic should be changed, subclass this class. 
 */
public class SubFilter implements IFilter {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private IFilter filter;
	private SubFilterEventConverter converter;	
	private String parentId;
	private String parentName;
	private int sectionIndex;
	
	StartSubfilter startSubfilter;
	EndSubfilter endSubfilter;
	
	public SubFilter (IFilter filter,
		IEncoder parentEncoder,
		int sectionIndex,
		String parentId,
		String parentName)
	{
		filter.close();
		this.filter = filter;
		this.parentId = parentId;
		this.parentName = parentName;
		this.sectionIndex = sectionIndex;
		this.converter = new SubFilterEventConverter(this, parentEncoder);
	}
		
	public IFilter getFilter () {
		return filter;
	}

	public SubFilterEventConverter getConverter () {
		return converter;
	}

	@Override
	public String getName () {
		return filter.getName();
	}

	@Override
	public String getDisplayName() {
		return filter.getDisplayName();
	}

	@Override
	public void open(RawDocument input) {
		converter.reset();
		filter.open(input);
	}

	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		converter.reset();
		filter.open(input, generateSkeleton);		
	}

	@Override
	public void close () {
		filter.close();
		converter.reset();
	}

	@Override
	public boolean hasNext () {
		return filter.hasNext();
	}

	@Override
	public Event next () {
		return converter.convertEvent(filter.next());
	}

	@Override
	public void cancel () {
		filter.cancel();
	}

	@Override
	public IParameters getParameters () {
		return filter.getParameters();
	}

	@Override
	public void setParameters (IParameters params) {
		filter.setParameters(params);
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		filter.setFilterConfigurationMapper(fcMapper);
	}

	@Override
	public ISkeletonWriter createSkeletonWriter () {
		return filter.createSkeletonWriter();
	}

	@Override
	public IFilterWriter createFilterWriter () {
		return filter.createFilterWriter();
	}

	@Override
	public EncoderManager getEncoderManager () {
		return filter.getEncoderManager();
	}

	@Override
	public String getMimeType () {
		return filter.getMimeType();
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		return filter.getConfigurations();
	}

	/**
	 * Get events by subfilter at once, without using open()/hasNext()/next()/close().
	 * @param input the {@link RawDocument} to retrieve events from.
	 * @return a list of events created the this subfilter for a given RawDocument input.
	 */
	public List<Event> getEvents (RawDocument input) {
		List<Event> events = new LinkedList<Event>();
		open(input);
		while (hasNext()) {
			events.add(next());
		}
		close();
		return Collections.unmodifiableList(events);		
	}
	
	public Code createRefCode () {
		startSubfilter.setIsReferent(true);
		Code c = new Code(TagType.PLACEHOLDER, startSubfilter.getName(), 
				TextFragment.makeRefMarker(startSubfilter.getId()));
		c.setReferenceFlag(true);
		return c;
	}
	
//	public ITextUnit createRefTU() {
//		ITextUnit tu = new TextUnit(buildRefTuId());
//		TextContainer tc = tu.getSource();
//		TextFragment tf = tc.getFirstContent();
//		tf.append(createRefCode());
//		tu.setName(buildRefTuName());
//		tu.setType("ref-ssf");
//		return tu;
//	}
	
	private DocumentPart buildRefDP (GenericSkeleton beforeSkeleton,
		GenericSkeleton afterSkeleton)
	{
		GenericSkeleton skel = new GenericSkeleton();
		DocumentPart dp = new DocumentPart(buildRefId(), false, skel);
		
		skel.add(beforeSkeleton);
		skel.addReference(startSubfilter);
		startSubfilter.setIsReferent(true);
		skel.add(afterSkeleton);
		
		dp.setName(buildRefName());
		dp.setType("ref-ssf");
		
		return dp;
	}
	
	// We need a separate event to store a reference to the sub-filtered content,
	// and cannot always attach the reference to EndSubfilter's skeleton because the
	// content can also be accessed from a code in a TextFragment or skeleton.
	// We have 2 options to refer to the content: from an event and from a code in
	// a TextFragment or skeleton.
	// If none of these options is used, the content remains unreferenced, and
	// the parent filter's writer has no means to write it out.
	// Sub-filtered content is accessible only by a reference, so it's the parent
	// filter's responsibility to invoke creation of such reference.
	
	public Event createRefEvent () {
		return createRefEvent(null, null);
	}
	
	public Event createRefEvent (IResource resource) {
		ISkeleton skel = resource.getSkeleton();
		if (skel instanceof GenericSkeleton) {
			GenericSkeleton[] parts = SkeletonUtil.splitSkeleton((GenericSkeleton) skel);
			return createRefEvent(parts[0], parts[1]);
		}
		else {
			if (skel != null) 
				logger.warn("Unknown skeleton type, ignored.");
			return createRefEvent();
		}			
	}
	
	public Event createRefEvent (ISkeleton beforeSkeleton,
		ISkeleton afterSkeleton)
	{
		if ( beforeSkeleton instanceof GenericSkeleton && 
				afterSkeleton instanceof GenericSkeleton ) {
			DocumentPart dp = buildRefDP(
					(GenericSkeleton) beforeSkeleton, 
					(GenericSkeleton) afterSkeleton);
			return new Event(EventType.DOCUMENT_PART, dp);
		}
		else {
			if ( beforeSkeleton != null || afterSkeleton != null )
				logger.warn("Unknown skeleton type, ignored.");
			DocumentPart dp = buildRefDP(null, null);
			return new Event(EventType.DOCUMENT_PART, dp);
		}
	}

//	public StartSubfilter getStartSubFilter() {
//		if (startSubfilter == null) {
//			throw new NullPointerException("startSubfilter is not generated yet by the subfilter.");
//		}
//		return startSubfilter;
//	}
//	
//	public EndSubfilter getEndSubFilter() {
//		if (endSubfilter == null) {
//			throw new NullPointerException("endSubfilter is not generated yet by the subfilter.");
//		}
//		return endSubfilter;
//	}
	
	private String buildStartSubfilterId (String originalResId) {
		return (originalResId != null) ?
				String.format("%s_%s", getParentId(), originalResId) :
				String.format("%s_%s%d", getParentId(), IdGenerator.START_SUBFILTER, getSectionIndex());
	}
	
	private String buildStartSubfilterName () {
		return IFilter.SUB_FILTER + getParentName();
	}
	
	private String buildEndSubfilterId (String originalResId) {
		return (originalResId != null) ?
				String.format("%s_%s", getParentId(), originalResId) :
				String.format("%s_%s%d", getParentId(), IdGenerator.END_SUBFILTER, getSectionIndex());
	}
	
	protected String buildResourceId (String resId,
		Class<? extends IResource> resClass)
	{
		if ( resClass == StartSubfilter.class )
			return buildStartSubfilterId(resId);
		
		else if ( resClass == EndSubfilter.class )
			return buildEndSubfilterId(resId);
		
		else
			return String.format("%s_%s%d_%s", getParentId(),
								 IdGenerator.SUBFILTERED_EVENT, getSectionIndex(), resId);
	}
	
	protected String buildResourceName (String resName,
		boolean autoGenerated,
		Class<? extends INameable> resClass)
	{
		if ( resClass == StartSubfilter.class )
			return buildStartSubfilterName();
		else
			return autoGenerated ? String.format("%s_%s", getParentName(), resName) : resName;
	}
	
	protected String buildRefId () {
		return String.format("ref-%s%s-%d", IFilter.SUB_FILTER, getParentId(), getSectionIndex());
	}

	public static boolean resourceIdsMatch(String startSubfilterResourceId, String endSubfilterResourceId) {
		if (startSubfilterResourceId == null || endSubfilterResourceId == null) {
			return false;
		}
		int i = startSubfilterResourceId.lastIndexOf(IdGenerator.START_SUBFILTER);
		if (i != -1) {
			startSubfilterResourceId = startSubfilterResourceId.substring(0, i) +
									   startSubfilterResourceId.substring(i + IdGenerator.START_SUBFILTER.length());
		}
		i = endSubfilterResourceId.lastIndexOf(IdGenerator.END_SUBFILTER);
		if (i != -1) {
			endSubfilterResourceId = endSubfilterResourceId.substring(0, i) +
									 endSubfilterResourceId.substring(i + IdGenerator.END_SUBFILTER.length());
		}
		return startSubfilterResourceId.equals(endSubfilterResourceId);
	}

	protected String buildRefName () {
		return "ref:" + getParentId();
	}	
	
	protected final String getParentId () {
		return parentId;
	}

	protected final String getParentName () {
		return parentName;
	}

	protected final int getSectionIndex () {
		return sectionIndex;
	}

	protected void convertRefsInSkeleton (ISkeleton skel) {
		if ( skel instanceof GenericSkeleton ) {
			GenericSkeleton gs = (GenericSkeleton)skel;
			for ( GenericSkeletonPart part : gs.getParts() ) {					
				String data = part.getData().toString();
				if ( !data.contains(TextFragment.REFMARKER_START) ) continue;

				String newData = converter.convertRefIds(data);
				part.setData(newData);
			}
		}
	}

}