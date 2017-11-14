/*===========================================================================
  Copyright (C) 2008 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.applications.rainbow.packages;

public class ManifestItem {

	public static final String POSPROCESSING_TYPE_DEFAULT = "default";
	public static final String POSPROCESSING_TYPE_RTF = "rtf";
	
	private String relativeWorkPath;
	private String relativeInputPath;
	private String relativeOutputPath;
	private String inputEncoding;
	private String outputEncoding;
	private String filterID;
	private boolean selected;
	private String postProcessingType;
	private boolean exists;

	public ManifestItem (String relativeWorkPath,
		String relativeInputPath,
		String relativeOutputPath,
		String inputEncoding,
		String outputEncoding,
		String filterID,
		String postProcessingType,
		boolean selected)
	{
		if ( relativeWorkPath == null ) throw new NullPointerException();
		if ( relativeInputPath == null ) throw new NullPointerException();
		if ( relativeOutputPath == null ) throw new NullPointerException();
		if ( inputEncoding == null ) throw new NullPointerException();
		if ( outputEncoding == null ) throw new NullPointerException();
		if ( filterID == null ) throw new NullPointerException();
		if ( postProcessingType == null ) throw new NullPointerException();
		
		this.relativeWorkPath = relativeWorkPath;
		this.relativeInputPath = relativeInputPath;
		this.relativeOutputPath = relativeOutputPath;
		this.inputEncoding = inputEncoding;
		this.outputEncoding = outputEncoding;
		this.filterID = filterID;
		this.selected = selected;
		this.postProcessingType = postProcessingType;
		exists = true;
	}

	public String getRelativeWorkPath () {
		return relativeWorkPath;
	}
	
	public String getRelativeInputPath () {
		return relativeInputPath;
	}
	
	public String getRelativeOutputPath () {
		return relativeOutputPath;
	}
	
	public String getInputEncoding () {
		return inputEncoding;
	}

	public String getOutputEncoding () {
		return outputEncoding;
	}

	public String getFilterID () {
		return filterID;
	}

	public boolean selected () {
		return selected;
	}
	
	public void setSelected (boolean value) {
		selected = value;
	}
	
	public boolean exists () {
		return exists;
	}
	
	public void setExists (boolean value) {
		exists = value;
	}
	
	public String getPostProcessingType () {
		return postProcessingType;
	}
	
}
