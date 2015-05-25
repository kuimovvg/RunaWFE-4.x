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
package ru.runa.wfe.var.logic;

import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.audit.AdminActionLog;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.logic.WFCommonLogic;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableUserType;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Process execution logic.
 * 
 * @author Dofs
 * @since 2.0
 */
public class VariableLogic extends WFCommonLogic {

    private static final Pattern HAS_QUALIFIER = Pattern.compile("\\[[^\\]]+\\]");
    private static final Pattern DICT_QUALIFIER = Pattern.compile("(.+)\\[\\s*([^\\]]+)\\]");
    private static final Pattern DICT_COMPLEX_QUALIFIER = Pattern.compile("(.+)\\[\\s*([^\\]]+)\\]\\s*\\.\\s*(.+)");

    public List<WfVariable> getVariables(User user, Long processId) throws ProcessDoesNotExistException {
        List<WfVariable> result = Lists.newArrayList();
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        Map<String, Object> values = variableDAO.getAll(process);
        for (VariableDefinition variableDefinition : processDefinition.getVariables()) {
            Object value;
            if (variableDefinition.isComplex()) {
                value = buildComplexVariable(variableDefinition, values, variableDefinition.getName());
            } else {
                value = values.remove(variableDefinition.getName());
            }
            if (value != null) {
                result.add(new WfVariable(variableDefinition, value));
            }
        }
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            result.add(new WfVariable(entry.getKey(), entry.getValue()));
        }
        return result;
    }

    private ComplexVariable buildComplexVariable(VariableDefinition variableDefinition, Map<String, Object> values, String prefix) {
        ComplexVariable variable = new ComplexVariable(variableDefinition);
        for (VariableDefinition attributeDefinition : variableDefinition.getUserType().getAttributes()) {
            String variableName = prefix + VariableUserType.DELIM + attributeDefinition.getName();
            if (attributeDefinition.isComplex()) {
                ComplexVariable complexVariable = buildComplexVariable(attributeDefinition, values, variableName);
                if (complexVariable != null) {
                    variable.put(attributeDefinition.getName(), complexVariable);
                }
            } else {
                Object value = values.remove(variableName);
                if (value != null) {
                    variable.put(attributeDefinition.getName(), value);
                }
            }
        }
        if (variable.isEmpty()) {
            return null;
        }
        return variable;
    }

    public WfVariable getVariable(User user, Long processId, String variableName) throws ProcessDoesNotExistException {
        Process process = processDAO.getNotNull(processId);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        String qualifier = null;
        String variableSubName = null;

        if (HAS_QUALIFIER.matcher(variableName).find()) {
            Matcher dictMatcher = DICT_QUALIFIER.matcher(variableName);
            Matcher dictComplexMatcher = DICT_COMPLEX_QUALIFIER.matcher(variableName);
            if (dictComplexMatcher.find()) {
                MatchResult mr = dictComplexMatcher.toMatchResult();
                if (mr.groupCount() == 3) {
                    variableName = mr.group(1).trim();
                    qualifier = mr.group(2).trim();
                    variableSubName = mr.group(3);
                }
            } else if (dictMatcher.find()) {
                MatchResult mr = dictMatcher.toMatchResult();
                if (mr.groupCount() == 2) {
                    variableName = mr.group(1).trim();
                    qualifier = mr.group(2).trim();
                }
            }
        }

        WfVariable variable = executionContext.getVariableProvider().getVariable(variableName);
        if (qualifier != null) {
            if (ListFormat.class.getName().equals(variable.getDefinition().getFormatClassName())) {
                VariableFormat qualifierFormat = FormatCommons.createComponent(variable, 0);
                List<Object> list = (List<Object>) variable.getValue();
                if (list == null) {
                    return new WfVariable(new VariableDefinition(true, variableName, variableName, qualifierFormat.getClass().getName()), null);
                }
                Object value = TypeConversionUtil.getListValue(list, Integer.parseInt(qualifier));
                if (value instanceof ComplexVariable) {
                    VariableUserType userType = ((ComplexVariable) value).getUserType();
                    for (VariableDefinition def : userType.getAttributes()) {
                        if (!def.getName().equals(variableSubName)) {
                            continue;
                        }
                        return new WfVariable(def, ((ComplexVariable) value).get(variableSubName));
                    }
                }
                return new WfVariable(new VariableDefinition(true, variableName, variableName, qualifierFormat.getClass().getName()), value);
            }
            if (MapFormat.class.getName().equals(variable.getDefinition().getFormatClassName())) {
                if (MapFormat.KEY_NULL_VALUE.equals(qualifier)) {
                    qualifier = "";
                }
                Map<Object, Object> map = (Map<Object, Object>) variable.getValue();
                VariableFormat qualifierFormat = FormatCommons.createComponent(variable, 0);
                if (map == null) {
                    return new WfVariable(new VariableDefinition(true, variableName, variableName, qualifierFormat.getClass().getName()), null);
                }
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    String keyInQualifierFormat = qualifierFormat.format(entry.getKey());
                    if (Objects.equal(keyInQualifierFormat, qualifier) || (keyInQualifierFormat == null && Strings.isNullOrEmpty(qualifier))) {
                        Object value = entry.getValue();
                        if (value instanceof ComplexVariable) {
                            VariableUserType userType = ((ComplexVariable) value).getUserType();
                            for (VariableDefinition def : userType.getAttributes()) {
                                if (!def.getName().equals(variableSubName)) {
                                    continue;
                                }
                                return new WfVariable(def, ((ComplexVariable) value).get(variableSubName));
                            }
                        }
                        return new WfVariable(new VariableDefinition(true, variableName, variableName, qualifierFormat.getClass().getName()), value);
                    }
                }
                throw new IllegalArgumentException("Invalid key = '" + qualifier + "'; all values: " + map);
            }
            throw new IllegalArgumentException("Key '" + qualifier + "' was provided but variable format is "
                    + variable.getDefinition().getFormatClassName());
        }
        return variable;
    }

    public void updateVariables(User user, Long processId, Map<String, Object> variables) {
        Process process = processDAO.getNotNull(processId);
        // TODO check ProcessPermission.UPDATE
        checkPermissionAllowed(user, process, ProcessPermission.READ);
        ProcessDefinition processDefinition = getDefinition(process);
        ExecutionContext executionContext = new ExecutionContext(processDefinition, process);
        processLogDAO.addLog(new AdminActionLog(user.getActor(), AdminActionLog.ACTION_UPDATE_VARIABLES), process, null);
        executionContext.setVariableValues(variables);
    }

}
