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
===========================================================================*/

package net.sf.okapi.lib.extra.pipelinebuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.pipelinedriver.IBatchItemContext;

public class XBatch extends XBatchItem {

	private List<IBatchItemContext> items;
	
	public XBatch() {
		super();
		this.items = new ArrayList<IBatchItemContext>();
	}
	
	public XBatch(XBatchItem... items) {		
		this();		
		addItems(items);
	}

	public void setItems(List<IBatchItemContext> items) {
		this.items = items;
	}

	public List<IBatchItemContext> getItems() {
		return items;
	}

	public XBatch addItems(XBatchItem... items) {
		for (XBatchItem item : items)
			if (item instanceof XBatch) 
				this.items.addAll(((XBatch)item).getItems());
			else
				this.items.add(item.getContext());
		return this;
	}
	
	public XBatch addItem(XBatchItem item) {
		if (item instanceof XBatch) 
			this.items.addAll(((XBatch)item).getItems());
		else
			this.items.add(item.getContext());
		return this;
	}
	
	public XBatch addItems(String dir, String[] fileList, 
			String defaultEncoding, LocaleId sourceLocale, LocaleId targetLocale) {		
		for (String file : fileList) {
			this.items.add(new XBatchItem(Util.toURI(dir + file), defaultEncoding, sourceLocale, targetLocale).getContext());
		}
		return this;
	}
		
	public XBatch addItems(String dir, String[] fileList, 
			String defaultEncoding, URI outputURI, String outputEncoding, LocaleId sourceLocale, LocaleId targetLocale) {		
		for (String file : fileList) {
			this.items.add(new XBatchItem(Util.toURI(dir + file), defaultEncoding, outputURI, outputEncoding,
					sourceLocale, targetLocale).getContext());
		}
		return this;
	}
	
	public XBatch addItem(String dir, String file, 
			String defaultEncoding, LocaleId sourceLocale, LocaleId targetLocale) {		
			this.items.add(new XBatchItem(Util.toURI(dir + file), defaultEncoding, sourceLocale, targetLocale).getContext());
		return this;
	}

	public XBatch addItem(String dir, String file, 
			String defaultEncoding, URI outputURI, String outputEncoding, LocaleId sourceLocale, LocaleId targetLocale) {		
			this.items.add(new XBatchItem(Util.toURI(dir + file), defaultEncoding, outputURI, outputEncoding,
					sourceLocale, targetLocale).getContext());
		return this;
	}
	
	public void clearItems() {
		items.clear();
	}
}
