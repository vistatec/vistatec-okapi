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
============================================================================*/

package net.sf.okapi.applications.rainbow.utilities.merging;

import java.util.Iterator;

import net.sf.okapi.applications.rainbow.packages.Manifest;
import net.sf.okapi.applications.rainbow.utilities.BaseUtility;
import net.sf.okapi.applications.rainbow.utilities.ISimpleUtility;
import net.sf.okapi.common.IParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utility extends BaseUtility implements ISimpleUtility {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private String manifestPath;
	private Manifest manifest;
	private Merger merger;
	
	public String getName () {
		return "oku_merging";
	}
	
	public void preprocess () {
		manifest = new Manifest();
		merger = new Merger();
	}

	public void postprocess () {
	}

	public IParameters getParameters () {
		return null;
	}

	public boolean hasParameters () {
		return true;
	}

	public boolean needsRoots () {
		return false;
	}

	public void setParameters (IParameters paramsObject) {
		// Not used in this utility
	}

	public boolean isFilterDriven () {
		return false;
	}
	
	public int requestInputCount () {
		return 1;
	}
	
	public void processInput () {
		manifestPath = getInputPath(0);
		// Load the manifest file to use
		manifest.load(manifestPath);
		// Check the package where the manifest has been found
		manifest.checkPackageContent();
		
		// UI check
		if ( canPrompt ) {
			ManifestDialog dlg = new ManifestDialog(shell, help);
			if ( !dlg.showDialog(manifest) ) {
				return;
			}
		}
		
		// Initialize the merger for this manifest
		merger.initialize(manifest);
		
		// One target language only, and take it from the manifest
		logger.info("Target: {}", manifest.getTargetLanguage());
		
		// Process each selected document in the manifest
		Iterator<Integer> iter = manifest.getItems().keySet().iterator();
		while ( iter.hasNext() ) {
			merger.execute(iter.next());
		}
	}

}
