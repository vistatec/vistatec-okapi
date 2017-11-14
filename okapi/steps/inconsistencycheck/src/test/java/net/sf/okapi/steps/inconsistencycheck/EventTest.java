package net.sf.okapi.steps.inconsistencycheck;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.FileCompare;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.TestUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class EventTest {

    private String root;
    private LocaleId locEN = new LocaleId("en", "us");
    private LocaleId locFR = new LocaleId("fr", "fr");
    private InconsistencyCheckStep step;

    @Before
    public void setUp() throws URISyntaxException {
        root = TestUtil.getParentDir(this.getClass(), "/SameSource.html.xlf");
        step = new InconsistencyCheckStep();
        Parameters params = (Parameters) step.getParameters();
        params.setAutoOpen(false);
    }

    @Test
    public void SameSourceTest() throws URISyntaxException, IOException {
        // Setup pipeline
        IPipelineDriver pdriver = new PipelineDriver();
        FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
        fcMapper.addConfigurations(XLIFFFilter.class.getName());
        pdriver.setFilterConfigurationMapper(fcMapper);
        pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root));
        pdriver.addStep(new RawDocumentToFilterEventsStep());
        pdriver.addStep(step);

        // Setup input
        String inputPath = root + "SameSource.html.xlf";
        URI inputURI = new File(inputPath).toURI();
        URI outputURI = inputURI;

        // Add files
        pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_xliff", outputURI, "UTF-8", locEN, locFR));

        // Run pipeline
        pdriver.processBatch();

        // Compare with GOLD file
        maskDocIdPath(root + "inconsistency-report.xml");
        FileCompare fc = new FileCompare();
        assertTrue(fc.compareFilesPerLines(root + "inconsistency-report.xml", root + "Gold_SameSource.xml", "UTF-8"));
    }

    @Test
    public void SameTargetTest() throws URISyntaxException, IOException {
        // Setup pipeline
        IPipelineDriver pdriver = new PipelineDriver();
        FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
        fcMapper.addConfigurations(XLIFFFilter.class.getName());
        pdriver.setFilterConfigurationMapper(fcMapper);
        pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root));
        pdriver.addStep(new RawDocumentToFilterEventsStep());
        pdriver.addStep(step);

        // Setup input
        String inputPath = root + "SameTarget.html.xlf";
        URI inputURI = new File(inputPath).toURI();
        URI outputURI = inputURI;

        // Add files
        pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_xliff", outputURI, "UTF-8", locEN, locFR));

        pdriver.processBatch();

        // Compare with GOLD file
        maskDocIdPath(root + "inconsistency-report.xml");
        FileCompare fc = new FileCompare();
        assertTrue(fc.compareFilesPerLines(root + "inconsistency-report.xml", root + "Gold_SameTarget.xml", "UTF-8"));
    }

    @Test
    public void SameSourceAndTargetTest() throws URISyntaxException, IOException {
        // Setup pipeline
        IPipelineDriver pdriver = new PipelineDriver();
        FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
        fcMapper.addConfigurations(XLIFFFilter.class.getName());
        pdriver.setFilterConfigurationMapper(fcMapper);
        pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root));
        pdriver.addStep(new RawDocumentToFilterEventsStep());
        pdriver.addStep(step);

        // Setup input
        String inputPath = root + "SameSourceAndTarget.html.xlf";
        URI inputURI = new File(inputPath).toURI();
        URI outputURI = inputURI;

        // Add files
        pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_xliff", outputURI, "UTF-8", locEN, locFR));

        pdriver.processBatch();

        // Compare with GOLD file
        maskDocIdPath(root + "inconsistency-report.xml");
        FileCompare fc = new FileCompare();
        assertTrue(fc.compareFilesPerLines(root + "inconsistency-report.xml", root + "Gold_SameSourceAndTarget.xml", "UTF-8"));
    }

    @Test
    public void SimpleSubDocTest() throws URISyntaxException, IOException {
        // Setup pipeline
        IPipelineDriver pdriver = new PipelineDriver();
        FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
        fcMapper.addConfigurations(XLIFFFilter.class.getName());
        pdriver.setFilterConfigurationMapper(fcMapper);
        pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root));
        pdriver.addStep(new RawDocumentToFilterEventsStep());
        pdriver.addStep(step);

        // Setup input
        String inputPath = root + "SimpleSubDocTest.html.xlf";
        URI inputURI = new File(inputPath).toURI();
        URI outputURI = inputURI;

        // Add files
        pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_xliff", outputURI, "UTF-8", locEN, locFR));

        pdriver.processBatch();

        // Compare with GOLD file
        maskDocIdPath(root + "inconsistency-report.xml");
        FileCompare fc = new FileCompare();
        assertTrue(fc.compareFilesPerLines(root + "inconsistency-report.xml", root + "Gold_SimpleSubDocTest.xml", "UTF-8"));
    }

    @Test
    public void SameSourceWithCodeTest() throws URISyntaxException, IOException {
        // Setup pipeline
        IPipelineDriver pdriver = new PipelineDriver();
        FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
        fcMapper.addConfigurations(XLIFFFilter.class.getName());
        pdriver.setFilterConfigurationMapper(fcMapper);
        pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root));
        pdriver.addStep(new RawDocumentToFilterEventsStep());
        pdriver.addStep(step);

        // Setup input
        String inputPath = root + "SameSourceWithCode.html.xlf";
        URI inputURI = new File(inputPath).toURI();
        URI outputURI = inputURI;

        // Add files
        pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_xliff", outputURI, "UTF-8", locEN, locFR));

        // Run pipeline
        pdriver.processBatch();

        // Compare with GOLD file
        maskDocIdPath(root + "inconsistency-report.xml");
        FileCompare fc = new FileCompare();
        assertTrue(fc.compareFilesPerLines(root + "inconsistency-report.xml", root + "Gold_SameSourceWithCode.xml", "UTF-8"));
    }

    @Test
    public void OriginalOutputWithCodeTest() throws URISyntaxException, IOException {
        // setup parameters
        Parameters params = (Parameters) step.getParameters();
        params.setDisplayOption(Parameters.DISPLAYOPTION_ORIGINAL);

        // Setup pipeline
        IPipelineDriver pdriver = new PipelineDriver();
        FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
        fcMapper.addConfigurations(XLIFFFilter.class.getName());
        pdriver.setFilterConfigurationMapper(fcMapper);
        pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root));
        pdriver.addStep(new RawDocumentToFilterEventsStep());
        pdriver.addStep(step);

        // Setup input
        String inputPath = root + "SameSourceWithCode.html.xlf";
        URI inputURI = new File(inputPath).toURI();
        URI outputURI = inputURI;

        // Add files
        pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_xliff", outputURI, "UTF-8", locEN, locFR));

        // Run pipeline
        pdriver.processBatch();

        // Compare with GOLD file
        maskDocIdPath(root + "inconsistency-report.xml");
        FileCompare fc = new FileCompare();
        assertTrue(fc.compareFilesPerLines(root + "inconsistency-report.xml", root + "Gold_OriginalOutputWithCode.xml", "UTF-8"));
    }

    @Test
    public void PlainOutputWithCodeTest() throws URISyntaxException, IOException {
        // setup parameters
        Parameters params = (Parameters) step.getParameters();
        params.setDisplayOption(Parameters.DISPLAYOPTION_PLAIN);

        // Setup pipeline
        IPipelineDriver pdriver = new PipelineDriver();
        FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
        fcMapper.addConfigurations(XLIFFFilter.class.getName());
        pdriver.setFilterConfigurationMapper(fcMapper);
        pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root));
        pdriver.addStep(new RawDocumentToFilterEventsStep());
        pdriver.addStep(step);

        // Setup input
        String inputPath = root + "SameSourceWithCode.html.xlf";
        URI inputURI = new File(inputPath).toURI();
        URI outputURI = inputURI;

        // Add files
        pdriver.addBatchItem(new BatchItemContext(inputURI, "UTF-8", "okf_xliff", outputURI, "UTF-8", locEN, locFR));

        // Run pipeline
        pdriver.processBatch();

        // Compare with GOLD file
        maskDocIdPath(root + "inconsistency-report.xml");
        FileCompare fc = new FileCompare();
        assertTrue(fc.compareFilesPerLines(root + "inconsistency-report.xml", root + "Gold_PlainOutputWithCode.xml", "UTF-8"));
    }

    @Test
    public void PerFileWithCodeTest() throws URISyntaxException, IOException {
        // setup parameters
        Parameters params = (Parameters) step.getParameters();
        params.setDisplayOption(Parameters.DISPLAYOPTION_GENERIC);
        params.setCheckPerFile(true);

        // Setup pipeline
        IPipelineDriver pdriver = new PipelineDriver();
        FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
        fcMapper.addConfigurations(XLIFFFilter.class.getName());
        pdriver.setFilterConfigurationMapper(fcMapper);
        pdriver.setRootDirectories(Util.deleteLastChar(root), Util.deleteLastChar(root));
        pdriver.addStep(new RawDocumentToFilterEventsStep());
        pdriver.addStep(step);

        // Setup input
        String inputPath = root + "SameSourceWithCode.html.xlf";
        URI inputURI1 = new File(inputPath).toURI();
        URI outputURI1 = inputURI1;
        inputPath = root + "SameTargetWithCode.html.xlf";
        URI inputURI2 = new File(inputPath).toURI();
        URI outputURI2 = inputURI2;

        // Add files
        pdriver.addBatchItem(new BatchItemContext(inputURI1, "UTF-8", "okf_xliff", outputURI1, "UTF-8", locEN, locFR));
        pdriver.addBatchItem(new BatchItemContext(inputURI2, "UTF-8", "okf_xliff", outputURI2, "UTF-8", locEN, locFR));

        // Run pipeline
        pdriver.processBatch();

        // Compare with GOLD file
        maskDocIdPath(root + "inconsistency-report.xml");
        FileCompare fc = new FileCompare();
        assertTrue(fc.compareFilesPerLines(root + "inconsistency-report.xml", root + "Gold_PerFileWithCode.xml", "UTF-8"));
    }

    //
    //  Helper Methods
    //
    /**
     * Masks the non-fixed part of the path in the docId URI for a given report
     * file. (this part depends on where the project is built).
     *
     * @param path the path of the report file to change.
     * @throws IOException if an error occurs.
     */
    private void maskDocIdPath(String path)
            throws IOException {
        BufferedReader br = null;
        Writer out = null;
        try {
            // Open the file to convert
            br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
            // Read it all in a buffer
            StringBuilder buf = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                buf.append(line + "\n"); // type of line break doesn't matter for the comparator
                line = br.readLine();
            }
            // Close it
            br.close();
            br = null;

            // Do the conversion
            Pattern pattern = Pattern.compile("doc=\"(.*)\"");
            Matcher m = pattern.matcher(buf.toString());
            int diff = 0;
            int start = 0;
            while (m.find(start)) {
                String filename = Util.getFilename(m.group(1), true);
                buf.replace(m.start() - diff, m.end() - diff, "doc=\"ROOT/" + filename + "\""); // Set the new path
                diff += m.group(1).length() - (filename.length() + 5); // Compute the difference due to the path change
                start = m.start() + 5; // Compute the new start
            }

            // Write out the result
            out = new OutputStreamWriter(new FileOutputStream(path), "UTF-8");
            out.write(buf.toString());
        } finally {
            if (br != null) {
                br.close(); // In case of error
            }
            if (out != null) {
                out.close();
            }
        }
    }
}
