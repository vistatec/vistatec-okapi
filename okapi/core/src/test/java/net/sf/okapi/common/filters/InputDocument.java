package net.sf.okapi.common.filters;

import java.io.InputStream;

import net.sf.okapi.common.FileLocation;

public class InputDocument {

	public String path;
	public String paramFile;
	private Class<?> resClass;
	private String resName;
	
	public InputDocument (String path, String paramFile) {
		this(path, null, null, paramFile);
	}
	
	public InputDocument (Class<?> resClass, String resName, String paramFile) {
		this(null, resClass, resName, paramFile);
	}
	
	public InputDocument (String path, Class<?> resClass, String resName, String paramFile) {
		this.path = path;
		this.paramFile = paramFile;
		this.resClass = resClass;
		this.resName = resName;
	}

	public InputStream getInStream() {
		// We cannot just store the input stream, as we might need it several times, but it can get closed
		if (resClass == null) return null;
		InputStream is = FileLocation.fromClass(resClass).in(resName).asInputStream();
		// First we try the resource as is, then if not found in the classes's package, we look for it in the root
		if (is == null && !resName.startsWith("/")) {
			is = FileLocation.fromClass(resClass).in("/" + resName).asInputStream();
		}
		return is;
	}
}
