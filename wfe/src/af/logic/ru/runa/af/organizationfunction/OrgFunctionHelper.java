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

package ru.runa.af.organizationfunction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.RelationPair;
import ru.runa.af.TmpApplicationContextFactory;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.RelationDAO;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;

import com.google.common.collect.Lists;

/**
 * Created on 03.01.2006
 * 
 */
public class OrgFunctionHelper {
    private static final Log log = LogFactory.getLog(OrgFunctionHelper.class);

    private OrgFunctionHelper() {
    }

    /**
     * @param executionService
     * @param token
     * @param swimlaneInititalizer
     * @param actorToSubstitudeCode
     *            if no one to substitute put null
     * @return
     * @throws FunctionParserException
     * @throws OrganizationFunctionException
     * @throws ExecutorOutOfDateException
     */
    public static List<Long> evaluateOrgFunction(Map<String, Object> variablesMap, String swimlaneInitializer, Long actorToSubstituteCode)
            throws FunctionParserException, OrganizationFunctionException {
        swimlaneInitializer = swimlaneInitializer.trim();
        List<Long> result = evaluateOrgFunctionInternal(variablesMap, getOrgFunction(swimlaneInitializer), actorToSubstituteCode);
        return applyRelation(swimlaneInitializer, result);
    }

    public static List<Long> evaluateOrgFunction(TaskInstance taskInstance, String swimlaneInitializer, Long actorToSubstituteCode)
            throws FunctionParserException, OrganizationFunctionException {
        swimlaneInitializer = swimlaneInitializer.trim();
        List<Long> result = evaluateOrgFunctionInternal(taskInstance, getOrgFunction(swimlaneInitializer), actorToSubstituteCode);
        return applyRelation(swimlaneInitializer, result);
    }

    static List<Long> applyRelation(String swimlaneInitializer, List<Long> executorIds) {
        if (!swimlaneInitializer.startsWith("@")) {
            return executorIds;
        }
        try {
            ExecutorDAO executorDAO = TmpApplicationContextFactory.getExecutorDAO();
            RelationDAO relationDAO = TmpApplicationContextFactory.getRelationDAO();
            String relationGroup = swimlaneInitializer.substring(1, swimlaneInitializer.indexOf('('));
            Set<Executor> executors = new HashSet<Executor>();
            for (Executor executor : executorDAO.getExecutors(executorIds)) {
                executors.add(executor);
                executors.addAll(executorDAO.getExecutorParentsAll(executor));
            }
            Set<Long> resultSet = new HashSet<Long>();
            for (RelationPair relation : relationDAO.getExecutorsRelationPairsRight(relationGroup,
                    new ArrayList<Executor>(executors))) {
                resultSet.add(relation.getLeft().getId());
            }
            return Lists.newArrayList(resultSet);
        } catch (Exception e) {
            log.error("evaluation relation failed", e);
        }
        return executorIds;
    }

    static String getOrgFunction(String swimlaneInitializer) {
        if (swimlaneInitializer.startsWith("@")) {
            return swimlaneInitializer.substring(swimlaneInitializer.indexOf('(') + 1, swimlaneInitializer.length() - 1);
        }
        return swimlaneInitializer;
    }

    private static List<Long> evaluateOrgFunctionInternal(Map<String, Object> variablesMap, String swimlaneInititalizer, Long actorToSubstituteCode)
            throws FunctionParserException, OrganizationFunctionException {
        FunctionConfiguration functionConfiguration = SimpleFunctionParser.INSTANCE.parse(swimlaneInititalizer);
        OrganizationFunction function = ReflectionOrganizaionFunctionFactory.INSTANCE.create(functionConfiguration.getFunctionName());
        Object[] parameters = fillProcessVariableArray(variablesMap, functionConfiguration, actorToSubstituteCode);
        return function.getExecutorIds(parameters);
    }

    private static List<Long> evaluateOrgFunctionInternal(TaskInstance taskInstance, String swimlaneInititalizer, Long actorToSubstituteCode)
            throws FunctionParserException, OrganizationFunctionException {
        FunctionConfiguration functionConfiguration = SimpleFunctionParser.INSTANCE.parse(swimlaneInititalizer);
        OrganizationFunction function = ReflectionOrganizaionFunctionFactory.INSTANCE.create(functionConfiguration.getFunctionName());
        Object[] parameters = fillProcessVariableArray(taskInstance, functionConfiguration, actorToSubstituteCode);
        return function.getExecutorIds(parameters);
    }

    private static final String ORG_FUNCTION_RESULT_SYMBOL = "?";

    private static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");// ^\$\{(.*)\}$

    private static Object[] fillProcessVariableArray(TaskInstance taskInstance, FunctionConfiguration functionConfiguration,
            Long actorToSubstituteCode) {
        String[] variableNames = functionConfiguration.getParameters();
        Object[] parameters = new Object[variableNames.length];
        for (int i = 0; i < variableNames.length; i++) {
            Matcher matcher = pattern.matcher(variableNames[i]);
            if (ORG_FUNCTION_RESULT_SYMBOL.equals(variableNames[i]) && actorToSubstituteCode != null) {
                parameters[i] = String.valueOf(actorToSubstituteCode.longValue());
            } else if (matcher.matches()) {
                String processVariableName = matcher.group(1);
                parameters[i] = taskInstance.getVariable(processVariableName);
            } else {
                parameters[i] = variableNames[i];
            }
        }
        return parameters;
    }

    private static Object[] fillProcessVariableArray(Map<String, Object> variablesMap, FunctionConfiguration functionConfiguration,
            Long actorToSubstituteCode) {
        String[] variableNames = functionConfiguration.getParameters();
        Object[] parameters = new Object[variableNames.length];
        for (int i = 0; i < variableNames.length; i++) {
            Matcher matcher = pattern.matcher(variableNames[i]);
            if (ORG_FUNCTION_RESULT_SYMBOL.equals(variableNames[i]) && actorToSubstituteCode != null) {
                parameters[i] = String.valueOf(actorToSubstituteCode.longValue());
            } else if (matcher.matches()) {
                String processVariableName = matcher.group(1);
                parameters[i] = variablesMap.get(processVariableName);
            } else {
                parameters[i] = variableNames[i];
            }
        }
        return parameters;
    }

}
