/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.microsoft;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MicrosoftBatchTokenConnectorTest {

	@Test
	public void paramTest () {
		MicrosoftMTConnector mmtc = new MicrosoftMTConnector();
		Parameters params = (Parameters) mmtc.getParameters();
		params.setAzureKey("testAzureKey");
		assertEquals("testAzureKey", params.getAzureKey());
	}

	// To test manually: uncomment and add clientId and secret
	// Make sure to comment before pushing to public repository
	//@Test
	public void manualTest () {
		int lenny;
		int lynn;
		QueryResult result;
		TextFragment frag;
		List<QueryResult> franz;
		List<List<QueryResult>> liszt;
		ArrayList<TextFragment> froggies;
		String sTranslation="";
		MicrosoftMTConnector mmtc = new MicrosoftMTConnector();
		Parameters params = (Parameters) mmtc.getParameters();
		// Add ClientId and Secret to test
		params.setAzureKey("");
		params.setCategory("");
		
		// test query
		mmtc.open();
		mmtc.setLanguages(new LocaleId("en", "US"), new LocaleId("es", "ES"));
		mmtc.setThreshold(0);
		frag = new TextFragment("What a wonderful bird the frog are!");
		mmtc.query(frag);
		if (mmtc.hasNext()) {
			result = mmtc.next();
			frag = result.target;
			sTranslation = frag.getText();
		}
		assertTrue(sTranslation.equals("Lo que un pájaro maravilloso son la rana!"));
		
		froggies = new ArrayList<TextFragment>();
		froggies.add(new TextFragment("When he stand he sit almost."));
		froggies.add(new TextFragment("When he hop he fly almost."));
		froggies.add(new TextFragment("He ain't got no sense hardly."));
		liszt = mmtc.batchQuery(froggies);
		lynn = liszt.size();
		sTranslation = "";
		for(int i=0; i<lynn; i++) {
			franz = liszt.get(i);
			lenny = franz.size();
			for(int j=0; j<lenny; j++) {
				sTranslation += "$" + franz.get(j).target.getText();				
			}
		}
		assertEquals("$Cuando él siente casi.$Cuando él salto que volar casi.$Él no consiguió ningún sentido apenas.", sTranslation);
		froggies = new ArrayList<TextFragment>();
		for(int i=0; i<10; i++) {
			froggies.add(new TextFragment(Integer.toString(i)+". When he stand he sit almost."));
		}
		liszt = mmtc.batchQuery(froggies);
		lynn = liszt.size();
		sTranslation = "";
		for(int i=0; i<lynn; i++) {
			franz = liszt.get(i);
			lenny = franz.size();
			for(int j=0; j<lenny; j++) {
				sTranslation = franz.get(j).target.getText();
				if (!(sTranslation.equalsIgnoreCase(Integer.toString(i)+". Cuando siente casi.") ||
					  (sTranslation.equalsIgnoreCase(Integer.toString(i)+". Cuando él siente casi.")))) {
					i = 0;
				}
				assertTrue(sTranslation.equalsIgnoreCase(Integer.toString(i)+". Cuando siente casi.") ||
						  (sTranslation.equalsIgnoreCase(Integer.toString(i)+". Cuando él siente casi.")));
			}
		}
	}
}
