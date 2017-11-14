package net.sf.okapi.steps.common.copysource;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextContainer;

public class CopySourceOnEmptyTargetStep extends BasePipelineStep {

	private LocaleId targetLocale;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected Event handleTextUnit(Event event) {
		ITextUnit tu = event.getTextUnit();
		TextContainer source = tu.getSource(); 
		
		if (!source.hasText(false))
			return super.handleTextUnit(event);
		
		TextContainer target = tu.getTarget(targetLocale);
		boolean copySource = target == null;
		if (!copySource) {
			copySource = !target.hasText(false);
		}
				
		if (copySource) {
			if (target == null)
				tu.createTarget(targetLocale, true, IResource.COPY_SEGMENTED_CONTENT);
			else {				
//				TextFragment tf = TextUnitUtil.storeSegmentation(source);
//				TextUnitUtil.restoreSegmentation(target, tf);
				String content = TextContainer.contentToString(source);
				target.setContentFromString(content);
			}
							
//			// Copy original target properties
//			boolean hasProperties = target != null && target.getPropertyNames() != null && target.getPropertyNames().size() > 0;
//			if (hasProperties) {
//				for (String propName : target.getPropertyNames()) {
//					newTarget.setProperty(target.getProperty(propName));
//				}
//			}
		}		
		return super.handleTextUnit(event);
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	public LocaleId getTargetLocale() {
		return targetLocale;
	}
}
