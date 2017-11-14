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

package net.sf.okapi.lib.extra.diff.incava;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DiffTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStrings1() {
		String[] a = { "a", "b", "c", "e", "h", "j", "l", "m", "n", "p" };
		String[] b = { "b", "c", "d", "e", "f", "j", "k", "l", "m", "r", "s", "t" };
		Difference[] expected = { new Difference(0, 0, 0, -1), new Difference(3, -1, 2, 2),
				new Difference(4, 4, 4, 4), new Difference(6, -1, 6, 6),
				new Difference(8, 9, 9, 11) };

		DiffLists<String> d = new DiffLists<String>(a, b);
		List<Difference> diffs = d.diff();

		int i = 0;
		for (Difference diff : diffs) {
			assertEquals(expected[i++], diff);
		}
	}

	@Test
	public void testStrings2() {
		String[] a = { "a", "b", "c", "d" };
		String[] b = { "c", "d" };
		Difference[] expected = { new Difference(0, 1, 0, -1) };

		DiffLists<String> d = new DiffLists<String>(a, b);
		List<Difference> diffs = d.diff();

		int i = 0;
		for (Difference diff : diffs) {
			assertEquals(expected[i++], diff);
		}
	}

	@Test
	public void testStrings3() {
		String[] a = { "a", "b", "c", "d", "x", "y", "z" };
		String[] b = { "c", "d" };
		Difference[] expected = { new Difference(0, 1, 0, -1), new Difference(4, 6, 2, -1) };

		DiffLists<String> d = new DiffLists<String>(a, b);
		List<Difference> diffs = d.diff();

		int i = 0;
		for (Difference diff : diffs) {
			assertEquals(expected[i++], diff);
		}
	}

	@Test
	public void testStrings4() {
		String[] a = { "a", "b", "c", "d", "e" };
		String[] b = { "a", "x", "y", "b", "c", "j", "e" };
		Difference[] expected = { new Difference(1, -1, 1, 2), new Difference(3, 3, 5, 5) };

		DiffLists<String> d = new DiffLists<String>(a, b);
		List<Difference> diffs = d.diff();

		int i = 0;
		for (Difference diff : diffs) {
			assertEquals(expected[i++], diff);
		}
	}

	@Test
	public void testInteger() {
		Integer[] a = { new Integer(1), new Integer(2), new Integer(3) };
		Integer[] b = { new Integer(2), new Integer(3) };
		Difference[] expected = { new Difference(0, 0, 0, -1) };

		DiffLists<Integer> d = new DiffLists<Integer>(a, b);
		List<Difference> diffs = d.diff();

		int i = 0;
		for (Difference diff : diffs) {
			assertEquals(expected[i++], diff);
		}
	}

	@Test
	public void testStringsMatches() {
		String[] a = { "a", "b", "c", "d", "e" };
		String[] b = { "a", "x", "y", "b", "c", "j", "e" };

		DiffLists<String> d = new DiffLists<String>(a, b);
		Map<Integer, Integer> matches = d.getMatches();
		
		assertEquals(0, (int)matches.get(0));
		assertEquals(3, (int)matches.get(1));
		assertEquals(4, (int)matches.get(2));
		assertEquals(6, (int)matches.get(4));
	}

	@Test
	public void testIntegerMatches() {
		Integer[] a = { new Integer(1), new Integer(2), new Integer(3) };
		Integer[] b = { new Integer(2), new Integer(3) };		

		DiffLists<Integer> d = new DiffLists<Integer>(a, b);		
		Map<Integer, Integer> matches = d.getMatches();
		
		assertEquals(0, (int)matches.get(1));
		assertEquals(1, (int)matches.get(2));		
	}
}
