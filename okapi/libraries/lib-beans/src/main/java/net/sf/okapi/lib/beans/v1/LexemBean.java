/*===========================================================================
  Copyright (C) 2008-2010 by the Okapi Framework contributors
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

import net.sf.okapi.common.Range;
import net.sf.okapi.common.annotation.IAnnotation;
import net.sf.okapi.lib.persistence.IPersistenceSession;
import net.sf.okapi.lib.persistence.PersistenceBean;
import net.sf.okapi.lib.persistence.beans.FactoryBean;
import net.sf.okapi.steps.tokenization.common.Lexem;

public class LexemBean extends PersistenceBean<Lexem> {

	private int id;
	private String value;
	private RangeBean range = new RangeBean();
	private int lexerId;
	private AnnotationsBean annotations = new AnnotationsBean();
	private boolean deleted;
	private boolean immutable;
	
	@Override
	protected Lexem createObject(IPersistenceSession session) {
		return new Lexem(id, value, range.get(Range.class, session));
	}

	@Override
	protected void fromObject(Lexem obj, IPersistenceSession session) {
		id = obj.getId();
		value = obj.getValue();
		range.set(obj.getRange(), session);
		lexerId = obj.getLexerId();
		annotations.set(obj.getAnnotations(), session);
		deleted = obj.isDeleted();
		immutable = obj.isImmutable();
	}

	@Override
	protected void setObject(Lexem obj, IPersistenceSession session) {
		obj.setLexerId(lexerId);
		for (FactoryBean annotationBean : annotations.getItems())
			obj.setAnnotation(annotationBean.get(IAnnotation.class, session));
		obj.setDeleted(deleted);
		obj.setImmutable(immutable);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public RangeBean getRange() {
		return range;
	}

	public void setRange(RangeBean range) {
		this.range = range;
	}

	public int getLexerId() {
		return lexerId;
	}

	public void setLexerId(int lexerId) {
		this.lexerId = lexerId;
	}

	public AnnotationsBean getAnnotations() {
		return annotations;
	}

	public void setAnnotations(AnnotationsBean annotations) {
		this.annotations = annotations;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean isImmutable() {
		return immutable;
	}

	public void setImmutable(boolean immutable) {
		this.immutable = immutable;
	}
}
