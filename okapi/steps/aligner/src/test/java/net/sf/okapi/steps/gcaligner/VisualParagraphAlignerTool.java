package net.sf.okapi.steps.gcaligner;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;

public class VisualParagraphAlignerTool {
	private static IFilter srcFilter;
	private static IFilter trgFilter;
	private static String sourceRoot;
	private static String targetRoot;

	/**
	 * @param args
	 * @throws UnsupportedEncodingException
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static void main(String[] args) throws UnsupportedEncodingException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		// Instantiate without forcing dependencies on the whole sentence-aligner project
		srcFilter = (IFilter)Class.forName("net.sf.okapi.filters.plaintext.PlainTextFilter").newInstance(); // change to the filter needed by your format
		trgFilter = (IFilter)Class.forName("net.sf.okapi.filters.plaintext.PlainTextFilter").newInstance(); // change to the filter needed by your format
		sourceRoot = "D:\\OKAPI\\build-okapi\\steps\\sentence-aligner\\src\\test\\resources\\"; // change to root path of your source file
		targetRoot = "D:\\OKAPI\\build-okapi\\steps\\sentence-aligner\\src\\test\\resources\\"; // change to root path of your target file

		int srcTuCount = 0;
		int trgTuCount = 0;
		PrintStream ps = new PrintStream(System.out, true, "UTF-8");

		String[] srcfiles = { "srcMultimatch.txt" };
		String[] trgfiles = { "trgMultimatch.txt" };
		
		for (int i = 0; i < srcfiles.length; i++) {
			srcFilter.open(new RawDocument(Util.toURI(sourceRoot + srcfiles[i]), "UTF-8",
					LocaleId.ENGLISH));
			trgFilter.open(new RawDocument(Util.toURI(targetRoot + trgfiles[i]), "UTF-8",
					LocaleId.PORTUGUESE));

			while (srcFilter.hasNext() && trgFilter.hasNext()) {
				Event srcEvent = srcFilter.next();
				Event trgEvent = trgFilter.next();
				String s = "";
				String t = "";

				if ( srcEvent.getEventType() == EventType.TEXT_UNIT ) {
					srcTuCount++;
					s = ((ITextUnit)srcEvent.getResource()).toString().trim();
					System.out.println(((ITextUnit)srcEvent.getResource()).getName());
					ps.print(s);
				}
				if ( trgEvent.getEventType() == EventType.TEXT_UNIT ) {
					trgTuCount++;
					t = ((ITextUnit)trgEvent.getResource()).toString().trim();
					ps.println("\n" + t);
				}
				System.out.println();

				if (srcTuCount != trgTuCount) {
					return; // misalignment
				}
			}

			if (srcTuCount != trgTuCount) {
				return; // misalignment
			}
		}

		srcFilter.close();
		trgFilter.close();
	}
}
