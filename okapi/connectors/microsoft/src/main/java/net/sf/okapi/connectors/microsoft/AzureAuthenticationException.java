package net.sf.okapi.connectors.microsoft;

import net.sf.okapi.common.exceptions.OkapiException;

public class AzureAuthenticationException extends OkapiException {
	private static final long serialVersionUID = 1L;

	public AzureAuthenticationException(String msg) {
		super(msg);
	}
	public AzureAuthenticationException(Throwable t) {
		super(t);
	}
	public AzureAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}
}
