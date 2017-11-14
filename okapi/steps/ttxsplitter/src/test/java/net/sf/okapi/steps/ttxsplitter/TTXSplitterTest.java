package net.sf.okapi.steps.ttxsplitter;

import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import net.sf.okapi.common.Util;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@RunWith(JUnit4.class)
public class TTXSplitterTest {
	
	private TTXSplitter splitter;

	@Test
	public void testSplitThenJoinSegmented ()
		throws MalformedURLException, URISyntaxException, XMLStreamException, IOException, SAXException
	{
		splitThenJoin("Test01.html");
	}

	@Test
	public void testSplitThenJoinNotSegmented ()
		throws MalformedURLException, URISyntaxException, XMLStreamException, IOException, SAXException
	{
		splitThenJoin("Test02_noseg.html");
	}

	private void splitThenJoin (String basename) 
		throws URISyntaxException, MalformedURLException, XMLStreamException, IOException, SAXException
	{
		String root = Util.getDirectoryName(getClass().getResource(
			"/Test01.html.ttx").toURI().getPath()) + File.separator;
		
		File joinedFile = new File(root+basename+"_joined.ttx");
		joinedFile.delete();
		File file = new File(root+basename+"_part001.ttx");
		file.delete();
		file = new File(root+basename+"_part002.ttx");
		file.delete();
		
		TTXSplitterParameters params = new TTXSplitterParameters();
		splitter = new TTXSplitter(params);
		File oriFile = new File(root+basename+".ttx");
		splitter.split(oriFile.toURI());

		// Check out (and prepare the join)
		List<URI> inputList = new ArrayList<>();
		file = new File(root+basename+"_part001.ttx");
		assertTrue(file.exists());
		inputList.add(file.toURI());
		file = new File(root+basename+"_part002.ttx");
		assertTrue(file.exists());
		inputList.add(file.toURI());
		
		TTXJoinerParameters params2 = new TTXJoinerParameters();
		TTXJoiner joiner = new TTXJoiner(params2);
		joiner.process(inputList);

		InputSource original = new InputSource(new BufferedInputStream(oriFile.toURI().toURL().openStream()));
		InputSource output = new InputSource(new BufferedInputStream(joinedFile.toURI().toURL().openStream()));
		
		XMLAssert.assertXMLEqual(original, output);
		
	}

}
