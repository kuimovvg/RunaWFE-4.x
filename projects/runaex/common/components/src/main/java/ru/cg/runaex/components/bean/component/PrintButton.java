package ru.cg.runaex.components.bean.component;

import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import ru.cg.runaex.components.bean.component.part.StoredProcedure;
import ru.cg.runaex.components.parser.StoredProcedureParser;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;

/**
 * @author urmancheev
 */
public class PrintButton extends Component implements ComponentWithCustomValidation {
  private static final long serialVersionUID = -96886423543433698L;

  private static final int NAME = 0;
  private static final int TABLE_ID = 1;
  private static final int TEMPLATE_FILE_NAME = 2;
  private static final int STORED_PROCEDURES = 3;
  private static final int VISIBILITY_RULE = 4;
  private static final int WIDTH = 5;

  private List<StoredProcedure> storedProcedures;
  private Integer width;

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    storedProcedures = StoredProcedureParser.parseStoredProcedures(getStoredProceduresStr(), getDefaultSchema());

    if (getParameter(WIDTH) != null)
      try {
        width = Integer.valueOf(getParameter(WIDTH));
      }
      catch (NumberFormatException ex) {
      }
  }

  @Pattern(regexp = "(?:[^,;()]+(?:(?:\\(\\))|(\\((?:[^,;()]+?\\s*,\\s*)*?[^,;()]+?\\)))\\s*;\\s*)*?(?:[^,;()]+(?:(?:\\(\\))|(\\((?:[^,;()]+?\\s*,\\s*)*?[^,;()]+?\\)))(?:\\s*;\\s*)?)", message = "{ru.cg.runaex.constraints.storedProceduresPattern}")
  private String getStoredProceduresStr() {
    return getParameter(STORED_PROCEDURES);
  }

  @Override
  public int getParametersNumber() {
    return 6;
  }

  @NotNull
  public String getName() {
    return getParameter(NAME);
  }

  public String getTableId() {
    return getParameter(TABLE_ID);
  }

  public String getTemplateFileName() {
    return getParameter(TEMPLATE_FILE_NAME);
  }

  @Valid
  public List<StoredProcedure> getStoredProcedures() {
    ensureFullyInitialized();
    return storedProcedures;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @Override
  public List<String> customValidate() {
    List<String> constraintCodes = new ArrayList<String>(1);

    boolean noTemplate = getTemplateFileName() == null;
    if (getStoredProcedures() != null && noTemplate) {
      constraintCodes = new ArrayList<String>(1);
      constraintCodes.add("PrintButton.noTemplateForStoredProcedure");
    }
    if (getParameter(WIDTH) != null)
      try {
        Integer.valueOf(getParameter(WIDTH));
      }
      catch (NumberFormatException ex) {
        constraintCodes.add("PrintButton.widthInvalidSyntax");
      }
    return constraintCodes;
  }

  public Integer getWidth() {
    ensureFullyInitialized();
    return width;
  }
}
