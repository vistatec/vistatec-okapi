
package net.sf.okapi.filters.xini.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ElementType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;simpleType name="ElementType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="editible text"/&gt;
 *     &lt;enumeration value="r/o text"/&gt;
 *     &lt;enumeration value="r/o image"/&gt;
 *     &lt;enumeration value="editible image"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 * 
 */
@XmlType(name = "ElementType")
@XmlEnum
public enum ElementType {

    @XmlEnumValue("editible text")
    EDITIBLE_TEXT("editible text"),
    @XmlEnumValue("r/o text")
    R_O_TEXT("r/o text"),
    @XmlEnumValue("r/o image")
    R_O_IMAGE("r/o image"),
    @XmlEnumValue("editible image")
    EDITIBLE_IMAGE("editible image");
    private final String value;

    ElementType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ElementType fromValue(String v) {
        for (ElementType c: ElementType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
