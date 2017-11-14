package net.sf.okapi.filters.table.csv;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.layerprovider.ILayerProvider;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;

public class CSVSkeletonWriter extends GenericSkeletonWriter {

	Parameters params = new Parameters();
	
	@Override
	public String processStartDocument(LocaleId outputLocale,
			String outputEncoding, ILayerProvider layer,
			EncoderManager encoderManager, StartDocument resource) {
		// Just get the current document's delimiter and qualifier
		IParameters params = resource.getFilterParameters();
		if (params instanceof net.sf.okapi.filters.table.Parameters) {
			params = ((net.sf.okapi.filters.table.Parameters) params).getActiveParameters();
		}
		this.params = (Parameters) params;
		return super.processStartDocument(outputLocale, outputEncoding, layer,
				encoderManager, resource);
	}
	
	@Override
	public String processTextUnit(ITextUnit tu) {
		if (tu.hasProperty(CommaSeparatedValuesFilter.PROP_QUALIFIED) && 
			"yes".equals(tu.getProperty(CommaSeparatedValuesFilter.PROP_QUALIFIED).getValue())) {
				return super.processTextUnit(tu);
		}

		TextContainer tc;
		boolean isTarget = tu.hasTarget(outputLoc);
		if (isTarget) {
			tc = tu.getTarget(outputLoc);
		}
		else {
			tc = tu.getSource();
		}
		
		if (tc == null || !params.addQualifiers)
			return super.processTextUnit(tu);
		
		// If the CSV file has a comma or new line in non-qualified filed values, qualify them 
		TextFragment tf = tc.getUnSegmentedContentCopy();
		String text = tf.toText(); // Just to detect "bad" characters
		if (text.contains(params.fieldDelimiter) || text.contains("\n")) {
			if (tc.hasBeenSegmented()) {
				tc.insert(0, new TextPart(params.textQualifier));
				tc.append(new TextPart(params.textQualifier));
			}
			else {
				tf.insert(0, new TextFragment(params.textQualifier));
				tf.append(params.textQualifier);
				tc.setContent(tf);
			}
			tu.setProperty(new Property(CommaSeparatedValuesFilter.PROP_QUALIFIED, 
					"yes"));
		}		
		return super.processTextUnit(tu);
	}

}
