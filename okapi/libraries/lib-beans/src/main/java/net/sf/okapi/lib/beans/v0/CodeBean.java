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

import java.util.LinkedHashMap;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment.TagType;

@Deprecated
public class CodeBean implements IPersistenceBean {

	private TagType tagType;
	private int id;
	private String type;
	private String data;
	private String outerData;
	private int flag;
	private LinkedHashMap<String, InlineAnnotationBean> annotations = new LinkedHashMap<String, InlineAnnotationBean>();
	
	@Override
	public void init(IPersistenceSession session) {
	}

	@Override
	public <T> T get(Class<T> classRef) {
		Code code = new Code(tagType, type, data);
		code.setId(id);
		code.setOuterData(outerData);
		// TODO flag handling in Code
		// code.setFlag
		
		return classRef.cast(code);
	}

	@Override
	public IPersistenceBean set(Object obj) {
		if (obj instanceof Code) {
			Code code = (Code) obj;
			tagType = code.getTagType();
			id = code.getId();
			type = code.getType();
			data = code.getData();
			outerData = code.getOuterData();
			// TODO flag handling in Code
			//flag = code.get;
		}
		return this;
	}

	public TagType getTagType() {
		return tagType;
	}

	public void setTagType(TagType tagType) {
		this.tagType = tagType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getOuterData() {
		return outerData;
	}

	public void setOuterData(String outerData) {
		this.outerData = outerData;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public LinkedHashMap<String, InlineAnnotationBean> getAnnotations() {
		return annotations;
	}

	public void setAnnotations(
			LinkedHashMap<String, InlineAnnotationBean> annotations) {
		this.annotations = annotations;
	}

}
