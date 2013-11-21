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
package ru.runa.wfe.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.decl.DefinitionServiceLocal;
import ru.runa.wfe.service.decl.DefinitionServiceRemote;
import ru.runa.wfe.service.interceptors.EjbExceptionSupport;
import ru.runa.wfe.service.interceptors.EjbTransactionSupport;
import ru.runa.wfe.service.interceptors.PerformanceObserver;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.base.Preconditions;

@Stateless(name = "DefinitionServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, PerformanceObserver.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "DefinitionAPI", serviceName = "DefinitionWebService")
@SOAPBinding
public class DefinitionServiceBean implements DefinitionServiceLocal, DefinitionServiceRemote {
    @Autowired
    private DefinitionLogic definitionLogic;

    @Override
    @WebResult(name = "result")
    public WfDefinition deployProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "processArchive") byte[] processArchive,
            @WebParam(name = "processTypes") List<String> processTypes) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(processArchive != null);
        Preconditions.checkArgument(processTypes != null);
        return definitionLogic.deployProcessDefinition(user, processArchive, processTypes);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition redeployProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "processId") Long processId,
            @WebParam(name = "processArchive") byte[] processArchive, @WebParam(name = "processTypes") List<String> processTypes) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(processTypes != null);
        return definitionLogic.redeployProcessDefinition(user, processId, processArchive, processTypes);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getLatestProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionName") String definitionName) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionName != null);
        return definitionLogic.getLatestProcessDefinition(user, definitionName);
    }

    @Override
    @WebResult(name = "result")
    public WfDefinition getProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        return definitionLogic.getProcessDefinition(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public List<WfDefinition> getLatestProcessDefinitions(@WebParam(name = "user") User user,
            @WebParam(name = "batchPresentation") BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.DEFINITIONS.createDefault();
        }
        return definitionLogic.getLatestProcessDefinitions(user, batchPresentation);
    }

    @Override
    @WebResult(name = "result")
    public void undeployProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "processName") String processName) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(processName != null);
        definitionLogic.undeployProcessDefinition(user, processName);
    }

    @Override
    @WebResult(name = "result")
    public void removeProcessDefinition(@WebParam(name = "user") User user, @WebParam(name = "definitionName") String definitionName,
            @WebParam(name = "version") int version) {
        Preconditions.checkArgument(user != null);
        throw new RuntimeException("not impl");
    }

    @Override
    @WebResult(name = "result")
    public List<String> getOutputTransitionNames(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId,
            @WebParam(name = "taskId") Long taskId, @WebParam(name = "withTimerTransitions") boolean withTimerTransitions) {
        Preconditions.checkArgument(user != null);
        return definitionLogic.getOutputTransitionNames(user, definitionId, taskId, withTimerTransitions);
    }

    @Override
    @WebResult(name = "result")
    public Interaction getTaskInteraction(@WebParam(name = "user") User user, @WebParam(name = "taskId") Long taskId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(taskId != null);
        return definitionLogic.getInteraction(user, taskId);
    }

    @Override
    @WebResult(name = "result")
    public Interaction getStartInteraction(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        return definitionLogic.getStartInteraction(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public byte[] getProcessDefinitionFile(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId,
            @WebParam(name = "fileName") String fileName) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        Preconditions.checkArgument(fileName != null);
        return definitionLogic.getFile(user, definitionId, fileName);
    }

    @Override
    @WebResult(name = "result")
    public List<SwimlaneDefinition> getSwimlaneDefinitions(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        return definitionLogic.getSwimlanes(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public List<VariableDefinition> getVariableDefinitions(@WebParam(name = "user") User user, @WebParam(name = "definitionId") Long definitionId) {
        return definitionLogic.getProcessDefinitionVariables(user, definitionId);
    }

    @Override
    @WebResult(name = "result")
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(@WebParam(name = "user") User user,
            @WebParam(name = "definitionId") Long definitionId, @WebParam(name = "subprocessId") String subprocessId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        return definitionLogic.getProcessDefinitionGraphElements(user, definitionId, subprocessId);
    }

    @Override
    @WebResult(name = "result")
    public List<WfDefinition> getProcessDefinitionHistory(@WebParam(name = "user") User user, @WebParam(name = "name") String name) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(name != null);
        return definitionLogic.getProcessDefinitionHistory(user, name);
    }
}
