//
// This file was ru.runa.xpdl.generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v1.0.5-b16-fcs
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.05.04 at 12:24:30 PM MSD 
//


package ru.runa.xpdl.generated.jpdl32;


/**
 * Java content class for task element declaration.
 * <p>The following schema fragment specifies the expected content contained within this java content object. (defined at file:/D:/MyProjects/AltLinux/xsd/jpdl-3.2.xsd line 379)
 * <p>
 * <pre>
 * &lt;element name="task">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;choice maxOccurs="unbounded" minOccurs="0">
 *           &lt;element ref="{urn:jbpm.org:jpdl-3.2}description"/>
 *           &lt;element ref="{urn:jbpm.org:jpdl-3.2}assignment"/>
 *           &lt;element ref="{urn:jbpm.org:jpdl-3.2}controller"/>
 *           &lt;element ref="{urn:jbpm.org:jpdl-3.2}event"/>
 *           &lt;element ref="{urn:jbpm.org:jpdl-3.2}timer"/>
 *           &lt;element name="reminder">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;attribute name="duedate" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                   &lt;attribute name="repeat" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *         &lt;/choice>
 *         &lt;attribute name="blocking" type="{urn:jbpm.org:jpdl-3.2}booleanType" default="false" />
 *         &lt;attribute name="description" type="{http://www.w3.org/2001/XMLSchema}string" />
 *         &lt;attribute name="duedate" type="{http://www.w3.org/2001/XMLSchema}string" />
 *         &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *         &lt;attribute name="notify" type="{urn:jbpm.org:jpdl-3.2}booleanType" default="false" />
 *         &lt;attribute name="priority" type="{urn:jbpm.org:jpdl-3.2}priorityType" default="normal" />
 *         &lt;attribute name="reassign" type="{urn:jbpm.org:jpdl-3.2}booleanType" default="false" />
 *         &lt;attribute name="signalling" type="{urn:jbpm.org:jpdl-3.2}booleanType" default="true" />
 *         &lt;attribute name="swimlane" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 * 
 */
public interface Task
    extends javax.xml.bind.Element, ru.runa.xpdl.generated.jpdl32.TaskType
{


}
