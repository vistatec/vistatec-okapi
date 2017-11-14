package net.sf.okapi.connectors.microsoft;

import org.junit.Test;

import org.custommonkey.xmlunit.XMLAssert;

public class TestGetTranslateOptions {

	private static final String EXPECTED =
			"<TranslateOptions xmlns=\"http://schemas.datacontract.org/2004/07/Microsoft.MT.Web.Service.V2\">" +
			"<Category>test-category</Category><ContentType>text/html</ContentType><ReservedFlags></ReservedFlags>" +
			"<State></State><Uri></Uri><User>defaultUser</User></TranslateOptions>";

	@Test
	public void testToXML() throws Exception {
		XMLAssert.assertXMLEqual(EXPECTED, new GetTranslateOptions("test-category").toXML());
	}
}