
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for dynamicField complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="dynamicField">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="dynamicValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fieldIdx" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "dynamicField", propOrder = {
    "dynamicValue",
    "fieldIdx"
})
public class DynamicField {

    protected String dynamicValue;
    protected Long fieldIdx;

    /**
     * Gets the value of the dynamicValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDynamicValue() {
        return dynamicValue;
    }

    /**
     * Sets the value of the dynamicValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDynamicValue(String value) {
        this.dynamicValue = value;
    }

    /**
     * Gets the value of the fieldIdx property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getFieldIdx() {
        return fieldIdx;
    }

    /**
     * Sets the value of the fieldIdx property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setFieldIdx(Long value) {
        this.fieldIdx = value;
    }

}
