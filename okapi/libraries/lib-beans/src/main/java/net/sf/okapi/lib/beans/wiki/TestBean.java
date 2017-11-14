/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.wiki;

public class TestBean {
	
	private int data;
	private boolean ready;
	
	public TestBean() { // No-arguments constructor
		super();
		data = -1;
		ready = false;
	}
	
	public int getData() { // Getter for the private data field
		return data;
	}
	
	public void setData(int data) { // Setter for the private data field
		this.data = data;
	}
	
	public boolean isReady() { // Getter for the private ready field
		return ready;
	}
	
	public void setReady(boolean ready) { // Setter for the private ready field
		this.ready = ready;
	}
}
