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

package net.sf.okapi.lib.ui.editor;

import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;

import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Allows to display or work on editing the target text for a bilingual text.
 * This class can edit either a segment or a text container.
 */
public class PairEditorPanel extends SashForm {

	private TextContainerEditorPanel edSource;
	private TextContainerEditorPanel edTarget;

	/**
	 * Creates a new PairEditorPanel object.
	 * @param parent the parent of this panel.
	 * @param flag the style flag: SWT.VERTICAL or SWT.HORIZONTAL 
	 */
	public PairEditorPanel (Composite parent,
		int flag)
	{
		super(parent, flag);

		// Default layout
		setLayout(new GridLayout());
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		edSource = new TextContainerEditorPanel(this, -1, false);
		edTarget = new TextContainerEditorPanel(this, -1, true);
		
		edSource.setEditable(false);
		edTarget.setTargetRelations(edSource, this);
		
		edTarget.setFocus();
	}

	public void setTextFragments (TextFragment source,
		TextFragment target)
	{
		edSource.setText(source);
		edTarget.setText(target);
	}
		
	public void setTextContainers (TextContainer source,
		TextContainer target)
	{
		edSource.setText(source);
		edTarget.setText(target);
	}
		
	public void clear () {
		edSource.clear();
		edTarget.clear();
	}

	public boolean isModified () {
		return (edTarget.isModified() || edSource.isModified());
	}
	
	public boolean applyChanges () {
		return edTarget.applyChanges();
	}

	@Override
	public void setEnabled (boolean enabled) {
		super.setEnabled(enabled);
		edSource.setEnabled(enabled);
		edTarget.setEnabled(enabled);
	}
	
}
