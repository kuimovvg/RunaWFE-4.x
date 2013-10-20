package ru.cg.runaex.web.model;

import java.io.Serializable;

/**
 * @author Bagautdinov
 */
public class PaginationConfig implements Serializable {
  private static final long serialVersionUID = -533951425639124284L;
  private Integer start;
  private Integer count;

  public Integer getStart() {
    return start;
  }

  public void setStart(Integer start) {
    this.start = start;
  }

  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }
}
