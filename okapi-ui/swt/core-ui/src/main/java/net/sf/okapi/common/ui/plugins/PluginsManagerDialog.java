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

package net.sf.okapi.common.ui.plugins;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.ui.ClosePanel;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Dialog to manage plugins.
 */
public class PluginsManagerDialog {
	
	private final static int BUFFERSIZE = 5120;
	
	private Shell shell;
	private IHelp help;
	private Text edURL;
	private Button btRefreshURL;
	private Text edDir;
	private Button btInstall;
	private Button btRemove;
	private TableModel modAvailable;
	private Table tblAvailable;
	private Text edDescription;
	private Button btPluginHelp;
	private TableModel modCurrent;
	private Table tblCurrent;
	private URL repository;
	private File dropinsDir;
	private Label stStatus;
	private boolean actionHasBeenCalled = false;
	private ArrayList<String> lockedPlugins;

	/**
	 * Creates a new PluginsManagerDialog object.
	 * @param parent the parent of this dialog.
	 * @param helpParam the help engine.
	 * @param dropinsDir the directory where the plugins are located.
	 * @param repository the URL of the repository where the available plugins are located.
	 * Use null to use the default remote repository.
	 */
	public PluginsManagerDialog (Shell parent,
		IHelp helpParam,
		File dropinsDir,
		URL repository)
	{
		this.repository = repository;
		this.dropinsDir = dropinsDir;
		help = helpParam;
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
		shell.setText("Plugins Manager");
		UIUtil.inheritIcon(shell, parent);
		shell.setLayout(new GridLayout());
		final int longButtonsWidth = 200;

		//--- Current plugins group
		
		Group grpCurrent = new Group(shell, SWT.NONE);
		grpCurrent.setText("Plugins Currently Installed");
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		grpCurrent.setLayoutData(gdTmp);
		grpCurrent.setLayout(new GridLayout(2, false));

		tblCurrent = createTable(false, grpCurrent, 2);
		modCurrent = new TableModel(tblCurrent, false);

		btRemove = UIUtil.createGridButton(grpCurrent, SWT.PUSH, "Remove Checked Plugins...", longButtonsWidth, 1);
		btRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				remove();
			}
		});

		edDir = new Text(grpCurrent, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		edDir.setLayoutData(gdTmp);
		edDir.setEditable(false);
		
		//--- Remote plugins group
		
		Group grpAvailable = new Group(shell, SWT.NONE);
		grpAvailable.setText("Plugins Available for Installation");
		gdTmp = new GridData(GridData.FILL_BOTH);
		grpAvailable.setLayoutData(gdTmp);
		grpAvailable.setLayout(new GridLayout(2, false));

		edURL = new Text(grpAvailable, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		edURL.setLayoutData(gdTmp);
		
		btRefreshURL = UIUtil.createGridButton(grpAvailable, SWT.PUSH, "Refresh", UIUtil.BUTTON_DEFAULT_WIDTH, 1);
		btRefreshURL.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				refresh();
			}
		});
		shell.setDefaultButton(btRefreshURL);

		tblAvailable = createTable(true, grpAvailable, 2);
		modAvailable = new TableModel(tblAvailable, true);
		tblAvailable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				updateAvailable();
            }
		});

		Composite cmpTmp = new Composite(grpAvailable, SWT.NONE);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		cmpTmp.setLayoutData(gdTmp);
		GridLayout layTmp = new GridLayout(2, false);
		layTmp.marginWidth = 0;
		layTmp.marginHeight = 0;
		cmpTmp.setLayout(layTmp);
		
		btInstall = new Button(cmpTmp, SWT.PUSH);
		btInstall.setText("Install Checked Plugins...");
		gdTmp = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		gdTmp.widthHint = longButtonsWidth;
		btInstall.setLayoutData(gdTmp);
		btInstall.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				install();
			}
		});
		
		edDescription = new Text(cmpTmp, SWT.BORDER | SWT.WRAP);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.verticalSpan = 2;
		edDescription.setLayoutData(gdTmp);
		edDescription.setEditable(false);
		
		btPluginHelp = new Button(cmpTmp, SWT.PUSH);
		btPluginHelp.setText("Plugin Help");
		gdTmp = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING);
		gdTmp.widthHint = longButtonsWidth;
		btPluginHelp.setLayoutData(gdTmp);
		btPluginHelp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				callPluginHelp();
			}
		});
		
		// Status
		stStatus = new Label(shell, SWT.NONE);
		stStatus.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		
		//--- Bottom buttons
		SelectionAdapter CloseActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					if ( help != null ) help.showWiki("Plugins Manager");
					return;
				}
				if ( e.widget.getData().equals("c") ) {//$NON-NLS-1$
					shell.close();
				}
			};
		};
		ClosePanel pnlActions = new ClosePanel(shell, SWT.NONE, CloseActions, true);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlActions.setLayoutData(gdTmp);
		
		shell.pack();
		Rectangle rect = shell.getBounds();
		shell.setMinimumSize(rect.width, rect.height);
		if ( rect.width < 750 ) rect.width = 750;
		if ( rect.height < 480 ) rect.height = 480;
		shell.setSize(rect.width, rect.height);
		Dialogs.centerWindow(shell, parent);

		setData();
		btRefreshURL.setFocus();
	}

	/**
	 * Opens the dialog box. All the removal/installation is done while the dialog box is opened.
	 * The caller is responsible for updating any objects dependent on the plugins directory that
	 * was passed as argument.
	 * @return true if any remove or install operation has been called (even if they failed).
	 */
	public boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return actionHasBeenCalled;
	}
	
	private void callPluginHelp () {
		String tmp = (String)btPluginHelp.getData();
		if ( tmp == null ) return;
		try {
			Util.openURL(new URL(tmp).toString());
		}
		catch ( MalformedURLException e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void updateAvailable () {
		int n = tblAvailable.getSelectionIndex();
		String helpString = null;
		if ( n == -1 ) {
			edDescription.setText("");
		}
		else {
			PluginInfo pi = (PluginInfo)tblAvailable.getItem(n).getData();
			edDescription.setText(pi.getDescription()==null ? "" : pi.getDescription());
			helpString = pi.getHelpURL();
		}
		btPluginHelp.setData(helpString);
		btPluginHelp.setEnabled(helpString!=null);
	}
	
	private Table createTable (boolean maxMode,
		Composite parent,
		int horizontalSpan)
	{
		final Table table = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.CHECK);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = horizontalSpan;
		gdTmp.minimumHeight = 40;
		gdTmp.minimumWidth = 350;
		table.setLayoutData(gdTmp);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		if ( maxMode ) {
			table.addControlListener(new ControlAdapter() {
			    public void controlResized(ControlEvent e) {
			    	Rectangle rect = table.getClientArea();
					int part = (int)(rect.width / 100);
					int remain = (int)(rect.width % 100);
					table.getColumn(0).setWidth(remain+(part*67));
					table.getColumn(1).setWidth(part*33);
			    }
			});
		}
		else {
			table.addControlListener(new ControlAdapter() {
			    public void controlResized(ControlEvent e) {
			    	Rectangle rect = table.getClientArea();
					int part = (int)(rect.width / 100);
					int remain = (int)(rect.width % 100);
					table.getColumn(0).setWidth(remain+(part*90));
					table.getColumn(1).setWidth(part*10);
			    }
			});
		}
		
		return table;
	}
	
	private void setStatus (String text) {
		if ( text == null ) stStatus.setText("");
		else stStatus.setText(text);
		
		// Would expect calling:
		// stStatus.redraw(); stStatus.update()
		// to update the status text, but it seem we have to
		// call layout() on the parent, and update() on the display
		// layout() causes controls to resize: annoying
		shell.layout();
		Display.getCurrent().update();
	}
	
	private void setData () {
		String tmp = "http://okapi.opentag.com/plugins"; // Default
		if ( repository != null ) {
			tmp = repository.toString();
		}
		edURL.setText(tmp);
		edDir.setText(dropinsDir.getPath());

		// Locked plugins are the ones installed when opening the dialog box
		List<PluginInfo> list = getCurrentPluginsFromDirectory();
		lockedPlugins = new ArrayList<String>();
		for ( PluginInfo pi : list ) {
			lockedPlugins.add(pi.getName());
		}
		// Display the current plugins
		refreshCurrentPlugins();
		updateAvailable();
	}

	private void refresh () {
		try {
			setStatus("Refreshing lists...");
			// Get the list of all current plugins
			refreshCurrentPlugins();

			// Get the URL of the repository
			String tmp = edURL.getText().trim();
			if ( tmp.endsWith("/") ) tmp = tmp.substring(0, tmp.length()-1);
			repository = new URL(tmp);
			edURL.setText(tmp.toString()); // Proper form
			
			// Get the list of available plugins
			int index = tblAvailable.getSelectionIndex();
			URL url = new URL(repository + "/pluginsDeployment.xml");
			modAvailable.updateTable(loadInfoList(url), null, index);
			updateAvailable();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		finally {
			setStatus(null);
		}
	}
	
	private List<PluginInfo> getCurrentPluginsFromDirectory () {
		ArrayList<PluginInfo> list = new ArrayList<PluginInfo>();
		try {
			File[] files = dropinsDir.listFiles();
			if ( files == null ) {
				throw new OkapiException(String.format("Invalid location for the installed plugins (%s)", dropinsDir));
			}
			for ( File file : files ) {
				if ( file.isDirectory() ) {
					list.add(new PluginInfo(file.getName(), null, null, null));
				}
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		return list;
	}
	
	private void refreshCurrentPlugins () {
		try {
			int index = tblCurrent.getSelectionIndex();
			List<PluginInfo> list = getCurrentPluginsFromDirectory();
			modCurrent.updateTable(list, lockedPlugins, index);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}

	private List<String> getCheckedItems (Table table) {
		// Gather the checked items
		ArrayList<String> list = new ArrayList<String>();
		for ( int i=0; i<table.getItemCount(); i++ ) {
			if ( table.getItem(i).getChecked() ) {
				list.add(table.getItem(i).getText(0));
			}
		}
		return list;
	}

	private void install () {
		boolean finalRefresh = false;
		try {
			// Get the list of plugins to install
			List<String> list = getCheckedItems(tblAvailable);
			if ( list.isEmpty() ) return;
			
			// Ask confirmation
			MessageBox dlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			dlg.setMessage("This command will install the plugins you have checked in the list.\n"
				+"Do you want to proceed?");
			dlg.setText("Installing Plugins");
			if ( dlg.open() != SWT.YES ) return;

			actionHasBeenCalled = true; // Something has been changed
			// Install each plugin in the list
			finalRefresh = true;
			for ( String name : list ) {
				installPlugin(name);
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		finally {
			setStatus(null);
			if ( finalRefresh ) refresh();
		}
	}

	private void remove () {
		boolean finalRefresh = false;
		try {
			// Get the list of plugins to remove
			List<String> list = getCheckedItems(tblCurrent);
			if ( list.isEmpty() ) return;

			// Ask confirmation
			MessageBox dlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			dlg.setMessage("This command will remove the unlocked plugins you have checked in the list.\n"
				+"Do you want to proceed?");
			dlg.setText("Removing Plugins");
			if ( dlg.open() != SWT.YES ) return;

			actionHasBeenCalled = true; // Something has been changed
			// Remove each plugin in the list
			finalRefresh = true;
			for ( String name : list ) {
				removePlugin(name);
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		finally {
			setStatus(null);
			if ( finalRefresh ) refresh();
		}
		
	}
	
	private void removePlugin (String pluginName) {
		try {
			setStatus(String.format("Removing %s...", pluginName));
			// Compute the directory of the plugin
			String subDir = dropinsDir + "/" + pluginName;
			// Delete it (content and the directory itself)
			Util.deleteDirectory(subDir, false);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}

	private void installPlugin (String pluginName) {
		ZipInputStream zis = null;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		try {
			setStatus(String.format("Installing %s...", pluginName));
			// Compute the plugin sub-directory
			String subDir = dropinsDir + "/" + pluginName + "/";
			
			// Get the zip file corresponding to the plugin
			URL url = new URL(repository+"/"+pluginName+".zip");
			URLConnection urlConnection = url.openConnection();
			zis = new ZipInputStream(new BufferedInputStream(urlConnection.getInputStream()));
			ZipEntry entry = null;

			// Unzip each component of the zip file into the sub-directory
			int count;
			byte data[] = new byte[BUFFERSIZE];
			while (( entry = zis.getNextEntry() ) != null ) {
				String outPath = subDir + entry.getName();
				// Make sure to create the proper directories as needed
				Util.createDirectories(outPath);
				if ( outPath.endsWith("\\") || outPath.endsWith("/") ) {
					// Move on to next for directories
					continue;
				}
				// Unzip
				fos = new FileOutputStream(outPath);
				bos = new BufferedOutputStream(fos, BUFFERSIZE);
	            while (( count = zis.read(data, 0, BUFFERSIZE) ) != -1 ) {
	            	bos.write(data, 0, count);
	            }
	            bos.flush();
	            bos.close();
	            fos.close();
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
			removePlugin(pluginName);
		}
		finally {
			if ( zis != null ) {
				try {
					zis.close();
				}
				catch ( IOException e ) {
					// Discard error
				}
			}
		}
	}
	
	private List<PluginInfo> loadInfoList (URL url) {
		ArrayList<PluginInfo> list = new ArrayList<PluginInfo>();
		try {
			DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
			Fact.setValidating(false);
			
			URLConnection urlConnection = url.openConnection();
			Document doc = Fact.newDocumentBuilder().parse(urlConnection.getInputStream());
			
			Element rootElem = doc.getDocumentElement();
			if ( !rootElem.getNodeName().equals("pluginsDeployment") ) { //$NON-NLS-1$
				throw new OkapiException("Invalid description file.");
			}
			
			NodeList nodes = doc.getElementsByTagName("plugin");
			Element elem;
			for ( int i=0; i<nodes.getLength(); i++ ) {
				elem = (Element)nodes.item(i);
				String name = elem.getAttribute("name");
				if ( Util.isEmpty(name) ) {
					throw new OkapiException("Invalid description file: missing name attribute.");
				}
				String description = null;
				NodeList list2 = elem.getElementsByTagName("description");
				if ( list2.getLength() > 0 ) {
					description = Util.getTextContent(list2.item(0));
				}
				String helpURL = elem.getAttribute("helpURL");
				if ( Util.isEmpty(helpURL) ) helpURL = null;
				list.add(new PluginInfo(name, elem.getAttribute("provider"), description, helpURL));
			}
			
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		return list;
	}
	
}
