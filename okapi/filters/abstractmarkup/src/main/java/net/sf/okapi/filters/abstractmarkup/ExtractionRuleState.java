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

package net.sf.okapi.filters.abstractmarkup;

import java.util.EmptyStackException;
import java.util.ListIterator;
import java.util.Stack;

import net.sf.okapi.filters.abstractmarkup.config.TaggedFilterConfiguration.RULE_TYPE;

/**
 * Holds the current parser's rule state. State is maintained on separate
 * stacks for each type of {@link RULE_TYPE}
 *  
 * @author HargraveJE
 *
 */
public class ExtractionRuleState {

	/**
	 * This class carries the rule and other information
	 * @author HargraveJE
	 *
	 */
	public final class RuleType {
		public String ruleName;
		public RULE_TYPE ruleType;
		public String idValue;
		public boolean ruleApplies;
		
		public RuleType(String ruleName, RULE_TYPE ruleType, String idValue) {
			this.ruleName = ruleName;
			this.ruleType = ruleType;
			this.idValue = idValue;	
			this.ruleApplies = true;
		}
		
		public RuleType(String ruleName, RULE_TYPE ruleType, boolean ruleApplies) {
			this.ruleName = ruleName;
			this.ruleType = ruleType;
			this.idValue = null;			
			this.ruleApplies = ruleApplies;
		}
	}
	
	// if no other rules are active, what do we do?
	private boolean excludeByDefault;

	// for rules without primary conditions
	private Stack<RuleType> preserveWhiteSpaceRuleStack;
	private Stack<RuleType> excludedIncludedRuleStack;
	private Stack<RuleType> groupRuleStack;
	private Stack<RuleType> textUnitRuleStack;
	private Stack<RuleType> inlineRuleStack;

	/**
	 * 
	 */
	public ExtractionRuleState(boolean preserveWhitespace,
								boolean excludeByDefault) {
		reset(preserveWhitespace, excludeByDefault);
	}

	public void reset(boolean preserveWhitespace,
					   boolean excludeByDefault) {
		this.excludeByDefault = excludeByDefault;
		preserveWhiteSpaceRuleStack = new Stack<RuleType>();
		pushPreserverWhitespaceRule(preserveWhitespace);
		
		excludedIncludedRuleStack = new Stack<RuleType>();
		groupRuleStack = new Stack<RuleType>();
		textUnitRuleStack = new Stack<RuleType>();
		inlineRuleStack = new Stack<RuleType>();
	}

	public boolean isExludedState() {
		if (excludedIncludedRuleStack.isEmpty()) {
			return excludeByDefault;
		}
		
		// reverse the stack as we want to see the most recently added first
		//ReversedIterator<RuleType> ri = new ReversedIterator<RuleType>(excludedIncludedRuleStack);
		// JEH 9/16/13 ReversedIterator was very slow - try a listIterator and move backwards 
		ListIterator<RuleType> ri = excludedIncludedRuleStack.listIterator(excludedIncludedRuleStack.size());
		while (ri.hasPrevious()) {
			RuleType rt = ri.previous();		
			if (rt.ruleType == RULE_TYPE.EXCLUDED_ELEMENT) {
				return true;
			}
			
			if (rt.ruleType == RULE_TYPE.INCLUDED_ELEMENT) {
				return false;
			}
		}
		
		return excludeByDefault;
	}

	public boolean isInlineExcludedState() {
		if (inlineRuleStack.isEmpty()) {
			return false;
		}

		// reverse the stack as we want to see the most recently added first
		//ReversedIterator<RuleType> ri = new ReversedIterator<RuleType>(inlineRuleStack);
		// JEH 9/16/13 ReversedIterator was very slow - try a listIterator and move backwards 
		ListIterator<RuleType> ri = inlineRuleStack.listIterator(inlineRuleStack.size());
		while (ri.hasPrevious()) {
			RuleType rt = ri.previous();
			if (rt.ruleType == RULE_TYPE.INLINE_EXCLUDED_ELEMENT) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isPreserveWhitespaceState() {
		if (preserveWhiteSpaceRuleStack.isEmpty()) {
			return false;
		}
		return preserveWhiteSpaceRuleStack.peek().ruleApplies;
	}

	public void pushPreserverWhitespaceRule(boolean ruleApplies) {
		preserveWhiteSpaceRuleStack.push(new RuleType("", RULE_TYPE.PRESERVE_WHITESPACE, ruleApplies));
	}

	public void pushPreserverWhitespaceRule(String ruleName, boolean ruleApplies) {
		preserveWhiteSpaceRuleStack.push(new RuleType(ruleName, RULE_TYPE.PRESERVE_WHITESPACE, ruleApplies));
	}

	public void pushExcludedRule(String ruleName) {
		excludedIncludedRuleStack.push(new RuleType(ruleName, RULE_TYPE.EXCLUDED_ELEMENT, null));
	}

	public void pushExcludedRule(String ruleName, RULE_TYPE rule) {
		excludedIncludedRuleStack.push(new RuleType(ruleName, rule, null));
	}
	
	public void pushIncludedRule(String ruleName) {
		excludedIncludedRuleStack.push(new RuleType(ruleName, RULE_TYPE.INCLUDED_ELEMENT, null));
	}
	
	public void pushIncludedRule(String ruleName, RULE_TYPE rule) {
		excludedIncludedRuleStack.push(new RuleType(ruleName, rule, null));
	}

	public void pushGroupRule(String ruleName) {
		groupRuleStack.push(new RuleType(ruleName, RULE_TYPE.GROUP_ELEMENT, null));
	}
	
	public void pushGroupRule(String ruleName, RULE_TYPE rule) {
		groupRuleStack.push(new RuleType(ruleName, rule, null));
	}
	
	public void pushInlineRule(String ruleName) {
		inlineRuleStack.push(new RuleType(ruleName, RULE_TYPE.INLINE_ELEMENT, null));
	}
	
	public void pushInlineRule(String ruleName, RULE_TYPE rule) {
		inlineRuleStack.push(new RuleType(ruleName, rule, null));
	}
	
	public void pushTextUnitRule(String ruleName) {
		textUnitRuleStack.push(new RuleType(ruleName, RULE_TYPE.TEXT_UNIT_ELEMENT, null));
	}
	
	public void pushTextUnitRule(String ruleName, RULE_TYPE rule, String idValue) {
		textUnitRuleStack.push(new RuleType(ruleName, rule, idValue));
	}

	public RuleType popPreserverWhitespaceRule() {
		return preserveWhiteSpaceRuleStack.pop();
	}

	public RuleType popExcludedIncludedRule() {
		return excludedIncludedRuleStack.pop();
	}

	public RuleType popGroupRule() {
		return groupRuleStack.pop();
	}

	public RuleType popTextUnitRule() {		
		return textUnitRuleStack.pop();
	}
	
	public RuleType popInlineRule() {		
		return inlineRuleStack.pop();
	}

	public RuleType peekPreserverWhitespaceRule() {
		return preserveWhiteSpaceRuleStack.peek();
	}

	public RuleType peekExcludedIncludedRule() {
		return excludedIncludedRuleStack.peek();
	}

	public RuleType peekGroupRule() {
		return groupRuleStack.peek();
	}

	public RuleType peekTextUnitRule() {		
		return textUnitRuleStack.peek();
	}
	
	public RuleType peekInlineRule() {		
		return inlineRuleStack.peek();
	}
	
	public void clearTextUnitRules() {
		textUnitRuleStack.clear();
	}
		
	public void clearInlineRules() {
		inlineRuleStack.clear();
	}
		
	public String getTextUnitElementName() {
		String n = "";
		try {
			n = textUnitRuleStack.peek().ruleName;
		} catch (EmptyStackException e) {
			// eat exception
		}
		return n;
	}
}
