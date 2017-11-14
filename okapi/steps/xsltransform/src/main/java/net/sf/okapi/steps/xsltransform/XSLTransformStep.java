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

package net.sf.okapi.steps.xsltransform;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import net.sf.okapi.common.ConfigurationString;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

@UsingParameters(Parameters.class)
public class XSLTransformStep extends BasePipelineStep {

	private static final String FACTORY_PROP = "javax.xml.transform.TransformerFactory";
	private static final String XPATH_PROP =  "javax.xml.xpath.XPathFactory";
	private static final String VAR_SRCLANG = "${srcLang}"; 
	private static final String VAR_TRGLANG = "${trgLang}"; 
	private static final String VAR_INPUTPATH = "${inputPath}"; 
	private static final String VAR_INPUTURI = "${inputURI}"; 
	private static final String VAR_OUTPUTPATH = "${outputPath}"; 
	private static final String VAR_INPUTPATH1 = "${inputPath1}"; 
	private static final String VAR_INPUTURI1 = "${inputURI1}"; 
	private static final String VAR_OUTPUTPATH1 = "${outputPath1}"; 
	private static final String VAR_INPUTPATH2 = "${inputPath2}"; 
	private static final String VAR_INPUTURI2 = "${inputURI2}"; 
	private static final String VAR_INPUTPATH3 = "${inputPath3}"; 
	private static final String VAR_INPUTURI3 = "${inputURI3}"; 

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private Source xsltInput;
	private Map<String, String> paramList;
	private Transformer trans;
	private javax.xml.transform.TransformerFactory fact;
	private boolean isDone;
	private URI outputURI;
	private RawDocument input1;
	private RawDocument input2;
	private RawDocument input3;
	private String originalProcessor;
	private String originalXpathProcessor;
	
	private ErrorListener errorListener;
	private String systemId;
	private EntityResolver entityResolver;
	private URIResolver uriResolver;
	
	
	public XSLTransformStep () {
		params = new Parameters();
		trans = null;
		originalProcessor = System.getProperty(FACTORY_PROP);
		originalXpathProcessor = System.getProperty(XPATH_PROP);		
	}
	
	@Override
	public void destroy () {
		// Make available to GC
		trans = null;
		xsltInput = null;
	}

	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_RAWDOC)
	public void setInput (RawDocument input) {
		input1 = input;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput (RawDocument secondInput) {
		input2 = secondInput;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.THIRD_INPUT_RAWDOC)
	public void setThirdInput (RawDocument thridInput) {
		input3 = thridInput;
	}
	
	public String getDescription () {
		return "Apply an XSLT template to an XML document."
			+ " Expects: raw XML document. Sends back: raw document.";
	}

	public String getName () {
		return "XSL Transformation";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}
	
	@Override
	public boolean isDone () {
		return isDone;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}
 
	@Override
	protected Event handleStartBatch (Event event) {
		try {
			// Create the parameters map
			ConfigurationString cfgString = new ConfigurationString(params.getParamList());
			paramList = cfgString.toMap();
			
			// Create the source for the XSLT
			xsltInput = new javax.xml.transform.stream.StreamSource(
				new File(params.getXsltPath()));
			
			// Create an instance of TransformerFactory
			if ( params.getUseCustomTransformer() ) {
				System.setProperty(FACTORY_PROP, params.getFactoryClass());
				if (!Util.isEmpty(params.getXpathClass()))
					System.setProperty(XPATH_PROP, params.getXpathClass());
			}
			fact = javax.xml.transform.TransformerFactory.newInstance();
			if (errorListener != null) {
				fact.setErrorListener(errorListener);
			}

			trans = fact.newTransformer(xsltInput);
			logger.info("Factory used: {}", fact.getClass().getCanonicalName());
			logger.info("Transformer used: {}", trans.getClass().getCanonicalName());
			isDone = true;
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error in XSLT input.\n" + e.getMessage(), e);
		}
//		finally {
			// Make sure to reset the original property
//			if ( params.useCustomTransformer ) {
//				System.setProperty(FACTORY_PROP, originalProcessor);
//				System.setProperty(XPATH_PROP, originalXpathProcessor);
//			}
//  	}
		
		return event;
	}
	
	@Override
	protected Event handleStartBatchItem (Event event) {
		isDone = false;
		return event;
	}

	@Override
	protected Event handleRawDocument (Event event) {
		try {
			RawDocument rawDoc = (RawDocument)event.getResource();
			File outFile = null;
			trans.reset();
			fillParameters();
			
			Properties props = trans.getOutputProperties();
			for ( Object obj: props.keySet() ) {
				String key = (String)obj;
				String value = props.getProperty(key);
				value = value+"";
			}
			
			// Create the input source
			// Use the stream, so the encoding auto-detection can be done
			XMLReader reader = XMLReaderFactory.createXMLReader();
			SAXSource xmlInput = new SAXSource(reader, new InputSource(rawDoc.getStream()));
			
			if (systemId != null) {
				xmlInput.setSystemId(systemId);
			}
			
			if (entityResolver != null) {
				reader.setEntityResolver(entityResolver);
			}
			
			// FIXME: do we need to set this twice? see handleStartBatch
			if (errorListener != null) {
				fact.setErrorListener(errorListener);
			}
			
			if (uriResolver != null) {
				trans.setURIResolver(uriResolver);
			}
		
			// Create the output
			outFile = new File(outputURI);
			
			// In some cases we want outputURI to be a directory
			// in this case use the input file name to complete the path
			if (outFile.isDirectory()) {
				outFile = new File(outFile, Util.getFilename(rawDoc.getInputURI().getPath(), true));
			}
			
			Result result = new javax.xml.transform.stream.StreamResult(outFile);
			
			// Apply the template
			trans.transform(xmlInput, result);
	
			if (params.getPassOnOutput()) {
				// Create the new raw-document resource
				event.setResource(new RawDocument(outFile.toURI(), "UTF-8", 
					rawDoc.getSourceLocale(), rawDoc.getTargetLocale()));
			} else {
				outFile.delete(); // tmp output file
				// the xslt controls the output - don't pass on to subsequent steps
				return Event.NOOP_EVENT;
			}
		}
		catch ( TransformerException e ) {
			throw new OkapiIOException("Transformation error.\n" + e.getMessage(), e);
		} catch (SAXException e) {
			throw new OkapiIOException("Parser error.\n" + e.getMessage(), e);		}
		finally {	
			isDone = true;			
		}
		
		return event;
	}

	@Override
	protected Event handleEndBatch(Event event) {
		if ( params.getUseCustomTransformer() ) {
			if (originalProcessor == null) {
				System.clearProperty(FACTORY_PROP);
			} else {
				System.setProperty(FACTORY_PROP, originalProcessor);
			}
			
			if (originalXpathProcessor == null) {
				System.clearProperty(XPATH_PROP);
			} else {
				System.setProperty(XPATH_PROP, originalXpathProcessor);
			}
		}
		return event;
	}
	
	public void setErrorListener(ErrorListener errorListener) {
		this.errorListener = errorListener;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	public void setEntityResolver(EntityResolver entityResolver) {
		this.entityResolver = entityResolver;
	}

	public void setUriResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}

	private void fillParameters () {
		trans.clearParameters();
		String value = null;
		try {
			for ( String key : paramList.keySet() ) {
				// Try to find the replacement(s)
				value = paramList.get(key).replace(VAR_SRCLANG, input1.getSourceLocale().getLanguage());
				if ( value.indexOf(VAR_TRGLANG) > -1 ) {
					value = value.replace(VAR_TRGLANG, input1.getTargetLocale().getLanguage());
				}

				if ( value.indexOf(VAR_INPUTPATH) > -1 ) {
					value = value.replace(VAR_INPUTPATH, input1.getInputURI().getPath());
				}
				if ( value.indexOf(VAR_INPUTURI) > -1 ) {
					value = value.replace(VAR_INPUTURI, input1.getInputURI().toString());
				}
				if ( value.indexOf(VAR_OUTPUTPATH) > -1 ) {
					value = value.replace(VAR_OUTPUTPATH, outputURI.getPath());
				}
				
				if ( value.indexOf(VAR_INPUTPATH1) > -1 ) { // Same as VAR_INPUTPATH
					value = value.replace(VAR_INPUTPATH1, input1.getInputURI().getPath());
				}
				if ( value.indexOf(VAR_INPUTURI1) > -1 ) {
					value = value.replace(VAR_INPUTURI1, input1.getInputURI().toString());
				}
				if ( value.indexOf(VAR_OUTPUTPATH1) > -1 ) { // Same as VAR_OUTPUTPATH
					value = value.replace(VAR_OUTPUTPATH1, outputURI.getPath());
				}
				
				if ( value.indexOf(VAR_INPUTPATH2) > -1 ) {
					if ( input2 == null ) {
						value = value.replace(VAR_INPUTPATH2, "null");
					}
					else {
						value = value.replace(VAR_INPUTPATH2, input2.getInputURI().getPath());
					}
				}
				if ( value.indexOf(VAR_INPUTURI2) > -1 ) {
					if ( input2 == null ) {
						value = value.replace(VAR_INPUTURI2, "null");
					}
					else {
						value = value.replace(VAR_INPUTURI2, input2.getInputURI().toString());
					}
				}
				
				if ( value.indexOf(VAR_INPUTPATH3) > -1 ) {
					if ( input3 == null ) {
						value = value.replace(VAR_INPUTPATH3, "null");
					}
					else {
						value = value.replace(VAR_INPUTPATH3, input3.getInputURI().getPath());
					}
				}
				if ( value.indexOf(VAR_INPUTURI3) > -1 ) {
					if ( input3 == null ) {
						value = value.replace(VAR_INPUTURI3, "null");
					}
					else {
						value = value.replace(VAR_INPUTURI3, input3.getInputURI().toString());
					}
				}
				
				// Assign the variable
				trans.setParameter(key, value);
			}
		}
		catch ( Throwable e ) {
			logger.error("Error when trying to substitute variables in the parameter value '{}'", value);
		}
	}

}
