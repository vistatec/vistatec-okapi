/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.pipeline;

/**
 * Maps the class of a pipeline step to its UI dependencies. 
 * Specifically, the editor class and the description provider class, both
 * of which are null, if not set. All fields are final and read-only.
 * 
 * @author Martin Wunderlich
 */
public class PipelineStepUIDescription {
	private final String pipelineStepClass;
	private final String editorClass;
	private final String descriptionProviderClass;
	
	public PipelineStepUIDescription(String pipelineStepClass) {
		this.pipelineStepClass = pipelineStepClass;
		this.editorClass = null;
		this.descriptionProviderClass = null;
	}
	
	public PipelineStepUIDescription(String pipelineStepClass, String editorClass, String descriptionProviderClass) {
		this.pipelineStepClass = pipelineStepClass;
		this.editorClass = editorClass;
		this.descriptionProviderClass = descriptionProviderClass;
	}
	
	public String getPipelineStepClass() {
		return pipelineStepClass;
	}

	public String getEditorClass() {
		return editorClass;
	}

	public String getDescriptionProviderClass() {
		return descriptionProviderClass;
	}

	public boolean hasDescriptionProvider() {
		return descriptionProviderClass != null;
	}

	public boolean hasEditor() {
		return editorClass != null;
	}
}
