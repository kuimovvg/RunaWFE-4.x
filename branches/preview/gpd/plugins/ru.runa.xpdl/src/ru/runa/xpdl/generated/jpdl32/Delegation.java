//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.04 at 12:24:30 PM MSD 
//


package ru.runa.xpdl.generated.jpdl32;


/**
 * Java content class for delegation complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/D:/MyProjects/AltLinux/xsd/jpdl-3.2.xsd line 459)
 * <p>
 * <pre>
 * &lt;complexType name="delegation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="class2" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="config-type" default="field">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *             &lt;enumeration value="field"/>
 *             &lt;enumeration value="bean"/>
 *             &lt;enumeration value="constructor"/>
 *             &lt;enumeration value="configuration-property"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface Delegation {


    /**
     * Gets the value of the class2 property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getClass2();

    /**
     * Sets the value of the class2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setClass2(java.lang.String value);

    /**
     * Gets the value of the Content property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the Content property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.Object}
     * {@link java.lang.String}
     * 
     */
    java.util.List getContent();

    /**
     * Gets the value of the configType property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getConfigType();

    /**
     * Sets the value of the configType property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setConfigType(java.lang.String value);

}
