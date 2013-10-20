package ru.cg.runaex.runa_ext.handler.webservice_call_handler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.gson.Gson;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import org.apache.commons.lang.StringUtils;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathHelper;

import ru.cg.runaex.components.bean.component.part.ColumnReference;
import ru.cg.runaex.components.parser.ComponentParser;
import ru.cg.runaex.components.validation.helper.ColumnReferenceValidationHelper;
import ru.cg.runaex.wsdl_analyzer.XMLSupport;

/**
 * @author urmancheev
 */
public class WebserviceCallConfigurationHelper {

  private static final Pattern ARRAY_ELEMENT_TAG_PATTERN = Pattern.compile("(.*)(\\[\\d+\\])");

  public static WebserviceCallConfiguration parse(String configurationStr) throws JDOMException {
    WebserviceCallConfiguration configuration = new Gson().fromJson(configurationStr, WebserviceCallConfiguration.class); //JsonSyntaxException

    ParametersBundle bundle = getParametersBundle(configuration.getRequest());
    configuration.setRequestDbParameters(bundle.getDbParameters());
    configuration.setRequestVariableParameters(bundle.getVariableParameters());

    bundle = getParametersBundle(configuration.getResponse());
    configuration.setResponseDbParameters(bundle.getDbParameters());
    configuration.setResponseVariableParameters(bundle.getVariableParameters());

    return configuration;
  }

  public static String serializeToString(WebserviceCallConfiguration configuration) {
    return new Gson().toJson(configuration);
  }

  private static ParametersBundle getParametersBundle(String xmlStub) throws JDOMException {
    List<Parameter> parameters = getParameters(xmlStub);
    List<DbParameter> dbParameters = new ArrayList<DbParameter>(parameters.size());
    List<VariableParameter> variableParameters = new ArrayList<VariableParameter>(parameters.size());

    for (Parameter parameter : parameters) {
      ColumnReference columnReference = ComponentParser.parseColumnReference(StringUtils.trimToNull(parameter.getValue()), null);

      boolean isDatabaseParameter = ColumnReferenceValidationHelper.isValid(columnReference);
      if (isDatabaseParameter) {
        DbParameter dbParameter = new DbParameter();
        dbParameter.setColumnReference(columnReference);
        dbParameter.setParentElementXpath(parameter.getParentElementXpath());
        dbParameters.add(dbParameter);
      }
      else {
        VariableParameter variableParameter = new VariableParameter(parameter.getValue(), parameter.getParentElementXpath());
        variableParameters.add(variableParameter);
      }
    }

    ParametersBundle bundle = new ParametersBundle();
    bundle.setDbParameters(dbParameters);
    bundle.setVariableParameters(variableParameters);

    return bundle;
  }

  private static List<Parameter> getParameters(String xmlStub) throws JDOMException {
    List<Parameter> parameters = new LinkedList<Parameter>();

    Document document = XMLSupport.buildDocumentFromXml(xmlStub);
    Iterator<CDATA> iterator = document.getDescendants(Filters.cdata());

    while (iterator.hasNext()) {
      CDATA parameterDeclaration = iterator.next();
      Parameter parameter = new Parameter();

      parameter.setValue(parameterDeclaration.getValue());

      String xpath = XPathHelper.getAbsolutePath(parameterDeclaration.getParentElement());
      String ignoreNamespaceXpath = makeIgnoreNamespaceXpath(xpath);
      parameter.setParentElementXpath(ignoreNamespaceXpath);

      parameters.add(parameter);
    }

    return parameters;
  }

  /**
   * Required because jdom does not support default namespace (namespace with empty prefix)
   */
  private static String makeIgnoreNamespaceXpath(String xpath) {
    StringBuilder result = new StringBuilder("/");
    String[] elements = xpath.split("/");

    for (String element : elements) {
      if (!element.isEmpty()) {
        Matcher matcher = ARRAY_ELEMENT_TAG_PATTERN.matcher(element);

        result.append("/*[local-name()='");
        if (matcher.find()) {
          result.append(matcher.group(1))
              .append("']")
              .append(matcher.group(2));
        }
        else {
          result.append(element);
          result.append("']");
        }
      }
    }

    return result.toString();
  }

  public static String convertParameter(Object parameter) {
    if (parameter instanceof Date) {
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTime((Date) parameter);
      XMLGregorianCalendar xmlCalendar = new XMLGregorianCalendarImpl(calendar);
      return xmlCalendar.toXMLFormat();  //todo only dateTime supported at the moment
    }
    //todo
    return parameter != null ? parameter.toString() : "";
  }

  public static Object convertParameterValue(String value) {
    //todo
    return value != null ? StringUtils.trimToNull(value) : null;
  }

  private static class Parameter {
    private String value;
    private String parentElementXpath;

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getParentElementXpath() {
      return parentElementXpath;
    }

    public void setParentElementXpath(String parentElementXpath) {
      this.parentElementXpath = parentElementXpath;
    }
  }

  private static class ParametersBundle {
    List<DbParameter> dbParameters;
    List<VariableParameter> variableParameters;

    public List<DbParameter> getDbParameters() {
      return dbParameters;
    }

    public void setDbParameters(List<DbParameter> dbParameters) {
      this.dbParameters = dbParameters;
    }

    public List<VariableParameter> getVariableParameters() {
      return variableParameters;
    }

    public void setVariableParameters(List<VariableParameter> variableParameters) {
      this.variableParameters = variableParameters;
    }
  }
}
