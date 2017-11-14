/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.common.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import net.sf.okapi.common.ClassInfo;
import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.DefaultFilenameFilter;
import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IEmbeddableParametersEditor;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

/**
 * Provides a way to discover and list plug-ins for a given location or file.   
 */
public class PluginsManager {

	private ArrayList<URL> urls = new ArrayList<URL>();
	private List<PluginItem> plugins = new ArrayList<PluginItem>();
	private URLClassLoader loader;
	private File pluginsDir;
	private ClassLoader parentClassLoader = null;
	
	/**
	 * Create a PluginsManager that uses the current thread's context ClassLoader
	 * when loading plugins.
	 */
	public PluginsManager() {
		this(Thread.currentThread().getContextClassLoader());
	}
	
	/**
	 * Create a PluginsManager that uses the specified ClassLoader when 
	 * loading plugins.
	 * 
	 * @param parentClassLoader ClassLoader to be used as the parent of any
	 * 			ClassLoaders used to load plugins. 
	 */
	public PluginsManager(ClassLoader parentClassLoader) {
		this.parentClassLoader = parentClassLoader;
	}
	
	/**
	 * Explores the given file or directory for plug-ins and add them to
	 * this manager.
	 * @param pluginsDir the directory where the plugins are located.
	 * @param append true to preserve any plug-ins already existing in this
	 * manager, false to reset and start with no plug-in.
	 */
	public void discover (File pluginsDir,
		boolean append)
	{
		try {			
			if ( pluginsDir == null ) return;
			if ( !pluginsDir.isDirectory() ) return;

			this.pluginsDir = pluginsDir;
			
			if ( !append ) {
				urls.clear();
			}
			loader = null;
	
			// The plug-ins directory can contain single plug-in jars, and/or first-level sub-directories containing plug-in jars
			FilenameFilter filter = new DefaultFilenameFilter(".jar");
			
			// Inspect single jars
			File[] files = pluginsDir.listFiles(filter);
			for ( File file : files ) {
				// Skip over any sub-directories in the plugins directory 
				if ( file.isDirectory() ) continue;
				inspectFile(file);
			}
			
			// Inspect sub-directories entries
			File[] dirs = pluginsDir.listFiles();
			for ( File dir : dirs ) {
				// Skip over any file at that level
				if ( !dir.isDirectory() ) continue;
				// Else explore all .jar just under the sub-folder
				files = dir.listFiles(filter);
				for ( File file : files ) {
					inspectFile(file);
				}
			}

			// Set the loader
			if ( urls.size() > 0 ) {
				final URL[] tmp = urls.toArray(new URL[urls.size()]);
				loader = AccessController.doPrivileged(
				        new PrivilegedAction<URLClassLoader>() {
						      public URLClassLoader run() {
						        return new URLClassLoader(tmp, parentClassLoader);
						      }
				        }
				);
			}
			
			// Associate the editor-type plugins with their action-type plugins
			for ( PluginItem item1 : plugins ) {
				Class<?> cls1 = Class.forName(item1.className, false, loader);
				switch ( item1.type ) {
				case PluginItem.TYPE_IFILTER:
				case PluginItem.TYPE_IPIPELINESTEP:
				case PluginItem.TYPE_IQUERY:
					// Get the getParameters() method
					UsingParameters usingParams = cls1.getAnnotation(UsingParameters.class);
					if ( usingParams == null ) continue;
					// Skip if the class does not use parameters
					if ( usingParams.value().equals(IParameters.class) ) continue;
					// Look at all plug-ins to see if any can be associated with that type
					for ( PluginItem item2 : plugins ) {
						switch ( item2.type ) {
						case PluginItem.TYPE_IPARAMETERSEDITOR:
						case PluginItem.TYPE_IEMBEDDABLEPARAMETERSEDITOR:
						case PluginItem.TYPE_IEDITORDESCRIPTIONPROVIDER:
							Class<?> cls2 = Class.forName(item2.className, false, loader);
							// Get the type of parameters for which this editor works  
							EditorFor editorFor = cls2.getAnnotation(EditorFor.class);
							if ( editorFor == null ) continue;
							if ( editorFor.value().equals(usingParams.value()) ) {
								if ( IParametersEditor.class.isAssignableFrom(cls2) ) {
									item1.paramsEditor = new ClassInfo(item2.className, loader);
								}
								if ( IEmbeddableParametersEditor.class.isAssignableFrom(cls2) ) {
									item1.embeddableParamsEditor = new ClassInfo(item2.className, loader);
								}
								if ( IEditorDescriptionProvider.class.isAssignableFrom(cls2) ) {
									item1.editorDescriptionProvider = new ClassInfo(item2.className, loader);
								}
							}
							cls2 = null; // Try to help unlocking the file
							break;
						}
					}
					break;
				}
				cls1 = null; // Try to help unlocking the file
			}
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiException("Class not found", e);
		}
		catch ( SecurityException e ) {
			throw new OkapiException("Error when looking for getParameters() method.", e);
		}
		finally {
			System.gc(); // Try freeing locks as soon as possible
		}
	}
	
	/**
	 * Gets the list of the class names of all available plug-ins 
	 * of a given type currently available in this manager.
	 * The method {@link #discover(File, boolean)} must be called once before
	 * calling this method.
	 * @param type the type of plug-ins to list.
	 * @return the list of available plug-ins for the given type.
	 */
	public List<String> getList (int type) {
		ArrayList<String> list = new ArrayList<String>();
		for ( PluginItem item : plugins ) {
			if ( item.type == type ) list.add(item.className);
		}
		return list;
	}

	/**
	 * Gets the list of all the plug-ins currently in this manager.
	 * @return the list of all the plug-ins currently in this manager.
	 */
	public List<PluginItem> getList () {
		return plugins;
	}
	
	/**
	 * Gets the list of URLs of the jars containing plug-ins currently in this manager. 
	 * @return the list of URLs.
	 */
	public ArrayList<URL> getURLs() {
		return urls;
	}

	/**
	 * Gets the URLClassLoader to use for creating new instance of the
	 * components listed in this manager. 
	 * The method {@link #discover(File, boolean)} must be called once before
	 * calling this method.
	 * @return the URLClassLoader for this manager.
	 */
	public URLClassLoader getClassLoader () {
		return loader;
	}
	
	private void inspectFile (File file) {
		try {
			// Make sure there is something to discover
			if (( file == null ) || !file.exists() ) return;
			
			// Create a temporary class loader
			URL[] tmpUrls = new URL[1]; 
			URL url = file.toURI().toURL();
			tmpUrls[0] = url;
			URLClassLoader loader = URLClassLoader.newInstance(tmpUrls, parentClassLoader);
		
			// Introspect the classes
			FileInputStream fis = new FileInputStream(file);
			JarInputStream jarFile = new JarInputStream(fis);
			JarEntry entry;
			Class<?> cls = null;
			while ( true ) {
				cls = null; // Try to help unlocking the file
				if ( (entry = jarFile.getNextJarEntry()) == null ) break;
				String name = entry.getName();
				if ( name.endsWith(".class") ) {
					name = name.substring(0, name.length()-6).replace('/', '.');
					try {
						cls = Class.forName(name, false, loader);
						// Skip interfaces
						if ( cls.isInterface() ) continue;
						// Skip abstract
						if ( Modifier.isAbstract(cls.getModifiers()) ) continue;
						// Check class type
						if ( IFilter.class.isAssignableFrom(cls) ) {
							// Skip IFilter classes that should not be used directly
							if ( cls.getAnnotation(UsingParameters.class) == null ) continue;
							if ( !urls.contains(url) ) urls.add(url);
							plugins.add(new PluginItem(PluginItem.TYPE_IFILTER, name));
						}
						else if ( IPipelineStep.class.isAssignableFrom(cls) ) {
							// Skip IPipelineStep classes that should not be used directly
							if ( cls.getAnnotation(UsingParameters.class) == null ) continue;
							if ( !urls.contains(url) ) urls.add(url);
							plugins.add(new PluginItem(PluginItem.TYPE_IPIPELINESTEP, name));
						}
						else if ( IParametersEditor.class.isAssignableFrom(cls) ) {
							// Skip IParametersEditor classes that should not be used directly
							if ( cls.getAnnotation(EditorFor.class) == null ) continue;
							if ( !urls.contains(url) ) urls.add(url);
							plugins.add(new PluginItem(PluginItem.TYPE_IPARAMETERSEDITOR, name));
						}
						else if ( IEmbeddableParametersEditor.class.isAssignableFrom(cls) ) {
							// Skip IEmbeddableParametersEditor classes that should not be used directly
							if ( cls.getAnnotation(EditorFor.class) == null ) continue;
							if ( !urls.contains(url) ) urls.add(url);
							plugins.add(new PluginItem(PluginItem.TYPE_IEMBEDDABLEPARAMETERSEDITOR, name));
						}
						else if ( IEditorDescriptionProvider.class.isAssignableFrom(cls) ) {
							// Skip IEditorDescriptionProvider classes that should not be used directly
							if ( cls.getAnnotation(EditorFor.class) == null ) continue;
							if ( !urls.contains(url) ) urls.add(url);
							plugins.add(new PluginItem(PluginItem.TYPE_IEDITORDESCRIPTIONPROVIDER, name));
						}
						else if ( IQuery.class.isAssignableFrom(cls) ) {
							// Skip IQuery classes that should not be used directly
							if ( cls.getAnnotation(UsingParameters.class) == null ) continue;
							if ( !urls.contains(url) ) urls.add(url);
							plugins.add(new PluginItem(PluginItem.TYPE_IQUERY, name));
						}
					}
					catch ( Throwable e ) {
						// If the class cannot be create for some reason, we skip it silently
					}
					cls = null; // Try to help unlocking the file
				}
			}
			if ( jarFile != null ) {
				jarFile.close();
				jarFile = null; // Try to help unlocking the file
				fis.close();
				fis = null; // Try to help unlocking the file
				file = null; // Try to help unlocking the file
			}
			cls = null; // Try to help unlocking the file
			loader = null; // Try to help unlocking the file
		}
		catch ( IOException e ) {
			throw new OkapiException("IO error when inspecting a file for plugins.", e);
		}
	}

	/**
	 * Gets the directory where the plug-ins are located.
	 * @return directory path.
	 */
	public File getPluginsDir() {
		return pluginsDir;
	}
	
	public void releaseClassLoader() {
		closeOpenJars(loader);
		loader = null;
	}

	/**
	 * Workaround for non-released jar file lock by URLClassLoader
	 * http://loracular.blogspot.com/2009/12/dynamic-class-loader-with.html
	 * @param classLoader the {@link ClassLoader} to use.
	 */
	@SuppressWarnings("rawtypes")
	public static void closeOpenJars(ClassLoader classLoader) {
		if (!(classLoader instanceof URLClassLoader)) return;
		try {
		   Class clazz = java.net.URLClassLoader.class;
		   java.lang.reflect.Field ucp = clazz.getDeclaredField("ucp");
		   ucp.setAccessible(true);
		   Object sun_misc_URLClassPath = ucp.get(classLoader);
		   java.lang.reflect.Field loaders = 
		      sun_misc_URLClassPath.getClass().getDeclaredField("loaders");
		   loaders.setAccessible(true);
		   Object java_util_Collection = loaders.get(sun_misc_URLClassPath);
		   for (Object sun_misc_URLClassPath_JarLoader :
		        ((java.util.Collection) java_util_Collection).toArray()) {
		      try {
		         java.lang.reflect.Field loader = 
		            sun_misc_URLClassPath_JarLoader.getClass().getDeclaredField("jar");
		         loader.setAccessible(true);
		         Object java_util_jar_JarFile = 
		            loader.get(sun_misc_URLClassPath_JarLoader);
		         java.util.jar.JarFile jarFile = (java.util.jar.JarFile) java_util_jar_JarFile;		         
		         jarFile.close();
		         cleanupJarFileFactory(jarFile.getName());
		      } catch (Throwable t) {
		         // if we got this far, this is probably not a JAR loader so skip it
		      }
		   }
		} catch (Throwable t) {
		   // probably not a SUN VM
		}
	}

	/**
	 * cleanup jar file factory cache
	 * http://loracular.blogspot.com/2009/12/dynamic-class-loader-with.html
	 * @param jarNames the names of the jar files.
	 * @return true if successful, false otherwise.
	 */
	@SuppressWarnings("rawtypes")
	public static boolean cleanupJarFileFactory (String... jarNames) {
		List<String> setJarFileNames2Close = ListUtil.arrayAsList(jarNames);  
		boolean res = false;
		Class<?> classJarURLConnection = null;
		classJarURLConnection = ClassUtil.getClass("sun.net.www.protocol.jar.JarURLConnection");
		if (classJarURLConnection == null) {
			return res;
		}
		Field f = null;
		try {
			f = classJarURLConnection.getDeclaredField("factory");
		} catch (NoSuchFieldException e) {
			//ignore
		}
		if (f == null) {
			return res;
		}
		f.setAccessible(true);
		Object obj = null;
		try {
			obj = f.get(null);
		} catch (IllegalAccessException e) {
			//ignore
		}
		if (obj == null) {
			return res;
		}
		Class<?> classJarFileFactory = obj.getClass();
		//
		HashMap fileCache = null;
		try {
			f = classJarFileFactory.getDeclaredField("fileCache");
			f.setAccessible(true);
			obj = f.get(null);
			if (obj instanceof HashMap) {
				fileCache = (HashMap)obj;
			}
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
			//ignore
		}
		HashMap urlCache = null;
		try {
			f = classJarFileFactory.getDeclaredField("urlCache");
			f.setAccessible(true);
			obj = f.get(null);
			if (obj instanceof HashMap) {
				urlCache = (HashMap)obj;
			}
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
			//ignore
		}
		if (urlCache != null) {
			HashMap urlCacheTmp = (HashMap)urlCache.clone();
			Iterator it = urlCacheTmp.keySet().iterator();
			while (it.hasNext()) {
				obj = it.next();
				if (!(obj instanceof JarFile)) {
					continue;
				}
				JarFile jarFile = (JarFile)obj;
				if (setJarFileNames2Close.contains(jarFile.getName())) {
					try {
						jarFile.close();
					} catch (IOException e) {
						//ignore
					}
					if (fileCache != null) {
						fileCache.remove(urlCache.get(jarFile));
					}
					urlCache.remove(jarFile);
				}
			}
			res = true;
		} else if (fileCache != null) {
			// urlCache := null
			HashMap fileCacheTmp = (HashMap)fileCache.clone();
			Iterator it = fileCacheTmp.keySet().iterator();
			while (it.hasNext()) {
				Object key = it.next();
				obj = fileCache.get(key);
				if (!(obj instanceof JarFile)) {
					continue;
				}
				JarFile jarFile = (JarFile)obj;
				if (setJarFileNames2Close.contains(jarFile.getName())) {
					try {
						jarFile.close();
					} catch (IOException e) {
						//ignore
					}
					fileCache.remove(key);
				}
			}
			res = true;
		}
		setJarFileNames2Close.clear();
		return res;
	  }
	
}
