
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for variableDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="variableDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="syntetic" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formatClassName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="publicAccess" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="defaultValue" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scriptingName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formatLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "variableDefinition", propOrder = {
    "syntetic",
    "name",
    "formatClassName",
    "publicAccess",
    "defaultValue",
    "scriptingName",
    "formatLabel"
})
public class VariableDefinition {

    protected boolean syntetic;
    protected String name;
    protected String formatClassName;
    protected boolean publicAccess;
    protected String defaultValue;
    protected String scriptingName;
    protected String formatLabel;

    /**
     * Gets the value of the syntetic property.
     * 
     */
    public boolean isSyntetic() {
        return syntetic;
    }

    /**
     * Sets the value of the syntetic property.
     * 
     */
    public void setSyntetic(boolean value) {
        this.syntetic = value;
    }

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
     * Gets the value of the formatClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatClassName() {
        return formatClassName;
    }

    /**
     * Sets the value of the formatClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatClassName(String value) {
        this.formatClassName = value;
    }

    /**
     * Gets the value of the publicAccess property.
     * 
     */
    public boolean isPublicAccess() {
        return publicAccess;
    }

    /**
     * Sets the value of the publicAccess property.
     * 
     */
    public void setPublicAccess(boolean value) {
        this.publicAccess = value;
    }

    /**
     * Gets the value of the defaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the value of the defaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    /**
     * Gets the value of the scriptingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScriptingName() {
        return scriptingName;
    }

    /**
     * Sets the value of the scriptingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScriptingName(String value) {
        this.scriptingName = value;
    }

    /**
     * Gets the value of the formatLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatLabel() {
        return formatLabel;
    }

    /**
     * Sets the value of the formatLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatLabel(String value) {
        this.formatLabel = value;
    }

}
