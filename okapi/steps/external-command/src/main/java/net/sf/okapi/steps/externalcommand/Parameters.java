/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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

package net.sf.okapi.steps.externalcommand;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.TextInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {
	
	private static final String COMMAND = "command";
	private static final String TIMEOUT = "timeout";

	public Parameters () {
		super();
	}
	
	public String getCommand() {
		return getString(COMMAND);
	}

	public void setCommand(String command) {
		setString(COMMAND, command);
	}

	public int getTimeout() {
		return getInteger(TIMEOUT);
	}

	public void setTimeout(int timeout) {
		setInteger(TIMEOUT, timeout);
	}

	@Override
	public void reset() {
		super.reset();
		setCommand("");
		setTimeout(-1); // default is no timeout
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(COMMAND, "Command line", "Command path to execute");
		desc.add(TIMEOUT, "Timeout", "Timeout in seconds after which the command is cancelled (use -1 for no timeout)");
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("External Command", true, false);
		desc.addTextInputPart(paramsDesc.get(COMMAND));
		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(TIMEOUT));
		tip.setVertical(false);
		return desc;
	}

}