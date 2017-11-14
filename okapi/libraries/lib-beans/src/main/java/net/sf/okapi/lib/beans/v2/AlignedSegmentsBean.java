package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.common.resource.AlignedSegments;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.ReferenceBean;

public class AlignedSegmentsBean extends PersistenceBean<AlignedSegments> {

//	private FactoryBean parent = new FactoryBean();
	private ReferenceBean parent = new ReferenceBean();
	
	@Override
	protected AlignedSegments createObject(IPersistenceSession session) {
		return new AlignedSegments(parent.get(ITextUnit.class, session));
	}

	@Override
	protected void setObject(AlignedSegments obj, IPersistenceSession session) {
	}

	@Override
	protected void fromObject(AlignedSegments obj, IPersistenceSession session) {
		parent.set(obj.getParent(), session);
	}

	public final ReferenceBean getParent() {
		return parent;
	}

	public final void setParent(ReferenceBean parent) {
		this.parent = parent;
	}
	
//	public final ReferenceBean getParent() {
//		return parent;
//	}
//
//	public final void setParent(ReferenceBean parent) {
//		this.parent = parent;
//	}

}
