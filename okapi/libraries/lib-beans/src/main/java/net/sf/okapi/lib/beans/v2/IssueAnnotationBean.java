package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.IssueAnnotation;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.lib.persistence.IPersistenceSession;

public class IssueAnnotationBean extends GenericAnnotationBean {

	private IssueType issueType = IssueType.OTHER;
	
	@Override
	protected GenericAnnotation createObject(IPersistenceSession session) {
		return new IssueAnnotation();
	}

	@Override
	protected void setObject(GenericAnnotation obj, IPersistenceSession session) {
		super.setObject(obj, session);
		if (obj instanceof IssueAnnotation) {
			((IssueAnnotation) obj).setIssueType(issueType);
		}
	}

	@Override
	protected void fromObject(GenericAnnotation obj, IPersistenceSession session) {
		super.fromObject(obj, session);
		if (obj instanceof IssueAnnotation) {
			issueType = ((IssueAnnotation) obj).getIssueType();
		}
	}

	public final IssueType getIssueType() {
		return issueType;
	}

	public final void setIssueType(IssueType issueType) {
		this.issueType = issueType;
	}
}
