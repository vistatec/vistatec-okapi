
package net.sf.okapi.filters.xini.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * An element can either contain Fields or a Table
 * 
 * <p>Java class for Element complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Element"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="ElementContent"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;choice&gt;
 *                   &lt;element name="Fields" type="{}Fields"/&gt;
 *                   &lt;element name="Table" type="{}Table"/&gt;
 *                   &lt;element name="INI_Table" type="{}INI_Table"/&gt;
 *                 &lt;/choice&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Style" type="{}TokenMaxLen255" minOccurs="0"/&gt;
 *         &lt;element name="Label" type="{}TokenMaxLen255" minOccurs="0"/&gt;
 *       &lt;/all&gt;
 *       &lt;attribute name="ElementID" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="CustomerTextID" type="{}TokenMaxLen255" /&gt;
 *       &lt;attribute name="Size" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="AlphaList" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="ElementType" type="{}ElementType" default="editible text" /&gt;
 *       &lt;attribute name="RawSourceBeforeElement" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="RawSourceAfterElement" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Element", propOrder = {

})
public class Element {

    @XmlElement(name = "ElementContent", required = true)
    protected Element.ElementContent elementContent;
    @XmlElement(name = "Style")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String style;
    @XmlElement(name = "Label")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String label;
    @XmlAttribute(name = "ElementID", required = true)
    protected int elementID;
    @XmlAttribute(name = "CustomerTextID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String customerTextID;
    @XmlAttribute(name = "Size")
    protected Integer size;
    @XmlAttribute(name = "AlphaList")
    protected Boolean alphaList;
    @XmlAttribute(name = "ElementType")
    protected ElementType elementType;
    @XmlAttribute(name = "RawSourceBeforeElement")
    protected String rawSourceBeforeElement;
    @XmlAttribute(name = "RawSourceAfterElement")
    protected String rawSourceAfterElement;

    /**
     * Gets the value of the elementContent property.
     * 
     * @return
     *     possible object is
     *     {@link Element.ElementContent }
     *     
     */
    public Element.ElementContent getElementContent() {
        return elementContent;
    }

    /**
     * Sets the value of the elementContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Element.ElementContent }
     *     
     */
    public void setElementContent(Element.ElementContent value) {
        this.elementContent = value;
    }

    /**
     * Gets the value of the style property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStyle() {
        return style;
    }

    /**
     * Sets the value of the style property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStyle(String value) {
        this.style = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * Gets the value of the elementID property.
     * 
     */
    public int getElementID() {
        return elementID;
    }

    /**
     * Sets the value of the elementID property.
     * 
     */
    public void setElementID(int value) {
        this.elementID = value;
    }

    /**
     * Gets the value of the customerTextID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCustomerTextID() {
        return customerTextID;
    }

    /**
     * Sets the value of the customerTextID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCustomerTextID(String value) {
        this.customerTextID = value;
    }

    /**
     * Gets the value of the size property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSize() {
        return size;
    }

    /**
     * Sets the value of the size property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSize(Integer value) {
        this.size = value;
    }

    /**
     * Gets the value of the alphaList property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAlphaList() {
        return alphaList;
    }

    /**
     * Sets the value of the alphaList property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAlphaList(Boolean value) {
        this.alphaList = value;
    }

    /**
     * Gets the value of the elementType property.
     * 
     * @return
     *     possible object is
     *     {@link ElementType }
     *     
     */
    public ElementType getElementType() {
        return elementType;
    }

    /**
     * Sets the value of the elementType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ElementType }
     *     
     */
    public void setElementType(ElementType value) {
        this.elementType = value;
    }

    /**
     * Gets the value of the rawSourceBeforeElement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRawSourceBeforeElement() {
        return rawSourceBeforeElement;
    }

    /**
     * Sets the value of the rawSourceBeforeElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRawSourceBeforeElement(String value) {
        this.rawSourceBeforeElement = value;
    }

    /**
     * Gets the value of the rawSourceAfterElement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRawSourceAfterElement() {
        return rawSourceAfterElement;
    }

    /**
     * Sets the value of the rawSourceAfterElement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRawSourceAfterElement(String value) {
        this.rawSourceAfterElement = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;choice&gt;
     *         &lt;element name="Fields" type="{}Fields"/&gt;
     *         &lt;element name="Table" type="{}Table"/&gt;
     *         &lt;element name="INI_Table" type="{}INI_Table"/&gt;
     *       &lt;/choice&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "fields",
        "table",
        "iniTable"
    })
    public static class ElementContent {

        @XmlElement(name = "Fields")
        protected Fields fields;
        @XmlElement(name = "Table")
        protected Table table;
        @XmlElement(name = "INI_Table")
        protected INITable iniTable;

        /**
         * Gets the value of the fields property.
         * 
         * @return
         *     possible object is
         *     {@link Fields }
         *     
         */
        public Fields getFields() {
            return fields;
        }

        /**
         * Sets the value of the fields property.
         * 
         * @param value
         *     allowed object is
         *     {@link Fields }
         *     
         */
        public void setFields(Fields value) {
            this.fields = value;
        }

        /**
         * Gets the value of the table property.
         * 
         * @return
         *     possible object is
         *     {@link Table }
         *     
         */
        public Table getTable() {
            return table;
        }

        /**
         * Sets the value of the table property.
         * 
         * @param value
         *     allowed object is
         *     {@link Table }
         *     
         */
        public void setTable(Table value) {
            this.table = value;
        }

        /**
         * Gets the value of the iniTable property.
         * 
         * @return
         *     possible object is
         *     {@link INITable }
         *     
         */
        public INITable getINITable() {
            return iniTable;
        }

        /**
         * Sets the value of the iniTable property.
         * 
         * @param value
         *     allowed object is
         *     {@link INITable }
         *     
         */
        public void setINITable(INITable value) {
            this.iniTable = value;
        }

    }

}
