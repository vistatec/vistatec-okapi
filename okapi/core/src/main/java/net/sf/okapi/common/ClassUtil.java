/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.sf.okapi.common.exceptions.OkapiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collection of helper functions for working with classes.
 */
public class ClassUtil {
	private final static Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);

	private final static String MSG_CANT_INSTANTIATE = "ClassUtil: cannot instantiate %s";
	private final static String MSG_EMPTY_CLASSNAME = "ClassUtil: class name cannot be empty";
	private final static String MSG_NULL_REF = "ClassUtil: class reference cannot be null";
	private final static String MSG_NULL_LOADER = "ClassUtil: class loader cannot be null";
	private final static String MSG_NONRESOLVABLE = "ClassUtil: cannot resolve class name %s";
	private final static Pattern SPACES = Pattern.compile("\\s");
	
	/**
	 * Gets the runtime class of a given object.
	 * @param obj The given object
	 * @return The object's runtime class
	 */
	public static Class<?> getClass(Object obj) {
		if (obj == null) return null;
	
		return obj.getClass();
	}

	/**
	 * Gets the class reference for a given qualified class name. 
	 * @param className The given class name
	 * @return Class reference
	 */
	public static Class<?> getClass(String className) {
		if (Util.isEmpty(className))
			throw new IllegalArgumentException(MSG_EMPTY_CLASSNAME);

		Class<?> ref;
		try {
			ref = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new OkapiException(String.format(MSG_NONRESOLVABLE, className));
		}
		return ref;
	}
	
	/**
	 * Gets the non-qualified (without package name prefix) class name for a given object.
	 * @param obj The given object
	 * @return The object's class name (w/o package name prefix)
	 */
	public static String getClassName(Object obj) {
		if (obj == null) return "";
	
		return getClassName(obj.getClass());
	}
	
	/**
	 * Gets the non-qualified (w/o package name prefix) class name of a given class reference.
	 * @param classRef The given class reference
	 * @return The name of the class (w/o package name prefix)
	 */
	public static String getClassName(Class<?> classRef) {
		if (classRef == null) return "";
	
		return classRef.getSimpleName();
	}

	/**
	 * Gets the qualified class name of a given object.
	 * @param obj The given object
	 * @return Qualified class name
	 */
	public static String getQualifiedClassName(Object obj) {
		if (obj == null) return "";
		
		return getQualifiedClassName(obj.getClass());
	}
	
	/**
	 * Gets the qualified class name of a given class reference.
	 * @param classRef The given class reference
	 * @return Qualified class name
	 */
	public static String getQualifiedClassName(Class<?> classRef) {
		if (classRef == null) return "";
		
		return classRef.getName();
	}
	
	/**
	 * Gets the non-qualified class name (w/o package name prefix) of a given class reference.
	 * @param classRef The given class reference
	 * @return Non-qualified class name
	 */
	public static String getShortClassName(Class<?> classRef) {
		if (classRef == null) return "";
		
		return extractShortClassName(getQualifiedClassName(classRef));
	}
	
	/**
	 * Gets the non-qualified class name (w/o package name prefix) of a given object.
	 * @param obj The given object
	 * @return Non-qualified class name
	 */
	public static String getShortClassName(Object obj) {
		if (obj == null) return "";
		
		return extractShortClassName(getQualifiedClassName(obj));
	}
		
	/**
	 * Gets the name of the package containing a given object's class.
	 * @param obj The given object
	 * @return Package name of the given object's class (w/o the trailing dot), or an empty string
	 */
	public static String getPackageName(Object obj) {
		if (obj == null) return "";
		
		return getPackageName(obj.getClass());
	}
	
	/**
	 * Gets the package name of a given class reference. 
	 * @param classRef The given class reference
	 * @return Package name of the given class reference (w/o the trailing dot), or an empty string 
	 */
	public static String getPackageName(Class<?> classRef) {
		if (classRef == null) return "";
		
		Package pkg = classRef.getPackage();
		
		if (pkg == null) {
			
			String className = classRef.getName();
			String shortClassName = classRef.getSimpleName();
			
			int index = className.lastIndexOf(shortClassName);
            if (index != -1) {
            	
            	String res = className.substring(0, index);
            	return res.endsWith(".") ? res.substring(0, res.length() - 1) : res; 
            }
            else
            	return "";
		}
		
		return pkg.getName();
	}

	public static String getTargetPath(Class<?> cls) {
		try {
			return cls.getResource("").toURI().getPath();
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	/**
	 * Extracts the package name part of a qualified class name.
	 * @param className Qualified class name
	 * @return Package name (w/o the trailing dot)
	 */
	public static String extractPackageName(String className) {
		if (Util.isEmpty(className)) return "";
		
		int index = className.lastIndexOf(".");
		if (index > -1)
			return className.substring(0, index);
		
		return "";
	}
	
	/**
	 * Extracts the class name part of a qualified class name.
	 * @param className Qualified class name
	 * @return Class name
	 */
	public static String extractShortClassName(String className) {
		if (Util.isEmpty(className)) return "";
		
		int index = className.lastIndexOf(".");
		if (index > -1)
			return className.substring(index + 1);
		
		return className;
	}

	/**
	 * Builds a qualified class name from given parts.  
	 * @param packageName Package name
	 * @param shortClassName Class name
	 * @return Qualified class name
	 */
	public static String qualifyName(String packageName, String shortClassName) {
		if (Util.isEmpty(packageName)) return "";
		if (Util.isEmpty(shortClassName)) return "";
		
		// Already qualified
		if (shortClassName.indexOf(".") != -1) return shortClassName;
			
		if (!packageName.endsWith("."))
			packageName += ".";
		
		return packageName + shortClassName;
	}

	/**
	 * Builds a qualified class name for the given class name. Package name is determined 
	 * from a reference to another class in the same package.
	 * @param siblingClassRef Reference to another class in the same package
	 * @param shortClassName Non-qualified name of the class to get a qualified name for
	 * @return Qualified class name
	 */
	public static String qualifyName(Class<?> siblingClassRef, String shortClassName) {		
		return qualifyName(getPackageName(siblingClassRef), shortClassName);
	}
	
	/**
	 * Builds a qualified class name for the given class name. Package name is determined 
	 * from an instance of another class in the same package.
	 * @param sibling Existing object, an instance of another class in the same package
	 * @param shortClassName Non-qualified name of the class to get a qualified name for
	 * @return Qualified class name
	 */
	public static String qualifyName(Object sibling, String shortClassName) {		
		if (sibling == null) return ""; 
		
		return qualifyName(sibling.getClass(), shortClassName);
	}
	
	/**
	 * Creates a new instance of a given class.
	 * @param <T> the type of the given class
	 * @param classRef The given class
	 * @return a newly created instance of the given class
	 * @throws InstantiationException when we fail to create an instance.
	 * @throws IllegalAccessException when trying to create an instance but don't have the right.
	 */
	public static <T> T instantiateClass(Class<T> classRef) 
		throws InstantiationException, IllegalAccessException {
		if (classRef == null)
			throw new IllegalArgumentException(MSG_NULL_REF);
		
		return classRef.cast(classRef.newInstance());
	}
	
	/**
	 * Creates a new instance of the class with a given class name.
	 * @param className The given class name
	 * @return a newly created instance of the class with the given class name
	 * @throws InstantiationException when we fail to create an instance.
	 * @throws IllegalAccessException when trying to create an instance but don't have the right.
	 * @throws ClassNotFoundException when no definition for the class was found
	 */
	public static Object instantiateClass(String className)
		throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (Util.isEmpty(className))
			throw new IllegalArgumentException(MSG_EMPTY_CLASSNAME);
		
		return instantiateClass(Class.forName(className));
	}
	
	/**
	 * Creates a new instance of the class with a given class name using a given class loader.
	 * @param className The given class name
	 * @param classLoader The class loader from which the class must be loaded
	 * @return A newly created instance of the desired class.
	 * @throws InstantiationException when we fail to create an instance.
	 * @throws IllegalAccessException when trying to create an instance but don't have the right.
	 * @throws ClassNotFoundException when no definition for the class was found
	 */
	public static Object instantiateClass(String className, ClassLoader classLoader) 
		throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (Util.isEmpty(className))
			throw new IllegalArgumentException(MSG_EMPTY_CLASSNAME);
		
		if (classLoader == null)
			throw new IllegalArgumentException(MSG_NULL_LOADER);
		
		Class<?> ref = Class.forName(className, true, classLoader);
		if (ref == null)
			throw new OkapiException(String.format(MSG_NONRESOLVABLE, className));
			
		return ref.cast(ref.newInstance());
	}
	
	/**
	 * Creates a new instance of the class using a given class loader and initialization parameters.
	 * @param <T> the type of the class to create
	 * @param classRef The given class
	 * @param constructorParameters The initialization parameters for the class constructor
	 * @return A newly created instance of the desired class
	 * @throws SecurityException if we don't have the right to create the class
	 * @throws NoSuchMethodException when a constructor cannot be found
	 * @throws IllegalArgumentException if the classRef was null.
	 * @throws InstantiationException when we fail to create an instance.
	 * @throws IllegalAccessException when trying to create an instance but don't have the right.
	 * @throws InvocationTargetException when the constructor throws an exception.
	 */
	public static <T> T instantiateClass(Class<T> classRef, Object... constructorParameters) 
		throws SecurityException, NoSuchMethodException, IllegalArgumentException, 
			InstantiationException, IllegalAccessException, InvocationTargetException { 
		if (classRef == null)
			throw new IllegalArgumentException(MSG_NULL_REF);
		
		if (constructorParameters == null) 
			return instantiateClass(classRef);
				
		// Find a constructor matching the given parameters (constructors' ambiguity is impossible)
		Constructor<?>[] constructors = classRef.getConstructors();
		
		for (Constructor<?> constructor : constructors) {
			
			if (constructor == null) continue;
			
			Class<?>[] parameterTypes = constructor.getParameterTypes();			
			if (parameterTypes.length != constructorParameters.length) continue;

			boolean matches = true;
			for (int i = 0; i < parameterTypes.length; i++) {
				
				Class<?> paramType = parameterTypes[i];
				Object constructorParameter = constructorParameters[i];
				
				if (!paramType.isInstance(constructorParameter)) {
					
					matches = false;
					break;
				}
			}
			
			if (matches)
				return classRef.cast(constructor.newInstance(constructorParameters));
		}
		throw new OkapiException(String.format(MSG_CANT_INSTANTIATE, classRef.getName()));
	}
	
	/**
	 * Creates a new instance of the class with a given class name and initialization parameters.
	 * @param className The given class name
	 * @param constructorParameters The initialization parameters for the class constructor
	 * @return A newly created instance of the desired class
	 * @throws SecurityException if we don't have the right to create the class
	 * @throws NoSuchMethodException when a constructor cannot be found
	 * @throws IllegalArgumentException if the class name is null or empty
	 * @throws InstantiationException when we fail to create an instance.
	 * @throws IllegalAccessException when trying to create an instance but don't have the right.
	 * @throws InvocationTargetException when the constructor throws an exception.
	 * @throws ClassNotFoundException when no definition for the class was found
	 */
	public static Object instantiateClass(String className, Object... constructorParameters) 
		throws SecurityException, NoSuchMethodException, IllegalArgumentException, 
			InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
		
		if (Util.isEmpty(className))
			throw new IllegalArgumentException(MSG_EMPTY_CLASSNAME);
		
		Class<?> ref = Class.forName(className);		
		return ref.cast(instantiateClass(ref, constructorParameters));
	}

	/**
	 * Creates a new instance of the class with a given class name and initialization parameters using a given class loader.
	 * @param className The given class name
	 * @param classLoader The given class loader
	 * @param constructorParameters The initialization parameters for the class constructor
	 * @return A newly created instance of the desired class
	 * @throws SecurityException if we don't have the right to create the class
	 * @throws NoSuchMethodException when a constructor cannot be found
	 * @throws IllegalArgumentException if the class name is null or empty, or if the classLoader is null.
	 * @throws InstantiationException when we fail to create an instance.
	 * @throws IllegalAccessException when trying to create an instance but don't have the right.
	 * @throws InvocationTargetException when the constructor throws an exception.
	 * @throws ClassNotFoundException when no definition for the class was found
	 */
	public static Object instantiateClass(String className, ClassLoader classLoader, Object... constructorParameters) 
	throws SecurityException, NoSuchMethodException, IllegalArgumentException, 
		InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException {
	
		if (Util.isEmpty(className))
			throw new IllegalArgumentException(MSG_EMPTY_CLASSNAME);
		
		if (classLoader == null)
			throw new IllegalArgumentException(MSG_NULL_LOADER);
		
		Class<?> ref = classLoader.loadClass(className); 		
		return ref.cast(instantiateClass(ref, constructorParameters));
	}

	/**
	 * Gets a full path of a given resource. 
	 * @param cls Class containing the given resource.
	 * @param resourceName Name of the given resource. Should be prefixed with a leading slash ("/name.ext") 
	 * if the resource is located in the class root. If the resource is located in the same
	 * package as the class, then no leading slash is needed ("name.ext"). 
	 * @return Full path of the given resource.
	 */
	public static String getResourcePath(Class<?> cls, String resourceName) {
		return new File(Util.URLtoURI(cls.getResource(resourceName))).getPath();
	}
	
	/**
	 * Gets the pathname parent of a given resource. 
	 * @param cls Class containing the given resource.
	 * @param resourceName Name of the given resource. Should be prefixed with a leading slash ("/name.ext") 
	 * if the resource is located in the class root. If the resource is located in the same
	 * package as the class, then no leading slash is needed ("name.ext"). 
	 * @return The given resource's pathname parent.
	 */
	public static String getResourceParent(Class<?> cls, String resourceName) {
// 		failsafe plugin and other cases when resource is in jar: <<< ERROR! java.lang.IllegalArgumentException: URI is not hierarchical
		LOGGER.trace("resourceName: {}", resourceName);
		LOGGER.trace("class: {}" ,cls);
		
		if (isInJar(cls, resourceName)) {
			URL url = cls.getResource(resourceName);
			try {
				String path = url.getPath();
				int index = path.indexOf('!');
				URI uri = new URI(path.substring(0, index));
				File file = new File(uri);
				return file.getAbsolutePath();
			} catch (URISyntaxException e) {
				return "";
			}			
		}
		else {
			URI uri = Util.URLtoURI(cls.getResource(resourceName));
			return new File(uri).getParent();
		}
	}
	
	/**
	 * Detect if a given class is located in a JAR.
	 * @param cls the given class.
	 * @param resPath resource path inside the jar
	 * @return true if the class is located in a JAR.
	 */
	public static boolean isInJar(Class<?> cls, String resPath) {
		URL url = cls.getResource(resPath);
		if (url == null && !resPath.startsWith("/")) {
			resPath = "/" + resPath;
			url = cls.getResource(resPath);
		}
		if (url == null) return false;
		return "jar".equalsIgnoreCase(url.getProtocol());
	}
	
	public static String buildClassPath(String... paths) {
		StringBuilder sb = new StringBuilder();
		for (String path : paths) {
			if (sb.length() > 0) sb.append(File.pathSeparator);
			File file = new File(path);
			try {
				sb.append(URLDecoder.decode(file.getAbsolutePath(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new OkapiException(e);
			}
		}
		String res = sb.toString();
		if (RegexUtil.contains(res, SPACES)) res = String.format("\"%s\"", res);
		System.out.println(res);
		return res;
	}
	
	public static String getPath(Class<?> cls) {
		return cls.getProtectionDomain().getCodeSource().getLocation().getPath();
	}
	
	public static String getClassFilePath(Class<?> cls) {
		return getPath(cls) + cls.getName().replace('.', '/').concat(".class");
	}
	
	
	public static String buildClassPath(Class<?>... classes) {
		List<String> paths = new ArrayList<String>(classes.length);
		for (Class<?> cls : classes) {
			paths.add(getPath(cls));
		}
		return buildClassPath(ListUtil.stringListAsArray(paths));		
	}
}
