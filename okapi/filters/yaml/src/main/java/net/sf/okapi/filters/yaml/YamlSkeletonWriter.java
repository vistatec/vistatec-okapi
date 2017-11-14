/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

package net.sf.okapi.filters.yaml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;

import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.StringUtil;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextUnitUtil;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.filters.yaml.parser.Line;
import net.sf.okapi.filters.yaml.parser.YamlScalarTypes;

public class YamlSkeletonWriter extends GenericSkeletonWriter {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private Property scalarType;
	private Property scalarFlow;
	private Property scalarParentIndent;
	private boolean flow;
	private int fullIndent;
	private FlowStyle flowStyle;
	private boolean wrap;

	public YamlSkeletonWriter( boolean wrap) {
		super();
		this.wrap = wrap;
		this.flowStyle = FlowStyle.AUTO;
		flow = false;
	}
	
	@Override
	public String processTextUnit(ITextUnit resource) {
		TextUnitUtil.unsegmentTU(resource);

		// save scalar type for fragment processing below
		if (resource.hasProperty(YamlFilter.YAML_SCALAR_TYPE_PROPERTY_NAME)) {
			scalarType = resource.getProperty(YamlFilter.YAML_SCALAR_TYPE_PROPERTY_NAME);
			scalarFlow = resource.getProperty(YamlFilter.YAML_SCALAR_FLOW_PROPERTY_NAME);
			scalarParentIndent = resource.getProperty(YamlFilter.YAML_PARENT_INDENT_PROPERTY_NAME);
			flow = scalarFlow.getBoolean();
			fullIndent = Integer.parseInt(scalarParentIndent.getValue());
			if (flow) {
				flowStyle = FlowStyle.FLOW;
			} else {
				flowStyle = FlowStyle.BLOCK;
			}
		}
				
		getEncoderManager().updateEncoder(MimeTypeMapper.YAML_MIME_TYPE);
		YamlEncoder encoder = (YamlEncoder)getEncoderManager().getEncoder(); 
		encoder.setScalarType(YamlScalarTypes.valueOf(scalarType.getValue()));
		
		// send to subfilter and YamlEncoders first
		String encoded = super.processTextUnit(resource);			
		
//		// currently FOLDED scalars are the only ones we need to add indentation
//		// as the LB inline codes will force the lines after the first to column 1 
//		if (YamlScalarTypes.valueOf(scalarType.getValue()) == YamlScalarTypes.FOLDED) {
//			// add proper indentation for blocks
//			String indented = Line.prependWhitespace(fullIndent);
//			encoded = encoded.replaceAll("\n(?!\n)", "\n" + indented);
//		}
		
		// restore original newlines
		encoded = encoded.replaceAll("\n", getEncoderManager().getLineBreak());

		// reset TU properties
		this.flowStyle = FlowStyle.AUTO;
		flow = false;
		this.fullIndent = 0;
						
		return encoded;
	}
}
