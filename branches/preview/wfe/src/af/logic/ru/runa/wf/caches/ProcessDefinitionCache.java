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
package ru.runa.wf.caches;

import java.util.List;
import java.util.Set;

import ru.runa.bpm.db.ProcessDefinitionDAO;
import ru.runa.bpm.graph.def.ExecutableProcessDefinition;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;

/**
 * Interface for process definition cache components.
 */
public interface ProcessDefinitionCache {
    /**
     * Returns {@link ExecutableProcessDefinition} with specified identity.
     * 
     * @param processDefinitionDAO
     *            {@link ProcessDefinitionDAO}, which will be used to load
     *            {@link ExecutableProcessDefinition} from database if it's not in cache.
     * @param definitionId
     *            {@link ExecutableProcessDefinition} identity.
     * @return {@link ExecutableProcessDefinition} with specified identity.
     * @throws ProcessDefinitionDoesNotExistException
     *             {@link ExecutableProcessDefinition} with specified identity doesn't
     *             exists.
     */
    public ExecutableProcessDefinition getDefinition(ProcessDefinitionDAO processDefinitionDAO, Long definitionId)
            throws ProcessDefinitionDoesNotExistException;

    /**
     * Returns {@link ExecutableProcessDefinition} with specified name and latest version.
     * 
     * @param processDefinitionDAO
     *            {@link ProcessDefinitionDAO}, which will be used to load
     *            {@link ExecutableProcessDefinition} from database if it's not in cache.
     * @param definitionName
     *            Name of {@link ExecutableProcessDefinition}
     * @return {@link ExecutableProcessDefinition} with specified name and latest version.
     * @throws ProcessDefinitionDoesNotExistException
     *             {@link ExecutableProcessDefinition} with specified name doesn't exists.
     */
    public ExecutableProcessDefinition getLatestDefinition(ProcessDefinitionDAO processDefinitionDAO, String definitionName)
            throws ProcessDefinitionDoesNotExistException;

    /**
     * Returns all {@link ExecutableProcessDefinition} with latest versions.
     * 
     * @param jbpmContext
     *            {@link JbpmContext}, which will be used to load
     *            {@link ExecutableProcessDefinition} from database if it's not in cache.
     * @return All {@link ExecutableProcessDefinition} with latest versions.
     */
    public List<ExecutableProcessDefinition> getLatestProcessDefinitions(ProcessDefinitionDAO processDefinitionDAO);

    /**
     * Returns all swimlane names, defined in all versions of
     * {@link ExecutableProcessDefinition}.
     * 
     * @param jbpmContext
     *            {@link JbpmContext}, which will be used to load
     *            {@link ExecutableProcessDefinition} from database if it's not in cache.
     * @param processName
     *            Name of {@link ExecutableProcessDefinition}.
     * @return All swimlane names, defined in all versions of
     *         {@link ExecutableProcessDefinition}.
     */
    public Set<String> getSwimlaneNamesForDefinition(ProcessDefinitionDAO processDefinitionDAO, String processName);

    /**
     * Returns {@link ExecutableProcessDefinition} type.
     * 
     * @param processName
     *            Process definition name.
     * @return {@link ExecutableProcessDefinition} type.
     */
    public String[] getDefinitionType(String processName);
}
