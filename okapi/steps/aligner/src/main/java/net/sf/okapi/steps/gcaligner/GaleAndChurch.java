/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */

/*===========================================================================
 Additional changes Copyright (C) 2009 by the Okapi Framework contributors
 ===========================================================================*/

package net.sf.okapi.steps.gcaligner;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.exceptions.OkapiException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GaleAndChurch<T> implements AlignmentScorer<T> {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private final static int BIG_DISTANCE = 2500;

	// average number of target characters per source character for
	// the source-target pair
	private double m_charDist;

	/**
	 * Set source and target locales.
	 * 
	 * @param p_sourceLocale
	 *            Source locale
	 * @param p_targetLocale
	 *            Target locale
	 */
	public void setLocales(LocaleId p_sourceLocale, LocaleId p_targetLocale) {
		m_charDist = getCharacterDistribution(p_sourceLocale, p_targetLocale);
		LOGGER.trace("Character Distribution = {}", m_charDist);
	}

	/**
	 * Calculate the cost of substitution of source segment by target segment.
	 * 
	 * @param p_sourceTuv
	 *            Source TUV. Source is in X sequence in the DP map.
	 * @param p_targetTuv
	 *            Target TUV. Target is in Y sequence in the DP map.
	 * @return cost of the substitution
	 */
	public int substitutionScore(T p_sourceTuv, T p_targetTuv) {
		// TODO: remove codes to get length
		int sourceLength = p_sourceTuv.toString().length();
		int targetLength = p_targetTuv.toString().length();

		return match(sourceLength, targetLength);
	}

	/**
	 * Calculate the cost of deletion of source segment.
	 * 
	 * @param p_sourceTuv
	 *            Source TUV. Source is in X sequence in the DP map.
	 * @return cost of the deletion
	 */
	public int deletionScore(T p_sourceTuv) {
		// TODO: remove codes to get length
		int sourceLength = p_sourceTuv.toString().length();

		return match(sourceLength, 0);
	}

	/**
	 * Calculate the cost of insertion of target segment.
	 * 
	 * @param p_targetTuv
	 *            Target TUV. Target is in Y sequence in the DP map.
	 * @return cost of the insertion
	 */
	public int insertionScore(T p_targetTuv) {
		// TODO: remove codes to get length
		int targetLength = p_targetTuv.toString().length();
		return match(0, targetLength);
	}

	/**
	 * Calculate the cost of contracting two source segments to one target segment.
	 * 
	 * @param p_sourceTuv1
	 *            Source TUV1. Source is in X sequence in the DP map.
	 * @param p_sourceTuv2
	 *            Source TUV2. Source is in X sequence in the DP map.
	 * @param p_targetTuv
	 *            Target TUV. Target is in Y sequence in the DP map.
	 * @return cost of the contraction
	 */
	public int contractionScore(T p_sourceTuv1, T p_sourceTuv2, T p_targetTuv) {
		// TODO: remove codes to get length
		int sourceLength1 = p_sourceTuv1.toString().length();
		int sourceLength2 = p_sourceTuv2.toString().length();
		int targetLength = p_targetTuv.toString().length();

		return match(sourceLength1 + sourceLength2, targetLength);
	}

	/**
	 * Calculate the cost of expanding one source segment to two target segments.
	 * 
	 * @param p_sourceTuv
	 *            Source TUV. Source is in X sequence in the DP map.
	 * @param p_targetTuv1
	 *            Target TUV1. Target is in Y sequence in the DP map.
	 * @param p_targetTuv2
	 *            Target TUV2. Target is in Y sequence in the DP map.
	 * @return cost of the expansion
	 */
	public int expansionScore(T p_sourceTuv, T p_targetTuv1, T p_targetTuv2) {
		// TODO: remove codes to get length
		int sourceLength = p_sourceTuv.toString().length();
		int targetLength1 = p_targetTuv1.toString().length();
		int targetLength2 = p_targetTuv2.toString().length();

		return match(sourceLength, targetLength1 + targetLength2);
	}

	/**
	 * Calculate the cost of melding of two source segments to two target segments.
	 * 
	 * @param p_sourceTuv1
	 *            Source TUV1. Source is in X sequence in the DP map.
	 * @param p_sourceTuv2
	 *            Source TUV2. Source is in X sequence in the DP map.
	 * @param p_targetTuv1
	 *            Target TUV1. Target is in Y sequence in the DP map.
	 * @param p_targetTuv2
	 *            Target TUV2. Target is in Y sequence in the DP map.
	 * @return cost of the melding
	 */
	public int meldingScore(T p_sourceTuv1, T p_sourceTuv2, T p_targetTuv1, T p_targetTuv2) {
		// TODO: remove codes to get length
		int sourceLength1 = p_sourceTuv1.toString().length();
		int sourceLength2 = p_sourceTuv2.toString().length();
		int targetLength1 = p_targetTuv1.toString().length();
		int targetLength2 = p_targetTuv2.toString().length();

		return match(sourceLength1 + sourceLength2, targetLength1 + targetLength2);
	}

	/**
	 * Returns the area under a normal distribution from -inf to z standard deviations
	 */
	private double pnorm(double z) {
		double t, pd;
		t = 1 / (1 + 0.2316419 * z);
		pd = 1 - 0.3989423
				* Math.exp(-z * z / 2)
				* ((((1.330274429 * t - 1.821255978) * t + 1.781477937) * t - 0.356563782) * t + 0.319381530)
				* t;
		/* see Gradsteyn & Rhyzik, 26.2.17 p932 */
		return (pd);
	}

	/**
	 * Return -100 * log probability that an source sentence of length len1 is a translation of a foreign sentence of
	 * length len2. The probability is based on two parameters, the mean and variance of number of foreign characters
	 * per source character.
	 * 
	 * Gale and Church hardcoded foreign_chars_per_eng_char as 1. It apparently works OK for European language
	 * alignment. We take the coefficient as a parameter so that non European languages can be aligned as well.
	 * */

	public int match(int len1, int len2) {
		/* variance per english character */
		/* May need tweak for the other languages */
		double var_per_eng_char = 6.8;

		if (len1 == 0 && len2 == 0)
			return (0);

		double mean = (len1 + len2 / m_charDist) / 2;
		double z = (m_charDist * len1 - len2) / Math.sqrt(var_per_eng_char * mean);

		/* Need to deal with both sides of the normal distribution */
		if (z < 0)
			z = -z;

		double pd = 2 * (1 - pnorm(z));

		if (pd > 0)
			return ((int) (-100 * Math.log(pd)));
		else
			return (BIG_DISTANCE);
	}

	/**
	 * Return the probability that an source sentence of length len1 is a translation of a foreign sentence of
	 * length len2. The probability is based on two parameters, the mean and variance of number of foreign characters
	 * per source character.
	 * 
	 * Gale and Church hardcoded foreign_chars_per_eng_char as 1. It apparently works OK for European language
	 * alignment. We take the coefficient as a parameter so that non European languages can be aligned as well.
	 * */

	public double prob(int len1, int len2) {
		/* variance per english character */
		/* May need tweak for the other languages */
		double var_per_eng_char = 6.8;

		if (len1 == 0 && len2 == 0)
			return (0);

		double mean = (len1 + len2 / m_charDist) / 2;
		double z = (m_charDist * len1 - len2) / Math.sqrt(var_per_eng_char * mean);

		/* Need to deal with both sides of the normal distribution */
		if (z < 0)
			z = -z;

		double pd = 2 * (1 - pnorm(z));

		return pd;
	}
	
	private double getCharacterDistribution(LocaleId p_sourceLocale, LocaleId p_targetLocale) {
		double charDist = 1;

		try {
			double srcCharDist = getCharDistProperty(p_sourceLocale);
			double trgCharDist = getCharDistProperty(p_targetLocale);

			charDist = trgCharDist / srcCharDist;
		} catch (Exception e) {
			throw new OkapiException(e);
		}

		return charDist;
	}

	private double getCharDistProperty(LocaleId p_locale) throws Exception {
		String lang = p_locale.getLanguage();
		String locale = p_locale.toString();

		ResourceBundle res = ResourceBundle.getBundle("net/sf/okapi/steps/gcaligner/CharDist");

		String charDistStr = null;
		try {
			charDistStr = res.getString(lang);
		} catch (MissingResourceException e) {
			try {
				charDistStr = res.getString(locale);
			} catch (MissingResourceException e2) {
				charDistStr = "1";
			}
		}

		return Double.parseDouble(charDistStr);
	}
}
