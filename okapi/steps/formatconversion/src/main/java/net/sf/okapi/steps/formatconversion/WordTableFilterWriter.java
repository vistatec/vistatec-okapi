/*===========================================================================
  Copyright (C) 2013-2014 by the Okapi Framework contributors
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LCIDUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextFragment.TagType;
import net.sf.okapi.common.skeleton.ISkeletonWriter;

/**
 * Implementation of the {@link IFilterWriter} interface for Word table-type output.
 */
public class WordTableFilterWriter implements IFilterWriter {

	private OutputStreamWriter writer;
	private OutputStream outputStream;
	private String outputPath;
	private LocaleId trgLoc;
	private String trgLCID;
	private String encoding;
	private CharsetEncoder chsEncoder;
	private File tempFile;
	private boolean fromTTX;
	
	@Override
	public void cancel () {
		//TODO: support cancel
	}

	@Override
	public void close () {
		if ( writer == null ) return;
		IOException err = null;
		InputStream orig = null;
		OutputStream dest = null;
		try {
			// And writer
			writer.close();
			writer = null;
			if ( outputStream != null ) {
				outputStream.close();
				outputStream = null;
			}

			// If it was in a temporary file, copy it over the existing one
			// If the IFilter.close() is called before IFilterWriter.close()
			// this should allow to overwrite the input.
			if ( tempFile != null ) {
				dest = new FileOutputStream(outputPath);
				orig = new FileInputStream(tempFile); 
				byte[] buffer = new byte[2048];
				int len;
				while ( (len = orig.read(buffer)) > 0 ) {
					dest.write(buffer, 0, len);
				}
			}
		}
		catch ( IOException e ) {
			err = e;
		}
		finally {
			// Make sure we close both files
			if ( dest != null ) {
				try {
					dest.close();
				}
				catch ( IOException e ) {
					err = e;
				}
				dest = null;
			}
			if ( orig != null ) {
				try {
					orig.close();
				} catch ( IOException e ) {
					err = e;
				}
				orig = null;
				if ( err != null ) throw new OkapiException(err);
				else {
					if ( tempFile != null ) {
						tempFile.delete();
						tempFile = null;
					}
				}
			}
		}
	}

	@Override
	public String getName () {
		return "WordTableFilterWriter";
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
		case START_BATCH:
			initialize();
			break;
		case START_DOCUMENT:
			handleStartDocument(event.getStartDocument());
			break;
		case TEXT_UNIT:
			handleTextUnit(event);
			break;
		case END_DOCUMENT:
			handleEndDocument(event.getEnding());
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
		// Ignored for now: encoding = defaultEncoding;
		trgLoc = locale;
		trgLCID = String.valueOf(LCIDUtil.getLCID(trgLoc));
	}

	@Override
	public void setOutput (String path) {
		outputPath = path;
	}

	@Override
	public void setOutput (OutputStream output) {
		outputStream = output;
	}

	@Override
	public void setParameters (IParameters params) {
		// Nothing to do
	}

	private void initialize () {
		//TODO
		
		// anything???
	}
	
	private void handleStartDocument (StartDocument sd) {
		try {
			tempFile = null;
			fromTTX = sd.getMimeType().equals(MimeTypeMapper.TTX_MIME_TYPE);
			// If needed, create the output stream from the path provided
			if ( outputStream == null ) {
				boolean useTemp = false;
				File f = new File(outputPath);
				if ( f.exists() ) {
					// If the file exists, try to remove
					useTemp = !f.delete();
				}
				if ( useTemp ) {
					// Use a temporary output if we can overwrite for now
					// If it's the input file, IFilter.close() will free it before we
					// call close() here (that is if IFilter.close() is called correctly
					tempFile = File.createTempFile("~okapi-44_gfwTmp_", null);
					outputStream = new BufferedOutputStream(new FileOutputStream(tempFile.getAbsolutePath()));
				}
				else { // Make sure the directory exists
					Util.createDirectories(outputPath);
					outputStream = new BufferedOutputStream(new FileOutputStream(outputPath));
				}
			}
			
			encoding = "windows-1252";
			chsEncoder = Charset.forName(encoding).newEncoder();
			writer = new OutputStreamWriter(outputStream, encoding);

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
				"\\paperw11907\\paperh16840\\viewkind4\\viewzk2\\viewscale100\\pard\\plain\\s0\\sb80\\slmult1\\widctlpar\\fs20\\f2 \n");
		
			writer.write("Review Table\\par\\par\n");
			
			// Header row
			writer.write("\\trowd\\trleft-1350\\trbrdrl\\brdrs\\brdrw10 \\trbrdrt\\brdrs\\brdrw10 \\trbrdrr\\brdrs\\brdrw10 \\trbrdrb\\brdrs\\brdrw10 \\trpaddl10\\trpaddr10\\trpaddfl3\\trpaddfr3"
				+ "\\clcbpat16\\clbrdrl\\brdrw10\\brdrs\\clbrdrt\\brdrw10\\brdrs\\clbrdrr\\brdrw10\\brdrs\\clbrdrb\\brdrw10\\brdrs "
				+ "\\cellx-400\\clcbpat16\\clbrdrl\\brdrw10\\brdrs\\clbrdrt\\brdrw10\\brdrs\\clbrdrr\\brdrw10\\brdrs\\clbrdrb\\brdrw10\\brdrs "
				+ "\\cellx600\\clcbpat16\\clbrdrl\\brdrw10\\brdrs\\clbrdrt\\brdrw10\\brdrs\\clbrdrr\\brdrw10\\brdrs\\clbrdrb\\brdrw10\\brdrs "
				+ "\\cellx5400\\clcbpat16\\clbrdrl\\brdrw10\\brdrs\\clbrdrt\\brdrw10\\brdrs\\clbrdrr\\brdrw10\\brdrs\\clbrdrb\\brdrw10\\brdrs "
				+ "\\cellx10400\n");
			
			writer.write("\\pard\\intbl\\widctlpar\\f2\\fs20\\b1 ID\\cell Type\\cell Source\\cell Target\\b0\\cell\\row\n");
			
			// Normal rows
			writer.write("\\trowd\\trleft-1350\\trbrdrl\\brdrs\\brdrw10 \\trbrdrt\\brdrs\\brdrw10 \\trbrdrr\\brdrs\\brdrw10 \\trbrdrb\\brdrs\\brdrw10 \\trpaddl10\\trpaddr10\\trpaddfl3\\trpaddfr3"
					+ "\\clcbpat17\\clbrdrl\\brdrw10\\brdrs\\clbrdrt\\brdrw10\\brdrs\\clbrdrr\\brdrw10\\brdrs\\clbrdrb\\brdrw10\\brdrs "
					+ "\\cellx-400\\clcbpat17\\clbrdrl\\brdrw10\\brdrs\\clbrdrt\\brdrw10\\brdrs\\clbrdrr\\brdrw10\\brdrs\\clbrdrb\\brdrw10\\brdrs "
					+ "\\cellx600\\clcbpat17\\clbrdrl\\brdrw10\\brdrs\\clbrdrt\\brdrw10\\brdrs\\clbrdrr\\brdrw10\\brdrs\\clbrdrb\\brdrw10\\brdrs "
					+ "\\cellx5400\\clcbpat17\\clbrdrl\\brdrw10\\brdrs\\clbrdrt\\brdrw10\\brdrs\\clbrdrr\\brdrw10\\brdrs\\clbrdrb\\brdrw10\\brdrs "
					+ "\\cellx10400\n");
		
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiException(e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Writing error.", e);
		}
	}
	
	private void handleEndDocument (Ending ending) {
		try {
			writer.write("}\n");
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Writing error.", e);
		}
		finally {
			close();
		}
	}
	
	private void handleTextUnit (Event event) {
		ITextUnit tu = event.getTextUnit();
		String tuId = tu.getId();
		
		TextContainer srcCont = tu.getSource();
		TextContainer trgCont = tu.getTarget(trgLoc);
		if ( trgCont == null ) {
			trgCont = new TextContainer(); // Empty
		}
		
		// If not segmented: index the whole entry
		if ( srcCont.contentIsOneSegment() ) {
			writeRow(tuId, srcCont.getFirstContent(), trgCont.getFirstContent(), null);
			return;
		}

		// Else: check if we have the same number of segments
		if ( trgCont.getSegments().count() != srcCont.getSegments().count() ) { 
			// If not: Fall back to full entry
			writeRow(tuId, srcCont.getUnSegmentedContentCopy(), trgCont.getUnSegmentedContentCopy(), null);
			//TODO: Log a warning
			return;
		}
		// If we do have the same number of segments:
		// Output each of them
		ISegments trgSegs = trgCont.getSegments();
		for ( Segment srcSeg : srcCont.getSegments() ) {
			Segment trgSeg = trgSegs.get(srcSeg.id);
			if ( trgSeg == null ) {
				writeRow(tuId+"."+srcSeg.id, srcSeg.text, null, null);
			}
			else {
				writeRow(tuId+"."+srcSeg.id, srcSeg.text, trgSeg.text, trgSeg.getAnnotation(AltTranslationsAnnotation.class));
			}
		}
	}

	private void writeRow (String id,
		TextFragment srcFrag,
		TextFragment trgFrag,
		AltTranslationsAnnotation altTrans)
	{
		String type = null;
		if ( fromTTX && ( altTrans != null )) {
			AltTranslation alt = altTrans.getFirst();
			type = String.format("%d%%", alt.getCombinedScore());
		}
		
		try {
			writer.write("\\pard\\intbl\\widctlpar\\f2\\fs20 ");
			// ID
			writer.write(id+"\\cell ");
			// Type
			if ( type != null ) {
				writer.write(type);
			}
			writer.write("\\cell ");
			// Source
			writer.write(format(srcFrag)+"\\cell\\lang"+trgLCID+" ");
			// Target
			if ( trgFrag != null ) {
				writer.write(format(trgFrag));
			}
			writer.write("\\cell\\row\n");
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error writing row.", e);
		}
	}

	private String format (TextFragment frag) {
		String codedText = frag.getCodedText();
		List<Code> codes = frag.getCodes();
		StringBuilder tmp = new StringBuilder();
		StringBuilder span = new StringBuilder();
		int index;
		for ( int i=0; i<codedText.length(); i++ ) {
			switch ( codedText.codePointAt(i) ) {
			case TextFragment.MARKER_OPENING:
			case TextFragment.MARKER_CLOSING:
			case TextFragment.MARKER_ISOLATED:
				if ( span.length() > 0 ) {
					tmp.append(Util.escapeToRTF(span.toString(), true, 0, chsEncoder));
					span.setLength(0);
				}
				tmp.append(Util.RTF_STARTINLINE);
				switch ( codedText.codePointAt(i) ) {
				case TextFragment.MARKER_OPENING:
					index = TextFragment.toIndex(codedText.charAt(++i));
					tmp.append(String.format("<%d>", codes.get(index).getId()));
					break;
				case TextFragment.MARKER_CLOSING:
					index = TextFragment.toIndex(codedText.charAt(++i));
					tmp.append(String.format("</%d>", codes.get(index).getId()));
					break;
				case TextFragment.MARKER_ISOLATED:
					index = TextFragment.toIndex(codedText.charAt(++i));
					if ( codes.get(index).getTagType() == TagType.OPENING ) {
						tmp.append(String.format("<b%d/>", codes.get(index).getId()));
					}
					else if ( codes.get(index).getTagType() == TagType.CLOSING ) {
						tmp.append(String.format("<e%d/>", codes.get(index).getId()));
					}
					else {
						tmp.append(String.format("<%d/>", codes.get(index).getId()));
					}
					break;
				}
				tmp.append(Util.RTF_ENDINLINE);
				break;
			// If it's a normal character:
			default:
				// It will get escaped for RTF later
				span.append(codedText.charAt(i));
				break;
			}
		}
		// We are done: make sure the last span, if any, is added
		if ( span.length() > 0 ) {
			tmp.append(Util.escapeToRTF(span.toString(), true, 0, chsEncoder));
		}
		return tmp.toString();
	}

}
