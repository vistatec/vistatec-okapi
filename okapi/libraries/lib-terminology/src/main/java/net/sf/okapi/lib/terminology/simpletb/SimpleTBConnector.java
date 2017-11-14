/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.terminology.simpletb;

import java.io.File;
import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.terminology.ITermAccess;
import net.sf.okapi.lib.terminology.TermHit;

public class SimpleTBConnector implements ITermAccess {

	private Parameters params;
	private SimpleTB tb;
	
	public SimpleTBConnector () {
		params = new Parameters();
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public void open () {
		tb = new SimpleTB(params.getSourceLocale(), params.getTargetLocale());
		// Import from file, if a path is defined
		if ( !Util.isEmpty(params.getGlossaryPath()) ) {
			tb.importTBX(new File(params.getGlossaryPath()));
		}
	}

	@Override
	public void close() {
		tb.removeAll();
	}

	@Override
	public List<TermHit> getExistingTerms (TextFragment fragment,
		LocaleId fragmentLocId,
		LocaleId otherLocId)
	{
		return tb.getExistingTerms(fragment, fragmentLocId, otherLocId);
	}
	
	@Override
	public List<TermHit> getExistingStrings (TextFragment fragment,
		LocaleId fragmentLocId,
		LocaleId otherLocId)
	{
		return tb.getExistingStrings(fragment, fragmentLocId, otherLocId);
	}
	
	public void initializeSearch(boolean stringSearch,
		boolean betweenCodes)
	{
		tb.initialize(stringSearch, betweenCodes);
	}
	
	public Entry addEntry (String srcTerm,
		String trgTerm)
	{
		return tb.addEntry(srcTerm, trgTerm);
	}

}
