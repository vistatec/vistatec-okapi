
package net.sf.okapi.filters.xini.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Root content type
 * 
 * <p>Java class for Xini complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Xini"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TargetLanguages" type="{}TargetLanguages" minOccurs="0"/&gt;
 *         &lt;element name="Main" type="{}Main"/&gt;
 *         &lt;element name="FilterInfo" type="{}FilterInfo" minOccurs="0"/&gt;
 *         &lt;element name="StatisticInfo" type="{}StatisticInfo" minOccurs="0"/&gt;
 *         &lt;element name="FileInfo" type="{}FileInfo" minOccurs="0"/&gt;
 *         &lt;element name="JobInfo" type="{}JobInfo" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="SchemaVersion" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="1.0" /&gt;
 *       &lt;attribute name="SourceLanguage" type="{http://www.w3.org/2001/XMLSchema}token" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Xini", propOrder = {
    "targetLanguages",
    "main",
    "filterInfo",
    "statisticInfo",
    "fileInfo",
    "jobInfo"
})
@XmlRootElement(name = "Xini")
public class Xini {

    @XmlElement(name = "TargetLanguages")
    protected TargetLanguages targetLanguages;
    @XmlElement(name = "Main", required = true)
    protected Main main;
    @XmlElement(name = "FilterInfo")
    protected FilterInfo filterInfo;
    @XmlElement(name = "StatisticInfo")
    protected StatisticInfo statisticInfo;
    @XmlElement(name = "FileInfo")
    protected FileInfo fileInfo;
    @XmlElement(name = "JobInfo")
    protected JobInfo jobInfo;
    @XmlAttribute(name = "SchemaVersion", required = true)
    protected String schemaVersion;
    @XmlAttribute(name = "SourceLanguage")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String sourceLanguage;

    /**
     * Gets the value of the targetLanguages property.
     * 
     * @return
     *     possible object is
     *     {@link TargetLanguages }
     *     
     */
    public TargetLanguages getTargetLanguages() {
        return targetLanguages;
    }

    /**
     * Sets the value of the targetLanguages property.
     * 
     * @param value
     *     allowed object is
     *     {@link TargetLanguages }
     *     
     */
    public void setTargetLanguages(TargetLanguages value) {
        this.targetLanguages = value;
    }

    /**
     * Gets the value of the main property.
     * 
     * @return
     *     possible object is
     *     {@link Main }
     *     
     */
    public Main getMain() {
        return main;
    }

    /**
     * Sets the value of the main property.
     * 
     * @param value
     *     allowed object is
     *     {@link Main }
     *     
     */
    public void setMain(Main value) {
        this.main = value;
    }

    /**
     * Gets the value of the filterInfo property.
     * 
     * @return
     *     possible object is
     *     {@link FilterInfo }
     *     
     */
    public FilterInfo getFilterInfo() {
        return filterInfo;
    }

    /**
     * Sets the value of the filterInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterInfo }
     *     
     */
    public void setFilterInfo(FilterInfo value) {
        this.filterInfo = value;
    }

    /**
     * Gets the value of the statisticInfo property.
     * 
     * @return
     *     possible object is
     *     {@link StatisticInfo }
     *     
     */
    public StatisticInfo getStatisticInfo() {
        return statisticInfo;
    }

    /**
     * Sets the value of the statisticInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatisticInfo }
     *     
     */
    public void setStatisticInfo(StatisticInfo value) {
        this.statisticInfo = value;
    }

    /**
     * Gets the value of the fileInfo property.
     * 
     * @return
     *     possible object is
     *     {@link FileInfo }
     *     
     */
    public FileInfo getFileInfo() {
        return fileInfo;
    }

    /**
     * Sets the value of the fileInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link FileInfo }
     *     
     */
    public void setFileInfo(FileInfo value) {
        this.fileInfo = value;
    }

    /**
     * Gets the value of the jobInfo property.
     * 
     * @return
     *     possible object is
     *     {@link JobInfo }
     *     
     */
    public JobInfo getJobInfo() {
        return jobInfo;
    }

    /**
     * Sets the value of the jobInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link JobInfo }
     *     
     */
    public void setJobInfo(JobInfo value) {
        this.jobInfo = value;
    }

    /**
     * Gets the value of the schemaVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchemaVersion() {
        if (schemaVersion == null) {
            return "1.0";
        } else {
            return schemaVersion;
        }
    }

    /**
     * Sets the value of the schemaVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchemaVersion(String value) {
        this.schemaVersion = value;
    }

    /**
     * Gets the value of the sourceLanguage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceLanguage() {
        return sourceLanguage;
    }

    /**
     * Sets the value of the sourceLanguage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceLanguage(String value) {
        this.sourceLanguage = value;
    }

}
