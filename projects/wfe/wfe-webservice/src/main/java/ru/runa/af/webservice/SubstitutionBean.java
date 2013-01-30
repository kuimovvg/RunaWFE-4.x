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
package ru.runa.af.webservice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;

import ru.runa.WSLoggerInterceptor;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionDoesNotExistException;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;

import com.google.common.collect.Lists;

@Stateless
@WebService(name = "Substitution", targetNamespace = "http://runa.ru/workflow/webservices", serviceName = "SubstitutionWebService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
@Interceptors({ SpringBeanAutowiringInterceptor.class, WSLoggerInterceptor.class })
public class SubstitutionBean {

    private final static String ACTOR_CODE_VARIABLE = "%self_code%";
    private final static String ACTOR_ID_VARIABLE = "%self_id%";
    private final static String ACTOR_NAME_VARIABLE = "%self_name%";

    @Autowired
    private ExecutorLogic executorLogic;
    @Autowired
    private SubstitutionLogic substitutionLogic;

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SubstitutionDescr", namespace = "http://runa.ru/workflow/webservices")
    public static class SubstitutionDescr {
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String orgFunc;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String criteria;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String isEnabled;
        @XmlElement(namespace = "http://runa.ru/workflow/webservices")
        String isFirst;

        public String getOrgFunc() {
            return orgFunc;
        }

        public String getCriteria() {
            return criteria;
        }

        public String getIsEnabled() {
            return isEnabled;
        }

        public String getIsFirst() {
            return isFirst;
        }
    }

    @WebMethod
    public void changeSubstitutions(
            @WebParam(mode = Mode.IN, name = "actorPrincipal", targetNamespace = "http://runa.ru/workflow/webservices") User user,
            @WebParam(mode = Mode.IN, name = "executors", targetNamespace = "http://runa.ru/workflow/webservices") List<String> executors,
            @WebParam(mode = Mode.IN, name = "addedSubstitutions", targetNamespace = "http://runa.ru/workflow/webservices") List<SubstitutionDescr> addedSubstitutionsDescr,
            @WebParam(mode = Mode.IN, name = "deletedSubstitutionsDescr", targetNamespace = "http://runa.ru/workflow/webservices") List<SubstitutionDescr> deletedSubstitutionsDescr)
            throws SubstitutionDoesNotExistException, AuthenticationException, AuthorizationException, ExecutorDoesNotExistException {
        Set<Actor> actors = new HashSet<Actor>();
        for (int i = 0; i < executors.size(); ++i) {
            actors.addAll(getActors(user, executors.get(i)));
        }

        List<Substitution> deleted = getDeletedSubstitution(user, deletedSubstitutionsDescr, actors);
        if (deleted.size() > 0) {
            List<Long> deletedIds = Lists.newArrayListWithExpectedSize(deleted.size());
            for (int i = 0; i < deleted.size(); ++i) {
                deletedIds.add(deleted.get(i).getId());
            }
            substitutionLogic.delete(user, deletedIds);
        }

        addSubstitution(user, addedSubstitutionsDescr, actors);
    }

    private void addSubstitution(User user, List<SubstitutionDescr> addedSubstitutionsDescr, Set<Actor> actors) throws AuthenticationException,
            AuthorizationException, ExecutorDoesNotExistException, SubstitutionDoesNotExistException {
        List<Substitution> firstSub = new ArrayList<Substitution>();
        List<Substitution> lastSub = new ArrayList<Substitution>();
        if (addedSubstitutionsDescr.size() == 0) {
            return;
        }
        for (int i = 0; i < addedSubstitutionsDescr.size(); ++i) {
            String orgFunc = null;
            SubstitutionCriteria criteria = null;
            boolean isEnabled = true;
            boolean isFirst = true;
            String orgFuncParam = addedSubstitutionsDescr.get(i).getOrgFunc();
            String criteriaParam = addedSubstitutionsDescr.get(i).getCriteria();
            String isEnabledParam = addedSubstitutionsDescr.get(i).getIsEnabled();
            String isFirstParam = addedSubstitutionsDescr.get(i).getIsFirst();
            if (orgFuncParam != null && orgFuncParam.trim().length() > 0) {
                orgFunc = orgFuncParam;
            }
            if (criteriaParam != null && criteriaParam.trim().length() > 0) {
                criteria = substitutionLogic.getSubstitutionCriteria(user, Long.parseLong(criteriaParam));
            }
            if (isEnabledParam != null && isEnabledParam.trim().length() > 0) {
                isEnabled = Boolean.parseBoolean(isEnabledParam);
            }
            if (isFirstParam != null && isFirstParam.trim().length() > 0) {
                isFirst = Boolean.parseBoolean(isFirstParam);
            }
            Substitution sub = null;
            if (orgFunc == null) {
                sub = new TerminatorSubstitution();
            } else {
                sub = new Substitution();
                sub.setSubstitutionOrgFunction(orgFunc);
            }
            sub.setCriteria(criteria);
            sub.setEnabled(isEnabled);
            if (isFirst) {
                firstSub.add(sub);
            } else {
                lastSub.add(sub);
            }
        }
        List<Substitution> deletedSubstitutions = new ArrayList<Substitution>();
        List<Substitution> createdSubstitutions = new ArrayList<Substitution>();
        for (Actor actor : actors) {
            List<Substitution> existing = substitutionLogic.getSubstitutions(user, actor.getId());
            if (!firstSub.isEmpty()) {
                for (Substitution sub : existing) {
                    deletedSubstitutions.add(sub);
                }
            }
            int subIdx = 0;
            for (Substitution sub : firstSub) {
                Substitution clone = (Substitution) sub.clone();
                clone.setSubstitutionOrgFunction(tuneOrgFunc(clone.getSubstitutionOrgFunction(), actor));
                clone.setPosition(subIdx++);
                clone.setActorId(actor.getId());
                createdSubstitutions.add(clone);
            }
            for (Substitution sub : existing) {
                Substitution clone = (Substitution) sub.clone();
                clone.setSubstitutionOrgFunction(tuneOrgFunc(clone.getSubstitutionOrgFunction(), actor));
                clone.setPosition(subIdx++);
                clone.setActorId(actor.getId());
                createdSubstitutions.add(clone);
            }
            for (Substitution sub : lastSub) {
                Substitution clone = (Substitution) sub.clone();
                clone.setSubstitutionOrgFunction(tuneOrgFunc(clone.getSubstitutionOrgFunction(), actor));
                clone.setPosition(subIdx++);
                clone.setActorId(actor.getId());
                createdSubstitutions.add(clone);
            }
        }

        for (Substitution substitution : deletedSubstitutions) {
            substitutionLogic.delete(user, substitution);
        }
        for (Substitution substitution : createdSubstitutions) {
            substitutionLogic.create(user, substitution);
        }
    }

    private List<Substitution> getDeletedSubstitution(User user, List<SubstitutionDescr> deletedSubstitutionsDescr, Set<Actor> actors)
            throws AuthenticationException, AuthorizationException, ExecutorDoesNotExistException {
        if (deletedSubstitutionsDescr.size() == 0) {
            return new ArrayList<Substitution>();
        }
        List<Substitution> retVal = new ArrayList<Substitution>();
        SubstitutionCriteria[] criterias = new SubstitutionCriteria[deletedSubstitutionsDescr.size()];
        String[] orgFuncs = new String[deletedSubstitutionsDescr.size()];
        for (int i = 0; i < deletedSubstitutionsDescr.size(); ++i) {
            criterias[i] = null;
            orgFuncs[i] = null;
            String orgFunc = deletedSubstitutionsDescr.get(i).getOrgFunc();
            String criteria = deletedSubstitutionsDescr.get(i).getCriteria();
            if (orgFunc != null && orgFunc.trim().length() > 0) {
                orgFuncs[i] = orgFunc;
            }
            if (criteria != null && criteria.trim().length() > 0) {
                criterias[i] = substitutionLogic.getSubstitutionCriteria(user, Long.parseLong(criteria));
            }
        }

        for (Actor actor : actors) {
            for (Substitution substitution : substitutionLogic.getSubstitutions(user, actor.getId())) {
                for (int i = 0; i < deletedSubstitutionsDescr.size(); ++i) {
                    if (isCriteriaMatch(substitution.getCriteria(), criterias[i])
                            && isStringMatch(substitution.getSubstitutionOrgFunction(), tuneOrgFunc(orgFuncs[i], actor))) {
                        retVal.add(substitution);
                        break;
                    }
                }
            }
        }

        return retVal;
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

    private boolean isCriteriaMatch(SubstitutionCriteria substitutionCriteria, SubstitutionCriteria matcher) {
        if (substitutionCriteria == null && matcher == null) {
            return true;
        } else if (substitutionCriteria == null || matcher == null) {
            return false;
        } else {
            return (isStringMatch(substitutionCriteria.getName(), matcher.getName()) && isStringMatch(substitutionCriteria.getConf(),
                    matcher.getConf()));
        }
    }

    private Set<Actor> getActors(User user, String executorName) throws AuthenticationException, AuthorizationException,
            ExecutorDoesNotExistException {
        Set<Actor> result = new HashSet<Actor>();
        Executor executor = executorLogic.getExecutor(user, executorName);
        if (executor instanceof Actor) {
            result.add((Actor) executor);
        } else {
            result.addAll(executorLogic.getGroupActors(user, (Group) executor));
        }
        return result;
    }

    private String tuneOrgFunc(String orgFunc, Actor self) {
        String retVal = null;
        if (orgFunc == null) {
            return null;
        }
        retVal = orgFunc.replaceAll(ACTOR_CODE_VARIABLE, Long.toString(self.getCode()));
        retVal = retVal.replaceAll(ACTOR_ID_VARIABLE, Long.toString(self.getId()));
        retVal = retVal.replaceAll(ACTOR_NAME_VARIABLE, self.getName());
        return retVal;
    }

}
