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

package net.sf.okapi.lib.terminology;

import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.TextFragment;

public interface ITermAccess {

	/**
	 * Sets the parameters for this termbase connector.
	 * @param params the new parameter
	 */
	public void setParameters (IParameters params);
	
	/**
	 * Gets the current parameters for this termbase connector.
	 * @return the current parameters for this termbase connector.
	 */
	public IParameters getParameters ();

	/**
	 * Opens the connection to the termbase.
	 * You may need to call {@link #setParameters(IParameters)} before this method.
	 */
	public void open ();
	
	/**
	 * Closes the connection to the termbase. 
	 */
	public void close ();
	
	/**
	 * Gets the list of all terms of the termbase that exist in a given fragment
	 * for a given source/target pair of locales. 
	 * @param fragment the fragment to examine.
	 * @param fragmentLoc the locale of the fragment.
	 * @param otherLoc the other (source or target) locale.
	 * @return the list of all terms of the termbase that exist in the given fragment.
	 */
	public List<TermHit> getExistingTerms (TextFragment fragment,
		LocaleId fragmentLoc,
		LocaleId otherLoc);

	public List<TermHit> getExistingStrings (TextFragment fragment,
		LocaleId fragmentLoc,
		LocaleId otherLoc);

}
