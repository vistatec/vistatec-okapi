/*===========================================================================
  Copyright (C) 2008-2009 by the Okapi Framework contributors
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

package net.sf.okapi.steps.tokenization.tokens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.steps.tokenization.common.Token;

/**
 * 
 * 
 * @version 0.1 08.07.2009
 */

public class Tokens extends ArrayList<Token> {

	private static final long serialVersionUID = 2484560539089898608L;
	
	private static TreeMap<Integer, TokenItem> idMap;
	private static TreeMap<String, TokenItem> nameMap;
	private static List<TokenItem> items;

	static {
		
		Parameters params = new Parameters();
		
		idMap = new TreeMap<Integer, TokenItem>();
		nameMap = new TreeMap<String, TokenItem>();

		if (params != null) { 
				
			params.loadItems();
			items = params.getItems();
			
			if (idMap != null && items != null && nameMap != null) {
	
				for (int i = 0; i < items.size(); i++) {
					
					TokenItem item = items.get(i);		
					if (item == null) continue;
			
					int id = i + 1; 
					
					idMap.put(id, item);
					nameMap.put(item.getName(), item);
				}
			}
		}
	}
	
	/**
	 * Return a list of Token objects. If tokenNames are specified, only the tokens with those names will be placed
	 * in the resulting list. If tokenNames is omitted, the list of all tokens will be returned. 
	 * @param tokenNames Optional array of strings with token name constants.
	 * @return List of tokens.
	 */
	public Tokens getFilteredList(String... tokenNames) {
		
		List<String> names = null; 
		
		if (tokenNames == null || (tokenNames != null && tokenNames.length == 0))
			return this;
		else
			names = Arrays.asList(tokenNames);

		Tokens res = new Tokens ();
		for (int i = 0; i < this.size(); i++) {
			
			Token token = this.get(i);
			
			if (token == null) continue;
			
			if (names.contains(token.getName()))
				res.add(token);
		}
		
		return res;
	}
	
	public void fixRanges(List<Integer> markerPositions) {
		
		for (Integer pos : markerPositions)			
			for (Token token : this) {
			
				if (token.getRange().start > pos)
					token.getRange().start += 2;
				
				if (token.getRange().end > pos)
					token.getRange().end += 2;
			}			
		}

	@Override
	public String toString() {

		List<String> res = new ArrayList<String>();
		
		for (Token token : this) {
			
			res.add(token.toString());
		}
		
		return ListUtil.listAsString(res, "\n");
	}
	
	public static int getTokenId(String tokenName) {
		
		if (nameMap == null) return 0;
		if (items == null) return 0;
		
		TokenItem item = nameMap.get(tokenName);
		
		return items.indexOf(item) + 1; // id is 1-based
	}
	
	public static String getTokenName(int tokenId) {
		
		if (idMap == null) return "";
		
		TokenItem item = idMap.get(tokenId);
		
		return (item != null) ? item.getName(): "";
	}
	
	public static String getTokenDescription(int tokenId) {
		
		if (idMap == null) return "";
		
		TokenItem item = idMap.get(tokenId);
		
		return (item != null) ? item.getDescription(): "";
	}
	
	public static String getTokenDescription(String tokenName) {
		
		if (nameMap == null) return "";
		
		TokenItem item = nameMap.get(tokenName);
		
		return (item != null) ? item.getDescription(): "";
	}
	
	public static String getTokenNamesStr() {
		
		if (nameMap == null) return "";
		
		return ListUtil.arrayAsString(nameMap.keySet().toArray(new String[] {}));
	}
	
	public static Collection<TokenItem> getTokenItems() {
		
		// Returns token items, sorted by id (params.getItems() doesn't guarantee it)
		if (idMap == null) return null;
		
		return idMap.values();
	}

	/**
	 * Gets list of names of all tokens.
	 * @return List of available token names.
	 */
	public static List<String> getTokenNames() {
	
		return new ArrayList<String>(nameMap.keySet());
	}
	
	/**
	 * Gets list of IDs of all tokens.
	 * @return List of available token IDs.
	 */
	public static List<Integer> getTokenIDs() {
	
		List<Integer> idList = new ArrayList<Integer>(); 
		
		for (String tokenName : nameMap.keySet())			
			idList.add(Tokens.getTokenId(tokenName));
		
		return idList; 
	}
	
	/**
	 * Gets list of IDs of the given tokens.
	 * @return List of token IDs.
	 */
	public static List<Integer> getTokenIDs(List<String> tokenNames) {
			
		List<Integer> idList = new ArrayList<Integer>(); 
	
		if (tokenNames != null)
			for (String tokenName : tokenNames)			
				idList.add(Tokens.getTokenId(tokenName));
		
		return idList; 
	}
	
	public void setImmutable(boolean immutable) {
		
		for (Token token : this)
			token.setImmutable(immutable);
	}
	
}
