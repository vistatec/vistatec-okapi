/*===========================================================================
  Copyright (C) 2010-2012 by the Okapi Framework contributors
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.ui.Dialogs;
import net.sf.okapi.common.ui.InputDialog;
import net.sf.okapi.common.ui.TextOptions;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Editing panel for a section of extracted text.
 * This editor can edit either a segment or a text container.
 */
public class TextContainerEditorPanel {

	private static final String SEGTYPECHAR = "\uFFF9";
	
	private String codedText;
	private List<Code> codes;
	private TextContainer textCont;
	private TextFragment textFrag;
	private StyledText edit;
	private TextStyle codeStyle;
	private TextStyle markStyle;
	private int mode = 0;
	private Menu contextMenu;
	private TextOptions textOptions;
	private ArrayList<StyleRange> ranges;
	private boolean updateCodeRanges = false;
	private int prevPos = 0;
	private int selAnchor = -1;
	private boolean targetMode = false;
	private TextContainerEditorPanel source;
	private PairEditorPanel parentPanel;
	private int nextCodeForCopy = -1;
	private boolean modified;
	private final int frontChars = 6;
	private final int tailChars = 3;
	private final int maxChars = frontChars+3+tailChars;
	
	public TextContainerEditorPanel (Composite parent,
		int flag,
		boolean paramTargetMode)
	{
		targetMode = paramTargetMode;
		if ( flag < 0 ) { // Use the default styles if requested
			flag = SWT.WRAP | SWT.V_SCROLL | SWT.BORDER;
		}
		edit = new StyledText(parent, flag);
		GridData gdTmp = new GridData(GridData.FILL_BOTH);
		edit.setLayoutData(gdTmp);
		
		codeStyle = new TextStyle();
		codeStyle.foreground = parent.getDisplay().getSystemColor(SWT.COLOR_RED);
		
		markStyle = new TextStyle();
		markStyle.foreground = parent.getDisplay().getSystemColor(SWT.COLOR_GRAY);
		
		createContextMenu();
		edit.setMenu(contextMenu);
		
		edit.addCaretListener(new CaretListener() {
			@Override
			public void caretMoved(CaretEvent e) {
				for ( StyleRange range : ranges ) {
					if (( e.caretOffset > range.start ) && ( e.caretOffset < range.start+range.length )) {

						if ( prevPos < e.caretOffset ) prevPos = range.start+range.length;
						else prevPos = range.start;
						
						if ( selAnchor != -1 ) { // Selection mode
							Point pt = edit.getSelection();
							if ( selAnchor < e.caretOffset ) {
								pt.x = selAnchor;
								pt.y = prevPos;
								edit.setSelection(pt);
							}
							else {
								pt.x = selAnchor; 
								pt.y = prevPos;
								edit.setSelection(pt);
							}
						}
						else { // Movement mode
							edit.setCaretOffset(prevPos);
						}
						return;
					}
				}
				// Else: No in a code range: just remember the position
				prevPos = e.caretOffset;
			}
		});
		
		edit.addMouseListener(new MouseListener() {
			@Override
			public void mouseUp(MouseEvent e) {
				selAnchor = -1;
			}
			@Override
			public void mouseDown(MouseEvent e) {
				Point pt = edit.getSelection();
				if ( pt.y == edit.getCaretOffset() ) selAnchor = pt.x;
				else selAnchor = pt.y;
			}
			@Override
			public void mouseDoubleClick (MouseEvent e) {
				int pos = edit.getCaretOffset();
				StyleRange sr = getCodeRangeIfInside(pos);
				if ( sr != null ) {
					edit.setSelection(sr.start, sr.start+sr.length);
				}
			}
		});
		
		edit.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				if ( e.keyCode == SWT.SHIFT ) {
					selAnchor = -1;
				}
			}
			@Override
			public void keyPressed(KeyEvent e) {
				if ( e.keyCode == SWT.SHIFT ) {
					Point pt = edit.getSelection();
					if ( pt.y == edit.getCaretOffset() ) selAnchor = pt.x;
					else selAnchor = pt.y;
				}
			}
		});
		
		edit.addVerifyKeyListener(new VerifyKeyListener() {
			@Override
			public void verifyKey(VerifyEvent e) {
				if ( e.stateMask == SWT.ALT ) {
					switch ( e.keyCode ) {
					case SWT.ARROW_RIGHT:
						selectNextCode(edit.getCaretOffset(), true);
						e.doit = false;
						break;
					case SWT.ARROW_LEFT:
						selectPreviousCode(edit.getCaretOffset(), true);
						e.doit = false;
						break;
					case SWT.ARROW_DOWN: // Target-mode command
						setNextSourceCode();
						e.doit = false;
						break;
					case SWT.ARROW_UP: // Target-mode command
						setPreviousSourceCode();
						e.doit = false;
						break;
					}
				}
				else if ( e.stateMask == SWT.CTRL ) {
					switch ( e.keyCode ) {
					case 'd':
						cycleDisplayMode();
						e.doit = false;
						break;
					case 'c':
						copyToClipboard(edit.getSelection());
						e.doit = false;
						break;
					case 'v':
						pasteFromClipboard();
						e.doit = false;
						break;
					case ' ':
						placeText("\u00a0");
						e.doit = false;
						break;
					}
				}
				else if ( e.stateMask == SWT.SHIFT ) {
					switch ( e.keyCode ) {
					case SWT.DEL:
						cutToClipboard(edit.getSelection());
						e.doit = false;
						break;
					case SWT.INSERT:
						pasteFromClipboard();
						e.doit = false;
						break;
					}
				}
			}
		});

		edit.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				if ( !updateCodeRanges ) return; // Not a modification
				if ( e.start == edit.getCharCount() ) {
					modified = true;
					return; // No need to update code ranges
				}
				int len = e.end-e.start;
				if ( len == 0 ) {
					if ( e.start > 0 ) {
						int n = isOnCodeRange(e.start);
						if (( n != -1 ) && ( n != e.start )) {
							e.doit = false;
							return;
						}
					} // Position zero always OK, and ranges still need to be updated
				}
				else {
					if ( breakRange(e.start, e.end) ) {
						e.doit = false;
						return;
					}
				}
				// Modification is allowed: Update the code ranges
				modified = true;
				updateRanges(e.start, e.end, e.text.length());
			}
		});

		edit.setMargins(2, 2, 2, 2);

		edit.setKeyBinding(SWT.CTRL|'a', ST.SELECT_ALL);

		// Disable Cut/Copy/Paste commands to override them
		edit.setKeyBinding(SWT.CTRL|'c', SWT.NULL);
		edit.setKeyBinding(SWT.CTRL|'v', SWT.NULL);
		edit.setKeyBinding(SWT.SHIFT|SWT.DEL, SWT.NULL);
		edit.setKeyBinding(SWT.SHIFT|SWT.INSERT, SWT.NULL);
		
		// Create a copy of the default text field options for the source
		textOptions = new TextOptions(parent.getDisplay(), edit, 3);
		textOptions.applyTo(edit);
	}
	
	@Override
	protected void finalize () {
		dispose();
	}

	public void dispose () {
		if ( textOptions != null ) {
			textOptions.dispose();
			textOptions = null;
		}
		UIUtil.disposeTextStyle(codeStyle);
		UIUtil.disposeTextStyle(markStyle);
	}

	public boolean setFocus () {
		return edit.setFocus();
	}
	
	public void setTargetRelations (TextContainerEditorPanel source,
		PairEditorPanel parentPanel)
	{
		this.source = source;
		this.parentPanel = parentPanel;
	}
	
	public void setEnabled (boolean enabled) {
		edit.setEnabled(enabled);
	}
	
	public void setEditable (boolean editable) {
		edit.setEditable(editable);
	}

	private void placeText (String text) {
		Point pt = edit.getSelection();
		edit.replaceTextRange(pt.x, pt.y-pt.x, text);
		edit.setCaretOffset(pt.x+text.length());
	}
	
//	public void applyTextOptions (TextOptions textOptions) {
//		edit.setBackground(textOptions.background);
//		edit.setForeground(textOptions.foreground);
//		edit.setFont(textOptions.font);
//		edit.setOrientation(textOptions.isBidirectional ? SWT.RIGHT_TO_LEFT : SWT.LEFT_TO_RIGHT);
//	}
	
	private void createContextMenu () {
		contextMenu = new Menu(edit.getShell(), SWT.POP_UP);

		MenuItem item = new MenuItem(contextMenu, SWT.PUSH);
		item.setText("Change Code Display Mode");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				cycleDisplayMode();
            }
		});
		
		if ( targetMode ) {
			new MenuItem(contextMenu, SWT.SEPARATOR);
		
			item = new MenuItem(contextMenu, SWT.PUSH);
			item.setText("Edit Code");
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					editCode();
	            }
			});

			item = new MenuItem(contextMenu, SWT.PUSH);
			item.setText("Remove All Codes");
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					clearCodes();
	            }
			});

			item = new MenuItem(contextMenu, SWT.PUSH);
			item.setText("Copy Source Into Target");
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					pasteSource();
	            }
			});

			item = new MenuItem(contextMenu, SWT.PUSH);
			item.setText("Copy Source Codes Into Target");
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					pasteAllSourceCodes();
	            }
			});
			
			new MenuItem(contextMenu, SWT.SEPARATOR);
			
			item = new MenuItem(contextMenu, SWT.PUSH);
			item.setText("Switch Panel Orientation");
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					parentPanel.setOrientation(parentPanel.getOrientation() == SWT.VERTICAL ? SWT.HORIZONTAL : SWT.VERTICAL);
	            }
			});
		}

		new MenuItem(contextMenu, SWT.SEPARATOR);
		item = new MenuItem(contextMenu, SWT.PUSH);
		item.setText("Options...");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				editOptions();
            }
		});
	}
	
	private void cycleDisplayMode () {
		Point pt = cacheContent(edit.getSelection());
		if ( pt == null ) return;
		mode = (mode==0 ? 1 : (mode==2 ? 0 : 2));
		updateText(pt);
	}
	
	private void refresh () {
		Point pt = cacheContent(edit.getSelection());
		if ( pt == null ) return;
		updateText(pt);
	}
	
	private void createContainerFromFragment () {
		try {
			textCont.clear();
			Code code;
			ISegments segs = textCont.getSegments();
			Segment seg = null;
			ArrayList<Code> tmpCodes = new ArrayList<Code>();
			StringBuilder tmp = new StringBuilder();
			
			for ( int i=0; i<codedText.length(); i++ ) {
				if ( TextFragment.isMarker(codedText.charAt(i)) ) {
					code = codes.get(TextFragment.toIndex(codedText.charAt(++i)));
					
					if ( code.getType().equals(SEGTYPECHAR) ) { // A segment marker
						if ( code.getTagType() == TagType.OPENING ) {
							if ( seg != null ) {
								throw new OkapiException("Invalid opening segment marker: "+code.getOuterData());
							}
							// Add previous part if needed
							if ( tmp.length() > 0 ) {
								textCont.append(new TextFragment(tmp.toString(), tmpCodes));
							}
							// Create new segment
							seg = new Segment();
							seg.id = code.getOuterData().substring(1, code.getOuterData().length()-1);
						}
						else { // Closing marker: add the segment, start a new one
							if ( seg == null ) {
								throw new OkapiException("Invalid closing segment marker: "+code.getOuterData());
							}
							seg.text = new TextFragment(tmp.toString(), tmpCodes);
							segs.append(seg);
							seg = null;
						}
						// In both cases: reset the fragment building variables
						tmp  = new StringBuilder();
						tmpCodes = new ArrayList<Code>();
					}
					else { // A normal code: add it to the fragment being build
						tmpCodes.add(code); // No need to clone
						tmp.append(String.format("%c%c", codedText.charAt(i-1),
							TextFragment.toChar(tmpCodes.size()-1)));
					}
				}
				else { // Part is either a text-part or a segment here
					tmp.append(codedText.charAt(i));
				}
			}
		
			if ( seg != null ) {
				throw new OkapiException("Missing closing segment marker.");
			}
			// Ensure we add any extra ending part
			if ( tmp.length() > 0 ) {
				textCont.append(new TextFragment(tmp.toString(), tmpCodes));
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), "Error creating fragment.\n"+e.getLocalizedMessage(), null);
		}		
	}
	
	private void createFragmentFromContainer () {
		try {
			StringBuilder tmp = new StringBuilder();
			codes = new ArrayList<Code>();
			Code segCode;
			StringBuilder tmpPart = new StringBuilder();
			
			for ( TextPart part : textCont ) {
				// Add start-segment marker 
				if ( part.isSegment() ) {
					segCode = new Code(TagType.OPENING, SEGTYPECHAR);
					segCode.setOuterData("{"+((Segment)part).getId()+">");
					codes.add(segCode);
					tmp.append(String.format("%c%c", (char)TextFragment.MARKER_OPENING,
						TextFragment.toChar(codes.size()-1)));
				}
				
				
				// Adjust the marker indices
				tmpPart.setLength(0);
				tmpPart.append(part.text.getCodedText());
				int newIndex = codes.size(); // Start at the last code
				for ( int j=0; j<tmpPart.length(); j++ ) {
					if ( TextFragment.isMarker(tmpPart.charAt(j)) ) {
						tmpPart.setCharAt(++j, TextFragment.toChar(newIndex++));
					}
				}
				// Add the part
				tmp.append(tmpPart);
				codes.addAll(part.text.getCodes());
				
				// Add end-segment marker
				if ( part.isSegment() ) {
					segCode = new Code(TagType.CLOSING, SEGTYPECHAR);
					segCode.setOuterData("<"+((Segment)part).getId()+"}");
					codes.add(segCode);
					tmp.append(String.format("%c%c", (char)TextFragment.MARKER_CLOSING,
						TextFragment.toChar(codes.size()-1)));
				}
			}
			// Adjust the index for the segments
			
			// Set the final coded text
			codedText = tmp.toString();
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), "Error creating container back.\n"+e.getLocalizedMessage(), null);
		}		
	}
	
	public void setText (TextFragment oriFrag) {
		modified = false;
		textCont = null;
		textFrag = oriFrag;
		edit.setEnabled(oriFrag != null);
		if ( oriFrag == null ) {
			edit.setText("");
		}
		else {
			codedText = textFrag.getCodedText();
			// Make a copy of the list, as getCodes() gives an un-modifiable list
			codes = new ArrayList<Code>(textFrag.getCodes());
			updateText(null);
		}
	}
	
	public void setText (TextContainer oriCont) {
		modified = false;
		textFrag = null;
		textCont = oriCont;
		edit.setEnabled(oriCont != null);
		if ( oriCont == null ) {
			edit.setText("");
			return;
		}
		// Otherwise: set the content
		createFragmentFromContainer();
		updateText(null);
	}
	
	public void clear () {
		textFrag = null;
		textCont = null;
		edit.setText("");
		codedText = null;
		modified = false;
	}
	
	public boolean isModified () {
		return modified;
	}

	public boolean applyChanges () {
		try {
			if ( !modified ) return true;
			cacheContent(null);
			if ( textFrag != null ) {
				textFrag.setCodedText(codedText, codes, true);
			}
			else {
				createContainerFromFragment();
			}
			modified = false;
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), "Error when applying changes.\n"+e.getLocalizedMessage(), null);
			edit.setFocus();
			return false;
		}
		return true;
	}

	/**
	 * Gets the text in the edit control to a coded-text string with the proper code
	 * markers. This also makes basic validation.
	 * @param sel optional selection to re-compute. Use null to not use.
	 * @return a Point corresponding to the converted selection passed as parameter. Will be 0 and 0
	 * if the given selection was null. Return null if an error occurred
	 * and the text could not be cached.
	 */
	private Point cacheContent (Point sel) {
		try {
			Point pt = new Point(0, 0);
			if ( sel != null ) {
				pt.x = sel.x; pt.y = sel.y;
			}
			
			if ((( sel == null ) && ( !modified )) || ( codedText == null )) return pt;
			
			Code code = null;
			StringBuilder tmp = new StringBuilder(edit.getText());
			int diff = 0;
			for ( StyleRange range : ranges ) {
				code = (Code)range.data;
				int index = getCodeObjectIndex(code);
				switch ( code.getTagType() ) {
				case OPENING:
					tmp.replace(diff+range.start, diff+(range.start+range.length),
						String.format("%c%c", (char)TextFragment.MARKER_OPENING, TextFragment.toChar(index)));
					break;
				case CLOSING:
					tmp.replace(diff+range.start, diff+(range.start+range.length),
						String.format("%c%c", (char)TextFragment.MARKER_CLOSING, TextFragment.toChar(index)));
					break;
				case PLACEHOLDER:
					tmp.replace(diff+range.start, diff+(range.start+range.length),
						String.format("%c%c", (char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(index)));
					break;
				}
				diff += (2-range.length);

				// Compute new selection if needed
				if ( sel != null ) {
					if ( sel.x >= range.start+range.length ) pt.x += (2-range.length);
					if ( sel.y >= range.start+range.length ) pt.y += (2-range.length);
				}
			}
			codedText = tmp.toString();
			return pt;
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), "Error when retrieving edited text.\n"+e.getLocalizedMessage(), null);
			edit.setFocus();
			return null;
		}
	}

	/**
	 * Gets the index in the codes array of the given code object.
	 * @param codeToSearch the code to search for.
	 * @return the index of the given code in the codes array, or -1 if not found.
	 */
	private int getCodeObjectIndex (Code codeToSearch) {
		for ( int i=0; i<codes.size(); i++ ) {
			if ( codeToSearch == codes.get(i) ) {
				return i;
			}
		}
		return -1; // Not found
	}
	
	private void updateRanges (int start,
		int end,
		int length)
	{
		// Check for flag
		// This is needed because resetting the content means we have ranges set before
		// the text is set in the control.
		if ( !updateCodeRanges ) return;
		
		// Compute the length difference between the selection part and the replacement
		length = length-(end-start);

		// Update ranges after the end
		Iterator<StyleRange> iter = ranges.iterator();
		StyleRange range;
		while ( iter.hasNext() ) {
			range = iter.next();
			// Is the range is after or at the end of the modified text
			if ( end <= range.start ) {
				range.start += length;
			}
			// Otherwise, if the range is included in the selection it's deletion or replacement
			// So that range needs to be removed, along with its code
			else if (( start <= range.start ) && ( end >= range.start+range.length )) {
				// Remove the code and the range
				codes.remove((Code)range.data);
				iter.remove();
			}
		}
	}
	
	/**
	 * Moves the select in this editor to the next code and returns it.
	 * @return the next code in this editor, or null if no code was found.
	 */
	public FragmentData getNextCode () {
		if ( codes.isEmpty() ) return null;
		nextCodeForCopy++;
		if ( nextCodeForCopy >= codes.size() ) {
			nextCodeForCopy = 0;
		}
		return getCode(nextCodeForCopy);
	}

	public FragmentData getPreviousCode () {
		if ( codes.isEmpty() ) return null;
		nextCodeForCopy--;
		if ( nextCodeForCopy < 0 ) {
			nextCodeForCopy = codes.size()-1;
		}
		return getCode(nextCodeForCopy);
	}

	public FragmentData getAllContent () {
		FragmentData data = new FragmentData();
		cacheContent(null);
		data.codedText = codedText;
		data.codes = new ArrayList<Code>(codes);
		return data;
	}
	
	public FragmentData getAllCodes () {
		// Get the code
		FragmentData data = new FragmentData();
		data.codes = new ArrayList<Code>();
		StringBuilder tmp = new StringBuilder();
		for ( Code code : codes ) {
			data.codes.add(code.clone());
			// Construct the coded text
			switch ( code.getTagType() ) {
			case OPENING:
				tmp.append(String.format("%c%c", (char)TextFragment.MARKER_OPENING,
					TextFragment.toChar(data.codes.size()-1)));
				break;
			case CLOSING:
				tmp.append(String.format("%c%c", (char)TextFragment.MARKER_CLOSING,
					TextFragment.toChar(data.codes.size()-1)));
				break;
			case PLACEHOLDER:
				tmp.append(String.format("%c%c", (char)TextFragment.MARKER_ISOLATED,
					TextFragment.toChar(data.codes.size()-1)));
				break;
			}
		}
		data.codedText = tmp.toString();
		return data;
	}
	
	private FragmentData getCode (int index) {
		// Get the code
		FragmentData data = new FragmentData();
		data.codes = new ArrayList<Code>();
		Code code = codes.get(index);
		data.codes.add(code.clone());
		
		// Construct the coded text
		switch ( code.getTagType() ) {
		case OPENING:
			data.codedText = String.format("%c%c", (char)TextFragment.MARKER_OPENING, TextFragment.toChar(0));
			break;
		case CLOSING:
			data.codedText = String.format("%c%c", (char)TextFragment.MARKER_CLOSING, TextFragment.toChar(0));
			break;
		case PLACEHOLDER:
			data.codedText = String.format("%c%c", (char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(0));
			break;
		}
		
		// Select the corresponding location in the editor
		for ( StyleRange range : ranges ) {
			if ( code == (Code)range.data ) {
				edit.setSelection(range.start, range.start+range.length);
				break;
			}
		}
		
		return data;
	}
	
	/**
	 * Indicates if a given position is on a code range.
	 * @param position the given position.
	 * @return the start of the first code range found for the position.
	 */
	private int isOnCodeRange (int position) {
		for ( StyleRange range : ranges ) {
			if (( position >= range.start ) && ( position < range.start+range.length )) {
				return range.start;
			}
		}
		return -1;
	}
	
	private StyleRange getCodeRangeIfInside (int position) {
		for ( StyleRange range : ranges ) {
			if (( position >= range.start ) && ( position < range.start+range.length )) {
				return range;
			}
		}
		return null;
	}

	/**
	 * Indicates if the given selection does break one of the code ranges.
	 * @param start the start of the selection to check.
	 * @param end the end of the selection to check.
	 * @return true if the selection falls within one of the code ranges.
	 */
	private boolean breakRange (int start,
		int end)
	{
		//end--; // Look at position just before the end of the selection
		for ( StyleRange range : ranges ) {
			if (( start > range.start ) && ( start < range.start+range.length )) {
				return true;
			}
			if (( end > range.start ) && ( end < range.start+range.length )) {
				return true;
			}
		}
		return false;
	}
	
	private String makeDisplayCode (Code code) {
		if ( mode == 0 ) { // Generic code
			if ( code.getData().isEmpty() ) {
				if ( code.getType().equals(SEGTYPECHAR) ) {
					return code.getOuterData();
				}
				switch ( code.getTagType() ) {
				case OPENING:
					return String.format("{%d}", code.getId());
				case CLOSING:
					return String.format("{/%d}", code.getId());
				case PLACEHOLDER:
					return String.format("{%d/}", code.getId());
				}
			}
			else { // Normal code
				switch ( code.getTagType() ) {
				case OPENING:
					return String.format("<%d>", code.getId());
				case CLOSING:
					return String.format("</%d>", code.getId());
				case PLACEHOLDER:
					return String.format("<%d/>", code.getId());
				}
				// This should never reach this point, but just in case:
				return "</OTHER/>";
			}
		}

		// OOther mode use the data
		String data = code.getData();
		if ( data.isEmpty() ) {
			data = code.getOuterData();
			if ( data.isEmpty() ) data = "<>";
		}
		
		if ( mode == 1 ) { // Full code
			return data;
		}
		
		// Otherwise: Partial code
		int len = data.length();
		if ( len > maxChars ) {
			return String.format("%s...%s", data.subSequence(0, frontChars), data.subSequence(len-tailChars, len));
		}
		else {
			return data;
		}
	}
	
	private void updateText (Point sel) {
		try {
			if ( codedText == null ) return;
			updateCodeRanges = false;
			StringBuilder tmp = new StringBuilder();
			ranges = new ArrayList<StyleRange>();
			int pos = 0;
			StyleRange sr;
			String disp = null;
			Point pt = new Point(0, 0);
			if ( sel != null ) {
				pt.x = sel.x; pt.y = sel.y;
			}
			Code code;
			for ( int i=0; i<codedText.length(); i++ ) {
				if ( TextFragment.isMarker(codedText.charAt(i)) ) {
					code = codes.get(TextFragment.toIndex(codedText.charAt(++i)));
					disp = makeDisplayCode(code);
					tmp.append(disp);
					sr = new StyleRange(code.getData().isEmpty() ? markStyle : codeStyle);
					sr.start = pos;
					sr.length = disp.length();
					sr.data = code;
					ranges.add(sr);
					pos += disp.length();

					// Update the selection if needed
					if ( sel != null ) {
						if ( sel.x >= i ) pt.x += (disp.length()-2);
						if ( sel.y >= i ) pt.y += (disp.length()-2);
					}
				}
				else {
					tmp.append(codedText.charAt(i));
					pos++;
				}
			}
			
			nextCodeForCopy = -1;
			edit.setText(tmp.toString());
			for ( StyleRange range : ranges ) {
				edit.setStyleRange(range);
			}
			if ( sel != null ) {
				edit.setSelection(pt);
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), "Error when updating text.\n"+e.getLocalizedMessage(), null);
			edit.setFocus();
		}
		finally {
			updateCodeRanges = true;
		}
	}
	
	private void pasteSource () {
		if ( !targetMode ) return;
		setFragmentData(source.getAllContent(), 1);
	}
	
	private void pasteAllSourceCodes () {
		if ( !targetMode ) return;
		setFragmentData(source.getAllCodes(), 1);
	}
	
	private void setNextSourceCode () {
		if ( !targetMode ) return;
		setFragmentData(source.getNextCode(), 0);
	}

	private void setPreviousSourceCode () {
		if ( !targetMode ) return;
		setFragmentData(source.getPreviousCode(), 0);
	}

	/**
	 * Sets a FragmentData into this editor. The fragment replaces the current selection.
	 * @param data the fragment data to set.
	 * @param positionAfter Indicates how to place the caret after:
	 * <ul><li>1=place the caret just before
	 * <li>2=place the caret just after
	 * <li>0 or other=select the part placed
	 */
	private void setFragmentData (FragmentData data,
		int positionAfter)
	{
		try {
			if ( data == null ) return; // Nothing to do
	
			// Remove the current selection
			// This removes any underlying ranges and codes
			Point sel = edit.getSelection();
			remove(sel.x, sel.y);
	
			// Find if there is a code just after or at the insertion point
			// Get the index of the first range after the insertion position
			int index = 0;
			for ( StyleRange range : ranges ) {
				if ( range.start >= sel.x ) break;
				index++;
			}
	
			// Insert the new codes and ranges and build the display text
			StringBuilder tmp = new StringBuilder();
			String disp = null;
			Code code;
			int pos = sel.x;
			StyleRange sr;
			ArrayList<StyleRange> newRanges = new ArrayList<StyleRange>();
			int insPos = index;
			for ( int i=0; i<data.codedText.length(); i++ ) {
				if ( TextFragment.isMarker(data.codedText.charAt(i)) ) {
					code = data.codes.get(TextFragment.toIndex(data.codedText.charAt(++i))).clone();
					disp = makeDisplayCode(code);
					tmp.append(disp);
					sr = new StyleRange(code.getData().isEmpty() ? markStyle : codeStyle);
					sr.start = pos;
					sr.length = disp.length();
					sr.data = code;
					pos += disp.length();
					
					// Do not set the range immediately, so the text update can be done properly
					newRanges.add(sr);
					codes.add(insPos++, code);
				}
				else {
					tmp.append(data.codedText.charAt(i));
					pos++;
				}
			}
			
			// Insert the display text. This will update 
			edit.replaceTextRange(sel.x, 0, tmp.toString());
			// Set the ranges, and now add them to the list
			for ( StyleRange newRange : newRanges ) {
				edit.setStyleRange(newRange);
				ranges.add(index++, newRange);
			}

			modified = true;
			// Place the caret
			switch ( positionAfter ) {
			case 1:
				edit.setCaretOffset(sel.x);
				break;
			case 2:
				edit.setCaretOffset(sel.x+tmp.length());
				break;
			default:
				edit.setSelection(sel.x, sel.x+tmp.length());
			}
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), "Error when placing fragment data.\n"+e.getLocalizedMessage(), null);
			edit.setFocus();
		}
	}
	
	private void remove (int start,
		int end)
	{
		if ( start == end ) return; // Nothing to remove
		// Delete the text from the control
		edit.replaceTextRange(start, end-start, "");
	}
	
	private void selectNextCode (int position,
		boolean cycle)
	{
		if ( ranges.size() == 0 ) return;
		while ( true ) {
			for ( StyleRange range : ranges ) {
				if ( position <= range.start ) {
					edit.setSelection(range.start, range.start+range.length);
					return;
				}
			}
			// Not found yet: Stop here if we don't cycle to the first
			if ( !cycle ) return;
			position = 0; // Otherwise: re-start from front
		}
	}
	
	private void clearCodes () {
		if ( !edit.getEditable() ) return;
		cacheContent(null);
		StringBuilder tmp = new StringBuilder();
		for ( int i=0; i<codedText.length(); i++ ) {
			if ( TextFragment.isMarker(codedText.charAt(i)) ) {
				i++; // Skip
			}
			else {
				tmp.append(codedText.charAt(i));
			}
		}
		codes.clear();
		codedText = tmp.toString();
		// Ranges will get cleared in updateText()
		updateText(null);
		edit.setCaretOffset(0);
	}
	
	private void selectPreviousCode (int position,
		boolean cycle)
	{
		if ( ranges.size() == 0 ) return;
		StyleRange sr;
		while ( true ) {
			for ( int i=ranges.size()-1; i>=0; i-- ) {
				sr = ranges.get(i);
				if ( position >= sr.start+sr.length ) {
					Point pt = edit.getSelection();
					if (( pt.x == sr.start ) && ( pt.x != pt.y )) continue;
					edit.setSelection(sr.start, sr.start+sr.length);
					return;
				}
			}
			// Not found yet: Stop here if we don't cycle to the first
			if ( !cycle ) return;
			position = edit.getCharCount()-1; // Otherwise: re-start from the end
		}
	}

	private FragmentData getSelection (Point selection) {
		FragmentData data = new FragmentData();
		StringBuilder tmp = new StringBuilder(edit.getText(selection.x, selection.y-1));
		data.codes = new ArrayList<Code>();
		int diff = -1*selection.x;
		Code code;
		for ( StyleRange range : ranges ) {
			// Only if the range is within the selection
			if ( range.start >= selection.y ) break; // Any after is not within
			if ( range.start+range.length <= selection.x ) continue; // Any after may be within
			// If the range is within the selection:
			code = (Code)range.data;
			switch ( code.getTagType() ) {
			case OPENING:
				tmp.replace(diff+range.start, diff+(range.start+range.length),
					String.format("%c%c", (char)TextFragment.MARKER_OPENING, TextFragment.toChar(data.codes.size())));
				break;
			case CLOSING:
				tmp.replace(diff+range.start, diff+(range.start+range.length),
					String.format("%c%c", (char)TextFragment.MARKER_CLOSING, TextFragment.toChar(data.codes.size())));
				break;
			case PLACEHOLDER:
				tmp.replace(diff+range.start, diff+(range.start+range.length),
					String.format("%c%c", (char)TextFragment.MARKER_ISOLATED, TextFragment.toChar(data.codes.size())));
				break;
			}
			data.codes.add(code.clone());
			diff += (2-range.length);
		}
		data.codedText = tmp.toString();
		return data;
	}

	private void editCode () {
		Point selection = edit.getSelection();
		if ( selection.x == selection.y ) {
			return; // Nothing is selected
		}
		FragmentData data = getSelection(selection);
		if (( data.codedText.length() != 2 ) || !TextFragment.isMarker(data.codedText.charAt(0)) ) {
			Dialogs.showError(edit.getShell(), "The selection must contain one code and one only.", null);
			return; 
		}

		Code code = data.codes.get(0);
		InputDialog dlg = new InputDialog(edit.getShell(), "Edit Code",
			"Content of the code", code.getData(), null, 0, 50, 1);
		String res = dlg.showDialog();
		if ( res == null ) return; // User cancellation
		
		// Else set the new code
		code.setData(res);
		setFragmentData(data, 2);
	}
	
	private void cutToClipboard (Point selection) {
		FragmentData data = getSelection(selection);
		String plainText = edit.getText(selection.x, selection.y-1);
		remove(selection.x, selection.y);
		placeIntoClipboard(data, plainText);
	}
	
	private void copyToClipboard (Point selection) {
		placeIntoClipboard(getSelection(selection), edit.getText(selection.x, selection.y-1));
	}
	
	private void pasteFromClipboard () {
		Clipboard clipboard = new Clipboard(edit.getDisplay());
		try {
			TransferData[] transferDatas = clipboard.getAvailableTypes();
			for ( TransferData transData : transferDatas ) {
				if ( FragmentDataTransfer.getInstance().isSupportedType(transData) ) {
					FragmentData data = (FragmentData)clipboard.getContents(FragmentDataTransfer.getInstance());
					setFragmentData(data, 2);
					break;
				}
			}
		}
		finally {
			if ( clipboard != null ) {
				clipboard.dispose();
			}
		}
	}
	
	private void placeIntoClipboard (FragmentData data,
		String plainText)
	{
		Clipboard clipboard = new Clipboard(edit.getDisplay());
		try {
			FragmentDataTransfer dataTrans = FragmentDataTransfer.getInstance();  
			TextTransfer textTrans = TextTransfer.getInstance();
			// Create the clipboard entry
			clipboard.setContents(new Object[]{data, plainText}, new Transfer[]{dataTrans, textTrans});
		}
		finally {
			if ( clipboard != null ) {
				clipboard.dispose();
			}
		}
	}

	private void editOptions () {
		try {
			OptionsDialog dlg = new OptionsDialog(edit.getShell(), null);
			dlg.setData(textOptions, codeStyle, markStyle);
			// Call the dialog. A null return means the user canceled
			if ( !dlg.showDialog() ) return;

			// Else: set the modified options for the source
			TextOptions tmpTO = textOptions; // With StyledText we cannot free the old before we set the new
			textOptions = dlg.getTextOptions();
			
			TextStyle tmpTS1 = codeStyle;
			codeStyle = dlg.getCodeStyle();
			
			TextStyle tmpTS2 = markStyle;
			markStyle = dlg.getMarkStyle();
			
			// Re set the styles
			textOptions.applyTo(edit);
			refresh();
			
			// Dispose only after redrawing
			tmpTO.dispose();
			UIUtil.disposeTextStyle(tmpTS1);
			UIUtil.disposeTextStyle(tmpTS2);
		}
		catch ( Throwable e ) {
			Dialogs.showError(edit.getShell(), "Error editing options.\n"+e.getMessage(), null);
		}
	}

}
