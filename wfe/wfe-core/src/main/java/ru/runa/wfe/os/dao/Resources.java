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
package ru.runa.wfe.os.dao;

import ru.runa.wfe.commons.ResourceCommons;

/**
 * Created on 03.01.2006
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m modified by miheev_a 19.05.2006
 */
public class Resources extends ResourceCommons {

    private Resources() {
        super(PROPERTIES);
    }

    private static final String PROPERTIES = "orgfunction.organization_hierarchy_dao";

    private static final String DATA_SOURCE_NAME = "datasource";

    private final static String CHIEF_CODE_BY_SUBORDINATE_CODE_SQL = "chief.code.by.subordinate.code.sql";

    private final static String SUBORDINATE_CODES_BY_CHIEF_CODE_SQL = "subordinate.codes.by.chief.code.sql";

    private final static String GET_ALL_DIRECTORS_CODES = "get.all.directors.codes.sql";

    public static String getChiefCodeBySubordinateCodeSQL() {
        return readProperty(CHIEF_CODE_BY_SUBORDINATE_CODE_SQL, PROPERTIES);
    }

    public static String getAllDirectorsCodes() {
        return readProperty(GET_ALL_DIRECTORS_CODES, PROPERTIES);
    }

    public static String getSubordinateCodesByChiefCodeSQL() {
        return readProperty(SUBORDINATE_CODES_BY_CHIEF_CODE_SQL, PROPERTIES);
    }

    public static String getDataSourceName() {
        return readProperty(DATA_SOURCE_NAME, PROPERTIES);
    }
}
