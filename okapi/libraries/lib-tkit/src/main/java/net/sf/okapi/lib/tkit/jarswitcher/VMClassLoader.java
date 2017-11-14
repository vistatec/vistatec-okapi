package net.sf.okapi.lib.tkit.jarswitcher;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;

import net.sf.okapi.common.FileUtil;

/**
 * Blocks delegation to the parent, first tries to find a class itself, and only if not found, asks the parent.
 */
public class VMClassLoader extends URLClassLoader {
	
	private String appRootName;
	private Class<?> appRoot;

	// http://docs.oracle.com/javase/7/docs/api/java/lang/ClassLoader.html#getSystemClassLoader%28%29
	public VMClassLoader(ClassLoader parent) {
		super(new URL[] {}, parent);
		
		// We cannot pass a JSON string here as we cannot depend on external libs yet
		DataInputStream dis = new DataInputStream(System.in);
		try {
			String path = dis.readUTF();
			addURL(FileUtil.fileToUrl(new File(path)));
			System.out.println(path);
			appRootName = dis.readUTF();
			System.out.println("appRootName: " + appRootName);
			final URL origClassURL = FileUtil.fileToUrl(new File(dis.readUTF()));
			System.out.println("Orig Class URL: " + origClassURL);
			
			final URL classURL = FileUtil.fileToUrl(new File(dis.readUTF()));
			System.out.println("class URL: " + classURL);
			
			AccessController.doPrivileged(				
	                new PrivilegedExceptionAction<Class<?>>() {
	                    public Class<?> run() throws ClassNotFoundException {
	                    	try {
	                    		URLConnection connection = classURL.openConnection();
		                        InputStream input = connection.getInputStream();
		                        try {
		                        	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		                            int data = input.read();

		                            while(data != -1){
		                                buffer.write(data);
		                                data = input.read();
		                            }
		                            byte[] classData = buffer.toByteArray();
		                            appRoot = defineClass(appRootName,
		                                    classData, 0, classData.length, 
		                                    new ProtectionDomain(new CodeSource(origClassURL, (Certificate[]) null), null));
		                            return appRoot;
		                        }
		                        finally {
		                        	input.close();
		                        }
	                    	} catch (IOException e) {
                                throw new ClassNotFoundException(appRootName, e);
                            }
	                    }
	                }, AccessController.getContext());
			
		} catch (Exception e) {
			// No URLs are added
			appRootName = null;
			appRoot = null;
		}
	}
	
	public VMClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}
	
	public VMClassLoader(URL[] urls) {
		super(urls);
	}
	
	String offset = "";
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		synchronized (getClassLoadingLock(name)) {			
			System.out.println(offset + "loading " + name);
			offset += " ";
			if (appRootName != null && 
					appRoot != null && 
					name.equals(appRootName)) {
				System.out.println(offset + "      !!! loaded self: " + appRootName);
				offset = offset.substring(0, offset.length() - 1);
				return appRoot;
			}				
			
			Class<?> cls = null;
			try {
				cls = findClass(name);
				if (cls != null) System.out.println(offset + "      found itself");
			} catch (Exception e) {
				System.out.println(offset + "      exception");
				cls = null;
			}
			
			if (cls == null && getParent() != null) {
				System.out.println(offset + "      loaded by parent");
				cls = getParent().loadClass(name);
			}
			if (cls == null) System.out.println(offset + "      !!! still null");
			offset = offset.substring(0, offset.length() - 1);
			return cls;
		}		
	}
	
	@Override
	public void addURL(URL url) {
		// Changes visibility from protected to public for dynamic class loading
		super.addURL(url);
	}

}
