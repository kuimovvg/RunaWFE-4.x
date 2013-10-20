package ru.cg.runaex.web.model;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Bagautdinov
 */
public class LoadConfig implements Serializable {
  private static final long serialVersionUID = 8169711152684685310L;
  private Map<String,String> filter;
  private PaginationConfig pagination;
  private Map<String, String> order;

  public Map<String, String> getFilter() {
    return filter;
  }

  public void setFilter(Map<String, String> filter) {
    this.filter = filter;
  }

  public PaginationConfig getPagination() {
    return pagination;
  }

  public void setPagination(PaginationConfig pagination) {
    this.pagination = pagination;
  }

  public Map<String, String> getOrder() {
    return order;
  }

  public void setOrder(Map<String, String> order) {
    this.order = order;
  }
}
