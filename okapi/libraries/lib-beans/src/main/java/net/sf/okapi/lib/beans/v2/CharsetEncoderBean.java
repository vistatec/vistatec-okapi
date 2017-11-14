package net.sf.okapi.lib.beans.v2;

import java.nio.charset.CharsetEncoder;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class CharsetEncoderBean extends PersistenceBean<CharsetEncoder> {

	private String className; // We need it as CharsetEncoder is abstract
	private CharsetBean charset = new CharsetBean();
    private float averageBytesPerChar;
    private float maxBytesPerChar;
    private byte[] replacement;
	
	@Override
	protected CharsetEncoder createObject(IPersistenceSession session) {
		Object res = null;
		try {
			res = ClassUtil.instantiateClass(className, charset, 
					averageBytesPerChar, maxBytesPerChar, replacement);
		} catch (Exception e) {
			res = null; // At least we tried
		}
		return (CharsetEncoder)res;
	}

	@Override
	protected void setObject(CharsetEncoder obj, IPersistenceSession session) {
	}

	@Override
	protected void fromObject(CharsetEncoder obj, IPersistenceSession session) {
		className = ClassUtil.getQualifiedClassName(obj);
		charset.set(obj.charset(), session);
		averageBytesPerChar = obj.averageBytesPerChar();
		maxBytesPerChar = obj.maxBytesPerChar();
		replacement = obj.replacement();
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public CharsetBean getCharset() {
		return charset;
	}

	public void setCharset(CharsetBean charset) {
		this.charset = charset;
	}

	public float getAverageBytesPerChar() {
		return averageBytesPerChar;
	}

	public void setAverageBytesPerChar(float averageBytesPerChar) {
		this.averageBytesPerChar = averageBytesPerChar;
	}

	public float getMaxBytesPerChar() {
		return maxBytesPerChar;
	}

	public void setMaxBytesPerChar(float maxBytesPerChar) {
		this.maxBytesPerChar = maxBytesPerChar;
	}

	public byte[] getReplacement() {
		return replacement;
	}

	public void setReplacement(byte[] replacement) {
		this.replacement = replacement;
	}

}
