/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

package net.sf.okapi.lib.tkit.jarswitcher;

import static org.junit.Assert.assertEquals;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import net.sf.okapi.common.ClassUtil;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TestVersionClass {

	@Test
	/**
	 * VersionClass in resources v1/, v2/, v3/ is different, returns a corresponding version number. 
	 */
	public void testDynamicClassLoading() 
			throws ClassNotFoundException, InstantiationException, 
			IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, 
			InvocationTargetException {
		final VersionManager vm = new VersionManager();
//		vm.load(this.getClass().getResource("test.json"));
		
		vm.add("v1", this.getClass().getResource("/v1/").getPath(), "2014-04-10");
		vm.add("v2", this.getClass().getResource("/v2/").getPath(), "2014-04-11");
		vm.add("v3", this.getClass().getResource("/v3/").getPath(), "2014-04-12");
		
		assertEquals(3, vm.getVersions().size());
		
		vm.loadVersion("v1");
		ClassLoader cl = vm.getClassLoader();
		Class<?> cls = cl.loadClass("net.sf.okapi.lib.tkit.jarswitcher.VersionClass");
		Object obj = cls.newInstance();
		Method m = cls.getDeclaredMethod("getVersion", new Class[]{});
		Object ret = m.invoke(obj, new Object[]{});
		assertEquals("Version 1", ret);
		
		vm.loadVersion("v2");
		cl = vm.getClassLoader();
		cls = cl.loadClass("net.sf.okapi.lib.tkit.jarswitcher.VersionClass");
		obj = cls.newInstance();
		m = cls.getDeclaredMethod("getVersion", new Class[]{});
		ret = m.invoke(obj, new Object[]{});
		assertEquals("Version 2", ret);
		
		vm.loadVersion("v3");
		cl = vm.getClassLoader();
		cls = cl.loadClass("net.sf.okapi.lib.tkit.jarswitcher.VersionClass");
		obj = cls.newInstance();
		m = cls.getDeclaredMethod("getVersion", new Class[]{});
		ret = m.invoke(obj, new Object[]{});
		assertEquals("Version 3", ret);
		
		vm.loadVersion("v1");
		cl = vm.getClassLoader();
		cls = cl.loadClass("net.sf.okapi.lib.tkit.jarswitcher.VersionClass");
		obj = cls.newInstance();
		m = cls.getDeclaredMethod("getVersion", new Class[]{});
		ret = m.invoke(obj, new Object[]{});
		assertEquals("Version 1", ret);
		
		// Static link to the class remains
		VersionClass vc = new VersionClass();
		assertEquals("Version 0", vc.getVersion());
	}

	@Ignore("Cuases maven to throw error: The forked VM terminated without properly saying goodbye. VM crash or System.exit called?")
	public void testReflectionClassLoading()
			throws ClassNotFoundException, InstantiationException, 
			IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, 
			InvocationTargetException {
		VersionManager vm = new VersionManager();
		
		vm.add("v1", this.getClass().getResource("/v1/").getPath(), "2014-04-10");
		vm.add("v2", this.getClass().getResource("/v2/").getPath(), "2014-04-11");
		vm.add("v3", this.getClass().getResource("/v3/").getPath(), "2014-04-12");
		
		assertEquals(3, vm.getVersions().size());
		
		vm.loadVersion("v1");
		ClassLoader cl = vm.getClassLoader();
		Class<?> cls = cl.loadClass(this.getClass().getName());
		Object obj = cls.newInstance();
		Method m = cls.getDeclaredMethod("main", new Class[]{String[].class});
		Thread.currentThread().setContextClassLoader(cl);
		m.invoke(obj, new Object[]{new String[]{}});
	}
	
	@Ignore("Cuases maven to throw error: The forked VM terminated without properly saying goodbye. VM crash or System.exit called?")
	public void testStaticClassLoading() throws IOException, InterruptedException, URISyntaxException {
		final VersionManager vm = new VersionManager();
		
		vm.add("v1", this.getClass().getResource("/v1/").getPath(), "2014-04-10");
		vm.add("v2", this.getClass().getResource("/v2/").getPath(), "2014-04-11");
		vm.add("v3", this.getClass().getResource("/v3/").getPath(), "2014-04-12");
		
		assertEquals(3, vm.getVersions().size());
		
		ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
		System.out.println("SysCL: " + sysClassLoader);
		ProcessBuilder pb =
				new ProcessBuilder(
						"java",
						"-cp",
						System.getProperty("java.class.path"), // classpath of this app
						"-Djava.system.class.loader=net.sf.okapi.lib.tkit.jarswitcher.VMClassLoader", 
						this.getClass().getName());
				
	   	pb.redirectOutput(Redirect.INHERIT);
	   	pb.redirectError(Redirect.INHERIT);
	   	Process p = pb.start();
	   	
	   	DataOutputStream dos = new DataOutputStream(p.getOutputStream());
	   	String appLibPath = this.getClass().getResource("/v2/").getPath();
	   	
	   	dos.writeUTF(appLibPath);
	   	dos.writeUTF(ClassUtil.getQualifiedClassName(this)); // appRootName
	   	dos.writeUTF(ClassUtil.getPath(this.getClass()));
	   	dos.writeUTF(ClassUtil.getClassFilePath(this.getClass()));
	   	dos.flush();
	   	
	   	System.out.println("Starting...\n");
	}
	
	public static void main(String[] args) {
		System.out.println("In main()");
		ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
		System.out.println("SysCL: " + sysClassLoader);
		
		VersionClass vc = new VersionClass();
		System.out.println("VersionClass CL: " + vc.getClass().getClassLoader());
		System.out.println(vc.getVersion());
		System.err.println("Test error");
		System.out.println("Success");
	}
}
