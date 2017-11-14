/*===========================================================================
  Copyright (C) 2008-2014 by the Okapi Framework contributors
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

import java.io.IOException;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONUtil {

	private static ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
	
	/**
	 * Serialize a given object to a JSON string. 
	 * Object type information is stored in the string.
	 * @param obj the given object.
	 * @return a JSON string containing the object type info and serialized object.
	 */
	public static <T> String toJSON(T obj) {
		return toJSON(obj, false);
	}
	
	/**
	 * Serialize a given object to a JSON string. 
	 * Object type information is stored in the string.
	 * @param obj the given object.
	 * @param prettyPrint true to output the JSON string as multi-line indented text. 
	 * @return a JSON string containing the object type info and serialized object.
	 */
	public static <T> String toJSON(T obj, boolean prettyPrint) {
		JSONBean<T> bean = new JSONBean<T>();
		bean.setClassName(ClassUtil.getQualifiedClassName(obj));
		try {			
			if (prettyPrint) {
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
			}
			else {
				mapper.disable(SerializationFeature.INDENT_OUTPUT);
			}
			bean.setContent(obj);
			return Util.normalizeNewlines(mapper.writeValueAsString(bean));
		} catch (JsonProcessingException e) {
			throw new OkapiIOException(e);
		}			
	}
	
	/**
	 * Deserialize an object from a given JSON string based on
	 * the type information stored in the string.
	 * @param json the given JSON string.
	 * @return a new object deserialized from the given string.
	 */
	public static <T> T fromJSON(String json) {
		try {
			JSONBean<T> bean = mapper.readValue(json, new TypeReference<JSONBean<T>>(){});
			@SuppressWarnings("unchecked")
			Class<T> cls = (Class<T>) Class.forName(bean.getClassName());			
			// Have Jackson do casting
			String content = mapper.writeValueAsString(bean.getContent());
			return mapper.readValue(content, cls);
		} catch (IOException | ClassNotFoundException e) {
			throw new OkapiIOException(e);
		}
	}	
}
