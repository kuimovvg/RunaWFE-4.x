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
package ru.runa.af.dao;

import java.util.List;

import ru.runa.af.log.SystemLog;

/**
  * DAO level interface for managing {@linkplain SystemLog}. 
  * @author Konstantinov Aleksey 25.02.2012
  */
public interface SystemLogDAO {

    /**
     * Save {@linkplain SystemLog}. Saving {@linkplain SystemLog} must not be saved before. 
     * @param log {@linkplain SystemLog} to save. 
     */
    void create(SystemLog log);

    /**
     * Load all {@linkplain SystemLog}.
     * @return {@linkplain SystemLog} list.
     */
    List<SystemLog> getAllSystemLogs();
}
