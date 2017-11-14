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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.xliff2.core.CTag;
import net.sf.okapi.lib.xliff2.core.Fragment;
import net.sf.okapi.lib.xliff2.core.MTag;
import net.sf.okapi.lib.xliff2.core.Note;
import net.sf.okapi.lib.xliff2.core.Part;
import net.sf.okapi.lib.xliff2.core.Unit;

public class X2ToOkpConverter {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final boolean originalWasX2;
	private final LocaleId trgLoc;

	/**
	 * Creates a new converter object.
	 * @param originalWasX2 true if the initial original format was XLIFF 2, false if not.
	 * @param trgLoc the target locale.
	 */
	public X2ToOkpConverter (boolean originalWasX2,
		LocaleId trgLoc)
	{
		this.originalWasX2 = originalWasX2;
		this.trgLoc = trgLoc;
	}
	
	public ITextUnit convert (Unit unit) {
		ITextUnit tu = new TextUnit(unit.getId());
		tu.setName(unit.getName());
		tu.setType(unit.getType());
		
		if ( unit.getNoteCount() > 0 ) {
			StringBuilder tmp = new StringBuilder();
			for ( Note note : unit.getNotes() ) {
				if ( tmp.length() > 0 ) {
					tmp.append("\n----------\n");
				}
				tmp.append(note.getText());
			}
			tu.setProperty(new Property(Property.NOTE, tmp.toString(), true));
		}
		
		// Transfer the source
		TextContainer tc = tu.getSource();
		convert(unit, tc, false);
		
		// Do we have at least one target part?
		boolean hasTarget = false;
		for ( Part part : unit ) {
			if ( part.hasTarget() ) {
				hasTarget = true;
				break;
			}
		}
		// Transfer the target if needed
		if ( hasTarget ) {
			tc = tu.createTarget(trgLoc, false, IResource.CREATE_EMPTY);
			convert(unit, tc, true);
		}
		
		return tu;
	}
	
	private void convert (Unit unit,
		TextContainer dest,
		boolean isTarget)
	{
		//TODO: handle target order: unit.hasTargetOrder()
		boolean first = true;
		List<TextPart> textParts = new ArrayList<>();
		int segId = 1;
		for ( Part part : unit ) {
			textParts.add(convert(first, part, dest, isTarget, segId));
			first = false;
			segId++;
		}
		dest.setParts(textParts.toArray(new TextPart[textParts.size()]));
	}
	
	private TextPart convert (boolean first,
		Part part,
		TextContainer dest,
		boolean isTarget,
		int segId)
	{
		TextPart tp;
		if ( part.isSegment() ) {
			String id = part.getId() == null ? Integer.toString(segId) : part.getId(); 
			Segment seg = new Segment(id);
			tp = seg;
		}
		else {
			// ignorable or inter-segment text
			tp = new TextPart();
		}
		// Convert content
		if ( isTarget ) {
			if ( part.hasTarget() ) {
				convert(part.getTarget(), tp);
			}
			else {
				// Nothing to do: we will get an empty part/segment
			}
		}
		else {
			convert(part.getSource(), tp);
		}
		// Add the part at the end of this container
		// Do not collapse on empty preceding parts, except on the first one
		// JEH: dest.append(tp, first) was converting ignorable to a segment if first part
		return tp;
	}

	private void convert (Fragment frag, TextPart part)
	{
		TextFragment tf = part.text;
		for ( Object obj : frag ) {
			if ( obj instanceof String ) {
				tf.append((String)obj);
			}
			else if ( obj instanceof CTag ) {
				CTag ctag = (CTag)obj;
				Code code = null;
				switch ( ctag.getTagType() ) {
				case CLOSING:
					code = new Code(TagType.CLOSING, ctag.getType());
					break;
				case OPENING:
					code = new Code(TagType.OPENING, ctag.getType());
					break;
				case STANDALONE:
					code = new Code(TagType.PLACEHOLDER, ctag.getType());
					break;
				}
				int id;
				try {
					id = Integer.parseInt(ctag.getId());
				}
				catch ( NumberFormatException e ) {
					// Fall back for non-integer tags
					id = ctag.getId().hashCode();
					code.setOriginalId(ctag.getId());
				}
				code.setId(id);
				code.setCloneable(ctag.getCanCopy());
				code.setDeleteable(ctag.getCanDelete());
				code.setData(ctag.getData());
				code.setDisplayText(ctag.getDisp());
				//TODO: subtype, etc.
				tf.append(code);
			}
			else if ( obj instanceof MTag ) {
				MTag mtag = (MTag)obj;
				tf.append("[MARKER]");
			}
		}
		
		//TODO: do we need to unwarp?
		//TextFragment.unwrap(tf);
	}

}
