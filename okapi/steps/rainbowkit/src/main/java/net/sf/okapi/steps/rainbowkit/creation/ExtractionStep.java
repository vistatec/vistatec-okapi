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

package net.sf.okapi.steps.rainbowkit.creation;

import java.io.File;
import java.net.URI;
import java.util.UUID;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.ExecutionContext;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IUserPrompt;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.RainbowKitFilter;
import net.sf.okapi.steps.rainbowkit.common.IMergeable;
import net.sf.okapi.steps.rainbowkit.common.IPackageWriter;
import net.sf.okapi.steps.rainbowkit.xliff.XLIFF2Options;
import net.sf.okapi.steps.rainbowkit.xliff.XLIFF2PackageWriter;

@UsingParameters(Parameters.class)
public class ExtractionStep extends BasePipelineStep {
	
	private IPackageWriter writer;
	private Parameters params;
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	private URI inputURI;
	private URI outputURI;
	private String outputEncoding;
	private String filterConfigId;
	private String rootDir;
	private String inputRootDir;
	private String outputRootDir;
	private String resolvedOutputDir;
	private String tempPackageRoot;
	private boolean createTipp;
	private ExecutionContext context;
	private boolean didMerge;

	public ExtractionStep () {
		super();
		params = new Parameters();
	}

	@Override
	public String getDescription () {
		return "Generates a Rainbow translation kit for a batch of input documents."
			+" Expects: filter events. Sends back: filter events or raw document";
	}

	@Override
	public String getName () {
		return "Rainbow Translation Kit Creation";
	}

	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale (LocaleId sourceLocale) {
		this.srcLoc = sourceLocale;
	}

	public LocaleId getSourceLocale() {
		return srcLoc;
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.trgLoc = targetLocale;
	}

	public LocaleId getTargetLocale() {
		return trgLoc;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_URI)
	public void setInputURI (URI inputURI) {
		this.inputURI = inputURI;
	}
	
	public URI getInputURI() {
		return inputURI;
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
	public URI getOutputURI() {
		return outputURI;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_ENCODING)
	public void setOutputEncoding (String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}

	public String getOutputEncoding() {
		return outputEncoding;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_ID)
	public void setFilterConfigurationId (String filterConfigId) {
		this.filterConfigId = filterConfigId;
	}

	public String getFilterConfigurationId() {
		return filterConfigId;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}

	public String getRootDir() {
		return rootDir;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
	public void setInputRootDirectory (String inputRootDir) {
		this.inputRootDir = inputRootDir;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_DIRECTORY)
	public void setOutputRootDirectory (String outputRootDir) {
		this.outputRootDir = outputRootDir;
	}

	public String getInputRootDirectory() {
		return inputRootDir;
	}

	@StepParameterMapping(parameterType = StepParameterType.EXECUTION_CONTEXT)
	public void setContext (ExecutionContext context) {
		this.context = context;
	}

	@Override
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case START_BATCH:
			return handleStartBatch(event);
		case END_BATCH:
			return handleEndBatch(event);
		case START_DOCUMENT:
			return handleStartDocument(event);
		case RAW_DOCUMENT:
			return handleRawDocument(event);
		default:
			return writer.handleEvent(event);
		}
	}

	@Override
	protected Event handleStartBatch (Event event) {
		try {
			// Get the package format (class name)
			String writerClass = params.getWriterClass();
			writer = (IPackageWriter)Class.forName(writerClass).newInstance();
			writer.setParameters(params);

			createTipp = false;
			if ( writer instanceof XLIFF2PackageWriter ) {
				XLIFF2Options opt = new XLIFF2Options();
				if ( !Util.isEmpty(params.getWriterOptions()) ) {
					opt.fromString(params.getWriterOptions());
					createTipp = opt.getCreateTipPackage();
				}
			}
			
			resolvedOutputDir = params.getPackageDirectory() + File.separator + params.getPackageName();
			resolvedOutputDir = Util.fillRootDirectoryVariable(resolvedOutputDir, rootDir);
			resolvedOutputDir = Util.fillInputRootDirectoryVariable(resolvedOutputDir, inputRootDir);
			resolvedOutputDir = LocaleId.replaceVariables(resolvedOutputDir, srcLoc, trgLoc);
			// Give the writer a chance to merge the new package with the old one
			didMerge = false;
			if ( new File(resolvedOutputDir).isDirectory() ) {
				if (shouldMerge()) {
					((IMergeable)writer).prepareForMerge(resolvedOutputDir);
					didMerge = true;
				} else {
					Util.deleteDirectory(resolvedOutputDir, false);
				}
			}
			
			String packageId = UUID.randomUUID().toString();
			String projectId = Util.makeId(params.getPackageName()+srcLoc.toString()+trgLoc.toString());

			// If we zip we create the initial package in a temp location, then zip it to the correct one
			// This is to avoid issues with files that are still locked and can't be deleted 
			tempPackageRoot = resolvedOutputDir;
			if ( params.getCreateZip() || createTipp ) {
				// Create a set the tempPackageRoot
				final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
		        String dirName = UUID.randomUUID().toString();
		        File newTempDir = new File(sysTempDir, dirName);
		        newTempDir.mkdirs();
		        tempPackageRoot = newTempDir.getAbsolutePath();
			}
			
			writer.setBatchInformation(resolvedOutputDir, srcLoc, trgLoc, inputRootDir,
				rootDir, packageId, projectId, params.getWriterOptions(), tempPackageRoot);
		}
		catch ( InstantiationException e ) {
			throw new OkapiException("Error creating writer class.", e);
		}
		catch ( IllegalAccessException e ) {
			throw new OkapiException("Error creating writer class.", e);
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiException("Error creating writer class.", e);
		}
		
		return writer.handleEvent(event);
	}

	@Override
	protected Event handleEndBatch (Event event) {
		event = writer.handleEvent(event);
		writer.close();
		if ( didMerge ) {
			((IMergeable)writer).doPostMerge();
		}
		writer = null;

		if ( createTipp ) {
			FileUtil.zipFiles(resolvedOutputDir + ".tipp", tempPackageRoot,
				Manifest.MANIFEST_FILENAME+".xml",
				XLIFF2PackageWriter.POBJECTS_DIR+".zip");
			Util.deleteDirectory(tempPackageRoot, false);
		}
		else if ( params.getCreateZip() ) {
			FileUtil.zipDirectory(tempPackageRoot, RainbowKitFilter.RAINBOWKIT_PACKAGE_EXTENSION, resolvedOutputDir);
			Util.deleteDirectory(tempPackageRoot, false);
		}
		
		return event;
	}
	
	@Override
	protected Event handleRawDocument (Event event) {
		RawDocument rd = event.getRawDocument();

		String tmpIn = rd.getInputURI().getPath();
		String relativeInput = tmpIn.substring(inputRootDir.length()+1);
		String relativeOutput = relativeInput; // Input and Output are the same for reference files
		
		writer.setDocumentInformation(relativeInput, "", "", "", relativeOutput, "", null);
		return writer.handleEvent(event);
	}
	
	@Override
	protected Event handleStartDocument (Event event) {
		StartDocument sd = event.getStartDocument();
		IParameters prm = sd.getFilterParameters();
		String paramsData = null;
		if ( prm != null ) {
			paramsData = prm.toString();
		}

		String relativeInput = new File(inputRootDir).toURI().relativize(inputURI).getPath();
		String relativeOutput = new File(outputRootDir).toURI().relativize(outputURI).getPath();

		writer.setDocumentInformation(relativeInput, filterConfigId, paramsData, sd.getEncoding(),
			relativeOutput, outputEncoding, sd.getFilterWriter().getSkeletonWriter());
		return writer.handleEvent(event);
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}
	
	@Override
	public IParameters getParameters () {
		return params;
	}

    protected IPackageWriter getPackageWriter() {
        return writer;
    }
	
	private boolean shouldMerge () {
		if (context == null || context.getIsNoPrompt()) return false;
		
		if ( writer instanceof IMergeable ) {
			return promptShouldMerge();
		}
		else {
			return !promptShouldOverwrite();
		}
	}

	private boolean promptShouldMerge () {
		String message = "A directory already exists at the target location. "
			+ "Would you like to merge the new translation kit with the existing directory? "
			+ "Select \"No\" to overwrite the existing directory.";
		
		return getPrompt().promptYesNoCancel(message);
	}

	private boolean promptShouldOverwrite () {
		String message = "A directory already exists at the target location.\n"
			+ "Select \"OK\" to overwrite it.";
		
		return getPrompt().promptOKCancel(message);
	}

	private IUserPrompt getPrompt() {
		String promptClass = context.getIsGui() ? "net.sf.okapi.common.ui.UserPrompt"
				: "net.sf.okapi.common.UserPrompt";
		try {
			IUserPrompt p = (IUserPrompt) Class.forName(promptClass).newInstance();
			p.initialize(context.getUiParent(), context.getApplicationName());
			return p;
		}
		catch ( Throwable e ) {
			throw new InstantiationError("Could not instantiate user prompt.");
		}
	}
}
