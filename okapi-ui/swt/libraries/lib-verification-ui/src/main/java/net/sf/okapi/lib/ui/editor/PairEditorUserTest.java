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

import java.io.File;
import java.util.ArrayList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.InputDocumentDialog;
import net.sf.okapi.virtualdb.IVDocument;
import net.sf.okapi.virtualdb.IVRepository;
import net.sf.okapi.virtualdb.IVTextUnit;
import net.sf.okapi.virtualdb.jdbc.Repository;
import net.sf.okapi.virtualdb.jdbc.h2.H2Access;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Temporary class for testing the fragment editor with end-users, using real files.
 */
public class PairEditorUserTest {

	private Shell shell;
	private IFilterConfigurationMapper fcMapper;
	private PairEditorPanel editPanel;
	private Text edInfo;
	private Button btFirst;
	private Button btLast;
	private Button btNext;
	private Button btPrevious;
	private Button btSave;
	private Button chkSegmentMode;
	private LocaleId srcLoc = LocaleId.ENGLISH;
	private LocaleId trgLoc = LocaleId.FRENCH;
	private RawDocument rawDoc;
	private int current = -1;
	private int curSeg = -1;
	private ISegments srcSegs;
	private ISegments trgSegs;
	private ITextUnit tu;
	private TextContainer srcCont;
	private TextContainer trgCont;
	private boolean segmentMode = true;
	private String inPath;
	private String inConfig;
	private String outEncoding;
	private String outPath;
	private int TUCount;
	private ArrayList<ITextUnit> textUnits = new ArrayList<ITextUnit>();
	private ArrayList<Long> keys = new ArrayList<Long>();
	private IVRepository repo;
	private IVDocument vdoc;
	private IVTextUnit vtu;
	private boolean useRepository;

	private static final String WINDOW_TITLE = "Fragment Editor Testing Console";

	public PairEditorUserTest (Object parent,
		IFilterConfigurationMapper fcMapper,
		boolean useRepository)
	{
		// If no parent is defined, create a new display and shell
		if ( parent == null ) {
			// Start the application
			Display dispMain = new Display();
			parent = new Shell(dispMain);
		}

		this.fcMapper = fcMapper;
		shell = new Shell((Shell)parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.MAX | SWT.MIN | SWT.APPLICATION_MODAL);
		shell.setText(WINDOW_TITLE);
		shell.setLayout(new GridLayout());

		this.useRepository = useRepository;
		if ( useRepository ) {
			String home = System.getProperty("user.home");
			H2Access h2db = new H2Access(home, fcMapper);
			repo = new Repository(h2db);
			h2db.create("tmpRepository");
		}
		
		createContent();

		// Extra feature when using the in-memory list
		if ( !useRepository ) {
			createInitialExtractedText();
		}

		updateButtons();
		
		Dialogs.centerWindow(shell, (Shell)parent);
	}
	
	@Override
	protected void finalize () {
		dispose();
	}

	private void dispose () {
	}

	public void showDialog () {
		shell.open();
		while ( !shell.isDisposed() ) {
			if ( !shell.getDisplay().readAndDispatch() )
				shell.getDisplay().sleep();
		}
	}
	
	private void createContent () {
		Composite comp = new Composite(shell, SWT.BORDER);
		comp.setLayout(new GridLayout(6, true));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Button btOpen = new Button(comp, SWT.PUSH);
		btOpen.setText("&Open File...");
		btOpen.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btOpen.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openDocument(inPath);
			};
		});

		btFirst = new Button(comp, SWT.PUSH);
		btFirst.setText("&First");
		btFirst.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btFirst.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				displayFirst();
			};
		});
		
		btPrevious = new Button(comp, SWT.PUSH);
		btPrevious.setText("&Previous");
		btPrevious.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btPrevious.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				displayPrevious();
			};
		});
		
		btNext = new Button(comp, SWT.PUSH);
		btNext.setText("&Next");
		btNext.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btNext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				displayNext();
			};
		});

		btLast = new Button(comp, SWT.PUSH);
		btLast.setText("&Last");
		btLast.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btLast.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				displayLast();
			};
		});
		
		btSave = new Button(comp, SWT.PUSH);
		btSave.setText("&Save Output");
		btSave.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btSave.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				saveOutput();
			};
		});

		chkSegmentMode = new Button(comp, SWT.CHECK);
		chkSegmentMode.setText("&Segment mode");
		chkSegmentMode.setSelection(segmentMode);
		chkSegmentMode.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				switchMode(!segmentMode);
			};
		});
		
		
		edInfo = new Text(comp, SWT.BORDER);
		edInfo.setEditable(false);
		GridData gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 5;
		edInfo.setLayoutData(gdTmp);
		
		editPanel = new PairEditorPanel(comp, SWT.VERTICAL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 6;
		editPanel.setLayoutData(gdTmp);
		editPanel.clear();
		
		// Drop target for the table
		DropTarget dropTarget = new DropTarget(shell, DND.DROP_DEFAULT | DND.DROP_COPY | DND.DROP_MOVE);
		dropTarget.setTransfer(new FileTransfer[]{FileTransfer.getInstance()}); 
		dropTarget.addDropListener(new DropTargetAdapter() {
			public void drop (DropTargetEvent e) {
				FileTransfer FT = FileTransfer.getInstance();
				if ( FT.isSupportedType(e.currentDataType) ) {
					String[] paths = (String[])e.data;
					if ( paths != null ) {
						for ( String path : paths ) {
							if ( !openDocument(path) ) {
								break; // Stop now
							}
						}
					}
				}
			}
		});

		// Set minimum and start sizes
		shell.pack();
		shell.setMinimumSize(shell.getSize());
		shell.setSize(680, 400);
	}

	private boolean openDocument (String path) {
		IFilter filter = null;
		try {
			InputDocumentDialog dlg = new InputDocumentDialog(shell, "Open File", fcMapper, false);
			dlg.setData(path, inConfig, "UTF-8", srcLoc, trgLoc);

			// Edit
			Object[] data = dlg.showDialog();
			if ( data == null ) return false;
			
			// Store input path and filter config to pre-populate for next time
			inPath = (String)data[0];
			inConfig = (String)data[1];
			File inFile = new File(inPath);
			shell.setText(WINDOW_TITLE + " - " + inFile.getName() + " (" + inConfig + ")");
			
			// Create the raw document to add to the session
			srcLoc = (LocaleId)data[3];
			trgLoc = (LocaleId)data[4];
			rawDoc = new RawDocument(inFile.toURI(), (String)data[2], srcLoc, trgLoc);
			rawDoc.setFilterConfigId(inConfig);
			
			// Update custom configs again in case user just edited them
			if (fcMapper instanceof FilterConfigurationMapper) {
				((FilterConfigurationMapper)fcMapper).updateCustomConfigurations();
			}
			
			filter = fcMapper.createFilter(rawDoc.getFilterConfigId());

			// Reset
			outEncoding = null;
			outPath = null;
			current = -1;
			curSeg = -1;
			srcSegs = null;
			trgSegs = null;
			
			// Load the document
			TUCount = 0;
			if ( useRepository ) {
				// Remove existing document
				if ( vdoc != null ) {
					repo.removeDocument(vdoc);
				}
				// Import new one
				repo.importDocument(rawDoc);
				vdoc = repo.getFirstDocument();
				keys = new ArrayList<Long>();
				for ( IVTextUnit vtu : vdoc.textUnits() ) {
					keys.add(vtu.getKey());
				}
				TUCount = keys.size();
			}
			else {
				textUnits = new ArrayList<ITextUnit>();
				filter.open(rawDoc);
				while ( filter.hasNext() ) {
					Event event = filter.next();
					if ( event.getEventType() == EventType.TEXT_UNIT ) {
						textUnits.add(event.getTextUnit());
					}
				}
				TUCount = textUnits.size();
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, "Error when opening document.\n"+e.getMessage(), null);
			return false;
		}
		finally {
			if ( filter != null ) {
				filter.close();
			}
			displayFirst();
		}
		return true;
	}
	
	private void updateButtons () {
		btSave.setEnabled(( rawDoc != null ) && ( TUCount > 0 ));
		
		if ( segmentMode ) {
			btPrevious.setEnabled(( current > 0 ) && (( curSeg > 0 ) || ( TUCount > 0 )));
			btNext.setEnabled(( TUCount > 0 ) && (( curSeg < srcSegs.count()-1 ) || ( current < (TUCount-1) )));
		}
		else {
			btPrevious.setEnabled(( current > 0 ) && ( TUCount > 0 ));
			btNext.setEnabled(( TUCount > 0 ) && ( current < (TUCount-1) ));
		}
		
		btFirst.setEnabled(btPrevious.getEnabled());
		btLast.setEnabled(btNext.getEnabled());
	}
	
	private void getNewCurrent (int index,
		boolean firstSegment)
	{
		try {
			if ( index < 0 ) return;
			
			// Get the text unit
			if ( useRepository ) {
				vtu = (IVTextUnit)vdoc.getItem(keys.get(index));
				tu = vtu.getTextUnit();
			}
			else {
				tu = textUnits.get(index);
			}
			
			// Get the source
			srcCont = tu.getSource();
			if ( segmentMode ) {
				srcSegs = srcCont.getSegments();
			}
			
			// Get the existing target, or create an empty one, segmented if needed
			trgCont = tu.getTarget(trgLoc);
			if ( trgCont == null ) {
				// Create a copy
				trgCont = tu.createTarget(trgLoc, false, IResource.COPY_ALL);
				trgSegs = trgCont.getSegments();
				// And empty the content of each segment
				for ( Segment seg : trgSegs ) {
					seg.text.clear();
				}
			}
			else if ( segmentMode ) {
				trgSegs = trgCont.getSegments();
			}
			
			// Set current segment
			if ( segmentMode ) {
				if ( firstSegment ) curSeg = 0;
				else curSeg = srcSegs.count()-1;
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, "Error when accessing text unit.\n"+e.getMessage(), null);
		}
	}
	
	private void displayCurrent () {
		if ( current < 0 ) {
			editPanel.clear();
			edInfo.setText("");
		}
		else {
			if ( segmentMode ) {
				Segment srcSeg = srcSegs.get(curSeg);
				Segment trgSeg = trgSegs.get(srcSeg.getId());
				if ( trgSeg == null ) {
					//TODO: Handle inter-segment before this segment
					trgSeg = new Segment(srcSeg.getId(), new TextFragment());
					trgSegs.append(trgSeg);
				}
				editPanel.setTextFragments(srcSeg.text, trgSeg.text);
				edInfo.setText(String.format("TU ID=%s / %d of %d / seg=%s",
					tu.getId(), current+1, TUCount, srcSeg.getId()));
			}
			else { // Show the text containers
				editPanel.setTextContainers(srcCont, trgCont);
				edInfo.setText(String.format("TU ID=%s / %d of %d / full content",
					tu.getId(), current+1, TUCount));
			}
			
			// Translatable?
			editPanel.setEnabled(tu.isTranslatable());
			if ( !editPanel.getEnabled() ) {
				edInfo.setText(edInfo.getText()+" NOT TRANSLATABLE!");
			}
		}
		updateButtons();
	}
	
	private void displayPrevious () {
//long start = System.currentTimeMillis();
		if ( current < 0 ) return;
		if ( !saveCurrent() ) return;

		if ( segmentMode ) {
			if ( curSeg == 0 ) {
				if ( current == 0 ) return; // No more
				else getNewCurrent(--current, false); // Get previous text unit
			}
			else curSeg--; // Move to previous segment 
		}
		else { // Non-segment mode
			if ( current == 0 ) return; // No more
			else getNewCurrent(--current, false); // Get previous text unit
		}
		displayCurrent();
	}
	
	private void displayNext () {
//long start = System.currentTimeMillis();
		if ( current < 0 ) return;
		if ( !saveCurrent() ) return;

		if ( segmentMode ) {
			if ( curSeg >= srcSegs.count()-1 ) {
				if ( current >= TUCount-1 ) return; // No more
				else getNewCurrent(++current, true); // Get next text unit 
			}
			else curSeg++; // Move to next segment
		}
		else { // Non-segment mode
			if ( current >= TUCount-1 ) return; // No more
			else getNewCurrent(++current, true); // Get next text unit
		}
		displayCurrent();
	}
	
	private void displayFirst () {
		if ( !saveCurrent() ) return;
		if ( TUCount > 0 ) {
			current = 0;
			getNewCurrent(current, true);
		}
		else {
			current = -1;
		}
		displayCurrent();
	}

	private void switchMode (boolean newSegmentMode) {
		if ( current < 0 ) {
			segmentMode = newSegmentMode;
			return;
		}
		if ( !saveCurrent() ) return;

		if ( segmentMode ) { // If it was in segment mode we just re-display
			segmentMode = newSegmentMode;
		}
		else { // If it was in non-segment mode we re-get the text unit to update the segments info
			segmentMode = newSegmentMode;
			getNewCurrent(current, true);
		}
		
		// Refresh the display with the new mode
		
		displayCurrent();
	}
	
	private void displayLast () {
		if ( current < 0 ) return;
		if ( !saveCurrent() ) return;
		if ( TUCount > 0 ) {
			current = TUCount-1;
			getNewCurrent(current, false);
		}
		else {
			current = -1;
		}
		displayCurrent();
	}
	
	private boolean saveCurrent () {
		if ( current == -1 ) return true;
		// Anything has changed?
		if ( !editPanel.isModified() ) return true;
		// Apply the changes from the edit box to the text container
		if ( !editPanel.applyChanges() ) return false; // Error occurred
		// Save the changes
		if ( vtu != null ) {
			vtu.save();
		}
		return true;
	}

	private void saveOutput () {
		if ( !saveCurrent() ) return;
		IFilter filter = null;
		IFilterWriter writer = null;
		try {
			filter = fcMapper.createFilter(rawDoc.getFilterConfigId());
			filter.open(rawDoc);
			int tuIndex = 0;
			while ( filter.hasNext() ) {
				Event event = filter.next();
				switch ( event.getEventType() ) {
				case START_DOCUMENT:
					StartDocument sd = (StartDocument)event.getResource();
					outEncoding = rawDoc.getEncoding();
					outPath = new File(rawDoc.getInputURI()).getPath();
					outPath = Util.getDirectoryName(outPath) + File.separator + Util.getFilename(outPath, false) + ".out" + Util.getExtension(outPath);
					writer = sd.getFilterWriter();
					writer.setOptions(trgLoc, outEncoding);
					writer.setOutput(outPath);
					break;
				case TEXT_UNIT:
					ITextUnit oriTU = event.getTextUnit();
					ITextUnit updTU;
					if ( useRepository ) {
						IVTextUnit updVTU = (IVTextUnit)vdoc.getItem(keys.get(tuIndex));
						updTU = updVTU.getTextUnit();
					}
					else {
						updTU = textUnits.get(tuIndex);
					}
					// Make sure they are in sync (just to be sure)
					if ( !oriTU.getId().equals(updTU.getId()) ) {
						throw new OkapiException("Text units de-synchronized: the underlying file has changed.");
					}
					TextContainer tc = updTU.getTarget(trgLoc);
					if ( tc != null ) oriTU.setTarget(trgLoc, tc);
					tuIndex++;
					break;
				default:
					break;
				}
				writer.handleEvent(event);
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(shell, "Error when saving document.\n"+e.getMessage(), null);
		}
	}
	
	private void createInitialExtractedText () {
		rawDoc = null;
		textUnits = new ArrayList<ITextUnit>();
		
		TextFragment srcFrag = new TextFragment("This is a dummy entry of ");
		srcFrag.append(TagType.OPENING, "emph", "<em>");
		srcFrag.append("extracted text");
		srcFrag.append(TagType.CLOSING, "emph", "</em>");
		srcFrag.append(". But you can also test REAL files too.");
		srcFrag.append(TagType.PLACEHOLDER, "SomeCode", "<br/>");
		srcFrag.append("Click ");
		srcFrag.append(TagType.OPENING, "bold", "<b>");
		srcFrag.append("Next");
		srcFrag.append(TagType.CLOSING, "emph", "</b>");
		srcFrag.append(" to learn how.");
		ITextUnit tu = new TextUnit("id1");
		tu.setSource(new TextContainer(srcFrag));
		textUnits.add(tu);
		
		srcFrag = new TextFragment("To get the text of a file: Click");
		srcFrag.append(TagType.OPENING, "bold", "<b>");
		srcFrag.append("Open File");
		srcFrag.append(TagType.CLOSING, "bold", "</b>");
		srcFrag.append(". Then select the document to open, the filter configuration to use, and the default encoding.");
		tu = new TextUnit("id2");
		tu.setSource(new TextContainer(srcFrag));
		textUnits.add(tu);

		TUCount = textUnits.size();
		displayFirst();
	}

}
