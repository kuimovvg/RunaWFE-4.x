//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl;


/**
 * Java content class for anonymous complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/C:/AltLinux/projects/xsd/TC-1025_schema_10_xpdl.xsd line 304)
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025No"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}Tool" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025SubFlow"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface TC1025ImplementationType {


    /**
     * Gets the value of the tc1025SubFlow property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025SubFlow}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025SubFlowType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025SubFlowType getTC1025SubFlow();

    /**
     * Sets the value of the tc1025SubFlow property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025SubFlow}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025SubFlowType}
     */
    void setTC1025SubFlow(ru.runa.xpdl.generated.bpmnxpdl.TC1025SubFlowType value);

    /**
     * Gets the value of the Tool property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the Tool property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTool().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ru.runa.xpdl.generated.bpmnxpdl.Tool}
     * {@link ru.runa.xpdl.generated.bpmnxpdl.ToolType}
     * 
     */
    java.util.List getTool();

    /**
     * Gets the value of the tc1025No property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025No}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025NoType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025NoType getTC1025No();

    /**
     * Sets the value of the tc1025No property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025No}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025NoType}
     */
    void setTC1025No(ru.runa.xpdl.generated.bpmnxpdl.TC1025NoType value);

}
