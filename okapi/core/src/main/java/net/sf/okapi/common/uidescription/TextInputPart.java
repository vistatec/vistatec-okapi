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

package net.sf.okapi.common.uidescription;

import net.sf.okapi.common.ParameterDescriptor;

/**
 * UI part descriptor for a text input field. This UI part supports the following
 * types: Integer and String.
 * <p>Use {@link #setAllowEmpty(boolean)} to specify if the text can be empty. by
 * default empty text is not allowed.
 * <p>Use {@link #setPassword(boolean)} to specify if the text should be treated
 * as a password text (e.g. hidden on input). By default the text is not treated
 * as a password.
 * <p>Use {@link #setHeight(int)} to specify a height for the field (-1 sets the default).
 */
public class TextInputPart extends AbstractPart {

	private boolean allowEmpty = false;
	private boolean password = false;
	private int minimumValue = Integer.MIN_VALUE;
	private int maximumValue = Integer.MAX_VALUE;
	private int height = -1;
	
	/**
	 * Creates a new TextInputPart object with a given parameter descriptor.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 */
	public TextInputPart (ParameterDescriptor paramDescriptor) {
		super(paramDescriptor);
	}
	
	@Override
	protected void checkType () {
		// Check type support
		if ( getType().equals(String.class) ) return;
		if ( getType().equals(int.class) ) return;
		// Otherwise: call the base method.
		super.checkType();
	}

	/**
	 * Indicates if the input text can be empty.
	 * @return true if the input text can be empty, false otherwise.
	 */
	public boolean isAllowEmpty () {
		return allowEmpty;
	}

	/**
	 * Sets the flag indicating if the input text can be empty. 
	 * @param allowEmpty true if the input text can be empty, false otherwise.
	 */
	public void setAllowEmpty (boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}

	/**
	 * Indicates if the input text should be treated like a password.
	 * @return true if the input text can be should be treated like a password.
	 */
	public boolean isPassword () {
		return password;
	}

	/**
	 * Sets the flag indicating if the input text should be treated like a password. 
	 * @param password true if the input text should be treated like a password, false otherwise.
	 */
	public void setPassword (boolean password) {
		this.password = password;
	}

	/**
	 * Gets the minimum value allowed (for integer input).
	 * @return the minimum value allowed.
	 */
	public int getMinimumValue () {
		return minimumValue;
	}
	
	/**
	 * Gets the maximum value allowed (for integer input).
	 * @return the maximum value allowed.
	 */
	public int getMaximumValue () {
		return maximumValue;
	}
	
	/**
	 * Sets the minimum and maximum values allowed (for integer input).
	 * If the values are lesser or greater than the minimum and maximum
	 * values allowed by an Integer, they are reset to those values.
	 * If the maximum is less than the minimum it is reset to the minimum.
	 * @param minimumValue the minimum value allowed.
	 * @param maximumValue the maximum value allowed.
	 */
	public void setRange (int minimumValue,
		int maximumValue)
	{
		if ( minimumValue < Integer.MIN_VALUE ) {
			minimumValue = Integer.MIN_VALUE;
		}
		this.minimumValue = minimumValue;
		
		if ( maximumValue < minimumValue ) {
			maximumValue = minimumValue;
		}
		if ( maximumValue > Integer.MAX_VALUE ) {
			maximumValue = Integer.MAX_VALUE;
		}
		this.maximumValue = maximumValue;
	}

	/**
	 * Gets the suggested height of this part (-1 for default)
	 * @return the suggested height of this part or -1 for default.
	 */
	public int getHeight () {
		return height;
	}

	/**
	 * Sets the suggested height of this part (-1 for default)
	 * @param height the suggested height of this part (use -1 for default)
	 */
	public void setHeight (int height) {
		this.height = height;
	}

}
