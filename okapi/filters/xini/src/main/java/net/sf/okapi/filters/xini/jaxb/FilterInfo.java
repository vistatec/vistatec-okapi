
package net.sf.okapi.filters.xini.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Describes information on the Filter
 * 
 * <p>Java class for FilterInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FilterInfo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="GlobalFilterProfileID" type="{}ID"/&gt;
 *         &lt;element name="FilterJobID" type="{}ID"/&gt;
 *         &lt;element name="PreviewEnabledByDefault" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterInfo", propOrder = {
    "globalFilterProfileID",
    "filterJobID",
    "previewEnabledByDefault"
})
public class FilterInfo {

    @XmlElement(name = "GlobalFilterProfileID")
    protected int globalFilterProfileID;
    @XmlElement(name = "FilterJobID")
    protected int filterJobID;
    @XmlElement(name = "PreviewEnabledByDefault")
    protected boolean previewEnabledByDefault;

    /**
     * Gets the value of the globalFilterProfileID property.
     * 
     */
    public int getGlobalFilterProfileID() {
        return globalFilterProfileID;
    }

    /**
     * Sets the value of the globalFilterProfileID property.
     * 
     */
    public void setGlobalFilterProfileID(int value) {
        this.globalFilterProfileID = value;
    }

    /**
     * Gets the value of the filterJobID property.
     * 
     */
    public int getFilterJobID() {
        return filterJobID;
    }

    /**
     * Sets the value of the filterJobID property.
     * 
     */
    public void setFilterJobID(int value) {
        this.filterJobID = value;
    }

    /**
     * Gets the value of the previewEnabledByDefault property.
     * 
     */
    public boolean isPreviewEnabledByDefault() {
        return previewEnabledByDefault;
    }

    /**
     * Sets the value of the previewEnabledByDefault property.
     * 
     */
    public void setPreviewEnabledByDefault(boolean value) {
        this.previewEnabledByDefault = value;
    }

}
