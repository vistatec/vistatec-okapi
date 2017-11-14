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

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.BaseNameable;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class StartDocumentBean extends BaseNameableBean {

	private String locale;
	private String encoding;
	private boolean isMultilingual;
	private FactoryBean filterParameters = new FactoryBean();
	private FactoryBean filterWriter = new FactoryBean();
	private boolean hasUTF8BOM;
	private String lineBreak;

	@Override
	protected BaseNameable createObject(IPersistenceSession session) {
		return new StartDocument(super.getId());
	}

	@Override
	protected void fromObject(BaseNameable obj, IPersistenceSession session) {
		super.fromObject(obj, session);
		
		if (obj instanceof StartDocument) {
			StartDocument sd = (StartDocument) obj;
	
			LocaleId loc = sd.getLocale(); 
			if (loc != null)
				locale = loc.toString();
			
			encoding = sd.getEncoding();
			isMultilingual = sd.isMultilingual();
			filterParameters.set(sd.getFilterParameters(), session);
			filterWriter.set(sd.getFilterWriter(), session);
			hasUTF8BOM = sd.hasUTF8BOM();
			lineBreak = sd.getLineBreak();
		}
	}

	@Override
	protected void setObject(BaseNameable obj, IPersistenceSession session) {
		super.setObject(obj, session);
		
		if (obj instanceof StartDocument) {
			StartDocument sd = (StartDocument) obj;
			
			sd.setLocale(LocaleId.fromString(locale));
			sd.setEncoding(encoding, hasUTF8BOM);
			sd.setMultilingual(isMultilingual);
			sd.setFilterParameters(filterParameters.get(IParameters.class, session));
			sd.setFilterWriter(filterWriter.get(IFilterWriter.class, session));
			sd.setLineBreak(lineBreak);
		}
	}
	
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public boolean isMultilingual() {
		return isMultilingual;
	}

	public void setMultilingual(boolean isMultilingual) {
		this.isMultilingual = isMultilingual;
	}

	public FactoryBean getFilterWriter() {
		return filterWriter;
	}

	public void setFilterWriter(FactoryBean filterWriter) {
		this.filterWriter = filterWriter;
	}

	public boolean isHasUTF8BOM() {
		return hasUTF8BOM;
	}

	public void setHasUTF8BOM(boolean hasUTF8BOM) {
		this.hasUTF8BOM = hasUTF8BOM;
	}

	public String getLineBreak() {
		return lineBreak;
	}

	public void setLineBreak(String lineBreak) {
		this.lineBreak = lineBreak;
	}

	public void setFilterParameters(FactoryBean filterParameters) {
		this.filterParameters = filterParameters;
	}

	public FactoryBean getFilterParameters() {
		return filterParameters;
	}
}
