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

import org.apache.commons.codec.binary.Base64;

import ru.runa.service.ExecutionService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wf.logic.bot.WebServiceTaskHandler;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * Helper class for XSLT transformation in {@link WebServiceTaskHandler}.
 */
public class WebServiceTaskHandlerXSLTHelper {

    /**
     * Current task, processed by {@link WebServiceTaskHandler}.
     */
    final WfTask task;

    /**
     * Current {@link WebServiceTaskHandler} subject.
     */
    final User user;

    /**
     * Variables, changed by result xslt transformation.
     */
    final Map<String, Object> variables;

    /**
     * Create instance for specified task and subject.
     * 
     * @param task
     *            Current task, processed by {@link WebServiceTaskHandler}.
     * @param user
     *            Current {@link WebServiceTaskHandler} user.
     */
    public WebServiceTaskHandlerXSLTHelper(WfTask task, User user) {
        this.task = task;
        this.user = user;
        variables = new HashMap<String, Object>();
    }

    /**
     * Read variable from current task instance.
     * 
     * @param name
     *            Variable name.
     * @return Variable value converted to string.
     */
    public String getVariable(String name) {
        ExecutionService executionService = Delegates.getExecutionService();
        WfVariable var = executionService.getVariable(user, task.getProcessId(), name);
        if (var.getValue() != null) {
            return var.getValue().toString();
        }
        throw new InternalApplicationException("Can't create SOAP request. WFE variable " + name + " not found");
    }

    /**
     * Read process instance id from variable and returns process instance graph
     * for this process instance encoded in {@link Base64}.
     * 
     * @param processIdVariable
     *            Variable name to read process instance id.
     * @return Process instance graph for this process instance encoded in
     *         {@link Base64}.
     */
    public String getProcessGraph(String processIdVariable) {
        ExecutionService executionService = Delegates.getExecutionService();
        WfVariable var = executionService.getVariable(user, task.getProcessId(), processIdVariable);
        if (var.getValue() != null) {
            return Base64.encodeBase64String(executionService.getProcessDiagram(user, (Long) var.getValue(), null, null));
        }
        throw new InternalApplicationException("Can't create SOAP request. WFE variable " + processIdVariable + " not found");
    }

    /**
     * Add variable to internal storage. You can merge this variables into your
     * storage using MergeVariablesIn call.
     * 
     * @param name
     *            Variable name.
     * @param value
     *            Variable value.
     */
    public void setNewVariable(String name, String value) {
        variables.put(name, value);
    }

    /**
     * Merging variables from internal storage into given map.
     * 
     * @param mergedTo
     *            Storage to merge in variables.
     */
    public void MergeVariablesIn(Map<String, Object> mergedTo) {
        mergedTo.putAll(variables);
    }
}
