
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for interaction complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="interaction">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="validationData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="useJSValidation" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="scriptData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="cssData" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="requiredVariableNames" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interaction", propOrder = {
    "name",
    "description",
    "type",
    "formData",
    "validationData",
    "useJSValidation",
    "scriptData",
    "cssData",
    "requiredVariableNames"
})
public class Interaction {

    protected String name;
    protected String description;
    protected String type;
    protected byte[] formData;
    protected byte[] validationData;
    protected boolean useJSValidation;
    protected byte[] scriptData;
    protected byte[] cssData;
    @XmlElement(nillable = true)
    protected List<String> requiredVariableNames;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the formData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getFormData() {
        return formData;
    }

    /**
     * Sets the value of the formData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setFormData(byte[] value) {
        this.formData = ((byte[]) value);
    }

    /**
     * Gets the value of the validationData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getValidationData() {
        return validationData;
    }

    /**
     * Sets the value of the validationData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setValidationData(byte[] value) {
        this.validationData = ((byte[]) value);
    }

    /**
     * Gets the value of the useJSValidation property.
     * 
     */
    public boolean isUseJSValidation() {
        return useJSValidation;
    }

    /**
     * Sets the value of the useJSValidation property.
     * 
     */
    public void setUseJSValidation(boolean value) {
        this.useJSValidation = value;
    }

    /**
     * Gets the value of the scriptData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getScriptData() {
        return scriptData;
    }

    /**
     * Sets the value of the scriptData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setScriptData(byte[] value) {
        this.scriptData = ((byte[]) value);
    }

    /**
     * Gets the value of the cssData property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getCssData() {
        return cssData;
    }

    /**
     * Sets the value of the cssData property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setCssData(byte[] value) {
        this.cssData = ((byte[]) value);
    }

    /**
     * Gets the value of the requiredVariableNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the requiredVariableNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRequiredVariableNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getRequiredVariableNames() {
        if (requiredVariableNames == null) {
            requiredVariableNames = new ArrayList<String>();
        }
        return this.requiredVariableNames;
    }

}
