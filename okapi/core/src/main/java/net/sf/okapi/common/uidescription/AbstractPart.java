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

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import net.sf.okapi.common.IParameterDescriptor;
import net.sf.okapi.common.ParameterDescriptor;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;

/**
 * Base UI part descriptor. All other UI part descriptors are derived
 * from this one. 
 */
public abstract class AbstractPart implements IParameterDescriptor {

	protected ParameterDescriptor paramDescriptor;
	protected IContainerPart container;
	protected boolean vertical = false;
	protected boolean labelFlushed = false;
	protected boolean withLabel = true;
	protected boolean labelNextToInput = false;
	protected AbstractPart masterPart;
	protected boolean enabledOnSelection;

	/**
	 * Creates a new BasePart object with a given parameter description. 
	 * @param paramDescriptor the parameter description for this UI part.
	 */
	public AbstractPart (ParameterDescriptor paramDescriptor) {
		if ( paramDescriptor == null ) {
			throw new NullPointerException("Parameter descriptor cannot be null.");
		}
		this.paramDescriptor = paramDescriptor;
		checkType();
	}

	public String getDisplayName () {
		return paramDescriptor.getDisplayName();
	}

	public String getName () {
		return paramDescriptor.getName();
	}

	public Object getParent () {
		return paramDescriptor.getParent();
	}

	public Method getReadMethod () {
		return paramDescriptor.getReadMethod();
	}

	public String getShortDescription () {
		return paramDescriptor.getShortDescription();
	}

	public Type getType () {
		return paramDescriptor.getType();
	}

	public Method getWriteMethod () {
		return paramDescriptor.getWriteMethod();
	}

	public void setDisplayName (String displayName) {
		paramDescriptor.setDisplayName(displayName);
	}

	public void setShortDescription (String shortDescription) {
		paramDescriptor.setShortDescription(shortDescription);
	}

	/**
	 * Indicates if this UI part should be displayed with a label. When this
	 * option is not set: no label is displayed and the entry field takes the space
	 * where the label would be.
	 * @return true if this UI part should be displayed with a label.
	 */
	public boolean isWithLabel () {
		return withLabel;
	}

	/**
	 * Sets the flag indicating if this UI part should be displayed with a label.
	 * @param withLabel true if this UI part should be displayed with a label.
	 */
	public void setWithLabel(boolean withLabel) {
		this.withLabel = withLabel;
	}

	/**
	 * Indicates if this UI part should be arranged vertically. By default
	 * it is not.
	 * @return true if this UI part should be arranged vertically.
	 */
	public boolean isVertical () {
		return vertical;
	}

	/**
	 * Sets the flag indicating if this UI part should be arranged vertically.
	 * @param vertical the new flag indicating if this UI part should be arranged vertically.
	 */
	public void setVertical (boolean vertical) {
		this.vertical = vertical;
	}

	/**
	 * Indicates if the label of this UI part should be flushed next to the part. 
	 * @return true if the label of this UI part should be flushed next to the part.
	 */
	public boolean isLabelFlushed () {
		return labelFlushed;
	}

	/**
	 * Sets the flag indicating if the label of this UI part should be flushed next to the part.
	 * @param labelFlushed the new flag indicating if the label of this UI part should be flushed next to the part.
	 */
	public void setLabelFlushed (boolean labelFlushed) {
		this.labelFlushed = labelFlushed;
	}

	/**
	 * Gets the container part of this part.
	 * @return the container part of this part or null.
	 */
	public IContainerPart getContainer () {
		return container;
	}

	/**
	 * Sets the container part for this part.
	 * @param container the new container part for this part.
	 */
	public void setContainer (IContainerPart container) {
		this.container = container;
	}
	
	/**
	 * Binds this part to a master part. Use this to make this part be enabled
	 * or disabled depending on the specified master part. 
	 * @param masterPart the UI part that enables or disables this part.
	 * @param enabledOnSelection true if this part is enabled only when the master
	 * part is selected, false to enable this part when the master part is not selected.
	 */
	public void setMasterPart (AbstractPart masterPart,
		boolean enabledOnSelection)
	{
		this.masterPart = masterPart;
		this.enabledOnSelection = enabledOnSelection;
	}
	
	/**
	 * Gets the master part associated with this UI part.
	 * @return the master part associated with this UI part, or null if there
	 * is none.
	 */
	public AbstractPart getMasterPart () {
		return masterPart;
	}
	
	/**
	 * Indicates how this part is enabled in relation to its master part.
	 * @return true if this part is enabled only when the master
	 * part is selected, false to enable this part when the master part is not selected.
	 */
	public boolean isEnabledOnSelection () {
		return enabledOnSelection;
	}

	/**
	 * Indicates that the label for this part is to be displayed next to the input field.
	 * @return true if the label for this part is to be displayed next to the input field.
	 */
	public boolean isLabelNextToInput () {
		return labelNextToInput;
	}

	/**
	 * Sets the indicator that tells if the label for this part is to be displayed next to the input field.
	 * @param labelNextToInput true to set the indicator so the label for this part is to be displayed next to the input field.
	 */
	public void setLabelNextToInput (boolean labelNextToInput) {
		this.labelNextToInput = labelNextToInput;
	}

	/**
	 * Checks the types supported by this UI part.
	 * @throws OkapiNotImplementedException if this UI part does not support
	 * the type of the parameter description it is created with.
	 */
	protected void checkType () {
		throw new OkapiNotImplementedException("Unsupported type for this UI part.");
	}

}
