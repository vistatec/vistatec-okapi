package net.sf.okapi.common.skeleton;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;

public class TestUtil {

	static final public LocaleId locEN = LocaleId.ENGLISH;
	static final public LocaleId locFR = LocaleId.FRENCH;
	static final public LocaleId locDE = LocaleId.GERMAN;
	
	static void createSrcSegRefPart(ISkeleton skel, ITextUnit tu, String segId) {
		createSegmentRefPart(skel, tu, segId, null);
	}
	
	static void createTrgSegRefPart(ISkeleton skel, ITextUnit tu, String segId, LocaleId locId) {
		createSegmentRefPart(skel, tu, segId, locId);
	}
	
	static void createSegmentRefPart(ISkeleton skel, ITextUnit parent, String segId, LocaleId locId) {
		if (skel instanceof GenericSkeleton) {
			GenericSkeletonPart part = new GenericSkeletonPart(TextFragment.makeRefMarker(segId, Segment.REF_MARKER), 
					parent, locId);
			((GenericSkeleton)skel).getParts().add(part);
		}
	}
	
	static ITextUnit createSimpleTU () {
		ITextUnit tu = new TextUnit("id1");
		tu.setSourceContent(new TextFragment("text1"));
		return tu;
	}

	static ITextUnit createTranslatedTU () {
		ITextUnit tu = new TextUnit("id1");
		tu.setSourceContent(new TextFragment("text1"));
		tu.setTarget(locFR, new TextContainer("target1"));
		return tu;
	}

	static List<Event> createStartEvents (boolean multilangual,
			ISkeletonWriter gsw, EncoderManager encMgt) {
		List<Event> list = new ArrayList<Event>();
		StartDocument sd = new StartDocument("sd");
		sd.setEncoding("UTF-8", false);
		sd.setName("docName");
		sd.setLineBreak("\n");
		sd.setLocale(locEN);
		sd.setMultilingual(multilangual);
		GenericFilterWriter gfw = new GenericFilterWriter(gsw, encMgt);
		sd.setFilterWriter(gfw);
		list.add(new Event(EventType.START_DOCUMENT, sd));
		return list;
	}
	
	static void addEndEvents (List<Event> list) {
		Ending ending = new Ending("end");
		list.add(new Event(EventType.END_DOCUMENT, ending));
	}
}
