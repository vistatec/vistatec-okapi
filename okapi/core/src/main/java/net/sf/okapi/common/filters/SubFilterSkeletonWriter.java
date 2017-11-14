/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.common.filters;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public class SubFilterSkeletonWriter implements ISkeletonWriter {

	public static final Ending GET_OUTPUT = new Ending("#$GET_SFSW_OUTPUT$#");
	public static final StartDocument SET_OPTIONS = new StartDocument("#$SET_OPTIONS$#");
	private ISkeletonWriter skelWriter; // Skeleton writer of the subfilter's internal filter
	private IEncoder parentEncoder;
	private StringBuilder sb;
	private String startResourceId;
	
	public SubFilterSkeletonWriter() {
		sb = new StringBuilder();
	}
	
	public SubFilterSkeletonWriter (StartSubfilter resource) {
		sb = new StringBuilder();
		IFilterWriter sfFilterWriter = resource.getFilterWriter();
		this.skelWriter = sfFilterWriter.getSkeletonWriter();
		this.parentEncoder = resource.getParentEncoder();	
		this.startResourceId = resource.getId();
	}

	@Override
	public void close () {
		skelWriter.close();		
	}

	public String getStartResourceId() {
		return startResourceId;
	}

	@Override
	public String processStartDocument (LocaleId outputLocale,
		String outputEncoding,
		ILayerProvider layer,
		EncoderManager encoderManager,
		StartDocument resource)
	{
//		if (resource == SET_OPTIONS) {
//			return "";
//		}
//		else {
//		if (skelWriter instanceof GenericSkeletonWriter &&
//				this != ((GenericSkeletonWriter) skelWriter).getSfWriter())
			sb.append(skelWriter.processStartDocument(outputLocale, outputEncoding, layer, 
				encoderManager, resource));
			return "";
//		}		
	}
	
	/**
	 * Get output created by this skeleton writer from a sequence of events.
	 * This method is useful when only an ISkeletonWriter reference is available.
	 * @param resource can be the SubFilterSkeletonWriter.GET_OUTPUT token (to return the overall output
	 * of this skeleton writer), or any other Ending resource.
	 * @return output of this skeleton writer if parameter is the SubFilterSkeletonWriter.GET_OUTPUT token
	 * or an empty string otherwise.  
	 */
	@Override
	public String processEndDocument (Ending resource) {
		if (resource == GET_OUTPUT) {
			return parentEncoder == null ? sb.toString() : parentEncoder.encode(sb.toString(), EncoderContext.TEXT);
		}
		else {
			sb.append(skelWriter.processEndDocument(resource));
			return "";
		}
	}

	@Override
	public String processStartSubDocument (StartSubDocument resource) {
		sb.append(skelWriter.processStartSubDocument(resource));
		return "";
	}

	@Override
	public String processEndSubDocument (Ending resource) {
		sb.append(skelWriter.processEndSubDocument(resource));
		return "";
	}

	@Override
	public String processStartGroup (StartGroup resource) {
		sb.append(skelWriter.processStartGroup(resource));
		return "";
	}

	@Override
	public String processEndGroup (Ending resource) {
		sb.append(skelWriter.processEndGroup(resource));
		return "";
	}

	@Override
	public String processTextUnit (ITextUnit resource) {
		sb.append(skelWriter.processTextUnit(resource));
		return "";
	}

	@Override
	public String processDocumentPart (DocumentPart resource) {
		sb.append(skelWriter.processDocumentPart(resource));
		return "";
	}

	@Override
	public String processStartSubfilter (StartSubfilter resource) {
		sb.append(skelWriter.processStartSubfilter(resource));
		return "";
	}

	@Override
	public String processEndSubfilter (EndSubfilter resource) {
		sb.append(skelWriter.processEndSubfilter(resource));
		return "";
	}

	public String getEncodedOutput () {
		return processEndDocument(GET_OUTPUT);
	}

	public SubFilterSkeletonWriter setOptions (LocaleId outputLocale,
		String outputEncoding, 
		StartSubfilter startSubfilter,
		ILayerProvider layer)
	{
		StartDocument sfStartDoc = startSubfilter.getStartDoc();
		IFilterWriter sfFilterWriter = sfStartDoc.getFilterWriter();
		EncoderManager sfEncoderManager = sfFilterWriter.getEncoderManager();
		
		processStartDocument(outputLocale, outputEncoding, layer, 
			sfEncoderManager,
			startSubfilter.getStartDoc());
		return this;
	}
	
	public ISkeletonWriter getSkelWriter() {
		return skelWriter;
	}
	
	// For serialization only

	protected void setSkelWriter(ISkeletonWriter skelWriter) {
		this.skelWriter = skelWriter;
	}

	protected IEncoder getParentEncoder() {
		return parentEncoder;
	}

	protected void setParentEncoder(IEncoder parentEncoder) {
		this.parentEncoder = parentEncoder;
	}

}
