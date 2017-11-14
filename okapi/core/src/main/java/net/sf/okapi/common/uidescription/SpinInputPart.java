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

package net.sf.okapi.common.uidescription;

import net.sf.okapi.common.ParameterDescriptor;

/**
 * UI part descriptor for a spin-like input field. This UI part supports the following
 * types: Integer.
 */
public class SpinInputPart extends AbstractPart {

	private int minimumValue = Integer.MIN_VALUE;
	private int maximumValue = Integer.MAX_VALUE;
	
	/**
	 * Creates a new TextInputPart object with a given parameter descriptor.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 */
	public SpinInputPart (ParameterDescriptor paramDescriptor) {
		super(paramDescriptor);
	}
	
	@Override
	protected void checkType () {
		// Check type support
		if ( getType().equals(int.class) ) return;
		// Otherwise: call the base method.
		super.checkType();
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
	 * Sets the minimum and maximum values allowed.
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

}
