/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.filters.po;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.okapi.common.LocaleId;

public class PluralForms {
	
	private static final String BUNDLE_NAME = "net.sf.okapi.filters.po.PluralForms";
	private static final String DEFAULT_EXP = "nplurals=2; plural=(n!=1);";

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
		.getBundle(BUNDLE_NAME);

	private PluralForms() {
	}

	public static String getExpression (LocaleId language) {
		try {
			return RESOURCE_BUNDLE.getString(language.toString());
		}
		catch ( MissingResourceException e ) {
			return DEFAULT_EXP; // Default fall-back
		}
	}

	public static int getNumber (String langCode) {
		String tmp;
		try {
			tmp = RESOURCE_BUNDLE.getString(langCode.toLowerCase());
		}
		catch ( MissingResourceException e ) {
			tmp = DEFAULT_EXP; // Default fall-back
		}
		int n1 = tmp.indexOf('=');
		int n2 = tmp.indexOf(';', n1);
		return Integer.valueOf(tmp.substring(n1+1, n2));
	}
}
