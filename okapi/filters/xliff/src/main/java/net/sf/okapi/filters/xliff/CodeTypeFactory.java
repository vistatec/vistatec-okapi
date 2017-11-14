package net.sf.okapi.filters.xliff;

import net.sf.okapi.common.resource.CodeTypeBuilder;

import javax.xml.stream.XMLStreamException;

import java.util.HashSet;
import java.util.Set;

import static net.sf.okapi.common.resource.Code.TYPE_BOLD;
import static net.sf.okapi.common.resource.Code.TYPE_ITALIC;
import static net.sf.okapi.common.resource.Code.TYPE_LINK;
import static net.sf.okapi.common.resource.ExtendedCodeType.COLOR;
import static net.sf.okapi.common.resource.ExtendedCodeType.HIGHLIGHT;
import static net.sf.okapi.common.resource.ExtendedCodeType.SHADOW;
import static net.sf.okapi.common.resource.ExtendedCodeType.STRIKE_THROUGH;
import static net.sf.okapi.common.resource.ExtendedCodeType.SUBSCRIPT;
import static net.sf.okapi.common.resource.ExtendedCodeType.SUPERSCRIPT;
import static net.sf.okapi.common.resource.ExtendedCodeType.UNDERLINE;

/**
 * Provides a code type factory.
 */
class CodeTypeFactory {

    private static final Set<String> HYPERLINK_DATA_START_ELEMENT_NAMES = new HashSet<>();

    private static final String DATA_END_MARKER = ">";
    private static final String DATA_CHUNKS_DELIMITER = " ";
    private static final String NAME_AND_VALUE_DELIMITER = "=";

    private static final String SINGLE_QUOTE = "'";
    private static final String DOUBLE_QUOTE = "\"";

    private static final String STRIKE_THROUGH_DATA_CHUNK_NAME = "strikethrough";
    private static final String SHADOW_DATA_CHUNK_NAME = "shadow";
    private static final String UNDERLINE_DATA_CHUNK_NAME = "underline";
    private static final String HIGHLIGHT_COLOR_DATA_CHUNK_NAME = "highlight";
    private static final String COLOR_DATA_CHUNK_NAME = "color";

    private static final String SUPERSCRIPT_DATA_CHUNK_NAME = "superscript";
    private static final String SUBSCRIPT_DATA_CHUNK_NAME = "subscript";

    private static final String FALSE_VALUE = Boolean.FALSE.toString();
    private static final String NONE_VALUE = "none";
    private static final String NO_STRIKE_VALUE = "nostrike";

    private static final boolean ADD_EXTENDED_CODE_TYPE_PREFIX = true;

    static {
        HYPERLINK_DATA_START_ELEMENT_NAMES.add("hyperlink");
        HYPERLINK_DATA_START_ELEMENT_NAMES.add("a");
    }

    static String createCodeType(String data, String dataStartElementName) throws XMLStreamException {

        CodeTypeBuilder codeTypeBuilder = new CodeTypeBuilder(ADD_EXTENDED_CODE_TYPE_PREFIX);

        if (HYPERLINK_DATA_START_ELEMENT_NAMES.contains(dataStartElementName)) {
            codeTypeBuilder.addType(TYPE_LINK);
        }

        String[] dataChunks = getDataChunks(data, dataStartElementName);

        for (String dataChunk : dataChunks) {
            handleDataChunk(codeTypeBuilder, dataChunk);
        }

        return codeTypeBuilder.build();
    }

    private static String[] getDataChunks(String data, String dataStartElementName) {

        if (null == dataStartElementName) {
            // the data start element name is not available
            return new String[0];
        }

        int dataStartElementNameIndex = data.indexOf(dataStartElementName);
        if (-1 == dataStartElementNameIndex) {
            // the data start element name is not found in data
            return new String[0];
        }

        int endDataIndex = data.indexOf(DATA_END_MARKER);
        if (-1 == endDataIndex) {
            // the data end element is not found
            endDataIndex = data.length() - 1;
        }

        int startDataIndex = dataStartElementNameIndex + dataStartElementName.length();

        if (startDataIndex >= endDataIndex) {
            // there are no data chunks
            return new String[0];
        }

        String chunksString = data.substring(startDataIndex, endDataIndex);

        return chunksString.split(DATA_CHUNKS_DELIMITER);
    }

    private static void handleDataChunk(CodeTypeBuilder codeTypeBuilder, String dataChunk) {
        String[] nameAndValue = dataChunk.split(NAME_AND_VALUE_DELIMITER);

        if (2 > nameAndValue.length) {
            return;
        }

        String value = getValue(nameAndValue);

        if (FALSE_VALUE.equals(value.toLowerCase())
                || NONE_VALUE.equals(value.toLowerCase())) {
            // get rid of "false" and "none" values
            return;
        }

        switch (nameAndValue[0]) {
            case TYPE_BOLD:
                codeTypeBuilder.addType(TYPE_BOLD);
                break;

            case TYPE_ITALIC:
                codeTypeBuilder.addType(TYPE_ITALIC);
                break;

            case STRIKE_THROUGH_DATA_CHUNK_NAME:
                if (!NO_STRIKE_VALUE.equals(value.toLowerCase())) {
                    codeTypeBuilder.addType(STRIKE_THROUGH.getValue());
                }
                break;

            case SHADOW_DATA_CHUNK_NAME:
                codeTypeBuilder.addType(SHADOW.getValue());
                break;

            case UNDERLINE_DATA_CHUNK_NAME:
                codeTypeBuilder.addType(UNDERLINE.getValue(), value);
                break;

            case HIGHLIGHT_COLOR_DATA_CHUNK_NAME:
                codeTypeBuilder.addType(HIGHLIGHT.getValue(), value);
                break;

            case COLOR_DATA_CHUNK_NAME:
                codeTypeBuilder.addType(COLOR.getValue(), value);
                break;

            case SUPERSCRIPT_DATA_CHUNK_NAME:
                codeTypeBuilder.addType(SUPERSCRIPT.getValue());
                break;

            case SUBSCRIPT_DATA_CHUNK_NAME:
                codeTypeBuilder.addType(SUBSCRIPT.getValue());
                break;
        }
    }

    private static String getValue(String[] nameAndValue) {

        if (2 > nameAndValue[1].length()) {
            return nameAndValue[1];
        }

        if (nameAndValue[1].startsWith(DOUBLE_QUOTE) && nameAndValue[1].endsWith(DOUBLE_QUOTE)
                || nameAndValue[1].startsWith(SINGLE_QUOTE) && nameAndValue[1].endsWith(SINGLE_QUOTE)) {

            return nameAndValue[1].substring(1, nameAndValue[1].length() - 1);
        }

        return nameAndValue[1];
    }
}

