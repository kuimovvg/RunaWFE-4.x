package ru.cg.runaex.components.bean.component;

import java.io.Serializable;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author Абдулин Ильдар
 */
public class SignVerify extends Component implements Serializable {
  private static final long serialVersionUID = 6495624978114828012L;

  private static final int TABLE = 0;
  private static final int DATA_FIELD = 1;
  private static final int SIGN_FIELD = 2;
  private static final int VISIBILITY_RULE = 3;

  private TableReference table;
  private ColumnReference dataColumn;
  private ColumnReference signColumn;

  @Override
  public int getParametersNumber() {
    return 4;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    table = parseTableReferenceInitTerm(getParameter(TABLE));
    dataColumn = parseColumnReference(getParameter(DATA_FIELD));
    signColumn = parseColumnReference(getParameter(SIGN_FIELD));
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
  @DatabaseStructureElement
  public String getDataColumn() {
    ensureFullyInitialized();
    return dataColumn.getColumn();
  }

  @NotNull
  @DatabaseStructureElement
  public String getSignColumn() {
    ensureFullyInitialized();
    return signColumn.getColumn();
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }
}
