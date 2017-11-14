/*===========================================================================
  Copyright (C) 2010-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.rainbowkit.common;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.DefaultFilenameFilter;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.resource.ISegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.MergingInfo;
import net.sf.okapi.steps.rainbowkit.creation.Parameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePackageWriter implements IPackageWriter {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	protected Parameters params;
	protected Manifest manifest;
	protected int docId;
	protected String extractionType;
	protected ISkeletonWriter skelWriter;
	protected boolean supporstOneOutputPerInput = true;
	protected String inputRootDir;
	protected String rootDir;
	
	protected TMXWriter tmxWriterApproved;
	protected String tmxPathApproved;
	protected String tempTmxPathApproved;
	
	protected TMXWriter tmxWriterUnApproved;
	protected String tmxPathUnApproved;
	protected String tempTmxPathUnApproved;
	
	protected TMXWriter tmxWriterAlternates;
	protected String tmxPathAlternates;
	protected String tempTmxPathAlternates;
	
	protected TMXWriter tmxWriterLeverage;
	protected String tmxPathLeverage;
	protected String tempTmxPathLeverage;
	
	protected boolean copiedTargetsLikeApproved = false;
	protected boolean useLetterCodes = false;
	protected boolean zeroBasedLetterCodes = true;
	protected boolean tmxInfoAlreadySet = false;
	
	public BasePackageWriter (String extractionType) {
		this.extractionType = extractionType;
		manifest = new Manifest();
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
	public void setBatchInformation (String packageRoot,
		LocaleId srcLoc,
		LocaleId trgLoc,
		String inputRootDir,
		String rootDir,
		String packageId,
		String projectId,
		String creatorParams,
		String tempPackageRoot)
	{
		this.inputRootDir = inputRootDir;
		this.rootDir = rootDir;
		manifest.setInformation(packageRoot, srcLoc, trgLoc, inputRootDir,
			packageId, projectId, creatorParams, tempPackageRoot);
	}

	public String getMainOutputPath () {
		return manifest.getPath();
	}
	
	@Override
	public void cancel () {
		// TODO
	}

	@Override
	public EncoderManager getEncoderManager () {
		// Not used
		return null;
	}

	@Override
	public ISkeletonWriter getSkeletonWriter () {
		return null;
	}

	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_BATCH:
			processStartBatch();
			break;
		case END_BATCH:
			processEndBatch();
			break;
		case START_BATCH_ITEM:
			processStartBatchItem();
			break;
		case RAW_DOCUMENT:
			processRawDocument(event);
			break;
		case END_BATCH_ITEM:
			processEndBatchItem();
			break;
		case START_DOCUMENT:
			processStartDocument(event);
			break;
		case END_DOCUMENT:
			// This method return an event because it may need to be modified with info
			// only the writer has (output file)
			event = processEndDocument(event);
			break;
		case START_SUBDOCUMENT:
			processStartSubDocument(event);
			break;
		case END_SUBDOCUMENT:
			processEndSubDocument(event);
			break;
		case START_GROUP:
		case START_SUBFILTER:
			processStartGroup(event);
			break;
		case END_GROUP:
		case END_SUBFILTER:
			processEndGroup(event);
			break;
		case TEXT_UNIT:
			processTextUnit(event);
			break;
		case DOCUMENT_PART:
			processDocumentPart(event);
			break;
		default:
			break;
		}

		// Update the returned event if needed
		if ( supporstOneOutputPerInput && params.getSendOutput() ) {
			switch ( event.getEventType() ) {
			case START_DOCUMENT:
			case START_SUBDOCUMENT:
			case START_GROUP:
			case END_SUBDOCUMENT:
			case END_GROUP:
			case DOCUMENT_PART:
			case TEXT_UNIT:
				return Event.NOOP_EVENT;
			case END_DOCUMENT:
				// This event was possibly changed by the concrete implementation of the writer
				return event;
			default:
				return event;
			}
		}
		else {
			return event;
		}
	}

	@Override
	public void setOptions (LocaleId locale,
		String defaultEncoding)
	{
		throw new UnsupportedOperationException("Use setDocumentInformation instead.");
	}

	@Override
	public void setOutput (String path) {
		throw new UnsupportedOperationException("Use setDocumentInformation instead.");
	}

	@Override
	public void setOutput (OutputStream output) {
		throw new UnsupportedOperationException("Output to stream not supported for now");
	}

	protected void processStartBatch () {
		docId = 0;
		initializeTMXWriters();
		copySupportMaterial();
	}
	
	protected void setTMXInfo (boolean generate,
		String pathApproved,
		boolean useLetterCodes,
		boolean zerobasedletterCodes,
		boolean overwrite)
	{
		if ( !overwrite && tmxInfoAlreadySet ) {
			return;
		}
		
		this.tmxInfoAlreadySet = true;
		this.useLetterCodes = useLetterCodes;
		this.zeroBasedLetterCodes = zerobasedletterCodes;
		if ( !generate ) {
			tmxPathApproved = null;
			tmxPathUnApproved = null;
			tmxPathAlternates = null;
			tmxPathLeverage = null;
			return;
		}
		
		if ( pathApproved == null ) {
			if ( tmxPathApproved == null ) {
				tmxPathApproved = manifest.getTempTmDirectory() + "approved.tmx";
				tempTmxPathApproved = manifest.getTempTmDirectory() + "approved.tmx";
			}
		}
		else {
			tmxPathApproved = pathApproved;
			//TOFIX: Case of overridden approved TMX not supported if tempPackageRoot is not the package root
			tempTmxPathApproved = pathApproved;
		}
		
		if ( tmxPathUnApproved == null ) {
			tmxPathUnApproved = manifest.getTempTmDirectory() + "unapproved.tmx";
			tempTmxPathUnApproved = manifest.getTempTmDirectory() + "unapproved.tmx";
		}
		
		if ( tmxPathAlternates == null ) {
			tmxPathAlternates = manifest.getTempTmDirectory() + "alternates.tmx";
			tempTmxPathAlternates = manifest.getTempTmDirectory() + "alternates.tmx";
		}
		
		if ( tmxPathLeverage == null ) {
			tmxPathLeverage = manifest.getTempTmDirectory() + "leverage.tmx";
			tempTmxPathLeverage = manifest.getTempTmDirectory() + "leverage.tmx";
		}
		
	}
	
	protected void initializeTMXWriters () {
		if ( tmxPathApproved != null ) {
			tmxWriterApproved = new TMXWriter(tempTmxPathApproved);
			tmxWriterApproved.setLetterCodedMode(useLetterCodes, zeroBasedLetterCodes);
			tmxWriterApproved.writeStartDocument(manifest.getSourceLocale(),
				manifest.getTargetLocale(), getClass().getName(), null, null, null, null);
		}

		if ( tmxPathUnApproved != null ) {
			tmxWriterUnApproved = new TMXWriter(tempTmxPathUnApproved);
			tmxWriterUnApproved.setLetterCodedMode(useLetterCodes, zeroBasedLetterCodes);
			tmxWriterUnApproved.writeStartDocument(manifest.getSourceLocale(),
				manifest.getTargetLocale(), getClass().getName(), null, null, null, null);
		}

		if ( tmxPathAlternates != null ) {
			tmxWriterAlternates = new TMXWriter(tempTmxPathAlternates);
			tmxWriterAlternates.setLetterCodedMode(useLetterCodes, zeroBasedLetterCodes);
			tmxWriterAlternates.writeStartDocument(manifest.getSourceLocale(),
				manifest.getTargetLocale(), getClass().getName(), null, null, null, null);
		}

		if ( tmxPathLeverage != null ) {
			tmxWriterLeverage = new TMXWriter(tempTmxPathLeverage);
			tmxWriterLeverage.setLetterCodedMode(useLetterCodes, zeroBasedLetterCodes);
			tmxWriterLeverage.writeStartDocument(manifest.getSourceLocale(),
				manifest.getTargetLocale(), getClass().getName(), null, null, null, null);
		}
	}

	protected void processEndBatch () {
		if ( params.getOutputManifest() ) {
			manifest.save(manifest.getTempPackageRoot());
		}

		if ( tmxWriterApproved != null ) {
			tmxWriterApproved.writeEndDocument();
			tmxWriterApproved.close();
			if ( tmxWriterApproved.getItemCount() == 0 ) {
				File file = new File(tempTmxPathApproved);
				file.delete();
			}
		}
		
		if ( tmxWriterUnApproved != null ) {
			tmxWriterUnApproved.writeEndDocument();
			tmxWriterUnApproved.close();
			if ( tmxWriterUnApproved.getItemCount() == 0 ) {
				File file = new File(tempTmxPathUnApproved);
				file.delete();
			}
		}

		if ( tmxWriterAlternates != null ) {
			tmxWriterAlternates.writeEndDocument();
			tmxWriterAlternates.close();
			if ( tmxWriterAlternates.getItemCount() == 0 ) {
				File file = new File(tempTmxPathAlternates);
				file.delete();
			}
		}
		
		if ( tmxWriterLeverage != null ) {
			tmxWriterLeverage.writeEndDocument();
			tmxWriterLeverage.close();
			if ( tmxWriterLeverage.getItemCount() == 0 ) {
				File file = new File(tempTmxPathLeverage);
				file.delete();
			}
		}
	}

	protected void processStartBatchItem () {
		// Do nothing by default
	}

	protected void processEndBatchItem () {
		// Do nothing by default
	}
	
	protected void processRawDocument (Event event) {
		String ori = manifest.getTempOriginalDirectory();
		if ( Util.isEmpty(ori) ) return; // No copy to be done
		
		// Else: copy the original
		MergingInfo info = manifest.getItem(docId);
		String inputPath = manifest.getInputRoot() + info.getRelativeInputPath();
		String outputPath = ori + info.getRelativeInputPath();
		StreamUtil.copy(inputPath, outputPath, false);
	}

	@Override
	public void setDocumentInformation (String relativeInputPath,
		String filterConfigId,
		String filterParameters,
		String inputEncoding,
		String relativeTargetPath,
		String targetEncoding,
		ISkeletonWriter skelWriter)
	{
		if ( Util.isEmpty(filterConfigId) ) {
			manifest.addDocument(++docId, Manifest.EXTRACTIONTYPE_NONE, relativeInputPath, "", filterParameters,
				inputEncoding, relativeTargetPath, targetEncoding);
		}
		else {
			this.skelWriter = skelWriter;
			String res[] = FilterConfigurationMapper.splitFilterFromConfiguration(filterConfigId);
			manifest.addDocument(++docId, extractionType, relativeInputPath, res[0], filterParameters,
				inputEncoding, relativeTargetPath, targetEncoding);
		}
	}
	
	protected void processStartDocument (Event event) {
		String ori = manifest.getTempOriginalDirectory();
		if ( Util.isEmpty(ori) ) return; // No copy to be done
		
		// Else: copy the original
		MergingInfo info = manifest.getItem(docId);
		String inputPath = manifest.getInputRoot() + info.getRelativeInputPath();
		String outputPath = ori + info.getRelativeInputPath();
		StreamUtil.copy(inputPath, outputPath, false);
	}

	protected abstract Event processEndDocument (Event event);

	protected void processStartSubDocument (Event event) {
		// Do nothing by default
	}

	protected void processEndSubDocument (Event event) {
		// Do nothing by default
	}

	protected void processStartGroup (Event event) {
		// Do nothing by default
	}

	protected void processEndGroup (Event event) {
		// Do nothing by default
	}

	protected void processDocumentPart (Event event) {
		// Do nothing by default
	}

	protected abstract void processTextUnit (Event event);

	protected void writeTMXEntries (ITextUnit tu) {
		// Check if we have a target
		LocaleId trgLoc = manifest.getTargetLocale();
		TextContainer tc = tu.getTarget(trgLoc);
		if ( tc == null ) {
			return; // No target
		}
		if ( !tu.getSource().hasText(false) ) {
			return; // Empty or no-text source
		}
		
		// Process translation(s) in the container itself (if there is one)
		boolean done = false;
		if ( !tc.isEmpty() ) {
			if ( tu.hasTargetProperty(trgLoc, Property.APPROVED) ) {
				if ( tu.getTargetProperty(trgLoc, Property.APPROVED).getValue().equals("yes") ) {
					// Write existing translation that was approved
					if ( tmxWriterApproved != null ) {
						tmxWriterApproved.writeItem(tu, null);
						done = true;
					}
				}
			}
			if ( !done ) {
				// If un-approved and source == target: don't count it as a translation
				if ( tu.getSource().compareTo(tc, true) != 0 ) {
					// Write existing translation not yet approved
					if ( tmxWriterUnApproved != null ) {
						tmxWriterUnApproved.writeItem(tu, null);
						done = true;
					}
				}
			}
		}
		
		// Look for annotations
		// In each segment
		ISegments srcSegs = tu.getSource().getSegments();
		for ( Segment seg : tc.getSegments() ) {
			Segment srcSeg = srcSegs.get(seg.id);
			if ( srcSeg == null ) continue;
			writeAltTranslations(seg.getAnnotation(AltTranslationsAnnotation.class), srcSeg.text);
		}
		// In the target container
		TextFragment srcOriginal;
		if ( tu.getSource().contentIsOneSegment() ) {
			srcOriginal = tu.getSource().getFirstContent();
		}
		else {
			srcOriginal = tu.getSource().getUnSegmentedContentCopy();
		}
		writeAltTranslations(tc.getAnnotation(AltTranslationsAnnotation.class), srcOriginal);

	}

	private void writeAltTranslations (AltTranslationsAnnotation ann,
		TextFragment srcOriginal)
	{
		if ( ann == null ) {
			return;
		}
		for ( AltTranslation alt : ann ) {
			if ( alt.getFromOriginal() ) {
				// If it's coming from the original it's a true alternate (e.g. XLIFF one)
				if ( tmxWriterAlternates != null ) {
					tmxWriterAlternates.writeAlternate(alt, srcOriginal);
				}
			}
			else {
				// Otherwise the translation is from a leveraging step
				if ( tmxWriterLeverage != null ) {
					tmxWriterLeverage.writeAlternate(alt, srcOriginal);
				}
			}
		}
	}

	@Override
	public void setSupporstOneOutputPerInput (boolean supporstOneOutputPerInput) {
		this.supporstOneOutputPerInput = supporstOneOutputPerInput;
	}

	protected Event creatRawDocumentEventSet (String inputPath,
		String defaultEncoding,
		LocaleId srcLoc,
		LocaleId trgLoc)
	{
		// Create the raw-document
		RawDocument rawDoc = new RawDocument(new File(inputPath).toURI(), defaultEncoding, srcLoc, trgLoc);
		// Create the list of events to send
		List<Event> list = new ArrayList<Event>();
		// Change the pipeline parameters for the raw-document-related data
		PipelineParameters pp = new PipelineParameters();
		pp.setOutputURI(rawDoc.getInputURI()); // Use same name as this output for now
		pp.setSourceLocale(rawDoc.getSourceLocale());
		pp.setTargetLocale(rawDoc.getTargetLocale());
		pp.setOutputEncoding(rawDoc.getEncoding()); // Use same as the output document
		pp.setInputRawDocument(rawDoc);
		// Add the event to the list
		list.add(new Event(EventType.PIPELINE_PARAMETERS, pp));
		// Add raw-document related events
		list.add(new Event(EventType.RAW_DOCUMENT, rawDoc));
		// Return the list as a multiple-event event
		return new Event(EventType.MULTI_EVENT, new MultiEvent(list));
	}
	
	protected void copySupportMaterial () {
		// Get the list of files to copy
		String data = params.getSupportFiles();
		if ( Util.isEmpty(data) ) return;
		List<String> list = params.convertSupportFilesToList(data);

		// For each item in the list of supported material
		for ( String item : list ) {
			// Decode the item (pattern/destination)
			int n = item.indexOf(Parameters.SUPPORTFILEDEST_SEP);
			String origin, destination = "";
			if ( n == -1 ) {
				origin = item;
			}
			else {
				origin = item.substring(0, n);
				destination = item.substring(n+1);
			}
			// Empty destination defaults to the package root and the same filename
			if ( destination.isEmpty() ) {
				destination = "/"+Parameters.SUPPORTFILE_SAMENAME;
			}
			
			// Resolve variables for destination
			// Not supported as the destination is a relative path: destination = Util.fillRootDirectoryVariable(destination, rootDir);
			// Not supported as the destination is a relative path: destination = Util.fillInputRootDirectoryVariable(destination, inputRootDir);
			destination = LocaleId.replaceVariables(destination, manifest.getSourceLocale(), manifest.getTargetLocale());

			// Resolve the variables for the origin
			origin = Util.fillRootDirectoryVariable(origin, rootDir);
			origin = Util.fillInputRootDirectoryVariable(origin, inputRootDir);
			origin = LocaleId.replaceVariables(origin, manifest.getSourceLocale(), manifest.getTargetLocale());
			// Decode the origin
			String pattern = Util.getFilename(origin, true);
			String origDir = Util.getDirectoryName(origin);
			
			File dir = new File(Util.getDirectoryName(origin));
			File[] files = dir.listFiles(new DefaultFilenameFilter(pattern, false));
			if ( files == null ) {
				logger.warn("Invalid list of files for '{}'", origin);
				continue;
			}
			
			for ( File file : files ) {
				String origFn = Util.getFilename(file.getAbsolutePath(), true);

				// Decode the destination
				String destFn = Util.getFilename(destination, true);
				if ( destFn.equalsIgnoreCase(Parameters.SUPPORTFILE_SAMENAME) ) {
					destFn = origFn;
				}
				String destDir = Util.getDirectoryName(destination);
				String destPath = manifest.getTempPackageRoot() + (destDir.isEmpty() ? "" : destDir+"/") + destFn;
			
				StreamUtil.copy(origDir+"/"+origFn, destPath, false);
			}
			
		}
	}
}
