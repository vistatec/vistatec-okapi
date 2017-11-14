/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.txml;

import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

public class TXMLSkeletonWriter extends GenericSkeletonWriter {

	private boolean allowEmptyOutputTarget;
	
	public TXMLSkeletonWriter() {
	}
	
	public TXMLSkeletonWriter (boolean allowEmptyOutputTarget) {
		this.allowEmptyOutputTarget = allowEmptyOutputTarget;
	}
	
	@Override
	public String processTextUnit (ITextUnit tu) {
		if ( tu == null ) return "";
		// Update the encoder from the TU's MIME type
		if ( encoderManager != null ) {
			encoderManager.updateEncoder(tu.getMimeType());
		}
		
		StringBuilder tmp = new StringBuilder();

		// The skeleton for the TU has three parts: before, reference and after
		// Process the first part
		GenericSkeleton skel = (GenericSkeleton)tu.getSkeleton();
		tmp.append(getString(skel.getParts().get(0), EncoderContext.SKELETON));
		
		TextContainer srcCont = tu.getSource();
		ensureTxmlPattern(srcCont);
		
		TextContainer trgCont = null;
		if ( tu.hasTarget(outputLoc) ) {
			trgCont = tu.getTarget(outputLoc);
			ensureTxmlPattern(srcCont);
		}
		else if ( !allowEmptyOutputTarget ) { // Fall back to source if we have no target and it's requested
			trgCont = srcCont;
		}
		
		// Go through the source, segment by segment
		// We can do this because the pattern has been reduced to the Txml pattern
		for ( int i=0; i<srcCont.count(); i++ ) {
			TextPart part = srcCont.get(i);
			// Skip non-segment part: they will be treated with the segments
			if ( !part.isSegment() ) {
				continue;
			}
			
			// This is a segment: treat it now
			Segment srcSeg = (Segment)part;
			TXMLSegAnnotation segAnn = srcSeg.getAnnotation(TXMLSegAnnotation.class);
			// Get the target, so we can get all information to output the segment attributes
			AltTranslation altTrans = null;
			Segment trgSeg = null;
			if ( trgCont != null ) {
				// Get the target segment if possible
				trgSeg = trgCont.getSegments().get(srcSeg.id);
				if ( trgSeg == null ) {
					// Fall back to the source if requested
					if ( !allowEmptyOutputTarget ) {
						trgSeg = srcSeg;
					}
				}
				// Get the alt-trans possible
				if ( trgSeg != null ) {
					AltTranslationsAnnotation ann = trgSeg.getAnnotation(AltTranslationsAnnotation.class);
					if ( ann != null ) {
						altTrans = ann.getFirst();
					}
				}
			}
			
			// Output the segment element
			tmp.append("<segment segmentId=\""+srcSeg.getId()+"\"");
			// Output the gtmt attribute
			boolean gtmt = false;
			if ( altTrans != null ) {
				gtmt = altTrans.getFromOriginal();
			}
			tmp.append(" gtmt=\"" + (gtmt ? "true" : "false") + "\"");
			tmp.append(">");
			
			// Do we have a part before this segment?
			if ( i > 0 ) {
				part = srcCont.get(i-1);
				if ( !part.isSegment() ) {
					if ( contentIsComment(part) ) {
						tmp.append(part.getContent());
					}
					else if ((( segAnn == null ) && ( i == 1 ))
						|| (( segAnn != null ) && segAnn.hasWSBefore() )) {
						// Not an original segment, but first one
						// Or original segment did have a ws before
						tmp.append("<ws>");
						tmp.append(processFragment(part.getContent(), EncoderContext.SKELETON));
						tmp.append("</ws>");
					}
				}
			}
			
			// Now output the segment source and target
			tmp.append("<source>");
			tmp.append(processFragment(srcSeg.getContent(), EncoderContext.SKELETON));
			tmp.append("</source>");

			// Do we have a part after the segment?
			// Note: the DTD indicates (ws?, source, target? ws?)
			// but the files are really (as declared in the XSD): (ws?, source, ws? target)
			boolean targetDone = false;
			if ( i+1 < srcCont.count() ) {
				part = srcCont.get(i+1);
				if ( !part.isSegment() ) {
					if ( contentIsComment(part) ) {
						// Part with a comment are not the same as WS parts
						// Target must be output before the comment
						// Output the target (if any)
						if ( trgSeg != null ) {
							tmp.append("<target>");
							tmp.append(processFragment(trgSeg.getContent(), EncoderContext.TEXT));
							tmp.append("</target>");
						}
						// Close the segment
						tmp.append("</segment>");
						targetDone = true;
						tmp.append(part.getContent());
					}
					else if (( segAnn == null ) || (( segAnn != null ) && segAnn.hasWSAfter() )) {
						// Not an original segment (any position)
						// Or original segment did have a ws after
						tmp.append("<ws>");
						tmp.append(processFragment(part.getContent(), EncoderContext.SKELETON));
						tmp.append("</ws>");
					}
					i++; // This part is done
				}
			}
			
			if ( !targetDone ) {
				// Output the target (if any)
				if ( trgSeg != null ) {
					tmp.append("<target>");
					tmp.append(processFragment(trgSeg.getContent(), EncoderContext.TEXT));
					tmp.append("</target>");
				}
				// Close the segment
				tmp.append("</segment>");
			}
		}

		// Process the last skeleton part (the third)
		tmp.append(getString(skel.getParts().get(2), EncoderContext.SKELETON));

		// Done
		return tmp.toString();
	}
	
	/**
	 * Tells if the content of this part is a comment or not (i.e. starts with "&lt;!--"
	 * @param part the text part to check.
	 * @return true if it's a comment, false, if not.
	 */
	private boolean contentIsComment (TextPart part) {
		return part.getContent().getCodedText().startsWith("<!--");
	}
	
//	private Segment fetchNextSegment (TextContainer tc,
//		int fromIndex)
//	{
//		for ( int i=fromIndex; i<tc.count(); i++ ) {
//			TextPart part = tc.get(i);
//			if ( part.isSegment() ) {
//				return (Segment)part;
//			}
//		}
//		return null;
//	}
	
	private int fetchNextSegmentIndex (TextContainer tc,
		int fromIndex)
	{
		for ( int i=fromIndex; i<tc.count(); i++ ) {
			TextPart part = tc.get(i);
			if ( part.isSegment() ) {
				return i;
			}
		}
		return -1;
	}
	
	protected void ensureTxmlPattern (TextContainer tc) {
		int i = 0;
		int diff;
		while ( i<tc.count() ) {
			// If the part is a segment, move to the next part
			if ( tc.get(i).isSegment() ) {
				i++;
				continue;
			}
			// Else: it's not a segment, and we try to collapse if needed
			
			// Where is the next segment
			int nextSegIndex = fetchNextSegmentIndex(tc, i);
			if ( nextSegIndex == -1 ) {
				// No next segment
				// Collapse all remaining parts into one
				diff = (tc.count()-i)-1;
			}
			else if ( i == 0 ) {
				// If this is the first part before the first index
				// Collapse until first segment
				diff = (nextSegIndex-i)-1;
			}
			else {
				// Else: it a part between segments
				// Allowing 2 parts
				diff = (nextSegIndex-i)-2;
			}

			// Collapse if needed
			if ( diff > 0 ) {
				tc.joinWithNext(i, diff);
			}
			i++;
		}
	}
	
//	private String processSegment (Segment srcSeg,
//		TextFragment trgFrag,
//		AltTranslation altTrans)
//	{
//		TextFragment srcFrag = srcSeg.getContent();
//		if ( trgFrag == null ) { // No target available: use the source
//			trgFrag = srcFrag;
//		}
//
//		StringBuilder tmp = new StringBuilder();
//		tmp.append("<segment segmentId=\""+srcSeg.getId()+"\"");
//		if ( altTrans != null ) {
////			tmp.append(String.format("MatchPercent=\"%d\">", altTrans.getScore()));
//		}
//		
//		tmp.append("><source>");
//		tmp.append(processFragment(srcFrag, 1));
//		tmp.append("</source>");
//		
//		tmp.append("<target>");
//
//		if ( getLayer() != null ) {
//			if ( altTrans != null ) {
//				// This is an entry with source and target
//				tmp.append(getLayer().endCode());
//				tmp.append(getLayer().startSegment());
//				tmp.append(processFragment(srcFrag, 1));
//				tmp.append(getLayer().midSegment(altTrans.getScore()));
//				tmp.append(processFragment(trgFrag, 0));
//				tmp.append(getLayer().endSegment());
//				tmp.append(getLayer().startCode());
//			}
//			else {
//				// Write target only
//				tmp.append(getLayer().endCode()); 
//				tmp.append(processFragment(trgFrag, 0));
//				tmp.append(getLayer().startCode()); 
//			}
//		}
//		else {
//			tmp.append(processFragment(trgFrag, 0));
//		}
//		
//		tmp.append("</target>");
//		tmp.append("</segment>");
//		return tmp.toString();
//	}

//	/**
//	 * Verifies that this skeleton writer can be used for internal purpose by the TXMLFilter
//	 * for outputting skeleton chunks.
//	 * @param lineBreak the type of line-break to use.
//	 */
//	protected void checkForFilterInternalUse (String lineBreak) {
//		if ( encoderManager == null ) {
//			encoderManager = new EncoderManager();
//			encoderManager.setMapping(MimeTypeMapper.TTX_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
//			encoderManager.setDefaultOptions(null, "US-ASCII", lineBreak); // Make sure we escape extended
//			encoderManager.updateEncoder(MimeTypeMapper.TTX_MIME_TYPE);
//		}
//	}
	
	/**
	 * Outputs a fragment.
	 * @param frag the fragment to output
	 * @param context output context: 0=text, 1=skeleton
	 * @return the output string.
	 */
	protected String processFragment (TextFragment frag,
			EncoderContext context)
	{
		StringBuilder tmp = new StringBuilder();
		String text = frag.getCodedText();

		for ( int i=0; i<text.length(); i++ ) {
			char ch = text.charAt(i);
			switch ( ch ) {
			case TextFragment.MARKER_ISOLATED:
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
				tmp.append(expandCode(frag.getCode(text.charAt(++i)), context));
				continue;
			default:
				tmp.append(encoderManager.encode(ch, context));
				continue;
			}
		}
		
		return tmp.toString(); 
	}

	private String expandCode (Code code,
			EncoderContext context)
	{
		if ( getLayer() != null ) {
			if ( context == EncoderContext.TEXT ) { // Parent is text -> codes are inline
				return getLayer().startInline() 
					+ getLayer().encode(code.getOuterData(), EncoderContext.INLINE)
					+ getLayer().endInline();
			}
			else {
				return getLayer().encode(code.getOuterData(), EncoderContext.SKELETON);
			}
		}
		// Else: no layer
		return code.getOuterData();
	}

}
