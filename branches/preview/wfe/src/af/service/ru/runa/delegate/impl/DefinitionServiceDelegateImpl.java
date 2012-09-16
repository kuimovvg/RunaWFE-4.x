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
package ru.runa.delegate.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.wf.ProcessDefinitionAlreadyExistsException;
import ru.runa.wf.ProcessDefinitionArchiveException;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionNameMismatchException;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.ProcessInstanceDoesNotExistException;
import ru.runa.wf.SuperProcessInstanceExistsException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.form.VariableDefinition;
import ru.runa.wf.graph.GraphElementPresentation;
import ru.runa.wf.service.DefinitionService;

/**
 * Provides simplified access to local ProcessDefinition. Created on 28.09.2004
 */
public class DefinitionServiceDelegateImpl extends EJB3Delegate implements DefinitionService {
    @Override
    protected String getBeanName() {
        return "DefinitionServiceBean";
    }

    private DefinitionService getDefinitionService() {
        return getService();
    }

    @Override
    public ProcessDefinition deployProcessDefinition(Subject subject, byte[] process, List<String> processType) throws AuthenticationException,
            AuthorizationException, ProcessDefinitionAlreadyExistsException, ProcessDefinitionArchiveException {
        return getDefinitionService().deployProcessDefinition(subject, process, processType);
    }

    @Override
    public ProcessDefinition redeployProcessDefinition(Subject subject, Long processId, byte[] processArchive, List<String> processType)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException, ProcessDefinitionArchiveException,
            ProcessDefinitionNameMismatchException {
        return getDefinitionService().redeployProcessDefinition(subject, processId, processArchive, processType);
    }

    @Override
    public List<ProcessDefinition> getLatestProcessDefinitionStubs(Subject subject, BatchPresentation batchPresentation)
            throws AuthenticationException, AuthorizationException {
        return getDefinitionService().getLatestProcessDefinitionStubs(subject, batchPresentation);
    }

    @Override
    public ProcessDefinition getLatestProcessDefinitionStub(Subject subject, String definitionName) throws AuthenticationException,
            AuthorizationException, ProcessDefinitionDoesNotExistException {
        return getDefinitionService().getLatestProcessDefinitionStub(subject, definitionName);
    }

    @Override
    public ProcessDefinition getProcessDefinitionStub(Subject subject, Long definitionId) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException {
        return getDefinitionService().getProcessDefinitionStub(subject, definitionId);
    }

    @Override
    public ProcessDefinition getProcessDefinitionStubByProcessId(Subject subject, Long processInstanceId) throws AuthenticationException,
            AuthorizationException, ProcessInstanceDoesNotExistException {
        return getDefinitionService().getProcessDefinitionStubByProcessId(subject, processInstanceId);
    }

    @Override
    public void undeployProcessDefinition(Subject subject, String processName) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException, SuperProcessInstanceExistsException {
        getDefinitionService().undeployProcessDefinition(subject, processName);
    }

    @Override
    public void removeProcessDefinition(Subject subject, String definitionName, int version) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException, SecuredObjectOutOfDateException {
        getDefinitionService().removeProcessDefinition(subject, definitionName, version);
    }

    @Override
    public List<String> getOutputTransitionNames(Subject subject, Long definitionId, Long taskId) throws AuthenticationException,
            TaskDoesNotExistException {
        return getDefinitionService().getOutputTransitionNames(subject, definitionId, taskId);
    }

    @Override
    public Interaction getTaskInteraction(Subject subject, Long taskId, String taskName) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        return getDefinitionService().getTaskInteraction(subject, taskId, taskName);
    }

    @Override
    public Interaction getStartInteraction(Subject subject, Long definitionId) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException {
        return getDefinitionService().getStartInteraction(subject, definitionId);
    }

    @Override
    public byte[] getFile(Subject subject, Long definitionId, String fileName) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException {
        return getDefinitionService().getFile(subject, definitionId, fileName);
    }

    @Override
    public Set<String> getAllSwimlanesNamesForAllProcessDefinition(Subject subject) throws AuthenticationException {
        return getDefinitionService().getAllSwimlanesNamesForAllProcessDefinition(subject);
    }

    @Override
    public Map<String, String> getOrgFunctionFriendlyNamesMapping(Subject subject, Long definitionId) throws AuthorizationException,
            AuthenticationException, ProcessDefinitionDoesNotExistException {
        return getDefinitionService().getOrgFunctionFriendlyNamesMapping(subject, definitionId);
    }

    @Override
    public List<VariableDefinition> getProcessDefinitionVariables(Subject subject, Long definitionId) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException {
        return getDefinitionService().getProcessDefinitionVariables(subject, definitionId);
    }

    @Override
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(Subject subject, Long definitionId) throws AuthorizationException,
            AuthenticationException {
        return getDefinitionService().getProcessDefinitionGraphElements(subject, definitionId);
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitionHistory(Subject subject, String name) throws AuthenticationException {
        return getDefinitionService().getProcessDefinitionHistory(subject, name);
    }
}
