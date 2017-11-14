/*===========================================================================
  Copyright (C) 2009-2012 by the Okapi Framework contributors
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

package net.sf.okapi.applications.rainbow.pipeline;

import java.io.File;
import java.net.URI;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sf.okapi.applications.rainbow.Input;
import net.sf.okapi.applications.rainbow.Project;
import net.sf.okapi.common.ExecutionContext;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.IParametersEditorMapper;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.ParametersEditorMapper;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.pipeline.IPipeline;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipeline.Pipeline;
import net.sf.okapi.common.pipelinedriver.BatchItemContext;
import net.sf.okapi.common.pipelinedriver.IPipelineDriver;
import net.sf.okapi.common.pipelinedriver.PipelineDriver;
import net.sf.okapi.common.plugins.PluginItem;
import net.sf.okapi.common.plugins.PluginsManager;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.steps.leveraging.LeveragingStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineWrapper {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Map<String, StepInfo> availableSteps;
	private Map<String, ClassLoader> pluginConnectors;
	private String path;
	private ArrayList<StepInfo> steps;
	private IPipelineDriver driver;
	private IFilterConfigurationMapper fcMapper;
	private IParametersEditorMapper peMapper;
	private PluginsManager pm;

	public PipelineWrapper (IFilterConfigurationMapper fcMapper,
		String appFolder,
		PluginsManager pm,
		String rootDir,
		String inputRootDir,
		String outputDir,
		Object uiParent,
		ExecutionContext context)
	{
		this.fcMapper = fcMapper;
		this.pm = pm;
		steps = new ArrayList<StepInfo>();
		driver = new PipelineDriver();
		driver.setFilterConfigurationMapper(this.fcMapper);
		driver.setRootDirectories(rootDir, inputRootDir);
		driver.setOutputDirectory(outputDir == null ? inputRootDir : outputDir);
		driver.setUIParent(uiParent);
		driver.setExecutionContext(context);
		refreshAvailableStepsList();
	}
	
	public void refreshAvailableStepsList () {
		// Hard-wired steps
		buildStepList();
		// Discover and add plug-ins
		addFromPlugins(pm);
	}
	
	public void addFromPlugins (PluginsManager pm) {
		try {
			pluginConnectors = new LinkedHashMap<String, ClassLoader>(); 
			List<PluginItem> plugins = pm.getList();
			URLClassLoader classLoader = pm.getClassLoader();
			for ( PluginItem item : plugins ) {
				if ( item.getType() == PluginItem.TYPE_IQUERY ) {
					pluginConnectors.put(item.getClassName(), classLoader);
					continue;
				}
				
				// Skip plug-ins that are not steps
				if ( item.getType() != PluginItem.TYPE_IPIPELINESTEP ) continue;
				try {
					// Instantiate the step and get its info
					IPipelineStep ps = (IPipelineStep)Class.forName(item.getClassName(), true, classLoader).newInstance();
					IParameters params = ps.getParameters();
					StepInfo stepInfo = new StepInfo(ps.getName(), ps.getDescription(), ps.getClass().getName(), classLoader,
						(params==null) ? null : params.getClass().getName());
					
					// Try to get the editor info if needed
					if ( params != null ) {
						stepInfo.paramsData = params.toString();
						if ( item.getEditorDescriptionProvider() != null ) {
							peMapper.addDescriptionProvider(item.getEditorDescriptionProvider(), stepInfo.paramsClass);
						}
						if ( item.getParamsEditor() != null ) {
							peMapper.addEditor(item.getParamsEditor(), stepInfo.paramsClass);
						}
					}
					
					// Add the step
					availableSteps.put(stepInfo.stepClass, stepInfo);
				}
				catch ( Throwable e ) {
					logger.warn("Could not instantiate step '{}' because of error.\n{}",
						item.getClassName(), e.getMessage());
				}
			}
			if (fcMapper instanceof FilterConfigurationMapper) {
				((FilterConfigurationMapper) fcMapper).addFromPlugins(pm);
			}	
		}
		catch ( Throwable e ) {
			throw new OkapiException("Error when creating the plug-ins lists.\n"+e.getMessage(), e);
		}
	}

	public void setRootDirectories (String rootDir,
		String inputRootDir)
	{
		driver.setRootDirectories(rootDir, inputRootDir);
	}
	
	/**
	 * Populate the hard-wired steps.
	 */
	private void buildStepList () {
		availableSteps = new LinkedHashMap<String, StepInfo>();		
		peMapper = new ParametersEditorMapper();
		List<PipelineStepUIDescription> pipelineStepDescriptions = buildPipelineStepDescriptions();
		
		for(PipelineStepUIDescription desc : pipelineStepDescriptions) {
			try {
				IPipelineStep ps = (IPipelineStep)Class.forName(desc.getPipelineStepClass()).newInstance();
				StepInfo step = new StepInfo(ps.getName(), ps.getDescription(), ps.getClass().getName(), null /* class loader */, null /* parameters class */);
				IParameters params = ps.getParameters();
				if ( params != null ) {
					step.paramsClass = params.getClass().getName();
					step.paramsData = params.toString();
					
					if (desc.hasDescriptionProvider())
						peMapper.addDescriptionProvider(desc.getDescriptionProviderClass(), step.paramsClass);
					
					if (desc.hasEditor())
						peMapper.addEditor(desc.getEditorClass(), step.paramsClass);
				}
				
				availableSteps.put(step.stepClass, step);
			} catch ( InstantiationException e ) {
				logger.warn("Could not instantiate step for class " + desc.getPipelineStepClass() + ". Skipping to next step.\n{}", e.getMessage());
			}
			catch ( IllegalAccessException e ) {
				logger.warn("Illegal access for step for class " + desc.getPipelineStepClass() + ". Skipping to next step.\n{}", e.getMessage());
			}
			catch ( ClassNotFoundException e ) {
				logger.warn("Step class " + desc.getPipelineStepClass() + " not found. Skipping to next step.\n{}", e.getMessage());
			}
			catch ( Throwable e ) {
				logger.warn("Error creating step for class " + desc.getPipelineStepClass() + " Skipping to next step.\n{}", e.getMessage());
			}
		}
	}
	
	/**
	 * Builds a list of (currently hard-coded) pipeline step descriptions for 
	 * available pipeline steps.  
	 */
	private List<PipelineStepUIDescription> buildPipelineStepDescriptions() {
		// OPT MW: The mapping could be externalised, e.g. to an XML file.
		
		List<PipelineStepUIDescription> pipelineStepUIDescriptions = new ArrayList<PipelineStepUIDescription>();

		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.RawDocumentToFilterEventsStep"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.FilterEventsToRawDocumentStep"));
		// Usable only via script	pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.FilterEventsWriterStep"));
		// Usable only via script	pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.RawDocumentWriterStep"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.leveraging.BatchTmLeveragingStep", null /* editorClass */, "net.sf.okapi.steps.leveraging.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.batchtranslation.BatchTranslationStep", null /* editorClass */, "net.sf.okapi.steps.batchtranslation.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.bomconversion.BOMConversionStep", "net.sf.okapi.steps.bomconversion.ui.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.charlisting.CharListingStep", null /* editorClass */, "net.sf.okapi.steps.charlisting.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.codesremoval.CodesRemovalStep", null /* editorClass */, "net.sf.okapi.steps.codesremoval.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.codesimplifier.CodeSimplifierStep", null /* editorClass */, "net.sf.okapi.steps.common.codesimplifier.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.codesimplifier.PostSegmentationCodeSimplifierStep", null /* editorClass */, "net.sf.okapi.steps.common.codesimplifier.PostSegmentationParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.cleanup.CleanupStep", null /* editorClass */, "net.sf.okapi.steps.cleanup.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.ConvertSegmentsToTextUnitsStep"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.copyormove.CopyOrMoveStep", null /* editorClass */, "net.sf.okapi.steps.copyormove.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.createtarget.CreateTargetStep", null /* editorClass */, "net.sf.okapi.steps.common.createtarget.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.desegmentation.DesegmentationStep", null /* editorClass */, "net.sf.okapi.steps.desegmentation.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.diffleverage.DiffLeverageStep", null /* editorClass */, "net.sf.okapi.steps.diffleverage.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.encodingconversion.EncodingConversionStep", "net.sf.okapi.steps.encodingconversion.ui.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.enrycher.EnrycherStep", null /* editorClass */, "net.sf.okapi.steps.enrycher.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.externalcommand.ExternalCommandStep", null /* editorClass */, "net.sf.okapi.steps.externalcommand.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.ExtractionVerificationStep", null /* editorClass */, "net.sf.okapi.steps.common.ExtractionVerificationStepParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.formatconversion.FormatConversionStep", null /* editorClass */, "net.sf.okapi.steps.formatconversion.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.fullwidthconversion.FullWidthConversionStep", "net.sf.okapi.steps.fullwidthconversion.ui.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.generatesimpletm.GenerateSimpleTmStep", null /* editorClass */, "net.sf.okapi.steps.generatesimpletm.ParametersUI"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.gttbatchtranslation.GTTBatchTranslationStep", null /* editorClass */, "net.sf.okapi.steps.gttbatchtranslation.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.idaligner.IdBasedAlignerStep", null /* editorClass */, "net.sf.okapi.steps.idaligner.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.idbasedcopy.IdBasedCopyStep", null /* editorClass */, "net.sf.okapi.steps.idbasedcopy.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.imagemodification.ImageModificationStep", null /* editorClass */, "net.sf.okapi.steps.imagemodification.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.inconsistencycheck.InconsistencyCheckStep", null /* editorClass */, "net.sf.okapi.steps.inconsistencycheck.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.leveraging.LeveragingStep", "net.sf.okapi.steps.leveraging.ui.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.terminologyleveraging.TerminologyLeveragingStep", null /* editorClass */, "net.sf.okapi.steps.terminologyleveraging.TerminologyParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.linebreakconversion.LineBreakConversionStep", null /* editorClass */, "net.sf.okapi.steps.linebreakconversion.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.moses.ExtractionStep", null /* editorClass */, "net.sf.okapi.filters.mosestext.FilterWriterParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.moses.MergingStep", null /* editorClass */, "net.sf.okapi.steps.moses.MergingParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.msbatchtranslation.MSBatchTranslationStep", null /* editorClass */, "net.sf.okapi.steps.msbatchtranslation.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.msbatchtranslation.MSBatchSubmissionStep", null /* editorClass */, "net.sf.okapi.steps.msbatchtranslation.SubmissionParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.paraaligner.ParagraphAlignerStep", null /* editorClass */, "net.sf.okapi.steps.paraaligner.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.qualitycheck.QualityCheckStep", "net.sf.okapi.lib.ui.verification.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.rainbowkit.creation.ExtractionStep", "net.sf.okapi.steps.rainbowkit.ui.CreationParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.rainbowkit.postprocess.MergingStep", null /* editorClass */, "net.sf.okapi.steps.rainbowkit.postprocess.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.removetarget.RemoveTargetStep", null /* editorClass */, "net.sf.okapi.steps.common.removetarget.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.repetitionanalysis.RepetitionAnalysisStep", null /* editorClass */, "net.sf.okapi.steps.repetitionanalysis.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.common.ResourceSimplifierStep"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.rtfconversion.RTFConversionStep", null /* editorClass */, "net.sf.okapi.steps.rtfconversion.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.scopingreport.ScopingReportStep", null /* editorClass */, "net.sf.okapi.steps.scopingreport.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.searchandreplace.SearchAndReplaceStep", "net.sf.okapi.steps.searchandreplace.ui.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.segmentation.SegmentationStep", "net.sf.okapi.steps.segmentation.ui.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.sentencealigner.SentenceAlignerStep", null /* editorClass */, "net.sf.okapi.steps.sentencealigner.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.simpletm2tmx.SimpleTM2TMXStep"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.spacecheck.SpaceCheckStep"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.termextraction.TermExtractionStep", null /* editorClass */, "net.sf.okapi.steps.termextraction.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.textmodification.TextModificationStep", "net.sf.okapi.steps.textmodification.ui.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.tmimport.TMImportStep", null /* editorClass */, "net.sf.okapi.steps.tmimport.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.tokenization.TokenizationStep", "net.sf.okapi.steps.tokenization.ui.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.translationcomparison.TranslationComparisonStep", null /* editorClass */, "net.sf.okapi.steps.translationcomparison.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.ttxsplitter.TTXJoinerStep", null /* editorClass */, "net.sf.okapi.steps.ttxsplitter.TTXJoinerParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.ttxsplitter.TTXSplitterStep", null /* editorClass */, "net.sf.okapi.steps.ttxsplitter.TTXSplitterParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.uriconversion.UriConversionStep", "net.sf.okapi.steps.uriconversion.ui.ParametersEditor", null /* descriptionProviderClass */));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.wordcount.WordCountStep", null /* editorClass */, "net.sf.okapi.steps.wordcount.common.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.wordcount.CharacterCountStep", null /* editorClass */, "net.sf.okapi.steps.wordcount.common.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.wordcount.SimpleWordCountStep"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.whitespacecorrection.WhitespaceCorrectionStep", null /* editorClass */, "net.sf.okapi.steps.whitespacecorrection.WhitespaceCorrectionStepParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.xmlcharfixing.XMLCharFixingStep", null /* editorClass */, "net.sf.okapi.steps.xmlcharfixing.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.xliffsplitter.XliffJoinerStep", null /* editorClass */, "net.sf.okapi.steps.xliffsplitter.XliffJoinerParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.xliffsplitter.XliffSplitterStep", null /* editorClass */, "net.sf.okapi.steps.xliffsplitter.XliffSplitterParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.xliffsplitter.XliffWCSplitterStep", null /* editorClass */, "net.sf.okapi.steps.xliffsplitter.XliffWCSplitterParameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.xmlanalysis.XMLAnalysisStep", null /* editorClass */, "net.sf.okapi.steps.xmlanalysis.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.xmlvalidation.XMLValidationStep", null /* editorClass */, "net.sf.okapi.steps.xmlvalidation.Parameters"));
		pipelineStepUIDescriptions.add(new PipelineStepUIDescription("net.sf.okapi.steps.xsltransform.XSLTransformStep", "net.sf.okapi.steps.xsltransform.ui.ParametersEditor", null /* descriptionProviderClass */));

		return pipelineStepUIDescriptions;
	}

	public void clear () {
		steps.clear();		
	}
	
	public String getPath () {
		return path;
	}
	
	public void setPath (String path) {
		this.path = path;
	}
	
	public IParametersEditorMapper getEditorMapper () {
		return peMapper;
	}
	
	public Map<String, StepInfo> getAvailableSteps () {
		return availableSteps;
	}
	
	public String getStringStorage () {
		copyInfoStepsToPipeline();
		PipelineStorage store = new PipelineStorage(availableSteps);
		store.write(driver.getPipeline());
		return store.getStringOutput();
	}
	
	public void reset () {
		clear();
		path = null;
		driver.setPipeline(new Pipeline());
	}
	
	public void loadFromStringStorageOrReset (String data) {
		if ( Util.isEmpty(data) ) {
			reset();
			return;
		}
		PipelineStorage store = new PipelineStorage(availableSteps, (CharSequence)data);
		loadPipeline(store.read(), null);
	}
	
	public void loadPipeline (IPipeline newPipeline,
		String path)
	{
		driver.setPipeline(newPipeline);
		// Set the info-steps
		StepInfo infoStep;
		IParameters params;
		steps.clear();
		for ( IPipelineStep step : driver.getPipeline().getSteps() ) {
			infoStep = new StepInfo(step.getName(), step.getDescription(),
				step.getClass().getName(), step.getClass().getClassLoader(), null);
			params = step.getParameters();
			if ( params != null ) {
				infoStep.paramsData = params.toString();
				infoStep.paramsClass = params.getClass().getName();
			}
			steps.add(infoStep);
		}
		this.path = path;
	}
	
	public void load (String path) {
		PipelineStorage store = new PipelineStorage(availableSteps, path);
		loadPipeline(store.read(), path);
	}
	
	public void save (String path) {
		PipelineStorage store = new PipelineStorage(availableSteps, path);
		copyInfoStepsToPipeline();
		store.write(driver.getPipeline());
		this.path = path;
	}
	
	public IPipeline getPipeline () {
		copyInfoStepsToPipeline();
		return driver.getPipeline();
	}
	
	public PluginsManager getPluginsManager() {
		return pm;		
	}
	
	private void copyInfoStepsToPipeline () {
		try {
			// Build the pipeline
			driver.setPipeline(new Pipeline());
			for ( StepInfo stepInfo : steps ) {
				IPipelineStep step;
				if ( stepInfo.loader == null ) {
					step = (IPipelineStep)Class.forName(stepInfo.stepClass).newInstance();
				}
				else {
					step = (IPipelineStep)Class.forName(stepInfo.stepClass,
						true, stepInfo.loader).newInstance();
				}
				// Update the parameters with the one in the pipeline storage
				IParameters params = step.getParameters();
				if (( params != null ) && ( stepInfo.paramsData != null )) {
					params.fromString(stepInfo.paramsData);
				}
				
				// Enable connectors from plug-ins
				if (step instanceof LeveragingStep) {
					LeveragingStep ls = (LeveragingStep) step;
					net.sf.okapi.steps.leveraging.Parameters lsParams = 
						(net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
					String connectorClassName = lsParams.getResourceClassName();
					ClassLoader connectorLoader = pluginConnectors.get(connectorClassName);
					
					ls.setConnectorContext(connectorLoader);
				}
				driver.addStep(step);
			}
		}
		catch ( InstantiationException e ) {
			throw new OkapiException(e);
		}
		catch ( IllegalAccessException e ) {
			throw new OkapiException(e);
		}
		catch ( ClassNotFoundException e ) {
			throw new OkapiException(e);
		}
	}

	public void copyParametersToPipeline (IPipeline pipeline) {
		List<IPipelineStep> destSteps = pipeline.getSteps();
		if ( destSteps.size() != steps.size() ) {
			throw new OkapiException("Parameters and destination do not match.");
		}
		StepInfo stepInfo;
		for ( int i=0; i<destSteps.size(); i++ ) {
			stepInfo = steps.get(i);
			IParameters params = destSteps.get(i).getParameters();
			if ( params != null ) {
				params.fromString(stepInfo.paramsData);
			}
		}
	}

	public void execute (Project prj) {
		execute(prj, null);
	}
	
	public void execute (Project prj, List<LocaleId> targetLocales) {
		copyInfoStepsToPipeline();
		// Set the batch items
		driver.clearItems();
		//TODO: Replace this: driver.getPipeline().getContext().removeProperty("outputFile");
		int f = -1;
		URI outURI;
		URI inpURI;
		RawDocument rawDoc;
		BatchItemContext bic;
		int inputRequested = driver.getRequestedInputCount();
		
		for ( Input item : prj.getList(0) ) {
			f++;
			// Set the data for the first input of the batch item
			outURI = (new File(prj.buildTargetPath(0, item.relativePath))).toURI();
			inpURI = (new File(prj.getInputRoot(0) + File.separator + item.relativePath)).toURI();
			rawDoc = new RawDocument(inpURI, prj.buildSourceEncoding(item),
				prj.getSourceLanguage(), prj.getTargetLanguage());
			rawDoc.setFilterConfigId(item.filterConfigId);
			rawDoc.setId(Util.makeId(item.relativePath)); // Set the document ID based on its relative path
			if(targetLocales != null)
				rawDoc.setTargetLocales(targetLocales);
			bic = new BatchItemContext(rawDoc, outURI, prj.buildTargetEncoding(item));
			
			// Add input/output data from other input lists if requested
			for ( int j=1; j<3; j++ ) {
				// Does the utility requests this list?
				if ( j >= inputRequested ) break; // No need to loop more
				// Do we have a corresponding input?
				if ( 3 > j ) {
					// Data is available
					List<Input> list = prj.getList(j);
					// Make sure we have an entry for that list
					if ( list.size() > f ) {
						Input item2 = list.get(f);
						// Input
						outURI = (new File(prj.buildTargetPath(j, item2.relativePath))).toURI();
						inpURI = (new File(prj.getInputRoot(j) + File.separator + item2.relativePath)).toURI();
						rawDoc = new RawDocument(inpURI, prj.buildSourceEncoding(item),
							prj.getSourceLanguage(), prj.getTargetLanguage());
						rawDoc.setFilterConfigId(item2.filterConfigId);
						rawDoc.setId(Util.makeId(item2.relativePath)); // Set the document ID based on its relative path
						if(targetLocales != null)
							rawDoc.setTargetLocales(targetLocales);
						bic.add(rawDoc, outURI, prj.buildTargetEncoding(item2));
					}
					// If no entry for that list: it'll be null
				}
				// Else: don't add anything
				// The lists will return null and that is up to the utility to check.
			}
			
			// Add the constructed batch item to the driver's list
			driver.addBatchItem(bic);
		}

		// Execute
		driver.processBatch();
	}

	public void addStep (StepInfo step) {
		steps.add(step);
	}
	
	public void insertStep (int index,
		StepInfo step)
	{
		if ( index == -1 ) {
			steps.add(step);
		}
		else {
			steps.add(index, step);
		}
	}
	
	public void removeStep (int index) {
		steps.remove(index);
	}
	
	public List<StepInfo> getSteps () {
		return steps;
	}

}
