package net.sf.okapi.steps.xliffkit.codec;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestPackageHexCodec {

private PackageHexCodec codec = new PackageHexCodec();
	
	@Test
	public void testEncodeDecode() {
		String snippet = "text before \\b \t \b text after";
		assertEquals("text before \\b _#x0009; _#x0008; text after", 
				codec.encode(snippet));
		assertEquals(snippet, codec.decode(codec.encode(snippet)));
	}
}
