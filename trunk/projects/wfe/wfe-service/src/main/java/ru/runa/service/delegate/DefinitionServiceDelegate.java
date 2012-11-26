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
package ru.runa.service.delegate;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.service.wf.DefinitionService;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.SuperProcessExistsException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Provides simplified access to local ProcessDefinition. Created on 28.09.2004
 */
public class DefinitionServiceDelegate extends EJB3Delegate implements DefinitionService {

    public DefinitionServiceDelegate() {
        super(DefinitionService.class);
    }

    private DefinitionService getDefinitionService() {
        return getService();
    }

    @Override
    public WfDefinition deployProcessDefinition(Subject subject, byte[] process, List<String> processType) {
        return getDefinitionService().deployProcessDefinition(subject, process, processType);
    }

    @Override
    public WfDefinition redeployProcessDefinition(Subject subject, Long processId, byte[] processArchive, List<String> processType) {
        return getDefinitionService().redeployProcessDefinition(subject, processId, processArchive, processType);
    }

    @Override
    public List<WfDefinition> getLatestProcessDefinitions(Subject subject, BatchPresentation batchPresentation) throws AuthenticationException,
            AuthorizationException {
        return getDefinitionService().getLatestProcessDefinitions(subject, batchPresentation);
    }

    @Override
    public WfDefinition getLatestProcessDefinition(Subject subject, String definitionName) throws AuthenticationException, AuthorizationException,
            DefinitionDoesNotExistException {
        return getDefinitionService().getLatestProcessDefinition(subject, definitionName);
    }

    @Override
    public WfDefinition getProcessDefinition(Subject subject, Long definitionId) throws AuthenticationException, AuthorizationException,
            DefinitionDoesNotExistException {
        return getDefinitionService().getProcessDefinition(subject, definitionId);
    }

    @Override
    public WfDefinition getProcessDefinitionByProcessId(Subject subject, Long processId) throws AuthenticationException, AuthorizationException,
            ProcessDoesNotExistException {
        return getDefinitionService().getProcessDefinitionByProcessId(subject, processId);
    }

    @Override
    public void undeployProcessDefinition(Subject subject, String processName) throws AuthenticationException, AuthorizationException,
            DefinitionDoesNotExistException, SuperProcessExistsException {
        getDefinitionService().undeployProcessDefinition(subject, processName);
    }

    @Override
    public void removeProcessDefinition(Subject subject, String definitionName, int version) throws AuthenticationException, AuthorizationException,
            DefinitionDoesNotExistException {
        getDefinitionService().removeProcessDefinition(subject, definitionName, version);
    }

    @Override
    public List<String> getOutputTransitionNames(Subject subject, Long definitionId, Long taskId) throws AuthenticationException,
            TaskDoesNotExistException {
        return getDefinitionService().getOutputTransitionNames(subject, definitionId, taskId);
    }

    @Override
    public Interaction getTaskInteraction(Subject subject, Long taskId) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        return getDefinitionService().getTaskInteraction(subject, taskId);
    }

    @Override
    public Interaction getStartInteraction(Subject subject, Long definitionId) throws AuthorizationException, AuthenticationException,
            DefinitionDoesNotExistException {
        return getDefinitionService().getStartInteraction(subject, definitionId);
    }

    @Override
    public byte[] getFile(Subject subject, Long definitionId, String fileName) throws AuthorizationException, AuthenticationException,
            DefinitionDoesNotExistException {
        return getDefinitionService().getFile(subject, definitionId, fileName);
    }

    @Override
    public List<SwimlaneDefinition> getSwimlanes(Subject subject, Long definitionId) throws AuthorizationException, DefinitionDoesNotExistException {
        return getDefinitionService().getSwimlanes(subject, definitionId);
    }

    @Override
    public List<VariableDefinition> getVariables(Subject subject, Long definitionId) throws AuthorizationException, AuthenticationException,
            DefinitionDoesNotExistException {
        return getDefinitionService().getVariables(subject, definitionId);
    }

    @Override
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(Subject subject, Long definitionId) throws AuthorizationException,
            AuthenticationException {
        return getDefinitionService().getProcessDefinitionGraphElements(subject, definitionId);
    }

    @Override
    public List<WfDefinition> getProcessDefinitionHistory(Subject subject, String name) throws AuthenticationException {
        return getDefinitionService().getProcessDefinitionHistory(subject, name);
    }
}
