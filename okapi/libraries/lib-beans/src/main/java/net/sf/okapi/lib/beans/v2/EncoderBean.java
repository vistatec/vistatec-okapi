package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.encoder.IEncoder;
import net.sf.okapi.lib.beans.v1.ParametersBean;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.beans.TypeInfoBean;

public class EncoderBean extends TypeInfoBean {

	private String lineBreak;
	private String encoding;
	private ParametersBean params = new ParametersBean(); 
	
	@Override
	protected IEncoder createObject(IPersistenceSession session) {
		return (IEncoder) super.createObject(session);
	}

	@Override
	protected void setObject(Object obj, IPersistenceSession session) {
		super.setObject(obj, session);
		if (obj instanceof IEncoder) {
			((IEncoder) obj).setOptions(params.get(IParameters.class, session), 
					encoding, lineBreak);
		}
	}

	@Override
	protected void fromObject(Object o, IPersistenceSession session) {
		super.fromObject(o, session);
		if (o instanceof IEncoder) {
			IEncoder obj = (IEncoder) o; 
			lineBreak = obj.getLineBreak();
			encoding = obj.getEncoding();
			params.set(obj.getParameters(), session);
		}
	}

	public final String getLineBreak() {
		return lineBreak;
	}

	public final void setLineBreak(String lineBreak) {
		this.lineBreak = lineBreak;
	}

	public final String getEncoding() {
		return encoding;
	}

	public final void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public final ParametersBean getParams() {
		return params;
	}

	public final void setParams(ParametersBean params) {
		this.params = params;
	}

}
