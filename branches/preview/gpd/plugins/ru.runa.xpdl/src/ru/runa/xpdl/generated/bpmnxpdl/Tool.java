//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl;


/**
 * Java content class for Tool element declaration.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/C:/AltLinux/projects/xsd/TC-1025_schema_10_xpdl.xsd line 554)
 * <p>
 * <pre>
 * &lt;element name="Tool">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025ActualParameters" minOccurs="0"/>
 *           &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025Description" minOccurs="0"/>
 *           &lt;element ref="{http://www.wfmc.org/2002/XPDL1.0}TC1025ExtendedAttributes" minOccurs="0"/>
 *         &lt;/sequence>
 *         &lt;attribute name="Id" use="required" type="{http://www.w3.org/2001/XMLSchema}NMTOKEN" />
 *         &lt;attribute name="Type">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *               &lt;enumeration value="APPLICATION"/>
 *               &lt;enumeration value="PROCEDURE"/>
 *             &lt;/restriction>
 *           &lt;/simpleType>
 *         &lt;/attribute>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 */
public interface Tool
    extends javax.xml.bind.Element, ru.runa.xpdl.generated.bpmnxpdl.ToolType
{


}
