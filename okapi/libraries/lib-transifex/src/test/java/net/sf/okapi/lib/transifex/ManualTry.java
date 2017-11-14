/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.lib.transifex;

import net.sf.okapi.common.LocaleId;

public class ManualTry {

	public static void main (String args[]) {
		TransifexClient cli = new TransifexClient("https://www.transifex.com/api/2");
		cli.setCredentials("archi", "PASSWORDHERE");

//		cli.setProject("Icaria2");
//		System.out.println(cli.getHost());
//		Object[] res = cli.getResourceList(LocaleId.fromString("en-us"));
//		System.out.println(res[0]);
//		System.out.println(res[1]);
//		System.out.println(res[2]);
		
//		String[] res2 = cli.createProject("testProject", "testProject", "desc-short",
//			LocaleId.ENGLISH, false, "http://okapi.opentag.com");
//		System.out.println(res2[0]);

		cli.setProject("testProject");
		String poPath = "C:\\Users\\ysavourel\\Desktop\\MQ\\testpo.po";
//		String[] res3 = cli.putSourceResource(poPath, LocaleId.ENGLISH, "thePOFile");
//		System.out.println(res3[0]);
		poPath = "C:\\Users\\ysavourel\\Desktop\\MQ\\testpo_fr.po";
		String[] res4 = cli.putTargetResource(poPath, LocaleId.FRENCH, "thePOFile", "thePOFile");
		System.out.println(res4[0]);
	}
	
}
