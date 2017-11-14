package net.sf.okapi.filters.yaml.parser;

import net.sf.okapi.common.StringUtil;

public class Key {
	// key types also are also scalars
	public YamlScalarTypes type = YamlScalarTypes.UNKOWN;
	public YamlNodeTypes nodeType = YamlNodeTypes.UNKOWN;
	public int indent = -1;

	// full key token with quotes and final key marker (":")
	public String key = "";
	public boolean flow = false;
		
	public String getKeyName() {
		if (key.isEmpty()) {
			return key;
		}
		
		// remove possible newline
		String k = StringUtil.chomp(key);
		
		// remove mapping separator and whitespace
		// at end of key
		k = k.replaceFirst("\\s*:\\s*$", "");
		
		// return key name without quotes or marker
		k = StringUtil.removeQualifiers(k, type.getQuoteChar());
		return k;
	}
	
	public boolean isEmpty() {
		return key.isEmpty();
	}

	@Override
	public String toString() {
		return "key=" + key;
	}
}
