/*===========================================================================
  Copyright 2003-2009 Christian d'Heureuse, Inventec Informatik, CH
  www.source-code.biz, www.inventec.ch/chdh
-----------------------------------------------------------------------------
  This module is multi-licensed and may be used under the terms
  of any of the following licenses:
  EPL, Eclipse Public License, www.eclipse.org/legal
  LGPL, GNU Lesser General Public License, www.gnu.org/licenses/lgpl.html
  AL, Apache License, www.apache.org/licenses
  BSD, BSD License, www.opensource.org/licenses/bsd-license.php
  Please contact the author if you need another license.
  This module is provided "as is", without warranties of any kind.
----------------------------------------------------------------------------
  Modified by the Okapi Framework project
===========================================================================*/

package net.sf.okapi.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import net.sf.okapi.common.exceptions.OkapiIOException;

/**
* Base64 Encoder/Decoder for all VM.
*/
public class Base64 {

	private static final String ENCSTR = "#BeNcStr";
	private static final Charset CSUTF8 = Charset.forName("UTF8");
	
	// Mapping table from 6-bit nibbles to Base64 characters.
	private static char[] map1 = new char[64];
	static {
		int i=0;
		for (char c='A'; c<='Z'; c++) map1[i++] = c;
		for (char c='a'; c<='z'; c++) map1[i++] = c;
		for (char c='0'; c<='9'; c++) map1[i++] = c;
		map1[i++] = '+'; map1[i++] = '/';
	}

	// Mapping table from Base64 characters to 6-bit nibbles.
	private static byte[] map2 = new byte[128];
	static {
		for (int i=0; i<map2.length; i++) map2[i] = -1;
		for (int i=0; i<64; i++) map2[map1[i]] = (byte)i;
	}

	/**
	 * Encodes a string into UTF-8 Base64 format.
	 * No blanks or line breaks are inserted.
	 * @param str the String to be encoded.
	 * @return the String with the Base64 encoded data.
	 */
	public static String encodeString (String str) {
		return new String(encode(str.getBytes(CSUTF8)));
	}

	/**
	* Encodes a byte array into Base64 format.
	* No blanks or line breaks are inserted.
	* @param data the array containing the data bytes to be encoded.
	* @return the character array with the Base64 encoded data.
	*/
	public static char[] encode (byte[] data) {
		return encode(data, data.length);
	}

	/**
	* Encodes a byte array into Base64 format.
	* No blanks or line breaks are inserted.
	* @param data the array containing the data bytes to be encoded.
	* @param iLen the number of bytes to process in <code>in</code>.
	* @return the character array with the Base64 encoded data.
	*/
	public static char[] encode (byte[] data,
		int iLen)
	{
		int oDataLen = (iLen*4+2)/3; // output length without padding
		int oLen = ((iLen+2)/3)*4; // output length including padding
		char[] out = new char[oLen];
		int ip = 0;
		int op = 0;
		while ( ip < iLen ) {
			int i0 = data[ip++] & 0xff;
			int i1 = ip < iLen ? data[ip++] & 0xff : 0;
			int i2 = ip < iLen ? data[ip++] & 0xff : 0;
			int o0 = i0 >>> 2;
			int o1 = ((i0 &   3) << 4) | (i1 >>> 4);
			int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			int o3 = i2 & 0x3F;
			out[op++] = map1[o0];
			out[op++] = map1[o1];
			out[op] = op < oDataLen ? map1[o2] : '='; op++;
			out[op] = op < oDataLen ? map1[o3] : '='; op++;
		}
		return out;
	}

	/**
	* Decodes a string from a UTF-8 Base64 string.
	* The coded string may have line breaks.
	* @param str the Base64 String to be decoded.
	* @return the String containing the decoded data.
	* @throws IllegalArgumentException if the input is not valid Base64 encoded data.
	*/
	public static String decodeString (String str) {
		str = str.replaceAll("[\r\n]", "");
		return new String(decode(str), CSUTF8);
	}
	
	/**
	* Decodes a byte array from Base64 format.
	* The coded string may have line breaks.
	* @param str the Base64 String to be decoded.
	* @return the array containing the decoded data bytes.
	* @throws IllegalArgumentException if the input is not valid Base64 encoded data.
	*/
	public static byte[] decode (String str) {
		str = str.replaceAll("[\r\n]", "");
		return decode(str.toCharArray());
	}

	/**
	* Decodes a byte array from Base64 format.
	* No blanks or line breaks are allowed within the Base64 encoded data.
	* @param data the character array containing the Base64 encoded data.
	* @return the array containing the decoded data bytes.
	* @throws IllegalArgumentException if the input is not valid Base64 encoded data.
	*/
	public static byte[] decode (char[] data) {
		int iLen = data.length;
		if ( iLen % 4 != 0 ) {
			throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
		}
		while ( iLen > 0 && data[iLen-1] == '=' ) iLen--;
		int oLen = (iLen*3)/4;
		byte[] out = new byte[oLen];
		int ip = 0;
		int op = 0;
		while ( ip < iLen ) {
			int i0 = data[ip++];
			int i1 = data[ip++];
			int i2 = ip < iLen ? data[ip++] : 'A';
			int i3 = ip < iLen ? data[ip++] : 'A';
			if ( i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127 ) {
				throw new IllegalArgumentException ("Illegal character in Base64 encoded data.");
			}
			int b0 = map2[i0];
			int b1 = map2[i1];
			int b2 = map2[i2];
			int b3 = map2[i3];
			if ( b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0 ) {
				throw new IllegalArgumentException ("Illegal character in Base64 encoded data.");
			}
			int o0 = ( b0 <<2 ) | ( b1 >>> 4 );
			int o1 = (( b1 & 0xf ) << 4 ) | ( b2 >>> 2 );
			int o2 = (( b2 & 3) << 6 ) | b3;
			out[op++] = (byte)o0;
			if ( op<oLen ) out[op++] = (byte)o1;
			if ( op<oLen ) out[op++] = (byte)o2;
		}
		return out;
	}
	
	
	public static String encode (InputStream is) {
		if (is == null)
			throw new IllegalArgumentException("Input stream for Base64 encoding cannot be null.");
		
		StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[2048];
		int bytesRead = 0;
		 
		try {
			while ((bytesRead = is.read(buffer)) > 0) {
				char[] chunk = encode(buffer, bytesRead);
				sb.append(chunk);
			 }
		} catch (IOException e) {
			throw new OkapiIOException ("I/O exception while reading data for Base64 encoding.", e);
		}
			 		
		return sb.toString();		
	}

	/**
	 * Encode a password-type string value.
	 * @param password the password (in clear).
	 * @return the masked value for the given string.
	 * @see #decodePassword(String)
	 */
	public static String encodePassword (String password) {
		return ENCSTR+Base64.encodeString(password);
	}
	
	/**
	 * Decode a string value that is possibly encoded into a clear string.
	 * @param password the string to decode. It may be a clear string too.
	 * @return the decoded string.
	 */
	public static String decodePassword (String password) {
		if ( password.startsWith(ENCSTR) ) {
			return Base64.decodeString(password.substring(ENCSTR.length()));
		}
		else {
			return password;
		}
	}

}


