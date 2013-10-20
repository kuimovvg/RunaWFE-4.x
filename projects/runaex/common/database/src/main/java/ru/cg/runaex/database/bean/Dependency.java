package ru.cg.runaex.database.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

public class Dependency {
  protected List<Cell> dependency;
  protected String businessRule;

  protected Dependency(){

  }

  public Dependency(String bR){
    String groovyBusinessRule = bR;
    dependency=new LinkedList<Cell>();
    Matcher matcher = Cell.cellPattern.matcher(bR);
    int pos = 0;
    while (matcher.find(pos)) {
      Cell cell = new Cell(Long.valueOf(matcher.group(1)), matcher.group(2));
      dependency.add(cell);
      groovyBusinessRule = groovyBusinessRule.replace(cell.toString(), cell.getAlias());
      pos = matcher.end();
    }
    this.businessRule=groovyBusinessRule;
  }


  public List<Cell> getDependency() {
    return dependency;
  }

  public void setDependency(List<Cell> dependency) {
    this.dependency = dependency;
  }

  public String getBusinessRule() {
    return businessRule;
  }

  public void setBusinessRule(String businessRule) {
    this.businessRule = businessRule;
  }


}
