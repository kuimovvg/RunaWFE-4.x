package ru.cg.runaex.web.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author urmancheev
 */
public class FlexiGridResult implements Serializable {

  private static final long serialVersionUID = -4061082274051262773L;
  private Integer page;
  private Integer total;
  private List<Row> rows;

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public List<Row> getRows() {
    return rows;
  }

  public void setRows(List<Row> rows) {
    this.rows = rows;
  }

  public static class Row implements Serializable {

    private static final long serialVersionUID = -2238847237086898579L;
    private String id;
    private List<String> cell;

    public Row(Long id, List<String> cell) {
      this(String.valueOf(id), cell);
    }

    public Row(String id, List<String> cell) {
      this.id = id;
      this.cell = cell;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public List<String> getCell() {
      return cell;
    }

    public void setCell(List<String> cell) {
      this.cell = cell;
    }
  }
}
