//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl;


/**
 * Java content class for anonymous complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/C:/AltLinux/projects/xsd/bpmnxpdl_31.xsd line 1999)
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.wfmc.org/2008/XPDL2.1}NodeGraphicsInfos" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2008/XPDL2.1}Lanes" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2008/XPDL2.1}Object" minOccurs="0"/>
 *         &lt;any/>
 *       &lt;/sequence>
 *       &lt;attribute name="BoundaryVisible" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="Id" use="required" type="{http://www.wfmc.org/2008/XPDL2.1}Id" />
 *       &lt;attribute name="MainPool" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="Orientation" default="HORIZONTAL">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="HORIZONTAL"/>
 *             &lt;enumeration value="VERTICAL"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="Participant" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="Process" type="{http://www.wfmc.org/2008/XPDL2.1}IdRef" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface PoolType {


    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getName();

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setName(java.lang.String value);

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
     * Gets the value of the lanes property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.LanesType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.Lanes}
     */
    ru.runa.xpdl.generated.bpmnxpdl.LanesType getLanes();

    /**
     * Sets the value of the lanes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.LanesType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.Lanes}
     */
    void setLanes(ru.runa.xpdl.generated.bpmnxpdl.LanesType value);

    /**
     * Gets the value of the orientation property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getOrientation();

    /**
     * Sets the value of the orientation property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setOrientation(java.lang.String value);

    /**
     * Gets the value of the boundaryVisible property.
     * 
     */
    boolean isBoundaryVisible();

    /**
     * Sets the value of the boundaryVisible property.
     * 
     */
    void setBoundaryVisible(boolean value);

    /**
     * Gets the value of the process property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getProcess();

    /**
     * Sets the value of the process property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setProcess(java.lang.String value);

    /**
     * Gets the value of the participant property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getParticipant();

    /**
     * Sets the value of the participant property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setParticipant(java.lang.String value);

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getId();

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setId(java.lang.String value);

    /**
     * Gets the value of the mainPool property.
     * 
     */
    boolean isMainPool();

    /**
     * Sets the value of the mainPool property.
     * 
     */
    void setMainPool(boolean value);

    /**
     * Gets the value of the object property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.Object}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.ObjectType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.ObjectType getObject();

    /**
     * Sets the value of the object property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.Object}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.ObjectType}
     */
    void setObject(ru.runa.xpdl.generated.bpmnxpdl.ObjectType value);

    /**
     * Gets the value of the nodeGraphicsInfos property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfos}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfosType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfosType getNodeGraphicsInfos();

    /**
     * Sets the value of the nodeGraphicsInfos property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfos}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfosType}
     */
    void setNodeGraphicsInfos(ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfosType value);

}
