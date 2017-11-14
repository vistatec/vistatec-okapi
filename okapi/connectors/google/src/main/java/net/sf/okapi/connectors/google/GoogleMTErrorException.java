/*===========================================================================
  Copyright (C) 2017 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.google;

import net.sf.okapi.common.exceptions.OkapiException;

public class GoogleMTErrorException extends OkapiException {
	
	private static final long serialVersionUID = 1L;

	private final int code;
    private final String message, domain, reason, query;

    public GoogleMTErrorException(int code, String message, String domain, String reason, String query) {
        super(String.format("Error: response code %d - %s", code, message));
        this.code = code;
        this.message = message;
        this.domain = domain;
        this.reason = reason;
        this.query = query;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDomain() {
        return domain;
    }

    public String getReason() {
        return reason;
    }

    public String getQuery() {
        return query;
    }
}
