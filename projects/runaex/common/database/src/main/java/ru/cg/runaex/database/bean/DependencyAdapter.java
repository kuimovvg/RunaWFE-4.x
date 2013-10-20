package ru.cg.runaex.database.bean;

import java.util.List;

/**
 * @author Абдулин Ильдар
 */
public interface DependencyAdapter {

  public List<Cell> getDependencies(Long processDefinitionId,Cell cell,FullTableParam param);

  public String getBusinessRule(List<Cell> cells);
}
