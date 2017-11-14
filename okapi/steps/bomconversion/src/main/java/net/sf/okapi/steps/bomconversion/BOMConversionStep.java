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

package net.sf.okapi.steps.bomconversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.RawDocument;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class BOMConversionStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final byte[] BOM_UTF8 = {(byte)0xEF,(byte)0xBB,(byte)0xBF};
	private final byte[] BOM_UTF16BE = {(byte)0xFE,(byte)0xFF};
	private final byte[] BOM_UTF16LE = {(byte)0xFF,(byte)0xFE};

	private Parameters params;
	private byte[] buffer;
	private URI outputURI;

	public BOMConversionStep () {
		params = new Parameters();
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI (URI outputURI) {
		this.outputURI = outputURI;
	}
	
	public String getDescription () {
		return "Add or remove Unicode Byte-Order-Mark (BOM) in a text-based file."
			+ " Expects: raw document. Sends back: raw document.";
	}

	public String getName () {
		return "BOM Conversion";
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
	protected Event handleStartBatchItem (Event event) {
		buffer = new byte[1024*2];
		return event;
	}

	@Override
	protected Event handleEndBatchItem (Event event) {
		// Release the buffer
		buffer = null;
		return event;
	}
	
	@Override
	protected Event handleRawDocument (Event event) {
		RawDocument rawDoc;
		InputStream input = null;
		FileOutputStream output = null;
		
		try {
			rawDoc = event.getRawDocument();
			input = rawDoc.getStream();
						
			// Open the output
			File outFile;
			if ( isLastOutputStep() ) {
				outFile = rawDoc.createOutputFile(outputURI);
			}
			else {
				try {
					outFile = File.createTempFile("~okapi-37_okp-bom_", ".tmp");
				}
				catch ( Throwable e ) {
					throw new OkapiIOException("Cannot create temporary output.", e);
				}
			}
			output = new FileOutputStream(outFile);
			
			// Reset the start of the buffer
			for ( int i=0; i<5; i++ ) buffer[i] = 0;
			// And read the 4 initial bytes
			int len = input.read(buffer, 0, 4);
			
			// Process the initial buffer
			if ( len == -1 ) {
				// Do nothing yet
			}
			else if ( len == 0 ) { // Empty file
				if ( !params.getRemoveBOM() ) { // Add the BOM
					// Let's make that empty file a UTF-8 file
					output.write(BOM_UTF8);
				}
			}
			else { // Non-empty file
				int n = hasBOM(buffer, len);
				if ( n > 0 ) { // A BOM is present
					if ( params.getRemoveBOM() ) {
						if (( n == 3 ) || ( params.getAlsoNonUTF8() )) {
							// Skip it, output the remaining bytes
							output.write(buffer, n, len-n);
						}
						else {
							// Keep the BOM
							output.write(buffer, 0, len);
						}
					}
					else { // Add the BOM: It's there, just write the buffer 
						output.write(buffer, 0, len);
					}
				}
				else { // No BOM present: use the default encoding provided
					if ( !params.getRemoveBOM() ) { // If we add, do it
						String enc = rawDoc.getEncoding().toLowerCase();
						if ( enc.equals("utf-16") || enc.equals("utf-16le") ) {
							output.write(BOM_UTF16LE);
							logger.info("Added UTF-16LE BOM");
						}
						else if ( enc.equals("utf-16be") ) {
							output.write(BOM_UTF16BE);
							logger.info("Added UTF-16BE BOM");
						}
						else if ( enc.equals("utf-8") ) {
							output.write(BOM_UTF8);
							logger.info("Added UTF-8 BOM");
						}
						else { // Cannot add to un-supported encodings
							logger.warn("Cannot add a BOM to a document in {}.", enc);
						}
					}
					// Then write the buffer we checked
					output.write(buffer, 0, len);
				}
			}
			
			// Now copy the remaining of the file
			while ( (len = input.read(buffer)) > 0 ) {
				output.write(buffer, 0, len);
			}
			
			// Done: close the files
			input.close(); input = null;
			output.close(); output = null;
			rawDoc.finalizeOutput();
			
			// Creates the new RawDocument
			event.setResource(new RawDocument(outFile.toURI(), rawDoc.getEncoding(), 
				rawDoc.getSourceLocale(), rawDoc.getTargetLocale()));
		}
		catch ( IOException e ) {
			throw new OkapiIOException("IO error while converting.", e);
		}
		finally {
			try { // Close the files
				if ( output != null ) {
					output.close();
					output = null;
				}
				if ( input != null ) {
					input.close();
					input = null;
				}
			}
			catch ( IOException e ) {
				throw new OkapiIOException("IO error while closing.", e);
			}
		}
		
		return event;
	}

	/**
	 * Checks for BOM presence
	 * @param buffer The buffer to check.
	 * @param length The number of usable bytes in the buffer.
	 * @return 0 if there is no BOM, or the number of bytes used by
	 * the BOM if it is present.
	 */
	private int hasBOM (byte[] buffer,
		int length)
	{
		if ( length > 1 ) {
			// Check for UTF-16
			if (( buffer[0] == (byte)0xFE )
				&& ( buffer[1] == (byte)0xFF )) {
				// UTF-16BE
				logger.info("UTF-16BE detected");
				return 2;
			}
			else if (( buffer[0] == (byte)0xFF )
				&& ( buffer[1] == (byte)0xFE )) {
				// UTF-16LE
				logger.info("UTF-16LE detected");
				return 2;
			}
			// Check for UTF-8
			if ( length > 2 ) {
				if (( buffer[0] == (byte)0xEF )
					&& ( buffer[1] == (byte)0xBB )
					&& ( buffer[2] == (byte)0xBF )) {
					// UTF-8
					logger.info("UTF-8 detected");
					return 3;
				}
				// Check for UTF-32
				if ( length > 3) {
					if (( buffer[0] == (byte)0xFF )
						&& ( buffer[1] == (byte)0xFE )
						&& ( buffer[2] == (byte)0x00 )
						&& ( buffer[3] == (byte)0x00 )) {
						// UTF-32LE
						logger.info("UTF-32LE detected");
						return 4;
					}
					else if (( buffer[0] == (byte)0x00 )
						&& ( buffer[1] == (byte)0x00 )
						&& ( buffer[2] == (byte)0xFE )
						&& ( buffer[3] == (byte)0xFF )) {
						// UTF-32BE
						logger.info("UTF-32BE detected");
						return 4;
					}
				}
			}
		}
		return 0;
	}

}
