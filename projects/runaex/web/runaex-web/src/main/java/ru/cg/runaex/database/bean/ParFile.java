package ru.cg.runaex.database.bean;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ru.cg.runaex.components.WfeRunaVariables;
import ru.cg.runaex.components.bean.component.*;
import ru.cg.runaex.components.bean.component.field.*;
import ru.cg.runaex.components.bean.component.filter.FilterField;
import ru.cg.runaex.components.bean.component.part.TextType;
import ru.cg.runaex.components.util.ComponentUtil;
import ru.cg.runaex.exceptions.BusinessProcessException;
import ru.cg.runaex.exceptions.ProjectParseException;
import ru.cg.runaex.generatedb.bean.Table;
import ru.cg.runaex.web.utils.SAXUtils;

/**
 * @author urmancheev
 */
public final class ParFile implements Serializable {
  private static final long serialVersionUID = 812391727123235625L;

  private static final Logger logger = LoggerFactory.getLogger(ParFile.class);

  private static final String PROCESS_DEFINITION_FILENAME = "processdefinition.xml";
  private static final String VARIABLES_FILENAME = "variables.xml";
  private static final Pattern FORM_TAG_PATTERN = Pattern.compile("<form id=\"runaex_form_validation_id\".*?>\\s*(?:<script>.*?</script>)?(.*)</form>\\s*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.UNICODE_CASE);

  private static final Pattern COMPONENT_PATTERN = Pattern.compile("\\$\\{((.+?)\\((.*?)\\))\\}", Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.MULTILINE);
  private static final Pattern PARAMETER_PATTERN = Pattern.compile("(?:(?:^\")|(?:(?<!\\\\)\"))(.*?)(?<!\\\\)\"");
  private static final Pattern USER_TASK_PATTERN = Pattern.compile("<userTask(.+?)>");
  private static final Pattern ID_AND_NAME_PATTERN = Pattern.compile("id=\\\"(.+?)\\\".*name=\\\"(.+?)\\\"");

  private String defaultSchema;
  private String processName;
  private List<String> taskNames;
  private List<FtlComponent> ftlComponents;

  private String processDefinitionContent = null;
  private String variablesContent = null;
  private Map<String, byte[]> otherZipEntries = new HashMap<String, byte[]>();
  private List<FtlFile> ftlFiles;
  private Map<String, String> tasksNames = new HashMap<String, String>();

  private ResourceBundleMessageSource messageSource;


  public ParFile(byte[] parFileBytes, ResourceBundleMessageSource messageSource) throws ProjectParseException, BusinessProcessException {
    this.messageSource = messageSource;
    this.ftlComponents = new ArrayList<FtlComponent>();
    ftlFiles = new LinkedList<FtlFile>();

    readEntries(parFileBytes);
    parseTasks();
  }

  public String getProcessName() {
    return processName;
  }

  public List<String> getTaskNames() {
    return taskNames;
  }

  public List<FtlComponent> getFtlComponents() {
    return ftlComponents;
  }

  @SuppressWarnings("unchecked")
  private void readEntries(byte[] parFileBytes) throws ProjectParseException, BusinessProcessException {
    ByteArrayOutputStream byteArrayOutputStream = null;
    BufferedOutputStream bufferedOutputStream = null;


    ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(parFileBytes));
    try {
      for (ZipEntry entry = zipInputStream.getNextEntry(); entry != null; entry = zipInputStream.getNextEntry()) {
        if (!entry.getName().endsWith(".ftl")) {
          if (PROCESS_DEFINITION_FILENAME.equals(entry.getName())) {
            this.processDefinitionContent = readCurrentZipEntryContent(zipInputStream);
            this.processName = parseProcessName(processDefinitionContent);
            tasksNames = getTasksNames(processDefinitionContent);
          }
          else if (VARIABLES_FILENAME.equals(entry.getName())) {
            this.variablesContent = readCurrentZipEntryContent(zipInputStream);
            this.defaultSchema = parseDefaultSchema(variablesContent);
          }
          else {
            try {
              byteArrayOutputStream = new ByteArrayOutputStream();
              bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);
              IOUtils.copy(zipInputStream, bufferedOutputStream);
              bufferedOutputStream.flush();
              otherZipEntries.put(entry.getName(), byteArrayOutputStream.toByteArray());
            }
            finally {
              IOUtils.closeQuietly(bufferedOutputStream);
              IOUtils.closeQuietly(byteArrayOutputStream);
            }
          }
        }
        else {
          ftlFiles.add(new FtlFile(entry.getName(), readCurrentZipEntryContent(zipInputStream)));
        }
      }
      for (FtlFile ftlFile : this.ftlFiles) {
        String ftlFileContent = ftlFile.getContents();
        String formFileName = ftlFile.getName().replace(".ftl", "");
        String formName = tasksNames.get(formFileName);

        List<FtlComponent> ftlComponents = parseFtlComponents(ftlFileContent, getProcessName(), formName);
        mergeComponents(ftlComponents);
      }
    }
    catch (IOException ex) {
      logger.error("Unreadable .par file", ex);
      throw new ProjectParseException("Unreadable .par file", ex);
    }
    catch (ArrayIndexOutOfBoundsException ex) {
      logger.error(ex.getMessage(), ex);
      throw new BusinessProcessException(ex.toString(), ex);
    }
    finally {
      IOUtils.closeQuietly(zipInputStream);
    }

//    if (otherZipEntries.get("META-INF/MANIFEST.MF") == null) {
//      throw new ProjectParseException("Can't find META-INF/MANIFEST.MF file");
//    }

  }

  private void parseTasks() throws ProjectParseException {
    XPathFactory xpFactory = XPathFactory.newInstance();
    XPath path = xpFactory.newXPath();
    try {
      Document doc = SAXUtils.getDocument(processDefinitionContent);

      NodeList taskNameNodes = (NodeList) path.evaluate("//task-node/@name", doc, XPathConstants.NODESET);
      taskNames = new ArrayList<String>(taskNameNodes.getLength());
      for (int i = 0; i < taskNameNodes.getLength(); i++) {
        Node el = taskNameNodes.item(i);
        taskNames.add(el.getNodeValue());
      }
    }
    catch (IOException ex) {
      throw new ProjectParseException("Unreadable process definition", ex);
    }
    catch (ParserConfigurationException ex) {
      throw new ProjectParseException("Could not parse process definition", ex);
    }
    catch (SAXException ex) {
      throw new ProjectParseException("Could not parse process definition", ex);
    }
    catch (XPathExpressionException ex) {
      throw new ProjectParseException("Could not parse process definition", ex);
    }
  }

  private String addValidationToFtl(FtlFile ftlFile) throws ProjectParseException {
    StringBuilder resultBuilder = new StringBuilder();
    String ftlFileContent = ftlFile.getContents();
    String formName = ftlFile.getName().replace(".ftl", "");
    String taskName = tasksNames.get(formName);
    Matcher matcher = FORM_TAG_PATTERN.matcher(ftlFileContent);

    /*
     already has validation tag
     */
    if (matcher.matches()) {
      ftlFileContent = matcher.group(1);
    }

    resultBuilder.append("<form id=\"runaex_form_validation_id\">");
    StringBuilder fieldsAndRules = new StringBuilder();
    boolean hasFieldsToValidate = false;
    for (FtlComponent ftlComponent : ftlComponents) {
      if (ftlComponent.getSourceForms().contains(taskName)) {
        String validateNameRulesStr = getValidateNameRulesStr(ftlComponent.getComponent());
        if (validateNameRulesStr != null) {
          fieldsAndRules.append(validateNameRulesStr).append(", \n");
          hasFieldsToValidate = true;
        }
      }
    }

    if (hasFieldsToValidate) {
      resultBuilder = addValidationScript(resultBuilder, fieldsAndRules);
    }

    resultBuilder.append(ftlFileContent);
    resultBuilder.append("</form>");
    return resultBuilder.toString();
  }

  /**
   * Get ftl components by text
   *
   * @param ftlFileContents - text
   * @return ftl components
   */
  private List<FtlComponent> parseFtlComponents(String ftlFileContents, String process, String formName) {
    Matcher matcher = COMPONENT_PATTERN.matcher(ftlFileContents);
    List<FtlComponent> ftlComponentList = new ArrayList<FtlComponent>();
    FtlComponent ftlComponent;
    ComponentType componentType;
    while (matcher.find()) {
      String sourceTag = matcher.group(1);
      String componentName = matcher.group(2);
      String parametersStr = matcher.group(3);

      Matcher parameterMatcher = PARAMETER_PATTERN.matcher(parametersStr);

      List<String> parametersList = new LinkedList<String>();
      String parameter;

      while (parameterMatcher.find()) {
        parameter = parameterMatcher.group(1);
        String trimmedStr = StringUtils.trimToNull(parameter);
        String parameterStr = StringEscapeUtils.unescapeXml(trimmedStr);
        parametersList.add(parameterStr);
      }
      String[] parameters = new String[parametersList.size()];
      parametersList.toArray(parameters);

      componentType = ComponentUtil.getComponentType(componentName);
      Component component = ComponentUtil.createComponent(componentType);
      component.init(componentName, componentType, parameters);
      component.setDefaultSchema(defaultSchema);

      ftlComponent = new FtlComponent(process, component, sourceTag);
      ftlComponent.addForm(formName);
      ftlComponentList.add(ftlComponent);
    }
    return ftlComponentList;
  }

  private String parseProcessName(String processDefinition) {
    //Get process-definition tag from xml
    Pattern pattern = Pattern.compile("<process-definition(.*?)>");
    Matcher matcher = pattern.matcher(processDefinition);

    //For Runa 4
    Pattern patternForFourthRuna = Pattern.compile("<process(.*?)>");
    Matcher matcherForFourthRuna = patternForFourthRuna.matcher(processDefinition);

    //Find matching str
    matcher.find();
    matcherForFourthRuna.find();

    String str;
    try {
      str = matcher.group();
    }
    catch (IllegalStateException ex) {
      str = matcherForFourthRuna.group();
    }

    //Get name attribute from process-definition tag
    pattern = Pattern.compile("name=\\\"(.*?)\\\"");
    matcher = pattern.matcher(str);
    matcher.find();
    //Cut last quote and attribute name
    return matcher.group().substring(0, matcher.group().length() - 1).replace("name=\"", "");
  }

  private Map<String, String> getTasksNames(String processDefinition) {
    Map<String, String> names = new HashMap<String, String>();
    Matcher nodeMatcher = USER_TASK_PATTERN.matcher(processDefinition);
    while (nodeMatcher.find()) {
      String findStr = nodeMatcher.group();
      Matcher matcher = ID_AND_NAME_PATTERN.matcher(findStr);
      matcher.find();
      names.put(matcher.group(1), matcher.group(2));
    }
    return names;
  }

  private String parseDefaultSchema(String variablesContent) {
    String defaultSchema = null;

    //Get variable tag with attribute name="schema" from xml
    Pattern pattern = Pattern.compile("(?i)(?u)(?d)(?m)(?s)<variable[^>].*?name=\\\"".concat(WfeRunaVariables.DEFAULT_SCHEMA_VARIABLE_NAME).concat("\\\".*?/>"));
    Matcher matcher = pattern.matcher(variablesContent);
    if (matcher.find()) {
      String str = matcher.group().replace("\n", "").replace("\r", "");
      //Get defaultValue attribute from variable tag
      pattern = Pattern.compile("defaultValue=\\\"(.*?)\\\"");
      matcher = pattern.matcher(str);
      if (matcher.find()) {
        //Cut last quote and attribute name
        defaultSchema = matcher.group().substring(0, matcher.group().length() - 1).replace("defaultValue=\"", "");
      }
    }

    return StringUtils.trimToNull(defaultSchema);
  }

  public byte[] getParFileWithValidation() throws ProjectParseException {
    ByteArrayOutputStream parFileWithValidation = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(parFileWithValidation, 1024 * 8));
    ZipEntry zipEntry;
    try {
      if (otherZipEntries.get("META-INF/MANIFEST.MF") != null) {
        zipEntry = new ZipEntry("META-INF/MANIFEST.MF");
        zos.putNextEntry(zipEntry);
        zos.write(otherZipEntries.get("META-INF/MANIFEST.MF"), 0, otherZipEntries.get("META-INF/MANIFEST.MF").length);
        zos.flush();
        zos.closeEntry();
      }

      for (String key : otherZipEntries.keySet()) {
        if (!"META-INF/MANIFEST.MF".equals(key)) {
          zipEntry = new ZipEntry(key);
          zos.putNextEntry(zipEntry);
          zos.write(otherZipEntries.get(key), 0, otherZipEntries.get(key).length);
          zos.flush();
          zos.closeEntry();
        }
      }

      byte[] bytes = processDefinitionContent.getBytes("UTF-8");
      zipEntry = new ZipEntry(PROCESS_DEFINITION_FILENAME);
      zos.putNextEntry(zipEntry);
      zos.write(bytes, 0, bytes.length);
      zos.flush();
      zos.closeEntry();

      bytes = variablesContent.getBytes("UTF-8");
      zipEntry = new ZipEntry(VARIABLES_FILENAME);
      zos.putNextEntry(zipEntry);
      zos.write(bytes, 0, bytes.length);
      zos.flush();
      zos.closeEntry();

      for (FtlFile ftlFile : this.ftlFiles) {
        String ftlFileWithValidation = addValidationToFtl(ftlFile);
        bytes = ftlFileWithValidation.getBytes("UTF-8");
        zipEntry = new ZipEntry(ftlFile.getName());
        zos.putNextEntry(zipEntry);
        zos.write(bytes, 0, bytes.length);
        zos.flush();
        zos.closeEntry();
      }
      zos.close();

      return parFileWithValidation.toByteArray();
    }
    catch (UnsupportedEncodingException ex) {
      logger.error(ex.getMessage(), ex);
      throw new ProjectParseException(ex.getMessage(), ex);
    }
    catch (IOException ex) {
      logger.error("Unreadable .par file", ex);
      throw new ProjectParseException("Unreadable .par file", ex);
    }
  }

  private StringBuilder addValidationScript(StringBuilder resultBuilder, StringBuilder fieldsAndRules) {
    int length = fieldsAndRules.length();
    fieldsAndRules.delete(length - 3, length);
    resultBuilder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"resources/stylesheets/validate.css\"/>\n");
    resultBuilder.append("<script type=\"text/javascript\" src=\"resources/javascripts/jquery.validate.min.js\"></script>\n");
    resultBuilder.append("<script>$(document).ready(function(){");
    StringBuilder regex = new StringBuilder();
    regex = regex.append(
        " $.validator.addMethod(\n").append(
        "      \"regex\",\n").append(
        "      function(value, element, regexp) {\n").append(
        "          var check = false;\n").append(
        "          var re = new RegExp(regexp);\n").append(
        "          var valid = re.test(value);\n ").append(
        "          if(!valid){\n").append(
        "            $(element).closest('.controls').addClass('error_regex');\n").append(
        "        }\n").append(
        "        else{\n").append(
        "          $(element).closest('.controls').removeClass('error_regex');\n").append(
        "        }\n").append(
        "          return valid;\n").append(
        "      },$.validator.format('").append(messageSource.getMessage("regexMessage", null, Locale.ROOT)).append("')\n").append(
        "); \n")
        .append("$.validator.addMethod(\n")
        .append("\"masked_number\",\n")
        .append("function(value, element){return isNumeric($(element).unmaskedValue());},$.validator.format('").append(messageSource.getMessage("validateNumberError", null, Locale.ROOT)).append("')); \n")
        .append("$.validator.addMethod(\n")
        .append("\"masked_max\",\n")
        .append("function(value,element,maxValue){$.validator.messages.masked_max='").append(messageSource.getMessage("validateMaxValueError", null, Locale.ROOT)).append(" '+ maxValue; return isNumeric($(element).unmaskedValue()) && parseFloat($(element).unmaskedValue())<=parseFloat(maxValue);});\n");
    StringBuilder validation = new StringBuilder();
    validation = validation.append(
        "$('#runaex_form_validation_id').validate ({\n ").append(
        "   ignore: [], \n").append(
        "   rules: { \n").append(
        fieldsAndRules.toString()).append(
        "  },\n").append(
        "  highlight: function(label) {  \n").append(
        "    $(label).closest('.control-group').addClass('error'); \n").append(
        "    $(label).closest('.control-group').removeClass('success'); \n").append(
        "  },\n").append(
        "  success: function(label) {   \n").append(
        "    if($(label).closest('.controls').hasClass('error_regex')) { \n").append(
        "       $(label).closest('.control-group').addClass('error'); \n").append(
        "       $(label).closest('.control-group').removeClass('success'); \n").append(
        "       return; \n").append(
        "    }  \n").append(
        "    $(label).closest('.control-group').removeClass('error'); \n").append(
        "    label.text('OK!').addClass('valid').closest('.control-group').addClass('success'); \n").append(
        "  }, \n").append(
        "  errorPlacement: function(label, element) { \n").append(
        "    if($(element).hasClass('check-error-above')) \n").append(
        "      $(element).parents('fieldset').append(label); \n").append(
        "    else if($(element).hasClass('check-error')) \n").append(
        "      $(element).parent().append(label); \n").append(
        "    else \n").append(
        "      label.insertAfter(element); \n").append(
        "  } \n").append(
        "}); \n");
    resultBuilder.append(regex);
    resultBuilder.append(validation);
    resultBuilder.append("});</script>");

    return resultBuilder;
  }

  private String getValidateNameRulesStr(IsComponent component) {
    String regex;

    int length;
    boolean email;
    String requiredRule = null;
    String fieldName = null;
    Long maxIntValue = null;
    boolean isNumber = false;

    switch (component.getComponentType()) {
      case FILTER_FIELD:
        FilterField filterField = (FilterField) component;
        email = filterField.getValueType() == TextType.EMAIL;
        regex = filterField.getRegex();
        length = filterField.getLength() > 0 ? filterField.getLength() : Table.DEFAULTS_FIELD_LENGTH;
        break;
      case AUTOCOMPLETE:
        requiredRule = ComponentUtil.createRequireRule(((Autocomplete) component).getRequireRule());
        email = false;
        length = 0;
        regex = null;
        fieldName = ((Autocomplete) component).getHiddenField();
        break;
      case FIAS_ADDRESS:
        requiredRule = ComponentUtil.createRequireRule(((FiasAddress) component).getRequireRule());
        email = false;
        length = 0;
        regex = null;
        fieldName = ((FiasAddress) component).getFullField();
        break;
      case FILTER_FIAS_ADDRESS:
      case FILTER_AUTOCOMPLETE:
      case FILTER_CHECKBOX:
      case FILTER_COMBO_BOX:
      case FILTER_DATE_TIME_PICKER:
      case FILTER_RADIO_BUTTON_GROUP:
        email = false;
        length = 0;
        regex = null;
        break;
      case CHECK_BOX:
        email = false;
        length = 0;
        regex = null;
        break;
      case COMBO_BOX:
        email = false;
        length = 0;
        regex = null;
        requiredRule = ComponentUtil.createRequireRule(((ComboBox) component).getRequireRule());
        break;
      case RADIO_BUTTON_GROUP:
        email = false;
        length = 0;
        regex = null;
        requiredRule = ComponentUtil.createRequireRule(((RadioButtonGroup) component).getRequireRule());
        break;
      case DATE_TIME_PICKER:
        email = false;
        length = 0;
        regex = null;
        requiredRule = ComponentUtil.createRequireRule(((DateTimePicker) component).getRequireRule());
        break;
      case FILE_UPLOAD:
        email = false;
        length = 0;
        regex = null;
        fieldName = ((FileUpload) component).getField().concat("-hidden");
        requiredRule = ComponentUtil.createRequireRule(((FileUpload) component).getRequireRule());
        break;
      case TEXT_FIELD:
        TextField textField = (TextField) component;
        regex = textField.getRegex();
        length = textField.getLength() > 0 ? textField.getLength() : Table.DEFAULTS_FIELD_LENGTH;
        email = textField.getType() == TextType.EMAIL;
        requiredRule = ComponentUtil.createRequireRule(textField.getRequireRule());
        break;
      case NUMBER_FIELD:
        NumberField numberField = (NumberField) component;
        maxIntValue = numberField.getMaxValue();
        length = 0;
        requiredRule = ComponentUtil.createRequireRule(numberField.getRequireRule());
        isNumber = true;
        regex = null;
        email = false;
        break;
      case FILTER_NUMBER_FIELD:
        length = 0;
        isNumber = true;
        regex = null;
        email = false;
        break;
//      case SELECT_TREE_GRID:
//        SelectTreeGrid selectTreeGrid = (SelectTreeGrid) component;
//        requiredRule = ComponentUtil.createRequireRule(selectTreeGrid.getRequireRule());
//        email = false;
//        regex = null;
//        length = 0;
//        break;
      default:
        return null;
    }
    if (fieldName == null)
      fieldName = ((ComponentWithSingleField) component).getField();

    StringBuilder rules = new StringBuilder();
    if (length > 0) {
      rules.append("maxlength: ").append(length);
    }
    if (regex != null && !regex.isEmpty()) {
      if (rules.length() > 0)
        rules.append(", ");
      rules.append(" regex: '").append(regex.trim()).append("' ");
    }
    if (email) {
      if (rules.length() > 0)
        rules.append(", ");
      rules.append("  email: true ");
    }
    if (isNumber) {
      if (rules.length() > 0)
        rules.append(", ");
      rules.append("  masked_number: true ");
    }
    if (requiredRule != null) {
      if (rules.length() > 0)
        rules.append(", ");
      rules.append("  required: ").append(requiredRule);
    }
    if (maxIntValue != null) {
      if (rules.length() > 0)
        rules.append(", ");
      rules.append("  masked_max: ").append(maxIntValue);
    }
    return " '" + fieldName + "': { " + rules.toString() + "    }";
  }

  private String readCurrentZipEntryContent(ZipInputStream zipInputStream) throws IOException {
    Writer writer = new StringWriter();
    char[] buffer = new char[1024];
    Reader reader = new BufferedReader(new InputStreamReader(zipInputStream, "UTF-8"));
    int n;
    while ((n = reader.read(buffer)) != -1) {
      writer.write(buffer, 0, n);
    }
    return writer.toString();
  }

  private void mergeComponents(List<FtlComponent> ftlComponents) {
    for (FtlComponent comp : ftlComponents) {
      if (!this.ftlComponents.contains(comp)) {
        this.ftlComponents.add(comp);
      }
      else {
        this.ftlComponents.get(this.ftlComponents.indexOf(comp)).addForms(comp.getSourceForms());
      }
    }
  }
}
