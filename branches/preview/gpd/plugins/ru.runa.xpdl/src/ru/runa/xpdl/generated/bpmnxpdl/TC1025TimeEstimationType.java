//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl;


/**
 * Java content class for anonymous complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/C:/AltLinux/projects/xsd/TC-1025_schema_10_xpdl.xsd line 546)
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025WaitingTime" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025WorkingTime" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025Duration" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface TC1025TimeEstimationType {


    /**
     * Gets the value of the tc1025Duration property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getTC1025Duration();

    /**
     * Sets the value of the tc1025Duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setTC1025Duration(java.lang.String value);

    /**
     * Gets the value of the tc1025WorkingTime property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getTC1025WorkingTime();

    /**
     * Sets the value of the tc1025WorkingTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setTC1025WorkingTime(java.lang.String value);

    /**
     * Gets the value of the tc1025WaitingTime property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getTC1025WaitingTime();

    /**
     * Sets the value of the tc1025WaitingTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setTC1025WaitingTime(java.lang.String value);

}
