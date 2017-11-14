
package net.sf.okapi.filters.xini.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for StatisticInfo complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StatisticInfo"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element name="Total" type="{}StatisticValue" minOccurs="0"/&gt;
 *         &lt;element name="Memory100" type="{}StatisticValue" minOccurs="0"/&gt;
 *         &lt;element name="Repetitions" type="{}StatisticValue" minOccurs="0"/&gt;
 *         &lt;element name="GrossTotal" type="{}StatisticValue" minOccurs="0"/&gt;
 *         &lt;element name="AditionalLines" type="{}StatisticValue" minOccurs="0"/&gt;
 *         &lt;element name="AditionalWords" type="{}StatisticValue" minOccurs="0"/&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatisticInfo", propOrder = {

})
public class StatisticInfo {

    @XmlElement(name = "Total")
    protected Float total;
    @XmlElement(name = "Memory100")
    protected Float memory100;
    @XmlElement(name = "Repetitions")
    protected Float repetitions;
    @XmlElement(name = "GrossTotal")
    protected Float grossTotal;
    @XmlElement(name = "AditionalLines")
    protected Float aditionalLines;
    @XmlElement(name = "AditionalWords")
    protected Float aditionalWords;

    /**
     * Gets the value of the total property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setTotal(Float value) {
        this.total = value;
    }

    /**
     * Gets the value of the memory100 property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getMemory100() {
        return memory100;
    }

    /**
     * Sets the value of the memory100 property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setMemory100(Float value) {
        this.memory100 = value;
    }

    /**
     * Gets the value of the repetitions property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getRepetitions() {
        return repetitions;
    }

    /**
     * Sets the value of the repetitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setRepetitions(Float value) {
        this.repetitions = value;
    }

    /**
     * Gets the value of the grossTotal property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getGrossTotal() {
        return grossTotal;
    }

    /**
     * Sets the value of the grossTotal property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setGrossTotal(Float value) {
        this.grossTotal = value;
    }

    /**
     * Gets the value of the aditionalLines property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getAditionalLines() {
        return aditionalLines;
    }

    /**
     * Sets the value of the aditionalLines property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setAditionalLines(Float value) {
        this.aditionalLines = value;
    }

    /**
     * Gets the value of the aditionalWords property.
     * 
     * @return
     *     possible object is
     *     {@link Float }
     *     
     */
    public Float getAditionalWords() {
        return aditionalWords;
    }

    /**
     * Sets the value of the aditionalWords property.
     * 
     * @param value
     *     allowed object is
     *     {@link Float }
     *     
     */
    public void setAditionalWords(Float value) {
        this.aditionalWords = value;
    }

}
