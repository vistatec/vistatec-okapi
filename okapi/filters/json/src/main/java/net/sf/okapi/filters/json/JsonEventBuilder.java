/*===========================================================================
  Copyright (C) 2009-2014 by the Okapi Framework contributors
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

package net.sf.okapi.filters.json;

import net.sf.okapi.common.filters.EventBuilder;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonEventBuilder extends EventBuilder {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private InlineCodeFinder codeFinder;
	private boolean escapeForwardSlashes = true;

	public JsonEventBuilder(String rootId, IFilter subFilter) {
		super(rootId, subFilter);
		codeFinder = null;
		// Get the output options
		if (subFilter.getParameters() != null ) {
			escapeForwardSlashes = subFilter.getParameters().getBoolean("escapeForwardSlashes");
		}
	}

	@Override
	protected ITextUnit postProcessTextUnit(ITextUnit textUnit) {
		TextFragment text = textUnit.getSource().getFirstContent();
		String unescaped = unescape(text);
		text.setCodedText(unescaped);

		if ( codeFinder != null ) {
			codeFinder.process(text);
		}
		return textUnit;
	}

	public String decode(String value) {
		return unescape(new TextFragment(value));
	}

	private String unescape(TextFragment text) {
		StringBuilder unescaped = new StringBuilder();
		char ch;
		for (int i = 0; i < text.length(); i++) {
			ch = text.charAt(i);
			switch(ch) {
				case '\\':
					break;
				default:
					unescaped.append(ch);
					continue;
			}

			// previous char was '\'
			ch = text.charAt(++i);
			switch (ch) {
				case 'b':
					unescaped.append('\b');
					break;
				case 'f':
					unescaped.append('\f');
					break;
				case 'n':
					unescaped.append('\n');
					break;
				case 'r':
					unescaped.append('\r');
					break;
				case 't':
					unescaped.append('\t');
					break;
				case '\\':
				case '"':
					unescaped.append(ch);
					break;
				case '/':
					if (escapeForwardSlashes)
						unescaped.append(ch);
					break;
				default: // Unexpected escape sequence
					logger.warn("Unexpected Json escape sequence '\\{}'.", ch);
					unescaped.append('\\');
					unescaped.append(ch);
					break;
			}
		}

		return unescaped.toString();
	}

	public void setCodeFinder(InlineCodeFinder codeFinder) {
		this.codeFinder = codeFinder;
	}
}
