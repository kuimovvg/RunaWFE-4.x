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
package ru.runa.wfe.commons.hibernate;

import java.io.Serializable;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.DBType;
import ru.runa.wfe.commons.cache.CachingLogic;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;

public class WFRunaHibernateInterceptor extends EmptyInterceptor {
    private static final long serialVersionUID = 1L;
    public final static String ORACLE_EMPTY_TOKEN = " ";
    private Boolean isOracle; // TODO another way of handling '' values?

    private void onChanges(Object entity, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (entity instanceof Task) {
            CachingLogic.onTaskChange(entity, currentState, previousState, propertyNames, types);
        } else if (entity instanceof Swimlane) {
            CachingLogic.onTaskChange(entity, currentState, previousState, propertyNames, types);
        } else if (entity instanceof Substitution) {
            CachingLogic.onSubstitutionChange(entity, currentState, previousState, propertyNames, types);
        } else if (entity instanceof Executor) {
            CachingLogic.onExecutorChange(entity, currentState, previousState, propertyNames, types);
        } else if (entity instanceof ExecutorGroupMembership) {
            CachingLogic.onExecutorChange(entity, currentState, previousState, propertyNames, types);
        } else if (entity instanceof Deployment) {
            CachingLogic.onProcessDefChange(entity, currentState, previousState, propertyNames, types);
        }
    }

    private boolean isOracle() {
        if (isOracle == null) {
            isOracle = ApplicationContextFactory.getDBType() == DBType.Oracle;
        }
        return isOracle;
    }

    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        boolean isModify = false;
        if (isOracle()) {
            for (int i = 0; i < state.length; ++i) {
                if (state[i] instanceof String) {
                    if (((String) state[i]).length() == 0) {
                        state[i] = ORACLE_EMPTY_TOKEN;
                        isModify = true;
                    }
                }
            }
        }
        onChanges(entity, state, null, propertyNames, types);
        return isModify;
    }

    @Override
    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        onChanges(entity, state, null, propertyNames, types);
    }

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        boolean isModify = false;
        if (isOracle()) {
            for (int i = 0; i < currentState.length; ++i) {
                if (currentState[i] instanceof String) {
                    if (((String) currentState[i]).length() == 0) {
                        currentState[i] = ORACLE_EMPTY_TOKEN;
                        isModify = true;
                    }
                }
            }
        }
        onChanges(entity, currentState, previousState, propertyNames, types);
        return isModify;
    }
}
