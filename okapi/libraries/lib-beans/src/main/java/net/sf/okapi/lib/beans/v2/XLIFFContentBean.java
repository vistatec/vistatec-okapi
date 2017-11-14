package net.sf.okapi.lib.beans.v2;

import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.filterwriter.ITSContent;
import net.sf.okapi.common.filterwriter.XLIFFContent;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.lib.beans.v1.CodeBean;
import net.sf.okapi.lib.persistence.BeanList;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;

public class XLIFFContentBean extends PersistenceBean<XLIFFContent> {

	private String codedText;
	private List<CodeBean> codes = new ArrayList<CodeBean>();
	private FactoryBean innerContent = new FactoryBean();
	private CharsetEncoderBean chsEnc = new CharsetEncoderBean();
	private List<GenericAnnotationsBean> standoff = new ArrayList<GenericAnnotationsBean>();
	private ITSContentBean itsCont = new ITSContentBean();
	
	@Override
	protected XLIFFContent createObject(IPersistenceSession session) {
		return new XLIFFContent(
				codedText, 
				BeanList.get(codes, Code.class, session),
				innerContent.get(XLIFFContent.class, session),
				chsEnc.get(CharsetEncoder.class, session),
				BeanList.get(standoff, GenericAnnotations.class, session),
				itsCont.get(ITSContent.class, session)
				);
	}

	@Override
	protected void setObject(XLIFFContent obj, IPersistenceSession session) {
	}

	@Override
	protected void fromObject(XLIFFContent obj, IPersistenceSession session) {
		codedText = obj.getCodedText();
		BeanList.set(codes, CodeBean.class, obj.getCodes(), session);
		innerContent.set(obj.getInnerContent(), session);
		chsEnc.set(obj.getCharsetEncoder(), session);
		BeanList.set(standoff, GenericAnnotationsBean.class, obj.getStandoff(), session);
		itsCont.set(obj.getItsCont(), session);
	}

	public final String getCodedText() {
		return codedText;
	}

	public final void setCodedText(String codedText) {
		this.codedText = codedText;
	}

	public final List<CodeBean> getCodes() {
		return codes;
	}

	public final void setCodes(List<CodeBean> codes) {
		this.codes = codes;
	}

	public final FactoryBean getInnerContent() {
		return innerContent;
	}

	public final void setInnerContent(FactoryBean innerContent) {
		this.innerContent = innerContent;
	}

	public final CharsetEncoderBean getChsEnc() {
		return chsEnc;
	}

	public final void setChsEnc(CharsetEncoderBean chsEnc) {
		this.chsEnc = chsEnc;
	}

	public final List<GenericAnnotationsBean> getStandoff() {
		return standoff;
	}

	public final void setStandoff(List<GenericAnnotationsBean> standoff) {
		this.standoff = standoff;
	}

	public final ITSContentBean getItsCont() {
		return itsCont;
	}

	public final void setItsCont(ITSContentBean itsCont) {
		this.itsCont = itsCont;
	}

}
