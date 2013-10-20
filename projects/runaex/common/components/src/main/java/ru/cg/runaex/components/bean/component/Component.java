package ru.cg.runaex.components.bean.component;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;

import ru.cg.runaex.components.GenerateFieldType;
import ru.cg.runaex.components.bean.component.part.*;
import ru.cg.runaex.components.parser.ComponentParser;

/**
 * @author urmancheev
 */
public abstract class Component implements IsComponent {
  private static final long serialVersionUID = -9216502183170296546L;

  protected String componentName;
  protected ComponentType componentType;
  private String[] parameters;
  private String defaultSchema;

  protected VisibilityRuleComponentPart visibilityRule;

  protected boolean fullyInitialized = false;

  public void init(String componentName, ComponentType componentType, String[] parameters) {
    this.componentName = componentName;
    this.componentType = componentType;

    if (parameters.length < getParametersNumber())
      this.parameters = Arrays.copyOf(parameters, getParametersNumber());
    else
      this.parameters = parameters;

    fullyInitialized = false;
  }

  protected void ensureFullyInitialized() {
    if (!fullyInitialized)
      fullyInitialize();
  }

  private void fullyInitialize() {
    initLazyFields();
    fullyInitialized = true;
  }

  protected void initLazyFields() {
    visibilityRule = parseVisibilityRule(getParameter(getVisibilityRuleParameterIndex()));
  }

  @Override
  public String getComponentName() {
    return componentName;
  }

  @Override
  public ComponentType getComponentType() {
    return componentType;
  }

  public GenerateFieldType getGenerateFieldType() {
    return null;
  }

  @Override
  public String getDefaultSchema() {
    return defaultSchema;
  }

  public void setDefaultSchema(String defaultSchema) {
    this.defaultSchema = defaultSchema;
  }

  protected String getSchema(int schemaParameterNumber) {
    String schemaParameter = getParameter(schemaParameterNumber);
    return schemaParameter != null ? schemaParameter : defaultSchema;
  }

  protected abstract int getVisibilityRuleParameterIndex();

  public VisibilityRuleComponentPart getVisibilityRule() {
    ensureFullyInitialized();
    return visibilityRule;
  }

  protected String getParameter(int parameterNumber) {
    return parameters[parameterNumber];
  }

  public abstract int getParametersNumber();

  public int getPresentParametersCount() {
    return parameters.length;
  }

  protected Integer convertLength(String value) {
    return value != null ? Integer.valueOf(value) : 0;
  }

  protected DefaultValueType convertDefaultValueType(String value) {
    if ("fromDb".equals(value))
      return DefaultValueType.FROM_DB;
    if ("manual".equals(value))
      return DefaultValueType.MANUAL;
    if ("executeGroovy".equals(value))
      return DefaultValueType.EXECUTE_GROOVY;
    return null;
  }

  protected boolean convertEnableSpeechInput(String value) {
    return value != null && Boolean.valueOf(value);
  }

  protected boolean convertVisiblePagination(String value) {
    return value != null && Boolean.valueOf(value);
  }

  protected TableReference parseTableReference(String tableReferenceStr) {
    return ComponentParser.parseTableReference(tableReferenceStr, defaultSchema);
  }

  protected TableReference parseTableReferenceInitTerm(String tableReferenceStr) {
    return ComponentParser.parseTableReferenceInitTerm(tableReferenceStr, defaultSchema);
  }

  protected ColumnReference parseColumnReference(String columnReferenceStr) {
    return ComponentParser.parseColumnReference(columnReferenceStr, defaultSchema);
  }


  protected ColumnReference parseColumnReferenceInitTerm(String columnReferenceStr) {
    return ComponentParser.parseColumnReferenceInitTerm(columnReferenceStr, defaultSchema);
  }

  protected List<GridColumn> parseGridColumns(String gridColumnsStr) {
    return ComponentParser.parseGridColumns(gridColumnsStr, defaultSchema);
  }

  protected List<EditableTreeGridColumn> parseEditableTreeGridColumns(String gridColumnsStr) {
    return ComponentParser.parseEditableTreeGridColumns(gridColumnsStr, defaultSchema);
  }

  protected DefaultValue parseDefaultValue(String valueStr) {
    DefaultValue defaultValue = ComponentParser.parseDefaultValue(valueStr);
    return defaultValue != null && defaultValue.getType() != DefaultValueType.NONE ? defaultValue : null;
  }

  protected RequireRuleComponentPart parseRequireRule(String value) {
    return ComponentParser.parseRequireRule(value);
  }

  protected GroovyRuleComponentPart parseGroovyRule(String value) {
    return ComponentParser.parseGroovyRule(value);
  }

  protected String getGroovyScriptParameter(int index) {
    return ComponentParser.parseBase64(getParameter(index));
  }

  protected VisibilityRuleComponentPart parseVisibilityRule(String value) {
    return ComponentParser.parseVisibilityRule(value);
  }

  protected EditabilityRuleComponentPart parseEditabilityRule(String value) {
    return ComponentParser.parseEditabilityRule(value);
  }
}
