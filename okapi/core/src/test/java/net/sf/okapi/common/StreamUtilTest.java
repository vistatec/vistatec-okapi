package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class StreamUtilTest {
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

	private FileLocation location = FileLocation.fromClass(this.getClass());

	@Test
	public void testCRC() {
		assertEquals(1018854380L, StreamUtil.calcCRC(location.in("/ParamTest01.txt").asInputStream()));
		assertEquals(3995389683L, StreamUtil.calcCRC(location.in("/safeouttest1.txt").asInputStream()));
		assertEquals(369693688L, StreamUtil.calcCRC(location.in("/test_path1.txt").asInputStream()));
		assertEquals(681066369L, StreamUtil.calcCRC(location.in("/test.html").asInputStream()));
	}
	
	@Test
	public void streamAsString() throws Exception {
		File tmp = testFolder.newFile();
		assertTrue(tmp.exists());
		// bis is closed twice by try-with-resource and streamAsString, but this
		// shows best practice of using
		// BOMAwareInputStream
		try (BOMAwareInputStream bis = new BOMAwareInputStream(
				new FileInputStream(tmp), "UTF-8")) {
			StreamUtil.streamAsString(bis, bis.detectEncoding());
		}
	}

	@Test
	public void testCopyStreamToFile() throws Exception {
		String data = "123456790";
		File temp = testFolder.newFile();
		try (InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))) {
			StreamUtil.copy(is, temp);
			try (InputStream output = new FileInputStream(temp)) {
				assertEquals(data, StreamUtil.streamUtf8AsString(output));
			}
		}
	}

	@Test
	public void testCopyStreamToPath() throws Exception {
		String data = "123456790";
		File temp = testFolder.newFile();
		try (InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8))) {
			StreamUtil.copy(is, temp.getAbsolutePath());
			try (InputStream output = new FileInputStream(temp)) {
				assertEquals(data, StreamUtil.streamUtf8AsString(output));
			}
		}
	}

	@Test
	public void testCopyFileToFile() throws Exception {
		File temp = testFolder.newFile();
		StreamUtil.copy(location.in("/ParamTest01.txt").asFile(), temp);
		new FileCompare().filesExactlyTheSame(temp.toURI(), location.in("/ParamTest01.txt").asUri());
	}

	@Test
	public void testCopyFileToOutputStream() throws Exception {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			StreamUtil.copy(location.in("/ParamTest01.txt").asFile(), os);
			String out = new String(os.toByteArray(), StandardCharsets.UTF_8);
			String gold = StreamUtil.streamUtf8AsString(location.in("/ParamTest01.txt").asInputStream());
			assertEquals(gold, out);
		}
	}
}
