/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

import java.util.Comparator;

import net.sf.okapi.common.StringUtil;

public class CodeComparatorOnOuterData implements Comparator<Code> {

	@Override
	public int compare(Code c1, Code c2) {	  
		// FIXME: remove whitespace because the XLIFF/TMX etc.. code data may be normalized
		String data1 = StringUtil.removeWhiteSpace(c1.getOuterData());
		String data2 = StringUtil.removeWhiteSpace(c2.getOuterData());
				
		if (data1.equals(data2) && c1.getTagType() == c2.getTagType()) {
			return 0;
		} else if (data1.equals(data2) && c1.getTagType() != c2.getTagType()) {
			return c1.getTagType().compareTo(c2.tagType); 
		} else {
			return data1.compareTo(data2);
		}
	}
}
