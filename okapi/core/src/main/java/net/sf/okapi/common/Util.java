/*===========================================================================
  Copyright (C) 2008-2017 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
============================================================================*/

package net.sf.okapi.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPathFactory;

import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;

import org.w3c.dom.Node;

/**
 * Collection of various all-purpose helper functions.
 */
public final class Util {

	/**
	 * Name of the root directory variable. 
	 */
	public static final String ROOT_DIRECTORY_VAR = "${rootDir}";
	
	/**
	 * Name of the input root directory variable.
	 */
	public static final String INPUT_ROOT_DIRECTORY_VAR = "${inputRootDir}";

	/**
	 * Enumeration of supported OSes	 	 
	 */
	public static enum SUPPORTED_OS {
		WINDOWS,
		MAC,
		LINUX		
	}

	/**
	 * Line-break string for DOS/Windows.
	 */
	public static final String LINEBREAK_DOS = "\r\n";
	/**
	 * Line-break string for Unix/Linux
	 */
	public static final String LINEBREAK_UNIX = "\n";
	/**
	 * Line-break string for Macintosh
	 */
	public static final String LINEBREAK_MAC = "\r";

	/**
	 * Default RTF style for starting an external code.
	 */
	public static final String RTF_STARTCODE = "{\\cs5\\f1\\cf15\\lang1024 ";
	/**
	 * Default RTF style for ending an external code.
	 */
	public static final String RTF_ENDCODE = "}";
	/**
	 * Default RTF style for starting an internal code.
	 */
	public static final String RTF_STARTINLINE = "{\\cs6\\f1\\cf6\\lang1024 ";
	/**
	 * Default RTF style for ending an internal code.
	 */
	public static final String RTF_ENDINLINE = "}";
	/**
	 * Default RTF style for starting an in-between source/target marker.
	 */
	public static final String RTF_STARTMARKER = "{\\cs15\\v\\cf12\\sub\\f2 \\{0>}{\\v\\f1 ";
	/**
	 * Default RTF style for the first half of a middle part of an in-between source/target marker.
	 */
	public static final String RTF_MIDMARKER1 = "}{\\cs15\\v\\cf12\\sub\\f2 <\\}";
	/**
	 * Default RTF style for the second half of a middle part of an in-between source/target marker.
	 */
	public static final String RTF_MIDMARKER2 = "\\{>}";
	/**
	 * Default RTF style for ending an in-between source/target marker.
	 */
	public static final String RTF_ENDMARKER = "{\\cs15\\v\\cf12\\sub\\f2 <0\\}}";

	private static final String NEWLINES_REGEX = "\r(\n)?";
	private static final Pattern NEWLINES_REGEX_PATTERN = Pattern.compile(NEWLINES_REGEX);
	/**
	 * Any variable of the type ${varname}.
	 */
	private static final String VARIABLE_REGEX = "\\$\\{([^\\}]+)\\}";
	private static final Pattern VARIABLE_REGEX_PATTERN = Pattern.compile(VARIABLE_REGEX);
	/**
	 * Shared flag indicating a translation that was generated using machine translation.
	 */
	public static final String MTFLAG = "MT!";
	

	// Used by openURL()
	private static final String[] browsers = { "firefox", "opera", "konqueror", "epiphany",
		"seamonkey", "galeon", "kazehakase", "mozilla", "netscape" };

	/**
	 * Converts all \r\n and \r to linefeed (\n)
	 * @param text 
	 *      the text to convert
	 * @return converted string
	 */
	static public String normalizeNewlines(String text) {
		return NEWLINES_REGEX_PATTERN.matcher(text).replaceAll("\n");
	}

	/**
	 * Removes from the start of a string any of the specified characters.
	 * 
	 * @param text
	 *            string to trim.
	 * @param chars
	 *            list of the characters to trim.
	 * @return The trimmed string.
	 */
	static public String trimStart (String text,
		String chars)
	{
		if ( text == null ) return text;
		int n = 0;
		while ( n < text.length() ) {
			if ( chars.indexOf(text.charAt(n)) == -1 ) {
				break;
			}
			n++;
		}
		if ( n >= text.length() ) return "";
		if ( n > 0 ) return text.substring(n);
		return text;
	}

	/**
	 * Removes from the end of a string any of the specified characters.
	 * @param text
	 *            string to trim.
	 * @param chars
	 *            list of the characters to trim.
	 * @return the trimmed string.
	 */
	static public String trimEnd (String text,
		String chars)
	{
		if ( text == null ) return text;
		int n = text.length() - 1;
		while ( n >= 0 ) {
			if (chars.indexOf(text.charAt(n)) == -1)
				break;
			n--;
		}
		if ( n < 0 ) return "";
		if ( n > 0 ) return text.substring(0, n + 1);
		return text;
	}

	/**
	 * Gets the directory name of a full path.
	 * 
	 * @param path
	 *         full path from where to extract the directory name. The path
	 *         can be a URL path (e.g. "/C:/test/file.ext").
	 * @return The directory name (without the final separator), or an empty
	 *         string if path is a filename.
	 */
	static public String getDirectoryName (String path) {
		String tmp = path.replace('\\', '/'); // Normalize separators (some path are mixed)
		int n = tmp.lastIndexOf('/');
		if ( n > 0 ) {
			return path.substring(0, n);
		}
		else {
			return "";
		}
	}
	
	/**
	 * Determines if a given path ends with a file name separator for the current platform.
	 * If not, the file separator is appended to the path.
	 * @param path the given path.
	 * @param forceForwardSlash true if the ending separator must be a forward slash.
	 * @return the given path ending with the file name separator.
	 */
	static public String ensureSeparator (String path,
		boolean forceForwardSlash)
	{
		if ( isEmpty(path) ) return path;
		if ( path.endsWith("/") ) return path;
		if ( path.endsWith(File.separator) ) {
			if ( forceForwardSlash ) {
				path = path.substring(0, path.length()-1);
			}
			else {
				return path;
			}
		}
		if ( forceForwardSlash ) {
			return path + "/";
		}
		else {
			return path + File.separator;
		}
	}
	
	/**
	 * Determines if a given path starts with a file name separator for the current platform.
	 * If not, the file separator is prefixed to the path.
	 * @param path the given path.
	 * @param forceForwardSlash true if the leading separator must be a forward slash.
	 * @return the given path starting with the file name separator.
	 */
	static public String ensureLeadingSeparator (String path,
			boolean forceForwardSlash)
		{
			if ( isEmpty(path) ) return path;
			if ( path.startsWith("/") ) return path;
			if ( path.startsWith(File.separator) ) {
				if ( forceForwardSlash ) {
					path = path.substring(1, path.length());
				}
				else {
					return path;
				}
			}
			if ( forceForwardSlash ) {
				return "/" + path;
			}
			else {
				return File.separator + path;
			}
		}
	
	/**
	 * Replaces unsupported characters in a given short file name (no directory path) with a given replacer.
	 * @param fileName the given short file name
	 * @param replacer the given replacer
	 * @return the given file name with fixed unsupported characters
	 */
	public static String fixFilename(String fileName, String replacer) {
		if (Util.isEmpty(fileName)) return "";
		if (Util.isEmpty(replacer)) return fileName;
		String regex = "[*:<>?\\\\/|]"; // TODO This regex is for Windows, add for Linux/Mac OS
		replacer = replacer.replaceAll(regex, "_"); // In case replacer contains unsupported chars
        return fileName.replaceAll(regex, replacer);
    }
	
	/**
	 * Removes exceeding separators in a given path. Normalizes the given path to contain OS-specific file separators.
	 * @param path the given path
	 * @return the fixed given path
	 */
	public static String fixPath(String path) {
		return fixPath(path, true);
	}
	
	/**
	 * Removes exceeding separators in a given path. Optionally normalizes the given path to contain OS-specific file separators.
	 * @param path the given path
	 * @param forceOsSeparators true to ensure the path contains only OS-specific separators
	 * @return the fixed given path
	 */
	public static String fixPath(String path, boolean forceOsSeparators) {
		final String FP_PREFIX = "file://";
		String res = path;
		boolean hasFileProtocol = res.startsWith(FP_PREFIX);
		if (hasFileProtocol) {
			res = res.substring(FP_PREFIX.length()); // Remove "file://" part not to replace "///" with "//"
		}
		
		res = res.replaceAll("[\\\\/]+", "/");
		
		if (!hasFileProtocol && forceOsSeparators) {
			res = res.replace("/", File.separator);
		}
		
//		if (res.startsWith("\\")) {
//			// Don't let a Windows path start with a back slash 
//			res = res.substring(1);
//		}
		
		return hasFileProtocol ? FP_PREFIX + res : res;
	}
	
	/**
	 * Replaces unsupported characters in a given short file name (no directory path) with underscore.
	 * @param fileName the given short file name
	 * @return the given file name with fixed unsupported characters
	 */
	public static String fixFilename(String fileName) {
		return fixFilename(fileName, "_");
	}	
	
	/**
	 * Builds a path from given parts. Path separators are normalized. 
	 * @param parts parts of the path
	 * @return path containing the given parts separated with a file name separator 
	 * for the current platform. 
	 */
	public static String buildPath(String... parts) {
		String res = null;
		for (final String part : parts) {
			String normalizedPart = part.replace("\\", "/"); // Normalize to slash
			
			if (res == null) {
				res = normalizedPart;
			}
			else {
				if (!res.endsWith("/")) {
					res += "/";
				}
				res += normalizedPart;
			}
		}
		return fixPath(res);
	}
	
	/**
	 * Return a list of files based on suffix (i.e, .xml, .html etc.)
	 * @param directory - directory where files are located
	 * @param suffix - the sufix used to filter files
	 * @return - list of files matching the suffix
	 * @throws URISyntaxException if the syntax is not correct.
	 */
	public static String[] getFilteredFiles(final String directory, final String suffix)
			throws URISyntaxException {
		File dir = new File(directory);
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(suffix);
			}
		};
		return dir.list(filter);
	}
	
	/**
	 * Creates the directory tree for the give full path (dir+filename)
	 * 
	 * @param path
	 *            directory to create and filename. If you want to pass only a directory
	 *            name make sure it has a trailing separator (e.g. "c:\project\tmp\").
	 *            The path can be a URL path (e.g. "/C:/test/file.ext").
	 * @return	true if one of more directories were created or if there was no directory to create,
	 *          false otherwise (an error occurred) 
	 */
	static public boolean createDirectories (String path) {
		String tmp = path.replace('\\', '/'); // Normalize separators (some path are mixed)
		int n = tmp.lastIndexOf('/');
		if ( n == -1 ) return true; // Nothing to do
		// Else, use the directory part and create the tree
		String dirPath = path.substring(0, n);
		File dir = new File(dirPath);
		if ( !dir.exists() ) return dir.mkdirs();
		else return true;
	}
	
	/**
	 * Escape newlines and whitespace so they survive roundtrip as an xml attribute
	 * \n=\u0098
	 * \r=\u0097
	 * \u0020=\u0096
	 * @param text - original text
	 * @return escaped string
	 */
	static public String escapeWhitespaceForXML (String text) {
		if ( text == null || text.isEmpty()) return "";
		StringBuffer sbTmp = new StringBuffer(text.length());
		char ch;
		for ( int i = 0; i < text.length(); i++ ) {
			ch = text.charAt(i);
			switch (ch) {
			case '\n':
				sbTmp.append('\u0098');
				continue;
			case '\r':
				sbTmp.append('\u0097');
				continue;
			case ' ':
				sbTmp.append('\u0096');
				continue;
			default:
				if ( text.charAt(i) > 127 ) { // Extended chars
					if ( Character.isHighSurrogate(ch) ) {
						int cp = text.codePointAt(i++);
						String tmp = new String(Character.toChars(cp));				
						sbTmp.append(tmp);				
					} else {
						sbTmp.append(text.charAt(i));
					}					
				} else { // ASCII chars
					sbTmp.append(text.charAt(i));
				}
				continue;
			}
		}		
		return sbTmp.toString();
	}

	static public String unescapeWhitespaceForXML (String text) {
		if ( text == null || text.isEmpty()) return "";
		StringBuffer sbTmp = new StringBuffer(text.length());
		char ch;
		for ( int i = 0; i < text.length(); i++ ) {
			ch = text.charAt(i);
			switch (ch) {
			case '\u0098':
				sbTmp.append('\n');
				continue;
			case '\u0097':
				sbTmp.append('\r');
				continue;
			case '\u0096':
				sbTmp.append(' ');
				continue;
			default:
				if ( text.charAt(i) > 127 ) { // Extended chars
					if ( Character.isHighSurrogate(ch) ) {
						int cp = text.codePointAt(i++);
						String tmp = new String(Character.toChars(cp));				
						sbTmp.append(tmp);				
					} else {
						sbTmp.append(text.charAt(i));
					}					
				} else { // ASCII chars
					sbTmp.append(text.charAt(i));
				}
				continue;
			}
		}		
		return sbTmp.toString();
	}

	/**
	 * Escapes a string for XML.
	 * 
	 * @param text
	 *            string to escape.
	 * @param quoteMode
	 *            0=no quote escaped, 1=apos and quot, 2=#39 and quot, and
	 *            3=quot only.
	 * @param escapeGT
	 *            true to always escape '&gt;' to gt
	 * @param encoder
	 *            the character set encoder to use to detect un-supported
	 *            character, or null to never escape normal characters.
	 * @return the escaped string.
	 */
	static public String escapeToXML (String text,
		int quoteMode,
		boolean escapeGT,
		CharsetEncoder encoder)
	{
		if ( text == null ) return "";
		StringBuffer sbTmp = new StringBuffer(text.length());
		char ch;
		for ( int i = 0; i < text.length(); i++ ) {
			ch = text.charAt(i);
			switch (ch) {
			case '<':
				sbTmp.append("&lt;");
				continue;
			case '>':
				if (escapeGT)
					sbTmp.append("&gt;");
				else {
					if (( i > 0 ) && ( text.charAt(i - 1) == ']' )) {
						sbTmp.append("&gt;");
					}
					else {
						sbTmp.append('>');
					}
				}
				continue;
			case '&':
				sbTmp.append("&amp;");
				continue;
			case '"':
				if ( quoteMode > 0 ) {
					sbTmp.append("&quot;");
				}
				else {
					sbTmp.append('"');
				}
				continue;
			case '\'':
				switch ( quoteMode ) {
				case 1:
					sbTmp.append("&apos;");
					break;
				case 2:
					sbTmp.append("&#39;");
					break;
				default:
					sbTmp.append(text.charAt(i));
					break;
				}
				continue;
			default:
				if ( text.charAt(i) > 127 ) { // Extended chars
					if ( Character.isHighSurrogate(ch) ) {
						int cp = text.codePointAt(i++);
						String tmp = new String(Character.toChars(cp));
						if (( encoder != null ) && !encoder.canEncode(tmp) ) {
							sbTmp.append(String.format("&#x%x;", cp));
						} else {
							sbTmp.append(tmp);
						}
					}
					else {
						if (( encoder != null ) && !encoder.canEncode(text.charAt(i)) ) {
							sbTmp.append(String.format("&#x%04x;", text.codePointAt(i)));
						}
						else { // No encoder or char is supported
							sbTmp.append(text.charAt(i));
						}
					}
				}
				else { // ASCII chars
					sbTmp.append(text.charAt(i));
				}
				continue;
			}
		}
		return sbTmp.toString();
	}

	/**
	 * Escapes a given string into RTF format.
	 * 
	 * @param text
	 *            the string to convert.
	 * @param convertLineBreaks
	 *            Indicates if the line-breaks should be converted.
	 * @param lineBreakStyle
	 *            Type of line-break conversion. 0=do nothing special, 1=close
	 *            then re-open as external, 2=close then re-open as internal.
	 * @param encoder
	 *            Encoder to use for the extended characters.
	 * @return The input string escaped to RTF.
	 */
	static public String escapeToRTF (String text,
		boolean convertLineBreaks,
		int lineBreakStyle,
		CharsetEncoder encoder)
	{
		try {
			if ( text == null ) return "";
			StringBuffer tmp = new StringBuffer(text.length());
			CharBuffer tmpBuf = CharBuffer.allocate(1);
			ByteBuffer encBuf;

			for ( int i=0; i<text.length(); i++ ) {
				switch ( text.charAt(i) ) {
				case '{':
				case '}':
				case '\\':
					tmp.append("\\").append(text.charAt(i));
					break;
				case '\r': // Skip
					break;
				case '\n':
					if ( convertLineBreaks ) {
						switch ( lineBreakStyle ) {
						case 1: // Outside external
							tmp.append(RTF_ENDCODE);
							tmp.append("\r\n\\par ");
							tmp.append(RTF_STARTCODE);
							continue;
						case 2:
							tmp.append(RTF_ENDINLINE);
							tmp.append("\r\n\\par ");
							tmp.append(RTF_STARTINLINE);
							continue;
						case 0: // Just convert
						default:
							tmp.append("\r\n\\par ");
							continue;
						}
					}
					else {
						tmp.append("\n");
					}
					break;
				case '\u00a0': // Non-breaking space
					tmp.append("\\~"); // No extra space (it's a control word)
					break;
				case '\t':
					tmp.append("\\tab ");
					break;
				case '\u2022':
					tmp.append("\\bullet ");
					break;
				case '\u2018':
					tmp.append("\\lquote ");
					break;
				case '\u2019':
					tmp.append("\\rquote ");
					break;
				case '\u201c':
					tmp.append("\\ldblquote ");
					break;
				case '\u201d':
					tmp.append("\\rdblquote ");
					break;
				case '\u2013':
					tmp.append("\\endash ");
					break;
				case '\u2014':
					tmp.append("\\emdash ");
					break;
				case '\u200d':
					tmp.append("\\zwj ");
					break;
				case '\u200c':
					tmp.append("\\zwnj ");
					break;
				case '\u200e':
					tmp.append("\\ltrmark ");
					break;
				case '\u200f':
					tmp.append("\\rtlmark ");
					break;

				default:
					if ( text.charAt(i) > 127 ) {
						if ( encoder.canEncode(text.charAt(i)) ) {
							tmpBuf.put(0, text.charAt(i));
							tmpBuf.position(0);
							encBuf = encoder.encode(tmpBuf);
							if ( encBuf.limit() > 1 ) {
								tmp.append(String.format("{\\uc%d", encBuf.limit()));
								tmp.append(String.format("\\u%d", (int) text.charAt(i)));
								for (int j = 0; j < encBuf.limit(); j++) {
									tmp.append(String.format("\\'%x", (encBuf.get(j) < 0 ? (0xFF ^ ~encBuf.get(j))
										: encBuf.get(j))));
								}
								tmp.append("}");
							}
							else {
								tmp.append(String.format("\\u%d", (int) text.charAt(i)));
								tmp.append(String.format("\\'%x", (encBuf.get(0) < 0 ? (0xFF ^ ~encBuf.get(0))
									: encBuf.get(0))));
							}
						}
						else { // Cannot encode in the RTF encoding, so use
							// Just Unicode
							tmp.append(String.format("\\u%d ?", (int) text.charAt(i)));
						}
					}
					else {
						tmp.append(text.charAt(i));
					}
					break;
				}
			}
			return tmp.toString();
		}
		catch ( CharacterCodingException e ) {
			throw new OkapiException(e);
		}
	}

	/**
	 * Recursive function to delete the content of a given directory (including
	 * all its sub-directories. This does not delete the original parent
	 * directory.
	 * @param directory the directory of the content to delete.
	 * @return true if the content was deleted, false otherwise.
	 */
	public static boolean deleteDirectory (File directory) {
		boolean res = true;
		File[] list = directory.listFiles();
		if ( list == null ) return true;
		for ( File f : list ) {
			if ( f.isDirectory() ) {
				deleteDirectory(f);
			}
			// Set the result, but keep deleting
			if ( !f.delete() ) res = false;
		}
		return res;
	}

	/**
	 * Deletes the content of a given directory, and if requested, the directory
	 * itself. Sub-directories and their content are part of the deleted
	 * content.
	 * 
	 * @param directory
	 *            the path of the directory to delete
	 * @param contentOnly
	 *            indicates if the directory itself should be removed. If this
	 *            flag is true, only the content is deleted.
	 */
	public static void deleteDirectory (String directory,
		boolean contentOnly)
	{
		File f = new File(directory);
		// Make sure this is a directory
		if ( !f.isDirectory() ) {
			return;
		}
		deleteDirectory(f);
		if ( !contentOnly ) {
			f.delete();
		}
	}

	/**
	 * Gets the filename of a path.
	 * 
	 * @param path
	 *            the path from where to get the filename. The path can be a URL
	 *            path (e.g. "/C:/test/file.ext").
	 * @param keepExtension
	 *            true to keep the existing extension, false to remove it.
	 * @return the filename with or without extension.
	 */
	static public String getFilename (String path,
		boolean keepExtension)
	{
		// Get the filename (allow path with mixed separators)
		int n = path.lastIndexOf('/');
		int n2 = path.lastIndexOf('\\');
		if ( n2 > n ) n = n2;
		if ( n == -1 ) { // Then try Windows
			n = path.lastIndexOf('\\');
		}
		if ( n > -1 ) {
			path = path.substring(n + 1);
		}
		// Stop here if we keep the extension
		if ( keepExtension ) {
			return path;
		}
		// Else: remove the extension if there is one
		n = path.lastIndexOf('.');
		if ( n > -1 ) {
			return path.substring(0, n);
		}
		// Else:
		return path;
	}

	/**
	 * Gets the extension of a given path or filename.
	 * 
	 * @param path
	 *            the original path or filename.
	 * @return the last extension of the filename (including the period), or
	 *         empty if there is no period in the filename. If the filename ends
	 *         with a period, the return is a period.
	 *         Never returns null.
	 */
	static public String getExtension (String path) {
		// Get the extension
		int n = path.lastIndexOf('.');
		if (n == -1) return ""; // Empty
		return path.substring(n);
	}

	/**
	 * Makes a URI string from a path. If the path itself can be recognized as a
	 * string URI already, it is passed unchanged. For example "C:\test" and
	 * "file:///C:/test" will both return "file:///C:/test" encoded as URI.
	 * 
	 * @param pathOrUri
	 *            the path to change to URI string.
	 * @return the URI string.
	 * @throws OkapiUnsupportedEncodingException if UTF-8 is not supported (can't happen).
	 */
	static public String makeURIFromPath (String pathOrUri) {
		if (isEmpty(pathOrUri)) {
			throw new IllegalArgumentException();
		}
		// This should catch most of the URI forms
		pathOrUri = pathOrUri.replace('\\', '/');
		if (pathOrUri.indexOf("://") != -1)
			return pathOrUri;
		// If not that, then assume it's a file
		if (pathOrUri.startsWith("file:/"))
			pathOrUri = pathOrUri.substring(6);
		if (pathOrUri.startsWith("/"))
			pathOrUri = pathOrUri.substring(1);
		String tmp = URLEncodeUTF8(pathOrUri);
		// Use '%20' instead of '+': '+ not working with File(uri) it seems
		return "file:///" + tmp.replace("+", "%20");
	}
	
	/**
	 * Creates a new URI object from a path or a URI string.
	 * 
	 * @param pathOrUri
	 *            the path or URI string to use.
	 * @return the new URI object for the given path or URI string.
	 */
	static public URI toURI (String pathOrUri) {
		try {
			// Satisfy unit test for empty path.
			if ( pathOrUri == null || pathOrUri.equals("") ) {
				return new URI("");
			}
			URI uri = new URI(pathOrUri);
			if ( uri != null && uri.isAbsolute() ) {
				return uri;
			}
		}
		catch ( URISyntaxException e ) {
			// If that didn't work, try going through File.
		}
		return new File(pathOrUri).toURI();
	}


	/**
	 * Creates a new URL object from a path or a URI string.  This is a
	 * convenience wrapper to catch the various conversion exceptions.
	 * 
	 * @param pathOrUri
	 *            the path or URI string to use.
	 * @return the new URI object for the given path or URI string.
	 */
	static public URL toURL(String pathOrUri) {
		return URItoURL(toURI(pathOrUri));
	}

	/**
	 * Convert a URL to a URI. Convenience method to avoid catching
	 * {@link URISyntaxException} all over the place.
	 * @param url The URL to convert
	 * @return The new URI object for the given URL
	 */
	static public URI URLtoURI (URL url) {
		try {
			return url.toURI();
		} catch (URISyntaxException e) {
			throw new OkapiException(e);
		}
	}

	/**
	 * Convert a URI to a URL.  Convenience method to avoid catching
	 * {@link MalformedURLException}.
	 * @param uri to convert
	 * @return the resulting url
	 */
	static public URL URItoURL(URI uri) {
		try {
			return uri.toURL();
		} catch (MalformedURLException e) {
			throw new OkapiException(e);
		}
	}

	/**
	 * Gets the longest common path between an existing current directory and a
	 * new one.
	 * 
	 * @param currentDir
	 *            the current longest common path.
	 * @param newDir
	 *            the new directory to compare with.
	 * @param ignoreCase
	 *            true if the method should ignore cases differences.
	 * @return the longest sub-directory that is common to both directories.
	 *         This can be a null if the current directory is null,
	 *         or empty if there is no common path.
	 */
	static public String longestCommonDir (String currentDir,
		String newDir,
		boolean ignoreCase)
	{
		if ( currentDir == null ) {
			return newDir;
		}
		if ( currentDir.length() == 0 ) {
			return currentDir;
		}

		// Get temporary copies
		String currentLow = currentDir;
		String newLow = newDir;
		if ( ignoreCase ) {
			currentLow = currentDir.toLowerCase();
			newLow = newDir.toLowerCase();
		}

		// The new path equals, or include the existing root: no change
		if ( newLow.indexOf(currentLow) == 0 ) {
			return currentDir;
		}

		// Search the common path
		String tmp = currentLow;
		int i = 0;
		while ( newLow.indexOf(tmp) != 0 ) {
			tmp = Util.getDirectoryName(tmp);
			i++;
			if ( tmp.length() == 0 ) {
				return ""; // No common path at all
			}
		}

		// Do not return currentDir.substring(0, tmp.length());
		// because the lower-case string maybe of a different length than cased one
		// (e.g. German Sz). Instead re-do the splitting as many time as needed.
		tmp = currentDir;
		for ( int j = 0; j < i; j++ ) {
			tmp = Util.getDirectoryName(tmp);
		}
		return tmp;
	}
	
	/**
	 * Gets the longest common path between directories on a given list.
	 * 
	 * @param ignoreCase
	 *            if the method should ignore cases differences.
	 * @param directories
	 *            the given list of directories.
	 * @return the longest sub-directory that is common to all directories.
	 *         This can be a null or empty string.
	 */
	static public String longestCommonDir (boolean ignoreCase, String... directories) {
		if (directories == null) return "";
		if (directories.length == 1) return directories[0]; // Can be null
		
		String res = directories[0];
		for (int i = 1; i < directories.length; i++) {
			res = longestCommonDir(res, directories[i], ignoreCase);
		}
		return res;
	}

	/**
	 * Indicates if the current OS is case-sensitive.
	 * 
	 * @return true if the current OS is case-sensitive, false if otherwise.
	 */
	static public boolean isOSCaseSensitive () {
		// May not work on all platforms,
		// But should on basic Windows, Mac and Linux
		// (Use Windows file separator-type to guess the OS)
		return !File.separator.equals("\\");
	}

	/**
	 * Writes a Byte-Order-Mark if the encoding indicates it is needed. This
	 * methods must be the first call after opening the writer.
	 * 
	 * @param writer
	 *            writer where to output the BOM.
	 * @param bomOnUTF8
	 *            indicates if we should use a BOM on UTF-8 files.
	 * @param encoding
	 *            encoding of the output.
	 * @throws OkapiIOException if anything went wrong with the writing.
	 */
	static public void writeBOMIfNeeded (Writer writer,
		boolean bomOnUTF8,
		String encoding)
	{
		try {
			String tmp = encoding.toLowerCase();

			// Check UTF-8 first (most cases)
			if ((bomOnUTF8) && (tmp.equalsIgnoreCase("utf-8"))) {
				writer.write("\ufeff");
				return;
			}

			/*
			 * It seems writers do the following: For "UTF-16" they output
			 * UTF-16BE with a BOM. For "UTF-16LE" they output UTF-16LE without
			 * BOM. For "UTF-16BE" they output UTF-16BE without BOM. So we force a
			 * BOM for UTF-16LE and UTF-16BE
			 */
			if (tmp.equals("utf-16be") || tmp.equals("utf-16le")) {
				writer.write("\ufeff");
				return;
			}
			// TODO: Is this an issue? Does *reading* UTF-16LE/BE does not check
			// for BOM?
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	/**
	 * Gets the default system temporary directory to use for the current user.
	 * The directory path returned has never a trailing separator.
	 * 
	 * @return The directory path of the temporary directory to use, without
	 *         trailing separator.
	 */
	public static String getTempDirectory () {
		String tmp = System.getProperty("java.io.tmpdir");
		// Normalize for all platforms: no trailing separator
		if (tmp.endsWith(File.separator)) // This separator is always
			// platform-specific
			tmp = tmp.substring(0, tmp.length() - 1);
		return tmp;
	}

	/**
	 * Gets the text content of the first TEXT child of an element node. This is
	 * to use instead of node.getTextContent() which does not work with some
	 * Macintosh Java VMs. Note this work-around get <b>only the first TEXT
	 * node</b>.
	 * 
	 * @param node
	 *            the container element.
	 * @return the text of the first TEXT child node.
	 */
	public static String getTextContent (Node node) {
		Node tmp = node.getFirstChild();
		while (true) {
			if (tmp == null)
				return "";
			if (tmp.getNodeType() == Node.TEXT_NODE) {
				return tmp.getNodeValue();
			}
			tmp = tmp.getNextSibling();
		}
	}

	/**
	 * Calculates safely a percentage. If the total is 0, the methods return 1.
	 * 
	 * @param part
	 *            the part of the total.
	 * @param total
	 *            the total.
	 * @return the percentage of part in total.
	 */
	public static int getPercentage (int part,
		int total)
	{
		return (total == 0 ? 1 : Math.round((float) part / (float) total * 100));
	}

	/**
	 * Creates a string Identifier based on the hash code of the given text.
	 * 
	 * @param text
	 *		the text to make an ID for.
	 * @return The string identifier for the given text.
	 */
	public static String makeId (String text) {
		int n = text.hashCode();
		return String.format("%s%X", ((n > 0) ? 'P' : 'N'), n);
	}

	/**
	 * Indicates if two language codes are 'the same'. The comparison ignores
	 * case differences, and if the parameter ignoreRegion is true, any part
	 * after the first '-' is also ignored. Note that the character '_' is
	 * treated like a character '-'.
	 * 
	 * @param lang1
	 *            first language code to compare.
	 * @param lang2
	 *            second language code to compare.
	 * @param ignoreRegion
	 *            True to ignore any part after the first separator, false to
	 *            take it into account.
	 * @return true if, according the given options, the two language codes are
	 *         the same. False otherwise.
	 */
	static public boolean isSameLanguage (String lang1,
		String lang2,
		boolean ignoreRegion)
	{
		lang1 = lang1.replace('_', '-');
		lang2 = lang2.replace('_', '-');
		if (ignoreRegion) { // Do not take the region part into account
			int n = lang1.indexOf('-');
			if (n > -1) {
				lang1 = lang1.substring(0, n);
			}
			n = lang2.indexOf('-');
			if (n > -1) {
				lang2 = lang2.substring(0, n);
			}
		}
		return lang1.equalsIgnoreCase(lang2);
	}

	/**
	 * Indicates if a given string is null or empty.
	 * 
	 * @param string
	 *            the string to check.
	 * @return true if the given string is null or empty.
	 */
	static public boolean isEmpty (String string) {
		return (( string == null ) || ( string.length() == 0 ));
	}

	/**
	 * Indicates if a locale id is null or empty.
	 * @param locale the locale id to examine.
	 * @return true if the given locale id is null or empty, false otherwise.
	 */
	static public boolean isNullOrEmpty (LocaleId locale) {
		return (( locale == null ) || ( locale.equals(LocaleId.EMPTY) ));
	}
	
	/**
	 * Indicates if a string is null or empty, optionally ignoring the white spaces.
	 * @param string the string to examine.
	 * @param ignoreWS true to ignore white spaces.
	 * @return true if the given string is null, or empty. The argument ignoreWS is true a string
	 * with only white spaces is concidered empty.
	 */
	static public boolean isEmpty (String string,
		boolean ignoreWS)
	{
        if ( ignoreWS && ( string != null )) {
            string = string.trim();
        }
		return isEmpty(string);
	}
	
	/**
	 * Indicates if a StringBuilder object is null or empty.
	 * @param sb the object to examine.
	 * @return true if the given object is null or empty. 
	 */
	static public boolean isEmpty (StringBuilder sb) {
		return (( sb == null ) || ( sb.length() == 0 ));
	}

	/**
	 * Indicates if a given list is null or empty.
	 * @param <E> the type of the elements in the list.
	 * @param e the list to examine.
	 * @return true if the list is null or empty.
	 */
	static public <E> boolean isEmpty (List <E> e) {
		return (( e == null ) || e.isEmpty() );
	}
	
	/**
	 * Indicates if a given map is null or empty.
	 * @param <K> the type of the map's keys.
	 * @param <V> the type of the map's values.
	 * @param map the map to examine.
	 * @return true if the map is null or empty,
	 */
	public static <K, V> boolean isEmpty (Map<K, V> map) {		
		return (( map == null ) || map.isEmpty() );
	}
	
	/**
	 * Indicates if an array is null or empty.
	 * @param e the array to examine.
	 * @return true if the given array is null or empty.
	 */
	static public boolean isEmpty (Object[] e) {
		return (e == null ||(e != null && e.length == 0));
	}
	
	/**
	 * Gets the length of a string, even a null one.
	 * @param string the string to examine.
	 * @return the length of the given string, 0 if the string is null.
	 */
	static public int getLength (String string) {
		return (isEmpty(string)) ? 0 : string.length();
	}

	/**
	 * Gets a character at a given position in a string.
	 * The string can be null and the position can be beyond the last character.
	 * @param string the string to examine.
	 * @param pos the position of the character to retrieve. 
	 * @return the character at the given position,
	 * or '\0' if the string is null or if the position is beyond the length of the string. 
	 */
	static public char getCharAt (String string,
		int pos)
	{
		if ( isEmpty(string) ) {
			return '\0';
		}
		return (string.length() > pos) ? string.charAt(pos) : '\0';
	}

	/**
	 * Gets the last character of a given string.
	 * The string can be null or empty.
	 * @param string the string to examine.
	 * @return the last character of the given string,
	 * or '\0' if the string is null or empty.
	 */
	static public char getLastChar (String string) {
		if ( isEmpty(string) ) {
			return '\0';
		}
		return string.charAt(string.length() - 1);
	}

	/**
	 * Deletes the last character of a given string.
	 * The string can be null or empty.
	 * @param string the string where to remove the character.
	 * @return a new string where the last character has been removed,
	 * or an empty string if the given string was null or empty. 
	 */
	static public String deleteLastChar (String string) {
		if ( isEmpty(string) ) {
			return "";
		}
		return string.substring(0, string.length() - 1);
	}

	/**
	 * Gets the last character of a given StringBuilder object.
	 * The object can be null or empty.
	 * @param sb the StringBuilder object to examine.
	 * @return the last character of the given StringBuilder object,
	 * or '\0' if the object is null or empty. 
	 */
	static public char getLastChar (StringBuilder sb) {
		if ( isEmpty(sb) ) {
			return '\0';
		}
		return sb.charAt(sb.length() - 1);
	}

	/**
	 * Deletes the last character of a given StringBuilder object.
	 * If the object is null or empty no character are removed.
	 * @param sb the StringBuilder object where to remove the character.
	 */
	static public void deleteLastChar (StringBuilder sb) {
		if ( isEmpty(sb) ) {
			return;
		}
		sb.deleteCharAt(sb.length() - 1);
	}

	/**
	 * Indicates if a given index is within the list bounds.
	 * @param <E> the type of the list's elements.
	 * @param index the given index.
	 * @param list the given list.
	 * @return true if a given index is within the list bounds.
	 */
	public static <E> boolean checkIndex (int index,
		List<E> list)
	{
		return (list != null) && (index >= 0) && (index < list.size());
	}

	/**
	 * Converts an integer value to a string.
	 * This method simply calls <code>String.valueOf(intValue);</code>.
	 * @param value the value to convert.
	 * @return the string representation of the given value.
	 */
	public static String intToStr (int value) {
		return String.valueOf(value);
	}
	
	/**
	 * Converts a string to an integer. If the conversion fails the method
	 * returns the given default value.
	 * @param value the string to convert.
	 * @param intDefault the default value to use if the conversion fails.
	 * @return the integer value of the string, or the provided default
	 * value if the conversion failed.
	 */
	public static int strToInt (String value,
		int intDefault)
	{
		if ( Util.isEmpty(value) ) {
			return intDefault;
		}
		try {
			return Integer.valueOf(value);
		}
		catch (NumberFormatException e) {
			return intDefault; 
		}
	}
	
	/**
	 * Convert String to int .
	 * Almost 3x faster than Integer.valueOf()
	 * @param s - string to be converted to int
	 * @return int represented by string
	 * @throws NumberFormatException if the {@link String} does not represent a number.
	 */
	public static int fastParseInt(final String s)
	{
		if (s == null)
			throw new NumberFormatException("Null string");

		// Check for a sign.
		int num = 0;
		int sign = -1;
		final int len = s.length();
		final char ch = s.charAt(0);
		if (ch == '-')
		{
			if (len == 1)
				throw new NumberFormatException("Missing digits:  " + s);
			sign = 1;
		}
		else
		{
			final int d = ch - '0';
			if (d < 0 || d > 9)
				throw new NumberFormatException("Malformed:  " + s);
			num = -d;
		}

		// Build the number.
		final int max = (sign == -1) ?
				-Integer.MAX_VALUE : Integer.MIN_VALUE;
		final int multmax = max / 10;
		int i = 1;
		while (i < len)
		{
			int d = s.charAt(i++) - '0';
			if (d < 0 || d > 9)
				throw new NumberFormatException("Malformed:  " + s);
			if (num < multmax)
				throw new NumberFormatException("Over/underflow:  " + s);
			num *= 10;
			if (num < (max + d))
				throw new NumberFormatException("Over/underflow:  " + s);
			num -= d;
		}

		return sign * num;
	}
	
	/**
	 * Converts a string to a long. If the conversion fails the method
	 * returns the given default value.
	 * @param value the string to convert.
	 * @param longDefault the default value to use if the conversion fails.
	 * @return the long value of the string, or the provided default
	 * value if the conversion failed.
	 */
	public static long strToLong (String value,
		long longDefault)
	{
		if ( Util.isEmpty(value) ) {
			return longDefault;
		}
		try {
			return Long.valueOf(value);
		}
		catch (NumberFormatException e) {
			return longDefault; 
		}
	}
	
	/**
	 * Converts a string to a double. If the conversion fails the method
	 * returns the given default value.
	 * @param value the string to convert.
	 * @param doubleDefault the default value to use if the conversion fails.
	 * @return the double value of the string, or the provided default
	 * value if the conversion failed.
	 */
	public static double strToDouble (String value,
		double doubleDefault)
	{
		if ( Util.isEmpty(value) ) {
			return doubleDefault;
		}
		try {
			return Double.valueOf(value);
		}
		catch (NumberFormatException e) {
			return doubleDefault; 
		}
	}

	/**
	 * Gets the element of an array for a given index.
	 * the method returns null if the index is out of bounds.
	 * @param <T> the type of the array's elements.
	 * @param array the array where to lookup the element.
	 * @param index the index.
	 * @return the element of the array for the given index, or null if the
	 * index is out of bounds, or if the element is null.
	 */
	public static <T>T get(T[] array,
		int index)
	{
		if (( index >= 0 ) && ( index < array.length )) {
			return array[index];
		}
		else {
			return null;
		}
	}

	/**
	 * Returns true if a given index is within the array bounds.
	 * @param <T> the type of the array's elements.
	 * @param index the given index.
	 * @param array the given list.
	 * @return true if a given index is within the array bounds.
	 */
	public static <T> boolean checkIndex (int index,
		T[] array)
	{
		return (array != null) && (index >= 0) && (index < array.length);
	}
	
	/**
	 * Indicates whether a byte-flag is set or not in a given value. 
	 * @param value the value to check.
	 * @param flag the flag to look for.
	 * @return true if the flag is set, false if it is not.
	 */
	public static boolean checkFlag (int value,
		int flag)
	{
		return (value & flag) == flag;
	}
	
	/**
	 * Get the operating system
	 * @return one of WINDOWS, MAC or LINUX
	 */
	public static SUPPORTED_OS getOS () {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Windows")) { // Windows case
			return SUPPORTED_OS.WINDOWS;
		}		
		else if (osName.contains("OS X")) { // Macintosh case
			return SUPPORTED_OS.MAC;
		}
		return SUPPORTED_OS.LINUX;
	}
		
   /**
    * Opens the specified page in a web browser (Java 1.5 compatible).
    * <p>This is based on the public domain class BareBonesBrowserLaunch from Dem Pilafian at
    * (<a href="http://www.centerkey.com/java/browser/">www.centerkey.com/java/browser</a>)
    * @param url the URL of the page to open.
    */
	public static void openURL (String url) {
		String osName = System.getProperty("os.name");
		try {
			if (osName.contains("OS X")) { // Macintosh case
				/* One possible way. But this causes warning when run with -XstartOnFirstThread option
				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[]{String.class});
				openURL.invoke(null, new Object[] {url}); */
				// So, use open, and seems to work on the Macintosh (not Linux)
				Runtime.getRuntime().exec("open " + url);
			}
			else if (osName.startsWith("Windows")) { // Windows case
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			}
			else { // Assumes Unix or Linux
				boolean found = false;
				for ( String browser : browsers ) {
					if ( !found ) { // Search for the first browser available
						found = Runtime.getRuntime().exec(new String[] {"which", browser}).waitFor() == 0;
						if ( found ) { // Start it if we find one
							Runtime.getRuntime().exec(new String[] {browser, url});
						}
					}
				}
				if ( !found ) {
					throw new Exception("No browser found.");
				}
			}
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error attempting to launch web browser.", e);
		}
	}
	
	/**
	 * Opens a given topic of the OkapiWiki.
	 * @param topic the title of the topic/page.
	 */
	public static void openWikiTopic (String topic) {
		try {
			// Resolve spaces
			topic = topic.replace(' ', '_');
			//TODO: get the base URL from a properties file
			Util.openURL(new URL(String.format("http://okapiframework.org/wiki/index.php?title=%s", topic)).toString());
		}
		catch ( MalformedURLException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the directory location of a given class. The value returned can be the directory
	 * where the .class file is located, or, if the class in a JAR file, the directory
	 * where the .jar file is located.    
	 * @param theClass the class to query.
	 * @return the directory location of the given class, or null if an error occurs.
	 */
	public static String getClassLocation (Class<?> theClass) {
		String res = null;
		File file = new File(theClass.getProtectionDomain().getCodeSource().getLocation().getFile());
		res = URLDecodeUTF8(file.getAbsolutePath());
		// Remove the JAR file if necessary
		if ( res.endsWith(".jar") ) {
			res = getDirectoryName(res);
		}
		return res;
	}
	

// Unused
//	/**
//	 * Generate a random string consisting only of numbers 
//	 * @param length - specifies length of the string
//	 * @return a random String
//	 */
//	public static String generateRandomId(int length) {
//		Random rnd = new Random();
//
//		StringBuilder sb = new StringBuilder( length );
//		  for( int i = 0; i < length; i++ ) { 
//			  sb.append(rnd.nextInt(9));
//		  }
//		  return sb.toString();
//
//	}

	/**
	 * Replaces in a given original string, a potential variable ROOT_DIRECTORY_VAR by a given root directory.
	 * @param original the original string where to perform the replacement.
	 * @param rootDir the root directory. If null it will be automatically set to the
	 * user home directory.
	 * @return the original string with ROOT_DIRECTORY_VAR replaced if it was there.
	 */
	public static String fillRootDirectoryVariable (String original,
		String rootDir)
	{
		if ( rootDir == null ) {
			if ( !original.contains(ROOT_DIRECTORY_VAR) ) {
				// We early-out here if no work needs to be done so as to avoid
				// unnecessarily throwing when resolving user.dir.
				return original;
			} else {
				// The user.dir system property is restricted in some scenarios.
				// See "Forbidden System Properties":
				// https://docs.oracle.com/javase/tutorial/deployment/doingMoreWithRIA/properties.html
				// However we know that we need to replace ROOT_DIRECTORY_VAR
				// now, and the path will be useless without replacement, so we
				// just throw and let the caller handle it.
				rootDir = System.getProperty("user.dir");
			}
		}
		return original.replace(ROOT_DIRECTORY_VAR, rootDir);
	}

	/**
	 * Replaces in a given original string, a potential variable INPUT_ROOT_DIRECTORY_VAR by a given root directory.
	 * @param original the original string where to perform the replacement.
	 * @param inputRootDir the input root directory. If null it will be automatically set to
	 * an empty string.
	 * @return the original string with INPUT_ROOT_DIRECTORY_VAR replaced if it was there.
	 */
	public static String fillInputRootDirectoryVariable (String original,
		String inputRootDir)
	{
		if ( inputRootDir == null ) {
			inputRootDir = "";
		}
		return original.replace(INPUT_ROOT_DIRECTORY_VAR, inputRootDir);
	}
	
	/**
	 * Expands environment variables by replacing strings of the type
	 * ${varname} with their values as reported by the system.
	 * @param original The original string in which to perform the replacement
	 * @return The original string with all environment variables expanded
	 */
	public static String fillSystemEnvars(String original)
	{
		for (Entry<String, String> e : System.getenv().entrySet()) {
			original = original.replace(String.format("${%s}", e.getKey()), e.getValue());
		}
		return original;
	}
	
	/**
	 * Check a piece of text to make sure that all contained variables (${foo})
	 * are resolvable by the fill...() methods in this class.
	 * @param text The text to check
	 * @param allowEnvar Whether or not to allow system environment variables
	 * @param allowRootDir Whether or not to allow ${rootDir}
	 * @param allowInputRootDir Whether or not to allow ${inputRootDir}
	 * @return Whether or not the input text's variables are valid
	 */
	public static boolean validateVariables(String text,
			boolean allowEnvar, boolean allowRootDir, boolean allowInputRootDir) {
		Matcher m = VARIABLE_REGEX_PATTERN.matcher(text);
		while (m.find()) {
			String var = m.group();
			String varName = m.group(1);
			if (allowEnvar && System.getenv().containsKey(varName)) continue;
			if (allowRootDir && var.equals(ROOT_DIRECTORY_VAR)) continue;
			if (allowInputRootDir && var.equals(INPUT_ROOT_DIRECTORY_VAR)) continue;
			return false;
		}
		return true;
	}
	
	/**
	 * Expand all supported variables and canonicalize (resolve ".." and ".")
	 * a path. If the input path has no variables, it is returned unchanged.
	 * rootDir and inputRootDir can be null.
	 * @param path The path to expand
	 * @param rootDir The directory to expand ${rootDir} into
	 * @param inputRootDir The directory to expand ${inputRootDir} into
	 * @return The expanded path
	 * @throws IOException If canonicalizing fails
	 */
	public static String expandPath (String path, String rootDir, String inputRootDir)
			throws IOException {
		if (!path.contains("${")) return path;
		
		path = Util.fillSystemEnvars(path);
		path = Util.fillRootDirectoryVariable(path, rootDir);
		path = Util.fillInputRootDirectoryVariable(path, inputRootDir);
		path = new File(path).getCanonicalPath();
		return path;
	}

	/**
	 * Returns the smallest value in a given array of values.
	 * @param values the given array
	 * @return the smallest value in the array
	 */
	public static int min (int... values) {
		int res = Integer.MAX_VALUE;
		for (int value : values) {
			res = Math.min(res, value);
		}
		return (values.length > 0) ? res : 0;		
	}

	/**
	 * Safely returns a value for a given key in a given map. 
	 * @param <V> the type of keys maintained by the given map
	 * @param <K> the type of mapped values of the given map
	 * @param map the given map
	 * @param key the key whose associated value is to be returned 
	 * @param defaultValue the default value to be returned if the key is not found
	 * @return the value to which the specified key is mapped, or
     *         defaultValue if the given map contains no mapping for the given key
	 */
	public static <K, V> V getValue(Map<K, V> map, K key, V defaultValue) {
		if (map == null) return defaultValue;
		if (!map.containsKey(key)) return defaultValue;
		
		return map.get(key);		
	}
	
	/**
	 * Given an integer range and a value normalize that value on a scale between 0 and 100.
	 * @param low - lowest value of the range
	 * @param high - highest value of the range
	 * @param value - the value that needs to be mapped to 0-100
	 * @return mapped value, a number between 0 and 100
	 */
	public static int normalizeRange(double low, double high, double value) {
		double m = 0.0d; // low value  of map to range
		double n = 100.0d; // high value of map to range
		return (int) (m +((value-low)/(high-low) * (n-m)));
	}

	/**
	 * Formats a double value so only the significant trailing zeros are displayed.
	 * Removes the decimal period if there are no significant decimal digits.
	 * @param value the double value to format (can be null).
	 * @return the formatted value or an empty string.
	 */
	public static String formatDouble (Double value) {
		if ( value == null ) return "";
		String tmp = String.format(Locale.ENGLISH, "%f", value);
		// Remove trailing zeros
		while (( tmp.length() > 1 ) && ( tmp.charAt(tmp.length()-1) == '0' )) {
			tmp = tmp.substring(0, tmp.length()-1);
		}
		// Remove ending period if it's the last character
		if ( tmp.charAt(tmp.length()-1) == '.' ) {
			tmp = tmp.substring(0, tmp.length()-1);
		}
		return tmp;
	}

	/**
	 * Indicates if the given character is valid in XML.
	 * See <a href='http://www.w3.org/TR/xml/#charsets'>http://www.w3.org/TR/xml/#charsets</a> for details. 
	 * @param ch the character to test.
	 * @return true if the given character is valid in XML, false otherwise.
	 */
	public static boolean isValidInXML (int ch) {
		if (( ch > 0x001F ) && ( ch < 0xD800 )) return true; // Most frequent
		if ( ch > 0xD7FF ) {
			if ( ch < 0xE000 ) return false;
			if (( ch == 0xFFFE ) || ( ch == 0xFFFF )) return false;
			return true;
		}
		// Else: control characters
		switch ( ch ) {
		case 0x0009:
		case 0x000a:
		case 0x000d:
			return true;
		}
		return false;
	}

	/**
	 * Decodes a application/x-www-form-urlencoded string using UTF-8.
	 *
	 * This wraps a call to {@link URLDecoder#decode(String, String)}, passing
	 * UTF-8 as the argument and wrapping the exception as an
	 * {@link IllegalStateException}.  This exists because URLDecoder does not
	 * offer a signature that takes a {@link Charset}.
	 * @param s the String to decode
	 * @return the newly decoded String
	 * @throws IllegalStateException if UTF-8 isn't recognized as a valid
	 * 		   encoding, which should never happen.
	 */
	public static String URLDecodeUTF8(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Translates a string into application/x-www-form-urlencoded format using
	 * UTF-8.
	 *
	 * This wraps a call to {@link URLEncoder#encode(String, String)}, passing
	 * UTF-8 as the argument and wrapping the exception as an
	 * {@link IllegalStateException}.  This exists because URLEncoder does not
	 * offer a signature that takes a {@link Charset}.
	 * @param s the String to be translated
	 * @return the translated String
	 * @throws IllegalStateException if UTF-8 isn't recognized as a valid
	 * 		   encoding, which should never happen.
	 */
	public static String URLEncodeUTF8(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Create a new {@link PrintWriter}, without automatic line flushing,
	 * writing to the specified file path and using the specified Charset.
	 * PrintWriter doesn't provide this signature and requires a charset
	 * name be passed instead, which requires additional exception handling.
	 * @param filePath the file to use as the destination of this writer.
	 * @param charset charset to use when writing to the file
	 * @return new PrintWriter instance
	 * @throws FileNotFoundException if the given string does not denote an
	 * 		existing, writable regular file and a new regular file of that
	 * 	    name can't be created.
	 */
	public static PrintWriter charsetPrintWriter(String filePath, Charset charset) 
								throws FileNotFoundException {
		return new PrintWriter(new OutputStreamWriter(
						new BufferedOutputStream(new FileOutputStream(filePath)),
						charset));
	}

	/**
	 * Given a Reader, discard any leading BOM.
	 * @param r the reader.
	 * @return a Reader instance representing the remaining content. This
	 *         may not be the same Reader instance that was passed in.
	 * @throws IOException if anything goes wrong when reading.
	 */
	public static Reader skipBOM(Reader r) throws IOException {
		PushbackReader pb = new PushbackReader(r, 1);
		char bom = (char)pb.read();
		if (bom != '\uFEFF') {
			pb.unread(bom);
		}
		return pb;
	}

	/**
	 * @param <T> the type of the class.
	 * @param clazz the class.
	 * @return a comparator for {@link java.util.TreeMap} that accepts null keys.
	 */
	public static <T extends Comparable<T>> Comparator<T> createComparatorHandlingNullKeys(Class<T> clazz) {
		return new Comparator<T>() {
			@Override public int compare(T s1, T s2) {
				if (s1 == null && s2 != null) {
					return -1;
				} else if (s1 == null && s2 == null) {
					return 0;
				} else if (s1 != null && s2 == null) {
					return 1;
				} else {
					return s1.compareTo(s2);
				}
			}
		};
	}

	/**
	 * creates an XPathFactory and tries to handle issues with saxon.
	 *
	 * @see <a href="https://saxonica.plan.io/issues/1944">https://saxonica.plan.io/issues/1944</a>
	 * @see <a href="http://sourceforge.net/p/saxon/mailman/message/33221102/">http://sourceforge.net/p/saxon/mailman/message/33221102/</a>
	 * @return the {@link XPathFactory} created.
	 */
	public static XPathFactory createXPathFactory() {
		// see: https://saxonica.plan.io/issues/1944
		// and http://sourceforge.net/p/saxon/mailman/message/33221102/
		// as soon as a new Saxon library is used (>= 9.5.1.5) this needs to be
		// reversed to use this workaround on Java <= 1.5
		// Saxon 9.6 will drop XPathFactory completely in the near future - so they say!!

		// try default XPath instance
		try {
			// will fail on Java >= 1.8 with saxon < 9.5.1.5
			// will fail on Java <= 1.5 with saxon >= 9.5.1.5
			return XPathFactory.newInstance();
		} catch(Exception e) {}

		// if saxon is available the previous try failed. use saxon directly.
		try {
			return XPathFactory.newInstance(
					  XPathFactory.DEFAULT_OBJECT_MODEL_URI,
					  "net.sf.saxon.xpath.XPathFactoryImpl",
					  ClassLoader.getSystemClassLoader());
		} catch(Exception e) {}

		return null;
	}
}
