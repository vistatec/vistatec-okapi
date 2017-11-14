/*===========================================================================
  Copyright (C) 2011-2017 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.google;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

public class GoogleMTv2Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String APIKEY = "apiKey";
	private static final String RETRY_MS = "retryIntervalMs";
	private static final String RETRY_COUNT = "retryCount";
	private static final String USE_PBMT = "usePBMT";
	
	public GoogleMTv2Parameters () {
	}
	
	public String getApiKey () {
		return getString(APIKEY).trim();
	}

	public void setApiKey (String apiKey) {
	    if (apiKey != null) {
	        apiKey = apiKey.trim();
	    }
		setString(APIKEY, apiKey);
	}

	public int getRetryIntervalMs () {
	    return getInteger(RETRY_MS);
	}

	public void setRetryIntervalMs (int retryMs) {
	    setInteger(RETRY_MS, retryMs);
	}

	public int getRetryCount () {
	    return getInteger(RETRY_COUNT);
	}

	public void setRetryCount (int retryCount) {
	    setInteger(RETRY_COUNT, retryCount);
	}

	public boolean getUsePBMT () {
	    return getBoolean(USE_PBMT);
	}

	public void setUsePBMT (boolean usePBMT) {
	    setBoolean(USE_PBMT, usePBMT);
	}

	@Override
	public void reset () {
		super.reset();
		setApiKey("");
		setUsePBMT(false);
		// The most likely error we will encounter is the rate limit of 100k
		// characters translated per 100 seconds.  We will retry every 10s
		// up to 10x, which is enough to flush the rate limit.
		setRetryIntervalMs(10 * 1000);
		setRetryCount(10);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(APIKEY,
			"Google API key",
			"The Google API key to identify the application/user");
		desc.add(USE_PBMT,
		    "Use Phrase-Based MT",
		    "Use the legacy PBMT system rather than Neural MT");
		desc.add(RETRY_COUNT,
	        "Retry Count",
	        "Number of retries to attempt before failing");
		desc.add(RETRY_MS,
	        "Retry Interval (ms)",
	        "Time to wait before retrying a failed query");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Google Translate v2 Connector Settings", true, false);
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(APIKEY));
		tip.setPassword(true);
		desc.addCheckboxPart(paramsDesc.get(USE_PBMT));
		desc.addTextInputPart(paramsDesc.get(RETRY_COUNT));
		desc.addTextInputPart(paramsDesc.get(RETRY_MS));
		return desc;
	}

}
