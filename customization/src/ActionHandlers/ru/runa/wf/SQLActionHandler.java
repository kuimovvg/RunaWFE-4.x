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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.ConfigurationException;
import ru.runa.af.Actor;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.bpm.graph.def.ActionHandler;
import ru.runa.bpm.graph.exe.ExecutionContext;
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

import com.google.common.base.Charsets;

/**
 * 
 * @author dofs[197@gmail.com]
 */
public class SQLActionHandler implements ActionHandler {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(SQLActionHandler.class);

    private String configuration;
    @Autowired
    private ExecutorDAO executorDAO;

    @Override
    public void setConfiguration(String configuration) throws ConfigurationException {
        this.configuration = configuration;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            log.info("SQLActionHandler started");
            Map<String, Object> in = executionContext.getContextInstance().getVariables();
            DatabaseTask[] databaseTasks = DatabaseTaskXmlParser.parse(configuration.getBytes(Charsets.UTF_8), in);
            in.put(DatabaseTask.INSTANCE_ID_VARIABLE_NAME, executionContext.getToken().getProcessInstance().getId());
            in.put(DatabaseTask.CURRENT_DATE_VARIABLE_NAME, new Date());
            log.debug("all variables: " + in);
            Map<String, Object> out = new HashMap<String, Object>();
            Context context = new InitialContext();
            for (int i = 0; i < databaseTasks.length; i++) {
                Connection conn = null;
                try {
                    DatabaseTask databaseTask = databaseTasks[i];
                    PreparedStatement ps = null;
                    DataSource ds = (DataSource) context.lookup(databaseTask.getDatasourceName());
                    conn = ds.getConnection();
                    for (int j = 0; j < databaseTask.getQueriesCount(); j++) {
                        AbstractQuery query = databaseTask.getQuery(j);
                        if (query instanceof Query) {
                            log.debug("Preparing query " + query.getSql());
                            ps = conn.prepareStatement(query.getSql());
                        } else if (query instanceof StoredProcedureQuery) {
                            log.debug("Preparing call " + query.getSql());
                            ps = conn.prepareCall(query.getSql());
                        } else {
                            String unknownQueryClassName = (query == null ? "null" : query.getClass().getName());
                            throw new Exception("Unknown query type:" + unknownQueryClassName);
                        }
                        fillQueryParameters(ps, in, query);
                        if (ps.execute()) {
                            ResultSet resultSet = ps.getResultSet();
                            if (!resultSet.next()) {
                                throw new Exception("No results in rowset for query " + query);
                            }
                            out.putAll(extractResultsToProcessVariables(in, resultSet, query));
                        }
                    }
                } finally {
                    SQLCommons.releaseResources(conn);
                }
            }
            // write variables
            for (String variableName : out.keySet()) {
                executionContext.setVariable(variableName, out.get(variableName));
            }
            log.info("SQLActionHandler finished");
        } catch (Exception e) {
            log.error("SQLActionHandler failed", e);
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> extractResultsToProcessVariables(Map<String, Object> in, ResultSet resultSet,
            AbstractQuery query) throws Exception {
        Map<String, Object> out = new HashMap<String, Object>();
        for (int i = 0; i < query.getResultVariableCount(); i++) {
            Result result = query.getResultVariable(i);
            String fieldName = result.getFieldName();
            Object newValue = resultSet.getObject(i + 1);
            log.debug("Obtaining result " + fieldName + " from " + newValue);
            if (result instanceof SwimlaneResult) {
                Actor actor = null;
                if ("code".equals(fieldName)) {
                    actor = executorDAO.getActorByCode(((Number) newValue).longValue());
                } else if ("id".equals(fieldName)) {
                    actor = executorDAO.getActor(((Number) newValue).longValue());
                } else {
                    actor = executorDAO.getActor(newValue.toString());
                }
                newValue = Long.toString(actor.getCode());
            } else if (result.isFieldSetup()) {
                Object variableValue = in.get(result.getVariableName());
                PropertyUtils.setProperty(variableValue, fieldName, newValue);
                newValue = variableValue;
            }
            out.put(result.getVariableName(), newValue);
        }
        return out;
    }

    private void fillQueryParameters(PreparedStatement ps, Map<String, Object> in, AbstractQuery query) throws Exception {
        for (int i = 0; i < query.getParameterCount(); i++) {
            Parameter parameter = query.getParameter(i);
            Object value = in.get(parameter.getVariableName());
            if (parameter instanceof SwimlaneParameter) {
                Actor actor = executorDAO.getActorByCode(Long.parseLong((String) value));
                value = PropertyUtils.getProperty(actor, ((SwimlaneParameter) parameter).getFieldName());
            } else if (parameter.isFieldSetup()) {
                value = PropertyUtils.getProperty(value, parameter.getFieldName());
            }
            if (value instanceof Date) {
                value = convertDate((Date) value);
            }
            int paramIndex = i + 1;
            log.debug("Setting parameter " + paramIndex + " to (" + parameter.getVariableName() + ") = " + value);
            ps.setObject(paramIndex, value);
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
