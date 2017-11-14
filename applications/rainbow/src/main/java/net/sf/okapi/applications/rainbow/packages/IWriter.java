/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.packages;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;

/**
 * Provides a common way create a translation package. 
 */
public interface IWriter extends IFilterWriter {

	public String getPackageType ();
	
	public String getReaderClass ();
	
	/**
	 * Sets the global parameters of the package.
	 * @param sourceLocale the source language.
	 * @param targetLocale the target language.
	 * @param projectID the project identifier.
	 * @param outputDir the root folder for the output.
	 * @param packageID the package identifier.
	 * @param sourceRoot the root folder of the original inputs.
	 * @param preSegmented indicates if the files are pre-segmented.
	 * @param creationTool the tool that creates the package.
	 */
	public void setInformation (LocaleId sourceLocale,
		LocaleId targetLocale,
		String projectID,
		String outputDir,
		String packageID,
		String sourceRoot,
		boolean preSegmented,
		String creationTool);
	
	public void writeStartPackage ();
	
	public void writeEndPackage (boolean createZip);
	
	public void createOutput (int docID,
		String relativeSourcePath,
		String relativeTargetPath,
		String sourceEncoding,
		String targetEncoding,
		String filterSettings,
		IParameters filterParams,
		EncoderManager encoderManager);

	public void createCopies (int docID,
		String relativeSourcePath);

	/**
	 * Helper method to output the TMX entries.
	 * @param tu the text unit to look at for possible output.
	 */
	public void writeTMXEntries (ITextUnit tu);

	/**
	 * Helper method to output scored entries. This method is called by {@link #writeTMXEntries(ITextUnit)}.
	 * @param item the text unit to process.
	 */
	public void writeScoredItem (ITextUnit item);

}
