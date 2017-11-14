/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.filters.rainbowkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.IdGenerator;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.lib.xliff2.core.CTag;
import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.Tags;
import net.sf.okapi.lib.xliff2.core.Part;
import net.sf.okapi.lib.xliff2.core.StartFileData;
import net.sf.okapi.lib.xliff2.core.StartGroupData;
import net.sf.okapi.lib.xliff2.core.StartXliffData;
import net.sf.okapi.lib.xliff2.core.Unit;
import net.sf.okapi.lib.xliff2.reader.XLIFFReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experimental filter to read XLIFF v2 documents.
 * This is not intended as a read/write filter, just as a way to merge back XLIFF2 documents.
 */
public class XLIFF2Filter implements IFilter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private XLIFFReader reader;
	private Event nextEvent;
	private IdGenerator otherId;
	private Stack<String> groupIds;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	
	@Override
	public String getName () {
		return "okf_xliff2";
	}

	@Override
	public String getDisplayName () {
		return "XLIFF-v2 Filter";
	}

	@Override
	public void open (RawDocument input) {
		open(input, true);
	}

	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		close();
		reader = new XLIFFReader(XLIFFReader.VALIDATION_MAXIMAL);
		// Open using something else than the stream if we can (to avoid buffering on validation)
		if ( input.getInputURI() != null ) {
			reader.open(input.getInputURI());
		}
		else if ( input.getInputCharSequence() != null ) {
			reader.open(input.getInputCharSequence().toString());
		}
		else {
			reader.open(input.getStream());
		}
		otherId = new IdGenerator(null, "d");
		groupIds = new Stack<>();
	}

	@Override
	public void close () {
		if ( reader != null ) {
			reader.close();
			reader = null;
		}
	}

	@Override
	public boolean hasNext () {
		return read();
	}

	@Override
	public Event next () {
		return nextEvent;
	}

	@Override
	public void cancel () {
		// TODO Auto-generated method stub
	}

	@Override
	public IParameters getParameters () {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParameters (IParameters params) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		// TODO Auto-generated method stub
	}

	@Override
	public ISkeletonWriter createSkeletonWriter () {
		// Not intended for re-writing
		return null;
	}

	@Override
	public IFilterWriter createFilterWriter () {
		// Not intended for re-writing
		return null;
	}

	@Override
	public EncoderManager getEncoderManager () {
		// Not intended for re-writing
		return null;
	}

	@Override
	public String getMimeType () {
		//TODO: Use the official MIME type when registered 
		return MimeTypeMapper.XLIFF_MIME_TYPE;
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.XLIFF_MIME_TYPE,
			getClass().getName(),
			"XLIFF v2",
			"Configuration for XLIFF v2 documents.",
			null,
			".xlf"));
		return list;
	}

	/**
	 * Gets the next event.
	 * @return true if there is a next event, false otherwise.
	 */
	private boolean read () {
		try {
		while ( reader.hasNext() ) {
			net.sf.okapi.lib.xliff2.reader.Event xEvent = reader.next();
			switch ( xEvent.getType() ) {
			case INSIGNIFICANT_PART:
			case START_DOCUMENT:
			case MID_FILE:
			case SKELETON:
				// Skip these cases
				continue;
				
			case END_DOCUMENT: // We are done
				close();
				return false;
				
			case START_XLIFF:
				StartXliffData dd = xEvent.getStartXliffData();
				StartDocument sd = new StartDocument("_sd");
				srcLoc = LocaleId.fromBCP47(dd.getSourceLanguage());
				if ( dd.getTargetLanguage() == null ) trgLoc = null;
				else trgLoc = LocaleId.fromBCP47(dd.getTargetLanguage());
				sd.setLocale(srcLoc);
				nextEvent = new Event(EventType.START_DOCUMENT, sd);
				return true;

			case END_XLIFF:
				nextEvent = new Event(EventType.END_DOCUMENT, new Ending(otherId.createId()));
				return true;

			case START_FILE:
				StartFileData sfd = xEvent.getStartFileData();
				StartSubDocument ssd = new StartSubDocument("_sd", sfd.getId());
				nextEvent = new Event(EventType.START_SUBDOCUMENT, ssd);
				return true;
				
			case END_FILE:
				nextEvent = new Event(EventType.END_SUBDOCUMENT, new Ending(otherId.createId()));
				return true;

			case START_GROUP:
				StartGroupData sgd = xEvent.getStartGroupData();
				StartGroup sg = new StartGroup(groupIds.isEmpty() ? null : groupIds.peek(), sgd.getId());
				sg.setIsTranslatable(sgd.getTranslate());
				//TODO: whitespace
				groupIds.push(sg.getId());
				nextEvent = new Event(EventType.START_GROUP, sg);
				return true;
				
			case END_GROUP:
				nextEvent = new Event(EventType.END_GROUP, new Ending(otherId.createId()));
				groupIds.pop();
				return true;
			
			case TEXT_UNIT:
				nextEvent = createTextUnit(xEvent.getUnit());
				return true;
			}
		}
		}
		catch ( Throwable e ) {
			e.printStackTrace();
			throw e;
		}
		return false;
	}
	
	private Event createTextUnit (Unit unit) {
		ITextUnit tu = new TextUnit(unit.getId());
		
		TextContainer srcTc = tu.getSource();
		TextContainer trgTc = ( trgLoc == null ? null : tu.createTarget(trgLoc, false, IResource.CREATE_EMPTY));
		for ( Part part : unit ) {
			srcTc.append(createTextFragment(part, true));
			if ( trgTc != null ) {
				trgTc.append(createTextFragment(part, false));
			}
		}
		
		Event event = new Event(EventType.TEXT_UNIT, tu);
		return event;
	}

	private TextFragment createTextFragment (Part part,
		boolean source)
	{
		TextFragment tf = new TextFragment();
		Fragment frag = null;
		Tags markers = null;
		if ( source ) {
			frag = part.getSource();
			markers = part.getSourceTags();
		}
		else {
			if ( part.hasTarget() ) {
				frag = part.getTarget();
				markers = part.getTargetTags();
			}
			else { // No target for this part
				//return tf;
				// Fall back to the source
				frag = part.getSource();
				markers = part.getSourceTags();
			}
		}
		
		String ct = frag.getCodedText();
		for ( int i=0; i<ct.length(); i++ ) {
			char ch = ct.charAt(i);
			switch ( ch ) {
			case Fragment.CODE_OPENING:
			case Fragment.CODE_CLOSING:
			case Fragment.CODE_STANDALONE:
				CTag cm = (CTag)markers.get(ct, i); i++;
				net.sf.okapi.lib.xliff2.core.TagType xTagType = cm.getTagType();
				Code code = new Code(null);
				switch ( xTagType ) {
				case CLOSING:
					code.setTagType(TagType.CLOSING);
					break;
				case OPENING:
					code.setTagType(TagType.OPENING);
					break;
				case STANDALONE:
					code.setTagType(TagType.PLACEHOLDER);
					break;
				}
				code.setData(cm.getData());
				code.setType(cm.getType()==null ? "x" : cm.getType());
				try {
					code.setId(Integer.parseInt(cm.getId()));
				}
				catch ( NumberFormatException e ) {
					logger.error("Unexpected id value '{}' for inline code.", cm.getId());
					code.setId(cm.getId().hashCode()); // Set some value
				}
				tf.append(code);
				break;
				
			case Fragment.MARKER_OPENING:
			case Fragment.MARKER_CLOSING:
				i++;
				break;
				
			case Fragment.PCONT_STANDALONE:
				i++;
				break;

			default:
				tf.append(ch);
			}
		}
		
		return tf;
	}

}
