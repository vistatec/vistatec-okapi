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

import java.nio.charset.CharsetEncoder;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public class MIFFilterWriter extends GenericFilterWriter {

	public MIFFilterWriter (ISkeletonWriter skelWriter,
		EncoderManager encoderManager)
	{
		super(skelWriter, encoderManager);
	}

	@Override
	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		// Force the encoding
		if ( !Util.isEmpty(defaultEncoding) && defaultEncoding.startsWith("UTF-16") ) {
			super.setOptions(locale, defaultEncoding);
		}
		else {
			// Null encoding should make the writer get it from the start-document info
			super.setOptions(locale, null);
		}
	}

	@Override
	protected CharsetEncoder createCharsetEncoder (String encodingtoUse) {
		// Special case for FrameRoman
		if ( encodingtoUse.equals(MIFFilter.FRAMEROMAN) ) {
			return new FrameRomanCharsetProvider().charsetForName(encodingtoUse).newEncoder();
		}
		// else: normal return
		return null; // Use default otherwise
	}
	
}
