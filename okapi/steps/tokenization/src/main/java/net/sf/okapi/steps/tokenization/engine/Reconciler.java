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

package net.sf.okapi.steps.tokenization.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Range;
import net.sf.okapi.common.Util;
import net.sf.okapi.steps.tokenization.common.AbstractLexer;
import net.sf.okapi.steps.tokenization.common.Lexem;
import net.sf.okapi.steps.tokenization.common.Lexems;
import net.sf.okapi.steps.tokenization.common.Token;
import net.sf.okapi.steps.tokenization.tokens.Tokens;

public class Reconciler extends AbstractLexer {

	private LinkedHashMap<String, List<Integer>> sameRangeMap;
	private LinkedHashMap<Integer, List<Integer>> sameScoreMap; 
	private LinkedHashMap<Integer, List<Integer>> reverseSameScoreMap;
	private Tokens deletedTokens;

	@Override
	protected boolean lexer_hasNext() {

		return false;
	}

	@Override
	protected void lexer_init() {

		sameRangeMap = new LinkedHashMap<String, List<Integer>>(); 
		sameScoreMap = new LinkedHashMap<Integer, List<Integer>>();
		reverseSameScoreMap = new LinkedHashMap<Integer, List<Integer>>();
		deletedTokens = new Tokens();
	}

	@Override
	protected Lexem lexer_next() {

		return null;
	}

	@Override
	protected void lexer_open(String text, LocaleId language, Tokens tokens) {
	}

	/**
	 * Returns true if range2 is within range.
	 * @param range
	 * @param range2
	 * @return
	 */
	private boolean contains(Range range, Range range2) {

		// Exact matches are dropped
		return (range.start < range2.start && range.end >= range2.end) ||
			(range.start <= range2.start && range.end > range2.end);
	}
	
	private String formRangeId(Range range) {
		
		if (range == null) return null;
		
		return String.format("%d %d", range.start, range.end);
	}

	private void setSameRangeMap(Tokens tokens) {
		
		// Create a map of lists of indices of tokens with equal ranges
		if (tokens == null) return;
		if (sameRangeMap == null) return;
		
		sameRangeMap.clear();
		
		for (int index = 0; index < tokens.size(); index++) {
			
			Token token = tokens.get(index);
			if (token == null) continue;
			if (token.isDeleted()) continue;
			
			String rangeId = formRangeId(token.getRange());
			List<Integer> list = sameRangeMap.get(rangeId);
			if (list == null) {
				
				list = new ArrayList<Integer>();
				sameRangeMap.put(rangeId, list);
			}
			
			list.add(index);
		}		
	}
	
	private	boolean getSameTokenIds(Token token1, Token token2) {
		
		return token1.getTokenId() == token2.getTokenId();
	}
	
	private boolean getSameRules(Token token1, Token token2) {
		
		return (token1.getLexerId() == token2.getLexerId()) &&		
			(token1.getLexemId() == token2.getLexemId());
	}
			
	private void setSameScoreMap(Tokens tokens) {
		
		// Create a map of lists of tokens which score change is synchronized (change in the score of one 
		// token should be applied to other tokens in the group too)
		if (tokens == null) return;
		if (Util.isEmpty(sameRangeMap)) return;
		if (sameScoreMap == null) return;
		
		sameScoreMap.clear();
		reverseSameScoreMap.clear();
		
		// Create the direct and reverse same-score maps
		for (List<Integer> list : sameRangeMap.values())
			for (int i = 0; i < list.size(); i++) {
				
				int index1 = list.get(i);
				Token token1 = tokens.get(index1);
				if (token1 == null) continue;
				if (token1.isDeleted()) continue;
				
				for (int j = 0; j < list.size(); j++) {
					
					if (i >= j) continue;
					
					int index2 = list.get(j);
					Token token2 = tokens.get(index2);
					if (token2 == null) continue;					
					if (token2.isDeleted()) continue;
					
					if (token2 == token1) continue;
					
					boolean sameTokenIds = getSameTokenIds(token1, token2);
					boolean sameRules = getSameRules(token1, token2); 
					
					if ((sameTokenIds && sameRules) || // Erroneous duplication of the same token in the rule's outTokens 
						(!sameTokenIds && sameRules) || // Both tokens are listed on the rule's outTokens
						(sameTokenIds && !sameRules)) { // 2 different rules recognize this range as the same token
			
						// Update token1 group
						List<Integer> groupIndices = sameScoreMap.get(index1);
						if (groupIndices == null) {
							
							groupIndices = new ArrayList<Integer>();
							sameScoreMap.put(index1, groupIndices); // token1 index
						}
						
						if (!groupIndices.contains(index2))
							groupIndices.add(index2);
						
						// Update token2 group
						List<Integer> groupIndices2 = reverseSameScoreMap.get(index2);
						if (groupIndices2 == null) {
							
							groupIndices2 = new ArrayList<Integer>();
							reverseSameScoreMap.put(index2, groupIndices2); // token2 index
						}
						
						if (!groupIndices2.contains(index1))
							groupIndices2.add(index1);
					}					
				}
			}
				
		// Merge 2 maps together (Okapi-B 45**)
		for (Integer key : sameScoreMap.keySet()) {
		
			List<Integer> groupIndices = sameScoreMap.get(key);
			if (groupIndices == null) continue;
			
			for (int i = groupIndices.size() - 1; i >= 0; i--) {
				
				int index = groupIndices.get(i);
				//Token token = tokens.get(index);
								
				List<Integer> groupIndices2 = sameScoreMap.get(index);
				if (groupIndices2 != null) {
				
					for (int index2 : groupIndices2) {
						
						if (index2 == index) continue;
						if (groupIndices.contains(index2)) continue; 
						groupIndices.add(index2);
					}
				}
				
				List<Integer> reverseIndices = reverseSameScoreMap.get(index);
				if (reverseIndices == null) continue;
				
				for (int index2 : reverseIndices) {
					
					if (index2 == key) continue;
					if (groupIndices.contains(index2)) continue;
					groupIndices.add(index2);
				}
			}
		}
		
	}
	
	private boolean firstIsNewer(Token token1, Token token2) {
		
		if (token1 == null) return false;
		if (token2 == null) return true;
		
		return (token1.getLexerId() > token2.getLexerId()) ||
		((token1.getLexerId() == token2.getLexerId()) && (token1.getLexemId() > token2.getLexemId()));
	}
	
	private void biggerEatsSmaller(Token bigger, Token smaller) {
		
		if (bigger == null) return;
		if (smaller == null) return;
		
		if (bigger.isDeleted()) return;
		if (!bigger.isImmutable() && smaller.isImmutable()) return; // Only immutables can eat immutables
		
		smaller.delete();
	}
	
	public Lexems process(String text, LocaleId language, Tokens tokens) {
		
		setSameRangeMap(tokens);
		setSameScoreMap(tokens);
					
		// Remove tokens included in other tokens etc. (Okapi-B 43)
		
		for (int i = 0; i < tokens.size(); i++) {
			
			Token token1 = tokens.get(i);
			if (token1.isDeleted()) continue;
			
			for (int j = 0; j < tokens.size(); j++) {
				
				if (i >= j) continue;
				
				Token token2 = tokens.get(j);				
				if (token2.isDeleted()) continue;
				if (token2 == token1) continue;				
			
				Range r1 = token1.getRange();
				Range r2 = token2.getRange();
												
				if (r1.start == r2.start && r1.end == r2.end) { // Equal ranges
					
					// Tokens are identical, remove duplication
					if (token1.getTokenId() == token2.getTokenId()) { 
						
						if (firstIsNewer(token1, token2))
							token2.delete();
						else
							token1.delete();
						
						continue;
					}
					
					// One of the tokens is on the other's InTokens list; delete if KeepInput=false, keep if KeepInput=true 
					
					
				}
				else { // One of the ranges contains the other, or no overlapping
				
					if (contains(r1, r2))						
						biggerEatsSmaller(token1, token2);
					
					else if (contains(r2, r1))						
						biggerEatsSmaller(token2, token1);
					
					else
						continue; // The two ranges don't overlap
				}
				
			}
		}
				
		// Set scores for equal range tokens (Okapi-B 45*)
		
		// Step 1. Temporarily *delete* tokens of all same-score groups to exclude them at step 2
		// First create a list of already deleted tokens not to set them the score > 0% at step 3
		deletedTokens.clear();
		for (Token token : tokens) {
			
			if (token == null) continue;
			if (token.isDeleted())
				deletedTokens.add(token);
		}
		
		for (Integer key : sameScoreMap.keySet()) {
			if (!Util.checkIndex(key, tokens)) continue;
			Token token = tokens.get(key);
			if (token == null) continue;
			if (token.isDeleted()) continue;
			
			List<Integer> list = sameScoreMap.get(key);
			for (Integer index2 : list) {
				
				token = tokens.get(index2);
				if (token != null)
					token.delete();
			}				
		}
		
		// Step 2. Count valid tokens in same-range lists 
		for (List<Integer> list : sameRangeMap.values()) { 
			
			int size = 0;
			for (Integer index : list) {
				
				if (!Util.checkIndex(index, tokens)) continue;
				Token token = tokens.get(index);
				if (token == null) continue;
				if (token.isDeleted()) continue;
				size++;
			}
			
			for (Integer index : list) {
				
				Token token = tokens.get(index);	
				if (token == null) continue;
				if (token.isDeleted()) continue;
				
				token.setScore(100 / size);
				token.undelete();
			}			
		}
		
		// Step 3. Fix same-score groups
		for (Integer index : sameScoreMap.keySet()) {
			
			if (!Util.checkIndex(index, tokens)) continue;
			Token token = tokens.get(index);
			if (token == null) continue;
			//if (token.isDeleted()) continue;
			if (deletedTokens.contains(token)) continue;
			
			int score = token.getScore();
			
			// Set the same score to all tokens in its same-score group
			List<Integer> groupIndices = sameScoreMap.get(index);
			if (groupIndices == null) continue;
			
			for (Integer groupIndex : groupIndices) {
				
				Token groupToken = tokens.get(groupIndex);
				//if (!groupToken.isDeleted())
				if (deletedTokens.contains(groupToken)) continue;
				groupToken.setScore(score);
				groupToken.undelete();
			}				
		}

		return null;
	}

}
