package ru.runa.wf.logic.bot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.handler.bot.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class ParamMappedTaskHandler extends TaskHandlerBase {
    private static final Log log = LogFactory.getLog(ParamMappedTaskHandler.class);
    protected static final String BOTS_XML_FILE = "bots.xml";
    protected static final String INPUT_PARAM = "input";
    protected static final String OUTPUT_PARAM = "output";
    protected static final String PARAMETER_PARAM = "param";
    protected static final String TASK_PARAM = "task";
    protected static final String NAME_PARAM = "name";
    protected static final String VALUE_PARAM = "value";
    protected static final String VARIABLE_PARAM = "variable";

    protected final Map<String, ParamDef> inputParams = new HashMap<String, ParamDef>();
    protected Map<String, ParamDef> outputParams = new HashMap<String, ParamDef>();
    protected Map<String, Object> inputVariables;
    protected Map<String, Object> outputVariables = new HashMap<String, Object>();

    protected <T> T getInputParam(String name) {
        ParamDef paramDef = inputParams.get(name);
        if (paramDef == null) {
            throw new NullPointerException("Parameter '" + name + "' not defined in configuration.");
        }
        return (T) getInputParam(name, false);
    }

    protected <T> T getInputParam(String name, T defaultValue) {
        T result = (T) getInputParam(name, true);
        if (result == null) {
            return defaultValue;
        }
        return result;
    }

    private <T> T getInputParam(String name, boolean optional) {
        ParamDef paramDef = inputParams.get(name);
        if (paramDef == null) {
            if (optional) {
                return null;
            }
            throw new NullPointerException("Parameter '" + name + "' not defined in configuration.");
        }
        T result = null;
        if (paramDef.variableName != null) {
            result = (T) inputVariables.get(paramDef.variableName);
        }
        if (result == null && paramDef.value != null) {
            result = (T) paramDef.value;
        }
        if (result == null && !optional) {
            throw new NullPointerException("Required parameter '" + paramDef + "' resolved as null, vars = " + inputVariables);
        }
        return result;
    }

    protected <T> T getInputParam(Class<T> clazz, String name) {
        Object object = getInputParam(name);
        return TypeConversionUtil.convertTo(object, clazz);
    }

    protected <T> T getInputParam(Class<T> clazz, String name, T defaultValue) {
        Object object = getInputParam(name, defaultValue);
        return TypeConversionUtil.convertTo(object, clazz);
    }

    protected <T> T getInputVariable(Class<T> clazz, String name, boolean required) {
        Object object = inputVariables.get(name);
        if (required && object == null) {
            throw new NullPointerException("Required variable '" + name + "' resolved as null, vars = " + inputVariables);
        }
        return TypeConversionUtil.convertTo(object, clazz);
    }

    protected void setInputVariable(String name, Object value) {
        ParamDef paramDef = inputParams.get(name);
        if (paramDef == null) {
            log.warn("Want to set undefined parameter " + name + "=" + value);
            return;
        }
        if (paramDef.variableName == null) {
            throw new NullPointerException("Variable not set for output parameter " + paramDef + " in configuration.");
        }
        if (value == null) {
            throw new NullPointerException("Trying to set null for parameter " + paramDef);
        }
        if (value != null) {
            inputVariables.put(paramDef.variableName, value);
        }
    }

    protected void setOutputVariable(String name, Object value) {
        ParamDef paramDef = outputParams.get(name);
        if (paramDef == null) {
            log.warn("Want to set undefined parameter " + name + "=" + value);
            return;
        }
        if (paramDef.variableName == null) {
            throw new NullPointerException("Variable not set for output parameter " + paramDef + " in configuration.");
        }
        if (value == null) {
            throw new NullPointerException("Trying to set null for parameter " + paramDef);
        }
        if (value != null) {
            outputVariables.put(paramDef.variableName, value);
        }
    }

    @Override
    public void setConfiguration(String configuration) throws Exception {
        Document doc = XmlUtils.parseWithoutValidation(configuration);
        Element inputElement = doc.getRootElement().element(INPUT_PARAM);
        if (inputElement != null) {
            List<Element> inputParamElements = inputElement.elements(PARAMETER_PARAM);
            for (Element element : inputParamElements) {
                ParamDef paramDef = new ParamDef(element);
                inputParams.put(paramDef.name, paramDef);
            }
        }
        Element outputElement = doc.getRootElement().element(OUTPUT_PARAM);
        if (outputElement != null) {
            List<Element> outputParamElements = outputElement.elements(PARAMETER_PARAM);
            for (Element element : outputParamElements) {
                ParamDef paramDef = new ParamDef(element);
                outputParams.put(paramDef.name, paramDef);
            }
        }
    }

    @Override
    public Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask task) throws Exception {
        byte[] botXmlFile = Delegates.getDefinitionService().getFile(subject, task.getDefinitionId(), BOTS_XML_FILE);
        Document doc = XmlUtils.parseWithoutValidation(botXmlFile);
        List<Element> taskElements = doc.getRootElement().elements(TASK_PARAM);
        for (Element taskElement : taskElements) {
            if (Objects.equal(taskElement.attributeValue(NAME_PARAM), task.getName())) {
                List<Element> params = Lists.newArrayList();
                findAllParamElement(params, taskElement);

                for (Element param : params) {
                    ParamDef paramDef = inputParams.get(param.attributeValue(NAME_PARAM));

                    if (paramDef != null) {
                        String varName = param.attributeValue(VALUE_PARAM);
                        if (!Strings.isNullOrEmpty(varName)) {
                            if (paramDef.isUseVariable()) {
                                Object value = variableProvider.getValue(varName);
                                if (value != null) {
                                    paramDef.variableName = varName;
                                    setInputVariable(param.attributeValue(NAME_PARAM), value);
                                }
                            } else {
                                paramDef.value = varName;
                            }
                        }

                        varName = param.attributeValue(VARIABLE_PARAM);
                        if (!Strings.isNullOrEmpty(varName)) {
                            paramDef.variableName = varName;
                        }
                    }

                    paramDef = outputParams.get(param.attributeValue(NAME_PARAM));

                    if (paramDef != null) {
                        String varName = param.attributeValue(VALUE_PARAM);
                        if (!Strings.isNullOrEmpty(varName)) {
                            paramDef.variableName = varName;
                        }
                        varName = param.attributeValue(VARIABLE_PARAM);
                        if (!Strings.isNullOrEmpty(varName)) {
                            paramDef.variableName = varName;
                        }
                    }
                }

                break;
            }
        }
        return outputVariables;
    }

    private void findAllParamElement(List<Element> resultElements, Element rootElement) {
        Iterator iterator = rootElement.elementIterator();
        while (iterator.hasNext()) {
            Element element = (Element) iterator.next();
            if (element.getName().equals(PARAMETER_PARAM)) {
                resultElements.add(element);
            } else {
                findAllParamElement(resultElements, element);
            }
        }
    }

    protected abstract void executeAction() throws Exception;

    public class ParamDef {
        private String name;
        private String variableName;
        private String value;
        private boolean useVariable;

        public ParamDef(Element element) {
            name = element.attributeValue(NAME_PARAM);
            useVariable = Boolean.parseBoolean(element.attributeValue(VARIABLE_PARAM));
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isUseVariable() {
            return useVariable;
        }

        public void setUseVariable(boolean useVariable) {
            this.useVariable = useVariable;
        }

        public String getVariableName() {
            return variableName;
        }

        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }

        @Override
        public String toString() {
            StringBuffer b = new StringBuffer(name);
            if (variableName != null) {
                b.append(" [var=").append(variableName).append("]");
            }
            if (value != null) {
                b.append(" [value=").append(value).append("]");
            }
            return b.toString();
        }
    }
}
