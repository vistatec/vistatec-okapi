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

package net.sf.okapi.filters.rainbowkit;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.MultiEvent;
import net.sf.okapi.common.resource.PipelineParameters;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.po.POFilter;
import net.sf.okapi.filters.rtf.RTFFilter;
import net.sf.okapi.filters.transtable.TransTableFilter;
import net.sf.okapi.filters.versifiedtxt.VersifiedTextFilter;
import net.sf.okapi.filters.xini.rainbowkit.XINIRainbowkitFilter;
import net.sf.okapi.filters.xliff.XLIFFFilter;
import net.sf.okapi.lib.transifex.TransifexClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class RainbowKitFilter implements IFilter {

	public static final String RAINBOWKIT_MIME_TYPE = "application/x-rainbowkit";
	public static final String RAINBOWKIT_PACKAGE_MIME_TYPE = "application/x-rainbowkit-package";
	public static final String RAINBOWKIT_PACKAGE_EXTENSION = ".rkp";
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private boolean canceled;
	private boolean hasNext;
	private LinkedList<Event> queue;
	private Manifest manifest;
	private MergingInfo info;
	private Iterator<Integer> iter;
	private IFilter filter;
	private RTFFilter rtfFilter;
	private boolean hasMoreDoc;
	private TransifexClient cli;
	private RawDocument outputDocument;
	private RawDocument input;
	
	public RainbowKitFilter () {
		params = new Parameters();
	}
	
	@Override
	public void cancel () {
		canceled = true;
	}

	@Override
	public void close () {
		if (input != null) {
			input.close();
		}
		if (outputDocument != null) {
			outputDocument.close();
		}
		
		if ( filter != null ) {
			filter.close();
			filter = null;
		}
		if ( rtfFilter != null ) {
			rtfFilter.close();
			rtfFilter = null;
		}
	}

	@Override
	public String getName () {
		return "okf_rainbowkit";
	}

	@Override
	public String getDisplayName () {
		return "Rainbow Translation Kit Filter";
	}

	@Override
	public String getMimeType () {
		return RAINBOWKIT_MIME_TYPE;
	}

	@Override
	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			RAINBOWKIT_MIME_TYPE,
			getClass().getName(),
			"Rainbow Translation Kit",
			"Configuration for Rainbow translation kit.",
			null,
			Manifest.MANIFEST_EXTENSION+";"));
		list.add(new FilterConfiguration(getName() + "-package",
				RAINBOWKIT_PACKAGE_MIME_TYPE,
			getClass().getName(),
			"Rainbow Translation Kit Package",
			"Configuration for Rainbow translation kit package.",
			null,
			RAINBOWKIT_PACKAGE_EXTENSION+";"));
		list.add(new FilterConfiguration(getName() + "-noprompt",
			RAINBOWKIT_MIME_TYPE,
			getClass().getName(),
			"Rainbow Translation Kit (No prompt)",
			"Configuration for Rainbow translation kit (without prompt).",
			"noprompt.fprm",
			Manifest.MANIFEST_EXTENSION+";"));
		
		return list;
	}
	
	@Override
	public EncoderManager getEncoderManager () {
		if ( filter != null ) {
			return filter.getEncoderManager();
		}
		else {
			return null;
		}
	}
	
	@Override
	public Parameters getParameters () {
		return params;
	}

	@Override
	public boolean hasNext () {
		return hasNext;
	}

	@Override
	public Event next () {
		try {
			// Check for cancellation first
			if ( canceled ) {
				queue.clear();
				queue.add(new Event(EventType.CANCELED));
				hasNext = false;
			}
			
			// Parse next if nothing in the queue
			if ( queue.isEmpty() ) {
				nextEventInDocument(false);
				if ( !hasMoreDoc ) {
					// All documents in the manifest have been processed
					// No need to send an END_BATCH_ITEM, as the one for the initial raw document will be sent
					// Use an no-operation event to flush the queue
					hasNext = false;
					queue.add(Event.NOOP_EVENT);
				}
			}
			
			// Return the head of the queue
			return queue.poll();
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error reading the package.\n"+e.getMessage(), e);
		}
	}

	@Override
	public void open (RawDocument input) {
		open(input, true);
	}
	
	@Override
	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		this.input = input;
		canceled = false;
		
		if ( input.getInputURI() == null ) {
			throw new OkapiIOException("This filter requires URI-based raw documents only.");
		}
		
		File inFile = new File(input.getInputURI());
		String inFileName = inFile.getAbsolutePath();
		boolean isPackage = inFileName.endsWith(RAINBOWKIT_PACKAGE_EXTENSION);
			
		manifest = new Manifest();
		
		// Unzip the package if needed and update the path to the extracted manifest
		// The package is always unzipped (no check if already has been) to be able to upload a new version of the same package 
		if ( isPackage ) {
			String root = Util.getDirectoryName(inFileName);
			String packageName = Util.getFilename(inFileName, false);
			String manifestFullName = Util.ensureSeparator(root, false);
			manifestFullName = Util.ensureSeparator(manifestFullName + packageName, false);
			// manifestFullName is the root (for now): unzip the package there
			FileUtil.unzip(inFileName, manifestFullName);
			// The add the filename
			manifestFullName = manifestFullName + Manifest.MANIFEST_FILENAME + Manifest.MANIFEST_EXTENSION;
			inFile = new File(manifestFullName); // inFile.exists()
		}
		
		manifest.load(inFile);
		
		// Prompt the user?
		if ( params.getOpenManifest() ) {
			String className = "net.sf.okapi.filters.rainbowkit.ui.ManifestDialog";
			try {
				IManifestEditor dlg = (IManifestEditor)Class.forName(className).newInstance();
				if ( !dlg.edit(null, manifest, true) ) {
					canceled = true;
					return; // Canceled
				}
			}
			catch ( Throwable e ) {
				LOGGER.error("Cannot create the editor ({})\n{}", className, e.getMessage());
				// And move on to the merge
			}
		}
		
		hasMoreDoc = true;
		queue = new LinkedList<Event>();
		hasNext = true;

		// Create the iterator
		iter = manifest.getItems().keySet().iterator();
		// Start passing the documents
		nextDocument();
	}
	
	@Override
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		// Not used
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public ISkeletonWriter createSkeletonWriter() {
		if ( filter != null ) {
			return filter.createSkeletonWriter();
		}
		else {
			return null;
		}
	}

	@Override
	public IFilterWriter createFilterWriter () {
		if ( filter != null ) {
			return filter.createFilterWriter();
		}
		else {
			return null;
		}
	}

	private void nextDocument () {
		while ( iter.hasNext() ) { // New document
			// Get the current item
			int id = iter.next();
			info = manifest.getItem(id);
			if ( info.getSelected() ) {
				startDocument();
				return;
			}
			// Not selected? get the next one
		}
		// Else: No more document
		// Empty queue will trigger the end
		hasMoreDoc = false;
	}

	private void startDocument () {
		File file = null;
		String extension = null;
		// Pick the filter to use (if any)
		if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_XLIFF) ) {
			filter = new XLIFFFilter();
			extension = ".xlf";
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_OMEGAT) ) {
			filter = new XLIFFFilter();
			extension = ".xlf";
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_PO) ) {
			filter = new POFilter();
			extension = ".po";
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_RTF) ) {
			file = postprocessRTF(Manifest.EXTRACTIONTYPE_RTF);
			// Send the resulting document as event
			// We also change the event to update the outputURI and other pipeline parameters
			ArrayList<Event> list = new ArrayList<Event>();
			list.add(new Event(EventType.START_BATCH_ITEM));
			// Create raw document for the output
			outputDocument = new RawDocument(file.toURI(), info.getTargetEncoding(), manifest.getTargetLocale(),
				manifest.getTargetLocale());
			// Change the pipeline parameters for the raw-document-related data
			PipelineParameters pp = new PipelineParameters();
			pp.setOutputURI(outputDocument.getInputURI()); // Use same name as this output for now
			pp.setSourceLocale(outputDocument.getSourceLocale());
			pp.setTargetLocale(outputDocument.getTargetLocale());
			pp.setOutputEncoding(outputDocument.getEncoding()); // Use same as the output document
			pp.setInputRawDocument(outputDocument);
			pp.setBatchInputCount(1);
			// Add the events to the list
			list.add(new Event(EventType.PIPELINE_PARAMETERS, pp));
			list.add(new Event(EventType.RAW_DOCUMENT, outputDocument));
			list.add(new Event(EventType.END_BATCH_ITEM));
			queue.add(new Event(EventType.MULTI_EVENT, new MultiEvent(list)));
			return;
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_XLIFFRTF) ) {
			// Remove the RTF layer and get the resulting XLIFF file
			file = postprocessRTF(Manifest.EXTRACTIONTYPE_XLIFFRTF);
			// Now we have an XLIFF document ready for parsing
			filter = new XLIFFFilter();
			extension = ""; // Extension is already on the filename
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_TRANSIFEX) ) {
			file = downloadFromTransifex(info);
			filter = new POFilter();
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_ONTRAM) ) {
			file = new File(manifest.getTempTargetDirectory()+"contents.xini");
			filter =  new XINIRainbowkitFilter(info.getRelativeInputPath());
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_VERSIFIED_RTF) ) {
			// RTF layer already removed by user
			file = postprocessRTF(Manifest.EXTRACTIONTYPE_VERSIFIED_RTF);
			// Now we have an Versified txt document ready for parsing
			filter = new VersifiedTextFilter();			
			extension = ""; // Extension is already on the filename
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_TABLE) ) {
			filter = new TransTableFilter();
			extension = ".txt";
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_XLIFF2) ) {
			filter = new XLIFF2Filter();
			extension = ".xlf";
		}
		else if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_NONE) ) {
			// Reference file: just copy it to the output
			String inputPath = manifest.getTempOriginalDirectory()+info.getRelativeInputPath();
			String outputPath = manifest.getMergeDirectory()+info.getRelativeInputPath();
			StreamUtil.copy(inputPath, outputPath, false);
			// We send a no-operation event rather than call nextDocument() to avoid
			// having nested calls that would keep going deeper 
			queue.add(Event.NOOP_EVENT);
			return;
		}
		else {
			throw new OkapiIOException("Unsupported extraction type: "+info.getExtractionType());
		}
		
		if ( file == null ) {
			file = new File(manifest.getTempTargetDirectory()+info.getRelativeInputPath()+extension);
		}
//		@SuppressWarnings("resource")
		// rd closed by filter
		RawDocument rd = new RawDocument(file.toURI(), info.getInputEncoding(),
			manifest.getSourceLocale(), manifest.getTargetLocale());
			
		// Open the document, and start sending its events
		filter.open(rd);
		nextEventInDocument(true);
	}

	/**
	 * Get the next event in the current document.
	 * @param attach true to attach the merging information and the manifest.
	 */
	private void nextEventInDocument (boolean attach) {
		// No filter cases
		if ( filter == null ) {
			nextDocument();
			return;
		}
		
		// Filter-based case
		if ( filter.hasNext() ) {
			if ( attach ) {
				// Attach the manifest and the current merging info to the start document
				Event event = filter.next();
				StartDocument sd = event.getStartDocument();
				sd.setAnnotation(info);
				sd.setAnnotation(manifest);
				queue.add(event);
			}
			else {
				Event e = filter.next();
				queue.add(e);
			}
		}
		else { // No more event: close the filter and move to the next document
			filter.close();
			nextDocument();
		}
	}

//	@SuppressWarnings("resource")
	// RawDocuments below closed by filters
	private File postprocessRTF (String tkitType) {
		File outputFile = null;
		OutputStreamWriter writer = null;
		try {
			// Instantiate the reader if needed
			if ( rtfFilter == null ) {
				rtfFilter = new RTFFilter();
			}
			// For the XLIFF+RTF, set the option to strip ws before '</xml"
			if ( tkitType == Manifest.EXTRACTIONTYPE_XLIFFRTF ) {
				rtfFilter.setStripWSBeforeTextStart(true);
			}
			//TODO: encoding output warnings
			
			//TODO: get LB info from original
			String lineBreak = Util.LINEBREAK_DOS;
			
			// Open the RTF input
			//TODO: guess encoding based on language
			File file;
			String outputPath = null;
			if (Manifest.EXTRACTIONTYPE_XLIFFRTF.equals(tkitType)) {
				file = new File(manifest.getTempTargetDirectory()+info.getRelativeInputPath() + ".xlf" + ".rtf");
				rtfFilter.open(new RawDocument(file.toURI(), "windows-1252", manifest.getTargetLocale()));
				outputPath = manifest.getTempTargetDirectory()+info.getRelativeInputPath() + ".xlf";
			}
			else if (Manifest.EXTRACTIONTYPE_VERSIFIED_RTF.equals(tkitType)) {
				file = new File(manifest.getTempTargetDirectory()+info.getRelativeInputPath() + ".vrsz" + ".rtf");
				rtfFilter.open(new RawDocument(file.toURI(), "UTF-8", manifest.getTargetLocale()));
				outputPath = manifest.getTempTargetDirectory()+info.getRelativeInputPath() + ".vrsz";
			}
			else {
				file = new File(manifest.getTempTargetDirectory()+info.getRelativeInputPath() + ".rtf");
				rtfFilter.open(new RawDocument(file.toURI(), "windows-1252", manifest.getTargetLocale()));
				outputPath = manifest.getMergeDirectory()+info.getRelativeTargetPath();
			}
				
			outputFile = new File(outputPath);
			Util.createDirectories(outputPath);
			writer = new OutputStreamWriter(new BufferedOutputStream(
				new FileOutputStream(outputPath)), info.getTargetEncoding());
			//TODO: check BOM option from original
			Util.writeBOMIfNeeded(writer, false, info.getTargetEncoding());
			
			// Process
			StringBuilder buf = new StringBuilder();
			while ( rtfFilter.getTextUntil(buf, -1, 0) == 0 ) {
				writer.write(buf.toString());
				writer.write(lineBreak);
			}
		}		
		catch ( Exception e ) {
			// Log and move on to the next file
			Throwable e2 = e.getCause();
			LOGGER.error("RTF Conversion error.\n{}", ((e2!=null) ? e2.getMessage() : e.getMessage()));
		}
		finally {
			if ( rtfFilter != null ) {
				rtfFilter.close();
			}
			if ( writer != null ) {
				try {
					writer.close();
				}
				catch ( IOException e ) {
					LOGGER.error("RTF Conversion error when closing file.\n{}", e.getMessage());
				}
			}
		}
		return outputFile;
	}
	
	private File downloadFromTransifex (MergingInfo info) {
		// Check if we have a resource id
		if ( Util.isEmpty(info.getResourceId()) ) {
			// No resource id in the info: cannot post-process
			LOGGER.error("The file '{}' does not have an associated Transifex resource id.",
				info.getRelativeInputPath());
			return null;
		}
		
		// Initialize the Transifex client if it's not done yet
		if ( cli == null ) {
			// Get the Transifex setting from the manifest
			net.sf.okapi.lib.transifex.Parameters prm = new net.sf.okapi.lib.transifex.Parameters();
			prm.fromString(manifest.getCreatorParameters());
			// Create the client with those settings
			cli = new TransifexClient(prm.getServer());
			cli.setProject(prm.getProjectId());
			cli.setCredentials(prm.getUser(), prm.getPassword());
		}
		
		// Set the path of the output
		// This file will be the one merged after it's downloaded
		String outputPath = manifest.getTempTargetDirectory()+info.getRelativeInputPath()+".po";
		// Retrieve the file
		String[] res = cli.getResource(info.getResourceId(), manifest.getTargetLocale(), outputPath);
		if ( res[0] == null ) {
			// Could not download the file
			LOGGER.error("Cannot pull the resource from Transifex.\n{}", res[1]);
			return null;
		}
		File file = new File(outputPath);
		return file;
	}
	
}
