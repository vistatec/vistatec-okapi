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

package net.sf.okapi.applications.rainbow.logger;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import net.sf.okapi.applications.rainbow.lib.ILog;

class LogHandlerJDK extends Handler implements ILogHandler {
	private ILog       log;

	protected LogHandlerJDK(){}

	public void initialize(ILog log) {
		if( log == null ) return;

		this.log = log;
		this.setLevel(Level.INFO);
		Logger.getLogger("").addHandler(this); //$NON-NLS-1$
	}

	public void setLogLevel(int level) {
		switch ( level ) {
			case LogLevel.DEBUG:
				this.setLevel(Level.FINE);
				Logger.getLogger("").setLevel(Level.FINE);
				break;
			case LogLevel.TRACE:
				this.setLevel(Level.FINEST);
				Logger.getLogger("").setLevel(Level.FINEST);
				break;
			default:
				this.setLevel(Level.INFO);
				Logger.getLogger("").setLevel(Level.INFO);
				break;
		}
	}

	@Override
	public void close() {
		// Do nothing
	}

	@Override
	public void flush() {
		// Do nothing
	}

	@Override
	public void publish(LogRecord record) {
		if( log == null ) return;

		/*
		 * JDK native levels:
		 *    Level.OFF     = Integer.MAX_VALUE;
		 *    Level.SEVERE  = 1000;
		 *    Level.WARNING =  900;
		 *    Level.INFO    =  800;
		 *    Level.CONFIG  =  700;
		 *    Level.FINE    =  500;
		 *    Level.FINER   =  400;
		 *    Level.FINEST  =  300;
		 *    Level.ALL     = Integer.MIN_VALUE;
		 */

		Level lev = record.getLevel();
		if ( lev == Level.SEVERE ) {
			log.error(record.getMessage());
			Throwable e = record.getThrown();
			if ( e != null ) {
				log.message(" @ "+e.toString()); //$NON-NLS-1$
			}
		}
		else if ( lev == Level.WARNING ) {
			// Filter out Axis warnings
			if ( "org.apache.axis.utils.JavaUtils".equals(record.getLoggerName()) ) return;
			// Otherwise print
			log.warning(record.getMessage());
		}
		else // INFO and below
			log.message(record.getMessage());
	}
}
