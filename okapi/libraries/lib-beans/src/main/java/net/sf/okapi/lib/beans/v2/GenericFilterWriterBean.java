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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.lib.beans.v1.LocaleIdBean;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class GenericFilterWriterBean extends PersistenceBean<GenericFilterWriter> {

	private LocaleIdBean locale = new LocaleIdBean();
	private String encoding;
	private FactoryBean skelWriter = new FactoryBean();
//	private EncoderManagerBean encoderManager = new EncoderManagerBean();
	private FactoryBean encoderManager = new FactoryBean();
	
	@Override
	protected GenericFilterWriter createObject(IPersistenceSession session) {
		return new GenericFilterWriter(skelWriter.get(ISkeletonWriter.class, session), 
				encoderManager.get(EncoderManager.class, session));
	}

	@Override
	protected void fromObject(GenericFilterWriter obj, IPersistenceSession session) {
		locale.set(obj.getLocale(), session);
		encoding = obj.getDefEncoding();
		skelWriter.set(obj.getSkeletonWriter(), session);
		encoderManager.set(obj.getEncoderManager(), session);
	}

	@Override
	protected void setObject(GenericFilterWriter obj, IPersistenceSession session) {
		if (locale.getLanguage() != null) {
			obj.setOptions(locale.get(LocaleId.class, session), encoding);
		}		
	}

	public final LocaleIdBean getLocale() {
		return locale;
	}

	public final void setLocale(LocaleIdBean locale) {
		this.locale = locale;
	}

	public final String getEncoding() {
		return encoding;
	}

	public final void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public final FactoryBean getSkelWriter() {
		return skelWriter;
	}

	public final void setSkelWriter(FactoryBean skelWriter) {
		this.skelWriter = skelWriter;
	}

	public final FactoryBean getEncoderManager() {
		return encoderManager;
	}

	public final void setEncoderManager(FactoryBean encoderManager) {
		this.encoderManager = encoderManager;
	}	
}
