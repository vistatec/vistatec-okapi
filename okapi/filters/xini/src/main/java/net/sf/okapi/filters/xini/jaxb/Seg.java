
package net.sf.okapi.filters.xini.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Seg complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Seg"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{}TextContent"&gt;
 *       &lt;attribute name="SegID" use="required" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="SubSeg" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="SegmentIDBeforeSegmentation" type="{http://www.w3.org/2001/XMLSchema}int" /&gt;
 *       &lt;attribute name="LeadingSpacer" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="TrailingSpacer" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="EmptyTranslation" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Seg")
@XmlRootElement
public class Seg
    extends TextContent
{

    @XmlAttribute(name = "SegID", required = true)
    protected int segID;
    @XmlAttribute(name = "SubSeg")
    protected Integer subSeg;
    @XmlAttribute(name = "SegmentIDBeforeSegmentation")
    protected Integer segmentIDBeforeSegmentation;
    @XmlAttribute(name = "LeadingSpacer")
    protected String leadingSpacer;
    @XmlAttribute(name = "TrailingSpacer")
    protected String trailingSpacer;
    @XmlAttribute(name = "EmptyTranslation")
    protected Boolean emptyTranslation;

    /**
     * Gets the value of the segID property.
     * 
     */
    public int getSegID() {
        return segID;
    }

    /**
     * Sets the value of the segID property.
     * 
     */
    public void setSegID(int value) {
        this.segID = value;
    }

    /**
     * Gets the value of the subSeg property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSubSeg() {
        return subSeg;
    }

    /**
     * Sets the value of the subSeg property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSubSeg(Integer value) {
        this.subSeg = value;
    }

    /**
     * Gets the value of the segmentIDBeforeSegmentation property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getSegmentIDBeforeSegmentation() {
        return segmentIDBeforeSegmentation;
    }

    /**
     * Sets the value of the segmentIDBeforeSegmentation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setSegmentIDBeforeSegmentation(Integer value) {
        this.segmentIDBeforeSegmentation = value;
    }

    /**
     * Gets the value of the leadingSpacer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLeadingSpacer() {
        return leadingSpacer;
    }

    /**
     * Sets the value of the leadingSpacer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeadingSpacer(String value) {
        this.leadingSpacer = value;
    }

    /**
     * Gets the value of the trailingSpacer property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrailingSpacer() {
        return trailingSpacer;
    }

    /**
     * Sets the value of the trailingSpacer property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrailingSpacer(String value) {
        this.trailingSpacer = value;
    }

    /**
     * Gets the value of the emptyTranslation property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEmptyTranslation() {
        return emptyTranslation;
    }

    /**
     * Sets the value of the emptyTranslation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEmptyTranslation(Boolean value) {
        this.emptyTranslation = value;
    }

}
