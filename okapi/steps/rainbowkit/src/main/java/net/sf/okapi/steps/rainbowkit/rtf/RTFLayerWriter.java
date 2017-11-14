/*===========================================================================
  Copyright (C) 2011-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.rtf;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.layerprovider.LayerProvider;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Writes an RTF layer on top of the output of a skeleton writer.
 */
public class RTFLayerWriter {

	private ILayerProvider layer;
	private PrintWriter writer;
	private ISkeletonWriter skelWriter;
	private String path;
	private LocaleId targetLocale;
	private String targetEncoding;

	public RTFLayerWriter (ISkeletonWriter skelWriter,
		String path,
		LocaleId targetLocale,
		String targetEncoding)
	{
		this.skelWriter = skelWriter;
		this.path = path;
		this.targetEncoding = targetEncoding;
		this.targetLocale = targetLocale;
	}

	public void writeEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_DOCUMENT:
			writeStartDocument(event);
			break;
		case END_DOCUMENT:
			writeEndDocument(event);
			break;
		case START_SUBDOCUMENT:
			writer.write(skelWriter.processStartSubDocument((StartSubDocument)event.getResource()));
			break;
		case END_SUBDOCUMENT:
			writer.write(skelWriter.processEndSubDocument(event.getEnding()));
			break;
		case START_GROUP:
			writer.write(skelWriter.processStartGroup(event.getStartGroup()));
			break;
		case START_SUBFILTER:
			writer.write(skelWriter.processStartSubfilter(event.getStartSubfilter()));
			break;
		case END_GROUP:
			writer.write(skelWriter.processEndGroup(event.getEnding()));
			break;
		case END_SUBFILTER:
			writer.write(skelWriter.processEndSubfilter(event.getEndSubfilter()));
			break;
		case DOCUMENT_PART:
			writer.write(skelWriter.processDocumentPart(event.getDocumentPart()));
			break;
		case TEXT_UNIT:
			writer.write(skelWriter.processTextUnit(event.getTextUnit()));
			break;
		default:
			break;
		}
	}
	
	private void writeStartDocument (Event event) {
		//TODO: Fix encoding so we use a windows-enabled one (especially for UTF-16)
		layer = new LayerProvider();
		layer.setOptions(null, targetEncoding, null);
		
		if ( skelWriter == null ) {
			throw new InvalidParameterException("You cannot use the RTF writer with no skeleton writer.\n"
				+ "The filter you are trying to use may be incompatible with an RTF output.");
		}
		// Keep 2 copies of the referents for RTF: source and target
		if ( skelWriter instanceof GenericSkeletonWriter ) {
			((GenericSkeletonWriter)this.skelWriter).setReferentCopies(2);
		}
		
		StartDocument sd = event.getStartDocument();
		try {
			Util.createDirectories(path);
			writer = new PrintWriter(path, targetEncoding);
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiIOException("Error creating RTF writer.\n"+e.getMessage(), e);
		}
		catch ( UnsupportedEncodingException e ) {
			throw new OkapiIOException("Error creating RTF writer.", e);
		}

		//TODO: change codepage
		// Write RTF header and stylesheet
		writer.write("{\\rtf1\\ansi\\ansicpg" + "1252" + "\\uc1\\deff1 \n"+
			"{\\fonttbl \n"+
			"{\\f1 \\fmodern\\fcharset0\\fprq1 Courier New;}\n"+
			"{\\f2 \\fswiss\\fcharset0\\fprq2 Arial;}\n"+
			"{\\f3 \\froman\\fcharset0\\fprq2 Times New Roman;}}\n"+
			"{\\colortbl \\red0\\green0\\blue0;\\red0\\green0\\blue0;\\red0\\green0\\blue255;"+
			"\\red0\\green255\\blue255;\\red0\\green255\\blue0;\\red255\\green0\\blue255;"+
			"\\red255\\green0\\blue0;\\red255\\green255\\blue0;\\red255\\green255\\blue255;"+
			"\\red0\\green0\\blue128;\\red0\\green128\\blue128;\\red0\\green128\\blue0;"+
			"\\red128\\green0\\blue128;\\red128\\green0\\blue0;\\red128\\green128\\blue0;"+
			"\\red128\\green128\\blue128;\\red192\\green192\\blue192;}\n"+
			"{\\stylesheet \n"+
			"{\\s0 \\sb80\\slmult1\\widctlpar\\fs20\\f1 \\snext0 Normal;}\n"+
			"{\\cs1 \\additive \\v\\cf12\\sub\\f1 tw4winMark;}\n"+
			"{\\cs2 \\additive \\cf4\\fs40\\f1 tw4winError;}\n"+
			"{\\cs3 \\additive \\f1\\cf11 tw4winPopup;}\n"+
			"{\\cs4 \\additive \\f1\\cf10 tw4winJump;}\n"+
			"{\\cs5 \\additive \\cf15\\f1\\lang1024\\noproof tw4winExternal;}\n"+
			"{\\cs6 \\additive \\cf6\\f1\\lang1024\\noproof tw4winInternal;}\n"+
			"{\\cs7 \\additive \\cf2 tw4winTerm;}\n"+
			"{\\cs8 \\additive \\cf13\\f1\\lang1024\\noproof DO_NOT_TRANSLATE;}\n"+
			"{\\cs9 \\additive Default Paragraph Font;}"+
			"{\\cs15 \\additive \\v\\f1\\cf12\\sub tw4winMark;}"+
			"}\n"+
			"\\paperw11907\\paperh16840\\viewkind4\\viewscale100\\pard\\plain\\s0\\sb80\\slmult1\\widctlpar\\fs20\\f1 \n"+
			Util.RTF_STARTCODE);

		// Write the skeleton
		writer.write(skelWriter.processStartDocument(targetLocale, targetEncoding,
			layer, sd.getFilterWriter().getEncoderManager(), sd));
	}
	
	private void writeEndDocument (Event event) {
		writer.write(skelWriter.processEndDocument(event.getEnding()));
		writer.write(Util.RTF_ENDCODE+"}\n");
		writer.close();
	}

	public void close () {
		if ( writer != null ) {
			writer.close();
			writer = null;
		}
	}

}
