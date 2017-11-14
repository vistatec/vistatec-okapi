package net.sf.okapi.lib.beans.v2;

import java.nio.charset.Charset;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class CharsetBean extends PersistenceBean<Charset> {

	private String className; // We need it as Charset is abstract
	private String name;
	private String[] aliases;
	
	@Override
	protected Charset createObject(IPersistenceSession session) {
		Object res = null;
		try {
			res = ClassUtil.instantiateClass(className, name, aliases);
		} catch (Exception e) {
			res = null; // At least we tried
		}
		return (Charset)res;
	}

	@Override
	protected void setObject(Charset obj, IPersistenceSession session) {
	}

	@Override
	protected void fromObject(Charset obj, IPersistenceSession session) {
		className = ClassUtil.getQualifiedClassName(obj);
		name = obj.name();
		aliases = obj.aliases().toArray(new String[0]);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getAliases() {
		return aliases;
	}

	public void setAliases(String[] aliases) {
		this.aliases = aliases;
	}

}
