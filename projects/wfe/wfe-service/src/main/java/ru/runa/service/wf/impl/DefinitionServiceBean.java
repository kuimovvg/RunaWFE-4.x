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
import javax.security.auth.Subject;

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
import ru.runa.wfe.var.VariableDefinition;

import com.google.common.base.Preconditions;

@Stateless(name = "DefinitionServiceBean")
@TransactionManagement(TransactionManagementType.BEAN)
@Interceptors({ EjbExceptionSupport.class, EjbTransactionSupport.class, SpringBeanAutowiringInterceptor.class })
public class DefinitionServiceBean implements DefinitionServiceLocal, DefinitionServiceRemote {
    @Autowired
    private DefinitionLogic definitionLogic;

    @Override
    public WfDefinition deployProcessDefinition(Subject subject, byte[] processArchive, List<String> processType) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(processArchive);
        Preconditions.checkNotNull(processType);
        return definitionLogic.deployProcessDefinition(subject, processArchive, processType);
    }

    @Override
    public WfDefinition redeployProcessDefinition(Subject subject, Long processId, byte[] processArchive, List<String> processType) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(processType);
        return definitionLogic.redeployProcessDefinition(subject, processId, processArchive, processType);
    }

    @Override
    public WfDefinition getLatestProcessDefinition(Subject subject, String definitionName) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getLatestProcessDefinition(subject, definitionName);
    }

    @Override
    public WfDefinition getProcessDefinition(Subject subject, Long definitionId) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getProcessDefinition(subject, definitionId);
    }

    @Override
    public WfDefinition getProcessDefinitionByProcessId(Subject subject, Long processId) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getProcessDefinitionByProcessId(subject, processId);
    }

    @Override
    public List<WfDefinition> getLatestProcessDefinitions(Subject subject, BatchPresentation batchPresentation) {
        Preconditions.checkNotNull(subject);
        Preconditions.checkNotNull(batchPresentation);
        return definitionLogic.getLatestProcessDefinitions(subject, batchPresentation);
    }

    @Override
    public void undeployProcessDefinition(Subject subject, String processName) {
        Preconditions.checkNotNull(subject);
        definitionLogic.undeployProcessDefinition(subject, processName);
    }

    @Override
    public void removeProcessDefinition(Subject subject, String definitionName, int version) {
        Preconditions.checkNotNull(subject);
        // archivingLogic.removeProcessDefinition(subject, definitionName,
        // version, 0);
    }

    @Override
    public List<String> getOutputTransitionNames(Subject subject, Long definitionId, Long taskId) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getOutputTransitionNames(subject, definitionId, taskId);
    }

    @Override
    public Interaction getTaskInteraction(Subject subject, Long taskId) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getInteraction(subject, taskId);
    }

    @Override
    public Interaction getStartInteraction(Subject subject, Long definitionId) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getStartInteraction(subject, definitionId);
    }

    @Override
    public byte[] getFile(Subject subject, Long definitionId, String fileName) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getFile(subject, definitionId, fileName);
    }

    @Override
    public List<SwimlaneDefinition> getSwimlanes(Subject subject, Long definitionId) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getSwimlanes(subject, definitionId);
    }

    @Override
    public List<VariableDefinition> getVariables(Subject subject, Long definitionId) {
        return definitionLogic.getProcessDefinitionVariables(subject, definitionId);
    }

    @Override
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(Subject subject, Long definitionId) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getProcessDefinitionGraphElements(subject, definitionId);
    }

    @Override
    public List<WfDefinition> getProcessDefinitionHistory(Subject subject, String name) {
        Preconditions.checkNotNull(subject);
        return definitionLogic.getProcessDefinitionHistory(subject, name);
    }
}
