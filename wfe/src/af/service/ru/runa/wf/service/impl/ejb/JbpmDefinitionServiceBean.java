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
package ru.runa.wf.service.impl.ejb;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;
import javax.security.auth.Subject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.af.ArgumentsCommons;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.SecuredObjectOutOfDateException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.service.impl.ejb.LoggerInterceptor;
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
import ru.runa.wf.logic.ArchivingLogic;
import ru.runa.wf.logic.JbpmDefinitionLogic;
import ru.runa.wf.service.DefinitionServiceLocal;
import ru.runa.wf.service.DefinitionServiceRemote;

@Stateless(name = "DefinitionServiceBean")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Interceptors({SpringBeanAutowiringInterceptor.class, LoggerInterceptor.class})
public class JbpmDefinitionServiceBean implements DefinitionServiceLocal, DefinitionServiceRemote {
    @Autowired
    private JbpmDefinitionLogic definitionLogic;
    @Autowired
    private ArchivingLogic archivingLogic;

    @Override
    public ProcessDefinition deployProcessDefinition(Subject subject, byte[] processArchive, List<String> processType)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionAlreadyExistsException, ProcessDefinitionArchiveException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(processArchive);
        ArgumentsCommons.checkNotNull(processType);
        return definitionLogic.deployProcessDefinition(subject, processArchive, processType);
    }

    @Override
    public ProcessDefinition redeployProcessDefinition(Subject subject, Long processId, byte[] processArchive, List<String> processType)
            throws AuthenticationException, AuthorizationException, ProcessDefinitionDoesNotExistException, ProcessDefinitionArchiveException,
            ProcessDefinitionNameMismatchException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(processType);
        return definitionLogic.redeployProcessDefinition(subject, processId, processArchive, processType);
    }

    @Override
    public ProcessDefinition getLatestProcessDefinitionStub(Subject subject, String definitionName) throws AuthenticationException,
            AuthorizationException, ProcessDefinitionDoesNotExistException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(definitionName, "Definition name");
        return definitionLogic.getLatestProcessDefinitionStub(subject, definitionName);
    }

    @Override
    public ProcessDefinition getProcessDefinitionStub(Subject subject, Long definitionId) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException {
        ArgumentsCommons.checkNotNull(subject);
        return definitionLogic.getProcessDefinitionStub(subject, definitionId);
    }

    @Override
    public ProcessDefinition getProcessDefinitionStubByProcessId(Subject subject, Long processInstanceId) throws AuthenticationException,
            AuthorizationException, ProcessInstanceDoesNotExistException {
        ArgumentsCommons.checkNotNull(subject);
        return definitionLogic.getProcessDefinitionStubByProcessId(subject, processInstanceId);
    }

    @Override
    public List<ProcessDefinition> getLatestProcessDefinitionStubs(Subject subject, BatchPresentation batchPresentation)
            throws AuthenticationException, AuthorizationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotNull(batchPresentation);
        return definitionLogic.getLatestProcessDefinitionStubs(subject, batchPresentation);
    }

    @Override
    public void undeployProcessDefinition(Subject subject, String processName) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException, SuperProcessInstanceExistsException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(processName, "Process name");
        definitionLogic.undeployProcessDefinition(subject, processName);
    }

    @Override
    public void removeProcessDefinition(Subject subject, String definitionName, int version) throws AuthenticationException, AuthorizationException,
            ProcessDefinitionDoesNotExistException, SecuredObjectOutOfDateException {
        ArgumentsCommons.checkNotNull(subject);
        archivingLogic.removeProcessDefinition(subject, definitionName, version, 0);
    }

    @Override
    public List<String> getOutputTransitionNames(Subject subject, Long definitionId, Long taskId) throws AuthenticationException,
            TaskDoesNotExistException {
        ArgumentsCommons.checkNotNull(subject);
        return definitionLogic.getOutputTransitionNames(subject, definitionId, taskId);
    }

    @Override
    public Interaction getTaskInteraction(Subject subject, Long taskId, String taskName) throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException {
        ArgumentsCommons.checkNotNull(subject);
        return definitionLogic.getInteraction(subject, taskId, taskName);
    }

    @Override
    public Interaction getStartInteraction(Subject subject, Long definitionId) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException {
        ArgumentsCommons.checkNotNull(subject);
        return definitionLogic.getStartInteraction(subject, definitionId);
    }

    @Override
    public byte[] getFile(Subject subject, Long definitionId, String fileName) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException {
        ArgumentsCommons.checkNotNull(subject);
        return definitionLogic.getFile(subject, definitionId, fileName);
    }

    @Override
    public Map<String, String> getOrgFunctionFriendlyNamesMapping(Subject subject, Long definitionId) throws AuthorizationException,
            AuthenticationException, ProcessDefinitionDoesNotExistException {
        ArgumentsCommons.checkNotNull(subject);
        return definitionLogic.getOrgFunctionFriendlyNamesMapping(subject, definitionId);
    }

    @Override
    public Set<String> getAllSwimlanesNamesForAllProcessDefinition(Subject subject) throws AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        return definitionLogic.getAllSwimlanesNamesForAllProcessDefinition(subject);
    }

    @Override
    public List<VariableDefinition> getProcessDefinitionVariables(Subject subject, Long definitionId) throws AuthorizationException, AuthenticationException,
            ProcessDefinitionDoesNotExistException {
        // TODO This method is used by BSHActionHandler
        // subject may be null
        return definitionLogic.getProcessDefinitionVariables(subject, definitionId);
    }

    @Override
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(Subject subject, Long definitionId) throws AuthorizationException,
            AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        return definitionLogic.getProcessDefinitionGraphElements(subject, definitionId);
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitionHistory(Subject subject, String name) throws AuthenticationException {
        ArgumentsCommons.checkNotNull(subject);
        ArgumentsCommons.checkNotEmpty(name, "Definition name");
        return definitionLogic.getProcessDefinitionHistory(subject, name);
    }
}
