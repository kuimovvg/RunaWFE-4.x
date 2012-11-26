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
package ru.runa.service.wf;

import java.util.List;

import javax.security.auth.Subject;

import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.execution.ProcessDoesNotExistException;
import ru.runa.wfe.execution.SuperProcessExistsException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.graph.view.GraphElementPresentation;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.var.VariableDefinition;

/**
 * Service interface for operations with process definitions.
 */
public interface DefinitionService {

    public WfDefinition deployProcessDefinition(Subject subject, byte[] process, List<String> processType) throws AuthorizationException,
            DefinitionAlreadyExistException, DefinitionArchiveFormatException;

    public WfDefinition redeployProcessDefinition(Subject subject, Long definitionId, byte[] process, List<String> processType)
            throws DefinitionDoesNotExistException, AuthorizationException, DefinitionDoesNotExistException, DefinitionArchiveFormatException,
            DefinitionNameMismatchException;

    public List<WfDefinition> getLatestProcessDefinitions(Subject subject, BatchPresentation batchPresentation) throws AuthorizationException;

    public WfDefinition getLatestProcessDefinition(Subject subject, String definitionName) throws DefinitionDoesNotExistException,
            AuthorizationException, DefinitionDoesNotExistException;

    public WfDefinition getProcessDefinition(Subject subject, Long definitionId) throws DefinitionDoesNotExistException, AuthorizationException,
            DefinitionDoesNotExistException;

    public WfDefinition getProcessDefinitionByProcessId(Subject subject, Long processId) throws AuthorizationException, ProcessDoesNotExistException;

    public void undeployProcessDefinition(Subject subject, String definitionName) throws DefinitionDoesNotExistException, AuthorizationException,
            SuperProcessExistsException;

    public void removeProcessDefinition(Subject subject, String definitionName, int version) throws AuthorizationException,
            DefinitionDoesNotExistException;

    /**
     * Retrieves file data from process definition archieve.
     * 
     * @return file data or <code>null</code> if file does not exist
     */
    public byte[] getFile(Subject subject, Long definitionId, String fileName) throws AuthorizationException, DefinitionDoesNotExistException;

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
    public List<String> getOutputTransitionNames(Subject subject, Long definitionId, Long taskId) throws TaskDoesNotExistException;

    public Interaction getTaskInteraction(Subject subject, Long taskId) throws TaskDoesNotExistException, AuthorizationException;

    public Interaction getStartInteraction(Subject subject, Long definitionId) throws DefinitionDoesNotExistException, AuthorizationException;

    public List<SwimlaneDefinition> getSwimlanes(Subject subject, Long definitionId) throws AuthorizationException, DefinitionDoesNotExistException;

    public List<VariableDefinition> getVariables(Subject subject, Long definitionId) throws AuthorizationException, DefinitionDoesNotExistException;

    public List<GraphElementPresentation> getProcessDefinitionGraphElements(Subject subject, Long definitionId) throws AuthorizationException;

    public List<WfDefinition> getProcessDefinitionHistory(Subject subject, String name) throws AuthorizationException;
}
