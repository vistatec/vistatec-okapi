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
 * Signals that a filter had an error when trying to open its input. This is likely
 * a problem in the type of input used to open the document.  
 */
public class OkapiBadFilterInputException extends OkapiException {

	private static final long serialVersionUID = 1122090108070908960L;

	/**
	 * Creates an empty new OkapiBadFilterInputException object.
	 */
	public OkapiBadFilterInputException () {
		super();
	}

	/**
	 * Creates a new OkapiBadFilterInputException object with a given message.
	 * @param message text of the message.
	 */
	public OkapiBadFilterInputException (String message) {
		super(message);		
	}

	/**
	 * Creates a new OkapiBadFilterInputException object with a given parent 
	 * exception cause.
	 * @param cause the parent exception cause.
	 */
	public OkapiBadFilterInputException (Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new OkapiBadFilterInputException object with a given message and 
	 * a given parent exception cause.
	 * @param message the message.
	 * @param cause the cause.
	 */
	public OkapiBadFilterInputException (String message, Throwable cause) {
		super(message, cause);
	}

}
