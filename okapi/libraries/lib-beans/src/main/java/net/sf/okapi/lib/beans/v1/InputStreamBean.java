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

package net.sf.okapi.lib.beans.v1;

import java.io.IOException;
import java.io.InputStream;

import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class InputStreamBean extends PersistenceBean<InputStream> {

	private byte[] data;

	@Override
	protected InputStream createObject(IPersistenceSession session) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void fromObject(InputStream obj, IPersistenceSession session) {
		boolean markSupported = obj.markSupported();
		if (markSupported)
			obj.mark(Integer.MAX_VALUE);
		
		try {
			data = StreamUtil.inputStreamToBytes(obj); // data.length
			
		} catch (IOException e1) {
			// TODO Handle exception
			e1.printStackTrace();
		}

		if (markSupported)
			try {
				obj.reset();
			} catch (IOException e) {
				// TODO Handle exception
				e.printStackTrace();
			}
	}

	@Override
	protected void setObject(InputStream obj, IPersistenceSession session) {
		// TODO Auto-generated method stub
		
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}
}
