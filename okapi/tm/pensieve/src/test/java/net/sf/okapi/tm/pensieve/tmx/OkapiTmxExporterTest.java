/*===========================================================================
Copyright (C) 2008-2011 by the Okapi Framework contributors
-----------------------------------------------------------------------------
Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.tm.pensieve.tmx;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.tm.pensieve.Helper;
import net.sf.okapi.tm.pensieve.common.MetadataType;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.seeker.PensieveSeeker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;

/**
 * @author Dax
 */
@RunWith(JUnit4.class)
public class OkapiTmxExporterTest {

    URI sampleTMX;
    OkapiTmxExporter handler;
    TMXWriter mockTmxWriter;
    Iterator<TranslationUnit> mockIterator;
    PensieveSeeker mockSeeker;
    LocaleId locEN = LocaleId.fromString("en");
    LocaleId locFR = LocaleId.fromString("fr");
    LocaleId locKR = LocaleId.fromString("kr");
    LocaleId locProps = LocaleId.fromString("Props"); // Not sure what is this locale?
    
    ArgumentCaptor<ITextUnit> tuCapture;

    
    @SuppressWarnings("unchecked")
	@Before
    public void setUp() throws URISyntaxException, IOException {
        tuCapture = ArgumentCaptor.forClass(ITextUnit.class);
        
        mockIterator = mock(Iterator.class);
        mockTmxWriter = mock(TMXWriter.class);

        sampleTMX = new URI("test.tmx");
        handler = new OkapiTmxExporter();

        mockSeeker = mock(PensieveSeeker.class);

        when(mockIterator.hasNext())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        TranslationUnit tuWithMetadata = Helper.createTU(locEN, locProps, "props_source", "props_target", "props_sourceid");
        tuWithMetadata.setMetadataValue(MetadataType.GROUP_NAME, "PropsGroupName");
        tuWithMetadata.setMetadataValue(MetadataType.FILE_NAME, "PropsFileName");

        when(mockIterator.next())
                .thenReturn(Helper.createTU(locEN, locFR, "source", "target", "sourceid"))
                .thenReturn(Helper.createTU(locEN, locFR, "source2", "target2", "sourceid2"))
                .thenReturn(Helper.createTU(locEN, locKR, "kr_source", "kr_target", "kr_sourceid"))
                .thenReturn(tuWithMetadata)
                .thenReturn(null);

        when(mockSeeker.iterator()).thenReturn(mockIterator);
    }

    @Test
    public void exportTmxBehavior() throws IOException {
        handler.exportTmx(locEN, locFR, mockSeeker, mockTmxWriter);

        verify(mockTmxWriter).writeStartDocument(locEN, locFR, "pensieve", "0.0.1", "sentence", "pensieve", "unknown");
        verify(mockTmxWriter, times(2)).writeTUFull((ITextUnit) anyObject());
        verify(mockTmxWriter).writeEndDocument();
        verify(mockTmxWriter).close();
    }

    @Test
    public void exportTmxTextUnitContentNoProps() throws IOException {
        handler.exportTmx(locEN, locFR, mockSeeker, mockTmxWriter);

        verify(mockTmxWriter, times(2)).writeTUFull(tuCapture.capture());
        assertEquals("source of first tu written", "source", tuCapture.getAllValues().get(0).getSource().getFirstContent().toText());
        assertEquals("target of first tu written", "target", tuCapture.getAllValues().get(0).getTarget(locFR).getFirstContent().toText());
        assertEquals("target of first tu written", "sourceid", tuCapture.getAllValues().get(0).getName());
        assertEquals("source of second tu written", "source2", tuCapture.getAllValues().get(1).getSource().getFirstContent().toText());
        assertEquals("target of second tu written", "target2", tuCapture.getAllValues().get(1).getTarget(locFR).getFirstContent().toText());
        assertEquals("target of second tu written", "sourceid2", tuCapture.getAllValues().get(1).getName());
    }

    @Test
    public void exportTmxTextUnitContentWithProps() throws IOException {
        handler.exportTmx(locEN, locProps, mockSeeker, mockTmxWriter);

        verify(mockTmxWriter, times(1)).writeTUFull(tuCapture.capture());
        ITextUnit capturedTU = tuCapture.getValue();
        assertEquals("source of first tu written", "props_source", capturedTU.getSource().getFirstContent().toText());
        assertEquals("target of first tu written", "props_target", capturedTU.getTarget(locProps).getFirstContent().toText());
        assertEquals("target of first tu written", "props_sourceid", capturedTU.getName());
        assertEquals("groupname metadata", "PropsGroupName", capturedTU.getProperty("Txt::GroupName").getValue());
        assertEquals("filename metadata", "PropsFileName", capturedTU.getProperty("Txt::FileName").getValue());
        assertEquals("metadata size", 2, capturedTU.getPropertyNames().size());
    }

    @Test
    public void exportTmxAllTargetLang() throws IOException {
        handler.exportTmx(locEN, mockSeeker, mockTmxWriter);
        verify(mockTmxWriter, times(4)).writeTUFull((ITextUnit)anyObject());
    }

    @Test
    public void exportTmxNoMatchingSourceLang() throws IOException {
        handler.exportTmx(locKR, locFR, mockSeeker, mockTmxWriter);
        verify(mockTmxWriter, never()).writeTUFull((ITextUnit)anyObject());
    }

    @Test
    public void exportTmxSpecificTargetLang() throws IOException {
        handler.exportTmx(locEN, locKR, mockSeeker, mockTmxWriter);
        verify(mockTmxWriter, times(1)).writeTUFull(tuCapture.capture());
        assertEquals("target of first tu written", "kr_sourceid", tuCapture.getValue().getName());
    }

    @Test
    public void exportTmxSeekerNull() throws IOException {
        String errMsg = null;
        try {
            handler.exportTmx(LocaleId.EMPTY, LocaleId.EMPTY, null, mockTmxWriter);
        } catch (IllegalArgumentException iae) {
            errMsg = iae.getMessage();
        }
        assertEquals("Error message", "'tmSeeker' was not set", errMsg);
    }

    @Test
    public void exportTmxWriterNull() throws IOException {
        String errMsg = null;
        try {
            handler.exportTmx(LocaleId.EMPTY, LocaleId.EMPTY, mockSeeker, null);
        } catch (IllegalArgumentException iae) {
            errMsg = iae.getMessage();
        }
        assertEquals("Error message", "'tmxWriter' was not set", errMsg);
    }

    @Test
    public void exportTmxSourceLangNull() throws IOException {
        String errMsg = null;
        try {
            handler.exportTmx(null, LocaleId.EMPTY, mockSeeker, mockTmxWriter);
        } catch (IllegalArgumentException iae) {
            errMsg = iae.getMessage();
        }
        assertEquals("Error message", "'sourceLang' was not set", errMsg);
    }
}
