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

package net.sf.okapi.lib.persistence;

import java.io.InputStream;
import java.io.OutputStream;

import net.sf.okapi.common.annotation.IAnnotation;

public interface IPersistenceSession {

	void registerVersions();
	
	/**
	 * Gets the current session state.
	 * @return session state
	 */
	SessionState getState();
	
	/**
	 * Starts serialization.
	 * @param outStream output stream to serialize to
	 */
	void start(OutputStream outStream);
	
	/**
	 * Starts deserialization.
	 * @param inStream input stream to deserialize from
	 */
	void start(InputStream inStream);
	
	/**
	 * Ends serialization or deserialization.
	 */
	void end();
	
	/**
	 * Serializes a given object to the session output stream. 
	 * @param obj the given object to be serialized
	 */
	void serialize(Object obj);
	
	/**
	 * Serializes to the session output stream a given object, labeling it with a given field label if implementation allows.
	 * @param obj the given object to be serialized
	 * @param name field name of the object
	 */
	void serialize(Object obj, String name);
	
	/**
	 * Deserializes an object from the session input stream.
	 * @return the deserialized object
	 */
	<T> T deserialize(Class<T> classRef);

	<T> T readObject(String content, Class<T> classRef);
	
	String writeObject(Object obj);
	
	<T> IPersistenceBean<T> createBean(Class<T> classRef);
	
	void cacheBean(Object obj, IPersistenceBean<?> bean);
	
	IPersistenceBean<?> uncacheBean(Object obj);
	
	/**
	 * Converts a given object to an expected type.
	 * The given object can be serialized as is, and then deserialized as 
	 * an expected class instance. This helps if the object was initially deserialized incorrectly.
	 * Implementers can use different strategies to achieve the goal. 	
	 * @param obj the given object to be converted
	 * @param expectedClass new class of the given object
	 * @return the converted object
	 */
	<T extends IPersistenceBean<?>> T convert(Object obj, Class<T> expectedClass);

	String getVersion();
	
	String getMimeType();
	
	String getItemClass();
	
	String getDescription();
		
	long getRefIdForObject(Object obj);
	
	public Object getObject(long refId);
	
	void setRefIdForObject(Object obj, long refId);

	void setReference(long parentRefId, long childRefId);

	void setSerialized(Object obj);
	
	Class<?> getClass(String objClassName);
	
	Class<?> getObjectClass(Class<? extends IPersistenceBean<?>> beanClassRef);
	
	<T> Class<IPersistenceBean<T>> getBeanClass(Class<T> classRef);
	
	Class<? extends IPersistenceBean<?>> getBeanClass(String className);
	
	IPersistenceBean<?> getProxy(String objClassName);
	
	IPersistenceBean<?> getProxy(Class<?> objClassRef);
	
	void registerBean(
			Class<?> classRef, 
			Class<? extends IPersistenceBean<?>> beanClassRef);
	
	<A extends IAnnotation> A getAnnotation (Class<A> annotationType);
	
	void setAnnotation (IAnnotation annotation);
	
	Iterable<IAnnotation> getAnnotations ();
	
	void registerEndTask(Runnable task);
}
