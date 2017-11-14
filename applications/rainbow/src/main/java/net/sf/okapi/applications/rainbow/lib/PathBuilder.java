/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.lib;

import java.io.File;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ParametersString;
import net.sf.okapi.common.Util;

public class PathBuilder {

	public static final int EXTTYPE_PREPEND     = 0;
	public static final int EXTTYPE_APPEND      = 1;
	public static final int EXTTYPE_REPLACE     = 2;

	private boolean useSubFolder;
	private String subFolder;
	private boolean useExt;
	private String ext;
	private int extType;
	private String prefix;
	private boolean usePrefix;
	private String suffix;
	private boolean useSuffix;
	private boolean useReplace;
	private String search;
	private String replace;

	public PathBuilder () {
		reset();
	}
	
	public void reset () {
		useSubFolder = false;
		subFolder = "";
		useExt = true;
		ext = ".${TrgLang}";
		extType = EXTTYPE_PREPEND;
		prefix = "";
		suffix = "";
		usePrefix = false;
		useSuffix = false;
		useReplace = false;
		search = "";
		replace = "";
	}

	public void copyFrom (PathBuilder pathBuilder) {
		useSubFolder = pathBuilder.useSubFolder;
		subFolder = pathBuilder.subFolder;
		useExt = pathBuilder.useExt;
		ext = pathBuilder.ext;
		extType = pathBuilder.extType;
		prefix = pathBuilder.prefix;
		suffix = pathBuilder.suffix;
		usePrefix = pathBuilder.usePrefix;
		useSuffix = pathBuilder.useSuffix;
		useReplace = pathBuilder.useReplace;
		search = pathBuilder.search;
		replace = pathBuilder.replace;
	}

	public String getSubfolder () {
		return subFolder;
	}
	
	public void setSubfolder (String p_sValue) {
		subFolder = p_sValue;
	}

	public boolean useSubfolder () {
		return useSubFolder;
	}
	
	public void setUseSubfolder (boolean p_bValue) {
		useSubFolder = p_bValue;
	}

	public String getExtension () {
		return ext;
	}
	
	public void setExtension (String p_sValue) {
		ext = p_sValue;
	}

	public boolean useExtension () {
		return useExt;
	}
	
	public void setUseExtension (boolean p_bValue) {
		useExt = p_bValue;
	}

	public int getExtensionType () {
		return extType;
	}
	
	public void setExtensionType (int p_nValue) {
		extType = p_nValue;
	}

	public String getPrefix () {
		return prefix;
	}
	
	public void setPrefix (String p_sValue) {
		prefix = p_sValue;
	}

	public boolean usePrefix () {
		return usePrefix;
	}
	
	public void setUsePrefix (boolean p_bValue) {
		usePrefix = p_bValue;
	}

	public String getSuffix () {
		return suffix;
	}
	
	public void setSuffix (String p_sValue) {
		suffix = p_sValue;
	}

	public boolean useSuffix () {
		return useSuffix;
	}
	
	public void setUseSuffix (boolean p_bValue) {
		useSuffix = p_bValue;
	}

	public String getSearch () {
		return search;
	}
	
	public void setSearch (String p_sValue) {
		search = p_sValue;
	}

	public String getReplace () {
		return replace;
	}
	
	public void setReplace (String p_sValue) {
		replace = p_sValue;
	}

	public boolean useReplace () {
		return useReplace;
	}
	
	public void setUseReplace (boolean p_bValue) {
		useReplace = p_bValue;
	}
	
	@Override
	public String toString () {
		ParametersString tmp = new ParametersString();
		tmp.setBoolean("useSubFolder", useSubfolder());
		tmp.setString("subFolder", getSubfolder());
		tmp.setBoolean("useExtension", useExtension());
		tmp.setInteger("extensionType", getExtensionType());
		tmp.setString("extension", getExtension());
		tmp.setBoolean("usePrefix", usePrefix());
		tmp.setString("prefix", getPrefix());
		tmp.setBoolean("useSuffix", useSuffix());
		tmp.setString("suffix", getSuffix());
		tmp.setBoolean("useReplace", useReplace());
		tmp.setString("search", getSearch());
		tmp.setString("replace", getReplace());
		return tmp.toString();
	}

	/**
	 * Transforms a given full path to a new path.
	 * @param p_sFullPath The path to transform.
	 * @param p_sOriginalRoot Root in the path to transform.
	 * @param p_sNewRoot New root to use.  If p_sNewRoot is null, 
	 * p_sOriginalRoot is used.
	 * @param srcLang Language code.
	 * @return The transformed path.
	 */
	public String getPath (String p_sFullPath,
		String p_sOriginalRoot,
		String p_sNewRoot,
		String srcLang,
		String trgLang)
	{
		String sPath = p_sFullPath.substring(p_sOriginalRoot.length());

		// Extension
		String sExt = Util.getExtension(sPath);
		if ( useExtension() ) {
			switch ( getExtensionType() ) {
				case EXTTYPE_REPLACE:
					sExt = getExtension();
					break;
				case EXTTYPE_APPEND:
					sExt += getExtension();
					break;
				case EXTTYPE_PREPEND:
					sExt = getExtension() + sExt;
					break;
			}
		}

		String sFile = Util.getFilename(sPath, false);
		if ( usePrefix() ) {
			sFile = getPrefix() + sFile;
		}
		if ( useSuffix() ) {
			sFile += getSuffix();
		}

		// Get the root
		String sTmp = (((p_sNewRoot==null)||(p_sNewRoot.length()==0))
			? p_sOriginalRoot : p_sNewRoot) + File.separatorChar;
			
		// Get optional extra sub-folder
		if ( useSubfolder() ) {
			String sSub = getSubfolder();
			if (( sSub != null ) && ( sSub.length() > 0 ))
				sTmp += (sSub + File.separatorChar);
		}

		String relPath = Util.getDirectoryName(sPath);
		if ( relPath.length() != 0 ) {
			sTmp += (relPath.substring(1) + File.separatorChar);
		}
		
		sTmp += (sFile + sExt);
		
		// Search/Replace text if needed
		if ( useReplace() && ( getSearch().length() != 0 ))
			sTmp = sTmp.replace(getSearch(), getReplace());

		return LocaleId.replaceVariables(sTmp, srcLang, trgLang);
	}
}
