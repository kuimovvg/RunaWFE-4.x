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
package ru.runa.af.dbpatch;

import ru.runa.af.SystemExecutors;
import ru.runa.commons.dbpatch.DBPatchBase;

/**
 * Creates system executor for process starter permission. 
 */
public class ProcessStarterExecutorAddition extends DBPatchBase {

    @Override
    public void applyPatch() {
        String isActive = "1";
        if (isPostgreSQL()) {
            isActive = "TRUE";
        }
        long code = ((Number) session.createSQLQuery("SELECT MIN(CODE) FROM EXECUTORS").uniqueResult()).longValue() - 1;
        if (isPostgreSQL()) {
            session.createSQLQuery(
                    "INSERT INTO EXECUTORS (IS_GROUP, ID, NAME, FULL_NAME, VERSION, DESCRIPTION, IS_ACTIVE, CODE) VALUES ('N', nextval('hibernate_sequence'), '"
                            + SystemExecutors.PROCESS_STARTER_NAME + "', '', 1, '" + SystemExecutors.PROCESS_STARTER_DESCRIPTION + "', " + isActive
                            + ", " + code + ")").executeUpdate();
        } else {
            session.createSQLQuery(
                    "INSERT INTO EXECUTORS (IS_GROUP, NAME, FULL_NAME, VERSION, DESCRIPTION, IS_ACTIVE, CODE) VALUES ('N', '"
                            + SystemExecutors.PROCESS_STARTER_NAME + "', '', 1, '" + SystemExecutors.PROCESS_STARTER_DESCRIPTION + "', " + isActive
                            + ", " + code + ")").executeUpdate();
        }
    }
}
