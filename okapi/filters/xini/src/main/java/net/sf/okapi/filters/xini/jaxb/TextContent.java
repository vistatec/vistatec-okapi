
package net.sf.okapi.filters.xini.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * Contains the text content itself and supports inline tags
 * 
 * <p>Java class for TextContent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TextContent"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *         &lt;element name="i" type="{}TextContent"/&gt;
 *         &lt;element name="b" type="{}TextContent"/&gt;
 *         &lt;element name="u" type="{}TextContent"/&gt;
 *         &lt;element name="sub" type="{}TextContent"/&gt;
 *         &lt;element name="sup" type="{}TextContent"/&gt;
 *         &lt;element name="br" type="{}Empty"/&gt;
 *         &lt;element name="ph" type="{}PlaceHolder"/&gt;
 *         &lt;element name="sph" type="{}StartPlaceHolder"/&gt;
 *         &lt;element name="eph" type="{}EndPlaceHolder"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TextContent", propOrder = {
    "content"
})
@XmlSeeAlso({
    PlaceHolder.class,
    Seg.class,
    Trans.class
})
public class TextContent {

    @XmlElementRefs({
        @XmlElementRef(name = "i", type = JAXBElement.class),
        @XmlElementRef(name = "sup", type = JAXBElement.class),
        @XmlElementRef(name = "eph", type = JAXBElement.class),
        @XmlElementRef(name = "sph", type = JAXBElement.class),
        @XmlElementRef(name = "ph", type = JAXBElement.class),
        @XmlElementRef(name = "u", type = JAXBElement.class),
        @XmlElementRef(name = "sub", type = JAXBElement.class),
        @XmlElementRef(name = "b", type = JAXBElement.class),
        @XmlElementRef(name = "br", type = JAXBElement.class)
    })
    @XmlMixed
    protected List<Serializable> content;

    /**
     * Contains the text content itself and supports inline tags Gets the value of the content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link TextContent }{@code >}
     * {@link String }
     * {@link JAXBElement }{@code <}{@link TextContent }{@code >}
     * {@link JAXBElement }{@code <}{@link EndPlaceHolder }{@code >}
     * {@link JAXBElement }{@code <}{@link TextContent }{@code >}
     * {@link JAXBElement }{@code <}{@link PlaceHolder }{@code >}
     * {@link JAXBElement }{@code <}{@link StartPlaceHolder }{@code >}
     * {@link JAXBElement }{@code <}{@link TextContent }{@code >}
     * {@link JAXBElement }{@code <}{@link Empty }{@code >}
     * {@link JAXBElement }{@code <}{@link TextContent }{@code >}
     * 
     * 
     */
    public List<Serializable> getContent() {
        if (content == null) {
            content = new ArrayList<Serializable>();
        }
        return this.content;
    }

}
