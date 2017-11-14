package net.sf.okapi.lib.beans.v1;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.steps.repetitionanalysis.RepetitiveSegmentAnnotation;
import net.sf.okapi.steps.repetitionanalysis.SegmentInfo;

public class RepetitiveSegmentAnnotationBean extends
		PersistenceBean<RepetitiveSegmentAnnotation> {

	private SegmentInfoBean info = new SegmentInfoBean();
	// Jackson is not able to deserialize a Map<SegmentInfoBean, Float> out of the box, 
	// so we store keys and values in 2 lists
	private List<SegmentInfoBean> segInfoBeans = new LinkedList<SegmentInfoBean>();
	private List<Float> scores = new LinkedList<Float>(); 
	
	@Override
	protected RepetitiveSegmentAnnotation createObject(
			IPersistenceSession session) {
		Map<SegmentInfo, Float> rsaMap = new HashMap<SegmentInfo, Float> ();
		for (int i = 0; i < segInfoBeans.size(); i++) {
			SegmentInfoBean bean = segInfoBeans.get(i);
			rsaMap.put(bean.get(SegmentInfo.class, session), scores.get(i));
		}
		return new RepetitiveSegmentAnnotation(info.get(SegmentInfo.class, session), rsaMap);
	}

	@Override
	protected void setObject(RepetitiveSegmentAnnotation obj,
			IPersistenceSession session) {
	}

	@Override
	protected void fromObject(RepetitiveSegmentAnnotation obj,
			IPersistenceSession session) {
		info.set(obj.getInfo(), session);
		
		for (SegmentInfo key : obj.getMap().keySet()) {
			SegmentInfoBean bean = new SegmentInfoBean();
			segInfoBeans.add(bean);
			scores.add(obj.getMap().get(key));
			
			bean.setTuid(key.getTuid());
			bean.setGroupId(key.getGroupId());
			bean.setSegId(key.getSegId());
		}
	}

	public SegmentInfoBean getInfo() {
		return info;
	}

	public void setInfo(SegmentInfoBean info) {
		this.info = info;
	}

	public List<SegmentInfoBean> getSegInfoBeans() {
		return segInfoBeans;
	}

	public void setSegInfoBeans(List<SegmentInfoBean> segInfoBeans) {
		this.segInfoBeans = segInfoBeans;
	}

	public List<Float> getScores() {
		return scores;
	}

	public void setScores(List<Float> scores) {
		this.scores = scores;
	}

}
