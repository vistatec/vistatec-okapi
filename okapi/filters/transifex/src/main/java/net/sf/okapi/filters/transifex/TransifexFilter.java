/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.filters.transifex;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.FilterUtil;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.po.POFilter;
import net.sf.okapi.lib.transifex.ResourceInfo;
import net.sf.okapi.lib.transifex.TransifexClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the IFilter interface for Transifex-based files.
 */
@UsingParameters(Parameters.class)
public class TransifexFilter implements IFilter {

	private static final String MIMETYPE = "application/x-transifex";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private Project proj;
	private POFilter pof;
	private Iterator<ResourceInfo> iter;
	private TransifexClient cli;
	private boolean canceled;
	private LinkedList<Event> queue;
	private boolean hasNext;
	private boolean hasMoreDoc;
	private String tempDir;
	private IFilterWriter writer;

	private RawDocument input;
	
	public TransifexFilter () {
		params = new Parameters();
		proj = new Project();
		pof = new POFilter();
	}
	
	public void cancel () {
		canceled = true;
	}

	public void close () {
		if (input != null) {
			input.close();
		}
		pof.close();
	}

	public ISkeletonWriter createSkeletonWriter () {
		return createFilterWriter().getSkeletonWriter();
	}

	public IFilterWriter createFilterWriter () {
		if ( writer == null ) {
			writer = new TransifexFilterWriter();
			writer.setOptions(proj.getTargetLocale(), null);
		}
		return writer;
	}

	public List<FilterConfiguration> getConfigurations () {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration(getName(),
			MIMETYPE,
			getClass().getName(),
			"Transifex Project",
			"Transifex project with prompt when starting",
			null,
			Project.PROJECT_EXTENSION+";"));
		list.add(new FilterConfiguration(getName()+"-noPrompt",
			MIMETYPE,
			getClass().getName(),
			"Transifex Project (without prompt)",
			"Transifex project without prompt when starting",
			"noPrompt.fprm"));
		return list;
	}

	public EncoderManager getEncoderManager () {
		return pof.getEncoderManager();
	}

	public String getDisplayName () {
		return "Transifex Filter";
	}

	public String getMimeType () {
		return MIMETYPE;
	}

	public String getName () {
		return "okf_transifex";
	}

	public Parameters getParameters () {
		return params;
	}

	public boolean hasNext () {
		return hasNext;
	}

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
				nextEventInDocument();
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
			throw new OkapiIOException("Error reading the package.", e);
		}
	}

	public void open (RawDocument input) {
		open(input, true);
	}

	public void open (RawDocument input,
		boolean generateSkeleton)
	{
		try {
			this.input = input;
			canceled = false;

			File temp = File.createTempFile("~okapi-27_tx_", null);
			temp.delete();
			temp.mkdirs();
			tempDir = temp.getAbsolutePath();

			// read the project file
			proj.read(new BufferedReader(input.getReader()), input.getSourceLocale(), input.getTargetLocale());
			proj.setPath(input.getInputURI().getPath());
			
			// Refresh the list of resources
			// Prompt the user if requested
			if ( params.getOpenProject() ) {
				if ( !editProjectFile() ) {
					return;
				}
			}
			// Refresh the resource (and their names)
			// All if there are none listed, otherwise, just the existing entries
			proj.refreshResources(!(proj.getResources().size()==0));
			
			// Initialize the client
			cli = new TransifexClient(proj.getHost());
			cli.setCredentials(proj.getUser(), proj.getPassword());
			cli.setProject(proj.getProjectId());
			
			// Initialize the iteration
			iter = proj.getResources().iterator();
			hasMoreDoc = true;
			queue = new LinkedList<Event>();
			hasNext = true;
			nextDocument();
		}
		catch ( IOException e ) {
			throw new OkapiIOException("Error processing input.\n"+e.getMessage(), e);
		}
	}
	
	public boolean editProjectFile () {
		String className = "net.sf.okapi.filters.transifex.ui.ProjectDialog";
		try {
			IProjectEditor dlg = (IProjectEditor)Class.forName(className).newInstance();
			if ( !dlg.edit(null, proj, true) ) {
				canceled = true;
				return false; // Canceled
			}
		}
		catch ( Throwable e ) {
			logger.error("Cannot create the editor ({})\n{}", className, e.getMessage());
			// And move on
			return false;
		}
		return true;
	}

	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		// Not used
	}

	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	private void nextDocument () {
		while ( iter.hasNext() ) {
			ResourceInfo info = iter.next();
			// Work only with selected resources
			if ( info.getSelected() ) {
				if ( prepareDocument(info) ) {
					nextEventInDocument();
					return;
				}
			}
			// Else: error with that file, or un-selected one: move to the next one
		}
		// No more document
		hasMoreDoc = false;
	}
	
	private void nextEventInDocument () {
		// Filter-based case
		if ( pof.hasNext() ) {
			Event e = pof.next();
			queue.add(e);
		}
		else { // No more event: close the filter and move to the next document
			pof.close();
			// Send end-of-batch-item for this document
			// (if it's not the last one)
			//TODO
			
			// Move to the next document
			nextDocument();
		}
	}

//	@SuppressWarnings("resource")
	// rd closed by pof filter
	private boolean prepareDocument (ResourceInfo info) {
//		// Check the information on this file
//		Object[] res = cli.getInformation(resId, trgLoc);
//		if ( res[0] == null ) {
//			logger.warn("Cannot get information for resource '{}' ({}).", resId, trgLoc.toPOSIXLocaleId());
//			return false;
//		}
//		//TODO: avoid re-downloading if we re-write here and the existing file is newer

		logger.info("Resource: {}", info.getId());
		// Download the PO for this resource and the given target language
		String outputPath = tempDir + File.separator + info.getName();
		String[] res = cli.getResource(info.getId(), proj.getTargetLocale(), outputPath);
		if ( res[0] == null ) {
			logger.error("Could not download the resource '{}'.\n{}", info.getId(), res[1]);
			return false;
		}

		// Set the options for the PO filter
		pof.getParameters().setBoolean(net.sf.okapi.filters.po.Parameters.PROTECTAPPROVED,
			proj.getProtectApproved());
		pof.getParameters().setBoolean(GenericSkeletonWriter.ALLOWEMPTYOUTPUTTARGET, true);
		
		// Open the local copy for processing
		RawDocument rd = new RawDocument(new File(outputPath).toURI(), "UTF-8",
			proj.getSourceLocale(), proj.getTargetLocale());
		pof.open(rd);
		
		// Send a start batch item if this is not the first document
		// If it is the first: one was already sent
		//todo
		
		// Get the original start-document of the input file
		if ( !pof.hasNext() ) {
			// Problem with the PO file
			throw new OkapiBadFilterInputException("Input did not generate an event.");
		}
		Event event = pof.next();
		if ( event.getEventType() != EventType.START_DOCUMENT ) {
			// Problem with the PO file
			throw new OkapiBadFilterInputException("First event of the input is not a start document event.");
		}
		// Modify the start document resource so it triggers the use of TransifexFilterWriter:
		StartDocument sd = event.getStartDocument();
		// Save the original one in an annotation
		FilterWriterAnnotation ann = new FilterWriterAnnotation();
		ann.setData(proj, info, sd.getFilterWriter());
		sd.setAnnotation(ann);
		// Set the one to expose
		sd.setFilterWriter(createFilterWriter());
		queue.add(event);
		
		// load simplifier rules and send as an event
		if (!Util.isEmpty(((Parameters)getParameters()).getSimplifierRules())) {			
			Event cs = FilterUtil.createCodeSimplifierEvent(((Parameters)getParameters()).getSimplifierRules());
			queue.add(cs);
		}	
		
		return true;
	}

}
