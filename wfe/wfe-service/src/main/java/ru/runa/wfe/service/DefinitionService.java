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
package ru.runa.wfe.service;

import java.util.List;

import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ParentProcessExistsException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Service interface for operations with process definitions.
 */
public interface DefinitionService {

    public WfDefinition deployProcessDefinition(User user, byte[] process, List<String> processType) throws DefinitionAlreadyExistException,
            DefinitionArchiveFormatException;

    public WfDefinition redeployProcessDefinition(User user, Long definitionId, byte[] process, List<String> processType)
            throws DefinitionDoesNotExistException, DefinitionDoesNotExistException, DefinitionArchiveFormatException,
            DefinitionNameMismatchException;

    public List<WfDefinition> getLatestProcessDefinitions(User user, BatchPresentation batchPresentation);

    public WfDefinition getLatestProcessDefinition(User user, String definitionName) throws DefinitionDoesNotExistException,
            DefinitionDoesNotExistException;

    public WfDefinition getProcessDefinition(User user, Long definitionId) throws DefinitionDoesNotExistException, DefinitionDoesNotExistException;

    public void undeployProcessDefinition(User user, String definitionName) throws DefinitionDoesNotExistException, ParentProcessExistsException;

    public void removeProcessDefinition(User user, String definitionName, int version) throws DefinitionDoesNotExistException;

    /**
     * Retrieves file data from process definition archieve.
     * 
     * @return file data or <code>null</code> if file does not exist
     */
    public byte[] getFile(User user, Long definitionId, String fileName) throws DefinitionDoesNotExistException;

    /**
     * Gets available output transition names. Process definition id or task id
     * is required.
     * 
     * @param subject
     *            valid subject
     * @param definitionId
     *            process definition id, can be <code>null</code>
     * @param taskId
     *            task id, can be <code>null</code>
     * @return names
     */
    public List<String> getOutputTransitionNames(User user, Long definitionId, Long taskId, boolean withTimerTransitions)
            throws TaskDoesNotExistException;

    public Interaction getTaskInteraction(User user, Long taskId) throws TaskDoesNotExistException;

    public Interaction getStartInteraction(User user, Long definitionId) throws DefinitionDoesNotExistException;

    public List<SwimlaneDefinition> getSwimlanes(User user, Long definitionId) throws DefinitionDoesNotExistException;

    public List<VariableDefinition> getVariables(User user, Long definitionId) throws DefinitionDoesNotExistException;

    public List<GraphElementPresentation> getProcessDefinitionGraphElements(User user, Long definitionId);

    public List<WfDefinition> getProcessDefinitionHistory(User user, String name);
}
