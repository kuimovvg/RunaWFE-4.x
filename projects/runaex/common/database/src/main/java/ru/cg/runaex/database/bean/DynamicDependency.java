package ru.cg.runaex.database.bean;

import java.util.ArrayList;
import java.util.List;

import ru.cg.runaex.database.context.DatabaseSpringContext;

/**
 * @author Абдулин Ильдар
 */
public class DynamicDependency extends Dependency {

  private Cell mainCell;
  private FullTableParam tableParam;
  private transient Long processDefinitionId;

  public DynamicDependency(Cell mainCell, String bR, FullTableParam tableParam) {
    this.mainCell = mainCell;
    this.tableParam = tableParam;
    dependency=new ArrayList<Cell>();

  }

  public static boolean isDynamicDependency(String businessRule) {
    if (DependencyFunctions.CHILDREN_SUM.getFunction().equals(businessRule.trim())) {
      return true;
    }
    return false;
  }

  public void init(Long processDefinitionId){
    this.processDefinitionId=processDefinitionId;
    DependencyAdapter dependencyAdapter=DatabaseSpringContext.getChildrenSumDependencyAdapter();
    dependency.clear();
    dependency.addAll(dependencyAdapter.getDependencies(processDefinitionId,mainCell,tableParam));
    businessRule=dependencyAdapter.getBusinessRule(dependency);
  }
}
