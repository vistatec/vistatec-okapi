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
===========================================================================*/

package net.sf.okapi.steps.rainbowkit.creation;

import java.util.List;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;

public class Parameters extends StringParameters {

	static public String SUPPORTFILE_SEP = "\t"; // Separator between two support file entries
	static public String SUPPORTFILEDEST_SEP = ";"; // Separator between the file pattern and its destination
	static public String SUPPORTFILE_SAMENAME = "<same>"; // Marker to indicate to use the same file name
	
	static final String WRITERCLASS = "writerClass"; //$NON-NLS-1$
	static final String WRITEROPTIONS = "writerOptions"; //$NON-NLS-1$
	static final String PACKAGENAME = "packageName"; //$NON-NLS-1$
	static final String PACKAGEDIRECTORY = "packageDirectory"; //$NON-NLS-1$ 
	static final String MESSAGE = "message"; //$NON-NLS-1$
	static final String OUTPUTMANIFEST = "outputManifest"; //$NON-NLS-1$
	static final String CREATEZIP = "createZip"; //$NON-NLS-1$
	static final String SENDOUTPUT = "sendOutput"; //$NON-NLS-1$
	/*List of the support files. The storage is done:
	 * origin1>destination1\torigin2>destination2\t...
	 * where origin is a path or path with pattern
	 * and destination is a directory relative to the root of the package,
	 * plus the file name, or <same> for the same filename
	 */
	static final String SUPPORTFILES = "supportFiles"; //$NON-NLS-1$
	
	public Parameters () {
		super();
	}
	
	@Override
	public void reset() {
		super.reset();
		setWriterClass("net.sf.okapi.steps.rainbowkit.xliff.XLIFFPackageWriter");
		setWriterOptions("");
		setPackageName("pack1");
		setPackageDirectory(Util.INPUT_ROOT_DIRECTORY_VAR);
		// Internal
		setSupportFiles("");
		setMessage("");
		setOuputManifest(true);
		setCreateZip(false);
		setSendOutput(false);
	}

	public String getWriterClass () {
		return getString(WRITERCLASS);
	}

	public void setWriterClass (String writerClass) {
		setString(WRITERCLASS, writerClass);
	}

	public String getWriterOptions () {
		return getGroup(WRITEROPTIONS);
	}

	public void setWriterOptions (String writerOptions) {
		setGroup(WRITEROPTIONS, writerOptions);
	}
	
	public String getMessage () {
		return getString(MESSAGE);
	}

	public void setMessage (String message) {
		setString(MESSAGE, message);
	}
	
	public String getPackageName () {
		return getString(PACKAGENAME);
	}

	public void setPackageName (String packageName) {
		setString(PACKAGENAME, packageName);
	}

	public String getPackageDirectory () {
		return getString(PACKAGEDIRECTORY);
	}

	public void setPackageDirectory (String packageDirectory) {
		setString(PACKAGEDIRECTORY, packageDirectory);
	}
	
	public String getSupportFiles () {
		return getString(SUPPORTFILES);
	}

	public void setSupportFiles (String supportFiles) {
		setString(SUPPORTFILES, supportFiles);		
	}
	
	public boolean getOutputManifest () {
		return getBoolean(OUTPUTMANIFEST);
	}

	public void setOuputManifest (boolean outputManifest) {
		setBoolean(OUTPUTMANIFEST, outputManifest);
	}

	public boolean getCreateZip () {
		return getBoolean(CREATEZIP);
	}

	public void setCreateZip(boolean createZip) {
		setBoolean(CREATEZIP, createZip);
	}

	public boolean getSendOutput () {
		return getBoolean(SENDOUTPUT);
	}

	public void setSendOutput (boolean sendOutput) {
		setBoolean(SENDOUTPUT, sendOutput);
	}

	public List<String> convertSupportFilesToList (String data) {
		return ListUtil.stringAsList(data, SUPPORTFILE_SEP);
	}
	
	public String convertSupportFilesToString (List<String> list) {
		return ListUtil.listAsString(list, SUPPORTFILE_SEP);
	}
}
