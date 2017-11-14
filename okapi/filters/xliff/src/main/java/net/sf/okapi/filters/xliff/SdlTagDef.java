package net.sf.okapi.filters.xliff;

import net.sf.okapi.common.resource.Code;

class SdlTagDef {
	String id;
	String name;
	String equiv_text;

	Code bpt;
	Code ept;
	Code ph;
	
	// TODO: find examples, never seen
	Code it;
	
	// external, non-inline codes. AFAIK always non-translatable except when there are subflows embedded
	Code st;
	
	public SdlTagDef() {
	}	
}
