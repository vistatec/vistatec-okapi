/*===========================================================================
  Copyright (C) 2011-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.archive;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.ListUtil;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ResourceUtil;
import net.sf.okapi.common.StreamUtil;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
import net.sf.okapi.common.filters.AbstractFilter;
import net.sf.okapi.common.filters.DefaultFilters;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.filterwriter.ZipFilterWriter;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.common.skeleton.ZipSkeleton;

/**
 * Implements the IFilter interface for archive-based (ZIP) content. This filter generates events for
 * certain files in the archive. Those files are processed with sub-filters that are instantiated automatically
 * based on the file names. Mapping between the file names and filter configurations to be used is
 * defined in the filter parameters. File names in filter parameters may contain wildcards (* and ?).
 * The files, for which file names no sub-filter configId is provided, will be sent as document parts with ZipSkeleton.
 * <p>
 * MIME type of the format supported by the filter is not defined by the filter, but is specified via filter parameters. 
 * It allows to use the same filter for different container formats, e.g. DOCX, ODT, etc. by only changing a set of 
 * filter parameters.
 * <p>
 * Please note that when you configure this filter and specify sub-filter classes for the files of interest inside the container,
 * you are responsible to provide visibility of class loaders of those sub-filter classes you specify, otherwise the ArchiveFilter
 * won't be able to instantiate the sub-filters, and an exception will be thrown.
 * <p>
 * To configure the filter, specify in Parameters the comma-separated lists of file names (may contain wildcards) and their corresponding 
 * config Ids.
 * <p>
 * When specifying congigId for a file, you can choose one of the configurations provided by DefaultFilters class. If the desired
 * configuration is not provided there, you can use setFilterConfigurationMapper() to set your own FilterConfiguationMapper,
 * or alternatively you can get a reference to the default implementation of IFilterConfigurationMapper provided by this class, and
 * use addConfiguration() and other methods to configure it to your desire.
 */

@UsingParameters(Parameters.class)
public class ArchiveFilter extends AbstractFilter {

	private enum NextAction {
		OPENZIP, NEXTINZIP, NEXTINSUBDOC, DONE
	}

	public static final String MIME_TYPE = "application/x-archive";
	
	private final String SID = "sd";
	private final String EID = "ed";
	
	private ZipFile zipFile;
	private ZipEntry entry;
	private ZipFilterWriter filterWriter;
	private NextAction nextAction;
	private URI docURI;
	private Enumeration<? extends ZipEntry> entries;
	private int subDocId;
	private LinkedList<Event> queue;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private IFilter subFilter;
	private IFilterWriter subDocWriter;
	private Parameters params;
	private String[] fileNames;
	private String[] configIds;
	private IFilterConfigurationMapper fcMapper;
	private StartDocument saveStartDoc;
	private RawDocument input;
	private File fromStreamTmpFile;

	public ArchiveFilter() {
		super();
		params = new Parameters();
		params.setMimeType(MIME_TYPE);
		setMimeType(MIME_TYPE);
		setFilterWriter(createFilterWriter());
		setParameters(new Parameters());
		setName(getName());
		setDisplayName(getDisplayName());
		addConfiguration(new FilterConfiguration(getName(),
				MIME_TYPE,
				getClass().getName(),
				"Archive Files",
				"Configuration for archive files",
				null,
				".archive;"));
	}
	
	@Override
	public void cancel() {
	}

	@Override
	public void close() {		
		if (input != null) {
			input.close();
		}		
		try {
			nextAction = NextAction.DONE;
			if ( zipFile != null ) {
				zipFile.close();
				zipFile = null;
			}
		}
		catch (IOException e) {
			throw new OkapiIOException(e);
		}
		if (fromStreamTmpFile != null) {
			fromStreamTmpFile.delete();
		}
	}

	@Override
	public IFilterWriter createFilterWriter() {
		if (filterWriter == null) {
			filterWriter = new ZipFilterWriter(getEncoderManager());
		}
		return filterWriter;
	}

	@Override
	public ISkeletonWriter createSkeletonWriter() {
		return null; // There is no corresponding skeleton writer
	}

	@Override
	public String getDisplayName() {
		return "Archive Filter (BETA)";
	}

	@Override
	public EncoderManager getEncoderManager() {
		// There is no corresponding encoder manager, encoding is handled by sub-filters
		return null;
	}

	@Override
	public String getMimeType() {
		return params == null ? MIME_TYPE : params.getMimeType();
	}

	@Override
	public String getName() {
		return "okf_archive";
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public boolean hasNext() {
		return ((( queue != null ) && ( !queue.isEmpty() ))
			|| ( nextAction != NextAction.DONE ));
	}

	@Override
	public Event next() {
		// Send remaining event from the queue first
		if ( queue.size() > 0 ) {
			return queue.poll();
		}
		
		// When the queue is empty: process next action
		switch ( nextAction ) {
		case OPENZIP:
			return openZipFile();
		case NEXTINZIP:
			return nextInZipFile();
		case NEXTINSUBDOC:
			return nextInSubDocument();
		default:
			throw new OkapiIllegalFilterOperationException("Invalid next() call.");
		}
	}

	@Override
	public void open(RawDocument input) {
		open(input, true);
	}	
	
	@Override
	public void open(RawDocument input, boolean generateSkeleton) {
		
		// save reference so we can delete later		
		this.input = input;
		
		if ( input.getInputURI() == null ) {
			fromStreamTmpFile = FileUtil.createTempFile("archive_filter_stream");
			StreamUtil.copy(input.getStream(), fromStreamTmpFile);
			this.input = new RawDocument(fromStreamTmpFile.toURI(), input.getEncoding(), 
					input.getSourceLocale(), input.getTargetLocale(), 
					input.getFilterConfigId());
			
		}		
		docURI = this.input.getInputURI();

		nextAction = NextAction.OPENZIP;
		queue = new LinkedList<Event>();
		srcLoc = input.getSourceLocale();
		trgLoc = input.getTargetLocale();
		
		updateFilterConfigurationMapper();			
					
		fileNames = ListUtil.stringAsArray(((Parameters)getParameters()).getFileNames());
		configIds = ListUtil.stringAsArray(((Parameters)getParameters()).getConfigIds());
		if ((configIds.length > 0) && (fileNames.length != configIds.length)) {
			throw new OkapiException("Different number of filter configuration ids and filenames in parameters");
		}
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(((Parameters)getParameters()).getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
			queue.add(cs);
		}	
	}
	
	private void updateFilterConfigurationMapper() {
		if (fcMapper == null) {
			fcMapper = new FilterConfigurationMapper();
			DefaultFilters.setMappings(fcMapper, true, true);
		}		
	}

	@Override
	public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}
	
	public IFilterConfigurationMapper getFilterConfigurationMapper() {
		updateFilterConfigurationMapper();
		return fcMapper;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters)params;
	}

	private Event openZipFile () {
		try {
			zipFile = new ZipFile(new File(docURI));
			entries = zipFile.entries();
			subDocId = 0;
			nextAction = NextAction.NEXTINZIP;
			
			StartDocument startDoc = new StartDocument(SID);
			startDoc.setName(docURI.getPath());
			startDoc.setLocale(srcLoc);
			startDoc.setMimeType(getMimeType());
			startDoc.setFilterParameters(getParameters());
			startDoc.setFilterWriter(createFilterWriter());
			startDoc.setLineBreak("\n"); // forced
			startDoc.setEncoding("UTF-8", false); // Forced
			return new Event(EventType.START_DOCUMENT, startDoc);
		}
		catch ( ZipException e ) {
			throw new OkapiIOException(e);
		}
		catch ( IOException e ) {
			throw new OkapiIOException(e);
		}
	}
	
	private Event nextInZipFile () {
		while( entries.hasMoreElements() ) {
			entry = entries.nextElement();
			subFilter = getSubFilter(entry.getName());
			if (subFilter != null) {	
				subDocWriter = subFilter.createFilterWriter();
				((ZipFilterWriter) createFilterWriter()).setSubDocWriter(subDocWriter);
				return openSubDocument();
			}
			else {
				DocumentPart dp = new DocumentPart(entry.getName(), false);
				ZipSkeleton skel = new ZipSkeleton(zipFile, entry);
				return new Event(EventType.DOCUMENT_PART, dp, skel);
			}
		}

		// No more sub-documents: end of the ZIP document
		close();
		Ending ending = new Ending(EID);
		return new Event(EventType.END_DOCUMENT, ending);
	}
	
	/**
	 * Gets the sub-filter or null for a given file in the archive.
	 * @param name the name of the file entry to check.
	 * @return the instance of the {@link IFilter} to use for the given file,
	 * or null if this file is not associated to a filter configuration and should
	 * not be extracted.
	 */
	private IFilter getSubFilter (String name) {
		for (int i = 0; i < fileNames.length; i++) {
			String fname = fileNames[i];
			if ( StringUtil.matchesWildcard(name, fname, true) ) {
				// Create sub-filter
				// If there is no instance possible we will get an error
				// but the process will not be stopped, the file will just be
				// treated as non-extractable
				return getFilterConfigurationMapper().createFilter(configIds[i]);
			}
		}
		return null; // File is not to be extracted
	}

	private Event openSubDocument () {
		if (subFilter != null) subFilter.close();
		Event event;
		try {
			subFilter.open(new RawDocument(zipFile.getInputStream(entry), "UTF-8", srcLoc, trgLoc));
			event = subFilter.next(); // START_DOCUMENT
		}
		catch (IOException e) {
			throw new OkapiIOException("Error opening internal file.", e);
		}
		
		// Change the START_DOCUMENT event from sub-filter to START_SUBDOCUMENT
		StartDocument sd = null;
		StartSubDocument ssd = null;
		if (event.getEventType() == EventType.START_DOCUMENT) {
			sd = (StartDocument) event.getResource();
			ssd = new StartSubDocument(SID, sd.getId());			
			saveStartDoc = sd; // Remember the SD transformed to SSD not to loose isMultilingual
		}
		else
			ssd = new StartSubDocument(SID, String.valueOf(++subDocId));
		
		ResourceUtil.copyProperties(sd, ssd);
		ssd.setName(docURI.getPath() + "/" + entry.getName()); // Use '/'
		nextAction = NextAction.NEXTINSUBDOC;
		ZipSkeleton skel = new ZipSkeleton(
			(GenericSkeleton)event.getResource().getSkeleton(), zipFile, entry);
		return new Event(EventType.START_SUBDOCUMENT, ssd, skel);
	}
	
	private Event nextInSubDocument () {
		if (saveStartDoc != null && subDocWriter != null) {
			// To set the lost StartDocument's isMultilingual in sub-filter's skeleton writer
			subDocWriter.getSkeletonWriter().processStartDocument(trgLoc, "UTF-8", null, 
					subDocWriter.getEncoderManager(), saveStartDoc);
			saveStartDoc = null;
		}
		
		Event event;
		while ( subFilter.hasNext() ) {
			event = subFilter.next();
			switch ( event.getEventType() ) {
			case END_DOCUMENT:
				// Change the END_DOCUMENT to END_SUBDOCUMENT
				Ending ending = (Ending) event.getResource();
				nextAction = NextAction.NEXTINZIP;
				ZipSkeleton skel = new ZipSkeleton(
					(GenericSkeleton)event.getResource().getSkeleton(), zipFile, entry);
				return new Event(EventType.END_SUBDOCUMENT, ending, skel);
			
			default: // Else: just pass the event through
				return event;
			}
		}
		return null; // Should not get here
	}

	@Override
	protected boolean isUtf8Encoding() {
		return false;
	}

	@Override
	protected boolean isUtf8Bom() {
		return false;
	}
}
