/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.formatconversion;

import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;

public class TableFilterWriterParameters extends StringParameters {

	public static final String INLINE_ORIGINAL = "original";
	public static final String INLINE_TMX = "tmx";
	public static final String INLINE_XLIFF = "xliff";
	public static final String INLINE_XLIFFGX = "xliffgx";
	public static final String INLINE_GENERIC = "generic";
	
	static final String INLINEFORMAT = "inlineFormat";
	static final String USEDOUBLEQUOTES = "useDoubleQuotes";
	static final String SEPARATOR = "separator";
	
	public TableFilterWriterParameters () {
		super();
	}

	public String getInlineFormat () {
		return getString(INLINEFORMAT);
	}

	public void setInlineFormat (String inlineFormat) {
		setString(INLINEFORMAT, inlineFormat);
	}

	public boolean getUseDoubleQuotes () {
		return getBoolean(USEDOUBLEQUOTES);
	}

	public void setUseDoubleQuotes (boolean useDoubleQuotes) {
		setBoolean(USEDOUBLEQUOTES, useDoubleQuotes);
	}

	public String getSeparator () {
		return getString(SEPARATOR);
	}

	public void setSeparator (String separator) {
		setString(SEPARATOR, separator);
	}

	public void reset () {
		super.reset();
		setInlineFormat(INLINE_ORIGINAL);
		setUseDoubleQuotes(false);
		setSeparator("\t");
	}


//	@Override
//	public ParametersDescription getParametersDescription () {
//		ParametersDescription desc = new ParametersDescription(this);
//		desc.add(INLINEFORMAT, "Inline codes format", "Format of the inline codes");
//		return desc;
//	}
//
//	public EditorDescription createEditorDescription(ParametersDescription paramDesc) {
//		EditorDescription desc = new EditorDescription("Format Conversion", true, false);
//
//		String[] choices = {FORMAT_PO, FORMAT_TMX, FORMAT_TABLE, FORMAT_PENSIEVE};
//		String[] choicesLabels = {"PO File", "TMX Document", "Tab-Delimited Table", "Pensieve TM"};
//		ListSelectionPart lsp = desc.addListSelectionPart(paramDesc.get(OUTPUTFORMAT), choices);
//		lsp.setChoicesLabels(choicesLabels);
//		
//		return desc;
//	}

	/**
	 * Sets the parameters values from two strings in a special short format that can be used
	 * with command-line tools.
	 * @param format the format ("csv" or "tab")
	 * @param inlineCodes the format of the inline codes.
	 */
	public void fromArguments (String format,
		String inlineFormat)
	{
		if ( !Util.isEmpty(format) ) {
			if ( format.equals("csv") ) {
				setSeparator(",");
				setUseDoubleQuotes(true);
			}
			else if ( format.equals("tab") ) {
				setSeparator("\t");
				setUseDoubleQuotes(false);
			}
			else {
				throw new OkapiException(String.format("Invalid option '%s' in format options.", format));
			}
		}
		if ( !Util.isEmpty(inlineFormat) ) {
			if (( inlineFormat.equals(INLINE_GENERIC) )
				|| ( inlineFormat.equals(INLINE_TMX) )
				|| ( inlineFormat.equals(INLINE_XLIFF) )
				|| ( inlineFormat.equals(INLINE_XLIFFGX) )
				|| ( inlineFormat.equals(INLINE_ORIGINAL) ))
			{
				setInlineFormat(inlineFormat);
			}
			else {
				throw new OkapiException(String.format("Invalid option '%s' in codes options.", inlineFormat));
			}
		}
	}

}
