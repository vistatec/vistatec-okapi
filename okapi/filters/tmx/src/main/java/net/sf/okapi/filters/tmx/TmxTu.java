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
============================================================================*/

package net.sf.okapi.filters.tmx;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.MimeTypeMapper;
import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.tmx.TmxFilter.TuvXmlLang;

class TmxTu {
	
	GenericSkeleton skelBefore = new GenericSkeleton();			//Skeleton before tuv
	List <Property> propsBefore = new ArrayList<Property>();	//Propes of tmx tuv (only in the opening elements)
	GenericSkeleton skelAfter = new GenericSkeleton();			//Skeleton before tuv
	private List <TmxTuv> tuvs = new ArrayList<TmxTuv>();		//List of all the tuvs
	TmxTuv curTuv;												//Get the current tuv being processed
	boolean reachedTuvSection;									//flag helping determine if adding/appending to skelBefore or skelAfter
	String lineBreak;
	LocaleId srcLang;												//Redundant convenience variable
	LocaleId trgLang;												//Redundant convenience variable
	int segType;
	private String sep;
	
	
	/**
	 * Creates a new TmxTu instance with specified source and target language. 
	 * @param srcLang The source language.
	 * @param trgLang The target language. 
	 * @param sep Separator string used to delimit duplicate property values
	 */		
	public TmxTu (LocaleId srcLang, LocaleId trgLang, String lineBreak, int segType, String sep){
		this.srcLang = srcLang;
		this.trgLang = trgLang;
		this.lineBreak = lineBreak;
		this.segType = segType;
		this.sep = sep;
	}

	
	/**
	 * Removes from the from of a string any of the specified characters. 
	 * @param lang The language of the new TmxTuv.
	 * @param trgType Indicates if it's TuvXmlLang.SOURCE, TuvXmlLang.TARGET, or TuvXmlLang.OTHER.
	 * @return The added TmxTuv.
	 */	
	TmxTuv addTmxTuv(LocaleId lang, TuvXmlLang trgType, String sep){
		int counter = langCount(lang);
		TmxTuv newTmxTuv = new TmxTuv(lang, trgType, ++counter, segType, lineBreak, sep);
		
		tuvs.add(newTmxTuv);
		curTuv = newTmxTuv;
		
		if(!reachedTuvSection)
			reachedTuvSection=true;
		
		return newTmxTuv;
	}
	
	
	/**
	 * Counts the number of <tuv>s for a certain language.  
	 * @param lang The language to be counted.
	 * @return The number of <tuv>s. Result above 1 indicates a duplicate.
	 */		
	int langCount (LocaleId lang) {
		int counter=0;
		for (TmxTuv tmxTuv : tuvs){
			if ( tmxTuv.lang.equals(lang) ) {
				counter++;
			}
		}
		return counter;	
	}
	

	/**
	 * Counts the total number total TmxTuvs currently in the TmxTu.  
	 * @return The number of TmxTuvs.
	 */			
	int tuvCount(){
		return tuvs.size();
	}
	
	
	/**
	 * Returns whether there is at least one TmxTuv in the current TmxTu. 
	 * @return true if at least one TmxTuv exists in the current TmxTu
	 */
	boolean hasTuvs() {
		return !tuvs.isEmpty();
	}
	
	
	/**
	 * Returns the source TmxTuv, the one with an trgType of TuvXmlLang.SOURCE.  
	 * @return The source TmxTuv. Null if no source exists.
	 */			
	TmxTuv getSourceTuv(){
		for (TmxTuv tmxTuv : tuvs){
			if(tmxTuv.trgType==TuvXmlLang.SOURCE){
				return tmxTuv;
			}
		}
		return null;	
	}
	
	
	/**
	 * Returns a list of duplicate TmxTuv.  
	 * @return List of TmxTuvs.
	 */			
	List<TmxTuv> getDuplicateTuvs(){
		List<TmxTuv> dups = new ArrayList<TmxTuv>();
		for (TmxTuv tmxTuv : tuvs){
			if(tmxTuv.langCount > 1){
				dups.add(tmxTuv);
			}
		}
		return dups;	
	}
		
	
	/**
	 * Appends skeleton to either 'skelBefore' or 'skelAfter' depending on the value of 'reachedTuvSection'. 
	 * @param pskel Skeleton to append.
	 */
	void appendToSkel(String pskel){
		if(!reachedTuvSection){
			skelBefore.append(pskel);
		}else{
			skelAfter.append(pskel);
		}
	}

	
	/**
	 * Convenience method appends skeleton to the TmxTu or the current TmxTuv depending on the value of 'tuvTrgType'. 
	 * @param pskel Skeleton to append.
	 */
	void smartAppendToSkel(TuvXmlLang tuvTrgType, String pskel){
		if(tuvTrgType==TuvXmlLang.UNDEFINED){
			appendToSkel(pskel);
		}else{
			curTuv.appendToSkel(pskel);
		}
	}
	
	
	/**
	 * Add tu-level property. If property (name) already exists the new property value is appended to the existing one.
	 * @param prop Property to add.
	 */
	void addProp(Property prop){
		Property existingProp = getProp(prop.getName());
		if(existingProp!=null){
			existingProp.setValue(existingProp.getValue() + sep + prop.getValue());
		}else{
			propsBefore.add(prop);	
		}
	}

	
	/**
	 * Get tu-level property with specified name.
	 * @param name Name of property to retrieve. Null if it can't be found.
	 */
	Property getProp(String name){
		for (Property p : propsBefore)
			if(p.getName().equals(name)){
				return p;
			}
		return null;
	}
	
	
	/**
	 * Parse element and add properties to TmxTuv.
	 * @param reader XmlStreamReader.
	 */
	void parseStartElement (XMLStreamReader reader,
		boolean escapeGT)
	{
		parseStartElement(reader, null, escapeGT);
	}


	/**
	 * Parse element and add properties to TmxTu or return the property name for props and notes.
	 * @param reader XmlStreamReader.
	 * @param elem Element is null, prop, or notes.
	 * @param escapeGT true to escape '>' false to output as raw character
	 * @return Name of property if elem is passed as "prop" or "note".
	 */
	String parseStartElement (XMLStreamReader reader,
		String elem,
		boolean escapeGT)
	{

		String propName="";
		
		String prefix = reader.getPrefix();
		if (( prefix == null ) || ( prefix.length()==0 )) {
			skelBefore.append("<"+reader.getLocalName());
		}
		else {
			skelBefore.append("<"+prefix+":"+reader.getLocalName());
		}

		int count = reader.getNamespaceCount();
		for ( int i=0; i<count; i++ ) {
			TmxUtils.copyXMLNSToSkeleton(skelBefore, reader.getNamespacePrefix(i), 
										 reader.getNamespaceURI(i));
		}
		count = reader.getAttributeCount();
		for ( int i=0; i<count; i++ ) {
			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
			TmxUtils.copyAttributeToSkeleton(skelBefore, reader, i, lineBreak, escapeGT);
			
			if(elem!=null && elem.equals("prop")){
				if(reader.getAttributeLocalName(i).equals("type")){
					propName=reader.getAttributeValue(i);
				}								
			}else if(elem!=null && elem.equals("note")){
				
			}else{
				propsBefore.add(new Property(reader.getAttributeLocalName(i),reader.getAttributeValue(i), true));	
			}
		}
		skelBefore.append(">");
		
		if(elem!=null && elem.equals("note")){
			propName="note";
		}
		
		return propName;
	}		
	
	/**
	 * Parse end element adding skeleton to TmxTu afterSkel
	 * @param reader XmlStreamReader.
	 */	
	void parseEndElement (XMLStreamReader reader) {
		parseEndElement(reader, false);
	}	

	
	/**
	 * Parse end element adding skeleton to TmxTu afterSkel
	 * @param reader XmlStreamReader.
	 * @param addToSkelBefore Set to true to add to skelBefore 
	 */	
	void parseEndElement (XMLStreamReader reader, boolean addToSkelBefore) {
		String ns = reader.getPrefix();
		if (( ns == null ) || ( ns.length()==0 )) {
			if(addToSkelBefore)
				skelBefore.append("</"+reader.getLocalName()+">");
			else
				skelAfter.append("</"+reader.getLocalName()+">");
		}
		else {
			if(addToSkelBefore)
				skelBefore.append("</"+ns+":"+reader.getLocalName()+">");
			else
				skelAfter.append("</"+ns+":"+reader.getLocalName()+">");
			
			skelBefore.append("</"+ns+":"+reader.getLocalName()+">");
		}
	}	
	
	
	/**
	 * Enforce various Tu related rules by throwing Okapi Exceptions if rules are broken
	 */	
	void enforceTuRules(){
		// RULE 1: Make sure each <tu> contains at least one <tuv>
		if ( !hasTuvs() ) {
			throw new OkapiBadFilterInputException("Each <tu> requires at least one <tuv>");							
		}

		// RULE 2: Make sure each <tu> contains one and only one source <tuv>
		if (( getSourceTuv() == null ) || ( langCount(srcLang) > 1 )) {
			throw new OkapiBadFilterInputException(String.format(
				"The source language specified is '%s', but no <tuv> in the <tu> are set to this language.", srcLang));			
		}
	}
	
	
	/**
	 * Add the primary TextUnit Event to the queue
	 * @param tuId Id sequence
	 * @param processAllTargets Indicates if all languages are processed or only the target
	 * @param queue Queue to add the TextUnit Event to
	 * @return Updated id sequence 
	 */	
	int addPrimaryTextUnitEvent(int tuId, boolean processAllTargets, LinkedList<Event> queue){
		
		ITextUnit tu;
		GenericSkeleton tuSkel;

		enforceTuRules();

		//--1. tu skel before--
		tu = new TextUnit(String.valueOf(++tuId));
		tuSkel = new GenericSkeleton();
		tuSkel.add(skelBefore);
		
		// Properties
		for ( Property prop : propsBefore ) {
			tu.setProperty(prop);
		}

		for (TmxTuv tuv : tuvs){
			//--don't include duplicates--
			if(tuv.langCount==1 || (tuv.trgType == TuvXmlLang.OTHER && !processAllTargets)){
				//--1. tuv skel before--
				tuSkel.add(tuv.skelBefore);
				if(tuv.trgType == TuvXmlLang.SOURCE){
					//--add source container--
					TmxTuv srcTuv = getSourceTuv();					
					tu.setSource(srcTuv.tc.clone());
					tuSkel.addContentPlaceholder(tu, null);
				}else{
					
					if(tuv.trgType == TuvXmlLang.OTHER && !processAllTargets){
						
					}else{
						tu.setTarget(tuv.lang, tuv.tc);
						tuSkel.addContentPlaceholder(tu, tuv.lang);
					}
				}
				tuSkel.add(tuv.skelAfter);
			}
		}
		
		//--create new skeleton and close source if target does not exist--
		if(langCount(trgLang)==0){
			tuSkel.append("<tuv xml:lang=\""+trgLang+"\"><seg>");
			tuSkel.addContentPlaceholder(tu, trgLang);
			tuSkel.append("</seg></tuv>"+lineBreak);
		}
		
		
		//--1. tu skel after--
		tuSkel.add(skelAfter);

		
		//--add the resname based on tuid--
		if(tu.getProperty("tuid")!=null){
			tu.setName(tu.getProperty("tuid").getValue());
		}		
		
		//--add the resname based on tuid--
		Property tuidProp = getProp("tuid"); 
		if ( tuidProp != null ) {
			tu.setName(tuidProp.getValue());
		}
		tu.setSkeleton(tuSkel);
		tu.setMimeType(MimeTypeMapper.TMX_MIME_TYPE);

		queue.add(new Event(EventType.TEXT_UNIT, tu));

		return tuId;
	}
	
	
	/**
	 * Add the duplicate TextUnit Events to the queue if any
	 * @param tuId Id sequence
	 * @param processAllTargets Indicates if all languages are processed or only the target
	 * @param queue Queue to add the TextUnit Event to
	 * @return Updated id sequence 
	 */		
	int addDuplicateTextUnitEvents(int tuId, boolean processAllTargets, LinkedList<Event> queue){
		
		TmxTuv srcTuv = getSourceTuv();
		ITextUnit dupTu;
		GenericSkeleton dupTuSkel;

		if(srcTuv==null){
			throw new OkapiBadFilterInputException("Each <tu> requires at least one source <tuv>");			
		}
		
		List<TmxTuv> dups = getDuplicateTuvs();
		for (TmxTuv dupTuv : dups){
			
			if(dupTuv.trgType==TuvXmlLang.OTHER && !processAllTargets){
				
			}else{
				dupTu = new TextUnit(String.valueOf(++tuId));
				dupTu.setSource(srcTuv.tc.clone());
				dupTu.setTarget(dupTuv.lang, dupTuv.tc);
				dupTuSkel = new GenericSkeleton();
				dupTuSkel.add(skelBefore);
				dupTuSkel.add(srcTuv.skelBefore);
				dupTuSkel.addContentPlaceholder(dupTu, null);
				dupTuSkel.add(srcTuv.skelAfter);

				dupTuSkel.add(dupTuv.skelBefore);
				dupTuSkel.addContentPlaceholder(dupTu, dupTuv.lang);
				dupTuSkel.add(dupTuv.skelAfter);
				dupTuSkel.add(skelAfter);
				
				//--add the resname based on tuid--
				Property tuidProp = getProp("tuid"); 
				if ( tuidProp != null ) {
					dupTu.setName(tuidProp.getValue());
				}
				dupTu.setSkeleton(dupTuSkel);
				dupTu.setMimeType(MimeTypeMapper.TMX_MIME_TYPE);
				
				queue.add(new Event(EventType.TEXT_UNIT, dupTu));
			}
		}
		
		return tuId;
	}
	
	
	/**
	 * toString() for debugging purposes
	 * @return Combined string
	 */		
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("-----TmxTu-----\n");
		sb.append("Skeleton Before: "+skelBefore+"\n");
		for (Property p : propsBefore)
			sb.append("Property Name: "+p.getName()+"     Property Value: "+p.getValue()+"\n");

		for (TmxTuv tuv : tuvs){
			sb.append(tuv.toString());
		}
		
		sb.append("Skeleton After: "+skelAfter+"\n");
		return sb.toString();
	}
}
