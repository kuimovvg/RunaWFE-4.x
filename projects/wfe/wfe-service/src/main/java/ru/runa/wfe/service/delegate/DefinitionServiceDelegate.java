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
package ru.runa.wfe.service.delegate;

import java.util.List;

import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.user.User;
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
    public WfDefinition deployProcessDefinition(User user, byte[] process, List<String> processType) {
        return getDefinitionService().deployProcessDefinition(user, process, processType);
    }

    @Override
    public WfDefinition redeployProcessDefinition(User user, Long processId, byte[] processArchive, List<String> processType) {
        return getDefinitionService().redeployProcessDefinition(user, processId, processArchive, processType);
    }

    @Override
    public List<WfDefinition> getLatestProcessDefinitions(User user, BatchPresentation batchPresentation) {
        return getDefinitionService().getLatestProcessDefinitions(user, batchPresentation);
    }

    @Override
    public WfDefinition getLatestProcessDefinition(User user, String definitionName) {
        return getDefinitionService().getLatestProcessDefinition(user, definitionName);
    }

    @Override
    public WfDefinition getProcessDefinition(User user, Long definitionId) {
        return getDefinitionService().getProcessDefinition(user, definitionId);
    }

    @Override
    public void undeployProcessDefinition(User user, String processName) {
        getDefinitionService().undeployProcessDefinition(user, processName);
    }

    @Override
    public void removeProcessDefinition(User user, String definitionName, int version) {
        getDefinitionService().removeProcessDefinition(user, definitionName, version);
    }

    @Override
    public List<String> getOutputTransitionNames(User user, Long definitionId, Long taskId, boolean withTimerTransitions) {
        return getDefinitionService().getOutputTransitionNames(user, definitionId, taskId, withTimerTransitions);
    }

    @Override
    public Interaction getTaskInteraction(User user, Long taskId) {
        return getDefinitionService().getTaskInteraction(user, taskId);
    }

    @Override
    public Interaction getStartInteraction(User user, Long definitionId) {
        return getDefinitionService().getStartInteraction(user, definitionId);
    }

    @Override
    public byte[] getFile(User user, Long definitionId, String fileName) {
        return getDefinitionService().getFile(user, definitionId, fileName);
    }

    @Override
    public List<SwimlaneDefinition> getSwimlanes(User user, Long definitionId) {
        return getDefinitionService().getSwimlanes(user, definitionId);
    }

    @Override
    public List<VariableDefinition> getVariables(User user, Long definitionId) {
        return getDefinitionService().getVariables(user, definitionId);
    }

    @Override
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(User user, Long definitionId) {
        return getDefinitionService().getProcessDefinitionGraphElements(user, definitionId);
    }

    @Override
    public List<WfDefinition> getProcessDefinitionHistory(User user, String name) {
        return getDefinitionService().getProcessDefinitionHistory(user, name);
    }
}
