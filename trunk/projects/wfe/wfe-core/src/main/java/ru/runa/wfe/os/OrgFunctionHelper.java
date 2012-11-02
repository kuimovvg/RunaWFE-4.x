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

package ru.runa.wfe.os;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDAO;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Created on 03.01.2006.
 */
public class OrgFunctionHelper {

    private OrgFunctionHelper() {
    }

    public static List<? extends Executor> evaluateOrgFunction(String swimlaneInitializer, Long actorToSubstituteCode) throws OrgFunctionException {
        swimlaneInitializer = swimlaneInitializer.trim();
        List<? extends Executor> result = evaluateOrgFunctionInternal(null, getOrgFunction(swimlaneInitializer), actorToSubstituteCode);
        return applyRelation(swimlaneInitializer, result);
    }

    public static List<? extends Executor> evaluateOrgFunction(IVariableProvider variableProvider, String swimlaneInitializer,
            Long actorToSubstituteCode) throws OrgFunctionException {
        swimlaneInitializer = swimlaneInitializer.trim();
        List<? extends Executor> result = evaluateOrgFunctionInternal(variableProvider, getOrgFunction(swimlaneInitializer), actorToSubstituteCode);
        return applyRelation(swimlaneInitializer, result);
    }

    private static List<? extends Executor> applyRelation(String swimlaneInitializer, List<? extends Executor> executors) {
        if (!swimlaneInitializer.startsWith("@")) {
            return executors;
        }
        ExecutorDAO executorDAO = ApplicationContextFactory.getExecutorDAO();
        RelationDAO relationDAO = ApplicationContextFactory.getRelationDAO();
        String relationGroup = swimlaneInitializer.substring(1, swimlaneInitializer.indexOf('('));
        Set<Executor> relationExecutors = new HashSet<Executor>();
        for (Executor executor : executors) {
            relationExecutors.add(executor);
            relationExecutors.addAll(executorDAO.getExecutorParentsAll(executor));
        }
        Set<Executor> resultSet = Sets.newHashSet();
        for (RelationPair relation : relationDAO.getExecutorsRelationPairsRight(relationGroup, new ArrayList<Executor>(relationExecutors))) {
            resultSet.add(relation.getLeft());
        }
        return Lists.newArrayList(resultSet);
    }

    private static String getOrgFunction(String swimlaneInitializer) {
        if (swimlaneInitializer.startsWith("@")) {
            return swimlaneInitializer.substring(swimlaneInitializer.indexOf('(') + 1, swimlaneInitializer.length() - 1);
        }
        return swimlaneInitializer;
    }

    private static List<? extends Executor> evaluateOrgFunctionInternal(IVariableProvider variableProvider, String swimlaneInititalizer,
            Long actorToSubstituteCode) throws OrgFunctionException {
        FunctionConfiguration functionConfiguration = OrgFunctionParser.parse(swimlaneInititalizer);
        OrgFunction function = ApplicationContextFactory.createAutowiredBean(functionConfiguration.getFunctionName());
        Object[] parameters = fillProcessVariableArray(variableProvider, functionConfiguration, actorToSubstituteCode);
        return function.getExecutors(parameters);
    }

    private static final String ORG_FUNCTION_RESULT_SYMBOL = "?";

    private static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");// ^\$\{(.*)\}$

    private static Object[] fillProcessVariableArray(IVariableProvider variableProvider, FunctionConfiguration functionConfiguration,
            Long actorToSubstituteCode) {
        String[] variableNames = functionConfiguration.getParameters();
        Object[] parameters = new Object[variableNames.length];
        for (int i = 0; i < variableNames.length; i++) {
            Matcher matcher = pattern.matcher(variableNames[i]);
            if (ORG_FUNCTION_RESULT_SYMBOL.equals(variableNames[i]) && actorToSubstituteCode != null) {
                parameters[i] = String.valueOf(actorToSubstituteCode.longValue());
            } else if (matcher.matches()) {
                String processVariableName = matcher.group(1);
                Preconditions.checkNotNull(variableProvider, "variableProvider required for this orgfunction");
                parameters[i] = variableProvider.get(processVariableName);
            } else {
                parameters[i] = variableNames[i];
            }
        }
        return parameters;
    }

}
