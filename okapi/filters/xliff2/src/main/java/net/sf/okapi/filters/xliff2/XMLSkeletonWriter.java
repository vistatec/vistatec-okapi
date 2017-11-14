/*===========================================================================
  Copyright (C) 2014 by the Okapi Framework contributors
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
============================================================================*/

package net.sf.okapi.filters.xliff2;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderContext;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.resource.DocumentPart;
import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.StartSubfilter;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

public class XMLSkeletonWriter extends GenericSkeletonWriter {
	public static final String NAME_PLACEHOLDER = "[$$NAME$$]";
	public static final String CANRESEGMENT_PLACEHOLDER = "[$$CANRESEGMENT$$]";
	public static final String TRANSLATE_PLACEHOLDER = "[$$TRANSLATE$$]";
	public static final String SRCDIR_PLACEHOLDER = "[$$SRCDIR$$]";
	public static final String TRGDIR_PLACEHOLDER = "[$$TRGDIR$$]";
	public static final String TYPE_PLACEHOLDER = "[$$TYPE$$]";
	public static final String SEGMENTS_PLACEHOLDER = "\n[$$SEGMENTS$$]\n";
	
	public static final String ID = "id";
	public static final String NAME = "name";
	public static final String CANRESEGMENT = "canResegment";
	public static final String TRANSLATE = "translate";
	public static final String SRCDIR = "srcDir";
	public static final String TRGDIR = "trgDir";
	public static final String TYPE = "type";
	
	private Parameters params;
	
	@Override
	public void close () {
		// Nothing to do
	}

	@Override
	public String processStartDocument (LocaleId outputLocale,
		String outputEncoding,
		ILayerProvider layer,
		EncoderManager encoderManager,
		StartDocument resource)
	{
		setOutputLoc(outputLocale);
		setInputLoc(resource.getLocale());
		setEncoderManager(encoderManager);
		this.params = (Parameters)resource.getFilterParameters();
		
		if ( this.encoderManager != null ) {
			this.encoderManager.setDefaultOptions(params, outputEncoding,
				resource.getLineBreak());
			this.encoderManager.updateEncoder(resource.getMimeType());
		}
		
		// Process the resource
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		return skel.toString().replace("[#$$self$@%encoding]", outputEncoding);
	}

	@Override
	public String processEndDocument (Ending resource) {
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		return skel.toString();
	}

	@Override
	public String processStartSubDocument (StartSubDocument resource) {
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		return skel.toString();
	}

	@Override
	public String processEndSubDocument (Ending resource) {
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		return skel.toString();
	}

	@Override
	public String processStartGroup (StartGroup resource) {
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		return skel.toString();
	}

	@Override
	public String processEndGroup (Ending resource) {
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		return skel.toString();
	}

	@Override
	public String processTextUnit (ITextUnit resource) {
		// skeleton contains the full
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		// Else: build the output
		String tmp = new String(skel.toString());
		
		StringBuilder parts = new StringBuilder();		
		TextContainer srcTc = resource.getSource();
		TextContainer trgTc = resource.getTarget(getOutputLoc());
		
		// original was a block or paragraph, preserve the original segmentation.
		if (params.getMergeAsParagraph()) {
			srcTc = new TextContainer(resource.getSource().createJoinedContent());
			trgTc = new TextContainer(resource.getTarget(getOutputLoc()).createJoinedContent());
		}

		TextPart srcPart = null;
		TextPart trgPart = null;
		for ( int i=0; i<srcTc.count(); i++ ) {
			srcPart = srcTc.get(i);
			trgPart = null;
			if (i < trgTc.count()) {
				trgPart = trgTc.get(i);
			}
			if (srcPart.isSegment()) {
				parts.append(String.format(" <segment id=\"%s\">\n", ((Segment)srcPart).id));
			} else {
				parts.append(" <ignorable>\n");
			}
			// source
			if (srcPart != null) {
				parts.append("  <source>");
				parts.append(getContent(srcPart.text, getInputLoc(), EncoderContext.TEXT));
				parts.append("</source>\n");
			}
			
			// target
			if (trgPart != null) {
				parts.append("  <target>");
				parts.append(getContent(trgPart.text, getOutputLoc(), EncoderContext.TEXT));
				parts.append("</target>\n");
			}
			
			if (srcPart.isSegment()) {
				parts.append(" </segment>\n");
			} else {
				parts.append(" </ignorable>\n");
			}
		}
		
		// replace placeholders with new values
		if (resource.getProperty(NAME) != null) 
			tmp = tmp.replace(NAME_PLACEHOLDER, String.format(" name=\"%s\"", resource.getProperty(NAME).getValue()));  
		if (resource.getProperty(CANRESEGMENT) != null) 
			tmp = tmp.replace(CANRESEGMENT_PLACEHOLDER, String.format(" canResegment=\"%s\"", resource.getProperty(CANRESEGMENT).getValue()));
		if (resource.getProperty(TRANSLATE) != null) 
			tmp = tmp.replace(TRANSLATE_PLACEHOLDER, String.format(" translate=\"%s\"", resource.getProperty(TRANSLATE).getValue()));
		if (resource.getProperty(SRCDIR) != null) 
			tmp = tmp.replace(SRCDIR_PLACEHOLDER, String.format(" srcDir=\"%s\"", resource.getProperty(SRCDIR).getValue()));
		if (resource.getProperty(TRGDIR) != null) 
			tmp = tmp.replace(TRGDIR_PLACEHOLDER, String.format(" trgDir=\"%s\"", resource.getProperty(TRGDIR).getValue()));
		if (resource.getProperty(TYPE) != null) 
			tmp = tmp.replace(TYPE_PLACEHOLDER, String.format(" type=\"%s\"", resource.getProperty(TYPE).getValue())); 
		if (parts.length() > 0) {
			tmp = tmp.replace(SEGMENTS_PLACEHOLDER, parts.toString());
		}
		
		return tmp.toString();
	}

	@Override
	public String processDocumentPart (DocumentPart resource) {
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		return skel.toString();
	}

	@Override
	public String processStartSubfilter (StartSubfilter resource) {
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		//TODO: implement subfilter support
		return null;
	}

	@Override
	public String processEndSubfilter (EndSubfilter resource) {
		XMLSkeleton skel = (XMLSkeleton)resource.getSkeleton();
		if ( skel == null ) return "";
		// TODO: implement subfilter support
		return null;
	}
}
