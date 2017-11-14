package net.sf.okapi.filters.openxml;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

public class XMLFactoriesForTest implements XMLFactories {
	private XMLInputFactory input = XMLInputFactory.newInstance();
	private XMLOutputFactory output = XMLOutputFactory.newInstance();
	private XMLEventFactory events = XMLEventFactory.newInstance();

	@Override
	public XMLInputFactory getInputFactory() {
		return input;
	}

	@Override
	public XMLOutputFactory getOutputFactory() {
		return output;
	}

	@Override
	public XMLEventFactory getEventFactory() {
		return events;
	}

}
