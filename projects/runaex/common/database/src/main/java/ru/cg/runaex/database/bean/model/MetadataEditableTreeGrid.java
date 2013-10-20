package ru.cg.runaex.database.bean.model;

import ru.cg.runaex.database.bean.DependencyMatrix;

/**
 * @author Абдулин Ильдар
 */
public class MetadataEditableTreeGrid {
  private Long id;
  private String tableId;
  private String businessRule;
  private String editableRule;
  private String cssClass;
  private DependencyMatrix dependencyMatrix;

  public MetadataEditableTreeGrid() {

  }

  public MetadataEditableTreeGrid(Long id, String tableId, String businessRule, String cssClass, String editableRule, DependencyMatrix dependencyMatrix) {
    this.id = id;
    this.tableId = tableId;
    this.businessRule = businessRule;
    this.editableRule = editableRule;
    this.cssClass = cssClass;
    this.dependencyMatrix = dependencyMatrix;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTableId() {
    return tableId;
  }

  public void setTableId(String tableId) {
    this.tableId = tableId;
  }

  public String getBusinessRule() {
    return businessRule;
  }

  public void setBusinessRule(String businessRule) {
    this.businessRule = businessRule;
  }

  public String getEditableRule() {
    return editableRule;
  }

  public String getCssClass() {
    return cssClass;
  }

  public void setEditableRule(String editableRule) {
    this.editableRule = editableRule;
  }

  public void setCssClass(String cssClass) {
    this.cssClass = cssClass;
  }

  public DependencyMatrix getDependencyMatrix() {
    return dependencyMatrix;
  }

  public void setDependencyMatrix(DependencyMatrix dependencyMatrix) {
    this.dependencyMatrix = dependencyMatrix;
  }
}
