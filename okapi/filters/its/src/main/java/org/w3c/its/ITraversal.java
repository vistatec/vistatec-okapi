/*===========================================================================
  Copyright (C) 2008-2013 by the Okapi Framework contributors
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

package org.w3c.its;

import org.w3c.dom.Attr;
import org.w3c.dom.Node;

/**
 * Provides the methods traverse a document and access the ITS data category available on each node.
 */
public interface ITraversal {

	/**
	 * Flag indicating a Left-To-Right directionality.
	 */
	public static final int DIR_LTR              = 0;
	/**
	 * Flag indicating a Right-To-Left directionality.
	 */
	public static final int DIR_RTL              = 1;
	/**
	 * Flag indicating a Left-To-Right directionality override.
	 */
	public static final int DIR_LRO              = 2;
	/**
	 * Flag indicating a Right-To-Left directionality override.
	 */
	public static final int DIR_RLO              = 3;
	
	/**
	 * Flag indicating an element that is not within text.
	 */
	public static final int WITHINTEXT_NO        = 0;
	/**
	 * Flag indicating an element that is within text (inline).
	 */
	public static final int WITHINTEXT_YES       = 1;
	/**
	 * Flag indicating an element that is nested.
	 */
	public static final int WITHINTEXT_NESTED    = 2;
	
	/**
	 * Starts the traversal of the document. This method must be called
	 * once before you call {@link #nextNode()}.
	 */
	public void startTraversal ();
	
	/**
	 * Moves to the next node in the traversal of the document.
	 * @return the current node of the traversal. Null if the document is traversed.
	 */
	public Node nextNode ();
	
	/**
	 * Indicates whether the current node is found while backtracking. For example,
	 * for an element node, this indicate the equivalent of a closing tag.
	 * @return true if the current node is found while backtracking, false otherwise. 
	 */
	public boolean backTracking ();

	/**
	 * Indicates if the current element or one of its attributes is
	 * translatable.
	 * @param attribute the attribute to query or null to query the element.
	 * @return true if the queried element or attribute is translatable, false otherwise.
	 */
	public boolean getTranslate (Attr attribute);

	/**
	 * Gets the target pointer for the current element of the traversal or one of its attributes.
	 * @param attribute the attribute to query or null to query the element.
	 * @return the XPath relative to the current element or attribute to the node where the
	 * translation should be placed.
	 */
	public String getTargetPointer (Attr attribute);
	
	/**
	 * Gets the id value for the current element of the traversal or one of its attributes.
	 * @param attribute the attribute to query or null to query the element.
	 * This method is used for both the ITS 2.0 feature and the deprecated extension to ITS 1.0.
	 * @return the value of the identifier for the queried part.
	 */
	public String getIdValue (Attr attribute);
	
	/**
	 * Gets the directionality for the text of a given attribute of the current 
	 * node of the traversal.
	 * @param attribute the attribute to query or null to query the element.
	 * @return the directionality information
	 * ({@link #DIR_LTR}, {@link #DIR_RTL}, {@link #DIR_LRO} or {@link #DIR_RLO})
	 * for the queried part.
	 */
	public int getDirectionality (Attr attribute);

	/**
	 * Gets the external resource reference for the current element of the traversal
	 * or one of its attributes. 
	 * @param attribute the attribute to query or null to query the element.
	 * @return the external resource reference for the queried part, or null.
	 */
	public String getExternalResourceRef (Attr attribute);
	
	/**
	 * Gets the standoff location of the Localization Quality Issue records for the current element
	 * or one of its attributes. 
	 * @param attribute the attribute to query, or null to query the current element.
	 * @return the standoff location of the records for the queried parts (can be null).
	 */
	public String getLocQualityIssuesRef (Attr attribute);
	
	/**
	 * Gets the number of Localization Quality Issue annotations for the current element
	 * or one of its attributes. 
	 * @param attribute the attribute to query, or null to query the current element.
	 * @return the number of issues for the queried part.
	 */
	public int getLocQualityIssueCount (Attr attribute);
	
	/**
	 * Gets the type of the Localization Quality Issue instance for the current element
	 * or one of its attribute, for the given index. 
	 * @param attribute the attribute to query, or null to query the current element.
	 * @param index the index of the issue in the list (zero-based).
	 * @return the type for the issue at the given index for the queried part (can be null).
	 * @see #getLocQualityIssueCount(Attr)
	 */
	public String getLocQualityIssueType (Attr attribute,
		int index);
	
	/**
	 * Gets the comment of the Localization Quality Issue instance for the current element
	 * or one of its attribute, for the given index. 
	 * @param attribute the attribute to query, or null to query the current element.
	 * @param index the index of the issue in the list (zero-based).
	 * @return the comment for the issue at the given index for the queried part (can be null).
	 * @see #getLocQualityIssueCount(Attr)
	 */
	public String getLocQualityIssueComment (Attr attribute,
		int index);
	
	/**
	 * Gets the severity of the Localization Quality Issue instance for the current element
	 * or one of its attribute, for the given index. 
	 * @param attribute the attribute to query, or null to query the current element.
	 * @param index the index of the issue in the list (zero-based).
	 * @return the severity for the issue at the given index for the queried part (can be null).
	 * @see #getLocQualityIssueCount(Attr)
	 */
	public Double getLocQualityIssueSeverity (Attr attribute,
		int index);
	
	/**
	 * Gets the comment of the Localization Quality Issue instance for the current element
	 * or one of its attribute, for the given index. 
	 * @param attribute the attribute to query, or null to query the current element.
	 * @param index the index of the issue in the list (zero-based).
	 * @return the comment for the issue at the given index for the queried part (can be null).
	 * @see #getLocQualityIssueCount(Attr)
	 */
	public String getLocQualityIssueProfileRef (Attr attribute,
		int index);
	
	/**
	 * Gets the enabled/disabled flag of the Localization Quality Issue instance for the current element
	 * or one of its attribute, for the given index. 
	 * @param attribute the attribute to query, or null to query the current element.
	 * @param index the index of the issue in the list (zero-based).
	 * @return the enabled/disabled flag for the issue at the given index for the queried part (can be null).
	 * @see #getLocQualityIssueCount(Attr)
	 */
	public Boolean getLocQualityIssueEnabled (Attr attribute,
		int index);
	
	/**
	 * Gets the element-withinText-related information for the current element.
	 * This data category applies only to elements.
	 * @return One of the WINTINTEXT_* values.
	 */
	public int getWithinText ();

	/**
	 * Indicates if a given attribute of the current element of the traversal or
	 * one of its attributes is a term.
	 * @param attribute The attribute to query or null for the element.
	 * @return True if the queried part is a term, false otherwise.
	 */
	public boolean getTerm (Attr attribute);
	
	/**
	 * Gets the information associated with a given term node or one of its
	 * attributes.
	 * @param attribute The attribute to query or null for the element.
	 * @return the information associated with the queried part.
	 */
	public String getTermInfo (Attr attribute);
	
	/**
	 * Gets the confidence associated with a given term node or one of its
	 * attributes.
	 * @param attribute The attribute to query or null for the element.
	 * @return the confidence associated with the queried part.
	 */
	public Double getTermConfidence (Attr attribute);
	
	/**
	 * Gets the localization note of the current element of the traversal or
	 * one of its attributes.
	 * @param attribute the attribute to query or null for the element.
	 * @return The localization note of the queried part.
	 */
	public String getLocNote (Attr attribute);
	
	public String getLocNoteType (Attr attribute);
	
	/**
	 * Gets the domain or domains for the current element
	 * or one of its attributes.
	 * @param attribute the attribute to query or null to query the current element.
	 * @return a comma-separated string representing the list of domains for the queried part.
	 * See <a href='http://www.w3.org/TR/its20/#domain-implementation'>http://www.w3.org/TR/its20/#domain-implementation</a>
	 * for details on the format of the string.
	 */
	public String getDomains (Attr attribute);

	/**
	 * Gets the locale filter information.
	 * @return A a comma-separated list of extended language ranges as defined in 
	 * BCP-47 (and possibly empty). If the first character is '!' the type is 'exclude'
	 * otherwise the type is 'include'.
	 */
	public String getLocaleFilter ();
	
	/**
	 * Indicates if the white spaces of the current element of the traversal
	 * or the given attribute must be preserved. 
	 * @return True if the white spaces of the current element or the given attribute must be preserve,
	 * false if they may or may not be preserved.
	 */
	public boolean preserveWS (Attr attribute);

	/**
	 * Gets the language for the current element of the traversal and its attributes.
	 * @return The language code for the current element and its attributes. 
	 */
	public String getLanguage ();
	
	/**
	 * Gets the storage size for the current element or one of its attributes.
	 * @param attribute the attribute to query or null to query the current element.
	 * @return the storage size for the queried part.
	 */
	public Integer getStorageSize(Attr attribute);
	
	/**
	 * Gets the storage encoding for the current element or one of its attributes.
	 * @param attribute the attribute to query or null to query the current element.
	 * @return the storage encoding for the queried part.
	 */
	public String getStorageEncoding (Attr attribute);
	
	/**
	 * Gets the storage line-break type for the current element or one of its attributes.
	 * @param attribute the attribute to query or null to query the current element.
	 * @return the storage line-break type for the queried part.
	 */
	public String getLineBreakType (Attr attribute);
	
	/**
	 * Gets the pattern of allowed characters for the current element or one of its attributes.
	 * @param attribute the attribute to query or null to query the current element.
	 * @return the pattern of allowed characters for the queried part.
	 */
	public String getAllowedCharacters (Attr attribute);

	/**
	 * Gets the tools references associated with the current element of the traversal and its attributes.
	 * <p>The returned value is sorted by data category and hold all data categories within scope
	 * (not just the ones set on the given node).
	 * @return the tools references associated with the current element of the traversal and its attributes.
	 */
	public String getAnnotatorsRef ();
	
	/**
	 * Gets the annotator reference for a given data category.
	 * @param dc the name of the data category to look up.
	 * @return the reference for the given data category, or null.
	 */
	public String getAnnotatorRef (String dc);
	
	/**
	 * Gets the MT Confidence value for the current element of the traversal or one
	 * of its attributes.
	 * @param attribute the attribute to query or null for the element.
	 * @return the MT Confidence value or null if none is set.
	 */
	public Double getMtConfidence (Attr attribute);

	public String getTextAnalysisClass (Attr attribute);
	
	public String getTextAnalysisSource (Attr attribute);
	
	public String getTextAnalysisIdent (Attr attribute);
	
	public Double getTextAnalysisConfidence (Attr attribute);

	public Double getLocQualityRatingScore (Attr attribute);
	
	public Integer getLocQualityRatingVote (Attr attribute);
	
	public Double getLocQualityRatingScoreThreshold (Attr attribute);
	
	public Integer getLocQualityRatingVoteThreshold (Attr attribute);
	
	public String getLocQualityRatingProfileRef (Attr attribute);
	
	public String getProvRecordsRef (Attr attribute);
	
	public int getProvRecordCount (Attr attribute);
	
	public String getProvPerson (Attr attribute,
		int index);
		
	public String getProvOrg (Attr attribute,
		int index);
		
	public String getProvTool (Attr attribute,
		int index);
		
	public String getProvRevPerson (Attr attribute,
		int index);
		
	public String getProvRevOrg (Attr attribute,
		int index);
		
	public String getProvRevTool (Attr attribute,
		int index);
	
	public String getProvRef (Attr attribute,
		int index);

}
