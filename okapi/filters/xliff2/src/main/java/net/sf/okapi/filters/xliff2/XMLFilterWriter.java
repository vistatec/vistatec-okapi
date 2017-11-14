/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xliff2;

import java.io.IOException;

import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public class XMLFilterWriter extends GenericFilterWriter {
	public XMLFilterWriter (ISkeletonWriter skeletonWriter,
		EncoderManager encodermanager)
	{
		super(skeletonWriter, encodermanager);
	}
	
	@Override
	public String getName () {
		return "XmlFilterWriter";
	}	

	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return super.getSkeletonWriter();
	}
	
	@Override
	protected void processTextUnit (ITextUnit resource) throws IOException {
		writer.write(getSkeletonWriter().processTextUnit(resource));
	}
}
