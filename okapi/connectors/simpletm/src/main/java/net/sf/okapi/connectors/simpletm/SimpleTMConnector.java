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

package net.sf.okapi.connectors.simpletm;

import java.util.LinkedHashMap;
import java.util.List;

import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.Util;
import net.sf.okapi.common.exceptions.OkapiNotImplementedException;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.lib.translation.BaseConnector;
import net.sf.okapi.lib.translation.ITMQuery;
import net.sf.okapi.tm.simpletm.Database;

public class SimpleTMConnector extends BaseConnector implements ITMQuery {
	
	private Database db;
	private int maxHits = 5;
	private int threshold = 98;
	private List<QueryResult> results;
	private int current = -1;
	private LinkedHashMap<String, String> attributes;
	private Parameters params;
	private String rootDir;

	public SimpleTMConnector () {
		params = new Parameters();
		db = new Database();
		attributes = new LinkedHashMap<String, String>();
	}
	
	@Override
	public String getName () {
		return "SimpleTM";
	}

	@Override
	public String getSettingsDisplay () {
		return String.format("Database: %s\nPenalize exact matches with different codes in source: %s, in target: %s",
			(Util.isEmpty(params.getDbPath()) ? "<To be specified>" : params.getDbPath()),
			(params.getPenalizeSourceWithDifferentCodes() ? "Yes" : "No"),
			(params.getPenalizeTargetWithDifferentCodes() ? "Yes" : "No"));
	}
	
	@Override
	public void setMaximumHits (int max) {
		if ( max < 1 ) maxHits = 1;
		else maxHits = max;
	}

	@Override
	public void setThreshold (int threshold) {
		this.threshold = threshold;
	}

	@Override
	public void close() {
		db.close();
	}

	@Override
	public boolean hasNext () {
		if ( results == null ) return false;
		if ( current >= results.size() ) {
			current = -1;
		}
		return (current > -1);
	}
	
	@Override
	public QueryResult next () {
		if ( results == null ) return null;
		if (( current > -1 ) && ( current < results.size() )) {
			current++;
			return results.get(current-1);
		}
		current = -1;
		return null;
	}

	@Override
	public void open () {
		db.open(Util.fillRootDirectoryVariable(params.getDbPath(), rootDir));
		db.setPenalizeSourceWithDifferentCodes(params.getPenalizeSourceWithDifferentCodes());
		db.setPenalizeTargetWithDifferentCodes(params.getPenalizeTargetWithDifferentCodes());
	}

	@Override
	public int query (String plainText) {
		TextFragment tf = new TextFragment(plainText);
		return query(tf);
	}
	
	@Override
	public int query (TextFragment text) {
		current = -1;
		results = db.query(text, attributes, maxHits, threshold);
		if ( results == null ) return 0;
		current = 0;
		return results.size();
	}
	
	@Override
	public List<List<QueryResult>> batchQuery (List<TextFragment> fragments) {
		throw new OkapiNotImplementedException();
	}
	
	@Override
	public void setAttribute (String name,
		String value)
	{
		assert(value!=null);
		if ( "resname".equals(name) ) name = Database.NNAME;
		if ( "restype".equals(name) ) name = Database.NTYPE;
		if ( attributes.put(name, value) == null ) {
			// Update the query if this attribute did not exist yet
			db.createStatement(attributes);
		}
	}
	
	@Override
	public void clearAttributes () {
		attributes.clear();
		db.clearAttributes();
	}

	@Override
	public void removeAttribute (String name) {
		if ( attributes.containsKey(name) ) {
			attributes.remove(name);
			db.createStatement(attributes);
		}
	}

	@Override
	public int getMaximumHits () {
		return maxHits;
	}

	@Override
	public int getThreshold () {
		return threshold;
	}

	@Override
	public IParameters getParameters() {
		return params;
	}

	@Override
	public void setParameters (IParameters params) {
		this.params = (Parameters)params;
	}

	@Override
	public void setRootDirectory (String rootDir) {
		this.rootDir = rootDir;
	}

//	/**
//	 * Leverages a text unit using the SimpleTM.
//	 * This uses the base leverage method, but add one extra step:
//	 * It downgrade best matches that are identical.
//	 */
//	//TODO: Should this extra process be in the base leverage with an option to do it or not?
//	@Override
//	public void leverage (TextUnit tu) {
//		// Call the default
//		super.leverage(tu);
//
//		// Check that we have results
//		TextContainer tc = tu.getTarget(getTargetLanguage());
//		if ( tc == null ) return;
//
//		// Proceed to downgrade the identical best matches
//		// Treat the container annotations
//		AltTranslationsAnnotation atAnn = tc.getAnnotation(AltTranslationsAnnotation.class);
//		if ( atAnn != null ) atAnn.downgradeIdenticalBestMatches(false);
//		// Treat each segment
//		for ( Segment seg : tc.getSegments() ) {
//			atAnn = seg.getAnnotation(AltTranslationsAnnotation.class);
//			if ( atAnn != null ) atAnn.downgradeIdenticalBestMatches(false);
//		}
//	}
	
}
