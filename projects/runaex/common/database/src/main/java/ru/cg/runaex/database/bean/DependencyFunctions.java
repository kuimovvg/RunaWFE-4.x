package ru.cg.runaex.database.bean;

/**
 * @author Абдулин Ильдар
 */
public enum DependencyFunctions {
  CHILDREN_SUM("cSum()");
  private String function;

  DependencyFunctions(String function){
    this.function=function;
  }

  public String getFunction(){
    return function;
  }
}
