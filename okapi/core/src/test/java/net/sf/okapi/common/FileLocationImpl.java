package net.sf.okapi.common;

import java.net.MalformedURLException;
import java.net.URL;

class FileLocationImpl {
	static URL makeUrlFromName(Class<?> clazz, String name, boolean out) {

		URL result = null;
		String tmpName = null == name ? "" : name;

		URL t;
		if (tmpName.startsWith("/")) {
			t = clazz.getResource("/");
			tmpName = tmpName.substring(1);
		} else {
			t = clazz.getResource("");
		}

		try {
			String filePart = t.getFile() + tmpName;
			if (out)
				filePart = filePart.replace("/target/test-classes/", "/target/test-classes/out/");
			/*
			 * Patchy way to deal with spaces...
			 * TODO: find a good a way to "juggle" between URI/URL and make this work with anything.
			 *   "The URL class does not itself encode or decode any URL components according
			 *   to the escaping mechanism defined in RFC2396. It is the responsibility of
			 *   the caller to encode any fields, which need to be escaped prior to calling URL,
			 *   and also to decode any escaped fields, that are returned from URL."
			 *   http://docs.oracle.com/javase/8/docs/api/java/net/URL.html
			 */
			filePart = filePart.replaceAll(" ", "%20");
			result = new URL(t.getProtocol(), t.getHost(), t.getPort(), filePart);
		} catch (MalformedURLException e) {
		}

		return result;
	}
}
