
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for processLogFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="processLogFilter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="includeSubprocessLogs" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="processId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="severities" type="{http://impl.service.wfe.runa.ru/}severity" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processLogFilter", propOrder = {
    "includeSubprocessLogs",
    "processId",
    "severities"
})
public class ProcessLogFilter {

    protected boolean includeSubprocessLogs;
    protected Long processId;
    @XmlElement(nillable = true)
    protected List<Severity> severities;

    /**
     * Gets the value of the includeSubprocessLogs property.
     * 
     */
    public boolean isIncludeSubprocessLogs() {
        return includeSubprocessLogs;
    }

    /**
     * Sets the value of the includeSubprocessLogs property.
     * 
     */
    public void setIncludeSubprocessLogs(boolean value) {
        this.includeSubprocessLogs = value;
    }

    /**
     * Gets the value of the processId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getProcessId() {
        return processId;
    }

    /**
     * Sets the value of the processId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setProcessId(Long value) {
        this.processId = value;
    }

    /**
     * Gets the value of the severities property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the severities property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSeverities().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Severity }
     * 
     * 
     */
    public List<Severity> getSeverities() {
        if (severities == null) {
            severities = new ArrayList<Severity>();
        }
        return this.severities;
    }

}
