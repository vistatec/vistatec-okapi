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

package net.sf.okapi.lib.tkit.writer;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.XLIFFWriter;

/**
 * Extends the implementation of {@link XLIFFWriter} for XLIFF document
 * using a skeleton file made of serialized events.
 */
public class XLIFFAndSkeletonWriter extends XLIFFWriter {

	BeanEventWriter skeleton;
	
	public XLIFFAndSkeletonWriter () {
		super();
		skeleton = new BeanEventWriter();
	}
	
	@Override
	public Event handleEvent (Event event) {
		if ( event.isStartDocument() ) {
			// Remove the target only for monolingual formats
			boolean multiLing = event.getStartDocument().isMultilingual();
			((net.sf.okapi.lib.tkit.writer.Parameters)skeleton.getParameters()).setRemoveTarget(!multiLing);
		}
		// Call the skeleton first to avoid side-effects
		// like super.handleEvent(event) calling close() on END_DOCUEMT before the skeleton is done
		// Note that we need to use the returned value because the skeleton writer may change the TU
		// (like strip out the targets)
		event = skeleton.handleEvent(event);
		return super.handleEvent(event); 
	}

	@Override
	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		super.setOptions(locale, defaultEncoding);
		skeleton.setOptions(locale, null); // Encoding is not used in skeleton
	}
	
	/**
	 * Sets the output path of both the XLIFF document and the skeleton file.
	 * The path of the skeleton file will be the same as the one specified for the XLIFF document,
	 * but with its extension replaced (or added) by '.skl'.
	 * Use {@link #setSkeletonOutput(String)} (after calling this method) to specify a different path
	 * for the skeleton file. 
	 * @param path the path of the XLIFF document.
	 */
	@Override
	public void setOutput (String path) {
		super.setOutput(path);
		int pos = path.lastIndexOf('.');
		if ( pos > -1 ) path = path.substring(0, pos);
		path += ".skl";
		skeleton.setOutput(path);
	}
	
	/**
	 * Sets the path of the skeleton file if it needs to be something else than the default
	 * set when calling {@link #setOutput(String)}.
	 * @param path the path to use for the skeleton file.
	 */
	public void setSkeletonOutput (String path) {
		skeleton.setOutput(path);
	}
	
	@Override
	public void close () {
		super.close();
		if ( skeleton != null ) {
			skeleton.close();
		}
	}

}
