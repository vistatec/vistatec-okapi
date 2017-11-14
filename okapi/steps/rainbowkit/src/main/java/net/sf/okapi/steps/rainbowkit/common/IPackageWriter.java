/*===========================================================================
  Copyright (C) 2010-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.common;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Provides a common writer to create a translation package. 
 */
public interface IPackageWriter extends IFilterWriter {

	public void setParameters (IParameters params);
	
	public IParameters getParameters ();
	
	public void setBatchInformation (String packageRoot,
		LocaleId srcLoc,
		LocaleId trgLoc,
		String inputRootDir,
		String rootDir,
		String packageId,
		String projectId,
		String creatorParams,
		String tempPackageRoot);
	
	public void setDocumentInformation (String relativeInputPath,
		String filterConfigId,
		String filterParameters,
		String inputEncoding,
		String relativeTargetPath,
		String outputEncoding,
		ISkeletonWriter skelWriter);

	//public String getMainOutputPath ();

	public void setSupporstOneOutputPerInput (boolean supporstOneOutputPerInput);
	
}
