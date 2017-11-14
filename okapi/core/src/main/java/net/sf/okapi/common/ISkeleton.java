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

package net.sf.okapi.common;

/**
 * Represents a skeleton object. It is an object that is associated to a resource and carries data
 * about the non extractable part of the resource. Skeleton objects are used by the 
 * {@link net.sf.okapi.common.filterwriter.IFilterWriter} implementations to reconstruct the 
 * original file format.
 */
public interface ISkeleton {

	/**
	 * Gets a string representation of this skeleton object. The value of the returned string depends
	 * on each implementation of class that implements ISkeleton. Different implementations may return
	 * strings that cannot be compared in a meaningful way. 
	 * @return the string representation of this skeleton object, or null.
	 */
	String toString ();
	
	/**
     * Clones this skeleton object.
     * @return a new skeleton object that is a copy of this one.
     */
	ISkeleton clone();

	/**
	 * Sets a parent of this skeleton object. The parent is the resource that attaches this skeleton with SetSkeleton().
	 * Normally the IResorce implementations set themselves as a parent for the skeleton.  
	 * @param parent reference to the resource that attaches this skeleton object with IResource.setSkeleton()
	 */
	void setParent(IResource parent);
	
	/**
	 * Gets the parent resource of this skeleton. 
	 * @return reference to the resource that attaches this skeleton object with IResource.setSkeleton()
	 */
	IResource getParent();
}
