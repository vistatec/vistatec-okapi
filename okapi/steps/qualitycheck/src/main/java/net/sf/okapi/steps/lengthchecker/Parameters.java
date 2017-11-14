/*===========================================================================
  Copyright (C) 2016 by the Okapi Framework contributors
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

package net.sf.okapi.steps.lengthchecker;

import net.sf.okapi.common.StringParameters;

public class Parameters extends StringParameters {

	private static final String CHECKSTORAGESIZE = "checkStorageSize";
	private static final String CHECKMAXCHARLENGTH = "checkMaxCharLength";
	private static final String MAXCHARLENGTHBREAK = "maxCharLengthBreak";
	private static final String MAXCHARLENGTHABOVE = "maxCharLengthAbove";
	private static final String MAXCHARLENGTHBELOW = "maxCharLengthBelow";
	private static final String CHECKMINCHARLENGTH = "checkMinCharLength";
	private static final String MINCHARLENGTHBREAK = "minCharLengthBreak";
	private static final String MINCHARLENGTHABOVE = "minCharLengthAbove";
	private static final String MINCHARLENGTHBELOW = "minCharLengthBelow";
	private static final String CHECKABSOLUTEMAXCHARLENGTH = "checkAbsoluteMaxCharLength";
	private static final String ABSOLUTEMAXCHARLENGTH = "absoluteMaxCharLength";

	public Parameters() {
		super();
	}

	public boolean getCheckStorageSize() {
		return getBoolean(CHECKSTORAGESIZE);
	}

	public void setCheckStorageSize(boolean checkStorageSize) {
		setBoolean(CHECKSTORAGESIZE, checkStorageSize);
	}

	public boolean getCheckMaxCharLength() {
		return getBoolean(CHECKMAXCHARLENGTH);
	}

	public void setCheckMaxCharLength(boolean checkMaxCharLength) {
		setBoolean(CHECKMAXCHARLENGTH, checkMaxCharLength);
	}

	public int getMaxCharLengthBreak() {
		return getInteger(MAXCHARLENGTHBREAK);
	}

	public void setMaxCharLengthBreak(int maxCharLengthBreak) {
		setInteger(MAXCHARLENGTHBREAK, maxCharLengthBreak);
	}

	public int getMaxCharLengthAbove() {
		return getInteger(MAXCHARLENGTHABOVE);
	}

	public void setMaxCharLengthAbove(int maxCharLengthAbove) {
		setInteger(MAXCHARLENGTHABOVE, maxCharLengthAbove);
	}

	public int getMaxCharLengthBelow() {
		return getInteger(MAXCHARLENGTHBELOW);
	}

	public void setMaxCharLengthBelow(int maxCharLengthBelow) {
		setInteger(MAXCHARLENGTHBELOW, maxCharLengthBelow);
	}

	public boolean getCheckMinCharLength() {
		return getBoolean(CHECKMINCHARLENGTH);
	}

	public void setCheckMinCharLength(boolean checkMinCharLength) {
		setBoolean(CHECKMINCHARLENGTH, checkMinCharLength);
	}

	public int getMinCharLengthBreak() {
		return getInteger(MINCHARLENGTHBREAK);
	}

	public void setMinCharLengthBreak(int minCharLengthBreak) {
		setInteger(MINCHARLENGTHBREAK, minCharLengthBreak);
	}

	public int getMinCharLengthAbove() {
		return getInteger(MINCHARLENGTHABOVE);
	}

	public void setMinCharLengthAbove(int minCharLengthAbove) {
		setInteger(MINCHARLENGTHABOVE, minCharLengthAbove);
	}

	public int getMinCharLengthBelow() {
		return getInteger(MINCHARLENGTHBELOW);
	}

	public void setMinCharLengthBelow(int minCharLengthBelow) {
		setInteger(MINCHARLENGTHBELOW, minCharLengthBelow);
	}

	public boolean getCheckAbsoluteMaxCharLength() {
		return getBoolean(CHECKABSOLUTEMAXCHARLENGTH);
	}

	public void setCheckAbsoluteMaxCharLength(boolean checkAbsoluteMaxCharLength) {
		setBoolean(CHECKABSOLUTEMAXCHARLENGTH, checkAbsoluteMaxCharLength);
	}

	public int getAbsoluteMaxCharLength() {
		return getInteger(ABSOLUTEMAXCHARLENGTH);
	}

	public void setAbsoluteMaxCharLength(int absoluteMaxCharLength) {
		setInteger(ABSOLUTEMAXCHARLENGTH, absoluteMaxCharLength);
	}

	
	
	@Override
	public void reset() {
		super.reset();		
		setCheckMaxCharLength(true);
		setMaxCharLengthBreak(20);
		setMaxCharLengthAbove(200);
		setMaxCharLengthBelow(350);
		setCheckMinCharLength(true);
		setMinCharLengthBreak(20);
		setMinCharLengthAbove(45);
		setMinCharLengthBelow(30);

		setCheckStorageSize(true);

		setCheckAbsoluteMaxCharLength(false);
		setAbsoluteMaxCharLength(255);

	}

	@Override
	public void fromString(String data) {
		super.fromString(data);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
