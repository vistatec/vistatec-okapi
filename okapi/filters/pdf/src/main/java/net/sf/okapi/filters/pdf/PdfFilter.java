/*===========================================================================
  Copyright (C) 2016 by the Okapi Framework contributors
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

package net.sf.okapi.filters.pdf;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.encoder.EncoderManager;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.FilterConfiguration;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericFilterWriter;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
import net.sf.okapi.common.skeleton.ISkeletonWriter;
import net.sf.okapi.filters.plaintext.PlainTextFilter;
import net.sf.okapi.filters.plaintext.paragraphs.ParaPlainTextFilter;
import net.sf.okapi.lib.extra.filters.WrapMode;

/**
 * Implements the IFilter interface for PDF files (extraction only).
 */
@UsingParameters(Parameters.class)
public class PdfFilter implements IFilter {
	// 10Meg memory buffer for PDF parser
	private final static int MAX_BUFFER = 1024 * 1024 * 10;

	private Parameters params;
	private EncoderManager encoderManager;
	private RawDocument input;
	private ParaPlainTextFilter textFilter;

	public PdfFilter() {
		params = new Parameters();
	}

	@Override
	public void cancel() {
	}

	@Override
	public void close() {
		if (input != null) {
			input.close();
		}
		if (textFilter != null) {
			textFilter.close();
		}
	}

	@Override
	public String getName() {
		return "okf_pdf";
	}

	@Override
	public String getDisplayName() {
		return "PDF Filter";
	}

	@Override
	public String getMimeType() {
		return MimeTypeMapper.PDF_MIME_TYPE;
	}

	@Override
	public Parameters getParameters() {
		return params;
	}

	@Override
	public void setFilterConfigurationMapper(IFilterConfigurationMapper fcMapper) {
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	@Override
	public ISkeletonWriter createSkeletonWriter() {
		return new GenericSkeletonWriter();
	}

	@Override
	public IFilterWriter createFilterWriter() {
		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
	}

	@Override
	public List<FilterConfiguration> getConfigurations() {
		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
		list.add(new FilterConfiguration("okf_pdf", MimeTypeMapper.PDF_MIME_TYPE, getClass().getName(),
				"PDF (Portable Document Format)", "Configuration for PDF documents", null, ".pdf;"));
		return list;
	}

	@Override
	public EncoderManager getEncoderManager() {
		if (encoderManager == null) {
			encoderManager = new EncoderManager();
			encoderManager.setMapping(MimeTypeMapper.PDF_MIME_TYPE, "net.sf.okapi.common.encoder.DefaultEncoder");
		}
		return encoderManager;
	}

	@Override
	public void open(RawDocument input) {
		open(input, true);
	}

	@Override
	public void open(RawDocument input, boolean generateSkeleton) {
		// keep a reference so we can clean up
		this.input = input;

		// Compile code finder rules
		if (params.getUseCodeFinder()) {
			params.codeFinder.compile();
		}

		PDDocument pdf = null;
		StringWriter pdfWriter = new StringWriter();
		try {
			// load the PDF file. Only use MAX_BYTES memory, then offload to
			// temp file
			pdf = PDDocument.load(input.getStream(), MemoryUsageSetting.setupMixed(MAX_BUFFER));
			PDFTextStripper textStripper = new PDFTextStripper();
			textStripper.setLineSeparator(params.getLineSeparator());
			textStripper.setParagraphEnd(params.getParagraphSeparator());
			textStripper.setIndentThreshold(Float.parseFloat(params.getIndentThreshold()));
			textStripper.setSpacingTolerance(Float.parseFloat(params.getSpacingTolerance()));
			textStripper.writeText(pdf, pdfWriter);

			// we have PDF text, now call Plain text filter on the contents
			textFilter = new ParaPlainTextFilter();
			@SuppressWarnings("resource")
			RawDocument rd = new RawDocument(pdfWriter.toString(), input.getSourceLocale());
			rd.setFilterConfigId(PlainTextFilter.FILTER_NAME);
			rd.setEncoding(input.getEncoding());
			rd.setId(input.getId());
			
			// transfer over parameters and setup for whitespace trimming on the assumption
			// the PDF text extractor will mess up whitespace anyway
			net.sf.okapi.filters.plaintext.paragraphs.Parameters p = (net.sf.okapi.filters.plaintext.paragraphs.Parameters) textFilter.getParameters();
			p.trimLeading = true;
			p.trimTrailing = true;
			p.extractParagraphs = true;
			p.preserveWS = params.getPreserveWhitespace();
			// replace linebreaks with spaces
			p.wrapMode = WrapMode.SPACES;
			p.useCodeFinder = params.getUseCodeFinder();
			p.codeFinderRules = params.codeFinder.toString();
			
			textFilter.setParameters(p);
			textFilter.open(rd, generateSkeleton);
		} catch (IOException e) {
			throw new OkapiIOException("Error parsing PDF file", e);
		} finally {
			try {
				pdfWriter.close();
				pdf.close();
			} catch (IOException e) {
				throw new OkapiIOException("Error closing the PDF parser.", e);
			}
		}
	}

	@Override
	public boolean hasNext() {
		return textFilter.hasNext();
	}

	@Override
	public Event next() {
		return textFilter.next();
	}
}
