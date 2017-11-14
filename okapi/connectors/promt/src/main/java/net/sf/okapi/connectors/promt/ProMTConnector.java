/*===========================================================================
  Copyright (C) 2009-2010 by the Okapi Framework contributors
-----------------------------------------------------------------------------
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
===========================================================================*/

package net.sf.okapi.connectors.promt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.okapi.common.DefaultEntityResolver;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.query.IQuery;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.QueryUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ProMTConnector extends BaseConnector implements IQuery {

	private static final String PTS8_SERVICE = "pts8/services/ptservice.asmx/";
//	private static final String TRANSLATETEXT = "TranslateText";
	private static final String GETPTSERVICEDATASET = "GetPTServiceDataSet";

	private static final String PTSXLIFF_SERVICE = "ptsxliff/PTSXLIFFTranslator.asmx/";
	private static final String TRANSLATEFORMATTEDTEXT = "TranslateFormattedText";

	private static final Pattern RESULTPATTERN = Pattern.compile("<string(.*?)>(.*?)</string>");

	private Locale srcJavaLoc;
	private Locale trgJavaLoc;
	private QueryResult result;
	private int current = -1;
	private Parameters params;
	private URL serviceURL;
	private String dirId;
	private HashMap<String, String> dirIdentifiers;
	private QueryUtil qutil;
	private DocumentBuilder docBuilder;

	public ProMTConnector () {
		params = new Parameters();
		qutil = new QueryUtil();
		DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
		Fact.setValidating(false);
		try {
			docBuilder = Fact.newDocumentBuilder();
		}
		catch ( ParserConfigurationException e ) {
			throw new OkapiException("Error creating document builder.", e);
		}
	}

	@Override
	public String getName () {
		return "ProMT";
	}

	@Override
	public String getSettingsDisplay () {
		return String.format("Server: %s", params.getHost());
	}
	
	@Override
	public void close () {
		// Nothing to do
	}

	@Override
	public boolean hasNext () {
		return (current>-1);
	}
	
	@Override
	public QueryResult next() {
		if ( current > -1 ) { // Only one result
			current = -1;
			return result;
		}
		return null;
	}

	private String getHost () {
		String tmp;
		if ( !Util.isEmpty(params.getUsername()) ) {
			//tmp = "http://"+params.getUsername()+":"+params.getPassword()+"@"+params.getHost();
			tmp = "http://"+params.getHost();
		}
		else {
			tmp = "http://"+params.getHost();
		}
		// Make sure the host ends with a separator
		if ( !tmp.endsWith("/") && tmp.endsWith("\\") ) {
			return tmp + "/";
		}
		return tmp;
	}
	
	@Override
	public void open () {
		// Try to authenticate if needed
		if ( !Util.isEmpty(params.getUsername()) ) {
			Authenticator.setDefault(new Authenticator() {
			    protected PasswordAuthentication getPasswordAuthentication() {
			        return new PasswordAuthentication(params.getUsername(), params.getPassword().toCharArray());
			    }
			});
		}
		
		initializePairsFromServer();
		// Set the full URL for the service
		try {
//			serviceURL = new URL(getHost()+PTS8_SERVICE+TRANSLATETEXT);
			serviceURL = new URL(getHost()+PTSXLIFF_SERVICE+TRANSLATEFORMATTEDTEXT);
		}
		catch ( MalformedURLException e ) {
//			throw new OkapiException(String.format("Cannot open the connection to '%s'", getHost()+PTS8_SERVICE), e); 
			throw new OkapiException(String.format("Cannot open the connection to '%s'", getHost()+PTSXLIFF_SERVICE), e); 
		}
	}

	@Override
	public int query (String text) {
		if ( Util.isEmpty(text) ) return 0;
		return queryUsingPOST(null, text);
	}

	@Override
	public int query (TextFragment frag) {
		if ( !frag.hasText(false) ) return 0;
		return queryUsingPOST(frag, null);
	}
	
	@Override
	public List<List<QueryResult>> batchQuery (List<TextFragment> fragments) {
		throw new OkapiNotImplementedException();
	}
	
	// Either frag or plainText must be null
	private int queryUsingPOST (TextFragment frag,
		String plainText)
	{
		current = -1;
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		
		String text;
		if ( frag != null ) {
			//text = qutil.separateCodesFromText(frag);
			text = qutil.toXLIFF(frag);
		}
		else {
			text = plainText;
		}

		if ( dirId == null ) return 0;
		try {
			// Open a connection
			URLConnection conn = serviceURL.openConnection();
			
			// Set the data
			//DirId=string&TplId=string&Text=string
//			String data = String.format("DirId=%s&TplId=%s&Text=%s",
//				dirId, "General", URLEncoder.encode(text, "UTF-8"));

			// DirId=string&TplId=string&strText=string&FileType=string
			String data = String.format("DirId=%s&TplId=%s&strText=%s&FileType=text/xliff",
				dirId, "General", URLEncoder.encode(text, "UTF-8"));

			// Post the data
			conn.setDoOutput(true);
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(data);
			wr.flush();
	        
	        // Get the response
	        rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
	        String buffer;
	        StringBuilder tmp = new StringBuilder();
	        while (( buffer = rd.readLine() ) != null ) {
	            tmp.append(buffer);
	        }
	        
	        // Treat the output 
	        Matcher m = RESULTPATTERN.matcher(tmp.toString());
	        if ( m.find() ) {
	        	buffer = m.group(2);
	        	if ( !Util.isEmpty(buffer) ) {
	        		result = new QueryResult();
	        		result.weight = getWeight();

	        		if ( frag != null ) {
	        			result.source = frag;
	        			result.target = qutil.fromXLIFF(prepareXLIFF(buffer), frag);
	        		}
	        		else { // Was plain text
	        			result.source = new TextFragment(text);
	        			result.target = qutil.fromXLIFF(prepareXLIFF(buffer), frag);
	        		}
	        		
	        		result.setFuzzyScore(95); // Arbitrary score for MT
	        		result.origin = getName();
	        		result.matchType = MatchType.MT;
	    			current = 0;
	        	}
	        }
		}
		catch ( MalformedURLException e ) {
			throw new OkapiException("Error during the query.", e);
		}
		catch ( IOException e ) {
			throw new OkapiException("Error during the query.", e);
		}
		finally {
        	try {
        		if ( wr != null ) wr.close();
    	        if ( rd != null ) rd.close();
   	        }
       		catch ( IOException e ) {
       			// Ignore this exception
	        }
		}
		return current+1;
	}
	
	private Element prepareXLIFF (String data) {
		try {
			// Un-escape first layer
			data = data.replace("&apos;", "'");
			data = data.replace("&lt;", "<");
			data = data.replace("&gt;", ">");
			data = data.replace("&quot;", "\"");
			data = data.replace("&amp;", "&");
			Document doc = docBuilder.parse(new InputSource(new StringReader("<s>"+data+"</s>")));
			return doc.getDocumentElement();
		}
		catch ( SAXException e ) {
			throw new OkapiException("Error when parsing result.", e);
		}
		catch ( IOException e ) {
			throw new OkapiException("Error when parsing result.", e);
		}
	}
	
	@Override
	public void removeAttribute (String name) {
		//TODO: use domain
	}

	@Override
	public void clearAttributes () {
		//TODO: use domain
	}

	@Override
	public void setAttribute (String name,
		String value)
	{
		//TODO: use domain
	}

	/**
	 * Sets the dirId value if possible. This will not be set if
	 * either the lookup table is not created or the locales are not set,
	 * allowing to call setLanguages() or open() in any order.
	 */
	private void setDirectionId () {
		// Initialize to 'no support'
		dirId = null;
		
		// If the lookup table is not yet initialized it's ok
		// It means the direction id will be set on open()
		if ( dirIdentifiers == null ) return;
		
		// Create the name to lookup
		if (( srcJavaLoc != null ) && ( trgJavaLoc != null )) {
			// getDisplayLanguage will not return null
			String pair = srcJavaLoc.getDisplayLanguage(Locale.ENGLISH)
				+ "-" + trgJavaLoc.getDisplayLanguage(Locale.ENGLISH);
			// Get the dirId (or null if not found)
			dirId = dirIdentifiers.get(pair);
		}
	}
	
	@Override
	public void setLanguages (LocaleId sourceLocale,
		LocaleId targetLocale)
	{
		super.setLanguages(sourceLocale, targetLocale);
		// Convert the codes
		srcJavaLoc = sourceLocale.toJavaLocale();
		trgJavaLoc = targetLocale.toJavaLocale();
		// Try to set the direction
		setDirectionId();
	}
		
	@Override
	protected String toInternalCode (LocaleId locale) {
		// Reduce the locale code to its base language part
		return locale.getLanguage();
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		params = (Parameters)params;
	}

//	private void initializePairs () {
//		dirId = null;
//		dirIdentifiers = new HashMap<String, String>();
//		dirIdentifiers.put("en_ru", "131073");
//		dirIdentifiers.put("ru_en", "65538");
//		dirIdentifiers.put("de_ru", "131076");
//		dirIdentifiers.put("ru_de", "262146");
//		dirIdentifiers.put("fr_ru", "131080");
//		dirIdentifiers.put("ru_fr", "524290");
//		dirIdentifiers.put("es_ru", "131104");
//		dirIdentifiers.put("ru_es", "2097154");
//		dirIdentifiers.put("en_de", "262145");
//		dirIdentifiers.put("de_en", "65540");
//		dirIdentifiers.put("en_fr", "524289");
//		dirIdentifiers.put("fr_en", "65544");
//		dirIdentifiers.put("de_fr", "524292");
//		dirIdentifiers.put("fr_de", "262152");
//		dirIdentifiers.put("en_it", "1048577");
//		dirIdentifiers.put("it_en", "65552");
//		dirIdentifiers.put("en_es", "2097153");
//		dirIdentifiers.put("es_en", "65568");
//		dirIdentifiers.put("de_es", "2097156");
//		dirIdentifiers.put("es_de", "262176");
//		dirIdentifiers.put("fr_es", "2097160");
//		dirIdentifiers.put("es_fr", "524320");
//		dirIdentifiers.put("en_pt", "4194305");
//		dirIdentifiers.put("pt_en", "65600");
//	}

	private void initializePairsFromServer () {
		dirId = null;
		dirIdentifiers = new HashMap<String, String>();
		OutputStreamWriter wr = null;
		BufferedReader rd = null;
		try {
			// Open a connection
			URL url = new URL(getHost()+PTS8_SERVICE+GETPTSERVICEDATASET);
			URLConnection conn = url.openConnection();
			
			// Post the data
			conn.setDoOutput(true);
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write("");
			wr.flush();
	        
	        // Get the response
	        rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), Charset.forName("UTF-8")));
	        String buffer;
	        StringBuilder tmp = new StringBuilder();
	        while (( buffer = rd.readLine() ) != null ) {
	            tmp.append(buffer);
	        }
	        
	        // Treat the result
			DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
			Fact.setValidating(false);
			Fact.setNamespaceAware(true);
			DocumentBuilder docBuilder = Fact.newDocumentBuilder();
			docBuilder.setEntityResolver(new DefaultEntityResolver());
			Document doc;
			doc = docBuilder.parse(new InputSource(new StringReader(tmp.toString())));
	        NodeList nodes = doc.getElementsByTagName("Directions");
	        for ( int i=0; i<nodes.getLength(); i++ ) {
	        	Element elem = (Element)nodes.item(i);
				NodeList dirs = elem.getChildNodes();
				String name = null;
				String id = null;
				// Gather the id and name
				for ( int j=0; j<dirs.getLength(); j++ ) {
					Node node = dirs.item(j);
					if ( "id".equals(node.getLocalName()) ) {
						id = Util.getTextContent(node);
					}
					else if ( "Name".equals(node.getLocalName()) ) {
						name = Util.getTextContent(node); 
					}
				}
				// Add the entry if we can
				if ( !Util.isEmpty(id) && !Util.isEmpty(name) ) {
					dirIdentifiers.put(name, id);
				}
	        }
		}
		catch ( MalformedURLException e ) {
			throw new OkapiException("Error during the initialization.", e);
		}
		catch ( IOException e ) {
			throw new OkapiException("Error during the initialization.", e);
		}
		catch ( ParserConfigurationException e ) {
			throw new OkapiException("Error during the initialization.", e);
		}
		catch ( SAXException e ) {
			throw new OkapiException("Error during the initialization.", e);
		}
		finally {
			setDirectionId();
        	try {
        		if ( wr != null ) wr.close();
    	        if ( rd != null ) rd.close();
   	        }
       		catch ( IOException e ) {
       			// Ignore this exception
	        }
		}
	}

	@Override
	public void setRootDirectory (String rootDir) {
		// Not used
	}
	
//	public static void main (String args[]) {
//		ProMTConnector con = new ProMTConnector();
//		con.setLanguages(LocaleId.fromString("en"), LocaleId.fromString("fr"));
//		con.open();
//		
//		TextFragment frag = new TextFragment("This is an <b>example</b>.");
//		frag.changeToCode(21, 25, TagType.CLOSING, "b");
//		frag.changeToCode(11, 14, TagType.OPENING, "b");
//		
////		TextFragment frag = new TextFragment("This is an example.");
//		
//		con.query(frag);
//		if ( con.hasNext() ) {
//			System.out.println(con.next().target.toString());
//		}
//		
//	}
}

