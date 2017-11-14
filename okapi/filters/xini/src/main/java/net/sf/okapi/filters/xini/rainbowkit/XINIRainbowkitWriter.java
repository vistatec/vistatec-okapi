/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xini.rainbowkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

public class XINIRainbowkitWriter extends FilterEventToXiniTransformer implements IFilterWriter {

	private EncoderManager encodingManager;
	private IParameters params;
	private String xiniPath;
	private String nextPageName;

	public XINIRainbowkitWriter() {

	}

	@Override
	public void cancel() {
	}

	@Override
	public void close() {
	}

	@Override
	public String getName() {
		return "XINIRainbowKitWriter";
	}

	@Override
	public void setOptions(LocaleId locale, String defaultEncoding) {
	}

	@Override
	public void setOutput(String path) {
	}

	@Override
	public void setOutput(OutputStream output) {
	}

	@Override
	public Event handleEvent(Event event) {

		switch (event.getEventType()) {
		case START_DOCUMENT:
			startPage(nextPageName);
			break;
		case TEXT_UNIT:
			transformTextUnit(event.getTextUnit());
			break;
		case START_GROUP:
			pushGroupToStack(event.getStartGroup());
			break;
		case END_GROUP:
			popGroupFromStack();
			break;
		default:
			break;
		}
		return event;
	}

	public void writeXINI() {

		try {
			File outputPath = new File(xiniPath);

			if (!outputPath.getParentFile().exists()) {
				outputPath.getParentFile().mkdirs();
			}

			if (!outputPath.exists())
				outputPath.createNewFile();

			FileOutputStream fos = new FileOutputStream(outputPath);
			marshall(fos);
			fos.close();
		}
		catch (FileNotFoundException e) {
			// Should be impossible. We created the file.
			throw new OkapiException(e);
		}
		catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = params;

	}

	@Override
	public EncoderManager getEncoderManager() {
		return encodingManager;
	}

	public void setOutputPath(String path) {
		this.xiniPath = path;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter() {
		return null;
	}

	public void setNextPageName(String nextPageName) {
		this.nextPageName = nextPageName;
	}

}
