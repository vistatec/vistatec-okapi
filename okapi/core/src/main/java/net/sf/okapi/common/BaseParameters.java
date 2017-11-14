/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;

import net.sf.okapi.common.exceptions.OkapiException;

/**
 * Base class for properties-like parameters that implement IParameters.
 */
public abstract class BaseParameters implements IParameters {

	/**
	 * Current path of the parameter file.
	 */
	protected String path;
	
	/**
	 * Creates a new BaseParameters object with a null path.
	 */
	public BaseParameters () {
		path = null;
	}

	@Override
	public String getPath () {
		return path;
	}

	@Override
	public void setPath (String filePath) {
		path = filePath;
	}
	
	@Override
	public void load (URL inputURL,
		boolean ignoreErrors)
	{
		try {
			load (inputURL.openStream(), ignoreErrors);
			path = inputURL.toURI().getPath();
		}
		catch ( IOException | URISyntaxException e) {
			if ( !ignoreErrors ) throw new OkapiException(e);
		}
	}
	
	@Override
	public void load(InputStream inStream, boolean ignoreErrors) {
		try (Reader SR = new InputStreamReader(
				new BufferedInputStream(inStream), "UTF-8")) {
			// Reset the parameters to their defaults
			reset();

			// Read the file in one string
			StringBuilder sbTmp = new StringBuilder(1024);
			char[] aBuf = new char[1024];
			int nCount;
			while ((nCount = SR.read(aBuf)) > -1) {
				sbTmp.append(aBuf, 0, nCount);	
			}

			// Parse it
			String tmp = sbTmp.toString().replace("\r\n", "\n");
			fromString(tmp.replace("\r", "\n"));
			path = "";
		}
		catch ( IOException e ) {
			if ( !ignoreErrors ) throw new OkapiException(e);
		}
	}

	@Override
	public void save (String newPath) {
		try (Writer SW = new OutputStreamWriter(
				new BufferedOutputStream(new FileOutputStream(newPath)),
				"UTF-8")) {
			// Save the fields on file
			SW.write(toString());
			path = newPath;
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
	}

	@Override
	public ParametersDescription getParametersDescription () {
		return null;
	}

}
