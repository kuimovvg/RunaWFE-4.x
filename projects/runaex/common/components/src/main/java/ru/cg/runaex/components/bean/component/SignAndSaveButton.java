package ru.cg.runaex.components.bean.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.TableReference;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author kochetkov
 */
public class SignAndSaveButton extends Component implements ComponentWithCustomValidation {
  private static final long serialVersionUID = 2272871521395891094L;

  private static final int NAME = 0;
  private static final int TABLE = 1;
  private static final int DATA_FIELD = 2;
  private static final int SIGN_FIELD = 3;
  private static final int VISIBILITY_RULE = 4;
  private static final int WIDTH = 5;

  private TableReference table;
  private String dataField;
  private String signField;
  private Integer width;

  @Override
  public int getParametersNumber() {
    return 6;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    table = parseTableReferenceInitTerm(getParameter(TABLE));
    ColumnReference dataFieldReference = parseColumnReference(getParameter(DATA_FIELD));
    dataField = dataFieldReference != null ? dataFieldReference.getColumn() : null;
    ColumnReference signFieldReference = parseColumnReference(getParameter(SIGN_FIELD));
    signField = signFieldReference != null ? signFieldReference.getColumn() : null;

    if (getParameter(WIDTH) != null)
      try {
        width = Integer.valueOf(getParameter(WIDTH));
      }
      catch (NumberFormatException ex) {

      }
  }

  public String getName() {
    return getParameter(NAME);
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
  public String getDataField() {
    ensureFullyInitialized();
    return dataField;
  }

  @NotNull
  @DatabaseStructureElement
  public String getSignField() {
    ensureFullyInitialized();
    return signField;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @Override
  public List<String> customValidate() {
    List<String> constraintCodes = Collections.emptyList();
    boolean fieldsNoNull = getDataField() != null && getSignField() != null;
    if (fieldsNoNull && getDataField().equals(getSignField())) {
      constraintCodes = new ArrayList<String>(1);
      constraintCodes.add("SignAndSaveButton.columnNamesMatch");
    }
    if (getParameter(WIDTH) != null)
      try {
        Integer.valueOf(getParameter(WIDTH));
      }
      catch (NullPointerException ex) {
        constraintCodes.add("SignAndSaveButton.widthInvalidSyntax");
      }
    return constraintCodes;
  }

  public Integer getWidth() {
    ensureFullyInitialized();
    return width;
  }
}
