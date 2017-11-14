package net.sf.okapi.lib.beans.v1;

import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.steps.repetitionanalysis.SegmentInfo;

public class SegmentInfoBean extends PersistenceBean<SegmentInfo> {

	private String tuid;
	private String groupId;
	private String segId;
	
	@Override
	protected SegmentInfo createObject(IPersistenceSession session) {
		return new SegmentInfo(tuid, groupId, segId);
	}

	@Override
	protected void setObject(SegmentInfo obj, IPersistenceSession session) {
	}

	@Override
	protected void fromObject(SegmentInfo obj, IPersistenceSession session) {
		tuid = obj.getTuid();
		groupId = obj.getGroupId();
		segId = obj.getSegId();
	}

	public String getTuid() {
		return tuid;
	}

	public void setTuid(String tuid) {
		this.tuid = tuid;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getSegId() {
		return segId;
	}

	public void setSegId(String segId) {
		this.segId = segId;
	}

}
