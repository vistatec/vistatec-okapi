/*===========================================================================
  Copyright (C) 2010-2011 by the Okapi Framework contributors
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

package net.sf.okapi.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import net.sf.okapi.common.exceptions.OkapiException;
import net.sf.okapi.common.exceptions.OkapiIOException;

/**
 * Helper methods for manipulating files.
 */
public final class FileUtil {

	/**
	 * Gets an array of the files in a given directory.
	 * <p>
	 * This method searches all {@link File}s recursively that pass the
	 * {@link FilenameFilter}. Adapted from
	 * http://snippets.dzone.com/posts/show/1875
	 * 
	 * @param directory
	 *            root directory
	 * @param filter
	 *            {@link FilenameFilter} used to filter the File candidates
	 * @param recurse
	 *            true to recurse in the sub-directories, false to not.
	 * @return an array of {@link File}s (File[])
	 */
	public static File[] getFilteredFilesAsArray(File directory,
			FilenameFilter filter, boolean recurse) {
		Collection<File> files = FileUtil.getFilteredFiles(directory, filter,
				recurse);
		File[] arr = new File[files.size()];
		return files.toArray(arr);
	}

	/**
	 * Gets a collection of the files in a given directory.
	 * <p>
	 * This method search all {@link File}s recursively that pass the
	 * {@link FilenameFilter}. Adapted from
	 * http://snippets.dzone.com/posts/show/1875
	 * 
	 * @param directory
	 *            root directory
	 * @param filter
	 *            {@link FilenameFilter} used to filter the File candidates
	 * @param recurse
	 *            true to recurse in the sub-directories, false to not.
	 * @return {@link Collection} of {@link File}s
	 */
	public static Collection<File> getFilteredFiles(File directory,
			FilenameFilter filter, boolean recurse) {
		// List of files / directories
		List<File> files = new LinkedList<File>();

		// Get files / directories in the directory
		File[] entries = directory.listFiles();

		if (entries == null) {
			return files;
		}

		// Go over entries
		for (File entry : entries) {
			// If there is no filter or the filter accepts the
			// file / directory, add it to the list
			if (filter == null || filter.accept(directory, entry.getName())) {
				files.add(entry);
			}

			// If the file is a directory and the recurse flag
			// is set, recurse into the directory
			if (recurse && entry.isDirectory()) {
				files.addAll(getFilteredFiles(entry, filter, recurse));
			}
		}

		// Return collection of files
		return files;
	}

	// Out of guessLanguages for better performance
	private static final Pattern pattern = Pattern
			.compile(
					"\\s(srclang|source-?language|xml:lang|lang|trglang|(target)?locale|(target-?)?language)\\s*?=\\s*?['\"](.*?)['\"]",
					Pattern.CASE_INSENSITIVE);
	/**
	 * Tries to guess the language(s) declared in the given input file. The
	 * method should work with XLIFF, TMX, TTX and TS files.
	 * <p>
	 * The methods looks in the file line by line, in the 10 first KB, or until
	 * a source and at least one target are detected, whichever comes first.
	 * <p>
	 * The encoding for the file is determined based on the BOM, if present.
	 * @param path
	 *            the full path of the file to process.
	 * @return a list of strings that can be empty (never null). The first
	 *         string is the possible source language, the next strings are the
	 *         potential target languages.
	 */
	public static List<String> guessLanguages(String path) {
		InputStreamReader reader = null;
		String encoding = Charset.defaultCharset().name();
		// Deal with the potential BOM
		try (FileInputStream fis = new FileInputStream(path); 
				BOMAwareInputStream bis = new BOMAwareInputStream(fis, encoding)) {								
			encoding = bis.detectEncoding();			
			reader = new InputStreamReader(fis, encoding);
			return guessLanguages(reader);
		}
		catch (Exception e) {
			throw new OkapiException("Error while trying to guess language information.\n"
							+ e.getLocalizedMessage());
		}
	}

	/**
	 * Tries to guess the language(s) declared in the given input. The
	 * method should work with XLIFF, TMX, TTX and TS files.
	 * <p>
	 * The methods looks in the file line by line, in the 10 first KB, or until
	 * a source and at least one target are detected, whichever comes first.
	 *
	 * @param reader
	 * 		   a reader providing the content to examine.  This reader will be closed
	 *         by this method.
	 * @return a list of strings that can be empty (never null). The first
	 *         string is the possible source language, the next strings are the
	 *         potential target languages.
	 */
	public static List<String> guessLanguages(Reader reader) {
		ArrayList<String> list = new ArrayList<String>();

		try {
			final int BYTES_TO_SCAN = 10240*10;
			char[] buffer = new char[BYTES_TO_SCAN];

			// Read the top of the file
			String trgValue = null;

			int readCount = reader.read(buffer, 0, BYTES_TO_SCAN);
			if (readCount <= 0)
				return list;
			String line = new String(buffer, 0, readCount);

			// Else: Try the detect the language codes
			// For XLIFF: source-language, xml:lang, lang, target-language
			// For TMX: srcLang, xml:lang, lang
			// For TTX: SourceLanguage, TargetLanguage, Lang
			// For TS: sourcelanguage, language
			// For TXML: locale, targetlocale
			// Note: the order matter: target cases should be last
			Matcher m = pattern.matcher(line);

			while (m.find()) {
				String lang = m.group(4).toLowerCase();
				if (lang.isEmpty()) {
					continue;
				}
				String name = m.group(1).toLowerCase();

				// If we have a header-type target declaration
				if (name.equals("language") || name.startsWith("target") || name.equals("trglang")) {
					if (list.isEmpty()) {
						// Note that we don't do anything to handle a second
						// match, but that should be OK
						trgValue = lang;
						continue; // Move to the next
					}
					// Else: we can add to the normal list as the source is
					// defined already
				}

				// Else: add the language
				if (!list.contains(lang)) {
					list.add(lang);
				}
				// Then check if we have a target to add. This will be done only
				// once.
				if (trgValue != null) {
					// Add the target
					list.add(trgValue);
					trgValue = null;
				}

				if (list.size() > 1)
					break;
			}

		} catch (Throwable e) {
			throw new OkapiException("Error while trying to guess language information.\n"
							+ e.getLocalizedMessage());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// Swallow this error
				}
			}			
		}
		return list;
	}

	private static final Pattern XLIFF_SEGMENTATION_PATTERN = Pattern
	        .compile(
	                "<\\s*seg-source\\s*>",
	                Pattern.CASE_INSENSITIVE);

	/**
     * Scans xliff file to see if it is segmented (has &lt;seg-source&gt;)
     * @param path
     *            the full path of the xliff file to process.
     * @return a true if xliff is segmented, false otherwise.
     */
    public static boolean isXliffSegmented(String path) {
        InputStreamReader reader = null;       
        String encoding = Charset.defaultCharset().name();       
        
        // Deal with the potential BOM
		try (FileInputStream fis = new FileInputStream(path); 
				BOMAwareInputStream bis = new BOMAwareInputStream(fis, encoding)) {            
            encoding = bis.detectEncoding();            

            reader = new InputStreamReader(fis, encoding);
            return isXliffSegmented(reader);
		}
		catch (Exception e) {
            throw new OkapiException("Error while trying to find xliff seg-source.\n"
                            + e.getLocalizedMessage());
		}
    }

	/**
     * Scans xliff file to see if it is segmented (has &lt;seg-source&gt;)
	 * @param reader
	 * 		   a reader providing the xliff content to examine.  This reader
	 *         will be closed by this method.
     * @return a true if xliff is segmented, false otherwise.
     */
    public static boolean isXliffSegmented(Reader reader) {
    	try {
            final int BYTES_TO_SCAN = 10240;
            char[] buffer = new char[BYTES_TO_SCAN];

            int readCount = reader.read(buffer, 0, BYTES_TO_SCAN);
            if (readCount <= 0)
                return false;
            String line = new String(buffer, 0, readCount);

            // Else: Try the detect seg-source
            Matcher m = XLIFF_SEGMENTATION_PATTERN.matcher(line);
            if (m.find()) {
                return true;
            }            
            return false;       
        } catch (Throwable e) {
            throw new OkapiException("Error while trying to find xliff seg-source.\n"
                            + e.getLocalizedMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Swallow this error
                }
            }            
        }        
    }
    
	/**
	 * Compresses a given directory. Creates in the same parent folder a ZIP
	 * file with the folder name as the file name and a given extension. The
	 * given directory is not deleted after compression.
	 * <p>
	 * This method uses the Java ZIP package and does not supports files to zip
	 * that have a path with extended characters.
	 * 
	 * @param sourceDir
	 *            the given directory to be compressed
	 * @param zipExtension
	 *            an extension for the output ZIP file (default is .zip if a
	 *            null or empty string is passed by the caller). The extension
	 *            is expected to contain the leading period.
	 */
	public static void zipDirectory(String sourceDir, String zipExtension) {
		zipDirectory(sourceDir, zipExtension, null);
	}

	/**
	 * Compresses a given directory. The given directory is not deleted after
	 * compression.
	 * <p>
	 * This method uses the Java ZIP package and does not supports files to zip
	 * that have a path with extended characters.
	 * 
	 * @param sourceDir
	 *            the given directory to be compressed
	 * @param zipExtension
	 *            an extension for the output ZIP file (default is .zip if a
	 *            null or empty string is passed by the caller). The extension
	 *            is expected to contain the leading period.
	 * @param destinationPathWithoutExtension
	 *            output path of the zip file, without extension. Use null to
	 *            use the source directory path.
	 */
	public static void zipDirectory(String sourceDir, String zipExtension,
			String destinationPathWithoutExtension) {
		ZipOutputStream os = null;
		String zipPath = null;

		if (Util.isEmpty(destinationPathWithoutExtension)) {
			// Use the directory as the destination path
			if (sourceDir.endsWith(File.separator) || sourceDir.endsWith("/")) {
				zipPath = sourceDir.substring(0, sourceDir.length() - 1);
			} else {
				zipPath = sourceDir;
			}
		} else { // destinationPathWithoutExtension is specified
			zipPath = destinationPathWithoutExtension;
		}
		// Set the extension
		if (Util.isEmpty(zipExtension)) {
			zipPath += ".zip";
		} else {
			zipPath += zipExtension;
		}

		// Compress the directory
		try {
			File dir = new File(sourceDir);
			if (!dir.isDirectory()) {
				return;
			}
			os = new ZipOutputStream(new FileOutputStream(zipPath));
			addDirectoryToZip(dir, os, null);
		} catch (IOException e) {
			throw new OkapiIOException("Error while zipping.", e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					throw new OkapiIOException(
							"Error closing stream while zipping.", e);
				}
			}
		}
	}

	/**
	 * Creates a ZIP file and adds a list of files in it.
	 * 
	 * @param zipPath
	 *            the path of the ZIP file to create.
	 * @param sourceDir
	 *            the path of the directory where the source files are located.
	 * @param filenames
	 *            the list of files to zip.
	 */
	public static void zipFiles(String zipPath, String sourceDir,
			String... filenames) {
		ZipOutputStream os = null;
		try {
			// Creates the zip file
			Util.createDirectories(zipPath);
			os = new ZipOutputStream(new FileOutputStream(zipPath));
			for (String name : filenames) {
				File file = new File(Util.ensureSeparator(sourceDir, true)
						+ name);
				addFileToZip(file, os);
			}
		} catch (IOException e) {
			throw new OkapiIOException("Error while zipping.", e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					throw new OkapiIOException(
							"Error closing stream while zipping.", e);
				}
			}
		}
	}

	/**
	 * Adds a file to a ZIP output.
	 * 
	 * @param file
	 *            the directory to add.
	 * @param os
	 *            the output stream where to add it.
	 * @throws IOException
	 *             signals an I/O error.
	 */
	private static void addFileToZip(File file, ZipOutputStream os)
			throws IOException {
		FileInputStream input = null;
		try {
			byte[] aBuf = new byte[1024];
			input = new FileInputStream(file);
			os.putNextEntry(new ZipEntry(file.getName()));
			int nCount;
			while ((nCount = input.read(aBuf)) > 0) {
				os.write(aBuf, 0, nCount);
			}
			os.closeEntry();
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}
	
	public static void addFileToZip(File zipFile, String file) throws IOException {
		File[] files = new File[1];
		files[0] = new File(file);
		addFilesToZip(zipFile, files);
	}

	public static void addFilesToZip(File zipFile, File[] files) throws IOException {
		File tempFile = null;
		ZipInputStream zin = null;
		ZipOutputStream out = null;
		byte[] buf = null;

		try {
			// get a temp file
			tempFile = File.createTempFile("~okapi-12_" + zipFile.getName(), null, new File(zipFile.getParent()));
			// delete it, otherwise you cannot rename your existing zip to it.
			tempFile.delete();

			boolean renameOk = zipFile.renameTo(tempFile);
			if (!renameOk) {
				throw new OkapiException("could not rename the file " + zipFile.getAbsolutePath()
						+ " to " + tempFile.getAbsolutePath());
			}
			buf = new byte[1024];

			zin = new ZipInputStream(new FileInputStream(tempFile));
			out = new ZipOutputStream(new FileOutputStream(zipFile));

			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				boolean notInFiles = true;
				for (File f : files) {
					if (f.getName().equals(name)) {
						notInFiles = false;
						break;
					}
				}
				if (notInFiles) {
					// Add ZIP entry to output stream.
					out.putNextEntry(new ZipEntry(name));
					// Transfer bytes from the ZIP file to the output file
					int len;
					while ((len = zin.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
				}
				entry = zin.getNextEntry();
			}
		} finally {
			// Close the streams
			if (zin != null) zin.close();
			if (out != null) {
				// Compress the files
				for (int i = 0; i < files.length; i++) {
					InputStream in = new FileInputStream(files[i]);
					// Add ZIP entry to output stream.
					out.putNextEntry(new ZipEntry(files[i].getName()));
					// Transfer bytes from the file to the ZIP file
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					// Complete the entry
					out.closeEntry();
					in.close();
				}
				out.close();
			}
			if (tempFile != null) tempFile.delete();
		}
	}

	/**
	 * Adds a directory to a ZIP output.
	 * 
	 * @param dir
	 *            the directory to add.
	 * @param os
	 *            the output stream where to add it.
	 * @param subDir
	 *            the sub-directory.
	 * @throws IOException
	 *             signals an I/O error.
	 */
	private static void addDirectoryToZip(File dir, ZipOutputStream os,
			String subDir) throws IOException {
		FileInputStream input = null;
		try {
			byte[] aBuf = new byte[1024];
			for (File file : dir.listFiles()) {
				// Go recursively if the entry is a sub-directory
				if (file.isDirectory()) {
					addDirectoryToZip(file, os, ((subDir == null) ? "" : subDir
							+ "\\")
							+ file.getName());
					continue;
				}
				// Or add the file to the zip
				input = new FileInputStream(file.getPath());
				os.putNextEntry(new ZipEntry(((subDir == null) ? "" : subDir
						+ "\\")
						+ file.getName()));

				int nCount;
				while ((nCount = input.read(aBuf)) > 0) {
					os.write(aBuf, 0, nCount);
				}
				os.closeEntry();
			}
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	/**
	 * Extract a given ZIP file to a given destination folder. From
	 * http://www.java2s
	 * .com/Code/Java/File-Input-Output/Extractzipfiletodestinationfolder.htm
	 * 
	 * @param zipFileName
	 *            full path of the given ZIP file
	 * @param destPath
	 *            destination folder
	 */
	public static void unzip(String zipFileName, String destPath) {
		ZipInputStream in = null;
		OutputStream out = null;
		// Create the directory where we will be unziping everything
		Util.createDirectories(destPath);

		try {
			// Open the ZIP file
			in = new ZipInputStream(new FileInputStream(zipFileName));
			byte[] buf = new byte[1024];
			ZipEntry entry = null;

			// Process the entries
			while ((entry = in.getNextEntry()) != null) {
				String outFilename = entry.getName();
				if (entry.isDirectory()) {
					new File(destPath, outFilename).mkdirs();
				} else {
					// Check if the entry has sub-directories, create them if
					// needed
					if (!Util.getDirectoryName(outFilename).isEmpty()) {
						File f = new File(destPath, outFilename);
						Util.createDirectories(f.getAbsolutePath());
					}
					out = new FileOutputStream(new File(destPath, outFilename));
					// Transfer bytes from the ZIP file to the output file
					int len;
					while ((len = in.read(buf)) > 0) {
						out.write(buf, 0, len);
					}
					// Close the stream
					out.close();
				}
			}
		} catch (IOException e) {
			throw new OkapiIOException("Error unzipping file.\n"
					+ e.getMessage(), e);
		} finally {
			// Close the stream
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new OkapiIOException(
							"Error closing input while unzipping file.", e);
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new OkapiIOException(
							"Error closing output while unzipping file.", e);
				}
			}
		}
	}

	/**
	 * Delete all files in the specified directory.
	 * 
	 * @throws OkapiIOException
	 *             if a file cannot be deleted.
	 * @param directoryPath
	 *            - the path to the directory
	 */
	public static void deleteAllFilesInDirectory(String directoryPath) {
		File directory = new File(directoryPath);
		// Get all files in directory
		File[] files = directory.listFiles();

		if (files == null) {
			throw new OkapiIOException("Error finding directory: "
					+ directoryPath);
		}

		for (File file : files) {
			// Delete each file
			if (!file.delete()) {
				throw new OkapiIOException("Error deleting file: "
						+ file.getPath());
			}
		}
	}

	/**
	 * Return a path to a locale based resource using the standard java property resource resolution. Works
	 * with any kind of files e.g., segmenter_en_US.srx or content_fr_FR.html
	 * <p><b>WARNING: Assumes default classLoader only!!</b> 
	 * @param baseName base name of the resource
	 * @param extension resource file extension
	 * @param locale locale of the resource we are looking for
	 * @return the path to the resource or null if not found
	 */
	public static String getLocaleBasedFile(String baseName, final String extension, LocaleId locale) {
		ResourceBundle.Control control = new ResourceBundle.Control() {
			private String resourceFound = null;

			@Override
			public List<String> getFormats(String baseName) {
				return Arrays.asList(extension);
			}

			@Override
			public ResourceBundle newBundle(String baseName, Locale locale, 
							String format, ClassLoader loader, boolean reload)
					throws IllegalAccessException, InstantiationException,	IOException {
				String bundleName = toBundleName(baseName, locale);
				String resourceName = toResourceName(bundleName, format);

				URL r = loader.getResource(resourceName);
				if (r != null) {
					resourceFound = new File(Util.URLtoURI(r)).getPath();					
					return new ResourceBundle() {

						@Override
						public Enumeration<String> getKeys() {
							return null;
						}

						@Override
						protected Object handleGetObject(String key) {
							return null;
						}
					};
				}
				return null;
			}

			@Override
			public String toString() {
				return resourceFound;
			}
		};
		ResourceBundle.clearCache();
		ResourceBundle.getBundle(baseName, locale.toJavaLocale(), control);
		return control.toString();
	}

	/**
	 * Gets the URI part before the file name.
	 * @param uri The URI to process.
	 * @return the URI part before the file name.
	 */
	public static String getPartBeforeFile (URI uri) {
		String tmp = uri.toString();
		int n = tmp.lastIndexOf('/');
		if ( n == -1 ) return uri.toString();
		else return tmp.substring(0, n+1);
	}
	
	public static File createTempFile(String prefix, String extension) {
		try {
			return File.createTempFile(prefix, extension);
		} catch (IOException e) {
			throw new OkapiIOException("Cannot create temporary file.", e);
		}		
	}
	
	public static File createTempFile(String prefix) {
		return createTempFile(prefix, ".tmp");
	}
	
	public static void deleteFileFromZip(File zipFile, String file) throws IOException {
		String[] files = new String[1];
		files[0] = file;
		deleteFilesFromZip(zipFile, files);
	}

	public static void deleteFilesFromZip(File zipFile, String[] files) throws IOException {
		ZipInputStream zin = null;
		ZipOutputStream zout = null;
		File tempFile = null;

		try {
			// get a temp file
			tempFile = File.createTempFile(zipFile.getName(), null, new File(zipFile.getParent()));
			// delete it, otherwise you cannot rename your existing zip to it.
			tempFile.delete();
			boolean renameOk = zipFile.renameTo(tempFile);
			if (!renameOk) {
				throw new OkapiException("Could not rename the file " + zipFile.getAbsolutePath()
						+ " to " + tempFile.getAbsolutePath());
			}
			byte[] buf = new byte[1024];

			zin = new ZipInputStream(new FileInputStream(tempFile));
			zout = new ZipOutputStream(new FileOutputStream(zipFile));

			ZipEntry entry = zin.getNextEntry();
			while (entry != null) {
				String name = entry.getName();
				boolean toBeDeleted = false;
				for (String f : files) {
					if (f.equals(name)) {
						toBeDeleted = true;
						break;
					}
				}
				if (!toBeDeleted) {
					// Add ZIP entry to output stream.
					zout.putNextEntry(new ZipEntry(name));
					// Transfer bytes from the ZIP file to the output file
					int len;
					while ((len = zin.read(buf)) > 0) {
						zout.write(buf, 0, len);
					}
				}
				entry = zin.getNextEntry();
			}
		} finally {
			if (zin != null) {
				zin.close();
			}

			if (zout != null) {
				zout.close();
			}

			if (tempFile != null) {
				tempFile.delete();
			}
		}
	}
	
	public static URL fileToUrl(File file) {
		try {
			return file.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new OkapiException(e);
		}
	}

	public static File urlToFile(URL url) {
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			throw new OkapiException(e);
		}
	}
}
