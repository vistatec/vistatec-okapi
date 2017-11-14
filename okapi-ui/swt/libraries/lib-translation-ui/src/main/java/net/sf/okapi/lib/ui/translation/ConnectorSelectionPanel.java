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

package net.sf.okapi.lib.ui.translation;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.common.ui.genericeditor.GenericEditor;
import net.sf.okapi.common.uidescription.IEditorDescriptionProvider;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Implements a composite panel to select a translation resource and set its parameters.
 */
public class ConnectorSelectionPanel extends Composite {

	private Label stConnectors;
	private Combo cbConnectors;
	private Button btOptions;
	private Text edSettings;
	private List<ConnectorInfo> infoList;
	private ArrayList<String> paramsList;
	private ArrayList<String> displayList;
	private GenericEditor gedit;
	private IContext context;
	private IQuery currentConnector;
	private IParameters currentParams;

	public ConnectorSelectionPanel (Composite p_Parent,
		int flags,
		IConnectorList connectors,
		IContext context,
		String caption)
	{
		super(p_Parent, SWT.NONE);
		this.context = context;
		infoList = connectors.getList();

		paramsList = new ArrayList<String>();
		displayList = new ArrayList<String>();
		for ( int i=0; i<infoList.size(); i++ ) {
			paramsList.add(null);
			displayList.add(null);
		}
		
		createContent(caption);
	}
	
	private void createContent (String caption) {
		GridLayout layTmp = new GridLayout(2, false);
		layTmp.marginHeight = 0;
		layTmp.marginWidth = 0;
		setLayout(layTmp);

		GridData gdTmp;
		if ( caption != null ) {
			stConnectors = new Label(this, SWT.NONE);
			stConnectors.setText(caption);
			gdTmp = new GridData();
			gdTmp.horizontalSpan = 2;
			stConnectors.setLayoutData(gdTmp);
		}

		cbConnectors = new Combo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		cbConnectors.setVisibleItemCount(10);
		cbConnectors.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateConnector();
			}
		});
		cbConnectors.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for ( ConnectorInfo ci : infoList ) {
			cbConnectors.add(ci.description);
		}
		
		btOptions = new Button(this, SWT.PUSH);
		btOptions.setText("&Settings...");
		btOptions.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editParameters();
			}
		});
		UIUtil.ensureWidth(btOptions, UIUtil.BUTTON_DEFAULT_WIDTH);
		
		edSettings = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		edSettings.setEditable(false);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		gdTmp.heightHint = 32;
		edSettings.setLayoutData(gdTmp);
	}

	private void instantiateConnector (int index) {
		try {
			if (infoList.get(index).classLoader == null) {
				currentConnector = (IQuery)Class.forName(infoList.get(index).connectorClass).newInstance();
			}
			else {
				currentConnector = (IQuery)Class.forName(infoList.get(index).connectorClass, true, 
						infoList.get(index).classLoader).newInstance();
			}
			
			currentParams = currentConnector.getParameters();
			if ( !Util.isEmpty(paramsList.get(index)) ) {
				// Set the current parameters if they exist
				currentParams.fromString(paramsList.get(index));
			} // Otherwise use the defaults
		}
		catch ( InstantiationException e ) {
			throw new OkapiException("Error creating the connector. " + e.getMessage(), e);
		}
		catch ( IllegalAccessException e ) {
			throw new OkapiException("Error creating the connector. " + e.getMessage(), e);
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiException("Error creating the connector. " + e.getMessage(), e);
		}
	}
	
	private void updateConnector () {
		int n = cbConnectors.getSelectionIndex();
		if ( n == -1 ) {
			btOptions.setEnabled(false);
			edSettings.setText("");
			return;
		}
		btOptions.setEnabled(infoList.get(n).descriptionProviderClass != null);
		
		if ( displayList.get(n) == null ) {
			instantiateConnector(n);
			String tmp = currentConnector.getSettingsDisplay();
			displayList.set(n, (tmp==null) ? "" : tmp);
		}
		edSettings.setText(displayList.get(n));
	}
	
	private void editParameters () {
		try {
			int n = cbConnectors.getSelectionIndex();
			if ( n == - 1 ) return;
			ConnectorInfo ci = infoList.get(n);
			
			if ( ci.descriptionProviderClass != null ) {
				IEditorDescriptionProvider descProv = null;
				if (ci.classLoader == null) {
					descProv = (IEditorDescriptionProvider)Class.forName(ci.descriptionProviderClass).newInstance();
				}
				else {
					descProv = (IEditorDescriptionProvider)Class.forName(ci.descriptionProviderClass, true, ci.classLoader).newInstance();
				}
				
				instantiateConnector(n);
				
				if ( gedit == null ) {
					gedit = new GenericEditor();
				}
				if ( !gedit.edit(currentParams, descProv, false, context) ) {
					return; // Cancel
				}
				// Else: Save the data
				paramsList.set(n, currentParams.toString());
				displayList.set(n, currentConnector.getSettingsDisplay());
				updateConnector(); // To update the display
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(getShell(), e.getMessage(), null);
		}
	}
	
	public void setData (String connectorClass,
		String paramsData)
	{
		int n = -1;
		boolean found = false;
		if ( !Util.isEmpty(connectorClass) ) {
			for ( ConnectorInfo ci : infoList ) {
				n++;
				if ( ci.connectorClass.equals(connectorClass) ) {
					found = true;
					paramsList.set(n, paramsData);
					break;
				}
			}
		}
		if ( !found ) n = 0;
		cbConnectors.select(n);
		updateConnector();
	}
	
	public String getConnectorClass () {
		int n = cbConnectors.getSelectionIndex();
		if ( n == - 1 ) return null;
		return infoList.get(n).connectorClass;
	}
	
	public String getConnectorParameters () {
		int n = cbConnectors.getSelectionIndex();
		if ( n == - 1 ) return null;
		return paramsList.get(n);
	}

	@Override
	public void setEnabled (boolean enabled) {
		if ( stConnectors != null ) stConnectors.setEnabled(enabled);
		cbConnectors.setEnabled(enabled);
		if ( enabled ) updateConnector();
		else btOptions.setEnabled(enabled);
		edSettings.setEnabled(enabled);
	}

}
