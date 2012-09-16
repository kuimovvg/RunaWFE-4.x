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

/**
 * 
 * Created on 08.01.2007
 * 
 */
public abstract class BaseOrganizationHierarchyFunction implements OrganizationFunction {

    @Autowired
    protected ExecutorDAO executorDAO;

    @Override
    public List<Long> getExecutorIds(Object[] parameters) throws OrganizationFunctionException {
        ArgumentsCommons.checkArrayLengthEQ(parameters, 1);
        try {
            Long code = Long.valueOf((String) parameters[0]);
            List<Long> codes = getCodes(code);
            return executorDAO.getActorIdsByCodes(codes);
        } catch (Exception e) {
            throw new OrganizationFunctionException(e);
        }
    }

    protected abstract List<Long> getCodes(Long code);
    
}
