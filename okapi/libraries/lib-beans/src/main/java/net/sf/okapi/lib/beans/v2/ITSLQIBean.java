package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.common.annotation.IssueAnnotation;
import net.sf.okapi.filters.xliff.its.ITSLQI;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ITSLQIBean extends PersistenceBean<ITSLQI> {

	private IssueAnnotationBean ann = new IssueAnnotationBean();
	
	@Override
	protected ITSLQI createObject(IPersistenceSession session) {
		return new ITSLQI(ann.get(IssueAnnotation.class, session));
	}

	@Override
	protected void setObject(ITSLQI obj, IPersistenceSession session) {
	}

	@Override
	protected void fromObject(ITSLQI obj, IPersistenceSession session) {
		ann.set(obj.getAnnotation(), session);
	}

	public final IssueAnnotationBean getAnn() {
		return ann;
	}

	public final void setAnn(IssueAnnotationBean ann) {
		this.ann = ann;
	}

}
