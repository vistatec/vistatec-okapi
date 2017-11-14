package net.sf.okapi.connectors.microsoft;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.XMLWriter;

public class AddTranslationsRequest {

	private List<String> sources = new ArrayList<>();
	private List<String> translations = new ArrayList<>();
	private List<Integer> ratings = new ArrayList<>();
	private String category;
	private String srcLang;
	private String trgLang;
	private String contentType = "text/html";

	public AddTranslationsRequest(List<String> sources, List<String> translations, List<Integer> ratings,
								  String srcLang, String trgLang, String category) {
		this.sources = sources;
		this.translations = translations;
		this.ratings = ratings;
		this.srcLang = srcLang;
		this.trgLang = trgLang;
		this.category = category;
	}

	public String toXML() {
		StringWriter strWriter = new StringWriter();
		XMLWriter xmlWriter = new XMLWriter(strWriter);
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("AddtranslationsRequest");
		xmlWriter.writeAttributeString("xmlns:o", "http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2");
		xmlWriter.writeElementString("AppId", "");
		xmlWriter.writeElementString("From", srcLang);
		xmlWriter.writeStartElement("Options");
		xmlWriter.writeElementString("o:Category", category);
		xmlWriter.writeElementString("o:ContentType", contentType);
		xmlWriter.writeElementString("o:ReservedFlags", "");
		xmlWriter.writeElementString("o:State", "");
		xmlWriter.writeElementString("o:Uri", "");
		xmlWriter.writeElementString("o:User", "defaultUser");
		xmlWriter.writeEndElement(); // Options
		xmlWriter.writeElementString("To", trgLang);
		xmlWriter.writeStartElement("Translations");
		for (int i = 0; i < sources.size(); i++) {
			writeAddTranslationListEntry(xmlWriter, sources.get(i), translations.get(i), ratings.get(i));
		}
		xmlWriter.writeEndElement(); // !Translations

		xmlWriter.writeEndElement(); // !AddtranslationsRequest
		xmlWriter.writeEndDocument();
		xmlWriter.close();
		return strWriter.toString();
	}

	private void writeAddTranslationListEntry(XMLWriter xmlWriter, String source, String translation, int rating) {
		if (( rating < -10 ) && ( rating > 10 )) rating = 4;
		xmlWriter.writeStartElement("o:Translation");
		// Source
		xmlWriter.writeStartElement("o:OriginalText");
		xmlWriter.writeString(source);
		xmlWriter.writeEndElement();
		// Rating
		xmlWriter.writeStartElement("o:Rating");
		xmlWriter.writeString(String.valueOf(rating));
		xmlWriter.writeEndElement();
		// Sequence
		xmlWriter.writeStartElement("o:Sequence");
		xmlWriter.writeString("0");
		xmlWriter.writeEndElement();
		// Translation
		xmlWriter.writeStartElement("o:TranslatedText");
		xmlWriter.writeString(translation);
		xmlWriter.writeEndElement();
		xmlWriter.writeEndElement(); // !Translation
	}
}
