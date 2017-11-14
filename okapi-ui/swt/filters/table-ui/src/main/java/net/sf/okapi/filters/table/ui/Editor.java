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

package net.sf.okapi.filters.table.ui;

import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.abstracteditor.SWTUtil;
import net.sf.okapi.filters.plaintext.ui.OptionsTab;
import net.sf.okapi.filters.plaintext.ui.common.FilterParametersEditor;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Widget;

/**
 * 
 * 
 * @version 0.1, 19.06.2009
 */
@EditorFor(net.sf.okapi.filters.table.Parameters.class)
public class Editor extends FilterParametersEditor {

	@Override
	protected void createPages(TabFolder pageContainer) {

		addPage("Table", TableTab.class);
		addPage("Columns", ColumnsTab.class);
		addPage("Options", OptionsTab.class);

		addSpeaker(TableTab.class, "btnCSV");
		addSpeaker(TableTab.class, "btnTSV");
		addSpeaker(TableTab.class, "btnFWC");
		addSpeaker(TableTab.class, "header");
		addSpeaker(TableTab.class, "body");
		addSpeaker(TableTab.class, "trim");
		addSpeaker(OptionsTab.class, "allow");
	}

	@Override
	public IParameters createParameters() {

		return new net.sf.okapi.filters.table.Parameters();
	}

	@Override
	protected String getCaption() {

		return "Table Filter Parameters";
	}

	@Override
	protected void interop(Widget speaker) {
		// Interpage interop

		// Find participants

		Control btnCSV = findControl(TableTab.class, "btnCSV");
		// Control btnTSV = findControl(TableTab.class, "btnTSV");
		Control btnFWC = findControl(TableTab.class, "btnFWC");

		Control header = findControl(TableTab.class, "header");
		Control body = findControl(TableTab.class, "body");

		Control start = findControl(AddModifyColumnDefPage.class, "start");
		Control lstart = findControl(AddModifyColumnDefPage.class, "lstart");

		Control end = findControl(AddModifyColumnDefPage.class, "end");
		Control lend = findControl(AddModifyColumnDefPage.class, "lend");

		Composite columns = findPage(ColumnsTab.class);
		Control all = findControl(ColumnsTab.class, "all");
		Control defs = findControl(ColumnsTab.class, "defs");

		Control trim = findControl(TableTab.class, "trim");
		Control allow = findControl(OptionsTab.class, "allow");
		Control lead = findControl(OptionsTab.class, "lead");
		Control trail = findControl(OptionsTab.class, "trail");

		// Interaction

		// StartPos & EndPos in column definitions are enabled only when
		// fixed-width columns
		SWTUtil.enableIfSelected(start, btnFWC);
		SWTUtil.enableIfSelected(lstart, btnFWC);
		SWTUtil.enableIfSelected(end, btnFWC);
		SWTUtil.enableIfSelected(lend, btnFWC);

		SWTUtil.disableIfNotSelected(start, btnFWC);
		SWTUtil.disableIfNotSelected(lstart, btnFWC);
		SWTUtil.disableIfNotSelected(end, btnFWC);
		SWTUtil.disableIfNotSelected(lend, btnFWC);

		// CSV actions/Trim to Options/Allow trimming

		if (speaker == allow && SWTUtil.getSelected(trim) && SWTUtil.getSelected(btnCSV))
			Dialogs.showWarning(getShell(),
					"You cannot unselect this check-box while the \"Table/CSV actions/Exclude leading/trailing white spaces from extracted text\" box is on.", null);

		if (speaker == body && (SWTUtil.getDisabled(header) || SWTUtil.getNotSelected(header)))
			Dialogs.showWarning(getShell(),
					"You cannot unselect this check-box, otherwise there's noting to extract from the table.", null);

		if (speaker == body && SWTUtil.getNotSelected(body) && SWTUtil.getSelected(header))
			Dialogs.showWarning(getShell(), "The Columns tab will be disabled as you're extracting the header only.",
					null);

		if (SWTUtil.getSelected(btnCSV)) {

			SWTUtil.selectIfSelected(allow, trim);
			SWTUtil.selectIfSelected(lead, trim);
			SWTUtil.selectIfSelected(trail, trim);

			SWTUtil.unselectIfNotSelected(allow, trim);
			SWTUtil.unselectIfNotSelected(lead, trim);
			SWTUtil.unselectIfNotSelected(trail, trim);

			SWTUtil.enableIfSelected(allow, trim);
			SWTUtil.enableIfSelected(lead, trim);
			SWTUtil.enableIfSelected(trail, trim);

			SWTUtil.disableIfNotSelected(allow, trim);
			SWTUtil.disableIfNotSelected(lead, trim);
			SWTUtil.disableIfNotSelected(trail, trim);

			SWTUtil.disableIfNotSelected(allow, trim);
			SWTUtil.disableIfNotSelected(lead, trim);
			SWTUtil.disableIfNotSelected(trail, trim);

			SWTUtil.enableIfSelected(allow, trim);
		} else {

			SWTUtil.setEnabled(allow, true);
		}

		// Extract table data enable state affects the Columns page
		SWTUtil.enableIfSelected(columns, body);
		// SWTUtil.selectIfSelected(all, body);

		if (SWTUtil.getSelected(body) && !SWTUtil.getSelected(defs))
			SWTUtil.setSelected(all, true);

		SWTUtil.disableIfNotSelected(columns, body);

		if (SWTUtil.getEnabled(columns))
			pageInterop(ColumnsTab.class, speaker); // to update the enabled 
		// state of numColuimns and panel
	}

	@Override
	protected String getWikiPage () {
		return "Table Filter";
	}
}
