package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class GenericAnnotationBean extends PersistenceBean<GenericAnnotation> {

	private String type;
	private String fields;
	
	@Override
	protected GenericAnnotation createObject(IPersistenceSession session) {
		return new GenericAnnotation(type);
	}

	@Override
	protected void setObject(GenericAnnotation obj, IPersistenceSession session) {
		obj.fromString(fields);
	}

	@Override
	protected void fromObject(GenericAnnotation obj, IPersistenceSession session) {
		type = obj.getType();
		fields = obj.toString();
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	

}
