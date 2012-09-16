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
package ru.runa.wf.logic.bot.webservice;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;

import jcifs.util.Base64;
import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.TaskStub;
import ru.runa.wf.logic.bot.WebServiceTaskHandler;
import ru.runa.wf.service.ExecutionService;

/**
 * Helper class for XSLT transformation in {@link WebServiceTaskHandler}.
 */
public class WebServiceTaskHandlerXSLTHelper {

    /**
     * Current task, processed by {@link WebServiceTaskHandler}.
     */
    final TaskStub task;

    /**
     * Current {@link WebServiceTaskHandler} subject. 
     */
    final Subject subject;

    /**
     * Variables, changed by result xslt transformation.
     */
    final Map<String, Object> variables;

    /**
     * Create instance for specified task and subject.
     * @param task Current task, processed by {@link WebServiceTaskHandler}.
     * @param subject Current {@link WebServiceTaskHandler} subject. 
     */
    public WebServiceTaskHandlerXSLTHelper(TaskStub task, Subject subject) {
        this.task = task;
        this.subject = subject;
        variables = new HashMap<String, Object>();
    }

    /**
     * Get current task instance id.
     * @return Current task instance id.
     */
    public Long getProcessInstanceId() {
        return task.getId();
    }

    /**
     * Read variable from current task instance.
     * @param name Variable name.
     * @return Variable value converted to string. 
     */
    public String getVariable(String name) throws TaskDoesNotExistException, AuthorizationException, AuthenticationException {
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        Object var = executionService.getVariable(subject, task.getId(), name);
        if (var != null) {
            return var.toString();
        }
        throw new InternalApplicationException("Can't create SOAP request. WFE variable " + name + " not found");
    }

    /**
     * Read process instance id from variable and returns process instance graph for this process instance encoded in {@link Base64}.
     * @param processInstanceIdVariable Variable name to read process instance id.
     * @return Process instance graph for this process instance encoded in {@link Base64}.
     */
    public String getProcessInstanceGraph(String processInstanceIdVariable) throws TaskDoesNotExistException, AuthorizationException,
            AuthenticationException, ProcessInstanceDoesNotExistException {
        ExecutionService executionService = DelegateFactory.getInstance().getExecutionService();
        Object var = executionService.getVariable(subject, task.getId(), processInstanceIdVariable);
        if (var != null) {
            return Base64.encode(executionService.getProcessInstanceDiagram(subject, (Long) var, -1L, 0L));
        }
        throw new InternalApplicationException("Can't create SOAP request. WFE variable " + processInstanceIdVariable + " not found");
    }

    /**
     * Add variable to internal storage. You can merge this variables into your storage using MergeVariablesIn call. 
     * @param name Variable name.
     * @param value Variable value.
     */
    public void setNewVariable(String name, String value) {
        variables.put(name, value);
    }

    /**
     * Merging variables from internal storage into given map. 
     * @param mergedTo Storage to merge in variables.
     */
    public void MergeVariablesIn(Map<String, Object> mergedTo) {
        mergedTo.putAll(variables);
    }
}
