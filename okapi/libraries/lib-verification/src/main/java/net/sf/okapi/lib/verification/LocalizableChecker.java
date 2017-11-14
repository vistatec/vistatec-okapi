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

package net.sf.okapi.lib.verification;

import java.text.ParsePosition;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.annotation.IssueType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.NumberFormat;

public class LocalizableChecker extends AbstractChecker {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private class Localizable {
		public Localizable(Number number, String original, int position) {
			this(original, position);
			this.number = number;
		}

		public Localizable(Date date, String original, int position) {
			this(original, position);
			this.date = date;
		}

		private Localizable(String original, int position) {
			this.original = original;
			this.position = position;
		}

		Number number;
		Date date;
		String original;
		int position;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((date == null) ? 0 : date.hashCode());
			result = prime * result + ((number == null) ? 0 : number.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof Localizable)) {
				return false;
			}
			Localizable other = (Localizable) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (date == null) {
				if (other.date != null) {
					return false;
				}
			} else if (!date.equals(other.date)) {
				return false;
			}

			if (number == null) {
				if (other.number != null) {
					return false;
				}
			} else if (!number.equals(other.number)) {
				return false;
			}
			
			return true;
		}

		@Override
		public String toString() {
			return "Localizable [number=" + number + ", date=" + date + "]";
		}

		private LocalizableChecker getOuterType() {
			return LocalizableChecker.this;
		}
	}

	private NumberFormat sourceNumbers;
	private NumberFormat targetNumbers;
	
	// for dates months, days, years
	private DateFormat sourceShortDates;
	private DateFormat targetShortDates;
	private DateFormat sourceMediumDates;
	private DateFormat targetMediumDates;
	private DateFormat sourceLongDates;
	private DateFormat targetLongDates;
	private DateFormat sourceFullDates;
	private DateFormat targetFullDates;
	
	// for time hours, minutes, seconds
	private DateFormat sourceShortTime;
	private DateFormat targetShortTime;
	private DateFormat sourceMediumTime;
	private DateFormat targetMediumTime;
	private DateFormat sourceLongTime;
	private DateFormat targetLongTime;
	private DateFormat sourceFullTime;
	private DateFormat targetFullTime;
	
	@Override
	public void startProcess(LocaleId sourceLocale, LocaleId targetLocale, Parameters params, List<Issue> issues) {
		super.startProcess(sourceLocale, targetLocale, params, issues);
		sourceNumbers = NumberFormat.getInstance(sourceLocale.toJavaLocale());
		targetNumbers = NumberFormat.getInstance(targetLocale.toJavaLocale());
		
		sourceShortDates = DateFormat.getDateInstance(DateFormat.SHORT, sourceLocale.toJavaLocale());
		targetShortDates = DateFormat.getDateInstance(DateFormat.SHORT, targetLocale.toJavaLocale());
		sourceMediumDates = DateFormat.getDateInstance(DateFormat.MEDIUM, sourceLocale.toJavaLocale());
		targetMediumDates = DateFormat.getDateInstance(DateFormat.MEDIUM, targetLocale.toJavaLocale());
		sourceLongDates = DateFormat.getDateInstance(DateFormat.LONG, sourceLocale.toJavaLocale());
		targetLongDates = DateFormat.getDateInstance(DateFormat.LONG, targetLocale.toJavaLocale());
		sourceFullDates = DateFormat.getDateInstance(DateFormat.FULL, sourceLocale.toJavaLocale());
		targetFullDates = DateFormat.getDateInstance(DateFormat.FULL, targetLocale.toJavaLocale());
		
		sourceShortTime = DateFormat.getTimeInstance(DateFormat.SHORT, sourceLocale.toJavaLocale());
		targetShortTime = DateFormat.getTimeInstance(DateFormat.SHORT, targetLocale.toJavaLocale());
		sourceMediumTime = DateFormat.getTimeInstance(DateFormat.MEDIUM, sourceLocale.toJavaLocale());
		targetMediumTime = DateFormat.getTimeInstance(DateFormat.MEDIUM, targetLocale.toJavaLocale());
		sourceLongTime = DateFormat.getTimeInstance(DateFormat.LONG, sourceLocale.toJavaLocale());
		targetLongTime = DateFormat.getTimeInstance(DateFormat.LONG, targetLocale.toJavaLocale());
		sourceFullTime = DateFormat.getTimeInstance(DateFormat.FULL, sourceLocale.toJavaLocale());
		targetFullTime = DateFormat.getTimeInstance(DateFormat.FULL, targetLocale.toJavaLocale());
	}

	@Override
	public void processStartDocument(StartDocument sd, List<String> sigList) {
		super.processStartDocument(sd, sigList);
	}

	@Override
	public void processStartSubDocument(StartSubDocument ssd) {
		super.processStartSubDocument(ssd);
	}

	@Override
	public void processTextUnit(ITextUnit tu) {
		// Skip non-translatable entries
		if (!tu.isTranslatable()) {
			return;
		}

		// Get the containers
		TextContainer srcCont = tu.getSource();
		TextContainer trgCont = tu.getTarget(getTrgLoc());

		// Check if we have a target and the source or target has text
		if (trgCont == null || !srcCont.hasText() || !trgCont.hasText()) {
			return;
		}

		String srcOri = srcCont.getUnSegmentedContentCopy().getText();
		String trgOri = trgCont.getUnSegmentedContentCopy().getText();
		
		try {
			// trim whitespace to avoid IndexOutOfBounds exception from ICU (bug?)
			checkLocalizables(srcOri.trim(), trgOri.trim(), tu, trgCont);
		} catch (Exception e) {
			LOGGER.error("Error in checkLocalizables: {}", srcOri);
			throw e;
		}
		setAnnotationIds(srcCont, trgCont);
	}

	// parse dates, times and numbers and log differences
	private void checkLocalizables(String srcOri, String trgOri, ITextUnit tu, TextContainer trgCont) {
		// dates
		Set<Localizable> srcDates = findDates(srcOri, sourceFullDates);
		Set<Localizable> trgDates = findDates(trgOri, targetFullDates);
		
		srcDates.addAll(findDates(srcOri, sourceLongDates));
		trgDates.addAll(findDates(trgOri, targetLongDates));
		
		srcDates.addAll(findDates(srcOri, sourceMediumDates));
		trgDates.addAll(findDates(trgOri, targetMediumDates));
		
		srcDates.addAll(findDates(srcOri, sourceShortDates));
		trgDates.addAll(findDates(trgOri, targetShortDates));
		
		// check that our source and target sets have the same elements
		// if not look for the differences
		if (!srcDates.equals(trgDates)) {
			for (Localizable loc : srcDates) {
				if (!trgDates.contains(loc)) {
					logIssue(IssueType.SUSPECT_DATE_TIME, tu, loc, trgCont);
				}
			}
		}
		
		// times
		Set<Localizable> srcTimes = findTimes(srcOri, sourceFullTime, srcDates);
		Set<Localizable> trgTimes = findTimes(trgOri, targetFullTime, trgDates);
		
		srcTimes.addAll(findTimes(srcOri, sourceLongTime, srcDates));
		trgTimes.addAll(findTimes(trgOri, targetLongTime, trgDates));
		
		srcTimes.addAll(findTimes(srcOri, sourceMediumTime, srcDates));
		trgTimes.addAll(findTimes(trgOri, targetMediumTime, trgDates));
		
		srcTimes.addAll(findTimes(srcOri, sourceShortTime, srcDates));
		trgTimes.addAll(findTimes(trgOri, targetShortTime, trgDates));
		
		// check that our source and target sets have the same elements
		// if not look for the differences
		if (!srcTimes.equals(trgTimes)) {
			for (Localizable loc : srcTimes) {
				if (!trgTimes.contains(loc)) {
					logIssue(IssueType.SUSPECT_DATE_TIME, tu, loc, trgCont);
				}
			}
		}
		
		// standalone numbers: MUST BE PROCESSED LAST!!
		
		// add times to numbers to avoid these if already parsed
		Set<Localizable> srcNumbers = findNumbers(srcOri, sourceNumbers, srcDates, srcTimes);
		Set<Localizable> trgNumbers = findNumbers(trgOri, targetNumbers, trgDates, trgTimes);
		// check that our source and target sets have the same elements
		// if not look for the differences
		if (!srcNumbers.equals(trgNumbers)) {
			for (Localizable loc : srcNumbers) {
				if (!trgNumbers.contains(loc)) {
					logIssue(IssueType.SUSPECT_NUMBER, tu, loc, trgCont);
				}
			}
		}
	}
	
	private Set<Localizable> findDates(String text, DateFormat parser) {
		ParsePosition pos = new ParsePosition(0);
		Set<Localizable> dates = new HashSet<>();
		for (int i = 0; i < text.length() && pos.getIndex() < text.length(); i++) {
			if (Character.isWhitespace(text.charAt(i))) continue;
			pos.setIndex(i);
			Date d = parser.parse(text, pos);
			if (d != null) {
				dates.add(new Localizable(d, text.substring(i, pos.getIndex()), i));
				i = pos.getIndex();
			}
		}
		return dates;
	}
	
	private Set<Localizable> findTimes(String text, DateFormat parser, Set<Localizable> dates) {
		ParsePosition pos = new ParsePosition(0);
		Set<Localizable> times = new HashSet<>();
		for (int i = 0; i < text.length() && pos.getIndex() < text.length(); i++) {
			if (Character.isWhitespace(text.charAt(i)) || alreadyParsed(i, dates)) continue;
			pos.setIndex(i);
			Date d = parser.parse(text, pos);
			if (d != null) {
				times.add(new Localizable(d, text.substring(i, pos.getIndex()), i));
				i = pos.getIndex();
			}
		}
		return times;
	}
	
	// make sure numbers don't overlap with dates already found
	private Set<Localizable> findNumbers(String text, NumberFormat parser, Set<Localizable> dates, Set<Localizable> times) {
		ParsePosition pos = new ParsePosition(0);
		Set<Localizable> numbers = new HashSet<>();
		for (int i = 0; i < text.length() && pos.getIndex() < text.length(); i++) {
			if (Character.isWhitespace(text.charAt(i)) || alreadyParsed(i, dates) || alreadyParsed(i, times)) continue;
			pos.setIndex(i);
			Number n = parser.parse(text, pos);
			if (n != null) {
				numbers.add(new Localizable(n, text.substring(i, pos.getIndex()), i));
				i = pos.getIndex();
			}
		}
		return numbers;
	}
	
	private void logIssue(IssueType issueType, ITextUnit tu, Localizable loc, TextContainer tc) {
		addAnnotationAndReportIssue(issueType, tu, tc, null,
				String.format("Number, date or time may be missing or not properly localized: '%s'", loc.original), loc.position,
				loc.original.length()+loc.position, 0, -1, Issue.SEVERITY_MEDIUM, tu.getSource().toString(), tc.toString(), null);
	}
	
	private boolean alreadyParsed(int i, Set<Localizable> datesOrTimes) {
	    for (Localizable loc : datesOrTimes) {
			if (i >= loc.position && i <= loc.original.length()+loc.position) {
				return true;
			}
		}
	    return false;
	}
}
