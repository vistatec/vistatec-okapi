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

package net.sf.okapi.filters.openxml;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.exceptions.OkapiException;

public class ConditionalParameters extends StringParameters {

	private static final String TRANSLATEDOCPROPERTIES = "bPreferenceTranslateDocProperties";
	private static final String TRANSLATECOMMENTS = "bPreferenceTranslateComments";
	private static final String AGGRESSIVECLEANUP = "bPreferenceAggressiveCleanup";
	private static final String AUTOMATICALLY_ACCEPT_REVISIONS = "bPreferenceAutomaticallyAcceptRevisions";
	private static final String TRANSLATEPOWERPOINTNOTES = "bPreferenceTranslatePowerpointNotes";
	private static final String TRANSLATEPOWERPOINTMASTERS = "bPreferenceTranslatePowerpointMasters";
	private static final String IGNOREPLACEHOLDERSINPOWERPOINTMASTERS = "bPreferenceIgnorePlaceholdersInPowerpointMasters";
	private static final String TRANSLATEWORDHEADERSFOOTERS = "bPreferenceTranslateWordHeadersFooters";
	private static final String TRANSLATEWORDHIDDEN = "bPreferenceTranslateWordHidden";
    // DWH 6-12-09 don't translate text in Excel in some colors
	private static final String TRANSLATEEXCELEXCLUDECOLORS = "bPreferenceTranslateExcelExcludeColors";
    // DWH 6-12-09 don't translate text in Excel in some specified cells
	private static final String TRANSLATEEXELEXCLUDECOLUMNS = "bPreferenceTranslateExcelExcludeColumns";
	private static final String TRANSLATEEXELSHEETNAMES = "bPreferenceTranslateExcelSheetNames";

	private static final String TRANSLATE_EXCEL_DIAGRAM_DATA = "bPreferenceTranslateExcelDiagramData";

	private static final String TRANSLATE_EXCEL_DRAWINGS = "bPreferenceTranslateExcelDrawings";

	// excludes pic:cnvpr and wp:docpr
	private static final String TRANSLATEWORDEXCLUDEGRAPHICMETADATA =
			"bPreferenceTranslateWordExcludeGraphicMetaData";
	private static final String TRANSLATEEXCELHIDDEN = "bPreferenceTranslateExcelHidden";
	private static final String EXTRACT_EXTERNAL_HYPERLINKS = "bExtractExternalHyperlinks";

	/**
	 * Add a \t character after {@code <w:tab/>} or {@code <a:tab/>}. This property is supported by
	 * the Word and Powerpoint filter.
	 */
	private static final String ADDTABASCHARACTER = "bPreferenceAddTabAsCharacter";

	/**
	 * Replace linebreak elements {@code <w:br/>} or {@code <a:br/>} by a character. This character
	 * is \n by default but can be changed by setting config parameter
	 * {@link #LINESEPARATORREPLACEMENT}. This property is supported by the Word and Powerpoint
	 * filter.
	 */
	private static final String ADDLINESEPARATORASCHARACTER =
			"bPreferenceAddLineSeparatorAsCharacter";

	/**
	 * The replacement character for linebreaks if {@link #ADDLINESEPARATORASCHARACTER} is set to
	 * {@code true}.
 	 */
	private static final String LINESEPARATORREPLACEMENT = "sPreferenceLineSeparatorReplacement";

	private static final String REPLACE_NO_BREAK_HYPHEN_TAG =
			"bPreferenceReplaceNoBreakHyphenTag";

	private static final String IGNORE_SOFT_HYPHEN_TAG =
			"bPreferenceIgnoreSoftHyphenTag";

	/**
	 * Powerpoint: Only include slides defined by {@link #tsPowerpointIncludedSlideNumbers}?
	 */
	private static final String POWERPOINT_INCLUDED_SLIDE_NUMBERS_ONLY = "bPreferencePowerpointIncludedSlideNumbersOnly";

	public final static int MSWORD=1;

	public TreeSet<String> tsComplexFieldDefinitionsToExtract;
	public TreeSet<String> tsExcelExcludedColors; // exclude if bPreferenceTranslateExcelExcludeColors
	public TreeSet<String> tsExcelExcludedColumns; // exclude if bPreferenceTranslateExcelExcludeCells
	public TreeSet<String> tsExcludeWordStyles;

	/**
	 * Slide numbers of those slides that should be extracted. The set is 1-based (not 0-based) for
	 * better readability, i.e. 1 ist the first slide. Only considered if
	 * {@link #POWERPOINT_INCLUDED_SLIDE_NUMBERS_ONLY} was set to {@code true}.
	 */
	public TreeSet<Integer> tsPowerpointIncludedSlideNumbers;

	// Not serialized, this is state that is stashed in the parameters as a hack.
	public ParseType nFileType=ParseType.MSWORD; // DWH 6-27-09

	public ConditionalParameters () {
		super();
	}

	public boolean getTranslateDocProperties() {
		return getBoolean(TRANSLATEDOCPROPERTIES);
	}

	public void setTranslateDocProperties(boolean translateDocProperties) {
		setBoolean(TRANSLATEDOCPROPERTIES, translateDocProperties);
	}

	public boolean getTranslateComments() {
		return getBoolean(TRANSLATECOMMENTS);
	}

	public void setTranslateComments(boolean translateComments) {
		setBoolean(TRANSLATECOMMENTS, translateComments);
	}

	public boolean getCleanupAggressively() {
		return getBoolean(AGGRESSIVECLEANUP);
	}

	public void setCleanupAggressively(boolean aggressiveCleanup) {
		setBoolean(AGGRESSIVECLEANUP, aggressiveCleanup);
	}

	public boolean getAutomaticallyAcceptRevisions() {
		return getBoolean(AUTOMATICALLY_ACCEPT_REVISIONS);
	}

	public void setAutomaticallyAcceptRevisions(boolean automaticallyAcceptRevisions) {
		setBoolean(AUTOMATICALLY_ACCEPT_REVISIONS, automaticallyAcceptRevisions);
	}

	public boolean getTranslatePowerpointNotes() {
		return getBoolean(TRANSLATEPOWERPOINTNOTES);
	}

	public void setTranslatePowerpointNotes(boolean translatePowerpointNotes) {
		setBoolean(TRANSLATEPOWERPOINTNOTES, translatePowerpointNotes);
	}

	public boolean getTranslatePowerpointMasters() {
		return getBoolean(TRANSLATEPOWERPOINTMASTERS);
	}

	public void setTranslatePowerpointMasters(boolean translatePowerpointMasters) {
		setBoolean(TRANSLATEPOWERPOINTMASTERS, translatePowerpointMasters);
	}

	public void setIgnorePlaceholdersInPowerpointMasters(boolean ignorePlaceholdersInPowerpointMasters) {
		setBoolean(IGNOREPLACEHOLDERSINPOWERPOINTMASTERS, ignorePlaceholdersInPowerpointMasters);
	}

	public boolean getIgnorePlaceholdersInPowerpointMasters() {
		return getBoolean(IGNOREPLACEHOLDERSINPOWERPOINTMASTERS);
	}

	public boolean getTranslateWordHeadersFooters() {
		return getBoolean(TRANSLATEWORDHEADERSFOOTERS);
	}

	public void setTranslateWordHeadersFooters(boolean translateWordHeadersFooters) {
		setBoolean(TRANSLATEWORDHEADERSFOOTERS, translateWordHeadersFooters);
	}

	public boolean getTranslateWordHidden() {
		return getBoolean(TRANSLATEWORDHIDDEN);
	}

	public void setTranslateWordHidden(boolean translateWordHidden) {
		setBoolean(TRANSLATEWORDHIDDEN, translateWordHidden);
	}

	/**
	 * Return true if we should translate hidden cells and columns, false (default)
	 * if we should not.  Note that this setting has no affect on cells that are excluded
	 * from translation due to color or by column name in the configuration.
	 * @return true if we should translate hidden cells and columns
	 */
	public boolean getTranslateExcelHidden() {
		return getBoolean(TRANSLATEEXCELHIDDEN);
	}

	public void setTranslateExcelHidden(boolean translateExcelHidden) {
		setBoolean(TRANSLATEEXCELHIDDEN, translateExcelHidden);
	}

	public boolean getTranslateExcelExcludeColors() {
		return getBoolean(TRANSLATEEXCELEXCLUDECOLORS);
	}

	public void setTranslateExcelExcludeColors(boolean translateExcelExcludeColors) {
		setBoolean(TRANSLATEEXCELEXCLUDECOLORS, translateExcelExcludeColors);
	}

	public boolean getTranslateExcelExcludeColumns() {
		return getBoolean(TRANSLATEEXELEXCLUDECOLUMNS);
	}

	public void setTranslateExcelExcludeColumns(boolean translateExcelExcludeColumns) {
		setBoolean(TRANSLATEEXELEXCLUDECOLUMNS, translateExcelExcludeColumns);
	}

	public boolean getTranslateExcelSheetNames() {
		return getBoolean(TRANSLATEEXELSHEETNAMES);
	}

	public void setTranslateExcelSheetNames(boolean translateExcelSheetNames) {
		setBoolean(TRANSLATEEXELSHEETNAMES, translateExcelSheetNames);
	}

	public boolean getTranslateExcelDiagramData() {
		return getBoolean(TRANSLATE_EXCEL_DIAGRAM_DATA);
	}

	public void setTranslateExcelDiagramData(boolean translateExcelDiagramData) {
		setBoolean(TRANSLATE_EXCEL_DIAGRAM_DATA, translateExcelDiagramData);
	}

	public boolean getTranslateExcelDrawings() {
		return getBoolean(TRANSLATE_EXCEL_DRAWINGS);
	}

	public void setTranslateExcelDrawings(boolean translateExcelDrawings) {
		setBoolean(TRANSLATE_EXCEL_DRAWINGS, translateExcelDrawings);
	}

	public boolean getTranslateWordExcludeGraphicMetaData() {
		return getBoolean(TRANSLATEWORDEXCLUDEGRAPHICMETADATA);
	}

	public void setTranslateWordExcludeGraphicMetaData(boolean excludeGraphicMetaData) {
		setBoolean(TRANSLATEWORDEXCLUDEGRAPHICMETADATA, excludeGraphicMetaData);
	}

	public boolean getAddTabAsCharacter() {
		return getBoolean(ADDTABASCHARACTER);
	}
	public void setAddTabAsCharacter(boolean bAddTabAsCharacter) {
		setBoolean(ADDTABASCHARACTER, bAddTabAsCharacter);
	}
	public boolean getAddLineSeparatorCharacter() {
		return getBoolean(ADDLINESEPARATORASCHARACTER);
	}
	public void setAddLineSeparatorCharacter(boolean bAddLineSeparatorAsCharacter) {
		setBoolean(ADDLINESEPARATORASCHARACTER, bAddLineSeparatorAsCharacter);
	}


	public char getLineSeparatorReplacement() {
		return getString(LINESEPARATORREPLACEMENT).charAt(0);
	}
	public void setLineSeparatorReplacement(char lineSeparatorReplacement) {
		setString(LINESEPARATORREPLACEMENT, String.valueOf(lineSeparatorReplacement));
	}

	public boolean getReplaceNoBreakHyphenTag(){
		return getBoolean(REPLACE_NO_BREAK_HYPHEN_TAG);
	}
	public void setReplaceNoBreakHyphenTag(boolean bReplaceNoBreakHyphenTag) {
		setBoolean(REPLACE_NO_BREAK_HYPHEN_TAG, bReplaceNoBreakHyphenTag);
	}

	public boolean getIgnoreSoftHyphenTag(){
		return getBoolean(IGNORE_SOFT_HYPHEN_TAG);
	}
	public void setIgnoreSoftHyphenTag(boolean bIgnoreSoftHyphenTag) {
		setBoolean(IGNORE_SOFT_HYPHEN_TAG, bIgnoreSoftHyphenTag);
	}

	public void setExtractExternalHyperlinks(boolean bExtractExternalHyperlinks) {
		setBoolean(EXTRACT_EXTERNAL_HYPERLINKS, bExtractExternalHyperlinks);
	}

	public boolean getExtractExternalHyperlinks() {
		return getBoolean(EXTRACT_EXTERNAL_HYPERLINKS);
	}

	public void setPowerpointIncludedSlideNumbersOnly(boolean bIncludedSlideNumbersOnly) {
		setBoolean(POWERPOINT_INCLUDED_SLIDE_NUMBERS_ONLY, bIncludedSlideNumbersOnly);
	}

	public boolean getPowerpointIncludedSlideNumbersOnly() {
		return getBoolean(POWERPOINT_INCLUDED_SLIDE_NUMBERS_ONLY);
	}

	public void reset () {
		super.reset();
		setTranslateDocProperties(true); // Word, Powerpoint, Excel Doc Properties
		setTranslateComments(true); // Word, Powerpoint, Excel Comments
		setTranslatePowerpointNotes(true); // Powerpoint Notes
		setTranslatePowerpointMasters(true); // Powerpoint Masters
		setIgnorePlaceholdersInPowerpointMasters(false); // Only shapes on masters without nvPr > ph
		setTranslateWordHeadersFooters(true); // Word Headers and Footers
		setTranslateWordHidden(false); // Word Hidden text
		setTranslateWordExcludeGraphicMetaData(false); // Word graphic metadata
		setTranslateExcelExcludeColors(false); // Excel exclude tsExcelExcludedColors
		setTranslateExcelExcludeColumns(false); // Excel exclude specific cells
		setTranslateExcelSheetNames(false); // Excel exclude sheet names
		setAddLineSeparatorCharacter(false);
		setLineSeparatorReplacement('\n');
		setReplaceNoBreakHyphenTag(false);
		setIgnoreSoftHyphenTag(false);
		setAddTabAsCharacter(false);
		setCleanupAggressively(false);
		setAutomaticallyAcceptRevisions(true);
		setPowerpointIncludedSlideNumbersOnly(false); // Powerpoint: Include tsPowerpointIncludeSlideNumbers only
		setTranslateExcelDiagramData(false);
        setTranslateExcelDrawings(false);
        tsComplexFieldDefinitionsToExtract = new TreeSet<>(); // exclude if bPreferenceTranslateExcelExcludeColors
        tsComplexFieldDefinitionsToExtract.add("HYPERLINK");
		tsExcelExcludedColors = new TreeSet<>(); // exclude if bPreferenceTranslateExcelExcludeColors
		tsExcelExcludedColumns = new TreeSet<>(); // exclude if bPreferenceTranslateExcelExcludeCells
		tsExcludeWordStyles = new TreeSet<>();
		tsPowerpointIncludedSlideNumbers = new TreeSet<>();
	}

	public void fromString (String data) {
		super.fromString(data);

		int i,siz;
		String sNummer;

		siz = buffer.getInteger("tsComplexFieldDefinitionsToExtract");
		if (siz > 0) {
			tsComplexFieldDefinitionsToExtract = new TreeSet<>();
			for(i=0;i<siz;i++)
			{
				sNummer = "cfd"+i;
				tsComplexFieldDefinitionsToExtract.add(buffer.getString(sNummer, ""));
			}
		}

		tsExcelExcludedColors = new TreeSet<>();
		siz = buffer.getInteger("tsExcelExcludedColors");
		for(i=0;i<siz;i++)
		{
			sNummer = "ccc"+i;
			tsExcelExcludedColors.add(buffer.getString(sNummer, "F1F2F3F4"));
		}

		tsExcelExcludedColumns = new TreeSet<>();
		siz = buffer.getInteger("tsExcelExcludedColumns");
		for(i=0;i<siz;i++)
			tsExcelExcludedColumns.add(buffer.getString("zzz"+i, "A1000"));

		tsExcludeWordStyles = new TreeSet<>();
		siz = buffer.getInteger("tsExcludeWordStyles");
		for(i=0;i<siz;i++)
			tsExcludeWordStyles.add(buffer.getString("sss"+i, "zzzzz"));

		tsPowerpointIncludedSlideNumbers = new TreeSet<>();
		siz = buffer.getInteger("tsPowerpointIncludedSlideNumbers");
		for(i=0;i<siz;i++)
			tsPowerpointIncludedSlideNumbers.add(buffer.getInteger("sln"+i, 1));
	}

	@Override
	public String toString ()
	{
		int i,siz;
		Iterator<String> it;

		if (tsComplexFieldDefinitionsToExtract==null)
			siz = 0;
		else
			siz = tsComplexFieldDefinitionsToExtract.size();
		buffer.setInteger("tsComplexFieldDefinitionsToExtract", siz);
		for(i=0,it=tsComplexFieldDefinitionsToExtract.iterator();i<siz && it.hasNext();i++)
		{
			buffer.setString("cfd"+i, it.next());
		}

		if (tsExcelExcludedColors==null)
			siz = 0;
		else
			siz = tsExcelExcludedColors.size();
		buffer.setInteger("tsExcelExcludedColors", siz);
		for(i=0,it=tsExcelExcludedColors.iterator();i<siz && it.hasNext();i++)
		{
			buffer.setString("ccc"+i, it.next());
		}

		if (tsExcelExcludedColumns==null)
			siz = 0;
		else
			siz = tsExcelExcludedColumns.size();
		buffer.setInteger("tsExcelExcludedColumns", siz);
		for(i=0,it=tsExcelExcludedColumns.iterator();i<siz && it.hasNext();i++)
		{
			buffer.setString("zzz"+i, it.next());
		}

		if (tsExcludeWordStyles==null)
			siz = 0;
		else
			siz = tsExcludeWordStyles.size();
		buffer.setInteger("tsExcludeWordStyles", siz);
		for(i=0,it=tsExcludeWordStyles.iterator();i<siz && it.hasNext();i++)
		{
			buffer.setString("sss"+i, it.next());
		}

		if (tsPowerpointIncludedSlideNumbers==null)
			siz = 0;
		else
			siz = tsPowerpointIncludedSlideNumbers.size();
		buffer.setInteger("tsPowerpointIncludedSlideNumbers", siz);
		Iterator<Integer> slideIterator;
		for(i=0,slideIterator=tsPowerpointIncludedSlideNumbers.iterator();i<siz && slideIterator.hasNext();i++)
		{
			buffer.setInteger("sln"+i, slideIterator.next());
		}

		return buffer.toString();
	}

	public void save (String newPath) {
		Writer SW = null;
		try {
			// Save the fields on file
			SW = new OutputStreamWriter(
				new BufferedOutputStream(new FileOutputStream(newPath)),
				"UTF-8");
			SW.write(toString());
			path = newPath;
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
		finally {
			if ( SW != null )
				try { SW.close(); } catch ( IOException e ) {};
		}
	}

	public void load (URI inputURI,
			boolean p_bIgnoreErrors)
		{
			char[] aBuf;
			try {
				// Reset the parameters to their defaults
				reset();
				// Open the file. use a URL so we can do openStream() and load
				// predefined files from JARs.
				URL url = inputURI.toURL();
				Reader SR = new InputStreamReader(
					new BufferedInputStream(url.openStream()), "UTF-8");

				// Read the file in one string
				StringBuilder sbTmp = new StringBuilder(1024);
				aBuf = new char[1024];
				int nCount;
				while ((nCount = SR.read(aBuf)) > -1) {
					sbTmp.append(aBuf, 0, nCount);
				}
				SR.close();
				SR = null;

				// Parse it
				String tmp = sbTmp.toString().replace("\r\n", "\n");
				fromString(tmp.replace("\r", "\n"));
				path = inputURI.getPath();
			}
			catch ( IOException e ) {
				if ( !p_bIgnoreErrors ) throw new OkapiException(e);
			}
			finally {
				aBuf = null;
			}
		}

	public ConditionalParameters clone()
	{
		ConditionalParameters cpnew = new ConditionalParameters();
		cpnew.fromString(this.toString()); // copy values from current ConditionalParameters to new one
		cpnew.nFileType = nFileType; // nFileType is not currently part of toString and fromString 
		return cpnew;
	}

	/**
	 * Extract the subset of the excluded columns that are relevant for this
	 * sheet.  Strip the numerical prefix and just return a set of column names.
	 * The column excludes are stored as a set of patterns "NX", where
	 * - N = 1, 2, or 3, corresponding to "1st sheet", "2nd sheet", and "3rd+ sheet"
	 * - X = a column identified ("A", "BB", etc)
	 * @param sheetNumber the sheet number to extract
	 * @return all the subset of the excluded columns that are relevant for this sheet
	 */
	public Set<String> findExcludedColumnsForSheetNumber(int sheetNumber) {
		if (!getTranslateExcelExcludeColumns()) {
			return Collections.emptySet();
		}
		Set<String> excludes = new HashSet<String>();
		if (sheetNumber > 3) {
			sheetNumber = 3;
		}
		String prefix = String.valueOf(sheetNumber);
		for (String origExclude : tsExcelExcludedColumns) {
			if (origExclude.startsWith(prefix)) {
				excludes.add(origExclude.substring(prefix.length()));
			}
		}
		return excludes;
	}
}
