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

import java.util.List;

import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.taskmgmt.exe.TaskInstance;
import ru.runa.wf.TaskDoesNotExistException;

public class TaskDAO extends CommonDAO {

    static String FIND_TI_BY_ACTOR = "select ti from ru.runa.bpm.taskmgmt.exe.TaskInstance as ti where ti.assignedActorId = ? and ti.open = true";

    /**
     * get the tasklist for a given actor.
     */
    public List<TaskInstance> findTaskInstances(String actorId) {
        return getHibernateTemplate().find(FIND_TI_BY_ACTOR, actorId);
    }

    static String FIND_TI_BY_PI = "select ti from ru.runa.bpm.taskmgmt.exe.TaskInstance ti where ti.token.processInstance = ? and ti.open = true";

    /**
     * get active taskinstances for a given token.
     */
    public List<TaskInstance> findTaskInstancesByProcessInstance(ProcessInstance processInstance) {
        return getHibernateTemplate().find(FIND_TI_BY_PI, processInstance);
    }

    /**
     * get the task instance for a given task instance-id.
     */
    public TaskInstance getTaskInstanceNotNull(Long taskInstanceId) {
        TaskInstance taskInstance = get(TaskInstance.class, taskInstanceId);
        if (taskInstance == null) {
            throw new TaskDoesNotExistException(taskInstanceId);
        }
        return taskInstance;
    }

}
