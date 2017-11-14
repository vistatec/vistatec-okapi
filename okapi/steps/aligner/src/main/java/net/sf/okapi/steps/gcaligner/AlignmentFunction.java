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

import java.util.Iterator;
import java.util.List;

import net.sf.okapi.common.LocaleId;

/**
 * SegmentAlignmentFunction implements DpFunction. The class is used to align source and target segments. The type of
 * the alignment element is PageTmTuv.
 * @param <T>
 */

public class AlignmentFunction<T> implements DpFunction<T> {
	private LocaleId m_sourceLocale;
	private LocaleId m_targetLocale;
	private List<AlignmentScorer<T>> m_scorerList;
	private Penalties penalties;

	public AlignmentFunction(LocaleId p_sourceLocale, LocaleId p_targetLocale, 
			List<AlignmentScorer<T>> scorerList, Penalties penalties) {
		this.m_scorerList = scorerList;
		this.penalties = penalties;
		m_sourceLocale = p_sourceLocale;
		m_targetLocale = p_targetLocale;
		for (AlignmentScorer<T> s : m_scorerList) {
			s.setLocales(m_sourceLocale, m_targetLocale);
		}		
	}
	
	/**
	 * Set the score to DpMatrixCell at the specified location of the matrix.
	 * 
	 * @param p_xPos
	 *            X index of the matrix.
	 * @param p_yPos
	 *            Y index of the matrix.
	 * @param p_matrix
	 *            matrix
	 */
	public void setCellScore(int p_xPos, int p_yPos, DpMatrix<T> p_matrix) {
		if (p_xPos == 0 && p_yPos == 0) {
			return;
		}

		DpMatrixCell currentCell = getCell(p_xPos, p_yPos, p_matrix);

		DpMatrixCell insertionCell = getCell(p_xPos, p_yPos - 1, p_matrix);
		DpMatrixCell deletionCell = getCell(p_xPos - 1, p_yPos, p_matrix);
		DpMatrixCell substitutionCell = getCell(p_xPos - 1, p_yPos - 1, p_matrix);
		DpMatrixCell contractionCell = getCell(p_xPos - 2, p_yPos - 1, p_matrix);
		DpMatrixCell expansionCell = getCell(p_xPos - 1, p_yPos - 2, p_matrix);
		DpMatrixCell meldingCell = getCell(p_xPos - 2, p_yPos - 2, p_matrix);

		int deletionScore = getDeletionScore(deletionCell, p_xPos, p_matrix);
		int insertionScore = getInsertionScore(insertionCell, p_yPos, p_matrix);
		int substitutionScore = getSubstitutionScore(substitutionCell, p_xPos, p_yPos, p_matrix);
		int contractionScore = getContractionScore(contractionCell, p_xPos, p_yPos, p_matrix);
		int expansionScore = getExpansionScore(expansionCell, p_xPos, p_yPos, p_matrix);
		int meldingScore = getMeldingScore(meldingCell, p_xPos, p_yPos, p_matrix);

		setScoreAndLink(currentCell, deletionCell, insertionCell, substitutionCell,
				contractionCell, expansionCell, meldingCell, deletionScore, insertionScore,
				substitutionScore, contractionScore, expansionScore, meldingScore);

	}

	private DpMatrixCell getCell(int p_xPos, int p_yPos, DpMatrix<T> p_matrix) {
		DpMatrixCell cell = null;

		if (p_xPos >= 0 && p_yPos >= 0) {
			cell = p_matrix.getCell(p_xPos, p_yPos);
		}

		return cell;
	}

	private int getDeletionScore(DpMatrixCell p_deletionCell, int p_xPos, DpMatrix<T> p_matrix) {
		int score = Integer.MAX_VALUE;

		if (p_deletionCell != null) {
			score = 0;
			T seg = p_matrix.getAlignmentElementX(p_xPos);

			Iterator<AlignmentScorer<T>> it = m_scorerList.iterator();
			while (it.hasNext()) {
				AlignmentScorer<T> scorer = it.next();
				score += scorer.deletionScore(seg);
			}
			score += p_deletionCell.getScore() + penalties.penalty0_1;
		}

		return score;
	}

	private int getInsertionScore(DpMatrixCell p_insertionCell, int p_yPos, DpMatrix<T> p_matrix) {
		int score = Integer.MAX_VALUE;

		if (p_insertionCell != null) {
			score = 0;
			T seg = p_matrix.getAlignmentElementY(p_yPos);

			Iterator<AlignmentScorer<T>> it = m_scorerList.iterator();
			while (it.hasNext()) {
				AlignmentScorer<T> scorer = it.next();
				score += scorer.insertionScore(seg);
			}
			score += p_insertionCell.getScore() + penalties.penalty0_1;
		}

		return score;
	}

	private int getSubstitutionScore(DpMatrixCell p_substitutionCell, int p_xPos, int p_yPos,
			DpMatrix<T> p_matrix) {
		int score = Integer.MAX_VALUE;

		if (p_substitutionCell != null) {
			score = 0;
			T sourceSeg = p_matrix.getAlignmentElementX(p_xPos);
			T targetSeg = p_matrix.getAlignmentElementY(p_yPos);

			Iterator<AlignmentScorer<T>> it = m_scorerList.iterator();
			while (it.hasNext()) {
				AlignmentScorer<T> scorer = it.next();
				score += scorer.substitutionScore(sourceSeg, targetSeg);
			}
			score += p_substitutionCell.getScore();
		}

		return score;
	}

	private int getContractionScore(DpMatrixCell p_contractionCell, int p_xPos, int p_yPos,
			DpMatrix<T> p_matrix) {
		int score = Integer.MAX_VALUE;

		if (p_contractionCell != null) {
			score = 0;

			T currentSourceSeg = p_matrix.getAlignmentElementX(p_xPos);
			T prevSourceSeg = p_matrix.getAlignmentElementX(p_xPos - 1);
			T targetSeg = p_matrix.getAlignmentElementY(p_yPos);

			Iterator<AlignmentScorer<T>> it = m_scorerList.iterator();
			while (it.hasNext()) {
				AlignmentScorer<T> scorer = it.next();
				score += scorer.contractionScore(currentSourceSeg, prevSourceSeg, targetSeg);
			}
			score += p_contractionCell.getScore() + penalties.penalty2_1;
		}

		return score;
	}

	private int getExpansionScore(DpMatrixCell p_expansionCell, int p_xPos, int p_yPos,
			DpMatrix<T> p_matrix) {
		int score = Integer.MAX_VALUE;

		if (p_expansionCell != null) {
			score = 0;

			T srcSeg = p_matrix.getAlignmentElementX(p_xPos);
			T currentTargetSeg = p_matrix.getAlignmentElementY(p_yPos);
			T prevTargetSeg = p_matrix.getAlignmentElementY(p_yPos - 1);

			Iterator<AlignmentScorer<T>> it = m_scorerList.iterator();
			while (it.hasNext()) {
				AlignmentScorer<T> scorer = it.next();
				score += scorer.expansionScore(srcSeg, currentTargetSeg, prevTargetSeg);
			}
			score += p_expansionCell.getScore() + penalties.penalty2_1;
		}

		return score;
	}

	private int getMeldingScore(DpMatrixCell p_meldingCell, int p_xPos, int p_yPos,
			DpMatrix<T> p_matrix) {
		int score = Integer.MAX_VALUE;

		if (p_meldingCell != null) {
			score = 0;

			T currentSourceSeg = p_matrix.getAlignmentElementX(p_xPos);
			// Segment prevSourceTuv = p_matrix.getAlignmentElementX(p_xPos - 1);
			T currentTargetSeg = p_matrix.getAlignmentElementY(p_yPos);
			T prevTargetSeg = p_matrix.getAlignmentElementY(p_yPos - 1);

			Iterator<AlignmentScorer<T>> it = m_scorerList.iterator();
			while (it.hasNext()) {
				AlignmentScorer<T> scorer = it.next();
				score += scorer.meldingScore(currentSourceSeg, prevTargetSeg, currentTargetSeg,
						prevTargetSeg);
			}
			score += p_meldingCell.getScore() + penalties.penalty2_2;
		}

		return score;
	}

	private void setScoreAndLink(DpMatrixCell p_currentCell, DpMatrixCell p_deletionCell,
			DpMatrixCell p_insertionCell, DpMatrixCell p_substitutionCell,
			DpMatrixCell p_contractionCell, DpMatrixCell p_expansionCell,
			DpMatrixCell p_meldingCell, int p_deletionScore, int p_insertionScore,
			int p_substitutionScore, int p_contractionScore, int p_expansionScore,
			int p_meldingScore) {
		DpMatrixCell backLink = null;
		int minScore = Integer.MAX_VALUE;

		if (minScore > p_deletionScore) {
			minScore = p_deletionScore;
			backLink = p_deletionCell;
		}

		if (minScore > p_insertionScore) {
			minScore = p_insertionScore;
			backLink = p_insertionCell;
		}

		if (minScore > p_substitutionScore) {
			minScore = p_substitutionScore;
			backLink = p_substitutionCell;
		}

		if (minScore > p_contractionScore) {
			minScore = p_contractionScore;
			backLink = p_contractionCell;
		}

		if (minScore > p_expansionScore) {
			minScore = p_expansionScore;
			backLink = p_expansionCell;
		}

		if (minScore > p_meldingScore) {
			minScore = p_meldingScore;
			backLink = p_meldingCell;
		}

		p_currentCell.setScoreAndLink(minScore, backLink);
	}
}
