/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.verification;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.internal.matchers.Any;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;

@RunWith(JUnit4.class)
public class LanguageToolTest {

    private QualityCheckSession session;
    private LocaleId locEN = LocaleId.US_ENGLISH;
    private LocaleId locFR = LocaleId.FRENCH;
    LanguageToolConnector ltConn;
    LanguageToolConnector ltConnBilingual;

    String response = "{\"software\":{\"name\":\"LanguageTool\",\"version\":\"3.7-SNAPSHOT\",\"buildDate\":\"2017-02-06 21:01\",\"apiVersion\":\"1\",\"status\":\"\"},"
            + "\"language\":{\"name\":\"French\",\"code\":\"fr\"},"
            + "\"matches\":[{\"message\":\"Cette phrase ne commence pas par une majuscule\",\"shortMessage\":\"\",\"replacements\":[{\"value\":\"Mon\"}],\"offset\":0,\"length\":3,"
            + "\"context\":{\"text\":\"mon texxte\",\"offset\":0,\"length\":3},\"rule\":{\"id\":\"UPPERCASE_SENTENCE_START\",\"description\":\"Absence de majuscule en dГ©but de phrase\","
            + "\"issueType\":\"typographical\",\"category\":{\"id\":\"CASING\",\"name\":\"Majuscules\"}}},"
            + "{\"message\":\"Faute de frappe possible trouvГ©e\",\"shortMessage\":\"Faute de frappe\",\"replacements\":[],\"offset\":4,\"length\":6,"
            + "\"context\":{\"text\":\"mon texxte\",\"offset\":4,\"length\":6},\"rule\":{\"id\":\"HUNSPELL_NO_SUGGEST_RULE\",\"description\":\"Faute d'orthographe possible (sans suggestions)\","
            + "\"issueType\":\"misspelling\",\"category\":{\"id\":\"TYPOS\",\"name\":\"Faute de frappe possible\"}}}]}";

    public LanguageToolTest() {
        session = new QualityCheckSession();
        session.startProcess(locEN, locFR);
        //You can use it for testing on current version of real API
        //but by default used mocks of 3.7 API (check tests)
        String serverUrl = "https://languagetool.org/api/"; // Access is limited to 20 requests per IP per minute. http://wiki.languagetool.org/public-http-api

        ltConn = Mockito.spy(new LanguageToolConnector());
        ltConn.initialize(locFR, locEN, serverUrl, session.getParameters().getTranslateLTMsg(), false,
                session.getParameters().getLtTranslationSource(), session.getParameters().getLtTranslationTarget(),
                session.getParameters().getLtTranslationServiceKey());

        ltConnBilingual = Mockito.spy(new LanguageToolConnector());
        ltConnBilingual.initialize(locFR, locEN, serverUrl, session.getParameters().getTranslateLTMsg(), true,
                session.getParameters().getLtTranslationSource(), session.getParameters().getLtTranslationTarget(),
                session.getParameters().getLtTranslationServiceKey());
    }

    @Test
    public void testLanguageTool() throws Exception {
        ITextUnit tu = new TextUnit("id", "My text");
        tu.setTarget(locFR, new TextContainer("mon texxte"));

        TextContainer srcCont = tu.getSource();
        TextContainer trgCont = tu.getTarget(locFR);

        ISegments srcSegs = srcCont.getSegments();
        ISegments trgSegs = trgCont.getSegments();

        //mock of response
        Mockito.doReturn((JSONObject) new JSONParser().parse(response)).when(ltConn).sendRequest(Mockito.any(URL.class));

        ltConn.checkSegment(new URI(""), "", srcSegs.get(0), trgSegs.get(0), tu);
        List<Issue> issues = ltConn.getIssues();
        assertEquals(2, issues.size());
        assertEquals("typographical", issues.get(0).getITSType());
        assertEquals("misspelling", issues.get(1).getITSType());
    }

    @Test
    public void testLanguageToolBilingual() throws Exception {
        ITextUnit tu = new TextUnit("id", "My text");
        tu.setTarget(locFR, new TextContainer("mon texxte"));

        TextContainer srcCont = tu.getSource();
        TextContainer trgCont = tu.getTarget(locFR);

        ISegments srcSegs = srcCont.getSegments();
        ISegments trgSegs = trgCont.getSegments();

        //mock of response
        //yes, the same response as in not bilingual mode (v3.7)
        Mockito.doReturn((JSONObject) new JSONParser().parse(response)).when(ltConnBilingual).sendRequest(Mockito.any(URL.class));

        ltConnBilingual.checkSegment(new URI(""), "", srcSegs.get(0), trgSegs.get(0), tu);
        List<Issue> issues = ltConnBilingual.getIssues();
        assertEquals(2, issues.size());
        assertEquals("typographical", issues.get(0).getITSType());
        assertEquals("misspelling", issues.get(1).getITSType());
    }

}
