//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl;


/**
 * Java content class for anonymous complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/C:/AltLinux/projects/xsd/bpmnxpdl_31.xsd line 2821)
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.wfmc.org/2008/XPDL2.1}WaitingTime" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2008/XPDL2.1}WorkingTime" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2008/XPDL2.1}Duration" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface TimeEstimationType {


    /**
     * Gets the value of the Any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the Any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link java.lang.Object}
     * 
     */
    java.util.List getAny();

    /**
     * Gets the value of the duration property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.DurationType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.Duration}
     */
    ru.runa.xpdl.generated.bpmnxpdl.DurationType getDuration();

    /**
     * Sets the value of the duration property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.DurationType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.Duration}
     */
    void setDuration(ru.runa.xpdl.generated.bpmnxpdl.DurationType value);

    /**
     * Gets the value of the workingTime property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.WorkingTimeType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.WorkingTime}
     */
    ru.runa.xpdl.generated.bpmnxpdl.WorkingTimeType getWorkingTime();

    /**
     * Sets the value of the workingTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.WorkingTimeType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.WorkingTime}
     */
    void setWorkingTime(ru.runa.xpdl.generated.bpmnxpdl.WorkingTimeType value);

    /**
     * Gets the value of the waitingTime property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.WaitingTime}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.WaitingTimeType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.WaitingTimeType getWaitingTime();

    /**
     * Sets the value of the waitingTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.WaitingTime}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.WaitingTimeType}
     */
    void setWaitingTime(ru.runa.xpdl.generated.bpmnxpdl.WaitingTimeType value);

}
