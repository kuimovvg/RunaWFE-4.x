package ru.cg.runaex.web.model;

import java.util.List;

/**
 * @author urmancheev
 */
public class DeployedBusinessApplication {
  private List<Long> oldProcessDefinitionIds;
  private List<DeployedProcess> processes;

  public DeployedBusinessApplication(List<Long> oldProcessDefinitionIds, List<DeployedProcess> processes) {
    this.oldProcessDefinitionIds = oldProcessDefinitionIds;
    this.processes = processes;
  }

  public List<Long> getOldProcessDefinitionIds() {
    return oldProcessDefinitionIds;
  }

  public List<DeployedProcess> getProcesses() {
    return processes;
  }
}
