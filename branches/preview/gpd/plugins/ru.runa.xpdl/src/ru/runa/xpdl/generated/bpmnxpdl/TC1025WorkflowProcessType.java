//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl;


/**
 * Java content class for anonymous complex type.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/C:/AltLinux/projects/xsd/TC-1025_schema_10_xpdl.xsd line 650)
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025ProcessHeader"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025RedefinableHeader" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025FormalParameters" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025DataFields" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025Participants" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025Applications" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025ActivitySets" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025Activities" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025Transitions" minOccurs="0"/>
 *         &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025ExtendedAttributes" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="AccessLevel">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *             &lt;enumeration value="PUBLIC"/>
 *             &lt;enumeration value="PRIVATE"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *       &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *       &lt;attribute name="Name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 */
public interface TC1025WorkflowProcessType {


    /**
     * Gets the value of the accessLevel property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String}
     */
    java.lang.String getAccessLevel();

    /**
     * Sets the value of the accessLevel property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String}
     */
    void setAccessLevel(java.lang.String value);

    /**
     * Gets the value of the tc1025DataFields property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025DataFieldsType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025DataFields}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025DataFieldsType getTC1025DataFields();

    /**
     * Sets the value of the tc1025DataFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025DataFieldsType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025DataFields}
     */
    void setTC1025DataFields(ru.runa.xpdl.generated.bpmnxpdl.TC1025DataFieldsType value);

    /**
     * Gets the value of the tc1025FormalParameters property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025FormalParameters}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025FormalParametersType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025FormalParametersType getTC1025FormalParameters();

    /**
     * Sets the value of the tc1025FormalParameters property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025FormalParameters}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025FormalParametersType}
     */
    void setTC1025FormalParameters(ru.runa.xpdl.generated.bpmnxpdl.TC1025FormalParametersType value);

    /**
     * Gets the value of the tc1025ProcessHeader property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ProcessHeaderType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ProcessHeader}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025ProcessHeaderType getTC1025ProcessHeader();

    /**
     * Sets the value of the tc1025ProcessHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ProcessHeaderType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ProcessHeader}
     */
    void setTC1025ProcessHeader(ru.runa.xpdl.generated.bpmnxpdl.TC1025ProcessHeaderType value);

    /**
     * Gets the value of the tc1025ExtendedAttributes property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ExtendedAttributesType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ExtendedAttributes}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025ExtendedAttributesType getTC1025ExtendedAttributes();

    /**
     * Sets the value of the tc1025ExtendedAttributes property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ExtendedAttributesType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ExtendedAttributes}
     */
    void setTC1025ExtendedAttributes(ru.runa.xpdl.generated.bpmnxpdl.TC1025ExtendedAttributesType value);

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
     * Gets the value of the tc1025ActivitySets property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitySetsType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitySets}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitySetsType getTC1025ActivitySets();

    /**
     * Sets the value of the tc1025ActivitySets property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitySetsType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitySets}
     */
    void setTC1025ActivitySets(ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitySetsType value);

    /**
     * Gets the value of the tc1025Activities property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitiesType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025Activities}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitiesType getTC1025Activities();

    /**
     * Sets the value of the tc1025Activities property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitiesType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025Activities}
     */
    void setTC1025Activities(ru.runa.xpdl.generated.bpmnxpdl.TC1025ActivitiesType value);

    /**
     * Gets the value of the tc1025RedefinableHeader property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025RedefinableHeader}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025RedefinableHeaderType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025RedefinableHeaderType getTC1025RedefinableHeader();

    /**
     * Sets the value of the tc1025RedefinableHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025RedefinableHeader}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025RedefinableHeaderType}
     */
    void setTC1025RedefinableHeader(ru.runa.xpdl.generated.bpmnxpdl.TC1025RedefinableHeaderType value);

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
     * Gets the value of the tc1025Participants property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025Participants}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ParticipantsType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025ParticipantsType getTC1025Participants();

    /**
     * Sets the value of the tc1025Participants property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025Participants}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ParticipantsType}
     */
    void setTC1025Participants(ru.runa.xpdl.generated.bpmnxpdl.TC1025ParticipantsType value);

    /**
     * Gets the value of the tc1025Applications property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025Applications}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ApplicationsType}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025ApplicationsType getTC1025Applications();

    /**
     * Sets the value of the tc1025Applications property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025Applications}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025ApplicationsType}
     */
    void setTC1025Applications(ru.runa.xpdl.generated.bpmnxpdl.TC1025ApplicationsType value);

    /**
     * Gets the value of the tc1025Transitions property.
     * 
     * @return
     *     possible object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025TransitionsType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025Transitions}
     */
    ru.runa.xpdl.generated.bpmnxpdl.TC1025TransitionsType getTC1025Transitions();

    /**
     * Sets the value of the tc1025Transitions property.
     * 
     * @param value
     *     allowed object is
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025TransitionsType}
     *     {@link ru.runa.xpdl.generated.bpmnxpdl.TC1025Transitions}
     */
    void setTC1025Transitions(ru.runa.xpdl.generated.bpmnxpdl.TC1025TransitionsType value);

}
