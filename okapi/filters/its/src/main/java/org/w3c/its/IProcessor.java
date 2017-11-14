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

import java.net.URI;

import org.w3c.dom.Document;

/**
 * Provides the mathods to apply ITS rules and markup to a given document.
 */
public interface IProcessor {

	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#trans-datacat'>Translate</a>
	 * data category (ITS 1.0)
	 */
	public static final long DC_TRANSLATE         = 0x00000001;
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#locNote-datacat'>Localization Note</a>
	 * data category (ITS 1.0, enhanced in 2.0)
	 */
	public static final long DC_LOCNOTE           = 0x00000002;
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#locNote-datacat'>Terminology</a>
	 * data category (ITS 1.0, enhanced in 2.0)
	 */
	public static final long DC_TERMINOLOGY       = 0x00000004;
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#directionality'>Directionality</a>
	 * data category (ITS 1.0)
	 */
	public static final long DC_DIRECTIONALITY    = 0x00000008;
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#language-information'>Language Information</a>
	 * data category (ITS 1.0)
	 */
	public static final long DC_LANGINFO          = 0x00000010;
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#elements-within-text'>Elements Within Text</a>
	 * data category (ITS 1.0)
	 */
	public static final long DC_WITHINTEXT        = 0x00000020;
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#domain'>Domain</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_DOMAIN            = 0x00000040; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#textanalysis'>Text Analysis</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_TEXTANALYSIS      = 0x00000080; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#LocaleFilter'>Locale Filter</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_LOCFILTER         = 0x00000100; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#provenance'>Provenance</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_PROVENANCE        = 0x00000200; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#externalresource'>External Resource</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_EXTERNALRES       = 0x00000400; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#target-pointer'>Target Pointer</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_TARGETPOINTER     = 0x00000800; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#idvalue'>Id Value</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_IDVALUE           = 0x00001000; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#preservespace'>Preserve Space</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_PRESERVESPACE     = 0x00002000; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#lqissue'>Localization Quality Issue</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_LOCQUALITYISSUE   = 0x00004000; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#lqrating'>Localization Quality Rating</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_LOCQUALITYRATING  = 0x00008000; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#mtconfidence'>MT Confidence</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_MTCONFIDENCE      = 0x00010000; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#allowedchars'>Allowed Characters</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_ALLOWEDCHARS      = 0x00020000; // ITS 2.0
	/**
	 * Flag for the <a href='http://www.w3.org/TR/its20/#storagesize'>Storage Size</a>
	 * data category (ITS 2.0)
	 */
	public static final long DC_STORAGESIZE       = 0x00040000; // ITS 2.0

	/**
	 * Flag for the Sub-Filter definition data category (Extension)
	 */
	public static final long DC_SUBFILTER         = 0x00080000; // Extension
	/**
	 * Flag for all categories (forward compatible).
	 */
	public static final long DC_ALL               = 0xFFFFFFFF;
	
	/**
	 * Adds a set of global rules to the document to process.
	 * <p>The rules are added to the internal storage of the document, not to the document tree.
	 * <p>Use this method to add one rule set or more before calling {@link #applyRules(long)}.
	 * @param rulesDoc Document where the global rules are declared.
	 * @param docURI URI of the document. This is needed because xlink:href need a initial location.
	 */
	void addExternalRules (Document rulesDoc,
		URI docURI);

	/**
	 * Adds a set of global rules to the document to process.
	 * See {@link #addExternalRules(Document, URI)} for more details.
	 * @param docURI URI of the document that contains the rules to add.
	 */
	void addExternalRules (URI docURI);

	/**
	 * Applies the current ITS rules to the document. This method decorates
	 * the document tree with special flags that are used for getting the
	 * different ITS information later.
	 * @param dataCategories Flag indicating what data categories to apply.
	 * The value must be one of the DC_* values or several combined with 
	 * a OR operator. For example: <code>applyRules(DC_TRANSLATE | DC_LOCNOTE);</code>
	 * <p>Use DC_ALL to apply all data categories.
	 */
	void applyRules (long dataCategories);
	
	/**
	 * Removes all the special flags added when applying the ITS rules.
	 * Once you have called this method you should call {@link #applyRules(long)} again to be able
	 * to use ITS-aware methods again.
	 */
	void disapplyRules ();

}
