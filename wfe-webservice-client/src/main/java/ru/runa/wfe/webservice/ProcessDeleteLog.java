
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for processDeleteLog complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="processDeleteLog">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}systemLog">
 *       &lt;sequence>
 *         &lt;element name="processId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processDeleteLog", propOrder = {
    "processId"
})
public class ProcessDeleteLog
    extends SystemLog
{

    protected Long processId;

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
