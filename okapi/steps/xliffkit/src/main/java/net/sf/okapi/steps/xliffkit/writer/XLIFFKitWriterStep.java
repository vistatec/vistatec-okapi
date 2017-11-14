/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.xliffkit.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.Annotations;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filterwriter.XLIFFWriter;
import net.sf.okapi.common.filterwriter.XLIFFWriterParameters;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.lib.beans.sessions.OkapiJsonSession;
import net.sf.okapi.lib.persistence.PersistenceSession;
import net.sf.okapi.steps.xliffkit.codec.CodecUtil;
import net.sf.okapi.steps.xliffkit.codec.DummyEncoder;
import net.sf.okapi.steps.xliffkit.codec.ICodec;
import net.sf.okapi.steps.xliffkit.opc.OPCPackageReader;
import net.sf.okapi.steps.xliffkit.opc.TKitRelationshipTypes;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.StreamHelper;
import org.apache.poi.openxml4j.opc.TargetMode;

@SuppressWarnings("unused")
@UsingParameters(Parameters.class)
public class XLIFFKitWriterStep extends BasePipelineStep {

	private XLIFFWriter writer;
	private ICodec codec;
	
	private LocaleId srcLoc;
	private LocaleId trgLoc;
	//private boolean inFile;
	private String docMimeType;
	private String docName;
	private String inputEncoding;
	private String configId;
	private Parameters params;
	private URI outputURI;
	
	private String resourcesFileExt = ".json";
	private String originalFileName;
	private String sourceFileName;
	private String xliffFileName;
	private String skeletonFileName;	
	private String resourcesFileName;
	
	private String originalPartName;
	private String sourcePartName;
	private String xliffPartName;
	private String skeletonPartName;	
	private String resourcesPartName;
	
	private String filterWriterClassName;

	private OPCPackage pack;
	private File tempXliff;
	private File tempResources;
	private PersistenceSession session;  // resource session
	private List<String> sources = new ArrayList<String> ();
	private List<String> originals = new ArrayList<String> ();
	
	public XLIFFKitWriterStep() {
		super();
		params = new Parameters();		
		session = new OkapiJsonSession(false);
		writer = new XLIFFWriter();
		// To output characters in PUA as &#xF003 (see XLIFFContent#toString());
		writer.getXLIFFContent().setCharsetEncoder(new DummyEncoder());
		codec = OPCPackageReader.CODEC;
	}
	
	public String getDescription () {
		return "Generate an XLIFF translation kit. Expects: filter events. Sends back: filter events.";
	}

	public String getName () {
		return "XLIFF Kit Writer";
	}

	public void close() {
		if ( writer != null ) {
			writer.close();
		}
	}

	public EncoderManager getEncoderManager() {
		return null;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.trgLoc = targetLocale;
	}

	public LocaleId getTargetLocale() {
	    return trgLoc;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}

	public URI getOutputURI() {
		return outputURI;
	}
	
	public Event handleEvent (Event event) {
		switch ( event.getEventType() ) {
		case NO_OP:
			return event;
		case START_BATCH:			
			processStartBatch();
			break;
		case END_BATCH:
			processEndBatch();
			break;
		case START_DOCUMENT:			
			processStartDocument(event.getStartDocument());
			break;
		case END_BATCH_ITEM:
			processEndBatchItem(event.getEnding()); // Closes persistence session
			close();
			break;
		case START_SUBDOCUMENT:
			processStartSubDocument(event.getStartSubDocument());
			break;
		case END_SUBDOCUMENT:
			processEndSubDocument(event.getEnding());
			break;
		case START_GROUP:
			processStartGroup(event.getStartGroup()); 
		    break;
		case START_SUBFILTER:
			processStartGroup(event.getStartSubfilter());
			break;
		case END_GROUP:
		case END_SUBFILTER:
			processEndGroup(event.getEnding());
			break;
		case TEXT_UNIT:
			ITextUnit tu = event.getTextUnit();			
			Event ev = new Event(EventType.TEXT_UNIT, tu.clone());
			session.serialize(event); // JSON
			processTextUnit(tu); // XLIFF
			return ev;
		case CANCELED:
		case CUSTOM:
		case DOCUMENT_PART:
		case END_DOCUMENT:
		case MULTI_EVENT:
		case PIPELINE_PARAMETERS:
		case RAW_DOCUMENT:
		case START_BATCH_ITEM:
		default:
			break;
		}
		session.serialize(event); // won't serialize END_DOCUMENT		
		return event;
	}
		
	private void processStartBatch() {		
		// If outputURI is defined explicitly in parameters, get it from there, otherwise use the one from the batch item
		if (params != null && !Util.isEmpty(params.getOutputURI())) {
			outputURI = Util.toURI(params.getOutputURI());
		}
		
		File outFile = new File(outputURI);
		if (outFile.exists()) 
			outFile.delete();
		
		Util.createDirectories(outFile.getAbsolutePath());
		try {
			pack = OPCPackage.openOrCreate(outFile);
		} catch (InvalidFormatException e1) {
			throw new OkapiException(e1);
		}

		XLIFFWriterParameters paramsXliff = (XLIFFWriterParameters)writer.getParameters();
		paramsXliff.setCopySource(params.isCopySource());
		paramsXliff.setPlaceholderMode(params.isPlaceholderMode());
		paramsXliff.setIncludeCodeAttrs(params.isIncludeCodeAttrs());

		session.setDescription(params.getMessage());
	}
	
	private void processEndBatch() {
		sources.clear();
		originals.clear();
		try {
			pack.close();
			
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
	}
	
	private void processStartDocument (StartDocument resource) {
		close();
		
		// Move persisted annotations from SD (annotations are placed there by 
		// the reader from its session), to the writer session annotations
		if (resource.getAnnotations() instanceof Annotations) {
			Annotations anns = (Annotations) resource.getAnnotations();
			for (IAnnotation ann : anns) {
				if (ann instanceof Serializable) {
					session.setAnnotation(ann);
				}
				// Remove all annotations from SD
				anns.remove(ann.getClass());
			}
		}
		
		srcLoc = resource.getLocale();						
		docMimeType = resource.getMimeType();
		docName = resource.getName();
		if (Util.isEmpty(docName))
			docName = "noname.noext"; // For the cases when the original file name is not found
		inputEncoding = resource.getEncoding();
		
		IParameters fparams = resource.getFilterParameters();
		if ( fparams == null ) configId = null;
		else configId = fparams.getPath();
		
		originalFileName = Util.getFilename(docName, true);
		sourceFileName = Util.getFilename(docName, true);
		xliffFileName = originalFileName + ".xlf";
		resourcesFileName = originalFileName + resourcesFileExt;
		skeletonFileName = String.format("resources/%s/%s", sourceFileName, resourcesFileName);
		
		filterWriterClassName = resource.getFilterWriter().getClass().getName();
		
		try {
			tempXliff = File.createTempFile("~okapi-54_" + xliffFileName + "_", null);
		
			tempResources = File.createTempFile("~okapi-55_" + resourcesFileName + "_", null);
		
			writer.create(tempXliff.getAbsolutePath(), skeletonFileName, resource.getLocale(), trgLoc,
					resource.getMimeType(), sourceFileName, params.getMessage());
		} catch (IOException e) {
			throw new OkapiIOException(e);
		}
		
		// Skeleton
		try {
			session.start(new FileOutputStream(tempResources));
		} catch (FileNotFoundException e) {
			throw new OkapiIOException(e);
		}
	}
	
	private PackagePart createPart(OPCPackage pack, PackagePart corePart, String name, File file, String contentType, String relationshipType) {		
		PackagePart part = null;
		try {			
			PackagePartName partName = PackagingURIHelper.createPartName("/" + name);
			if (pack.containPart(partName))	return null;
			
			part = pack.createPart(partName, contentType);
			if (corePart != null)
				corePart.addRelationship(partName, TargetMode.INTERNAL, relationshipType);
			else 
				pack.addRelationship(partName, TargetMode.INTERNAL, relationshipType);				
			
			try {
				InputStream is = new FileInputStream(file);
				OutputStream os = part.getOutputStream(); 
				StreamHelper.copyStream(is, os);
				try {
					is.close();
					os.close();
				} catch (IOException e) {
					throw new OkapiIOException(e);
				}
				
			} catch (FileNotFoundException e) {
				throw new OkapiIOException(e);
			}
			
		} catch (InvalidFormatException e) {
			throw new OkapiException(e);
		}
		return part;
	}
	
	
	
	private void processEndBatchItem (Ending ending) {
		// Get serializable annotations and store them in session annotations
		// No need to remove them from the resource as EBI is not serialized (only SD...ED)
		if (ending != null)
			for (IAnnotation ann : ending.getAnnotations()) {
				if (ann instanceof Serializable) {
					session.setAnnotation(ann);
				}
			}
		
		// Skeleton
		session.end();
		
		// XLIFF
		writer.close();
		
		originalPartName = encodePartName(String.format("content/original/%s/%s", srcLoc.toString(), originalFileName));
		sourcePartName = encodePartName(String.format("content/source/%s/%s", srcLoc.toString(), sourceFileName));
		xliffPartName = encodePartName(String.format("content/target/%s.%s/%s", srcLoc.toString(), trgLoc.toString(), xliffFileName));
		resourcesPartName = encodePartName(String.format("content/target/%s.%s/resources/%s/%s", srcLoc.toString(), trgLoc.toString(), sourceFileName, resourcesFileName));		
		
		PackagePart corePart =
			createPart(pack, null, xliffPartName, tempXliff, MimeTypeMapper.XLIFF_MIME_TYPE, TKitRelationshipTypes.CORE_DOCUMENT);
		
		createPart(pack, corePart, resourcesPartName, tempResources, session.getMimeType(), TKitRelationshipTypes.RESOURCES);
		
		if (params.isIncludeSource())
			if (!sources.contains(docName)) {				
				createPart(pack, corePart, sourcePartName, new File(docName), docMimeType, TKitRelationshipTypes.SOURCE);
				sources.add(docName);
			}
		
		if (params.isIncludeOriginal())
			if (!originals.contains(docName)) {
				createPart(pack, corePart, originalPartName, new File(docName), docMimeType, TKitRelationshipTypes.ORIGINAL);
				originals.add(docName);
			}
	}

	/**
	 * Encode part name to exclude spaces and non-ASCII characters coming from the original file name.    
	 * @param partName the given part name
	 * @return encoded part name.
	 */
	private String encodePartName(String partName) {
		partName = partName.replaceAll("\\s", "_");
		partName = Normalizer.normalize(partName, Normalizer.Form.NFD);
		return partName.replaceAll("[^\\p{ASCII}]", "");
	}

	private void processStartSubDocument (StartSubDocument resource) {
		writer.writeStartFile(resource.getName(), resource.getMimeType(), skeletonFileName);
	}
	
	private void processEndSubDocument (Ending resource) {
		writer.writeEndFile();
	}
	
	private void processStartGroup (StartGroup resource) {
		writer.writeStartGroup(resource);
	}
	
	private void processEndGroup (Ending resource) {
		writer.writeEndGroup();
	}
	
	private void processTextUnit (ITextUnit tu) {
		tu = tu.clone(); // Not to affect preserveWS and other parts of the original TU
		tu.setPreserveWhitespaces(true); // Not to end up with \r\r\n for line breaks
		CodecUtil.encodeTextUnit(tu, codec);
		writer.writeTextUnit(tu);
	}
	
	@Override
	public IParameters getParameters() {
		return params;
	}

	protected PersistenceSession getSession() {
		return session;
	}

	protected void setSession(PersistenceSession session) {
		this.session = session;
	}

	public String getResourcesFileExt() {
		return resourcesFileExt;
	}

	public void setResourcesFileExt(String resourcesFileExt) {
		this.resourcesFileExt = resourcesFileExt;
	}
	
	protected XLIFFWriter getWriter() {
		return writer;
	}
}
