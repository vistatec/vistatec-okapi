package net.sf.okapi.connectors.mymemory;

import net.sf.okapi.common.LocaleId;
import net.sf.okapi.common.query.QueryResult;
import net.sf.okapi.common.resource.TextFragment;

public class Manual {

	public static void main (String[] args) {
		MyMemoryTMConnector conn = new MyMemoryTMConnector();
		conn.setThreshold(10);
		conn.setMaximumHits(10);
		conn.setLanguages(LocaleId.ENGLISH, LocaleId.FRENCH);
		conn.open();

		TextFragment tf = new TextFragment("Open the Download window");
		
		conn.query(tf);
		while ( conn.hasNext() ) {
			QueryResult qr = conn.next();
			System.out.println("\nScore = "+String.valueOf(qr.getCombinedScore())+"\n"+qr.source.toText()+"\n"+qr.target.toText());
			System.out.println("Origin = "+qr.origin);
		}
	}

}
