package net.sf.okapi.filters.xini;

public enum XINIProperties {

	PAGE_ID("pageId"),
	CONTEXT_INFORMATION_URL("contextInformattionUrl"),
	ELEMENT_ID("elementId"),
	ELEMENT_CUSTOMER_TEXT_ID("elementCustomerTextId"),
	ELEMENT_SIZE("elementSize"),
	ELEMENT_ALPHA_LIST("elementAlphaList"),
	ELEMENT_ELEMENT_TYPE("elementElementType"),
	ELEMENT_RAW_SOURCE_BEFORE_ELEMENT("rawSourceBeforeElement"),
	ELEMENT_RAW_SOURCE_AFTER_ELEMENT("rawSourceAfterElement"),
	ELEMENT_LABEL("elementLabel"),
	ELEMENT_STYLE("elementStyle"),
	FIELD_ID("fieldId"),
	FIELD_CUSTOMER_TEXT_ID("fieldCustomerTextId"),
	FIELD_EMPTY_SEGMENTS_FLAGS("fieldEmptySegmentsFlags"),
	FIELD_EXTERNAL_ID("fieldExternalIds"),
	FIELD_LABEL("fieldLabel"),
	FIELD_RAW_SOURCE_AFTER_FIELD("fieldRawSourceAfterField"),
	FIELD_RAW_SOURCE_BEFORE_FIELD("fieldRawSourceBeforefield"),
	FIELD_NO_CONTENT("fieldNoContent"),
	SEGMENT_ID("segmentId"),
	TABLE_CUSTOMER_TEXT_ID("tableCustomerTextId"),
	TABLE_EMPTY_SEGMENTS_FLAGS("tableEmptySegmentsFlags"),
	TABLE_EXTERNAL_ID("tableExternalIds"),
	TABLE_LABEL("tableLabel"),
	TABLE_NO_CONTENT("tableNoContent"),
	INITABLE_CUSTOMER_TEXT_ID("iniTableCustomerTextId"),
	INITABLE_EMPTY_SEGMENTS_FLAGS("iniTableEmptySegmentsFlags"),
	INITABLE_EXTERNAL_ID("iniTableExternalIds"),
	INITABLE_LABEL("iniTableLabel"),
	INITABLE_NO_CONTENT("iniTableNoContent"),
	EMPTY_TRANSLATION("emptyTranslation"),
	SOURCE_LANGUAGE("sourceLanguage"),
	TARGET_LANGUAGES("targetLanguage");

    private final String value;

    XINIProperties(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static XINIProperties fromValue(String v) {
        for (XINIProperties c: XINIProperties.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
