package net.sf.okapi.filters.versifiedtxt;

import java.io.InputStream;
import java.net.URISyntaxException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.TextUnitUtil;

public class PrintEvents {

	/**
	 * @param args
	 * @throws URISyntaxException
	 */
	@SuppressWarnings("resource")
	// s closed by rd, rd closed by filter
	public static void main(final String[] args) throws URISyntaxException {
		VersifiedTextFilter filter = new VersifiedTextFilter();
		InputStream s = VersifiedTxtFilterTest.class.getResourceAsStream("/"
				+ "bilingual.txt");
		filter.open(new RawDocument(s, "UTF-8", LocaleId.ENGLISH, LocaleId.SPANISH));
		while (filter.hasNext()) {
			Event event = filter.next();
			if (event.getEventType() == EventType.TEXT_UNIT) {
				System.out
						.print(TextUnitUtil.getSourceText(event.getTextUnit()));
				if (event.getTextUnit().getSkeleton() != null) {
					System.out.print(event.getTextUnit().getSkeleton()
							.toString());
				}
				System.out.print(TextUnitUtil.getTargetText(
						event.getTextUnit(), LocaleId.SPANISH));
			} else {
				System.out.print(event.getResource().toString());
			}
		}
		filter.close();
	}
}
