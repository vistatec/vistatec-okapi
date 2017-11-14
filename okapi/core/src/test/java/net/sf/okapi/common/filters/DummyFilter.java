/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public class DummyFilter implements IFilter {

	private boolean canceled;
	private LinkedList<Event> queue;
	private LocaleId srcLang;
	private LocaleId trgLang;
	private IParameters params;
	private EncoderManager encoderManager;

	public DummyFilter () {
		params = new StringParameters();
	}
	
	public void cancel () {
		canceled = true;
	}

	public void close () {
		if ( queue != null ) {
			queue.clear();
			queue = null;
		}
	}

	public String getName () {
		return "okf_dummy";
	}

	public String getDisplayName () {
		return "Dummy Filter";
	}

	public String getMimeType () {
		return "text/xml";
	}

	public IParameters getParameters () {
		return params;
	}

	public boolean hasNext () {
		return (( queue != null ) && ( !queue.isEmpty() )); 
	}

	public Event next () {
		if ( canceled ) {
			queue.clear();
			return new Event(EventType.CANCELED);
		}
		return queue.poll();
	}

	/**
	 * Use this filter with a string input where the string has all your text unit,
	 * each text separated by a '\n'.
	 * For dummy inline codes use "@#$N" where N is a number between 0 and 9.
	 */
	public void open (RawDocument input) {
		open(input, true);
	}
	
	/**
	 * Opens the document with pre-defined entries. Use a CharSequence==##def## for
	 * default events. Use Charsequence="##seg##" for default segmented entries,
	 * use your own CharSequence otherwise: it will be split by \n and the sequences 
	 * "@#$" will be replaced by inline codes. 
	 */
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		setOptions(input.getSourceLocale(), input.getTargetLocale(),
			input.getEncoding(), generateSkeleton);
		if ( input.getInputCharSequence().equals("##def##") ) {
			reset();
		}
		else {
			reset(input.getInputCharSequence().toString());
		}
	}

	private void setOptions (LocaleId sourceLanguage,
		LocaleId targetLanguage,
		String defaultEncoding,
		boolean generateSkeleton)
	{
		srcLang = sourceLanguage;
		trgLang = targetLanguage;
	}

	public void setParameters (IParameters params) {
		this.params = params;
	}

	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
	}

	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	public IFilterWriter createFilterWriter () {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	public EncoderManager getEncoderManager () {
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setAllKnownMappings();
		}
		return encoderManager;
	}

	private void reset (String data) {
		if ( data.equalsIgnoreCase("##seg##") ) {
			resetWithSegments();
			return;
		}
		
		close();
		queue = new LinkedList<Event>();
		String[] parts = data.split("\n", 0);

		StartDocument sd = new StartDocument("sd1");
		sd.setLocale(srcLang);
		sd.setMultilingual(parts.length>1);
		sd.setMimeType("text");
		queue.add(new Event(EventType.START_DOCUMENT, sd));
		
		ITextUnit tu = new TextUnit("id1", parts[0]);
		// Use getFirstContent fine because there is nothing segmented
		String text = tu.getSource().getSegments().getFirstContent().getCodedText();
		int n = text.indexOf("@#$");
		while ( n > -1 ) {
			tu.getSource().getSegments().getFirstContent().changeToCode(n, n+4, TagType.PLACEHOLDER, "z");
			text = tu.getSource().getSegments().getFirstContent().getCodedText();
			n = text.indexOf("@#$");
		}
		
		if ( parts.length > 1 ) {
			TextFragment tf = new TextFragment(parts[1]);
			text = tf.getCodedText();
			n = text.indexOf("@#$");
			while ( n > -1 ) {
				tf.changeToCode(n, n+4, TagType.PLACEHOLDER, "z");
				text = tf.getCodedText();
				n = text.indexOf("@#$");
			}
			TextContainer tc = new TextContainer(tf);
			tu.setTarget(trgLang, tc);
		}
		
		queue.add(new Event(EventType.TEXT_UNIT, tu));

		Ending ending = new Ending("ed1");
		queue.add(new Event(EventType.END_DOCUMENT, ending));
	}
	
	private void resetWithSegments () {
		close();
		queue = new LinkedList<Event>();

		StartDocument sd = new StartDocument("sd1");
		sd.setLocale(srcLang);
		sd.setMultilingual(true);
		sd.setMimeType("text/xml");
		GenericSkeleton skel = new GenericSkeleton("<doc>\n");
		sd.setSkeleton(skel);
		queue.add(new Event(EventType.START_DOCUMENT, sd));
		
		ITextUnit tu = new TextUnit("tu1", "First segment for SRC. Second segment for SRC");
		TextContainer tc = tu.getSource();
		List<Range> ranges = new ArrayList<Range>();
		ranges.add(new Range(0, 22));
		ranges.add(new Range(23, -1));
		tc.getSegments().create(ranges);
		
		tc = tu.setTarget(trgLang, new TextContainer("First segment for TRG. Second segment for TRG"));
		tc.getSegments().create(ranges);

		skel = new GenericSkeleton("<text>\n<s>First segment for SRC. Second segment for SRC</s>\n<t>");
		skel.addContentPlaceholder(tu, trgLang);
		skel.append("<t>\n</text>\n");
		tu.setSkeleton(skel);
		queue.add(new Event(EventType.TEXT_UNIT, tu));
		
		Ending ending = new Ending("ed1");
		skel = new GenericSkeleton("</doc>\n");
		ending.setSkeleton(skel);
		queue.add(new Event(EventType.END_DOCUMENT, ending));
	}
	
	private void reset () {
		close();
		queue = new LinkedList<Event>();

		StartDocument sd = new StartDocument("sd1");
		sd.setLocale(srcLang);
		sd.setMultilingual(true);
		sd.setMimeType("text/xml");
		GenericSkeleton skel = new GenericSkeleton("<doc>\n");
		sd.setSkeleton(skel);
		queue.add(new Event(EventType.START_DOCUMENT, sd));
		
		ITextUnit tu = new TextUnit("tu1", "Source text");
		tu.setTarget(trgLang, new TextContainer("Target text"));
		skel = new GenericSkeleton("<text>\n<s>Source text</s>\n<t>");
		skel.addContentPlaceholder(tu, trgLang);
		skel.append("<t>\n</text>\n");
		tu.setSkeleton(skel);
		queue.add(new Event(EventType.TEXT_UNIT, tu));
		
		tu = new TextUnit("tu2", "Source text 2");
		skel = new GenericSkeleton("<text>\n<s>Source text 2</s>\n<t>");
		skel.addContentPlaceholder(tu, trgLang);
		skel.append("<t>\n</text>\n");
		tu.setSkeleton(skel);
		queue.add(new Event(EventType.TEXT_UNIT, tu));
		
		Ending ending = new Ending("ed1");
		skel = new GenericSkeleton("</doc>\n");
		ending.setSkeleton(skel);
		queue.add(new Event(EventType.END_DOCUMENT, ending));
	}

	public List<FilterConfiguration> getConfigurations() {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.DEFAULT_MIME_TYPE,
			getClass().getName(),
			"Dummy Filter",
			"Default for dummy."));
		return list;
	}

}
