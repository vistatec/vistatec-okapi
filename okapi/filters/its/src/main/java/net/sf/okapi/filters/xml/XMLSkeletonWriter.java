/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xml;

import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

/**
 * Implements ISkeletonWriter for the ITS filters 
 */
public class XMLSkeletonWriter extends GenericSkeletonWriter {

	// Not used: private final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Gets the original content of a TextFragment.
	 * @param tf the TextFragment to process.
	 * @param locToUse locale to output. Use null for the source, or the locale
	 * for the target locales. This is used for referenced content in inline codes.
	 * @param context Context flag: 0=text, 1=skeleton, 2=inline.
	 * @return The string representation of the text unit content.
	 */
	@Override
	public String getContent (TextFragment tf,
		LocaleId locToUse,
		EncoderContext context)
	{
		// Output simple text
		if ( !tf.hasCode() ) {
			if ( encoderManager == null ) {
				if ( layer == null ) {
					return tf.toText();
				}
				else {
					return layer.encode(tf.toText(), context);
				}
			}
			else {
				if ( layer == null ) {
					return encoderManager.encode(tf.toText(), context);
				}
				else {
					return layer.encode(
						encoderManager.encode(tf.toText(), context), context);
				}
			}
		}

		// Output text with in-line codes
		List<Code> codes = tf.getCodes();
		StringBuilder tmp = new StringBuilder();
		String text = tf.getCodedText();
		Code code;
		char ch;
		for ( int i=0; i<text.length(); i++ ) {
			ch = text.charAt(i);
			switch ( ch ) {
			case TextFragment.MARKER_OPENING:
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				tmp.append(expandCodeContent(code, locToUse, context));
				break;
			case TextFragment.MARKER_CLOSING:
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				tmp.append(expandCodeContent(code, locToUse, context));
				break;
			case TextFragment.MARKER_ISOLATED:
				code = codes.get(TextFragment.toIndex(text.charAt(++i)));
				tmp.append(expandCodeContent(code, locToUse, context));
				break;
			default:
				if ( Character.isHighSurrogate(ch) ) {
					int cp = text.codePointAt(i);
					i++; // Skip low-surrogate
					if ( encoderManager == null ) {
						if ( layer == null ) {
							tmp.append(new String(Character.toChars(cp)));
						}
						else {
							tmp.append(layer.encode(cp, context));
						}
					}
					else {
						if ( layer == null ) {
							tmp.append(encoderManager.encode(cp, context));
						}
						else {
							tmp.append(layer.encode(
								encoderManager.encode(cp, context),
								context));
						}
					}
				}
				else { // Non-supplemental case
					if ( encoderManager == null ) {
						if ( layer == null ) {
							tmp.append(ch);
						}
						else {
							tmp.append(layer.encode(ch, context));
						}
					}
					else {
						if ( layer == null ) {
							tmp.append(encoderManager.encode(ch, context));
						}
						else {
							tmp.append(layer.encode(
								encoderManager.encode(ch, context),
								context));
						}
					}
				}
				break;
			}
		}
		return tmp.toString();
	}

	/**
	 * Overrides the default behaviour to force "UTF-16" as declared XML encoding (not "UTF-16LE"
	 * or "UTF-16BE").
	 */
	protected String getPropertyValue (INameable resource,
			String name,
			LocaleId locToUse,
			EncoderContext context) {
		String result = super.getPropertyValue(resource, name, locToUse, context);
		if (!Property.ENCODING.equals(name)) {
			return result;
		}

		if (result != null && result.startsWith("UTF-16")) {
			result = "UTF-16";
		}
		return result;
	}

}
