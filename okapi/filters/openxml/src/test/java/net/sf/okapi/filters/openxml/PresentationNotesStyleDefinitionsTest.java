package net.sf.okapi.filters.openxml;

import org.junit.Test;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import java.io.InputStreamReader;
import java.io.Reader;

import static java.util.Collections.singletonList;
import static net.sf.okapi.filters.openxml.RunPropertyFactory.createRunProperty;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ccudennec
 * @since 07.09.2017
 */
public class PresentationNotesStyleDefinitionsTest {

    @Test
    public void testGetCombinedRunProperties() throws Exception {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/pptxParser/notesMaster/notesMaster1.xml"), "UTF-8")) {
            StyleDefinitions styleDefinitions = new PresentationNotesStylesParser(
                    XMLEventFactory.newFactory(),
                    XMLInputFactory.newFactory(),
                    reader,
                    new ConditionalParameters()
            ).parse();

            assertThat(styleDefinitions).isNotNull();

            RunProperties runProperties = new RunProperties.DefaultRunProperties(null, null, singletonList(createRunProperty(new QName(null, "baseline"), "0")));

            RunProperties combinedRunProperties = styleDefinitions.getCombinedRunProperties(null, "unknown", runProperties);

            assertThat(getRunPropertyValueByLocalPart(combinedRunProperties, "baseline")).isEqualTo("0");
            assertThat(getRunPropertyValueByLocalPart(combinedRunProperties, "kern")).isEqualTo("1200");
            assertThat(getRunPropertyValueByLocalPart(combinedRunProperties, "sz")).isEqualTo("1200");
        }
    }

    private String getRunPropertyValueByLocalPart(RunProperties runProperties, String localPart) {
        for (RunProperty runProperty : runProperties.getProperties()) {

            if (localPart.equals(runProperty.getName().getLocalPart())) {
                return runProperty.getValue();
            }
        }

        return null;
    }
}