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

package net.sf.okapi.applications.rainbow.utilities;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.filters.FilterConfigurationMapper;

/**
 * Provides a common way of executing an arbitrary utility.
 */
public interface IUtility {

	public void setFilterAccess (FilterConfigurationMapper mapper);
	
	/**
	 * Sets the context UI to use with the utility.
	 * This allows utilities to create UI elements during the process that are
	 * attached to the main UI shell from where the utilities are called.
	 * @param contextUI The shell object to use.
	 * @param helpParam The IHelp object to display help.
	 * @param updateCommand The string command to use to invoke the update
	 * system for this utility.
	 * @param projectDir Project directory (without trailing separator).
	 * @param canPrompt False if the utility should try to avoid prompting the user
	 * (used in batch mode for example).
	 */
	public void setContextUI (Object contextUI,
		IHelp helpParam,
		String updateCommand,
		String projectDir,
		boolean canPrompt);

	/**
	 * Adds a cancel listener to this utility.
	 * @param listener The listener to add.
	 */
	public void addCancelListener (CancelListener listener);
	
	/**
	 * Gets the unique string that identifies the utility.
	 * @return the name of the utility.
	 */
	public String getName ();

	/**
	 * Resets the input and output lists. This is to call when a utility 
	 * uses more than one input list, before {@link #addInputData(String, String, String)}
	 * and {@link #addOutputData(String, String)}. 
	 */
	public void resetLists ();
	
	/**
	 * Sets the runtime options for this utility.
	 * This method should be called once, before processing each input.
	 * @param sourceLanguage Language code for the source.
	 * @param targetLanguage Language code for the target.
	 */
	public void setOptions (LocaleId sourceLanguage,
		LocaleId targetLanguage);

	/**
	 * Indicates if the utility has parameters.
	 * @return True if the utility has parameters, false otherwise.
	 */
	public boolean hasParameters ();
	
	/**
	 * Gets the parameters object for the utility.
	 */
	public IParameters getParameters ();
	
	/**
	 * Sets the parameters object for the utility.
	 * @param paramsObject The new parameters object.
	 */
	public void setParameters (IParameters paramsObject);
	
	/**
	 * Indicates if the utility need the root to be defined.
	 * @return True if the root is needed, false otherwise.
	 */
	public boolean needsRoots ();
	
	/**
	 * Sets the input and output roots for the utility.
	 * @param inputRoot The input root for the utility.
	 * @param outputRoot The output root for the utility. 
	 */
	public void setRoots (String inputRoot,
		String outputRoot);

	/**
	 * Indicates if the utility is filter driven or not.
	 * @return True if the utility is filter-driven, false otherwise.
	 */
	public boolean isFilterDriven ();
	
	/**
	 * Adds a set of document information for the the input.
	 * @param path The full path of the input to process.
	 * @param encoding The default encoding.
	 * @param filterSettings The filter settings to use.
	 */
	public void addInputData (String path,
		String encoding,
		String filterSettings);
	
	/**
	 * Adds a set of document information for the the output.
	 * @param path The full path of the output.
	 * @param encoding The encoding.
	 */
	public void addOutputData (String path,
		String encoding);

	/**
	 * Gets the most useful folder for the given utility, or null if there is
	 * no relevant folder. This folder is the one that make more sense for a user
	 * to go after the utility has been executed, for example, the folder where
	 * the output documents have been created.
	 * @return The output folder for the utility, or null.
	 */
	public String getFolderAfterProcess ();
	
	public void preprocess ();
	
	public void postprocess ();
	
	public void cancel ();
	
	public void destroy ();

	public int inputCountRequested ();

}
