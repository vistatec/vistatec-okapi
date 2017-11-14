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

package net.sf.okapi.common.pipelinedriver;

import java.net.URI;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.RawDocument;

/**
 * Common set of methods for a pipeline batch item.
 * A batch item corresponds to the data provided by the caller application to
 * execute one process of all the steps of a pipeline.
 * <p>Most of the time a batch item is composed of a single input file with 
 * possibly some output information if some of the steps generate output for 
 * each batch item.
 * <p>Some steps require more than one input document per batch item, for 
 * example a steps that align the content of two files will request two inputs 
 * per batch item: the source file and its corresponding translated file.
 */
public interface IBatchItemContext {

	/**
	 * Gets the filter configuration identifier for a given input document of 
	 * this batch item. 
	 * @param index the zero-based index of the input document.
	 * @return the filter configuration identifier of the given the input document.
	 */
	public String getFilterConfigurationId (int index);
	
	/**
	 * Gets a RawDocument object from the given input document of this batch item.
	 * @param index the zero-based index of the input document.
	 * @return the RawDocument object from the given input document,
	 * or null if there is no RawDocument for the given input.
	 */
	public RawDocument getRawDocument (int index);
	
	/**
	 * Gets the output URI for a given input document of this batch item. 
	 * @param index the zero-based index of the input document.
	 * @return the output URI of the given the input document,
	 * or null if there is no output URI for the given input.
	 */
	public URI getOutputURI (int index);
	
	/**
	 * Gets the output encoding for a given input document of this batch item. 
	 * @param index the zero-based index of the input document.
	 * @return the output encoding of the given the input document,
	 * or null if there is no output encoding for the given input.
	 */
	public String getOutputEncoding (int index);
	
	/**
	 * Gets the source locale for a given input document of this batch item. 
	 * @param index the zero-based index of the input document.
	 * @return the source locale of the given the input document,
	 * or null if there is no source locale for the given input.
	 */
	public LocaleId getSourceLocale (int index);
	
	/**
	 * Gets the target locale for a given input document of this batch item. 
	 * @param index the zero-based index of the input document.
	 * @return the target locale of the given the input document,
	 * or null if there is no target locale for the given input.
	 */
	public LocaleId getTargetLocale (int index);
	
}
