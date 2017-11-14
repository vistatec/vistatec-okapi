/*===========================================================================
  Copyright (C) 2008-2016 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.applications.rainbow.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;

import net.sf.okapi.applications.rainbow.utilities.BaseUtility;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;

import org.eclipse.swt.widgets.Text;

public class Utils {

	static public String escapeToRTF (String p_sText,
		boolean p_bConvertLineBreaks,
		int p_nLineBreakStyle,
		Charset p_Enc)
	{
		if ( p_sText == null ) return "";
		StringBuffer sbTmp = new StringBuffer(p_sText.length());
		for ( int i=0; i<p_sText.length(); i++ ) {
			switch ( p_sText.charAt(i) ) {
				case '{':
				case '}':
				case '\\':
					sbTmp.append('\\');
					sbTmp.append(p_sText.charAt(i));
					continue;

				case '\r':
					//TODO: Fix case when input is Mac
					break;

				case '\n':
					if ( p_bConvertLineBreaks )
					{
						switch ( p_nLineBreakStyle )
						{
							case 1: // Outside external
								sbTmp.append(RTFStyle.ENDCODE);
								sbTmp.append("\r\n\\par ");
								sbTmp.append(RTFStyle.STARTCODE);
								continue;
							case 2:
								sbTmp.append(RTFStyle.ENDINLINE);
								sbTmp.append("\r\n\\par ");
								sbTmp.append(RTFStyle.STARTINLINE);
								continue;
							case 0: // Just convert
							default:
								sbTmp.append("\r\n\\par ");
								continue;
						}
					}
					else sbTmp.append(p_sText.charAt(i));
					continue;

				case '\u00a0': // Non-breaking space
					sbTmp.append("\\~"); // No extra space (it's a control word)
					break;

				case '\t':
					sbTmp.append("\\tab ");
					break;
				case '\u2022':
					sbTmp.append("\\bullet ");
					break;
				case '\u2018':
					sbTmp.append("\\lquote ");
					break;
				case '\u2019':
					sbTmp.append("\\rquote ");
					break;
				case '\u201c':
					sbTmp.append("\\ldblquote ");
					break;
				case '\u201d':
					sbTmp.append("\\rdblquote ");
					break;
				case '\u2013':
					sbTmp.append("\\endash ");
					break;
				case '\u2014':
					sbTmp.append("\\emdash ");
					break;
				case '\u200d':
					sbTmp.append("\\zwj ");
					break;
				case '\u200c':
					sbTmp.append("\\zwnj ");
					break;
				case '\u200e':
					sbTmp.append("\\ltrmark ");
					break;
				case '\u200f':
					sbTmp.append("\\rtlmark ");
					break;

				default:
					if ( p_sText.codePointAt(i) > 127 )
					{
						//TODO: fix this. limit() is not the length!
						ByteBuffer bBuf = p_Enc.encode(Integer.toString(p_sText.codePointAt(i)));
						if ( bBuf.limit() > 1 )
						{
							sbTmp.append(String.format("{{\\uc%1$d", bBuf.limit()));
							sbTmp.append(String.format("\\u%1$d", p_sText.codePointAt(i)));
							for ( int b=0; b<bBuf.limit(); b++ )
								sbTmp.append(String.format("\\'%1$x", bBuf.getChar(b)));
							sbTmp.append("}");
						}
						else
						{
							sbTmp.append(String.format("\\u%1$d", p_sText.codePointAt(i)));
							sbTmp.append(String.format("\\'%1$x", bBuf.getChar(0)));
						}
					}
					else sbTmp.append(p_sText.charAt(i));
					continue;
			}
		}
		return sbTmp.toString();
	}
	
	static public int getPercentage (long p_nPart,
		long p_nTotal)
	{
		return (int)((float)p_nPart/(float)((p_nTotal==0)?1:p_nTotal)*100);
	}
	
	static public String getANSIEncoding (String p_sLanguage)
	{
		String sEncoding = "windows-1252";
/*TODO: getANSIEncoding
		// Fall back to an RTF-friendly encoding
		try
		{
			CultureInfo CI = new CultureInfo(p_sLanguage);
			int nCP = CI.TextInfo.ANSICodePage;
			sEncoding = Encoding.GetEncoding(nCP).WebName;
		}
		catch
		{
			// Use the user choice
			//TODO: handle error: give warning
		}
*/
		return sEncoding;
	}

	static public LocaleId getDefaultSourceLanguage () {
		// In most case the 'source' language is English
		// Even when we are on non-English machines
		return LocaleId.fromString("en-us");
	}
	
	static public LocaleId getDefaultTargetLanguage () {
		// Use the local language by default
		LocaleId lang = new LocaleId(Locale.getDefault());
		if ( lang.sameLanguageAs(getDefaultSourceLanguage()) ) {
			lang = LocaleId.fromString("fr-fr");
		}
		return lang;
	}

	static public String getCurrentLanguage () {
		String tmp1 = Locale.getDefault().getLanguage();
		String tmp2 = Locale.getDefault().getCountry();
		return (tmp1 + (tmp2.length()==0 ? "" : ("-"+tmp2))); 
	}
	
	static public String getOkapiSharedFolder (String rootFolder,
		boolean fromJar)
	{
		if ( fromJar ) {
			return rootFolder + File.separatorChar + "lib" + File.separator + "shared";
		}
		// Else: From maven build
		return rootFolder + File.separatorChar + "classes" + File.separator + "shared";
	}

	/*static public String getOkapiParametersFolder (String rootFolder) {
		return getOkapiSharedFolder(rootFolder) + File.separatorChar + "parameters";
	}*/

	/*
	 * Gets the Okapi Filter Parameters folder for a give type.
	 * @param p_nType Type of the folder to fetch: 0=System, 1=User, 2=Project
	 * @return The Filter Parameters folder for the given type (without a trailing separator).
	 *
	static public String getOkapiParametersFolder (String rootFolder,
		int p_nType)
	{
		String sTmp;
		switch ( p_nType ) {
		case 2: // Project folder
			// Check for the environment variable
			sTmp = System.getenv(PARAMETERS_PRJDIR);
			if (( sTmp != null ) && ( sTmp.length() > 0 ))
				return sTmp;
			// Else, fall through: use the User folder
		case 1: // User folder
			sTmp = System.getProperty("user.dir");
			sTmp = sTmp + File.separatorChar + "okapi"
				+ File.separatorChar + "parameters";
			return sTmp;
		case 0: // System folder
		default:
			return getOkapiParametersFolder(rootFolder);
		}
	}
*/
	static public String removeExtension (String p_sPath)
	{
		int n1 = p_sPath.lastIndexOf(File.separator);
        int n2 = p_sPath.lastIndexOf('.');
        if (( n2 > -1 ) && ( n1 < n2 )) {
        	return p_sPath.substring(0, n2);
        }
        return p_sPath;
	}
	
	
//	/**
//	 * Gets the extension of a path or file name.
//	 * @param p_sPath The path or file name.
//	 * @return The extension (with the period), or an empty string.
//	 */
//	static public String getExtension (String p_sPath)
//	{
//		int n1 = p_sPath.lastIndexOf(File.separator);
//        int n2 = p_sPath.lastIndexOf('.');
//        if (( n2 > -1 ) && ( n1 < n2 )) {
//        	return p_sPath.substring(n2);
//        }
//        return "";
//	}
	
	/**
	 * Tries to detect the encoding and optionally the line-break type of a given file.
	 * @param p_sPath the full path of the file.
	 * @return The detected encoding or null.
	 */
	public static String detectEncoding (String p_sPath) {
		// Set defaults
		FileInputStream IS = null;
		String encoding = null;
		try {
			// Opens the file
			IS = new FileInputStream(p_sPath);
			byte Buf[] = new byte[9];
			int nRead = IS.read(Buf, 0, 3);
			// Try to detect the encoding
			//TODO: add detection for UTF-32			
			if ( nRead > 1 ) {
				// Try to get detect the encoding values
				if (( Buf[0]==(byte)0xFE ) && ( Buf[1]==(byte)0xFF )) encoding = "UTF-16BE";
				if (( Buf[0]==(byte)0xFF ) && ( Buf[1]==(byte)0xFE )) encoding = "UTF-16LE";
				if ( nRead > 2 ) {
					if (( Buf[0]==(byte)0xEF ) && ( Buf[1]==(byte)0xBB ) && ( Buf[3]==(byte)0xBF ))
						encoding = "UTF-8";
				}
			}
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
		finally {
			if ( IS != null )
				try { IS.close(); } catch ( IOException e ){};
		}
		return encoding;
	}
	
	/**
	 * Checks if a string contain at least one occurrence of one of the characters
	 * listed in a string.
	 * @param p_sText Text to validate.
	 * @param p_sCharList List of characters to match against.
	 * @return (char)0 if no character is found, else the character found.
	 */
	public static char checksCharList (String p_sText,
		String p_sCharList) {
		if (( p_sCharList == null ) || ( p_sCharList.length() == 0 )) return (char)0;
		if (( p_sText == null ) || ( p_sText.length() == 0 )) return (char)0;
		for ( int i=0; i<p_sCharList.length(); i++ ) {
			if ( p_sText.indexOf(p_sCharList.charAt(i)) != -1 ) {
				return p_sCharList.charAt(i);
			}
		}
		return (char)0; // Does not contain any characters listed in p_sCharList 
	}

	public static void checkProjectDirAfterPick (String path,
		Text edField,
		String projectDir)
	{
		if ( path == null ) return;
		String oriPath = edField.getText().replace(BaseUtility.VAR_PROJDIR, projectDir);
		if ( !path.equals(oriPath) ) {
			edField.setText(path);
		}
		edField.selectAll();
		edField.setFocus();
	}
}
