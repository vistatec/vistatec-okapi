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

package net.sf.okapi.filters.openxml;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.filters.PropertyTextUnitPlaceholder;

/**
 * Holds text and placeholder information for a list of tags.  This
 * is used in OpenXMLContentFilter as a temporary holding place
 * for information that will be needed to create a single code
 * for a sequence of tags such as &lt;w:r&gt;...&lt;w:t&gt;
 * or &lt;/w:t&gt;...&lt;/w:r&gt;.
 */
public class TextRun {
	
	private StringBuilder text = new StringBuilder();
	private List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders;

	public TextRun()
	{
		propertyTextUnitPlaceholders = null;
	}
	
	/**
	 * Appends a string to the text run. If the string is null, it is ignored.
	 * @param text The string to append.
	 */
	public void append (String text) {
		if ( text != null )
			this.text.append(text);
	}

	/**
	 * Appends an existing list placeholders to this text run.
	 * @param text optional text to add to the run
	 * @param offset the offset of the placeholders (added to the start / end)
	 * @param propertyTextUnitPlaceholders The existing placeholders to add.
	 */
	public void appendWithPropertyTextUnitPlaceholders(String text, int offset, List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders) {
		if ( text != null )
			this.text.append(text);
		for(PropertyTextUnitPlaceholder p : propertyTextUnitPlaceholders)
		{
			p.setMainStartPos(p.getMainStartPos()+offset);
			p.setMainEndPos(p.getMainEndPos()+offset);
			p.setValueStartPos(p.getValueStartPos()+offset);
			p.setValueEndPos(p.getValueEndPos()+offset);
			if (this.propertyTextUnitPlaceholders==null)
				this.propertyTextUnitPlaceholders = new ArrayList<PropertyTextUnitPlaceholder>();
			this.propertyTextUnitPlaceholders.add(p);
		}
	}

	public String getText()
	{
		return text.toString();
	}
	public void setText(String text)
	{
		this.text = new StringBuilder(text);
	}
	public List<PropertyTextUnitPlaceholder> getPropertyTextUnitPlaceholders()
	{
		return propertyTextUnitPlaceholders;
	}
	public void setPropertyTextUnitPlaceholders(List<PropertyTextUnitPlaceholder> propertyTextUnitPlaceholders)
	{
		this.propertyTextUnitPlaceholders = propertyTextUnitPlaceholders;
	}

	@Override
	public String toString() {
		return text.toString();
	}
}
