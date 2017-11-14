/*===========================================================================
  Copyright (C) 2009-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Stack;
import java.util.regex.Pattern;

import net.sf.okapi.common.exceptions.OkapiIOException;

/**
 * Helper class to write XML documents.
 * <p><b> IMPORTANT: flush() or close() must always be called to assure all content is flushed to 
 * the underlying streams.</b></p>
 * <p><b>If a {@link StringWriter} is used the flush() or close() must be be called before calling the 
 * toString() method </b></p>
 */
public class XMLWriter {

	private static final String ERRMSG = "XMLWriter output error.";
	private static int defaultCharBufferSize = 16384;

	/**
	 * Matches \r\n or \n or \r.
	 */
	private static final Pattern LINE_BRAKE_PATTERN = Pattern.compile("\r?\n|\r");

	private BufferedWriter writer; // Need to be able to nullify it on close and close() can be idempotent.
    private boolean inStartTag;
    private Stack<String> elements = new Stack<String>();
    private String lineBreak = System.lineSeparator();
    
    /**
     * Creates a new XML document on disk.
     * @param path the full path of the document to create. If any directory in the
     * path does not exists yet it will be created automatically. The document is
     * always written in UTF-8 and the type of line-breaks is the one of the
     * platform where the application runs.
     */
    public XMLWriter (String path) {
    	try {
    		Util.createDirectories(path);
    		final OutputStreamWriter osw = new OutputStreamWriter(new BufferedOutputStream(
    				new FileOutputStream(path)), StandardCharsets.UTF_8);
    		writer = new BufferedWriter(osw, defaultCharBufferSize);
    	}
    	catch ( IOException e ) {
    		throw new OkapiIOException(ERRMSG, e);
    	}
    }

    /**
     * Creates a new XML document for a given writer object.
     * @param writer the writer to use to output the document. If this writer outputs to
     * bytes it must be set to output in UTF-8.
     */
    public XMLWriter (Writer writer) {
    	this.writer = new BufferedWriter(writer, defaultCharBufferSize);
    }
    
    protected void finalize()
    	throws Throwable
    {
    	try {
    		close();
    	}
    	finally {
    		super.finalize();
    	}
    }
    
    /**
     * Sets the type of line-break to use when writing out the
     * document. By default, the type is the one of the platform.
     * This method can be used for example, to force the line-breaks
     * to be "\n" instead of "\r\n" when writing out on a string that
     * will be later written out on a file where "\n" will be converted
     * to "\r\n" (therefore avoiding to end up with "\r\r\n").
     * @param lineBreak the new type of line-break to use.
     */
    public void setLineBreak (String lineBreak) {
    	this.lineBreak = lineBreak;
    }
    
    /**
     * Gets the line break string currently used for this writer.
     * @return the line break string used.
     */
    public String getLineBreak () {
    	return lineBreak;
    }

    /**
     * Closes the writer and release any associated resources.
     */
    public void close () {
    	if ( writer != null ) {
    		try {
				writer.flush();
				writer.close();
				writer = null;
			}
    		catch ( IOException e ) {
				throw new OkapiIOException(ERRMSG, e);
			}    		
    	}
    	if ( elements != null ) {
    		elements.clear();
    		elements = null;
    	}
    }

    /**
     * Writes the start of the document. This method generates the XML declaration.
     */
    public void writeStartDocument () {    	
		write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+lineBreak);
    }

    /**
     * Writes the start of a XHTML document, including the head and start of body element.
     * @param title the title of the document (can be null).
     */
    public void writeStartHTMLDocument (String title) {
			write("<html>"+lineBreak);
			
	    	write("<head>"+lineBreak);
	    	write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">"+lineBreak);
	    	if ( title != null ) {
	    		writeElementString("title", title);
	    		writeLineBreak();
	    	}
	    	write("</head>"+lineBreak);
    }

    /**
     * Writes the end of the document. This method closes any open tag, and
     * flush the writer.
     */
    public void writeEndDocument () {
    	closeStartTag();
    	try {
			writer.flush();
		} catch (IOException e) {
			throw new OkapiIOException(ERRMSG, e);
		}
    }

    /**
     * Writes the start of an element.
     * @param name the name of the element to start.
     */
    public void writeStartElement (String name) {
    	closeStartTag();
    	elements.push(name);
    	write("<" + name);
    	inStartTag = true;
    }

    /**
     * Writes the end of the last started element.
     */
    public void writeEndElement () {
    	closeStartTag();
    	write("</" + elements.pop() + ">");
    }

    /**
     * Writes the end of the last started element and writes a line-break.
     */
    public void writeEndElementLineBreak () {
    	closeStartTag();
   		write("</" + elements.pop() + ">"+lineBreak);
    }

    /**
     * Writes an element and its content.
     * @param name the name of the element to write.
     * @param content the content to enclose inside this element.
     */
    public void writeElementString (String name,
   		String content)
    {
    	closeStartTag();
    	write("<" + name + ">");
    	write(Util.escapeToXML(content, 0, false, null));
    	write("</" + name + ">");
    }

    /**
     * Writes an attribute and its associated value. You must use
     * {@link #writeStartElement(String)} just before.
     * @param name the name of the attribute.
     * @param value the value of the attribute.
     */
    public void writeAttributeString (String name,
   		String value)
    {
    	write(" " + name + "=\"" + Util.escapeToXML(value, 3, false, null) + "\"");
    }

    /**
     * Writes a string. The text is automatically escaped.
     * @param text the text to output.
     */
    public void writeString (String text) {
    	closeStartTag();
    	appendRawXML(Util.escapeToXML(text, 0, false, null));
    }

    /**
     * Writes a chunk of raw XML (and line-breaks are normalized to platform specific ones).
     * If a tag is open, it is closed automatically before the data are written.
     * Use {@link #appendRawXML(String)} to output without preliminary closing.
     * @param xmlData the data to output. No escaping is performed, but the line-breaks are
     * converted to the line-break type of the output.
     */
    public void writeRawXML (String xmlData) {
    	closeStartTag();
    	appendRawXML(xmlData);
    }
    
    /**
     * Writes a chunk of raw XML (and line-breaks are normalized to platform specific ones).
     * If a tag is open it is not closed (so this allows you to output raw attributes).
     * @param xmlData the data to output. No escaping is performed, but the line-breaks are
     * converted to the line-break type of the output.
     */
    public void appendRawXML (String xmlData) {
    	write(LINE_BRAKE_PATTERN.matcher(xmlData).replaceAll(lineBreak));
    }

    /**
     * Writes a comment.
     * @param text the text of the comment.
     * @param withLineBreak add a line break at the end of the comment.
     */
    public void writeComment (String text,
    	boolean withLineBreak)
    {
    	closeStartTag();
    	write("<!--");
    	appendRawXML(text);
    	write("-->");
    	if ( withLineBreak ) write(lineBreak);
    }

    /**
     * Writes a line-break, and if the writer is in a start tag, close it before.
     */
    public void writeLineBreak () {
    	closeStartTag();
    	write(lineBreak);
    }

    /**
     * Closes the tag of the last start tag output, if needed.
     */
    private void closeStartTag () {
    	if ( inStartTag ) {
    		write(">");
    		inStartTag = false;
    	}
    }
    
    /**
     * Flushes the current buffer to the device.
     */
    public void flush () {
    	try {
			writer.flush();
		} catch (IOException e) {
			throw new OkapiIOException(ERRMSG, e);
		}
    }
    
    private void write(String string) {
    	try {
			writer.write(string);
		} catch (IOException e) {
			throw new OkapiIOException(ERRMSG, e);
		}
    }
}
