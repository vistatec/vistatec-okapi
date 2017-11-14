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

package net.sf.okapi.applications.rainbow.utilities.extraction;

import net.sf.okapi.applications.rainbow.lib.IAnalysisReporter;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;

class HTMLReporter implements IAnalysisReporter {

	private XMLWriter writer;
	private int docSegTotal;
	private int docSegExact;
	private int docSegFuzzy;
	private int totalSegTotal;
	private int totalSegExact;
	private int totalSegFuzzy;
	private String docPath;

	public void create (String path) {
		writer = new XMLWriter(path);
		writer.writeStartDocument();
		writer.writeStartElement("html");
		writer.writeStartElement("head");
		writer.writeRawXML("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
		writer.writeRawXML("<style>td.n {font-family:Courier New; text-align:right; white-space:pre}</style>\n");
		writer.writeEndElement(); // head
		writer.writeStartElement("body");
		writer.writeRawXML("<table cellspacing=\"0\" cellpadding=\"4\" border=\"1\">");

		writer.writeElementString("h2", "Translation Package Report");

		writer.writeStartElement("tr");
		writer.writeRawXML(String.format("<td class=\"h\">%s</td>", "Exact"));
		writer.writeRawXML(String.format("<td class=\"h\">%s</td>", "Fuzzy"));
		writer.writeRawXML(String.format("<td class=\"h\">%s</td>", "New"));
		writer.writeRawXML(String.format("<td class=\"h\">%s</td>", "Total"));
		writer.writeRawXML(String.format("<td class=\"h\">%s</td>", "Documents"));
		writer.writeEndElement(); // tr

	}

	public void close () {
		if ( writer != null ) {
			// Compute grand total
			writer.writeStartElement("tr");
			// Exact
			writer.writeRawXML(String.format("<td class=\"n\">%d (%3d%%)</td>",
				totalSegExact, Util.getPercentage(totalSegExact, totalSegTotal)));
			// Fuzzy
			writer.writeRawXML(String.format("<td class=\"n\">%d (%3d%%)</td>",
				totalSegFuzzy, Util.getPercentage(totalSegFuzzy, totalSegTotal)));
			// New
			int n = totalSegTotal-(totalSegExact+totalSegFuzzy);
			writer.writeRawXML(String.format("<td class=\"n\">%d (%3d%%)</td>",
				n, Util.getPercentage(n, totalSegTotal)));
			// Total
			writer.writeRawXML(String.format("<td class=\"n\">%d</td>", totalSegTotal));
			// Path
			writer.writeElementString("td", "All Documents");
			writer.writeEndElement(); // tr

			// Close all elements
			writer.writeRawXML("</table>"); // table
			writer.writeEndElement(); // body
			writer.writeEndElement(); // html
			writer.writeEndDocument();
			writer.close();
			writer = null;
		}
	}

	public void startDocument (String path) {
		docPath = path;
		docSegTotal = docSegExact = docSegFuzzy = 0;
	}
	
	public void endDocument () {
		// Compute for grand total
		totalSegTotal += docSegTotal;
		totalSegExact += docSegExact;
		totalSegFuzzy += docSegFuzzy;
		
		writer.writeStartElement("tr");
		// Exact
		writer.writeRawXML(String.format("<td class=\"n\">%d (%3d%%)</td>",
			docSegExact, Util.getPercentage(docSegExact, docSegTotal)));
		// Fuzzy
		writer.writeRawXML(String.format("<td class=\"n\">%d (%3d%%)</td>",
			docSegFuzzy, Util.getPercentage(docSegFuzzy, docSegTotal)));
		// New
		int n = docSegTotal-(docSegExact+docSegFuzzy);
		writer.writeRawXML(String.format("<td class=\"n\">%d (%3d%%)</td>",
			n, Util.getPercentage(n, docSegTotal)));
		// Total
		writer.writeRawXML(String.format("<td class=\"n\">%d</td>", docSegTotal));
		// Path
		writer.writeElementString("td", docPath);
		writer.writeEndElement(); // tr
	}
	
	public void addExactMatch (int value) {
		docSegExact += value;
	}

	public void addFuzzyMatch (int value) {
		docSegFuzzy += value;
	}

	public void addSegmentCount (int value) {
		docSegTotal += value;
	}

}
