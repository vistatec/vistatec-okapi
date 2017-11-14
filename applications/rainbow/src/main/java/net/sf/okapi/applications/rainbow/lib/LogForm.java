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

package net.sf.okapi.applications.rainbow.lib;

import net.sf.okapi.common.IHelp;
import net.sf.okapi.common.ui.UIUtil;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * General-purpose default log as a window.
 * This log is not thread-safe.
 */
public class LogForm implements ILog {

	private Shell shell;
	private Text edLog;
	private StringBuffer strLog = new StringBuffer();
	private Button btStop;
	private int errorCount;
	private int warningCount;
	private long data = 0;
	private ProgressBar pbPrimary;
	private ProgressBar pbSecondary;
	private boolean inProgress = false;
	private IHelp help;
	private String helpPath;
	private long startTime;
	
	public LogForm (Shell p_Parent) {
		shell = new Shell(p_Parent, SWT.BORDER | SWT.RESIZE | SWT.TITLE
			| SWT.MODELESS | SWT.CLOSE | SWT.MAX | SWT.MIN);
		UIUtil.inheritIcon(shell, p_Parent);
		createContent();
	}
	
	private void createContent () {
		shell.setLayout(new GridLayout(4, false));
		
		// On close: Hide instead of closing
		shell.addListener(SWT.Close, new Listener() {
			public void handleEvent(Event event) {
				event.doit = false;
				hide();
			}
		});
		
		int nWidth = 80;
		Button button = new Button(shell, SWT.PUSH);
		button.setText(Res.getString("LogForm.help")); //$NON-NLS-1$
		GridData gdTmp = new GridData();
		gdTmp.widthHint = nWidth;
		button.setLayoutData(gdTmp);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (( help != null ) && ( helpPath != null )) help.showTopic(null, helpPath);
			}
		});
		
		button = new Button(shell, SWT.PUSH);
		button.setText(Res.getString("LogForm.clear")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.widthHint = nWidth;
		button.setLayoutData(gdTmp);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				clear();
			}
		});
		
		btStop = new Button(shell, SWT.PUSH);
		btStop.setText(Res.getString("LogForm.stop")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.widthHint = nWidth;
		btStop.setLayoutData(gdTmp);
		btStop.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				cancel(true);
			}
		});
		
		button = new Button(shell, SWT.PUSH);
		button.setText(Res.getString("LogForm.close")); //$NON-NLS-1$
		gdTmp = new GridData();
		gdTmp.widthHint = nWidth;
		button.setLayoutData(gdTmp);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				hide();
			}
		});
		
		//=== Progress
		
		pbPrimary = new ProgressBar(shell, SWT.HORIZONTAL);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		pbPrimary.setLayoutData(gdTmp);

		pbSecondary = new ProgressBar(shell, SWT.HORIZONTAL);
		gdTmp = new GridData(GridData.FILL_HORIZONTAL);
		gdTmp.horizontalSpan = 4;
		pbSecondary.setLayoutData(gdTmp);
		
		//=== Log itself

		edLog = new Text(shell, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		gdTmp = new GridData(GridData.FILL_BOTH);
		gdTmp.horizontalSpan = 4;
		edLog.setLayoutData(gdTmp);
		
		updateDisplay();
		shell.pack();
		shell.setMinimumSize(shell.getSize());
		shell.setSize(600, 300);
	}

	private void updateDisplay () {
		final Display disp = Display.getDefault();
		// Don't do anything if the current thread is not the display thread
		if (Thread.currentThread().getId() != disp.getThread().getId())
			return;

		edLog.append(strLog.toString());
		strLog = new StringBuffer();

		btStop.setEnabled(inProgress);
		pbPrimary.setEnabled(inProgress);
		pbSecondary.setEnabled(inProgress);

		while (disp.readAndDispatch()) {}
	}

	public boolean beginProcess (String p_sText) {
		if ( inProgress() ) return false;
		clear();
		startTime = System.currentTimeMillis();
		strLog.append(Res.getString("LogForm.startProcess")); //$NON-NLS-1$
		if (( p_sText != null ) && ( p_sText.length() > 0 ))
			setLog(LogType.MESSAGE, 0, p_sText);
		errorCount = warningCount = 0;
		inProgress = true;
		updateDisplay();
		return inProgress;
	}

	public boolean beginTask (String p_sText) {
		if (( p_sText != null ) && ( p_sText.length() > 0 ))
			setLog(LogType.MESSAGE, 0, p_sText);
		return inProgress;
	}

	public boolean canContinue () {
		//TODO: Implement user-cancel with escape key
		return inProgress;
	}

	public void cancel (boolean p_bAskConfirmation) {
		if ( inProgress() )
		{
			if ( p_bAskConfirmation )
			{
//				System.out.print(Res.getString("CONFIRM_CANCEL")); //$NON-NLS-1$
//TODO				char chRes = char.ToLower((char)Console.Read());
//				string sYN = m_RM.GetString("CONFIRM_YESNOLETTERS");
//				if ( chRes != sYN[0] ) return; // No cancellation
			}
			// Cancel the process
			endTask(null);
			endProcess(null);
		}
	}

	public void clear () {
		edLog.setText(""); //$NON-NLS-1$
		strLog = new StringBuffer();
	}

	private String toHMSMS (long millis) {
		long hours = millis/3600000;
		millis = millis - (hours*3600000);
		long minutes = millis/60000;
		millis = millis-(minutes*60000);
		long seconds = millis/1000;
		millis = millis-(seconds*1000);
		return String.format("%dh %dm %ds %dms", hours, minutes, seconds, millis);
	}
	
	public void endProcess (String p_sText) {
		if ( inProgress ) {
			if (( p_sText != null ) && ( p_sText.length() > 0 ))
				setLog(LogType.MESSAGE, 0, p_sText);
			strLog.append(String.format(Res.getString("LogForm.errorCount"), errorCount)); //$NON-NLS-1$
			strLog.append(String.format(Res.getString("LogForm.warningCount"), warningCount)); //$NON-NLS-1$
			strLog.append(String.format(Res.getString("LogForm.duration"), //$NON-NLS-1$
				toHMSMS(System.currentTimeMillis()-startTime)));
			strLog.append(Res.getString("LogForm.endProcess")); //$NON-NLS-1$
		}
		inProgress = false;
		updateDisplay();
	}

	public void endTask (String p_sText) {
		if ( inProgress ) {
			if (( p_sText != null ) && ( p_sText.length() > 0 ))
				setLog(LogType.MESSAGE, 0, p_sText);
		}
	}

	public boolean error (String p_sText) {
		return setLog(LogType.ERROR, 0, p_sText);
	}

	public long getCallerData () {
		return data;
	}

	public int getErrorAndWarningCount () {
		return errorCount+warningCount;
	}

	public int getErrorCount () {
		return errorCount;
	}

	public int getWarningCount () {
		return warningCount;
	}

	public boolean inProgress () {
		return inProgress;
	}

	public boolean message (String p_sText) {
		return setLog(LogType.MESSAGE, 0, p_sText);
	}

	public boolean newLine () {
		return setLog(LogType.MESSAGE, 0, "\n"); //$NON-NLS-1$
	}

	public void save (String path) {
		// Not implemented for this implementation
	}

	public void setCallerData (long newData) {
		// Not used, just store it
		data = newData;
	}

	public void setHelp (IHelp helpParam,
		String helpPath) {
		help = helpParam;
		this.helpPath = helpPath;
	}

	public boolean setLog (int p_nType,
		int p_nValue,
		String p_sValue)
	{
		switch ( p_nType ) {
		case LogType.ERROR:
			strLog.append(Res.getString("LogForm.error") + p_sValue + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			updateDisplay();
			errorCount++;
			break;
		case LogType.WARNING:
			strLog.append(Res.getString("LogForm.warning") + p_sValue + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			updateDisplay();
			warningCount++;
			break;
		case LogType.MESSAGE:
			strLog.append(p_sValue + "\n"); //$NON-NLS-1$
			updateDisplay();
			break;
		case LogType.SUBPROGRESS:
		case LogType.MAINPROGRESS:
			break;
		case LogType.USERFEEDBACK:
		default:
			break;
		}
		return canContinue();
	}

	public void setMainProgressMode (int p_nValue) {
		if ( p_nValue < 0 ) p_nValue = 0;
		if ( p_nValue > 100 ) p_nValue = 100;
		//pbPrimary. .s.set.setValue(p_nValue);
	}

	public boolean setOnTop (boolean p_bValue) {
		//TODO boolean bRes = isAlwaysOnTop();
		//setAlwaysOnTop(p_bValue);
		return false; //bRes;
	}

	public void setSubProgressMode (int p_nValue) {
		if ( p_nValue < 0 ) p_nValue = 0;
		if ( p_nValue > 100 ) p_nValue = 100;
		//pbSecondary.setValue(p_nValue);
	}

	public boolean warning (String p_sText) {
		return setLog(LogType.WARNING, 0, p_sText);
	}

	public void hide () {
		shell.setVisible(false);
	}

	public void setTitle (String text) {
		shell.setText(text);
	}

	public void show () {
		shell.setVisible(true);
		if ( shell.getMinimized() ) shell.setMinimized(false);
	}

	public boolean isVisible() {
		return shell.isVisible();
	}
}
