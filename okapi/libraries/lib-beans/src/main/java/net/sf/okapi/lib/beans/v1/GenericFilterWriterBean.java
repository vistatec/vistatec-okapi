/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class GenericFilterWriterBean extends PersistenceBean<GenericFilterWriter> {

	@Override
	protected GenericFilterWriter createObject(IPersistenceSession session) {
		EncoderManager encoderManager = new EncoderManager();
		encoderManager.setAllKnownMappings();
		return new GenericFilterWriter(new GenericSkeletonWriter(), encoderManager);
	}

	@Override
	protected void fromObject(GenericFilterWriter obj, IPersistenceSession session) {
	}

	@Override
	protected void setObject(GenericFilterWriter obj, IPersistenceSession session) {
	}	
}
