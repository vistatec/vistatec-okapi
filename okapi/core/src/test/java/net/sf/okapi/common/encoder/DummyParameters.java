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

package net.sf.okapi.common.encoder;

import java.io.InputStream;
import java.net.URL;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ParametersDescription;

public class DummyParameters implements IParameters {

	private boolean escapeExtendedChars;
	private boolean escapeGt;
	private boolean escapeNbsp;

	public void setBoolean (String name,
		boolean value)
	{
		if ( name.equals("escapeExtendedChars") )
			escapeExtendedChars = value;
		if ( name.equals(XMLEncoder.ESCAPEGT) )
			escapeGt = value;
		if ( name.equals(XMLEncoder.ESCAPENBSP) )
			escapeNbsp = value;
	}
	
	public void fromString (String data) {
		// Not needed for tests
	}

	public boolean getBoolean (String name) {
		if ( name.equals("escapeExtendedChars") )
			return escapeExtendedChars;
		if ( name.equals(XMLEncoder.ESCAPEGT) )
			return escapeGt;
		if ( name.equals(XMLEncoder.ESCAPENBSP) )
			return escapeNbsp;
		return false;
	}

	public String getPath () {
		// Not needed for tests
		return null;
	}
	
	public void setPath (String filePath) {
		// Not needed for tests
	}

	public String getString (String name) {
		// Not needed for tests
		return null;
	}

	public void load (URL inputURL, boolean ignoreErrors) {
		// Not needed for tests
	}
	
	@Override
	public void load(InputStream inStream, boolean ignoreErrors) {
		// Not needed for tests		
	}

	public void reset () {
		// Not needed for tests
	}

	public void save (String filePath) {
		// Not needed for tests
	}

	public int getInteger(String name) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public ParametersDescription getParametersDescription () {
		return null;
	}

	@Override
	public void setInteger (String name, int value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setString (String name, String value) {
		// TODO Auto-generated method stub
	}

}
