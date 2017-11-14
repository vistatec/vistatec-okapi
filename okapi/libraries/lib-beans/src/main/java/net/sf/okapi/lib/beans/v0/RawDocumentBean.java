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

import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.LocaleId;

@Deprecated
public class RawDocumentBean implements IPersistenceBean {

	private List<FactoryBean> annotations = new ArrayList<FactoryBean>();
	private String filterConfigId;
	private String id;
	private String encoding;
	private String srcLoc;
	private LocaleId trgLoc;
	private URI inputURI;
	private CharSequence inputCharSequence;
	private boolean hasReaderBeenCalled;
	private Reader reader;
	private InputStream inputStream;
	private FactoryBean skeleton;
	
	@Override
	public void init(IPersistenceSession session) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T get(Class<T> classRef) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPersistenceBean set(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFilterConfigId() {
		return filterConfigId;
	}

	public void setFilterConfigId(String filterConfigId) {
		this.filterConfigId = filterConfigId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getSrcLoc() {
		return srcLoc;
	}

	public void setSrcLoc(String srcLoc) {
		this.srcLoc = srcLoc;
	}

	public LocaleId getTrgLoc() {
		return trgLoc;
	}

	public void setTrgLoc(LocaleId trgLoc) {
		this.trgLoc = trgLoc;
	}

	public URI getInputURI() {
		return inputURI;
	}

	public void setInputURI(URI inputURI) {
		this.inputURI = inputURI;
	}

	public CharSequence getInputCharSequence() {
		return inputCharSequence;
	}

	public void setInputCharSequence(CharSequence inputCharSequence) {
		this.inputCharSequence = inputCharSequence;
	}

	public boolean isHasReaderBeenCalled() {
		return hasReaderBeenCalled;
	}

	public void setHasReaderBeenCalled(boolean hasReaderBeenCalled) {
		this.hasReaderBeenCalled = hasReaderBeenCalled;
	}

	public Reader getReader() {
		return reader;
	}

	public void setReader(Reader reader) {
		this.reader = reader;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public FactoryBean getSkeleton() {
		return skeleton;
	}

	public void setSkeleton(FactoryBean skeleton) {
		this.skeleton = skeleton;
	}

	public void setAnnotations(List<FactoryBean> annotations) {
		this.annotations = annotations;
	}

	public List<FactoryBean> getAnnotations() {
		return annotations;
	}

}
