/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.ArrayList;
import java.util.Iterator;

public class FrameRomanCharsetProvider extends CharsetProvider {

	private static final String NAME = "x-FrameRoman";
	
	private final String[] aliases = {"FrameRoman", "MIFRoman"};

	// Zero-argument constructor
    public FrameRomanCharsetProvider() {
    }

    @Override
	public Charset charsetForName (String name) {
		// Check the main name
		if ( name.equalsIgnoreCase(NAME) ) {
			return new FrameRomanCharset(NAME, aliases);
		}
		// Check our aliases
		for ( String aliasName : aliases) {
			if ( name.equalsIgnoreCase(aliasName)) {
				return new FrameRomanCharset(NAME, aliases);
			}
		}
		// Else: Unknown name
		return null;
	}

	@Override
	public Iterator<Charset> charsets () {
		// Create a list with the lone encoding this provider supports
		ArrayList<Charset> list = new ArrayList<Charset>();
		list.add(Charset.forName(NAME));
		// Return the iterator
		return list.iterator();
	}

}
