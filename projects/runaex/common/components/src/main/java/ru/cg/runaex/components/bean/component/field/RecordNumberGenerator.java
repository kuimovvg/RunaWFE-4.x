package ru.cg.runaex.components.bean.component.field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.StringUtils;

import ru.cg.runaex.components.GpdRunaConfigComponent;
import ru.cg.runaex.components.bean.component.EditableFieldImpl;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.validation.ComponentWithCustomValidation;
import ru.cg.runaex.components.validation.annotation.DatabaseStructureElement;
import ru.cg.runaex.components.validation.annotation.NotNullSchema;

/**
 * @author korablev
 */
public class RecordNumberGenerator extends EditableFieldImpl implements ComponentWithCustomValidation {
  private static final long serialVersionUID = -3833423502166036509L;

  private static final int FIELD = 0;
  private static final int SEQUENCE_NAME = 1;
  private static final int PATTERN = 2;
  private static final int GENERATION_CONDITION = 3;
  private static final int VISIBILITY_RULE = 4;
  private static final int EDITABILITY_RULE = 5;

  private ColumnReference field;

  @Override
  public int getParametersNumber() {
    return 6;
  }

  @Override
  protected void initLazyFields() {
    super.initLazyFields();

    field = parseColumnReferenceInitTerm(getParameter(FIELD));
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

  @DatabaseStructureElement
  public String getSequenceName() {
    return getParameter(SEQUENCE_NAME);
  }

  @NotNull
  public String getPattern() {
    return getParameter(PATTERN);
  }

  @NotNull
  public GenerationCondition getGenerationCondition() {
    return convertGenerateCondition(getParameter(GENERATION_CONDITION));
  }

  private GenerationCondition convertGenerateCondition(String valueTypeStr) {
    if (GpdRunaConfigComponent.RECORD_NUMBER_GENERATOR_WHEN_VIEW.equals(valueTypeStr))
      return GenerationCondition.WHEN_VIEW;
    else if (GpdRunaConfigComponent.RECORD_NUMBER_GENERATOR_WHEN_SAVE.equals(valueTypeStr))
      return GenerationCondition.WHEN_SAVE;
    return null;
  }

  @Override
  protected int getVisibilityRuleParameterIndex() {
    return VISIBILITY_RULE;
  }

  @Override
  protected int getEditabilityRuleParameterIndex() {
    return EDITABILITY_RULE;
  }

  public Boolean showGenerationProcess() {
    return GenerationCondition.WHEN_VIEW == getGenerationCondition();
  }

  @Override
  public List<String> customValidate() {
    List<String> constraintCodes = Collections.emptyList();
    if (!getPatternVerified()) {
      constraintCodes = new ArrayList<String>(1);
      constraintCodes.add("RecordNumberGenerator.patternSpecificationError");
    }
    return constraintCodes;
  }

  public boolean getPatternVerified() {
    return getPattern() != null && StringUtils.countMatches(getPattern(), "%") == 1;
  }

  @DatabaseStructureElement
  public String getFormattedSequenceName() {
    if (getSequenceName() != null)
      return String.format("%s_seq", getSequenceName());
    return String.format("%s_%s_seq", getTable(), getField());
  }

  private enum GenerationCondition {
    /**
     * Генерировать при отображении
     */
    WHEN_VIEW,
    /**
     * Генерировать при сохранении
     */
    WHEN_SAVE
  }
}
