
package net.sf.okapi.filters.xini.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PlaceHolderType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;simpleType name="PlaceHolderType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="style"/&gt;
 *     &lt;enumeration value="link"/&gt;
 *     &lt;enumeration value="ph"/&gt;
 *     &lt;enumeration value="deleted"/&gt;
 *     &lt;enumeration value="inserted"/&gt;
 *     &lt;enumeration value="updated"/&gt;
 *     &lt;enumeration value="memory100"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "PlaceHolderType")
@XmlEnum
public enum PlaceHolderType {

    @XmlEnumValue("style")
    STYLE("style"),
    @XmlEnumValue("link")
    LINK("link"),
    @XmlEnumValue("ph")
    PH("ph"),
    @XmlEnumValue("deleted")
    DELETED("deleted"),
    @XmlEnumValue("inserted")
    INSERTED("inserted"),
    @XmlEnumValue("updated")
    UPDATED("updated"),
    @XmlEnumValue("memory100")
    MEMORY_100("memory100");
    private final String value;

    PlaceHolderType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PlaceHolderType fromValue(String v) {
        for (PlaceHolderType c: PlaceHolderType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
