/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.ExecutionContext;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.ISkeleton;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;

/**
 * Special resource used to carry runtime parameters.
 */
public class PipelineParameters implements IResource, IWithAnnotations {

	private Annotations annotations;
	private String id;
	private URI outputURI;
	private LocaleId targetLocale;
	private List<LocaleId> trgLocs;
	private LocaleId sourceLocale;
	private String outputEncoding;
	private URI inputURI;
	private String filterConfigId;
	private IFilterConfigurationMapper fcMapper;
	private RawDocument inputRawDocument;
	private RawDocument secondInputRawDocument;
	private RawDocument thirdInputRawDocument;
	private String rootDirectory;
	private String inputRootDirectory;
	private Object uiParent;
	private ExecutionContext context;
	private int batchInputCount = -1;

	/**
	 * Creates a new empty ParametersEvent object.
	 */
	public PipelineParameters () {
	}
	
	
	/**
	 * Creates a ParametersEvent object with most majority of defaults initialized
	 * @param startDoc - current {@link StartDocument}
	 * @param inputDoc - input {@link RawDocument}
	 * @param secondDoc - optional second input {@link RawDocument}
	 * @param thirdDoc - optional third input {@link RawDocument}
	 */
	public PipelineParameters (StartDocument startDoc, RawDocument inputDoc, 
			RawDocument secondDoc, RawDocument thirdDoc) {
		this.id = startDoc.getId();
		this.outputURI = null;
		this.targetLocale = inputDoc.getTargetLocale();
		this.trgLocs = new LinkedList<>(); 
		trgLocs.addAll(startDoc.getTargetLocales());
		this.sourceLocale = inputDoc.getSourceLocale();
		this.outputEncoding = null;
		this.inputURI = inputDoc.getInputURI();
		this.filterConfigId = inputDoc.getFilterConfigId();
		this.fcMapper = null;
		this.inputRawDocument = inputDoc;
		this.secondInputRawDocument = secondDoc;
		this.thirdInputRawDocument = thirdDoc;		
		this.rootDirectory = null;
		this.inputRootDirectory = null;
		this.uiParent = null;
		this.context = null;
	}
	
	@Override
	public <A extends IAnnotation> A getAnnotation(Class<A> annotationType) {
		if ( annotations == null ) {
			return null;
		}
		return annotationType.cast(annotations.get(annotationType));
	}

	@Override
	public String getId () {
		return id;
	}

	@Override
	public ISkeleton getSkeleton () {
		throw new OkapiNotImplementedException("This resource does not have a skeketon");
	}

	@Override
	public void setAnnotation (IAnnotation annotation) {
		if ( annotations == null ) {
			annotations = new Annotations();
		}
		annotations.set(annotation);
	}

	@Override
	public void setId (String id) {
		this.id = id;
	}

	@Override
	public void setSkeleton(ISkeleton skeleton) {
		throw new OkapiNotImplementedException("This resource does not have a skeketon");
	}

	public Iterable<IAnnotation> getAnnotations () {
		if ( annotations == null ) {
			return Collections.emptyList();
		}
		return annotations;
	}

	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
	public URI getOutputURI () {
		return outputURI;
	}

	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	public LocaleId getTargetLocale () {
		return targetLocale;
	}

	public void setTargetLocales (List<LocaleId> trgLocs) {
		this.trgLocs = trgLocs;
	}

	public List<LocaleId> getTargetLocales () {
		return trgLocs;
	}
	
	public void setSourceLocale (LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	
	public LocaleId getSourceLocale () {
		return sourceLocale;
	}

	public void setOutputEncoding (String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}
	
	public String getOutputEncoding () {
		return outputEncoding;
	}

	public void setInputURI (URI inputURI) {
		this.inputURI = inputURI;
	}
	
	public URI getInputURI () {
		return inputURI;
	}

	public void setFilterConfigurationId (String filterConfigId) {
		this.filterConfigId = filterConfigId;
	}
	
	public String getFilterConfigurationId () {
		return filterConfigId;
	}

	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}
	
	public IFilterConfigurationMapper getFilterConfigurationMapper () {
		return fcMapper;
	}

	public void setInputRawDocument (RawDocument inputRawDocument) {
		this.inputRawDocument = inputRawDocument;
	}
	
	public RawDocument getInputRawDocument () {
		return inputRawDocument;
	}

	public void setSecondInputRawDocument (RawDocument secondInputRawDocument) {
		this.secondInputRawDocument = secondInputRawDocument;
	}
	
	public RawDocument getSecondInputRawDocument () {
		return secondInputRawDocument;
	}

	public void setThirdInputRawDocument (RawDocument thirdInputRawDocument) {
		this.thirdInputRawDocument = thirdInputRawDocument;
	}
	
	public RawDocument getThirdInputRawDocument () {
		return thirdInputRawDocument;
	}

	public void setRootDirectory (String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	
	public String getRootDirectory () {
		return rootDirectory;
	}

	public void setInputRootDirectory (String inputRootDirectory) {
		this.inputRootDirectory = inputRootDirectory;
	}
	
	public String getInputRootDirectory () {
		return inputRootDirectory;
	}

	public void setUIParent (Object uiParent) {
		this.uiParent = uiParent;
	}
	
	public Object getUIParent () {
		return uiParent;
	}
	
	public void setExecutionContext (ExecutionContext context) {
		this.context = context;
	}
	
	public ExecutionContext getExecutionContext () {
		return context;
	}

	public void setBatchInputCount (int batchInputCount) {
		this.batchInputCount = batchInputCount;
	}
	
	public int getBatchInputCount () {
		return batchInputCount;
	}

}
