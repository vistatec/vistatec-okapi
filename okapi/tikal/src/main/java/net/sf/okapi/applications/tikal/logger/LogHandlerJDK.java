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

import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

class LogHandlerJDK extends Handler implements ILogHandler {
	private PrintStream       ps;

	protected LogHandlerJDK(){}

	public void initialize(PrintStream ps) {
		if( ps == null ) return;

		this.ps = ps;
		this.setLevel(Level.INFO);

		// Disable root console handler
		Handler[] handlers = Logger.getLogger("").getHandlers();
		for ( Handler handler : handlers )
			Logger.getLogger("").removeHandler(handler);

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
		if( ps == null ) return;

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
			ps.println("Error: " + record.getMessage());
			Throwable e = record.getThrown();
			if ( e != null ) {
				ps.println(e.getMessage());
				ps.println(" @ "+e.toString()); //$NON-NLS-1$
			}
		}
		else if ( lev == Level.WARNING ) {
			// Filter out Axis warnings
			if ( "org.apache.axis.utils.JavaUtils".equals(record.getLoggerName()) ) return;
			// Otherwise print
			ps.println("Warning: " + record.getMessage());
		}
		else if ( lev == Level.INFO )
			ps.println(record.getMessage());
		else // below INFO
			ps.println("Trace: " + record.getMessage());
	}
}
