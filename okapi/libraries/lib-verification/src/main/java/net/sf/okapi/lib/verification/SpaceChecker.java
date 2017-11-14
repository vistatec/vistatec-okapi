/*===========================================================================
  Copyright (C) 2012 by the Okapi Framework contributors
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

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpaceChecker {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	/**
	 * Checks and fixes white spaces for a given text unit.
	 * The text unit is passed as a parameter is modified.
	 * @param tu original text unit
	 * @param trgLoc target locale to update
	 * @return the number of changes done.
	 */
	public int checkUnitSpacing (ITextUnit tu,
		LocaleId trgLoc)
	{
		int changes = 0;
		if ( !tu.isEmpty() ) {
			ISegments srcSegs = tu.getSourceSegments();
			for ( Segment srcSeg : srcSegs ) {
				Segment trgSeg = tu.getTargetSegment(trgLoc, srcSeg.getId(), false);
				// Skip non-translatable parts
				if ( trgSeg != null ) {
					changes += checkSpaces(srcSeg.text, trgSeg.text);
				}
			}
		}
		return changes;
	}

	/**
	 * Checks and fixes white spaces for a given text fragment.
	 * The target fragment passed as a parameter is modified.
	 * @param srcFrag original fragment
	 * @param trgFrag the fragment to fix.
	 * @return the number of changes done.
	 */
	public int checkSpaces(TextFragment srcFrag,
		TextFragment trgFrag)
	{
		int changes = 0;
		try {
			if (( !trgFrag.isEmpty() ) && ( trgFrag.hasCode() )) {
				if ( trgFrag.compareTo(srcFrag, true) != 0 ) {
					StringBuilder trgText = new StringBuilder(trgFrag.getCodedText());
					StringBuilder srcText = new StringBuilder(srcFrag.getCodedText());
					int tCur = 0;
	
					// Iterate over trgText
					while ( tCur < trgText.length() ) {
						if ( TextFragment.isMarker(trgText.charAt(tCur)) ) {
							int tIndexBefore = 0;
							int tIndexAfter = 0;
	
							if ( tCur == 0 ) {
								tIndexBefore = tCur;
							}
							else {
								tIndexBefore = tCur - 1;
							}
	
							if ( tCur >= trgText.length() - 2 ) {
								tIndexAfter = trgText.length() - 1;
							}
							else {
								tIndexAfter = tCur + 2;
							}
	
							Code tCode = trgFrag.getCode(trgText.charAt(tCur + 1));
	
							// Search source for matching code
							int sCur = 0;
							while ( sCur < srcText.length() ) {
								if ( TextFragment.isMarker(srcText.charAt(sCur)) ) {
									Code sCode = srcFrag.getCode(srcText.charAt(sCur + 1));
									if (( sCode.getId() == tCode.getId() )
											&& ( sCode.getTagType() == tCode.getTagType() )) {
										int sIndexBefore = 0;
										int sIndexAfter = 0;
	
										if ( sCur == 0 ) {
											sIndexBefore = sCur;
										} 
										else {
											sIndexBefore = sCur - 1;
										}
	
										if ( sCur >= srcText.length() - 2 ) {
											sIndexAfter = srcText.length() - 1;
										}
										else {
											sIndexAfter = sCur + 2;
										}
	
										// fix spaces before tag
										while ( sIndexBefore >= 0 ) {
											if ( Character.isWhitespace(srcText.charAt(sIndexBefore)) ) {
												if (( tIndexBefore > 0 )
													&& ( !Character.isWhitespace(trgText.charAt(tIndexBefore)) ))
												{
													trgText.insert(tIndexBefore + 1, srcText.charAt(sIndexBefore));
													tCur += 1;
													tIndexAfter += 1;
													changes++;
												}
												else if ( tIndexBefore >= 0 ) {
													if ( tIndexBefore > 0 )
														tIndexBefore -= 1;
													else
														break;
												}
												sIndexBefore -= 1;
											}
											else {
												// check target
												while (tIndexBefore >= 0) {
													if ( Character.isWhitespace(trgText.charAt(tIndexBefore)) ) {
														trgText.deleteCharAt(tIndexBefore);
														tCur -= 1;
														tIndexAfter -= 1;
														changes++;
													}
													else {
														break;
													}
													tIndexBefore -= 1;
												}
												break;
											}
										}
	
										// fix spaces after tag
										while ( sIndexAfter < srcText.length() ) {
											if ( Character.isWhitespace(srcText.charAt(sIndexAfter)) ) {
												if (( tIndexAfter < trgText.length() )
													&& (!Character.isWhitespace(trgText.charAt(tIndexAfter)) ))
												{
													// check target cursor for end of segment - 1
													if (tIndexAfter < trgText.length() - 1) {
														trgText.insert(tIndexAfter, srcText.charAt(sIndexAfter));
														tIndexAfter += 1;
														changes++;
													}
												}
												else if ( tIndexAfter < trgText.length() ) {
													if ( tIndexAfter < trgText.length() )
														tIndexAfter += 1;
													else
														break;
												}
												sIndexAfter += 1;
											}
											else {
												// check target
												while ( tIndexAfter < trgText.length() ) {
													if ( Character.isWhitespace(trgText.charAt(tIndexAfter)) ) {
														trgText.deleteCharAt(tIndexAfter);
														changes++;
													}
													else {
														tIndexAfter += 1;
														break;
													}
												}
												break;
											}
										}
										// continue to next target tag
										break;
									}
									// skip index character
									sCur += 1;
								}
								// iterate
								sCur += 1;
							}
							// skip index character
							tCur += 1;
						}
						// iterate
						tCur += 1;
					}
					
					// Check for leading and trailing whitespace
					if (( Character.isWhitespace(srcText.charAt(0)) ) && ( !Character.isWhitespace(trgText.charAt(0)) )) {
						trgText.insert(0, srcText.charAt(0));
						changes++;
					}
					if (( Character.isWhitespace(srcText.charAt(srcText.length() - 1)) )
						&& ( !Character.isWhitespace(trgText.charAt(trgText.length() - 1)) ))
					{
						trgText.insert(trgText.length(), srcText.charAt(srcText.length() - 1));
						changes++;
					}
					
					// write fixed string into target
					trgFrag.setCodedText(trgText.toString(), false);
					
				}
				// Else: no differences: nothing to do
			}
		}
		catch ( Throwable e ) {
//			throw new OkapiException("Error while checking spaces.\n"
//				+ e.getMessage()+"\n"
//				+ "Source fragment: "+srcFrag.toText());
			LOGGER.error("The following error has occured \"{}\" while checking the spaces in the source: {}", e.getMessage(), srcFrag.toText());
		}
		
		return changes;
	}
}
