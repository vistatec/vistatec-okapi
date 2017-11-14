package net.sf.okapi.lib.beans.v1;

import net.sf.okapi.common.annotation.XLIFFTool;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class XLIFFToolBean extends PersistenceBean<XLIFFTool> {

	private String id;
	private String name;
	private String version;
	private String company;
	private String skel;
	
	@Override
	protected XLIFFTool createObject(IPersistenceSession session) {
		return new XLIFFTool(id, name);
	}

	@Override
	protected void setObject(XLIFFTool obj, IPersistenceSession session) {
		obj.setVersion(version);
		obj.setCompany(company);
		obj.addSkeletonContent(skel);
	}

	@Override
	protected void fromObject(XLIFFTool obj, IPersistenceSession session) {
		id = obj.getId();
		name = obj.getName();
		version = obj.getVersion();
		company = obj.getCompany();
		skel = obj.getSkel();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getSkel() {
		return skel;
	}

	public void setSkel(String skel) {
		this.skel = skel;
	}

}
