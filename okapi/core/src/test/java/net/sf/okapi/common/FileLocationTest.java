package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FileLocationTest {

	@Test
	public void getParentDir_ValidFile() {
		assertTrue("Incorrect path returned",
				FileLocation.fromClass(this.getClass())
				.in(FileLocation.ROOT_FOLDER)
				.asUrl().toString().endsWith("/target/test-classes/"));
	}

	@Test
	public void testInputAccess() {
		FileLocation location = FileLocation.fromClass(this.getClass())
				.in(FileLocation.ROOT_FOLDER);
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("/okapi/core/target/test-classes/"));

		location = FileLocation.fromClass(this.getClass())
				.in(FileLocation.CLASS_FOLDER);
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("okapi/core/target/test-classes/net/sf/okapi/common/"));

		location = FileLocation.fromClass(this.getClass())
				.in("/test.txt");
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("okapi/core/target/test-classes/test.txt"));

		location = FileLocation.fromClass(this.getClass())
				.in("test.txt");
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("okapi/core/target/test-classes/net/sf/okapi/common/test.txt"));
	}

	@Test
	public void testOutputAccess() {
		FileLocation location = FileLocation.fromClass(this.getClass())
				.out(FileLocation.ROOT_FOLDER);
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("/okapi/core/target/test-classes/out/"));

		location = FileLocation.fromClass(this.getClass())
				.out(FileLocation.CLASS_FOLDER);
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("okapi/core/target/test-classes/out/net/sf/okapi/common/"));

		location = FileLocation.fromClass(this.getClass())
				.out("/test.txt");
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("okapi/core/target/test-classes/out/test.txt"));

		location = FileLocation.fromClass(this.getClass())
				.out("test.txt");
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("okapi/core/target/test-classes/out/net/sf/okapi/common/test.txt"));
	}

	@Test
	public void testReuse() {
		FileLocation location = FileLocation.fromClass(this.getClass())
				.in(FileLocation.ROOT_FOLDER);
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("/okapi/core/target/test-classes/"));

		location.out(FileLocation.CLASS_FOLDER);
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("okapi/core/target/test-classes/out/net/sf/okapi/common/"));

		location.in("/test.txt");
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("okapi/core/target/test-classes/test.txt"));

		location.out("test.txt");
		assertTrue("Incorrect path returned",
				location.asUrl().toString()
				.endsWith("okapi/core/target/test-classes/out/net/sf/okapi/common/test.txt"));
	}

	@Test
	public void testVariousOutputTypes() {
		final String EXPECTEDU_END = "okapi/core/target/test-classes/TestUtilTestTestFile.txt";
		final String EXPECTED_END = EXPECTEDU_END.replace('/', File.separatorChar);
		FileLocation location = FileLocation.fromClass(this.getClass())
				.in("/TestUtilTestTestFile.txt");

		assertTrue("Incorrect file returned",
				location.asFile().getPath()
				.endsWith(EXPECTED_END));

		assertTrue("Incorrect path returned",
				location.asPath().toString()
				.endsWith(EXPECTED_END));

		String tmp = location.asUrl().toString();
		assertTrue("Incorrect url returned",
				tmp.endsWith(EXPECTEDU_END));
		assertTrue("Incorrect url returned",
				tmp.startsWith("file:/"));

		tmp = location.asUri().toString();
		assertTrue("Incorrect uri returned",
				tmp.endsWith(EXPECTEDU_END));
		assertTrue("Incorrect uri returned",
				tmp.startsWith("file:/"));

		assertTrue("Incorrect string returned",
				location.toString()
				.endsWith(EXPECTED_END));

		try (InputStream in = location.asInputStream()) {
			assertEquals(34, in.available());
			byte [] buffer = new byte[in.available()];
			in.read(buffer, 0, buffer.length);
			assertEquals("This file is for the TestUtil Yay!", new String(buffer, "utf-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testOutAndIn() throws FileNotFoundException, IOException {
		final byte [] buffer = { 0x12, 0x34, 0x56, 0x78 };

		FileLocation location = FileLocation.fromClass(this.getClass()).out("/OutAndIn.txt");

		try (OutputStream os = location.asOutputStream()) {
			os.write(buffer);
		}

		location.in("/out/OutAndIn.txt");
		try (InputStream in = location.asInputStream()) {
			assertEquals(buffer.length, in.available());
			byte [] bufferIn = new byte[buffer.length];
			in.read(bufferIn, 0, bufferIn.length);
			assertEquals(Arrays.toString(buffer), Arrays.toString(bufferIn));
		}
	}
}
