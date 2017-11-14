package net.sf.okapi.steps.diffleverage;

/**
 * Stores alignments of {@link FileLikeThing}s
 * 
 * @author HargraveJE
 * 
 * @param <T>
 */
public class FileAlignment<T> {
	// new src file or src in bi-lingual case
	private FileLikeThing<T> newFile;
	private FileLikeThing<T> trgFile;

	private FileLikeThing<T> oldSrcFile;
	// optional target file aligned with old source
	private FileLikeThing<T> oldTrgFile;

	/**
	 * Bi-lingual alignment
	 * 
	 * @param newFile
	 *            source file
	 * @param trgFile
	 *            target file
	 */
	public FileAlignment(FileLikeThing<T> newFile, FileLikeThing<T> trgFile) {
		this.newFile = newFile;
		this.trgFile = trgFile;
	}

	/**
	 * Tri-lingual alignment
	 * 
	 * @param newFile
	 *            new source file
	 * @param oldSrcFile
	 *            old source file
	 * @param oldTrgFile
	 *            old target file
	 */
	public FileAlignment(FileLikeThing<T> newFile, FileLikeThing<T> oldSrcFile,
			FileLikeThing<T> oldTrgFile) {
		this.newFile = newFile;
		this.oldSrcFile = oldSrcFile;
		this.oldTrgFile = oldTrgFile;
	}

	/**
	 * Create a 1-0 alignment (source only)
	 * 
	 * @param newFile
	 *            new source file
	 */
	public FileAlignment(FileLikeThing<T> newFile) {
		this.newFile = newFile;
	}

	/**
	 * Get the new source {@link FileLikeThing}
	 * 
	 * @return new source {@link FileLikeThing}
	 */
	public FileLikeThing<T> getNew() {
		return newFile;
	}

	/**
	 * Get the old source {@link FileLikeThing} (matches the new source)
	 * 
	 * @return old source {@link FileLikeThing}
	 */
	public FileLikeThing<T> getOldSrc() {
		return oldSrcFile;
	}

	/**
	 * Get old target {@link FileLikeThing}
	 * 
	 * @return old target {@link FileLikeThing} (matches old source)
	 */
	public FileLikeThing<T> getOldTrg() {
		return oldTrgFile;
	}

	/**
	 * Get target {@link FileLikeThing} (matches "new" source file)
	 * 
	 * @return the trgFile
	 */
	public FileLikeThing<T> getTrgFile() {
		return trgFile;
	}
}