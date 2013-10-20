package ru.cg.runaex.components.bean.component.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.Component;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.EditableTreeGridColumn;
import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.validation.annotation.*;

/**
 * @author Kochetkov
 */
public class EditableTreeGrid extends Component implements GridComponent {
  private static final long serialVersionUID = 6420684257321373793L;

  public static final int TABLE = 0;
  public static final int COLUMNS = 1;
  public static final int TABLE_ID = 2;
  public static final int PARENT_COLUMN = 3;
  public static final int LINK_COLUMNS = 4;
  public static final int FILTER = 5;
  public static final int HEIGHT_WEIGHT = 6;
  public static final int SORT_COLUMN = 7;
  public static final int SORT_ORDER = 8;
  public static final int WIDTH_WEIGHT = 9;
  public static final int VISIBLE_PAGINATION = 10;
  public static final int VISIBILITY_RULE = 11;
  public static final int BUSINESS_RULE = 12;
  public static final int EDITABLE_RULE = 13;
  public static final int CSS_CLASS = 14;
  public static final int NODE_COUNT = 15;

  private TableReference table;
  private String sortColumn;
  private String parentColumn;

  boolean isPaginationVisible;
  private List<EditableTreeGridColumn> columns;
  private List<EditableTreeGridColumn> linkColumns;
  private List<String> businessRules;

  @Override
  public int getParametersNumber() {
    return 16;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    table = parseTableReferenceInitTerm(getParameter(TABLE));
    ColumnReference sortColumnReference = parseColumnReference(getParameter(SORT_COLUMN));
    ColumnReference parentColumnReference = parseColumnReference(getParameter(PARENT_COLUMN));
    sortColumn = sortColumnReference != null ? sortColumnReference.getColumn() : null;
    parentColumn = parentColumnReference != null ? parentColumnReference.getColumn() : null;

    isPaginationVisible = convertVisiblePagination(getVisiblePaginationStr());
    columns = parseEditableTreeGridColumns(getColumnsStr());
    linkColumns = parseEditableTreeGridColumns(getLinkColumnsStr());
    String businessRulesStr = getGroovyScriptParameter(BUSINESS_RULE);
    if (businessRulesStr != null && !businessRulesStr.isEmpty()) {
      businessRulesStr = businessRulesStr.replace("\r\n", "");
      businessRules = new ArrayList<String>(Arrays.asList(businessRulesStr.split(";")));
    }
    else
      businessRules = new ArrayList<String>();
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

  //  @NotNull
  public String getLinkColumnsStr() {
    return getParameter(LINK_COLUMNS);
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

  @DatabaseStructureElement
  public String getParentColumn() {
    ensureFullyInitialized();
    return parentColumn;
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
  public List<EditableTreeGridColumn> getColumns() {
    ensureFullyInitialized();
    return columns;
  }

  @Valid
  public List<EditableTreeGridColumn> getLinkColumns() {
    ensureFullyInitialized();
    return linkColumns;
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

  public List<String> getBusinessRules() {
    ensureFullyInitialized();
    return businessRules;
  }

  public String getEditableRule() {
    return getGroovyScriptParameter(EDITABLE_RULE);
  }

  public String getCssClass() {
    return getGroovyScriptParameter(CSS_CLASS);
  }

  @AssertNumber
  public String getNodeCount() {
    return getParameter(NODE_COUNT);
  }
}
