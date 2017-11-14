package net.sf.okapi.common;

import static net.sf.okapi.common.ReversedIterator.reverse;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


@RunWith(JUnit4.class)
public class ReversedIteratorTest {

	@Test
	public void reversedList() {
		List<String> l = new LinkedList<String>();		
		l.add("A");
		l.add("B");
		l.add("C");
		
		ReversedIterator<String> ri = new ReversedIterator<String>(l);
		List<String> rl = new LinkedList<String>();
		for (String s : ri) {
			rl.add(s);
		}
		assertEquals("C", rl.remove(0));
		assertEquals("B", rl.remove(0));
		assertEquals("A", rl.remove(0));
	}
	
	@Test
	public void reversedListWithStatic() {
		List<String> l = new LinkedList<String>();		
		l.add("A");
		l.add("B");
		l.add("C");
		
		List<String> rl = new LinkedList<String>();
		for (String s : reverse(l)) {
			rl.add(s);
		}
		assertEquals("C", rl.remove(0));
		assertEquals("B", rl.remove(0));
		assertEquals("A", rl.remove(0));
	}
}
