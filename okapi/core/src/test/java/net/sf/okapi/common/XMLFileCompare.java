package net.sf.okapi.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XMLFileCompare {
	private final static Logger LOGGER = LoggerFactory.getLogger(XMLFileCompare.class);
	
	/**
	 * Formats a given XML string.
	 * @see http://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
	 * @param xml the given XML string.
	 * @return pretty-printed given XML string.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ClassCastException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static String formatXML(String xml) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
        InputSource src = new InputSource(new StringReader(xml));
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        
        // ignore any DTD's
        builder.setEntityResolver(new EntityResolver() {
            @Override
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                if (systemId.endsWith(".dtd")) {
                    return new InputSource(new StringReader(""));
                } else {
                    return null;
                }
            }
        });

        Node document = builder.parse(src).getDocumentElement();
                
        Boolean keepDeclaration = Boolean.valueOf(xml.startsWith("<?xml"));
        
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
        LSSerializer writer = impl.createLSSerializer();

        writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
        writer.getDomConfig().setParameter("xml-declaration", keepDeclaration);

        return writer.writeToString(document);
    }
	
	public boolean compareFilesPerLines(String out, String gold) throws FileNotFoundException{
		String outXML = null;
		String goldXML = null;
		try {
			outXML = TestUtil.getFileAsString(new File(out));
		} catch (Exception e) {
			LOGGER.trace("Error opening/reading file:\n" + out, e);
			return false;
		}		
		
		try {
			goldXML = TestUtil.getFileAsString(new File(gold));
		} catch (Exception e) {
			LOGGER.trace("Error opening/reading file:\n" + gold, e);
			return false;
		}
		
		try {
			outXML = formatXML(outXML);
		} catch (Exception e) {
			LOGGER.trace("Error formatting XML:\n" + out, e);
			return false;
		}
		
		try {
			goldXML = formatXML(goldXML);
		} catch (Exception e) {
			LOGGER.trace("Error formatting XML:\n + gold", e);
			return false;
		}
		
		File tempFileOut = FileUtil.createTempFile("~okapi-xliffFileCompare-out" + Util.getFilename(out, true) + "_");
		File tempFileGold = FileUtil.createTempFile("~okapi-xliffFileCompare-gold" + Util.getFilename(gold, true) + "_");
		
		String tempOut = tempFileOut.getAbsolutePath();
		String tempGold = tempFileGold.getAbsolutePath();
		
		try {
			TestUtil.writeString(outXML, tempOut, "UTF-8");
			TestUtil.writeString(goldXML, tempGold, "UTF-8");
		} catch (IOException e) {
			LOGGER.trace("Error writing files\n");
			LOGGER.trace(Util.getFilename(out, true), e);
			LOGGER.trace(Util.getFilename(gold, true), e);
			return false;
		}		
		
		FileCompare fc = new FileCompare();
		boolean c = fc.compareFilesPerLines(tempOut, tempGold, "UTF-8");
		
		tempFileGold.delete();
		tempFileOut.delete();		
		return c;
	}
}
