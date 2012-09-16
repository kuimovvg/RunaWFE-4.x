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

import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.Actor;
import ru.runa.af.ArgumentsCommons;
import ru.runa.af.Group;
import ru.runa.af.dao.ExecutorDAO;

import com.google.common.collect.Lists;

/**
 * 
 * Created on 19.05.2005
 */
public class DemoChiefFunction implements OrganizationFunction {
    @Autowired
    protected ExecutorDAO executorDAO;

    @Override
    public List<Long> getExecutorIds(Object[] parameters) throws OrganizationFunctionException {
        ArgumentsCommons.checkArrayLengthEQ(parameters, 1);
        try {
            Actor actor = executorDAO.getActorByCode(Long.parseLong((String) parameters[0]));
            Enumeration<String> patternsEnumeration = DemoChiefResources.getPatterns();
            String chiefName = null;
            while (patternsEnumeration.hasMoreElements()) {
                String pattern = patternsEnumeration.nextElement();
                if (Pattern.matches(pattern, actor.getName())) {
                    chiefName = DemoChiefResources.getChiefName(pattern);
                    break;
                }
                if (executorDAO.isExecutorExist(pattern)) {
                    Group group = executorDAO.getGroup(pattern);
                    if (executorDAO.isExecutorInGroup(actor, group)) {
                        chiefName = DemoChiefResources.getChiefName(pattern);
                        break;
                    }
                }
            }
            if (chiefName == null) {
                throw new OrganizationFunctionException("Wrong parameters" + parameters[0]);
            }
            return Lists.newArrayList(executorDAO.getActor(chiefName).getId());
        } catch (Exception e) {
            throw new OrganizationFunctionException(e);
        }
    }
}
