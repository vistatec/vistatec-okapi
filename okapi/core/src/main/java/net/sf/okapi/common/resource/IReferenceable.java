/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

/**
 * Interface for all resources that can be passed as referents through the filter events.
 */
public interface IReferenceable {

	/**
	 * Sets the flag indicating if this resource is a referent (i.e. is referred to by another 
	 * resource) or not. This also sets the count of time this referent is referenced to 1.
	 * @param value true if the resource is a referent, false if it is not.
	 */
	public void setIsReferent (boolean value);
	
	/**
	 * Indicates if this resource is a referent (i.e. is referred to by another resource)
	 * or not.
	 * @return true if this resource is a referent, false if it is not.
	 */
	public boolean isReferent ();

	/**
	 * Gets the number of time this referent is referenced to.
	 * @return the number of time this referent is referenced to.
	 */
	public int getReferenceCount ();
	
	/**
	 * Sets the number of time this referent is referenced to.
	 * @param value the number of time this referent is referenced to.
	 */
	public void setReferenceCount (int value);
	
}
