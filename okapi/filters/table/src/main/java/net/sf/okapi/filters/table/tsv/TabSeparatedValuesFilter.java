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

package net.sf.okapi.filters.table.tsv;

import java.util.List;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.filters.table.base.BaseTableFilter;
import net.sf.okapi.lib.extra.filters.TextProcessingResult;

/**
 * Tab-Separated Values filter. Extracts columns, separated by one or more tabs.
 * 
 * @version 0.1, 09.06.2009
 */
public class TabSeparatedValuesFilter  extends BaseTableFilter {
	
	public static final String FILTER_NAME		= "okf_table_tsv";
	public static final String FILTER_CONFIG	= "okf_table_tsv";
	
	public TabSeparatedValuesFilter() {
		
		super();		
		
		setName(FILTER_NAME);
		// Parameters are set in Base Table Filter

		addConfiguration(true, // Do not inherit configurations from Base Table Filter
				FILTER_CONFIG,
				"Table (Tab-Separated Values)",
				"Columns, separated by one or more tabs.", 
				"okf_table_tsv.fprm");
		
		setParameters(new Parameters());	// Tab-Separated Values Filter parameters
	}

	@Override
	protected TextProcessingResult extractCells(List<ITextUnit> cells, TextContainer lineContainer, long lineNum) {
		
		if (cells == null) return TextProcessingResult.REJECTED;
		if (lineContainer == null) return TextProcessingResult.REJECTED;
		
		String line = lineContainer.getCodedText();
		if (Util.isEmpty(line)) return TextProcessingResult.REJECTED;
		
		int start = -1;
		int prevStart = -1;
		
		for (int i = 0; i < line.length(); i++) {
						
			if (start > -1 && line.charAt(i) == '\t') {
				if (prevStart > -1)
					cells.add(TextUnitUtil.buildTU(line.substring(prevStart, start)));
				
				prevStart = start;
				start = -1;
				continue;
			}
			
			if (start == -1 && line.charAt(i) >= ' ') {
				start = i;
			}						
		}
		
		if (start == -1) start = line.length();
		
		if (prevStart > -1 && start > -1)
			cells.add(TextUnitUtil.buildTU(line.substring(prevStart, start)));
		
		if (start < line.length()) {
			cells.add(TextUnitUtil.buildTU(line.substring(start, line.length())));
		}
		
		return TextProcessingResult.ACCEPTED;
	}
}
