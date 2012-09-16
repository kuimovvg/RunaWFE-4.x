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
package ru.runa.af.util;

import java.util.Collection;
import java.util.Map;

public class OracleCommons {
    public static String fixNullString(String orig) {
        if (orig != null) {
            return orig;
        }
        return "";
    }

    public static void fixValuesInMap(Map<String, String> orig) {
        Collection<String> ids = orig.keySet();
        for (String id : ids) {
            if (orig.get(id) == null) {
                orig.put(id, "");
            }
        }
    }
}
