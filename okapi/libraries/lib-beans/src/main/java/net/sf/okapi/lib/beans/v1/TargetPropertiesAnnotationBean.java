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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TargetPropertiesAnnotation;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class TargetPropertiesAnnotationBean extends PersistenceBean<TargetPropertiesAnnotation> {

	private ConcurrentHashMap<String, Map<String, PropertyBean>> targets = 
		new ConcurrentHashMap<String, Map<String, PropertyBean>>();

	@Override
	protected TargetPropertiesAnnotation createObject(IPersistenceSession session) {
		return new TargetPropertiesAnnotation();
	}

	@Override
	protected void fromObject(TargetPropertiesAnnotation obj, IPersistenceSession session) {
		for (LocaleId locId : obj) {
			Map<String, PropertyBean> propBeans = new LinkedHashMap<String, PropertyBean>();
			Map<String, Property> props = obj.get(locId);
			
			for (String key : props.keySet()) {
				Property prop = props.get(key);
				PropertyBean propBean = new PropertyBean();
				propBean.set(prop, session);
				propBeans.put(key, propBean);
			}								
			targets.put(locId.toString(), propBeans);
		}
	}

	@Override
	protected void setObject(TargetPropertiesAnnotation obj, IPersistenceSession session) {
		for (String locTag : targets.keySet()) {
			Map<String, PropertyBean> propBeans = targets.get(locTag);
			Map<String, Property> props = new LinkedHashMap<String, Property>();
			
			for (String key : propBeans.keySet()) {
				PropertyBean propBean = propBeans.get(key);
				Property prop = propBean.get(Property.class, session);
				props.put(key, prop);
			}
			
			obj.set(LocaleId.fromString(locTag), props);
		}
	}

	public ConcurrentHashMap<String, Map<String, PropertyBean>> getTargets() {
		return targets;
	}

	public void setTargets(
			ConcurrentHashMap<String, Map<String, PropertyBean>> targets) {
		this.targets = targets;
	}
}
