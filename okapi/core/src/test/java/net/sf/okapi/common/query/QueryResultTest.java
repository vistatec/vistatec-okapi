package net.sf.okapi.common.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import net.sf.okapi.common.resource.TextFragment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class QueryResultTest {
	private TextFragment source1;
	private TextFragment source2;
	private TextFragment source3;

	private TextFragment target1;
	private TextFragment target2;
	private TextFragment target3;
	
	private QueryResult at1;
	private QueryResult at2;
	private QueryResult at3;
	
	private QueryResult at4;
	private QueryResult at5;
	private QueryResult at6;
	
	private QueryResult at7;
	private QueryResult at8;
	private QueryResult at9;
	
	private QueryResult at10;
	private QueryResult at11;
	private QueryResult at12;

	
	@Before
	public void setUp() throws Exception {
		source1 = new TextFragment("source one");
		source2 = new TextFragment("source two");
		source3 = new TextFragment("source three");
		
		target1 = new TextFragment("target one");
		target2 = new TextFragment("target two");
		target3 = new TextFragment("target three");
		
		at1 = new QueryResult();
		at1.matchType = MatchType.MT;
		at1.setFuzzyScore(60);
		at1.source = source1;
		at1.target = target1;
		
		at2 = new QueryResult();
		at2.matchType = MatchType.EXACT;
		at2.setFuzzyScore(90);
		at2.source = source2;
		at2.target = target2;
		
		at3 = new QueryResult();
		at3.matchType = MatchType.EXACT_PREVIOUS_VERSION;
		at3.setFuzzyScore(100);
		at3.source = source3;
		at3.target = target3;
		
		at4 = new QueryResult();
		at4.matchType = MatchType.FUZZY;
		at4.setFuzzyScore(60);
		at4.source = source1;
		at4.target = target1;
		
		at5 = new QueryResult();
		at5.matchType = MatchType.EXACT_TEXT_ONLY;
		at5.setFuzzyScore(95);
		at5.source = source2;
		at5.target = target2;		
		
		at6 = new QueryResult();
		at6.matchType = MatchType.FUZZY_UNIQUE_ID;
		at6.setFuzzyScore(99);
		at6.source = source3;
		at6.target = target3;
		
		at7 = new QueryResult();
		at7.matchType = MatchType.FUZZY;
		at7.setFuzzyScore(99);
		at7.source = source3;
		at7.target = target3;
		
		at8 = new QueryResult();
		at8.matchType = MatchType.FUZZY;
		at8.setFuzzyScore(98);
		at8.creationDate = new Date(2);
		at8.source = source3;
		at8.target = target3;
		
		at9 = new QueryResult();
		at9.matchType = MatchType.FUZZY;
		at9.setFuzzyScore(98);
		at8.creationDate = new Date(1);
		at9.source = source3;
		at9.target = target3;
		
		at10 = new QueryResult();
		at10.matchType = MatchType.FUZZY;
		at10.setFuzzyScore(99);
		at10.source = source3;
		at10.target = target3;
		
		at11 = new QueryResult();
		at11.matchType = MatchType.FUZZY;
		at11.setFuzzyScore(98);
		at11.source = source3;
		at11.target = target3;
		
		at12 = new QueryResult();
		at12.matchType = MatchType.FUZZY;
		at12.setFuzzyScore(97);
		at12.source = source3;
		at12.target = target3;
	}

	@Test
	public void QueryResultSortedList() {
		List<QueryResult> ats = new ArrayList<QueryResult>();
		ats.add(at1);
		ats.add(at2);
		ats.add(at3);
		Collections.sort(ats);
		assertSame(at3, ats.get(0));
		assertSame(at2, ats.get(1));
		assertSame(at1, ats.get(2));
	}
	
	@Test
	public void QueryResultFuzzySortedList() {
		List<QueryResult> ats = new ArrayList<QueryResult>();
		ats.add(at4);
		ats.add(at5);
		ats.add(at6);
		Collections.sort(ats);
		assertSame(at5, ats.get(0));
		assertSame(at6, ats.get(1));
		assertSame(at4, ats.get(2));
	}
	
	@Test
	public void QueryResultCreationDateSortedList() {
		List<QueryResult> ats = new ArrayList<QueryResult>();
		ats.add(at9);
		ats.add(at8);
		ats.add(at7);
		Collections.sort(ats);
		assertSame(at7, ats.get(0));
		assertSame(at8, ats.get(1));
		assertSame(at9, ats.get(2));
	}
	
	@Test
	public void QueryResultScoreSortedList() {
		List<QueryResult> ats = new ArrayList<QueryResult>();
		ats.add(at10);
		ats.add(at11);
		ats.add(at12);
		Collections.sort(ats);
		assertSame(at10, ats.get(0));
		assertSame(at11, ats.get(1));
		assertSame(at12, ats.get(2));
	}
	
	@Test
	public void instanceEquality() {
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.EXACT;
		h1.setFuzzyScore(90);
		h1.source = source2;
		h1.target = target2;
		
		QueryResult h2 = h1;
		assertTrue("instance equality", h1.equals(h2));
	}

	@Test
	public void equals() {		
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.EXACT;
		h1.setFuzzyScore(90);
		h1.source = source2;
		h1.target = target2;

		assertTrue("equals", h1.equals(at2));
	}

	@Test
	public void notEquals() {
		assertFalse("not equals", at1.equals(at2));
	}
	
	@Test 
	public void compareToEquals() {
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.EXACT;
		h1.setFuzzyScore(90);
		h1.source = source2;
		h1.target = target2;
		
		assertEquals(0, h1.compareTo(at2));
	}
	
	@Test 
	public void compareToGreaterThanScore() {
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.FUZZY;
		h1.setFuzzyScore(90);
		h1.source = source2;
		h1.target = target2;

		
		QueryResult h2 = new QueryResult();
		h2.matchType = MatchType.FUZZY;
		h2.setFuzzyScore(50);
		h2.source = source2;
		h2.target = target2;

		assertTrue(h1.compareTo(h2) < 0);
	}
	
	@Test 
	public void compareToLessThanScore() {
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.FUZZY;
		h1.setFuzzyScore(90);
		h1.source = source2;
		h1.target = target2;
		
		QueryResult h2 = new QueryResult();
		h2.matchType = MatchType.FUZZY;
		h2.setFuzzyScore(50);
		h2.source = source2;
		h2.target = target2;
		
		assertTrue(h2.compareTo(h1) > 0);
	}
	
	@Test 
	public void compareToLessThanMatchType() {
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.EXACT;
		h1.setFuzzyScore(90);
		h1.source = source2;
		h1.target = target2;
		
		QueryResult h2 = new QueryResult();
		h2.matchType = MatchType.MT;
		h2.setFuzzyScore(90);
		h2.source = source2;
		h2.target = target2;
		
		assertTrue(h1.compareTo(h2) < 0);
	}
	
	@Test 
	public void compareToGreaterThanMatchType() {
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.EXACT;
		h1.setFuzzyScore(90);
		h1.source = source2;
		h1.target = target2;
		
		QueryResult h2 = new QueryResult();
		h2.matchType = MatchType.MT;
		h2.setFuzzyScore(90);
		h2.source = source2;
		h2.target = target2;
		
		assertTrue(h2.compareTo(h1) > 0);
	}
	
	@Test 
	public void testQuality () {
		QueryResult qr = new QueryResult();
		assertEquals(QueryResult.QUALITY_UNDEFINED, qr.getQuality());
		qr.setQuality(50);
		assertEquals(50, qr.getQuality());
	}
	
	@Test 
	public void testCombinedScore () {
		QueryResult qr = new QueryResult();
		qr.setFuzzyScore(99);
		assertEquals(qr.getFuzzyScore(), qr.getCombinedScore()); // When not set: Should be like score
		qr.setCombinedScore(44);
		assertEquals(44, qr.getCombinedScore());
		qr.setCombinedScore(QueryResult.COMBINEDSCORE_UNDEFINED);
		assertEquals(qr.getFuzzyScore(), qr.getCombinedScore());
	}
	
	@Test 
	public void compareToLessThanSource() {
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.EXACT;
		h1.setFuzzyScore(90);
		h1.source = new TextFragment("A");
		h1.target = target2;
		
		QueryResult h2 = new QueryResult();
		h2.matchType = MatchType.MT;
		h2.setFuzzyScore(90);
		h2.source = new TextFragment("B");
		h2.target = target2;

		assertTrue(h1.compareTo(h2) < 0);
	}
	
	@Test 
	public void compareToGreaterThanSource() {
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.EXACT;
		h1.setFuzzyScore(90);
		h1.source = new TextFragment("A");
		h1.target = target2;
		
		QueryResult h2 = new QueryResult();
		h2.matchType = MatchType.MT;
		h2.setFuzzyScore(90);
		h2.source = new TextFragment("B");
		h2.target = target2;
		
		assertTrue(h2.compareTo(h1) > 0);
	}
	
	@Test 
	public void compareToWithCreationDates() {
		QueryResult h1 = new QueryResult();
		h1.matchType = MatchType.EXACT;
		h1.setFuzzyScore(100);
		h1.source = new TextFragment("A");
		h1.target = target2;
		h1.creationDate = new Date(0);
		
		QueryResult h2 = new QueryResult();
		h2.matchType = MatchType.EXACT;
		h2.setFuzzyScore(100);
		h2.source = new TextFragment("A");
		h2.target = target2;
		h2.creationDate = new Date(1);
		
		// h2 > h1
		assertTrue(h2.compareTo(h1) < 0);
	}
}
