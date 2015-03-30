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
 * aLong with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.definition.cache;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

import ru.runa.wfe.commons.cache.BaseCacheImpl;
import ru.runa.wfe.commons.cache.Cache;
import ru.runa.wfe.commons.cache.Change;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.definition.par.ProcessArchive;
import ru.runa.wfe.lang.ProcessDefinition;

import com.google.common.collect.Lists;

class ProcessDefCacheImpl extends BaseCacheImpl implements ProcessDefinitionCache {

    public static final String definitionIdToDefinitionName = "ru.runa.wfe.definition.cache.definitionIdToDefinition";
    public static final String definitionNameToLatestDefinitionName = "ru.runa.wfe.definition.cache.definitionNameToLatestDefinition";

    private final Cache<Long, ProcessDefinition> definitionIdToDefinition;
    private final Cache<String, Long> definitionNameToId;
    private final List<ProcessDefinition> latestProcessDefinition = Lists.newArrayList();

    public ProcessDefCacheImpl() {
        definitionIdToDefinition = createCache(definitionIdToDefinitionName);
        definitionNameToId = createCache(definitionNameToLatestDefinitionName);
    }

    public void onDeploymentChange(Deployment deployment, Change change) {
        // TODO different calc depending on change
        if (deployment.getId() != null) {
            definitionIdToDefinition.remove(deployment.getId());
        }
        definitionNameToId.remove(deployment.getName());
        latestProcessDefinition.clear();
    }

    @Override
    public ProcessDefinition getDefinition(DeploymentDAO deploymentDAO, Long definitionId) throws DefinitionDoesNotExistException {
        ProcessDefinition processDefinition = null;
        synchronized (this) {
            processDefinition = definitionIdToDefinition.get(definitionId);
            if (processDefinition != null) {
                return processDefinition;
            }
        }
        Deployment deployment = deploymentDAO.getNotNull(definitionId);
        Hibernate.initialize(deployment);
        if (deployment instanceof HibernateProxy) {
            deployment = (Deployment) (((HibernateProxy) deployment).getHibernateLazyInitializer().getImplementation());
        }
        ProcessArchive archive = new ProcessArchive(deployment);
        processDefinition = archive.parseProcessDefinition();
        synchronized (this) {
            definitionIdToDefinition.put(definitionId, processDefinition);
        }
        return processDefinition;
    }

    @Override
    public ProcessDefinition getLatestDefinition(DeploymentDAO deploymentDAO, String definitionName) {
        Long definitionId = null;
        synchronized (this) {
            definitionId = definitionNameToId.get(definitionName);
            if (definitionId != null) {
                return getDefinition(deploymentDAO, definitionId);
            }
        }
        definitionId = deploymentDAO.findLatestDeployment(definitionName).getId();
        synchronized (this) {
            definitionNameToId.put(definitionName, definitionId);
        }
        return getDefinition(deploymentDAO, definitionId);
    }

    @Override
    public List<ProcessDefinition> getLatestProcessDefinitions(DeploymentDAO deploymentDAO) {
        synchronized (this) {
            if (latestProcessDefinition.size() != 0) {
                return latestProcessDefinition;
            }
        }
        List<Deployment> result = deploymentDAO.findLatestDeployments();
        synchronized (this) {
            for (Deployment deployment : result) {
                latestProcessDefinition.add(getDefinition(deploymentDAO, deployment.getId()));
            }
        }
        return latestProcessDefinition;
    }

}
