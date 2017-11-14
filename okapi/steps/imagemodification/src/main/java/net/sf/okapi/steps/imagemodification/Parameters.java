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

package net.sf.okapi.steps.imagemodification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.ParametersDescription;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.uidescription.CheckboxPart;
import net.sf.okapi.common.uidescription.EditorDescription;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;
import net.sf.okapi.common.uidescription.ListSelectionPart;
import net.sf.okapi.common.uidescription.SpinInputPart;

@EditorFor(Parameters.class)
public class Parameters extends StringParameters implements IEditorDescriptionProvider {
	
	private static final String SCALEWIDTH = "scaleWidth";
	private static final String SCALEHEIGHT = "scaleHeight";
	private static final String FORMAT = "format";
	private static final String MAKEGRAY = "makeGray";

	public Parameters () {
		super();
	}
	
	public int getScaleWidth () {
		return getInteger(SCALEWIDTH);
	}

	public void setScaleWidth (int scaleWidth) {
		setInteger(SCALEWIDTH, scaleWidth);
	}

	public int getScaleHeight() {
		return getInteger(SCALEHEIGHT);
	}

	public void setScaleHeight (int scaleHeight) {
		setInteger(SCALEHEIGHT, scaleHeight);
	}

	public String getFormat () {
		return getString(FORMAT);
	}

	public void setFormat (String format) {
		setString(FORMAT, format);
	}

	public boolean getMakeGray () {
		return getBoolean(MAKEGRAY);
	}

	public void setMakeGray (boolean makeGray) {
		setBoolean(MAKEGRAY, makeGray);
	}

	@Override
	public void reset () {
		super.reset();
		setScaleHeight(50);
		setScaleWidth(50);
		setFormat(""); // Same as original
		setMakeGray(false);
	}
	
	@Override
	public ParametersDescription getParametersDescription () {
		ParametersDescription desc = new ParametersDescription(this);
		desc.add(SCALEHEIGHT, "Percentage of the original height", "Height percentage (must be greater than 0.");		
		desc.add(SCALEWIDTH, "Percentage of the original width", "Width percentage (must be greater than 0.");
		desc.add(MAKEGRAY, "Convert to gray scale", null);
		desc.add(FORMAT, "Output format", "Format of the output files.");
		return desc;
	}
	
	@Override
	public EditorDescription createEditorDescription(ParametersDescription paramsDesc) {
		EditorDescription desc = new EditorDescription("Image Modification", false, false);	
		
		SpinInputPart sip = desc.addSpinInputPart(paramsDesc.get(SCALEWIDTH));
		sip.setRange(1, 1000);
		sip = desc.addSpinInputPart(paramsDesc.get(SCALEHEIGHT));
		sip.setRange(1, 1000);
	
		CheckboxPart cbp = desc.addCheckboxPart(paramsDesc.get(MAKEGRAY));
		cbp.setVertical(true);
		
		// List of the output formats
		List<String> available = new ArrayList<String>(Arrays.asList(ImageIO.getWriterFileSuffixes()));
		// Pre-defined set of output
		List<String> suffixes = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		names.add("<Same format as the original>");
		suffixes.add("");
		if ( available.contains("png") ) {
			names.add("PNG (Portable Network Graphics)");
			suffixes.add("png");
		}
		if ( available.contains("jpg") ) {
			names.add("JPEG (Joint Photographic Experts Group)");
			suffixes.add("jpg");
		}
		if ( available.contains("bmp") ) {
			names.add("BMP Bitmap");
			suffixes.add("bmp");
		}
		if ( available.contains("gif") ) {
			names.add("GIF (Graphics Interchange Format)");
			suffixes.add("gif");
		}
		
		ListSelectionPart lsp = desc.addListSelectionPart(paramsDesc.get(FORMAT), suffixes.toArray(new String[0]));
		lsp.setChoicesLabels(names.toArray(new String[0]));
		lsp.setVertical(true);
		lsp.setLabelFlushed(false);
		return desc;
	}
	
}
