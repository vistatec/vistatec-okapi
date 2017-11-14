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
 * Signals a general error related to a filter operation.
 */
public class OkapiIllegalFilterOperationException extends OkapiException {

	private static final long serialVersionUID = 5617083235059687346L;

	/**
	 * Creates an empty new OkapiIllegalFilterOperationException object.
	 */
	public OkapiIllegalFilterOperationException () {
		super();
	}
	
	/**
	 * Creates a new OkapiIllegalFilterOperationException object with a given message.
	 * @param message text of the message.
	 */
	public OkapiIllegalFilterOperationException (String message) {
		super(message);
	}
	
	/**
	 * Creates a new OkapiIllegalFilterOperationException object with a given parent 
	 * exception cause.
	 * @param cause the parent exception cause.
	 */
	public OkapiIllegalFilterOperationException (Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new OkapiIllegalFilterOperationException object with a given message and 
	 * a given parent exception cause.
	 * @param message the message.
	 * @param cause the cause.
	 */
	public OkapiIllegalFilterOperationException (String message, Throwable cause) {
		super(message, cause);
	}

}

