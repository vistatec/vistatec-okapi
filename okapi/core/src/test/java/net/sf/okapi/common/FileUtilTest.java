package net.sf.okapi.common;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class FileUtilTest {

	@Test
	public void testXliff2GuessLanguagesFromFile() {
		List<String> guessed = FileUtil.guessLanguages(
				FileLocation.fromClass(getClass()).in("en-fr.xlf").toString());
		assertEquals("en-us", guessed.get(0));
		assertEquals("fr", guessed.get(1));
	}

	@Test
	public void testGuessLanguagesFromFile() {
		List<String> guessed = FileUtil.guessLanguages(
				FileLocation.fromClass(getClass()).in("hello.xlf").toString());
		assertEquals("en", guessed.get(0));
		assertEquals("fr", guessed.get(1));
	}
	
	@Test
	public void testGuessLanguagesFromReader() {
		InputStream is = FileLocation.fromClass(getClass()).in("hello.xlf").asInputStream();
		List<String> guessed = FileUtil.guessLanguages(new InputStreamReader(is, StandardCharsets.UTF_8));
		assertEquals("en", guessed.get(0));
		assertEquals("fr", guessed.get(1));
	}

	@Test
	public void testIsXliffSegmentedFromFile() {
		assertFalse(FileUtil.isXliffSegmented(
				FileLocation.fromClass(getClass()).in("hello.xlf").toString()));
	}

	@Test
	public void testIsXliffSegmentedFromReader() {
		InputStream is = FileLocation.fromClass(getClass()).in("hello.xlf").asInputStream();
		assertFalse(FileUtil.isXliffSegmented(new InputStreamReader(is, StandardCharsets.UTF_8)));
	}
}
