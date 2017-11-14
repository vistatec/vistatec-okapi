package net.sf.okapi.lib.extra.pipelinebuilder;

import net.sf.okapi.common.filters.FilterConfigurationMapper;
import net.sf.okapi.common.filters.IFilter;

public class XFilters {

	private FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
	
	public XFilters(IFilter... filters) {
		for (IFilter filter : filters) {
			fcMapper.addConfigurations(filter.getClass().getName());
		}
	}
	
	public XFilters(Class<? extends IFilter>... filterClasses) {
		for (Class<? extends IFilter> cls : filterClasses) {
			fcMapper.addConfigurations(cls.getName());
		}
	}
	
	public XFilters(String... filtersNames) {
		for (String fname : filtersNames) {
			fcMapper.addConfigurations(fname);
		}
	}
	
	public FilterConfigurationMapper getFcMapper() {
		return fcMapper;
	}
	
}
