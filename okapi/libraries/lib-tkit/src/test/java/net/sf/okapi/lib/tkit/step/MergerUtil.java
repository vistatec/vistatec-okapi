package net.sf.okapi.lib.tkit.step;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.filterwriter.XLIFFWriter;
import net.sf.okapi.common.filterwriter.XLIFFWriterParameters;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.lib.tkit.writer.XLIFFAndSkeletonWriter;
import net.sf.okapi.steps.common.FilterEventsWriterStep;

public class MergerUtil {
	public static ArrayList<Event> getTextUnitEvents(IFilter filter, RawDocument rd) {
		ArrayList<Event> list = new ArrayList<Event>();
		try {
			filter.open(rd);
			while (filter.hasNext()) {
				Event e = filter.next();
				if (e.isTextUnit()) {
					list.add(e);
				}			
			}
		} finally {
			if (filter != null) filter.close();
		}
		return list;
	}

	public static void writeXliffAndSkeleton(List<Event> events, String root, String path) {
		writeXliffAndSkeleton(events, root, path, true);
	}
	
	public static void writeXliffAndSkeleton(List<Event> events, String root, String path, boolean writeSkeleton) {
		IFilterWriter writer;
		if (writeSkeleton) {
			writer = new XLIFFAndSkeletonWriter();
		} else {
			writer = new XLIFFWriter();
		}
		writer.setOptions(LocaleId.SPANISH, "UTF-8");
		writer.setOutput(path);

		// Filter events to raw document final step (using the XLIFF writer)
		FilterEventsWriterStep fewStep = new FilterEventsWriterStep();
		fewStep.setDocumentRoots(root);
		fewStep.setFilterWriter(writer);
		fewStep.setOutputURI(Util.toURI(path));
		fewStep.setOutputEncoding("UTF-8");
		fewStep.setLastOutputStep(true);
		fewStep.setTargetLocale(LocaleId.ENGLISH);

		XLIFFWriterParameters paramsXliff = (XLIFFWriterParameters) writer.getParameters();
		paramsXliff.setPlaceholderMode(true);

		for (Event event : events) {
			fewStep.handleEvent(event);
		}
		writer.close();
		fewStep.destroy();
	}
}
