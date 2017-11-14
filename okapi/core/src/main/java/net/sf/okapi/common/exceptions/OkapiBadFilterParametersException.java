/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.common.exceptions;

/**
 * Signals that filter parameters are either null or not an instance of the expected class. 
 * This is likely a wrong value passed with setParameters().
 * The exception is thrown when an input is open.
 */
public class OkapiBadFilterParametersException extends OkapiException {

	private static final long serialVersionUID = 2577656170650825842L;
	
	/**
	 * Creates an empty new OkapiBadFilterParametersException object.
	 */
	public OkapiBadFilterParametersException () {
		super();
	}

	/**
	 * Creates a new OkapiBadFilterParametersException object with a given message.
	 * @param message text of the message.
	 */
	public OkapiBadFilterParametersException (String message) {
		super(message);		
	}

	/**
	 * Creates a new OkapiBadFilterParametersException object with a given parent 
	 * exception cause.
	 * @param cause the parent exception cause.
	 */
	public OkapiBadFilterParametersException (Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new OkapiBadFilterParametersException object with a given message and 
	 * a given parent exception cause.
	 * @param message the message.
	 * @param cause the cause.
	 */
	public OkapiBadFilterParametersException (String message, Throwable cause) {
		super(message, cause);
	}

}
