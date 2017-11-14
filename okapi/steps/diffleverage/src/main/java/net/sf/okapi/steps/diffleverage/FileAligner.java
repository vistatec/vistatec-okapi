package net.sf.okapi.steps.diffleverage;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.okapi.common.exceptions.OkapiException;

/**
 * Match up (align) files based on full path name. There is a bi-lingual case (source matched with target files) and
 * tri-lingual (new source matched with old source, matched with old target).
 * 
 * @author HARGRAVEJE
 * @param <T>
 * 
 */
public class FileAligner<T> implements Iterable<FileAlignment<T>> {
	// used for both new source and source in the bi-lingual case
	private List<FileLikeThing<T>> newFiles;
	private Map<String, FileLikeThing<T>> trgFilesMap;
	private URI newRootUri;
	private Map<String, FileLikeThing<T>> oldSrcFilesMap;
	private Map<String, FileLikeThing<T>> oldTrgFilesMap;
	private List<FileAlignment<T>> alignedFiles;
	private boolean lowerCase;
	private boolean trilingual;

	/**
	 * Tri-lingual alignment (new source matched with old source, matched with old target). This method will lower case
	 * the file paths by default.
	 * 
	 * @param newFiles
	 *            - new source files
	 * @param oldSrcFiles
	 *            - old source files (i.e, from previous translation)
	 * @param oldTrgFiles
	 *            - old target files that match the old source files.
	 * @param newRootUri
	 *            - root directory of the new source files.
	 * @param oldSrcRootUri
	 *            - root directory of the old source files.
	 * @param oldTrgRootUri
	 *            - root directory of the new target files.
	 */
	public FileAligner(List<FileLikeThing<T>> newFiles, List<FileLikeThing<T>> oldSrcFiles,
			List<FileLikeThing<T>> oldTrgFiles, URI newRootUri, URI oldSrcRootUri, URI oldTrgRootUri) {
		this(true, newFiles, oldSrcFiles, oldTrgFiles, newRootUri, oldSrcRootUri, oldTrgRootUri);
	}

	/**
	 * Tri-lingual alignment (new source matched with old source, matched with old target).
	 * 
	 * @param lowerCase
	 *            - true to lower case file paths before matching, false to leave as-is
	 * @param newFiles
	 *            - new source files
	 * @param oldSrcFiles
	 *            - old source files (i.e, from previous translation)
	 * @param oldTrgFiles
	 *            - old target files that match the old source files.
	 * @param newRootUri
	 *            - root directory of the new source files.
	 * @param oldSrcRootUri
	 *            - root directory of the old source files.
	 * @param oldTrgRootUri
	 *            - root directory of the new target files.
	 */
	public FileAligner(boolean lowerCase, List<FileLikeThing<T>> newFiles,
			List<FileLikeThing<T>> oldSrcFiles, List<FileLikeThing<T>> oldTrgFiles, URI newRootUri,
			URI oldSrcRootUri, URI oldTrgRootUri) {
		this.lowerCase = lowerCase;
		this.trilingual = true;
		this.newFiles = newFiles;
		this.newRootUri = newRootUri;

		oldTrgFilesMap = new TreeMap<String, FileLikeThing<T>>();
		oldSrcFilesMap = new TreeMap<String, FileLikeThing<T>>();
		createMatchingMap(lowerCase, oldSrcFilesMap, newFiles, oldSrcFiles, newRootUri,
				oldSrcRootUri);

		// put old files into our sorted map
		for (FileLikeThing<T> f : oldTrgFiles) {
			String key = getRealtivePath(f.getPath(), oldTrgRootUri);
			if (lowerCase) {
				key = key.toLowerCase();
			}

			if (oldTrgFilesMap.containsKey(key)) {
				// FIXME: somehow we have a duplicate, throw an exception for now
				throw new OkapiException("Duplicate path entry: " + key);
			} else {
				oldTrgFilesMap.put(key, f);
			}
		}
	}

	/**
	 * Bi-lingual alignment (match source with target files). This method will lower case the file paths by default.
	 * 
	 * @param srcFiles
	 *            - source files
	 * @param trgFiles
	 *            - target files
	 * @param srcRootUri
	 *            - source root directory
	 * @param trgRootUri
	 *            - target root directory
	 */
	public FileAligner(List<FileLikeThing<T>> srcFiles, List<FileLikeThing<T>> trgFiles,
			URI srcRootUri, URI trgRootUri) {
		this(true, srcFiles, trgFiles, srcRootUri, trgRootUri);
	}

	/**
	 * Bi-lingual alignment (match source with target files)
	 * 
	 * @param lowerCase
	 *            - true to lower case file paths before matching, false to leave as-is.
	 * @param srcFiles
	 *            - source files
	 * @param trgFiles
	 *            - target files
	 * @param srcRootUri
	 *            - source root directory
	 * @param trgRootUri
	 *            - target root directory
	 */
	public FileAligner(boolean lowerCase, List<FileLikeThing<T>> srcFiles,
			List<FileLikeThing<T>> trgFiles, URI srcRootUri, URI trgRootUri) {
		this.newFiles = srcFiles; // reuse newFiles field for bi-lingual alignment
		this.newRootUri = srcRootUri;
		this.lowerCase = lowerCase;
		this.trilingual = false;

		this.trgFilesMap = new TreeMap<String, FileLikeThing<T>>();
		createMatchingMap(lowerCase, trgFilesMap, srcFiles, trgFiles, srcRootUri, trgRootUri);
	}

	/*
	 * Create map of the matching files. Either old source or target.
	 */
	private void createMatchingMap(boolean lowerCase,
			Map<String, FileLikeThing<T>> matchingFileMap, List<FileLikeThing<T>> newFiles,
			List<FileLikeThing<T>> matchingFiles, URI newRootUri, URI matchingRotUri) {

		// put old files into our sorted map
		for (FileLikeThing<T> f : matchingFiles) {
			String key = getRealtivePath(f.getPath(), matchingRotUri);
			if (lowerCase) {
				key = key.toLowerCase();
			}

			if (matchingFileMap.containsKey(key)) {
				// FIXME: somehow we have a duplicate, throw an exception for now
				throw new OkapiException("Duplicate path entry: " + key);
			} else {
				matchingFileMap.put(key, f);
			}
		}
	}

	/**
	 * match up (align) new files (i.e., source our new source) with
	 */
	public void align() {
		alignedFiles = new LinkedList<FileAlignment<T>>();
		for (FileLikeThing<T> f : newFiles) {
			String key = getRealtivePath(f.getPath(), newRootUri);
			if (lowerCase) {
				key = key.toLowerCase();
			}

			FileLikeThing<T> o = null;
			if (trilingual) {
				o = oldSrcFilesMap.get(key);
				if (o != null) {
					if (oldTrgFilesMap != null) {
						FileLikeThing<T> t = oldTrgFilesMap.get(key);
						// we found an old source and matching target
						alignedFiles.add(new FileAlignment<T>(f, o, t));
					} else {
						// we found an old source file without a matching target
						alignedFiles.add(new FileAlignment<T>(f, o));
					}
				} else {
					// this is a new file not found in the old source
					alignedFiles.add(new FileAlignment<T>(f));
				}
			} else {
				o = trgFilesMap.get(key);
				if (o != null) {
					// source file matches target
					alignedFiles.add(new FileAlignment<T>(f, o));
				} else {
					// source file without a target
					alignedFiles.add(new FileAlignment<T>(f));
				}
			}
		}
	}

	/**
	 * Iterator over aligned {@link FileLikeThing}s
	 */
	public Iterator<FileAlignment<T>> iterator() {
		return alignedFiles.iterator();
	}

	/**
	 * Get {@link List} of {@link FileLikeThing}s
	 * 
	 * @return {@link FileAlignment} as a list
	 */
	public List<FileAlignment<T>> getAlignments() {
		return alignedFiles;
	}

	/*
	 * Return path minus the root
	 */
	private static String getRealtivePath(URI path, URI root) {
		String r = path.relativize(root).toString();
		return path.toString().replaceFirst(r, "");
	}
}