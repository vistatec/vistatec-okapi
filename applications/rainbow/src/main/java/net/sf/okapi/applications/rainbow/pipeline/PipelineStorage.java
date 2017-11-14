/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
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
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.pipeline.IPipeline;
import net.sf.okapi.common.pipeline.IPipelineReader;
import net.sf.okapi.common.pipeline.IPipelineStep;
import net.sf.okapi.common.pipeline.IPipelineWriter;
import net.sf.okapi.common.pipeline.Pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class PipelineStorage implements IPipelineWriter, IPipelineReader {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private String path;
	private StringWriter strWriter;
	private CharSequence inputData;
	private Map<String, StepInfo> availableSteps;

	public PipelineStorage (Map<String, StepInfo> availableSteps) {
		this.availableSteps = availableSteps;
		path = null;
	}
	
	public PipelineStorage (Map<String, StepInfo> availableSteps,
		String path)
	{
		this.availableSteps = availableSteps;
		this.path = path;
	}

	public PipelineStorage (Map<String, StepInfo> availableSteps,
		CharSequence inputData)
	{
		this.availableSteps = availableSteps;
		this.inputData = inputData;
	}
	
	public String getStringOutput () {
		return strWriter.toString();
	}
	
	@Override
	public void write (IPipeline pipeline) {
		XMLWriter writer = null;
		try {
			// Select the destination
			if ( path == null ) {
				// Use string writer
				strWriter = new StringWriter();
				writer = new XMLWriter(strWriter);
				// Force "\n" to avoid conversion of "\r\n" to "\r\r\n" later
				writer.setLineBreak(Util.LINEBREAK_UNIX);
			}
			else {
				// File writer
				writer = new XMLWriter(path);
			}

			writer.writeStartDocument();
			writer.writeStartElement("rainbowPipeline");
			writer.writeAttributeString("version", "1");
			for ( IPipelineStep step : pipeline.getSteps() ) {
				writer.writeStartElement("step");
				writer.writeAttributeString("class", step.getClass().getName());
				IParameters params = step.getParameters();
				if ( params != null ) {
					writer.writeString(params.toString());
				}
				writer.writeEndElementLineBreak(); // step
			}
		}
		finally {
			if ( writer != null ) {
				writer.writeEndElementLineBreak(); // rainbowPipeline
				writer.writeEndDocument();
				writer.close();
			}
		}
	}

	public IPipeline read () {
		try {
			DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
			Fact.setValidating(false);

			// Select the type of input
			Document doc;
			if ( path == null ) {
				// From a CharSequence
				InputSource is = new InputSource(new StringReader(inputData.toString()));
				doc = Fact.newDocumentBuilder().parse(is);
			}
			else {
				// From a path
				doc = Fact.newDocumentBuilder().parse(new File(path));
			}
			
			NodeList nodes = doc.getElementsByTagName("step");
			Pipeline pipeline = new Pipeline();
			
			for ( int i=0; i<nodes.getLength(); i++ ) {
				Node elem = nodes.item(i);
				Node node = elem.getAttributes().getNamedItem("class");
				if ( node == null ) {
					throw new OkapiException("The attribute 'class' is missing.");
				}
				// Create the class
				// Check if we can use the available steps (and their loaders)
				String className = node.getNodeValue();
				StepInfo stepInfo = availableSteps.get(className);
				if ( stepInfo == null ) {
					// The pipeline has a step that is not currently in the available steps
					LOGGER.warn(String.format(
						"The step '%s' is not among the steps currently available. " +
						"It will be removed from the loaded pipeline.",
						className));
					continue;
				}
				IPipelineStep step;
				if ( stepInfo.loader == null ) {
					step = (IPipelineStep)Class.forName(stepInfo.stepClass).newInstance();
				}
				else {
					step = (IPipelineStep)Class.forName(stepInfo.stepClass, true, stepInfo.loader).newInstance();
				}
				// Load the parameters if needed
				IParameters params = step.getParameters();
				if ( params != null ) {
					params.fromString(Util.getTextContent(elem));
				}
				// add the step
				pipeline.addStep(step);
			}
			
			return pipeline;
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
		catch ( SAXException e ) {
			throw new OkapiException(e);
		}
		catch ( ParserConfigurationException e ) {
			throw new OkapiException(e);
		}
		catch ( DOMException e ) {
			throw new OkapiException(e);
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

}
