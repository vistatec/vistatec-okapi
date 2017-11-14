package net.sf.okapi.filters.html;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;

public class PrintEvents {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 */
	public static void main(final String[] args) throws URISyntaxException, IOException {
		String[] files = HtmlUtils.getHtmlTestFiles();
		
		try (HtmlFilter htmlFilter = new HtmlFilter();
			 InputStream htmlStream = HtmlFullFileTest.class.getResourceAsStream("/" + files[2])) {
			htmlFilter.open(new RawDocument(htmlStream, "UTF-8", LocaleId.fromString("en")));
			while (htmlFilter.hasNext()) {
				Event event = htmlFilter.next();
				System.out.println(event.getEventType());				
			}
		}
	}
}
