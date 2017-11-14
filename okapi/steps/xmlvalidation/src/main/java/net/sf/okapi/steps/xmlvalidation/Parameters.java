/*===========================================================================
  Copyright (C) 2010-2017 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.steps.xmlvalidation;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.ReferenceParameter;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.common.uidescription.PathInputPart;


@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

	public static final int VALIDATIONTYPE_DTD = 0;
	public static final int VALIDATIONTYPE_SCHEMA = 1;
	public static final int VALIDATIONTYPE_RELAXNG = 2;
	
	private static String VALIDATE = "validate";
	private static String USEFOUNDDTD = "useFoundDTD";
	private static String SCHEMAPATH = "schemaPath";
	private static final String VALIDATIONTYPE = "validationType";

	public Parameters () {
		super();
	}
	
	public boolean isValidate () {
		return getBoolean(VALIDATE);
	}

	public void setValidate (boolean validate) {
		setBoolean(VALIDATE, validate);
	}
	
	public boolean getUseFoundDTD () {
		return getBoolean(USEFOUNDDTD);
	}

	public void setUseFoundDTD (boolean useFoundDTD) {
		setBoolean(USEFOUNDDTD, useFoundDTD);
	}
	
	@ReferenceParameter
	public String getSchemaPath () {
		return getString(SCHEMAPATH);
	}

	public void setSchemaPath (String schemaPath) {
		setString(SCHEMAPATH, schemaPath);
	}
	
	public int getValidationType () {
		return getInteger(VALIDATIONTYPE);
	}
	
	public void setValidationType (int validationType) {
		setInteger(VALIDATIONTYPE, validationType);
	}
	
	public void reset () {
		super.reset();
		setSchemaPath("");
		setValidate(false);
		setValidationType(0);
		setUseFoundDTD(true);
	}

	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(USEFOUNDDTD,
			"Use DTD declarations found in the input documents", null);
		desc.add(VALIDATE,
			"Validate the documents structure [Well-formedness is always checked]", null);
		desc.add(VALIDATIONTYPE,
			"Type of validation", "Indicates which validation to use");
		desc.add(SCHEMAPATH,
			"Path of the XML Schema", "Full path of the XML Schema.");
	
		return desc;
	}

	
	@Override
	public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
		EditorDescription desc = new EditorDescription("XML Validation", true, false);

		desc.addCheckboxPart(paramDesc.get(USEFOUNDDTD));

		CheckboxPart cbvalidate = desc.addCheckboxPart(paramDesc.get(VALIDATE));

		String[] labels = {
			"DTD",
			"XML Schema",
			"RelaxNG Schema"
		};
		String[] values = {
			"0",
			"1",
			"2"
		};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(VALIDATIONTYPE), values);
		lsp.setChoicesLabels(labels);
		lsp.setMasterPart(cbvalidate, true);
		
		PathInputPart part = desc.addPathInputPart(paramDesc.get(SCHEMAPATH), "Schema", false);
		part.setBrowseFilters("Schema Files (*.xsd)\tAll Files (*.*)", "*.xsd\t*.*");
		part.setAllowEmpty(true);
		part.setMasterPart(cbvalidate, true);
	
		return desc;
	}
}
