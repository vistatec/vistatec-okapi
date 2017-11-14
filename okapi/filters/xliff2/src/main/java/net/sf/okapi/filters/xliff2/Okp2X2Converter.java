/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xliff2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.lib.xliff2.core.CTag;
import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.Part;
import net.sf.okapi.lib.xliff2.core.Part.GetTarget;
import net.sf.okapi.lib.xliff2.core.Unit;

public class Okp2X2Converter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final boolean originalWasX2;
	private final LocaleId trgLoc;

	public Okp2X2Converter (boolean originalWasX2,
		LocaleId trgLoc)
	{
		this.originalWasX2 = originalWasX2;
		this.trgLoc = trgLoc;
	}
	
	public Unit convert (ITextUnit tu) {
		Unit unit = new Unit(tu.getId());
		unit.setName(tu.getName());
		unit.setType(tu.getType());
		
		if ( unit.getNoteCount() > 0 ) {
			StringBuilder tmp = new StringBuilder();
			//TODO: notes
			Property prop = tu.getProperty(Property.NOTE);
//			if ( prop != null ) {
//				for ( Note note : unit.getNotes() ) {
//					if ( tmp.length() > 0 ) {
//						tmp.append("\n----------\n");
//					}
//					tmp.append(note.getText());
//				}
//			}
		}
		
		// Convert the content
		TextContainer srcTc = tu.getSource();
		TextContainer trgTc = tu.getTarget(trgLoc);
		ISegments trgSegs = null;
		if ( trgTc != null ) {
			trgSegs = trgTc.getSegments();
		}
		
		// FIXME: assumes full alignment and same number of targets and source. 
		// But we may have extra ignorable in either source or target that will mess 
		// up the alignment. Or other edge cases like joined/split segments.
		TextPart trgPart = null;
		for ( int i=0; i<srcTc.count(); i++ ) {
			TextPart srcPart = srcTc.get(i);
			if (i < trgTc.count()) {
				trgPart = trgTc.get(i);
			}
			convert(srcPart, trgPart, unit);
		}
		
		return unit;
	}
	
	private void convert (TextPart srcPart,
		TextPart trgPart,
		Unit destUnit)
	{
		// Create the destination part
		Part destPart;
		if ( srcPart.isSegment() ) {
			destPart = destUnit.appendSegment();
		}
		else {
			destPart = destUnit.appendIgnorable();
		}
		// Transfer the source
		convert(srcPart.getContent(), destPart.getSource(), false);
		
		// Transfer the target (if needed)
		if ( trgPart != null ) {
			convert(trgPart.getContent(), destPart.getTarget(GetTarget.CREATE_EMPTY), true);
		}
		
		// Handle the Segment-specific data
		if ( destPart.isSegment() ) {
			Segment seg = (Segment)srcPart;
			destPart.setId(seg.getId());
			//TODO: annotations
		}
	}
	
	private void convert (TextFragment oriFrag,
		Fragment destFrag,
		boolean isTarget)
	{
		// First, try to shortcut the copy
		if ( !oriFrag.hasCode() ) {
			// No codes: all is text
			destFrag.setCodedText(oriFrag.getCodedText());
			return;
		}
		// Else: deal with the codes

		// Copy the whole content, then process the inline codes
		String ct = oriFrag.getCodedText();
		destFrag.setCodedText(ct);
		for ( int i=0; i<ct.length(); i++ ) {
			char ch = ct.charAt(i);
			if ( !TextFragment.isMarker(ch) ) continue;
			// Else: transfer the code
			Code code = oriFrag.getCode(ct.charAt(++i));
			CTag ctag;
			// Set the id (original if available)
			String id = ((code.getOriginalId() != null) ? code.getOriginalId() : ""+code.getId());
			// Create the X2 code
			switch ( code.getTagType() ) {
			case OPENING:
				destFrag.delete(i-1, i+1);
				ctag = destFrag.insert(net.sf.okapi.lib.xliff2.core.TagType.OPENING,
					code.getType(), id, code.getData(), i-1, false, true);
				ctag.setDisp(code.getDisplayText());
				ctag.setCanCopy(code.isCloneable());
				ctag.setCanDelete(code.isDeleteable());
				break;
			case CLOSING:
				destFrag.delete(i-1, i+1);
				ctag = destFrag.insert(net.sf.okapi.lib.xliff2.core.TagType.CLOSING,
					code.getType(), id, code.getData(), i-1, true, true);
				ctag.setDisp(code.getDisplayText());
				ctag.setCanCopy(code.isCloneable());
				ctag.setCanDelete(code.isDeleteable());
				break;
			case PLACEHOLDER:
				destFrag.delete(i-1, i+1);
				ctag = destFrag.insert(net.sf.okapi.lib.xliff2.core.TagType.STANDALONE,
					code.getType(), id, code.getData(), i-1, false, true);
				ctag.setDisp(code.getDisplayText());
				ctag.setCanCopy(code.isCloneable());
				ctag.setCanDelete(code.isDeleteable());
				break;
			}
		}
	}

}
