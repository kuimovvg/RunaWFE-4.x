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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.sql.DataSource;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.Actor;
import ru.runa.af.service.ExecutorService;
import ru.runa.commons.SQLCommons;
import ru.runa.commons.sqltask.AbstractQuery;
import ru.runa.commons.sqltask.DatabaseTask;
import ru.runa.commons.sqltask.DatabaseTaskXmlParser;
import ru.runa.commons.sqltask.Parameter;
import ru.runa.commons.sqltask.Query;
import ru.runa.commons.sqltask.Result;
import ru.runa.commons.sqltask.StoredProcedureQuery;
import ru.runa.commons.sqltask.SwimlaneParameter;
import ru.runa.commons.sqltask.SwimlaneResult;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.TaskStub;
import ru.runa.wf.service.ExecutionService;

import com.google.common.io.ByteStreams;

/**
 * @created on 01.04.2005
 * @modifier 22.03.2006 gaidomartin@gmail.com
 */
public class DatabaseTaskHandler implements TaskHandler {
    private static final Log log = LogFactory.getLog(DatabaseTaskHandler.class);

    private byte[] configuration = null;

    public void configure(String configurationName) throws TaskHandlerException {
        InputStream inputStream = DatabaseTaskHandler.class.getResourceAsStream(configurationName);
        if (inputStream == null) {
            throw new TaskHandlerException("Unable to find configuration " + configurationName);
        }
        try {
            this.configuration = ByteStreams.toByteArray(inputStream);
        } catch (IOException e) {
            throw new TaskHandlerException("Unable to read configuration " + configurationName, e);
        }
    }

    public void configure(byte[] configuration) {
        this.configuration = configuration;
    }

    public void handle(Subject subject, TaskStub taskStub) throws TaskHandlerException {
        try {
            log.info("DatabaseTask started, task " + taskStub);
            ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
            ExecutorService executorService = ru.runa.delegate.DelegateFactory.getInstance().getExecutorService();
            Map<String, Object> variables = executionService.getVariables(subject, taskStub.getId());
            variables.put(DatabaseTask.INSTANCE_ID_VARIABLE_NAME, new Long(taskStub.getProcessInstanceId()));
            variables.put(DatabaseTask.CURRENT_DATE_VARIABLE_NAME, new Date());
            DatabaseTask[] databaseTasks = DatabaseTaskXmlParser.parse(configuration, variables);
            Context context = new InitialContext();
            for (int i = 0; i < databaseTasks.length; i++) {
                DatabaseTask databaseTask = databaseTasks[i];
                Connection conn = null;
                PreparedStatement ps = null;
                try {
                    DataSource ds = (DataSource) context.lookup(databaseTask.getDatasourceName());
                    conn = ds.getConnection();
                    // conn.setAutoCommit(false);
                    for (int j = 0; j < databaseTask.getQueriesCount(); j++) {
                        AbstractQuery query = databaseTask.getQuery(j);
                        try {
                            if (query instanceof Query) {
                                ps = conn.prepareStatement(query.getSql());
                            } else if (query instanceof StoredProcedureQuery) {
                                ps = conn.prepareCall(query.getSql());
                            } else {
                                String unknownQueryClassName = (query == null ? "null" : query.getClass().getName());
                                throw new TaskHandlerException("Unknown query type:" + unknownQueryClassName);
                            }
                            fillQueryParameters(executorService, subject, ps, variables, query);
                            if (ps.execute()) {
                                ResultSet resultSet = ps.getResultSet();
                                if (!resultSet.next()) {
                                    throw new TaskHandlerException("No results in rowset for query " + query);
                                }
                                extractResultsToProcessVariables(subject, executorService, variables, resultSet, query);
                            }
                        } finally {
                            SQLCommons.releaseResources(ps);
                        }
                    }
                    // conn.commit();
                } finally {
                    SQLCommons.releaseResources(conn);
                }
            }
            // TODO fix this to return out bot params
            executionService.completeTask(subject, taskStub.getId(), taskStub.getName(), taskStub.getTargetActor().getId(), variables);
            log.info("DatabaseTask finished, task " + taskStub);
        } catch (Exception e) {
            throw new TaskHandlerException(e);
        }
    }

    private void extractResultsToProcessVariables(Subject subject, ExecutorService executorService, Map<String, Object> variables,
            ResultSet resultSet, AbstractQuery query) throws Exception {
        for (int i = 0; i < query.getResultVariableCount(); i++) {
            Result result = query.getResultVariable(i);

            Object newValue = resultSet.getObject(i + 1);
            Object variableValue = variables.get(result.getVariableName());
            if (result instanceof SwimlaneResult) {
                String fieldName = result.getFieldName();
                Actor actor = null;
                if ("code".equals(fieldName)) {
                    actor = executorService.getActorByCode(subject, ((Long) newValue).longValue());
                } else if ("id".equals(fieldName)) {
                    actor = executorService.getActor(subject, ((Long) newValue).longValue());
                } else {
                    actor = executorService.getActor(subject, (String) newValue);
                }
                newValue = Long.toString(actor.getCode());
            } else if (result.isFieldSetup()) {
                String fieldName = result.getFieldName();
                PropertyUtils.setProperty(variableValue, fieldName, newValue);
                newValue = variableValue;
                // } else if (newValue instanceof Integer){
                // newValue = (new Long(((Integer)newValue).intValue()));
            }
            variables.put(result.getVariableName(), newValue);
        }
    }

    private void fillQueryParameters(ExecutorService executorService, Subject subject, PreparedStatement ps,
            Map<String, Object> variables, AbstractQuery query) throws Exception {
        for (int i = 0; i < query.getParameterCount(); i++) {
            Parameter parameter = query.getParameter(i);
            Object value = variables.get(parameter.getVariableName());
            if (parameter instanceof SwimlaneParameter) {
                Actor actor = executorService.getActorByCode(subject, Long.parseLong((String) value));
                value = PropertyUtils.getProperty(actor, ((SwimlaneParameter) parameter).getFieldName());
            } else if (parameter.isFieldSetup()) {
                value = PropertyUtils.getProperty(value, parameter.getFieldName());
            }
            // TODO dirty hack
            if (value instanceof Date) {
                value = convertDate((Date) value);
            }
            ps.setObject(i + 1, value);
        }
    }

    private Object convertDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(100, 1, 1);
        if (date.before(calendar.getTime())) {
            calendar.setTime(date);
            calendar.set(calendar.get(Calendar.YEAR) + 2000, calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), calendar.get(Calendar.HOUR),
                    calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
            date = calendar.getTime();
        }
        return new Timestamp(date.getTime());
    }
}
