/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.ui.verification;

import java.util.List;

import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.lib.verification.Issue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

class IssuesTableModel {
	
	Table table;
	List<Issue> list;
	Color[] colors;

	public IssuesTableModel (Display display) {
		// This array must be in the same size/order as the Issue.SEVERITY_??? flags
		colors = new Color[] {
			display.getSystemColor(SWT.COLOR_YELLOW),
			new Color(null, 255, 153, 0), // Make sure we dispose of it on close
			display.getSystemColor(SWT.COLOR_RED)
		};
	}
	
	@Override
	protected void finalize () {
		dispose();
	}
	
	public void dispose () {
		// Dispose of the non-system resources
		if ( colors != null ) {
			colors[1].dispose();
			colors[1] = null;
		}
	}

	void linkTable (Table newTable,
		Listener sortListener)
	{
		table = newTable;
		
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.addListener(SWT.Selection, sortListener);
		
		col = new TableColumn(table, SWT.NONE);
		col.addListener(SWT.Selection, sortListener);

		col = new TableColumn(table, SWT.NONE);
		col.setText("Text Unit");
		col.addListener(SWT.Selection, sortListener);

		col = new TableColumn(table, SWT.NONE);
		col.setText("Seg");
		col.addListener(SWT.Selection, sortListener);
		
		col = new TableColumn(table, SWT.NONE);
		col.setText("Description");
		col.addListener(SWT.Selection, sortListener);
	}
	
	void setIssues (List<Issue> list) {
		this.list = list;
	}

	// displayType: 0=all, 1=enabled 2=disabled
	void updateTable (int selection,
		int displayType,
		int issueType)
	{
		table.removeAll();
		if ( list == null ) return;
		for ( Issue issue : list ) {
			// Select the type of items to show
			switch ( displayType ) {
			case QualityCheckEditor.ISSUETYPE_ENABLED: // Enabled
				if ( !issue.getEnabled() ) continue;
				break;
			case QualityCheckEditor.ISSUETYPE_DISABLED: // Disabled
				if ( issue.getEnabled() ) continue;
				break;
			}
			// Select the issue type
			if ( issueType > 0 ) {
				IssueType itype = issue.getIssueType();
				if ( itype == null ) {
					if ( issueType != 13 ) continue;
				}
				else switch ( itype ) {
				case MISSING_TARGETTU:
					if ( issueType != 1 ) continue;
					break;
				case MISSING_TARGETSEG:
				case EXTRA_TARGETSEG:
					if ( issueType != 2 ) continue;
					break;
				case EMPTY_TARGETSEG:
				case EMPTY_SOURCESEG:
					if ( issueType != 3 ) continue;
					break;
				case TARGET_SAME_AS_SOURCE:
					if ( issueType != 4 ) continue;
					break;
				case MISSING_LEADINGWS:
				case MISSINGORDIFF_LEADINGWS:
				case MISSING_TRAILINGWS:
				case MISSINGORDIFF_TRAILINGWS:
				case EXTRA_LEADINGWS:
				case EXTRAORDIFF_LEADINGWS:
				case EXTRA_TRAILINGWS:
				case EXTRAORDIFF_TRAILINGWS:
					if ( issueType != 5 ) continue;
					break;
				case MISSING_CODE:
				case EXTRA_CODE:
				case SUSPECT_CODE:
					if ( issueType != 6 ) continue;
					break;
				case UNEXPECTED_PATTERN:
					if ( issueType != 7 ) continue;
					break;
				case SUSPECT_PATTERN:
					if ( issueType != 8 ) continue;
					break;
				case SOURCE_LENGTH:
				case TARGET_LENGTH:
					if ( issueType != 9 ) continue;
					break;
				case ALLOWED_CHARACTERS:
					if ( issueType != 10 ) continue;
					break;
				case TERMINOLOGY:
					if ( issueType != 11 ) continue;
					break;
				case LANGUAGETOOL_ERROR:
					if ( issueType != 12 ) continue;
					break;
				default:
					continue;
				}
			}
			// Display the item
			TableItem item = new TableItem(table, SWT.NONE);
			item.setChecked(issue.getEnabled());
			item.setForeground(1, colors[issue.getDisplaySeverity()]);
			item.setText(1, "\u2588");
			if ( issue.getTuName() == null ) {
				item.setText(2, issue.getTuId());
			}
			else {
				item.setText(2, issue.getTuId() + " (" + issue.getTuName() + ")");
			}
			item.setText(3, (issue.getSegId() == null ? "" : issue.getSegId()));
			item.setText(4, issue.getMessage());
			item.setData(issue);
		}
		
		if (( selection < 0 ) || ( selection > table.getItemCount()-1 )) {
			selection = table.getItemCount()-1;
		}
		if ( table.getItemCount() > 0 ) {
			table.setSelection(selection);
		}
	}

}
