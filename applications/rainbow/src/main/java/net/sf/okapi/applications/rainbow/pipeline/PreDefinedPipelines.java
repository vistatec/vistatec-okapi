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
============================================================================*/

package net.sf.okapi.applications.rainbow.pipeline;

import java.util.HashMap;

public class PreDefinedPipelines {

	private HashMap<String, String> map;
	
	public PreDefinedPipelines () {
		map = new HashMap<String, String>();
		add("BatchTranslation");
		add("BOMConversion");
		add("CharListing");
		add("EncodingConversion");
		add("FormatConversion");
		add("ImageModification");
		add("ImportTM");
		add("LineBreakConversion");
		add("QualityCheck");
		add("RTFConversion");
		add("SnRWithFilter");
		add("SnRWithoutFilter");
		add("TermExtraction");
		add("TextRewriting");
		add("TranslationComparison");
		add("TranslationKitCreation");
		add("TranslationKitPostProcessing");
		add("URIConversion");
		add("XMLAnalysis");
		add("XMLCharactersFixing");
		add("XMLValidation");
		add("XSLTransform");
	}
	
	/**
	 * Adds a mapping. The added mapping uses the name in lowercase for the key
	 * and the cased name with the proper namespace for the class name.
	 * @param name the Correctly cased name of the predefined pipeline.
	 */
	private void add (String name) {
		map.put(name.toLowerCase(),
			"net.sf.okapi.applications.rainbow.pipeline." + name + "Pipeline");
	}
	
	/**
	 * Creates a predefined pipeline object for a given pre-defined pipeline name.
	 * @param name the name of the predefined pipeline to create (not case-sensitive)
	 * @return the predefined pipeline or null if it could not be created.
	 */
	public IPredefinedPipeline create (String name) {
		String className = map.get(name.toLowerCase());
		if ( className == null ) return null;
		IPredefinedPipeline pp;
		try {
			pp = (IPredefinedPipeline)Class.forName(className).newInstance();
		}
		catch ( Throwable e ) {
			return null; // TODO: warning
		}
		return pp;
	}

}
