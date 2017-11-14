package net.sf.okapi.lib.tkit.roundtrip;


import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.lib.tkit.filter.BeanEventFilter;
import net.sf.okapi.lib.tkit.writer.BeanEventWriter;

public final class RoundTripUtils {
	public static String path; 
	
	public static List<Event> roundTripSerilaizedEvents(
			List<Event> events, ISkeletonWriter skeletonWriter, String serializedPath, LocaleId locale) {
		try (BeanEventWriter eventWriter = new BeanEventWriter();
			 BeanEventFilter eventReader = new BeanEventFilter()) {
			eventWriter.setOptions(locale, null);
			eventWriter.setOutput(serializedPath);
			net.sf.okapi.lib.tkit.writer.Parameters p = new net.sf.okapi.lib.tkit.writer.Parameters();
			p.setRemoveTarget(false);
			p.setMessage("Hello!");
			eventWriter.setParameters(p);

			// Serialize all the events
			for (Event event : events) {
				eventWriter.handleEvent(event);
			}
			eventWriter.close();
			events.clear();

			// now read the events we just serialized
			eventReader.open(new RawDocument(Util.toURI(serializedPath), "UTF-8", locale));
			while (eventReader.hasNext()) {
				events.add(eventReader.next());
			}

			return events;
		}
	}
	
	public static List<Event> roundTripSerilaizedEvents(List<Event> events) {
		return roundTripSerilaizedEvents(events, null, path, LocaleId.FRENCH);
	}
	
	public static List<Event> roundTripSerilaizedEvents(List<Event> events, LocaleId locale) {
		return roundTripSerilaizedEvents(events, null, path, locale);
	}
	
	public static List<Event> roundTripSerilaizedEvents(List<Event> events, ISkeletonWriter skeletonWriter, LocaleId locale) {
		return roundTripSerilaizedEvents(events, skeletonWriter, path, locale);
	}
	
	public static List<Event> roundTripSerilaizedEvents(List<Event> events, ISkeletonWriter skeletonWriter) {
		return roundTripSerilaizedEvents(events, skeletonWriter, path, LocaleId.FRENCH);
	}
}
