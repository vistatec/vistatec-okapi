package net.sf.okapi.lib.tkit.step;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import net.sf.okapi.common.StringParameters;
import net.sf.okapi.common.Util;

public class MultipleVersionsParameters extends StringParameters {
	private static final String VERSIONS_CONFIG_URI = "versions_config_uri"; //$NON-NLS-1$
	private static final String OKAPI_JAR_VERSION = "okapi_jar_version"; //$NON-NLS-1$
	private static final String OKAPI_JAR_URI = "okapi_jar_uri"; //$NON-NLS-1$
	public static final String DEFAULT_VERSIONS_ROOT = ".okapi_versions"; //$NON-NLS-1$
	public static final String DEFAULT_VERSION = "m24"; //$NON-NLS-1$
	public static final String DEFAULT_VERSION_CONFIG_NAME = "versions.json"; //$NON-NLS-1$

	public MultipleVersionsParameters () {
		super();
	}
	
	@Override
	public void reset () {
		super.reset();
		URI u = Util.toURI(System.getProperty("user.home")+
				File.pathSeparator+
				DEFAULT_VERSIONS_ROOT+
				File.pathSeparator+
				DEFAULT_VERSION_CONFIG_NAME);
		setVersionsConfigUri(u);
		setOkapiJarUri(null);
		setOkapiJarVersion(DEFAULT_VERSION);
	}

	public URI getVersionsConfigUri() throws URISyntaxException {
		return new URI(getString(VERSIONS_CONFIG_URI));
	}

	public void setVersionsConfigUri(URI versionsConfigUri) {
		String s = "";
		if (versionsConfigUri != null) {
			s = versionsConfigUri.toString();
		}
		setString(VERSIONS_CONFIG_URI, s);
	}
	
	public String getOkapiJarVersion() {
		return getString(OKAPI_JAR_VERSION);
	}

	public void setOkapiJarVersion(String okapiJarVersion) {
		setString(OKAPI_JAR_VERSION, okapiJarVersion);
	}
	
	public URI getOkapiJarUri() throws URISyntaxException {
		return new URI(getString(OKAPI_JAR_URI));
	}

	public void setOkapiJarUri(String okapiJarUri) {
		String s = "";
		if (okapiJarUri != null) {
			s = okapiJarUri.toString();
		}
		setString(OKAPI_JAR_URI, s);
	}
}
