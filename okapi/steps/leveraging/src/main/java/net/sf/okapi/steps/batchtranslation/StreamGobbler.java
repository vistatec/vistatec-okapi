/*===========================================================================
  Copyright (C) 2009-2014 by the Okapi Framework contributors
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

package net.sf.okapi.steps.batchtranslation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import net.sf.okapi.common.exceptions.OkapiException;

public class StreamGobbler extends Thread {
	
	private InputStream inStream;

	public StreamGobbler (InputStream inStream,
		String display)
	{
		this.inStream = inStream;
	}

	@Override
	public void run () {
		try {
			InputStreamReader isr = new InputStreamReader(inStream);
			BufferedReader br = new BufferedReader(isr);
			// We cannot use the log because the thread is different 
			// FIXME: We should be able to log from any thread
			while ( br.readLine() != null ) {}
		}
		catch ( IOException e ) {
			throw new OkapiException(e);  
		}
	}
}
