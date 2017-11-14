package net.sf.okapi.steps.common.tufiltering;

import net.sf.okapi.common.resource.ITextUnit;

import org.junit.Ignore;

@Ignore("This class is instantiated in TestTuFilteringStep only")
public class MyTuFilter implements ITextUnitFilter {

	@Override
	public boolean accept(ITextUnit tu) {
		return "tu2".equals(tu.getId());
	}

}
