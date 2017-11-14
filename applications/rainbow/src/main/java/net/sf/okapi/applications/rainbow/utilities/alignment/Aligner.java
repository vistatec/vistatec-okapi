/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.utilities.alignment;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.InvalidContentException;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.ui.ClosePanel;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.UIUtil;
import net.sf.okapi.connectors.google.GoogleMTv2Connector;
import net.sf.okapi.connectors.google.GoogleMTv2Parameters;
import net.sf.okapi.lib.ui.segmentation.SRXEditor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Aligner {
	
	protected static final String ALIGNSTATUS_KEY = "Att::AlignStatus";
	protected static final String ALIGNSTATUS_TOREVIEW = "TO-REVIEW";
	
	private Shell shell;
	private int result = 0;
	private ClosePanel pnlActions;
	private List srcList;
	private List trgList;
	private Text edDocument;
	private Text edName;
	private Text edCause;
	private Button btMoveUp; 
	private Button btMoveDown; 
	private Button btMerge; 
	private Button btSplit;
	private Button btAccept; 
	private Button btSkip;
	private Button btEditRules;
	private Button btEditSeg;
	private Button btToReview;
	private Button btAutoCorrect;
	private List lbIssues;
	private Text edSource;
	private Text edTarget;
	private Text edSrcSeg;
	private Text edTrgSeg;
	private Font textFont;
	private Text edCounter;
	private Button chkShowInlineCodes;
	private Button chkSyncScrolling;
	private Button chkCheckSingleSegUnit;
	private Button chkUseAutoCorrection;
	private TextContainer source;
	private TextContainer target;
	private boolean splitMode = false;
	private boolean editMode = false;
	private int indexActiveSegment;
	private GenericContent genericCont;
	private String targetSrxPath;
	private Color colorGreen;
	private Color colorAmber;
	private Color colorRed;
	private boolean canAcceptUnit;
	private int issueType;
	private boolean warnOnClosing;
	private Pattern anchors;
	private ArrayList<String> anchorList = new ArrayList<String>();
	private boolean manualCorrection;
	private LocaleId srcLang;
	private LocaleId trgLang;
	private IHelp help;
	private IQuery mtQuery;

	@Override
	protected void finalize () {
		dispose();
	}
	
	public void dispose () {
		if ( textFont != null ) {
			textFont.dispose();
			textFont = null;
		}
		if ( shell != null ) {
			shell.close();
			shell = null;
		}
		if ( colorGreen != null ) {
			colorGreen.dispose();
			colorGreen = null;
		}
		if ( colorAmber != null ) {
			colorAmber.dispose();
			colorAmber = null;
		}
		if ( colorRed != null ) {
			colorRed.dispose();
			colorRed = null;
		}
		if ( mtQuery != null ) {
			mtQuery.close();
			mtQuery = null;
		}
	}
	
	public void closeWithoutWarning () {
		warnOnClosing = false;
		close();
	}

	public Aligner (Shell parent,
		IHelp helpParam)
	{
		help = helpParam;
		warnOnClosing = true;
		anchors = Pattern.compile("((\\d+[\\.,])*\\d+)");
		
		colorGreen = new Color(null, 0, 128, 0);
		colorAmber = new Color(null, 255, 153, 0);
		colorRed = new Color(null, 220, 20, 60);
		
		genericCont = new GenericContent();
		
		shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | 
			SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL);
		shell.setText("Alignment Verification");
		UIUtil.inheritIcon(shell, parent);
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		shell.setLayout(layout);
		
		// On close: Hide instead of closing
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				if ( !confirmCancel() ) return;
				result = 0;
				hide();
			}
		});

		SashForm sashTop = new SashForm(shell, SWT.VERTICAL);
		sashTop.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashTop.setSashWidth(3);
		sashTop.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
		
		//--- Top part
		
		Composite cmpTop = new Composite(sashTop, SWT.NONE);
		layout = new GridLayout(4, true);
		layout.marginWidth = 0;
		cmpTop.setLayout(layout);
		cmpTop.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		edDocument = new Text(cmpTop, SWT.BORDER);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		edDocument.setLayoutData(gdTmp);
		edDocument.setEditable(false);
		
		edCounter = new Text(cmpTop, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);;
		edCounter.setLayoutData(gdTmp);
		edCounter.setEditable(false);
		
		edName = new Text(cmpTop, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 3;
		edName.setLayoutData(gdTmp);
		edName.setEditable(false);
		
		Font font = edName.getFont(); // Get default font
		FontData[] fontData = font.getFontData();
		fontData[0].setHeight(11);
		textFont = new Font(font.getDevice(), fontData[0]);

		srcList = new List(cmpTop, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		srcList.setLayoutData(gdTmp);
		srcList.setFont(textFont);
		srcList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( chkSyncScrolling.getSelection() ) synchronizeFromSource();
				else updateSourceSegmentDisplay();
			}
		});
		
		trgList = new List(cmpTop, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 2;
		trgList.setLayoutData(gdTmp);
		trgList.setFont(textFont);
		trgList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( chkSyncScrolling.getSelection() ) synchronizeFromTarget();
				else updateTargetSegmentDisplay();
			}
		});

		createListContextMenus();
		
		//=== Bottom sash
		
		SashForm sashBottom = new SashForm(sashTop, SWT.VERTICAL);
		sashBottom.setLayoutData(new GridData(GridData.FILL_BOTH));
		sashBottom.setSashWidth(3);
		sashBottom.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));

		sashTop.setWeights(new int[]{30,70});
		
		//--- Middle part
		
		Composite cmpMiddle = new Composite(sashBottom, SWT.NONE);
		layout = new GridLayout(4, false);
		layout.marginWidth = 0;
		cmpMiddle.setLayout(layout);
		cmpMiddle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Options
		
		Composite cmpOptions = new Composite(cmpMiddle, SWT.NONE);
		layout = new GridLayout();
		layout.marginWidth = 0;
		cmpOptions.setLayout(layout);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 1;
		cmpOptions.setLayoutData(gdTmp);
		
		chkSyncScrolling = new Button(cmpOptions, SWT.CHECK);
		chkSyncScrolling.setText("Synchronize scrolling");
		chkSyncScrolling.setSelection(true);
		chkSyncScrolling.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( chkSyncScrolling.getSelection() ) synchronizeFromTarget();
			}
		});
		
		chkShowInlineCodes = new Button(cmpOptions, SWT.CHECK);
		chkShowInlineCodes.setText("Display in-line codes with generic markers");
		chkShowInlineCodes.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				fillSourceList(srcList.getSelectionIndex());
				updateSourceDisplay();
				fillTargetList(trgList.getSelectionIndex());
				updateTargetDisplay();
			}
		});
		
		chkUseAutoCorrection = new Button(cmpOptions, SWT.CHECK);
		chkUseAutoCorrection.setText("Try an auto-correction automatically");

		chkCheckSingleSegUnit = new Button(cmpOptions, SWT.CHECK);
		chkCheckSingleSegUnit.setText("Verify in-line codes for text-unit with a single segment");

		// Main buttons
		
		int buttonWidth = 100;
		
		Composite cmpButtons = new Composite(cmpMiddle, SWT.NONE);
		layout = new GridLayout(5, false);
		layout.marginWidth = 0;
		cmpButtons.setLayout(layout);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		gdTmp.horizontalSpan = 3;
		cmpButtons.setLayoutData(gdTmp);

		btAutoCorrect = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Try Auto-Fix", buttonWidth, -1);
		btAutoCorrect.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				autoCorrect();
			}
		});
		
		btEditRules = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Edit Rules...", buttonWidth, -1);
		btEditRules.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editRules();
			}
		});
		
		btMoveUp = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Move Up", buttonWidth, -1);
		btMoveUp.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveSegment(-1);
			}
		});
	
		btMerge = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Join Next", buttonWidth, -1);
		btMerge.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				mergeWithNext();
			}
		});
		
		btAccept = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Accept", buttonWidth, -1);
		btAccept.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( splitMode ) endSplitMode(true);
				else if ( editMode ) endEditMode(true);
				else { // Accept this text unit
					result = 1;
					if ( !saveData() ) return;
					hide();
				}
			}
		});

		btToReview = UIUtil.createGridButton(cmpButtons, SWT.CHECK, "To review later", buttonWidth, -1);
		btToReview.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				toggleToReview();
			}
		});
		
		btEditSeg = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Edit Segment...", buttonWidth, -1);
		btEditSeg.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				startEditMode();
			}
		});
		
		btMoveDown = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Move Down", buttonWidth, -1);
		btMoveDown.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				moveSegment(+1);
			}
		});
		
		btSplit = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Split...", buttonWidth, -1);
		btSplit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				startSplitMode();
			}
		});

		btSkip = UIUtil.createGridButton(cmpButtons, SWT.PUSH, "Skip", buttonWidth, -1);
		btSkip.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( splitMode ) {
					endSplitMode(false);
					return;
				}
				else if ( editMode ) {
					endEditMode(false);
					return;
				}
				// Else: Skip this text unit
				result = 2;
				hide();
			}
		});
		
		
		// Error/warning list
		
		edCause = new Text(cmpMiddle, SWT.BORDER);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		edCause.setLayoutData(gdTmp);
		edCause.setEditable(false);
		edCause.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		lbIssues = new List(cmpMiddle, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 4;
		gdTmp.heightHint = 32;
		lbIssues.setLayoutData(gdTmp);
		lbIssues.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				gotoIssue();
			}
		});
		
		
		//--- Bottom part
		
		Composite cmpBottom = new Composite(sashBottom, SWT.NONE);
		sashBottom.setWeights(new int[]{14,86});
		
		
		layout = new GridLayout();
		layout.marginWidth = 0;
		cmpBottom.setLayout(layout);
		cmpBottom.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Edit boxes
		
		edSrcSeg = new Text(cmpBottom, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		edSrcSeg.setEditable(false);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 16;
		edSrcSeg.setLayoutData(gdTmp);
		edSrcSeg.setFont(textFont);
		
		edTrgSeg = new Text(cmpBottom, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		edTrgSeg.setEditable(false);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 16;
		edTrgSeg.setLayoutData(gdTmp);
		edTrgSeg.setFont(textFont);
		edTrgSeg.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent e) {
				if ( e.keyCode == SWT.CR ) {
					e.doit = true;
				}
			}
		});

		Label label = new Label(cmpBottom, SWT.NONE);
		label.setText("Full text unit:");
		gdTmp = new GridData();
		label.setLayoutData(gdTmp);
		
		edSource = new Text(cmpBottom, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		edSource.setEditable(false);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 32;
		edSource.setLayoutData(gdTmp);
		edSource.setFont(textFont);
		
		edTarget = new Text(cmpBottom, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
		edTarget.setEditable(false);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.heightHint = 32;
		edTarget.setLayoutData(gdTmp);
		edTarget.setFont(textFont);
		
		//--- Dialog-level buttons

		SelectionAdapter CloseActions = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if ( e.widget.getData().equals("h") ) {
					if ( help != null ) help.showWiki("Rainbow - Alignment Verification");
					return;
				}
				if ( e.widget.getData().equals("c") ) {
					if ( !confirmCancel() ) return;
					result = 0;
					hide();
				}
			};
		};
		pnlActions = new ClosePanel(shell, SWT.NONE, CloseActions, true);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		pnlActions.setLayoutData(gdTmp);
		pnlActions.btClose.setText("Cancel");
		shell.setDefaultButton(btAccept);
		
		// Size and position of dialog
		shell.pack();
		shell.setMinimumSize(shell.getSize());
		Point startSize = shell.getMinimumSize();
		if ( startSize.x < 700 ) startSize.x = 700; 
		if ( startSize.y < 700 ) startSize.y = 700; 
		shell.setSize(startSize);
		Dialogs.centerWindow(shell, parent);
	}

	private void createListContextMenus () {
		// Context menu for the input list
		Menu contextMenu = new Menu(shell, SWT.POP_UP);
		
		MenuItem menuItem = new MenuItem(contextMenu, SWT.PUSH);
		menuItem.setText("Get Machine Translation of &Source");
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				getGist(false);
            }
		});
		
		menuItem = new MenuItem(contextMenu, SWT.PUSH);
		menuItem.setText("Get Machine Translation of &Target");
		menuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				getGist(true);
            }
		});
		
		srcList.setMenu(contextMenu);
		trgList.setMenu(contextMenu);
	}
	
	public boolean wasModifiedManually () {
		return manualCorrection;
	}
	
	private boolean confirmCancel () {
		try {
			if ( !warnOnClosing ) return true;
			// Ask confirmation
			MessageBox dlg = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			dlg.setMessage("Do you really want to interrupt this alignment?");
			dlg.setText(shell.getText());
			return (dlg.open() == SWT.YES);
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
		return false;
	}
	
	/**
	 * Sets information data for the visual verification.
	 * @param targetSrxPath The full path of the SRX document to use for the target.
	 * @param checkSingleSegUnit True if unit with a single segment should be checked, false to accept
	 * them as aligned without checking.
	 */
	public void setInfo (String targetSrxPath,
		boolean checkSingleSegUnit,
		boolean useAutoCorrection,
		LocaleId sourceLanguage,
		LocaleId targetLanguage,
		String mtKey)
	{
		this.srcLang = sourceLanguage;
		this.trgLang = targetLanguage;
		this.targetSrxPath = targetSrxPath;
		chkCheckSingleSegUnit.setSelection(checkSingleSegUnit);
		chkUseAutoCorrection.setSelection(useAutoCorrection);
		
		// Query engine
		if ( mtQuery == null ) {
			if ( !Util.isEmpty(mtKey) ) {
				mtQuery = new GoogleMTv2Connector();
				GoogleMTv2Parameters prm = new GoogleMTv2Parameters();
				prm.setApiKey(mtKey);
				mtQuery.open();
			}
		}
	}
	
	private void gotoIssue () {
		try {
			int n = lbIssues.getSelectionIndex();
			if ( n == -1 ) return;
			int p = lbIssues.getItem(n).indexOf(':');
			if ( p != -1 ) {
				p = Integer.valueOf(lbIssues.getItem(n).substring(0, p));
				trgList.setSelection(p-1);
				synchronizeFromTarget();
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void synchronizeFromSource () {
		updateSourceSegmentDisplay();
		int n = srcList.getSelectionIndex();
		if ( n >= trgList.getItemCount() ) {
			edTrgSeg.setText("");
			return; // Cannot synchronize
		}
		trgList.setSelection(n);
		updateTargetSegmentDisplay();
	}

	private void synchronizeFromTarget () {
		updateTargetSegmentDisplay();
		int n = trgList.getSelectionIndex();
		if ( n >= srcList.getItemCount() ) {
			edSrcSeg.setText("");
			return; // Cannot synchronize
		}
		srcList.setSelection(n);
		updateSourceSegmentDisplay();
	}

	private void updateSourceDisplay () {
		edSource.setText(genericCont.printSegmentedContent(source, true,
			!chkShowInlineCodes.getSelection()));
	}
	
	private void updateTargetDisplay () {
		edTarget.setText(genericCont.printSegmentedContent(target, true,
			!chkShowInlineCodes.getSelection()));
	}
	
	private void updateSourceSegmentDisplay () {
		int n = srcList.getSelectionIndex();
		if ( n < 0 ) edSrcSeg.setText("");
		else edSrcSeg.setText(srcList.getItem(n));
	}
	
	private void updateTargetSegmentDisplay () {
		int n = trgList.getSelectionIndex();
		if ( n < 0 ) edTrgSeg.setText("");
		else edTrgSeg.setText(trgList.getItem(n));

		n = trgList.getSelectionIndex();
		int count = trgList.getItemCount();
		btMoveUp.setEnabled(n>0);
		btMoveDown.setEnabled(( n < count-1 ) && ( n > -1 ));
		btMerge.setEnabled(( n < count-1 ) && ( n > -1 ));
		btSplit.setEnabled(( count > 0 ) && ( n > -1 ));
	}

	private void toggleToReview () {
		if ( target.hasProperty(ALIGNSTATUS_KEY) ) {
			target.removeProperty(ALIGNSTATUS_KEY);
		}
		else {
			target.setProperty(new Property(ALIGNSTATUS_KEY, ALIGNSTATUS_TOREVIEW));
		}
	}
	
	/**
	 * Moves the current target segment.
	 * @param direction Use 1 to move up, -1 to move down.
	 */
	private void moveSegment (int direction) {
		try {
			int n = trgList.getSelectionIndex();
			// Sanity checks
			if (( direction != 1 ) && ( direction != -1 )) return;
			if ( direction == 1 ) {
				if ( n+1 > trgList.getItemCount() ) return;
			}
			else {
				if ( n < 1 ) return;
			}
			// Swap current segment with the previous/next one
			target.getSegments().swap(n+direction, n);
			// Update
			updateTargetDisplay();
			fillTargetList(n+direction);
			trgList.setFocus();
			// Re-check for issues, but don't select one
			hasIssue(true, true, false);
			synchronizeFromTarget();
			manualCorrection = true;
		}
		catch ( Throwable e) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}

	private void mergeWithNext () {
		try {
			int n = trgList.getSelectionIndex();
			if ( n < 0  ) return;
			// n is segment index not part, don't use getPartIndex()
			target.getSegments().joinWithNext(n);
			updateTargetDisplay();
			fillTargetList(n);
			trgList.setFocus();
			// Re-check for issues
			hasIssue(true, true, true);
			manualCorrection = true;
		}
		catch ( Throwable e) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void splitSegment (int segIndex,
		int start,
		int end)
	{
		// No split if location is not a range and is not inside text
		if (( start == end ) && ( start == 0 )) return;
		// Convert segment index to part index
		int partIndex = target.getSegments().getPartIndex(segIndex);
		// Do the split
		target.split(partIndex, start, end, false);
		// Indicates we made a manual change
		manualCorrection = true;
	}
	
	public void close () {
		shell.close();
	}
	
	/**
	 * Calls the form to align a text unit visually.
	 * @return 1=the segments are deemed aligned, 2=skip this entry,
	 * 0=stop the process.
	 */
	private int showDialog (ITextUnit tu) {
		edName.setText("");
		if ( tu != null ) {
			if ( tu.getName().length() > 0 ) {
				edName.setText(tu.getName());
			}
		}
		
		shell.setVisible(true);
		trgList.setFocus();
		Display Disp = shell.getDisplay();
		while ( shell.isVisible() ) {
			if ( !Disp.readAndDispatch() )
				Disp.sleep();
		}
		return result;
	}
	
	public void setDocumentName (String name) {
		edDocument.setText(name);
	}

	/**
	 * Verifies the alignment of the segments of a given TextUnit object.
	 * @param tu The text unit containing the segments to verify.
	 * @param currentSource The current source unit being verified.
	 * @param totalTarget The total number of target units available.
	 * @return 1=the segments are deemed aligned, 2=skip this entry,
	 * 0=stop the process.
	 */
	public int align (ITextUnit tu,
		int currentSource,
		int totalTarget)
	{
		manualCorrection = false;
		// Make sure we do have a target to align
		if ( !tu.hasTarget(trgLang) ) return 2;
		// Set the new values
		source = tu.getSource();
		target = tu.getTarget(trgLang);
		btToReview.setSelection(false);
		// If none is segmented, no need to check
		if ( source.contentIsOneSegment() && target.contentIsOneSegment() ) return 1;
		// Check for issues
		if ( hasIssue(false, true, true) ) {
			setData();
			if ( issueType == 1 ) {
				lbIssues.setSelection(0);
				gotoIssue();
			}
			// Try to auto-correct
			if ( chkUseAutoCorrection.getSelection() ) autoCorrect();
			// Correct manually
			edCounter.setText(String.format("Source: #%d / Targets: %d", currentSource, totalTarget));
			return showDialog(tu);
		}
		// Else: assumes correct alignment
		return 1;
	}

	private void hide () {
		shell.setVisible(false);
		if ( shell.getMinimized() ) shell.setMinimized(false);
	}

	private void fillTargetList (int selection) {
		trgList.removeAll();
		boolean useGeneric = chkShowInlineCodes.getSelection();
		for ( Segment seg : target.getSegments() ) {
			if ( useGeneric ) trgList.add(genericCont.setContent(seg.text).toString());
			else trgList.add(seg.text.toText());
		}
		if (( trgList.getItemCount() > 0 ) && ( selection < trgList.getItemCount() )) 
			trgList.setSelection(selection);
		
		if ( !edTrgSeg.getEditable() ) { // Not while the field can be edited
			updateTargetSegmentDisplay();
		}
	}
	
	private void fillSourceList (int selection) {
		srcList.removeAll();
		boolean useGeneric = chkShowInlineCodes.getSelection();
		for ( Segment seg : source.getSegments() ) {
			if ( useGeneric ) srcList.add(genericCont.setContent(seg.text).toString());
			else srcList.add(seg.toString());
		}
		if (( srcList.getItemCount() > 0 ) && ( selection < srcList.getItemCount() )) 
			srcList.setSelection(selection);
		updateSourceSegmentDisplay();
	}
	
	private void setData () {
		updateSourceDisplay();
		fillSourceList(0);
		updateTargetDisplay();
		fillTargetList(0);
	}
	
	private boolean saveData () {
		if ( splitMode ) return false;
		
		// the class works based on index, so we need to make sure
		// that we have id matching in source and target
		// On accept both container should have the same number of segments
		int i = 0;
		for ( Segment seg : source.getSegments() ) {
			seg.id = String.valueOf(i);
			i++;
		}
		i = 0;
		for ( Segment seg : target.getSegments() ) {
			seg.id = String.valueOf(i);
			i++;
		}
		
		return true;
	}
	
	private void startSplitMode () {
		indexActiveSegment = trgList.getSelectionIndex();
		if ( indexActiveSegment == -1 ) return;
		splitMode = true;
		toggleFields(true);
	}
	
	/**
	 * Converts a SWT Point to a Range.
	 * @param point The object to convert.
	 * @return The range created from the point.
	 */
	private Range conv (Point point) {
		return new Range(point.x, point.y);
	}
	
	private void endSplitMode (boolean accept) {
		try {
			// Compute the new segmentation
			if ( accept ) {
				// genericCont is already set with the proper text
				Range sel = genericCont.getCodedTextPosition(conv(edTrgSeg.getSelection()));
				splitSegment(indexActiveSegment, sel.start, sel.end);
			}
			// Re-check for issues
			hasIssue(true, true, true);
			
			// Reset the controls
			splitMode = false;
			toggleFields(false);
			
			// Update the display
			if ( accept ) {
				updateTargetDisplay();
				fillTargetList(indexActiveSegment);
			}
			else updateTargetSegmentDisplay();
			trgList.setFocus();
		}
		catch ( Throwable e) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
	private void editRules () {
		try {
			SRXEditor editor = new SRXEditor(shell, true, help);
			editor.showDialog(targetSrxPath);
		}
		catch ( Throwable e) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}

	private void startEditMode () {
		indexActiveSegment = trgList.getSelectionIndex();
		if ( indexActiveSegment == -1 ) return;
		editMode = true;
		toggleFields(true);
	}
	
	private void endEditMode (boolean accept) {
		try {
			// Update the content
			if ( accept ) {
				try {
					// genericCont is already set with the proper text
					GenericContent.updateFragment(edTrgSeg.getText(),
						target.getSegments().get(indexActiveSegment).text, true);
				}
				catch ( InvalidContentException e ) {
					Dialogs.showError(shell, e.getMessage(), null);
					return;
					//TODO: recover by resetting the original, or prevent end of
					//edit mode
				}
				manualCorrection = true;
			}
			// Re-check for issues
			hasIssue(true, true, true);
			
			// Reset the controls
			editMode = false;
			toggleFields(false);
			
			// Update the display
			if ( accept ) {
				updateTargetDisplay();
				fillTargetList(indexActiveSegment);
			}
			else updateTargetSegmentDisplay();
			trgList.setFocus();
		}
		catch ( Throwable e) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}

	private void toggleFields (boolean specialMode) {
		if ( specialMode ) {
			genericCont.setContent(target.getSegments().get(indexActiveSegment).text);
			edTrgSeg.setText(genericCont.toString());
			edTrgSeg.setFocus();
			btAccept.setEnabled(true);
		}
		else {
			btAccept.setEnabled(canAcceptUnit);
		}
		edTrgSeg.setEditable(specialMode);
		srcList.setEnabled(!specialMode);
		trgList.setEnabled(!specialMode);
		btEditRules.setVisible(!specialMode);
		btEditSeg.setVisible(!specialMode);
		btMoveUp.setVisible(!specialMode);
		btMoveDown.setVisible(!specialMode);
		btMerge.setVisible(!specialMode);
		btSplit.setVisible(!specialMode);
		btAutoCorrect.setVisible(!specialMode);
		btToReview.setVisible(!specialMode);
		chkSyncScrolling.setVisible(!specialMode);
		lbIssues.setEnabled(!specialMode);
		
		if ( specialMode ) {
			if ( splitMode ) btAccept.setText("Accept Split");
			else btAccept.setText("Accept Changes");
			btSkip.setText("Discard");
		}
		else {
			btAccept.setText("Accept");
			btSkip.setText("Skip");
		}
	}

	private void resetIssues () {
		issueType = 0;
		lbIssues.removeAll();
	}
	
	/**
	 * Tries to find some issue with the current alignment.
	 * @param forceIssueDisplay True if we need to set the issue display.
	 * Such display is not needed when calling the function when the dialog is hidden
	 * and no issues are found.
	 * @param resetList True to reset the list of issues.
	 * @param gotoIssue True to select the first issue and the corresponding segment.
	 * @return True if an issue has been found. False if no issue has been found.
	 */
	private boolean hasIssue (boolean forceIssueDisplay,
		boolean resetList,
		boolean gotoIssue)
	{
		try {
			if ( resetList ) resetIssues();
			
			int srcSegCount = source.getSegments().count();
			int trgSegCount = target.getSegments().count();
			// Check the number of segments
			if ( srcSegCount != trgSegCount ) {
				// Optional visual alignment to fix the problems
				addIssue(2, "Error- Different number of segments in source and target.");
				return updateIssueStatus(gotoIssue);
			}
			// Assumes the list have same number of segments now
			
			// Check if we do further verification for single-segment unit
			if (( srcSegCount == 1 ) && !chkCheckSingleSegUnit.getSelection() ) {
				// We assume it's ok to align
				if ( forceIssueDisplay ) addIssue(0, null);
				return updateIssueStatus(gotoIssue);
			}
			
			// Sanity check using common anchors
			int i = 0;
			ISegments trgSegs = target.getSegments();
			for ( Segment srcSeg : source.getSegments() ) {
				// Normally we would use srcSeg.id, but the class works based on segment index not id
				Segment trgSeg = trgSegs.get(i);
				if ( trgSeg == null ) {
					addIssue(1, String.format("%d: Warning- No target segment for the source segment.", i+1));
				}
				if ( srcSeg.text.getCodes().size() != trgSeg.text.getCodes().size() ) {
					addIssue(1, String.format("%d: Warning- Different number of inline codes in source and target.", i+1));
				}
				checkAnchors(srcSeg.text, trgSeg.text, i);
				i++;
			}
			if ( forceIssueDisplay ) addIssue(0, null);
			return updateIssueStatus(gotoIssue);
		}
		catch ( Throwable e) {
			Dialogs.showError(shell, e.getMessage(), null);
			return false;
		}
	}

	private void checkAnchors (TextFragment source,
		TextFragment target,
		int index)
	{
		//--- Check the inline code data
		anchorList.clear();
		for ( Code code : source.getCodes() ) {
			anchorList.add(code.getData());
		}
		for ( Code code : target.getCodes() ) {
			if ( !anchorList.contains(code.getData()) ) {
				// An inline code found in the target is not in the source
				addIssue(1, String.format("%d: Warning- Target inline code '%s' is not in the source.", index+1, code.getData())); 
			}
			else { // Change matched entries so they don't match again
				anchorList.set(anchorList.indexOf(code.getData()), "");
			}
		}
		// List of the source inline codes not found in the target
		boolean extra = false;
		StringBuilder tmp = new StringBuilder();
		for ( String str : anchorList ) {
			if ( str.length() == 0 ) continue;
			if ( tmp.length() > 0 ) tmp.append(", ");
			tmp.append("\'" + str + "\'");
			extra = true;
		}
		if ( extra ) {
			addIssue(1, String.format("%d: Warning- Source inline codes not found in the target: %s", index+1,
				tmp.toString()));
		}
		
		//--- Check the patterns
		Matcher m = anchors.matcher(source.getCodedText());
		anchorList.clear();
		// Get the list anchors for the source
		while ( m.find() ) {
			anchorList.add(m.group());
		}
		// Go through the anchors for the target
		m = anchors.matcher(target.getCodedText());
		while ( m.find() ) {
			if ( !anchorList.contains(m.group()) ) {
				// An anchor found in the target is not in the source
				addIssue(1, String.format("%d: Warning- Extra pattern '%s' in target.", index+1, m.group())); 
			}
			else anchorList.remove(m.group());
		}
		// List of the anchors found in the source but not in the target
		if ( anchorList.size() > 0 ) {
			tmp = new StringBuilder();
			for ( String str : anchorList ) {
				if ( tmp.length() > 0 ) tmp.append(", ");
				tmp.append("\'" + str + "\'");
			}
			addIssue(1, String.format("%d: Warning- One or more missing patterns in target: %s", index+1,
				tmp.toString()));
		}
	}

	/**
	 * Tries to automatically adjust mis-aligned segments.
	 * @return True if some auto-fix was applied, false if the segments
	 * have not been modified.
	 */
	private boolean autoCorrect () {
		boolean modified = false;
		int n = trgList.getSelectionIndex();
		if ( n == -1 ) n = 0;
		try {
			ISegments sourceSegments = source.getSegments();
			ISegments targetSegments = target.getSegments();
			
			int lastMatch = -1;
			int trgStart = 0;
			int srcNoMatchCount = 0;
			boolean matchFound;
			String srcText;
			int toJoin;

			for ( Segment srcSeg : sourceSegments ) {
	    		
				matchFound = false;
				srcText = srcSeg.toString();
				for ( int j=trgStart; j<targetSegments.count(); j++ ) {
					String s2 = targetSegments.get(j).toString(); //TODO: replace by direct call after debug
					if ( srcText.equals(s2) ) {
						// We have a match
						if ( srcNoMatchCount == 1 ) {
							if ( lastMatch == -1 ) {
								toJoin = (j-1);
								// lastMatch=-1 is ok with following calculations
							}
							else {
								// We have only one source segment between this match and last
								// Compute the number of target segments between matches
								toJoin = ((j-1) - lastMatch)-1;
							}
							if ( toJoin > 0 ) {
								// We have more than one, so we can join them
								// The target segment just after the last match is the base
								for ( int k=0; k<toJoin; k++ ) {
									targetSegments.joinWithNext(lastMatch+1);
								}
								if ( !modified ) {
									resetIssues();
									modified = true;
								}
								addIssue(1, String.format("%d: Warning- Segment auto-corrected by joining two or more.",
									lastMatch+1+1)); // Show 1 for 0
								// Correct the target position since we joined one or more segments
								j -= toJoin;
							}
							// Then we reset the position for the next try
							lastMatch = j;
							trgStart = j+1;
							srcNoMatchCount = 0;
							matchFound = true;
							break;
						}
						else {
							// Can't auto-fix more than single source between two match.
							// So we move on to the next case.
							lastMatch = j;
							trgStart = j+1;
							srcNoMatchCount = 0;
							matchFound = true;
							break;
						}
					}
				}
				if ( !matchFound ) srcNoMatchCount++;
			}
			
			// Case of one source with no match left
			if ( srcNoMatchCount == 1 ) {
				// If there was no match at all: we group all targets into one
				if ( lastMatch == -1 ) {
					if ( targetSegments.count() > 1 ) {
						// Several target for one source: merge them
						targetSegments.joinAll();
						if ( !modified ) {
							resetIssues();
							modified = true;
						}
						addIssue(1, "Warning- All target segments have been merged into one by auto-correction.");
					}
				}
				else { // There was at least one match, we group everything after it
					toJoin = ((targetSegments.count()-1) - lastMatch)-1;
					if ( toJoin > 0 ) {
						// We have more than one, so we can join them
						// The target segment just after the last match is the base
						for ( int k=0; k<toJoin; k++ ) {
							targetSegments.joinWithNext(lastMatch+1);
						}
						if ( !modified ) {
							resetIssues();
							modified = true;
						}
						addIssue(1, String.format("%d: Warning- Segment auto-corrected by joining two or more.",
							lastMatch+1+1)); // Show 1 for 0
					}
				}
				
			}
			// Case of one source with many target but no matches
			
			updateTargetDisplay();
			if ( modified ) {
				fillTargetList(0);
				if ( chkSyncScrolling.getSelection() ) synchronizeFromTarget();
				trgList.setFocus();
				// Re-check for issues
				hasIssue(true, false, true);
			}
		}
		catch ( Throwable e ) {
			addIssue(2, "Error- Auto-correction error occured.");
			Dialogs.showError(shell, e.getMessage(), null);
		}
		return modified;
	}
	
	private void addIssue (int type,
		String causeText)
	{
		switch ( type ) {
		case 1:
			lbIssues.add(causeText);
			if ( issueType < 1 ) issueType = 1;
			break;
		case 2:
			lbIssues.add(causeText);
			issueType = 2;
			break;
		}
	}
	
	private boolean updateIssueStatus (boolean gotoIssue) {
		switch ( issueType ) {
		case 0:
			edCause.setText("No issue automatically detected.");
			edCause.setBackground(colorGreen);
			break;
		case 1:
			edCause.setText("One or more WARNINGS detected.");
			edCause.setBackground(colorAmber);
			if ( lbIssues.getItemCount() > 0 ) {
				lbIssues.setSelection(0);
				if ( gotoIssue ) gotoIssue();
			}
			break;
		case 2:
			edCause.setText("One or more ERRORS detected.");
			edCause.setBackground(colorRed);
			break;
		}
		canAcceptUnit = (issueType<2);
		btAccept.setEnabled(canAcceptUnit);
		return (issueType>0);
	}

	private void getGist (boolean targetToSource) {
		try {
			String text;
			if ( mtQuery != null ) {
				int n;
				TextFragment oriFrag;
				if ( targetToSource ) {
					if ( (n = trgList.getSelectionIndex()) == -1 ) return;
					mtQuery.setLanguages(trgLang, srcLang);
					oriFrag = target.getSegments().get(n).text;
				}
				else {
					if ( (n = srcList.getSelectionIndex()) == -1 ) return;
					mtQuery.setLanguages(srcLang, trgLang);
					oriFrag = source.getSegments().get(n).text;
				}
				
				mtQuery.query(oriFrag);
				if ( mtQuery.hasNext() ) {
					text = "Original segment:\n" + oriFrag.toText()
						+ "\n\nPossible translation:\n" + mtQuery.next().target.toText();
				}
				else {
					text = "No translation found for the select segment.";
				}
			}
			else {
				text = "No MT key has been specified. This function is disabled.";
			}
			MessageBox dlg = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
			dlg.setMessage(text);
			dlg.setText("Translation Query");
			dlg.open();
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, e.getMessage(), null);
		}
	}
	
}
