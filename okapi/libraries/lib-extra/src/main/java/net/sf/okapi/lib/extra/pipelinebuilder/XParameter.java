/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.extra.pipelinebuilder;

import net.sf.okapi.common.pipeline.annotations.StepParameterType;

public class XParameter {
	private StepParameterType type = null;
	private String name;
	private Object value;
	private boolean asGroup = false;

	// Type safety constructors
	public XParameter(String name, String value) {
		setParameter(name, value);
	}
	
	public XParameter(String name, String value, boolean asGroup) {
		setParameter(name, value);
		this.asGroup = asGroup;
	}

	public XParameter(String name, int value) {
		setParameter(name, value);
	}
	
	public XParameter(String name, boolean value) {
		setParameter(name, value);
	}
	
//	public XParameter(StepParameterType type, RawDocument value) {
//		this(type);
//	}
//	
//	public XParameter(StepParameterType type, URI value) {
//		this(type);
//	}
//	
//	public XParameter(StepParameterType type, LocaleId value) {
//		this(type);
//	}
//	
//	public XParameter(StepParameterType type, String value) {
//		this(type);
//	}
//	
//	public XParameter(StepParameterType type, IFilterConfigurationMapper value) {
//		this(type);
//	}
	
	public XParameter(StepParameterType type, Object value) {
		super();
		this.type = type;
		this.value = value;
	}
	
//	public XParameter(StepParameterType type, int value) {
//		this(type);
//	}

	private void setParameter(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		return getName() + "=" + getValue();
	}

	public StepParameterType getType() {
		return type;
	}

	public boolean isAsGroup() {
		return asGroup;
	}	
}
