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

package net.sf.okapi.filters.table;

import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.filters.table.csv.CommaSeparatedValuesFilter;
import net.sf.okapi.filters.table.fwc.FixedWidthColumnsFilter;
import net.sf.okapi.filters.table.tsv.TabSeparatedValuesFilter;
import net.sf.okapi.lib.extra.filters.CompoundFilter;

/**
 * Table filter, processes table-like files such as tab-delimited, CSV, fixed-width columns, etc.
 * 
 * @version 0.1, 09.06.2009
 */
@UsingParameters(Parameters.class)
public class TableFilter extends CompoundFilter {
		
	public static final String FILTER_NAME		= "okf_table";
	public static final String FILTER_MIME		= MimeTypeMapper.CSV_MIME_TYPE;	
	public static final String FILTER_CONFIG	= "okf_table";
	
	public TableFilter() {
		
		super();
		
		setName(FILTER_NAME);
		setDisplayName("Table Filter");
		setMimeType(FILTER_MIME);
		setParameters(new Parameters());	// Table Filter parameters

		addConfiguration(true, 
				FILTER_CONFIG,
				"Table Files",
				"Table-like files such as tab-delimited, CSV, fixed-width columns, etc.", 
				null);
	
		addSubFilter(CommaSeparatedValuesFilter.class);
		addSubFilter(FixedWidthColumnsFilter.class);
		addSubFilter(TabSeparatedValuesFilter.class);
	}
		
}
