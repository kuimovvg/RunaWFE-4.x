package ru.cg.runaex.components.bean.component;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author Абдулин Ильдар
 */
public class Timer extends Component {
  private static final long serialVersionUID = 8827693799485760097L;

  private static final int DATA_COLUMN = 0;
  private static final int PATTERN = 1;
  private static final int VISIBILITY_RULE = 2;

  private ColumnReference dataColumn;

  @Override
  public int getParametersNumber() {
    return 3;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    dataColumn = parseColumnReferenceInitTerm(getParameter(DATA_COLUMN));
  }

  @NotNullSchema
  @DatabaseStructureElement
  public String getSchema() {
    ensureFullyInitialized();
    return dataColumn.getSchema();
  }

  @NotNull
  @DatabaseStructureElement
  public String getTable() {
    ensureFullyInitialized();
    return dataColumn.getTable();
  }

  @NotNull
  @DatabaseStructureElement
  public String getDataColumn() {
    ensureFullyInitialized();
    return dataColumn.getColumn();
  }

  /**
   * regexp - проверка на то что должен быть обязательно символ либо DD или HH или MM или SS
   *
   * @return
   */
  @Pattern(regexp = ".*((D{2})|(H{2})|(M{2})|(S{2})).*", message = "{ru.cg.runaex.constraints.Timer.pattern}")
  public String getPattern() {
    return getParameter(PATTERN);
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }
}
