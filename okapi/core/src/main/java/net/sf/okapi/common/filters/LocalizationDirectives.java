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

package net.sf.okapi.common.filters;

import java.util.Stack;

/**
 * Processes localization directives.
 */
public class LocalizationDirectives {

	private boolean useLD;
	private boolean localizeOutside;
	private Stack<Context> context;
	
	private class Context {

		boolean isGroup;
		boolean extract;
		
		Context (boolean isGroup,
			boolean extract)
		{
			this.isGroup = isGroup;
			this.extract = extract;
		}
	}

	/**
	 * Creates a new LocalizationDirectives object.
	 */
	public LocalizationDirectives () {
		reset();
	}
	
	/**
	 * Resets this localization directives processor.
	 */
	public void reset () {
		context = new Stack<Context>();
		setOptions(true, true);
	}
	
	/**
	 * Indicates if localization directives are to be used or not.
	 * @return True if localization directives should be processed,
	 * false if they should not.
	 */
	public boolean useLD () {
		return useLD;
	}
	
	/**
	 * Indicates if text outside the scope of localization directives should be
	 * extracted or not.
	 * @return True if text text outside the scope of localization directives is to
	 * be extracted, false if it should not. 
	 */
	public boolean localizeOutside () {
		if ( !useLD ) return true; // Always localize all when LD not used
		return localizeOutside;
	}
	
	/**
	 * Indicates if the current context is inside the scope of a localization directive.
	 * @return True if the current context is inside the scope of a localization directive,
	 * false if it is outside.
	 */
	public boolean isWithinScope () {
		return (context.size() > 0);
	}
	
	/**
	 * Indicates if the current context is localizable or not.
	 * @param popSingle Indicates if non-group directives should be popped
	 * out of the context when calling this method.
	 * @return True if the current context is localizable, or if localization directives
	 * are not to be used.
	 */
	public boolean isLocalizable (boolean popSingle) {
		// If LD not used always localize
		if ( !useLD ) return true;
		// Default
		boolean res = localizeOutside;
		if ( context.size() > 0 ) {
			res = context.peek().extract;
			if ( popSingle ) {
				// Pop only the non-group properties
				if ( !context.peek().isGroup ) {
					context.pop();
				}
			}
		}
		return res;
	}
	
	/**
	 * Sets the options for this localization directives processor.
	 * @param useLD Indicates if localization directives are to be used or not.
	 * @param localizeOutside Indicates if text outside the scope of localization 
	 * directives should be extracted or not.
	 */
	public void setOptions (boolean useLD,
		boolean localizeOutside)
	{
		this.useLD = useLD;
		this.localizeOutside = localizeOutside;
	}
	
	/**
	 * Evaluates a string that contain localization directives and update the object
	 * state based on the given instructions.
	 * @param content The text to process.
	 */
	public void process (String content) {
		// Check if we need to process
		if (( content == null ) || ( !useLD )) return;

		// Process
		content = content.toLowerCase();
		if ( content.lastIndexOf("_skip") > -1 ) {
			push(false, false);
		}
		else if ( content.lastIndexOf("_bskip") > -1 ) {
			push(true, false);
		}
		else if ( content.lastIndexOf("_eskip") > -1 ) {
			//TODO: check if groups are balanced
			popIfPossible();
		}
		else if ( content.lastIndexOf("_text") > -1 ) {
			push(false, true);
		}
		else if ( content.lastIndexOf("_btext") > -1 ) {
			push(true, true);
		}
		else if ( content.lastIndexOf("_etext") > -1 ) {
			//TODO: check if groups are balanced
			popIfPossible();
		}
	}

	private void push (boolean isGroup,
		boolean extract)
	{
		// Pop top context if it's a single
		if ( context.size() > 0 ) {
			if ( !context.peek().isGroup ) context.pop(); 
		}
		// Add new context
		context.add(new Context(isGroup, extract));
	}

	private void popIfPossible () {
		if ( context.size() > 0 ) context.pop();
	}
	
}
