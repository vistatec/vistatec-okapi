/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.lib.verification;

import java.util.List;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.annotation.GenericAnnotation;
import net.sf.okapi.common.annotation.GenericAnnotationType;
import net.sf.okapi.common.annotation.GenericAnnotations;
import net.sf.okapi.common.annotation.IssueAnnotation;

public class QualityCheckReport {

	private XMLWriter xmlWriter;
	
	public static enum Type {
		HTML,
		XML
	}
	
	private Type type = Type.HTML;
	
	public void startReport (String fullPath,
		Type type)
	{
		this.type = type;
		closeWriter(); // just in case
		switch ( type ) {
		case HTML:
			startReportInHTML(fullPath);
			break;
		case XML:
			startReportInXML(fullPath);
			break;
		}
	}
	
	public void processAnnotations (GenericAnnotations annotations) {
		switch ( type ) {
		case HTML:
			reportInHTML(annotations);
			break;
		case XML:
			reportInXML(annotations);
			break;
		}
	}
	
	public void endReport () {
		switch ( type ) {
		case HTML:
			endReportInHTML();
			break;
		case XML:
			endReportInXML();
			break;
		}
	}
	
	public void closeWriter () {
		if ( xmlWriter != null ) {
			xmlWriter.close();
			xmlWriter = null;
		}
	}

	private void startReportInHTML (String fullPath) {
		// Create the output file
		xmlWriter = new XMLWriter(fullPath);
		// Writer the starting parts
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("html");
		xmlWriter.writeRawXML("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
			+ "<title>Quality Check Report</title><style type=\"text/css\">"
			+ "body { font-family: Verdana; font-size: smaller; }"
			+ "h1 { font-size: 110%; }"
			+ "h2 { font-size: 100%; }"
			+ "h3 { font-size: 100%; }"
			+ "p.s { font-family: Courier New, courier; font-size: 100%;"
			+ "   border: solid 1px; padding: 0.5em; margin-top:-0.7em; border-color: silver; background-color: #C0FFFF; }"
			+ "p.t { font-family: Courier New, courier; font-size: 100%; margin-top:-1.1em;"
			+ "   border: solid 1px; padding: 0.5em; border-color: silver; background-color: #C0FFC0; }"
			+ "span.hi { background-color: #FFFF00; }"
			+ "</style></head>");
		xmlWriter.writeStartElement("body");
		xmlWriter.writeLineBreak();
		xmlWriter.writeElementString("h1", "Quality Check Report");
	}
	
	private void reportInHTML (GenericAnnotations anns) {
		if ( anns == null ) return;
		List<GenericAnnotation> list = anns.getAnnotations(GenericAnnotationType.LQI);
		for ( GenericAnnotation ann : list ) {
			// Skip disabled issues
			if ( !ann.getBoolean(GenericAnnotationType.LQI_ENABLED) ) continue;
			// Get the IssueAnnotation object if possible
//			IssueAnnotation ia = null;
//			if ( ann instanceof IssueAnnotation ) {
//				ia = (IssueAnnotation)ann; 
//			}
			//TODO
		}
	}
	
	private void endReportInHTML () {
		// Write end of document
		xmlWriter.writeEndElementLineBreak(); // body
		xmlWriter.writeEndElementLineBreak(); // html
		xmlWriter.writeEndDocument();
		closeWriter();
	}
	
	private void startReportInXML (String fullPath) {
		// Create the output file
		xmlWriter = new XMLWriter(fullPath);
		xmlWriter.writeStartDocument();
		xmlWriter.writeStartElement("qualityCheckReport");
		xmlWriter.writeLineBreak();
		xmlWriter.writeStartElement("issues");
		xmlWriter.writeLineBreak();
	}
	
	private void reportInXML (GenericAnnotations anns) {
		if ( anns == null ) return;
		List<GenericAnnotation> list = anns.getAnnotations(GenericAnnotationType.LQI);
		for ( GenericAnnotation ann : list ) {
			// Skip disabled issues
			if ( !ann.getBoolean(GenericAnnotationType.LQI_ENABLED) ) continue;
			// Get the IssueAnnotation object if possible
			IssueAnnotation ia = null;
			if ( ann instanceof IssueAnnotation ) {
				ia = (IssueAnnotation)ann; 
			}
			xmlWriter.writeStartElement("issue"); xmlWriter.writeLineBreak();
			//TODO: xmlWriter.writeElementString("input", issue.getDocumentURI().getPath());
			//TODO: xmlWriter.writeElementString("tuName", issue.getTuName());
			//TODO: xmlWriter.writeElementString("tuId", issue.getTuId());
			xmlWriter.writeElementString("segId", ia==null ? "" : ia.getSegId());
			xmlWriter.writeElementString("severity", Util.formatDouble(ann.getDouble(GenericAnnotationType.LQI_SEVERITY)));
			xmlWriter.writeElementString("issueType", ia==null ? "" : ia.getIssueType().toString());
			xmlWriter.writeElementString("message", ann.getString(GenericAnnotationType.LQI_COMMENT));
			//TODO: xmlWriter.writeElementString("source", issue.getSource(), issue.getSourceStart(), issue.getSourceEnd());
			//TODO: xmlWriter.writeElementString("target", issue.getTarget(), issue.getTargetStart(), issue.getTargetEnd());

			xmlWriter.writeEndElementLineBreak(); // issue
		}
	}
	
	private void endReportInXML () {
		// Write end of document
		xmlWriter.writeEndElementLineBreak(); // issues
		xmlWriter.writeEndElementLineBreak(); // qualityCheckReport
		xmlWriter.writeEndDocument();
		closeWriter();
	}
	
//	private void generateHTMLReport (String finalPath) {
//		XMLWriter writer = null;
//		try {
//			// Create the output file
//			writer = new XMLWriter(finalPath);
//			writer.writeStartDocument();
//			writer.writeStartElement("html");
//			writer.writeRawXML("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
//				+ "<title>Quality Check Report</title><style type=\"text/css\">"
//				+ "body { font-family: Verdana; font-size: smaller; }"
//				+ "h1 { font-size: 110%; }"
//				+ "h2 { font-size: 100%; }"
//				+ "h3 { font-size: 100%; }"
//				+ "p.s { font-family: Courier New, courier; font-size: 100%;"
//				+ "   border: solid 1px; padding: 0.5em; margin-top:-0.7em; border-color: silver; background-color: #C0FFFF; }"
//				+ "p.t { font-family: Courier New, courier; font-size: 100%; margin-top:-1.1em;"
//				+ "   border: solid 1px; padding: 0.5em; border-color: silver; background-color: #C0FFC0; }"
//				+ "span.hi { background-color: #FFFF00; }"
//				+ "</style></head>");
//			writer.writeStartElement("body");
//			writer.writeLineBreak();
//			writer.writeElementString("h1", "Quality Check Report");
//
//			// Process the issues
//			URI docId = null;
//			for ( Issue issue : issues ) {
//				// Skip disabled issues
//				if ( !issue.getEnabled() ) continue;
//				// Do we start a new input document?
//				if (( docId == null ) || !docId.equals(issue.getDocumentURI()) ) {
//					// Ruler only after first input document
//					if ( docId != null ) writer.writeRawXML("<hr />");
//					docId = issue.getDocumentURI();
//					writer.writeElementString("p", "Input: "+docId.getPath());
//				}
//
//				String position = String.format("ID=%s", issue.getTuId());
//				if ( issue.getTuName() != null ) {
//					position += (" ("+issue.getTuName()+")");
//				}
//				if ( issue.getSegId() != null ) {
//					position += String.format(", segment=%s", issue.getSegId());
//				}
//				writer.writeStartElement("p");
//				writer.writeString(position+":");
//				writer.writeRawXML("<br />");
//				writer.writeString(issue.getMessage());
//				writer.writeEndElementLineBreak(); // p
//				writer.writeRawXML("<p class='s'>");
//				writer.writeRawXML("S: '"+highlight(issue.getSource(), issue.getSourceStart(), issue.getSourceEnd())+"'");
//				writer.writeRawXML("<p><p class='t'>");
//				writer.writeRawXML("T: '"+highlight(issue.getTarget(), issue.getTargetStart(), issue.getTargetEnd())+"'");
//				writer.writeRawXML("</p>");
//				writer.writeLineBreak();
//
//			} // End of for issues
//
//			// Write end of document
//			writer.writeEndElementLineBreak(); // body
//			writer.writeEndElementLineBreak(); // html
//			writer.writeEndDocument();
//		}
//		finally {
//			if ( writer != null ) writer.close();
//		}
//	}
//
//	private void generateTabDelimitedReport (String finalPath) {
//		PrintWriter writer = null;
//		try {
//			// Create the output file
//			writer = new PrintWriter(new File(finalPath), "UTF-8");
//			writer.println("Quality Check Report\t\t\t");
//
//			// Process the issues
//			URI docId = null;
//			for ( Issue issue : issues ) {
//				// Skip disabled issues
//				if ( !issue.getEnabled() ) continue;
//				// Do we start a new input document?
//				if (( docId == null ) || !docId.equals(issue.getDocumentURI()) ) {
//					// Ruler only after first input document
//					docId = issue.getDocumentURI();
//					writer.println(docId.getPath()+"\t\t\t");
//				}
//
//				String position = String.format("ID=%s", issue.getTuId());
//				if ( issue.getTuName() != null ) {
//					position += (" ("+issue.getTuName()+")");
//				}
//				if ( issue.getSegId() != null ) {
//					position += String.format(", segment=%s", issue.getSegId());
//				}
//				// position<tab>message<tab>source<tab>target
//				writer.print(position+"\t");
//				writer.print(issue.getMessage()+"\t");
//				writer.print(escape(issue.getSource())+"\t");
//				writer.println(escape(issue.getTarget()));
//
//			} // End of for issues
//		}
//		catch ( Throwable e ) {
//			throw new OkapiIOException("Error when creating the report.\n"+e.getMessage(), e);
//		}
//		finally {
//			if ( writer != null ) writer.close();
//		}
//	}
//
//	private void generateXMLReport (String finalPath) {
//		XMLWriter writer = null;
//		try {
//			// Create the output file
//			writer = new XMLWriter(finalPath);
//			writer.writeStartDocument();
//			writer.writeStartElement("qualityCheckReport");	writer.writeLineBreak();
//			writer.writeStartElement("issues");				writer.writeLineBreak();
//
//			// Process the issues
//			for ( Issue issue : issues ) {
//				// Skip disabled issues
//				if ( !issue.getEnabled() ) continue;
//
//				writer.writeStartElement("issue");		writer.writeLineBreak();
//				writeIndentedElementString( writer, "input", issue.getDocumentURI().getPath());
//				writeIndentedElementString( writer, "tuName", issue.getTuName());
//				writeIndentedElementString( writer, "tuId", issue.getTuId());
//				writeIndentedElementString( writer, "segId", issue.getSegId());
//				writeIndentedElementString( writer, "severity", Integer.toString(issue.getSeverity()));
//				writeIndentedElementString( writer, "issueType", issue.getIssueType().toString());
//				writeIndentedElementString( writer, "message", issue.getMessage());
//				writeIndentedElementStringHilite(writer,"source", issue.getSource(), issue.getSourceStart(), issue.getSourceEnd());
//				writeIndentedElementStringHilite(writer,"target", issue.getTarget(), issue.getTargetStart(), issue.getTargetEnd());
//
//				writer.writeEndElementLineBreak(); // issue
//			} // End of for issues
//
//			// Write end of document
//			writer.writeEndElementLineBreak(); // issues
//			writer.writeEndElementLineBreak(); // qualityCheckReport
//			writer.writeEndDocument();
//		}
//		finally {
//			if ( writer != null ) writer.close();
//		}
//	}
//
//	private static void writeIndentedElementString (XMLWriter writer,
//		String element,
//		String text)
//	{
//		writer.writeString("\t");
//		writer.writeElementString(element, text);
//		writer.writeLineBreak();
//	}
//
//	private static void writeIndentedElementStringHilite (XMLWriter writer,
//		String element,
//		String text,
//		int start,
//		int end)
//	{
//		if (end > 0) {
//			writer.writeString("\t");
//			writer.writeStartElement(element);
//			writer.writeString(text.substring(0,start));
//			writer.writeElementString("hi",text.substring(start,end));
//			writer.writeString(text.substring(end));
//			writer.writeEndElementLineBreak(); // element
//		}
//		else
//			writeIndentedElementString( writer, element, text);
//	}
//
//	private String escape (String text) {
//		return text.replaceAll("\t", "\\t");
//	}
//	
//	private String highlight (String text,
//		int start,
//		int end)
//	{
//		if ( end > 0 ) {
//			// Add placeholder for the highlights
//			StringBuilder buf = new StringBuilder(text);
//			buf.insert(start, '\u0017');
//			buf.insert(end+1, '\u0018');
//			String tmp = Util.escapeToXML(buf.toString(), 0, false, null);
//			tmp = tmp.replace("\u0017", "<span class='hi'>");
//			tmp = tmp.replace("\u0018", "</span>");
//			return tmp.replace("\n", "<br/>");
//		}
//		// Else: just escape the string
//		return Util.escapeToXML(text, 0, false, null).replace("\n", "<br/>");
//	}
	
}
