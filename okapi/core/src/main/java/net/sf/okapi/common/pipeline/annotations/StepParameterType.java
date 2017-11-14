/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.pipeline.annotations;

/**
 * Types of the runtime parameters for steps.
 */
public enum StepParameterType {

	/**
	 * RawDocument object of the main input document.
	 */
	INPUT_RAWDOC,
	
	/**
	 * RawDocument object of the second input document.
	 */
	SECOND_INPUT_RAWDOC,
	
	/**
	 * RawDocument object of the third input document.
	 */
	THIRD_INPUT_RAWDOC,
	
	/**
	 * URI of the main input document.
	 */
	INPUT_URI,
	
	/**
	 * URI of the main output document.
	 */
	OUTPUT_URI,
	
	/**
	 * output stream of the main output document.
	 */
	OUTPUT_STREAM,
	
	/**
	 * Source locale.
	 */
	SOURCE_LOCALE,

	/**
	 * Single Target locale. Default, bilingual case used in Rainbow. 
	 * 
	 */
	TARGET_LOCALE,

	/**
	 * List of target locales. Used when more than one target needs to be initialized.
	 */
	TARGET_LOCALES,

	/**
	 * Filter configuration identifier for the main input document.
	 */
	FILTER_CONFIGURATION_ID,
	
	/**
	 * Filter configuration mapper.
	 */
	FILTER_CONFIGURATION_MAPPER,
	
	/**
	 * Output encoding of the main output document.
	 */
	OUTPUT_ENCODING,
	
	/**
	 * Root directory for the batch.
	 */
	ROOT_DIRECTORY,
	
	/**
	 * UI parent object of the calling application (shell, main window, etc.)
	 * 
	 * @deprecated The UI parent is now encapsulated in {@link StepParameterType#EXECUTION_CONTEXT}. Use that instead.
	 */
	@Deprecated
	UI_PARENT,

	/**
	 * Number of input documents in the current batch.
	 */
	BATCH_INPUT_COUNT,
	
	/**
	 * Root directory of the first set of input files.
	 */
	INPUT_ROOT_DIRECTORY,
	
	/**
	 * Output directory.
	 */
	OUTPUT_DIRECTORY,

	/**
	 * Context in which execution is occurring (CLI parameters, UI parent shell, etc.),
	 * via an {@link net.sf.okapi.common.ExecutionContext ExecutionContext} object.
	 */
	EXECUTION_CONTEXT
	
}
