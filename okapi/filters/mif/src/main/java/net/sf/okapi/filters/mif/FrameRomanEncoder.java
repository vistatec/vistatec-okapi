/*===========================================================================
  Copyright (C) 2011-2012 by the Okapi Framework contributors
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
===========================================================================*/

package net.sf.okapi.filters.mif;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.util.HashMap;

/**
 * Implements CharsetEncoder for the FrameMaker Roman character set.
 * This goes from the Unicode characters to the MIF bytes.
 */
class FrameRomanEncoder extends CharsetEncoder {

	protected FrameRomanEncoder (Charset charset,
		float averageCharsPerByte,
		float maxCharsPerByte)
	{
		super(charset, averageCharsPerByte, maxCharsPerByte);
	}

	@Override
	protected CoderResult encodeLoop (CharBuffer in,
		ByteBuffer out)
	{
		Byte outputByte;
		while ( in.hasRemaining() ) {
			// First, check if we can output
			if ( !out.hasRemaining() ) {
				// If not, return and tell the caller
				return CoderResult.OVERFLOW;
			}

			// Get the input character
			char inputChar = in.get();
			
			if ( inputChar <= 0x7e ) {
				// Simple ASCII mapping
				outputByte = (byte)inputChar;
			}
			else {
				// Or use the table for control and extended values
				outputByte = charToByte.get(Character.valueOf(inputChar));
				// Treat the characters not mappable
				if ( outputByte == null ) {
					outputByte = (byte)'?';
				}
			}
			// Output to the buffer
			out.put(outputByte);
		}
		// Done
        return CoderResult.UNDERFLOW;
	}
	
	private final static HashMap<Character, Byte> charToByte;

	static {
		charToByte = new HashMap<Character, Byte>();

		// Control characters
		charToByte.put('\u00ad', (byte)0x04);
		charToByte.put('\u200d', (byte)0x05);
		charToByte.put('\u2010', (byte)0x06);
		charToByte.put('\u2007', (byte)0x10);
		charToByte.put('\u00a0', (byte)0x11);
		charToByte.put('\u2009', (byte)0x12);
		charToByte.put('\u2002', (byte)0x13);
		charToByte.put('\u2003', (byte)0x14);
		charToByte.put('\u2011', (byte)0x15);
		
		// Extended characters
		charToByte.put('\u00c4', (byte)0x80);
		charToByte.put('\u00c5', (byte)0x81);
		charToByte.put('\u00c7', (byte)0x82);
		charToByte.put('\u00c9', (byte)0x83);
		charToByte.put('\u00d1', (byte)0x84);
		charToByte.put('\u00d6', (byte)0x85);
		charToByte.put('\u00dc', (byte)0x86);
		charToByte.put('\u00e1', (byte)0x87);
		charToByte.put('\u00e0', (byte)0x88);
		charToByte.put('\u00e2', (byte)0x89);
		charToByte.put('\u00e4', (byte)0x8a);
		charToByte.put('\u00e3', (byte)0x8b);
		charToByte.put('\u00e5', (byte)0x8c);
		charToByte.put('\u00e7', (byte)0x8d);
		charToByte.put('\u00e9', (byte)0x8e);
		charToByte.put('\u00e8', (byte)0x8f);
		charToByte.put('\u00ea', (byte)0x90);
		charToByte.put('\u00eb', (byte)0x91);
		charToByte.put('\u00ed', (byte)0x92);
		charToByte.put('\u00ec', (byte)0x93);
		charToByte.put('\u00ee', (byte)0x94);
		charToByte.put('\u00ef', (byte)0x95);
		charToByte.put('\u00f1', (byte)0x96);
		charToByte.put('\u00f3', (byte)0x97);
		charToByte.put('\u00f2', (byte)0x98);
		charToByte.put('\u00f4', (byte)0x99);
		charToByte.put('\u00f6', (byte)0x9a);
		charToByte.put('\u00f5', (byte)0x9b);
		charToByte.put('\u00fa', (byte)0x9c);
		charToByte.put('\u00f9', (byte)0x9d);
		charToByte.put('\u00fb', (byte)0x9e);
		charToByte.put('\u00fc', (byte)0x9f);
		charToByte.put('\u2020', (byte)0xa0);
		charToByte.put('\u00b0', (byte)0xa1);
		charToByte.put('\u00a2', (byte)0xa2);
		charToByte.put('\u00a3', (byte)0xa3);
		charToByte.put('\u00a7', (byte)0xa4);
		charToByte.put('\u2022', (byte)0xa5);
		charToByte.put('\u00b6', (byte)0xa6);
		charToByte.put('\u00df', (byte)0xa7);
		charToByte.put('\u00ae', (byte)0xa8);
		charToByte.put('\u00a9', (byte)0xa9);
		charToByte.put('\u2122', (byte)0xaa);
		charToByte.put('\u00b4', (byte)0xab);
		charToByte.put('\u00a8', (byte)0xac);
		charToByte.put('\u00a6', (byte)0xad);
		charToByte.put('\u00c6', (byte)0xae);
		charToByte.put('\u00d8', (byte)0xaf);
		charToByte.put('\u00d7', (byte)0xb0);
		charToByte.put('\u00b1', (byte)0xb1);
		charToByte.put('\u00f0', (byte)0xb2);
		charToByte.put('\u0160', (byte)0xb3);
		charToByte.put('\u00a5', (byte)0xb4);
		charToByte.put('\u00b5', (byte)0xb5);
		charToByte.put('\u00b9', (byte)0xb6);
		charToByte.put('\u00b2', (byte)0xb7);
		charToByte.put('\u00b3', (byte)0xb8);
		charToByte.put('\u00bc', (byte)0xb9);
		charToByte.put('\u00bd', (byte)0xba);
		charToByte.put('\u00aa', (byte)0xbb);
		charToByte.put('\u00ba', (byte)0xbc);
		charToByte.put('\u00be', (byte)0xbd);
		charToByte.put('\u00e6', (byte)0xbe);
		charToByte.put('\u00f8', (byte)0xbf);
		charToByte.put('\u00bf', (byte)0xc0);
		charToByte.put('\u00a1', (byte)0xc1);
		charToByte.put('\u00ac', (byte)0xc2);
		charToByte.put('\u00d0', (byte)0xc3);
		charToByte.put('\u0192', (byte)0xc4);
		charToByte.put('\u00dd', (byte)0xc5);
		charToByte.put('\u00fd', (byte)0xc6);
		charToByte.put('\u00ab', (byte)0xc7);
		charToByte.put('\u00bb', (byte)0xc8);
		charToByte.put('\u2026', (byte)0xc9);
		charToByte.put('\u00fe', (byte)0xca);
		charToByte.put('\u00c0', (byte)0xcb);
		charToByte.put('\u00c3', (byte)0xcc);
		charToByte.put('\u00d5', (byte)0xcd);
		charToByte.put('\u0152', (byte)0xce);
		charToByte.put('\u0153', (byte)0xcf);
		charToByte.put('\u2013', (byte)0xd0);
		charToByte.put('\u2014', (byte)0xd1);
		charToByte.put('\u201c', (byte)0xd2);
		charToByte.put('\u201d', (byte)0xd3);
		charToByte.put('\u2018', (byte)0xd4);
		charToByte.put('\u2019', (byte)0xd5);
		charToByte.put('\u00f7', (byte)0xd6);
		charToByte.put('\u00de', (byte)0xd7);
		charToByte.put('\u00ff', (byte)0xd8);
		charToByte.put('\u0178', (byte)0xd9);
		charToByte.put('\u2044', (byte)0xda);
		charToByte.put('\u00a4', (byte)0xdb);
		charToByte.put('\u2039', (byte)0xdc);
		charToByte.put('\u203a', (byte)0xdd);
		charToByte.put('\ufb01', (byte)0xde);
		charToByte.put('\ufb02', (byte)0xdf);
		charToByte.put('\u2021', (byte)0xe0);
		charToByte.put('\u00b7', (byte)0xe1);
		charToByte.put('\u201a', (byte)0xe2);
		charToByte.put('\u201e', (byte)0xe3);
		charToByte.put('\u2030', (byte)0xe4);
		charToByte.put('\u00c2', (byte)0xe5);
		charToByte.put('\u00ca', (byte)0xe6);
		charToByte.put('\u00c1', (byte)0xe7);
		charToByte.put('\u00cb', (byte)0xe8);
		charToByte.put('\u00c8', (byte)0xe9);
		charToByte.put('\u00cd', (byte)0xea);
		charToByte.put('\u00ce', (byte)0xeb);
		charToByte.put('\u00cf', (byte)0xec);
		charToByte.put('\u00cc', (byte)0xed);
		charToByte.put('\u00d3', (byte)0xee);
		charToByte.put('\u00d4', (byte)0xef);
		charToByte.put('\u0161', (byte)0xf0);
		charToByte.put('\u00d2', (byte)0xf1);
		charToByte.put('\u00da', (byte)0xf2);
		charToByte.put('\u00db', (byte)0xf3);
		charToByte.put('\u00d9', (byte)0xf4);
		charToByte.put('\u20ac', (byte)0xf5);
		charToByte.put('\u02c6', (byte)0xf6);
		charToByte.put('\u02dc', (byte)0xf7);
		charToByte.put('\u00af', (byte)0xf8);
		charToByte.put('\u02c7', (byte)0xf9);
		charToByte.put('\u017d', (byte)0xfa);
		charToByte.put('\u02da', (byte)0xfb);
		charToByte.put('\u00b8', (byte)0xfc);
		charToByte.put('\u02dd', (byte)0xfd);
		charToByte.put('\u017e', (byte)0xfe);
	}

}
