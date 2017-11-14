
package net.sf.okapi.filters.xini.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Contains the text content of a page element as well as additional page related information
 * 
 * <p>Java class for Page complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Page">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PageName" type="{}TokenMaxLen255" minOccurs="0"/>
 *         &lt;element name="Elements" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="Element" type="{}Element" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="ContextInformationURL" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ThumbnailImageURL" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="PageID" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Page", propOrder = {
    "pageName",
    "elements",
    "contextInformationURL",
    "thumbnailImageURL"
})
public class Page {

    @XmlElement(name = "PageName")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String pageName;
    @XmlElement(name = "Elements")
    protected Page.Elements elements;
    @XmlElement(name = "ContextInformationURL")
    protected String contextInformationURL;
    @XmlElement(name = "ThumbnailImageURL")
    protected String thumbnailImageURL;
    @XmlAttribute(name = "PageID", required = true)
    protected int pageID;

    /**
     * Gets the value of the pageName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPageName() {
        return pageName;
    }

    /**
     * Sets the value of the pageName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPageName(String value) {
        this.pageName = value;
    }

    /**
     * Gets the value of the elements property.
     * 
     * @return
     *     possible object is
     *     {@link Page.Elements }
     *     
     */
    public Page.Elements getElements() {
        return elements;
    }

    /**
     * Sets the value of the elements property.
     * 
     * @param value
     *     allowed object is
     *     {@link Page.Elements }
     *     
     */
    public void setElements(Page.Elements value) {
        this.elements = value;
    }

    /**
     * Gets the value of the contextInformationURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContextInformationURL() {
        return contextInformationURL;
    }

    /**
     * Sets the value of the contextInformationURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContextInformationURL(String value) {
        this.contextInformationURL = value;
    }

    /**
     * Gets the value of the thumbnailImageURL property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThumbnailImageURL() {
        return thumbnailImageURL;
    }

    /**
     * Sets the value of the thumbnailImageURL property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThumbnailImageURL(String value) {
        this.thumbnailImageURL = value;
    }

    /**
     * Gets the value of the pageID property.
     * 
     */
    public int getPageID() {
        return pageID;
    }

    /**
     * Sets the value of the pageID property.
     * 
     */
    public void setPageID(int value) {
        this.pageID = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="Element" type="{}Element" maxOccurs="unbounded" minOccurs="0"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "element"
    })
    public static class Elements {

        @XmlElement(name = "Element")
        protected List<Element> element;

        /**
         * Gets the value of the element property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the element property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getElement().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Element }
         * 
         * 
         */
        public List<Element> getElement() {
            if (element == null) {
                element = new ArrayList<Element>();
            }
            return this.element;
        }

    }

}

