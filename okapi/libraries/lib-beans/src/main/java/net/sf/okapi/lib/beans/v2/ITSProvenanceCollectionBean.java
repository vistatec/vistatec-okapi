package net.sf.okapi.lib.beans.v2;

import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.filters.xliff.its.ITSProvenance;
import net.sf.okapi.filters.xliff.its.ITSProvenanceCollection;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ITSProvenanceCollectionBean extends
		PersistenceBean<ITSProvenanceCollection> {

	private String resource;
	private String xmlid;
	private String version;
	private List<ITSProvenanceBean> records = new LinkedList<ITSProvenanceBean>();
	
	@Override
	protected ITSProvenanceCollection createObject(IPersistenceSession session) {
		List<ITSProvenance> list = new LinkedList<ITSProvenance>();
		for (ITSProvenanceBean bean : records) {
			list.add(bean.get(ITSProvenance.class, session));
		}
		return new ITSProvenanceCollection(list, resource, xmlid, version);
	}

	@Override
	protected void setObject(ITSProvenanceCollection obj,
			IPersistenceSession session) {
	}

	@Override
	protected void fromObject(ITSProvenanceCollection obj,
			IPersistenceSession session) {
		while (obj.iterator().hasNext()) {
			ITSProvenance item = obj.iterator().next();
			ITSProvenanceBean bean = new ITSProvenanceBean();
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

	public final List<ITSProvenanceBean> getRecords() {
		return records;
	}

	public final void setRecords(List<ITSProvenanceBean> records) {
		this.records = records;
	}

}
