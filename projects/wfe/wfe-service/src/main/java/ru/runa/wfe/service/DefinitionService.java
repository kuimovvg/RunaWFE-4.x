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
 * Process definition service.
 * 
 * @author Dofs
 * @since 4.0
 */
public interface DefinitionService {

    /**
     * Deploys new process definition.
     * 
     * @param user
     *            authorized user
     * @param archive
     *            process definition archive (ZIP format)
     * @param categories
     *            process categories
     * @return deployed definition
     * @throws DefinitionAlreadyExistException
     * @throws DefinitionArchiveFormatException
     */
    public WfDefinition deployProcessDefinition(User user, byte[] archive, List<String> categories) throws DefinitionAlreadyExistException,
            DefinitionArchiveFormatException;

    /**
     * Redeploys process definition by name.
     * 
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param archive
     *            process definition archive (ZIP format)
     * @param categories
     *            process categories
     * @return redeployed definition
     * @throws DefinitionDoesNotExistException
     * @throws DefinitionDoesNotExistException
     * @throws DefinitionArchiveFormatException
     * @throws DefinitionNameMismatchException
     */
    public WfDefinition redeployProcessDefinition(User user, Long definitionId, byte[] archive, List<String> categories)
            throws DefinitionDoesNotExistException, DefinitionDoesNotExistException, DefinitionArchiveFormatException,
            DefinitionNameMismatchException;

    /**
     * Gets only last version from each process definition.
     * 
     * @param user
     *            authorized user
     * @param batchPresentation
     * @return not <code>null</code>
     */
    public List<WfDefinition> getLatestProcessDefinitions(User user, BatchPresentation batchPresentation);

    /**
     * Gets only last version from process definition by name.
     * 
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     * @throws DefinitionDoesNotExistException
     */
    public WfDefinition getLatestProcessDefinition(User user, String definitionName) throws DefinitionDoesNotExistException,
            DefinitionDoesNotExistException;

    /**
     * Gets process definition by id.
     * 
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     * @throws DefinitionDoesNotExistException
     */
    public WfDefinition getProcessDefinition(User user, Long definitionId) throws DefinitionDoesNotExistException, DefinitionDoesNotExistException;

    /**
     * Deletes process definition by name with all versions and all processes.
     * 
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @throws DefinitionDoesNotExistException
     * @throws ParentProcessExistsException
     */
    public void undeployProcessDefinition(User user, String definitionName) throws DefinitionDoesNotExistException, ParentProcessExistsException;

    /**
     * Deletes process definition by name of specified version.
     * 
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @param version
     * @throws DefinitionDoesNotExistException
     */
    public void removeProcessDefinition(User user, String definitionName, int version) throws DefinitionDoesNotExistException;

    /**
     * Retrieves file data from process definition archive.
     * 
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param fileName
     *            file name in definition archive
     * @return file data or <code>null</code> if file does not exist
     * @throws DefinitionDoesNotExistException
     */
    public byte[] getProcessDefinitionFile(User user, Long definitionId, String fileName) throws DefinitionDoesNotExistException;

    /**
     * Gets available output transition names. Process definition id or task id
     * is required.
     * 
     * @param user
     *            authorized user
     * 
     * @param definitionId
     *            process definition id, can be <code>null</code>
     * @param taskId
     *            task id, can be <code>null</code>
     * @return not <code>null</code>
     */
    public List<String> getOutputTransitionNames(User user, Long definitionId, Long taskId, boolean withTimerTransitions)
            throws TaskDoesNotExistException;

    /**
     * Gets task user interaction.
     * 
     * @param user
     *            authorized user
     * @param taskId
     *            task id
     * @return not <code>null</code>
     * @throws TaskDoesNotExistException
     */
    public Interaction getTaskInteraction(User user, Long taskId) throws TaskDoesNotExistException;

    /**
     * Gets start task user interaction.
     * 
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public Interaction getStartInteraction(User user, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Gets all role definitions for process definition by id.
     * 
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public List<SwimlaneDefinition> getSwimlaneDefinitions(User user, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Gets all variable definitions for process definition by id.
     * 
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @return not <code>null</code>
     * @throws DefinitionDoesNotExistException
     */
    public List<VariableDefinition> getVariableDefinitions(User user, Long definitionId) throws DefinitionDoesNotExistException;

    /**
     * Gets all graph elements for process definition by id.
     * 
     * @param user
     *            authorized user
     * @param definitionId
     *            process definition id
     * @param subprocessId
     *            embedded subprocess id or <code>null</code>
     * @return not <code>null</code>
     */
    public List<GraphElementPresentation> getProcessDefinitionGraphElements(User user, Long definitionId, String subprocessId);

    /**
     * Gets all versions of process definition specified by name.
     * 
     * @param user
     *            authorized user
     * @param definitionName
     *            process definition name
     * @return not <code>null</code>
     */
    public List<WfDefinition> getProcessDefinitionHistory(User user, String definitionName);

}
