/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.wiki;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.lib.beans.v1.EventBean;
import net.sf.okapi.lib.beans.v1.PropertyBean;
import net.sf.okapi.lib.beans.v1.RawDocumentBean;
import net.sf.okapi.lib.beans.v1.TextUnitBean;
import net.sf.okapi.lib.persistence.BeanMapper;
import net.sf.okapi.lib.persistence.IVersionDriver;

public class OkapiBeansVersion1 implements IVersionDriver {

	public static final String VERSION = "OKAPI 1.0";
	
	@Override
	public String getVersionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerBeans(BeanMapper beanMapper) {
		beanMapper.registerBean(Event.class, EventBean.class);		
		beanMapper.registerBean(ITextUnit.class, TextUnitBean.class);
		beanMapper.registerBean(RawDocument.class, RawDocumentBean.class);
		beanMapper.registerBean(Property.class, PropertyBean.class);
	}
}
