/*===========================================================================
  Copyright (C) 2010-2012 by the Okapi Framework contributors
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

import java.util.List;

import net.sf.okapi.common.exceptions.OkapiException;

/**
 * Provides a common way to generate sequential ID that are unique for a given root.
 * The root can be null or empty.
 * <p>Each value generated is made of two main parts separated by a '-':
 * <ol><li>The hexadecimal representation of the hash code of the root (if a root exists)
 * <li>The sequential identifier starting at 1, with a fixed prefix if one was provided.  
 * </ol>
 */
public class IdGenerator {
	
	public static final String START_DOCUMENT = "sd";
	public static final String END_DOCUMENT = "ed";
	public static final String START_GROUP = "sg";
	public static final String END_GROUP = "eg";
	public static final String TEXT_UNIT = "tu";
	public static final String DOCUMENT_PART = "dp";
	public static final String START_SUBDOCUMENT = "ssd";
	public static final String END_SUBDOCUMENT = "esd";
	public static final String START_SUBFILTER = "ssf";
	public static final String SUBFILTERED_EVENT = "sf";
	public static final String END_SUBFILTER = "esf";
	public static final String DEFAULT_ROOT_ID = "noDocName";
	
	private long seq = 0;
	private String rootId;
	private String prefix;
	private String lastId;
	
	/**
	 * Creates a generator with a given root and no prefix.
	 * @param root
	 * 	the root to use (case-sensitive, can be null or empty)
	 */
	public IdGenerator (String root) {
		create(root, "");
	}

	/**
	 * Creates a generator with a given root and a given prefix.
	 * @param root
	 * 	the root to use (case-sensitive, can be null)
	 * @param prefix
	 * 	the prefix to use (case-sensitive, can be null)
	 */
	public IdGenerator (String root,
		String prefix)
	{
		create(root, prefix);
	}

	/**
	 * Returns the same value as {@link #getLastId()}. 
	 */
	@Override
	public String toString () {
		return getLastId();
	}
	
	/**
	 * Creates a new identifier.
	 * @return
	 *  the new identifier.
	 */
	public String createId () {
		if ( rootId == null ) {
			lastId = prefix + Long.toString(++seq);
		}
		else {
			lastId = rootId + "-" + prefix + Long.toString(++seq);
		}
		return lastId;
	}
	
	/**
	 * Creates a new identifier that is not in the given list.
	 * @param list the list of identifiers not to use.
	 * @return a new identifier that is not in the given list.
	 */
	public String createIdNotInList (List<String> list) {
		String tmp = createId();
		while ( list.contains(tmp) ) {
			tmp = createId();
		}
		return tmp;
	}
	
	/**
	 * Creates a new identifier with the given prefix
	 * @param prefix the prefix to be used  with this id
	 * @return
	 *  the new identifier.
	 */
	public String createId (String prefix) {
		String orginalPrefix = this.prefix;
		this.prefix = prefix;
		try {
			if ( rootId == null ) {
				lastId = prefix + Long.toString(++seq);
			}
			else {
				lastId = rootId + "-" + prefix + Long.toString(++seq);
			}
			return lastId;
		}
		finally {
			this.prefix = orginalPrefix;
		}		
	}
	
	/**
	 * sets the internal value that is used to remember the last identifier.
	 * Use this method when you create the ID from outside the object, but still need the last id. 
	 * @param lastId the new last id.
	 */
	public void setLastId (String lastId) {
		this.lastId = lastId;
	}

	/**
	 * Gets the last identifier generated.
	 * This method allows you to get the last identifier that was returned by {@link #createId()}.
	 * @return
	 *  the last identifier generated.
	 * @throws
	 *  RuntimeException if the method {@link #createId()} has not been called at least once
	 *  before call this method. 
	 */
	public String getLastId () {
		if ( lastId == null ) {
			throw new OkapiException("The method createId() has not been called yet.");
		}
		return lastId;
	}

//	/**
//	 * Gets the last identifier generated with the given prefix
//	 * This method allows you to get the last identifier that was returned by {@link #createId()}.
//	 * @param prefix prefix to be used with this id
//	 * @return
//	 *  the last identifier generated.
//	 * @throws
//	 *  RuntimeException if the method {@link #createId()} has not been called at least once
//	 *  before call this method. 
//	 */
//	public String getLastId (String prefix) {
//		String orginalPrefix = this.prefix;
//		this.prefix = prefix;
//
//		try {
//			if ( seq <= 0 ) {
//				throw new OkapiException("The method createId() has not been called yet.");
//			}
//			if ( rootId == null ) {
//				return prefix + Long.toString(seq);
//			}
//			else {
//				return rootId + "-" + prefix + Long.toString(seq);
//			}
//		} finally {
//			this.prefix = orginalPrefix;
//		}
//	}
	
	/**
	 * Gets the id generated from the root string given when creating this object.
	 * @return the id of the root for this object (can be null)
	 */
	public String getRootId () {
		return rootId;
	}
	
	/**
	 * Set the sequence from outside. Useful to renumber.
	 * @param sequence the new sequence.
	 */
	public void setSequence(long sequence) {
		seq = sequence;
	}
	
	/**
	 * Get the current sequence number.
	 * @return the sequence
	 */
	public long getSequence() {
		return seq;
	}
	
	/**
	 * Reset the {@link IdGenerator} with a new root id. 
	 * Use the same prefix and set the sequence count to 0.
	 * @param rootId new root id (can be null or empty)
	 */
	public void reset (String rootId) {
		seq = 0;
		create(rootId, prefix);
	}

	/**
	 * Sets the prefix to use when creating the id.
	 * @param prefix the new prefix to use (can be null).
	 */
	public void setPrefix (String prefix) {
		if ( prefix == null ) this.prefix = "";
		else this.prefix = prefix;
	}
	
	private void create (String root,
		String prefix)
	{
		// Set the root part
		if ( Util.isEmpty(root) ) {
			// Use null for empty or null
			rootId = null;
		}
		else {
			// makeId() uses the String.hashCode which should be reproducible across VM and sessions
			rootId = Util.makeId(root);
		}
	
		// Set the prefix part (empty is OK)
		setPrefix(prefix);
	}

}
