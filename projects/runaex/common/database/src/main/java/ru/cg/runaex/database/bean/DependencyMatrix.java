package ru.cg.runaex.database.bean;

import java.util.*;
import java.util.regex.Matcher;

import javax.persistence.Transient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Абдулин Ильдар
 */
public class DependencyMatrix {


  private Map<Cell, Dependency> staticDependencies;
  private Map<Cell, DynamicDependency> dynamicDependencies;
  private transient Map<Cell, Dependency> dependencies;

  public static DependencyMatrix create(List<String> businessRuleRows, FullTableParam tableParam) {
    return new DependencyMatrix(businessRuleRows, tableParam);
  }

  private DependencyMatrix(List<String> businessRuleRows, FullTableParam tableParam) {
    dependencies = new HashMap<Cell, Dependency>();
    staticDependencies = new HashMap<Cell, Dependency>();
    dynamicDependencies = new HashMap<Cell, DynamicDependency>();

    for (String bR : businessRuleRows) {
      if (!validateBusinessRule(bR))
        throw new RuntimeException("validate error");

      Matcher matcher = Cell.cellPattern.matcher(bR);
      matcher.find();
      Cell cell = new Cell(Long.valueOf(matcher.group(1)), matcher.group(2));
      String rightValue = getRightValue(bR);
      if (DynamicDependency.isDynamicDependency(rightValue)) {
        dynamicDependencies.put(cell, new DynamicDependency(cell, rightValue, tableParam));
      }
      else {
        staticDependencies.put(cell, new Dependency(rightValue));
      }
    }
    refresh(null);
  }

  public void refresh(Long processDefinitionId) {
    if(dependencies==null)
      dependencies=new HashMap<Cell, Dependency>();
    dependencies.clear();
    dependencies.putAll(staticDependencies);
    if (processDefinitionId == null)
      for (Cell cell : staticDependencies.keySet()) {
        recalculateCell(cell);
      }
    else {
      for (DynamicDependency d : dynamicDependencies.values()) {
        d.init(processDefinitionId);
      }
      dependencies.putAll(dynamicDependencies);
      for (Cell cell : dynamicDependencies.keySet()) {
        recalculateCell(cell);
      }
    }

  }

  protected String getRightValue(String businessRule) {
    return businessRule.split("=")[1];
  }

  protected Dependency recalculateCell(Cell cell) {
    Dependency dp = dependencies.get(cell);
    if (dp == null) {
      return null;
    }
    List<Cell> removeDp = new ArrayList<Cell>();
    List<Cell> addDp = new ArrayList<Cell>();
    for (Cell dpCell : dp.getDependency()) {
      Dependency ndp = recalculateCell(dpCell);
      if (ndp != null) {
        dp.setBusinessRule(dp.getBusinessRule().replace(dpCell.getAlias(), ndp.getBusinessRule()));
        removeDp.add(dpCell);
        addDp.addAll(ndp.getDependency());
      }
    }
    dp.getDependency().removeAll(removeDp);
    dp.getDependency().addAll(addDp);
    return dp;
  }

  public static DependencyMatrix fromJson(String json) {
    if (json == null || json.isEmpty())
      return null;
    return getGson().fromJson(json, DependencyMatrix.class);
  }

  private static Gson getGson() {
    return new GsonBuilder().registerTypeAdapter(Cell.class, new Cell.CellDeserializer()).registerTypeAdapter(Cell.class, new Cell.CellSerializer()).create();
  }

  public String toJson() {
    return getGson().toJson(this);
  }

  public static boolean validateBusinessRule(String businessRule) {
    return true;
  }

  public Map<Cell, Dependency> getDependencies() {
    return dependencies;
  }
}