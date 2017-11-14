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

package net.sf.okapi.steps.msbatchtranslation;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.SeparatorPart;
import net.sf.okapi.common.uidescription.SpinInputPart;
import net.sf.okapi.common.uidescription.TextInputPart;
import net.sf.okapi.common.uidescription.TextLabelPart;

@EditorFor(SubmissionParameters.class)
public class SubmissionParameters extends StringParameters implements IEditorDescriptionProvider {

	private static final String AZUREKEY = "azureKey";
	private static final String CATEGORY = "category";
	private static final String BATCHSIZE = "batchSize";
	private static final String RATING = "rating";
	
	public SubmissionParameters () {
		super();
	}
	
	public SubmissionParameters (String initialData) {
		super(initialData);
	}

	@Override
	public void reset () {
		super.reset();
		// Default
		setAzureKey("");
		setCategory("");
		setBatchSize(80);
		setRating(4);
	}

	public int getBatchSize () {
		return getInteger(BATCHSIZE);
	}
	
	public void setBatchSize (int batchSize) {
		setInteger(BATCHSIZE, batchSize);
	}
	
	public int getRating () {
		return getInteger(RATING);
	}
	
	public void setRating (int rating) {
		setInteger(RATING, rating);
	}

	public String getAzureKey () {
		return getString(AZUREKEY);
	}

	public String getCategory () {
		return getString(CATEGORY);
	}

	public void setAzureKey (String azureKey) {
		setString(AZUREKEY, azureKey);
	}

	public void setCategory (String category) {
		setString(CATEGORY, category);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(AZUREKEY, "Azure Key", "Microsoft Azure subscription key");
		desc.add(CATEGORY, "Category", "Category code if accessing a trained system");
		desc.add(BATCHSIZE, "Batch size", "Number of segments to send in each batch");
		desc.add(RATING, "Default rating", "Default rating to use for if one is not provided with the translated segment");
		return desc;
	}

	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Microsoft Batch Submission Settings");

		TextLabelPart tlp = desc.addTextLabelPart("Powered by Microsoft\u00AE Translator"); // Required by TOS
		tlp.setVertical(true);
		SeparatorPart sp = desc.addSeparatorPart();
		sp.setVertical(true);

		TextInputPart tip = desc.addTextInputPart(paramsDesc.get(AZUREKEY));
		tip.setPassword(true);

		tip = desc.addTextInputPart(paramsDesc.get(CATEGORY));
		tip.setAllowEmpty(true);
		tip.setPassword(false);

		sp = desc.addSeparatorPart();
		sp.setVertical(true);
		
		SpinInputPart sip = desc.addSpinInputPart(paramsDesc.get(RATING));
		sip.setRange(-10, 10);
		
		sip = desc.addSpinInputPart(paramsDesc.get(BATCHSIZE));
		sip.setRange(1, 100);
		
		return desc;
	}

}
