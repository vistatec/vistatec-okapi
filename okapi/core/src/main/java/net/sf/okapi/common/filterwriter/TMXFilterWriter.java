/*===========================================================================
//Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.common.filterwriter;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Implementation of {@link IFilterWriter} for TMX. This class is not
 * designed to be used with the TMX Filter, but as a standalone writer that
 * can be driven by filter events.
 */
public class TMXFilterWriter implements IFilterWriter {

	private TMXWriter writer;
	private OutputStream outputStream;
	private String outputPath;
	private LocaleId locale;
	private boolean canceled;
	private String segType;
	private Parameters params;
	private List<Event> delayedEvents;
	private LocaleId sourceLocale;
	
	public TMXFilterWriter() {
		this.params = new Parameters();
		delayedEvents = new LinkedList<>(); 
	}

	public TMXFilterWriter(TMXWriter writer) {
		this.params = new Parameters();
		this.writer = writer;
	}
	
	public void setSegType (String segType) {
		this.segType = segType;
	}
	
	public void cancel () {
		close();
		canceled = true;
	}

	public void close () {
		if ( writer == null ) return;
		writer.writeEndDocument();
		writer.close();
		writer = null;
	}

	public String getName () {
		return "TMXFilterWriter";
	}

	public EncoderManager getEncoderManager () {
		return null;
	}
	
	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return null;
	}

	public IParameters getParameters () {
		return params;
	}

	public Event handleEvent (Event event) {
		if ( canceled ) {
			return new Event(EventType.CANCELED);
		}
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
		    sourceLocale = event.getStartDocument().getLocale();
		    if (!Util.isNullOrEmpty(sourceLocale)) {
		        processStartDocument(event);		        
		    } else {
		         // delay writing start document until we know the source locale
		        delayedEvents.add(event);
		    }
			break;
		case END_DOCUMENT:
		    sourceLocale = null;
			processEndDocument();
			break;
		case TEXT_UNIT:
		    if (!Util.isNullOrEmpty(sourceLocale)) {
		        processTextUnit(event);
		    } else {
		        // delay writing text unit until we know the source locale
		        delayedEvents.add(event);
		    }
			break;
		case MULTI_EVENT:
			for (Event e : event.getMultiEvent()) {
				handleEvent(e);
			}
			break;
		case END_SUBDOCUMENT:
			// FIXME: reset to the default target locale
			// but this assumes PIPELINE_PARAMTERS is only 
			// sent for subdocs, could be for every TextUnit.
			writer.setTrgLoc(locale);
			break;
		case PIPELINE_PARAMETERS:
		    // Write out delayed events now that we know the source locale
		    if (!delayedEvents.isEmpty()) {
	            sourceLocale = event.getPipelineParameters().getSourceLocale();
		        delayedEvents.get(0).getStartDocument().setLocale(sourceLocale);
		        // we know the source locale now write out all delayed events
		        // should only be START_DOCUMENT and TEXT_UNITs
		        for (Event e : delayedEvents) {
                    handleEvent(e);
                }
		        delayedEvents.clear();
		    }
			processPipelineParameters(event);
			break;
		default:
			break;
		}
		return event;
	}

	/*
	 * The input to the writer may be multilingual. XliffFilter
	 * sends a PipelineParameters event so that we can reset the target
	 * locale. FIXME: Source is assumed to be the same.
	 */
	private void processPipelineParameters(Event event) {
		PipelineParameters pp = event.getPipelineParameters();
		if (pp.getSourceLocale() != null) {
			writer.setSrcLoc(pp.getSourceLocale());
		}
		if (pp.getTargetLocale() != null) {
			writer.setTrgLoc(pp.getTargetLocale());
		}
	}
	/**
	 * Sets the options for this writer.
	 * @param locale output locale.
	 * @param defaultEncoding this argument is ignored for this writer: the output is always UTF-8.
	 */
	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		this.locale = locale;
		// encoding is ignore: we always use UTF-8
	}

	public void setOutput (String path) {
		outputPath = path;
	}

	public void setOutput (OutputStream output) {
		outputStream = output;
	}

	public void setParameters (IParameters params) {
		this.params = (Parameters)params;		
	}
	
	private void processStartDocument (Event event) {
		StartDocument sd = (StartDocument)event.getResource();
		// Create the output
		if ( outputStream == null ) {	
			if (writer == null) {
				writer = new TMXWriter(outputPath);				
			} else {
				writer.setPath(outputPath);
			}
		}
		else if ( outputStream != null ) {
			if (writer == null) {
				writer = new TMXWriter(new XMLWriter(
						new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)));
			} else {
				writer.setXmlWriter(new XMLWriter(
						new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)));
			}
		}
			
		writer.setWriteAllPropertiesAsAttributes(params.isWriteAllPropertiesAsAttributes());		
		writer.setExpandDuplicateProps(params.isEnableDuplicateProps());
		writer.setPropValueSep(params.getPropValueSep());
		writer.setGenerateUUID(params.isGenerateUUID());
		writer.setNormalizeCodeIds(params.isNormalizeInlineIDs());
	    writer.writeStartDocument(sd, sd.getLocale(), locale, null, null, segType, "unknown", "text");
	}

	private void processEndDocument () {
		close();
	}

	private void processTextUnit (Event event) {
		writer.writeTUFull(event.getTextUnit());
	}
}
