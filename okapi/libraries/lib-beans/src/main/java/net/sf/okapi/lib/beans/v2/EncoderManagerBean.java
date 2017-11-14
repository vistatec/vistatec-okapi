/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v2;

import java.util.Hashtable;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.lib.beans.v1.ParametersBean;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class EncoderManagerBean extends PersistenceBean<EncoderManager> {
	
	private Hashtable<String, String> mimeMap = new Hashtable<String, String>(); // mimeType to encoder class name map
	private Hashtable<String, FactoryBean> encoders = new Hashtable<String, FactoryBean>(); // mimeType to encoder instance map
	private String mimeType;
	private FactoryBean encoder = new FactoryBean(); // Holds a reference to an encoder in the encoders map	
	private String defEncoding;
	private String defLineBreak;
	private ParametersBean defParams = new ParametersBean();

	@Override
	protected EncoderManager createObject(IPersistenceSession session) {
		return new EncoderManager();
	}

	@Override
	protected void setObject(EncoderManager obj, IPersistenceSession session) {
		for (String mimeType : mimeMap.keySet()) {
			obj.setMapping(mimeType, mimeMap.get(mimeType));
		}
		for (String mimeType : encoders.keySet()) {
			obj.setMapping(mimeType, encoders.get(mimeType).get(IEncoder.class, session));
		}
		if (defLineBreak != null) {
			obj.setDefaultOptions(defParams.get(IParameters.class, session), defEncoding, defLineBreak);
		}		
		obj.updateEncoder(mimeType);
	}

	@Override
	protected void fromObject(EncoderManager obj, IPersistenceSession session) {
		for (String mimeType : obj.getMimeMap().keySet()) {
			mimeMap.put(mimeType, obj.getMimeMap().get(mimeType));
		}
		for (String mimeType : obj.getEncoders().keySet()) {
			FactoryBean encoder = new FactoryBean();
			encoder.set(obj.getEncoders().get(mimeType), session);
			encoders.put(mimeType, encoder);
		}
		encoder.set(obj.getEncoder(), session);
		defEncoding = obj.getEncoding();
		defLineBreak = obj.getDefLineBreak();
		defParams.set(obj.getParameters(), session);
	}

	public Hashtable<String, String> getMimeMap() {
		return mimeMap;
	}

	public void setMimeMap(Hashtable<String, String> mimeMap) {
		this.mimeMap = mimeMap;
	}

	public Hashtable<String, FactoryBean> getEncoders() {
		return encoders;
	}

	public void setEncoders(Hashtable<String, FactoryBean> encoders) {
		this.encoders = encoders;
	}

	public final String getMimeType() {
		return mimeType;
	}

	public final void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public FactoryBean getEncoder() {
		return encoder;
	}

	public void setEncoder(FactoryBean encoder) {
		this.encoder = encoder;
	}

	public String getDefEncoding() {
		return defEncoding;
	}

	public void setDefEncoding(String defEncoding) {
		this.defEncoding = defEncoding;
	}

	public String getDefLineBreak() {
		return defLineBreak;
	}

	public void setDefLineBreak(String defLineBreak) {
		this.defLineBreak = defLineBreak;
	}

	public ParametersBean getDefParams() {
		return defParams;
	}

	public void setDefParams(ParametersBean defParams) {
		this.defParams = defParams;
	}

}
