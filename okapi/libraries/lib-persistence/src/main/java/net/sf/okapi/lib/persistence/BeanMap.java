package net.sf.okapi.lib.persistence;

import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.exceptions.OkapiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanMap {

	private static final Logger logger = LoggerFactory.getLogger(BeanList.class);
	
	/**
	 * Fills up a given bean map with new beans created for objects in a given
	 * object map. The base class of the beans can be a superclass
	 * of the objects' class.
	 * @param beanMap the given bean map.
	 * @param beanClass the base class of the given bean map.
	 * @param objMap the given object map.
	 * @param session the persistence session.
	 */
	public static <E, S extends E, T extends IPersistenceBean<E>, U> void set(
			Map<U, T> beanMap,
			Class<T> beanClass,
			Map<U, S> objMap, 					
			IPersistenceSession session) {
		if (objMap == null) return;
		
		for (U key : objMap.keySet()) {
			S obj = objMap.get(key);
			T bean = null;
			try {
				bean = beanClass.newInstance();
				bean.set(obj, session);
				beanMap.put(key, bean);
			} catch (Exception e) {
				logger.error("Failed to create bean '{}' for '{}'.",
						ClassUtil.getQualifiedClassName(bean),
						ClassUtil.getQualifiedClassName(obj));
				throw new OkapiException(e);
			}
		}
	}
	
	/**
	 * Get an object map created from a given bean map. The base class of 
	 * the beans can be a superclass of the objects' class.  
	 * @param beanMap the given bean map.
	 * @param objClass the base class of the returned object map.
	 * @param session the persistence session.
	 * @return an object map created from the given bean map.
	 */
	public static <E, S extends E, T extends IPersistenceBean<E>, U> Map<U, S> get(
			Map<U, T> beanMap,
			Class<S> objClass,
			IPersistenceSession session) {
		Map<U, S> objMap = new HashMap<U, S>();
		for (U key : beanMap.keySet()) {
			T bean = beanMap.get(key);
			S obj = bean.get(objClass, session);
			objMap.put(key, obj);
		}
		return objMap;		
	}

}
