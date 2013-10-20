package ru.cg.runaex.components.bean.component.field;

import ru.cg.runaex.components.GpdRunaConfigComponent;
import ru.cg.runaex.components.bean.component.Component;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author urmancheev
 */
public class HiddenInput extends Component implements ComponentWithCustomValidation {
  private static final long serialVersionUID = 6262062461203000409L;

  private static final int FIELD = 0;
  private static final int COLUMN_REFERENCE = 1;
  private static final int TYPE = 2;
  private static final int VALUE = 3;

  private ColumnReference field;
  private ColumnReference columnReference;

  @Override
  public int getParametersNumber() {
    return 4;
  }

  @Override
  protected void initLazyFields() {
    field = parseColumnReferenceInitTerm(getParameter(FIELD));
    columnReference = parseColumnReference(getParameter(COLUMN_REFERENCE));
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return 0;
  }

  @NotNullSchema
  @DatabaseStructureElement
  public String getSchema() {
    ensureFullyInitialized();
    return field.getSchema();
  }

  @NotNull
  @DatabaseStructureElement
  public String getTable() {
    ensureFullyInitialized();
    return field.getTable();
  }

  @NotNull
  @DatabaseStructureElement
  public String getField() {
    ensureFullyInitialized();
    return field.getColumn();
  }

  @Valid
  public ColumnReference getColumnReference() {
    ensureFullyInitialized();
    return columnReference;
  }

  public Type getType() {
    return convertHiddenInputType(getParameter(TYPE));
  }

  private Type convertHiddenInputType(String valueTypeStr) {
    if (GpdRunaConfigComponent.HIDDEN_CURRENT_DATE.equals(valueTypeStr))
      return Type.CURRENT_TIME;
    else if (GpdRunaConfigComponent.HIDDEN_CURRENT_USER.equals(valueTypeStr))
      return Type.CURRENT_USER;
    else if (GpdRunaConfigComponent.HIDDEN_MANUAL.equals(valueTypeStr))
      return Type.MANUAL;
    return null;
  }

  public boolean isCurrentTimeAsDefaultValue() {
    return Type.CURRENT_TIME == getType();
  }

  public boolean isCurrentUserAsDefaultValue() {
    return Type.CURRENT_USER == getType();
  }

  public String getValue() {
    return getParameter(VALUE);
  }

  @Override
  public List<String> customValidate() {
    List<String> constraintCodes = new ArrayList<String>(2);
    if (isCurrentTimeAsDefaultValue()) {
      if (isCurrentTimeSelectedAndLinkSpecified()) {
        constraintCodes.add("HiddenInput.currentTimeSelectedAndLinkSpecified");
      }
      if (isCurrentTimeSelectedAndValueSpecified()) {
        constraintCodes.add("HiddenInput.currentTimeSelectedAndValueSpecified");
      }
    }
    else if (isCurrentUserAsDefaultValue()) {
      if (isCurrentUserSelectedAndLinkSpecified()) {
        constraintCodes.add("HiddenInput.currentUserSelectedAndLinkSpecified");
      }
      if (isCurrentUserSelectedAndValueSpecified()) {
        constraintCodes.add("HiddenInput.currentUserSelectedAndValueSpecified");
      }
    }
    return constraintCodes;
  }

  private boolean isCurrentTimeSelectedAndLinkSpecified() {
    return isCurrentTimeAsDefaultValue() && getColumnReference() != null;

  }

  private boolean isCurrentTimeSelectedAndValueSpecified() {
    return isCurrentTimeAsDefaultValue() && getValue() != null;
  }

  private boolean isCurrentUserSelectedAndLinkSpecified() {
    return isCurrentUserAsDefaultValue() && getColumnReference() != null;

  }

  private boolean isCurrentUserSelectedAndValueSpecified() {
    return isCurrentUserAsDefaultValue() && getValue() != null;
  }

  private enum Type {
    /**
     * в ручную
     */
    MANUAL,

    /**
     * текущее время
     */
    CURRENT_TIME,
    /**
     * текущий пользователь
     */
    CURRENT_USER
  }
}
