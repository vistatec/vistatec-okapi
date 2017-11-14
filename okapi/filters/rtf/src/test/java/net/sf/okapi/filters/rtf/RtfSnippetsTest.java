package net.sf.okapi.filters.rtf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
//import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class RtfSnippetsTest {

//	private RTFFilter filter;
//	private LocaleId locEN = LocaleId.fromString("en");

	@Before
	public void setUp() {
//		filter = new RTFFilter();
	}

	@After
	public void tearDown() {
	}
	
	@Test
	public void testBold() {
		
	}
	
//	private ArrayList<Event> getEvents (String snippet) {
//		ArrayList<Event> list = new ArrayList<Event>();
//		filter.open(new RawDocument(snippet, locEN));
//		while (filter.hasNext()) {
//			Event event = filter.next();
//			list.add(event);
//		}
//		filter.close();
//		return list;
//	}

//	private String generateOutput(ArrayList<Event> list, String original, LocaleId trgLang) {
//		GenericSkeletonWriter writer = new GenericSkeletonWriter();
//		StringBuilder tmp = new StringBuilder();
//		for (Event event : list) {
//			switch (event.getEventType()) {
//			case START_DOCUMENT:
//				writer.processStartDocument(trgLang, "utf-8", null, filter.getEncoderManager(),
//					(StartDocument) event.getResource());
//				break;
//			case TEXT_UNIT:
//				ITextUnit tu = event.getTextUnit();
//				tmp.append(writer.processTextUnit(tu));
//				break;
//			case DOCUMENT_PART:
//				DocumentPart dp = (DocumentPart) event.getResource();
//				tmp.append(writer.processDocumentPart(dp));
//				break;
//			case START_GROUP:
//				StartGroup startGroup = (StartGroup) event.getResource();
//				tmp.append(writer.processStartGroup(startGroup));
//				break;
//			case END_GROUP:
//				Ending ending = (Ending) event.getResource();
//				tmp.append(writer.processEndGroup(ending));
//				break;
//			}
//		}
//		writer.close();
//		return tmp.toString();
//	}
}
