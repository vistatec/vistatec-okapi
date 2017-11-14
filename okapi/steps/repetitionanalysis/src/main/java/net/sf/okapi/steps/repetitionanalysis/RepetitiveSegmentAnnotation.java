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

package net.sf.okapi.steps.repetitionanalysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.tm.pensieve.common.TmHit;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;

public class RepetitiveSegmentAnnotation implements IAnnotation {
	
	private SegmentInfo info;
	private Map<SegmentInfo, Float> map; // TranslationUnit Id + score

	public RepetitiveSegmentAnnotation(SegmentInfo info, Map<SegmentInfo, Float> map) {
		super();
		this.info = info;
		this.map = map;
	}
	
	public RepetitiveSegmentAnnotation(SegmentInfo info, List<TmHit> hits) {
		super();
		this.info = info;
		map = new HashMap<SegmentInfo, Float> ();
		if (hits == null) return;
		
		for (TmHit hit : hits) {
			TranslationUnit hitTu = hit.getTu();
			//map.put(hitTu.getMetadataValue(MetadataType.ID), hit.getScore());
			map.put(new SegmentInfo(hitTu.getMetadata()), hit.getScore());
		}
	}
	
	public Map<SegmentInfo, Float> getMap() {
		return Collections.unmodifiableMap(map);
	}

	public void setMap(Map<SegmentInfo, Float> map) {
		this.map = map;
	}

	public SegmentInfo getInfo() {
		return info;
	}

	public void setInfo(SegmentInfo info) {
		this.info = info;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (SegmentInfo refInfo : map.keySet()) {
			sb.append(String.format(", %s - %3.2f", refInfo.getTuid(), map.get(refInfo)));
		}
		return String.format("(tuid: %s groupId: %s segId: %s)%s", 
				info.getTuid(), info.getGroupId(), info.getSegId(), sb.toString());
	}

}
