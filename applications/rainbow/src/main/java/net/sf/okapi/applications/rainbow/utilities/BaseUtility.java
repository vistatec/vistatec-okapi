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

package net.sf.okapi.applications.rainbow.utilities;

import java.io.File;
import java.util.ArrayList;

import javax.swing.event.EventListenerList;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filters.FilterConfigurationMapper;

import org.eclipse.swt.widgets.Shell;

public abstract class BaseUtility implements IUtility {

	public static final String VAR_PROJDIR = "${ProjDir}";
	
	protected EventListenerList listenerList = new EventListenerList();
	protected FilterConfigurationMapper mapper;
	protected Shell shell;
	protected IHelp help;
	protected ArrayList<InputData> inputs;
	protected ArrayList<OutputData> outputs;
	protected String inputRoot;
	protected String outputRoot;
	protected LocaleId srcLang;
	protected LocaleId trgLang;
	protected String commonFolder;
	protected String updateCommand;
	protected String projectDir;
	protected boolean canPrompt;

	public BaseUtility () {
		inputs = new ArrayList<InputData>();
		outputs = new ArrayList<OutputData>();
	}
	
	public void addCancelListener (CancelListener listener) {
		listenerList.add(CancelListener.class, listener);
	}

	public void removeCancelListener (CancelListener listener) {
		listenerList.remove(CancelListener.class, listener);
	}

	public String getHelpLocation () {
		return ".." + File.separator + "help" + File.separator + "steps";
	}

	public String getDescription() {
		// TODO: Implement real description
		return null;
	}
	
	public void setContextUI (Object contextUI,
		IHelp helpParam,
		String updateCommand,
		String projectDir,
		boolean canPrompt)
	{
		shell = (Shell)contextUI;
		help = helpParam;
		this.updateCommand = updateCommand;
		this.projectDir = projectDir;
		this.canPrompt = canPrompt;
		commonFolder = null;
	}

	public void setOptions (LocaleId sourceLanguage,
		LocaleId targetLanguage)
	{
		srcLang = sourceLanguage;
		trgLang = targetLanguage;
	}

	public void setFilterAccess (FilterConfigurationMapper mapper) {
		this.mapper = mapper;
	}

	protected void fireCancelEvent (CancelEvent event) {
		Object[] listeners = listenerList.getListenerList();
		for ( int i=0; i<listeners.length; i+=2 ) {
			if ( listeners[i] == CancelListener.class ) {
				((CancelListener)listeners[i+1]).cancelOccurred(event);
			}
		}
	}

	public void addInputData (String path,
		String encoding,
		String filterSettings)
	{
		inputs.add(new InputData(path, encoding, filterSettings));
	}

	public void addOutputData (String path,
		String encoding)
	{
		outputs.add(new OutputData(path, encoding));
		// Compute the longest common folder
		commonFolder = Util.longestCommonDir(commonFolder,
			Util.getDirectoryName(path), !Util.isOSCaseSensitive());
	}

	public String getInputRoot () {
		return inputRoot;
	}

	public String getOutputRoot () {
		return outputRoot;
	}

	public void resetLists () {
		inputs.clear();
		outputs.clear();
	}

	public void setRoots (String inputRoot,
		String outputRoot)
	{
		this.inputRoot = inputRoot;
		this.outputRoot = outputRoot;
	}

	public String getFolderAfterProcess () {
		return commonFolder;
	}

	public String getInputPath (int index) {
		if ( index > inputs.size()-1 ) return null;
		return inputs.get(index).path;
	}
	
	public String getInputEncoding (int index) {
		if ( index > inputs.size()-1 ) return null;
		return inputs.get(index).encoding;
	}

	public String getInputFilterSettings (int index) {
		if ( index > inputs.size()-1 ) return null;
		return inputs.get(index).filterSettings;
	}

	public String getOutputPath (int index) {
		if ( index > inputs.size()-1 ) return null;
		return outputs.get(index).path;
	}
	
	public String getOutputEncoding (int index) {
		if ( index > inputs.size()-1 ) return null;
		return outputs.get(index).encoding;
	}

	public void cancel () {
		fireCancelEvent(new CancelEvent(this));
	}

	public void destroy () {
		// Do nothing by default
	}
	
	public int inputCountRequested () {
		return 1; // Default
	}

}
