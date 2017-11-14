/*===========================================================================
  Copyright (C) 2012-2013 by the Okapi Framework contributors
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

package net.sf.okapi.common.annotation;

/**
 * Types of Generic annotation.
 */
public class GenericAnnotationType {
	
	//========== Start of ITS Section =========================
	
	/**
	 * Label to use for a generic annotation set (e.g. on an inline code).
	 */
	public static final String GENERIC = "generic";
	
	/**
	 * Prefix used to indicate a reference value.
	 */
	public static final String REF_PREFIX = "REF:";

	/**
	 * Annotation identifier for the ITS annotators references.
	 * <p>See <a href='http://www.w3.org/TR/its20/#its-tool-annotation'>http://www.w3.org/TR/its20/#its-tool-annotation</a>
	 * for more information.
	 */
	public static final String ANNOT = "its-annotators";
	/**
	 * Annotators references: a string in the format of a space delimited pairs of 
	 * data category identifier and IRI (separated by a character '|').
	 * See <a href='http://www.w3.org/TR/its20/#its-tool-annotation'>http://www.w3.org/TR/its20/#its-tool-annotation</a>
	 * for the definition.
	 */
	public static final String ANNOT_VALUE = "annotatorsValue";

	/**
	 * A single annotator reference: a string IRI.
	 * <p>Use the {@link #ANNOT} annotation and its {@link #ANNOT_VALUE} value for the list of annotators references.
	 */
	public static final String ANNOTATORREF = "annotatorRef";
	
	/**
	 * Annotation identifier for the 
	 * <a href='http://www.w3.org/TR/its20/#trans-datacat'>ITS Translate</a> data category.
	 * This may be mapped to an inline code.
	 * <p>Related field: {@link #TRANSLATE_VALUE}. 
	 */
	public static final String TRANSLATE = "its-translate";
	/**
	 * Translate flag: a boolean. True to translate, false to not translate.
	 */
	public static final String TRANSLATE_VALUE = "translateValue"; // Boolean
	
	/**
	 * Annotation identifier for the 
	 * <a href='http://www.w3.org/TR/its20/#locnote'>ITS Localization Note</a> data category.
	 * <p>Related fields: {@link #LOCNOTE_TYPE} and {@link #LOCNOTE_VALUE}.
	 */
	public static final String LOCNOTE = "its-ln";
	/**
	 * Localization note value: a string. A reference IRI if there is a {@link #REF_PREFIX} prefix), a text otherwise.
	 */
	public static final String LOCNOTE_VALUE = "lnValue";
	/**
	 * Localization note type: a string "alert" or "description".
	 */
	public static final String LOCNOTE_TYPE = "lnType";
	
	/**
	 * Annotation identifier for the 
	 * <a href='http://www.w3.org/TR/its20/#terminology'>ITS Terminology</a> data category.
	 * <p>Related fields: {@link #TERM_CONFIDENCE}, {@link #TERM_INFO}.
	 */
	public static final String TERM = "its-term";
	/**
	 * Term information: a string. A reference IRI if there is a {@link #REF_PREFIX} prefix), a text otherwise.
	 */
	public static final String TERM_INFO = "termInfo";
	/**
	 * Term confidence: a double between 0.0 and 1.0.
	 */
	public static final String TERM_CONFIDENCE = "termConfidence";
	
	/**
	 * Annotation identifier for the 
	 * <a href='http://www.w3.org/TR/its20/#language-information'>Language Information</a> data category.
	 * <p>Related field: {@link #LANG_VALUE}.
	 */
	public static final String LANG = "its-lang";
	/**
	 * Language information: a string which is the language code.
	 */
	public static final String LANG_VALUE = "langValue";
	
	/**
	 * Annotation identifier for the 
	 * <a href='http://www.w3.org/TR/its20/#domain'>ITS Domain</a> data category.
	 * <p>Related field: {@link #DOMAIN_VALUE}.
	 */
	public static final String DOMAIN = "its-domain";
	/**
	 * Domain value(s): a string in the format of a comma-separated list of values.
	 * See the definition at  
	 * <a href='http://www.w3.org/TR/its20/#domain-implementation'>http://www.w3.org/TR/its20/#domain-implementation</a>:
	 */
	public static final String DOMAIN_VALUE = "domainValue";
	
	/**
	 * Annotation identifier for the
	 * <a href='http://www.w3.org/TR/its20/#textanalysis'>ITS Text Analysis</a> data category.
	 * <p>Related fields: {@link #TA_CLASS}, {@link #TA_CONFIDENCE}, {@link #TA_IDENT} and {@link #TA_SOURCE}.
	 */
	public static final String TA = "its-ta";
	public static final String TA_CLASS = "taClass";
	public static final String TA_SOURCE = "taSource";
	public static final String TA_IDENT = "taIdent";
	/**
	 * Confidence: a double between 0 and 1.
	 */
	public static final String TA_CONFIDENCE = "taConfidence";

	/**
	 * Annotation identifier for the
	 * <a href='http://www.w3.org/TR/its20/#provenance'>ITS Provenance</a> data category.
	 * <p>Related fields: {@link #PROV_ORG}, {@link #PROV_PERSON}, {@link #PROV_TOOL},
	 * {@link #PROV_REVORG}, {@link #PROV_REVPERSON}, {@link #PROV_REVTOOL},
	 * {@link #PROV_PROVREF} and {@link #PROV_RECSREF}. 
	 */
	public static final String PROV = "its-prov";
	/**
	 * Records reference: a IRI string.
	 */
	public static final String PROV_RECSREF = "provRecsRef";
	/**
	 * Human translation provenance: A reference IRI if there is a {@link #REF_PREFIX} prefix), a text otherwise.
	 */
	public static final String PROV_PERSON = "provPerson";
	/**
	 * Organizational translation provenance: A reference IRI if there is a {@link #REF_PREFIX} prefix), a text otherwise.
	 */
	public static final String PROV_ORG = "provOrg";
	/**
	 * Tool translation provenance: A reference IRI if there is a {@link #REF_PREFIX} prefix), a text otherwise.
	 */
	public static final String PROV_TOOL = "provTool";
	/**
	 * Human revision provenance: A reference IRI if there is a {@link #REF_PREFIX} prefix), a text otherwise.
	 */
	public static final String PROV_REVPERSON = "provRevPerson";
	/**
	 * Organizational revision provenance: A reference IRI if there is a {@link #REF_PREFIX} prefix), a text otherwise.
	 */
	public static final String PROV_REVORG = "provRevOrg";
	/**
	 * Tool revision provenance: A reference IRI if there is a {@link #REF_PREFIX} prefix), a text otherwise.
	 */
	public static final String PROV_REVTOOL = "provRevTool";
	/**
	 * Provenance reference: A IRI string pointing to an external provenance information set.
	 */
	public static final String PROV_PROVREF = "provRef";

	/**
	 * Annotation identifier for the 
	 * <a href='http://www.w3.org/TR/its20/#externalresource'>ITS External resource</a> data category.
	 * <p>Related field: {@link #EXTERNALRES_VALUE}.
	 */
	public static final String EXTERNALRES = "its-externalres";
	/**
	 * Reference: a string IRI.
	 */
	public static final String EXTERNALRES_VALUE = "its-externalresValue";
	
	/**
	 * Annotation identifier for the 
	 * <a href='http://www.w3.org/TR/its20/#preservespace'>Preserve Space</a> data category.
	 * <p>Related field: {@link #PRESERVEWS_INFO}.
	 */
	public static final String PRESERVEWS = "its-preservews";
	/**
	 * Preserve space: a string 'preserve' or 'default'.
	 */
	public static final String PRESERVEWS_INFO = "preservewsInfo";
	
	/**
	 * Annotation identifier for the
	 * <a href='http://www.w3.org/TR/its20/#lqissue'>ITS Localization Quality Issue</a> data category.
	 * <p>Related fields: {@link #LQI_COMMENT}, {@link #LQI_ENABLED}, {@link #LQI_ISSUESREF},
	 * {@link #LQI_PROFILEREF}, {@link #LQI_SEVERITY} and {@link #LQI_TYPE}.
	 * <p>And also: {@link #LQI_XCODES}, {@link #LQI_XEND}, {@link #LQI_XSEGID}, {@link #LQI_XSTART},
	 * {@link #LQI_XTRGEND}, {@link #LQI_XTRGSTART} and {@link #LQI_XTYPE}.
	 */
	public static final String LQI = "its-lqi";
	/**
	 * Issues reference: a string IRI.
	 */
	public static final String LQI_ISSUESREF = "lqiIssuesRef";
	/**
	 * Issue type: a string (values limited to the set define in the table at
	 * <a href='http://www.w3.org/TR/its20/#lqissue-typevalues'>http://www.w3.org/TR/its20/#lqissue-typevalues</a>).
	 */
	public static final String LQI_TYPE = "lqiType";
	/**
	 * Issue comment: a string.
	 */
	public static final String LQI_COMMENT = "lqiComment";
	/**
	 * Severity: a double between 0.0 and 100.0
	 */
	public static final String LQI_SEVERITY = "lqiSeverity";
	/**
	 * Profile reference: a string IRI.
	 */
	public static final String LQI_PROFILEREF = "lqiProfileRef";
	/**
	 * Enabled flag: a boolean.
	 */
	public static final String LQI_ENABLED = "lqiEnabled";
	// Extensions
	public static final String LQI_XTYPE = "lqiXType"; // String
	public static final String LQI_XSEGID = "lqiXSegId"; // String
	public static final String LQI_XSTART = "lqiXStart"; // Integer
	public static final String LQI_XEND = "lqiXEnd"; // Integer
	public static final String LQI_XTRGSTART = "lqiXTrgStart"; // Integer
	public static final String LQI_XTRGEND = "lqiXTrgEnd"; // Integer
	public static final String LQI_XCODES = "lqiXCodes"; // String

	/**
	 * Annotation identifier for the
	 * <a href='http://www.w3.org/TR/its20/#lqrating'>ITS Localization Quality Rating</a> data category.
	 * <p>Related fields: {@link #LQR_PROFILEREF}, {@link #LQR_SCORETHRESHOLD}, {@link #LQR_SCORE},
	 * {@link #LQR_VOTETHRESHOLD} and {@value #LQR_VOTETHRESHOLD}.
	 */
	public static final String LQR = "its-lqr";
	/**
	 * Score: a double between 0.0 and 100.0 
	 */
	public static final String LQR_SCORE = "lqrScore";
	/**
	 * Vote: a signed integer.
	 */
	public static final String LQR_VOTE = "lqrVote";
	/**
	 * Score threshold: a double between 0.0 and 100.0
	 */
	public static final String LQR_SCORETHRESHOLD = "lqrScoreThreshold";
	/**
	 * Vote threshold: a signed integer.
	 */
	public static final String LQR_VOTETHRESHOLD = "lqrVoteThreshold";
	/**
	 * Profile reference: a string IRI.
	 */
	public static final String LQR_PROFILEREF = "lqrProfileRef";
	
	/**
	 * Annotation identifier for the
	 * <a href='http://www.w3.org/TR/its20/#mtconfidence'>ITS MT Confidence</a> data category.
	 * <p>Related field: {@link #MTCONFIDENCE_VALUE}.
	 */
	public static final String MTCONFIDENCE = "its-mtconfidence";
	/**
	 * MT confidence: a double between 0 and 1.
	 */
	public static final String MTCONFIDENCE_VALUE = "its-mtconfidenceValue";

	/**
	 * Annotation identifier for the
	 * <a href='http://www.w3.org/TR/its20/#allowedchars'>ITS Allowed Characters</a> data category.
	 * <p>Related field: {@link #ALLOWEDCHARS_VALUE}.
	 */
	public static final String ALLOWEDCHARS = "its-allowedchars";
	/**
	 * Allowed characters: a string that is a regular expression
	 * (restricted to a small sub-set common to several engines.). 
	 */
	public static final String ALLOWEDCHARS_VALUE = "allowedcharsValue";

	/**
	 * Annotation identifier for the
	 * <a href='http://www.w3.org/TR/its20/#storagesize'>ITS Storage Size</a> data category.
	 * <p>Related fields: {@link #STORAGESIZE_ENCODING}, {@link #STORAGESIZE_LINEBREAK} and {@link #STORAGESIZE_SIZE}.
	 */
	public static final String STORAGESIZE = "its-storagesize";
	/**
	 * Storage size: an unsigned integer.
	 */
	public static final String STORAGESIZE_SIZE = "storagesizeSize";
	/**
	 * Storage encoding: a string representing a IANA character set name.
	 */
	public static final String STORAGESIZE_ENCODING = "storagesizeEncoding";
	/**
	 * Linebreak type: a string "cr", "lf" or "crlf".
	 */
	public static final String STORAGESIZE_LINEBREAK = "storagesizeLinebreak";
	
	/**
	 * Annotation identifier for the 
	 * <a href='http://www.w3.org/TR/its20/#LocaleFilter'>ITS Locale Filter</a> data category.
	 * <p>This is represented by a unique value, if the first character is '!' the type is 'exclude'
	 * otherwise it is 'include', the list comes after.
	 * <p>Related field: {@link #LOCFILTER_VALUE}.
	 */
	public static final String LOCFILTER = "its-locfilter";
	/**
	 * Locale filter list: a string in the format of an optional '!' followed by
	 * a comma separated list of locale BCP47 codes.
	 * <p>If the first character of this string is '!' the type is 'exclude'
	 * otherwise it is 'include'. The syntax of the list is the same as the one defined for
	 * "extended language ranges" in 
	 * <a href='http://www.rfc-editor.org/rfc/bcp/bcp47.txt'>http://www.rfc-editor.org/rfc/bcp/bcp47.txt</a>.
	 */
	public static final String LOCFILTER_VALUE = "its-locfilterValue";

	//========== End of ITS Section ===========================
	
}
