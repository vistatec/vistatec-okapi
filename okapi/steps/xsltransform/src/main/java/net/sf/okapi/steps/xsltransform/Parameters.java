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

package net.sf.okapi.steps.xsltransform;

import net.sf.okapi.common.ReferenceParameter;
import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {

	private static final String XSLTPATH = "xsltPath";
	private static final String PARAMLIST = "paramList";
	private static final String USECUSTOMTRANSFORMER = "useCustomTransformer";
	private static final String FACTORYCLASS = "factoryClass";
	private static final String XPATHCLASS = "xpathClass";
	private static final String PASSONOUTPUT = "passOnOutput";

	public Parameters () {
		super();
	}
	
	public void reset () {
		super.reset();
		setXsltPath("");
		setParamList("");
		setUseCustomTransformer(false);
		// Example: net.sf.saxon.TransformerFactoryImpl
		setFactoryClass("");
		setXpathClass("");
		setPassOnOutput(true);
	}
	
	public void setXsltPath (String xsltPath) {
		setString(XSLTPATH, xsltPath);
	}
	
	@ReferenceParameter
	public String getXsltPath () {
		return getString(XSLTPATH);
	}

	public String getParamList() {
		return getString(PARAMLIST);
	}

	public void setParamList(String paramList) {
		setString(PARAMLIST, paramList);
	}

	public boolean getUseCustomTransformer() {
		return getBoolean(USECUSTOMTRANSFORMER);
	}

	public void setUseCustomTransformer(boolean useCustomTransformer) {
		setBoolean(USECUSTOMTRANSFORMER, useCustomTransformer);
	}

	public String getFactoryClass() {
		return getString(FACTORYCLASS);
	}

	public void setFactoryClass(String factoryClass) {
		setString(FACTORYCLASS, factoryClass);
	}

	public String getXpathClass() {
		return getString(XPATHCLASS);
	}

	public void setXpathClass(String xpathClass) {
		setString(XPATHCLASS, xpathClass);
	}

	public boolean getPassOnOutput() {
		return getBoolean(PASSONOUTPUT);
	}

	public void setPassOnOutput(boolean passOnOutput) {
		setBoolean(PASSONOUTPUT, passOnOutput);
	}
}
