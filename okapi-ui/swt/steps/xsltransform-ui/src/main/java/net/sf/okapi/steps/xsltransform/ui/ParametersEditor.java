/*===========================================================================
  Copyright (C) 2008-2012 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xsltransform.ui;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.okapi.common.ConfigurationString;
import net.sf.okapi.common.EditorFor;
import net.sf.okapi.common.IContext;
import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditor;
import net.sf.okapi.common.NSContextManager;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.ISWTEmbeddableParametersEditor;
import net.sf.okapi.common.ui.OKCancelPanel;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.steps.xsltransform.Parameters;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@EditorFor(Parameters.class)
public class ParametersEditor implements IParametersEditor, ISWTEmbeddableParametersEditor {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Shell shell;
	private boolean result = false;
	private OKCancelPanel pnlActions;
	private Parameters params;
	private Button chkUseCustomTransformer;
	private Text edFactoryClass;
	private Text edXpathFactoryClass;
	private Text edXsltPath;
	private Text edParameters;
	private IHelp help;
	private String projectDir;
	private Composite mainComposite;

	public boolean edit (IParameters params,
		boolean readOnly,
		IContext context)
	{
		boolean bRes = false;
		try {
			shell = null;
			help = (IHelp)context.getObject("help");
			this.projectDir = context.getString("projDir");
			this.params = (Parameters)params;
			shell = new Shell((Shell)context.getObject("shell"), SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.APPLICATION_MODAL);
			create((Shell)context.getObject("shell"), readOnly);
			return showDialog();
		}
		catch ( Exception e ) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
			bRes = false;
		}
		finally {
			// Dispose of the shell, but not of the display
			if ( shell != null ) shell.dispose();
		}
		return bRes;
	}
	
	public IParameters createParameters () {
		return new Parameters();
	}
	
	@Override
	public Composite getComposite () {
		return mainComposite;
	}

	@Override
	public void initializeEmbeddableEditor (Composite parent,
		IParameters paramsObject,
		IContext context)
	{
		params = (Parameters)paramsObject; 
		shell = (Shell)context.getObject("shell");
		createComposite(parent);
		setData();
	}

	@Override
	public String validateAndSaveParameters () {
		if ( !saveData() ) return null;
		return params.toString();
	}
	
	private void create (Shell parent,
		boolean readOnly)
	{
		shell.setText(Res.getString("editor.caption")); //$NON-NLS-1$
		if ( parent != null ) UIUtil.inheritIcon(shell, parent);
		GridLayout layTmp = new GridLayout();
		layTmp.marginBottom = 0;
		layTmp.verticalSpacing = 0;
		shell.setLayout(layTmp);

		createComposite(shell);

		//--- Dialog-level buttons

		SelectionAdapter OKCancelActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				result = false;
				if ( e.widget.getData().equals("h") ) { //$NON-NLS-1$
					if ( help != null ) help.showWiki("XSL Transformation Step");
					return;
				}
				if ( e.widget.getData().equals("o") ) { //$NON-NLS-1$
					if ( !saveData() ) return;
				}
				shell.close();
			};
		};
		pnlActions = new OKCancelPanel(shell, SWT.NONE, OKCancelActions, true);
		pnlActions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pnlActions.btOK.setEnabled(!readOnly);
		if ( !readOnly ) {
			shell.setDefaultButton(pnlActions.btOK);
		}

		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Dialogs.centerWindow(shell, parent);
		setData();
	}
	
	private void createComposite (Composite parent) {
		mainComposite = new Composite(parent, SWT.BORDER);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		mainComposite.setLayout(new GridLayout(4, false));
		
		Label label = new Label(mainComposite, SWT.NONE);
		label.setText(Res.getString("editor.stXsltPath")); //$NON-NLS-1$
		GridData gdTmp = new GridData();
		gdTmp.horizontalSpan = 4;
		label.setLayoutData(gdTmp);

		edXsltPath = new Text(mainComposite, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		edXsltPath.setLayoutData(gdTmp);
		
		Button btGetPath = new Button(mainComposite, SWT.PUSH);
		btGetPath.setText("..."); //$NON-NLS-1$
		btGetPath.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		btGetPath.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String[] paths = Dialogs.browseFilenames(shell,
					Res.getString("editor.captionSelectTemplate"), //$NON-NLS-1$
					false, null,
					Res.getString("editor.filterSelectTemplate"), //$NON-NLS-1$
					"*.xsl;*.xslt\t*.*"); //$NON-NLS-1$
				if ( paths == null ) return;
				UIUtil.checkProjectFolderAfterPick(paths[0], edXsltPath, projectDir);
			}
		});

		label = new Label(mainComposite, SWT.NONE);
		label.setText(Res.getString("editor.stParameters")); //$NON-NLS-1$
		
		int wideButtonWidth = Res.getInt("editor.wideButtonWidth"); //$NON-NLS-1$
		Button btGetDefaults = new Button(mainComposite, SWT.PUSH);
		btGetDefaults.setText(Res.getString("editor.btGetDefaults")); //$NON-NLS-1$
		gdTmp = new GridData();
		btGetDefaults.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btGetDefaults, wideButtonWidth);
		btGetDefaults.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getParametersFromTemplate();
			}
		});
		
		Button btOpenFile = new Button(mainComposite, SWT.PUSH);
		btOpenFile.setText(Res.getString("editor.btOpenTemplate")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 2;
		btOpenFile.setLayoutData(gdTmp);
		UIUtil.ensureWidth(btOpenFile, wideButtonWidth);
		btOpenFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Program.launch(edXsltPath.getText()); 
			}
		});
		
		edParameters = new Text(mainComposite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 70;
		gdTmp.horizontalSpan = 4;
		edParameters.setLayoutData(gdTmp);
		
		chkUseCustomTransformer = new Button(mainComposite, SWT.CHECK);
		chkUseCustomTransformer.setText(Res.getString("editor.useCustomTransformer")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.horizontalSpan = 4;
		chkUseCustomTransformer.setLayoutData(gdTmp);
		chkUseCustomTransformer.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				edFactoryClass.setEnabled(chkUseCustomTransformer.getSelection());
				edFactoryClass.setText("net.sf.saxon.TransformerFactoryImpl"); // default for XSLT 2.0
				edXpathFactoryClass.setEnabled(chkUseCustomTransformer.getSelection());
				edXpathFactoryClass.setText("net.sf.saxon.xpath.XPathFactoryImpl"); // default for XSLT 2.0
			}
		});
		
		edFactoryClass = new Text(mainComposite, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		edFactoryClass.setLayoutData(gdTmp);

		edXpathFactoryClass = new Text(mainComposite, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		edXpathFactoryClass.setLayoutData(gdTmp);
}
	
	private boolean showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
		return result;
	}

	private void setData () {
		chkUseCustomTransformer.setSelection(params.getUseCustomTransformer());
		edFactoryClass.setText(params.getFactoryClass());
		edXpathFactoryClass.setText(params.getXpathClass());
		edXsltPath.setText(params.getXsltPath());
		ConfigurationString tmp = new ConfigurationString(params.getParamList());
		edParameters.setText(tmp.toString());
		edFactoryClass.setEnabled(chkUseCustomTransformer.getSelection());
		edXpathFactoryClass.setEnabled(chkUseCustomTransformer.getSelection());
	}

	private boolean saveData () {
		if ( edXsltPath.getText().length() == 0 ) {
			Dialogs.showError(shell, "You must specify a path for the XSLT file.", null);
			edXsltPath.setFocus();
			return false;
		}
		if ( chkUseCustomTransformer.getSelection() ) {
			if ( edFactoryClass.getText().length() == 0 ) {
				Dialogs.showError(shell, "You must specify a factory class.", null);
				edFactoryClass.setFocus();
				return false;
			}
			// If no Xpath factory is chosen, the default one will be used
		}

		params.setUseCustomTransformer(chkUseCustomTransformer.getSelection());
		if ( params.getUseCustomTransformer() ) {
			params.setFactoryClass(edFactoryClass.getText());
			params.setXpathClass(edXpathFactoryClass.getText());
		}

		params.setXsltPath(edXsltPath.getText());
		ConfigurationString tmp = new ConfigurationString(edParameters.getText());
		params.setParamList(tmp.toString());
		result = true;
		return result;
	}
	
	private void getParametersFromTemplate () {
		try {
			String path = edXsltPath.getText();
			if ( path.length() == 0 ) return;

			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			// security concern. Turn off DTD processing
			// https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
			try {
				// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
				// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
				domFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
				 
				// Xerces 2 only - http://xerces.apache.org/xerces-j/features.html#external-general-entities
				domFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
				 
			} catch (ParserConfigurationException e) {
				// Tried an unsupported feature. This may indicate that a different XML processor is being
				// used. If so, then its features need to be researched and applied correctly.
				// For example, using the Xerces 2 feature above on a Xerces 1 processor will throw this
				// exception.
				logger.warn("Unsupported DocumentBuilderFactory feature. Possible security vulnerabilities.", e);
			}
			
		    DocumentBuilder builder = domFactory.newDocumentBuilder();
		    Document doc = builder.parse(path);

			// Macintosh work-around
			// When you use -XstartOnFirstThread as a java -Xarg on Leopard, your ContextClassloader gets set to null.
			// That is not the case on 10.4 or with Windows or Linux flavors
			// This allows XPathFactory.newInstance() to have a non-null context
			//Removed because not needed any more (1.7 not supported by 10.5)
			//Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
			// end work-around
		    XPathFactory factory = Util.createXPathFactory();

		    XPath xpath = factory.newXPath();
		    xpath.setNamespaceContext(new NSContextManager());
		    XPathExpression expr = xpath.compile("//xsl:param"); //$NON-NLS-1$

		    Object result = expr.evaluate(doc, XPathConstants.NODESET);
		    NodeList nodes = (NodeList) result;
		    ConfigurationString paramList = new ConfigurationString();
		    Element elem;
		    for (int i = 0; i < nodes.getLength(); i++) {
		    	elem = (Element)nodes.item(i);
		    	paramList.add(elem.getAttribute("name"), Util.getTextContent(elem)); //$NON-NLS-1$
		    }
		    edParameters.setText(paramList.toString());
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
}
