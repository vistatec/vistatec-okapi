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

package net.sf.okapi.steps.tokenization.common;

import net.sf.okapi.common.LocaleFilter;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ParametersString;
import net.sf.okapi.lib.extra.AbstractParameters;

public class LanguageParameters extends AbstractParameters {

	private LocaleFilter localeFilter;
		
	@Override
	protected void parameters_init() {

	}
	
	@Override
	protected void parameters_load(ParametersString buffer) {
		if (localeFilter == null) return;		
		localeFilter.fromString(buffer.getString("languages"));
	}

	@Override
	protected void parameters_reset() {
		localeFilter = new LocaleFilter();
	}

	@Override
	protected void parameters_save(ParametersString buffer) {
		
		if (localeFilter == null) return;
		buffer.setString("languages", localeFilter.toString());
	}

	public boolean supportsLanguage(LocaleId language) {
		
		if (localeFilter == null) return false;		
		return localeFilter.matches(language);
	}

	public LocaleFilter getLocaleFilter() {
		
		return localeFilter;
	}
	
	public String getLanguages() {
		
		if (localeFilter == null) return "";
		return localeFilter.toString();
	}

	public void setLocaleFilter(LocaleFilter localeFilter) {
		
		this.localeFilter = localeFilter;
	}		
	
	public void setLocaleFilter(String string) {
		
		if (localeFilter == null) return;
		this.localeFilter.fromString(string);
	}
}
