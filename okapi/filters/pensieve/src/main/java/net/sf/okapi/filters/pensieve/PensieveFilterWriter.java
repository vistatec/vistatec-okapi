/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.filters.pensieve;

import java.io.File;
import java.io.OutputStream;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.tm.pensieve.common.PensieveUtil;
import net.sf.okapi.tm.pensieve.common.TranslationUnit;
import net.sf.okapi.tm.pensieve.common.TranslationUnitVariant;
import net.sf.okapi.tm.pensieve.writer.ITmWriter;
import net.sf.okapi.tm.pensieve.writer.TmWriterFactory;

/**
 * Implementation of the {@link IFilterWriter} interface for Pensieve TM.
 * The resources are expected to have a target entry for the given target locale.
 */
public class PensieveFilterWriter implements IFilterWriter {

	private ITmWriter writer;
	private String directory;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private boolean overwriteSameSource = false;

	@Override
	public void cancel () {
		// TODO: support cancel
	}

	@Override
	public void close () {
		if ( writer != null ) {
			writer.close();
		}
	}

	@Override
	public String getName () {
		return "PensieveFilterWriter";
	}

	@Override
	public EncoderManager getEncoderManager () {
		return null;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return null;
	}

	@Override
	public IParameters getParameters () {
		return null;
	}

	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			handleStartDocument(event);
			break;
		case TEXT_UNIT:
			handleTextUnit(event);
			break;
		case END_DOCUMENT:
			close();
			break;
		default:
			break;
		}
		return event;
	}

	@Override
	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		trgLoc = locale;
		// Encoding is ignored in this writer
	}

	@Override
	public void setOutput (String path) {
		File f = new File(path);
		// We need to make sure this is absolute
		directory = f.getAbsolutePath(); // We assume it is a directory
	}
	
	public void setOverwriteSameSource (boolean overwriteSameSource) {
		this.overwriteSameSource = overwriteSameSource;
	}

	@Override
	public void setOutput (OutputStream output) {
		throw new OkapiNotImplementedException("Output type not supported.");
	}

	@Override
	public void setParameters (IParameters params) {
		// Not used for now
	}

	private void handleStartDocument (Event event) {
		Util.createDirectories(directory + File.separator);
		// TODO: Move this check at the pensieve package level
		File file = new File(directory + File.separator + "segments.gen");
		// Create a new index only if one does not exists yet
		// If one exists we pass false to append to it
		writer = TmWriterFactory.createFileBasedTmWriter(directory, !file.exists());
		StartDocument sd = (StartDocument) event.getResource();
		srcLoc = sd.getLocale();
	}

	private void handleTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();
		if ( !tu.hasTarget(trgLoc) ) return;

		TextContainer srcCont = tu.getSource();
		TextContainer trgCont = tu.getTarget(trgLoc);
		
		// Un-segmented entry get their metadata
		if ( srcCont.contentIsOneSegment() && trgCont.contentIsOneSegment() ) {
			writer.indexTranslationUnit(PensieveUtil.convertToTranslationUnit(srcLoc, trgLoc, tu), overwriteSameSource);
			return;
		}
		
		// Check if we have the same number of segments
		if ( trgCont.getSegments().count() != srcCont.getSegments().count() ) {
			// Make sure we have at least the full full entry
			TranslationUnitVariant source = new TranslationUnitVariant(srcLoc, srcCont.getUnSegmentedContentCopy());
			TranslationUnitVariant target = new TranslationUnitVariant(trgLoc, trgCont.getUnSegmentedContentCopy());
			TranslationUnit trUnit = new TranslationUnit(source, target);
			// TODO: what do we do with properties? e.g. tuid should not be used as it
			// PensieveUtil.populateMetaDataFromProperties(tu, trUnit);
			writer.indexTranslationUnit(trUnit, overwriteSameSource);
			// TODO: Log a warning
			return;
		}

		// Index each segment
		ISegments trgSegs = trgCont.getSegments();
		for ( Segment srcSeg :srcCont.getSegments() ) {
			Segment trgSeg = trgSegs.get(srcSeg.id);
			// Skip entries with no target match
			if (trgSeg != null && trgSeg.getContent().hasText()) { 	
				TranslationUnitVariant source = new TranslationUnitVariant(srcLoc, srcSeg.text);
				TranslationUnitVariant target = new TranslationUnitVariant(trgLoc, trgSeg.text);
				TranslationUnit trUnit = new TranslationUnit(source, target);
				// TODO: what do we do with properties? e.g. tuid should not be used as it
				// PensieveUtil.populateMetaDataFromProperties(tu, trUnit);
				writer.indexTranslationUnit(trUnit, overwriteSameSource);
			}
		}
	}

}
