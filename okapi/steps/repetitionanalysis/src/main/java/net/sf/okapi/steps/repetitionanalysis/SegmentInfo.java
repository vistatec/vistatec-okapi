/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.repetitionanalysis;

import net.sf.okapi.tm.pensieve.common.Metadata;
import net.sf.okapi.tm.pensieve.common.MetadataType;

public class SegmentInfo {

	private String tuid;
	private String groupId;
	private String segId;
	
	public SegmentInfo() {
		super();
	}
			
	public SegmentInfo(String tuid, String groupId, String segId) {
		super();
		this.tuid = tuid;
		this.groupId = groupId;
		this.segId = segId;
	}

	public SegmentInfo(Metadata metadata) {
		this(metadata.get(MetadataType.ID), 
			 metadata.get(MetadataType.GROUP_NAME),				
			 metadata.get(MetadataType.FILE_NAME));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SegmentInfo) {
			SegmentInfo si = (SegmentInfo) obj;
			if (!si.tuid.equals(this.tuid)) return false;
			if (!si.groupId.equals(this.groupId)) return false;
			if (!si.segId.equals(this.segId)) return false;
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (String.format("%s %s %s", tuid, groupId, segId)).hashCode();
	}
	
	public String getTuid() {
		return tuid;
	}
	
	public void setTuid(String tuid) {
		this.tuid = tuid;
	}
	
	public String getGroupId() {
		return groupId;
	}
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public String getSegId() {
		return segId;
	}
	
	public void setSegId(String segId) {
		this.segId = segId;
	}
}
