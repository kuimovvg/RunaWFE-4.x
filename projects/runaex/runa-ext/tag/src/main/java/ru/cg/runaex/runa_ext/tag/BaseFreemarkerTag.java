/*
 * Copyright (c) 2012.
 *
 * Class: BaseFreemarkerTag
 * Last modified: 06.09.12 10:33
 *
 * Author: Sabirov
 * Company Center
 */

package ru.cg.runaex.runa_ext.tag;

import com.google.gson.Gson;
import freemarker.template.TemplateModelException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.html.Label;
import org.springframework.context.support.ResourceBundleMessageSource;
import ru.cg.runaex.components.GpdRunaConfigComponent;
import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.Component;
import ru.cg.runaex.components.bean.component.ComponentType;
import ru.cg.runaex.components.bean.component.IsComponent;
import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.bean.component.part.GroovyRuleComponentPart;
import ru.cg.runaex.components.bean.session.ObjectInfo;
import ru.cg.runaex.components.util.ComponentUtil;
import ru.cg.runaex.core.DateFormat;
import ru.cg.runaex.database.bean.transport.ClassType;
import ru.cg.runaex.database.bean.transport.Data;
import ru.cg.runaex.database.bean.transport.TransportData;
import ru.cg.runaex.database.bean.transport.TransportDataSet;
import ru.cg.runaex.database.context.DatabaseSpringContext;
import ru.cg.runaex.database.util.GsonUtil;
import ru.cg.runaex.groovy.util.GroovyRuleUtils;
import ru.cg.runaex.runa_ext.tag.template.JsTemplateManager;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Date: 04.09.12
 * Time: 17:34
 *
 * @author Sabirov
 */
public abstract class BaseFreemarkerTag<C extends IsComponent> extends FreemarkerTag {
  private static final long serialVersionUID = 6326751929333562959L;
  protected static final Log logger = LogFactory.getLog(BaseFreemarkerTag.class);
  protected static final ResourceBundleMessageSource resourceBundleMessageSource = new ResourceBundleMessageSource();

  private static final String COMPONENT_JS_PATH = "resources/runaex/";
  private static final String COMPONENT_CSS_PATH = "resources/runaex/";
  private static final String JS_PATH = "resources/javascripts/";
  private static final String CSS_PATH = "resources/stylesheets/";
  protected static final String IMG_PATH = "resources/images/";

  protected static final String ActionButtonTag = "ActionButtonTag";
  protected static final String ExtractFormData = "ExtractFormData";
  protected static final String AutocompleteTag = "AutocompleteTag";
  protected static final String ComboBoxTag = "ComboBoxTag";
  protected static final String DateTimePickerTag = "DateTimePicker";
  protected static final String LabelTag = "LabelTag";
  protected static final String NavigateButtonTag = "NavigateButtonTag";
  protected static final String RadioButtonGroupTag = "RadioButtonGroupTag";
  protected static final String TextFieldTag = "TextFieldTag";
  protected static final String PrintButtonTag = "PrintButtonTag";
  protected static final String HelpButtonTag = "HelpButtonTag";
  protected static final String NavigationTreeMenuTag = "NavigationTreeMenuTag";

  protected static final String FlexiGridTag = "FlexigridTag";
  protected static final String EditableTreeGridTag = "EditableTreeGridTag";
  protected static final String DependentFlexiGridTag = "DependentFlexiGridTag";
  protected static final String LinkFlexiGridTag = "LinkFlexigridTag";
  protected static final String TreeGridTag = "TreegridTag";
  protected static final String SelectTreeGridTag = "SelectTreegridTag";

  protected static final String FlexiGridCommon = "FlexiGridCommon";
  protected static final String NavigationCommon = "NavigationCommon";

  protected static final String FiasAddressTag = "FiasAddressTag";
  protected static final String SphinxSearchTag = "SphinxSearchTag";
  protected static final String FileUploadTag = "FileUploadTag";
  protected static final String SignAndSaveButtonTag = "SignAndSaveButtonTag";
  protected static final String SignVerifyTag = "SignVerifyTag";
  protected static final String TimerTag = "TimerTag";
  protected static final String NumberFieldTag = "NumberFieldTag";
  protected static final String ExecuteGroovy = "ExecuteGroovy";

  protected static final String SpeechRecognition = "SpeechRecognition.js";
  protected static final String JqueryUiAutoCompleteCss = "jquery-ui-1.8.21.autocomplete.css";
  protected static final Gson GSON = GsonUtil.getGsonObject();

  private ObjectInfo objectInfo;
  protected Map<String, Object> variables = new HashMap<String, Object>();
  private Map<String, String> jsTemplateAttrs = new HashMap<String, String>();
  private String jsTemplateName;
  protected String projectName;

  static {
    resourceBundleMessageSource.setBasename("ru/cg/runaex/runa_ext/tag/messages/messages");
    resourceBundleMessageSource.setUseCodeAsDefaultMessage(true);
  }

  protected BaseFreemarkerTag() {
    super();
    logger.debug("create " + new Date());
  }

  @Override
  protected void finalize() throws Throwable {
    logger.debug("close " + new Date());
    super.finalize();
  }

  protected abstract ComponentType getComponentType();

  @Override
  protected Object executeTag() throws TemplateModelException {
    logger.debug("this.subject - " + this.user);
    logger.debug("this.variables - " + this.variables);

    try {
      projectName = (String) variableProvider.getValue(WfeRunaVariables.PROJECT_NAME_VARIABLE);

      C component = getComponent();

      initAdditionalParameters(component);

      String html = executeToHtml(component);

      GroovyRuleComponentPart visibilityRule = component.getVisibilityRule();
      if (visibilityRule != null) {
        boolean componentIsVisible = GroovyRuleUtils.executeGroovyRule(visibilityRule.getGroovyScript(), getVariables(), projectName, getProcessDefinitionId(), getProcessInstanceId());

        if (!componentIsVisible) {
          StringBuilder sb = new StringBuilder();
          sb.append("<div class=\"displayNone\" style='display:none'>");
          sb.append(html);
          sb.append("</div>");
          html = sb.toString();
        }
      }

      if (!jsTemplateAttrs.isEmpty() && jsTemplateName != null) {
        html = new StringBuilder(html).append(JsTemplateManager.process(jsTemplateName, jsTemplateAttrs)).toString();
      }

      return html;
    } catch (RuntimeException ex) {
      logger.error(ex.toString(), ex);
      throw new TemplateModelException(ex.toString(), ex);
    }
  }

  protected abstract String executeToHtml(C component) throws TemplateModelException,
      AuthorizationException, AuthenticationException, TaskDoesNotExistException;

  @SuppressWarnings("unchecked")
  protected C getComponent() throws TemplateModelException, AuthorizationException, TaskDoesNotExistException, AuthenticationException {
    Component component = ComponentUtil.createComponent(getComponentType());

    String[] parameters = new String[component.getParametersNumber()];
    for (int i = 0; i < component.getParametersNumber(); i++) {
      parameters[i] = getComponentParameter(i);
    }
    component.init(null, getComponentType(), parameters);

    return (C) component;
  }

  protected String getComponentParameter(int parameterNumber) throws TemplateModelException {
    String parameter = getParameterAs(String.class, parameterNumber);
    if (parameter != null)
      parameter = StringUtils.trimToNull(parameter);
    return parameter;
  }

  protected Long getProcessInstanceId() {
    return (Long) variableProvider.getValue(WfeRunaVariables.PROCESS_INSTANCE_ID);
  }


  protected Long getProcessDefinitionId() {
    return (Long) variableProvider.getValue(WfeRunaVariables.PROCESS_DEFINITION_ID);
  }

  protected Long getSelectedRowId() {
    Long selectedRowId = objectInfo != null ? objectInfo.getId() : null;
    logger.debug("selectedRowId - " + selectedRowId);
    return selectedRowId;
  }

  @SuppressWarnings("unchecked")
  protected void initObjectInfo(String schema, String table) throws TemplateModelException {
    String navigationAction = (String) variableProvider.getValueNotNull(WfeRunaVariables.NAVIGATOR_ACTION);
    logger.debug("navigationAction - " + navigationAction);

    Map<Long, ObjectInfo> objectInfoMap = (Map<Long, ObjectInfo>) variableProvider.getValueNotNull(WfeRunaVariables.OBJECT_INFO);
    if (objectInfoMap.isEmpty())
      return;

    if (GpdRunaConfigComponent.NAVIGATE_ACTION_ADD.equals(navigationAction)) {
      TransportData createHiddenInput = (TransportData) variableProvider.getValue(WfeRunaVariables.CREATE_HIDDEN_INPUT);
      Map<Long, String> changeMap = (Map<Long, String>) variableProvider.getValue(WfeRunaVariables.CHANGE_OBJECT_INFO);

      for (Map.Entry<Long, ObjectInfo> entry : objectInfoMap.entrySet()) {
        Long contextVariableId = entry.getKey();
        ObjectInfo tmpObjectInfo = entry.getValue();

        if (schema.equals(tmpObjectInfo.getSchema()) && table.equals(tmpObjectInfo.getTable())) {
          /**
           * add item (tree || table)
           */
          /**
           * if id is null then parent id because after reload page don't rewrite parent id( is actual now)
           */
          if (tmpObjectInfo.getId() != null) {
            tmpObjectInfo.setParentId(tmpObjectInfo.getId());
          }
          tmpObjectInfo.setId(null);

          changeMap.put(contextVariableId, GSON.toJson(tmpObjectInfo));
          objectInfoMap.remove(contextVariableId);

          Data data = new Data(tmpObjectInfo.getTable() + "_parent_id", tmpObjectInfo.getParentId(), null);
          data.setSchema(tmpObjectInfo.getSchema());
          data.setTable(tmpObjectInfo.getTable());
          createHiddenInput.add(data);
          break;
        }
      }
      logger.debug("changeMap - " + changeMap);
      logger.debug("createHiddenInput - " + createHiddenInput);
    }
    else {
      for (ObjectInfo tmpObjectInfo : objectInfoMap.values()) {
        if (schema.equals(tmpObjectInfo.getSchema()) && table.equals(tmpObjectInfo.getTable())) {
          this.objectInfo = tmpObjectInfo;
          break;
        }
      }
      logger.debug("objectInfo - " + objectInfo);
    }
  }

  protected void appendComponentCssReference(String componentTagName, StringBuilder htmlBuilder) {
    String css = componentTagName + ".css";
    Boolean alreadyAdded = (Boolean) variables.get(css);
    if (alreadyAdded == null || !alreadyAdded) {
      htmlBuilder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(COMPONENT_CSS_PATH).append(componentTagName).append(".css\"/>");
      variables.put(css, true);
    }
  }

  protected void appendComponentJsReference(String componentTagName, StringBuilder htmlBuilder) {
    String jsKey = componentTagName + ".js";
    Boolean alreadyAdded = (Boolean) variables.get(jsKey);
    if (alreadyAdded == null || !alreadyAdded) {
      htmlBuilder.append("<script type=\"text/javascript\" src=\"").append(COMPONENT_JS_PATH).append(componentTagName).append(".js\"></script>");
      variables.put(jsKey, true);
    }
  }

  protected void appendJsReference(String jsFilename, StringBuilder htmlBuilder) {
    Boolean added = (Boolean) variables.get(jsFilename);
    if (added == null || !added) {
      htmlBuilder.append("<script type=\"text/javascript\" src=\"").append(JS_PATH).append(jsFilename).append("\"></script>");
      variables.put(jsFilename, true);
    }
  }

  protected void appendCssReference(String cssFileName, StringBuilder htmlBuilder) {
    Boolean alreadyAdded = (Boolean) variables.get(cssFileName);
    if (alreadyAdded == null || !alreadyAdded) {
      htmlBuilder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(CSS_PATH).append(cssFileName).append("\"/>");
      variables.put(cssFileName, true);
    }
  }

  protected String getHtmlWithValidation(WrapComponent wrapComponent) {
    List<WrapComponent> wrapComponents = new ArrayList<WrapComponent>(1);
    wrapComponents.add(wrapComponent);
    return getHtmlWithValidation(wrapComponents);
  }

  protected String getHtmlWithValidation(List<WrapComponent> list) {
    if (list == null || list.isEmpty()) {
      return "";
    }
    return getHtmlWithValidationDiv(list).toString();
  }

  protected StringBuilder getHtmlWithValidationDiv(List<WrapComponent> list) {
    StringBuilder sb = new StringBuilder();
    for (WrapComponent wrapComponent : list) {
      sb.append("<div class=\"control-group\"><div class=\"controls inline\">").
          append(wrapComponent.getElem1().toString());
      if (wrapComponent.getElem2() != null)
        sb.append(wrapComponent.getElem2().toString());
      if (wrapComponent.getLabel() != null)
        sb.append(wrapComponent.getLabel().toString());
      sb.append("</div>");
      sb.append("</div>");
    }
    return sb;
  }

  protected class WrapComponent {
    final Label label;
    final ConcreteElement elem1;
    final ConcreteElement elem2;

    public WrapComponent(Label label, ConcreteElement elem1) {
      this(label, elem1, null);
    }

    public WrapComponent(Label label, ConcreteElement elem1, ConcreteElement elem2) {
      this.label = label;
      this.elem1 = elem1;
      this.elem2 = elem2;
    }

    public Label getLabel() {
      return label;
    }

    public ConcreteElement getElem1() {
      return elem1;
    }

    public ConcreteElement getElem2() {
      return elem2;
    }
  }

  /**
   * Get variable from db
   *
   * @param processInstanceId
   * @param filterKey         - variable name
   */
  protected TransportData getVariableFromDb(Long processInstanceId, String filterKey) {
    String value = DatabaseSpringContext.getComponentDbServices().getBaseDaoService().getVariableFromDb(processInstanceId, filterKey);
    return GsonUtil.getObjectFromJson(value, TransportData.class);
  }

  /**
   * Get data
   * <p/>
   * if object1 and object2 is not empty then need to get data from link table from to column-reference table(object1 and object2)
   *
   * @param schema    - schema name
   * @param table     - table name
   * @param fields    - fields
   * @param sortName  - sort field name
   * @param sortOrder - sort order
   * @return get data
   */
  protected TransportDataSet getDataFromDB(String schema, String table, List<String> fields,
                                           String sortName, String sortOrder) throws RuntimeException {
    return DatabaseSpringContext.getComponentDbServices().getBaseDaoService().getData(getProcessDefinitionId(), schema, table, (String[]) fields.toArray(),
        null, null, sortName, sortOrder, null, null, null, null, null, null);
  }

  /**
   * Get data
   *
   * @param rowId      - selected object id
   * @param schema     - selected object schema
   * @param table      - selected object table
   * @param field      - selected object field
   * @param references - selected object references
   * @return data
   * @throws RuntimeException
   */
  protected <T> T getValue(Long rowId, String schema, String table, String field, String references, Class<T> clazz, T defaults) throws RuntimeException {
    TransportData transportData = getDataByIdFromDB(rowId, schema, table, field, references);
    return getValue(clazz, transportData, field, defaults);
  }

  protected TransportData getValue(Long rowId, String schema, String table, Collection<String> fields) throws RuntimeException {
    return getDataByIdFromDB(rowId, schema, table, fields);
  }

  /**
   * Get data
   *
   * @param rowId      - selected object id
   * @param schema     - selected object schema
   * @param table      - selected object table
   * @param field      - selected object field
   * @param references - selected object references
   * @return data
   * @throws RuntimeException
   */
  private TransportData getDataByIdFromDB(Long rowId, String schema, String table, String field, String references) throws RuntimeException {
    return DatabaseSpringContext.getComponentDbServices().getBaseDaoService().getDataById(getProcessDefinitionId(), rowId, schema, table, field, references);
  }

  private TransportData getDataByIdFromDB(Long rowId, String schema, String table, Collection<String> fields) throws RuntimeException {
    return DatabaseSpringContext.getComponentDbServices().getBaseDaoService().getDataById(getProcessDefinitionId(), schema, table, fields, rowId);
  }

  protected <T> T getValue(Class<T> clazz, TransportData transportData, String field, T defaults) throws RuntimeException {
    T tValue = defaults;
    logger.debug("transportData - " + transportData);
    if (transportData != null) {
      Data data = transportData.getData(field);
      if (data == null) {
        return defaults;
      }
      Object value = data.getValue();
      if (value instanceof Date) {
        if (clazz == Date.class) {
          tValue = (T) value;
        }
        else {
          SimpleDateFormat dateFormat = ClassType.DATE == data.getClassType() ? DateFormat.getDateFormat() : DateFormat.getDateTimeFormat();
          String selected = dateFormat.format((Date) value);
          tValue = clazz.cast(selected);
        }
      }
      else if (value instanceof String) {
        String strValue = ((String) value).trim();
        if (!strValue.isEmpty()) {
          if (clazz == Boolean.class) {
            Boolean selected = Boolean.valueOf(strValue);
            tValue = clazz.cast(selected);
          }
          else if (clazz == Long.class) {
            try {
              Long selected = Long.valueOf(strValue);
              tValue = clazz.cast(selected);
            }
            catch (NumberFormatException ex) {
              logger.error(ex.toString(), ex);
              throw new RuntimeException(ex.getMessage());
            }
          }
          else if (clazz == Date.class) {
            try {
              SimpleDateFormat dateFormat = ClassType.DATE == data.getClassType() ? DateFormat.getDateFormat() : DateFormat.getDateTimeFormat();
              Date selected = dateFormat.parse(strValue);
              tValue = clazz.cast(selected);
            }
            catch (ParseException ex) {
              logger.error(ex.toString(), ex);
              throw new RuntimeException(ex.getMessage());
            }
          }
          else if (clazz == Serializable.class || clazz == String.class) {
            tValue = clazz.cast(strValue);
          }
        }
      }
      else if (value instanceof BigDecimal) {
        if (clazz == BigDecimal.class) {
          tValue = clazz.cast(value);
        }
        else if (clazz == String.class) {
          String strValue = String.valueOf(value);
          tValue = clazz.cast(strValue);
        }
      }
      else if (value instanceof Boolean) {
        if (clazz == Boolean.class) {
          tValue = clazz.cast(value);
        }
        else if (clazz == String.class) {
          String strValue = String.valueOf(value);
          tValue = clazz.cast(strValue);
        }
      }
      else if (clazz.isInstance(value)) {
        tValue = clazz.cast(data.getValue());
      }
      logger.debug("tValue - " + tValue);
    }
    return tValue;
  }

  public Long getIdByFieldValue(ColumnReference columnReference, String value) {
    String schema = columnReference.getSchema();
    return getIdByFieldValue(schema, columnReference.getTable(), columnReference.getColumn(), value);
  }

  protected Long getIdByFieldValue(String schema, String table, String field, String value) {
    TransportData transportData = getIdByFieldValueFromDB(schema, table, field, value);
    return getValue(Long.class, transportData, table.concat("_id"), null);  //todo use id postfix from constants
  }

  private TransportData getIdByFieldValueFromDB(String schema, String table, String field, String value) {
    return DatabaseSpringContext.getComponentDbServices().getBaseDaoService().getIdByFieldValue(getProcessDefinitionId(), schema, table, field, value);
  }

  /**
   * Add variables to DB
   *
   * @param variableName - Variable name
   * @param jsonData     - Json data
   */
  protected void addVariablesFromDb(Long processInstanceId, String variableName, String jsonData) {
    DatabaseSpringContext.getComponentDbServices().getBaseDaoService().addVariableToDb(processInstanceId, variableName, jsonData);
  }

  protected void addObjectToJs(String key, Boolean val) {
    if (val != null)
      jsTemplateAttrs.put(key, toJsObject(val));
  }

  protected void addObjectToJs(String key, String val) {
    addObjectToJs(key, val, true);
  }

  protected void addObjectToJs(String key, String val, boolean convertToJs) {
    if (val != null) {
      String value = convertToJs ? toJsObject(val) : val;
      jsTemplateAttrs.put(key, value);
    }
  }

  protected void addObjectToJs(String key, Number val) {
    if (val != null)
      jsTemplateAttrs.put(key, toJsObject(val));
  }

  protected void setJsTemplateName(String jsTemplateName) {
    this.jsTemplateName = jsTemplateName;
  }

  /**
   * Пример использования
   * <p/>
   * List<Map<String, Object>> params = new ArrayList<Map<String, Object>>();
   * <p/>
   * Map<String, Object> param = new HashMap<String, Object>();
   * param.put("name", "columns");
   * param.put("value", "aaa,bbb,ccc");
   * params.add(param);
   * <p/>
   * param = new HashMap<String, Object>();
   * param.put("name", "tableId");
   * param.put("value", '111');
   * params.add(param);
   * <p/>
   * param = new HashMap<String, Object>();
   * param.put("name", "schema");
   * param.put("value", "public");
   * params.add(param);
   * <p/>
   * param = new HashMap<String, Object>();
   * param.put("name", "table");
   * param.put("value", grid.getTable());
   * params.add(param);
   * <p/>
   * в js по ключу можно будет подставлятся [{name:'columns',value:'aaa,bbb,ccc'},{name:'tableId',value:111},{name:'schema',value:'public'}]
   *
   * @param key
   * @param val
   */
  protected void addObjectToJs(String key, List<Map<String, Object>> val) {
    if (val == null || val.isEmpty()) {
      jsTemplateAttrs.put(key, "[]");
      return;
    }
    StringBuilder stringBuilder = new StringBuilder("[");
    for (Map<String, Object> m : val) {
      stringBuilder.append("{");
      for (String mapKey : m.keySet()) {
        stringBuilder.append("'").append(mapKey).append("'").append(": ").append(toJsObject(m.get(mapKey))).append(",");
      }

      stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "},");
    }
    stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "]");
    jsTemplateAttrs.put(key, stringBuilder.toString());
  }

  protected void addObjectToJs(String key, Map<String, Object> val) {
    StringBuilder stringBuilder = new StringBuilder("{");
    for (String mapKey : val.keySet()) {
      stringBuilder.append("'").append(mapKey).append("'").append(": ").append(toJsObject(val.get(mapKey))).append(",");
    }
    if (stringBuilder.length() > 1)
      stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "}");
    else
      stringBuilder.append("}");
    jsTemplateAttrs.put(key, stringBuilder.toString());
  }

  protected void addObjectToJsWithoutFormatting(String key, String value) {
    jsTemplateAttrs.put(key, value);
  }

  protected String toJsObject(Object o) {
    if (o instanceof Number) {
      return o.toString();
    }
    else if (o instanceof String) {
      if (((String) o).startsWith(WfeRunaVariables.FUNCTION_PREFIX)) {
        return o.toString().replace(WfeRunaVariables.FUNCTION_PREFIX, "");
      }
      if (((String) o).startsWith("{") && ((String) o).endsWith("}"))
        return o.toString();

      return "'" + o + "'";
    }
    else if (o instanceof Boolean) {
      return o.toString();
    }
    if (o != null)
      return o.toString();
    else
      return "";
  }

  protected void initAdditionalParameters(C component) throws TemplateModelException {

  }

  @SuppressWarnings("unchecked")
  protected Map<String, Object> getVariables() throws TemplateModelException {
    return (Map<String, Object>) variableProvider.getValue(WfeRunaVariables.RUNA_VARIABLES);
  }

  protected boolean executeGroovyRule(String groovyRuleScript) throws TemplateModelException {
    return GroovyRuleUtils.executeGroovyRule(groovyRuleScript, getVariables(), projectName, getProcessDefinitionId(), getProcessInstanceId());
  }
}
