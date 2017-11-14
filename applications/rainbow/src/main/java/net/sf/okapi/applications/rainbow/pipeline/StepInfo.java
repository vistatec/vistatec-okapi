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

package net.sf.okapi.applications.rainbow.pipeline;

public class StepInfo {

	public String name;
	public String description;
	public String stepClass;
	public ClassLoader loader;
	public String paramsClass;
	public String paramsData;

	public StepInfo (String name,
		String description,
		String stepClass,
		ClassLoader loader,
		String parametersClass)
	{
		this.name = name;
		this.description = description;
		this.stepClass = stepClass;
		this.paramsClass = parametersClass;
		this.loader = loader;
	}

	@Override
	public StepInfo clone () {
		StepInfo newStep = new StepInfo(name, description, stepClass, loader, paramsClass);
		newStep.paramsData = paramsData;
		return newStep;
	}

}
