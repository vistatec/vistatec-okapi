/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.connectors.mymemory;

import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	public static final String KEY = "key";
	public static final String USEMT = "useMT";
	public static final String SENDIP = "sendIP";
	
	public Parameters () {
		super();
	}
	
	public Parameters (String initialData) {
		super(initialData);
	}
	
	public String getKey () {
		return getString(KEY);
	}

	public void setKey (String key) {
		setString(KEY, key);
	}

	public int getUseMT() {
		return getInteger(USEMT);
	}

	public void setUseMT (int useMT) {
		setInteger(USEMT, useMT);
	}
	
	public boolean getSendIP () {
		return getBoolean(SENDIP);
	}
	
	public void setSendIP (boolean sendIP) {
		setBoolean(SENDIP, sendIP);
	}

	@Override
	public void reset () {
		super.reset();
		setKey("mmDemo123");
		setUseMT(1);
		setSendIP(true);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(KEY, "Key", "Access key");
		desc.add(USEMT, "Provide also machine translation result", null);
		desc.add(SENDIP, "Send IP address (recommended for large volumes)", null);
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("MyMemory TM Connector Settings");
		// Key is used for setting translations, which is not implemented
		// The key is not used with REST interface
//		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(Parameters.KEY));
//		tip.setPassword(true);
		desc.addCheckboxPart(paramsDesc.get(USEMT));
		desc.addCheckboxPart(paramsDesc.get(SENDIP));
		return desc;
	}

}