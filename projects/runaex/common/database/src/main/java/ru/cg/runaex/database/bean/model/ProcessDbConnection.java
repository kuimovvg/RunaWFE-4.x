package ru.cg.runaex.database.bean.model;

/**
 * @author urmancheev
 */
public class ProcessDbConnection {
  private Long processDefinitionId;
  private String jndiName;
  private String driverClassName;

  public ProcessDbConnection(Long processDefinitionId, String jndiName, String driverClassName) {
    this.processDefinitionId = processDefinitionId;
    this.jndiName = jndiName;
    this.driverClassName = driverClassName;
  }

  public Long getProcessDefinitionId() {
    return processDefinitionId;
  }

  public String getJndiName() {
    return jndiName;
  }

  public String getDriverClassName() {
    return driverClassName;
  }
}
