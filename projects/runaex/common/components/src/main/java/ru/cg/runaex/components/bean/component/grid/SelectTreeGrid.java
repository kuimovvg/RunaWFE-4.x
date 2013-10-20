package ru.cg.runaex.components.bean.component.grid;

import ru.cg.runaex.components.bean.component.ComponentWithSingleField;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.RequireRuleComponentPart;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;

/**
 * @author golovlyev
 */
public class SelectTreeGrid extends BaseTree implements ComponentWithSingleField, ComponentWithCustomValidation {
  private static final long serialVersionUID = 3539432560346791471L;

  public static final int FIELD = 2;
  public static final int REQUIRED = 4;

  private ColumnReference field;
  private RequireRuleComponentPart requireRule;

  @Override
  protected void initLazyFields() {
    super.initLazyFields();
    field = parseColumnReferenceInitTerm(getParameter(FIELD));
  }

  @Override
  public int getParametersNumber() {
    return 5;
  }

  @NotNull
  @DatabaseStructureElement
  public String getSaveTable() {
    ensureFullyInitialized();
    return field.getTable();
  }

  @NotNull
  @DatabaseStructureElement
  public String getSaveField() {
    ensureFullyInitialized();
    return field.getColumn();
  }

  @NotNull
  public RequireRuleComponentPart getRequireRule() {
    if (requireRule == null)
      requireRule = parseRequireRule(getParameter(REQUIRED));
    return requireRule;
  }

  @NotNull
  @DatabaseStructureElement
  public String getField() {
    ensureFullyInitialized();
    return field.getColumn();
  }

  @Override
  public List<String> customValidate() {
    return Collections.emptyList();
  }
}
