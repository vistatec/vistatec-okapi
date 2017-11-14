/*===========================================================================
  Copyright (C) 2010 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v1;

import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class TextPartBean extends PersistenceBean<TextPart> {
	private TextFragmentBean text = new TextFragmentBean();
	
	@Override
	protected TextPart createObject(IPersistenceSession session) {
		return new TextPart(text.get(TextFragment.class, session));
	}

	@Override
	protected void fromObject(TextPart obj, IPersistenceSession session) {
		if (obj instanceof TextPart) {
			TextPart tp = (TextPart) obj;
			
			text.set(tp.getContent(), session);
		}
	}

	@Override
	protected void setObject(TextPart obj, IPersistenceSession session) {
	}

	public void setPart(TextFragmentBean text) {
		this.text = text;
	}

	public TextFragmentBean getPart() {
		return text;
	}
}
