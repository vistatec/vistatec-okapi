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

package net.sf.okapi.lib.verification;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.resource.RawDocument;

public interface IQualityCheckEditor {

	/**
	 * Initializes this IQualityCheckEditor object.
	 * @param parent the object representing the parent window/shell for this editor.
	 * The type of this parameter depends on the implementation.
	 * @param asDialog true if used from another program.
	 * @param helpParam the help engine to use.
	 * @param fcMapper the IFilterConfigurationMapper object to use with the editor.
	 * @param session an optional session to use (null to use one created internally)
	 */
	public void initialize (Object parent,
		boolean asDialog,
		IHelp helpParam,
		IFilterConfigurationMapper fcMapper,
		QualityCheckSession session);

	/**
	 * Adds a raw document to the session. If this is the
	 * first document added to the session, the locales of the session are automatically
	 * set to the source and target locale of this document.
	 * This method can be called without the UI being setup yet.
	 * @param rawDoc the raw document to add (it must have an input URI and its
	 * source and target locale set).
	 */
	public void addRawDocument (RawDocument rawDoc);
	
	/**
	 * Gets the session associated with this editor. You want to call this method
	 * only after {@link #initialize(Object, boolean, IHelp, IFilterConfigurationMapper, QualityCheckSession)}
	 * has been called.
	 * @return the session associated with this editor.
	 */
	public QualityCheckSession getSession ();
	
	/**
	 * Runs an editing session with this IQualityCheckEditor object.
	 * You must have called {@link #initialize(Object, boolean, IHelp, IFilterConfigurationMapper, QualityCheckSession)}
	 * once before calling this method.
	 * @param processOnStart true to trigger the verification process when the editor is opened.
	 */
	public void edit (boolean processOnStart);

}

