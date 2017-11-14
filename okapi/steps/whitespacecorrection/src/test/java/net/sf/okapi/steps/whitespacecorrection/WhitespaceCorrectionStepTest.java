package net.sf.okapi.steps.whitespacecorrection;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileCompare;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.html.HtmlFilter;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
import net.sf.okapi.steps.common.FilterEventsToRawDocumentStep;
import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
import net.sf.okapi.steps.segmentation.SegmentationStep;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.sf.okapi.steps.segmentation.Parameters.TRIM_YES;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Provides a whitespace correction test case.
 */
@RunWith(DataProviderRunner.class)
public class WhitespaceCorrectionStepTest {

    private static final String OUTPUT_DIRECTORY_NAME = "out";
    private static final String GOLD_DIRECTORY_NAME = "gold";

    private Path inputDirectoryPath;
    private Path outputDirectoryPath;
    private Path goldDirectoryPath;

    @Before
    public void initialize() throws URISyntaxException {
        Path workingDirectoryPath = Paths.get(getClass().getResource("/segmentation-rules.srx").toURI()).getParent();

        inputDirectoryPath = workingDirectoryPath;
        outputDirectoryPath = workingDirectoryPath.resolve(OUTPUT_DIRECTORY_NAME);
        goldDirectoryPath = workingDirectoryPath.resolve(GOLD_DIRECTORY_NAME);
    }

    @DataProvider
    public static Object[][] roundTripDataProvider() {
        return new Object[][]{
                {"text-with-kutens-and-whitespaces.html", "text-with-periods-and-whitespaces.html", 
                    LocaleId.JAPANESE, LocaleId.ENGLISH, new HtmlFilter(), new PunctuationConversionStep.Ja2EnTranslator(),
                    new WhitespaceCorrectionStep()},
                {"text-with-kutens-and-whitespaces.html", "text-with-kutens.html", LocaleId.ENGLISH, LocaleId.JAPANESE,
                        new HtmlFilter(), new PunctuationConversionStep.En2JaTranslator(), new WhitespaceCorrectionStep()},
        };
    }

    @Test
    @UseDataProvider("roundTripDataProvider")
    public void roundTrip(String inputFilename, String outputFilename, LocaleId sourceLocale, LocaleId targetLocale,
            IFilter filter, PunctuationConversionStep.PunctuationTranslator converter,
            IPipelineStep whitespaceCorrectionStep) throws Exception {
        Path outputFilePath = getOutputDirectoryFilePath(outputFilename);

        new XPipeline(
                "Round-trip through Events",
                new XBatch(
                        new XBatchItem(
                                getInputDirectoryFilePath(inputFilename).toUri(),
                                StandardCharsets.UTF_8.name(),
                                sourceLocale,
                                targetLocale)
                ),

                new RawDocumentToFilterEventsStep(filter),
                getSegmentationStep(),
                new PunctuationConversionStep(converter),
                whitespaceCorrectionStep,
                getFilterEventsToRawDocumentStep(outputFilePath)
        ).execute();

        Assert.assertThat(new FileCompare().filesExactlyTheSame(outputFilePath.toString(), getGoldDirectoryFilePath(outputFilename).toString()), is(true));
    }

    private Path getInputDirectoryFilePath(String filename) {
        return inputDirectoryPath.resolve(filename);
    }

    private Path getOutputDirectoryFilePath(String filename) {
        return outputDirectoryPath.resolve(filename);
    }

    private Path getGoldDirectoryFilePath(String filename) {
        return goldDirectoryPath.resolve(filename);
    }

    private XPipelineStep getSegmentationStep() {
        return new XPipelineStep(
                new SegmentationStep(),
                new XParameter("sourceSrxPath", getInputDirectoryFilePath("segmentation-rules.srx").toString()),
                new XParameter("trimSrcLeadingWS", TRIM_YES),
                new XParameter("trimSrcTrailingWS", TRIM_YES),
                new XParameter("trimTrgLeadingWS", TRIM_YES),
                new XParameter("trimTrgTrailingWS", TRIM_YES)
        );
    }

    private IPipelineStep getFilterEventsToRawDocumentStep(Path outputFilePath) {
        FilterEventsToRawDocumentStep filterEventsToRawDocumentStep = new FilterEventsToRawDocumentStep();

        filterEventsToRawDocumentStep.setOutputURI(outputFilePath.toUri());
        filterEventsToRawDocumentStep.setOutputEncoding(StandardCharsets.UTF_8.name());

        return filterEventsToRawDocumentStep;
    }

    @Test
    public void testRemoveTrailingWhitespace() {
        WhitespaceCorrectionStep step = new WhitespaceCorrectionStep();
        step.setSourceLocale(LocaleId.ENGLISH);
        step.setTargetLocale(LocaleId.JAPANESE);

        TextUnit tu = new TextUnit("tu1");
        tu.setSource(new TextContainer("Hello.  "));
        tu.setTarget(LocaleId.JAPANESE, new TextContainer("Japanese。  "));
        Event e = new Event(EventType.TEXT_UNIT, tu);

        step.handleEvent(e);

        assertThat(e.getTextUnit().getTarget(LocaleId.JAPANESE).toString(), equalTo("Japanese。"));
    }
}
