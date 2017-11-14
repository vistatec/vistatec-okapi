/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.formatconversion;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Implementation of the {@link IFilterWriter} interface for corpus-type output.
 * The corpus output is made of plain text UTF-8 document with one line per segment
 * or text unit and one output file per language in the input.
 */
public class CorpusFilterWriter implements IFilterWriter {

	private OutputStreamWriter srcWriter;
	private OutputStream srcOutputStream;
	private String srcOutputPath;
	private File srcTempFile;
	private OutputStreamWriter trgWriter;
	private OutputStream trgOutputStream;
	private String trgOutputPath;
	private File trgTempFile;
	private String baseOutputPath;
	private LocaleId trgLoc;
	private String linebreak = System.getProperty("line.separator");

	public CorpusFilterWriter () {
	}
	
	@Override
	public void cancel () {
		//TODO: Implement cancel()
	}

	@Override
	public void close () {
		if ( srcWriter == null ) return;
		try {
			// Source writer
			srcWriter.close();
			srcWriter = null;
			if ( srcOutputStream != null ) {
				srcOutputStream.close();
				srcOutputStream = null;
			}
			// If it was in a temporary file, copy it over the existing one
			// If the IFilter.close() is called before IFilterWriter.close()
			// this should allow to overwrite the input.
			if ( srcTempFile != null ) {
				StreamUtil.copy(new FileInputStream(srcTempFile), srcOutputPath);
			}

			// target writer
			trgWriter.close();
			trgWriter = null;
			if ( trgOutputStream != null ) {
				trgOutputStream.close();
				trgOutputStream = null;
			}
			if ( trgTempFile != null ) {
				StreamUtil.copy(new FileInputStream(trgTempFile), trgOutputPath);
			}
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error closing the output files.", e);
		}
	}

	@Override
	public String getName () {
		return "CorpusFilterWriter";
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
		// Encoding not used: use always UTF-8
	}

	@Override
	public void setOutput (String path) {
		baseOutputPath = path;
	}

	@Override
	public void setOutput (OutputStream output) {
		throw new OkapiNotImplementedException("The method setOutput(OutputStream) is not supported for this filter-writer.");
	}

	@Override
	public void setParameters (IParameters params) {
		// Nothing for now
	}

	private void handleStartDocument (Event event) {
		try {
			srcTempFile = null;
			trgTempFile = null;

			StartDocument sd = (StartDocument)event.getResource();
			srcOutputPath = baseOutputPath + "." + sd.getLocale().toBCP47().toLowerCase(); 
			trgOutputPath = baseOutputPath + "." + trgLoc.toBCP47().toLowerCase();
			
			//--- Create the source output stream
			boolean useTemp = false;
			File f = new File(srcOutputPath);
			if ( f.exists() ) {
				// If the file exists, try to remove
				useTemp = !f.delete();
			}
			if ( useTemp ) {
				// Use a temporary output if we can overwrite for now
				// If it's the input file, IFilter.close() will free it before we
				// call close() here (that is if IFilter.close() is called correctly
				srcTempFile = File.createTempFile("~okapi-41_gfwTmp_", null);
				srcOutputStream = new BufferedOutputStream(new FileOutputStream(srcTempFile.getAbsolutePath()));
			}
			else { // Make sure the directory exists
				Util.createDirectories(srcOutputPath);
				srcOutputStream = new BufferedOutputStream(new FileOutputStream(srcOutputPath));
			}
			// Create the output, always UTF-8 and without BOM
			srcWriter = new OutputStreamWriter(srcOutputStream, "UTF-8");

			//--- Create the source output stream
			useTemp = false;
			f = new File(trgOutputPath);
			if ( f.exists() ) {
				// If the file exists, try to remove
				useTemp = !f.delete();
			}
			if ( useTemp ) {
				// Use a temporary output if we can overwrite for now
				// If it's the input file, IFilter.close() will free it before we
				// call close() here (that is if IFilter.close() is called correctly
				trgTempFile = File.createTempFile("~okapi-42_gfwTmp_", null);
				trgOutputStream = new BufferedOutputStream(new FileOutputStream(trgTempFile.getAbsolutePath()));
			}
			else { // Make sure the directory exists
				Util.createDirectories(trgOutputPath);
				trgOutputStream = new BufferedOutputStream(new FileOutputStream(trgOutputPath));
			}
			// Create the output, always UTF-8 and without BOM
			trgWriter = new OutputStreamWriter(trgOutputStream, "UTF-8");
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiException(e);
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
	}
	
	private void handleTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();
		if ( !tu.isTranslatable() ) return;

		TextContainer srcCont = tu.getSource();
		TextContainer trgCont = tu.getTarget(trgLoc);
		if ( trgCont == null ) {
			// Use an empty target when we have none
			trgCont = new TextContainer("");
		}

		// If not segmented: use the whole entry
		if ( !srcCont.contentIsOneSegment() ) {
			writeLine(srcCont.getFirstContent(), trgCont.getFirstContent());
			return;
		}

		// Else: go by segments
		ISegments trgSegs = trgCont.getSegments();
		for ( Segment srcSeg : srcCont.getSegments() ) {
			Segment trgSeg = trgSegs.get(srcSeg.id);
			if ( trgSeg == null ) {
				// Use an empty target segment if we have none 
				trgSeg = new Segment();
			}
			writeLine(srcSeg.text, trgSeg.text);
		}
	}

	private void writeLine (TextFragment srcFrag,
		TextFragment trgFrag)
	{
		try {
			srcWriter.write(format(srcFrag));
			srcWriter.write(linebreak);
			
			trgWriter.write(format(trgFrag));
			trgWriter.write(linebreak);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error writing line.", e);
		}
	}

	private String format (TextFragment frag) {
		// Strip the inline codes
		return TextUnitUtil.getText(frag);
	}

}
