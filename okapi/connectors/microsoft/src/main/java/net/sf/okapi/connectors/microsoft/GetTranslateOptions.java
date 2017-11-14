package net.sf.okapi.connectors.microsoft;

import java.io.StringWriter;

import net.sf.okapi.common.XMLWriter;

public class GetTranslateOptions {
	private String category;
	private String contentType = "text/html";

	public GetTranslateOptions(String category) {
		this.category = category;
	}

	public String toXML() {
		StringWriter sw = new StringWriter();
		XMLWriter xml = new XMLWriter(sw);
		xml.writeStartDocument();
		xml.writeStartElement("TranslateOptions");
		xml.writeAttributeString("xmlns", "http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2");
		xml.writeStartElement("Category");
		xml.writeString(this.category);
		xml.writeEndElement();
		xml.writeStartElement("ContentType");
		xml.writeString(this.contentType);
		xml.writeEndElement();
		xml.writeStartElement("ReservedFlags");
		xml.writeEndElement();
		xml.writeStartElement("State");
		xml.writeEndElement();
		xml.writeStartElement("Uri");
		xml.writeEndElement();
		xml.writeStartElement("User");
		xml.writeString("defaultUser");
		xml.writeEndElement();
		xml.writeEndElement(); // !TranslateOptions
		xml.close();
		return sw.toString();
	}
}