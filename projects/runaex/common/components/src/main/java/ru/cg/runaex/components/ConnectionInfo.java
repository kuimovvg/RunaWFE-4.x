package ru.cg.runaex.components;

import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.Gson;

/**
 * @author Абдулин Ильдар
 */
public class ConnectionInfo {
  private String driverClassName;
  private String dbUrl;

  private String targetDbName;
  private String targetDbUsername;
  private String targetDbPassword;

  private String referenceDbName;
  private String referenceDbUsername;
  private String referenceDbPassword;

  private String jndiName;

  public String getJndiName() {
    return jndiName;
  }

  public void setJndiName(String jndiName) throws NamingException {
    this.jndiName = jndiName;

    Context initCtx = new InitialContext();
    Object obj = initCtx.lookup(jndiName);

    if (obj instanceof Properties) {
      Properties properties = (Properties) obj;
      driverClassName = properties.getProperty("driverClassName");
      dbUrl = properties.getProperty("dbUrl");

      targetDbName = properties.getProperty("targetDbName");
      targetDbUsername = properties.getProperty("targetDbUsername");
      targetDbPassword = properties.getProperty("targetDbPassword");

      referenceDbName = properties.getProperty("referenceDbName");
      referenceDbUsername = properties.getProperty("referenceDbUsername");
      referenceDbPassword = properties.getProperty("referenceDbPassword");
    }
    else if (obj instanceof String) {
      Map<String,String> propertiesMap = new Gson().fromJson((String) obj, Map.class);

      driverClassName = propertiesMap.get("driverClassName");
      dbUrl = propertiesMap.get("dbUrl");

      targetDbName = propertiesMap.get("targetDbName");
      targetDbUsername = propertiesMap.get("targetDbUsername");
      targetDbPassword = propertiesMap.get("targetDbPassword");

      referenceDbName = propertiesMap.get("referenceDbName");
      referenceDbUsername = propertiesMap.get("referenceDbUsername");
      referenceDbPassword = propertiesMap.get("referenceDbPassword");
    }
    else {
      throw new NamingException("object must be " + Properties.class.getName());
    }
  }

  public String getDriverClassName() {
    return driverClassName;
  }

  public void setDriverClassName(String driverClassName) {
    this.driverClassName = driverClassName;
  }

  public String getTargetDbUrl() {
    return getDbUrl() + "/" + getTargetDbName();
  }

  public String getTargetDbUsername() {
    return targetDbUsername;
  }

  public void setTargetDbUsername(String targetDbUsername) {
    this.targetDbUsername = targetDbUsername;
  }

  public String getTargetDbPassword() {
    return targetDbPassword;
  }

  public void setTargetDbPassword(String targetDbPassword) {
    this.targetDbPassword = targetDbPassword;
  }

  public String getReferenceDbUrl() {
    return getDbUrl() + "/" + getReferenceDbName();
  }

  public String getReferenceDbUsername() {
    return referenceDbUsername;
  }

  public void setReferenceDbUsername(String referenceDbUsername) {
    this.referenceDbUsername = referenceDbUsername;
  }

  public String getReferenceDbPassword() {
    return referenceDbPassword;
  }

  public void setReferenceDbPassword(String referenceDbPassword) {
    this.referenceDbPassword = referenceDbPassword;
  }

  public String getDbUrl() {
    return dbUrl;
  }

  public void setDbUrl(String dbUrl) {
    this.dbUrl = dbUrl;
  }

  public String getTargetDbName() {
    return targetDbName;
  }

  public void setTargetDbName(String targetDbName) {
    this.targetDbName = targetDbName;
  }

  public String getReferenceDbName() {
    return referenceDbName;
  }

  public void setReferenceDbName(String referenceDbName) {
    this.referenceDbName = referenceDbName;
  }
}
