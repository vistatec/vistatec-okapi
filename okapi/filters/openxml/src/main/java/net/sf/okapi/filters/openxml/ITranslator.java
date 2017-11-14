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
package net.sf.okapi.filters.openxml;

import net.sf.okapi.common.resource.TextFragment;

import org.slf4j.Logger;

/**
 * This is used for testing, allowing automatic manipulation
 * of text in a text unit that will be available for translation. 
 */
public interface ITranslator
{
	public String translate(TextFragment tf, Logger lgr, ParseType nFileType);
}
