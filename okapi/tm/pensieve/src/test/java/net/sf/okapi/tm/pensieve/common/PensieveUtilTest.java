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

package net.sf.okapi.tm.pensieve.common;

import static org.junit.Assert.assertEquals;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.tm.pensieve.Helper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PensieveUtilTest {

    @Test
    public void stupidTestOnlyForCoverage() throws Exception {
        Helper.genericTestConstructor(PensieveUtil.class);
    }

    @Test
    public void convertToTranslationUnitMetadata(){
        ITextUnit textUnit = new TextUnit("someId", "some great text");
        textUnit.setTargetContent(LocaleId.fromString("kr"), new TextFragment("some great text in Korean"));
        textUnit.setProperty(new Property(MetadataType.FILE_NAME.fieldName(), "sumdumfilename"));
        TranslationUnit tu = PensieveUtil.convertToTranslationUnit(LocaleId.fromString("en"), LocaleId.fromString("kr"), textUnit);
        assertEquals("# of meetadaatas", 1, tu.getMetadata().size());
        assertEquals("file name meta data", "sumdumfilename", tu.getMetadataValue(MetadataType.FILE_NAME));
    }

    @Test
    public void convertToTranslationUnitNonMatchingProperty(){
        ITextUnit textUnit = new TextUnit("someId", "some great text");
        textUnit.setTargetContent(LocaleId.fromString("kr"), new TextFragment("some great text in Korean"));
        textUnit.setProperty(new Property(MetadataType.FILE_NAME.fieldName(), "sumdumfilename"));
        textUnit.setProperty(new Property("somedumbkey", "sumdumvalue"));
        TranslationUnit tu = PensieveUtil.convertToTranslationUnit(LocaleId.fromString("en"), LocaleId.fromString("kr"), textUnit);
        assertEquals("# of meetadaatas", 1, tu.getMetadata().size());
    }

    @Test
    public void convertToTranslationUnit(){
        ITextUnit textUnit = new TextUnit("someId", "some great text");
        textUnit.setTargetContent(LocaleId.fromString("kr"), new TextFragment("some great text in Korean"));
        TranslationUnit tu = PensieveUtil.convertToTranslationUnit(LocaleId.fromString("en"), LocaleId.fromString("kr"), textUnit);
        assertEquals("sourceLang", "en", tu.getSource().getLanguage().toString());
        assertEquals("source content", "some great text", tu.getSource().getContent().toText());
        assertEquals("targetLang", "kr", tu.getTarget().getLanguage().toString());
        assertEquals("target content", "some great text in Korean", tu.getTarget().getContent().toText());
    }

    @Test
    public void convertToTextUnitNullId(){
        TranslationUnit tu = Helper.createTU(LocaleId.fromString("EN"), LocaleId.fromString("KR"), "bipity bopity boo", "something in korean", null);
        tu.setMetadataValue(MetadataType.GROUP_NAME, "groupie");
        ITextUnit textUnit = PensieveUtil.convertToTextUnit(tu);
        assertEquals("source content", "bipity bopity boo", textUnit.getSource().getFirstContent().toText());
        assertEquals("target content", "something in korean",
        	textUnit.getTarget(LocaleId.fromString("KR")).getFirstContent().toText());
        assertEquals("tuid", null, textUnit.getId());
        assertEquals("name", null, textUnit.getName());
        assertEquals("group attribute", "groupie", textUnit.getProperty(MetadataType.GROUP_NAME.fieldName()).getValue());
    }

    @Test
    public void convertToTextUnit(){
        TranslationUnit tu = Helper.createTU(LocaleId.fromString("EN"), LocaleId.fromString("KR"), "bipity bopity boo", "something in korean", "1");
        tu.setMetadataValue(MetadataType.GROUP_NAME, "groupie");
        ITextUnit textUnit = PensieveUtil.convertToTextUnit(tu);
        assertEquals("source content", "bipity bopity boo", textUnit.getSource().getFirstContent().toText());
        assertEquals("target content", "something in korean",
        	textUnit.getTarget(LocaleId.fromString("KR")).getFirstContent().toText());
        assertEquals("tuid", "1", textUnit.getId());
        assertEquals("name", "1", textUnit.getName());
        assertEquals("group attribute", "groupie", textUnit.getProperty(MetadataType.GROUP_NAME.fieldName()).getValue());
    }

}
