/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tokenization.common;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.ParametersString;
import net.sf.okapi.lib.extra.AbstractParameters;
import net.sf.okapi.lib.extra.Notification;

public class StructureParameters extends AbstractParameters {

	public String description;
	private List<StructureParametersItem> items = new ArrayList<StructureParametersItem>();	

	@Override
	protected void parameters_load(ParametersString buffer) {

		description = buffer.getString("description", "");
		loadGroup(buffer, items, StructureParametersItem.class);
		
		if (owner != null)
			owner.exec(this, Notification.PARAMETERS_CHANGED, null);
	}

	@Override
	protected void parameters_reset() {
		
		description = "";
		
		if (items == null) return;
		items.clear();		
	}

	@Override
	protected void parameters_save(ParametersString buffer) {
		
		buffer.setString("description", description);
		saveGroup(buffer, items, StructureParametersItem.class);
	}

	public List<StructureParametersItem> getItems() {
		
		return items;
	}

}
