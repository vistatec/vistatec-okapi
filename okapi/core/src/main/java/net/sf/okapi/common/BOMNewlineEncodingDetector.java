/*
 * Jericho HTML Parser - Java based library for analysing and manipulating HTML Version 3.0-beta1 Copyright (C) 2007
 * Martin Jericho http://jerichohtml.sourceforge.net/ This library is free software; you can redistribute it and/or
 * modify it under the terms of either one of the following licences: 1. The Eclipse Public License (EPL) version 1.0,
 * included in this distribution in the file licence-epl-1.0.html or available at
 * http://www.eclipse.org/legal/epl-v10.html 2. The GNU Lesser General Public License (LGPL) version 2.1 or later,
 * included in this distribution in the file licence-lgpl-2.1.txt or available at http://www.gnu.org/licenses/lgpl.txt
 * This library is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * individual licence texts for more details.
 */
/*===========================================================================
  Additional changes
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiUnsupportedEncodingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to detect byte-order-mark and other easily guessed of encodings, as well as the type of line-break used
 * in a given input. Based on information in: http://www.w3.org/TR/REC-xml/#sec-guessing-no-ext-info
 * http://www.w3.org/TR/html401/charset.html#h-5.2
 */
public final class BOMNewlineEncodingDetector {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private static final int MAX_LOOKAHEAD = 1024*8;

	/**
	 * Defines type friendly newline types.
	 */
	public enum NewlineType {
		/**
		 * Carriage Return
		 */
		CR {
			public String toString() {
				return "\r";
			}
		},

		/**
		 * Line Feed
		 */
		LF {
			public String toString() {
				return "\n";
			}
		},

		/**
		 * Carriage Return Line Feed
		 */
		CRLF {
			public String toString() {
				return "\r\n";
			}
		},
		
		/**
		 * UNKOWN
		 */
		UNKOWN {
			public String toString() {
				return "";
			}
		}
		
	}

	/**
	 * Java friendly UTF-16 encoding name.
	 */
	public static final String UTF_16 = "UTF-16";
	/**
	 * Java friendly UTF-16 big endian encoding name.
	 */
	public static final String UTF_16BE = "UTF-16BE";
	/**
	 * Java friendly UTF-16 little endian encoding name.
	 */
	public static final String UTF_16LE = "UTF-16LE";
	/**
	 * Java friendly UTF-8 encoding name.
	 */
	public static final String UTF_8 = "UTF-8";
	/**
	 * Java friendly ISO-8859-1 encoding name.
	 */
	public static final String ISO_8859_1 = "ISO-8859-1";
	/**
	 * Java friendly EBCDIC encoding name..
	 */
	public static final String EBCDIC = "Cp037"; // aka IBM037, not guaranteed,
	// but available on most
	// platforms

	// All of the following encodings are generally not supported in java and
	// will usually throw an exception if decoding is attempted.
	// Specified explicitly using Byte Order Mark:

	/**
	 * SCSU (Standard Compression Scheme for Unicode)
	 */
	public static final String SCSU = "SCSU";
	/**
	 * Java friendly UTF-7 encoding name..
	 */
	public static final String UTF_7 = "UTF-7";
	/**
	 * Java friendly UTF-EBCDIC encoding name..
	 */
	public static final String UTF_EBCDIC = "UTF-EBCDIC";
	/**
	 * BOCU (Binary Ordered Compression for Unicode)
	 */
	public static final String BOCU_1 = "BOCU-1";
	/**
	 * Java friendly UTF-32 encoding name..
	 */
	public static final String UTF_32 = "UTF-32";
	// Guessed from presence of 00 bytes in first four bytes:
	/**
	 * Java friendly UTF-32 big endian encoding name..
	 */
	public static final String UTF_32BE = "UTF-32BE";
	/**
	 * Java friendly UTF-32 little endian encoding name..
	 */
	public static final String UTF_32LE = "UTF-32LE";

	private String defaultEncoding = ISO_8859_1;

	private final InputStream inputStream;
	private String encoding = null;
	private String encodingSpecificationInfo = null;
	private boolean definitive = true;
	private int bomSize;
	private boolean hasUtf8Bom;
	private boolean hasUtf7Bom;
	private boolean hasBom;
	private boolean autodetected;
	private NewlineType newlineType = NewlineType.UNKOWN;

	/**
	 * Create a new BOMNewlineEncodingDetector from an {@link InputStream}. Cannot detect {@link NewlineType} unless a
	 * valid encoding is detected.
	 * 
	 * @param inputStream the input stream
	 */
	public BOMNewlineEncodingDetector(final InputStream inputStream) {
		if (inputStream.markSupported()) {
			this.inputStream = inputStream;
		} else {
			this.inputStream = new BufferedInputStream(inputStream, MAX_LOOKAHEAD+1024);
		}

		inputStream.mark(MAX_LOOKAHEAD);
		autodetected = false;
		bomSize = 0;
	}

	/**
	 * Create a new BOMNewlineEncodingDetector from an {@link InputStream} and a user provided encoding. This
	 * BOMNewlineEncodingDetector can convert the input bytes to Unicode for detection of the {@link NewlineType}
	 * 
	 * @param inputStream the input stream
	 * @param defaultEncoding the default encoding
	 */
	public BOMNewlineEncodingDetector(final InputStream inputStream, String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;

		if (inputStream.markSupported()) {
			this.inputStream = inputStream;
		} else {
			this.inputStream = new BufferedInputStream(inputStream, MAX_LOOKAHEAD+1024);
		}

		inputStream.mark(MAX_LOOKAHEAD);
		autodetected = false;
		bomSize = 0;
	}
	
	public BOMNewlineEncodingDetector(final InputStream inputStream, Charset defaultEncoding) {
		this(inputStream, defaultEncoding.name());
	}

	/**
	 * Static helper method for detecting newline type used in a run of text.
	 * 
	 * @param text
	 *            - text which includes newlines.
	 * @return the detected or guessed {@link NewlineType}
	 */
	public static NewlineType getNewlineType(CharSequence text) {
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == '\n')
				return NewlineType.LF;
			if (ch == '\r')
				return (++i < text.length() && text.charAt(i) == '\n') ? NewlineType.CRLF : NewlineType.CR;
		}

		// must guess the newline type, none detected
		if (System.getProperty("line.separator").equals("\r\n")) {
			return NewlineType.CRLF;
		}
		if (System.getProperty("line.separator").equals("\n")) {
			return NewlineType.LF;
		}
		return NewlineType.CR;

	}

	private void setNewlineType() {
		int c;
		int count = 0;
		Reader reader = null;
		try {
			inputStream.mark(MAX_LOOKAHEAD);
			reader = openReader();
			while ((c = reader.read()) != -1) {
				// passed our buffer size if we didn't find any new lines yet
				// then set the default and warn.
                count += 2;
				if (count > MAX_LOOKAHEAD) {
					LOGGER.debug(
							"Could not find newlines within lookahead buffer. Setting default newline type.");
					break;
				}

				if (c == '\n') {
					newlineType = NewlineType.LF;
					return;
				}
				if (c == '\r') {
					int c2 = reader.read();
					if (c2 == -1) {
						newlineType = NewlineType.CR;
						return;
					}
					else {
						newlineType = ((char) c2 == '\n') ? NewlineType.CRLF : NewlineType.CR;
						return;
					}
				}
			}
		} catch (IOException e) {
			throw new OkapiUnsupportedEncodingException("I/O Error getting newline type", e);
		} finally {
			try {
				inputStream.reset();
			} catch (IOException e) {
				throw new OkapiIOException("Could not reset the input stream to it's start position", e);
			}
		}

		// must guess the newline type, none detected
		if (System.getProperty("line.separator").equals("\r\n")) {
			newlineType = NewlineType.CRLF;
			return;
		}
		if (System.getProperty("line.separator").equals("\n")) {
			newlineType = NewlineType.LF;
			return;
		}

		newlineType = NewlineType.CR;
	}

	/**
	 * Detects newline type using the inputStream itself.
	 * 
	 * @return the detected or guessed {@link NewlineType}
	 */
	public NewlineType getNewlineType() {
		if (newlineType == NewlineType.UNKOWN) {
			setNewlineType();
		}
		return newlineType;
	}

	/**
	 * Get the input stream pased in to the constructor
	 * 
	 * @return the {@link InputStream}
	 */
	public InputStream getInputStream() {
		return inputStream;
	}

	/**
	 * Get the guessed encoding or if encoding couldn't be guessed return the user supplied encoding. If no user
	 * supplied encoding is found use ISO_8859_1.
	 * 
	 * @return the guessed or user supplied encoding.
	 */
	public String getEncoding() {
		return encoding;
	}

	/**
	 * Return a short description of the encoding.
	 * 
	 * @return String containing the specification.
	 */
	public String getEncodingSpecificationInfo() {
		return encodingSpecificationInfo;
	}

	/**
	 * Are we confident of the document encoding?
	 * 
	 * @return true if the encoding is obvious from the BOM or bytes, false if the encoding must be guessed.
	 */
	public boolean isDefinitive() {
		return definitive;
	}

	private Reader openReader() throws UnsupportedEncodingException {
		// encoding==null only if input stream is empty so use an arbitrary encoding.
		if (encoding == null)
			return new InputStreamReader(inputStream, ISO_8859_1);
		if (!Charset.isSupported(encoding))
			throw new UnsupportedEncodingException(encoding + " - " + encodingSpecificationInfo);
		return new InputStreamReader(inputStream, encoding);
	}

	private boolean setEncoding(final String encoding, final String encodingSpecificationInfo) {
		this.encoding = encoding;
		this.encodingSpecificationInfo = encodingSpecificationInfo;
		return true;
	}

	public void detectBom() {
		try {
			detectBomInternal();
			try {
				setNewlineType();
			} catch (OkapiUnsupportedEncodingException e) {
				// skip if encoding not recognized
			}
		} catch (IOException e) {
			throw new OkapiIOException("Error detecting Byte Order Mark (BOM)", e);
		}
	}

	public void detectAndRemoveBom() {
		try {
			detectBomInternal();
			try {
				setNewlineType();
			} catch (OkapiUnsupportedEncodingException e) {
				// skip if encoding not recognized
			}
			if (hasBom()) {
				long skipped = inputStream.skip(getBomSize()); // skip bom bytes
				inputStream.mark(MAX_LOOKAHEAD);
				if (skipped != getBomSize()) {
					throw new IOException("The number of bytes skipped is not equal to the expected BOM size");
				}
			}
		} catch (IOException e) {
			throw new OkapiIOException("Error detecting Byte Order Mark (BOM)", e);
		}
	}

	private boolean detectBomInternal() throws IOException {
		hasUtf8Bom = false;
		hasUtf7Bom = false;
		hasBom = false;

		try {
            inputStream.mark(MAX_LOOKAHEAD);
			final int b1 = inputStream.read();
			if (b1 == -1) {
				// Use default encoding for empty stream, or we get null pointer later
				return setEncoding(defaultEncoding, "empty input stream");
			}
			final int b2 = inputStream.read();
			final int b3 = inputStream.read();
			final int b4 = inputStream.read();

			// Check for Unicode Byte Order Mark:
			if (b1 == 0xEF) {
				if (b2 == 0xBB && b3 == 0xBF) {
					hasUtf8Bom = true;
					hasBom = true;
					autodetected = true;
					bomSize = 3;
					return setEncoding(UTF_8, "UTF-8 Byte Order Mark (EF BB BF)");
				}
			} else if (b1 == 0xFE) {
				if (b2 == 0xFF) {
					hasBom = true;
					autodetected = true;
					bomSize = 2;
					return setEncoding(UTF_16BE, "UTF-16 big-endian Byte Order Mark (FE FF)");
				}
			} else if (b1 == 0xFF) {
				if (b2 == 0xFE) {
					if (b3 == 0 && b4 == 0) {
						hasBom = true;
						autodetected = true;
						bomSize = 4;
						return setEncoding(UTF_32LE, "UTF-32 little-endian Byte Order Mark (FF EE 00 00)");
					}
					hasBom = true;
					autodetected = true;
					bomSize = 2;
					return setEncoding(UTF_16LE, "UTF-16 little-endian Byte Order Mark (FF EE)");
				}
			} else if (b1 == 0) {
				if (b2 == 0 && b3 == 0xFE && b4 == 0xFF) {
					hasBom = true;
					autodetected = true;
					bomSize = 4;
					return setEncoding(UTF_32BE, "UTF-32 big-endian Byte Order Mark (00 00 FE FF)");
				}
			} else if (b1 == 0x0E) {
				if (b2 == 0xFE && b3 == 0xFF) {
					hasBom = true;
					autodetected = true;
					bomSize = 3;
					return setEncoding(SCSU, "SCSU Byte Order Mark (0E FE FF)");
				}
			} else if (b1 == 0x2B) {
				if (b2 == 0x2F && b3 == 0x76) {
					hasUtf7Bom = true;
					hasBom = true;
					autodetected = true;
					bomSize = 3;
					return setEncoding(UTF_7, "UTF-7 Byte Order Mark (2B 2F 76)");
				}
			} else if (b1 == 0xDD) {
				if (b2 == 0x73 && b3 == 0x66 && b4 == 0x73) {
					hasBom = true;
					autodetected = true;
					bomSize = 4;
					return setEncoding(UTF_EBCDIC, "UTF-EBCDIC Byte Order Mark (DD 73 66 73)");
				}
			} else if (b1 == 0xFB) {
				if (b2 == 0xEE && b3 == 0x28) {
					hasBom = true;
					autodetected = true;
					bomSize = 3;
					return setEncoding(BOCU_1, "BOCU-1 Byte Order Mark (FB EE 28)");
				}
			}

			// No Unicode Byte Order Mark found. Have to start guessing.
			definitive = false;
			autodetected = false;
			hasBom = false;
			bomSize = 0;

			LOGGER.debug("BOM not found. Now trying to guess document encoding.");

			/*
			 * The best we can do is to provide an encoding that reflects the correct number and ordering of bytes for
			 * characters in the ASCII range. The result will be one of ISO_8859_1, EBCDIC, UTF_16BE, UTF_16LE, UTF_32BE
			 * or UTF_32LE. Assumes 00 bytes indicate multi-byte encodings rather than the presence of NUL characters or
			 * characters with a code that is a multiple of 0x100.
			 */
			if (b4 == -1) {
				/*
				 * The stream contains between 1 and 3 bytes. This means the document can't possibly specify the
				 * encoding, so make a best guess based on the first 3 bytes. It might be possible to rule out some
				 * encodings based on these bytes, but it is impossible to make a definite determination. The main thing
				 * to determine is whether it is an 8-bit or 16-bit encoding. In order to guess the most likely
				 * encoding, assume that the text contains only ASCII characters, and that any 00 bytes indicate a
				 * 16-bit encoding. The only strictly 8-bit encoding guaranteed to be supported on all java platforms is
				 * ISO-8859-1 (UTF-8 uses a variable number of bytes per character). If no 00 bytes are present it is
				 * safest to assume ISO-8859-1, as this accepts the full range of values 00-FF in every byte.
				 */
				if (b2 == -1 || b3 != -1)
					return setEncoding(ISO_8859_1, "default 8-bit ASCII-compatible encoding (stream 3 bytes long)"); // The
				/*
				 * stream contains exactly 1 or 3 bytes, so assume an 8-bit encoding regardless of whether any 00 bytes
				 * are present. The stream contains exactly 2 bytes.
				 */
				if (b1 == 0)
					return setEncoding(UTF_16BE,
							"default 16-bit BE encoding (byte stream starts with 00, stream 2 bytes long)");
				if (b2 == 0)
					return setEncoding(UTF_16LE,
							"default 16-bit LE encoding (byte stream pattern XX 00, stream 2 bytes long)");
				// No 00 bytes present, assume 8-bit encoding:
				return setEncoding(defaultEncoding, "default encoding: " + defaultEncoding);
			}
			/*
			 * Stream contains at least 4 bytes. The patterns used for documentation are made up of: 0 - zero byte X -
			 * non-zero byte ? - byte value not yet determined
			 */
			if (b1 == 0) {
				// pattern 0???
				if (b2 == 0)
					return setEncoding(UTF_32BE, "default 32-bit BE encoding (byte stream starts with 00 00)"); // pattern
				/*
				 * 00?? most likely indicates UTF-32BE pattern 0X?? Regardless of the final two bytes, assume that the
				 * first two bytes indicate a 16-bit BE encoding. There are many circumstances where this could be an
				 * incorrect assumption, for example: - UTF-16LE encoding with first character U+0100 (or any other
				 * character whose code is a multiple of 100Hex) - any encoding with first character NUL - UTF-32BE
				 * encoding with first character outside of Basic Multilingual Plane (BMP) Checking the final two bytes
				 * might give some clues as to whether any of these other situations are more likely, but none of the
				 * clues will yield less than a 50% chance that the encoding is in fact UTF-16BE as suggested by the
				 * first two bytes.
				 */
				return setEncoding(UTF_16BE, "default 16-bit BE encoding (byte stream starts with 00)"); // >=50%
				/*
				 * chance that encoding is UTF-16BE
				 */
			}
			// pattern X???
			if (b4 == 0) {
				// pattern X??0
				if (b3 == 0)
					return setEncoding(UTF_32LE,
							"default 32-bit LE encoding (byte stream starts with pattern XX ?? 00 00)"); // pattern
				/*
				 * X?00 most likely indicates UTF-32LE pattern X?X0
				 */
				return setEncoding(UTF_16LE, "default 16-bit LE encoding (byte stream stars with pattern XX ?? XX 00)"); // Regardless
				/*
				 * of the second byte, assume the fourth 00 byte indicates UTF-16LE.
				 */
			}
			// pattern X??X
			if (b2 == 0) {
				/*
				 * pattern X0?X Assuming the second 00 byte doesn't indicate a NUL character, and that it is very
				 * unlikely that this is a 32-bit encoding of a character outside of the BMP, we can assume that it
				 * indicates a 16-bit encoding. If the pattern is X00X, there is a 50/50 chance that the encoding is BE
				 * or LE, with one of the characters have a code that is a multiple of 0x100. This should be a very rare
				 * occurrence, and there is no more than a 50% chance that the encoding will be different to that
				 * assumed (UTF-16LE) without checking for this occurrence, so don't bother checking for it. If the
				 * pattern is X0XX, this is likely to indicate a 16-bit LE encoding with the second character > U+00FF.
				 */
				return setEncoding(UTF_16LE, "default 16-bit LE encoding (byte stream starts with pattern XX 00 ?? XX)");
			}
			// pattern XX?X
			if (b3 == 0)
				return setEncoding(UTF_16BE, "default 16-bit BE encoding (byte stream starts with pattern XX XX 00 XX)"); // pattern
			/*
			 * XX0X likely to indicate a 16-bit BE encoding with the first character > U+00FF. pattern XXXX Although it
			 * is still possible that this is a 16-bit encoding with the first two characters > U+00FF Assume the more
			 * likely case of four 8-bit characters <= U+00FF. Check whether it fits some common EBCDIC strings that
			 * might be found at the start of a document:
			 */
			if (b1 == 0x4C) { // first character is EBCDIC '<' (ASCII 'L'),
				// check a
				// couple more characters before assuming EBCDIC
				// encoding:
				if (b2 == 0x6F && b3 == 0xA7 && b4 == 0x94)
					return setEncoding(EBCDIC, "default EBCDIC encoding (<?xml...> detected)"); // first
				/*
				 * four bytes are "<?xm" in EBCDIC
				 */
				if (b2 == 0x5A && b3 == 0xC4 && b4 == 0xD6)
					return setEncoding(EBCDIC, "default EBCDIC encoding (<!DOCTYPE...> detected)"); // first
				/*
				 * four bytes are "<!DO" in EBCDIC
				 */
				if ((b2 & b3 & b4 & 0x80) != 0)
					return setEncoding(EBCDIC, "default EBCDIC-compatible encoding (HTML element detected)"); // all
				/*
				 * of the 3 bytes after the '<' have the high-order bit set, indicating EBCDIC letters such as "<HTM",
				 * or "<htm" although this is not an exhaustive check for EBCDIC, it is safer to assume a more common
				 * preliminary encoding if none of these conditions are met.
				 */
			}

			/*
			 * Now confident that it is not EBCDIC, but some other 8-bit encoding. Most other 8-bit encodings are
			 * compatible with ASCII. Since a document specified encoding requires only ASCII characters, just choose an
			 * arbitrary 8-bit preliminary encoding. UTF-8 is however not a good choice as it is not strictly an 8-bit
			 * encoding. UTF-8 bytes with a value >= 0x80 indicate the presence of a multi-byte character, and there are
			 * many byte values that are illegal. Therefore, choose the only true 8-bit encoding that accepts all byte
			 * values and is guaranteed to be available on all java implementations.
			 */
			return setEncoding(defaultEncoding, "default encoding: " + defaultEncoding);
		} finally {
			inputStream.reset();
		}
	}

	/**
	 * Get the defaultEncoding set by the user.
	 * 
	 * @return String representation of the encoding
	 */
	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	/**
	 * Set the default encoding.
	 * 
	 * @param defaultEncoding default encoding
	 */
	public void setDefaultEncoding(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	/**
	 * Does this document have a byte order mark?
	 * 
	 * @return true if there is a BOM, false otherwise.
	 */
	public boolean hasBom() {
		return hasBom;
	}

	/**
	 * Indicates if the guessed encoding is UTF-8 and this file has a BOM.
	 * 
	 * @return True if the guessed encoding is UTF-8 and this file has a BOM, false otherwise.
	 */
	public boolean hasUtf8Bom() {
		return hasUtf8Bom;
	}

	/**
	 * Does this document have a UTF-7 byte order mark?
	 * 
	 * @return true if there is a BOM, false otherwise.
	 */
	public boolean hasUtf7Bom() {
		return hasUtf7Bom;
	}

	/**
	 * Indicates if the guessed encoding was auto-detected. If not it is the default encoding that was provided.
	 * 
	 * @return True if the guessed encoding was auto-detected, false if not.
	 */
	public boolean isAutodetected() {
		return autodetected;
	}

	/**
	 * Gets the number of bytes used by the Byte-Order-mark in this document.
	 * 
	 * @return The byte size of the BOM in this document.
	 */
	public int getBomSize() {
		return bomSize;
	}

	public boolean hasUtf8Encoding() {
		return getEncoding().equals(BOMNewlineEncodingDetector.UTF_8) ? true : false;
	}
}
