/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common.resource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import net.sf.okapi.common.ReversedIterator;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiBadStepInputException;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.core.simplifierrules.ParseException;
import net.sf.okapi.core.simplifierrules.SimplifierRules;

/**
 * @author Fredrik Liden
 * @author Sergei Vasilyev
 * @author Jim Hargrave
 *
 */
public class CodeSimplifier {
	protected static final int MAX = 10;
	protected boolean postSegmentation = false;
	protected String rules;
	protected SimplifierRules ruleEvaluator;
	
	class CodeNode {

		/**
		 * Index of the code marker (1-st char of the 2-char code ref) in the CodedText string.
		 */
		int offset;
		
		/**
		 * Index of the code (recorded in the 2-nd char of the code marker in the string) in the list of codes.
		 */
		int intIndex; 
		
		/**
		 * The same as intIndex but represented as a char.
		 */
		char charIndex;
		
		/**
		 * The code object referred to from the code marker in the string.
		 */
		Code code;
		
		/**
		 * The 2-char code marker as a string.
		 */
		String marker;
		
		/**
		 * The 1-st char of the code marker represented as a string.
		 */
		String markerFlag;
		
		/**
		 * True if the current code is adjacent to the previous code in their common text fragment (no text in-between the two).
		 */
		boolean adjacentPrev = false;
		
		/**
		 * True if the current code is adjacent to the next code in their common text fragment (no text in-between the two).
		 */
		boolean adjacentNext = false;
		
		public CodeNode (int offset, int intIndex, char charIndex, Code code) {
			this.offset = offset;
			this.intIndex = intIndex;
			this.charIndex = charIndex;
			this.code = code;
		}
		
		public void setMergedData(CodeNode node1, CodeNode node2) {
			
			// only store mergedData of this is called from the post segmentation simplifier
			if (postSegmentation) {
				List<Code> codes1 = new LinkedList<>();
				List<Code> codes2 = new LinkedList<>();
				
				if (node1.code.isMerged()) {
					codes1.addAll(Code.stringToCodes(node1.code.getMergedData())); 
				} else {
					codes1.add(node1.code);
				}
				
				if (node2.code.isMerged()) {
					codes2.addAll(Code.stringToCodes(node2.code.getMergedData())); 
				} else {
					codes2.add(node2.code);
				}
				
				codes1.addAll(codes2);
				this.code.setMergedData(Code.codesToString(codes1));
			}
			
			String data1 = node1.code.getData();
			String data2 = node2.code.getData();
			
			String odata1 = node1.code.hasOuterData() ? node1.code.getOuterData() : null;
			String odata2 = node2.code.hasOuterData() ? node2.code.getOuterData() : null;		
			
			if (!Util.isEmpty(data1) && !Util.isEmpty(data2)) {
				this.code.setData(data1 + data2);
			} 
			
			if (!Util.isEmpty(odata1) && !Util.isEmpty(odata2)) {
				this.code.setOuterData(odata1 + odata2);
			}
		}
	}
	
	class StartCodeNode extends CodeNode {
		
		EndCodeNode endNode;
		
		StartCodeNode (int offset, int intIndex, char charIndex, Code code) {
			super(offset, intIndex, charIndex, code);
			marker = new String("" + (char)TextFragment.MARKER_OPENING + charIndex);
			markerFlag = new String("" + (char)TextFragment.MARKER_OPENING);
		}
	}
	
	class EndCodeNode extends CodeNode {

		StartCodeNode beginNode;
		
		EndCodeNode (int offset, int intIndex, char charIndex, Code code) {
			super(offset, intIndex, charIndex, code);
			marker = new String("" + (char)TextFragment.MARKER_CLOSING + charIndex);
			markerFlag = new String("" + (char)TextFragment.MARKER_CLOSING);
		}
	}
	
	class PhCodeNode extends CodeNode {

		PhCodeNode (int offset, int intIndex, char charIndex, Code code) {
			super(offset, intIndex, charIndex, code);
			marker = new String("" + (char)TextFragment.MARKER_ISOLATED + charIndex);
			markerFlag = new String("" + (char)TextFragment.MARKER_ISOLATED);
		}
	}
	
	private LinkedList<CodeNode> codeNodesList;
	private String codedText;
	
	/*
	 * Prepare simplifier for simplifying
	 */
	private void prepare (String pCodedText, List<Code> pCodes) {
		
		if (ruleEvaluator == null && !Util.isEmpty(rules)) {
			ruleEvaluator = new SimplifierRules(new StringReader(rules));
		}
		codeNodesList = new LinkedList<CodeNode>();
		codedText = pCodedText;
		
		Stack<StartCodeNode> codeNodesStack = new Stack<StartCodeNode>();
		
		for (int i = 0; i < codedText.length(); i++){
			
		    int c = codedText.codePointAt(i);
		    
		    if(c == TextFragment.MARKER_OPENING){
		    	
		    	StartCodeNode cn = new StartCodeNode(i, TextFragment.toIndex(codedText.charAt(i+1)), codedText.charAt(i+1), pCodes.get(TextFragment.toIndex(codedText.charAt(i+1))));

		    	codeNodesList.add(cn);
		    	codeNodesStack.push(cn);

		    }else if(c == TextFragment.MARKER_CLOSING){
		    	
		    	EndCodeNode cn = new EndCodeNode(i, TextFragment.toIndex(codedText.charAt(i+1)), codedText.charAt(i+1), pCodes.get(TextFragment.toIndex(codedText.charAt(i+1))));
		    	
		    	codeNodesList.add(cn);
		    	codeNodesStack.pop().endNode = cn;
		    	
		    }else if(c == TextFragment.MARKER_ISOLATED){
		    	
		    	PhCodeNode cn = new PhCodeNode(i, TextFragment.toIndex(codedText.charAt(i+1)), codedText.charAt(i+1), pCodes.get(TextFragment.toIndex(codedText.charAt(i+1))));
		    	
		    	codeNodesList.add(cn);
		    	
		    }
		}
		updateAdjacentFlags(); // codeNodesList.get(2)
	}
	
	/*
	 * Update the adjacent flags of all the code nodes 
	 */
	private void updateAdjacentFlags () {
		
		CodeNode peekCn;
		
		for(int i=0; i< codeNodesList.size(); i++){
		
			CodeNode cn = codeNodesList.get(i);
			
			if(i+1 < codeNodesList.size()){
		
				peekCn = codeNodesList.get(i+1);
				
				if(adjacentMarkers(cn, peekCn)){

					cn.adjacentNext = true;
					peekCn.adjacentPrev = true;

				}
			}
		}
	}
	
	/**
	 * Simplifies all possible tags in a given text fragment.
	 * @param tf the text fragment to modify.
	 * @param maxIterations maximum number of iterations in merging of adjacent codes.
	 * @param removeLeadingTrailingCodes true to remove the leading and/or the trailing codes
	 * of the fragment and place their text in the result.
	 * This works for isolated codes only for now.
	 * <b>It is the responsibility of the caller to put the leading/trailing data into the skeleton.</b>
	 * @param mergeCodes true to merge adjacent codes, false to leave as-is
	 * @return Null (no leading or trailing code removal was) or a string array with the
	 * original data of the codes removed. The first string if there was a leading code, the second string
	 * if there was a trailing code. Both or either can be null.
	 */
	public TextFragment[] simplifyAll (TextFragment tf,
		int maxIterations,
		boolean removeLeadingTrailingCodes, boolean mergeCodes)
	{
		int isolatedMerges=0;
		int openCloseMerges=0;
		int emptyOpenCloseMerges=0;
		int iteration = 0;
		
		try {
			if (mergeCodes) {
				do {
					iteration++;
					
					// TODO check if codes can be combined. Codes can have references to different
					// resources (parent is different), in this case cannot combine them even if
					// they both are placeholders
					
					prepare(tf.getCodedText(), tf.getCodes());
					isolatedMerges = simplifyIsolated();
					tf.setCodedText(getCodedText(), getCodes());
		
					prepare(tf.getCodedText(), tf.getCodes());
					openCloseMerges = simplifyOpeningClosing();
					tf.setCodedText(getCodedText(), getCodes());				
					
					prepare(tf.getCodedText(), tf.getCodes());
					emptyOpenCloseMerges = simplifyEmptyOpeningClosing();
					tf.setCodedText(getCodedText(), getCodes());				
				}
				while ((iteration < maxIterations) && (isolatedMerges + openCloseMerges + emptyOpenCloseMerges) > 0);
			}
			
			//tf.renumberCodes();
			
			// Check leading and trailing codes if requested
			if ( removeLeadingTrailingCodes ) {
				// Change segmentation markers from opening/closing to placeholder type
				// to be able to remove them to skeleton
				return removeLeadingTrailingCodes(tf, maxIterations);
			}
			else {
				return null;
			}
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error simplifiying codes.\n"+e.getMessage(), e);
		}
	}
	
	
	/**
	 * Simplifies all possible tags in a given possibly segmented text container.
	 * @param tc the given text container to modify
	 * @param removeLeadingTrailingCodes true to remove the leading and/or the trailing code
	 * of the fragment and place their text in the result.
	 * <b>It is the responsibility of the caller to put the leading/trailing data into the skeleton.</b>
	 * @return Null (no leading or trailing code removal was) or a string array with the
	 * @param mergeCodes true to merge adjacent codes, false to leave as-is
	 * original data of the codes removed. The first string if there was a leading code, the second string
	 * if there was a trailing code. Both or either can be null.
	 */
	public TextFragment[] simplifyAll (TextContainer tc, boolean removeLeadingTrailingCodes, boolean mergeCodes) {
		// Cannot simplify individual segments as segmentation can change later. 
		// Store and remove segmentation, simplify the source as the whole, then re-apply initial segmentation,
		// moving segment boundaries outside codes.
				
		TextFragment tf = TextUnitUtil.storeSegmentation(tc);
		TextFragment[] res = simplifyAll(tf, removeLeadingTrailingCodes, mergeCodes);
		
		if (removeLeadingTrailingCodes && res != null) {
			boolean hasLeading = !TextUnitUtil.isEmpty(res[0]);
			boolean hasTrailing = !TextUnitUtil.isEmpty(res[1]);
			
			if (hasLeading) {
				TextFragment leadingMarkers = new TextFragment();
				res[0] = TextUnitUtil.extractSegMarkers(leadingMarkers, res[0], true);
				tf.insert(0, leadingMarkers, true);
			}
			
			if (hasTrailing) {
				TextFragment trailingMarkers = new TextFragment();
				res[1] = TextUnitUtil.extractSegMarkers(trailingMarkers, res[1], true);
				tf.insert(-1, trailingMarkers, true);
			}
			
			if (TextUnitUtil.isEmpty(res[0]) && TextUnitUtil.isEmpty(res[1])) res = null; 
		}
		
		TextUnitUtil.restoreSegmentation(tc, tf);
		
		// Another attempt to extract leading/trailing text, from TextParts this time
		if (removeLeadingTrailingCodes) {
			int firstSegIndex = -1;
			int lastSegIndex = -1;
			int index = -1;
			for (TextPart part : tc) {
				index++;
				if (part.isSegment()) {
					if (firstSegIndex == -1) {
						firstSegIndex = index;
					}
					lastSegIndex = index;
				}
			}
			if (firstSegIndex > 0 || lastSegIndex < tc.count() - 1) {
				List<Integer> removedIndexes = new LinkedList<Integer>();
				if (res == null) res = new TextFragment[2];
				TextFragment sb = new TextFragment();
				for (int i = 0; i < firstSegIndex; i++) { // seg is not included
					sb.append(tc.get(i).getContent());
					removedIndexes.add(i);
				}
				if (TextUnitUtil.isEmpty(res[0]))
					res[0] = sb.isEmpty() ? null : sb; 
				
				sb = new TextFragment();
				for (int i = lastSegIndex + 1; i < tc.count(); i++) {
					sb.append(tc.get(i).getContent());
					removedIndexes.add(i);
				}
				if (TextUnitUtil.isEmpty(res[1]))
					res[1] = sb.isEmpty() ? null : sb;
				
				// Remove the text parts moved to res[]
				for (Integer removedIndex : new ReversedIterator<Integer>(removedIndexes)) {
					tc.remove(removedIndex);
				}
			}
		}		
		
		//TextUnitUtil.renumberCodes(tc);
		
		TextUnitUtil.convertTextParts_whitespaceCodesToText(tc);
		
		return res;		
	}
	
	private TextFragment[] removeLeadingTrailingCodes (TextFragment tf, int maxIterations) {
		String ctext;
		List<Code> codes;
		Code code = null;		
		int startPos;
		int endPos;
		TextFragment leading = new TextFragment();
		TextFragment trailing = new TextFragment();
		TextFragment sb;		
		int iteration = 0;
		boolean removed;
		
		do {
			// Iterations are needed to catch pairs of opening/closing tags at the edges, only if both are there, then remove the pair
			iteration++;
			removed = false;
			
			// Remove leading isolated codes and spaces
			ctext = tf.getCodedText();
			codes = tf.getCodes();
			startPos = 0;
			endPos = 0;
			sb = new TextFragment();
					
			for (int i = 0; i < ctext.length(); i++){
				if ( TextFragment.isMarker(ctext.charAt(i)) ) {				
					int codeIndex = TextFragment.toIndex(ctext.charAt(i + 1));
					code = codes.get(codeIndex);
					if (ctext.codePointAt(i) != TextFragment.MARKER_ISOLATED) break;																										
					if (!canSimplify(code)) break;
					sb.append(code);	
					endPos = i + 2;
					i++; // Skip the pair
				}
				// For the 1-st iteration mind spaces only after a code not to trim-head the string
				else if (Character.isWhitespace(ctext.charAt(i)) && (endPos > 0 || iteration > 1)) {
						sb.append(ctext.charAt(i));
						endPos = i + 1;
				}
				else {
					// If came across a non-space and non-code, fall off
					break; 
				}			
			}
			
			if (startPos < endPos) {
				tf.remove(startPos, endPos);
				leading.append(sb);
				removed = true;
			}		
			
			// Remove trailing isolated codes and spaces
			ctext = tf.getCodedText();
			codes = tf.getCodes();
			startPos = ctext.length();
			endPos = startPos;
			sb = new TextFragment();
			
			for (int i = ctext.length() - 1; i > 0; i--) {
				if ( TextFragment.isMarker(ctext.charAt(i - 1)) ) {				
					int codeIndex = TextFragment.toIndex(ctext.charAt(i));
					code = codes.get(codeIndex);
					if (ctext.codePointAt(i - 1) != TextFragment.MARKER_ISOLATED) break;
					if (!canSimplify(code)) break;					
					sb.insert(0, code);									
					i--; // Skip the pair
					startPos = i;
				}
				// For the 1-st iteration mind spaces only before a code not to trim-tail the string
				else if (Character.isWhitespace(ctext.charAt(i)) && (startPos < ctext.length() - 1 || iteration > 1)) {
						sb.insert(0, Character.toString(ctext.charAt(i)));
						startPos = i;
				}
				else {
					// If came across a non-space and non-code, and the previous char is not a code marker, fall off
					break; 
				}
			}
			
			if (startPos < endPos) {
				tf.remove(startPos, endPos);
				trailing.insert(0, sb);
				removed = true;
			}
			
			Code leadingCode = null;
			Code trailingCode = null;
			
			ctext = tf.getCodedText();
			codes = tf.getCodes();
			
			if ( ctext.length() > 1 && !Util.isEmpty(codes) ) {
				// Check leading code
				ctext = tf.getCodedText();
				codes = tf.getCodes();
				if ( TextFragment.isMarker(ctext.charAt(0)) ) {				
					int codeIndex = TextFragment.toIndex(ctext.charAt(1));
					code = codes.get(codeIndex);
					if (ctext.codePointAt(0) == TextFragment.MARKER_OPENING && canSimplify(code)) {
						leadingCode = code;
					}
				}
				
				// Check trailing code
				ctext = tf.getCodedText();
				codes = tf.getCodes();
				if ( TextFragment.isMarker(ctext.charAt(ctext.length() - 2)) ) {				
					int codeIndex = TextFragment.toIndex(ctext.charAt(ctext.length() - 1));
					code = codes.get(codeIndex);
					if (ctext.codePointAt(ctext.length() - 2) == TextFragment.MARKER_CLOSING && canSimplify(code)) {
						trailingCode = code;
					}
				}
				
				if (leadingCode != null && trailingCode != null && leadingCode.getId() == trailingCode.getId()) {
					tf.remove(0, 2);
					leading.append(leadingCode);
				
					ctext = tf.getCodedText();
					codes = tf.getCodes();
					
					tf.remove(ctext.length() - 2, ctext.length());
					trailing.insert(0, trailingCode);
					
					removed = true;
				}
			}			
			
			if (!removed) break; // 1 exceeding iteration at maximum
		} while (iteration < maxIterations);
						
		TextFragment res0 = leading;
		TextFragment res1 = trailing;
		
		TextFragment[] res = new TextFragment[] {
				TextUnitUtil.isEmpty(res0) ? null : res0, 
				TextUnitUtil.isEmpty(res1) ? null : res1};
		return (TextUnitUtil.isEmpty(res0) && TextUnitUtil.isEmpty(res1)) ? null : res;
	}
		
	/**
	 * Simplifies all possible tags in a given text fragment.
	 * @param tf the text fragment to modify.
	 * @param removeLeadingTrailingCodes true to remove the leading and/or the trailing code
	 * of the fragment and place their text in the result.
	 * <b>It is the responsibility of the caller to put the leading/trailing data into the skeleton.</b>
	 * @param mergeCodes true to merge adjacent codes, false to leave as-is
	 * @return Null (no leading or trailing code removal was) or a string array with the
	 * original data of the codes removed. The first string if there was a leading code, the second string
	 * if there was a trailing code. Both or either can be null.
	 */
	public TextFragment[] simplifyAll (TextFragment tf,
		boolean removeLeadingTrailingCodes, boolean mergeCodes)
	{
		return simplifyAll(tf, MAX, removeLeadingTrailingCodes, mergeCodes);
	}
	
	/**
	 * Simplifies all possible tags in a given text fragment.
	 * @param tf the text fragment to modify.
	 * @param removeLeadingTrailingCodes true to remove the leading and/or the trailing code
	 * of the fragment and place their text in the result.
	 * <b>It is the responsibility of the caller to put the leading/trailing data into the skeleton.</b>
	 * @return Null (no leading or trailing code removal was) or a string array with the
	 * original data of the codes removed. The first string if there was a leading code, the second string
	 * if there was a trailing code. Both or either can be null.
	 */
	public TextFragment[] simplifyAll (TextFragment tf,
			boolean removeLeadingTrailingCodes)
		{
			return simplifyAll(tf, MAX, removeLeadingTrailingCodes, true);
		}

	/**
	 * Simplifies the place-holders in a given text fragment.
	 * @param tf the text fragment to modify.
	 */
	public void simplifyIsolated (TextFragment tf) {
		prepare(tf.getCodedText(), tf.getCodes());
		simplifyIsolated();
		tf.setCodedText(getCodedText(), getCodes());
	}
	
	private int simplifyIsolated () {
		int merges = 0;
		CodeNode peekCn;
		
		for(int i=0; i< codeNodesList.size(); i++){
		
			CodeNode cn = codeNodesList.get(i);
			
			if(i+1 < codeNodesList.size()){
		
				peekCn = codeNodesList.get(i+1);
				
				if(cn.adjacentNext && peekCn.adjacentPrev){
					
					// We can merge a placehorder or isolated type code with another code of any type, 
					// and both codes cannot contain seg boundary markers altogether (either one can)
					if ((cn instanceof PhCodeNode || peekCn instanceof PhCodeNode)
						&& canJoin(cn.code, peekCn.code)){
						
						//TODO: Possibly update it to direct where the PH should be added, open or closing. 
						//      Possibly do two runs one forward and one backwards.

						mergeNodes(cn,peekCn);
						merges++;
						i--;
						continue;
					}
				}
			}
		}
		renumberMarkerIndexes();
		updateCodeIds();
		
		return merges;
	}
	
	private boolean canSimplify(Code code) {
		if (ruleEvaluator != null) {
			try {
				if (ruleEvaluator.evaluate(rules, code)) {
					return false;
				}
			} catch (ParseException e) {
				throw new OkapiBadStepInputException("Simplifier rules syntax error.", e);
			}
		}
		return true;
	}
		
	private boolean canJoin(Code code1, Code code2) {
		// evaluate the current codes based on the simplifier rules defined in the
		// filter config file. If any of the rules are true then do not merge.
		if (!canSimplify(code1) || !canSimplify(code2)) {
			return false;
		} 

		// Mostly for OpenXml. Cases like [#$tu4], [#$sg34] etc..
		// add more patterns to EXTERNAL_REF_REGEX if needed
		if (TextUnitUtil.hasExternalRefMarker(code1) || TextUnitUtil.hasExternalRefMarker(code2)) {
			return false;
		}
				
		boolean ss2 = TextUnitUtil.hasSegStartMarker(code2);		
		boolean se1 = TextUnitUtil.hasSegEndMarker(code1);
		boolean dontJoin = (se1 && ss2);
		
		return !dontJoin;
	}

	/*
	 * Simplifies the isolated tags
	 */
	public void simplifyOpeningClosing (TextFragment tf) {
		prepare(tf.getCodedText(), tf.getCodes());
		simplifyOpeningClosing();
		tf.setCodedText(getCodedText(), getCodes());
	}
	
	/*
	 * Merges the Start tags
	 */
	private int simplifyOpeningClosing () {
		
		int merges = 0;
		CodeNode peekCn;
		
		for(int i=0; i< codeNodesList.size(); i++){
		
			CodeNode cn = codeNodesList.get(i);
			
			if(i+1 < codeNodesList.size()){
		
				peekCn = codeNodesList.get(i+1);
				
				if(cn.adjacentNext && peekCn.adjacentPrev) {
					
					if(cn.code.getTagType() == TagType.OPENING && 
							peekCn.code.getTagType() == TagType.OPENING &&
							cn instanceof StartCodeNode &&
							peekCn instanceof StartCodeNode	&& 
							canJoin(cn.code, peekCn.code)) {
						
						StartCodeNode scn1 = (StartCodeNode)cn;
						StartCodeNode scn2 = (StartCodeNode)peekCn;

						EndCodeNode ecn2 = scn2.endNode;
						EndCodeNode ecn1 = scn1.endNode;

						if (adjacentMarkers(ecn2, ecn1)){

							mergeEndNodes(ecn2, ecn1);
							mergeNodes(scn1, scn2);
							
							merges++;
							i--;
							continue;
						}
					}
				}
			}
		}
		
		renumberMarkerIndexes();
		updateCodeIds();
		
		return merges;
	}
	
	/*
	 * Simplify the isolated tags
	 */
	public void simplifyEmptyOpeningClosing (TextFragment tf) {
		prepare(tf.getCodedText(), tf.getCodes());
		simplifyEmptyOpeningClosing();
		tf.setCodedText(getCodedText(), getCodes());
	}
	
	/*
	 * Merges the Start tags
	 */
	private int simplifyEmptyOpeningClosing(){
		
		int merges = 0;
		CodeNode peekCn;
		
		for(int i=0; i< codeNodesList.size(); i++){
		
			CodeNode cn = codeNodesList.get(i);
			
			if(i+1 < codeNodesList.size()){
		
				peekCn = codeNodesList.get(i+1);
				
				if(cn.adjacentNext && peekCn.adjacentPrev){
					
					if(cn.code.getTagType() == TagType.OPENING && 
							peekCn.code.getTagType() == TagType.CLOSING && 
							cn.intIndex == (peekCn.intIndex-1) &&
							cn instanceof StartCodeNode &&
							peekCn instanceof EndCodeNode && 
							canJoin(cn.code, peekCn.code)) {
						
						StartCodeNode scn = (StartCodeNode)cn;
						EndCodeNode ecn = (EndCodeNode)peekCn;

						mergeEmptyNodes(scn, ecn);

						merges++;
						i--;
						continue;
					}
				}
			}
		}
		renumberMarkerIndexes();
		updateCodeIds();
		
		return merges;
	}
	
	/*
	 * Renumber marker indexes
	 */
	private void renumberMarkerIndexes () {
		
		for (int i=0; i< codeNodesList.size(); i++) {
			CodeNode cn = codeNodesList.get(i);
			char newCharIndex = TextFragment.toChar(i);
			String newMarker = new String(cn.markerFlag+newCharIndex);
			codedTextReplace(cn.marker, newMarker);
			cn.intIndex = i;
			cn.charIndex = newCharIndex;
			cn.marker = newMarker;
		}
	}
		
	/*
	 * Generates the list of codes from the code node list
	 */
	private List<Code> getCodes () {
		List<Code> codes = new ArrayList<Code>(); 
		for(CodeNode cn : codeNodesList){
			codes.add(cn.code);
		}
		return codes;
	}
	
	/*
	 * Return codedText
	 */
	private String getCodedText () {
		return codedText;
	}

	/*
	 * Update the code ids  
	 */
	private void updateCodeIds () {
		// TODO: Remove code
		// JIMH - we now want to keep the original id's to make the merging easier
		// for cases where we don't have the original inlin code data to align on.
		
//		for(int i=0; i< codeNodesList.size(); i++){
//		
//			CodeNode cn = codeNodesList.get(i);
//
//			if(cn.code.getTagType() == TagType.OPENING){
//				
//				if (cn instanceof StartCodeNode) {
//					StartCodeNode scn = (StartCodeNode)cn;
//					scn.code.setId(i+1);
//					scn.endNode.code.setId(i+1);
//				}
//				else {
//					cn.code.setId(i+1);
//				}
//				
//			}else if(cn.code.getTagType() == TagType.PLACEHOLDER){
//
//				cn.code.setId(i+1);
//			}
//		}
	}
	
	/*
	 * Check if markers of two codes are adjacent
	 */
	private boolean adjacentMarkers (CodeNode node1, CodeNode node2) {
		return node1.offset + 2 == node2.offset;
	}
	
	/*
	 * merges codedText and codes for start and isolated nodes
	 */
	private void mergeNodes (CodeNode node1, CodeNode node2) {		
		String cst = node1.marker + node2.marker;
						
		// PH before Start/End merges to the Start/End		
		if (node1 instanceof PhCodeNode && 
				(node2 instanceof StartCodeNode || node2 instanceof EndCodeNode)) {
			codedTextReplace(cst, node2.marker);
			node2.setMergedData(node1, node2);
			codeNodesList.remove(node1);
		}
		// PH after Start/End merges to the Start/End
		else if (node2 instanceof PhCodeNode && 
				(node1 instanceof StartCodeNode || node1 instanceof EndCodeNode)) {
			codedTextReplace(cst, node1.marker);
			node1.setMergedData(node1, node2);
			codeNodesList.remove(node2);
		}		
		// PH + PH
		else {
			codedTextReplace(cst, node1.marker);
			node1.setMergedData(node1, node2);
			codeNodesList.remove(node2);
		}
	}
	
	private void updateOffsets(int start, int delta) {
		for (CodeNode node : codeNodesList) {
			if (node.offset > start) {
				node.offset += delta;
			}
		}
	}
	
	/*
	 * merges codedText and codes for ending nodes
	 */
	private void mergeEndNodes (CodeNode node1, CodeNode node2) {
		String cst = node1.marker + node2.marker;
		
		codedTextReplace(cst, node2.marker);
		node2.setMergedData(node1, node2);
		node2.offset = node1.offset;
		codeNodesList.remove(node1);		
	}

	/*
	 * merges codes for empty start/end tags
	 */
	private void mergeEmptyNodes(CodeNode node1, CodeNode node2){
		String cst = node1.marker + node2.marker;
		codedTextReplace(cst, new String("" + (char)TextFragment.MARKER_ISOLATED + node1.charIndex));
		
		node1.setMergedData(node1, node2);
		node1.code.setTagType(TagType.PLACEHOLDER);

		PhCodeNode pcn = new PhCodeNode(node1.offset,node1.intIndex, node1.charIndex, node1.code);
		
		int i = codeNodesList.indexOf(node2);
		
		codeNodesList.add(i, pcn);
		
		codeNodesList.remove(node1);
		codeNodesList.remove(node2);		
	}

	private void codedTextReplace(String findWhat, String replaceWith) {
		int startLen = codedText.length();
		int startIndex = codedText.indexOf(findWhat);
		codedText = codedText.replace(findWhat, replaceWith);
		int endLen = codedText.length();
		int delta = endLen - startLen;
		
		if (delta != 0) {
			updateOffsets(startIndex, delta);
		}
	}

	public void setRules(String rules) {
		this.rules = rules;
	}

	public boolean isPostSegmentation() {
		return postSegmentation;
	}

	public void setPostSegmentation(boolean postSegmentation) {
		this.postSegmentation = postSegmentation;
	}	
}