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
 * Signals that a non-implemented method was called, or a non-implemented feature
 * was invoked. This is generally due to a class that cannot implement a method 
 * of an Okapi interface because of specific requirement.
 */
public class OkapiNotImplementedException extends OkapiException {

	private static final long serialVersionUID = -1943082812163691869L;

	/**
	 * Creates an empty new OkapiNotImplementedException object.
	 */
	public OkapiNotImplementedException () {
		super();
	}

	/**
	 * Creates a new OkapiNotImplementedException object with a given message.
	 * @param message text of the message.
	 */
	public OkapiNotImplementedException (String message) {
		super(message);
	}

	/**
	 * Creates a new OkapiNotImplementedException object with a given parent 
	 * exception cause.
	 * @param cause the parent exception cause.
	 */
	public OkapiNotImplementedException (Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new OkapiNotImplementedException object with a given message and 
	 * a given parent exception cause.
	 * @param message the message.
	 * @param cause the cause.
	 */
	public OkapiNotImplementedException (String message, Throwable cause) {
		super(message, cause);	
	}

}
