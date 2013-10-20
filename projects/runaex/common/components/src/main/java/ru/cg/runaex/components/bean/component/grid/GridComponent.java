package ru.cg.runaex.components.bean.component.grid;

import java.util.List;

import ru.cg.runaex.components.bean.component.IsComponent;
import ru.cg.runaex.components.bean.component.part.GridColumn;

/**
 * @author urmancheev
 */
public interface GridComponent extends IsComponent {

  public String getSchema();

  public String getTable();

  public String getTableId();

  public <T extends GridColumn> List<T> getColumns();

  public String getHeightWeight();

  public String getWidthWeight();

  public String getSortOrder();

  public String getSortColumn();

  public String getFilter();

  public boolean isPaginationVisible();
}
