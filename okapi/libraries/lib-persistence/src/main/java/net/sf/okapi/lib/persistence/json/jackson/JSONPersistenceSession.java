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

package net.sf.okapi.lib.persistence.json.jackson;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiFileNotFoundException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.lib.persistence.IPersistenceBean;
import net.sf.okapi.lib.persistence.PersistenceSession;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@SuppressWarnings("unchecked")
public abstract class JSONPersistenceSession extends PersistenceSession {
	public static final String MSG_JSON_READ_EX = "JSONPersistenceSession: error reading.";
	public static final String MSG_JSON_WRITE_EX = "JSONPersistenceSession: error writing.";
	
	private static final String JSON_HEADER = "header"; //$NON-NLS-1$
	private static final String JSON_BODY = "body"; //$NON-NLS-1$	
	private static final String JSON_VER = "version"; //$NON-NLS-1$
	private static final String JSON_DESCR = "description"; //$NON-NLS-1$
	private static final String JSON_CLASS = "itemClass"; //$NON-NLS-1$
	private static final String JSON_MIME = "mimeType"; //$NON-NLS-1$
	private static final String JSON_FRAMES = "frames"; //$NON-NLS-1$
	private static final String JSON_ANNOTATIONS = "annotations"; //$NON-NLS-1$

	//private static final String VERSION = "1.0"; //$NON-NLS-1$	
	// JSON RFC http://www.ietf.org/rfc/rfc4627.txt
	private static final String MIME_TYPE = "application/json";  //$NON-NLS-1$ 	
	
	private ObjectMapper mapper;
	private JsonFactory jsonFactory;
	private JsonParser parser;
	private JsonGenerator headerGen;
	private JsonGenerator bodyGen;	
	private OutputStream bodyOut;
	private File bodyTemp;	
	private boolean compress;

	public JSONPersistenceSession(boolean compress) {
		super();
		
		this.compress = compress;
		mapper = new ObjectMapper();		
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true); 
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(MapperFeature.USE_ANNOTATIONS, true);
		mapper.configure(Feature.AUTO_CLOSE_SOURCE, false);
		mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
		
		jsonFactory = mapper.getFactory();
	}
	
	@Override
	public <T extends IPersistenceBean<?>> T convert(Object object, Class<T> expectedClass) {		
		return mapper.convertValue(object, expectedClass);
	}

	@Override
	protected <T extends IPersistenceBean<?>> T readBean(Class<T> beanClass, String name) {
		
		T bean = null;
		try {
			JsonToken token = parser.nextToken();
			if (token == JsonToken.END_OBJECT)
				return null;

			String fieldName = parser.getCurrentName();
			if (fieldName != null && name != null && !fieldName.startsWith(name))
				throw(new OkapiIOException(String.format("JSONPersistenceSession: input stream " +
						"is broken. Item label should start with \"%s\", but was \"%s\"", name, fieldName)));			
			parser.nextToken();
			
			bean = mapper.readValue(parser, beanClass);
//			JsonToken token = parser.nextToken();
//			if (token == JsonToken.END_OBJECT)
//				end();
						
		} catch (JsonParseException e) {			
			throw new OkapiException(MSG_JSON_READ_EX, e);
		} catch (JsonMappingException e) {
			throw new OkapiException(MSG_JSON_READ_EX, e);
		} catch (EOFException e) {
			throw new OkapiIOException("JSONPersistenceSession: input stream is broken -- unexpected EOF.", e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
		
		return bean;
	}
	
	@Override
	protected void writeBean(IPersistenceBean<?> bean, String name) {
		try {
			bodyGen.writeFieldName(name);
			mapper.writeValue(bodyGen, bean);
		} catch (JsonGenerationException e) {
			throw new OkapiException(MSG_JSON_WRITE_EX, e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	@Override
	public String getMimeType() {
		return MIME_TYPE;
	}

	private String readFieldValue(String fieldName) {
		String res = ""; 
		try {
			parser.nextToken();
			if (!Util.isEmpty(fieldName))
				if (!fieldName.equalsIgnoreCase(parser.getCurrentName()))
					throw(new OkapiIOException("JSONPersistenceSession: input stream is broken"));
			
			parser.nextToken();
			res = parser.getText();
		} catch (JsonParseException e) {
			throw new OkapiException(MSG_JSON_READ_EX, e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
		return res;
	}
	
	@SuppressWarnings("unused")
	@Override
	protected void startReading(InputStream inStream) {
		InputStream in = null;
		try {
			in = inStream;
			if (compress) {
				in = new GZIPInputStream(inStream);
			}
			parser = jsonFactory.createParser(in);
			
			JsonToken token = parser.nextToken(); 
			if (token != JsonToken.START_OBJECT)
				throw(new OkapiIOException("JSONPersistenceSession: input stream is broken"));
			
			parser.nextToken();						
			if (!JSON_HEADER.equalsIgnoreCase(parser.getCurrentName()))
				throw(new OkapiIOException("JSONPersistenceSession: input stream is broken"));
			
			token = parser.nextToken(); 
			if (token != JsonToken.START_OBJECT)
				throw(new OkapiIOException("JSONPersistenceSession: input stream is broken"));
			
			// Header
			String version = readFieldValue(JSON_VER);
			setVersion(version);
															
			String description = readFieldValue(JSON_DESCR);
			String itemClass = readFieldValue(JSON_CLASS);
			String mimeType = readFieldValue(JSON_MIME);
			
			// Frames
			parser.nextToken();
			if (!JSON_FRAMES.equalsIgnoreCase(parser.getCurrentName()))
				throw(new OkapiIOException("JSONPersistenceSession: input stream is broken"));
			
			parser.nextToken();
						
			this.setFrames(mapper.readValue(parser, List.class));
			parser.nextToken();
			
			// TODO move to OkapiJsonSession, we are bound to the Annotations class
			// Optional annotations entry
			if (JSON_ANNOTATIONS.equalsIgnoreCase(parser.getCurrentName())) {
				parser.nextToken();
				Class<IPersistenceBean<Annotations>> beanClass = 
						this.getBeanClass(Annotations.class);
				IPersistenceBean<Annotations> bean = mapper.readValue(parser, beanClass);
				this.setAnnotations(bean);
				parser.nextToken();
			}
						
			parser.nextToken();
			
			// Prepare the stream for items deserialization
			if (!JSON_BODY.equalsIgnoreCase(parser.getCurrentName()))
				throw(new OkapiIOException("JSONPersistenceSession: input stream is broken"));
			parser.nextToken();
			if (token != JsonToken.START_OBJECT)
				throw(new OkapiIOException("JSONPersistenceSession: input stream is broken"));
			//parser.nextToken();
			
		} catch (JsonParseException e) {
			throw(new OkapiIOException("JSONPersistenceSession: input stream is broken. ", e));
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	@Override
	protected void endReading(InputStream inStream) {
		try {
			parser.close();
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	@Override
	protected void startWriting(OutputStream outStream) {
		try {			
			bodyTemp = File.createTempFile("~JSONPrestistenceSession_bodyTemp", null);
			 
			if (compress) {
				bodyOut = new GZIPOutputStream(new FileOutputStream(bodyTemp)){{def.setLevel(Deflater.BEST_COMPRESSION);}};
			} else {
				bodyOut = new FileOutputStream(bodyTemp);
			}
			bodyGen = jsonFactory.createGenerator(bodyOut, JsonEncoding.UTF8);
			bodyGen.useDefaultPrettyPrinter();
			bodyGen.writeStartObject();
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	@Override
	protected void endWriting(OutputStream outStream) {
		// Finalize body
		try {
			bodyGen.writeRaw('}'); // writeEndObject() counts levels and throws exception instead
			
			bodyGen.close();
			bodyOut.close();
		} catch (JsonGenerationException e) {
			throw new OkapiException(MSG_JSON_WRITE_EX, e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
		
		// Write header and frames
		OutputStream headerOut = null;
		File headerTemp = null;
		try {
			headerTemp = File.createTempFile("~JSONPrestistenceSession_headerTemp", null);
			if (compress) {
				headerOut = new GZIPOutputStream(new FileOutputStream(headerTemp)){{def.setLevel(Deflater.BEST_COMPRESSION);}};
			} else {
				headerOut = new FileOutputStream(headerTemp);
			}			
		
			headerGen = jsonFactory.createGenerator(headerOut, JsonEncoding.UTF8);
			headerGen.useDefaultPrettyPrinter();

			headerGen.writeStartObject(); // The file root
			
			// All this, because references are built during body serialization, but need to be in header
			headerGen.writeFieldName(JSON_HEADER);			
			headerGen.writeStartObject();
			headerGen.writeStringField(JSON_VER, this.getVersion());
			headerGen.writeStringField(JSON_DESCR, this.getDescription());			
			headerGen.writeStringField(JSON_CLASS, this.getItemClass());
			headerGen.writeStringField(JSON_MIME, this.getMimeType());			
			headerGen.writeObjectField(JSON_FRAMES, this.getFrames());
			headerGen.writeObjectField(JSON_ANNOTATIONS, this.getAnnotationsBean());			
			headerGen.writeEndObject();
			
			headerGen.writeFieldName(JSON_BODY);
			headerGen.writeRaw(" : ");								
		} catch (JsonGenerationException e) {
			throw new OkapiException(MSG_JSON_WRITE_EX, e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		} finally {
			try {
				headerGen.flush(); // !!!
			} catch (IOException e) {
				
			} 
			try {
				if (headerOut != null) headerOut.close();
			} catch (IOException e) {
				
			}
		}
		
		// Write out stream
		try {
			InputStream headerIn;
			InputStream bodyIn;
			OutputStream os;
			if (compress) {
				headerIn = new GZIPInputStream(new FileInputStream(headerTemp));
				bodyIn = new GZIPInputStream(new FileInputStream(bodyTemp));				
				os =  new GZIPOutputStream(outStream){{def.setLevel(Deflater.BEST_COMPRESSION);}};
			} else {
				headerIn = new FileInputStream(headerTemp);
				bodyIn = new FileInputStream(bodyTemp);				
				os =  outStream;
			}
			
			try {
				StreamUtil.copy(Channels.newChannel(headerIn), Channels.newChannel(os), false);
				StreamUtil.copy(Channels.newChannel(bodyIn), Channels.newChannel(os), true);
			}
			finally {
				headerIn.close();
				bodyIn.close();
				// !!! Do not close external outStream
				bodyTemp.delete();
				headerTemp.delete();
				if (compress) {
					((GZIPOutputStream)os).finish();
				}
			}
		} catch (FileNotFoundException e) {
			throw new OkapiFileNotFoundException(e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}		
	}

	@Override
	protected String writeBeanToString(IPersistenceBean<?> bean) {
		try {
			return mapper.writeValueAsString(bean);
		} catch (JsonGenerationException e) {
			throw new OkapiException(MSG_JSON_WRITE_EX, e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	@Override
	protected <T extends IPersistenceBean<?>> T readBeanFromString(
			String content, Class<T> beanClass) {
		try {
			return mapper.readValue(content, beanClass);
		} catch (JsonParseException e) {			
			throw new OkapiException(MSG_JSON_READ_EX, e);
		} catch (JsonMappingException e) {
			throw new OkapiException(MSG_JSON_READ_EX, e);
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	protected JsonFactory getJsonFactory() {
		return jsonFactory;
	}
}
