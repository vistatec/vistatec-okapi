package net.sf.okapi.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * User: Christian Hargraves
 * Date: Aug 13, 2009
 * Time: 8:08:16 AM
 */
@RunWith(JUnit4.class)
public class TestUtilTest {

    @Test
    public void getParentDir_FileNotFound() {
        assertNull("A parent directory for a nonexistent file should be null",
                TestUtil.getParentDir(this.getClass(), "some/nonexistent/file/that/could/no/way/exist.txt"));
    }

    @Test
    public void getParentDir_ValidFile() {
        String parentDir = TestUtil.getParentDir(this.getClass(), "/TestUtilTestTestFile.txt");
        assertNotNull(parentDir);
        assertTrue("Incorrect path returned", parentDir.endsWith("test-classes/"));
    }

}
