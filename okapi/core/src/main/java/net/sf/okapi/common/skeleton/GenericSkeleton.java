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

package net.sf.okapi.common.skeleton;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.IReferenceable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;

/**
 * Simple generic implementation of the ISkeleton interface.
 * This class implements a skeleton as a list of parts: some are simple text storage
 * string corresponding to the original code of the input document, others are
 * placeholders for the content the the text units, or the values of modifiable
 * properties. 
 */
public class GenericSkeleton implements ISkeleton {

	private ArrayList<GenericSkeletonPart> list;
	private boolean createNew = true;
	private IResource parent;

	/**
	 * Creates a new empty GenericSkeleton object.
	 */
	public GenericSkeleton () {
		list = new ArrayList<GenericSkeletonPart>();
	}

	/**
	 * Creates a new GenericSkeleton object and append some data to it.
	 * @param data the data to append.
	 */
	public GenericSkeleton (String data) {
		list = new ArrayList<GenericSkeletonPart>();
		if ( data != null ) add(data);
	}
	
	/**
	 * Creates a new GenericSkeleton object and initialize it with the parts
	 * of an existing one passed as a parameter.
	 * @param skel the existing skeleton from which to copy the parts.
	 */
	public GenericSkeleton (GenericSkeleton skel) {
		list = new ArrayList<GenericSkeletonPart>();		
		if ( skel != null ) { 
			for ( GenericSkeletonPart part : skel.list ) {
				list.add(part);
			}
		}
	}

	/**
	 * Indicates if this skeleton is empty or not.
	 * @return true if this skeleton is empty, false if it has at least one part.
	 */
	public boolean isEmpty () {
		return (list.size()==0);
	}
	
	/**
	 * Indicates if this skeleton is empty or not, considering the white-spaces
	 * or not.
	 * @param ignoreWhitespaces true to ignore the white-spaces.
	 * @return true if this skeleton is empty, false if it has at least one part.
	 */
	public boolean isEmpty (boolean ignoreWhitespaces) {
		if ( ignoreWhitespaces ) {
			for ( GenericSkeletonPart part : list ) {
				for ( int i=0; i<part.data.length(); i++ ) {
					if ( !Character.isWhitespace(part.data.charAt(i)) ) {
						return false;
					}
				}
			}
			return true; // no parts, or only with white-spaces
		}
		else { // Just like isEmpty()
			return (list.size()==0);
		}
	}

	/**
	 * Adds a new part to this skeleton, and set a string data to it.
	 * Empty or null data has no effect.
	 * @param data the data to add.
	 */
	public void add (String data) {
		if ( Util.isEmpty(data) ) return;
		GenericSkeletonPart part = new GenericSkeletonPart(data);
		list.add(part);
		createNew = false;
	}
	
	/**
	 * Forces the current part to be completed, so the next call to append data will start a new part.
	 */
	public void flushPart () {
		createNew = true;
	}
	
	/**
	 * Adds a new part to this skeleton, and set a character data to it.
	 * @param data the data to add.
	 */
	public void add (char data) {
		GenericSkeletonPart part = new GenericSkeletonPart(data);
		list.add(part);
		createNew = false;
	}
	
	/**
	 * Adds to this skeleton all the parts of a given skeleton.
	 * @param skel the existing skeleton from which to copy the parts.
	 */
	public void add (GenericSkeleton skel) {
		if ( skel != null ) { 
			for ( GenericSkeletonPart part : skel.list ) {
				list.add(part);
			}
		}
	}

	/**
	 * Appends a string of data to the first skeleton part, a new
	 * part is created is none exists already.
	 * @param data the string data to append.
	 */
	public void appendToFirstPart (String data) { // DWH 5-2-09
		if ( data.length() == 0 ) return;
		if ( list.isEmpty() ) {
			add(data);
		}
		else {
			list.get(0).append(data);
		}
	}

	/**
	 * Appends a string of data to this skeleton. The text is added to
	 * the current part if the current part is already a data part, a new
	 * part is created is necessary. 
	 * @param data the string data to append.
	 */
	public void append (String data) {
		if ( data.length() == 0 ) return;
		if ( createNew || list.isEmpty() ) {
			add(data);
		}
		else {
			list.get(list.size()-1).append(data);
		}
	}

	/**
	 * Appends a character data to this skeleton. The text is added to
	 * the current part if the current part is already a data part, a new
	 * part is created is necessary. 
	 * @param data the character data to append.
	 */
	public void append (char data) {
		if ( createNew || list.isEmpty() ) {
			add(data);
		}
		else {
			list.get(list.size()-1).data.append(data);
		}
	}

	/**
	 * Adds to this skeleton a placeholder for the source content of the resource
	 * to which this skeleton is attached.
	 * @param textUnit the resource object.
	 */
	public void addContentPlaceholder (ITextUnit textUnit)
	{
		GenericSkeletonPart part = new GenericSkeletonPart(TextFragment.makeRefMarker("$self$"));
		part.parent = textUnit;
		part.locId = null;
		list.add(part);
		// Flag that the next append() should start a new part
		createNew = true;
	}
	
	/**
	 * Adds to this skeleton a placeholder for the content (in a given locale) of the resource
	 * to which this skeleton is attached.
	 * @param textUnit the resource object.
	 * @param locId the locale; use null if the reference is the source.
	 */
	public void addContentPlaceholder (ITextUnit textUnit,
		LocaleId locId)
	{
		GenericSkeletonPart part = new GenericSkeletonPart(TextFragment.makeRefMarker("$self$"));
		part.parent = textUnit;
		part.locId = locId;
		list.add(part);
		// Flag that the next append() should start a new part
		createNew = true;
	}

	/**
	 * Adds to this skeleton a place-holder for the value of a property (in a given locale)
	 * of the resource to which this skeleton is attached.
	 * @param referent the resource object.
	 * @param propName the property name.
	 * @param locId the locale; use null for the source; LocaleId.EMPTY for resource-level property.
	 * @return the added skeleton part.
	 */
	public GenericSkeletonPart addValuePlaceholder (INameable referent,
		String propName,
		LocaleId locId)
	{
		GenericSkeletonPart part = new GenericSkeletonPart(
			TextFragment.makeRefMarker("$self$", propName));
		part.locId = locId;
		part.parent = referent;
		list.add(part);
		// Flag that the next append() should start a new part
		createNew = true;
		return part;
	}
	
	/**
	 * Updates all the self-references to use the given referent. 
	 * @param newReferent the new referent to use.
	 */
	public void changeSelfReferents (INameable newReferent) {
		String start = TextFragment.REFMARKER_START+"$self$";
		for ( GenericSkeletonPart part : list ) {
			if (( part.data != null ) && ( part.data.toString().startsWith(start) )) {
				part.parent = newReferent;
			}
		}
	}
	
	/**
	 * Adds to this skeleton a reference to an existing resource send before the one to 
	 * which this skeleton is attached to.
	 * @param referent the resource to refer to.
	 */
	public void addReference (IReferenceable referent) {
		GenericSkeletonPart part = new GenericSkeletonPart(
			TextFragment.makeRefMarker(((IResource)referent).getId()));
			part.locId = null;
			part.parent = null; // This is a reference to a real referent
			list.add(part);
			// Flag that the next append() should start a new part
			createNew = true;
	}
	
	@Override
	public void setParent(IResource parent) {
		String start = TextFragment.REFMARKER_START + "$self$";
    	
		// Update references to the current parent with references to the new parent
    	for (GenericSkeletonPart part : list) {
			if (part.data.toString().startsWith(start) && part.getParent() != null && part.getParent() == this.getParent()) {
				part.parent = parent;
			}
		}
		this.parent = parent;
	}
	
	/**
	 * Attaches a parent resource to a skeleton part.
	 * @param parent the parent resource to attach.
	 */
	public void attachParent (INameable parent) {
		if ( createNew || list.isEmpty() ) {
			GenericSkeletonPart part = new GenericSkeletonPart("");
			list.add(part);
			createNew = false;
		}
		list.get(list.size()-1).parent = parent;
	}
	
	/**
	 * Gets a list of all the parts of this skeleton.
	 * @return the list of all the parts of this skeleton.
	 */
	public List<GenericSkeletonPart> getParts () {
		return list;
	}
	
	/*
	 * For serialization only
	 */
	protected void setParts(ArrayList<GenericSkeletonPart> list) {
		this.list = list;
	}

	/**
	 * Gets a string representation of the content of all the part of the skeleton.
	 * This should be used for display only.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (GenericSkeletonPart part : list) {
			b.append(part.toString());
		}
		return b.toString();
	}
	
	/**
	 * Gets the last part of this skeleton, or null if there are none.
	 * @return the last part of this skeleton, or null if there are none.
	 */
	public GenericSkeletonPart getLastPart () {
		if  ( list.size() == 0 ) return null;
		else return list.get(list.size()-1);
	}

	/**
	 * Gets the first part of this skeleton, or null if there are none.
	 * @return the first part of this skeleton, or null if there are none.
	 */
	public GenericSkeletonPart getFirstPart () {
		if  ( list.size() == 0 ) return null;
		else return list.get(0);
	}
	
	protected void copyFields(GenericSkeleton toSkel) {
		toSkel.createNew = this.createNew;
		toSkel.parent = this.parent;
		
		if (toSkel.list == null) {
			toSkel.list = new ArrayList<GenericSkeletonPart>();
		}
		
		for (GenericSkeletonPart part : list) {
			GenericSkeletonPart newPart = new GenericSkeletonPart(part.data.toString(), part.parent, part.locId);
			toSkel.list.add(newPart);
		}
	}
	
	/**
	 * Clones this GenericSkeleton object. Shallow copy is provided as the cloned skeleton can be coupled with
	 * its original via the parent field. After the cloned skeleton is attached to a parent resource which implementation
	 * invokes ISkeleton.setParent(), the copy becomes deep as the parent fields are updated with new values thus decoupling
	 * the original and the clone. 
	 * @return a new GenericSkeleton object that is a shallow copy of this object.
	 */
	@Override
	public GenericSkeleton clone() {		
		GenericSkeleton newSkel = new GenericSkeleton();
		copyFields(newSkel);		
		return newSkel;
	}

	@Override
	public IResource getParent() {
		return parent;
	}
}
