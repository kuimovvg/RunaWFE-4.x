/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import ru.runa.ConfigurationException;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
import ru.runa.commons.TypeConversionUtil;

/**
 * Base class for standard XML parameter-based configuration.
 * 
 * @author dofs[197@gmail.com]
 */
@SuppressWarnings("unchecked")
public abstract class ParamBasedActionHandler implements ActionHandler {
    protected static final Log log = LogFactory.getLog(ParamBasedActionHandler.class);
    private static final long serialVersionUID = 1L;
    protected String configuration;
    private final Map<String, ParamDef> inputParams = new HashMap<String, ParamDef>();
    protected Map<String, ParamDef> outputParams = new HashMap<String, ParamDef>();
    protected Long processInstanceId;
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

    protected abstract void executeAction() throws Exception;

    @Override
    public final void execute(ExecutionContext executionContext) throws Exception {
        try {
            processInstanceId = executionContext.getProcessInstance().getId();
            log.debug("Executing in " + processInstanceId + " with " + configuration);
            inputVariables = executionContext.getContextInstance().getVariables();
            executeAction();
            executionContext.getContextInstance().setVariables(executionContext, outputVariables);
        } catch (Throwable th) {
            log.error("Action handler execution error.", th);
            if (th instanceof Exception) {
                throw (Exception) th;
            }
            throw new RuntimeException(th);
        }
    }

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        this.configuration = configuration;
        if (configuration.trim().length() == 0) {
            return;
        }
        try {
            Document doc = DocumentHelper.parseText(configuration);
            Element inputElement = doc.getRootElement().element("input");
            if (inputElement != null) {
                List<Element> inputParamElements = inputElement.elements("param");
                for (Element element : inputParamElements) {
                    ParamDef paramDef = new ParamDef(element);
                    inputParams.put(paramDef.name, paramDef);
                }
            }
            Element outputElement = doc.getRootElement().element("output");
            if (outputElement != null) {
                List<Element> outputParamElements = outputElement.elements("param");
                for (Element element : outputParamElements) {
                    ParamDef paramDef = new ParamDef(element);
                    outputParams.put(paramDef.name, paramDef);
                }
            }
        } catch (Throwable th) {
            log.error("Configuration error.", th);
            throw new ConfigurationException(th);
        }
    }

    public static class ParamDef {
        private final String name;
        private final String variableName;
        private final String value;

        public ParamDef(Element element) {
            name = element.attributeValue("name");
            variableName = element.attributeValue("variable");
            value = element.attributeValue("value");
        }

        public String getVariableName() {
            return variableName;
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
