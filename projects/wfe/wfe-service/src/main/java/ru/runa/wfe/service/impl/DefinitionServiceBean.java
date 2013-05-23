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
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.base.Preconditions;

@Stateless(name = "DefinitionServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
@WebService(name = "DefinitionAPI", serviceName = "DefinitionWebService")
@SOAPBinding
public class DefinitionServiceBean implements DefinitionServiceLocal, DefinitionServiceRemote {
    @Autowired
    private DefinitionLogic definitionLogic;

    @Override
    public WfDefinition deployProcessDefinition(User user, byte[] processArchive, List<String> processType) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(processArchive != null);
        Preconditions.checkArgument(processType != null);
        return definitionLogic.deployProcessDefinition(user, processArchive, processType);
    }

    @Override
    public WfDefinition redeployProcessDefinition(User user, Long processId, byte[] processArchive, List<String> processType) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(processType != null);
        Preconditions.checkArgument(processArchive != null);
        Preconditions.checkArgument(processType != null);
        return definitionLogic.redeployProcessDefinition(user, processId, processArchive, processType);
    }

    @Override
    public WfDefinition getLatestProcessDefinition(User user, String definitionName) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionName != null);
        return definitionLogic.getLatestProcessDefinition(user, definitionName);
    }

    @Override
    public WfDefinition getProcessDefinition(User user, Long definitionId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        return definitionLogic.getProcessDefinition(user, definitionId);
    }

    @Override
    public List<WfDefinition> getLatestProcessDefinitions(User user, BatchPresentation batchPresentation) {
        Preconditions.checkArgument(user != null);
        if (batchPresentation == null) {
            batchPresentation = BatchPresentationFactory.DEFINITIONS.createDefault();
        }
        return definitionLogic.getLatestProcessDefinitions(user, batchPresentation);
    }

    @Override
    public void undeployProcessDefinition(User user, String processName) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(processName != null);
        definitionLogic.undeployProcessDefinition(user, processName);
    }

    @Override
    public void removeProcessDefinition(User user, String definitionName, int version) {
        Preconditions.checkArgument(user != null);
        throw new RuntimeException("not impl");
        // archivingLogic.removeProcessDefinition(user, definitionName,
        // version, 0);
    }

    @Override
    public List<String> getOutputTransitionNames(User user, Long definitionId, Long taskId, boolean withTimerTransitions) {
        Preconditions.checkArgument(user != null);
        return definitionLogic.getOutputTransitionNames(user, definitionId, taskId, withTimerTransitions);
    }

    @Override
    public Interaction getTaskInteraction(User user, Long taskId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(taskId != null);
        return definitionLogic.getInteraction(user, taskId);
    }

    @Override
    public Interaction getStartInteraction(User user, Long definitionId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        return definitionLogic.getStartInteraction(user, definitionId);
    }

    @Override
    public byte[] getFile(User user, Long definitionId, String fileName) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        Preconditions.checkArgument(fileName != null);
        return definitionLogic.getFile(user, definitionId, fileName);
    }

    @Override
    public List<SwimlaneDefinition> getSwimlanes(User user, Long definitionId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        return definitionLogic.getSwimlanes(user, definitionId);
    }

    @Override
    public List<VariableDefinition> getVariables(User user, Long definitionId) {
        return definitionLogic.getProcessDefinitionVariables(user, definitionId);
    }

    @Override
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(User user, Long definitionId) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(definitionId != null);
        return definitionLogic.getProcessDefinitionGraphElements(user, definitionId);
    }

    @Override
    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        Preconditions.checkArgument(user != null);
        Preconditions.checkArgument(name != null);
        return definitionLogic.getProcessDefinitionHistory(user, name);
    }
}
