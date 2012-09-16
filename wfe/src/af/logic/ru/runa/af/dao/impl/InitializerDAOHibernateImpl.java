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
package ru.runa.af.dao.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import ru.runa.af.dao.InitializerDAO;
import ru.runa.commons.ApplicationContextFactory;

/**
 * Initializes AA Database. Creates appropriate tables (drops tables if such tables already exists) and records. 
 */
public class InitializerDAOHibernateImpl extends HibernateDaoSupport implements InitializerDAO {
    private static final Log log = LogFactory.getLog(InitializerDAOHibernateImpl.class);

    @Override
    public void createTables() {
        SchemaExport schemaExport = new SchemaExport(ApplicationContextFactory.getConfiguration());
        schemaExport.create(true, true);
    }
    
    private Constant getConstant(String name) {
        List<Constant> list = getHibernateTemplate().find("select c from Constant c where c.name = ?", name);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public String getValue(String name) {
        try {
            Constant constant = getConstant(name);
            if (constant == null) {
                return null;
            }
            return constant.getValue();
        } catch (Exception e) {
            log.warn("Unable to get constant value", e);
            return null;
        }
    }

    @Override
    public void saveOrUpdateConstant(String name, String value) {
        Constant constant = getConstant(name);
        if (constant == null) {
            constant = new Constant();
            constant.setName(name);
        }
        constant.setValue(value);
        getHibernateTemplate().saveOrUpdate(constant);
    }
    
}
