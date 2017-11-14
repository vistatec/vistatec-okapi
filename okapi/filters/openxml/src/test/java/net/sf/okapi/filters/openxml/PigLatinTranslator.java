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
package net.sf.okapi.filters.openxml;

import net.sf.okapi.common.resource.TextFragment;

import org.slf4j.Logger;

/**
 * Implements ITranslator and translates the text to be
 * translated into one dialect of Pig Latin.  This is
 * used in debugging by OpenXMLRoundTripTest, so that
 * one can easily see that all text that should be 
 * translated is available to the translator for translation.
 */

public class PigLatinTranslator extends AbstractTranslator {
	public final static int MSWORD=1;
	public final static int MSEXCEL=2;
	public final static int MSPOWERPOINT=3;
	static final String CONS="BCDFGHJKLMNPQRSTVWXYZbcdfghjklmnpqrstvwxyz";
	static final String NUM="0123456789";
	static final String PUNC=" 	`~!#$%^&*()_+[{]}\\;:\",<.>?";
	static final String PUNCNL=" 	`~!#$%^&*()_+[{]}\\;:\",<.>?\u00f2\u00f3\u203a";
	static final String PUNCDNL="- 	`~!#$%^&*()_+[{]}\\;:\",<.>";
	static final String UPR="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	static final String LWR="abcdefghijklmnopqrstuvwxyz";

	public PigLatinTranslator()
	{
	}

	@Override
	public String translate(TextFragment tf, Logger LOGGER, ParseType nFileType)
	{
		String s = tf.getCodedText();
		String rslt=s,slow;
		StringBuilder ss = new StringBuilder();
		int i,j,k,len;
		char carrot;
		len = s.length();
		if (len>1)
		{
			for(i=0;i<len;i++)
			{
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
						ss.append(slow).append("ay");
						i += j-1;
						continue;
					}
					else
					{
						k = hominyLetters(s.substring(i));
						if (k>0)
						{
							ss.append(s.substring(i,i+k)).append("hay");
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
			rslt = ss.toString();
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
}
