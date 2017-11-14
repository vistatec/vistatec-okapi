/*===========================================================================
  Copyright (C) 2013 by the Okapi Framework contributors
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

package net.sf.okapi.steps.inconsistencycheck;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.sf.okapi.common.Util;
import net.sf.okapi.common.XMLWriter;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.resource.TextFragment;
import net.sf.okapi.common.resource.TextUnitUtil;

public class InconsistencyCheck {

    class Group {

        String keyDisplay;
        List<Duplicate> entries;

        public Group(String keyDisplay) {
            this.keyDisplay = keyDisplay;
            entries = new ArrayList<Duplicate>();
        }
    }
    private Parameters params;
    private Map<String, Group> trgDifferences;
    private Map<String, Group> srcDifferences;
    private GenericContent fmt;
    private XMLWriter writer;

    public InconsistencyCheck() {
 	Comparator<String> keyComparator = Util.createComparatorHandlingNullKeys(String.class);

        this.params = new Parameters();
        this.fmt = new GenericContent();
        this.trgDifferences = new TreeMap<String, Group>(keyComparator);
        this.srcDifferences = new TreeMap<String, Group>(keyComparator);
        this.writer = null;
    }

    public void reset(boolean includeXml) {
        if (includeXml) {
            this.writer.close();
            this.writer = null;
        }
        this.trgDifferences.clear();
        this.srcDifferences.clear();
    }
    
    public Parameters getParameters() {
        return this.params;
    }

    public void setParameters(Parameters params) {
        this.params = params;
    }

    /***
     * Stores the source-target pair for comparison
     * @param docId the full path to the document
     * @param subDocId the sub-document number of the source-target pair.
     * @param tuId the text unit number of the source-target pair.
     * @param segId the segment number of the source-target pair.
     * @param srcTf the source text fragment to store (cannot be null).
     * @param trgTf the target text fragment to store (cannot be null).
     */
	public void store (String docId,
		String subDocId,
		String tuId,
		String segId,
		TextFragment srcTf,
		TextFragment trgTf)
	{
		addToMap(trgDifferences, docId, subDocId, tuId, segId, srcTf, trgTf);
		addToMap(srcDifferences, docId, subDocId, tuId, segId, trgTf, srcTf);
	}

    private void detectIssues() {
        computeIssues(trgDifferences);
        computeIssues(srcDifferences);
    }

    /***
     * Creates the inconsistency report.
     * 
     * @param reportPath the location for the report.
     * @param isEndBatch indicates if this method is invoked from the EndBatch event handler (true),
     * or if from the EndDocument event handler (false). The second case is done only in per-file mode.
     */
    public void generateReport(String reportPath, boolean isEndBatch) {
        detectIssues();
        generateXMLReport(reportPath, isEndBatch);
    }

    /**
     * Gets the display representation of a text fragment.
     *
     * @param tf the text fragment to convert.
     * @return the display representation of the text fragment.
     */
    private String toDisplayFormat(TextFragment tf) {
        String displayOption = params.getDisplayOption();
        if (displayOption.equals(Parameters.DISPLAYOPTION_ORIGINAL)) {
            return tf.toText();
        } else if (displayOption.equals(Parameters.DISPLAYOPTION_GENERIC)) {
            return fmt.setContent(tf).toString();
        } else {
            return TextUnitUtil.removeCodes(tf.getCodedText());
        }
    }

    /**
     * Gets the string to use when comparing two content.
     *
     * @param tf the text fragment to convert.
     * @return the string to use when comparing.
     */
    private String toCompareFormat(TextFragment tf) {
        //TODO: Should we normalize this further?
        // e.g. normalize whitespace, lowercase, etc.
        return TextUnitUtil.removeCodes(tf.getCodedText());
    }

	private void addToMap (Map<String, Group> map,
		String docId,
		String subDocId,
		String tuId,
		String segId,
		TextFragment keyTf,
		TextFragment dataTf)
	{
		// Format issue display text
		// Create the entry for the data
		Duplicate dup = new Duplicate(docId, subDocId, tuId, segId,
			toCompareFormat(dataTf),
			toDisplayFormat(dataTf));
		// Create the key
		String key = toCompareFormat(keyTf);
		// Check existing entries
		Group grp = map.get(key);
		if ( grp == null ) {
			grp = new Group(toDisplayFormat(keyTf));
			map.put(key, grp);
		}
		// Add to group
		grp.entries.add(dup);
	}

    /***
     * Detect any potential issues in consistency.
     * 
     * @param map the list of strings to compare
     */
    private void computeIssues(Map<String, Group> map) {
        Iterator<Group> iter = map.values().iterator();
        while (iter.hasNext()) {
            Group group = iter.next();
            List<Duplicate> list = group.entries;
            if (list.size() < 2) {
                iter.remove();
            } else {
                boolean keep = false;
                int i = 0;
                while (i < list.size() && !keep) {
                    String text1 = list.get(i).getText();
                    int j = i + 1;
                    while (j < list.size()) {
                        String text2 = list.get(j).getText();
                        if (!text1.equals(text2)) {
                            keep = true;
                            break;
                        }
                        j++;
                    }
                    i++;
                }
                if (!keep) {
                    iter.remove();
                }
            }
        }
    }

    /***
     * Generates an XML report in the path specified.
     * 
     * @param finalPath the path where the report will be placed
     * @param isEndBatch whether the XMLWriter should be closed:
     *          true: call came from the END_BATCH event and the writer should be closed.
     *          false: call came from the END_DOCUMENT event and the writer should be left open.
     */
    private void generateXMLReport(String finalPath, boolean isEndBatch) {
        try {
            // Create report file
            if (writer == null) {
                writeXmlStart(finalPath);
            }

            // Process the target differences
            writeIssues(writer, trgDifferences, true);

            // Process the source differences
            writeIssues(writer, srcDifferences, false);

            // Write end of report
            if (isEndBatch) {
                writeXmlEnd();
            } else {
                reset(false);
            }
        } finally {
            if (isEndBatch) {
                if (writer != null) {
                    writer.close();
                    // Open the output if requested
                    if (params.isAutoOpen()) {
                        Util.openURL((new File(finalPath)).getAbsolutePath());
                    }
                }
            }
        }
    }

    private void writeXmlStart(String finalPath) {
        writer = new XMLWriter(finalPath);
        writer.writeStartDocument();
        writer.writeStartElement("inconsistencyCheck");
        if (params.getCheckPerFile()) {
            writer.writeAttributeString("perFile", "true");
        } else {
            writer.writeAttributeString("perFile", "false");
        }
        writer.writeLineBreak();
    }

    private void writeIssues(XMLWriter writer,
            Map<String, Group> map,
            boolean isKeyTheSource) {
        for (String key : map.keySet()) {
            // Group element
            writer.writeStartElement(isKeyTheSource ? "targetDifferences" : "sourceDifferences");
            writer.writeLineBreak();

            // Key
            Group grp = map.get(key);
            writer.writeElementString(isKeyTheSource ? "source" : "target", grp.keyDisplay);
            writer.writeLineBreak();

            for (Duplicate dup : grp.entries) {
                writer.writeStartElement("issue");
                writer.writeAttributeString("seg", dup.getSegId());
                writer.writeAttributeString("tu", dup.getTuId());
                writer.writeAttributeString("subDoc", dup.getSubDocId());
                writer.writeAttributeString("doc", dup.getDocId());
                writer.writeString(dup.getDisplay());
                writer.writeEndElementLineBreak(); // issue
            }

            // Close group
            writer.writeEndElementLineBreak();
        }
    }

    private void writeXmlEnd() {
        writer.writeEndElementLineBreak(); // inconsistencyCheck
        writer.writeEndDocument();
    }
}
