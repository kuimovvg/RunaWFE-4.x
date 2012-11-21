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
package ru.runa.wfe.handler.bot;

import java.util.Map;

import javax.security.auth.Subject;

import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Interface for bot task execution.
 */
public interface TaskHandler {
    /**
     * If this variable assigned to Boolean.TRUE then bot system will not
     * complete task.
     */
    public static final String SKIP_TASK_COMPLETION_VARIABLE_NAME = "skipTaskCompletion";

    /**
     * Configuring bot from database.
     * 
     * @param configuration
     *            Loaded from database configuration.
     */
    public void setConfiguration(byte[] configuration) throws Exception;

    /**
     * Handles task, assigned to bot.
     * 
     * @param subject
     *            Current bot subject.
     * @param variableProvider
     *            access process variables
     * @param taskStub
     *            Task to be processed.
     * @return variables passed to process
     */
    public Map<String, Object> handle(Subject subject, IVariableProvider variableProvider, WfTask taskStub) throws Exception;
}
