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

package net.sf.okapi.steps.rainbowkit.versified;

import java.io.File;

import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.filters.rainbowkit.Manifest;
import net.sf.okapi.filters.rainbowkit.MergingInfo;
import net.sf.okapi.filters.versifiedtxt.VersifiedTextFilter;
import net.sf.okapi.steps.rainbowkit.rtf.RTFLayerWriter;

public class VersifiedRtfPackageWriter extends VersifiedPackageWriter {

	public VersifiedRtfPackageWriter() {
		super();
		extractionType = Manifest.EXTRACTIONTYPE_VERSIFIED_RTF;
	}

	@Override
	protected void processEndBatchItem() {
		// Finish the Versified output
		super.processEndBatchItem();

		// The Versified output is done.
		// Now re-write it with the RTF layer.
		RTFLayerWriter layerWriter = null;
		IFilter filter = null;
		File inpFile = null;
		try {
			// Prepare the output in RTF from the temporary Versified file
			MergingInfo info = manifest.getItem(docId);
			
			if ( info.getExtractionType().equals(Manifest.EXTRACTIONTYPE_NONE) ) {
				// This file is not to be extracted
				return;
			}
			
			inpFile = new File(manifest.getTempSourceDirectory() + info.getRelativeInputPath() + ".vrsz");
			String outPath = inpFile.getAbsolutePath() + ".rtf";
			RawDocument rd = new RawDocument(inpFile.toURI(), "UTF-8", manifest.getSourceLocale());
			rd.setTargetLocale(manifest.getTargetLocale());

			// Create the Versified filter and open the Versified file
			filter = new VersifiedTextFilter();
			filter.open(rd);

			// Prepare the layer writer
			layerWriter = new RTFLayerWriter(filter.createSkeletonWriter(), outPath,
					manifest.getTargetLocale(), info.getTargetEncoding());

			// Process the file
			while (filter.hasNext()) {
				layerWriter.writeEvent(filter.next());
			}
		} finally {
			if (filter != null)
				filter.close();
			if (layerWriter != null)
				layerWriter.close();
			if (inpFile != null) {
				inpFile.delete();
			}
		}
	}
}