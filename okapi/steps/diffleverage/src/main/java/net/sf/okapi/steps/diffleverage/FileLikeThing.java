package net.sf.okapi.steps.diffleverage;

import java.io.File;
import java.net.URI;

/**
 * Wrapper for any type of class that acts like a {@link File}
 * @author HARGRAVEJE
 *
 * @param <T> A class that abstracts a {@link File}. 
 */
public class FileLikeThing<T> {
	private URI path;
	private T fileLikeThing;
	
	public FileLikeThing(URI path, T fileLikeThing) {
		this.path = path;
		this.fileLikeThing = fileLikeThing;
	}
	
	public URI getPath() {
		return path;
	}
	
	public T getFileLikeThing() {
		return fileLikeThing;
	}	
	
	@Override
	public String toString() {
		return path.toString();
	}
}
