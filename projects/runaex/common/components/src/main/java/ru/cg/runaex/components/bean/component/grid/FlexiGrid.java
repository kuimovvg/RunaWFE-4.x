package ru.cg.runaex.components.bean.component.grid;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.Component;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.validation.annotation.*;

/**
 * @author urmancheev
 */
public class FlexiGrid extends Component implements GridComponent {
  private static final long serialVersionUID = 6420684257321373793L;

  private static final int TABLE = 0;
  private static final int COLUMNS = 1;
  private static final int TABLE_ID = 2;
  private static final int FILTER = 3;
  private static final int HEIGHT_WEIGHT = 4;
  private static final int SORT_COLUMN = 5;
  private static final int SORT_ORDER = 6;
  private static final int WIDTH_WEIGHT = 7;
  private static final int VISIBLE_PAGINATION = 8;
  private static final int VISIBILITY_RULE = 9;

  private TableReference table;
  private String sortColumn;

  boolean isPaginationVisible;
  private List<GridColumn> columns;

  @Override
  public int getParametersNumber() {
    return 10;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    table = parseTableReferenceInitTerm(getParameter(TABLE));
    ColumnReference sortColumnReference = parseColumnReference(getParameter(SORT_COLUMN));
    sortColumn = sortColumnReference != null ? sortColumnReference.getColumn() : null;

    isPaginationVisible = convertVisiblePagination(getVisiblePaginationStr());
    columns = parseGridColumns(getColumnsStr());
  }

  @NotNullSchema
  @DatabaseStructureElement
  public String getSchema() {
    ensureFullyInitialized();
    return table.getSchema();
  }

  @NotNull
  @DatabaseStructureElement
  public String getTable() {
    ensureFullyInitialized();
    return table.getTable();
  }

  @NotNull
  public String getColumnsStr() {
    return getParameter(COLUMNS);
  }

  @NotNull
  public String getTableId() {
    return getParameter(TABLE_ID);
  }

  public String getFilter() {
    return getParameter(FILTER);
  }

  @AssertNumber
  public String getHeightWeight() {
    return getParameter(HEIGHT_WEIGHT);
  }

  @DatabaseStructureElement
  public String getSortColumn() {
    ensureFullyInitialized();
    return sortColumn;
  }

  @SortOrder
  public String getSortOrder() {
    return getParameter(SORT_ORDER);
  }

  @AssertNumber
  public String getWidthWeight() {
    return getParameter(WIDTH_WEIGHT);
  }

  @AssertBoolean
  public String getVisiblePaginationStr() {
    return getParameter(VISIBLE_PAGINATION);
  }

  @Valid
  public List<GridColumn> getColumns() {
    ensureFullyInitialized();
    return columns;
  }

  @Override
  public boolean isPaginationVisible() {
    ensureFullyInitialized();
    return isPaginationVisible;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }
}
