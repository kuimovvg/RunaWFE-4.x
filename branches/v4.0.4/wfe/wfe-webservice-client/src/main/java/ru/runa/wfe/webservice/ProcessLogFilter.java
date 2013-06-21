
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
    "processId"
})
public class ProcessLogFilter {

    protected boolean includeSubprocessLogs;
    protected Long processId;

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

}
