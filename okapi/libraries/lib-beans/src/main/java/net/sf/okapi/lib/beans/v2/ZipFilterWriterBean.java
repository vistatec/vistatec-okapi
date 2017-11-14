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
import net.sf.okapi.common.filterwriter.ZipFilterWriter;
import net.sf.okapi.lib.beans.v1.LocaleIdBean;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ZipFilterWriterBean extends PersistenceBean<ZipFilterWriter> {

	private LocaleIdBean locale = new LocaleIdBean();
	private EncoderManagerBean encoderManager = new EncoderManagerBean();
	
	@Override
	protected ZipFilterWriter createObject(IPersistenceSession session) {
		return new ZipFilterWriter(encoderManager.get(EncoderManager.class, session));
	}

	@Override
	protected void fromObject(ZipFilterWriter obj, IPersistenceSession session) {
		locale.set(obj.getLocale(), session);
		encoderManager.set(obj.getEncoderManager(), session);
	}

	@Override
	protected void setObject(ZipFilterWriter obj, IPersistenceSession session) {
		obj.setOptions(locale.get(LocaleId.class, session), null);
	}
	
	public final LocaleIdBean getLocale() {
		return locale;
	}

	public final void setLocale(LocaleIdBean locale) {
		this.locale = locale;
	}

	public final EncoderManagerBean getEncoderManager() {
		return encoderManager;
	}

	public final void setEncoderManager(EncoderManagerBean encoderManager) {
		this.encoderManager = encoderManager;
	}
}
