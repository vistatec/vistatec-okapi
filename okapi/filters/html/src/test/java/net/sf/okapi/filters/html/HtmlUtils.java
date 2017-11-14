package net.sf.okapi.filters.html;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubfilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlUtils {
	private final static Logger LOGGER = LoggerFactory.getLogger(HtmlUtils.class);
	private final static FileLocation location = FileLocation.fromClass(HtmlUtils.class);

	public static String[] getHtmlTestFiles() throws URISyntaxException {
		// read all files in the test html directory
		File dir = location.in("/").asFile();

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".html") || name.endsWith(".htm");
			}
		};
		return dir.list(filter);
	}

	public static void printEvents(ArrayList<Event> events) {
		for (Event event : events) {
			if (event.getEventType() == EventType.TEXT_UNIT) {
				assertTrue(event.getResource() instanceof ITextUnit);
			} else if (event.getEventType() == EventType.DOCUMENT_PART) {
				assertTrue(event.getResource() instanceof DocumentPart);
			} else if (event.getEventType() == EventType.START_GROUP || event.getEventType() == EventType.END_GROUP) {
				assertTrue(event.getResource() instanceof StartGroup || event.getResource() instanceof Ending);
			} else if (event.getEventType() == EventType.START_SUBFILTER || event.getEventType() == EventType.END_SUBFILTER) {
				assertTrue(event.getResource() instanceof StartSubfilter || event.getResource() instanceof Ending);
			}
			LOGGER.trace("{}: ", event.getEventType().toString());
			if (event.getResource() != null) {
				if (event.getResource() instanceof DocumentPart) {
					LOGGER.trace(((DocumentPart) event.getResource()).getSourcePropertyNames().toString());
				} else {
					LOGGER.trace(event.getResource().toString());
				}
				if (event.getResource().getSkeleton() != null) {
					LOGGER.trace("\tSkeleton: " + event.getResource().getSkeleton().toString());
				}
			}
		}
	}

	public static void printEvents(String file) {
		HtmlFilter htmlFilter = new HtmlFilter();
		InputStream htmlStream = location.in("/" + file).asInputStream();
		htmlFilter.open(new RawDocument(htmlStream, "UTF-8", LocaleId.fromString("en")));
		try {
			while (htmlFilter.hasNext()) {
				Event event = htmlFilter.next();
				if (event.getEventType() == EventType.TEXT_UNIT) {
					assertTrue(event.getResource() instanceof ITextUnit);
				} else if (event.getEventType() == EventType.DOCUMENT_PART) {
					assertTrue(event.getResource() instanceof DocumentPart);
				} else if (event.getEventType() == EventType.START_GROUP || event.getEventType() == EventType.END_GROUP) {
					assertTrue(event.getResource() instanceof StartGroup || event.getResource() instanceof Ending);
				} else if (event.getEventType() == EventType.START_SUBFILTER || event.getEventType() == EventType.END_SUBFILTER) {
					assertTrue(event.getResource() instanceof StartSubfilter || event.getResource() instanceof Ending);
				}
				LOGGER.trace(event.getEventType().toString() + ": ");
				if (event.getResource() != null) {
					if (event.getResource() instanceof DocumentPart) {
						LOGGER.trace(((DocumentPart) event.getResource()).getSourcePropertyNames().toString());
					} else {
						LOGGER.trace(event.getResource().toString());
					}
					if (event.getResource().getSkeleton() != null) {
						LOGGER.trace("\tSkeketon: " + event.getResource().getSkeleton().toString());
					}
				}
			}
			htmlFilter.close();
		} catch (Exception e) {
			LOGGER.trace(e.toString());
		}
	}
}
