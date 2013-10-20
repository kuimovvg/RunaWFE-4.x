package ru.cg.runaex.components.bean.component.grid;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.Component;
import ru.cg.runaex.components.bean.component.part.GridColumn;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author golovlyev
 */
public abstract class BaseTree extends Component {
  private static final long serialVersionUID = -1655400553567256336L;

  public static final int TABLE = 0;
  public static final int COLUMNS = 1;
  public static final int VISIBILITY_RULE = 3;

  protected TableReference table;
  private List<GridColumn> columns;

  @Override
  protected void initLazyFields() {
    super.initLazyFields();
    table = parseTableReferenceInitTerm(getParameter(TABLE));
    columns = parseGridColumns(getColumnsStr());
  }

  @Override
  public int getParametersNumber() {
    return 4;
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

  @Valid
  public List<GridColumn> getColumns() {
    ensureFullyInitialized();
    return columns;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }
}
