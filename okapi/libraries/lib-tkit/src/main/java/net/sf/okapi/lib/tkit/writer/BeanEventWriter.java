/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.tkit.writer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterWriter;
import java.io.OutputStream;
import java.io.Serializable;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.IWithAnnotations;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.lib.beans.sessions.OkapiJsonSession;

/**
 * {@link Event} {@link FilterWriter} using {@link OkapiJsonSession}
 * <p>
 * Serialized {@link Event}s can be deserialized using {@link SerializedEventFilter} 
 * which is an {@link IFilter} implementation.
 * @author jimh
 *
 */
public class BeanEventWriter implements IFilterWriter {
	private OkapiJsonSession persiSession;
	private LocaleId targetLocale;
	private Parameters params;
	private String outputPath;
	private OutputStream outputStream;

	public BeanEventWriter() {
		persiSession = new OkapiJsonSession(true);
		params = new Parameters();
	}

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public void setOptions(LocaleId locale, String defaultEncoding) {
		this.targetLocale = locale;
	}

	@Override
	public void setOutput(String path) {
		this.outputPath = path;
		Util.createDirectories(outputPath);
	}

	@Override
	public void setOutput(OutputStream output) {
		this.outputStream = output;
	}

	@Override
	public Event handleEvent(Event event) {
		switch (event.getEventType()) {
		case START_BATCH:
			return processStartBatch(event);
		case START_DOCUMENT:
			return processStartDocument(event);
		case END_BATCH_ITEM:
			return processEndBatchItem(event);
		case TEXT_UNIT:
			return processTextUnit(event);
		case NO_OP:
			return event;
		case END_BATCH:
		case START_BATCH_ITEM:
		case START_SUBDOCUMENT:
		case END_SUBDOCUMENT:
		case DOCUMENT_PART:
		case START_GROUP:
		case END_GROUP:
		case START_SUBFILTER:
		case END_SUBFILTER:
		case PIPELINE_PARAMETERS:
		case RAW_DOCUMENT:
		case CANCELED:
		case CUSTOM:
		case MULTI_EVENT:
		default:
			persiSession.serialize(event);
			return event;
		}
	}

	@Override
	public void close() {
		if (persiSession != null) {
			persiSession.end();
		}
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	@Override
	public void cancel() {
		close();
	}

	@Override
	public EncoderManager getEncoderManager() {
		return null;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter() {
		return null;
	}

	protected Event processStartBatch(Event event) {
		persiSession.setDescription(params.getMessage());
		persiSession.serialize(event);
		return event;
	}

	protected Event processStartDocument(Event event) {
		if (!Util.isEmpty(outputPath)) {
			try {
				persiSession.start(new FileOutputStream(outputPath));
			} catch (FileNotFoundException e) {
				throw new OkapiIOException(String.format(
						"Cannot create event serialization file '%s'", outputPath), e);
			}

		} else if (outputStream != null) {
			persiSession.start(outputStream);
		} else {
			throw new OkapiIOException("Output path or stream not defined for event writer");
		}

		// Move persisted annotations from SD (annotations are placed there
		// by the reader from its session), to the writer session annotations
		IWithAnnotations resource = event.getStartDocument();
		if (resource.getAnnotations() instanceof Annotations) {
			Annotations anns = (Annotations) resource.getAnnotations();
			for (IAnnotation ann : anns) {
				if (ann instanceof Serializable) {
					persiSession.setAnnotation(ann);
				}
				// Remove all annotations from SD
				anns.remove(ann.getClass());
			}
		}
		persiSession.serialize(event);
		return event;
	}

	protected Event processTextUnit(Event event) {
		ITextUnit tu = event.getTextUnit();
		Event ev = new Event(EventType.TEXT_UNIT, tu.clone());
		if (params.isRemoveTarget() && targetLocale != null) {
			// Also removes AltTranslationsAnnotations
			tu.removeTarget(targetLocale);
		}

		persiSession.serialize(event); // JSON
		return ev;
	}

	protected Event processEndBatchItem(Event event) {
		// Get serializable annotations and store them in session annotations
		// No need to remove them from the resource as EBI is not serialized
		// (only SD...ED)
		IWithAnnotations ending = event.getEnding();
		if (ending != null) {
			for (IAnnotation ann : ending.getAnnotations()) {
				if (ann instanceof Serializable) {
					persiSession.setAnnotation(ann);
				}
			}
		}
		persiSession.serialize(event); // JSON
		close();
		return event;
	}
}
