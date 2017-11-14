/*===========================================================================
  Copyright (C) 2010-2017 by the Okapi Framework contributors
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;

public class QualityCheckSession {

	public static final String FILE_EXTENSION = ".qcs";
	
	private static final String SERIALSIGNATURE = "OQCS";
	private static final long SERIALVERSIONUID = 2L;
	private static final long SERIALVERSIONUID_1BLOCK = 1L; // Versions using a simple writeUTF for the parameters
	// Keep block size way below 64K because of UTF character size for non-ASCII
	private static final int MAXBLOCKLEN = (65000/3);

	Map<URI, RawDocument> rawDocs; // Temporary solution waiting for the DB
	IFilterConfigurationMapper fcMapper;
	
	private Parameters params;
	private List<Issue> issues;
	private QualityChecker checker;
	private LocaleId sourceLocale = LocaleId.ENGLISH;
	private LocaleId targetLocale = LocaleId.FRENCH;
	private IFilter filter;
	private boolean modified;
	private boolean autoRefresh;

	public QualityCheckSession() {
		reset();
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified(boolean modified) {
		this.modified = modified;
	}

	public List<Issue> getIssues() {
		return issues;
	}

	public Parameters getParameters() {
		return params;
	}

	public void setParameters(Parameters params) {
		this.params = params;
	}

	public boolean getAutoRefresh() {
		return autoRefresh;
	}

	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}

	/**
	 * Adds a raw document to the session. If this is the first document added
	 * to the session, the locales of the session are automatically set to the
	 * source and target locale of this document.
	 *
	 * @param rawDoc the raw document to add (it must have an input URI and its
	 * source and target locale set).
	 */
	public void addRawDocument(RawDocument rawDoc) {
		URI uri = rawDoc.getInputURI();
		rawDocs.put(uri, rawDoc);
		// If it is the first document: its locales become the default
		if (rawDocs.size() == 1) {
			sourceLocale = rawDoc.getSourceLocale();
			targetLocale = rawDoc.getTargetLocale();
		}
		modified = true;
	}

	public List<RawDocument> getDocuments() {
		return new ArrayList<RawDocument>(rawDocs.values());
	}

	public Map<URI, RawDocument> getDocumentsMap() {
		return rawDocs;
	}

	public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}

	public IFilterConfigurationMapper getFilterConfigurationMapper() {
		return fcMapper;
	}

	public LocaleId getSourceLocale() {
		return sourceLocale;
	}

	public void setSourceLocale(LocaleId sourceLocale) {
		if (!this.sourceLocale.equals(sourceLocale)) {
			modified = true;
		}
		this.sourceLocale = sourceLocale;
	}

	public LocaleId getTargetLocale() {
		return targetLocale;
	}

	public void setTargetLocale(LocaleId targetLocale) {
		if (!this.targetLocale.equals(targetLocale)) {
			modified = true;
		}
		this.targetLocale = targetLocale;
	}

	public void reset() {
		rawDocs = new HashMap<URI, RawDocument>();
		issues = new ArrayList<Issue>();
		params = new Parameters();
		checker = new QualityChecker();
	}

	public void resetDisabledIssues() {
		for (Issue issue : issues) {
			issue.setEnabled(true);
		}
		modified = true;
	}

	public int getDocumentCount() {
		return rawDocs.size();
	}

	public void recheckDocument(URI docId) {
		startProcess(sourceLocale, targetLocale);
		RawDocument rd = rawDocs.get(docId);
		if (rd != null) {
			executeRecheck(rd, null);
		}
	}

	public void recheckAll(List<String> sigList) {
		if (rawDocs.size() == 0) {
			issues.clear();
			return;
		}
		startProcess(sourceLocale, targetLocale);
		for (RawDocument rd : rawDocs.values()) {
			executeRecheck(rd, sigList);
		}
	}

	private void executeRecheck(RawDocument rd,
			List<String> sigList) {
		try {
			// Process the document
			filter = fcMapper.createFilter(rd.getFilterConfigId(), filter);
			if (filter == null) {
				throw new OkapiException("Unsupported filter type.");
			}

			if (params.getCheckXliffSchema()) {
				if ("okf_xliff".equals(filter.getName())) {
					ValidateXliffSchema.validateXliffSchema(rd.getInputURI());
				}
			}

			filter.open(rd);
			while (filter.hasNext()) {
				Event event = filter.next();
				switch (event.getEventType()) {
					case START_DOCUMENT:
						StartDocument sd = event.getStartDocument();
						// If signatures exists, don't create the list from the current issues
						if (sigList == null) {
							sigList = clearIssues(rd.getInputURI(), true);
						} else {
							clearIssues(rd.getInputURI(), false);
						}
						processStartDocument(sd, sigList);
						break;
					case START_SUBDOCUMENT:
						processStartSubDocument(event.getStartSubDocument());
						break;
					case TEXT_UNIT:
						processTextUnit(event.getTextUnit());
						break;
					default: // Do nothing
						break;
				}
			}
		} finally {
			if (filter != null) {
				filter.close();
			}
		}
	}

	// Gets all signatures 
	private List<String> getAllSignatures() {
		ArrayList<String> list = new ArrayList<String>();
		Iterator<Issue> iter = issues.iterator();
		while (iter.hasNext()) {
			Issue issue = iter.next();
			if (!issue.getEnabled()) {
				list.add(issue.getSignature());
			}
		}
		return list;
	}

	public List<String> clearIssues(URI docId,
			boolean generateSigList) {
		ArrayList<String> sigList = null;
		// Create signature list if needed
		if (generateSigList) {
			sigList = new ArrayList<String>();
		}

		Iterator<Issue> iter = issues.iterator();
		while (iter.hasNext()) {
			Issue issue = iter.next();
			if (issue.getDocumentURI().equals(docId)) {
				// Generate signature if the issue is disabled
				if (generateSigList && !issue.getEnabled()) {
					sigList.add(issue.getSignature());
				}
				// Remove issue
				iter.remove();
			}
		}
		return sigList;
	}

	public void saveSession(String path) {
		try {
			saveSessionToStream(new FileOutputStream(path));
		} catch (IOException e) {
			throw new OkapiIOException("Error while saving session.", e);
		}
	}

	private void saveSessionToStream(OutputStream outputStream) {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(outputStream);

			// Header
			dos.writeBytes(SERIALSIGNATURE);
			
			// Try to write backward compatible file if possible
			long version = (getLongStringBlockCount(params.toString())>1 ? SERIALVERSIONUID : SERIALVERSIONUID_1BLOCK);
			dos.writeLong(version);

			// Locales
			dos.writeUTF(sourceLocale.toString());
			dos.writeUTF(targetLocale.toString());

			// Parameters
			// Save differently depending on the version
			if ( version == SERIALVERSIONUID_1BLOCK ) {
				dos.writeUTF(params.toString());
			}
			else {
				writeLongString(dos, params.toString());
			}

			// Document list
			dos.writeInt(rawDocs.size());
			for (RawDocument rd : rawDocs.values()) {
				dos.writeUTF(rd.getInputURI().toString());
				dos.writeUTF(rd.getFilterConfigId());
				dos.writeUTF(rd.getEncoding());
			}

			// Issues to keep disabled
			List<String> list = getAllSignatures();
			dos.writeInt(list.size());
			for (String sig : list) {
				dos.writeUTF(sig);
			}
			modified = false;
		} catch (IOException e) {
			throw new OkapiIOException("Error while saving session.", e);
		} finally {
			if (dos != null) {
				try {
					dos.close();
				} catch (IOException e) {
					throw new OkapiIOException("Error closing session file.", e);
				}
			}
		}
	}

	public void loadSession(String path) {
		try {
			loadSessionFromStream(new FileInputStream(path));
		} catch (Throwable e) {
			throw new OkapiIOException("Error reading session file.\n" + e.getMessage(), e);
		}
	}

	private void loadSessionFromStream(InputStream inputStream) {
		reset();
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(inputStream);

			// Header
			byte[] buf = new byte[4];
			dis.read(buf, 0, 4);
			String tmp = new String(buf);
			if (!tmp.equals(SERIALSIGNATURE)) {
				throw new OkapiIOException("Invalid signature: This file is not a QCS file, or is corrupted.");
			}
			long version = dis.readLong();
			if (version > SERIALVERSIONUID) {
				// For now just check the number, later we may have different ways of reading
				throw new OkapiIOException("Invalid version number: This file is not a QCS file, or is corrupted.");
			}

			// Locales
			tmp = dis.readUTF(); // Source
			sourceLocale = LocaleId.fromString(tmp);
			tmp = dis.readUTF(); // Target
			targetLocale = LocaleId.fromString(tmp);

			// Parameters
			// Allow for backward compatibility
			if ( version == SERIALVERSIONUID_1BLOCK ) {
				tmp = dis.readUTF();
			}
			else {
				tmp = readLongString(dis);
			}
			params.fromString(tmp);

			// Document list
			int count = dis.readInt();
			for (int i = 0; i < count; i++) {
				tmp = dis.readUTF();
				URI uri = new URI(tmp);
				String configId = dis.readUTF();
				String encoding = dis.readUTF();
				RawDocument rd = new RawDocument(uri, encoding, sourceLocale, targetLocale);
				rd.setFilterConfigId(configId);
				rawDocs.put(uri, rd);
			}

			// Signatures of issues to keep disabled
			List<String> sigList = new ArrayList<String>();
			count = dis.readInt();
			for (int i = 0; i < count; i++) {
				sigList.add(dis.readUTF());
			}
			recheckAll(sigList);
			modified = false;
		} catch (Throwable e) {
			throw new OkapiIOException("Error reading session file.\n" + e.getMessage(), e);
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					throw new OkapiIOException("Error closing session file.", e);
				}
			}
		}
	}

	public void startProcess(LocaleId srcLoc,
			LocaleId trgLoc) {
		checker.startProcess(srcLoc, trgLoc, params, issues);
	}

	public void processStartDocument(StartDocument startDoc,
			List<String> sigList) {
		checker.processStartDocument(startDoc, sigList);
	}

	public void processStartSubDocument(StartSubDocument startSubDoc) {
		checker.processStartSubDocument(startSubDoc);
	}

	public void processTextUnit(ITextUnit textUnit) {
		checker.processTextUnit(textUnit);
	}

	public void generateReport (String rootDir) {
		// Replace the rootDir variable if needed
		String finalPath = Util.fillRootDirectoryVariable(params.getOutputPath(), rootDir);
		// Build the common root for the issues if needed
		String inputRoot = null;
		if ( !params.getShowFullPath() ) {
			inputRoot = buildIssuesDocRoot();
		}
		// Generate the report in the selected format
		switch (params.getOutputType()) {
			case 1: // Text
				generateTabDelimitedReport(finalPath, inputRoot);
				break;
			case 2: // XML
				generateXMLReport(finalPath, inputRoot);
				break;
			default: // HTML
				generateHTMLReport(finalPath, inputRoot);
		}
	}

	/**
	 * Gets the common root for the document URIs of the current issues.
	 * @return the common path or an empty string (never null).
	 */
	private String buildIssuesDocRoot () {
		// Note that rawDocs is not always set, so we cannot use it
		List<String> dirs = new ArrayList<>(issues.size());
		for ( Issue issue : issues ) {
			dirs.add(Util.getDirectoryName(issue.getDocumentURI().getPath()));
		}
		if ( !dirs.isEmpty() ) {
			String root = Util.longestCommonDir(!Util.isOSCaseSensitive(), dirs.toArray(new String[0]));
			root = Util.ensureSeparator(root, true); // The slash should be cross-platform on URIs 
			return root;
		}
		return "";
	}

	private void generateHTMLReport (String finalPath,
		String inputRoot)
	{
		XMLWriter writer = null;
		try {
			// Create the output file
			writer = new XMLWriter(finalPath);
			writer.writeStartDocument();
			writer.writeStartElement("html");
			writer.writeRawXML("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
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
			writer.writeStartElement("body");
			writer.writeLineBreak();
			writer.writeElementString("h1", "Quality Check Report");

			// Process the issues
			URI docId = null;
			String inputPath = null;
			for (Issue issue : issues) {
				// Skip disabled issues
				if ( !issue.getEnabled() ) {
					continue;
				}
				// Do we start a new input document?
				if ((docId == null) || !docId.equals(issue.getDocumentURI())) {
					// Ruler only after first input document
					if ( docId != null ) {
						writer.writeRawXML("<hr />");
					}
					docId = issue.getDocumentURI();
					// Determine if showing full or relative path
					inputPath = docId.getPath();
					if ( !params.getShowFullPath() ) {
						inputPath = inputPath.replaceFirst(inputRoot, "");
					}
					writer.writeElementString("p", "Input: " + inputPath);
				}

				String position = String.format("ID=%s", issue.getTuId());
				if (issue.getTuName() != null) {
					position += (" (" + issue.getTuName() + ")");
				}
				if (issue.getSegId() != null) {
					position += String.format(", segment=%s", issue.getSegId());
				}
				writer.writeStartElement("p");
				writer.writeString(position + ":");
				writer.writeRawXML("<br />");
				writer.writeString(issue.getMessage());
				writer.writeEndElementLineBreak(); // p
				writer.writeRawXML("<p class='s'>");
				writer.writeRawXML("S: '" + highlight(issue.getSource(), issue.getSourceStart(), issue.getSourceEnd()) + "'");
				writer.writeRawXML("</p><p class='t'>");
				writer.writeRawXML("T: '" + highlight(issue.getTarget(), issue.getTargetStart(), issue.getTargetEnd()) + "'");
				writer.writeRawXML("</p>");
				writer.writeLineBreak();

			} // End of for issues

			// If docId is still null it means there was nothing to report
			if ( docId == null ) {
				writer.writeRawXML("<p>No issue detected.</p>");
			}

			// Write end of document
			writer.writeEndElementLineBreak(); // body
			writer.writeEndElementLineBreak(); // html
			writer.writeEndDocument();
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private void generateTabDelimitedReport (String finalPath,
		String inputRoot)
	{
		PrintWriter writer = null;
		try {
			// Create the output file
			writer = new PrintWriter(new File(finalPath), "UTF-8");
			writer.println("Quality Check Report\t\t\t");

			// Process the issues
			URI docId = null;
			for (Issue issue : issues) {
				// Skip disabled issues
				if (!issue.getEnabled()) {
					continue;
				}
				// Do we start a new input document?
				if ((docId == null) || !docId.equals(issue.getDocumentURI())) {
					docId = issue.getDocumentURI();
					// Determine if showing full or relative path
					String inputPath = docId.getPath();
					if ( !params.getShowFullPath() ) {
						inputPath = inputPath.replaceFirst(inputRoot, "");
					}
					writer.println(inputPath + "\t\t\t");
				}

				String position = String.format("ID=%s", issue.getTuId());
				if (issue.getTuName() != null) {
					position += (" (" + issue.getTuName() + ")");
				}
				if (issue.getSegId() != null) {
					position += String.format(", segment=%s", issue.getSegId());
				}
				// position<tab>message<tab>source<tab>target
				writer.print(position + "\t");
				writer.print(issue.getMessage() + "\t");
				writer.print(escape(issue.getSource()) + "\t");
				writer.println(escape(issue.getTarget()));

			} // End of for issues
		}
		catch ( Throwable e ) {
			throw new OkapiIOException("Error when creating the report.\n" + e.getMessage(), e);
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private void generateXMLReport (String finalPath,
		String inputRoot)
	{
		XMLWriter writer = null;
		try {
			// Create the output file
			writer = new XMLWriter(finalPath);
			writer.writeStartDocument();
			writer.writeStartElement("qualityCheckReport");
			writer.writeLineBreak();
			writer.writeStartElement("issues");
			writer.writeLineBreak();

			// Process the issues
			URI docId = null;
			String inputPath = null;
			for (Issue issue : issues) {
				// Skip disabled issues
				if ( !issue.getEnabled() ) {
					continue;
				}
				// Do we start a new input document?
				if ((docId == null) || !docId.equals(issue.getDocumentURI())) {
					// Ruler only after first input document
					if (docId != null) {
						writer.writeRawXML("<hr />");
					}
					docId = issue.getDocumentURI();
					// Determine if showing full or relative path
					inputPath = docId.getPath();
					if ( !params.getShowFullPath() ) {
						inputPath = inputPath.replaceFirst(inputRoot, "");
					}
				}
				writer.writeStartElement("issue");
				writer.writeLineBreak();
				writeIndentedElementString(writer, "input", inputPath);
				writeIndentedElementString(writer, "tuName", issue.getTuName());
				writeIndentedElementString(writer, "tuId", issue.getTuId());
				writeIndentedElementString(writer, "segId", issue.getSegId());
				writeIndentedElementString(writer, "severity", Integer.toString(issue.getDisplaySeverity()));
				writeIndentedElementString(writer, "issueType", issue.getIssueType().toString());
				writeIndentedElementString(writer, "message", issue.getMessage());
				writeIndentedElementStringHilite(writer, "source", issue.getSource(), issue.getSourceStart(), issue.getSourceEnd());
				writeIndentedElementStringHilite(writer, "target", issue.getTarget(), issue.getTargetStart(), issue.getTargetEnd());

				writer.writeEndElementLineBreak(); // issue
			} // End of for issues

			// Write end of document
			writer.writeEndElementLineBreak(); // issues
			writer.writeEndElementLineBreak(); // qualityCheckReport
			writer.writeEndDocument();
		}
		finally {
			if (writer != null) {
				writer.close();
			}
		}
	}

	private static void writeIndentedElementString (XMLWriter writer,
		String element,
		String text)
	{
		writer.writeString("\t");
		writer.writeElementString(element, text);
		writer.writeLineBreak();
	}

	private static void writeIndentedElementStringHilite(XMLWriter writer,
			String element,
			String text,
			int start,
			int end) {
		if (end > 0) {
			writer.writeString("\t");
			writer.writeStartElement(element);
			writer.writeString(text.substring(0, start));
			writer.writeElementString("hi", text.substring(start, end));
			writer.writeString(text.substring(end));
			writer.writeEndElementLineBreak(); // element
		} else {
			writeIndentedElementString(writer, element, text);
		}
	}

	private String escape(String text) {
		return text.replaceAll("\t", "\\t");
	}

	private String highlight(String text,
			int start,
			int end) {
		if (end > 0) {
			// Add placeholder for the highlights
			StringBuilder buf = new StringBuilder(text);
			buf.insert(start, '\u0017');
			buf.insert(end + 1, '\u0018');
			String tmp = Util.escapeToXML(buf.toString(), 0, false, null);
			tmp = tmp.replace("\u0017", "<span class='hi'>");
			tmp = tmp.replace("\u0018", "</span>");
			return tmp.replace("\n", "<br/>");
		}
		// Else: just escape the string
		return Util.escapeToXML(text, 0, false, null).replace("\n", "<br/>");
	}

	private int getLongStringBlockCount (String data) {
		int r = (data.length() % MAXBLOCKLEN);
		int n = (data.length() / MAXBLOCKLEN);
		return n + ((r > 0) ? 1 : 0);
	}

	private void writeLongString (DataOutputStream dos,
		String data)
		throws IOException
	{
		int r = (data.length() % MAXBLOCKLEN);
		int n = (data.length() / MAXBLOCKLEN);
		int count = n + ((r > 0) ? 1 : 0);
		
		dos.writeInt(count); // Number of blocks
		int pos = 0;
	
		// Write the full blocks
		for ( int i=0; i<n; i++ ) {
			dos.writeUTF(data.substring(pos, pos+MAXBLOCKLEN));
			pos += MAXBLOCKLEN;
		}
		// Write the remaining text
		if ( r > 0 ) {
			dos.writeUTF(data.substring(pos));
		}
	}

	private String readLongString (DataInputStream dis)
		throws IOException
	{
		StringBuilder tmp = new StringBuilder();
		int count = dis.readInt(); // Read number of blocks
		for ( int i=0; i<count; i++ ) {
			tmp.append(dis.readUTF()); // Read each block
		}
		return tmp.toString();
	}

}
