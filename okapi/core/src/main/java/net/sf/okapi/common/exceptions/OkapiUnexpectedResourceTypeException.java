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
===========================================================================*/

package net.sf.okapi.common.exceptions;

/**
 * Signals that a method has returned a resource in an unexpected type. Usually this
 * is coming from requesting a specific type of resource from an Event that carries
 * another type of resource.  
 */
public class OkapiUnexpectedResourceTypeException extends OkapiException {

	private static final long serialVersionUID = 5080370613358911235L;

	/**
	 * Creates an empty new OkapiEditorCreationException object.
	 */
	public OkapiUnexpectedResourceTypeException () {
		super();
	}

	/**
	 * Creates a new OkapiEditorCreationException object with a given message.
	 * @param message text of the message.
	 */
	public OkapiUnexpectedResourceTypeException (String message) {
		super(message);
	}

	/**
	 * Creates a new OkapiEditorCreationException object with a given parent 
	 * exception cause.
	 * @param cause the parent exception cause.
	 */
	public OkapiUnexpectedResourceTypeException (Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new OkapiEditorCreationException object with a given message and 
	 * a given parent exception cause.
	 * @param message the message.
	 * @param cause the cause.
	 */
	public OkapiUnexpectedResourceTypeException (String message, Throwable cause) {
		super(message, cause);
	}
}
