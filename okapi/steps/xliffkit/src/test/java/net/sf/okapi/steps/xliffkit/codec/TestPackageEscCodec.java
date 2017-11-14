package net.sf.okapi.steps.xliffkit.codec;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestPackageEscCodec {

	private PackageEscCodec codec = new PackageEscCodec();
	
	@Test
	public void testEncode() {
		// \b
		String snippet = "text before \\b \t \b text after";
		String res = codec.encode(snippet);
		assertEquals("text before \\b \uFFF0\\t \uFFF0\\b text after", res);
	}
	
	@Test
	public void testEncode2() {
		// \\b
		String snippet = "text before \\\\b \t \b text after";
		String res = codec.encode(snippet);
		assertEquals("text before \\\\b \uFFF0\\t \uFFF0\\b text after", res);
	}
	
	@Test
	public void testEncode3() {
		// \BS
		String snippet = "text before \\\b \t \b text after";
		String res = codec.encode(snippet);
		assertEquals("text before \\\uFFF0\\b \uFFF0\\t \uFFF0\\b text after", res); 
	}
	
	@Test
	public void testEncode4() {
		// \\BS
		String snippet = "text before \\\\\b \t \b text after";
		String res = codec.encode(snippet);
		assertEquals("text before \\\\\uFFF0\\b \uFFF0\\t \uFFF0\\b text after", res); 
	}
	
	@Test
	public void testEncode5() {
		// ^A
		String snippet = "text before ^A \t \u0001 text after";
		String res = codec.encode(snippet);
		assertEquals("text before ^A \uFFF0\\t \uFFF0^A text after", res);
	}
	
	@Test
	public void testEncode6() {
		// ^^A
		String snippet = "text before ^^A \t \u0001 text after";
		String res = codec.encode(snippet);
		assertEquals("text before ^^A \uFFF0\\t \uFFF0^A text after", res);
	}
	
	@Test
	public void testEncode7() {
		// \SOH
		String snippet = "text before \\\u0001 \t \u0001 text after";
		String res = codec.encode(snippet);
		assertEquals("text before \\\uFFF0^A \uFFF0\\t \uFFF0^A text after", res); 
	}
	
	@Test
	public void testEncode8() {
		// \\SOH
		String snippet = "text before \\\\\u0001 \t \u0001 text after";
		String res = codec.encode(snippet);
		assertEquals("text before \\\\\uFFF0^A \uFFF0\\t \uFFF0^A text after", res); 
	}
		
	@Test
	public void testEncodeDecode() {
		String snippet = "text before \\b \t \b text after";
		assertEquals(snippet, codec.decode(codec.encode(snippet)));
	}
	
	@Test
	public void testEncodeDecode2() {
		String snippet = "text before ^A \t \u0001 text after";
		assertEquals(snippet, codec.decode(codec.encode(snippet)));
	}
}
