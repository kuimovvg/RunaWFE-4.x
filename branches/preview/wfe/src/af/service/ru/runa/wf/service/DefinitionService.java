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
package ru.runa.wf.service;

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

/**
 * A wrapper for {@link ru.runa.bpm.DefinitionService}.
 * 
 */
public interface DefinitionService {

    public ProcessDefinition deployProcessDefinition(Subject subject, byte[] process, List<String> processType) throws AuthenticationException,
            AuthorizationException, ProcessDefinitionAlreadyExistsException, ProcessDefinitionArchiveException;

    public ProcessDefinition redeployProcessDefinition(Subject subject, Long definitionId, byte[] process, List<String> processType)
            throws ProcessDefinitionDoesNotExistException, AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException,
            ProcessDefinitionArchiveException, ProcessDefinitionNameMismatchException;

    public List<ProcessDefinition> getLatestProcessDefinitionStubs(Subject subject, BatchPresentation batchPresentation)
            throws AuthenticationException, AuthorizationException;

    public ProcessDefinition getLatestProcessDefinitionStub(Subject subject, String definitionName)
            throws ProcessDefinitionDoesNotExistException, AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException;

    public ProcessDefinition getProcessDefinitionStub(Subject subject, Long definitionId) throws ProcessDefinitionDoesNotExistException,
            AuthorizationException, AuthenticationException, ProcessDefinitionDoesNotExistException;

    public ProcessDefinition getProcessDefinitionStubByProcessId(Subject subject, Long processInstanceId) throws AuthenticationException,
            AuthorizationException, ProcessInstanceDoesNotExistException;

    public void undeployProcessDefinition(Subject subject, String definitionName) throws ProcessDefinitionDoesNotExistException,
            AuthenticationException, AuthorizationException, SuperProcessInstanceExistsException;

    public void removeProcessDefinition(Subject subject, String definitionName, int version) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException, SecuredObjectOutOfDateException;

    /**
     * Retrieves file data from process definition archieve.
     * @return file data or <code>null</code> if file does not exist
     */
    public byte[] getFile(Subject subject, Long definitionId, String fileName) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException;

    public List<String> getOutputTransitionNames(Subject subject, Long definitionId, Long taskId) throws AuthenticationException,
            TaskDoesNotExistException;

    public Interaction getTaskInteraction(Subject subject, Long taskId, String taskName) throws TaskDoesNotExistException, AuthorizationException,
            AuthenticationException;

    public Interaction getStartInteraction(Subject subject, Long definitionId) throws ProcessDefinitionDoesNotExistException, AuthorizationException,
            AuthenticationException;

    public Set<String> getAllSwimlanesNamesForAllProcessDefinition(Subject subject) throws AuthenticationException;

    public Map<String, String> getOrgFunctionFriendlyNamesMapping(Subject subject, Long definitionId) throws AuthorizationException,
            AuthenticationException, ProcessDefinitionDoesNotExistException;

    public List<VariableDefinition> getProcessDefinitionVariables(Subject subject, Long definitionId) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException;

    public List<GraphElementPresentation> getProcessDefinitionGraphElements(Subject subject, Long definitionId) throws AuthorizationException,
            AuthenticationException;

    public List<ProcessDefinition> getProcessDefinitionHistory(Subject subject, String name) throws AuthenticationException;
}
