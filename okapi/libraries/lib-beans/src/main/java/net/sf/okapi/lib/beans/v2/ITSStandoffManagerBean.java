package net.sf.okapi.lib.beans.v2;

import net.sf.okapi.filters.xliff.its.IITSDataStore;
import net.sf.okapi.filters.xliff.its.ITSStandoffManager;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class ITSStandoffManagerBean extends PersistenceBean<ITSStandoffManager> {

	private IITSDataStoreBean dataStore = new IITSDataStoreBean();
	
	@Override
	protected ITSStandoffManager createObject(IPersistenceSession session) {
		return new ITSStandoffManager(dataStore.get(IITSDataStore.class, session));
	}

	@Override
	protected void setObject(ITSStandoffManager obj, IPersistenceSession session) {
	}

	@Override
	protected void fromObject(ITSStandoffManager obj,
			IPersistenceSession session) {
		dataStore.set(obj.getDataStore(), session);
	}

	public final IITSDataStoreBean getDataStore() {
		return dataStore;
	}

	public final void setDataStore(IITSDataStoreBean dataStore) {
		this.dataStore = dataStore;
	}
}
