/*===========================================================================
  Copyright (C) 2009 by the Okapi Framework contributors
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

import javax.xml.stream.XMLStreamReader;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.resource.AlignmentStatus;
import net.sf.okapi.common.resource.Property;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.skeleton.GenericSkeleton;
import net.sf.okapi.filters.tmx.TmxFilter.TuvXmlLang;

public class TmxTuv {

	LocaleId lang;											//tuv language
	TuvXmlLang trgType;										//SOURCE, TARGET, OTHER
	int langCount;											// > 1 means duplicates
	GenericSkeleton skelBefore = new GenericSkeleton();		
	GenericSkeleton skelAfter = new GenericSkeleton();
	TextContainer tc = new TextContainer();
	String lineBreak;
	boolean finishedSegSection;								//flag helping determine if adding/appending to skelBefore or skelAfter
	//boolean allSkeleton;									//Redundant convenience variable
	private String sep;
	
	
	/**
	 * Creates a new TmxTuv instance with specified language, trgType, and counter. 
	 * @param lang Language of tuv
	 * @param trgType TuvXmlLang.SOURCE, TuvXmlLang.TARGET, TuvXmlLang.OTHER 
	 * @param counter Higher than 1 indicates the number of duplicate 
	 * @param segType 0 = sentence = segmented, 1 = paragraph = unsegmented
	 * @param sep Separator string used to delimit duplicate property values
	 */		
	TmxTuv (LocaleId lang, TuvXmlLang trgType, int counter, int segType, String lineBreak, String sep){
		this.lang = lang;
		this.trgType = trgType;
		this.langCount = counter;
		this.lineBreak = lineBreak;
		this.sep = sep;
				
		if (segType == 0){
			tc.setHasBeenSegmentedFlag(true);
			tc.getSegments().setAlignmentStatus(AlignmentStatus.ALIGNED);
		}else if (segType == 1){ 
			tc.setHasBeenSegmentedFlag(false);
			tc.getSegments().setAlignmentStatus(AlignmentStatus.NOT_ALIGNED);
		}
	}
	
	
	/**
	 * Parse element and add properties to TmxTuv or return the property name for props and notes.
	 * @param reader XmlStreamReader.
	 * @param tuvTrgType Together with processAllTargets determines if property should be added. 
	 * @param processAllTargets Together with tuvTrgType determines if property should be added.
	 * @param escapeGT whether to escape the '>' symbol in attribute values
	 * @return Name of property if elem is passed as "prop" or "note".
	 */
	void parseStartElement (XMLStreamReader reader , TuvXmlLang tuvTrgType, boolean processAllTargets,
							boolean escapeGT) {
		parseStartElement (reader,tuvTrgType, processAllTargets, escapeGT, null);
	}	
	
	
	/**
	 * Parse element and add properties to TmxTuv or return the property name for props and notes.
	 * @param reader XmlStreamReader.
	 * @param tuvTrgType Together with processAllTargets determines if property should be added. 
	 * @param processAllTargets Together with tuvTrgType determines if property should be added.
	 * @return Name of property if elem is passed as "prop" or "note".
	 */
	String parseStartElement (XMLStreamReader reader , TuvXmlLang tuvTrgType, boolean processAllTargets, 
						      boolean escapeGT, String elem) {

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
				if (tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || processAllTargets){
					tc.setProperty(new Property(reader.getAttributeLocalName(i),reader.getAttributeValue(i), true));
				}				
			}
		}
		skelBefore.append(">");
		
		if(elem!=null && elem.equals("note")){
			propName="note";
		}
		
		return propName;		
	}
	
	/**
	 * Appends skeleton to either 'skelBefore' or 'skelAfter' depending on the value of 'finishedSegSection'. 
	 * @param pskel Skeleton to append.
	 */
	void appendToSkel(String pskel){
		if(!finishedSegSection){
			skelBefore.append(pskel);
		}else{
			skelAfter.append(pskel);
		}
	}
		
	
	/**
	 * Parse end element adding skeleton to TmxTuv afterSkel
	 * @param reader XmlStreamReader.
	 */	
	void parseEndElement (XMLStreamReader reader) {
		parseEndElement(reader, false);
	}		
	
	
	/**
	 * Parse end element adding skeleton to TmxTuv afterSkel
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
	 * Sets the property of this TmxTuv. Adds to existing or creates new one.
	 * @param prop Property to add
	 */	
	void setProperty(Property prop){
		Property existingProp = tc.getProperty(prop.getName());
		if(existingProp!=null){
			existingProp.setValue(existingProp.getValue() + sep + prop.getValue());
		}else{
			tc.setProperty(prop);	
		}
	}

	
	/**
	 * toString() for debugging purposes
	 * @return Combined string
	 */	
	public String toString(){
		StringBuilder sb = new StringBuilder();
	
		sb.append("-----TmxTuv-----\n");
		sb.append("Trg Lang: "+lang+"     Trg Type: "+trgType+"     Counter: "+langCount+"\n");
		sb.append("Skel Before: "+skelBefore+"\n");
		for (String pname : tc.getPropertyNames())
			sb.append("Prop Name: "+pname+"     Prop Value: "+tc.getProperty(pname)+"\n");
		sb.append(tc.getCodedText()+"\n");
		sb.append("Skel After: "+skelAfter+"\n\n");
		
		return sb.toString();
	}
	
}
