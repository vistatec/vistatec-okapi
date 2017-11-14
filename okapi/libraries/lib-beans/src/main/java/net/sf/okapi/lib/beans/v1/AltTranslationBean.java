/*===========================================================================
  Copyright (C) 2008-2011 by the Okapi Framework contributors
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

package net.sf.okapi.lib.beans.v1;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.annotation.AltTranslation;
import net.sf.okapi.common.annotation.XLIFFTool;
import net.sf.okapi.common.query.MatchType;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;

public class AltTranslationBean extends PersistenceBean<AltTranslation> {

	private String srcLocId;
	private String trgLocId;
	private TextUnitBean tu = new TextUnitBean();
	private MatchType type;
	private int score;
	private String origin;
	private int fuzzyScore;
	private int qualityScore;
	private boolean fromOriginal;
	private String engine;
	private XLIFFToolBean tool = new XLIFFToolBean();
	
	@Override
	protected AltTranslation createObject(IPersistenceSession session) {
		ITextUnit tunit = tu.get(ITextUnit.class, session);
		LocaleId srcLoc = null;
		LocaleId trgLoc = null;
		
		if (!Util.isEmpty(srcLocId))
			srcLoc = LocaleId.fromString(srcLocId);
		
		if (!Util.isEmpty(trgLocId))
			trgLoc = LocaleId.fromString(trgLocId);
		
		TextFragment src = null; 
		TextFragment trg = null;
		
		if (tunit != null) {
			src = tunit.getSource().getSegments().getFirstContent(); 
			trg = tunit.getTarget(trgLoc).getSegments().getFirstContent();
		}		
		return new AltTranslation(srcLoc, trgLoc, null, src, trg, type, score, origin);
	}

	@Override
	protected void fromObject(AltTranslation obj, IPersistenceSession session) {		
		srcLocId = obj.getSourceLocale().toString();
		trgLocId = obj.getTargetLocale().toString();
		tu.set(obj.getEntry(), session);
		type = obj.getType();
		score = obj.getCombinedScore();
		origin = obj.getOrigin();
		fuzzyScore = obj.getFuzzyScore();
		qualityScore = obj.getQualityScore();
		fromOriginal = obj.getFromOriginal();
		engine = obj.getEngine();
		tool.set(obj.getTool(), session);
	}

	@Override
	protected void setObject(AltTranslation obj, IPersistenceSession session) {
		// Other fields are set in constructor
		 obj.setFuzzyScore(fuzzyScore);
		 obj.setQualityScore(qualityScore);
		 obj.setFromOriginal(fromOriginal);
		 obj.setEngine(engine);
		 obj.setTool(tool.get(XLIFFTool.class, session));
	}

	public String getSrcLocId() {
		return srcLocId;
	}

	public void setSrcLocId(String srcLocId) {
		this.srcLocId = srcLocId;
	}

	public String getTrgLocId() {
		return trgLocId;
	}

	public void setTrgLocId(String trgLocId) {
		this.trgLocId = trgLocId;
	}

	public TextUnitBean getTu() {
		return tu;
	}

	public void setTu(TextUnitBean tu) {
		this.tu = tu;
	}

	public MatchType getType() {
		return type;
	}

	public void setType(MatchType type) {
		this.type = type;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public int getFuzzyScore() {
		return fuzzyScore;
	}

	public void setFuzzyScore(int fuzzyScore) {
		this.fuzzyScore = fuzzyScore;
	}

	public int getQualityScore() {
		return qualityScore;
	}

	public void setQualityScore(int qualityScore) {
		this.qualityScore = qualityScore;
	}

	public boolean isFromOriginal() {
		return fromOriginal;
	}

	public void setFromOriginal(boolean fromOriginal) {
		this.fromOriginal = fromOriginal;
	}

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	public XLIFFToolBean getTool() {
		return tool;
	}

	public void setTool(XLIFFToolBean tool) {
		this.tool = tool;
	}
}
