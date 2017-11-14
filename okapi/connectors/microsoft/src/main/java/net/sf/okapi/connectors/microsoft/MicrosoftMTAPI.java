package net.sf.okapi.connectors.microsoft;

import java.io.IOException;
import java.util.List;

public interface MicrosoftMTAPI {

    /**
     * Call the getTranslations() API method.
     * @return API responses, or null if the API call fails
     */
	List<TranslationResponse> getTranslations(String query, String srcLang, String trgLang, int maxHits, int threshold);

    /**
     * Call the getTranslationsArray() API method.
     * @return API responses, or null if the API call fails
     */
	List<List<TranslationResponse>> getTranslationsArray(GetTranslationsArrayRequest request, String srcLang,
			String trgLang, int maxHits, int threshold);

	int addTranslation(String original, String translation, String srcLang, String trgLang, int rating)
			throws IOException;

	int addTranslationArray(List<String> sources, List<String> translations, List<Integer> ratings, String srcLang,
			String trgLang) throws IOException;

}