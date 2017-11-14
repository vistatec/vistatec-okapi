/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffkit.opc;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.AbstractFilter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.filters.xliff.Parameters;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.beans.sessions.OkapiJsonSession;
import net.sf.okapi.lib.persistence.PersistenceSession;
import net.sf.okapi.lib.tkit.merge.TextUnitMerger;
import net.sf.okapi.steps.xliffkit.codec.CodecUtil;
import net.sf.okapi.steps.xliffkit.codec.ICodec;
import net.sf.okapi.steps.xliffkit.codec.PackageSymCodec;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;

public class OPCPackageReader extends AbstractFilter {

//	public static final ICodec CODEC = new PackageOfsCodec();
	public static final ICodec CODEC = new PackageSymCodec();
	
	private OPCPackage pack;
	private OkapiJsonSession session = new OkapiJsonSession(false);
	private Event event;
	private LinkedList<PackagePart> coreParts = new LinkedList<PackagePart>();
	private PackagePart activePart;
	private PackagePart resourcesPart;
	private XLIFFFilter xliffReader;
	private TextUnitMerger merger;
	private LocaleId srcLoc;
	private String outputEncoding;
	private IFilterWriter filterWriter;
	private boolean generateTargets = false;
	private String outputPath;
	private boolean groupByPackagePath = true;
	private boolean cacheEvents = false;
	private LinkedList<Event> events = new LinkedList<Event>();
	private ICodec codec;
	//private Event sde;
//	private LocaleId trgLoc;
	
	public OPCPackageReader(TextUnitMerger merger) {
		super();
		this.merger = merger;
		codec = CODEC;
	}

	@Override
	protected boolean isUtf8Bom() {
		return false;
	}

	@Override
	protected boolean isUtf8Encoding() {
		return false;
	}

	private void writeEvent(Event event) {
		if (!generateTargets) return;
		if (filterWriter == null) return;
		if (events == null) return;
		
		if (cacheEvents) {
			events.add(event);
		}
		else {
			while (events.size() > 0)
				filterWriter.handleEvent(events.poll());
			filterWriter.handleEvent(event);
		}
	}
	
	@Override
	public void close() {
		clearParts();
		session.end();
		try {
			pack.close();
		} catch (IOException e) {
			throw new OkapiIOException("OPCPackageReader: cannot close package");
		}
	}

	private void clearParts() {
		coreParts.clear();
		activePart = null;
		resourcesPart = null;		
	}

	@Override
	public IParameters getParameters() {
		return null;
	}

	@Override
	public boolean hasNext() {
		return event != null;
	}

	@Override
	public Event next() {
		Event prev = event;
		event = deserializeEvent();
		return prev;
	}

	/*
	 * Deserializes events from JSON files in OPC package
	 * @return null if no events are available 
	 */
	private Event deserializeEvent() {
		Event event = null;
		if (activePart == null) {
			activePart = coreParts.poll();
			if (activePart == null) 
				return null;
			else {
				resourcesPart = OPCPackageUtil.getResourcesPart(activePart);
				try {
					if (resourcesPart != null)
						session.start(resourcesPart.getInputStream());
				} catch (IOException e) {
					throw new OkapiIOException("OPCPackageReader: cannot get resources from package", e);
				}
				
				// Create XLIFF filter for the core document
				if (xliffReader != null) {
					xliffReader.close();
					xliffReader = null;
				}
				xliffReader = new XLIFFFilter();
				
				// Keep code Ids as are in the package XLIFF file, don't do balancing
				Parameters params = xliffReader.getParameters();
				params.setBalanceCodes(false);
				
				try {
					// Here targetLocale is set to srcLoc, actual target locale is taken from the StartSubDocument's property targetLocale
					xliffReader.open(new RawDocument(activePart.getInputStream(), "UTF-8", srcLoc, getTrgLoc()));
				} catch (IOException e) {
					throw new OkapiException(String.format("OPCPackageReader: cannot open input stream for %s", 
							activePart.getPartName().getName()), e);
				}
			}
		}
		event = session.deserialize(Event.class);
		if (event == null) {			
			session.end();
			activePart = null;
			return deserializeEvent(); // Recursion until all parts are tried
		} else 
			switch (event.getEventType()) {				
			case START_DOCUMENT:
				processStartDocument(event);
				break;

			case END_DOCUMENT:
				processEndDocument(event);
				break;
			
			case TEXT_UNIT:
				processTextUnit(event); // updates tu with a target from xliff
				break;
				
			case START_SUBDOCUMENT:
			case START_GROUP:
			case END_SUBDOCUMENT:
			case END_GROUP:
			case START_SUBFILTER:
			case END_SUBFILTER:
			case DOCUMENT_PART:
					writeEvent(event);
			default:
				break;
			}
		return event;
	}

	@Override
	public void open(RawDocument input) {
		open(input, false);		
	}

	@Override
	public void open(RawDocument input, boolean generateSkeleton) {
		try {
			srcLoc = input.getSourceLocale();
			//trgLoc = input.getTargetLocale();
			pack = OPCPackage.open(input.getStream());
		} catch (Exception e) {
			throw new OkapiIOException("OPCPackageReader: cannot open package", e);
		}
		
		clearParts();
		coreParts.addAll(OPCPackageUtil.getCoreParts(pack));		
		event = deserializeEvent();
	}

	@Override
	public void setParameters(IParameters params) {
	}

	private ITextUnit getNextXliffTu() {
		if (xliffReader == null)
			throw new OkapiException("OPCPackageReader: xliffReader is not initialized");
		
		Event ev = null;
		while (xliffReader.hasNext()) {
			ev = xliffReader.next();
			if (ev == null) return null;
			if (ev.getEventType() == EventType.START_SUBDOCUMENT) {
				StartSubDocument startSubDoc = (StartSubDocument)ev.getResource();
				Property prop = startSubDoc.getProperty("targetLanguage");
				if ( prop != null ) {
					LocaleId trgLoc = LocaleId.fromString(prop.getValue());
					merger.setTargetLocale(trgLoc);
					filterWriter.setOptions(trgLoc, outputEncoding);
					cacheEvents = false;
				}
			}
			if (ev.getEventType() == EventType.TEXT_UNIT) {
				return ev.getTextUnit();
			}
		}
		return null;
	}
	
	private void processStartDocument (Event event) {
		// Translate src doc name for writers
		StartDocument startDoc = event.getStartDocument();
		String srcName = startDoc.getName();		
		String partName = activePart.getPartName().toString();
		
		filterWriter = startDoc.getFilterWriter();
		filterWriter.setParameters(startDoc.getFilterParameters());
		if (generateTargets) {
			String outFileName = groupByPackagePath ? 
					Util.buildPath(outputPath, Util.getDirectoryName(partName), Util.getFilename(srcName, true)) :
					Util.buildPath(outputPath, Util.getFilename(srcName, true));
			File outputFile = new File(outFileName);
			Util.createDirectories(outputFile.getAbsolutePath());
			filterWriter.setOutput(outputFile.getAbsolutePath());
			//sde = event; // Store for delayed processing
			cacheEvents = true; // In case output locale is not known until START_SUBDOCUMENT 
			writeEvent(event);
		}
	}
	
	private void processEndDocument (Event event) {
		writeEvent(event);
		if (generateTargets)
			filterWriter.close();
	}
	
	private void processTextUnit(Event event) {
		if (merger == null) return;
		
		ITextUnit tu = event.getTextUnit(); 
		ITextUnit xtu = getNextXliffTu();
		if (xtu == null) return;
		
//		// Set tu source from xtu source
//		TextContainer tc = tu.getSource(); // tu source is empty string + codes in JSON
//		TextFragment xtf = xtu.getSource().getUnSegmentedContentCopy();
//		tc.append(xtf.getCodedText());
//		//tu.setSource(xtu.getSource());
		
		CodecUtil.decodeTextUnit(xtu, codec);
		merger.mergeTargets(tu, xtu);
		if (generateTargets) {
			// Desegment the target before writing
			TextContainer target = tu.getTarget(merger.getTargetLocale());
			if (target != null) {
				target.getSegments().joinAll();
			}
		}
		writeEvent(event);
	}

	public void setGeneratorOptions(String outputEncoding, String outputPath, boolean groupByPackagePath) {
		this.outputEncoding = outputEncoding;
		this.generateTargets = !Util.isEmpty(outputPath);
		this.outputPath = outputPath;
		this.groupByPackagePath = groupByPackagePath;
	}
	
	public PersistenceSession getSession() {
		return session;
	}
}
