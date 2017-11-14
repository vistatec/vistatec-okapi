package net.sf.okapi.filters.openxml;

import static org.junit.Assert.fail;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractOpenXMLRoundtripTest {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    protected boolean allGood=true;

    public void runOneTest (String filename, boolean bTranslating, boolean bPeeking, ConditionalParameters cparams) {
        runOneTest(filename, bTranslating, bPeeking, cparams, "");
    }
    public void runOneTest (String filename, boolean bTranslating, boolean bPeeking, ConditionalParameters cparams, String goldSubdirPath) {
        runOneTest(filename, bTranslating, bPeeking, cparams, goldSubdirPath, LocaleId.US_ENGLISH);
    }
    public void runOneTest (String filename, boolean bTranslating, boolean bPeeking, ConditionalParameters cparams,
            String goldSubdirPath, LocaleId localeId) {
        FileLocation root = FileLocation.fromClass(getClass());
        Event event;
        URI uri;
        OpenXMLFilter filter = null;
        boolean rtrued2;
        try {
            if (bPeeking)
            {
                if (bTranslating)
                    filter = new OpenXMLFilter(new CodePeekTranslator(), localeId);
                else
                    filter = new OpenXMLFilter(new TagPeekTranslator(), localeId);
            }
            else if (bTranslating) {
                filter = new OpenXMLFilter(new PigLatinTranslator(), localeId);
            }
            else
                filter = new OpenXMLFilter();

            filter.setParameters(cparams);
            filter.setOptions(localeId, "UTF-8", true);

            uri = root.in("/" + filename).asUri();

            try
            {
                filter.open(new RawDocument(uri,"UTF-8", localeId));
            }
            catch(Exception e)
            {
                throw new OkapiException(e);
            }

            OpenXMLZipFilterWriter writer = new OpenXMLZipFilterWriter(); // DWH 4-8-09 was just ZipFilterWriter
            writer.setParameters(cparams);

            if (bPeeking)
                writer.setOptions(localeId, "UTF-8");
            else if (bTranslating)
                writer.setOptions(localeId, "UTF-8");
                //				writer.setOptions(locLA, "UTF-8");
            else
                writer.setOptions(localeId, "UTF-8");

            String prefix = bPeeking ? (bTranslating ? "Peek" : "Tag") : (bTranslating ? "Tran" : "Out");
            String writerFilename = "/" + prefix + filename;
            writer.setOutput(root.out(writerFilename).toString());

            while ( filter.hasNext() ) {
                event = filter.next();
                if (event!=null)
                {
                    writer.handleEvent(event);
                }
                else
                    event = null; // just for debugging
            }
            writer.close();
            Path outputPath = root.out(writerFilename).asPath();
            LOGGER.debug("Output: {}", outputPath);
            Path goldPath = Paths.get(root.in("/gold").toString(), goldSubdirPath, writerFilename);
            LOGGER.debug("Gold: {}", goldPath);

            OpenXMLPackageDiffer differ = new OpenXMLPackageDiffer(Files.newInputStream(goldPath),
                    Files.newInputStream(outputPath));
            rtrued2 = differ.isIdentical();
            if (!rtrued2) {
                LOGGER.warn("{}{}{}", prefix, filename, (rtrued2 ? " SUCCEEDED" : " FAILED"));
                for (OpenXMLPackageDiffer.Difference d : differ.getDifferences()) {
                    LOGGER.warn("+ {}", d.toString());
                }
            }
            if (!rtrued2)
                allGood = false;
            differ.cleanup();
        }
        catch ( Throwable e ) {
            LOGGER.warn("Failed to roundtrip file " + filename, e);
            fail("An unexpected exception was thrown on file '"+filename+"', msg="+e.getMessage());
        }
        finally {
            if ( filter != null ) filter.close();
        }
    }
}
