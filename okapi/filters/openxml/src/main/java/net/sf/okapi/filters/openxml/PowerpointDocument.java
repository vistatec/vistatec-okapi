/*===========================================================================
  Copyright (C) 2016-2017 by the Okapi Framework contributors
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

package net.sf.okapi.filters.openxml;

import static net.sf.okapi.filters.openxml.ContentTypes.Types.Common.CORE_PROPERTIES_TYPE;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.xml.stream.XMLStreamException;

import net.sf.okapi.filters.openxml.ContentTypes.Types.Drawing;
import net.sf.okapi.filters.openxml.ContentTypes.Types.Powerpoint;

class PowerpointDocument extends DocumentType {

	private static final String COMMENTS_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/comments");

	private static final String NOTES_SLIDE_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/notesSlide");

	private static final String NOTES_MASTER_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/notesMaster");

	private static final String CHART_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/chart");

	private static final String DIAGRAM_DATA_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/diagramData");

	private List<String> slideNames = new ArrayList<>();

	private List<String> slideLayoutNames = new ArrayList<>();

	private static final Pattern RELS_NAME_PATTERN = Pattern.compile(".+slide\\d+\\.xml\\.rels");

	private Matcher relsNameMatcher = RELS_NAME_PATTERN.matcher("").reset();

	private static final String SLIDE_LAYOUT_REL =
			Namespaces.DocumentRelationships.getDerivedURI("/slideLayout");

	private StyleDefinitions presentationNotesStyleDefinitions;

	/**
	 * Uses the slide name as key and the comment name as value.
	 */
	private Map<String, String> slidesByComment = new HashMap<>();

	/**
	 * Uses the slide name as key and the note name as value.
	 */
	private Map<String, String> slidesByNote = new HashMap<>();

	/**
	 * Uses the slide name as key and the chart name as value.
	 */
	private Map<String, String> slidesByChart = new HashMap<>();

	/**
	 * Uses the slide name as key and the diagram name as value.
	 */
	private Map<String, String> slidesByDiagramData = new HashMap<>();

	PowerpointDocument(OpenXMLZipFile zipFile,
			ConditionalParameters params) {
		super(zipFile, params);
	}

	@Override
	boolean isClarifiablePart(String contentType) {
		return Powerpoint.MAIN_DOCUMENT_TYPE.equals(contentType);
	}

	@Override
	boolean isStyledTextPart(String entryName, String type) {
		if (type.equals(Powerpoint.SLIDE_TYPE)) return true;
		if (type.equals(Drawing.DIAGRAM_TYPE)) return true;
		if (isMasterPart(entryName, type)) return true;
		if (getParams().getTranslatePowerpointNotes() && type.equals(Powerpoint.NOTES_TYPE)) return true;
		if (type.equals(Drawing.CHART_TYPE)) return true;
		return false;
	}

	boolean isMasterPart(String entryName, String type) {
		if (getParams().getTranslatePowerpointMasters()) {
			if (type.equals(Powerpoint.MASTERS_TYPE)) return true;
			// Layouts are translatable if we are translating masters and this particular layout is
			// in use by a slide
			if (type.equals(Powerpoint.LAYOUT_TYPE)
					&& slideLayoutNames.contains(entryName)) return true;
		}

		return false;
	}

	@Override
	void initialize() throws IOException, XMLStreamException {
		presentationNotesStyleDefinitions = parsePresentationNotesStyleDefinitions();
		slideNames = findSlides();
		slideLayoutNames = findSlideLayouts(slideNames);
		slidesByComment = findComments(slideNames);
		slidesByNote = findNotes(slideNames);
		slidesByChart = findCharts(slideNames);
		slidesByDiagramData = findDiagramDatas(slideNames);
	}

	private StyleDefinitions parsePresentationNotesStyleDefinitions() throws IOException, XMLStreamException {
		String relationshipTarget = getRelationshipTarget(NOTES_MASTER_REL);

		if (null == relationshipTarget) {
			return new EmptyStyleDefinitions();
		}

		Reader reader = getZipFile().getPartReader(relationshipTarget);

		return new PresentationNotesStylesParser(
				getZipFile().getEventFactory(),
				getZipFile().getInputFactory(),
				reader,
				getParams()
		).parse();
	}

	@Override
	OpenXMLPartHandler getHandlerForFile(ZipEntry entry, String contentType) {
		relsNameMatcher.reset(entry.getName());
		if (isRelationshipsPart(contentType) && relsNameMatcher.matches() && getParams().getExtractExternalHyperlinks()) {
			return new RelationshipsPartHandler(getParams(), getZipFile(), entry);
		}

		// Check to see if this is non-translatable
		if (!isTranslatableType(entry.getName(), contentType)) {
			if (isClarifiablePart(contentType)) {
				return new ClarifiablePartHandler(getZipFile(), entry);
			}

			return new NonTranslatablePartHandler(getZipFile(), entry);
		}

		ParseType parseType = getParseType(contentType);
		getParams().nFileType = parseType;

		if (isMasterPart(entry.getName(), contentType)) {
			return new MasterPartHandler(getParams(), getZipFile(), entry, new EmptyStyleDefinitions());

		} else if (isStyledTextPart(entry.getName(), contentType)) {
			StyleDefinitions styleDefinitions;

			switch (contentType) {
				case Powerpoint.NOTES_TYPE:
					styleDefinitions = presentationNotesStyleDefinitions;
					break;
				default:
					styleDefinitions = new EmptyStyleDefinitions();
			}

			return new StyledTextPartHandler(getParams(), getZipFile(), entry, styleDefinitions);
		}

		OpenXMLContentFilter openXMLContentFilter = new OpenXMLContentFilter(getParams(), entry.getName());

		if (Powerpoint.SLIDE_TYPE.equals(contentType)) {
			openXMLContentFilter.setBInMainFile(true);
		}
		openXMLContentFilter.setUpConfig(parseType);

		// Other configuration
		return new StandardPartHandler(openXMLContentFilter, getParams(), getZipFile(), entry);
	}

    /**
     * @param entryName ZIP entry name
     * @param contentType the entry's content type
     * @return {@code true} if the entry is to be excluded due to
     * {@link ConditionalParameters#getPowerpointIncludedSlideNumbersOnly()} and
     * {@link ConditionalParameters#tsPowerpointIncludedSlideNumbers}
     */
    private boolean isExcluded(String entryName, String contentType) {
        return isExcludedSlide(entryName, contentType)
                || isExcludedNote(entryName, contentType)
                || isExcludedComment(entryName, contentType)
                || isExcludedChart(entryName, contentType)
                || isExcludedDiagramData(entryName, contentType);
    }

    private ParseType getParseType(String contentType) {
        ParseType parseType;
		if (contentType.equals(CORE_PROPERTIES_TYPE)) {
			parseType = ParseType.MSWORDDOCPROPERTIES;
		}
		else if (contentType.equals(Powerpoint.COMMENTS_TYPE)) {
			parseType = ParseType.MSPOWERPOINTCOMMENTS;
		} else {
			parseType = ParseType.MSPOWERPOINT;
		}

		return parseType;
	}

	private boolean isTranslatableType(String entryName, String type) {
		if (!entryName.endsWith(".xml")) return false;
        if (isExcluded(entryName, type)) return false;
		if (isStyledTextPart(entryName, type)) return true;
		if (getParams().getTranslateDocProperties() && type.equals(CORE_PROPERTIES_TYPE)) return true;
		if (getParams().getTranslateComments() && type.equals(Powerpoint.COMMENTS_TYPE)) return true;
		if (type.equals(Drawing.DIAGRAM_TYPE)) return true;
		if (getParams().getTranslatePowerpointNotes() && type.equals(Powerpoint.NOTES_TYPE)) return true;
		return false;
	}

	/**
	 * Do additional reordering of the entries for PPTX files to make
	 * sure that slides are parsed in the correct order.  This is done
	 * by scraping information from one of the rels files and the
	 * presentation itself in order to determine the proper order, rather
	 * than relying on the order in which things appeared in the zip.
	 * @return the sorted enum of ZipEntry
	 * @throws IOException if any error is encountered while reading the stream
	 * @throws XMLStreamException if any error is encountered while parsing the XML
	 */
	@Override
	Enumeration<? extends ZipEntry> getZipFileEntries() throws IOException, XMLStreamException {
		OpenXMLZipFile zipFile = getZipFile();
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		List<? extends ZipEntry> entryList = Collections.list(entries);
		Collections.sort(entryList, new ZipEntryComparator(slideNames));
		return Collections.enumeration(entryList);
	}

	List<String> findSlides() throws IOException, XMLStreamException {
		OpenXMLZipFile zipFile = getZipFile();
		// XXX Not strictly correct, I should really look for the main document and then go from there
		Relationships rels = zipFile.getRelationships("ppt/_rels/presentation.xml.rels");
		Presentation pres = new Presentation(zipFile.getInputFactory(), rels);
		pres.parseFromXML(zipFile.getPartReader("ppt/presentation.xml"));
		return pres.getSlidePartNames();
	}

	/**
	 * Examine relationship information to find all layouts that are used in
	 * a slide in this document.  Return a list of their entry names, in order.
	 * @return list of entry names.
	 * @throws XMLStreamException See {@link OpenXMLZipFile#getRelationshipsForTarget(String)}
	 * @throws IOException See {@link OpenXMLZipFile#getRelationshipsForTarget(String)}
	 */
	List<String> findSlideLayouts(List<String> slideNames) throws IOException, XMLStreamException {
		List<String> layouts = new ArrayList<>();
		OpenXMLZipFile zipFile = getZipFile();
		for (String slideName : slideNames) {
			List<Relationships.Rel> rels =
					zipFile.getRelationshipsForTarget(slideName).getRelByType(SLIDE_LAYOUT_REL);
			if (!rels.isEmpty()) {
				layouts.add(rels.get(0).target);
			}
		}
		return layouts;
	}

	/**
	 * @param entryName the entry name
	 * @param contentType the entry's content type
	 * @return {@code true} if the given entry represents a slide that was not included using
	 * option {@link ConditionalParameters#tsPowerpointIncludedSlideNumbers}
	 */
	private boolean isExcludedSlide(String entryName, String contentType) {
		if (!Powerpoint.SLIDE_TYPE.equals(contentType)) {
			return false;
		}

		if (!getParams().getPowerpointIncludedSlideNumbersOnly()) {
			return false;
		}

		int slideIndex = slideNames.indexOf(entryName);
		if (slideIndex == -1) {
			return false;
		}

		int slideNumber = slideIndex + 1; // human readable / 1-based slide numbers
		return !getParams().tsPowerpointIncludedSlideNumbers.contains(slideNumber);
	}

	/**
	 * @param entryName the entry name
	 * @param contentType the entry's content type
	 * @return {@code true} if the given entry represents a note that is used on a slide that was
	 * not included using option {@link ConditionalParameters#tsPowerpointIncludedSlideNumbers}
	 */
	private boolean isExcludedNote(String entryName, String contentType) {
		if (!Powerpoint.NOTES_TYPE.equals(contentType)
				|| !slidesByNote.containsKey(entryName)) {
			return false;
		}

		String slideName = slidesByNote.get(entryName);
		return isExcludedSlide(slideName, Powerpoint.SLIDE_TYPE);
	}

	/**
	 * @param entryName the entry name
	 * @param contentType the entry's content type
	 * @return {@code true} if the given entry represents a comment that is used on a slide that was
	 * not included using option {@link ConditionalParameters#tsPowerpointIncludedSlideNumbers}
	 */
	private boolean isExcludedComment(String entryName, String contentType) {
		if (!Powerpoint.COMMENTS_TYPE.equals(contentType)
				|| !slidesByComment.containsKey(entryName)) {
			return false;
		}

		String slideName = slidesByComment.get(entryName);
		return isExcludedSlide(slideName, Powerpoint.SLIDE_TYPE);
	}

	/**
	 * @param entryName the entry name
	 * @param contentType the entry's content type
	 * @return {@code true} if the given entry represents a chart that is used on a slide that was
	 * not included using option {@link ConditionalParameters#tsPowerpointIncludedSlideNumbers}
	 */
	private boolean isExcludedChart(String entryName, String contentType) {
		if (!Drawing.CHART_TYPE.equals(contentType)
				|| !slidesByChart.containsKey(entryName)) {
			return false;
		}

		String slideName = slidesByChart.get(entryName);
		return isExcludedSlide(slideName, Powerpoint.SLIDE_TYPE);
	}

	/**
	 * "Diagram data" is used by SmartArt, for example.
	 *
	 * @param entryName the entry name
	 * @param contentType the entry's content type
	 * @return {@code true} if the given entry represents a diagram that is used on a slide that was
	 * not included using option {@link ConditionalParameters#tsPowerpointIncludedSlideNumbers}
	 */
	private boolean isExcludedDiagramData(String entryName, String contentType) {
		if (!Drawing.DIAGRAM_TYPE.equals(contentType)
				|| !slidesByDiagramData.containsKey(entryName)) {
			return false;
		}

		String slideName = slidesByDiagramData.get(entryName);
		return isExcludedSlide(slideName, Powerpoint.SLIDE_TYPE);
	}

	/**
	 * Initializes the relationships of type {@link #NOTES_SLIDE_REL}.
	 */
	private Map<String, String> findNotes(List<String> slideNames)
			throws IOException, XMLStreamException {
		return initializeRelsBySlide(slideNames, NOTES_SLIDE_REL);
	}

	/**
	 * Initializes the relationships of type {@link #COMMENTS_REL}.
	 */
	private Map<String, String> findComments(List<String> slideNames)
			throws IOException, XMLStreamException {
		return initializeRelsBySlide(slideNames, COMMENTS_REL);
	}

	/**
	 * Initializes the relationships of type {@link #CHART_REL}.
	 */
	private Map<String, String> findCharts(List<String> slideNames)
			throws IOException, XMLStreamException {
		return initializeRelsBySlide(slideNames, CHART_REL);
	}

	/**
	 * Initializes the relationships of type {@link #DIAGRAM_DATA_REL}.
	 */
	private Map<String, String> findDiagramDatas(List<String> slideNames)
			throws IOException, XMLStreamException {
		return initializeRelsBySlide(slideNames, DIAGRAM_DATA_REL);
	}

	/**
	 * Initializes the relationships of the given type.
	 */
	private Map<String, String> initializeRelsBySlide(List<String> slideNames, String relType)
			throws IOException, XMLStreamException {
		Map<String, String> result = new HashMap<>();
		OpenXMLZipFile zipFile = getZipFile();
		for (String slideName : slideNames) {
			List<Relationships.Rel> rels =
					zipFile.getRelationshipsForTarget(slideName).getRelByType(relType);
			if (rels != null && !rels.isEmpty()) {
				for (Relationships.Rel rel : rels) {
					result.put(rel.target, slideName);
				}
			}
		}
		return result;
	}
}
