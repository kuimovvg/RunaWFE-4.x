//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.23 at 09:51:37 PM MSD 
//


package ru.runa.xpdl.generated.bpmnxpdl;


/**
 * BPMN and XPDL
 * Java content class for Assignment element declaration.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/C:/AltLinux/projects/xsd/bpmnxpdl_31.xsd line 474)
 * <p>
 * <pre>
 * &lt;element name="Assignment">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="Target" type="{http://www.wfmc.org/2008/XPDL2.1}ExpressionType"/>
 *           &lt;element name="Expression" type="{http://www.wfmc.org/2008/XPDL2.1}ExpressionType"/>
 *           &lt;any/>
 *         &lt;/sequence>
 *         &lt;attribute name="AssignTime" default="Start">
 *           &lt;simpleType>
 *             &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *               &lt;enumeration value="Start"/>
 *               &lt;enumeration value="End"/>
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
public interface Assignment
    extends javax.xml.bind.Element, ru.runa.xpdl.generated.bpmnxpdl.AssignmentType
{


}
