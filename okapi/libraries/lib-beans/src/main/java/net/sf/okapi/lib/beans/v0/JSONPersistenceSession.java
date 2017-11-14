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

package net.sf.okapi.lib.beans.v0;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.exceptions.OkapiException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Deprecated
public class JSONPersistenceSession implements IPersistenceSession {

	public static final String MIME_TYPE = "application/json";
	
	private ObjectMapper mapper;
	private JsonFactory jsonFactory;
	private JsonParser parser;
	private OutputStream outStream;
	private InputStream inStream;
	private boolean isActive;
	private Class<?> rootClass;
	private Class<? extends IPersistenceBean> beanClass;

	public JSONPersistenceSession(Class<?> rootClass) {
		super();
		this.rootClass = rootClass;
		this.beanClass = PersistenceMapper.getBeanClass(rootClass);
	}
	
	public JSONPersistenceSession(Class<?> rootClass, OutputStream outStream) {
		this(rootClass);
		start(outStream);
	}
	
	public JSONPersistenceSession(Class<?> rootClass, InputStream inStream) {
		this(rootClass);
		start(inStream);
	}
	
	@Override
	public <T> T convert(Object object, Class<T> expectedClass) {		
		return mapper.convertValue(object, expectedClass);
	}

	@Override
	public Object deserialize() {
		if (!isActive) return null;
		
		IPersistenceBean bean = null;
		try {
			//bean = mapper.readValue(inStream, beanClass);
			bean = mapper.readValue(parser, beanClass);
			bean.init(this);
						
		} catch (JsonParseException e) {
			// TODO Handle exception
		} catch (JsonMappingException e) {
			// TODO Handle exception
		} catch (IOException e) {
			// TODO Handle exception
		}
		
		return bean.get(rootClass);
	}

	@Override
	public void end() {
		if (!isActive) return;
		isActive = false;
		
		if (inStream != null)
			try {
				inStream.close();
			} catch (IOException e) {
				// TODO Handle exception
			}
		if (outStream != null)
			try {
				outStream.close();
			} catch (IOException e) {
				// TODO Handle exception
			}
		inStream = null;
		outStream = null;
		mapper = null;		
	}

	@Override
	public void serialize(Object obj) {
		if (!isActive) return;
		if (!rootClass.isInstance(obj))
			throw new IllegalArgumentException(String.format("JSONPersistenceSession: " +
					"unable to serialize %s, this session handles only %s", 
					ClassUtil.getQualifiedClassName(obj),
					ClassUtil.getQualifiedClassName(rootClass)));
		
		IPersistenceBean bean = PersistenceMapper.getBean(rootClass);
		
		bean.init(this);
		bean.set(obj);
		
		try {
			mapper.writeValue(outStream, bean);
			
		} catch (JsonGenerationException e) {
			throw new OkapiException(e);
		} catch (JsonMappingException e) {
			throw new OkapiException(e);
		} catch (IOException e) {
			throw new OkapiException(e);		}
	}

	@Override
	public void start(OutputStream outStream) {
		end();
		
		this.outStream = outStream;
		startSession();
	}

	private void startSession() {		
		mapper = new ObjectMapper();
		
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true); 
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		
		jsonFactory = mapper.getJsonFactory();
		isActive = true;
	}

	@Override
	public void start(InputStream inStream) {
		end();
		
		this.inStream = inStream;		
		startSession();
		try {
			parser = jsonFactory.createJsonParser(inStream);
		} catch (JsonParseException e) {
			// TODO Handle exception
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Handle exception
			e.printStackTrace();
		}
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

}
