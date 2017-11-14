/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

import net.sf.okapi.common.filters.InlineCodeFinder;

/**
 * Parameters implementation based on {@link ParametersString}.
 * <p>
 * Subclasses that wish to expose individual parameters via dedicated
 * getters and setters should rely on the internal param buffer as much
 * as possible.  Any default values should be set in the implementation
 * of {@link #reset()}. 
 */
public class StringParameters extends BaseParameters {
	/**
	 * Buffer where the parameters are stored during conversion.
	 */
	protected ParametersString buffer;

	/**
	 * Creates a new StringParameters object with a null path and an empty buffer.
	 */
	public StringParameters () {
		super();
		buffer = new ParametersString();
		reset(); // Set default values
	}

	/**
	 * Creates a new StringParameters object with a null path the specified
	 * initial parameter data
	 * @param data the string holding the parameters. See {@link IParameters#fromString}.
	 */
	public StringParameters (String data) {
		super();
		buffer = new ParametersString();
		fromString(data);
	}
	
	/**
	 * Reset this parameters object to its default values.  
	 * <p>
	 * Subclasses should override this method to set any initial
	 * values and instantiate any objects that require allocation. It 
	 * is recommended that subclasses also call <code>super.reset()</code>
	 * in the override to ensure that the buffer is empty.
	 */
	@Override
	public void reset () {
		buffer.reset();
	}

	/**
	 * Reset this parameters object to its default values and then load
	 * additional parameters from the provided data.
	 * <p>
	 * Subclasses should not normally need to override this method unless
	 * they are maintaining complex values (eg, {@link InlineCodeFinder})
	 * that require extra initialization as part of their parameter state.
	 */
	@Override
	public void fromString (String data) {
		buffer.reset(); // Clear buffer
		reset(); // Set default values
		buffer.fromString(data, false);
	}

	/**
	 * Load additional parameters from the provided data.
	 * If clearParameters is set, then it first resets this parameters object to its default values.
	 * <p>
	 * Subclasses should not normally need to override this method unless
	 * they are maintaining complex values (eg, {@link InlineCodeFinder})
	 * that require extra initialization as part of their parameter state.
	 * @param data the string holding the parameters. See {@link IParameters#fromString}.
	 * @param clearParameters if true, the internal parameters will be cleared before
	 *        loading the data. If false, any existing parameters will be left intact
	 *        unless they are overridden by the data.
	 */
	public void fromString (String data, boolean clearParameters) {
		buffer.fromString(data, clearParameters);
	}

	/**
	 * Serialize this parameters object to a string.
	 * <p>
	 * Subclasses should not normally need to override this method unless
	 * they are maintaining complex values (eg, {@link InlineCodeFinder})
	 * that require extra serialization as part of their parameter state.
	 */
	@Override
	public String toString () {
		return buffer.toString();
	}
	
	@Override
	public boolean getBoolean (String name) {
		return buffer.getBoolean(name);
	}
	
	@Override
	public void setBoolean (String name,
		boolean value)
	{
		buffer.setBoolean(name, value);
	}
	
	@Override
	public String getString (String name) {
		return buffer.getString(name);
	}
	
	@Override
	public void setString (String name,
		String value)
	{
		buffer.setString(name, value);
	}

	@Override
	public int getInteger (String name) {
		return buffer.getInteger(name);
	}

	@Override
	public void setInteger (String name,
		int value)
	{
		buffer.setInteger(name, value);
	}

	protected String getGroup(String name) {
		return buffer.getGroup(name);
	}
	
	protected void setGroup(String name, String value) {
		buffer.setGroup(name, value);
	}
}
