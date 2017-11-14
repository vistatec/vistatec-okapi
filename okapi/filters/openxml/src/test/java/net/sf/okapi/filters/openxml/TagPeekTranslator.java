/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

import java.util.List;

import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.TextFragment;
import static net.sf.okapi.filters.openxml.ParseType.*;

import org.slf4j.Logger;

/**
 * Implements ITranslator and modifies text to be translated to
 * show the tags the translator will see while translating.  This
 * is used in OpenXMLRoundTripTest, so the tags are shown in the
 * orignal file format.
 */

public class TagPeekTranslator extends AbstractTranslator {
	  // extends GenericSkeletonWriter because expandCodeContent is protected
	static final String CONS="BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz";
	static final String NUM="0123456789";
	static final String PUNC=" 	`~!#$%^&*()_+[{]}\\;:\",<.>?";
	static final String PUNCNL=" 	`~!#$%^&*()_+[{]}\\;:\",<.>?\u00f2\u00f3\u203a";
	static final String PUNCDNL="- 	`~!#$%^&*()_+[{]}\\;:\",<.>";
	static final String UPR="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static final String LWR="abcdefghijklmnopqrstuvwxyz";
	static final String wordon="<w:r><w:t>";
	static final String wordoff="</w:t></w:r>";
	static final String ppon="<a:r><a:t>";
	static final String ppoff="</a:t></a:r>";
	static final String lbrac="{";
	static final String rbrac="}";

	@Override
	public String translate(TextFragment tf, Logger LOGGER, ParseType nFileType)
	{
		String s = tf.getCodedText();
		String rslt=s,slow,sss;
		StringBuilder ss = new StringBuilder();
		int i,j,k,len,codenum;
		char carrot;
		int nSurroundingCodes=0; // DWH 4-8-09
		Code code;
		char ch;
		try
		{
			len = s.length();
			List<Code> codes = tf.getCodes();
			if (len>1)
			{
				for(i=0;i<len;i++)
				{
					ch = s.charAt(i);
					code = null;
					switch ( ch )
					{
						case TextFragment.MARKER_OPENING:
							sss = s.substring(i,i+2);
							codenum = TextFragment.toIndex(s.charAt(++i));
							code = codes.get(codenum);
							ss.append(sss + lbrac + "g" + codenum + rbrac);
							nSurroundingCodes++;
							break;
						case TextFragment.MARKER_CLOSING:
							sss = s.substring(i,i+2);
							codenum = TextFragment.toIndex(s.charAt(++i));
							code = codes.get(codenum);
							ss.append(lbrac + "/g" + codenum + rbrac + sss);
							nSurroundingCodes--;
							break;
						case TextFragment.MARKER_ISOLATED:
							sss = s.substring(i,i+2);
							codenum = TextFragment.toIndex(s.charAt(++i));
							code = codes.get(codenum);
							if (code.getTagType()==TextFragment.TagType.OPENING)
								nSurroundingCodes++;
							else if (code.getTagType()==TextFragment.TagType.CLOSING)
								nSurroundingCodes--;
							if (nSurroundingCodes>0)
							{
								if (nFileType==MSWORD)
								{
									ss.append(sss + "lbrac + x" + codenum + rbrac);
								}
								else if (nFileType==MSPOWERPOINT)
									ss.append(sss);
								else
									ss.append(sss);
							}
							else
							{
								if (nFileType==MSWORD)
								{
									if (code.getTagType()==TextFragment.TagType.OPENING)
										ss.append(wordon + lbrac + "x" + codenum + rbrac + wordoff + sss);
									else if (code.getTagType()==TextFragment.TagType.OPENING)
										ss.append(sss + wordon + lbrac + "x" + codenum + rbrac + wordoff);
									else
										ss.append(sss + lbrac + "x" + codenum + rbrac);
								}
								else if (nFileType==MSPOWERPOINT)
									ss.append(sss) /* + ppon + lbrac + codenum + rbrac + ppoff*/;
								else
									ss.append(sss);
							}
							break;
//TODO: Does it need to be implemented with new TextContainer?
//						case TextFragment.MARKER_SEGMENT:
//							sss = s.substring(i,i+2);
//							codenum = TextFragment.toIndex(s.charAt(++i));
//							code = codes.get(codenum);
//							if (code.getTagType()==TextFragment.TagType.OPENING)
//								nSurroundingCodes++;
//							else if (code.getTagType()==TextFragment.TagType.CLOSING)
//								nSurroundingCodes--;
//							ss += sss /* + lbrac + "y" + codenum + rbrac */;
//							break;
					}
					if (code!=null)
						continue;
					if (i+2<len && s.substring(i,i+3).equals("---"))
					{
						ss.append("---");
						i += 2;
					}
					else if (i+1<len && s.substring(i,i+2).equals("--"))
					{
						ss.append("--");
						i += 1;				
					}
					else
					{
						j = hominyOf(s.substring(i),NUM);
						if (j>0)
						{
							ss.append(s.substring(i,i+j));
							i += j-1;
							continue;
						}
						j = hominyOf(s.substring(i),CONS);
						if (j>0)
						{
							k = hominyLetters(s.substring(i+j));
							slow = s.substring(i,i+j).toLowerCase();
							if (k > -1)
							{
								ss.append(s.substring(i+j,i+j+k));
								i += k;
							}
							ss.append(slow+"ay");
							i += j-1;
							continue;
						}
						else
						{
							k = hominyLetters(s.substring(i));
							if (k>0)
							{
								ss.append(s.substring(i,i+k)+"hay");
								i += k-1;
							}
							else
							{
								carrot = s.charAt(i);
								if (carrot=='&') // DWH 4-21-09 handle entities
								{
									k = s.indexOf(';', i);
									if (k>=0 && (k-i<=5 || (k-i<=7 && s.charAt(i+1)=='#')))
										// entity: leave it alone
									{
										ss.append(s.substring(i,k+1));
										i += k-i;
									}
									else
										ss.append(carrot);
								}
								else if (TextFragment.isMarker(carrot))
								{
									ss.append(s.substring(i,i+2));
									i++;
								}
								else
									ss.append(carrot);
							}
						}
					}			
				}
				ss.append(ss);
			}
		}
		catch(Throwable e)
		{
			LOGGER.warn("Tag Translator failed on {}", s);
		}
		return rslt;
	}
	private int hominyOf(String s, String of)
	{
		int i=0,len=s.length();
		char carrot;
		for(i=0;i<len;i++)
		{
			carrot = s.charAt(i);
			if (of.indexOf(carrot) < 0)
				break;
		}
		return i;
	}
	private int hominyLetters(String s)
	{
		int i=0,len=s.length();
		char carrot;
		for(i=0;i<len;i++)
		{
			carrot = s.charAt(i);
			if (!Character.isLetter(carrot) && carrot!='\'')
				break;
		}
		return i;		
	}

//	private String eggspand(Code code)
//	{
//		String s,ss="";
//		int len;
//		char carrot;
//		s = expandCodeContent(code, "en-US", 1);
//		len = s.length();
//		for(int i=0; i<len; i++)
//		{
//			carrot = s.charAt(i);
//			if (carrot=='<')
//				ss += "&lt;";
//			else
//				ss += carrot;
//		}
//		return ss;
//	}
}
