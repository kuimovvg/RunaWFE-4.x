//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl;


/**
 * Java content class for anonymous complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/C:/AltLinux/projects/xsd/TC-1025_schema_10_xpdl.xsd line 491)
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025Cost"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025TimeEstimation"/>
 *       &lt;/sequence>
 *       &lt;attribute name="Instantiation">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="ONCE"/>
 *             &lt;enumeration value="MULTIPLE"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface TC1025SimulationInformationType {


    /**
     * Gets the value of the tc1025TimeEstimation property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025TimeEstimation}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025TimeEstimationType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025TimeEstimationType getTC1025TimeEstimation();

    /**
     * Sets the value of the tc1025TimeEstimation property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025TimeEstimation}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025TimeEstimationType}
     */
    void setTC1025TimeEstimation(ru.runa.xpdl.generated.bpmnxpdl.TC1025TimeEstimationType value);

    /**
     * Gets the value of the tc1025Cost property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getTC1025Cost();

    /**
     * Sets the value of the tc1025Cost property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setTC1025Cost(java.lang.String value);

    /**
     * Gets the value of the instantiation property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getInstantiation();

    /**
     * Sets the value of the instantiation property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setInstantiation(java.lang.String value);

}
