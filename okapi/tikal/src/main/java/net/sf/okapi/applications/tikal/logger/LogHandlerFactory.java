/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.applications.tikal.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogHandlerFactory {
	static public ILogHandler getLogHandler() {
		Logger localLogger = LoggerFactory.getLogger(LogHandlerFactory.class);
		String realLogger = localLogger.getClass().getName();
		if( "org.slf4j.impl.JDK14LoggerAdapter".equals(realLogger) )
			return new LogHandlerJDK();
		return new LogHandlerNop();
	}
}
