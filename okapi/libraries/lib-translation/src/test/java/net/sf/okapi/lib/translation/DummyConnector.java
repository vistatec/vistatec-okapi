package net.sf.okapi.lib.translation;

import net.sf.okapi.common.resource.TextFragment;

public class DummyConnector extends BaseConnector {

	@Override
	public void close () {
	}

	@Override
	public String getName () {
		return "DummyConnector";
	}

	@Override
	public String getSettingsDisplay () {
		return "Dummy settings";
	}

	@Override
	public void open () {
	}

	@Override
	public int query (String plainText) {
		return 0;
	}

	@Override
	public int query (TextFragment text) {
		return 0;
	}

}
