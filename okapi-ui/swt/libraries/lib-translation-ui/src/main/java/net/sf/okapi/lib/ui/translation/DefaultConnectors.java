package net.sf.okapi.lib.ui.translation;

import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.ClassUtil;
import net.sf.okapi.common.plugins.PluginItem;
import net.sf.okapi.common.plugins.PluginsManager;
import net.sf.okapi.common.query.IQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultConnectors implements IConnectorList {

	private ArrayList<ConnectorInfo> list;
	
	@Deprecated 
	public DefaultConnectors () {
		list = new ArrayList<ConnectorInfo>();
		setDefaultMapping();
	}
	
	// Temporary hard-coded mapping
	private void setDefaultMapping () {
		ConnectorInfo trc = new ConnectorInfo();
		trc.description = "Pensieve TM Engine";
		trc.connectorClass = "net.sf.okapi.connectors.pensieve.PensieveTMConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.pensieve.Parameters";
		list.add(trc);
		
		trc = new ConnectorInfo();
		trc.description = "SimpleTM TM Engine";
		trc.connectorClass = "net.sf.okapi.connectors.simpletm.SimpleTMConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.simpletm.ParametersUI";
		list.add(trc);
		
		trc = new ConnectorInfo();
		trc.description = "GlobalSight TM Web Services";
		trc.connectorClass = "net.sf.okapi.connectors.globalsight.GlobalSightTMConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.globalsight.Parameters";
		list.add(trc);

		trc = new ConnectorInfo();
		trc.description = "Google Translate Services (v2, paid service)";
		trc.connectorClass = "net.sf.okapi.connectors.google.GoogleMTv2Connector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.google.GoogleMTv2Parameters";
		list.add(trc);
		
		trc = new ConnectorInfo();
		trc.description = "MyMemory TM Web Services";
		trc.connectorClass = "net.sf.okapi.connectors.mymemory.MyMemoryTMConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.mymemory.Parameters";
		list.add(trc);

		trc = new ConnectorInfo();
		trc.description = "Translate Toolkit TM Server";
		trc.connectorClass = "net.sf.okapi.connectors.translatetoolkit.TranslateToolkitTMConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.translatetoolkit.Parameters";
		list.add(trc);

		trc = new ConnectorInfo();
		trc.description = "Apertium MT Web Server";
		trc.connectorClass = "net.sf.okapi.connectors.apertium.ApertiumMTConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.apertium.Parameters";
		list.add(trc);

		trc = new ConnectorInfo();
		trc.description = "Microsoft Translator Services";
		trc.connectorClass = "net.sf.okapi.connectors.microsoft.MicrosoftMTConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.microsoft.Parameters";
		list.add(trc);

		trc = new ConnectorInfo();
		trc.description = "KantanMT";
		trc.connectorClass = "net.sf.okapi.connectors.kantan.KantanMTConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.kantan.KantanMTConnectorParameters";
		list.add(trc);

//Not ready yet
//		trc = new ConnectorInfo();
//		trc.description = "ProMT MT Server (Beta connector)";
//		trc.connectorClass = "net.sf.okapi.connectors.promt.ProMTConnector";
//		trc.descriptionProviderClass = "net.sf.okapi.connectors.promt.Parameters";
//		list.add(trc);
//

// Not ready		
//		trc = new ConnectorInfo();
//		trc.description = "CrossLanguage MT Server (Beta connector)";
//		trc.connectorClass = "net.sf.okapi.connectors.crosslanguage.CrossLanguageMTConnector";
//		trc.descriptionProviderClass = "net.sf.okapi.connectors.crosslanguage.Parameters";
//		list.add(trc);

//Implement concordance search only for now
//		trc = new ConnectorInfo();
//		trc.description = "TAUS Data Association Search Repository (Beta connector)";
//		trc.connectorClass = "net.sf.okapi.connectors.tda.TDASearchConnector";
//		trc.descriptionProviderClass = "net.sf.okapi.connectors.tda.Parameters";
//		list.add(trc);
		
		trc = new ConnectorInfo();
		trc.description = "Bilingual File";
		trc.connectorClass = "net.sf.okapi.connectors.bifile.BilingualFileConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.bifile.Parameters";
		list.add(trc);

		trc = new ConnectorInfo();
		trc.description = "Lingo24 Premium MT API";
		trc.connectorClass = "net.sf.okapi.connectors.lingo24.Lingo24Connector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.lingo24.Parameters";
		list.add(trc);

		trc = new ConnectorInfo();
		trc.description = "ModernMT API";
		trc.connectorClass = "net.sf.okapi.connectors.mmt.MMTConnector";
		trc.descriptionProviderClass = "net.sf.okapi.connectors.mmt.Parameters";
		list.add(trc);
	}

	public List<ConnectorInfo> getList () {
		return list;
	}

	private static IConnectorList connectors;
	static {
		connectors = new DefaultConnectors();
	}
	
	public static void setConnectors(IConnectorList connectors) {
		DefaultConnectors.connectors = connectors;
	}

	public static IConnectorList getConnectors() {
		return connectors;
	}
	
	private static String getName(String connectorClass, ClassLoader classLoader) {
		// Instantiate the connector to get the description
		IQuery connector = null;
		try {
			connector = (IQuery) ClassUtil.instantiateClass(connectorClass, classLoader);
		} catch (Exception e) {
			Logger localLogger = LoggerFactory.getLogger(DefaultConnectors.class);
			localLogger.warn(String.format("Cannot instantiate the connector '%s'.", connectorClass));
			return "Unknown plug-in connector";
		}
		
		return connector.getName();		
	}
	
	public static void addFromPlugins (PluginsManager pm) {
		List<PluginItem> list = pm.getList();
		
		for ( PluginItem item : list ) {
			if ( item.getType() == PluginItem.TYPE_IQUERY ) {
				ConnectorInfo trc = new ConnectorInfo();
				trc.description = getName(item.getClassName(), pm.getClassLoader());
				trc.connectorClass = item.getClassName();
				trc.descriptionProviderClass = item.getEditorDescriptionProvider() == null ? null : 
					item.getEditorDescriptionProvider().name;
				trc.classLoader = pm.getClassLoader();
				connectors.getList().add(trc);
			}
		}
	}
}
