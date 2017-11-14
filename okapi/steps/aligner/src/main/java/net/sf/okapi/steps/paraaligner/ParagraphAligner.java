/*  Copyright 2009 Welocalize, Inc. 
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
 Additional changes Copyright (C) 2009-2011 by the Okapi Framework contributors
 ===========================================================================*/

package net.sf.okapi.steps.paraaligner;

import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.steps.gcaligner.AlignmentFunction;
import net.sf.okapi.steps.gcaligner.AlignmentScorer;
import net.sf.okapi.steps.gcaligner.DpMatrix;
import net.sf.okapi.steps.gcaligner.DpMatrixCell;
import net.sf.okapi.steps.gcaligner.Penalties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SentenceAligner aligns source and target (paragraph) {@link TextUnit}s and returns a list of aligned sentence-based
 * {@link TextUnit} objects.
 */

public class ParagraphAligner {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	private static final long MAX_CELL_SIZE = 80000L;
	private List<AlignmentScorer<ITextUnit>> scorerList;
	
	public ParagraphAligner(List<AlignmentScorer<ITextUnit>> scorerList) {
		this.scorerList = scorerList;
	}

	public AlignedParagraphs align(List<ITextUnit> sourceParagraphs, List<ITextUnit> targetParagraphs, LocaleId srcLocale,
			LocaleId trgLocale, boolean outputOneTOneMatchesOnly) {
		return alignWithoutSkeletonAlignment(sourceParagraphs, targetParagraphs, srcLocale, trgLocale, outputOneTOneMatchesOnly);
	}

	private AlignedParagraphs alignWithoutSkeletonAlignment(List<ITextUnit> sourceParagraphs,
			List<ITextUnit> targetParagraphs, LocaleId srcLocale, LocaleId trgLocale, boolean outputOneTOneMatchesOnly) {
		AlignmentFunction<ITextUnit> alignmentFunction = new AlignmentFunction<ITextUnit>(srcLocale,
				trgLocale, scorerList, new Penalties());
		return alignSegments(sourceParagraphs, targetParagraphs, srcLocale, trgLocale,
				alignmentFunction, outputOneTOneMatchesOnly);
	}

	private AlignedParagraphs alignSegments(List<ITextUnit> sourceParagraphs, List<ITextUnit> targetParagraphs,
			LocaleId srcLocale, LocaleId trgLocale, AlignmentFunction<ITextUnit> alignmentFunction, boolean outputOneTOneMatchesOnly) {

		// To prevent OutOfMemory exception, simply don't perform the
		// alignment for a block with a lot of segments. TEMPORARY FIX
		if (sourceParagraphs.size()
				* targetParagraphs.size() > MAX_CELL_SIZE) {
			throw new IllegalArgumentException("Too many segments. Can only align "
					+ Long.toString(MAX_CELL_SIZE)
					+ ". Where the number equals the source segments times the target segments.");
		}

		DpMatrix<ITextUnit> matrix = new DpMatrix<ITextUnit>(sourceParagraphs, targetParagraphs, alignmentFunction);

		List<DpMatrixCell> result = matrix.align();
		AlignedParagraphs alignedParas = new AlignedParagraphs(trgLocale);
		
		Iterator<DpMatrixCell> it = result.iterator();
		while (it.hasNext()) {
			DpMatrixCell cell = it.next(); 
			
			if (outputOneTOneMatchesOnly) {
				if (cell.getState() == DpMatrixCell.MATCH) {
					ITextUnit sourcePara = matrix.getAlignmentElementX(cell.getXindex());
					ITextUnit targetPara = matrix.getAlignmentElementY(cell.getYindex());
					alignedParas.addAlignment(sourcePara, targetPara);
				}
				continue;
			}			
			
			if (cell.getState() == DpMatrixCell.DELETED) {
				ITextUnit sourcePara = matrix.getAlignmentElementX(cell.getXindex());				
				alignedParas.addAlignment(sourcePara, null);
				LOGGER.warn("{}\nTarget segment deleted (TU ID: {}): Non 1-1 match. Please confirm alignment.",
						sourcePara.toString(), sourcePara.getName());
			} else if (cell.getState() == DpMatrixCell.INSERTED) {
				ITextUnit targetPara = matrix.getAlignmentElementY(cell.getYindex());
				alignedParas.addAlignment(null, targetPara);
				LOGGER.warn("{}\nSource segment deleted (TU ID: {}): Non 1-1 match. Please confirm alignment.",
						targetPara.toString(), targetPara.getName());
			} else if (cell.getState() == DpMatrixCell.MATCH) {
				ITextUnit sourcePara = matrix.getAlignmentElementX(cell.getXindex());
				ITextUnit targetPara = matrix.getAlignmentElementY(cell.getYindex());
				alignedParas.addAlignment(sourcePara, targetPara);
			} else if (cell.getState() == DpMatrixCell.MULTI_MATCH) {
				List<ITextUnit> sourceParas = matrix.getAlignmentElementsX(
						cell.getMultiMatchXIndexBegin(), cell.getMultiMatchXIndexEnd());
				List<ITextUnit> targetParas = matrix.getAlignmentElementsY(
						cell.getMultiMatchYIndexBegin(), cell.getMultiMatchYIndexEnd());
				alignedParas.addAlignment(sourceParas, targetParas);
				ITextUnit p = null;
				try {
					p = sourceParas.get(0);
				} catch (IndexOutOfBoundsException e) {
					p = targetParas.get(0);
				}
				LOGGER.warn("{}\nMulti-ITextUnit Match (TU ID: {}): Non 1-1 match. Please confirm alignment.",
						p.getSource().toString(), p.getName());
			}
		}
		
		return alignedParas;
	}
}
