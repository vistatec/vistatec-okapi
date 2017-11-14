/*===========================================================================
  Copyright (C) 2011 by the Okapi Framework contributors
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

package net.sf.okapi.steps.common;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.okapi.common.IResource;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.BaseReferenceable;
import net.sf.okapi.common.resource.Code;
import net.sf.okapi.common.resource.INameable;
import net.sf.okapi.common.resource.IReferenceable;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.Segment;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *	Reused the test-scoped FilterTestDriver. Could probably be generalized and moved to the resources themselves or into a helper class.
 *
 */
public class ExtractionVerificationUtil {
	
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	boolean compareSkeleton;
	boolean isMultilingual;
	LocaleId targetLocale;
	private boolean targetLocaleOverriden = false;
	
	public ExtractionVerificationUtil(){
		this.compareSkeleton = true;
		this.isMultilingual = false;
	}

	public ExtractionVerificationUtil(boolean compareSkeleton){
		this.compareSkeleton = compareSkeleton;
	}
	
	public boolean isCompareSkeleton() {
		return this.compareSkeleton;
	}

	public void setCompareSkeleton(boolean compareSkeleton) {
		this.compareSkeleton = compareSkeleton;
	}
	
	public boolean isMultilingual() {
		return this.isMultilingual;
	}

	public void setMultilingual(boolean isMultilingual) {
		this.isMultilingual = isMultilingual;
	}
	
	public LocaleId getTargetLocale() {
		return this.targetLocale;
	}

	public void setTargetLocale(LocaleId targetLocale) {
		this.targetLocale = targetLocale;
	}

	public boolean isTargetLocaleOverriden(){
		return this.targetLocaleOverriden;
	}
	
	public void setTargetLocaleOverriden(boolean targetLocaleOverriden) {
		this.targetLocaleOverriden = targetLocaleOverriden;
	}
	/**
	 * Compare two StartSubDocuments 
	 * @param ssd1 First StartSubDocument
	 * @param ssd2 Second StartSubDocument
	 * @return true if equal else false
	 */
	public boolean compareStartSubDocument(StartSubDocument ssd1, StartSubDocument ssd2) {

		//--both are null no point checking anything else--
		if(bothAreNull(ssd1,ssd2)){
			return true;
		}
		
		//--one is null--
		if(oneIsNulll(ssd1,ssd2,"compareStartSubDocument","StartSubDocument") 
				|| oneIsNulll(ssd1.getParentId(),ssd2.getParentId(),"compareStartSubDocument","StartSubDocument Parent Id")
				|| oneIsNulll(ssd1.getFilterParameters(),ssd2.getFilterParameters(),"compareStartSubDocument","StartSubDocument Parent Id")){
			return false;
		}

		//--Parent Id--
		if( !bothAreNull(ssd1.getParentId(),ssd2.getParentId()) ){
			if ( !ssd1.getParentId().equals(ssd2.getParentId()) ) {
				LOGGER.warn("compareStartSubDocument warning: StartSubDocument Parent Id difference.");
				return false;
			}
		}
		
		//--FilterParameters--
		if( !bothAreNull(ssd1.getFilterParameters(),ssd2.getFilterParameters()) ){
			if ( !ssd1.getFilterParameters().equals(ssd2.getFilterParameters()) ) {
				LOGGER.warn("compareStartSubDocument warning: StartSubDocument FilterParameters difference.");
				return false;
			}
		}
		
		//--INameable portion--
		if (!compareINameables(ssd1, ssd2)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Compare two BaseReferenceables 
	 * @param br1 First BaseReferenceable
	 * @param br2 Second BaseReferenceable
	 * @return true if equal else false
	 */
	public boolean compareBaseReferenceable(BaseReferenceable br1, BaseReferenceable br2) {

		//--both are null no point checking anything else--
		if(bothAreNull(br1,br2)){
			return true;
		}
		
		//--one is null--
		if(oneIsNulll(br1,br2,"compareBaseReferenceable","BaseReferenceable") 
				|| oneIsNulll(br1.getParentId(),br2.getParentId(),"compareBaseReferenceable","BaseReferenceable Parent Id") ){
			return false;
		}

		//--Id--
		if( !bothAreNull(br1.getParentId(),br2.getParentId()) ){
			if ( !br1.getParentId().equals(br2.getParentId()) ) {
				LOGGER.warn("compareBaseReferenceable warning: BaseReferenceable Parent Id difference.");
				return false;
			}
		}
		
		//--INameable portion--
		if (!compareINameables(br1, br2)) {
			return false;
		}
		
		//--IReferenceable portion--
		if (!compareIReferenceables(br1, br2)) {
			return false;
		}

		return true;
	}
	
	/**
	 * Compare two ITextUnits 
	 * @param tu1 First ITextUnit
	 * @param tu2 Second ITextUnit
	 * @return true if equal else false
	 */
	public boolean compareTextUnits(ITextUnit tu1, ITextUnit tu2) {

		//--both are null no point checking anything else--
		if(bothAreNull(tu1,tu2)){
			return true;
		}
		
		//--one is null--
		if(oneIsNulll(tu1,tu2,"compareTextUnits","ITextUnit") ){
			return false;
		}
		
		//--INameable portion--
		if (!compareINameables(tu1, tu2)) {
			return false;
		}
		
		//--IReferenceable portion--
		if (!compareIReferenceables(tu1, tu2)) {
			return false;
		}

		//--Source Container: Monolingual format only needs to check this--
		if (!compareTextContainers(tu1.getSource(), tu2.getSource())) {
			return false;
		}

		if (isMultilingual()){
			
			//--Check if the targetlang was overriden--
			if(!targetLocaleOverriden){
				Set<LocaleId> targetLocales = tu2.getTargetLocales();
				if(targetLocales.size()==1 && !targetLocales.contains(targetLocale)){
					LocaleId overridenLocaleId = targetLocales.iterator().next();
					LOGGER.warn("compareTextUnits warning: Specified targetLocale not found. Assuming it was overriden by the filter. [Overriden from: {}\tto: {}]", targetLocale, overridenLocaleId);
					targetLocale = targetLocales.iterator().next();
					targetLocaleOverriden = true;
				}
			}
			
			//--target is copied from source during the first run--
			if( !tu1.hasTarget(targetLocale) && tu2.hasTarget(targetLocale)){

				if (!compareTextContainers(tu1.getSource(), tu2.getTarget(targetLocale), true)) {
					return false;
				}	
				
				if (tu1.getTargetLocales().size() > tu2.getTargetLocales().size()-1){
					LOGGER.warn("compareTextUnits warning: ITextUnit targetCount difference. Tu1 has more targets than Tu2");
					return false;
				}else if (tu2.getTargetLocales().size()-1 > tu1.getTargetLocales().size()){
					LOGGER.warn("compareTextUnits warning: ITextUnit targetCount difference. Tu2 has more targets than Tu1");
					return false;
				}
	
			}else{
				
				if (tu1.getTargetLocales().size() > tu2.getTargetLocales().size()){
					LOGGER.warn("compareTextUnits warning: ITextUnit targetCount difference. Tu1 has more targets than Tu2");
					return false;
				}else if (tu2.getTargetLocales().size() > tu1.getTargetLocales().size()){
					LOGGER.warn("compareTextUnits warning: ITextUnit targetCount difference. Tu2 has more targets than Tu1");
					return false;
				}
			}
			
			//--compares existing targets--
			for (LocaleId locId : tu1.getTargetLocales()) {
				if (!compareTextContainers(tu1.getTarget(locId), tu2.getTarget(locId))) {
					return false;
				}		
			}
		}

		return true;
	}

	/**
	 * Compares two TextContainers.
	 * @param tc1 First TextContainer
	 * @param tc2 Second TextContainer
	 * @return true if equal else false
	 */
	public boolean compareTextContainers(TextContainer tc1, TextContainer tc2) {
		return compareTextContainers(tc1, tc2, false);
	}
	
	/**
	 * Compares two TextContainers.
	 * @param tc1 First TextContainer
	 * @param tc2 Second TextContainer
	 * @param allowPropValChanges In the cases of multilingual formats some properties values may change such as target lang
	 * @return true if equal else false
	 */
	public boolean compareTextContainers(TextContainer tc1, TextContainer tc2, boolean allowPropValChanges) {
		
		//--both are null no point checking anything else--
		if(bothAreNull(tc1,tc2)){
			return true;
		}

		//--one is null--
		if(oneIsNulll(tc1,tc2,"compareTextContainer","TextContainer")){
			return false;
		}
		
		//--HasBeenSegmented--
		if (tc1.hasBeenSegmented() != tc2.hasBeenSegmented()) {
			LOGGER.warn("compareTextContainer warning: hasBeenSegmented difference.");
			return false;
		}

		//--PROPERTY CHECK--
		if( !tc1.getPropertyNames().equals(tc2.getPropertyNames()) ){
			LOGGER.warn("compareTextContainer warning: TextContainer properties difference. [tc1:{}\ttc2:{}]",
					tc1.getPropertyNames(), tc2.getPropertyNames());
			if(!allowPropValChanges){
				return false;				
			}
		}else{
			for (String name : tc1.getPropertyNames()) {
				if(!compareProperties(tc1.getProperty(name), tc2.getProperty(name))){
					if(!allowPropValChanges){ 
						return false;
					}
				}
			}
		}
		
		//--TextPart count--
		if (tc1.count() != tc2.count()) {
			LOGGER.warn("compareTextContainer warning: TextPart count difference.\n" +
					"tc1={}\ntc2={}", tc1.count(), tc2.count() );
			return false;
		}
		
		//--Segments--
		//if (tc1.hasBeenSegmented()) {
			
			Iterator<TextPart> it1 = tc1.iterator();
			Iterator<TextPart> it2 = tc2.iterator();
			
			while ( it1.hasNext() ){
				TextPart tp1 = it1.next();
				TextPart tp2 = it2.next();
				
				if( !(tp1.isSegment()) == (tp2.isSegment())){
					LOGGER.warn("compareTextContainer warning: TextContainer TextPart <--> Segment difference.");
					return false;
				}
				
				if (tp1.isSegment()){
					if( !compareSegments((Segment)tp1, (Segment)tp2)){
						return false;
					}
				}else{
					if( !compareTextParts(tp1, tp2)){
						return false;
					}
				}
		    }
		//}

		return true;
	}

	/**
	 * Compares two Segments. 
	 * @param seg1 First Segment
	 * @param seg2 Second Segment
	 * @return true if equal else false
	 */
	public boolean compareSegments(Segment seg1, Segment seg2) {
		
		//--both are null no point checking anything else--
		if(bothAreNull(seg1,seg2)){
			return true;
		}
		
		//--one is null--
		if( oneIsNulll(seg1,seg2,"compareSegments","Segment")
				|| oneIsNulll(seg1.getId(),seg2.getId(),"compareSegment","Segment Id")){
			return false;
		}

		//--Id--
		if( !bothAreNull(seg1.getId(),seg2.getId()) ){
			if ( !seg1.getId().equals(seg2.getId()) ) {
				LOGGER.warn("compareSegment warning: Segment Id difference.");
				return false;
			}
		}

		//--Content--
		if ( !compareTextFragments(seg1.getContent(), seg2.getContent()) ){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Compares two TextPart. 
	 * @param tp1 First TextPart
	 * @param tp2 Second TextPart
	 * @return true if equal else false
	 */
	public boolean compareTextParts(TextPart tp1, TextPart tp2) {
		
		//--both are null no point checking anything else--
		if(bothAreNull(tp1,tp2)){
			return true;
		}
		
		//--one is null--
		if( oneIsNulll(tp1,tp2,"compareTextPart","TextPart") ){
			return false;
		}
		
		//--Content--
		if ( !compareTextFragments(tp1.getContent(), tp2.getContent()) ){
			return false;
		}
		
		return true;
	}
	
	/**
	 * Compares two TextFragments. Basically it's the TextFragments own compareTo with additional null checks
	 * @param tf1 First TextFragment
	 * @param tf2 Second TextFragment
	 * @return true if equal else false
	 */
	public boolean compareTextFragments(TextFragment tf1, TextFragment tf2) {
		
		//--both are null no point checking anything else--
		if(bothAreNull(tf1,tf2)){
			return true;
		}
		
		//--one is null--
		if(oneIsNulll(tf1,tf2,"compareTextFragment","TextFragment") ){
			return false;
		}

		//--number of codes/this would get caught by coded text but with less detail--
		List<Code> codes1 = tf1.getCodes();
		List<Code> codes2 = tf2.getCodes();
		
		//--code count--
		if (codes1.size() != codes2.size()) {
			LOGGER.warn("compareTextFragment warning: TextFragment code count difference.\n" +
					"tf1={}\ntf2={}", codes1.size(), codes2.size());
			return false;
		}

		//--coded text--
		if (!tf1.getCodedText().equals(tf2.getCodedText())) {
			//if(tf1.compareTo(tf2) != 0){
			LOGGER.warn("compareTextFragment warning: TextFragment coded text difference.\n" +
					"text1=\"{}\"\ntext2=\"{}\"", tf1.getCodedText(), tf2.getCodedText());
			return false;
		}
		
		for (int i = 0; i < codes1.size(); i++) {
			
			Code code1 = codes1.get(i);
			Code code2 = codes2.get(i);
			
			// Id
			if (code1.getId() != code2.getId()) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code id difference.\n" +
						"code1 id=\"{}\"\ncode2 id=\"{}\"", code1.getId(), code2.getId());
				return false;
			}

			// Data
			if ( !code1.getData().equals(code2.getData()) ) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code data difference.\n" +
						"code1 data=\"{}\"\ncode2 data=\"{}\"", code1.getData(), code2.getData());
				return false;
			}

			// Outer data
			if ( !code1.getOuterData().equals(code2.getOuterData()) ) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code outer data difference.\n" +
						"code1 outerdata=\"{}\"\ncode2 outerdata=\"{}\"", code1.getOuterData(), code2.getOuterData());
				return false;
			}
			
			if ( !code1.getType().equals(code2.getType()) ) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code type difference.\n" +
						"code1 type=\"{}\"\ncode2 type=\"{}\"", code1.getType(), code2.getType());
				return false;
			}
			if (code1.getTagType() != code2.getTagType()) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code TagType difference.");
				return false;
			}
			if (code1.hasReference() != code2.hasReference()) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code hasReference difference.");
				return false;
			}
			if (code1.isCloneable() != code2.isCloneable()) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code isCloenable difference.");
				return false;
			}
			if (code1.isDeleteable() != code2.isDeleteable()) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code isDeleteable difference.");
				return false;
			}
			if (code1.hasAnnotation() != code2.hasAnnotation()) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code hasAnnotation difference.");
				return false;
			}
			
			if (code1.hasAnnotation() != code2.hasAnnotation()) {
				LOGGER.warn("compareTextFragment warning: TextFragment Code hasAnnotation difference.");
				return false;
			}
		}
		
		// CodesToString
		String codeStr1 = Code.codesToString(codes1);
		String codeStr2 = Code.codesToString(codes2);
		if ( !codeStr1.equals(codeStr2) ) {
			LOGGER.warn("compareTextFragment warning: TextFragment Code string difference.\n" +
					"code1=\"{}\"\ncode2=\"{}\"", codeStr1, codeStr2);
			return false;
		}
		
		return true;
	}
	
	
	/**
	 * Compare two INameables (Assuming isTranslatable() and preserveWhitespaces() do not return null)
	 * @param n1 First INameable
	 * @param n2 Second INameable
	 * @return true if equal else false
	 */	
	public boolean compareINameables(INameable n1, INameable n2) {
		
		//--both are null no point checking anything else--
		if(bothAreNull(n1,n2)){
			return true;
		}
		
		//--one is null--
		if(oneIsNulll(n1,n2,"compareINameable","INameable") 
				|| oneIsNulll(n1.getName(),n2.getName(),"compareINameable","INameable Name")
				|| oneIsNulll(n1.getType(),n2.getType(),"compareINameable","INameable Type")
				|| oneIsNulll(n1.getMimeType(),n2.getMimeType(),"compareINameable","INameable MimeType")){
			return false;
		}

		//--Name--
		if( !bothAreNull(n1.getName(),n2.getName()) ){
			if (!n1.getName().equals(n2.getName())) {
				LOGGER.warn("compareINameables warning: INameable Name difference.");
				return false;
			}
		}

		//--Type--
		if( !bothAreNull(n1.getType(),n2.getType()) ){
			if (!n1.getType().equals(n2.getType())) {
				LOGGER.warn("compareINameables warning: INameable Type difference.");
				return false;
			}
		}

		//--MimeType--
		if( !bothAreNull(n1.getMimeType(),n2.getMimeType()) ){
			if (!n1.getMimeType().equals(n2.getMimeType())) {
				LOGGER.warn("compareINameables warning: INameable MimeType difference.");
				return false;
			}
		}
		
		//--IsTranslatable--
		if (n1.isTranslatable() != n2.isTranslatable()) {
			LOGGER.warn("compareINameables warning: INameable isTranslatable difference.");
			return false;
		}

		//--PreserveWhitespaces--
		if (n1.preserveWhitespaces() != n2.preserveWhitespaces()) {
			LOGGER.warn("compareINameables warning: INameable isTranslatable difference.");
			return false;
		}
		
		//--PROPERTY CHECK--
		if( !n1.getPropertyNames().equals(n2.getPropertyNames()) ){
			LOGGER.warn("compareINameables warning: INameable properties difference.");
			return false;
		}
		
		for (String name : n1.getPropertyNames()) {
			if(!compareProperties(n1.getProperty(name), n2.getProperty(name)) ){
				return false;
			}
		}
		
		//--SOURCE PROPERTY CHECK--
		if( !n1.getSourcePropertyNames().equals(n2.getSourcePropertyNames()) ){
			LOGGER.warn("compareINameables warning: INameable source properties difference.");
			return false;
		}
		
		for (String name : n1.getSourcePropertyNames()) {
			if(!compareProperties(n1.getSourceProperty(name), n2.getSourceProperty(name)) ){
				return false;
			}
		}	
		
		//--TARGET LOCALE CHECK--
		if(!n1.getTargetLocales().equals(n2.getTargetLocales()) ){
			if(!isMultilingual()){
				LOGGER.warn("compareINameables warning: INameable target locales difference.");
				return false;
			}
		}
		
		//TODO: For multilingual: might need to check a possible added target in the second run and compare to source props
		for (LocaleId locId : n1.getTargetLocales()) {

			//--TARGET PROPERTY CHECK--
			if( !n1.getTargetPropertyNames(locId).equals(n2.getTargetPropertyNames(locId)) ){
				LOGGER.warn("compareINameables warning: INameable target properties difference.");
				return false;
			}
			
			for (String name : n1.getTargetPropertyNames(locId)) {
				if(!compareProperties(n1.getTargetProperty(locId, name), n2.getTargetProperty(locId, name)) ){
					return false;
				}
			}		
		}

		//--COMPARE IRESOURCE PORTION--
		if (!compareIResources(n1, n2)) {
			return false;
		}

		return true;
	}

	/**
	 * Compare two IResources (Assuming isTranslatable() and preserveWhitespaces() do not return null)
	 * @param r1 First IResource
	 * @param r2 Second IResource
	 * @return true if equal else false
	 */	
	public boolean compareIResources(IResource r1, IResource r2){

		//--both are null no point checking anything else--
		if(bothAreNull(r1,r2)){
			return true;
		}
		
		//--one is null--
		if(oneIsNulll(r1,r2,"compareIResource","IResource") 
				|| oneIsNulll(r1.getId(),r2.getId(),"compareIResource","IResource Id")){
			return false;
		}
		
		if( !bothAreNull(r1.getId(),r2.getId()) ){
			if (!r1.getId().equals(r2.getId())) {
				LOGGER.warn("compareIResource warning: IResource Id difference.");
				return false;
			}
		}
		
		// Skeleton
		if ( !compareSkeleton ) {
			return true;
		}

		//--both are null no point checking anything else--
		if( bothAreNull(r1.getSkeleton(),r2.getSkeleton()) ){
			return true;
		}

		//--one is null--
		if(oneIsNulll(r1.getSkeleton(),r2.getSkeleton(),"compareIResource","IResource skeleton") || oneIsNulll(r1.getSkeleton().toString(),r2.getSkeleton().toString(),"compareIResource","IResource skeleton string")){
			return false;
		}

		if (!r1.getSkeleton().toString().equals(r2.getSkeleton().toString())) {
			LOGGER.warn("compareIResource warning: Skeleton content difference (If skeleton difference is acceptable turn it off in the settings).\n" +
					"skel1=\"{}\"\nskel2=\"{}\"", r1.getSkeleton().toString(), r2.getSkeleton().toString());
			return false;
		}

		return true;
	}

	
	/**
	 * Compare two IReferenceables (Assuming isReferent() and referenceCount() do not return null) 
	 * @param r1 First IReferencable
	 * @param r2 Second IReferencable
	 * @return true if equal else false
	 */
	public boolean compareIReferenceables(IReferenceable r1, IReferenceable r2) {
		
		//--both are null no point checking anything else--
		if(bothAreNull(r1,r2)){
			return true;
		}
		
		//--one is null--
		if(oneIsNulll(r1,r2,"compareIReferenceable","IReferenceable") ){
			return false;
		}
		
		//--IsReferent--
		if (r1.isReferent() != r2.isReferent()) {
			LOGGER.warn("compareIReferenceable warning: IReferenceable isReferent difference.");
			return false;
		}

		//--ReferenceCount--
		if (r1.getReferenceCount() != r2.getReferenceCount()) {
			LOGGER.warn("compareIReferenceable warning: IReferenceable getReferenceCount difference.");
			return false;
		}
		
		return true;
	}

	/**
	 * Compare two Properties
	 * @param p1 First Property  
	 * @param p2 Second Property 
	 * @return true if equal else false
	 */
	public boolean compareProperties(Property p1, Property p2) {

		//--both are null no point checking anything else--
		if(bothAreNull(p1,p2)){
			return true;
		}
		
		//--one is null--
		if(oneIsNulll(p1,p2,"compareProperty","Property") 
				|| oneIsNulll(p1.getName(),p2.getName(),"compareProperty","Property Name") 
				|| oneIsNulll(p1.getValue(),p2.getValue(),"compareProperty","Property Value")){
			return false;
		}

		if( !bothAreNull(p1.getName(),p2.getName()) ){
			if (!p1.getName().equals(p2.getName())) {
				LOGGER.warn("compareProperty warning: Property name difference.");
				return false;
			}
		}
		if( !bothAreNull(p1.getValue(),p2.getValue()) ){
			if (!p1.getValue().equals(p2.getValue())) {
				LOGGER.warn("compareProperty warning: Property value difference. [prop name:{}\tval p1:{}\tval p2:{}]", p1.getName(), p1.getValue(), p2.getValue());
				return false;
			}
		}
		if (p1.isReadOnly() != p2.isReadOnly()) {
			LOGGER.warn("compareProperty warning: isReadOnly difference.");
			return false;
		}
		
		return true;
	}
	
	/**
	 * Checks if both objects are null
	 * @param o1 First Object
	 * @param o2 Second Object
	 * @return true if both are null else false 
	 */
	public boolean bothAreNull(Object o1, Object o2){
		
		if (o1 == null && o2 == null){
			return true;
		}
		return false;
	}

	/**
	 * Checks if one object is null
	 * @param o1 First Object
	 * @param o2 Second Object
	 * @param function The function initiating the call. Used for logging. 
	 * @param type The object type. Used for logging.
	 * @return True if one object is null
	 */
	public boolean oneIsNulll(Object o1, Object o2, String function, String type){
		
		if (o1 == null && o2 != null){
			LOGGER.warn("{} warning: {} 1 is null and {} 2 is not null.", function, type, type);
			return true;
		} 
		if (o1 != null && o2 == null){				
			LOGGER.warn("{} warning: {} 1 is not null and {} 2 is null.", function, type, type);
			return true;
		}
		return false;
	}
}
