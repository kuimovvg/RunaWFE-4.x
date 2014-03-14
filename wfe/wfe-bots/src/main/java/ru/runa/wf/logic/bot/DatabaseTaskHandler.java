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
package ru.runa.wf.logic.bot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.beanutils.PropertyUtils;

import ru.runa.wfe.commons.SQLCommons;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.sqltask.AbstractQuery;
import ru.runa.wfe.commons.sqltask.DatabaseTask;
import ru.runa.wfe.commons.sqltask.DatabaseTaskXmlParser;
import ru.runa.wfe.commons.sqltask.Parameter;
import ru.runa.wfe.commons.sqltask.Query;
import ru.runa.wfe.commons.sqltask.Result;
import ru.runa.wfe.commons.sqltask.StoredProcedureQuery;
import ru.runa.wfe.commons.sqltask.SwimlaneParameter;
import ru.runa.wfe.commons.sqltask.SwimlaneResult;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Maps;

/**
 * @created on 01.04.2005
 * @modifier 22.03.2006 gaidomartin@gmail.com
 */
public class DatabaseTaskHandler extends TaskHandlerBase {
    private String configuration;

    @Override
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws Exception {
        Map<String, Object> outputVariables = Maps.newHashMap();
        if (variableProvider.getVariable(DatabaseTask.INSTANCE_ID_VARIABLE_NAME) != null) {
            outputVariables.put(DatabaseTask.INSTANCE_ID_VARIABLE_NAME, task.getProcessId());
        }
        if (variableProvider.getVariable(DatabaseTask.CURRENT_DATE_VARIABLE_NAME) != null) {
            outputVariables.put(DatabaseTask.CURRENT_DATE_VARIABLE_NAME, new Date());
        }
        DatabaseTask[] databaseTasks = DatabaseTaskXmlParser.parse(configuration, variableProvider);
        Context context = new InitialContext();
        for (int i = 0; i < databaseTasks.length; i++) {
            DatabaseTask databaseTask = databaseTasks[i];
            Connection conn = null;
            PreparedStatement ps = null;
            try {
                DataSource ds = (DataSource) context.lookup(databaseTask.getDatasourceName());
                conn = ds.getConnection();
                for (int j = 0; j < databaseTask.getQueriesCount(); j++) {
                    AbstractQuery query = databaseTask.getQuery(j);
                    try {
                        if (query instanceof Query) {
                            ps = conn.prepareStatement(query.getSql());
                        } else if (query instanceof StoredProcedureQuery) {
                            ps = conn.prepareCall(query.getSql());
                        } else {
                            String unknownQueryClassName = (query == null ? "null" : query.getClass().getName());
                            throw new Exception("Unknown query type:" + unknownQueryClassName);
                        }
                        fillQueryParameters(user, ps, variableProvider, query, task);
                        if (ps.execute()) {
                            ResultSet resultSet = ps.getResultSet();
                            if (!resultSet.next()) {
                                throw new Exception("No results in rowset for query " + query);
                            }
                            outputVariables.putAll(extractResultsToProcessVariables(user, variableProvider, resultSet, query));
                        }
                    } finally {
                        SQLCommons.releaseResources(ps);
                    }
                }
            } finally {
                SQLCommons.releaseResources(conn);
            }
        }
        return outputVariables;
    }

    private Map<String, Object> extractResultsToProcessVariables(User user, IVariableProvider variableProvider, ResultSet resultSet,
            AbstractQuery query) throws Exception {
        Map<String, Object> outputVariables = Maps.newHashMap();
        for (int i = 0; i < query.getResultVariableCount(); i++) {
            Result result = query.getResultVariable(i);

            Object newValue = resultSet.getObject(i + 1);
            Object variableValue = variableProvider.getValue(result.getVariableName());
            if (result instanceof SwimlaneResult) {
                String fieldName = result.getFieldName();
                Actor actor = null;
                if ("code".equals(fieldName)) {
                    actor = TypeConversionUtil.convertToExecutor(newValue, new DelegateExecutorLoader(user));
                } else if ("id".equals(fieldName)) {
                    actor = Delegates.getExecutorService().getExecutor(user, (Long) newValue);
                } else {
                    actor = Delegates.getExecutorService().getExecutorByName(user, (String) newValue);
                }
                newValue = Long.toString(actor.getCode());
            } else if (result.isFieldSetup()) {
                String fieldName = result.getFieldName();
                PropertyUtils.setProperty(variableValue, fieldName, newValue);
                newValue = variableValue;
            }
            outputVariables.put(result.getVariableName(), newValue);
        }
        return outputVariables;
    }

    private void fillQueryParameters(User user, PreparedStatement ps, IVariableProvider variableProvider, AbstractQuery query, WfTask task)
            throws Exception {
        for (int i = 0; i < query.getParameterCount(); i++) {
            Parameter parameter = query.getParameter(i);
            Object value = variableProvider.getValue(parameter.getVariableName());
            if (value == null) {
                if (DatabaseTask.INSTANCE_ID_VARIABLE_NAME.equals(parameter.getVariableName())) {
                    value = task.getProcessId();
                }
                if (DatabaseTask.CURRENT_DATE_VARIABLE_NAME.equals(parameter.getVariableName())) {
                    value = new Date();
                }
            }
            if (parameter instanceof SwimlaneParameter) {
                Actor actor = TypeConversionUtil.convertToExecutor(value, new DelegateExecutorLoader(user));
                value = PropertyUtils.getProperty(actor, ((SwimlaneParameter) parameter).getFieldName());
            } else if (parameter.isFieldSetup()) {
                value = PropertyUtils.getProperty(value, parameter.getFieldName());
            }
            ps.setObject(i + 1, value);
        }
    }

}
