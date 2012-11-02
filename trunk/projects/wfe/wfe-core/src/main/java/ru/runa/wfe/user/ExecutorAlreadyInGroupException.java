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
package ru.runa.wfe.user;

import ru.runa.wfe.ApplicationException;

/**
 * Created on 10.08.2004
 * 
 */
public class ExecutorAlreadyInGroupException extends ApplicationException {
    private static final long serialVersionUID = 4591345908128542827L;

    private final String executorName;

    private final String groupName;

    public ExecutorAlreadyInGroupException(String executorName, String groupName) {
        super("Executor " + executorName + " already in group " + groupName);
        this.executorName = executorName;
        this.groupName = groupName;
    }

    public String getExecutorName() {
        return executorName;
    }

    public String getGroupName() {
        return groupName;
    }
}
