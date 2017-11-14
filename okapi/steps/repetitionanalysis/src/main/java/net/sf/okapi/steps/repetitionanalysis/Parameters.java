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

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.SpinInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String FUZZYTHRESHOLD = "fuzzyThreshold";
	private static final String MAXHITS = "maxHits";
	
	public Parameters () {
		super();
	}
	
	public void reset() {
		super.reset();
		setFuzzyThreshold(100);
		setMaxHits(20);
	}
	
	public void setFuzzyThreshold(int fuzzyThreshold) {
		setInteger(FUZZYTHRESHOLD, fuzzyThreshold);
	}

	public int getFuzzyThreshold() {
		return getInteger(FUZZYTHRESHOLD);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(FUZZYTHRESHOLD, "Fuzzy threshold (1-100)", "Fuzzy threshold for fuzzy repetitions. Leave 100 for exact repetitions only.");
		desc.add(MAXHITS, "Max hits", "Maximum number of exact and fuzzy repetitions to keep track of for every segment.");
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("Repetition Analysis");		
		SpinInputPart sip = desc.addSpinInputPart(paramDesc.get(FUZZYTHRESHOLD));
		SpinInputPart sip2 = desc.addSpinInputPart(paramDesc.get(MAXHITS));
		sip.setRange(1, 100);		
		sip2.setRange(1, 100);
		return desc;
	}

	public int getMaxHits() {
		return getInteger(MAXHITS);
	}

	public void setMaxHits(int maxHits) {
		setInteger(MAXHITS, maxHits);
	}

}
