package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.filters.xliff.its.ITSProvenance;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ITSProvenanceBean extends PersistenceBean<ITSProvenance> {

	private String person, org, tool, revPerson, revOrg, revTool, provRef;
	
	@Override
	protected ITSProvenance createObject(IPersistenceSession session) {
		return new ITSProvenance(person, org, tool, revPerson, revOrg, 
				revTool, provRef);
	}

	@Override
	protected void setObject(ITSProvenance obj, IPersistenceSession session) {
	}

	@Override
	protected void fromObject(ITSProvenance obj, IPersistenceSession session) {
		person = obj.getPerson();
		org = obj.getOrg();
		tool = obj.getTool();
		revPerson = obj.getPerson();
		revOrg = obj.getRevOrg();
		revTool = obj.getRevTool();
		provRef = obj.getProvRef();
	}

	public final String getPerson() {
		return person;
	}

	public final void setPerson(String person) {
		this.person = person;
	}

	public final String getOrg() {
		return org;
	}

	public final void setOrg(String org) {
		this.org = org;
	}

	public final String getTool() {
		return tool;
	}

	public final void setTool(String tool) {
		this.tool = tool;
	}

	public final String getRevPerson() {
		return revPerson;
	}

	public final void setRevPerson(String revPerson) {
		this.revPerson = revPerson;
	}

	public final String getRevOrg() {
		return revOrg;
	}

	public final void setRevOrg(String revOrg) {
		this.revOrg = revOrg;
	}

	public final String getRevTool() {
		return revTool;
	}

	public final void setRevTool(String revTool) {
		this.revTool = revTool;
	}

	public final String getProvRef() {
		return provRef;
	}

	public final void setProvRef(String provRef) {
		this.provRef = provRef;
	}

}
