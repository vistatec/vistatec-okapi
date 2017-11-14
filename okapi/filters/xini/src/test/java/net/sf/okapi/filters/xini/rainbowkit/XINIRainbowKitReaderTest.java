package net.sf.okapi.filters.xini.rainbowkit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterTestDriver;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class XINIRainbowKitReaderTest {

	private static final String DESCENDING_PHS = "descendingPhs.xini";
	private static final String ASCENDING_PHS = "ascendingPhs.xini";
	private static final String DEFAULT_CONTENT = "contents.xini";

	@Test
	public void textSplitTagCodeNumbering() throws Exception {
		List<Event> events = getEventsFromResourceFile(DEFAULT_CONTENT);
		assertEquals(EventType.START_DOCUMENT, events.get(0).getEventType());

		TextUnit tu = new TextUnit("tu1_tu1");
		ISegments segs = tu.getSource().getSegments();
		TextFragment tf = new TextFragment();
		tf.append(makeCode(TagType.OPENING, null, 1));
		tf.append("Test!");
		// XXX Explicitly balance markers to match what is happening during
		// TF production inside the XINI filter
		tf.balanceMarkers();
		segs.append(tf, true);
		TextFragment tf2 = new TextFragment();
		tf2.append(makeCode(TagType.CLOSING, null, 1));
		tf2.append(makeCode(TagType.PLACEHOLDER, null, 2));
		// XXX balance markers here as well
		tf2.balanceMarkers();
		segs.append(tf2, true);
		segs.append(new TextFragment("Test."), true);

		assertEquals(EventType.TEXT_UNIT, events.get(1).getEventType());
		ITextUnit actual = events.get(1).getTextUnit();
		assertNotNull(actual);
		assertTrue(FilterTestDriver.compareTextContainer(tu.getSource(), actual.getSource()));
	}
	
	@Test
	public void textSplitTagCodeNumberingDescending() throws Exception {
		List<Event> events = getEventsFromResourceFile(DESCENDING_PHS);

		TextUnit tu = new TextUnit("tu1_tu1");
		ISegments segs = tu.getSource().getSegments();
		TextFragment tf = new TextFragment();
		tf.append(makeCode(TagType.OPENING, null, 2));
		tf.append(makeCode(TagType.OPENING, null, 1));
		tf.append("Test!");
		// XXX Explicitly balance markers to match what is happening during
		// TF production inside the XINI filter
		tf.balanceMarkers();
		tf.append(makeCode(TagType.CLOSING, null, -1));
		tf.append(makeCode(TagType.CLOSING, null, -1));
		segs.append(tf, true);

		ITextUnit actual = events.get(1).getTextUnit();
		assertTrue(FilterTestDriver.compareTextContainer(tu.getSource(), actual.getSource()));
	}

	@Test
	public void textSplitTagCodeNumberingAscending() throws Exception {
		List<Event> events = getEventsFromResourceFile(ASCENDING_PHS);

		TextUnit tu = new TextUnit("tu1_tu1");
		ISegments segs = tu.getSource().getSegments();
		TextFragment tf = new TextFragment();
		tf.append(makeCode(TagType.OPENING, null, 1));
		tf.append(makeCode(TagType.OPENING, null, 2));
		tf.append("Test!");
		// XXX Explicitly balance markers to match what is happening during
		// TF production inside the XINI filter
		tf.balanceMarkers();
		tf.append(makeCode(TagType.CLOSING, null, -1));
		tf.append(makeCode(TagType.CLOSING, null, -1));
		segs.append(tf, true);

		ITextUnit actual = events.get(1).getTextUnit();
		assertTrue(FilterTestDriver.compareTextContainer(tu.getSource(), actual.getSource()));
	}

	private LinkedList<Event> getEventsFromResourceFile(String resourceFile) throws URISyntaxException {
		URI uri = getClass().getResource("/" + resourceFile).toURI();
		RawDocument rd = new RawDocument(uri, "UTF-8", LocaleId.ENGLISH);
		XINIRainbowkitReader reader = new XINIRainbowkitReader();
		reader.open(rd);
		return reader.getFilterEvents("test1.xml");
	}

	private Code makeCode(TagType tagType, String type, int id) {
		Code code = new Code(tagType, type);
		code.setId(id);
		code.setTagType(tagType);
		return code;
	}
}
