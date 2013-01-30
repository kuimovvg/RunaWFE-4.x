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
package ru.runa.service.wf.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.service.interceptors.EjbExceptionSupport;
import ru.runa.service.interceptors.EjbTransactionSupport;
import ru.runa.service.wf.DefinitionServiceLocal;
import ru.runa.service.wf.DefinitionServiceRemote;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.definition.logic.DefinitionLogic;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.base.Preconditions;

@Stateless(name = "DefinitionServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class DefinitionServiceBean implements DefinitionServiceLocal, DefinitionServiceRemote {
    @Autowired
    private DefinitionLogic definitionLogic;

    @Override
    public WfDefinition deployProcessDefinition(User user, byte[] processArchive, List<String> processType) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(processArchive);
        Preconditions.checkNotNull(processType);
        return definitionLogic.deployProcessDefinition(user, processArchive, processType);
    }

    @Override
    public WfDefinition redeployProcessDefinition(User user, Long processId, byte[] processArchive, List<String> processType) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(processType);
        return definitionLogic.redeployProcessDefinition(user, processId, processArchive, processType);
    }

    @Override
    public WfDefinition getLatestProcessDefinition(User user, String definitionName) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getLatestProcessDefinition(user, definitionName);
    }

    @Override
    public WfDefinition getProcessDefinition(User user, Long definitionId) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getProcessDefinition(user, definitionId);
    }

    @Override
    public WfDefinition getProcessDefinitionByProcessId(User user, Long processId) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getProcessDefinitionByProcessId(user, processId);
    }

    @Override
    public List<WfDefinition> getLatestProcessDefinitions(User user, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(batchPresentation);
        return definitionLogic.getLatestProcessDefinitions(user, batchPresentation);
    }

    @Override
    public void undeployProcessDefinition(User user, String processName) {
        Preconditions.checkNotNull(user);
        definitionLogic.undeployProcessDefinition(user, processName);
    }

    @Override
    public void removeProcessDefinition(User user, String definitionName, int version) {
        Preconditions.checkNotNull(user);
        // archivingLogic.removeProcessDefinition(user, definitionName,
        // version, 0);
    }

    @Override
    public List<String> getOutputTransitionNames(User user, Long definitionId, Long taskId) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getOutputTransitionNames(user, definitionId, taskId);
    }

    @Override
    public Interaction getTaskInteraction(User user, Long taskId) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getInteraction(user, taskId);
    }

    @Override
    public Interaction getStartInteraction(User user, Long definitionId) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getStartInteraction(user, definitionId);
    }

    @Override
    public byte[] getFile(User user, Long definitionId, String fileName) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getFile(user, definitionId, fileName);
    }

    @Override
    public List<SwimlaneDefinition> getSwimlanes(User user, Long definitionId) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getSwimlanes(user, definitionId);
    }

    @Override
    public List<VariableDefinition> getVariables(User user, Long definitionId) {
        return definitionLogic.getProcessDefinitionVariables(user, definitionId);
    }

    @Override
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(User user, Long definitionId) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getProcessDefinitionGraphElements(user, definitionId);
    }

    @Override
    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        Preconditions.checkNotNull(user);
        return definitionLogic.getProcessDefinitionHistory(user, name);
    }
}
