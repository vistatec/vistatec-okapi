/*===========================================================================
  Copyright (C) 2010-2017 by the Okapi Framework contributors
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

package net.sf.okapi.lib.verification;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.ReferenceParameter;
import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;

public class Parameters extends StringParameters {

	public static final String FILE_EXTENSION = ".qccfg";

	public static final int SCOPE_ALL = 0;
	public static final int SCOPE_APPROVEDONLY = 1;
	public static final int SCOPE_NOTAPPROVEDONLY = 2;

	private static final String OUTPUTPATH = "outputPath";
	private static final String OUTPUTTYPE = "outputType";
	private static final String AUTOOPEN = "autoOpen";
	private static final String LEADINGWS = "leadingWS";
	private static final String TRAILINGWS = "trailingWS";
	private static final String EMPTYTARGET = "emptyTarget";
	private static final String EMPTYSOURCE = "emptySource";
	private static final String TARGETSAMEASSOURCE = "targetSameAsSource";
	private static final String TARGETSAMEASSOURCE_FORSAMELANGUAGE = "targetSameAsSourceForSameLanguage";
	private static final String TARGETSAMEASSOURCE_WITHCODES = "targetSameAsSourceWithCodes";
	private static final String CODEDIFFERENCE = "codeDifference";
	private static final String GUESSOPENCLOSE = "guessOpenClose";
	private static final String CHECKXLIFFSCHEMA = "checkXliffSchema";
	private static final String CHECKPATTERNS = "checkPatterns";
	private static final String PATTERNCOUNT = "patternCount";
	private static final String USEPATTERN = "usePattern";
	private static final String FROMSOURCEPATTERN = "fromSourcePattern";
	private static final String SEVERITYPATTERN = "severityPattern";
	private static final String SOURCEPATTERN = "sourcePattern";
	private static final String TARGETPATTERN = "targetPattern";
	private static final String DESCPATTERN = "descPattern";
	private static final String CHECKWITHLT = "checkWithLT";
	private static final String SERVERURL = "serverURL";
	private static final String TRANSLATELTMSG = "translateLTMsg";
	private static final String LTBILINGUALMODE = "ltBilingualMode";
	private static final String LTTRANSLATIONSOURCE = "ltTranslationSource";
	private static final String LTTRANSLATIONTARGET = "ltTranslationTarget";
	private static final String LTTRANSLATIONSERVICEKEY = "ltTranslationServiceKey";
	private static final String SAVESESSION = "saveSession";
	private static final String SESSIONPATH = "sessionPath";
	private static final String DOUBLEDWORD = "doubledWord";
	private static final String DOUBLEDWORDEXCEPTIONS = "doubledWordExceptions";
	private static final String CHECKSTORAGESIZE = "checkStorageSize";
	private static final String CHECKMAXCHARLENGTH = "checkMaxCharLength";
	private static final String MAXCHARLENGTHBREAK = "maxCharLengthBreak";
	private static final String MAXCHARLENGTHABOVE = "maxCharLengthAbove";
	private static final String MAXCHARLENGTHBELOW = "maxCharLengthBelow";
	private static final String CHECKMINCHARLENGTH = "checkMinCharLength";
	private static final String MINCHARLENGTHBREAK = "minCharLengthBreak";
	private static final String MINCHARLENGTHABOVE = "minCharLengthAbove";
	private static final String MINCHARLENGTHBELOW = "minCharLengthBelow";
	private static final String CHECKABSOLUTEMAXCHARLENGTH = "checkAbsoluteMaxCharLength";
	private static final String ABSOLUTEMAXCHARLENGTH = "absoluteMaxCharLength";
	private static final String CHECKCHARACTERS = "checkCharacters";
	private static final String CHECKALLOWEDCHARACTERS = "checkAllowedCharacters";
	private static final String CHARSET = "charset";
	private static final String EXTRACHARSALLOWED = "extraCharsAllowed";
	private static final String CORRUPTEDCHARACTERS = "corruptedCharacters";
	private static final String SCOPE = "scope";
	private static final String EXTRACODESALLOWED = "extraCodesAllowed";
	private static final String MISSINGCODESALLOWED = "missingCodesAllowed";
	private static final String CHECKTERMS = "checkTerms";
	private static final String TERMSPATH = "termsPath";
	private static final String CHECKBLACKLIST = "checkBlacklist";
	private static final String ALLOWBLACKLISTSUB = "allowBlacklistSub";	
	private static final String BLACKLISTSRC = "blacklistSrc";	
	private static final String BLACKLISTPATH = "blacklistPath";
	private static final String STRINGMODE = "stringMode";
	private static final String BETWEENCODES = "betweenCodes";
	private static final String TYPESTOIGNORE = "typesToIgnore";
	private static final String SHOWFULLPATH = "showFullPath";

	List<PatternItem> patterns;
	List<String> extraCodesAllowed;
	List<String> missingCodesAllowed;
	InputStream blacklistStream;

	public Parameters() {
		super();
	}

	public List<String> getExtraCodesAllowed() {
		return extraCodesAllowed;
	}

	public List<String> getMissingCodesAllowed() {
		return missingCodesAllowed;
	}

	public String getTypesToIgnore() {
		return getString(TYPESTOIGNORE);
	}

	public void setTypesToIgnore(String typesToIgnore) {
		setString(TYPESTOIGNORE, typesToIgnore);
	}

	public int getScope() {
		return getInteger(SCOPE);
	}

	public void setScope(int scope) {
		setInteger(SCOPE, scope);
	}

	public boolean getCorruptedCharacters() {
		return getBoolean(CORRUPTEDCHARACTERS);
	}

	public void setCorruptedCharacters(boolean corruptedCharacters) {
		setBoolean(CORRUPTEDCHARACTERS, corruptedCharacters);
	}

	public boolean getCheckAllowedCharacters() {
		return getBoolean(CHECKALLOWEDCHARACTERS);
	}

	public void setCheckAllowedCharacters(boolean checkAllowedCharacters) {
		setBoolean(CHECKALLOWEDCHARACTERS, checkAllowedCharacters);
	}

	public boolean getCheckCharacters() {
		return getBoolean(CHECKCHARACTERS);
	}

	public void setCheckCharacters(boolean checkCharacters) {
		setBoolean(CHECKCHARACTERS, checkCharacters);
	}

	public String getCharset() {
		return getString(CHARSET);
	}

	public void setCharset(String charset) {
		setString(CHARSET, charset);
	}

	public String getExtraCharsAllowed() {
		return getString(EXTRACHARSALLOWED);
	}

	public void setExtraCharsAllowed(String extraCharsAllowed) {
		setString(EXTRACHARSALLOWED, extraCharsAllowed);
	}

	public boolean getCheckStorageSize() {
		return getBoolean(CHECKSTORAGESIZE);
	}

	public void setCheckStorageSize(boolean checkStorageSize) {
		setBoolean(CHECKSTORAGESIZE, checkStorageSize);
	}

	public boolean getCheckMaxCharLength() {
		return getBoolean(CHECKMAXCHARLENGTH);
	}

	public void setCheckMaxCharLength(boolean checkMaxCharLength) {
		setBoolean(CHECKMAXCHARLENGTH, checkMaxCharLength);
	}

	public int getMaxCharLengthBreak() {
		return getInteger(MAXCHARLENGTHBREAK);
	}

	public void setMaxCharLengthBreak(int maxCharLengthBreak) {
		setInteger(MAXCHARLENGTHBREAK, maxCharLengthBreak);
	}

	public int getMaxCharLengthAbove() {
		return getInteger(MAXCHARLENGTHABOVE);
	}

	public void setMaxCharLengthAbove(int maxCharLengthAbove) {
		setInteger(MAXCHARLENGTHABOVE, maxCharLengthAbove);
	}

	public int getMaxCharLengthBelow() {
		return getInteger(MAXCHARLENGTHBELOW);
	}

	public void setMaxCharLengthBelow(int maxCharLengthBelow) {
		setInteger(MAXCHARLENGTHBELOW, maxCharLengthBelow);
	}

	public boolean getCheckMinCharLength() {
		return getBoolean(CHECKMINCHARLENGTH);
	}

	public void setCheckMinCharLength(boolean checkMinCharLength) {
		setBoolean(CHECKMINCHARLENGTH, checkMinCharLength);
	}

	public int getMinCharLengthBreak() {
		return getInteger(MINCHARLENGTHBREAK);
	}

	public void setMinCharLengthBreak(int minCharLengthBreak) {
		setInteger(MINCHARLENGTHBREAK, minCharLengthBreak);
	}

	public int getMinCharLengthAbove() {
		return getInteger(MINCHARLENGTHABOVE);
	}

	public void setMinCharLengthAbove(int minCharLengthAbove) {
		setInteger(MINCHARLENGTHABOVE, minCharLengthAbove);
	}

	public int getMinCharLengthBelow() {
		return getInteger(MINCHARLENGTHBELOW);
	}

	public void setMinCharLengthBelow(int minCharLengthBelow) {
		setInteger(MINCHARLENGTHBELOW, minCharLengthBelow);
	}

	public boolean getCheckAbsoluteMaxCharLength() {
		return getBoolean(CHECKABSOLUTEMAXCHARLENGTH);
	}

	public void setCheckAbsoluteMaxCharLength(boolean checkAbsoluteMaxCharLength) {
		setBoolean(CHECKABSOLUTEMAXCHARLENGTH, checkAbsoluteMaxCharLength);
	}

	public int getAbsoluteMaxCharLength() {
		return getInteger(ABSOLUTEMAXCHARLENGTH);
	}

	public void setAbsoluteMaxCharLength(int absoluteMaxCharLength) {
		setInteger(ABSOLUTEMAXCHARLENGTH, absoluteMaxCharLength);
	}

	public boolean getDoubledWord() {
		return getBoolean(DOUBLEDWORD);
	}

	public void setDoubledWord(boolean doubledWord) {
		setBoolean(DOUBLEDWORD, doubledWord);
	}

	public String getDoubledWordExceptions() {
		return getString(DOUBLEDWORDEXCEPTIONS);
	}

	public void setDoubledWordExceptions(String doubledWordExceptions) {
		setString(DOUBLEDWORDEXCEPTIONS, doubledWordExceptions);
	}

	public boolean getSaveSession() {
		return getBoolean(SAVESESSION);
	}

	public void setSaveSession(boolean saveSession) {
		setBoolean(SAVESESSION, saveSession);
	}

	public String getSessionPath() {
		return getString(SESSIONPATH);
	}

	public void setSessionPath(String sessionPath) {
		setString(SESSIONPATH, sessionPath);
	}

	public String getOutputPath() {
		return getString(OUTPUTPATH);
	}

	public void setOutputPath(String outputPath) {
		setString(OUTPUTPATH, outputPath);
	}

	public int getOutputType() {
		return getInteger(OUTPUTTYPE);
	}

	public void setOutputType(int outputType) {
		setInteger(OUTPUTTYPE, outputType);
	}

	public boolean getAutoOpen() {
		return getBoolean(AUTOOPEN);
	}

	public void setAutoOpen(boolean autoOpen) {
		setBoolean(AUTOOPEN, autoOpen);
	}

	public boolean getLeadingWS() {
		return getBoolean(LEADINGWS);
	}

	public void setLeadingWS(boolean leadingWS) {
		setBoolean(LEADINGWS, leadingWS);
	}

	public boolean getTrailingWS() {
		return getBoolean(TRAILINGWS);
	}

	public void setTrailingWS(boolean trailingWS) {
		setBoolean(TRAILINGWS, trailingWS);
	}

	public boolean getEmptyTarget() {
		return getBoolean(EMPTYTARGET);
	}

	public void setEmptyTarget(boolean emptyTarget) {
		setBoolean(EMPTYTARGET, emptyTarget);
	}

	public boolean getEmptySource() {
		return getBoolean(EMPTYSOURCE);
	}

	public void setEmptySource(boolean emptySource) {
		setBoolean(EMPTYSOURCE, emptySource);
	}

	public boolean getTargetSameAsSource() {
		return getBoolean(TARGETSAMEASSOURCE);
	}

	public void setTargetSameAsSource(boolean targetSameAsSource) {
		setBoolean(TARGETSAMEASSOURCE, targetSameAsSource);
	}

	public boolean getTargetSameAsSourceForSameLanguage() {
		return getBoolean(TARGETSAMEASSOURCE_FORSAMELANGUAGE);
	}

	public void setTargetSameAsSourceForSameLanguage(boolean targetSameAsSourceForSameLanguage) {
		setBoolean(TARGETSAMEASSOURCE_FORSAMELANGUAGE, targetSameAsSourceForSameLanguage);
	}

	public boolean getTargetSameAsSourceWithCodes() {
		return getBoolean(TARGETSAMEASSOURCE_WITHCODES);
	}

	public void setTargetSameAsSourceWithCodes(boolean targetSameAsSourceWithCodes) {
		setBoolean(TARGETSAMEASSOURCE_WITHCODES, targetSameAsSourceWithCodes);
	}

	public boolean getCodeDifference() {
		return getBoolean(CODEDIFFERENCE);
	}

	public void setCodeDifference(boolean codeDifference) {
		setBoolean(CODEDIFFERENCE, codeDifference);
	}

	public boolean getGuessOpenClose() {
		return getBoolean(GUESSOPENCLOSE);
	}

	public void setGuessOpenClose(boolean guessOpenClose) {
		setBoolean(GUESSOPENCLOSE, guessOpenClose);
	}

	public boolean getCheckXliffSchema() {
		return getBoolean(CHECKXLIFFSCHEMA);
	}

	public void setCheckXliffSchema(boolean schema) {
		setBoolean(CHECKXLIFFSCHEMA, schema);
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

	public boolean getCheckWithLT() {
		return getBoolean(CHECKWITHLT);
	}

	public void setCheckWithLT(boolean checkWithLT) {
		setBoolean(CHECKWITHLT, checkWithLT);
	}

	public String getServerURL() {
		return getString(SERVERURL);
	}

	public void setServerURL(String serverURL) {
		setString(SERVERURL, serverURL);
	}

	public boolean getTranslateLTMsg() {
		return getBoolean(TRANSLATELTMSG);
	}

	public void setTranslateLTMsg(boolean translateLTMsg) {
		setBoolean(TRANSLATELTMSG, translateLTMsg);
	}

	public boolean getLtBilingualMode() {
		return getBoolean(LTBILINGUALMODE);
	}

	public void setLtBilingualMode(boolean ltBilingualMode) {
		setBoolean(LTBILINGUALMODE, ltBilingualMode);
	}

	public String getLtTranslationSource() {
		return getString(LTTRANSLATIONSOURCE);
	}

	public void setLtTranslationSource(String ltTranslationSource) {
		setString(LTTRANSLATIONSOURCE, ltTranslationSource);
	}

	public String getLtTranslationTarget() {
		return getString(LTTRANSLATIONTARGET);
	}

	public void setLtTranslationTarget(String ltTranslationTarget) {
		setString(LTTRANSLATIONTARGET, ltTranslationTarget);
	}

	public String getLtTranslationServiceKey() {
		return getString(LTTRANSLATIONSERVICEKEY);
	}

	public void setLtTranslationServiceKey(String ltTranslationServiceKey) {
		setString(LTTRANSLATIONSERVICEKEY, ltTranslationServiceKey);
	}

	public boolean getCheckTerms() {
		return getBoolean(CHECKTERMS);
	}

	public void setCheckTerms(boolean checkTerms) {
		setBoolean(CHECKTERMS, checkTerms);
	}

	public boolean getCheckBlacklist() {
		return getBoolean(CHECKBLACKLIST);
	}
	
	public boolean getAllowBlacklistSub() {
		return getBoolean(ALLOWBLACKLISTSUB);
	}
	
	public boolean getBlacklistSrc() {
		return getBoolean(BLACKLISTSRC);
	}

	public void setCheckBlacklist(boolean checkBlacklist) {
		setBoolean(CHECKBLACKLIST, checkBlacklist);
	}
	
	public void setAllowBlacklistSub(boolean allowBlacklistSub) {
		setBoolean(ALLOWBLACKLISTSUB, allowBlacklistSub);
	}
	
	public void setBlacklistSrc(boolean blacklistSrc) {
		setBoolean(BLACKLISTSRC, blacklistSrc);
	}

	public boolean getStringMode() {
		return getBoolean(STRINGMODE);
	}

	public void setStringMode(boolean stringMode) {
		setBoolean(STRINGMODE, stringMode);
	}

	public boolean getBetweenCodes() {
		return getBoolean(BETWEENCODES);
	}

	public void setBetweenCodes(boolean betweenCodes) {
		setBoolean(BETWEENCODES, betweenCodes);
	}
	
	public boolean getShowFullPath() {
		return getBoolean(SHOWFULLPATH);
	}

	public void setShowFullPath(boolean showFullPath) {
		setBoolean(SHOWFULLPATH, showFullPath);
	}

	@ReferenceParameter
	public String getTermsPath() {
		return getString(TERMSPATH);
	}

	public void setTermsPath(String termsPath) {
		setString(TERMSPATH, termsPath);
	}

	@ReferenceParameter
	public String getBlacklistPath() {
		return getString(BLACKLISTPATH);
	}

	public void setblacklistPath(String blacklistPath) {
		setString(BLACKLISTPATH, blacklistPath);
	}

	public InputStream getBlacklistStream() {
		return blacklistStream;
	}

	public void setBlacklistStream(InputStream blacklistStream) {
		this.blacklistStream = blacklistStream;
	}

	@Override
	public void reset() {
		super.reset();
		setOutputPath(Util.ROOT_DIRECTORY_VAR + "/qa-report.html");
		setOutputType(0);
		setAutoOpen(true);
		setLeadingWS(true);
		setTrailingWS(true);
		setEmptyTarget(true);
		setEmptySource(true);
		setTargetSameAsSource(true);
		setTargetSameAsSourceForSameLanguage(true);
		setTargetSameAsSourceWithCodes(true);
		setCodeDifference(true);
		setGuessOpenClose(true);
		setCheckXliffSchema(true);
		setCheckPatterns(true);
		setCheckWithLT(false);
		setServerURL("http://localhost:8081/");
		setTranslateLTMsg(false);
		setLtBilingualMode(false);
		setLtTranslationSource("");
		setLtTranslationTarget("en");
		setLtTranslationServiceKey("");
		setSaveSession(true);
		setSessionPath(Util.ROOT_DIRECTORY_VAR + "/qa-session" + QualityCheckSession.FILE_EXTENSION);
		setDoubledWord(true);
		setDoubledWordExceptions("sie;vous;nous");
		setCorruptedCharacters(true);
		setScope(SCOPE_ALL);

		setCheckMaxCharLength(true);
		setMaxCharLengthBreak(20);
		setMaxCharLengthAbove(200);
		setMaxCharLengthBelow(350);
		setCheckMinCharLength(true);
		setMinCharLengthBreak(20);
		setMinCharLengthAbove(45);
		setMinCharLengthBelow(30);

		setCheckStorageSize(true);
		
		setCheckAbsoluteMaxCharLength(false);
		setAbsoluteMaxCharLength(255);
		
		setCheckAllowedCharacters(true);
		setCheckCharacters(false);
		setCharset("ISO-8859-1");
		setExtraCharsAllowed("");
		
		setCheckTerms(false);
		setTermsPath("");
		setStringMode(false);
		setBetweenCodes(false);
		setShowFullPath(true);
		setCheckBlacklist(false);
		setAllowBlacklistSub(false);
		setBlacklistSrc(false);
		setblacklistPath("");
		setBlacklistStream(null);

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
		
		extraCodesAllowed = new ArrayList<String>();
		missingCodesAllowed = new ArrayList<String>();
		
		setTypesToIgnore("mrk;x-df-s;");
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

		// Allowed extra codes
		count = buffer.getInteger(EXTRACODESALLOWED, 0);
		if (count > 0) {
			extraCodesAllowed.clear();
		}
		for (int i = 0; i < count; i++) {
			extraCodesAllowed.add(buffer.getString(String.format("%s%d", EXTRACODESALLOWED, i), ""));
		}
		// Allowed missing codes
		count = buffer.getInteger(MISSINGCODESALLOWED, 0);
		if (count > 0) {
			missingCodesAllowed.clear();
		}
		for (int i = 0; i < count; i++) {
			missingCodesAllowed.add(buffer.getString(String.format("%s%d", MISSINGCODESALLOWED, i), ""));
		}

	}

	@Override
	public void fromString(String data, boolean clearParameters) {
		super.fromString(data, clearParameters);

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

		// Allowed extra codes
		count = buffer.getInteger(EXTRACODESALLOWED, 0);
		if (count > 0) {
			extraCodesAllowed.clear();
		}
		for (int i = 0; i < count; i++) {
			extraCodesAllowed.add(buffer.getString(String.format("%s%d", EXTRACODESALLOWED, i), ""));
		}
		// Allowed missing codes
		count = buffer.getInteger(MISSINGCODESALLOWED, 0);
		if (count > 0) {
			missingCodesAllowed.clear();
		}
		for (int i = 0; i < count; i++) {
			missingCodesAllowed.add(buffer.getString(String.format("%s%d", MISSINGCODESALLOWED, i), ""));
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

		buffer.setInteger(EXTRACODESALLOWED, extraCodesAllowed.size());
		for (int i = 0; i < extraCodesAllowed.size(); i++) {
			buffer.setString(String.format("%s%d", EXTRACODESALLOWED, i), extraCodesAllowed.get(i));
		}
		buffer.setInteger(MISSINGCODESALLOWED, missingCodesAllowed.size());
		for (int i = 0; i < missingCodesAllowed.size(); i++) {
			buffer.setString(String.format("%s%d", MISSINGCODESALLOWED, i), missingCodesAllowed.get(i));
		}
		
		return super.toString();
	}
}
