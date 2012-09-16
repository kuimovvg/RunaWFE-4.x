/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.bpm.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.bpm.graph.def.ArchievedProcessDefinition;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * are the graph related database operations. TODO: split to BPDefinitionDAO and
 * BPInstanceDAO
 */
public class ProcessDefinitionDAO extends CommonDAO {

    public void deployDefinition(ArchievedProcessDefinition processDefinition, ArchievedProcessDefinition previousLatestVersion) {
        // if there is a current latest process definition
        if (previousLatestVersion != null) {
            // take the next version number
            processDefinition.setVersion(previousLatestVersion.getVersion() + 1);
        } else {
            // start from 1
            processDefinition.setVersion(1L);
        }
        getHibernateTemplate().save(processDefinition);
    }

    /**
     * gets a process definition from the database by the identifier.
     * 
     * @return the referenced process definition or null in case it doesn't
     *         exist.
     */
    public ArchievedProcessDefinition getDefinitionNotNull(Long definitionId) {
        ArchievedProcessDefinition definition = get(ArchievedProcessDefinition.class, definitionId);
        if (definition == null) {
            throw new ProcessDefinitionDoesNotExistException(definitionId);
        }
        return definition;
    }

    /**
     * queries the database for the latest version of a process definition with
     * the given name.
     */
    public ArchievedProcessDefinition findLatestDefinition(String name) {
        ArchievedProcessDefinition definition = findFirstOrNull("from ArchievedProcessDefinition where name=? order by version desc", name);
        if (definition == null) {
            throw new ProcessDefinitionDoesNotExistException(name);
        }
        return definition;
    }

    /**
     * queries the database for the latest version of each process definition.
     * Process definitions are distinct by name.
     */
    public List<ArchievedProcessDefinition> findLatestDefinitions() {
        Map<String, ArchievedProcessDefinition> processDefinitionsByName = new HashMap<String, ArchievedProcessDefinition>();
        List<ArchievedProcessDefinition> allProcessDefinitions = getHibernateTemplate().find(
                "from ArchievedProcessDefinition order by name, version desc");
        // TODO performance?
        for (ArchievedProcessDefinition processDefinition : allProcessDefinitions) {
            String processDefinitionName = processDefinition.getName();
            ArchievedProcessDefinition previous = processDefinitionsByName.get(processDefinitionName);
            if ((previous == null) || (previous.getVersion() < processDefinition.getVersion())) {
                processDefinitionsByName.put(processDefinitionName, processDefinition);
            }
        }
        return Lists.newArrayList(processDefinitionsByName.values());
    }

    /**
     * queries the database for all versions of process definitions with the
     * given name, ordered by version (descending).
     */
    public List<ArchievedProcessDefinition> findAllDefinitionVersions(String name) {
        return getHibernateTemplate().find("from ArchievedProcessDefinition where name=? order by version desc", name);
    }

    public void deleteDefinition(ArchievedProcessDefinition processDefinition) {
        Preconditions.checkNotNull(processDefinition, "processDefinition is null");
        // TODO List<ProcessState> referencingProcessStates =
        // getHibernateTemplate().find("from ru.runa.bpm.graph.node.ProcessState where subProcessDefinition=?",
        // processDefinition);
        // for (ProcessState processState : referencingProcessStates) {
        // processState.setSubProcessDefinition(null);
        // }
        // then delete the process definition
        getHibernateTemplate().delete(processDefinition);
    }

}
