package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.common.resource.AlignmentStatus;
import net.sf.okapi.common.resource.Segments;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.ReferenceBean;

public class SegmentsBean extends PersistenceBean<Segments> {

	private AlignmentStatus alignmentStatus;
    private ReferenceBean parent = new ReferenceBean();
//    private ReferenceBean parts = new ReferenceBean();
    
    public SegmentsBean() {
		super();
	}
    
//    private class PartsList extends ArrayList<TextPart> {
//		private static final long serialVersionUID = 1L;    	
//    }
    
	@Override
	protected Segments createObject(IPersistenceSession session) {
		return new Segments(parent.get(TextContainer.class, session));
	}

	@Override
	protected void setObject(Segments obj, IPersistenceSession session) {
		obj.setAlignmentStatus(alignmentStatus);
//		obj.setParts(BeanList.get(parts, TextPart.class, session));
		
//		List<TextPart> destParts = new ArrayList<TextPart>(); 
//		for (ReferenceBean bean : parts) {
//			if (bean.contains(Segment.class)) {
//				destParts.add(bean.get(Segment.class, session));
//			}
//			else if (bean.contains(TextPart.class)) {
//				destParts.add(bean.get(TextPart.class, session));
//			}
//		}
		TextContainer p = parent.get(TextContainer.class, session);
		obj.setParts(p.getParts());
	}

	@Override
	protected void fromObject(Segments obj, IPersistenceSession session) {
		alignmentStatus = obj.getAlignmentStatus();
		parent.set(obj.getParent(), session);
//		BeanList.set(parts, TextPartBean.class, obj.getParts(), session);
		
//		for (TextPart part : obj.getParts()) {
//			FactoryBean bean = new FactoryBean();
//			bean.set(part, session);
//			parts.add(bean);
//		}
//		parts.set(obj.getParts(), session);
	}

	public final AlignmentStatus getAlignmentStatus() {
		return alignmentStatus;
	}

	public final void setAlignmentStatus(AlignmentStatus alignmentStatus) {
		this.alignmentStatus = alignmentStatus;
	}

	public final ReferenceBean getParent() {
		return parent;
	}

	public final void setParent(ReferenceBean parent) {
		this.parent = parent;
	}

//	public ReferenceBean getParts() {
//		return parts;
//	}
//
//	public final void setParts(ReferenceBean parts) {
//		this.parts = parts;
//	}

}
