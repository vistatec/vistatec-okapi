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

package net.sf.okapi.lib.beans.sessions;

import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.JSONEncoder;
import net.sf.okapi.lib.beans.v0.PersistenceMapper;
import net.sf.okapi.lib.beans.v1.OkapiBeans;
import net.sf.okapi.lib.beans.v2.OkapiBeans2;
import net.sf.okapi.lib.persistence.VersionMapper;
import net.sf.okapi.lib.persistence.json.jackson.JSONPersistenceSession;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

@SuppressWarnings("deprecation")
public class OkapiJsonSession extends JSONPersistenceSession {
	public OkapiJsonSession(boolean compress) {
		super(compress);
		
		// Set Okapi JSON encoder for resources
		JsonFactory jsonFactory = getJsonFactory();
		jsonFactory.setCharacterEscapes(new JSONStringEncoder());
	}
	
	private class JSONStringEncoder extends CharacterEscapes {
		private static final long serialVersionUID = 6753659638115445103L;

		JSONEncoder encoder;
		Map<Integer, SerializableString> cache;
		
		public JSONStringEncoder() {
			encoder = new JSONEncoder();
			
			// TODO when session made thread-safe, change to ConcurrentHashMap
			cache = new HashMap<Integer, SerializableString>(); 
			IParameters params = new StringParameters();
			params.setBoolean("escapeExtendedChars", true);
			encoder.setOptions(params, "UTF-8", encoder.getLineBreak());
		}
		
		@Override
		public int[] getEscapeCodesForAscii() {
			int[] res = standardAsciiEscapesForJSON();
			res[127] = -1; // escape DEL to \u007F
			return res;
		}

		@Override
		public SerializableString getEscapeSequence(int ch) {
			if (ch > 127) return null; // Don't escape UTF-8 chars
			SerializableString res = cache.get(ch);
			if (res == null) {
				res = new SerializedString(encoder.encode(ch, EncoderContext.TEXT));
				cache.put(ch, res);
			}
			return res;
		}		
	}
	
	@Override
	public void registerVersions() {
		VersionMapper.registerVersion(PersistenceMapper.class); // v0
		VersionMapper.registerVersion(OkapiBeans.class);		// v1
		VersionMapper.registerVersion(OkapiBeans2.class);		// v2
	}

	@Override
	protected Class<?> getDefItemClass() {
		return Event.class;
	}

	@Override
	protected String getDefItemLabel() {
		return "event";
	}

	@Override
	protected String getDefVersionId() {
		return OkapiBeans2.VERSION;
	}
}
