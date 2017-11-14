/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.table.fwc;

import java.util.List;

import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.filters.table.base.BaseTableFilter;
import net.sf.okapi.lib.extra.filters.TextProcessingResult;

/**
 * Fixed-Width Columns filter. Extracts text from a fixed-width columns table padded with white-spaces.
 * 
 * @version 0.1, 09.06.2009
 */
public class FixedWidthColumnsFilter extends BaseTableFilter {

	public static final String FILTER_NAME		= "okf_table_fwc";	
	public static final String FILTER_CONFIG	= "okf_table_fwc";
	
	public static String COLUMN_WIDTH	= "column_width";
	
	private Parameters params; // Fixed-Width Columns Filter parameters
	//protected List<Integer> columnWidths;
	protected List<Integer> columnStartPositions;
	protected List<Integer> columnEndPositions;
	
	public FixedWidthColumnsFilter() {
	
		setName(FILTER_NAME);

		addConfiguration(true, // Do not inherit configurations from Base Table Filter
						FILTER_CONFIG,
						"Table (Fixed-Width Columns)",
						"Fixed-width columns table padded with white-spaces.", 
						"okf_table_fwc.fprm");
		
		setParameters(new Parameters());	// Fixed-Width Columns Filter parameters
	}

	@Override
	protected void component_init() {
		
		// Commons, should be included in all descendants introducing own params
		params = getParameters(Parameters.class);	// Throws OkapiBadFilterParametersException
		super.component_init();
		
		// Initialization
		//columnWidths = ListUtil.stringAsIntList(params.columnWidths);
		columnStartPositions = ListUtil.stringAsIntList(params.columnStartPositions);
		columnEndPositions = ListUtil.stringAsIntList(params.columnEndPositions);
	}

	@Override
	protected TextProcessingResult extractCells(List<ITextUnit> cells, TextContainer lineContainer, long lineNum) {
		
		if (cells == null) return TextProcessingResult.REJECTED;
		if (lineContainer == null) return TextProcessingResult.REJECTED;
		
		String line = lineContainer.getCodedText();		
		if (Util.isEmpty(line)) return TextProcessingResult.REJECTED;
		
		int len = Math.min(columnStartPositions.size(), columnEndPositions.size());
		for (int i = 0; i < len; i++) {
			int start = columnStartPositions.get(i) - 1; // 0-base
			int end = columnEndPositions.get(i) - 1; // 0-base
			if (start >= end) continue;
			if (start >= line.length()) continue;
			if (end > line.length()) end = line.length(); 
			
			int skelEnd;
			if (i < len - 1)
				skelEnd = columnStartPositions.get(i + 1) - 1; // start of next column
			else
				skelEnd = line.length();

			if (skelEnd > line.length()) skelEnd = line.length();
			
			String srcPart = line.substring(start, end); // end is excluded
			String skelPart = line.substring(end, skelEnd);
			
			ITextUnit cell = TextUnitUtil.buildTU(srcPart, skelPart);
			// TODO check (end - start)
			cell.setSourceProperty(new Property(COLUMN_WIDTH, String.valueOf(end - start), true));
			cells.add(cell);
		}
		
		return TextProcessingResult.ACCEPTED;
	}	
}
