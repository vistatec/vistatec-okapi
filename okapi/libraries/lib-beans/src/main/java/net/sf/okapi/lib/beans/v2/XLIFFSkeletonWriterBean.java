package net.sf.okapi.lib.beans.v2;

import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.filterwriter.ITSContent;
import net.sf.okapi.common.filterwriter.XLIFFContent;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.filters.xliff.Parameters;
import net.sf.okapi.filters.xliff.XLIFFSkeletonWriter;
import net.sf.okapi.filters.xliff.its.ITSStandoffManager;
import net.sf.okapi.lib.beans.v1.ParametersBean;
import net.sf.okapi.lib.persistence.BeanMap;
import net.sf.okapi.lib.persistence.IPersistenceSession;

public class XLIFFSkeletonWriterBean extends GenericSkeletonWriterBean {

	private ParametersBean params = new ParametersBean();
	private XLIFFContentBean fmt = new XLIFFContentBean();
	private ITSContentBean itsCont = new ITSContentBean();
	private ITSStandoffManagerBean itsStandoffManager = new ITSStandoffManagerBean();
	private Map<String, GenericAnnotationsBean> lqiStandoff = new HashMap<String, GenericAnnotationsBean>();
	private Map<String, GenericAnnotationsBean> provStandoff = new HashMap<String, GenericAnnotationsBean>();
	private CharsetEncoderBean chsEnc = new CharsetEncoderBean();
	
	@Override
	protected XLIFFSkeletonWriter createObject(IPersistenceSession session) {
		XLIFFSkeletonWriter skelWriter = new XLIFFSkeletonWriter(
				params.get(Parameters.class, session),
				fmt.get(XLIFFContent.class, session),
				itsCont.get(ITSContent.class, session),
				itsStandoffManager.get(ITSStandoffManager.class, session),
				BeanMap.get(lqiStandoff, GenericAnnotations.class, session),
				BeanMap.get(provStandoff, GenericAnnotations.class, session),
				chsEnc.get(CharsetEncoder.class, session)				
			);
//		super.set(skelWriter, session);
		return skelWriter;
	}

	@Override
	protected void fromObject(GenericSkeletonWriter o,
			IPersistenceSession session) {
		super.fromObject(o, session);
		if (o instanceof XLIFFSkeletonWriter) {
			XLIFFSkeletonWriter obj = (XLIFFSkeletonWriter) o;
			params.set(obj.getParams(), session);
			fmt.set(obj.getFmt(), session);
			itsCont.set(obj.getItsCont(), session);
			itsStandoffManager.set(obj.getITSStandoffManager(), session);
			BeanMap.set(lqiStandoff, GenericAnnotationsBean.class, obj.getLqiStandoff(), session); 
			BeanMap.set(provStandoff, GenericAnnotationsBean.class, obj.getProvStandoff(), session);
			chsEnc.set(obj.getCharsetEncoder(), session);
		}		
	}

	public final ParametersBean getParams() {
		return params;
	}

	public final void setParams(ParametersBean params) {
		this.params = params;
	}

	public final XLIFFContentBean getFmt() {
		return fmt;
	}

	public final void setFmt(XLIFFContentBean fmt) {
		this.fmt = fmt;
	}

	public final ITSContentBean getItsCont() {
		return itsCont;
	}

	public final void setItsCont(ITSContentBean itsCont) {
		this.itsCont = itsCont;
	}

	public final ITSStandoffManagerBean getItsStandoffManager() {
		return itsStandoffManager;
	}

	public final void setItsStandoffManager(
			ITSStandoffManagerBean itsStandoffManager) {
		this.itsStandoffManager = itsStandoffManager;
	}

	public final Map<String, GenericAnnotationsBean> getLqiStandoff() {
		return lqiStandoff;
	}

	public final void setLqiStandoff(Map<String, GenericAnnotationsBean> lqiStandoff) {
		this.lqiStandoff = lqiStandoff;
	}

	public final Map<String, GenericAnnotationsBean> getProvStandoff() {
		return provStandoff;
	}

	public final void setProvStandoff(
			Map<String, GenericAnnotationsBean> provStandoff) {
		this.provStandoff = provStandoff;
	}

	public final CharsetEncoderBean getChsEnc() {
		return chsEnc;
	}

	public final void setChsEnc(CharsetEncoderBean chsEnc) {
		this.chsEnc = chsEnc;
	}

}
