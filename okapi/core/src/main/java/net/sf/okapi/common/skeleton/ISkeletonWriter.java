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

package net.sf.okapi.common.skeleton;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;

/**
 * Provides the methods common to all skeleton writers.
 */
public interface ISkeletonWriter {

	/**
	 * Closes this skeleton writer.
	 */
	public void close ();
	
	/**
	 * Processes the START_DOCUMENT event.
	 * @param outputLocale the output locale. 
	 * @param outputEncoding the name of the output charset encoding.
	 * @param layer the layer provider to use.
	 * @param encoderManager the encoder manager to use.
	 * @param resource the StartDocument resource associated with the event.
	 * @return the string output corresponding to this event.
	 */
	public String processStartDocument (LocaleId outputLocale,
		String outputEncoding,
		ILayerProvider layer,
		EncoderManager encoderManager,
		StartDocument resource);
	
	/**
	 * Processes the END_DOCUMENT event.
	 * @param resource the Ending resource associated with the event.
	 * @return the string output corresponding to this event.
	 */
	public String processEndDocument (Ending resource);
	
	/**
	 * Processes a START_SUBDOCUMENT event.
	 * @param resource the StartSubDocument resource associated with the event.
	 * @return the string output corresponding to this event.
	 */
	public String processStartSubDocument (StartSubDocument resource);
	
	/**
	 * Processes the END_SUBDOCUMENT event.
	 * @param resource the Ending resource associated with the event.
	 * @return the string output corresponding to this event.
	 */
	public String processEndSubDocument (Ending resource);
	
	/**
	 * Processes the START_GROUP event.
	 * @param resource the StartGroup resource associated with the event.
	 * @return the string output corresponding to this event.
	 */
	public String processStartGroup (StartGroup resource);
	
	/**
	 * Processes the END_GROUP event.
	 * @param resource the Ending resource associated with the event.
	 * @return the string output corresponding to this event.
	 */
	public String processEndGroup (Ending resource);
	
	/**
	 * Processes the TEXT_UNIT event.
	 * @param resource the TextUnit resource associated with the event.
	 * @return the string output corresponding to this event.
	 */
	public String processTextUnit (ITextUnit resource);
	
	/**
	 * Processes the DOCUMENT_PART event.
	 * @param resource the DocumentPart resource associated with the event.
	 * @return the string output corresponding to this event.
	 */
	public String processDocumentPart (DocumentPart resource);
	
	public String processStartSubfilter (StartSubfilter resource);
	
	public String processEndSubfilter (EndSubfilter resource);	
}
