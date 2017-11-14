/*===========================================================================
  Copyright (C) 2015 by the Okapi Framework contributors
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

package net.sf.okapi.filters.icml;

import java.util.HashMap;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.resource.TextFragment;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ICMLSkeleton implements ISkeleton {

	public final static String NODEREMARKER = "SKLREF";

	private Document doc; // Used for Startgroup of story
	private Node topNode; // Used for TextUnit
	private Node scopeNode; // Used for TextUnit
	private HashMap<String, NodeReference> refs; // Used for TextUnit
	private TextFragment[] movedParts; // Temporary moved outside the content
	private boolean forced = false; // Indicates the TU was forced (for example by a Br tag) and need special care on merge
	
	public ICMLSkeleton (Document doc)
	{
		this.doc = doc;
	}
	
	public ICMLSkeleton (Node topNode,
		Node scopeNode)
	{
		this.topNode = topNode;
		this.scopeNode = scopeNode;
	}
	
	public void addReferenceNode (String id,
		NodeReference ref)
	{
		if ( refs == null ) {
			refs = new HashMap<String, NodeReference>();
		}
		refs.put(id, ref);
	}
	
	public void addMovedParts (TextFragment[] movedParts) {
		this.movedParts = movedParts;
	}
	
	public TextFragment[] getMovedParts () {
		return movedParts;
	}
	
	public boolean hasReferences () {
		return (( refs != null ) && ( refs.size() > 0 ));
	}
	
	public HashMap<String, NodeReference> getReferences () {
		return refs;
	}
	
	public Document getDocument () {
		return doc;
	}

	public Node getTopNode () {
		return topNode;
	}
	
	public Document getOriginal () {
		return doc;
	}

	public Node getScopeNode () {
		return scopeNode;
	}
	
	public void setForced (boolean forced) {
		this.forced = forced;
	}
	
	public boolean getForced () {
		return forced;
	}

	/**
	 * Returns a shallow copy of this skeleton.
	 */
	@Override
	public ICMLSkeleton clone () {
		ICMLSkeleton newSkel = new ICMLSkeleton(doc);
		newSkel.doc = doc;
		newSkel.topNode = topNode;
		newSkel.scopeNode = scopeNode;
		newSkel.refs = refs;
		newSkel.movedParts = movedParts;
		return newSkel;
	}

	@Override
	public void setParent(IResource parent) {
		// Parent is not stored
	}

	@Override
	public IResource getParent() {
		return null;
	}
}
 