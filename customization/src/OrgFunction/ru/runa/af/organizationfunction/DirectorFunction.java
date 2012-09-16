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
package ru.runa.af.organizationfunction;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.ArgumentsCommons;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.organizationfunction.dao.OrganizationHierarchyDAO;
import ru.runa.af.organizationfunction.dao.Resources;

/**
 * Created on 19.05.2006 10:35:40
 */
public class DirectorFunction implements OrganizationFunction {
    
    @Autowired
    protected ExecutorDAO executorDAO;

    @Override
    public List<Long> getExecutorIds(Object[] parameters) throws OrganizationFunctionException {
        ArgumentsCommons.checkArrayLengthEQ(parameters, 1);
        try {
            Long actorCode = Long.parseLong((String) parameters[0]);
            List<Long> codes = OrganizationHierarchyDAO.getDirectorCode(Resources.getAllDirectorsCodes(), Resources.getChiefCodeBySubordinateCodeSQL(),
                    actorCode);
            return executorDAO.getActorIdsByCodes(codes);
        } catch (Exception e) {
            throw new OrganizationFunctionException(e);
        }
    }

}
