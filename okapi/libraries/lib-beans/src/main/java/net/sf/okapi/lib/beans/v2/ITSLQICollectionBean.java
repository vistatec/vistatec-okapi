package net.sf.okapi.lib.beans.v2;

import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.filters.xliff.its.ITSLQI;
import net.sf.okapi.filters.xliff.its.ITSLQICollection;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ITSLQICollectionBean extends PersistenceBean<ITSLQICollection> {

	private String resource;
	private String xmlid;
	private String version;
	private List<ITSLQIBean> issues = new LinkedList<ITSLQIBean>();
	
	@Override
	protected ITSLQICollection createObject(IPersistenceSession session) {
		List<ITSLQI> list = new LinkedList<ITSLQI>();
		for (ITSLQIBean bean : issues) {
			list.add(bean.get(ITSLQI.class, session));
		}
		return new ITSLQICollection(list, resource, xmlid, version);
	}

	@Override
	protected void setObject(ITSLQICollection obj, IPersistenceSession session) {
	}

	@Override
	protected void fromObject(ITSLQICollection obj, IPersistenceSession session) {
		while (obj.iterator().hasNext()) {
			ITSLQI item = obj.iterator().next();
			ITSLQIBean bean = new ITSLQIBean();
			bean.set(item, session);
		}
	}

	public final String getResource() {
		return resource;
	}

	public final void setResource(String resource) {
		this.resource = resource;
	}

	public final String getXmlid() {
		return xmlid;
	}

	public final void setXmlid(String xmlid) {
		this.xmlid = xmlid;
	}

	public final String getVersion() {
		return version;
	}

	public final void setVersion(String version) {
		this.version = version;
	}

	public final List<ITSLQIBean> getIssues() {
		return issues;
	}

	public final void setIssues(List<ITSLQIBean> issues) {
		this.issues = issues;
	}

}
