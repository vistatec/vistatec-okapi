/*===========================================================================
  Copyright (C) 2009-2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.translationcomparison;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.filterwriter.TMXWriter;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.lib.search.lucene.analysis.AlphabeticNgramTokenizer;
import net.sf.okapi.lib.translation.TextMatcher;
import net.sf.okapi.steps.wordcount.common.GMX;
import net.sf.okapi.steps.wordcount.common.Metrics;
import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UsingParameters(Parameters.class)
public class TranslationComparisonStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final String WIKIURL = "http://okapiframework.org/wiki/index.php?title=Translation_Comparison_Step";

	private Parameters params;
	private IFilter filter2;
	private IFilter filter3;
	private TextMatcher matcher;
	private XMLWriter writer;
	private TMXWriter tmx;
	private PrintWriter prnWriter;
	private boolean isBaseMultilingual;
	private boolean isInput2Multilingual;
	private boolean isInput3Multilingual;
	private String pathToOpen;
	private int options;
	private Property deScore1to2Prop;
	private Property deScore1to3Prop;
	private Property fmScore1to2Prop;
	private Property fmScore1to3Prop;
	private long deScoreTotal1to2;
	private long deScoreTotal1to3;
	private long deScoreTotal2to3;
	private long fmScoreTotal1to2;
	private long fmScoreTotal1to3;
	private long fmScoreTotal2to3;
	private int itemCount;
	private String baseDocumentPath;
	private String compDocumentPath;
	private IFilterConfigurationMapper fcMapper;
	private LocaleId targetLocale;
	private LocaleId targetLocale2Extra;
	private LocaleId targetLocale3Extra;
	private LocaleId sourceLocale;
	private URI inputURI;
	private RawDocument rawDoc2;
	private RawDocument rawDoc3;
	private GenericContent fmt;
	private String rootDir;
	private String inputRootDir;
	private AlphabeticNgramTokenizer tokenizer;
	private SimpleDateFormat df;
	private long wcTotal;
	private int[] edBrackets = new int[11];
	private int[] edWCBrackets = new int[11];
	private int[] fmBrackets = new int[11];
	private int[] fmWCBrackets = new int[11];
	
	private int edScoreWordTotal;
	private int fmScoreWordTotal;

	public TranslationComparisonStep () {
		params = new Parameters();
		fmt = new GenericContent();
	}
	
	@StepParameterMapping(parameterType = StepParameterType.FILTER_CONFIGURATION_MAPPER)
	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
		this.fcMapper = fcMapper;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.SOURCE_LOCALE)
	public void setSourceLocale (LocaleId sourceLocale) {
		this.sourceLocale = sourceLocale;
	}
	
	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale (LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.INPUT_URI)
	public void setInputURI (URI inputURI) {
		this.inputURI = inputURI;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.SECOND_INPUT_RAWDOC)
	public void setSecondInput (RawDocument secondInput) {
		this.rawDoc2 = secondInput;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.THIRD_INPUT_RAWDOC)
	public void setThirdInput (RawDocument thirdInput) {
		this.rawDoc3 = thirdInput;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}

	@StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
	public void setInputRootDirectory (String inputRootDir) {
		this.inputRootDir = inputRootDir;
	}

	@Override
	public String getName () {
		return "Translation Comparison";
	}

	@Override
	public String getDescription () {
		return "Compare the translated text units between several documents. "
			+ "Expects: filter events. Sends back: filter events.";
	}

	@Override
	public IParameters getParameters () {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}
 
	@Override
	protected Event handleStartBatch (Event event) {
		// Both strings are in the target language.
		matcher = new TextMatcher(targetLocale, targetLocale);
		
		// Start TMX writer (one for all input documents)
		if ( params.isGenerateTMX() ) {
			String resolvedPath = Util.fillRootDirectoryVariable(params.getTmxPath(), rootDir);
			resolvedPath = Util.fillInputRootDirectoryVariable(resolvedPath, inputRootDir);
			resolvedPath = LocaleId.replaceVariables(resolvedPath, sourceLocale, targetLocale);
			tmx = new TMXWriter(resolvedPath);
			tmx.writeStartDocument(sourceLocale, targetLocale,
				getClass().getName(), null, null, null, null);
		}
		pathToOpen = null;
		deScore1to2Prop = new Property("Txt::EDScore", "", false);
		fmScore1to2Prop = new Property("Txt::FMScore", "", false);
		targetLocale2Extra = LocaleId.fromString(targetLocale.toString()+params.getTarget2Suffix());
		deScore1to3Prop = new Property("Txt::EDScore1to3", "", false);
		fmScore1to3Prop = new Property("Txt::FMScore1to3", "", false);
		targetLocale3Extra = LocaleId.fromString(targetLocale.toString()+params.getTarget3Suffix());
		
		options = 0;
		if ( !params.isCaseSensitive() ) options |= TextMatcher.IGNORE_CASE;
		if ( !params.isWhitespaceSensitive() ) options |= TextMatcher.IGNORE_WHITESPACES;
		if ( !params.isPunctuationSensitive() ) options |= TextMatcher.IGNORE_PUNCTUATION;
		
		tokenizer = net.sf.okapi.lib.search.lucene.scorer.Util.createNgramTokenizer(3, targetLocale);
		
		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
		df.setTimeZone(TimeZone.getTimeZone("GMT"));

		return event;
	}
	
	@Override
	protected Event handleEndBatch (Event event) {
		matcher = null;
		if ( writer != null ) {
			writer.close();
			writer = null;
		}
		if ( prnWriter != null ) {
			prnWriter.close();
			prnWriter = null;
		}
		if ( tmx != null ) {
			tmx.writeEndDocument();
			tmx.close();
			tmx = null;
		}
		Runtime.getRuntime().gc();
		if ( params.isAutoOpen() && ( pathToOpen != null )) {
			Util.openURL((new File(pathToOpen)).getAbsolutePath());
		}
		
		return event;
	}
	
	@Override
	protected Event handleStartDocument (Event event1) {
		StartDocument startDoc1 = (StartDocument)event1.getResource();
		initializeDocumentData();
		isBaseMultilingual = startDoc1.isMultilingual();
		
		// Move to start document for second input
		Event event2 = synchronize(filter2, EventType.START_DOCUMENT);
		StartDocument startDoc2 = (StartDocument)event2.getResource();
		isInput2Multilingual = startDoc2.isMultilingual();
		
		// Move to start document for third input
		if ( filter3 != null ) {
			Event event3 = synchronize(filter3, EventType.START_DOCUMENT);
			StartDocument startDoc3 = (StartDocument)event3.getResource();
			isInput3Multilingual = startDoc3.isMultilingual();
		}
		
		deScoreTotal1to2 = 0;
		deScoreTotal1to3 = 0;
		deScoreTotal2to3 = 0;
		fmScoreTotal1to2 = 0;
		fmScoreTotal1to3 = 0;
		fmScoreTotal2to3 = 0;
		wcTotal = 0;
		
		edScoreWordTotal = 0;
		fmScoreWordTotal = 0;
		
		for ( int i=0; i<edBrackets.length; i++ ) {
			edBrackets[i] = 0;
			edWCBrackets[i] = 0;
			fmBrackets[i] = 0;
			fmWCBrackets[i] = 0;
		}
		
		itemCount = 0;
		
		return event1;
	}
	
	@Override
	protected Event handleEndDocument (Event event) {

		float avgScore1to2 = 0;
		float avgScore1to3 = 0;
		float avgScore2to3 = 0;
		float avgFuzzyScore1to2 = 0;
		float avgFuzzyScore1to3 = 0;
		float avgFuzzyScore2to3 = 0;

		if ( filter2 != null ) {
    		filter2.close();
    	}
    	if ( filter3 != null ) {
    		filter3.close();
    	}
    	
    	// Average scores
    	if ( itemCount > 0 ) {
    		avgScore1to2 = (float)deScoreTotal1to2 / itemCount;
    		avgScore1to3 = (float)deScoreTotal1to3 / itemCount;
    		avgScore2to3 = (float)deScoreTotal2to3 / itemCount;
    		avgFuzzyScore1to2 = (float)fmScoreTotal1to2 / itemCount;
    		avgFuzzyScore1to3 = (float)fmScoreTotal1to3 / itemCount;
    		avgFuzzyScore2to3 = (float)fmScoreTotal2to3 / itemCount;
    	}
    	
    	// Output report
    	if ( params.isGenerateHTML() ) {
			writer.writeEndElement(); // table
    		writer.writeElementString("p", String.format("", itemCount));
    		if ( itemCount > 0 ) {

    			writer.writeStartElement("h2"); //$NON-NLS-1$
    			writer.writeAttributeString("id", "summary");
    			writer.writeString("Summary");
    			writer.writeEndElement();
    			
    			// Matrix output
    			writer.writeElementString("p", String.format("Repartition for %s to %s:", params.getDocument1Label(), params.getDocument2Label()));
    			
    			writer.writeRawXML("<table border=1 cellspacing=0 cellpadding=5>"); //$NON-NLS-1$
    			writer.writeRawXML("<tr><th rowspan=2>Scores</th><th colspan=4>ED-Scores</th><th colspan=4>FM-Scores</th></tr>");
    			writer.writeRawXML("<tr>"
       				+ "<th style=\"text-align: right; width: 100px;\">Segments </th>"
       				+ "<th style=\"text-align: right; width: 100px;\">% </th>"
       				+ "<th style=\"text-align: right; width: 100px;\">Words </th>"
       				+ "<th style=\"text-align: right; width: 100px;\">% </th>"
       				+ "<th style=\"text-align: right; width: 100px;\">Segments </th>"
       				+ "<th style=\"text-align: right; width: 100px;\">% </th>"
       				+ "<th style=\"text-align: right; width: 100px;\">Words </th>"
       				+ "<th style=\"text-align: right; width: 100px;\">% </th>"
    				+ "</tr>");
    			printBracket(10, "100");
    			printBracket(9, "90 - 99");
    			printBracket(8, "80 - 89");
    			printBracket(7, "70 - 79");
    			printBracket(6, "60 - 69");
    			printBracket(5, "50 - 59");
    			printBracket(4, "40 - 49");
    			printBracket(3, "30 - 39");
    			printBracket(2, "20 - 29");
    			printBracket(1, "10 - 19");
    			printBracket(0, "0 - 9");
    			writer.writeRawXML("<tr><td>Total</td>"); //$NON-NLS-1$
    			writer.writeRawXML(String.format("<td align='right'>%d</td>", itemCount)); //$NON-NLS-1$    			
    			writer.writeRawXML("<td align='right'>100%</td>"); //$NON-NLS-1$
    			writer.writeRawXML(String.format("<td align='right'>%d</td>", wcTotal)); //$NON-NLS-1$
    			writer.writeRawXML("<td align='right'>100%</td>"); //$NON-NLS-1$
    			writer.writeRawXML(String.format("<td align='right'>%d</td>", itemCount)); //$NON-NLS-1$    			
    			writer.writeRawXML("<td align='right'>100%</td>"); //$NON-NLS-1$
    			writer.writeRawXML(String.format("<td align='right'>%d</td>", wcTotal)); //$NON-NLS-1$
    			writer.writeRawXML("<td align='right'>100%</td>"); //$NON-NLS-1$
    			writer.writeRawXML("</tr>\n"); //$NON-NLS-1$
    			writer.writeRawXML("</table>\n"); // table
    			writer.writeElementString("p", " ");
    			
    			writer.writeStartElement("table"); //$NON-NLS-1$
    			
    			//--total number of segments--
    			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
    			writer.writeString("Total Number of Segments:"); //$NON-NLS-1$
    			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
    			writer.writeString(String.format("%d", itemCount));
    			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$

    			//--total number of words--
    			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
    			writer.writeString("Total Number of Words:"); //$NON-NLS-1$
    			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
    			writer.writeString(String.format("%d", wcTotal));
    			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$

    			//--average wordcount per segment--
    			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
    			writer.writeString("Average word count per segment:");
    			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
    			writer.writeString(String.format("%.2f", (float)wcTotal / itemCount));
    			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$
    			
    			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
    			writer.writeString("Average ED-Score (by segment):"); //$NON-NLS-1$
    			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
    			writer.writeString(String.format("%s to %s = %.2f",
    				params.getDocument1Label(), params.getDocument2Label(), avgScore1to2));
    			if ( deScoreTotal1to3 > 0 ) {
        			writer.writeString(String.format(",  %s to %s = %.2f,  ",
        				params.getDocument1Label(), params.getDocument3Label(), avgScore1to3));
        			writer.writeString(String.format("%s to %s = %.2f",
        				params.getDocument2Label(), params.getDocument3Label(), avgScore2to3));
    			}
    			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$
    			
    			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
    			writer.writeString("Average FM-Score (by segment):");
    			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
    			writer.writeString(String.format("%s to %s = %.2f",
    				params.getDocument1Label(), params.getDocument2Label(), avgFuzzyScore1to2));
    			if ( deScoreTotal1to3 > 0 ) {
        			writer.writeString(String.format(",  %s to %s = %.2f,  ",
        				params.getDocument1Label(), params.getDocument3Label(), avgFuzzyScore1to3));
        			writer.writeString(String.format("%s to %s = %.2f",
        				params.getDocument2Label(), params.getDocument3Label(), avgFuzzyScore2to3));
    			}
    			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$
    			
    			float edScoreWordTotalAvg = ((float)edScoreWordTotal/wcTotal);
    			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
    			writer.writeString("Average ED-Score (by word):");
    			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
    			writer.writeString(String.format("%s to %s = %.2f",
    				params.getDocument1Label(), params.getDocument2Label(), edScoreWordTotalAvg));
    			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$
    			
    			float fmScoreWordTotalAvg = ((float)fmScoreWordTotal/wcTotal);
    			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
    			writer.writeString("Average FM-Score (by word):");
    			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
    			writer.writeString(String.format("%s to %s = %.2f",
    				params.getDocument1Label(), params.getDocument2Label(), fmScoreWordTotalAvg));
    			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$
    			
    			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
    			writer.writeString("Edit Effort Score:");
    			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
    			writer.writeString(String.format("%.2f", 100 - ((edScoreWordTotalAvg + fmScoreWordTotalAvg)/2)));
    			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$
    			
    			writer.writeEndElement(); // table    			
    			writer.writeElementString("p", " ");    			
    		}
			writer.writeEndElement(); // body
			writer.writeEndElement(); // html
    		writer.close();
    	}
    	
    	// Append to data log if requested
    	if (( itemCount > 0 ) && params.getUseDataLog() ) {
    		PrintWriter out = null;
			try {
	    		// Open the tab-delimited log file
				out = new PrintWriter(
					new BufferedWriter(
						new OutputStreamWriter(
							new FileOutputStream(params.getDataLogPath(), true),
							"UTF-8")
						)
					);
	    		// Append the results
				// Date/Time in UTC <tab> baseDoc <tab> compDoc <tab> EDAvgScore <tab> FuzzyScore
	    	    out.println(String.format("%s\t%s\t%s\t%.2f\t%.2f",
	    	    	df.format(new Date()), baseDocumentPath, compDocumentPath, avgScore1to2, avgFuzzyScore1to2));
			}
			catch ( IOException e ) {
				logger.error("Cannot create the file '{}'\n"+e.getMessage(), params.getDataLogPath());
			}
			finally {
	    		// Close
	    	    if ( out != null ) out.close();
			}
    	}
    	
    	return event;
	}

	private void printBracket (int index,
		String range)
	{
		writer.writeRawXML("<tr "+
			((index % 2)==0 ? "" : "bgcolor=#FFFFCC")
			+ "><td>"+range+"</td>"); //$NON-NLS-1$
		writer.writeRawXML(String.format("<td align='right'>%d</td>", edBrackets[index])); //$NON-NLS-1$
		// ED-Scores
		writer.writeRawXML(((itemCount == 0) ? "<td align='right'>NA</td>" : String.format("<td align='right'>%.0f</td>", (float)edBrackets[index]/itemCount*100))); //$NON-NLS-1$
		writer.writeRawXML(String.format("<td align='right'>%d</td>", edWCBrackets[index])); //$NON-NLS-1$
		writer.writeRawXML(((wcTotal == 0) ? "<td align='right'>NA</td>" : String.format("<td align='right'>%.0f</td>", (float)edWCBrackets[index]/wcTotal*100))); //$NON-NLS-1$
		writer.writeRawXML(String.format("<td align='right'>%d</td>", fmBrackets[index])); //$NON-NLS-1$
		// FM-Scores
		writer.writeRawXML(((itemCount == 0) ? "<td align='right'>NA</td>" : String.format("<td align='right'>%.0f</td>", (float)fmBrackets[index]/itemCount*100))); //$NON-NLS-1$
		writer.writeRawXML(String.format("<td align='right'>%d</td>", fmWCBrackets[index])); //$NON-NLS-1$
		writer.writeRawXML(((wcTotal == 0) ? "<td align='right'>NA</td>" : String.format("<td align='right'>%.0f</td>", (float)fmWCBrackets[index]/wcTotal*100))); //$NON-NLS-1$
		writer.writeRawXML("</tr>\n"); //$NON-NLS-1$
	}
	
	@Override
	protected Event handleTextUnit (Event event1) {
		ITextUnit tu1 = event1.getTextUnit();
		// Move to the next TU
		Event event2 = synchronize(filter2, EventType.TEXT_UNIT);
		Event event3 = null;
		if ( filter3 != null ) {
			event3 = synchronize(filter3, EventType.TEXT_UNIT);
		}
		// Skip non-translatable
		if ( !tu1.isTranslatable() ) return event1;
		
		ITextUnit tu2 = event2.getTextUnit();
		ITextUnit tu3 = null;
		if ( event3 != null ) {
			tu3 = event3.getTextUnit();
		}

		TextFragment srcFrag = null;
		if ( isBaseMultilingual ) {
			if ( tu1.getSource().contentIsOneSegment() ) {
				srcFrag = tu1.getSource().getFirstContent();
			}
			else {
				srcFrag = tu1.getSource().getUnSegmentedContentCopy();
			}
		}
		else {
			if ( isInput2Multilingual ) {
				if ( tu2.getSource().contentIsOneSegment() ) {
					srcFrag = tu2.getSource().getFirstContent();
				}
				else {
					srcFrag = tu2.getSource().getUnSegmentedContentCopy();
				}
			}
			else if (( tu3 != null ) && isInput3Multilingual ) {
				if ( tu3.getSource().contentIsOneSegment() ) {
					srcFrag = tu3.getSource().getFirstContent();
				}
				else {
					srcFrag = tu3.getSource().getUnSegmentedContentCopy();
				}
			}
		}
		
		TextContainer trgCont;
		// Get the text for the base translation
		TextFragment trgFrag1;
		if ( isBaseMultilingual ) {
			trgCont = tu1.getTarget(targetLocale);
			if ( trgCont == null ) {
				throw new OkapiException(String.format("Missing '%s' entry for text unit id='%s' in base document.",
					targetLocale.toString(), tu1.getId()));
			}
			if ( params.getUseAltTrans() ) {
				AltTranslationsAnnotation ata = trgCont.getAnnotation(AltTranslationsAnnotation.class);
				trgFrag1 = null;
				if ( ata != null ) {
					for ( AltTranslation at : ata ) {
						if (( at != null ) && params.getAltTransOrigin().equals(at.getOrigin()) ) {
							trgFrag1 = at.getTarget().getFirstContent();
							break;
						}
					}
				}
				if ( trgFrag1 == null ) {
					// No alt-trans found for this entry: so we move on to the next trans-unit
					// Using alt-trans assumes the two inputs are the same file.
					//And the entries without alt-trans are skipped.
					return event1;
				}
			}
			else {
				if ( trgCont.contentIsOneSegment() ) {
					trgFrag1 = trgCont.getFirstContent();
				}
				else {
					trgFrag1 = trgCont.getUnSegmentedContentCopy();
				}
			}
		}
		else {
			if ( tu1.getSource().contentIsOneSegment() ) {
				trgFrag1 = tu1.getSource().getFirstContent();
			}
			else {
				trgFrag1 = tu1.getSource().getUnSegmentedContentCopy();
			}
		}

		// Get the text for the to-compare translation 1
		TextFragment trgFrag2;
		if ( isInput2Multilingual ) {
			trgCont = tu2.getTarget(targetLocale);
			if ( trgCont == null ) {
				throw new OkapiException(String.format("Missing '%s' entry for text unit id='%s' in document 2.",
					targetLocale.toString(), tu2.getId()));
			}
			if ( trgCont.contentIsOneSegment() ) {
				trgFrag2 = trgCont.getFirstContent();
			}
			else {
				trgFrag2 = trgCont.getUnSegmentedContentCopy();
			}
		}
		else {
			if ( tu2.getSource().contentIsOneSegment() ) {
				trgFrag2 = tu2.getSource().getFirstContent();
			}
			else {
				trgFrag2 = tu2.getSource().getUnSegmentedContentCopy();
			}
		}
		
		// Get the text for the to-compare translation 2
		TextFragment trgFrag3 = null;
		if ( tu3 != null ) {
			if ( isInput3Multilingual ) {
				trgCont = tu3.getTarget(targetLocale);
				if ( trgCont == null ) {
					throw new OkapiException(String.format("Missing '%s' entry for text unit id='%s' in document 3.",
						targetLocale.toString(), tu3.getId()));
				}
				if ( trgCont.contentIsOneSegment() ) {
					trgFrag3 = trgCont.getFirstContent();
				}
				else {
					trgFrag3 = trgCont.getUnSegmentedContentCopy();
				}
			}
			else {
				if ( tu3.getSource().contentIsOneSegment() ) {
					trgFrag3 = tu3.getSource().getFirstContent();
				}
				else {
					trgFrag3 = tu3.getSource().getUnSegmentedContentCopy();
				}
			}
		}
		
		// Do we have a base translation?
		if ( trgFrag1 == null ) {
			// No comparison if there is no base translation
			return event1;
		}
		// Do we have a translation to compare to?
		if ( trgFrag2 == null ) {
			// Create and empty entry
			trgFrag2 = new TextFragment();
		}
		if ( event3 != null ) {
			if ( trgFrag3 == null ) {
				// Create and empty entry
				trgFrag3 = new TextFragment();
			}
		}
		
		// Compute the distance
		int edScore1to2 = matcher.compare(trgFrag1, trgFrag2, options);
		int fmScore1to2 = Math.round(net.sf.okapi.lib.search.lucene.scorer.Util.calculateNgramDiceCoefficient(
				trgFrag1.getText(), trgFrag2.getText(), tokenizer));
		int edScore1to3 = -1;
		int fmScore1to3 = -1;
		int edScore2to3 = -1;
		int fmScore2to3 = -1;
		if ( event3 != null ) {
			edScore1to3 = matcher.compare(trgFrag1, trgFrag3, options);
			fmScore1to3 = Math.round(net.sf.okapi.lib.search.lucene.scorer.Util.calculateNgramDiceCoefficient(
				trgFrag1.getText(), trgFrag3.getText(), tokenizer));
			edScore2to3 = matcher.compare(trgFrag2, trgFrag3, options);
			fmScore2to3 = Math.round(net.sf.okapi.lib.search.lucene.scorer.Util.calculateNgramDiceCoefficient(
				trgFrag2.getText(), trgFrag3.getText(), tokenizer));
		}
		
		// Store the scores for the average
		deScoreTotal1to2 += edScore1to2;
		deScoreTotal1to3 += edScore1to3;
		deScoreTotal2to3 += edScore2to3;
		fmScoreTotal1to2 += fmScore1to2;
		fmScoreTotal1to3 += fmScore1to3;
		fmScoreTotal2to3 += fmScore2to3;
		
		MetricsAnnotation sma = tu1.getSource().getAnnotation(MetricsAnnotation.class);
		long srcWC = 0;
		if (sma != null) {
			Metrics m = sma.getMetrics();
			srcWC = m.getMetric(GMX.TotalWordCount);
			wcTotal += srcWC;
		}
		
		// Populate the scoreWordTotals
		edScoreWordTotal += (srcWC * edScore1to2);
		fmScoreWordTotal += (srcWC * fmScore1to2);
		
		// Populate the matrix for the ED-Score
		if ( edScore1to2 < 10 ) {
			edBrackets[0]++;
			edWCBrackets[0] += srcWC;
		}
		else if (( edScore1to2 >= 10 ) && ( edScore1to2 < 20 )) {
			edBrackets[1]++;
			edWCBrackets[1] += srcWC;
		}
		else if (( edScore1to2 >= 20 ) && ( edScore1to2 < 30 )) {
			edBrackets[2]++;
			edWCBrackets[2] += srcWC;
		}
		else if (( edScore1to2 >= 30 ) && ( edScore1to2 < 40 )) {
			edBrackets[3]++;
			edWCBrackets[3] += srcWC;
		}
		else if (( edScore1to2 >= 40 ) && ( edScore1to2 < 50 )) {
			edBrackets[4]++;
			edWCBrackets[4] += srcWC;
		}
		else if (( edScore1to2 >= 50 ) && ( edScore1to2 < 60 )) {
			edBrackets[5]++;
			edWCBrackets[5] += srcWC;
		}
		else if (( edScore1to2 >= 60 ) && ( edScore1to2 < 70 )) {
			edBrackets[6]++;
			edWCBrackets[6] += srcWC;
		}
		else if (( edScore1to2 >= 70 ) && ( edScore1to2 < 80 )) {
			edBrackets[7]++;
			edWCBrackets[7] += srcWC;
		}
		else if (( edScore1to2 >= 80 ) && ( edScore1to2 < 90 )) {
			edBrackets[8]++;
			edWCBrackets[8] += srcWC;
		}
		else if (( edScore1to2 >= 90 ) && ( edScore1to2 < 100 )) {
			edBrackets[9]++;
			edWCBrackets[9] += srcWC;
		}
		else if ( edScore1to2 >= 100 ) { // 100
			edBrackets[10]++;
			edWCBrackets[10] += srcWC;
		}
		
		// Populate the matrix for the FM-Score
		if ( fmScore1to2 < 10 ) {
			fmBrackets[0]++;
			fmWCBrackets[0] += srcWC;
		}
		else if (( fmScore1to2 >= 10 ) && ( fmScore1to2 < 20 )) {
			fmBrackets[1]++;
			fmWCBrackets[1] += srcWC;
		}
		else if (( fmScore1to2 >= 20 ) && ( fmScore1to2 < 30 )) {
			fmBrackets[2]++;
			fmWCBrackets[2] += srcWC;
		}
		else if (( fmScore1to2 >= 30 ) && ( fmScore1to2 < 40 )) {
			fmBrackets[3]++;
			fmWCBrackets[3] += srcWC;
		}
		else if (( fmScore1to2 >= 40 ) && ( fmScore1to2 < 50 )) {
			fmBrackets[4]++;
			fmWCBrackets[4] += srcWC;
		}
		else if (( fmScore1to2 >= 50 ) && ( fmScore1to2 < 60 )) {
			fmBrackets[5]++;
			fmWCBrackets[5] += srcWC;
		}
		else if (( fmScore1to2 >= 60 ) && ( fmScore1to2 < 70 )) {
			fmBrackets[6]++;
			fmWCBrackets[6] += srcWC;
		}
		else if (( fmScore1to2 >= 70 ) && ( fmScore1to2 < 80 )) {
			fmBrackets[7]++;
			fmWCBrackets[7] += srcWC;
		}
		else if (( fmScore1to2 >= 80 ) && ( fmScore1to2 < 90 )) {
			fmBrackets[8]++;
			fmWCBrackets[8] += srcWC;
		}
		else if (( fmScore1to2 >= 90 ) && ( fmScore1to2 < 100 )) {
			fmBrackets[9]++;
			fmWCBrackets[9] += srcWC;
		}
		else if ( fmScore1to2 >= 100 ) { // 100
			fmBrackets[10]++;
			fmWCBrackets[10] += srcWC;
		}

		itemCount++;

		// Output in HTML
		if ( params.isGenerateHTML() ) {
			writer.writeRawXML("<tr><td class='p'>"); //$NON-NLS-1$
			// Output source if we have one
			if ( srcFrag != null ) {
				writer.writeString("Src:");
				writer.writeRawXML("</td>"); //$NON-NLS-1$
				writer.writeRawXML("<td class='p'>"); //$NON-NLS-1$
				fmt.setContent(srcFrag);
				writer.writeString(fmt.toString(!params.getGenericCodes()));
				
				writer.writeRawXML("</td></tr>\n"); //$NON-NLS-1$
				writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
			}
			writer.writeString(params.getDocument1Label()+":");
			writer.writeRawXML("</td>"); //$NON-NLS-1$
			if ( srcFrag != null ) writer.writeRawXML("<td>"); //$NON-NLS-1$
			else writer.writeRawXML("<td class='p'>"); //$NON-NLS-1$
			fmt.setContent(trgFrag1);
			writer.writeString(fmt.toString(!params.getGenericCodes()));
			writer.writeRawXML("</td></tr>"); //$NON-NLS-1$
			// T2
			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
			writer.writeString(params.getDocument2Label()+":");
			writer.writeRawXML("</td><td>"); //$NON-NLS-1$
			fmt.setContent(trgFrag2);
			writer.writeString(fmt.toString(!params.getGenericCodes()));
			writer.writeRawXML("</td></tr>"); //$NON-NLS-1$
			// T3
			if ( filter3 != null ) {
				writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
				writer.writeString(params.getDocument3Label()+":");
				writer.writeRawXML("</td><td>"); //$NON-NLS-1$
				fmt.setContent(trgFrag3);
				writer.writeString(fmt.toString(!params.getGenericCodes()));
				writer.writeRawXML("</td></tr>"); //$NON-NLS-1$
			}
			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
			writer.writeString("ED-Score:");
			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
			writer.writeString(String.format("%s to %s = %d",
				params.getDocument1Label(), params.getDocument2Label(), edScore1to2));
			if ( edScore1to3 > -1 ) {
				writer.writeString(String.format(",  %s to %s = %d",
					params.getDocument1Label(), params.getDocument3Label(), edScore1to3));
				writer.writeString(String.format(",  %s to %s = %d",
					params.getDocument2Label(), params.getDocument3Label(), edScore2to3));
			}
			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$
			
			writer.writeRawXML("<tr><td>"); //$NON-NLS-1$
			writer.writeString("FM-Score:");
			writer.writeRawXML("</td><td><b>"); //$NON-NLS-1$
			writer.writeString(String.format("%s to %s = %d",
				params.getDocument1Label(), params.getDocument2Label(), fmScore1to2));
			if ( edScore1to3 > -1 ) {
				writer.writeString(String.format(",  %s to %s = %d",
					params.getDocument1Label(), params.getDocument3Label(), fmScore1to3));
				writer.writeString(String.format(",  %s to %s = %d",
					params.getDocument2Label(), params.getDocument3Label(), fmScore2to3));
			}
			writer.writeRawXML("</b></td></tr>\n"); //$NON-NLS-1$

			if (sma != null) {
				writer.writeRawXML("<tr><td>Src Word Count:</td><td><b>");
				writer.writeString(String.format("%d", srcWC));
				writer.writeRawXML("</b></td></tr>\n");
			}
		}
		
		if ( prnWriter != null ) {
			prnWriter.println(String.format("%d\t%s\t%d", edScore1to2, fmScore1to2, srcWC));
		}
		
		if ( params.isGenerateTMX() ) {
			ITextUnit tmxTu = new TextUnit(tu1.getId());
			// Set the source: Use the tu1 if possible
			if ( isBaseMultilingual ) tmxTu.setSource(tu1.getSource());
			else if ( srcFrag != null ) {
				// Otherwise at least try to use the content of tu2
				tmxTu.setSourceContent(srcFrag);
			}
			tmxTu.setTargetContent(targetLocale, trgFrag1);
			tmxTu.setTargetContent(targetLocale2Extra, trgFrag2);
			deScore1to2Prop.setValue(String.format("%03d", edScore1to2));
			fmScore1to2Prop.setValue(String.format("%03d", fmScore1to2));
			tmxTu.setTargetProperty(targetLocale2Extra, deScore1to2Prop);
			tmxTu.setTargetProperty(targetLocale2Extra, fmScore1to2Prop);						
			if ( filter3 != null ) {
				tmxTu.setTargetContent(targetLocale3Extra, trgFrag3);
				deScore1to3Prop.setValue(String.format("%03d", edScore1to3));
				fmScore1to3Prop.setValue(String.format("%03d", fmScore1to3));
				tmxTu.setTargetProperty(targetLocale3Extra, deScore1to3Prop);
				tmxTu.setTargetProperty(targetLocale3Extra, fmScore1to3Prop);
			}
			tmx.writeTUFull(tmxTu);
		}
		
		return event1;
	}

    private String getOutputFilename(){
        return inputURI.getPath() + ".html"; //$NON-NLS-1$
     }

    private String getStatsFilename(){
        return inputURI.getPath() + ".txt"; //$NON-NLS-1$
     }

	private void initializeDocumentData () {
		// Initialize the filter to read the translation 1 to compare
		filter2 = fcMapper.createFilter(rawDoc2.getFilterConfigId(), filter2);
		// Open the second input for this batch item
		filter2.open(rawDoc2);

		if ( rawDoc3 != null ) {
			// Initialize the filter to read the translation 2 to compare
			filter3 = fcMapper.createFilter(
				rawDoc3.getFilterConfigId(), filter3);
			// Open the third input for this batch item
			filter3.open(rawDoc3);
		}
		
		baseDocumentPath = inputURI.getPath();
		compDocumentPath = rawDoc2.getInputURI().getPath();
			
		// Start HTML output
		if ( writer != null ) writer.close();
		if ( prnWriter != null ) prnWriter.close();
		
		if ( params.isGenerateHTML() ) {
			// Use the to-compare file for the output name
			if ( pathToOpen == null ) {
				pathToOpen = getOutputFilename();
			}
			writer = new XMLWriter(getOutputFilename()); //$NON-NLS-1$
			writer.writeStartDocument();
			writer.writeStartElement("html"); //$NON-NLS-1$
			writer.writeStartElement("head"); //$NON-NLS-1$
			writer.writeRawXML("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"); //$NON-NLS-1$
			writer.writeRawXML("<style>td { font-family: monospace } td { vertical-align: top; white-space: pre } td.p { border-top-style: solid; border-top-width: 1px;}</style>"); //$NON-NLS-1$
			writer.writeEndElement(); // head
			writer.writeStartElement("body"); //$NON-NLS-1$
			writer.writeStartElement("h1"); //$NON-NLS-1$
			writer.writeString("Translation Comparison");
			writer.writeEndElement();
			writer.writeStartElement("p"); //$NON-NLS-1$
			writer.writeString(String.format("Base document (%s): %s",
				params.getDocument1Label(), baseDocumentPath));
			writer.writeRawXML("<br>");
			writer.writeString(String.format("Comparison 1 (%s): %s",
				params.getDocument2Label(), compDocumentPath));
			if ( rawDoc3 != null ) {
				writer.writeRawXML("<br>");
				writer.writeString(String.format("Comparison 2 (%s): %s",
					params.getDocument3Label(), rawDoc3.getInputURI().getPath()));
			}
			writer.writeString(".");
			writer.writeEndElement();
			writer.writeRawXML("<p>ED-Score = Edit distance score, FM-Score = Fuzzy Match score.<br/>For details see: "
				+"<a href='"+WIKIURL+"'>"+WIKIURL+"</a>.</p>");
			writer.writeRawXML("<p><a href='#summary'>Go to Summary</a></p>");
			writer.writeStartElement("table"); //$NON-NLS-1$
			
			// Starts file
    		// Open the tab-delimited log file
			try {
				prnWriter = new PrintWriter(
					new BufferedWriter(
						new OutputStreamWriter(
								new FileOutputStream(getStatsFilename()),
							"UTF-8")));
			}
			catch ( Throwable e) {
				logger.error("Cannot create the file '{}'\n"+e.getMessage(), getStatsFilename());
			}
			prnWriter.println("ED-Score\tFM-Score\tWordCount");

		}
	}

	private Event synchronize (IFilter filter,
		EventType untilType)
	{
		boolean found = false;
		Event event = null;
		while ( !found && filter.hasNext() ) {
			event = filter.next();
			found = (event.getEventType() == untilType);
    	}
   		if ( !found ) {
    		throw new OkapiException("The document to compare is de-synchronized.");
    	}
   		return event;
	}
	
}
