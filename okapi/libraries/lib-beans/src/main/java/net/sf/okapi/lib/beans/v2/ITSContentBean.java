package net.sf.okapi.lib.beans.v2;

import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.filterwriter.ITSContent;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ITSContentBean extends PersistenceBean<ITSContent> {

	private CharsetEncoderBean encoder = new CharsetEncoderBean();
	private boolean isHTML5;
	private boolean isXLIFF;
	private List<GenericAnnotationsBean> standoff = new ArrayList<GenericAnnotationsBean>();
	
	@Override
	protected ITSContent createObject(IPersistenceSession session) {
		return new ITSContent(encoder.get(CharsetEncoder.class, session),
				isHTML5, isXLIFF);
	}

	@Override
	protected void setObject(ITSContent obj, IPersistenceSession session) {
		for (GenericAnnotationsBean bean : standoff) {
			if (obj.hasStandoff()) {
				obj.getStandoff().add(bean.get(GenericAnnotations.class, session));
			}			
		}		
	}

	@Override
	protected void fromObject(ITSContent obj, IPersistenceSession session) {
		encoder.set(obj.getEncoder(), session);
		isHTML5 = obj.isHTML5();
		isXLIFF = obj.isXLIFF();
		List<GenericAnnotations> soff = obj.getStandoff();
		if (soff == null) return;
		for (GenericAnnotations gas : soff) {
			GenericAnnotationsBean bean = new GenericAnnotationsBean();
			bean.set(gas, session);
			standoff.add(bean);
		}
	}

	public CharsetEncoderBean getEncoder() {
		return encoder;
	}

	public void setEncoder(CharsetEncoderBean encoder) {
		this.encoder = encoder;
	}

	public boolean isHTML5() {
		return isHTML5;
	}

	public void setHTML5(boolean isHTML5) {
		this.isHTML5 = isHTML5;
	}

	public boolean isXLIFF() {
		return isXLIFF;
	}

	public void setXLIFF(boolean isXLIFF) {
		this.isXLIFF = isXLIFF;
	}

	public List<GenericAnnotationsBean> getStandoff() {
		return standoff;
	}

	public void setStandoff(List<GenericAnnotationsBean> standoff) {
		this.standoff = standoff;
	}

}
