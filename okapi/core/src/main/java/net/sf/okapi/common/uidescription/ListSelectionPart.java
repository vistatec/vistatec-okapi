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
 * UI part descriptor for a string selection. This UI part supports the following
 * types: String and int.
 * <p>Use {@link #setListType(int)} to specify the type of list the UI should use. By default
 * a simple list box will be used.
 */
public class ListSelectionPart extends AbstractPart {
	
	public static final int LISTTYPE_SIMPLE = 0;
	public static final int LISTTYPE_DROPDOWN = 1;

	private String[] choicesValues;
	private int listType = LISTTYPE_SIMPLE;
	private String[] choicesLabels;
	
	/**
	 * Creates a new ListSelectionPart object with a given parameter descriptor.
	 * @param paramDescriptor the parameter descriptor for this UI part.
	 * @param choicesValues the list of the items that can be selected. When the type of the parameter
	 * is an int, the list of values must be defined. 
	 */
	public ListSelectionPart (ParameterDescriptor paramDescriptor,
		String[] choicesValues)
	{
		super(paramDescriptor);
		setChoicesValues(choicesValues);
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
	 * Gets the list of items that can be selected.
	 * @return the list of items that can be selected.
	 */
	public String[] getChoicesValues () {
		return choicesValues;
	}

	/**
	 * Sets the list of items that can be selected.
	 * @param choicesValues the new list of items that can be selected.
	 */
	public void setChoicesValues (String[] choicesValues) {
		this.choicesValues = choicesValues;
	}

	/**
	 * Gets the type of list this UI part should use.
	 * @return the type of list this UI part should use.
	 */
	public int getListType () {
		return listType;
	}

	/**
	 * Sets the type of list this UI part should use.
	 * <p>The possible values are:
	 * <ul><li>{@link ListSelectionPart#LISTTYPE_SIMPLE} for a a simple list</li>
	 * <li> {@link ListSelectionPart#LISTTYPE_DROPDOWN} for a drop-down list</li></ul>
	 * @param listType the new type of list this UI part should use.
	 */
	public void setListType (int listType) {
		this.listType = listType;
	}

	/**
	 * Gets the list of the localizable labels to use with the selectable values.
	 * @return the list of the localizable labels to use with the selectable values.
	 */
	public String[] getChoicesLabels () {
		return choicesLabels;
	}

	/**
	 * Sets the list of the localizable labels to use with the selectable values. If this list
	 * is not set, the values themselves will be used for display.
	 * @param choicesLabels the list of the localizable labels to use with the selectable values.
	 */
	public void setChoicesLabels (String[] choicesLabels) {
		this.choicesLabels = choicesLabels;
	}

}
