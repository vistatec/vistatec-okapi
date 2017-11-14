package net.sf.okapi.common.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import net.sf.okapi.common.FileLocation;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiIOException;

@RunWith(JUnit4.class)
public class RawDocumentTest {

	LocaleId locEN = LocaleId.fromString("en");
	
	@Test
	public void getReaderWithURI() throws URISyntaxException, IOException {
		URI uri = FileLocation.fromClass(RawDocumentTest.class).in("/test.html").asUri();
		RawDocument ir = new RawDocument(uri, "UTF-8", locEN);
		Reader r = ir.getReader();
		assertTrue(r.ready());
		ir.close();
	}

	@Test(expected = OkapiIOException.class)
	public void getReaderWithExceptionUriNotFound() throws URISyntaxException {
		RawDocument ir = new RawDocument(new URI("file:///home/username/bad.bad"), "UTF-8", locEN);
		ir.getReader();
		ir.close();
	}
	
	@Test(expected = OkapiIOException.class)
	public void getReaderWithExceptionIllegalUri() throws URISyntaxException {
		RawDocument ir = new RawDocument(new URI("/bad/bad"), "UTF-8", locEN);
		ir.getReader();
		ir.close();
	}

	@Test
	public void testFromStream ()
		throws URISyntaxException, FileNotFoundException
	{
		File expectedFile = FileLocation.fromClass(this.getClass()).out("/safeouttest1.out.txt").asFile();
		expectedFile.delete();

		InputStream is = FileLocation.fromClass(this.getClass()).in("/safeouttest1.txt").asInputStream();
		RawDocument rd = new RawDocument(is, "UTF-8", LocaleId.ENGLISH);
		writeStringAndFinalize(expectedFile.toURI(), rd, "output text 1");
		
		assertTrue(expectedFile.exists());
		assertEquals(13, expectedFile.length());
	}

	@Test
	public void testOutputIsDifferentFromInput ()
		throws URISyntaxException, FileNotFoundException
	{
		File expectedFile = FileLocation.fromClass(this.getClass()).out("/safeouttest1.out.txt").asFile();
		expectedFile.delete();
		
		InputStream is = FileLocation.fromClass(this.getClass()).in("/safeouttest1.txt").asInputStream();
		RawDocument rd = new RawDocument(is, "UTF-8", LocaleId.ENGLISH);
		writeStringAndFinalize(expectedFile.toURI(), rd, "test1");
		
		assertTrue(expectedFile.exists());
		assertEquals(5, expectedFile.length());
	}

	@Test
	public void testOutputIsSameAsInput ()
		throws URISyntaxException, FileNotFoundException
	{
		File expectedFile = FileLocation.fromClass(this.getClass()).out("/safeouttest1.out.txt").asFile();
		expectedFile.delete();
		
		InputStream is = FileLocation.fromClass(this.getClass()).in("/safeouttest1.txt").asInputStream();
		RawDocument rd = new RawDocument(is, "UTF-8", LocaleId.ENGLISH);
		writeStringAndFinalize(expectedFile.toURI(), rd, "test1+");
		
		assertTrue(expectedFile.exists());
		assertEquals(6, expectedFile.length());
	}

	@Test
	public void testTargetLocaleWithConstructors () {
		RawDocument rd = new RawDocument("abc", LocaleId.ENGLISH, LocaleId.RUSSIAN);
		assertEquals(LocaleId.RUSSIAN, rd.getTargetLocale());
		assertEquals(1, rd.getTargetLocales().size());
		assertEquals(LocaleId.RUSSIAN, rd.getTargetLocales().get(0));
		
		rd = new RawDocument(new File("test").toURI(), "UTF-8", LocaleId.ENGLISH, LocaleId.FRENCH);
		assertEquals(LocaleId.FRENCH, rd.getTargetLocale());
		assertEquals(1, rd.getTargetLocales().size());
		assertEquals(LocaleId.FRENCH, rd.getTargetLocales().get(0));

		rd = new RawDocument(new ByteArrayInputStream(new byte[1]), "UTF-8", LocaleId.ENGLISH, LocaleId.SPANISH);
		assertEquals(LocaleId.SPANISH, rd.getTargetLocale());
		assertEquals(1, rd.getTargetLocales().size());
		assertEquals(LocaleId.SPANISH, rd.getTargetLocales().get(0));
		
		rd = new RawDocument(new File("test").toURI(), "UTF-8", LocaleId.ENGLISH, LocaleId.ITALIAN, "okf_xml");
		assertEquals(LocaleId.ITALIAN, rd.getTargetLocale());
		assertEquals(1, rd.getTargetLocales().size());
		assertEquals(LocaleId.ITALIAN, rd.getTargetLocales().get(0));
	}

	@Test
	public void testTargetLocaleWithSetters () {
		RawDocument rd = new RawDocument("abc", LocaleId.ENGLISH);
		rd.setTargetLocale(LocaleId.RUSSIAN);
		assertEquals(LocaleId.RUSSIAN, rd.getTargetLocale());
		assertEquals(1, rd.getTargetLocales().size());
		assertEquals(LocaleId.RUSSIAN, rd.getTargetLocales().get(0));
		rd.close();

		rd = new RawDocument("abc", LocaleId.ENGLISH);
		ArrayList<LocaleId> list = new ArrayList<LocaleId>();
		list.add(LocaleId.FRENCH);
		rd.setTargetLocales(list);
		assertEquals(LocaleId.FRENCH, rd.getTargetLocale());
		assertEquals(1, rd.getTargetLocales().size());
		assertEquals(LocaleId.FRENCH, rd.getTargetLocales().get(0));
		rd.close();
	}

	@Test
	public void testTargetLocaleOverrides () {
		RawDocument rd = new RawDocument("abc", LocaleId.ENGLISH);
		rd.setTargetLocale(LocaleId.RUSSIAN);
		assertEquals(LocaleId.RUSSIAN, rd.getTargetLocale());
		assertEquals(1, rd.getTargetLocales().size());
		assertEquals(LocaleId.RUSSIAN, rd.getTargetLocales().get(0));

		ArrayList<LocaleId> list = new ArrayList<LocaleId>();
		list.add(LocaleId.FRENCH);
		list.add(LocaleId.JAPANESE);
		rd.setTargetLocales(list);
		assertEquals(LocaleId.FRENCH, rd.getTargetLocale());
		assertEquals(2, rd.getTargetLocales().size());
		assertEquals(LocaleId.FRENCH, rd.getTargetLocales().get(0));
		
		rd.setTargetLocale(LocaleId.ITALIAN);
		assertEquals(LocaleId.ITALIAN, rd.getTargetLocale());
		assertEquals(1, rd.getTargetLocales().size());
		assertEquals(LocaleId.ITALIAN, rd.getTargetLocales().get(0));
		rd.close();
	}

	private void writeStringAndFinalize (URI outputURI,
		RawDocument rd,
		String text)
		throws FileNotFoundException
	{
		File file = rd.createOutputFile(outputURI);
		PrintWriter writer = new PrintWriter(file);
		writer.write(text);
		writer.close();
		rd.finalizeOutput();
	}
	
}
