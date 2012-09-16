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

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.af.Actor;
import ru.runa.af.ArgumentsCommons;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.TerminatorSubstitution;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.dao.SubstitutionDAO;

import com.google.common.collect.Lists;

/**
 * This function returns executors according to substitution table. Created on 06.02.2006
 */
public class TableSubstitutionFunction implements OrganizationFunction {

    @Autowired
    protected ExecutorDAO executorDAO;
    @Autowired
    private SubstitutionDAO substitutionDAO;

    @Override
    public List<Long> getExecutorIds(Object[] parameters) throws OrganizationFunctionException {
        ArgumentsCommons.checkNotNull(parameters);
        ArgumentsCommons.checkArrayLengthEQ(parameters, 2);
        ArgumentsCommons.checkNotNull(parameters[0]);
        ArgumentsCommons.checkNotNull(parameters[1]);
        ArgumentsCommons.checkType(parameters[0], String.class);
        ArgumentsCommons.checkType(parameters[1], SubstitutionCriteria.class);
        String actorCodeAsString = (String) parameters[0];
        Long actorCode = Long.valueOf(actorCodeAsString);
        return getSusbstitutorIds(actorCode, actorCodeAsString, (SubstitutionCriteria) parameters[1]);
    }

    private List<Long> getSusbstitutorIds(Long actorCode, String actorCodeAsString, SubstitutionCriteria criteria) throws OrganizationFunctionException {
        try {
            Long actorId = executorDAO.getActorByCode(actorCode).getId();
            List<Substitution> substitutions = substitutionDAO.getActorSubstitutions(actorId);
            for (Substitution substitution : substitutions) {
                if (substitution.isEnabled() && evaluateCriteria(criteria, substitution.getCriteria())) {
                    if (substitution instanceof TerminatorSubstitution) {
                        return Lists.newArrayList();
                    }
                    String substitutionOrgFunction = substitution.getSubstitutionOrgFunction();
                    try {
                        List<Long> executorIds = OrgFunctionHelper.evaluateOrgFunction(new HashMap<String, Object>(), substitutionOrgFunction, actorCode);
                        List<Long> actorIds = getActiveActorIds(executorDAO, executorIds);
                        if (actorIds.size() > 0) {
                            return actorIds;
                        }
                    } catch (Exception e) {
                        throw new OrganizationFunctionException(e);
                    }
                }
            }
            return Lists.newArrayList();
        } catch (ExecutorOutOfDateException e) {
            throw new OrganizationFunctionException(e);
        }
    }

    private List<Long> getActiveActorIds(ExecutorDAO executorDAO, List<Long> executorIds) throws ExecutorOutOfDateException {
        List<Actor> actors = executorDAO.getActorsByExecutorIds(executorIds);
        List<Long> activeActorCodes = Lists.newArrayList();
        for (Actor actor : actors) {
            if (actor.isActive()) {
                activeActorCodes.add(actor.getCode());
            }
        }
        return executorDAO.getActorIdsByCodes(activeActorCodes);
    }

    private boolean isStringMatch(String criteria, String matcher) {
        if (matcher == null) {
            return true;
        }
        if (criteria == null) {
            return false;
        }
        return criteria.equals(matcher);
    }

    private boolean evaluateCriteria(SubstitutionCriteria criteriaParameter, SubstitutionCriteria criteria) {
        if (criteria == null) {
            return true;
        } else if (criteriaParameter == null) {
            return false;
        } else {
            return (isStringMatch(criteriaParameter.getName(), criteria.getName()) && isStringMatch(criteriaParameter.getConf(), criteria.getConf()));
        }
    }

}
