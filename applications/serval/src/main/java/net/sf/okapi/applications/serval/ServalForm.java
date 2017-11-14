package net.sf.okapi.applications.serval;

import java.io.File;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.InputDialog;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.filters.tmx.TmxFilter;
import net.sf.okapi.lib.translation.QueryManager;
import net.sf.okapi.tm.pensieve.tmx.OkapiTmxImporter;
import net.sf.okapi.tm.pensieve.writer.ITmWriter;
import net.sf.okapi.tm.pensieve.writer.TmWriterFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class ServalForm {

	private Shell shell;
	private Text edQuery;
	private Text edSource;
	private Text edTarget;
	private Text edThreshold;
	private Text edMaxHits;
	private Table tblResults;
	private TableModel modResults;
	private QueryManager queryMgt;
	private Font displayFont;
	private Button chkRawText;
	private Button chkLeverage;
	private Text edAttributes;
	private Label stElapsedTime;
	
	public ServalForm (Shell shell) {
		try {
			this.shell = shell;
			
			queryMgt = new QueryManager();
			queryMgt.setLanguages(getDefaultSourceLanguage(), this.getDefaultTargetLanguage());

			// Default
			//queryMgt.addAndInitializeResource(new GoogleMTConnector(), "GoogleMT", null);
			// For test
			//SimpleTMConnector smptm = new SimpleTMConnector();
			//queryMgt.addAndInitializeResource(smptm, "SimpleTM test", "C:\\Projects\\CaridianBCT\\AlignerTests\\RealProject\\PL-PL\\outputdb_PL");
			
			createContent();
		}
		catch ( Throwable E ) {
			Dialogs.showError(shell, E.getMessage(), null);			
		}
	}
	
	private void createContent () {
		GridLayout layTmp = new GridLayout(9, false);
		shell.setLayout(layTmp);
		
		// Menus
	    Menu menuBar = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menuBar);

		// File menu
		MenuItem topItem = new MenuItem(menuBar, SWT.CASCADE);
		topItem.setText("&File");
		Menu dropMenu = new Menu(shell, SWT.DROP_DOWN);
		topItem.setMenu(dropMenu);
		
		MenuItem menuItem = new MenuItem(dropMenu, SWT.PUSH);
		menuItem.setText("&Resources Manager...");
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				editOptions();
            }
		});

		menuItem = new MenuItem(dropMenu, SWT.PUSH);
		menuItem.setText("&Import TMX in Pensieve TM...");
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				importTMXInPensieve();
            }
		});

		new MenuItem(dropMenu, SWT.SEPARATOR);
		
		menuItem = new MenuItem(dropMenu, SWT.PUSH);
		menuItem.setText("Exit\tAlt+F4");
		menuItem.setAccelerator(SWT.F4 | SWT.ALT);
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
            }
		});
		
		Label stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Query:");
		stTmp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	
		edQuery = new Text(shell, SWT.BORDER | SWT.WRAP);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.heightHint = 36;
		gdTmp.horizontalSpan = 8;
		edQuery.setLayoutData(gdTmp);

		Font font = edQuery.getFont();
		FontData[] fontData = font.getFontData();
		fontData[0].setHeight(10);
		displayFont = new Font(font.getDevice(), fontData[0]);
		edQuery.setFont(displayFont);
		
		stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Attrbutes (key=value):");
		stTmp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		edAttributes = new Text(shell, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 8;
		edAttributes.setLayoutData(gdTmp);
		
		stTmp = new Label(shell, SWT.NONE); // Place-holder
		
		Button btSearch = UIUtil.createGridButton(shell, SWT.PUSH, "Search", 80, 1);
		btSearch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				query();
			}
		});
		shell.setDefaultButton(btSearch);
		
		chkRawText = new Button(shell, SWT.CHECK);
		chkRawText.setText("Raw text");
		
		chkLeverage = new Button(shell, SWT.CHECK);
		chkLeverage.setText("Leverage");
		
		stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Threshold:");
		edThreshold = new Text(shell, SWT.BORDER);
		edThreshold.setText("95");
		gdTmp = new GridData();
		gdTmp.widthHint = 30;
		edThreshold.setLayoutData(gdTmp);
		
		stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Maximun number of hits:");
		edMaxHits = new Text(shell, SWT.BORDER);
		edMaxHits.setText("25");
		gdTmp = new GridData();
		gdTmp.widthHint = 30;
		edMaxHits.setLayoutData(gdTmp);

		stElapsedTime = new Label(shell, SWT.NONE);
		stElapsedTime.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Target:");
		stTmp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		edTarget = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.heightHint = 36;
		gdTmp.horizontalSpan = 8;
		edTarget.setLayoutData(gdTmp);
		edTarget.setEditable(false);
		edTarget.setFont(displayFont);

		stTmp = new Label(shell, SWT.NONE);
		stTmp.setText("Source:");
		stTmp.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));

		edSource = new Text(shell, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.heightHint = 36;
		gdTmp.horizontalSpan = 8;
		edSource.setLayoutData(gdTmp);
		edSource.setEditable(false);
		edSource.setFont(displayFont);
		
		tblResults = new Table(shell, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 9;
		tblResults.setFont(displayFont);
		tblResults.setLayoutData(gdTmp);
		tblResults.setHeaderVisible(true);
		tblResults.addControlListener(new ControlAdapter() {
		    public void controlResized(ControlEvent e) {
		    	Table table = (Table)e.getSource();
		    	Rectangle rect = table.getClientArea();
				int nPart = rect.width / 100;
				int nRemain = rect.width % 100;
				table.getColumn(0).setWidth(8*nPart);
				table.getColumn(1).setWidth(12*nPart);
				table.getColumn(2).setWidth(40*nPart);
				table.getColumn(3).setWidth((40*nPart)+nRemain);
		    }
		});
		tblResults.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				updateCurrentHit(-1);
            }
		});
		modResults = new TableModel();
		modResults.linkTable(tblResults);
		
		if (!shell.getMaximized()) { // not RWT full-screen mode
			// Set the minimal size to the packed size
			// And then set the start size
			Point startSize = shell.getSize();
			shell.pack();
			shell.setMinimumSize(shell.getSize());
			shell.setSize(startSize);
			
			// Workaround for RWT (stretches the shell horizontally when column widths are adjusted)
			shell.setMaximized(true);
			shell.setMaximized(false);

			UIUtil.centerShell(shell);
		}
	}
	
	private boolean setAttributes () {
		String tmp = edAttributes.getText();
		queryMgt.clearAttributes();
		if ( tmp.length() == 0 ) {
			return true;
		}
		String[] pairs = tmp.split("[\\s;,]", 0);
		for ( String pair : pairs ) {
			if ( pair.length() == 0 ) continue;
			String parts[] = pair.split("=", 0);
			if ( parts.length != 2 ) {
				Dialogs.showError(shell, "Syntax error in the attributes.", null);
				return false;
			}
			queryMgt.setAttribute(parts[0].trim(), parts[1].trim());
		}
		return true;
	}
	
	private void query () {
		try {
			String tmp = edThreshold.getText();
			int n;
			try {
				n = Integer.valueOf(tmp);
				if ( n < 0 ) n = 0;
				if ( n > 100 ) n = 100;
			}
			catch ( NumberFormatException e ) {
				n = 95;
			}
			edThreshold.setText(String.valueOf(n));
			queryMgt.setThreshold(n);
			
			tmp = edMaxHits.getText();
			try {
				n = Integer.valueOf(tmp);
				if ( n < 0 ) n = 0;
			}
			catch ( NumberFormatException e ) {
				n = 25;
			}
			edMaxHits.setText(String.valueOf(n));
			queryMgt.setMaximumHits(n);
			
			if ( !setAttributes() ) return;

			long start = System.nanoTime();
			
			if ( chkLeverage.getSelection() ) {
				ITextUnit tu = new TextUnit("id");
				if ( chkRawText.getSelection() ) {
					tu.setSource(new TextContainer(edQuery.getText()));
				}
				else {
					tu.setSourceContent(parseToTextFragment(edQuery.getText()));
				}
				queryMgt.setOptions(1, false, false, false, null, 0, false);
				queryMgt.leverage(tu); //, 1, false, null, 0);
			}
			else {
				if ( chkRawText.getSelection() ) {
					queryMgt.query(edQuery.getText());
				}
				else {
					queryMgt.query(parseToTextFragment(edQuery.getText()));
				}
			}
			
			long end = System.nanoTime(); 
			modResults.updateTable(queryMgt);
			updateCurrentHit(end-start);
		}
		catch ( Throwable e ) {
			modResults.clearTable();
			edTarget.setText("");
			edSource.setText("");
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
		}
	}
	
	private void editOptions () {
		try {
			QueryManagerForm form = new QueryManagerForm(shell, "Resources Manager", queryMgt);
			form.showDialog();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
		}
	}
	private void updateCurrentHit (long elapsedTime) {
		int n = tblResults.getSelectionIndex();
		if ( elapsedTime > 0 ) {
			stElapsedTime.setText("Time (in milliseconds): "+elapsedTime/1000000);
		}
		else {
			stElapsedTime.setText("");
		}
		if ( n < 0 ) {
			edTarget.setText("");
			edSource.setText("");
			return;
		}
		// Else: set the new current data
		QueryResult qr = queryMgt.getResults().get(n);
		edTarget.setText(qr.target.toText());
		edSource.setText(qr.source.toText());
	}
	
	public void run () {
		try {
			Display disp = shell.getDisplay();			
			while ( !shell.isDisposed() ) {
				if (!disp.readAndDispatch())
					disp.sleep();
			}
		}
		finally {
			// Dispose of any global resources
			if ( queryMgt != null ) {
				queryMgt.close();
				queryMgt = null;
			}
			if ( displayFont != null ) {
				displayFont.dispose();
				displayFont = null;
			}
		}
	}

	private LocaleId getDefaultSourceLanguage () {
		// In most case the 'source' language is English
		// Even when we are on non-English machines
		return LocaleId.fromString("en-us");
	}
	
	private LocaleId getDefaultTargetLanguage ()
	{
		// Use the local language by default
		LocaleId locId = new LocaleId(Locale.getDefault());
		// If it's the same as the source, use an arbitrary value.
		if ( locId.sameLanguageAs(getDefaultSourceLanguage()) ) {
			return LocaleId.fromString("fr-fr");
		}
		return locId;
	}

	/**
	 * Converts the search string into a TextFragment.
	 * With minor modifications this code is based on the SRXEditor -> processInlineCodes() method.
	 * @param  text  	Textstring to convert to TextFragment
	 * @return      TextFragment created from the search string text field
	 */	
	public TextFragment parseToTextFragment (String text) {
		
		//--parses any thing within <...> into opening codes
		//--parses any thing within </...> into closing codes
		//--parses any thing within <.../> into placeholder codes
		Pattern patternOpening = Pattern.compile("\\<(\\w+)[ ]*[^\\>/]*\\>");
		Pattern patternClosing = Pattern.compile("\\</(\\w+)[ ]*[^\\>]*\\>");
		Pattern patternPlaceholder = Pattern.compile("\\<(\\w+)[ ]*[^\\>]*/\\>");
		
		TextFragment tf = new TextFragment();
		
		tf.setCodedText(text);

		int n;
		int start = 0;
		int diff = 0;
		
		Matcher m = patternOpening.matcher(text);
		
		while ( m.find(start) ) {
			n = m.start();
			diff += tf.changeToCode(n+diff, (n+diff)+m.group().length(),
				TagType.OPENING, m.group(1));
			start = (n+m.group().length());
		}
		
		text = tf.getCodedText();
		start = diff = 0;
		m = patternClosing.matcher(text);
		while ( m.find(start) ) {
			n = m.start();
			diff += tf.changeToCode(n+diff, (n+diff)+m.group().length(),
				TagType.CLOSING, m.group(1));
			start = (n+m.group().length());
		}
		
		text = tf.getCodedText();
		start = diff = 0;
		m = patternPlaceholder.matcher(text);
		while ( m.find(start) ) {
			n = m.start();
			diff += tf.changeToCode(n+diff, (n+diff)+m.group().length(),
				TagType.PLACEHOLDER, null);
			start = (n+m.group().length());
		}
		return tf;
	}

	private void importTMXInPensieve () {
		try {
			// Get the directory
			DirectoryDialog dlg = new DirectoryDialog(shell);
			String dir = dlg.open();
			if (  dir == null ) return;
			
			// Get the languages
			InputDialog dlg2 = new InputDialog(shell, "Languages",
				"Enter source and target language separated by a space", "EN-US FR-FR", null, 0, -1, -1);
			String tmp = dlg2.showDialog();
			if ( Util.isEmpty(tmp) ) return;
			String[] langs = tmp.split("[ ,]", 0);
			if ( langs.length != 2 ) {
				throw new OkapiException(String.format("Invalid languages: '%s'", tmp));
			}
			
			// Get TMX file
			String[] paths = Dialogs.browseFilenames(shell, "Select TMX Document to Import", false, null, null, null);
			if ( paths == null ) return;
			
			TmxFilter filter = new TmxFilter();
			OkapiTmxImporter imp = new OkapiTmxImporter(LocaleId.fromString(langs[0]), filter);
			
			ITmWriter writer = TmWriterFactory.createFileBasedTmWriter(dir, true);
			
			File file = new File(paths[0]);
			imp.importTmx(file.toURI(), LocaleId.fromString(langs[1]), writer);
			writer.close();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getLocalizedMessage(), null);
		}
	}

}

