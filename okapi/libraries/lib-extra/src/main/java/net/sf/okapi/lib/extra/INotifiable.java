package net.sf.okapi.lib.extra;


/**
 * Interface for enabling objects to receive notifications from other objects
 * 
 * @version 0.1, 16.06.2009
 */

public interface INotifiable {

	/**
	 * Sends a command for the object to react
	 * @param command a string identifying the command
	 * @param info command-specific  object 
	 * @return true if command has been executed and doesn't need to be sent on
	 */
	boolean exec(Object sender, String command, Object info);
	
}
