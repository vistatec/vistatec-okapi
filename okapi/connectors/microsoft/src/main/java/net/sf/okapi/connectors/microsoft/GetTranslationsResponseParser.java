package net.sf.okapi.connectors.microsoft;

import java.util.ArrayList;
import java.util.List;

/**
 * Parse XML responses from GetTranslations and GetTranslationsArray.
 *
 * XXX I've preserved the old string hacking for now, but once tests are
 * in place it would be nice to switch to real XML parsing.
 */
public class GetTranslationsResponseParser {

	// XXX I could parse this directly out of the response... out of the InputStream
	// probably better.
	public List<TranslationResponse> parseGetTranslationsResponse(String block, int maxHits, int threshold) {
		List<TranslationResponse> list = new ArrayList<>(maxHits);

		int n1, n2, from = 0;
		if (block==null)
			return list;

		// Get the results for the given entry
		while ( true ) {
			// Isolate the next match result
			n1 = block.indexOf("<TranslationMatch>", from);
			if ( n1 < 0 ) break; // Done
			n2 = block.indexOf("</TranslationMatch>", n1);
			String res = block.substring(n1, n2);
			from = n2+1; // For next iteration

			// Parse the found match
			n1 = res.indexOf("<MatchDegree>");
			n2 = res.indexOf("</MatchDegree>", n1+1);
			int score = Integer.parseInt(res.substring(n1+13, n2));
			// Get the rating
			int rating = 5;
			n1 = res.indexOf("<Rating", 0); // No > to handle /> cases
			n2 = res.indexOf("</Rating>", n1);
			if ( n2 > -1 ) {
				rating = Integer.parseInt(res.substring(n1+8, n2));
				// Ensure it's within expected range of -10 to 10.
				if ( rating < -10 ) rating = -10;
				else if ( rating > 10 ) rating = 10;
			}

			// Get the source (when available)
			n1 = res.indexOf("<MatchedOriginalText", 0); // No > to handle /> cases
			n2 = res.indexOf("</MatchedOriginalText", n1);
			String stext = null; // No source (same as original
			if ( n2 > -1 ) stext = unescapeXML(res.substring(n1+21, n2));
			// Translation
			String ttext = "";
			n1 = res.indexOf("<TranslatedText", n2); // No > to handle /> cases
			n2 = res.indexOf("</TranslatedText", n1);
			if ( n2 > -1 ) ttext = unescapeXML(res.substring(n1+16, n2));

			TranslationResponse response = new TranslationResponse(stext, ttext, rating, score);
			if (response.combinedScore >= threshold) {
				list.add(response);
				if (list.size() >= maxHits) {
					break;
				}
			}
		}
		return list;
	}

	private String unescapeXML (String text) {
		text = text.replace("&apos;", "'");
		text = text.replace("&lt;", "<");
		text = text.replace("&gt;", ">");
		text = text.replace("&quot;", "\"");
		return text.replace("&amp;", "&"); // Ampersand must be done last
	}

	public List<List<TranslationResponse>> parseGetTranslationsArrayResponse(String block, int maxHits, int threshold) {
		List<List<TranslationResponse>> responses = new ArrayList<>();
		if (block == null) {
			return responses;
		}
		int from = 0;
		for (from = block.indexOf("<Translations>", from); from >= 0; from = block.indexOf("<Translations>", from)) {
			int n = block.indexOf("</Translations>", from);
			String subBlock = block.substring(from, n);
			responses.add(parseGetTranslationsResponse(subBlock, maxHits, threshold));
			from = n + 1;
		}
		return responses;
	}
}
