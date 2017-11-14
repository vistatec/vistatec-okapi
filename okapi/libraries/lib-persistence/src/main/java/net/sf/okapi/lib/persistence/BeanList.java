package net.sf.okapi.lib.persistence;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.exceptions.OkapiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanList {

	private static final Logger logger = LoggerFactory.getLogger(BeanList.class);
		
	/**
	 * Fills up a given list of beans with new beans created for objects in a given
	 * list of objects. The base class of the beans can be a superclass
	 * of the list of objects' base class.
	 * @param beanList the given list of beans.
	 * @param beanClass the base class of the given list of beans.
	 * @param objList the given list of objects.
	 * @param session the persistence session.
	 */
	public static <E, S extends E, T extends IPersistenceBean<E>> void set(
			List<T> beanList,
			Class<T> beanClass,
			List<S> objList, 					
			IPersistenceSession session) {
		if (objList == null) return;
		
		for (S obj : objList) {
			T bean = null;
			try {
				bean = beanClass.newInstance();
				bean.set(obj, session);
				beanList.add(bean);
			} catch (Exception e) {
				logger.error("Failed to create bean '{}' for '{}'.",
						ClassUtil.getQualifiedClassName(bean),
						ClassUtil.getQualifiedClassName(obj));
				throw new OkapiException(e);
			}
		}
	}
	
	/**
	 * Get a list of objects created from a given list of beans. The base class of 
	 * the beans can be a superclass of the list of objects' base class.  
	 * @param beanList the given list of beans.
	 * @param objClass the class of objects in the returned list.
	 * @param session the persistence session.
	 * @return a list of objects created from the given list of beans.
	 */
	public static <E, S extends E, T extends IPersistenceBean<E>> List<S> get(
			List<T> beanList,
			Class<S> objClass,
			IPersistenceSession session) {
		List<S> objList = new ArrayList<S>();
		for (T bean : beanList) {
			S obj = bean.get(objClass, session);
			objList.add(obj);
		}
		return objList;		
	}
			
}
