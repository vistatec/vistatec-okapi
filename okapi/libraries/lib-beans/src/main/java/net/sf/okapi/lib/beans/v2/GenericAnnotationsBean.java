package net.sf.okapi.lib.beans.v2;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.lib.persistence.IPersistenceSession;

public class GenericAnnotationsBean extends InlineAnnotationBean {

	private List<GenericAnnotationBean> list = new ArrayList<GenericAnnotationBean>();
	
	@Override
	protected Object createObject(IPersistenceSession session) {
		return super.createObject(session);
	}

	@Override
	protected void setObject(Object o, IPersistenceSession session) {
		super.setObject(o, session);
		
		if (o instanceof GenericAnnotations) {
			GenericAnnotations obj = (GenericAnnotations) o;
			for (GenericAnnotationBean bean : list) {
				obj.add(bean.get(GenericAnnotation.class, session));
			}			
		}		
	}
	
	@Override
	protected void fromObject(Object o, IPersistenceSession session) {
		super.fromObject(o, session);
		
		if (o instanceof GenericAnnotations) {
			GenericAnnotations obj = (GenericAnnotations) o;
			for (GenericAnnotation ann : obj) {
				GenericAnnotationBean bean = new GenericAnnotationBean();
				bean.set(ann, session);
				list.add(bean);
			}
		}
	}

	public List<GenericAnnotationBean> getList() {
		return list;
	}

	public void setList(List<GenericAnnotationBean> list) {
		this.list = list;
	}

}
