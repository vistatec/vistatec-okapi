package net.sf.okapi.lib.beans.v1;

import net.sf.okapi.common.resource.EndSubfilter;
import net.sf.okapi.common.resource.Ending;
import net.sf.okapi.lib.persistence.IPersistenceSession;

public class EndSubfilterBean extends EndingBean {

	@Override
	protected EndSubfilter createObject(IPersistenceSession session) {
		return new EndSubfilter(getId());
	}

	@Override
	protected void setObject(Ending obj, IPersistenceSession session) {
		super.setObject(obj, session);
	}

	@Override
	protected void fromObject(Ending obj, IPersistenceSession session) {
		super.fromObject(obj, session);
	}

}
