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

import ru.runa.bpm.graph.exe.ProcessInstance;
import ru.runa.bpm.graph.exe.Token;
import ru.runa.bpm.logging.log.ProcessLog;

public class LoggingDAO extends CommonDAO {

    public Map<Token, List<ProcessLog>> findLogsByProcessInstance(Long processInstanceId) {
        Map<Token, List<ProcessLog>> tokenLogs = new HashMap<Token, List<ProcessLog>>();
        ProcessInstance processInstance = getHibernateTemplate().load(ProcessInstance.class, processInstanceId);
        collectTokenLogs(tokenLogs, processInstance.getRootToken());
        return tokenLogs;
    }

    static String FIND_LOGS_BY_TOKEN = "select pl from ru.runa.bpm.logging.log.ProcessLog as pl where pl.token = ? order by pl.index";

    private void collectTokenLogs(Map<Token, List<ProcessLog>> tokenLogs, Token token) {
        List<ProcessLog> logs = getHibernateTemplate().find(FIND_LOGS_BY_TOKEN, token);
        tokenLogs.put(token, logs);
        Map<String, Token> children = token.getChildren();
        if (children != null && !children.isEmpty()) {
            for (Token child : children.values()) {
                collectTokenLogs(tokenLogs, child);
            }
        }
    }

}
