package net.sf.okapi.steps.diffleverage;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import net.sf.okapi.common.Util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FileAlignerTest {
	private URI newFilesRoot;
	private URI oldSrcFilesRoot;
	private URI oldTrgFilesRoot;
	private List<FileLikeThing<String>> newFiles;
	private List<FileLikeThing<String>> oldSrcFiles;
	private List<FileLikeThing<String>> oldTrgFiles;

	@Before
	public void setUp() throws Exception {
		newFilesRoot = Util.toURI("C:/yyy/");
		oldSrcFilesRoot = Util.toURI("C:/zzz/");
		oldTrgFilesRoot = Util.toURI("C:/xxx/");

		newFiles = new LinkedList<FileLikeThing<String>>();
		oldSrcFiles = new LinkedList<FileLikeThing<String>>();
		oldTrgFiles = new LinkedList<FileLikeThing<String>>();

		newFiles.add(new FileLikeThing<String>(Util.toURI("C:/yyy/one"), "C:/yyy/one"));
		newFiles.add(new FileLikeThing<String>(Util.toURI("C:/yyy/two"), "C:/yyy/two"));
		newFiles.add(new FileLikeThing<String>(Util.toURI("C:/yyy/yyy/three"), "C:/yyy/yyy/three"));

		oldSrcFiles.add(new FileLikeThing<String>(Util.toURI("C:/zzz/one"), "C:/zzz/one"));
		oldSrcFiles.add(new FileLikeThing<String>(Util.toURI("C:/zzz/yyy/three"), "C:/zzz/yyy/three"));
		
		oldTrgFiles.add(new FileLikeThing<String>(Util.toURI("C:/xxx/one"), "C:/xxx/one"));
		oldTrgFiles.add(new FileLikeThing<String>(Util.toURI("C:/xxx/yyy/three"), "C:/xxx/yyy/three"));
	}

	@Test
	public void alignTwoWayFiles() {
		FileAligner<String> aligner = new FileAligner<String>(newFiles, oldSrcFiles, newFilesRoot,
				oldSrcFilesRoot);
		aligner.align();
		List<FileAlignment<String>> alignments = aligner.getAlignments();
		Assert.assertTrue(alignments.size() == 3);
		FileAlignment<String> a1 = alignments.get(0);
		Assert.assertTrue(a1.getNew().getFileLikeThing().equals("C:/yyy/one"));

		FileAlignment<String> a3 = alignments.get(2);
		Assert.assertTrue(a3.getNew().getFileLikeThing().equals("C:/yyy/yyy/three"));
	}
	
	@Test
	public void alignThreeWayFiles() {
		FileAligner<String> aligner = new FileAligner<String>(newFiles, oldSrcFiles, oldTrgFiles, newFilesRoot,
				oldSrcFilesRoot, oldTrgFilesRoot);
		aligner.align();
		List<FileAlignment<String>> alignments = aligner.getAlignments();
		Assert.assertTrue(alignments.size() == 3);
		
		FileAlignment<String> a1 = alignments.get(0);
		Assert.assertTrue(a1.getNew().getFileLikeThing().equals("C:/yyy/one"));
		Assert.assertTrue(a1.getOldTrg().getFileLikeThing().equals("C:/xxx/one"));

		FileAlignment<String> a2 = alignments.get(1);
		Assert.assertTrue(a2.getNew().getFileLikeThing().equals("C:/yyy/two"));
		Assert.assertNull(a2.getOldSrc());
		Assert.assertNull(a2.getOldTrg());
		
		FileAlignment<String> a3 = alignments.get(2);
		Assert.assertTrue(a3.getNew().getFileLikeThing().equals("C:/yyy/yyy/three"));
		Assert.assertTrue(a3.getOldTrg().getFileLikeThing().equals("C:/xxx/yyy/three"));				
	}
}
