package ru.cg.runaex.web.model;

import ru.cg.runaex.database.bean.ParFile;

/**
 * @author urmancheev
 */
public class DeployedProcess {
  private Long definitionId;
  private ParFile parFile;

  public DeployedProcess(Long definitionId, ParFile parFile) {
    this.definitionId = definitionId;
    this.parFile = parFile;
  }

  public Long getDefinitionId() {
    return definitionId;
  }

  public ParFile getParFile() {
    return parFile;
  }
}
