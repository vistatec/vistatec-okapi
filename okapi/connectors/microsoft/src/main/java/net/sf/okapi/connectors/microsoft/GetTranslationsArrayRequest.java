package net.sf.okapi.connectors.microsoft;

import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

import net.sf.okapi.common.XMLWriter;

class GetTranslationsArrayRequest {
	private String category;
	private List<String> texts;
	private String srcLang;
	private String trgLang;
	private int maxHits;
	private String contentType = "text/html";

	GetTranslationsArrayRequest(List<String> texts, String srcLang, String trgLang, int maxHits, String category) {
		this.texts = texts;
		this.category = category;
		this.srcLang = srcLang;
		this.trgLang = trgLang;
		this.maxHits = maxHits;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null || !(o instanceof GetTranslationsArrayRequest)) return false;
		GetTranslationsArrayRequest r = (GetTranslationsArrayRequest)o;
		return Objects.equals(category, r.category) &&
			   Objects.equals(texts, r.texts) &&
			   Objects.equals(srcLang, r.srcLang) &&
			   Objects.equals(trgLang, r.trgLang) &&
			   Objects.equals(maxHits, r.maxHits);
	}

	@Override
	public int hashCode() {
		return Objects.hash(category, texts, srcLang, trgLang, maxHits);
	}

	public String toXML() {
		StringWriter sw = new StringWriter();
		XMLWriter xmlWriter = new XMLWriter(sw);
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("GetTranslationsArrayRequest");
		xmlWriter.writeElementString("AppId", "");
		xmlWriter.writeElementString("From", srcLang);
		xmlWriter.writeStartElement("Options");
		xmlWriter.writeAttributeString("xmlns:o", "http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2");
		xmlWriter.writeElementString("o:Category", category);
		xmlWriter.writeElementString("o:ContentType", contentType);
		xmlWriter.writeElementString("o:ReservedFlags", "");
		xmlWriter.writeElementString("o:State", "");
		xmlWriter.writeElementString("o:Uri", "");
		xmlWriter.writeElementString("o:User", "");
		xmlWriter.writeEndElement(); // Options
		xmlWriter.writeStartElement("Texts");
		xmlWriter.writeAttributeString("xmlns:s", "http://schemas.microsoft.com/2003/10/Serialization/Arrays");
		for (String text : texts) {
			xmlWriter.writeStartElement("s:string");
			xmlWriter.writeString(text);
			xmlWriter.writeEndElement();
		}
		xmlWriter.writeEndElement(); // !Texts
		xmlWriter.writeElementString("To", trgLang);
		xmlWriter.writeElementString("MaxTranslations", String.valueOf(maxHits));
		xmlWriter.writeEndElement(); // !GetTranslationsArrayRequest
		xmlWriter.writeEndDocument();
		xmlWriter.close();
		return sw.toString();
	}
}
