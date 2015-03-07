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
package ru.runa.wfe.definition.cache;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.cache.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.commons.cache.ChangedObjectParameter;
import ru.runa.wfe.commons.cache.ProcessDefChangeListener;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.dao.DeploymentDAO;
import ru.runa.wfe.lang.ProcessDefinition;

public class ProcessDefCacheCtrl extends BaseCacheCtrl<ProcessDefCacheImpl> implements ProcessDefChangeListener {
    @Autowired
    private DeploymentDAO deploymentDAO;

    private ProcessDefCacheCtrl() {
        CachingLogic.registerChangeListener(this);
    }

    @Override
    public ProcessDefCacheImpl buildCache() {
        return new ProcessDefCacheImpl();
    }

    @Override
    public void doOnChange(ChangedObjectParameter changedObject) {
        ProcessDefCacheImpl cache = getCache();
        if (cache == null) {
            return;
        }
        if (changedObject.object instanceof Deployment) {
            cache.onDeploymentChange((Deployment) changedObject.object, changedObject.changeType);
            return;
        }
        throw new InternalApplicationException("Unexpected object " + changedObject.object);
    }

    public ProcessDefinition getDefinition(Long definitionId) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(this).getDefinition(deploymentDAO, definitionId);
    }

    public ProcessDefinition getLatestDefinition(String definitionName) throws DefinitionDoesNotExistException {
        return CachingLogic.getCacheImpl(this).getLatestDefinition(deploymentDAO, definitionName);
    }

    public List<ProcessDefinition> getLatestProcessDefinitions() {
        return CachingLogic.getCacheImpl(this).getLatestProcessDefinitions(deploymentDAO);
    }

}
