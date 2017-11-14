package net.sf.okapi.lib.terminology;

import java.io.File;
import java.io.InputStream;

/**
 * Provides read access to various glossary formats using a common set of methods.
 */
public interface IGlossaryReader {

	/**
	 * Opens a glossary from a File.
	 * @param file the glossary file.
	 */
	public void open (File file);
	
	/**
	 * Opens a glossary from an input stream.
	 * @param input the input to read.
	 */
	public void open (InputStream input);
	
	/**
	 * Close the glossary reader.
	 */
	public void close ();
	
	/**
	 * Indicates if there is another entry.
	 * @return true if there is another entry, false otherwise.
	 */
	public boolean hasNext ();
	
	/**
	 * Gets the next entry in this glossary.
	 * @return the next entry in this glossary.
	 */
	public ConceptEntry next ();
	
}
