/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.encoder.IEncoder;

/**
 * Implements {@link IEncoder} for Adobe FrameMaker MIF format
 * when the text is inside a marker string (that cannot be using &lt;Char xyz&gt;).
 */
public class MIFStringEncoder extends MIFEncoder {

	/**
	 * Tries to convert an escaped hexadecimal character/byte into a corresponding Unicode value or inline code.
	 * <p>Inline code are bracketed between {@link #ILC_START} and {@link #ILC_END} characters.
	 * @param hexaValue the value to convert.
	 * @return the string with the conversion, or null if the value could not be mapped.
	 */
	static String convertCtrl (int hexaValue) {
		switch ( hexaValue ) {
		case 0x04: return "\u00ad"; // Discretionary hyphen
		case 0x05: return MIFFilter.ILC_START+"\\x05 "+MIFFilter.ILC_END; // Suppress hyphenation
		case 0x06: return MIFFilter.ILC_START+"\\x06 "+MIFFilter.ILC_END; // Automatic hyphen
		case 0x08: return "\u0007"; // Tab
		case 0x09: return MIFFilter.ILC_START+"\\x09 "+MIFFilter.ILC_END; // Forced return/line-break
		case 0x0a: return MIFFilter.ILC_START+"\\x0a "+MIFFilter.ILC_END; // End of paragraph
		case 0x0b: return MIFFilter.ILC_START+"\\x0b "+MIFFilter.ILC_END; // End of flow
		case 0x10: return "\u2007"; // Numeric space
		case 0x11: return "\u00a0"; // Non-breaking space
		case 0x12: return "\u2009"; // Thin space
		case 0x13: return "\u2002"; // En space
		case 0x14: return "\u2003"; // Em space
		case 0x15: return "\u2011"; // Non-breaking/hard hyphen
		}
		return null;
	}

	@Override
	protected String tryCharStatment (int value) {
		switch ( value ) {
		case '\t': return "\\x08 ";
		case '\u00a0': return "\\x11 "; // Non-breaking space
		case '\u2011': return "\\x15 "; // Non-breaking/hard hyphen
		case '\u00ad': return "\\x04 "; // Discretionary hyphen
		case '\u2007': return "\\x10 "; // Numeric space
		case '\u2009': return "\\x12 "; // Thin space
		case '\u2002': return "\\x13 "; // En space
		case '\u2003': return "\\x14 "; // Em space
		}
		return null;
	}

}
