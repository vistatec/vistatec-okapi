/*===========================================================================
  Copyright (C) 2009-2014 by the Okapi Framework contributors
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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.connectors.apertium.ApertiumMTConnector;
import net.sf.okapi.connectors.translatetoolkit.TranslateToolkitTMConnector;
import net.sf.okapi.lib.translation.ITMQuery;

public class Main {

	public static void main (String[] args) {
		try {
			/* An implementation of the connector interface is the one for
			 * the Apertium MT engine. Apertium is an open-source GMS project that offers
			 * Rule-based MT capabilityies. There is also a public server that can be used.
			 * This is an example on how to access such server.
			 */
			QueryResult res;
			System.out.println("------------------------------------------------------------");
			System.out.println("Accessing Apertium resources");
			IQuery mtConnector = new ApertiumMTConnector();
			// English to Espertanto
			mtConnector.setLanguages(LocaleId.fromString("en"), LocaleId.fromString("eo"));
			mtConnector.open();
			System.out.println("Apertium MT Service:");
			System.out.println(mtConnector.getSettingsDisplay());
			mtConnector.query("Open the file");
			if ( mtConnector.hasNext() ) {
				res = mtConnector.next();
				System.out.println("   Original: " + res.source.toText());
				System.out.println("Translation: " + res.target.toText());
			}

			/* The default Translate Toolkit repository is Amagama
			 * (http://translate.sourceforge.net/wiki/virtaal/amagama) 
			 * which includes entries from open-source software projects.
			 * Okapi provide a connector to easily query the server.
			 */
			System.out.println("------------------------------------------------------------");
			System.out.println("Accessing Amagama resources");
			ITMQuery connector = new TranslateToolkitTMConnector();
			connector.setLanguages(LocaleId.fromString("en"), LocaleId.fromString("fr"));
			connector.open();
			String query = "Open the file";
			connector.query(query);
			System.out.println(String.format("Amagama results for \"%s\":", query));
			while ( connector.hasNext() ) {
				res = connector.next();
				System.out.println("- Source: " + res.source.toText());
				System.out.println("  Target: " + res.target.toText());
			}

// Example with globalSight connector, for those who can access a GloablSight server
// Simply un-comment the following lines
//			/* Another implementation of the connector interface is the one for
//			 * GlobalSight. GlobalSight is an open-source GMS project that offers
//			 * a TM server and a Web service to access it.
//			 * If you have access to a GlobalSight TM server, the following calls
//			 * will allow you to query it.
//			 */
//			connector = new GlobalSightTMConnector();
//			Parameters params = new Parameters();
//			Scanner scanner = new Scanner(System.in);
//			
//			System.out.println("------------------------------------------------------------");
//			System.out.println("Example of querying a GlobalSight TM server");
//			System.out.println("You need to have access to a GlobalSight TM server for this.");
//			System.out.print("Do you want to continue (y or n + Enter)? ");
//			String tmp = scanner.nextLine();
//			if (( tmp.length() == 0 ) || (( tmp.charAt(0) != 'y') && ( tmp.charAt(0) != 'Y' ))) {
//				// Stop demo here
//				return;
//			}
//			
//			System.out.print("Service URL (e.g. http://myserver:8080/globalsight/services/AmbassadorWebService?wsdl) = ");
//			params.setServerURL(scanner.nextLine());
//			System.out.print("Username = ");
//			params.setUsername(scanner.nextLine());
//			System.out.print("Password = ");
//			params.setPassword(scanner.nextLine());
//			System.out.print("TM Profile name = ");
//			params.setTmProfile(scanner.nextLine());
//			System.out.print("Source language (e.g. en) = ");
//			String srcLang = scanner.nextLine();
//			System.out.print("Target language (e.g. fr) = ");
//			String trgLang = scanner.nextLine();
//			
//			connector.setLanguages(LocaleId.fromString(srcLang), LocaleId.fromString(trgLang));
//			connector.setParameters(params);
//			connector.open();
//			
//			GenericContent fmt = new GenericContent();
//			System.out.print("Text to query = ");
//			query = scanner.nextLine();
//			connector.query(query);
//			System.out.println(String.format("--- GlobalSight-TM results for \"%s\":", query));
//			while ( connector.hasNext() ) {
//				res = connector.next();
//				System.out.println(String.format("- Score: %d%%", res.score));
//				System.out.println(" Source: " + fmt.setContent(res.source).toString());
//				System.out.println(" Target: " + fmt.setContent(res.target).toString());
//			}
			
		}
		catch ( Throwable e ) {
			e.printStackTrace();
		}
	}

}
