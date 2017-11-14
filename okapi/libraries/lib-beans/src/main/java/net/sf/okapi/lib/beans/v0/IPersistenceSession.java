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

package net.sf.okapi.lib.beans.v0;

import java.io.InputStream;
import java.io.OutputStream;

@Deprecated
public interface IPersistenceSession {

	/**
	 * Starts serialization.
	 * @param outStream output stream to serialize to.
	 */
	void start(OutputStream outStream);
	
	/**
	 * Starts deserialization.
	 * @param inStream input stream to deserialize from.
	 */
	void start(InputStream inStream);
	
	/**
	 * Ends serialization or deserialization.
	 */
	void end();
	
	/**
	 * Returns the current session status.
	 * @return true if serialization or deserialization is happening.
	 */
	boolean isActive();
	
	/**
	 * Serializes a given object to the session output stream. 
	 * @param obj the given object to be serialized.
	 */
	void serialize(Object obj);
	
	/**
	 * Deserializes an object from the session input stream.
	 * @return the deserialized object.
	 */
	Object deserialize();

	/**
	 * Converts a given object to an expected type.
	 * The given object can be serialized as is, and then deserialized as 
	 * an expected class instance. This helps if the object was initially deserialized incorrectly.
	 * Implementers can use different strategies to achieve the goal. 	
	 * @param obj the given object to be converted.
	 * @param expectedClass new class of the given object.
	 * @return the converted object.
	 */
	<T> T convert(Object obj, Class<T> expectedClass);
}
