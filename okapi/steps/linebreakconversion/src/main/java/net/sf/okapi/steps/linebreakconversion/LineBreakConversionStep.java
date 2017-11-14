/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.linebreakconversion;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

@UsingParameters(Parameters.class)
public class LineBreakConversionStep extends BasePipelineStep {

	private static final int BUFFER_SIZE = 1024;
	
	private Parameters params;
	private URI outputURI;

	public LineBreakConversionStep () {
		params = new Parameters();
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
	public String getDescription () {
		return "Convert the type of line-breaks in a text-based file."
			+ " Expects: raw document. Sends back: raw document.";
	}

	public String getName () {
		return "Line-Break Conversion";
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
	protected Event handleRawDocument (Event event) {
		RawDocument rawDoc;
		BufferedReader reader = null;
		OutputStreamWriter writer = null;
		try {
			rawDoc = event.getRawDocument();
			
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(rawDoc.getStream(), rawDoc.getEncoding());
			detector.detectAndRemoveBom();
			rawDoc.setEncoding(detector.getEncoding());
			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), rawDoc.getEncoding()));
			
			// Open the output
			File outFile;
			if ( isLastOutputStep() ) {
				outFile = rawDoc.createOutputFile(outputURI);
			}
			else {
				try {
					outFile = File.createTempFile("~okapi-48_okp-lbc_", ".tmp");
				}
				catch ( Throwable e ) {
					throw new OkapiIOException("Cannot create temporary output.", e);
				}
			}
			OutputStream output = new FileOutputStream(outFile);
			
			writer = new OutputStreamWriter(new BufferedOutputStream(output), rawDoc.getEncoding());
			// Write BOM if there was one
			Util.writeBOMIfNeeded(writer, detector.hasUtf8Bom(), rawDoc.getEncoding());
			
			// Set the variables
			char[] buf = new char[BUFFER_SIZE];
			int length = 0;
			int i;
			int done = 0;
			
			// Process the file
			while ( (length = reader.read(buf, 0, BUFFER_SIZE-1)) > 0 ) {
				// Check if you need to read the next char to avoid splitting cases
				if ( buf[length-1] == '\r'  ) {
					int count = reader.read(buf, length, 1);
					if ( count > -1 ) length++;
				}
				// Reset 'done' flag on second pass after it was set
				if ( done == 1 ) done++; else done = 0;
				// Replace line-breaks
				int start = 0;
				for ( i=0; i<length; i++ ) {
					if ( buf[i] == '\n') {
						if (( i != 0 ) || ( done == 0 )) {
							writer.write(buf, start, i-start);
							writer.write(params.getLineBreak());
						}
						start = i+1;
					}
					else if ( buf[i] == '\r') {
						writer.write(buf, start, i-start);
						writer.write(params.getLineBreak());
						// Check if it's a \r\n
						if ( i+1 < length ) {
							if ( buf[i+1] == '\n' ) {
								i++; // Skip it
							}
						}
						start = i+1;
						// We could be splitting a \r\n, so let's remember
						done = 1;
					}
				}
				// Write out the remainder of the buffer
				if ( length-start > 0 ) {
					writer.write(buf, start, length-start);
				}
			}
			
			// Done: close the output
			reader.close(); reader = null;
			writer.close(); writer = null;
			rawDoc.finalizeOutput();
			
			// Creates the new RawDocument
			event.setResource(new RawDocument(outFile.toURI(), rawDoc.getEncoding(), 
				rawDoc.getSourceLocale(), rawDoc.getTargetLocale()));
		}
		catch ( IOException e ) {
			throw new OkapiIOException("IO error while converting.", e);
		}
		finally {
			try {
				if ( writer != null ) {
					writer.close();
				}
				if ( reader != null ) {
					reader.close();
				}
			}
			catch ( IOException e ) {
				throw new OkapiIOException("IO error while closing.", e);
			}
		}
		
		return event;
	}
}
