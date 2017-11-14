/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.virtualdb.jdbc.h2;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Stack;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.IResource;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filters.IFilterConfigurationMapper;
import net.sf.okapi.common.resource.ITextUnit;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.virtualdb.IVDocument;
import net.sf.okapi.virtualdb.IVItem.ItemType;

public class H2Importer {

	private H2Access db;
	private IFilterConfigurationMapper fcMapper;
	private LinkedHashMap<Long, H2Navigator> items;
	private Stack<H2Navigator> parents;
	private Stack<H2Navigator> prevItems;
	private long docItemKey;
	private H2Navigator prevItem;
	private int level;

	public H2Importer (H2Access db,
		IFilterConfigurationMapper fcMapper)
	{
		this.db = db;
		this.fcMapper = fcMapper;
	}
	
	public long importDocument (RawDocument rd) {
		items = new LinkedHashMap<Long, H2Navigator>();
		parents = new Stack<H2Navigator>();
		prevItems = new Stack<H2Navigator>();
		docItemKey = -1;
		try (IFilter filter = fcMapper.createFilter(rd.getFilterConfigId())) {
			filter.open(rd);
			while ( filter.hasNext() ) {
				Event event = filter.next();
				switch ( event.getEventType() ) {
				case START_DOCUMENT:
					processStartDocument((StartDocument)event.getResource());
					break;
				
				case START_SUBDOCUMENT:
					if (( prevItem.firstChild > -1 ) || ( prevItem.itemType == ItemType.TEXT_UNIT ) || ( prevItem.itemType == ItemType.SUB_DOCUMENT )) {
						addSibling(event.getResource(), ItemType.SUB_DOCUMENT);
					}
					else { // Document
						addChild(event.getResource(), ItemType.SUB_DOCUMENT);
					}
					break;
				
				case START_GROUP:
				case START_SUBFILTER:
					if (( prevItem.firstChild > -1 ) || ( prevItem.itemType == ItemType.TEXT_UNIT )) {
						addSibling(event.getResource(), ItemType.GROUP);
					}
					else { // Document, sub-document or group
						addChild(event.getResource(), ItemType.GROUP);
					}
					break;
					
				case END_SUBDOCUMENT:
				case END_GROUP:
				case END_SUBFILTER:
					// Don't pop if no children were added, like with empty group.
					// Ex: TEXT_UNIT, START_GROUP (added as sibling), END_GROUP
					if (( prevItem.previous == -1 ) || ( prevItem.itemType == ItemType.TEXT_UNIT )) {
						parents.pop();
						level--;
						prevItem = prevItems.pop();
					}
					break;
					
				case TEXT_UNIT:
					if (( prevItem.firstChild > -1 ) || ( prevItem.itemType == ItemType.TEXT_UNIT )) {
						addSibling(event.getResource(), ItemType.TEXT_UNIT);
					}
					else { // Document, sub-document or group
						addChild(event.getResource(), ItemType.TEXT_UNIT);
					}
					break;

				default:
					break;
				}
			}
			db.completeItemsWriting(items);
		}
		return docItemKey;
	}
	
	private void processStartDocument  (StartDocument sd) {
		// Look for the last document in the repository
		H2Document doc = null;
		Iterator<IVDocument> iter = db.documents().iterator();
		while ( iter.hasNext() ) {
			doc = (H2Document)iter.next();
		}
		// Now doc is null or the last document
		// Next: Add this document
		docItemKey = db.writeResourceData(sd, ItemType.DOCUMENT, -1);
		level = -1;
		H2Navigator item = new H2Navigator(ItemType.DOCUMENT, docItemKey, -1, level);
		// Update pointers if needed
		if ( doc != null ) {
			doc.next = item.key;
			item.previous = doc.key;
			db.saveDocument(doc); // Don't forget to save the changes in the former last document
		}
		items.put(item.key, item);
		prevItem = item;
	}
	
	private void addChild (IResource res,
		ItemType type)
	{
		long key = -1;
		switch ( type ) {
		case SUB_DOCUMENT:
			key = db.writeResourceData((StartSubDocument)res, type, docItemKey);
			break;
		case GROUP:
			key = db.writeResourceData((StartGroup)res, type, docItemKey);
			break;
		case TEXT_UNIT:
			key = db.writeTextUnitData((ITextUnit)res, docItemKey);
			break;
		default:
			break;
		}
		level++;
		H2Navigator item = new H2Navigator(type, key, docItemKey, level);
		items.put(item.key, item);
		item.parent = prevItem.key;
		prevItem.firstChild = key;
		parents.push(prevItem);
		prevItems.push(prevItem);
		prevItem = item;
	}
	
	private void addSibling (IResource res,
		ItemType type)
	{
		long key = -1;
		switch ( type ) {
		case SUB_DOCUMENT:
			key = db.writeResourceData((StartSubDocument)res, type, docItemKey);
			break;
		case GROUP:
			key = db.writeResourceData((StartGroup)res, type, docItemKey);
			break;
		case TEXT_UNIT:
			key = db.writeTextUnitData((ITextUnit)res, docItemKey);
			break;
		default:
			break;
		}
		H2Navigator item = new H2Navigator(type, key, docItemKey, level);
		items.put(item.key, item);
		item.previous = prevItem.key;
		item.parent = parents.peek().key;
		prevItem.next = key;
		prevItem = item;
	}
	
}
