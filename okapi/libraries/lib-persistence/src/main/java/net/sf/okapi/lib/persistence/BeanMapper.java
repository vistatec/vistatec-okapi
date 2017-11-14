/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanMapper {
	
	private static final String MAPPER_NOT_INIT = "BeanMapper: bean mapping is not initialized";
	private static final String MAPPER_UNK_CLASS = "BeanMapper: unknown class: %s";		
	private static final String MAPPER_EMPTY_REF = "BeanMapper: class reference cannot be empty";
	private static final String MAPPER_NOT_REG = "No bean class registered for {}, using {} for {} instead.";
	
	private static final String OBJ_MAPPER_NOT_INIT = "BeanMapper: object mapping is not initialized";
	private static final String OBJ_MAPPER_EMPTY_REF = "BeanMapper: bean class reference cannot be empty";
	
	private static final String PROXIES_CANT_INST = "BeanMapper: cannot instantiate a proxy for %s";
	private static final String PROXIES_NOT_INIT = "BeanMapper: proxy mapping is not initialized";	
	
	// !!! LinkedHashMap to preserve registration order
	private LinkedHashMap<Class<?>, Class<? extends IPersistenceBean<?>>> beanClassMapping;
	private HashMap<Class<? extends IPersistenceBean<?>>, Class<?>> objectClassMapping;
	private ArrayList<Class<?>> loggedClasses; // To log no bean only once	
	private ArrayList<String> loggedClassNames;
	private ConcurrentHashMap<String, IPersistenceBean<?>> proxies; // used in ref resolution
	private IPersistenceSession session;
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	public BeanMapper(IPersistenceSession session) {
		this.session = session;
		beanClassMapping = new LinkedHashMap<Class<?>, Class<? extends IPersistenceBean<?>>> ();
		objectClassMapping = new HashMap<Class<? extends IPersistenceBean<?>>, Class<?>> ();
		proxies = new ConcurrentHashMap<String, IPersistenceBean<?>>();
		loggedClasses = new ArrayList<Class<?>>();
		loggedClassNames = new ArrayList<String>();
	}

	public void reset() {
		beanClassMapping.clear();
		objectClassMapping.clear();
		proxies.clear();
		loggedClasses.clear(); 
		loggedClassNames.clear();		
	}
	
	public void registerBean(
			Class<?> classRef, 
			Class<? extends IPersistenceBean<?>> beanClassRef) {
		registerBean(classRef, beanClassRef, false);
	}
	
	public void registerBean(
			Class<?> classRef, 
			Class<? extends IPersistenceBean<?>> beanClassRef,
					boolean blockNotMappedWarning) {
		if (classRef == null || beanClassRef == null)
			throw(new IllegalArgumentException());
		
		if (beanClassMapping == null)
			throw(new OkapiException(MAPPER_NOT_INIT));
		
		if (objectClassMapping == null)
			throw(new OkapiException(OBJ_MAPPER_NOT_INIT));
		
		// If we are inserting mapping for a class already in the beanClassMapping map,
		// we need to first remove the old value so that new mappings are
		// always added to the end of beanClassMapping (preserving insertion order)
		if (beanClassMapping.containsKey(classRef)) {
			Class<? extends IPersistenceBean<?>> oldBeanClass =
					beanClassMapping.get(classRef);
			if (objectClassMapping.containsKey(oldBeanClass)) {
				objectClassMapping.remove(oldBeanClass);
			}
			beanClassMapping.remove(classRef);
		}
		
		beanClassMapping.put(classRef, beanClassRef);
		if (blockNotMappedWarning) {
			loggedClasses.add(classRef); // As if already logged the not-mapped warning
		}
				
		objectClassMapping.put(beanClassRef, classRef);
				
		if (proxies == null)
			throw(new OkapiException(PROXIES_NOT_INIT));
		
//		String beanClassName = ClassUtil.getQualifiedClassName(beanClassRef);
//		try {
//			proxies.put(beanClassName, ClassUtil.instantiateClass(beanClassRef));
//		} catch (Exception e) {
//			throw new OkapiException(String.format(PROXIES_CANT_INST, beanClassName), e);
//		}
		
		String objClassName = ClassUtil.getQualifiedClassName(classRef);
		String beanClassName = ClassUtil.getQualifiedClassName(classRef);
		try {
			IPersistenceBean<?> proxy = ClassUtil.instantiateClass(beanClassRef);
			if (proxy != null)
				proxy.setRefId(0); // to distinguish from regular beans 
			
			proxies.put(objClassName, proxy);
		} catch (Exception e) {
			throw new OkapiException(String.format(PROXIES_CANT_INST, beanClassName), e);
		}		
	}
	
	@SuppressWarnings("unchecked")
	public <T> Class<IPersistenceBean<T>> getBeanClass(Class<T> classRef) {
		if (classRef == null)
			throw(new IllegalArgumentException(MAPPER_EMPTY_REF));
	
		if (beanClassMapping == null)
			throw(new OkapiException(MAPPER_NOT_INIT));
		
		Class<IPersistenceBean<T>> beanClass = (Class<IPersistenceBean<T>>) beanClassMapping.get(classRef);
				
		// If not found explicitly, try to find a matching bean
		if (beanClass == null) {
			if (IPersistenceBean.class.isAssignableFrom(classRef)) // A bean is a bean for itself 
				beanClass = (Class<IPersistenceBean<T>>) classRef;
			else {
				// We iterate backwards to fall back to more generic beans
				LinkedList<Class<?>> classes = new LinkedList<Class<?>>(beanClassMapping.keySet()); 
				for(Iterator<Class<?>> it = classes.descendingIterator(); it.hasNext();) {
					Class<?> cls = it.next();
					
					if (cls.isAssignableFrom(classRef)) {
						beanClass = (Class<IPersistenceBean<T>>) beanClassMapping.get(cls);
						if (session.getState() == SessionState.WRITING && 
								!loggedClasses.contains(classRef) &&
								!loggedClasses.contains(cls)) { // Blocked in registerBean(classRef, beanClassRef, true)
							loggedClasses.add(classRef);
							LOGGER.debug(MAPPER_NOT_REG,
									ClassUtil.getQualifiedClassName(classRef),
									ClassUtil.getQualifiedClassName(beanClass),
									ClassUtil.getQualifiedClassName(cls));
						}					
						break;
					}
				}
			}
			
			if (beanClass == null && !loggedClasses.contains(classRef)) {
				loggedClasses.add(classRef);
				LOGGER.debug("No bean class registered for {}", ClassUtil.getQualifiedClassName(classRef));
			}				
		}
		return beanClass;		
	}
	
	public Class<?> getObjectClass(Class<? extends IPersistenceBean<?>> beanClassRef) {
		if (beanClassRef == null)
			throw(new IllegalArgumentException(OBJ_MAPPER_EMPTY_REF));
	
		if (objectClassMapping == null)
			throw(new OkapiException(OBJ_MAPPER_NOT_INIT));
		
		return objectClassMapping.get(beanClassRef);		
	}
	
	public Class<?> getClass(String objClassName) {
		return ClassUtil.getClass(NamespaceMapper.getMapping(objClassName));
	}
	
	public IPersistenceBean<?> getProxy(String objClassName) {
		if (Util.isEmpty(objClassName)) return null;
		
		IPersistenceBean<?> proxy = proxies.get(NamespaceMapper.getMapping(objClassName)); 
		if (proxy == null && !loggedClassNames.contains(objClassName)) {
			loggedClassNames.add(objClassName);
			LOGGER.debug("No proxy found for {}", objClassName);
		}
		
		return proxy;
	}
	
	public IPersistenceBean<?> getProxy(Class<?> objClassRef) {
		if (objClassRef == null) return null;
				
//		Class<? extends IPersistenceBean> beanClassRef = getBeanClass(objClassRef);
//		if (beanClassRef == null) return null;
//		return getProxy(beanClassRef.getName());
		return getProxy(objClassRef.getName());
	}
	
	public Class<? extends IPersistenceBean<?>> getBeanClass(String className) {
			
		Class<? extends IPersistenceBean<?>> res = null;
		try {
			res = getBeanClass(Class.forName(NamespaceMapper.getMapping(className)));
		} catch (ClassNotFoundException e) {
			throw(new OkapiException(String.format(MAPPER_UNK_CLASS, className)));
		}
		return res;		
	}
}
