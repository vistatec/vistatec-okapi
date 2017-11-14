package net.sf.okapi.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class IdGeneratorTest {
	
	private IdGenerator idGen;
	
	@Test
	public void testresultNotNull () {
		idGen = new IdGenerator("test");
		assertNotNull(idGen.createId());
		assertNotNull(idGen.createId());
	}

	@Test
	public void testresultNotEmpty () {
		idGen = new IdGenerator("test");
		assertTrue(idGen.createId().length()>0);
		assertTrue(idGen.createId().length()>0);
	}

	@Test
	public void testCreationWithNullRoot () {
		idGen = new IdGenerator(null);
		assertEquals("1", idGen.createId());
		assertEquals("1", idGen.getLastId());
	}
	
	@Test
	public void testCreationWithEmptyRoot () {
		idGen = new IdGenerator("");
		assertEquals("1", idGen.createId());
		assertEquals("1", idGen.getLastId());
	}
	
	@Test
	public void testWithNullPrefix () {
	   	idGen = new IdGenerator("test");
	   	String id1 = idGen.createId();
	   	idGen = new IdGenerator("test", null);
	   	assertEquals(id1, idGen.createId());
	}

	@Test
	public void testWithEmptyPrefix () {
		idGen = new IdGenerator("test");
		String id1 = idGen.createId();
		idGen = new IdGenerator("test", "");
		assertEquals(id1, idGen.createId());
	}

	@Test
	public void testSameIdForSameRoot () {
		idGen = new IdGenerator("test");
		String id1 = idGen.createId();
		String id2 = idGen.createId();
		idGen = new IdGenerator("test");
		assertTrue(id1.equals(idGen.createId()));
		assertTrue(id2.equals(idGen.createId()));
   }

	@Test
	public void testDifferentIdForDifferentRoot () {
		idGen = new IdGenerator("test");
		String id1 = idGen.createId();
		String id2 = idGen.createId();
		idGen = new IdGenerator("Test"); // Case difference
		// Should get different IDs
		assertFalse(id1.equals(idGen.createId()));
		assertFalse(id2.equals(idGen.createId()));
	}

	@Test
	public void testresultNotNullWithPrefix () {
		idGen = new IdGenerator("test", "p");
		assertNotNull(idGen.createId());
		assertNotNull(idGen.createId());
	}

	@Test
	public void testresultNotEmptyWithPrefix () {
		idGen = new IdGenerator("test", "p");
		assertTrue(idGen.createId().length() > 0);
		assertTrue(idGen.createId().length() > 0);
	}

	@Test
	public void testSameIdForSameRootAndPrefix () {
		idGen = new IdGenerator("test", "p");
		String id1 = idGen.createId();
		String id2 = idGen.createId();
		idGen = new IdGenerator("test", "p");
		assertTrue(id1.equals(idGen.createId()));
		assertTrue(id2.equals(idGen.createId()));
	}

	@Test
	public void testDifferentIdForDifferentRootSamePrefix () {
		idGen = new IdGenerator("test", "p");
		String id1 = idGen.createId();
		String id2 = idGen.createId();
		idGen = new IdGenerator("Test", "p"); // Case difference
		// Should get different IDs
		assertFalse(id1.equals(idGen.createId()));
		assertFalse(id2.equals(idGen.createId()));
	}

	@Test
	public void testDifferentIdForSameRootDifferentPrefix () {
		idGen = new IdGenerator("test", "p");
		String id1 = idGen.createId();
		String id2 = idGen.createId();
		idGen = new IdGenerator("test", "P"); // Case difference
		// Should get different IDs
		assertFalse(id1.equals(idGen.createId()));
		assertFalse(id2.equals(idGen.createId()));
	}

	@Test
	public void testDifferentIdForDifferentRootAndPrefix () {
		idGen = new IdGenerator("test", "p");
		String id1 = idGen.createId();
		String id2 = idGen.createId();
		idGen = new IdGenerator("Test", "P"); // Case difference
		// Should get different IDs
		assertFalse(id1.equals(idGen.createId()));
		assertFalse(id2.equals(idGen.createId()));
	}

	@Test
	public void testCanReproduceValue () {
		// The String.hashCode() is the same across platform normally
		idGen = new IdGenerator("test", "p");
		String id1 = idGen.createId();
		String id2 = idGen.createId();
		assertEquals("P364492-p1", id1);
		assertEquals("P364492-p2", id2);
		idGen = new IdGenerator("test/A/b/C");
		id1 = idGen.createId();
		id2 = idGen.createId();
		assertEquals("P269F9F4B-1", id1);
		assertEquals("P269F9F4B-2", id2);
	}

	@Test
	public void testLastId () {
		idGen = new IdGenerator("test", "p");
		String id = idGen.createId();
		String bis = null;
		for ( int i=0; i<10; i++ ) {
			bis  = idGen.getLastId();
		}
		assertEquals(id, bis);
	}

	@Test
	public void testCreateIdWithPrefix () {
		idGen = new IdGenerator(null, "p");
		String id = idGen.createId();
		assertEquals("p1", id);
		id = idGen.createId("xyz");
		assertEquals("xyz2", id);
		id = idGen.createId();
		assertEquals("p3", id);
	}

	@Test
	public void testLastIdWithPrefix () {
		idGen = new IdGenerator(null, "p");
		String id = idGen.createId("zxc");
		assertEquals("zxc1", id);
		assertEquals("zxc1", idGen.getLastId());
		assertEquals("p2", idGen.createId());
//Not working or not logical		assertEquals("zxc1", idGen.getLastId("zxc"));
	}

	@Test
	public void testToString () {
		idGen = new IdGenerator("test", "p");
		String id = idGen.createId();
		// toString() is the same as getLastId()
		assertEquals(idGen.toString(), idGen.getLastId());
		assertEquals(id, idGen.toString());
	}

}
