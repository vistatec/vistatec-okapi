
package net.sf.okapi.filters.xini.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for INI_TD complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="INI_TD"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;sequence maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="Seg" type="{}Seg"/&gt;
 *           &lt;element name="Trans" type="{}Trans" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;/sequence&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="NoContent" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="CustomerTextID" type="{}TokenMaxLen255" /&gt;
 *       &lt;attribute name="Label" type="{}TokenMaxLen255" /&gt;
 *       &lt;attribute name="ExternalID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="EmptySegmentsFlags" type="{}TokenMaxLen255" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "INI_TD", propOrder = {
    "segAndTrans"
})
public class INITD {

    @XmlElements({
			@XmlElement(name = "Trans", type = Trans.class),
			@XmlElement(name = "Seg", type = Seg.class)
    })
    protected List<TextContent> segAndTrans;
    @XmlAttribute(name = "NoContent")
    protected Boolean noContent;
    @XmlAttribute(name = "CustomerTextID")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String customerTextID;
    @XmlAttribute(name = "Label")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String label;
    @XmlAttribute(name = "ExternalID")
    protected String externalID;
    @XmlAttribute(name = "EmptySegmentsFlags")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String emptySegmentsFlags;

	/**
	 * Gets the value of the segAndTrans property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
	 * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
	 * the segAndTrans property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSegAndTrans().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Trans } {@link Seg }
	 * 
	 * 
	 */
    public List<TextContent> getSegAndTrans() {
        if (segAndTrans == null) {
            segAndTrans = new ArrayList<TextContent>();
        }
        return this.segAndTrans;
    }

	public List<Seg> getSeg() {
		List<Seg> segs = new ArrayList<Seg>();
		for (TextContent tc : getSegAndTrans()) {
			if (tc instanceof Seg) {
				segs.add((Seg) tc);
			}
		}
		return segs;
	}

    /**
     * Gets the value of the noContent property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isNoContent() {
        return noContent;
    }

    /**
     * Sets the value of the noContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setNoContent(Boolean value) {
        this.noContent = value;
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
     * Gets the value of the externalID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExternalID() {
        return externalID;
    }

    /**
     * Sets the value of the externalID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExternalID(String value) {
        this.externalID = value;
    }

    /**
     * Gets the value of the emptySegmentsFlags property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmptySegmentsFlags() {
        return emptySegmentsFlags;
    }

    /**
     * Sets the value of the emptySegmentsFlags property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmptySegmentsFlags(String value) {
        this.emptySegmentsFlags = value;
    }

}
