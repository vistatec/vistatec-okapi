package net.sf.okapi.filters.idml.tests;

import net.sf.okapi.common.FileLocation;
import com.googlecode.junittoolbox.MultithreadingTester;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.idml.IDMLFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.URI;

/**
 * Provides a parallel running issue test case for the IDMLFilter.
 */
@RunWith(JUnit4.class)
public class IDMLFilterInParallelTest {
    private static final int NUMBER_OF_THREADS = 10;
    private static final int NUMBER_OF_ROUNDS_PER_THREAD = 2;

    private static final String FILE_NAME = "TextPathTest04.idml";
    private static final URI PATH = FileLocation.fromClass(IDMLFilterInParallelTest.class).in("/" + FILE_NAME).asUri();

    public static final String ENCODING = "UTF-8";
    private static final LocaleId LOCALE = LocaleId.fromString("en");

    @Test
    public void testInMultipleThreads() throws Exception {
        MultithreadingTester multithreadingTester = new MultithreadingTester()
                .numThreads(NUMBER_OF_THREADS)
                .numRoundsPerThread(NUMBER_OF_ROUNDS_PER_THREAD);

        for (int i = 0; i < NUMBER_OF_THREADS; i++) {
            multithreadingTester.add(new Work());
        }

        multithreadingTester.run();
    }

    private class Work implements Runnable {
        @Override
        public void run() {
            RawDocument rd = new RawDocument(PATH, ENCODING, LOCALE);
            IFilter filter = new IDMLFilter();
            filter.open(rd);

            while (filter.hasNext()) {
                filter.next();
            }

            filter.close();
        }
    }
}
