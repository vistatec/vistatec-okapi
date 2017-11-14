/*===========================================================================
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

package net.sf.okapi.filters.plaintext.base;

import net.sf.okapi.common.ParametersString;
import net.sf.okapi.common.filters.InlineCodeFinder;
import net.sf.okapi.lib.extra.AbstractParameters;
import net.sf.okapi.lib.extra.filters.WrapMode;

/**
 * Base Plain Text Filter parameters
 * 
 * @version 0.1, 09.06.2009
 */

public class Parameters extends AbstractParameters {
	
	public boolean unescapeSource;
	public boolean trimLeading;
	public boolean trimTrailing;
	public boolean preserveWS;
	public boolean useCodeFinder;
	public String codeFinderRules;	
	public WrapMode wrapMode;
	private InlineCodeFinder codeFinder;
	public String subfilter;
	
//----------------------------------------------------------------------------------------------------------------------------
	
	@Override
	protected void parameters_init() {
		
	}

	@Override
	protected void parameters_load(ParametersString buffer) {
		unescapeSource = buffer.getBoolean("unescapeSource", true);
		trimLeading = buffer.getBoolean("trimLeading", false);
		trimTrailing = buffer.getBoolean("trimTrailing", false);
		preserveWS = buffer.getBoolean("preserveWS", true);
		useCodeFinder = buffer.getBoolean("useCodeFinder", false);
		codeFinderRules = buffer.getString("codeFinderRules", codeFinder.toString());
		wrapMode = WrapMode.values()[buffer.getInteger("wrapMode", WrapMode.NONE.ordinal())];
		subfilter = buffer.getString("subfilter", subfilter);
	}

	@Override
	protected void parameters_reset() {
		
		unescapeSource = true;
		trimLeading = false;
		trimTrailing = false;
		preserveWS = true;
		useCodeFinder = false;
		
		// Default in-line codes: special escaped-chars and printf-style variable
		codeFinder = new InlineCodeFinder();
		
		// Default in-line codes: special escaped-chars and printf-style variable
		codeFinder.addRule("%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]");
		codeFinder.addRule("(\\\\r\\\\n)|\\\\a|\\\\b|\\\\f|\\\\n|\\\\r|\\\\t|\\\\v");
		
		codeFinderRules = codeFinder.toString();
			
		wrapMode = WrapMode.NONE;
		subfilter = null;
	}

	@Override
	protected void parameters_save(ParametersString buffer) {		
		buffer.setBoolean("unescapeSource", unescapeSource);
		buffer.setBoolean("trimLeading", trimLeading);
		buffer.setBoolean("trimTrailing", trimTrailing);
		buffer.setBoolean("preserveWS", preserveWS);
		buffer.setBoolean("useCodeFinder", useCodeFinder);
		buffer.setString("codeFinderRules", codeFinderRules);
		buffer.setInteger("wrapMode", wrapMode.ordinal());
		buffer.setString("subfilter", subfilter);
	}
}
