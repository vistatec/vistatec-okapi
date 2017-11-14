package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.filters.its.Parameters;
import net.sf.okapi.lib.beans.v1.ParametersBean;
import net.sf.okapi.lib.persistence.IPersistenceSession;

public class ITSParametersBean extends ParametersBean {

	private boolean quoteModeDefined;
	private int quoteMode;
	
	@Override
	protected void setObject(IParameters o, IPersistenceSession session) {
		super.setObject(o, session);
		
		if (o instanceof Parameters) {
			Parameters obj = (Parameters) o;
			obj.quoteModeDefined = quoteModeDefined;
			obj.quoteMode = quoteMode;
		}
	}
	
	@Override
	protected void fromObject(IParameters o, IPersistenceSession session) {
		super.fromObject(o, session);
		
		if (o instanceof Parameters) {
			Parameters obj = (Parameters) o;
			quoteModeDefined = obj.quoteModeDefined;
			quoteMode = obj.quoteMode;
		}
	}

	public final boolean isQuoteModeDefined() {
		return quoteModeDefined;
	}

	public final void setQuoteModeDefined(boolean quoteModeDefined) {
		this.quoteModeDefined = quoteModeDefined;
	}

	public final int getQuoteMode() {
		return quoteMode;
	}

	public final void setQuoteMode(int quoteMode) {
		this.quoteMode = quoteMode;
	}
}
