/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.inconsistencycheck;

import java.io.File;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.common.uidescription.PathInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {

    private static final String CHECKINCONSISTENCIES = "checkInconststencies";
    private static final String CHECKPERFILE = "checkPerFile";
    private static final String OUTPUTPATH = "outputPath";
    private static final String DISPLAYOPTION = "displayOption";
    private static final String AUTOOPEN = "autoOpen";
    
    public static final String DISPLAYOPTION_ORIGINAL = "original";
    public static final String DISPLAYOPTION_GENERIC = "generic";
    public static final String DISPLAYOPTION_PLAIN = "plain";
    
    public Parameters() {
        super();
    }

    public boolean getCheckInconststencies () {
    	return getBoolean(CHECKINCONSISTENCIES);
    }
    
    public void setCheckInconststencies (boolean checkInconststencies) {
    	setBoolean(CHECKINCONSISTENCIES, checkInconststencies);
    }
    
    public boolean getCheckPerFile() {
    	return getBoolean(CHECKPERFILE);
    }

    public void setCheckPerFile(boolean checkPerFile) {
    	setBoolean(CHECKPERFILE, checkPerFile);
    }

    public String getOutputPath() {
        return getString(OUTPUTPATH);
    }

    public void setOutputPath(String outputPath) {
        setString(OUTPUTPATH, outputPath);
    }

    public String getDisplayOption () {
        return getString(DISPLAYOPTION);
    }
    
    public void setDisplayOption (String displayOption) {
        setString(DISPLAYOPTION, displayOption);
    }

    public boolean isAutoOpen() {
        return getBoolean(AUTOOPEN);
    }

    public void setAutoOpen(boolean autoOpen) {
        setBoolean(AUTOOPEN, autoOpen);
    }

    @Override
    public void reset() {
		super.reset();
    	setCheckInconststencies(true);
    	setCheckPerFile(false);
    	setOutputPath(Util.ROOT_DIRECTORY_VAR + File.separator + "inconsistency-report.xml");
    	setDisplayOption(DISPLAYOPTION_GENERIC);
    	setAutoOpen(true);
    }

    @Override
    public ParametersDescription getParametersDescription () {
        ParametersDescription desc = new ParametersDescription(this);
        desc.add(CHECKINCONSISTENCIES, "Check inconsistencies", null);
        desc.add(CHECKPERFILE, "Check for inconsistencies on a file-by-file basis", null);
        desc.add(OUTPUTPATH, "Path of the report file:", null);
        desc.add(DISPLAYOPTION, "Representation of the inline codes in the report", null);
        desc.add(AUTOOPEN, "Open the report file after completion", null);
        return desc;
    }

    @Override
    public EditorDescription createEditorDescription (ParametersDescription paramDesc) {
        EditorDescription desc = new EditorDescription("Inconsistency Check", true, false);

        CheckboxPart master = desc.addCheckboxPart(paramDesc.get(CHECKINCONSISTENCIES));
        
        CheckboxPart cbp = desc.addCheckboxPart(paramDesc.get(CHECKPERFILE));
        cbp.setMasterPart(master, true);
        
        PathInputPart pip = desc.addPathInputPart(paramDesc.get(OUTPUTPATH), "Inconsistency Report File", true);
        pip.setMasterPart(master, true);
        
        cbp = desc.addCheckboxPart(paramDesc.get(AUTOOPEN));
        cbp.setMasterPart(master, true);
        
        String[] values = {DISPLAYOPTION_ORIGINAL, DISPLAYOPTION_GENERIC, DISPLAYOPTION_PLAIN};
        String[] labels = {"Original codes", "Generic markers", "Plain text"};
		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(DISPLAYOPTION), values);
        lsp.setChoicesLabels(labels);
        lsp.setListType(ListSelectionPart.LISTTYPE_DROPDOWN);
        lsp.setMasterPart(master, true);
        
        return desc;
    }
}
