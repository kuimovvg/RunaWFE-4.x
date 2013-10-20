package ru.cg.runaex.database.bean.model;

/**
 * @author Абдулин Ильдар
 */
public class SphinxSearchIndexingAndViewColumns {
  private String indexingColumns;
  private String viewColumns;

  public String getIndexingColumns() {
    return indexingColumns;
  }

  public void setIndexingColumns(String indexingColumns) {
    this.indexingColumns = indexingColumns;
  }

  public String getViewColumns() {
    return viewColumns;
  }

  public void setViewColumns(String viewColumns) {
    this.viewColumns = viewColumns;
  }
}
