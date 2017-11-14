/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.lib.extra;

import net.sf.okapi.common.BaseParameters;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.exceptions.OkapiBadFilterParametersException;

/**
 * Configurable Okapi component like a filter or pipeline step
 * 
 * @version 0.1 13.07.2009
 */

public abstract class OkapiComponent extends Component implements IConfigurable {

	private IParameters params;
	private String parametersClassName;
	
	abstract protected void component_init();
	
	protected void component_done() {
		
	}

	public IParameters getParameters() {
		return params;
	}
	
	public void setParameters(IParameters params) {
		this.params = (BaseParameters) params;
		
		if (params instanceof INotifiable)
			((INotifiable) params).exec(this, Notification.PARAMETERS_SET_OWNER, this);
		
		if (this instanceof INotifiable)
			((INotifiable) this).exec(this, Notification.PARAMETERS_CHANGED, null);
		
//		if (!Util.isEmpty(parametersClassName)) return; // This name is set by the first call from the filter's constructor
		if (params == null) return;
		if (params.getClass() == null) return;
		
		parametersClassName = params.getClass().getName();
	}

	protected <A> A getParameters(Class<A> expectedClass) {
		if (params == null) {
			throw new OkapiBadFilterParametersException("Filter parameters object is null.");			
		}
		
		if (!expectedClass.isInstance(params)) {
			
			String st = "null";
			if (params.getClass() != null) st = params.getClass().getName();
			
			throw new OkapiBadFilterParametersException(
					String.format("Parameters of class <%s> expected, but are <%s>",
							expectedClass.getName(), st));			
		}
		
		return expectedClass.cast(params);
	}

	public String getParametersClassName() {
		return parametersClassName;
	}

}
