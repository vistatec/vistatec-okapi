/*===========================================================================
  Copyright (C) 2016 by the Okapi Framework contributors
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

package net.sf.okapi.steps.patternschecker;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.StringParameters;
import net.sf.okapi.lib.verification.Issue;
import net.sf.okapi.lib.verification.PatternItem;

public class Parameters extends StringParameters {
	  private static final String CHECKPATTERNS = "checkPatterns";
	  private static final String PATTERNCOUNT = "patternCount";
	  private static final String USEPATTERN = "usePattern";
	  private static final String FROMSOURCEPATTERN = "fromSourcePattern";
	  private static final String SEVERITYPATTERN = "severityPattern";
	  private static final String SOURCEPATTERN = "sourcePattern";
	  private static final String TARGETPATTERN = "targetPattern";
	  private static final String DESCPATTERN = "descPattern";

	List<PatternItem> patterns;

	public Parameters() {
		super();
	}


	public boolean getCheckPatterns() {
		return getBoolean(CHECKPATTERNS);
	}

	public void setCheckPatterns(boolean patterns) {
		setBoolean(CHECKPATTERNS, patterns);
	}

	public List<PatternItem> getPatterns() {
		return this.patterns;
	}

	public void setPatterns(List<PatternItem> patterns) {
		this.patterns = patterns;
	}


	@Override
	public void reset() {
		super.reset();
		setCheckPatterns(true);
		patterns = new ArrayList<PatternItem>();

		// Opening parentheses
		patterns.add(new PatternItem(
				"[\\(\\uFF08]", "[\\(\\uFF08]",
				true, Issue.DISPSEVERITY_LOW, "Opening parenthesis"));

		// Closing parentheses
		patterns.add(new PatternItem(
				"[\\)\\uFF09]", "[\\)\\uFF09]",
				true, Issue.DISPSEVERITY_LOW, "Closing parenthesis"));

		// Bracketing characters (except parentheses)
		patterns.add(new PatternItem(
				"[\\p{Ps}\\p{Pe}&&[^\\(\\)\\uFF08\\uFF09]]", "<same>",
				true, Issue.DISPSEVERITY_LOW, "Bracketing characters (except parentheses)"));

		// Email addresses
		patterns.add(new PatternItem(
				"[\\w\\.\\-]+@[\\w\\.\\-]+", "<same>",
				true, Issue.DISPSEVERITY_MEDIUM, "Email addresses"));

		// URLs
		patterns.add(new PatternItem(
				//"((http|https|ftp|sftp)\\:\\/\\/([-_a-z0-9]+\\@)?)?(([-_a-z0-9]+\\.)+[-_a-z0-9]+(\\:[0-9]+)?)((\\/([-_.:;+~%#$?=&,()\\w]*[\\w])?))*", "<same>",
				"https?:[\\w/\\.:;+\\-~\\%#\\$?=&,()]+[\\w/:;+\\-~\\%#\\$?=&,()]+|www\\.[\\w/\\.:;+\\-~\\%#\\$?=&,()]+|ftp:[\\w/\\.:;+\\-~\\%#?=&,]+", "<same>",
				true, Issue.DISPSEVERITY_MEDIUM, "URLs"));

		// IP addresses
		patterns.add(new PatternItem(
				"\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b", "<same>",
				true, Issue.DISPSEVERITY_HIGH, "IP addresses"));

		// C-style printf 
		patterns.add(new PatternItem(
				"%(([-0+#]?)[-0+#]?)((\\d\\$)?)(([\\d\\*]*)(\\.[\\d\\*]*)?)[dioxXucsfeEgGpn]", "<same>",
				true, Issue.DISPSEVERITY_HIGH, "C-style printf codes"));

		// Triple letter
		PatternItem item = new PatternItem(
				"<same>", "([\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}])\\1\\1",
				true, Issue.DISPSEVERITY_MEDIUM, "Tripled letter");
		item.fromSource = false;
		patterns.add(item);
	}

	@Override
	public void fromString(String data) {
		super.fromString(data);

		// Patterns
		int count = buffer.getInteger(PATTERNCOUNT, 0);
		if (count > 0) {
			patterns.clear(); // Clear the defaults
		}
		for (int i = 0; i < count; i++) {
			boolean enabled = buffer.getBoolean(String.format("%s%d", USEPATTERN, i), true);
			int severity = buffer.getInteger(String.format("%s%d", SEVERITYPATTERN, i), Issue.DISPSEVERITY_MEDIUM);
			boolean fromSource = buffer.getBoolean(String.format("%s%d", FROMSOURCEPATTERN, i), true);
			String source = buffer.getString(String.format("%s%d", SOURCEPATTERN, i), "");
			String target = buffer.getString(String.format("%s%d", TARGETPATTERN, i), PatternItem.SAME);
			String desc = buffer.getString(String.format("%s%d", DESCPATTERN, i), "");
			patterns.add(new PatternItem(source, target, enabled, severity, fromSource, desc));
		}
	}

	@Override
	public String toString() {

		buffer.setInteger(PATTERNCOUNT, patterns.size());
		for (int i = 0; i < patterns.size(); i++) {
			buffer.setBoolean(String.format("%s%d", USEPATTERN, i), patterns.get(i).enabled);
			buffer.setBoolean(String.format("%s%d", FROMSOURCEPATTERN, i), patterns.get(i).fromSource);
			buffer.setInteger(String.format("%s%d", SEVERITYPATTERN, i), patterns.get(i).severity);
			buffer.setString(String.format("%s%d", SOURCEPATTERN, i), patterns.get(i).source);
			buffer.setString(String.format("%s%d", TARGETPATTERN, i), patterns.get(i).target);
			buffer.setString(String.format("%s%d", DESCPATTERN, i), patterns.get(i).description);
		}		
		return super.toString();
	}
}
