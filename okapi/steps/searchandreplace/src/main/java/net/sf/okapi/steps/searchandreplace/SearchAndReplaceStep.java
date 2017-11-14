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

package net.sf.okapi.steps.searchandreplace;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.okapi.common.BOMNewlineEncodingDetector;
import net.sf.okapi.common.Event;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.UsingParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.pipeline.BasePipelineStep;
import net.sf.okapi.common.pipeline.annotations.StepParameterMapping;
import net.sf.okapi.common.pipeline.annotations.StepParameterType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.TextContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This step performs search and replace actions on either the text units or the full content of input documents. Source
 * and/or target content can be searched and replaced.
 * 
 * Takes: Raw document or Filter events. Sends: same as the input.
 * 
 * The step provides a way to define a list of search entries and corresponding replacements. You can use regular
 * expressions if needed.
 * 
 * The step can take as input either a raw document or filter events.
 * 
 * If the step receives filter events, the search and replace is done on the content of the text units, and the step
 * sends updated filter events to the next step. If the step receives a raw document, the search and replace is done on
 * the whole file, and the step sends an updated raw document to the next step. Note that in this case, the raw document
 * must be in some text-based file format for the search and replace to work: The document is seen exactly like it would
 * be in a text editor (no conversion of escaped characters is done for example).
 * 
 * @author Fredrik L.
 * @author Yves S.
 * @author HargraveJE
 * 
 */
@UsingParameters(Parameters.class)
public class SearchAndReplaceStep extends BasePipelineStep {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private Parameters params;
	private Matcher matcher;
	private Pattern patterns[];
	private URI outputURI;
	private LocaleId targetLocale;
	private String search[];
	private String replace[];
	private int sourceCounts[];
	private int targetCounts[];
	List<String[]> replacementWords;
	private int sourceReplacementCounts[];
	private int targetReplacementCounts[];
	private ProcType procType;

	private String rootDir;
	private String inputRootDir;
	
	public enum ProcType {
		UNSPECIFIED, PLAINTEXT, FILTER;
	}
	
	public enum TargetType {
		SOURCE, TARGET, ALL;
	}

	private boolean firstEventDone = false;

	@Override
	public void destroy() {
		// Nothing to do
	}

	public SearchAndReplaceStep() {
		params = new Parameters();
	}

	@StepParameterMapping(parameterType = StepParameterType.ROOT_DIRECTORY)
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}

	@StepParameterMapping(parameterType = StepParameterType.INPUT_ROOT_DIRECTORY)
	public void setInputRootDirectory (String inputRootDir) {
		this.inputRootDir = inputRootDir;
	}
	
	@StepParameterMapping(parameterType = StepParameterType.OUTPUT_URI)
	public void setOutputURI(URI outputURI) {
		this.outputURI = outputURI;
	}

	@SuppressWarnings("deprecation")
	@StepParameterMapping(parameterType = StepParameterType.TARGET_LOCALE)
	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	public String getDescription() {
		return "Performs search and replace on the entire file or the text units. "
				+ "Expects raw document or filter events. Sends back: raw document or filter events.";
	}

	public String getName() {
		return "Search and Replace";
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters(IParameters params) {
		this.params = (Parameters) params;
	}

	@Override
	protected Event handleStartBatch (Event event) {
		// Compile the search patterns
		// As regex patterns if regex is on
		if ( params.getRegEx() ) {
			int flags = 0;
			patterns = new Pattern[params.rules.size()];
			if ( params.getDotAll() ) flags |= Pattern.DOTALL;
			if ( params.getIgnoreCase() ) flags |= Pattern.CASE_INSENSITIVE;
			if ( params.getMultiLine() ) flags |= Pattern.MULTILINE;
			for (int i = 0; i < params.rules.size(); i++) {
				String s[] = params.rules.get(i);
				if (params.getRegEx()) {
					patterns[i] = Pattern.compile(s[1], flags);
				}
			}
		}
		else {
			// As normal search if regex is off
			search = new String[params.rules.size()];
			for (int i = 0; i < params.rules.size(); i++) {
				search[i] = unescape(params.rules.get(i)[1], false);
			}
		}

		// Compile the replacement strings
		replace = new String[params.rules.size()];
		sourceCounts = new int[params.rules.size()];
		targetCounts = new int[params.rules.size()];
		for (int i = 0; i < params.rules.size(); i++) {
			replace[i] = unescape(params.rules.get(i)[2], params.getRegEx());
		}

		//--load alternative replacements--
		if ( !isEmpty(params.getReplacementsPath()) ) {
			String finalPath = Util.fillRootDirectoryVariable(params.getReplacementsPath(), rootDir);
			finalPath = Util.fillInputRootDirectoryVariable(finalPath, inputRootDir);
			replacementWords = loadList(finalPath);
		}
		else {
			replacementWords = Collections.emptyList();
		}

		//--initialize the replacement counts--
		sourceReplacementCounts = new int[replacementWords.size()];
		targetReplacementCounts = new int[replacementWords.size()];
		
		return event;
	}
	
	@Override
	protected Event handleEndBatch (Event event) {
		StringBuffer sb = new StringBuffer();
		if ( procType == ProcType.FILTER ) {
			sb.append("Search and Replace in text units:\r\n");
		}
		else {
			sb.append("Search and Replace in file:\r\n");
		}
		if ( procType == ProcType.FILTER ) {
			if ( params.getSource() ) {
				//--source results--
				sb.append("\r\nSource Results:\r\n");
				sb.append(sourceResults());
			}
			if ( params.getTarget() ) {
				//--target results--
				sb.append("\r\nTarget Results:\r\n");
				sb.append(targetResults());
			}
		}
		else {
			//--target results if filter and target--
			sb.append("\r\nResults:\r\n");
			sb.append(targetResults());
		}
	
		logger.info(sb.toString());
		
		if ( params.getSaveLog() ){
			generateReport(sb.toString());
		}
		
		return event;		
	}

	private String sourceResults () {
		StringBuffer sb = new StringBuffer();
		if ( params.getRegEx() ) {
			sb.append("\r\nRegEx:\r\n");
			for ( int i=0; i<patterns.length; i++ ) {
				sb.append(String.format("# of replacements for '%s': %s\r\n", patterns[i], sourceCounts[i]));
			}
		}
		else {
			sb.append("\r\nNon-RegEx:\r\n");
			for ( int i=0; i<search.length; i++ ) {
				sb.append(String.format("# of replacements for '%s': %s\r\n", search[i], sourceCounts[i]));
			}
		}
		int replacementIndex = 0;
		sb.append("\r\nNon-RegEx from file:\r\n");
		for ( String[] values : replacementWords ) {					
			sb.append(String.format("# of replacements for '%s': %s\r\n", values[0], sourceReplacementCounts[replacementIndex]));
			replacementIndex++;
		}
		return sb.toString();
	}
	
	private String targetResults () {
		StringBuffer sb = new StringBuffer();
		if ( params.getRegEx() ) {
			sb.append("\r\nRegEx:\r\n");
			for ( int i=0; i<patterns.length; i++ ) {
				sb.append(String.format("# of replacements for '%s': %s\r\n", patterns[i], targetCounts[i]));
			}
		}
		else {
			sb.append("\r\nNon-RegEx:\r\n");
			for ( int i=0; i<search.length; i++ ) {
				sb.append(String.format("# of replacements for '%s': %s\r\n", search[i], targetCounts[i]));
			}
		}
		int replacementIndex = 0;
		sb.append("\r\nNon-RegEx from file:\r\n");
		for ( String[] values : replacementWords ) {					
			sb.append(String.format("# of replacements for '%s': %s\r\n", values[0], targetReplacementCounts[replacementIndex]));
			replacementIndex++;
		}
		return sb.toString();
	}

	private void generateReport (String text) {
		// Output the report
		PrintWriter writer = null;
		try {
			String finalPath = Util.fillRootDirectoryVariable(params.getLogPath(), rootDir);
			finalPath = Util.fillInputRootDirectoryVariable(finalPath, inputRootDir);
			Util.createDirectories(finalPath);
			writer = new PrintWriter(finalPath, "UTF-8");
			writer.println(text);
		}
		catch ( IOException e ) {
			throw new OkapiException("Error when writing output file.", e);
		}
		finally {
			if ( writer != null ) {
				writer.close();
				writer = null;
			}
		}
	}
	
	@Override
	protected Event handleRawDocument (Event event) {
		// --first event determines processing type--
		if ( !firstEventDone ) {			
			firstEventDone = true;
		}

		//--set proctype--
		procType = ProcType.PLAINTEXT;
		
		RawDocument rawDoc;
		String encoding = null;
		BufferedReader reader = null;
		BufferedWriter writer = null;

		String result = null;
		StringBuilder assembled = new StringBuilder();

		try {
			rawDoc = event.getRawDocument();

			// Detect the BOM (and the encoding) if possible
			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(
					rawDoc.getStream(), rawDoc.getEncoding());
			detector.detectAndRemoveBom();
			encoding = detector.getEncoding();
			// Create the reader from the BOM-aware stream, with the possibly new encoding
			reader = new BufferedReader(new InputStreamReader(detector.getInputStream(), encoding));

			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				assembled.append(buf, 0, numRead);
			}
			reader.close(); reader = null;
			result = assembled.toString();
			assembled = null;

			// Open the output
			File outFile;
			if ( isLastOutputStep() ) {
				// No need to use a temporary file as this step work with its whole content in memory
				outFile = new File(outputURI);
				Util.createDirectories(outFile.getAbsolutePath());
			}
			else {
				try {
					outFile = File.createTempFile("~okapi-50_okp-snr_", ".tmp");
				} catch (Throwable e) {
					throw new OkapiIOException("Cannot create temporary output.", e);
				}
			}

			result = searchAndReplace(result, TargetType.ALL);

			writer = new BufferedWriter(
				new OutputStreamWriter(
					new FileOutputStream(outFile), encoding));
			Util.writeBOMIfNeeded(writer, detector.hasUtf8Bom(), encoding);
			writer.write(result);
			writer.close(); writer = null;

			event.setResource(new RawDocument(outFile.toURI(), encoding,
				rawDoc.getSourceLocale(), rawDoc.getTargetLocale()));
		}
		catch ( FileNotFoundException e ) {
			throw new OkapiException(e);
		}
		catch ( IOException e ) {
			throw new OkapiException(e);
		}
		finally {			
			try {
				if ( writer != null ) {
					writer.close();
					writer = null;
				}
				if ( reader != null ) {
					reader.close();
					reader = null;
				}
			}
			catch ( IOException e ) {
				throw new OkapiException(e);
			}
		}

		return event;
	}

	/** Un-escapes Unicode escape sequences and other special constructs.
	 * @param s The string to un-escape
	 * @param isRegex true if the expression is to be used with either a search or a replace using 
	 * the regular expressions mode on. If this is true, only \n, \r, \t, (backslash)uHHHH and \N
	 * should be un-escape to leave the other constructs intact.
	 * @return the un-escaped string.
	 */
	private String unescape (String s,
		boolean isRegex)
	{		
		int i=0, len=s.length();
		char c;
		StringBuffer sb = new StringBuffer(len);
		while ( i < len ) {
			c = s.charAt(i++);
			if ( c == '\\' ) {
				if ( i < len ) {
					c = s.charAt(i++);
					switch ( c ) {
					case 'u':
						c = (char)Integer.parseInt(s.substring(i, i+4), 16);
						sb.append(c);
						i += 4;
						continue;
					case 'N':
						sb.append(System.getProperty("line.separator"));
						continue;
					case 'n':
						sb.append('\n');
						continue;
					case 'r':
						sb.append('\r');
						continue;
					case 't':
						sb.append('\t');
						break;
					default:
						if ( isRegex ) {
							// Leave the sequence as it
							sb.append('\\');
						}
						// Else: skip the backslash,
						// Fall back to append the escaped character
					}
				}
			}
			// Fall back for all cases without 'continue'
			sb.append(c);
		}
		return sb.toString();
	}

	@Override
	protected Event handleTextUnit (Event event) {

		// --first event determines processing type--
		if ( !firstEventDone ) {			
			firstEventDone = true;
		}
		//--set proctype--
		procType = ProcType.FILTER;
		
		ITextUnit tu = event.getTextUnit();
		// Skip non-translatable
		if ( !tu.isTranslatable() ) {
			return event;
		}

		String tmp = null;
		try {
			
			// search and replace on source
			if ( params.getSource() ) {
				TextContainer tc = tu.getSource();
				for ( Segment seg : tc.getSegments() ) {
					tmp = searchAndReplace(seg.text.toString(), TargetType.SOURCE);
					seg.text.setCodedText(tmp);
				}
			}
			
			// search and replace on target
			if ( params.getTarget() ) {							
				TextContainer tc = tu.getTarget(targetLocale);
				if ( tc != null ) {
					for ( Segment seg : tc.getSegments() ) {
						tmp = searchAndReplace(seg.text.toString(), TargetType.TARGET);
						seg.text.setCodedText(tmp);
					}		
				}
			}
		} 
		catch ( Exception e ) {
			logger.warn("Error when updating content: '{}'.\n{}", tmp, e.getMessage(), e);
		}

		return event;
	}

	private String searchAndReplace (String result,
		TargetType targetType)
	{
		if ( params.getRegEx() ) {
			for ( int i=0; i<params.rules.size(); i++ ) {
				String s[] = params.rules.get(i);
				if ( s[0].equals("true") ) {
					//--count the matches--
					int matches = countRegExMatches(result, patterns[i]);
					//--replace matches--
					matcher = patterns[i].matcher(result);
					if (params.getReplaceAll()) {
						result = matcher.replaceAll(replace[i]);
					} else {
						if (matches > 0){
							matches = 1;
							result = matcher.replaceFirst(replace[i]);
						}
					}
					if(targetType == TargetType.SOURCE){
						sourceCounts[i] += matches;						
					}else{
						targetCounts[i] += matches;
					}
				}
			}
		}
		else {
			for ( int i=0; i<params.rules.size(); i++ ) {
				if ( params.rules.get(i)[0].equals("true") ) {
					//--count the matches--
					int matches = countMatches(result, search[i]);
					if(targetType == TargetType.SOURCE){
						sourceCounts[i] += matches;						
					}else{
						targetCounts[i] += matches;
					}
					//--replace matches--
					result = result.replace(search[i], replace[i]);
				}
			}
		}
		
		//--Run replacements from replacements file--
		if ( replacementWords != null ) {
			int replacementIndex = 0;
			for ( String[] values : replacementWords ) {					
				//--count the matches--
				int matches = countMatches(result, values[0]);
				if ( targetType == TargetType.SOURCE ) {
					sourceReplacementCounts[replacementIndex] += matches;						
				}
				else {
					targetReplacementCounts[replacementIndex] += matches;
				}
				
				replacementIndex++;
				//--replace matches--
				result = result.replaceAll(values[0], values[1]);					
			}
		}

		return result;
	}
	
	List<String[]> loadList (String path)
	{
		ArrayList<String[]> map = new ArrayList<String[]>();
		BufferedReader reader = null;
		try {
			InputStream is = new FileInputStream(path);

			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String line;
			while ((line = reader.readLine()) != null ) {
				String test = line.trim();
				if ( test.length() == 0 ) continue;
				if ( test.charAt(0) == '#' ) continue;
				// Add the word to the list, make sure we skip duplicate to avoid error
				String[] values = line.split("\\t", -1);
				if(values != null && values.length >=2){
					map.add(values);
				}
			}
		}
		catch ( IOException e ) {
			throw new OkapiException("Error reading replacements list.", e);
		}
		finally {
			if ( reader != null ) {
				try {
					reader.close();
				}
				catch ( IOException e ) {
					throw new OkapiException("Error reading replacements list.", e);
				}
			}
		}
		return map;
	}
	
	public static int countRegExMatches(String str, Pattern p) {
		if (isEmpty(str)) {
			return 0;
		}
		Matcher matcher = p.matcher(str);
		int count = 0;
		while (matcher.find()) {
		    count++;
		}
		return count;
	}
	
	public static int countMatches(String str, String sub) {
		if (isEmpty(str) || isEmpty(sub)) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		while ((idx = str.indexOf(sub, idx)) != -1) {
			count++;
			idx += sub.length();
		}
		return count;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.length() == 0;
	}
}
