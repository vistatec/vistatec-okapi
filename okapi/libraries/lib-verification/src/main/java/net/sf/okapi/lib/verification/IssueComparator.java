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

package net.sf.okapi.lib.verification;

import java.util.Comparator;

public class IssueComparator implements Comparator<Issue> {

	public static final int DIR_ASC = 1;
	public static final int DIR_DESC = -1;
	
	public static final int TYPE_ENABLED = 0;
	public static final int TYPE_SEVERITY = 1;
	public static final int TYPE_TU = 2;
	public static final int TYPE_SEG = 3;
	public static final int TYPE_MESSAGE = 4;
	
	private int type = 0;
	private int direction = 1;
	
	public IssueComparator (int type,
		int direction)
	{
		this.type = type;
		this.direction = direction;
	}
	
	@Override
	public int compare (Issue issue1,
		Issue issue2)
	{
		switch ( type ) {
		case TYPE_ENABLED:
			if ( issue1.getEnabled() == issue2.getEnabled() ) return 0;
			if ( direction == DIR_ASC ) {
				return issue1.getEnabled() ? 1 : -1;
			}
			return issue1.getEnabled() ? -1 : 1;

		case TYPE_SEVERITY:
			if ( issue1.getSeverity() == issue2.getSeverity() ) return 0;
			if ( direction == DIR_ASC ) {
				return (issue1.getSeverity() > issue2.getSeverity()) ? 1 : -1;
			}
			return (issue1.getSeverity() > issue2.getSeverity()) ? -1 : 1;

		case TYPE_TU:
			String key1 = issue1.getDocumentURI().toString()+issue1.getTuId();
			String key2 = issue2.getDocumentURI().toString()+issue2.getTuId();
			if ( key1.equals(key2) ) return 0;
			if ( direction == DIR_ASC ) {
				return key1.compareTo(key2);
			}
			return key2.compareTo(key1);

		case TYPE_SEG:
			key1 = issue1.getDocumentURI().toString()+issue1.getTuId() + (issue1.getSegId()==null ? "" : issue1.getSegId());
			key2 = issue2.getDocumentURI().toString()+issue2.getTuId() + (issue2.getSegId()==null ? "" : issue2.getSegId());
			if ( key1.equals(key2) ) return 0;
			if ( direction == DIR_ASC ) {
				return key1.compareTo(key2);
			}
			return key2.compareTo(key1);

		case TYPE_MESSAGE:
			if ( issue1.getMessage().equals(issue2.getMessage()) ) return 0;
			if ( direction == DIR_ASC ) {
				return issue1.getMessage().compareTo(issue2.getMessage());
			}
			return issue2.getMessage().compareTo(issue1.getMessage());
		}
		return 0;
	}

}
