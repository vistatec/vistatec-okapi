package net.sf.okapi.filters.openxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import net.sf.okapi.common.FileCompare;
import net.sf.okapi.common.FileUtil;
import net.sf.okapi.common.Util;

import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonResult;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.DifferenceEvaluator;

/**
 * Class to compare to perform XML-aware comparisons (as opposed to
 * byte-for-byte comparisons) of two OpenXML documents.
 */
public class OpenXMLPackageDiffer {
	private InputStream goldIs, outputIs;
	private Path tempGoldDir, tempOutputDir;
	private List<Difference> differences = null;

	/**
	 * Create a differ for the packages whose contents are accessed via
	 * the specified InputStreams.  The differ will close these inputstreams.
	 * @param gold
	 * @param output
	 */
	public OpenXMLPackageDiffer(InputStream gold, InputStream output) {
		this.goldIs = gold;
		this.outputIs = output;
	}

	public interface Difference {
		String toString();
	}

	public List<Difference> getDifferences() throws IOException, SAXException {
		if (differences == null) {
			evaluate();
		}
		return differences;
	}

	public boolean isIdentical() throws IOException, SAXException {
		return getDifferences().size() == 0;
	}

	public void cleanup() throws IOException {
		deleteDirectory(tempOutputDir);
		deleteDirectory(tempGoldDir);
	}

	private void evaluate() throws IOException, SAXException {
		differences = new ArrayList<Difference>();
		tempGoldDir = Files.createTempDirectory("gold");
		tempOutputDir = Files.createTempDirectory("output");
		Path tempGoldZip = Files.createTempFile("gold", "zip");
		Path tempOutputZip = Files.createTempFile("output", "zip");
		Files.copy(goldIs, tempGoldZip, StandardCopyOption.REPLACE_EXISTING);
		Files.copy(outputIs, tempOutputZip, StandardCopyOption.REPLACE_EXISTING);
		outputIs.close();
		goldIs.close();
		FileUtil.unzip(tempGoldZip.toString(), tempGoldDir.toString());
		FileUtil.unzip(tempOutputZip.toString(), tempOutputDir.toString());
		TreeSet<String> goldPaths = getRelativeChildPaths(tempGoldDir);
		TreeSet<String> outputPaths = getRelativeChildPaths(tempOutputDir);
		FileCompare nonXmlComparer = new FileCompare();
		// Iterate over a copy, so we can modify the original collection
		for (final String relativePath : new TreeSet<String>(goldPaths)) {
			goldPaths.remove(relativePath);
			if (!outputPaths.remove(relativePath)) {
				differences.add(new Difference() {
					@Override public String toString() {
						return "Output is missing part " + relativePath;
					}
				});
			}
			Path goldFile = tempGoldDir.resolve(relativePath);
			Path outputFile = tempOutputDir.resolve(relativePath);
			// HACK The correct way to do this is actually to look at content types.
			if (relativePath.endsWith(".xml") || relativePath.endsWith(".rels")) {
				compareXml(relativePath, goldFile, outputFile);
			}
			else {
				// Compare non-XML parts byte-by-byte
				if (!nonXmlComparer.filesExactlyTheSame(outputFile.toString(), goldFile.toString())) {
					differences.add(new Difference() {
						@Override public String toString() {
							return "Mismatch in non-XML content part " + relativePath;
						}
					});
				}
			}
		}
		// Anything left in output shouldn't be there
		for (final String leftover : outputPaths) {
			differences.add(new Difference() {
				@Override public String toString() {
					return "Output contains unexpected part " + leftover;
				}
			});
		}
		Files.delete(tempGoldZip);
		Files.delete(tempOutputZip);
	}

	private void deleteDirectory(Path dir) throws IOException {
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc)
					throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private void compareXml(final String partName, Path gold, Path output) throws IOException, SAXException {
		try (Reader goldReader = Util.skipBOM(new InputStreamReader(Files.newInputStream(gold), StandardCharsets.UTF_8));
			 Reader outputReader = Util.skipBOM(new InputStreamReader(Files.newInputStream(output), StandardCharsets.UTF_8))) {
			final Diff diff = DiffBuilder.compare(Input.fromReader(goldReader))
					.withTest(Input.fromReader(outputReader))
					.withDifferenceEvaluator(new StandloneXmlEvaluator())
					.checkForIdentical()
					.build();
			if (diff.hasDifferences()) {
				differences.add(new Difference() {
					@Override public String toString() {
						return "Output and gold differ in part " + partName + ": " + diff.toString();
					}
				});
			}
		}
	}

	/**
	 * We do not care about differences in the XML header.
	 */
	class StandloneXmlEvaluator implements DifferenceEvaluator {
		@Override
		public ComparisonResult evaluate(Comparison c,
				ComparisonResult r) {
			if (r == ComparisonResult.DIFFERENT) {
				if (c.getType() == ComparisonType.XML_STANDALONE || c.getType() == ComparisonType.XML_ENCODING) {
					return ComparisonResult.EQUAL;
				}
			}
			return r;
		}
	}

	private TreeSet<String> getRelativeChildPaths(Path dir) throws IOException {
		final TreeSet<String> paths = new TreeSet<String>();
		final int prefixLen = dir.toString().length();
		Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				String s = file.toString().substring(prefixLen);
				String separator = file.getFileSystem().getSeparator();
				if (s.startsWith(separator)) {
					s = s.substring(separator.length());
				}
				paths.add(s);
				return super.visitFile(file, attrs);
			}
		});
		return paths;
	}
}
