/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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

package net.sf.okapi.filters.xliff2;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.lib.xliff2.core.Directionality;
import net.sf.okapi.lib.xliff2.core.MidFileData;
import net.sf.okapi.lib.xliff2.core.Skeleton;
import net.sf.okapi.lib.xliff2.core.StartFileData;
import net.sf.okapi.lib.xliff2.core.StartGroupData;
import net.sf.okapi.lib.xliff2.core.StartXliffData;
import net.sf.okapi.lib.xliff2.core.Unit;
import net.sf.okapi.lib.xliff2.reader.XLIFFReader;
import net.sf.okapi.lib.xliff2.writer.XLIFFWriter;

@UsingParameters(Parameters.class)
public class XLIFF2Filter implements IFilter {	
	public static final String XML_SPACE = "xml:space";

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private X2ToOkpConverter cvt;
	private boolean canceled;
	private XLIFFReader reader;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private EncoderManager encoderManager;
	private Stack<String> idStack;
	private XMLSkeleton skel;
	private StartDocument startDoc;
	private StartSubDocument startSubDoc;
	private String lb;
	private LinkedList<Event> queue;
	private XLIFFWriter writer;
	private StringWriter writerBuffer;

	
	public XLIFF2Filter () {
		params = new Parameters();
	}
	
	@Override
	public String getName () {
		return "okf_xliff2";
	}

	@Override
	public String getDisplayName () {
		return "XLIFF-2 Filter (Experimental)";
	}

	@Override
	public void open (RawDocument input) {
		canceled = false;

		// Determine encoding based on BOM, if any
		input.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(),
			input.getEncoding());
		detector.detectBom();
		boolean hasUTF8BOM = detector.hasUtf8Bom();
		lb = detector.getNewlineType().toString();
		String encoding = "UTF-8";
		if ( detector.isAutodetected() ) {
			encoding = detector.getEncoding();
		}

		srcLoc = input.getSourceLocale();
		trgLoc = input.getTargetLocale();
		String docName = null;
		if ( input.getInputURI() != null ) {
			docName = input.getInputURI().getPath();
		}

		int validation = (params.getMaxValidation()
			? XLIFFReader.VALIDATION_MAXIMAL
			: XLIFFReader.VALIDATION_MINIMAL);
		reader = new XLIFFReader(validation);

		reader.open(input.getStream());
		idStack = new Stack<>();
		queue = new LinkedList<>();
		skel = new XMLSkeleton();
		
		startDoc = new StartDocument(idStack.push("docid"));
		startDoc.setName(docName);
		startDoc.setEncoding(encoding, hasUTF8BOM);
		startDoc.setFilterParameters(getParameters());
		startDoc.setFilterWriter(createFilterWriter());
		startDoc.setType(MimeTypeMapper.XLIFF2_MIME_TYPE);
		startDoc.setMimeType(MimeTypeMapper.XLIFF2_MIME_TYPE);
		startDoc.setMultilingual(true);
		startDoc.setLineBreak(lb);

		// The XML declaration is not reported by the parser, so we need to
		// create it as a document part when starting
		startDoc.setProperty(new Property(Property.ENCODING, encoding, false));
		skel.add("<?xml version=\"1.0\" encoding=\"");
		skel.addValuePlaceholder(startDoc, Property.ENCODING, LocaleId.EMPTY);
		skel.add("\"?>"+lb);
		startDoc.setSkeleton(skel);
		
		writer = new XLIFFWriter();
		writer.setLineBreak(lb);
		writer.setUseIndentation(true);
		writerBuffer = new StringWriter();
		
		// Compile code finder rules
		if (params.getUseCodeFinder()) {
			params.getCodeFinder().compile();		
		}
	}
	
	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		open(input); // Skeleton option is just ignored
	}
	
	@Override
	public void close () {
		if ( reader != null ) {
			reader.close();
		}
		reader = null;
	}

	@Override
	public boolean hasNext () {
		if ( !queue.isEmpty() ) return true;
		if ( canceled ) return false;
		return reader.hasNext();
	}

	@Override
	public Event next () {
		// Dispatch queued events first
		if ( queue.isEmpty() ) {
			while ( !readNext() ) ;
		}
		return queue.poll(); 
	}
	
	private boolean readNext () {
		// Otherwise process get the next event
		net.sf.okapi.lib.xliff2.reader.Event x2Event = reader.next();
		switch ( x2Event.getType() ) {
		case START_DOCUMENT:
			// Handled in START_XLIFF
			return false;
		case START_XLIFF:
			StartXliffData sxd = x2Event.getStartXliffData();
			processLocales(sxd);
			skel.add(conv(sxd));
			queue.add(new Event(EventType.START_DOCUMENT, startDoc));
			skel = new XMLSkeleton();
			// send DEEPEN_SEGMENTATION event to SegmenterStep if needed
			if (params.getNeedsSegmentation()) {
				queue.add(FilterUtil.createDeepenSegmentationEvent());
			}
			return true;
		case START_FILE:
			StartFileData sfd = x2Event.getStartFileData();
			startSubDoc = new StartSubDocument(idStack.peek(), sfd.getId());
			idStack.push(sfd.getId());
			startSubDoc.setName(sfd.getOriginal());
			skel.add(conv(sfd));
			return false;
		case MID_FILE:
			skel.add(conv(x2Event.getMidFileData()));
			return false;
		case SKELETON:
			skel.add(conv(x2Event.getSkeletonData()));
			return false;
		case START_GROUP:
			sendStartSubDocumentIfNeeded();
			StartGroupData sgd = x2Event.getStartGroupData();
			StartGroup sg = new StartGroup(idStack.peek(), sgd.getId());
			idStack.push(sgd.getId());
//			skel.add(true, conv(sg));
			queue.add(new Event(EventType.START_GROUP, sg, skel));
			skel = new XMLSkeleton();
			return true;
		case TEXT_UNIT:
			sendStartSubDocumentIfNeeded();
			Unit unit = x2Event.getUnit();
			ITextUnit tu = cvt.convert(unit);	
			tu.getAlignedSegments().align(trgLoc);
			if (params.getUseCodeFinder()) {
				for(Segment s : tu.getAlignedSegments()) {					
					params.getCodeFinder().process(s.text);
					params.getCodeFinder().process(tu.getTargetSegment(trgLoc, s.id, false).text);
				}				
				// FIXME: new codes may need to be escaped for XML!!!
			}
			createUnitSkeleton(tu, unit, skel);						
			tu.setMimeType(getMimeType());
			queue.add(new Event(EventType.TEXT_UNIT, tu, skel));
			skel = new XMLSkeleton();
			return true;
		case END_GROUP:
			Ending eg = new Ending("end"+idStack.pop());
			skel.add(convEndGroup());
			queue.add(new Event(EventType.END_SUBDOCUMENT, eg, skel));
			skel = new XMLSkeleton();
			return true;
		case END_FILE:
			Ending esd = new Ending("end"+idStack.pop());
			skel.add(convEndFile());
			queue.add(new Event(EventType.END_SUBDOCUMENT, esd, skel));
			skel = new XMLSkeleton();
			return true;
		case END_XLIFF:
			// End is handled in END_DOCUMENT
			return false;
		case END_DOCUMENT:
			Ending ed = new Ending("end"+idStack.pop());
			skel.add(convEndDocument());
			queue.add(new Event(EventType.END_DOCUMENT, ed, skel));
			skel = new XMLSkeleton();
			return true;
		case INSIGNIFICANT_PART:
			skel.add(x2Event.getInsingnificantPartData().getData());
			return false;
		}
		// Should never get here
		return false;
	}

	private void addTuProperties(ITextUnit tu, Unit unit) {
		if (unit.getName() != null) tu.setProperty(new Property(XMLSkeletonWriter.NAME, unit.getName()));
		tu.setProperty(new Property(XMLSkeletonWriter.CANRESEGMENT, unit.getCanResegment() ? "yes" : "no"));
		tu.setProperty(new Property(XMLSkeletonWriter.TRANSLATE, unit.getTranslate() ? "yes" : "no"));
		if (unit.getSourceDir() != Directionality.AUTO) tu.setProperty(new Property(XMLSkeletonWriter.SRCDIR, unit.getSourceDir().toString()));
		if (unit.getTargetDir() != Directionality.AUTO) tu.setProperty(new Property(XMLSkeletonWriter.TRGDIR, unit.getTargetDir().toString()));
		if (unit.getType() != null) tu.setProperty(new Property(XMLSkeletonWriter.TYPE, unit.getType()));
	}

	private void createUnitSkeleton(ITextUnit tu, Unit unit, XMLSkeleton skel) {
		addTuProperties(tu, unit);
		skel.add(String.format("<unit id=\"%s\"", unit.getId()));
		if (tu.hasProperty(XMLSkeletonWriter.NAME)) skel.add(XMLSkeletonWriter.NAME_PLACEHOLDER);  
		if (tu.hasProperty(XMLSkeletonWriter.CANRESEGMENT)) skel.add(XMLSkeletonWriter.CANRESEGMENT_PLACEHOLDER);
		if (tu.hasProperty(XMLSkeletonWriter.TRANSLATE)) skel.add(XMLSkeletonWriter.TRANSLATE_PLACEHOLDER);
		if (tu.hasProperty(XMLSkeletonWriter.SRCDIR)) skel.add(XMLSkeletonWriter.SRCDIR_PLACEHOLDER);
		if (tu.hasProperty(XMLSkeletonWriter.TRGDIR)) skel.add(XMLSkeletonWriter.TRGDIR_PLACEHOLDER);
		if (tu.hasProperty(XMLSkeletonWriter.TYPE)) skel.add(XMLSkeletonWriter.TYPE_PLACEHOLDER);
		
		// close unit start tag
		skel.add(">\n");				
		skel.add(XMLSkeletonWriter.SEGMENTS_PLACEHOLDER);
		skel.add("</unit>\n");
	}

	private void sendStartSubDocumentIfNeeded () {
		if ( startSubDoc == null ) return; // Done already
		queue.add(new Event(EventType.START_SUBDOCUMENT, startSubDoc, skel));
		skel = new XMLSkeleton();
		startSubDoc = null;
	}
	
	private String conv (StartXliffData sxd) {
		writer.writeStartDocument(sxd, null);
		String tmp = writerBuffer.toString();
		writerBuffer.getBuffer().setLength(0);
		// Strip the XML declaration which is already in the skeleton
		return tmp.substring(tmp.indexOf("<xlif"));
	}
	
	private String conv (StartFileData sfd) {
		writer.writeStartFile(sfd);
		String tmp = writerBuffer.toString();
		writerBuffer.getBuffer().setLength(0);
		return tmp;
	}
	
	private String conv (MidFileData mfd) {
		writer.writeMidFile(mfd);
		String tmp = writerBuffer.toString();
		writerBuffer.getBuffer().setLength(0);
		return tmp;
	}
	
	private String conv (Skeleton skelData) {
		writer.writeSkeleton(skelData);
		String tmp = writerBuffer.toString();
		writerBuffer.getBuffer().setLength(0);
		return tmp;
	}
	
	private String convEndGroup () {
		writer.writeEndGroup();
		String tmp = writerBuffer.toString();
		writerBuffer.getBuffer().setLength(0);
		return tmp;
	}
	
	private String convEndFile () {
		writer.writeEndFile();
		String tmp = writerBuffer.toString();
		writerBuffer.getBuffer().setLength(0);
		return tmp;
	}
	
	private String convEndDocument () {
		writer.writeEndDocument();
		String tmp = writerBuffer.toString();
		writerBuffer.getBuffer().setLength(0);
		return tmp;
	}
	
	/**
	 * Checks the source and target locales of the document and
	 * deal with any discrepancy if needed.
	 * @param sxd the START_XLIFF data
	 */
	private void processLocales (StartXliffData sxd) {
		// Check the source
		String src = sxd.getSourceLanguage();
		if ( srcLoc != null && !srcLoc.equals(LocaleId.EMPTY)) {
			if ( srcLoc.compareTo(src) != 0 ) {
				logger.warn("Discripancy between expected source ({}) and source in document ({}), Using '{}'",
					srcLoc.toString(), src, src);
				// Use the locale in the file
				srcLoc = LocaleId.fromBCP47(src);
			}
		}
		else { // source locale was to be guessed
			srcLoc = LocaleId.fromBCP47(src);
		}
		startDoc.setLocale(srcLoc);
		writer.create(writerBuffer, srcLoc.toBCP47());

		// Check the target
		String trg = sxd.getTargetLanguage();
		if ( trg != null) {
			if ( trgLoc != null && !trgLoc.equals(LocaleId.EMPTY)) {
				// Compare raw-document settings with real file 
				if ( trgLoc.compareTo(trg) != 0 ) {
					logger.warn("Discripancy between expected target ({}) and target in document ({}), Using '{}'",
						trgLoc.toString(), trg, trg);
					// Use the locale in the file
					trgLoc = LocaleId.fromBCP47(trg);
				}
			}
			else { // Nothing set in raw-document: use the one in the file
				trgLoc = LocaleId.fromBCP47(trg);
			}
		}
		else {
			if ( trgLoc == null ) {
				// No target locale specified (in either raw-document or document itself)
				throw new NullPointerException("Target language not set and cannot be guessed.");
			}
		}
		cvt = new X2ToOkpConverter(true, trgLoc);
	}
	
	@Override
	public void cancel () {
		canceled = true;
		queue.clear();
		queue.add(new Event(EventType.CANCELED));
		close();
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		// TODO Auto-generated method stub
	}

	@Override
	public ISkeletonWriter createSkeletonWriter () {
		// TODO implement real skeleton writer
		XMLSkeletonWriter writer = new XMLSkeletonWriter();
		return writer;
		
	}

	@Override
	public IFilterWriter createFilterWriter () {
		//TODO: Implement real filter writer as needed
		return new XMLFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	@Override
	public EncoderManager getEncoderManager () {
		//TODO: implement real encoder if needed
		if ( encoderManager == null ) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.XLIFF2_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
		}
		return encoderManager;
	}

	@Override
	public String getMimeType () {
		return MimeTypeMapper.XLIFF2_MIME_TYPE;
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MimeTypeMapper.XLIFF2_MIME_TYPE,
			getClass().getName(),
			"XLIFF-2",
			"Configuration for XLIFF-2 documents.",
			null,
			".xlf"));
		return list;
	}

}
