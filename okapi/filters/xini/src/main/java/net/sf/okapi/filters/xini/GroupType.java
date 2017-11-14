package net.sf.okapi.filters.xini;

public enum GroupType {

	PAGE("page"),
	ELEMENT("element"),
	FIELDS("fields"),
	FIELD("field"),
	TABLE("table"),
	TR("tr"),
	TD("td"),
	INITABLE("initable"),
	INITR("initr"),
	INITD("initd");

    private final String value;

    GroupType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static GroupType fromValue(String v) {
        for (GroupType c: GroupType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
