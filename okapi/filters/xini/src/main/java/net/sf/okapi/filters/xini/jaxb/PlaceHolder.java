
package net.sf.okapi.filters.xini.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PlaceHolder complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="PlaceHolder"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{}TextContent"&gt;
 *       &lt;attribute name="ID" use="required" type="{}ID" /&gt;
 *       &lt;attribute name="type" type="{}PlaceHolderType" default="ph" /&gt;
 *       &lt;attribute name="opening" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="closing" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceHolder")
public class PlaceHolder
    extends TextContent
{

    @XmlAttribute(name = "ID", required = true)
    protected int id;
    @XmlAttribute
    protected PlaceHolderType type;
    @XmlAttribute
    protected String opening;
    @XmlAttribute
    protected String closing;

    /**
     * Gets the value of the id property.
     * 
     */
    public int getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setID(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link PlaceHolderType }
     *     
     */
    public PlaceHolderType getType() {
        if (type == null) {
            return PlaceHolderType.PH;
        } else {
            return type;
        }
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link PlaceHolderType }
     *     
     */
    public void setType(PlaceHolderType value) {
        this.type = value;
    }

    /**
     * Gets the value of the opening property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOpening() {
        return opening;
    }

    /**
     * Sets the value of the opening property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOpening(String value) {
        this.opening = value;
    }

    /**
     * Gets the value of the closing property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClosing() {
        return closing;
    }

    /**
     * Sets the value of the closing property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClosing(String value) {
        this.closing = value;
    }
}
