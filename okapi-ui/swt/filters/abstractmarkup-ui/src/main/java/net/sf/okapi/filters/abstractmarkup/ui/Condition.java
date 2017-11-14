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

package net.sf.okapi.filters.abstractmarkup.ui;

import net.sf.okapi.common.ListUtil;

class Condition {

	String part1;
	String operator;
	String part2;

	@Override
	public Condition clone () {
		Condition newCond = new Condition();
		newCond.part1 = part1;
		newCond.operator = operator;
		newCond.part2 = part2;
		return newCond;
	}
	
	@Override
	public String toString () {
		StringBuilder tmp = new StringBuilder();
		tmp.append(String.format("['%s', %s, ", part1, operator));
		if ( part2.indexOf(',') != -1 ) {
			java.util.List<String> list = ListUtil.stringAsList(part2);
			tmp.append("[");
			for ( int i=0; i<list.size(); i++ ) {
				if ( i > 0 ) tmp.append(", ");
				tmp.append(quotedValue(list.get(i)));
			}
			tmp.append("]");
		}
		else {
			tmp.append(quotedValue(part2));
		}
		tmp.append("]");
		return tmp.toString();
	}
	
	private String quotedValue (String value) {
		return "'"+value.replace("'", "''")+"'";
	}

}
