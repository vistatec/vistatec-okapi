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

package net.sf.okapi.lib.beans.v1;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.resource.Document;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class DocumentBean extends PersistenceBean<Document> {

	private AnnotationsBean annotations = new AnnotationsBean();
	private String id;
	private List<FactoryBean> documentResources = new ArrayList<FactoryBean>();

	@Override
	protected Document createObject(IPersistenceSession session) {
		return new Document();
	}

	@Override
	protected void fromObject(Document obj, IPersistenceSession session) {
		annotations.set(obj.getAnnotations(), session);
		
		id = obj.getId();
		
		for (IResource res : obj) {
			FactoryBean resBean = new FactoryBean();
			resBean.set(res, session);
			documentResources.add(resBean);
		}						
	}

	@Override
	protected void setObject(Document obj, IPersistenceSession session) {
		for (FactoryBean annotationBean : annotations.getItems())
			obj.setAnnotation(annotationBean.get(IAnnotation.class, session));
		
		obj.setId(id);
		
		for (FactoryBean docPropBean : documentResources)
			obj.addResource(docPropBean.get(IResource.class, session));
	}
	
	public AnnotationsBean getAnnotations() {
		return annotations;
	}

	public void setAnnotations(AnnotationsBean annotations) {
		this.annotations = annotations;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<FactoryBean> getDocumentResources() {
		return documentResources;
	}

	public void setDocumentResources(List<FactoryBean> documentResources) {
		this.documentResources = documentResources;
	}
}
