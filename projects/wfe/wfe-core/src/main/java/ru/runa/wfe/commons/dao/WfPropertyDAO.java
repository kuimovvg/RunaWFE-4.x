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
package ru.runa.wfe.commons.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DAO for database initialization and variables managing. Creates appropriate
 * tables (drops tables if such tables already exists) and records.
 */
public class WfPropertyDAO extends GenericDAO<WfProperty> {
	private static final Log log = LogFactory.getLog(WfPropertyDAO.class);
	
    private WfProperty get(String fileName, String name) {
        return findFirstOrNull("from WfProperty where fileName = ? and name = ?", fileName, name);
    }

    public String getValue(String fileName, String name) {
    	log.debug("getValue(" + fileName + ", " + name + ")");
        WfProperty property = get(fileName, name);
        if (property == null) {
            return null;
        }
        log.debug("value = " + property.getValue());
        return property.getValue();
    }

    public void setValue(String fileName, String name, String value) {
    	log.debug("setValue(" + fileName + ", " + name + ", " + value + ")");
        WfProperty property = get(fileName, name);
        if (value == null) {
        	if (property != null)
        		delete(property);
        	return;
        }
        if (property == null) {
            create(new WfProperty(fileName, name, value));
        } else {
            property.setValue(value);
        }
    }

    public void clear() {
    	List<WfProperty> list = getAll();
    	for (WfProperty l : list)
    		delete(l);
    }
    
}
