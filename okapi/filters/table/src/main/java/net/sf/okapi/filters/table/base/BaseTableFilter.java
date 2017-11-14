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

package net.sf.okapi.filters.table.base;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.EventType;
import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.plaintext.base.BasePlainTextFilter;
import net.sf.okapi.filters.table.csv.CommaSeparatedValuesFilter;
import net.sf.okapi.lib.extra.filters.AbstractLineFilter;
import net.sf.okapi.lib.extra.filters.TextProcessingResult;

/**
 * 
 * 
 * @version 0.1, 09.06.2009
 */
public class BaseTableFilter extends BasePlainTextFilter {
	
	public static final String FILTER_NAME		= "okf_csv";
	public static final String FILTER_MIME		= MimeTypeMapper.CSV_MIME_TYPE;	
	public static final String FILTER_CONFIG	= "okf_csv";
	
	public static String ROW_NUMBER		= "row_number";
	public static String COLUMN_NUMBER	= "column_number";			
	
	private Parameters params; // Base Table Filter parameters
	
	protected List<Integer> sourceColumns;
	protected List<Integer> targetColumns;
	protected List<Integer> targetSourceRefs;
	protected List<Integer> commentColumns;
	protected List<Integer> commentSourceRefs;	
	protected List<Integer> sourceIdColumns;
	protected List<Integer> sourceIdSourceRefs;
	
	protected List<String> columnNames;
	protected List<String> sourceIdSuffixes;
	protected List<LocaleId> targetLanguages;
	
	private int rowNumber = 0;
	private boolean inHeaderArea = true;
	private boolean sendListedMode = false;
	private boolean inMultilineColumnNames = false;
	private boolean isHeaderLine;
	private boolean isColumnNames;
	private boolean isFixedNumColumns;
	
	private LocaleId columnDefinedSource;
	private List<LocaleId> columnDefinedTargets;
	
	public BaseTableFilter() {	
		setName(FILTER_NAME);
		setMimeType(FILTER_MIME);
		columnNames = new ArrayList<String>();				
		setParameters(new Parameters());	// Base Table Filter parameters
	}

	@Override
	protected void component_init() {
		
		// Commons, should be included in all descendants introducing own params
		params = getParameters(Parameters.class);	// Throws OkapiBadFilterParametersException
		super.component_init();
		
		// Initialization
		sourceIdColumns = ListUtil.stringAsIntList(params.sourceIdColumns);
		sourceColumns = ListUtil.stringAsIntList(params.sourceColumns);
		targetColumns = ListUtil.stringAsIntList(params.targetColumns);
		targetLanguages = ListUtil.stringAsLanguageList(params.targetLanguages);
		commentColumns = ListUtil.stringAsIntList(params.commentColumns);
		targetSourceRefs = ListUtil.stringAsIntList(params.targetSourceRefs);
		commentSourceRefs = ListUtil.stringAsIntList(params.commentSourceRefs);
		sourceIdSourceRefs = ListUtil.stringAsIntList(params.sourceIdSourceRefs);
		sourceIdSuffixes = ListUtil.stringAsList(params.sourceIdSuffixes);
										
		sendListedMode = params.sendColumnsMode == Parameters.SEND_COLUMNS_LISTED;
		setMultilingual(sendListedMode && targetColumns.size() > 0);
		
		rowNumber = 0;
		
		if (columnNames != null) 
			columnNames.clear();
		else
			columnNames = new ArrayList<String>();
		
		inMultilineColumnNames = false;
		
		columnDefinedSource = null;
		columnDefinedTargets = new LinkedList<>();
	}

	@Override
	protected TextProcessingResult component_exec(TextContainer lineContainer) {		
		
		if (lineContainer == null || (!isMerging() && lineContainer.isEmpty())) return TextProcessingResult.REJECTED;
				
		Property lineNumProp = lineContainer.getProperty(AbstractLineFilter.LINE_NUMBER);
		long lineNum = new Long(lineNumProp.getValue());

		updateLineInfo(lineNum);
		
		// no matter the header or column name extraction settings if this is
		// the column name line and either source or target locales are EMPTY 
		// then we must parse the locale names from the column names
		if (isColumnNames && (srcLang == LocaleId.EMPTY || trgLang == LocaleId.EMPTY)) {
		    List<String> columnNames = new ArrayList<>();
		    // must make exception for comma delimited filter becuase it does not
		    // populate TextUnit list in extractCells
		    if (this instanceof CommaSeparatedValuesFilter) { 
		        for (String column : ListUtil.stringAsArray(lineContainer.getCodedText(), getFieldDelimiter())) {		    
		            columnNames.add(StringUtil.removeQualifiers(column.trim(), getFieldQualifier()));            		                                                                    
		        }
		    } else {
		        List<ITextUnit> cn = new ArrayList<>();
		        extractCells(cn, lineContainer, lineNum);
		        for (ITextUnit tu: cn) {
		            columnNames.add(tu.getSource().getCodedText().trim());
		        }
		    }
		    
	        int i = 1;
            for (String l : columnNames) {
                // if source and/or target columns are defined
                // and the column name is a legal locale then use it and
                // send a PipeParameter event to update the pipeline locales
                if (LocaleId.EMPTY == srcLang && isSource(i)) {
                    try {
                        columnDefinedSource = LocaleId.fromString(l);                      
                    } catch (IllegalArgumentException e) {}
                } else if (LocaleId.EMPTY == trgLang && isTarget(i)) {
                    try {
                        columnDefinedTargets.add(LocaleId.fromString(l));                      
                    } catch (IllegalArgumentException e) {}
                }       
                i++;
            }
                        
            // if there are any column locales defined send them as a PipelineParameters
            PipelineParameters pp = null;
            
            // check for a defined source locale
            if (columnDefinedSource != null) {
                pp = new PipelineParameters(startDoc, input, null, null);
                pp.setSourceLocale(columnDefinedSource);
                srcLang = columnDefinedSource;
            }
            
            // check for defined target locales
            if (!columnDefinedTargets.isEmpty()) {
                if (pp == null) pp = new PipelineParameters(startDoc, input, null, null);
                if (columnDefinedTargets.size() <= 1) {
                    pp.setTargetLocale(columnDefinedTargets.get(0));
                    trgLang = columnDefinedTargets.get(0);
                } else {
                    pp.setTargetLocales(columnDefinedTargets);
                }
            }
            
            // send the PipelineParameters event
            if (pp != null) {
                sendEvent(EventType.PIPELINE_PARAMETERS, pp);
            }           
		}
		
		if (inHeaderArea && params.sendHeaderMode == Parameters.SEND_HEADER_NONE)  
			return TextProcessingResult.REJECTED;
		
		if (inHeaderArea && !isColumnNames && params.sendHeaderMode == Parameters.SEND_HEADER_COLUMN_NAMES_ONLY) 
			return TextProcessingResult.REJECTED;
		
		if (inHeaderArea)			
			rowNumber = 0;
		else {			
			if (rowNumber <= 0)
				rowNumber = 1;
			else
				rowNumber++;
		}

		// Send regular header lines (not column names) as a whole
		if (isHeaderLine) {
			
			lineContainer.setProperty(new Property(ROW_NUMBER, String.valueOf(rowNumber), true));  // rowNumber = 0 for header rows
			return super.sendAsSource(lineContainer);
		}
		
		List<ITextUnit> cells = new ArrayList<ITextUnit>(); 
		TextProcessingResult res = extractCells(cells, lineContainer, lineNum);
		
		switch (res) {
		case REJECTED:
			return res;
		case DELAYED_DECISION:
			if ( isColumnNames ) inMultilineColumnNames = true;
			return res;
		case ACCEPTED:
		case NONE:
			break;
		}
			
		if (isColumnNames) inMultilineColumnNames = false;
		if (Util.isEmpty(cells)) return super.sendAsSource(lineContainer); // No chunks, process the whole line

		// Assign missing id's
		for (int i = 0; i < cells.size(); i++)	{
			ITextUnit cell = cells.get(i);
			if (Util.isEmpty(cell.getId()))
				cell.setId(String.format("%d_%d", lineNum, i + 1));
		}
		
		if (processCells(cells, lineNum))
			return TextProcessingResult.ACCEPTED;
		else 
			return TextProcessingResult.REJECTED;
	}
	
	/**
	 * Splits line into table cells. 
	 * @param line string containing separated cells
	 * @return string array of cells
	 */
	protected TextProcessingResult extractCells(List<ITextUnit> cells, TextContainer lineContainer, long lineNum) {		
		// To be overridden in descendant classes
		
		if (cells != null) cells.add(TextUnitUtil.buildTU(lineContainer));
		
		return TextProcessingResult.ACCEPTED; 
	}
	
	protected String getFieldDelimiter() {
		
		return null;
	}
	
	protected String getFieldQualifier() {   
        return null;
    }
	
	protected boolean processCells(List<ITextUnit> cells, long lineNum) {
		// Processes cells of one line
		// To be called from descendants, least likely overridden

		if (params.sendColumnsMode == Parameters.SEND_COLUMNS_NONE) return false;
		if (cells == null) return false;		
		
		updateLineInfo(lineNum);
		
		// If a fixed number of columns is expected, truncate extra chunks, or pad with empty chunks for missing
		if (isFixedNumColumns) {
						
			if (cells.size() < params.numColumns)
				for (int i = cells.size(); i < params.numColumns; i++)
					cells.add(TextUnitUtil.buildTU(""));

			if (cells.size() > params.numColumns)
				cells.subList(params.numColumns, cells.size()).clear();
		}
								
		if (isColumnNames) {
			columnNames.clear();
			for (ITextUnit tu : cells) {
				String st = TextUnitUtil.getSourceText(tu).trim();
				columnNames.add(st);
			}
						
			if (params.detectColumnsMode == Parameters.DETECT_COLUMNS_COL_NAMES)
				params.numColumns = cells.size();
		}
				
		boolean tuSent = false;
		int startGroupIndex = getQueueSize();
		
		// Send all cells
		if (params.sendColumnsMode == Parameters.SEND_COLUMNS_ALL || inHeaderArea) {
			
			for (int i = 0; i < cells.size(); i++)	{
				
				if (i > 0) sendAsSkeleton(getFieldDelimiter());
				
				ITextUnit cell = cells.get(i);
				int colNumber = i + 1;
				
				if (TextUnitUtil.isEmpty(cell, true)) {  // only spaces, no translatable text
					sendAsSkeleton(cell);
					continue;
				}					
								
				cell.setSourceProperty(new Property(AbstractLineFilter.LINE_NUMBER, String.valueOf(lineNum), true));				
				cell.setSourceProperty(new Property(COLUMN_NUMBER, String.valueOf(colNumber), true));
				cell.setSourceProperty(new Property(ROW_NUMBER, String.valueOf(rowNumber), true));  // rowNumber = 0 for header rows
				
				if (sendAsSource(cell) != TextProcessingResult.ACCEPTED) continue;
				tuSent = true;
			}					
		}
		
		// Send only listed cells (id, source, target, comment)
		else if (sendListedMode) {
							
			String recordId = "";
			
			// Add content of other columns to the created sources
			for (int i = 0; i < cells.size(); i++)	{
				
				ITextUnit cell = cells.get(i); // Can be empty								
				ITextUnit temp = new TextUnit("temp", TextUnitUtil.getSourceText(cell)); 
				TextUnitUtil.trimTU(temp, true, true);
				String trimmedCell = TextUnitUtil.getSourceText(temp);
				
				int colNumber = i + 1;
				
				if (isRecordId(colNumber))
					recordId = trimmedCell;
				
				if (isSourceId(colNumber)) {
					
					ITextUnit tu = getSourceFromIdRef(cells, colNumber);
					if (tu == null) continue;										
					
					if (TextUnitUtil.isEmpty(cell, true)) {

						String recordID = ""; 
						int index = params.recordIdColumn - 1;
						
						if (Util.checkIndex(index, cells))
							recordID = TextUnitUtil.getSourceText(cells.get(index));
						
						if (recordID != null) recordID = recordID.trim();
						
						String colSuffix = getSuffixFromSourceRef(colNumber);
						
						if (!Util.isEmpty(recordID) && !Util.isEmpty(colSuffix))
							tu.setName(recordID + colSuffix);
					}
					else
						tu.setName(trimmedCell);
															
					continue;
				}
				
				if (isComment(colNumber)) {
					
					ITextUnit tu = getSourceFromCommentRef(cells, colNumber);
					if (tu == null) continue;
					if (Util.isEmpty(trimmedCell)) continue;
					
					tu.setProperty(new Property(Property.NOTE, trimmedCell));
					
					continue;
				}
				
				if (isSource(colNumber)) {
					
					if (cell == null) continue;
					
					cell.setSourceProperty(new Property(AbstractLineFilter.LINE_NUMBER, String.valueOf(lineNum), true));				
					cell.setSourceProperty(new Property(COLUMN_NUMBER, String.valueOf(colNumber), true));
					cell.setSourceProperty(new Property(ROW_NUMBER, String.valueOf(rowNumber), true));  // rowNumber = 0 for header rows
					
					continue;
				}				
			}

			
			// 1. Send all TUs with their skeletons 
			for (int i = 0; i < cells.size(); i++)	{				
				ITextUnit cell = cells.get(i); // Can be empty				
				int colNumber = i + 1;
				if (params.subfilter != null) {
					if (i > 0) sendAsSkeleton(getFieldDelimiter());
				}
				if (isSource(colNumber)) {
					
					if (cell == null) continue;

					if (Util.isEmpty(cell.getName()) && sendListedMode && !Util.isEmpty(recordId))
						cell.setName(recordId + getSuffix(colNumber));
					
					// subfilter controls its own skeleton
					if (params.subfilter == null) {
						cell.setIsReferent(true); // not to have a writer write the skeleton
					}
					sendAsSource(cell, false); // TU is processed (spaces etc.)					
					tuSent = true;
					
					continue;
				}					
				
				if (isTarget(colNumber)) {
					continue;
				}
				
				if (params.subfilter != null) {
					// All other kinds of cells go to skeleton
					sendAsSkeleton(cell);	
				}
			}
			
			if (params.subfilter == null) {
				// 2. Create a document part to provide a skeleton for references
				// subfilter controls its own skeleton
				GenericSkeleton skel = new GenericSkeleton();
				sendEvent(EventType.DOCUMENT_PART, new DocumentPart(null, false, new GenericSkeleton())); // id will be autoset
				skel = getActiveSkeleton(); // references are added to the document part's skeleton
				
				// 3. Loop through the cells, set references to tu's, and hook up the tu skeletons
				for (int i = 0; i < cells.size(); i++)	{			
					if (i > 0) sendAsSkeleton(getFieldDelimiter());
								
					ITextUnit cell = cells.get(i); // Can be empty
					
					int colNumber = i + 1;
					
					if (isSource(colNumber)) {
						
						if (cell == null) continue;
						
						// subfilter controls its own skeleton
						GenericSkeleton tuSkel = (GenericSkeleton)cell.getSkeleton(); 
						tuSkel.changeSelfReferents(cell);
						skel.add(tuSkel); // TU keeps its skeleton as well (2 refs to the generic parts list) for other cells to use
						continue;
					}
					
					if (isTarget(colNumber)) {
						ITextUnit tu = getSourceFromTargetRef(cells, colNumber);
						if ( tu == null ) {
							sendAsSkeleton(cell);
							continue;
						}
						// Else:
						LocaleId language = getLanguageFromTargetRef(colNumber);
						if ( Util.isNullOrEmpty(language) ) {
							sendAsSkeleton(cell);
							continue;
						}
						
						sendAsTarget(cell, tu, language);
						continue;
					}
					
					// All other kinds of cells go to skeleton
					sendAsSkeleton(cell);				
				}
			}
		}
		
		if (tuSent) {
			
			StartGroup startGroup = new StartGroup("");
			
			if (startGroup != null)
				startGroup.setType("row"); // restype = "row"
				
			sendEvent(startGroupIndex, EventType.START_GROUP, startGroup);
			
			sendEvent(EventType.END_GROUP, new Ending(""));
		}
		
		return true;		
	}
	
	public List<String> getColumnNames() {
		
		if (columnNames == null)
			columnNames = new ArrayList<String>();
				
		return columnNames;
	}
	
	private boolean isSource(int colNumber) {return (sourceColumns == null) ? null : sourceColumns.contains(colNumber);}
	private boolean isSourceId(int colNumber) {return (sourceIdColumns == null) ? null : sourceIdColumns.contains(colNumber);}	
	private boolean isTarget(int colNumber) {return (targetColumns == null) ? null : targetColumns.contains(colNumber);}
	private boolean isComment(int colNumber) {return (commentColumns == null) ? null : commentColumns.contains(colNumber);}	
	private boolean isRecordId(int colNumber) {return params.recordIdColumn > 0 && colNumber == params.recordIdColumn;}

	private ITextUnit getSource(List<ITextUnit> cells, int colNum, List<Integer> columnsList, List<Integer> refList) {
		
		if (columnsList == null) return null;		
		int index = columnsList.indexOf(colNum); 
		
		if (!Util.checkIndex(index, refList)) return null;
		int ref = refList.get(index) - 1; // refList items are 1-based
		
		if (!Util.checkIndex(ref, cells)) return null;
		return cells.get(ref);
	}
	
	private ITextUnit getSourceFromTargetRef(List<ITextUnit> cells, int colNum) {

		return getSource(cells, colNum, targetColumns, targetSourceRefs);
	}
			
	private ITextUnit getSourceFromIdRef(List<ITextUnit> cells, int colNum) {
		
		return getSource(cells, colNum, sourceIdColumns, sourceIdSourceRefs);
	}
	
	private ITextUnit getSourceFromCommentRef(List<ITextUnit> cells, int colNum) {
		
		return getSource(cells, colNum, commentColumns, commentSourceRefs);
	}
		
	private LocaleId getLanguageFromTargetRef(int colNum) {
		if ( targetColumns == null ) return LocaleId.EMPTY;		
		int index = targetColumns.indexOf(colNum); 
		if ( !Util.checkIndex(index, targetLanguages) ) 
			return getTargetLocale();
		
		return targetLanguages.get(index);
	}
	
	private String getSuffixFromSourceRef(int colNum) {
		
		if (sourceIdColumns == null) return "";		
		int index = sourceIdColumns.indexOf(colNum);
		
		if (!Util.checkIndex(index, sourceIdSuffixes)) return "";
		return sourceIdSuffixes.get(index);
	}
	
	private String getSuffix(int colNum) {
		
		if (sourceColumns == null) return "";		
		int index = sourceColumns.indexOf(colNum);
		
		if (!Util.checkIndex(index, sourceIdSuffixes)) return "";
		return sourceIdSuffixes.get(index);
	}
		
	private void updateLineInfo(long lineNum) {
		inHeaderArea = lineNum < params.valuesStartLineNum;
		
		isColumnNames = 
			inHeaderArea && 
					(lineNum == params.columnNamesLineNum || 
					(lineNum > params.columnNamesLineNum && inMultilineColumnNames));
		
		isHeaderLine = inHeaderArea && !isColumnNames;
		
		isFixedNumColumns = 		
			(params.detectColumnsMode == Parameters.DETECT_COLUMNS_FIXED_NUMBER && params.numColumns > 0) ||
			(params.detectColumnsMode == Parameters.DETECT_COLUMNS_COL_NAMES && !inHeaderArea);
	}

	protected boolean isSendListedMode() {
		return sendListedMode;
	}
	
	protected boolean isMerging() {
		return false;
	}
}
