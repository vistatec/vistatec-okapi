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

package net.sf.okapi.persistence.xml.java.beans;

import java.beans.XMLEncoder;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.lib.persistence.IPersistenceBean;
import net.sf.okapi.lib.persistence.PersistenceSession;

public abstract class JavaBeansPersistenceSession extends PersistenceSession {

	private XMLEncoder encoder;
	
	@Override
	protected void endReading(InputStream inStream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void endWriting(OutputStream outStream) {
		encoder.close();		
	}

	@Override
	protected <T extends IPersistenceBean<?>> T readBean(Class<T> beanClass,
			String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void startReading(InputStream inStream) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void startWriting(OutputStream outStream) {
		encoder = new XMLEncoder(outStream);
	}

	@Override
	protected void writeBean(IPersistenceBean<?> bean, String name) {
		encoder.writeObject(bean);		
	}

	@Override
	public <T extends IPersistenceBean<?>> T convert(Object obj,
			Class<T> expectedClass) {
		return null;
	}

	@Override
	public String getMimeType() {
		return MimeTypeMapper.XML_MIME_TYPE;
	}
	
	@Override
	protected String writeBeanToString(IPersistenceBean<?> bean) {
		return null;
	}

	@Override
	protected <T extends IPersistenceBean<?>> T readBeanFromString(
			String content, Class<T> beanClass) {
		return null;
	}
}
