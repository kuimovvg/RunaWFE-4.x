package ru.cg.runaex.esb.bean;

import java.util.Date;
import java.util.GregorianCalendar;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

/**
 * @author Петров А.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "variable", namespace = "http://runaex", propOrder = {
    "name",
    "stringValue",
    "booleanValue",
    "longValue",
    "doubleValue",
    "dateValue",
    "byteaValue"
})
public class Variable {

  @XmlElement(name = "name", required = true, nillable = false, namespace = "http://runaex")
  private String name;

  @XmlElement(name = "stringValue", namespace = "http://runaex")
  private String stringValue;

  @XmlElement(name = "booleanValue", namespace = "http://runaex")
  private Boolean booleanValue;

  @XmlElement(name = "longValue", namespace = "http://runaex")
  private Long longValue;

  @XmlElement(name = "doubleValue", namespace = "http://runaex")
  private Double doubleValue;

  @XmlElement(name = "dateValue", namespace = "http://runaex")
  private XMLGregorianCalendar dateValue;

  @XmlElement(name = "byteaValue", namespace = "http://runaex")
  private byte[] byteaValue;

  protected void resetValue() {
    stringValue = null;
    booleanValue = null;
    longValue = null;
    doubleValue = null;
    dateValue = null;
    byteaValue = null;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    resetValue();
    this.stringValue = stringValue;
  }

  public Boolean getBooleanValue() {
    return booleanValue;
  }

  public void setBooleanValue(Boolean booleanValue) {
    resetValue();
    this.booleanValue = booleanValue;
  }

  public Long getLongValue() {
    return longValue;
  }

  public void setLongValue(Long longValue) {
    resetValue();
    this.longValue = longValue;
  }

  public Double getDoubleValue() {
    return doubleValue;
  }

  public void setDoubleValue(Double doubleValue) {
    resetValue();
    this.doubleValue = doubleValue;
  }

  public XMLGregorianCalendar getDateValue() {
    return dateValue;
  }

  public void setDateValue(XMLGregorianCalendar dateValue) {
    resetValue();
    this.dateValue = dateValue;
  }

  public byte[] getByteaValue() {
    return byteaValue;
  }

  public void setByteaValue(byte[] byteaValue) {
    resetValue();
    this.byteaValue = byteaValue;
  }

}
