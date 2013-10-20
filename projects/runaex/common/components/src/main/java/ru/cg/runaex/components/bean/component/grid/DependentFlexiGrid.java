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
public class DependentFlexiGrid extends Component implements GridComponent {
  private static final long serialVersionUID = -5210710847906189643L;

  private static final int TABLE = 0;
  private static final int MAIN_TABLE_REFERENCE = 1;
  private static final int COLUMNS = 2;
  private static final int TABLE_ID = 3;
  private static final int FILTER = 4;
  private static final int HEIGHT_WEIGHT = 5;
  private static final int SORT_COLUMN = 6;
  private static final int SORT_ORDER = 7;
  private static final int WIDTH_WEIGHT = 8;
  private static final int VISIBLE_PAGINATION = 9;
  private static final int VISIBILITY_RULE = 10;

  private TableReference table;
  private String sortColumn;

  private boolean isPaginationVisible;
  private TableReference mainTableReference;
  private List<GridColumn> columns;

  @Override
  public int getParametersNumber() {
    return 11;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    table = parseTableReferenceInitTerm(getParameter(TABLE));
    ColumnReference sortColumnReference = parseColumnReference(getParameter(SORT_COLUMN));
    sortColumn = sortColumnReference != null ? sortColumnReference.getColumn() : null;

    isPaginationVisible = convertVisiblePagination(getVisiblePaginationStr());
    mainTableReference = parseTableReference(getMainTableReferenceStr());
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

  public String getMainTableReferenceStr() {
    return getParameter(MAIN_TABLE_REFERENCE);
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
  @NotNull
  public TableReference getMainTableReference() {
    ensureFullyInitialized();
    return mainTableReference;
  }

  @Valid
  @Override
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
