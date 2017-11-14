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

import net.sf.okapi.common.ClassUtil;

@Deprecated
public class FactoryBean implements IPersistenceBean {

	private String className;
	private Object content;
	@SuppressWarnings("unused")
	private IPersistenceSession session;
	
	@Override
	public IPersistenceBean set(Object obj) {		
		className = ClassUtil.getQualifiedClassName(obj);		
		IPersistenceBean bean = PersistenceMapper.getBean(ClassUtil.getClass(obj));
		content = bean;
		
		return (bean instanceof FactoryBean) ? this : bean.set(obj); 
	}

	@Override
	public <T> T get(Class<T> classRef) {
		return (validateContent()) ? ((IPersistenceBean) content).get(classRef) : null;
	}
	
	private boolean validateContent() {
		if (content == null) return false;
		if (className == null) return false;
		
		boolean res = content instanceof IPersistenceBean; 
		if (!res) {
			content = JSONObjectConverter.convert(content, PersistenceMapper.getBeanClass(className));
			res = content instanceof IPersistenceBean;
		}
		return res;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public Object getContent() {
		return content;
	}

	@Override
	public void init(IPersistenceSession session) {		
		this.session = session;
	}
}
