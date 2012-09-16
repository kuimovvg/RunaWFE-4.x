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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorAlreadyExistsException;
import ru.runa.af.dao.ExecutorDAO;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.wf.presentation.WFProfileStrategy;

/**
 * <p>
 * Created on 20.03.2006 18:00:44
 * </p>
 */
public class DemoSubordinateRecursive {

    private final Log log = LogFactory.getLog(DemoSubordinateRecursive.class);

    /**
     * @param parameters
     *            array of executor ids. Array size must be 1.
     * @throws ExecutorAlreadyExistsException
     */
    public List<Actor> getSubordinateActors(ExecutorDAO executorDAO, Object[] parameters) throws OrganizationFunctionException {
        if (parameters.length != 1) {
            throw new OrganizationFunctionException("Wrong parameters array " + parameters);
        }
        try {
            LinkedList<Actor> list = new LinkedList<Actor>();
            LinkedList<Actor> subordinatesList = new LinkedList<Actor>();
            Actor actor = executorDAO.getActorByCode(Long.parseLong((String) parameters[0]));

            BatchPresentation batchPresentation = WFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
            batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
            List<Executor> executors = executorDAO.getAll(batchPresentation);
            DemoChiefFunction demoChiefFunction = new DemoChiefFunction();
            for (Executor executor : executors) {
                if (executor instanceof Actor) {
                    try {
                        Actor currentActor = (Actor) executor;
                        Object[] currentActorCode = new String[] { new Long(currentActor.getCode()).toString() };
                        List<Long> executorIds = demoChiefFunction.getExecutorIds(currentActorCode);
                        if (executorIds.size() > 0) {
                            list.add(currentActor);
                        }
                    } catch (OrganizationFunctionException e) {
                        log.warn("DemoSubordinateRecursive getSubordinateActors. Chief is not proper defined forActor", e);
                    }
                }
            }

            findDirectSubordinates(list, subordinatesList, actor, demoChiefFunction);
            findIndirectSubordinates(list, subordinatesList, demoChiefFunction);

            return subordinatesList;
        } catch (Exception e) {
            throw new OrganizationFunctionException(e);
        }
    }

    /**
     * @param list
     * @param subordinatesList
     * @param actor
     * @param demoChiefFunction
     * @throws OrganizationFunctionException
     */
    private int findDirectSubordinates(LinkedList<Actor> list, LinkedList<Actor> subordinatesList, Actor actor, DemoChiefFunction demoChiefFunction)
            throws OrganizationFunctionException {
        int result = 0;
        for (ListIterator<Actor> iter = list.listIterator(); iter.hasNext();) {
            Actor acurr = iter.next();

            Object[] currentActorCode = new String[] { new Long(acurr.getCode()).toString() };
            List<Long> executorIds = demoChiefFunction.getExecutorIds(currentActorCode);
            Long chifId = executorIds.get(0);
            if (chifId == actor.getId() && acurr.getId() != chifId) {
                subordinatesList.add(acurr);
                result++;
            }
        }
        return result;
    }

    /**
     * @param list
     * @param subordinatesList
     * @param demoChiefFunction
     * @throws OrganizationFunctionException
     */
    private void findIndirectSubordinates(LinkedList<Actor> list, LinkedList<Actor> subordinatesList, DemoChiefFunction demoChiefFunction)
            throws OrganizationFunctionException {
        int flag = -1;
        while (flag != 0) {
            LinkedList<Actor> newGeneratedSubordinates = new LinkedList<Actor>();
            for (ListIterator<Actor> iter = subordinatesList.listIterator(); iter.hasNext();) {
                findDirectSubordinates(list, newGeneratedSubordinates, iter.next(), demoChiefFunction);
            }
            flag = addNotContainedElements(subordinatesList, newGeneratedSubordinates);
        }
    }

    /**
     * @param subordinatesList
     * @param flag
     * @param newSubordinates
     * @return
     */
    private int addNotContainedElements(LinkedList<Actor> subordinatesList, LinkedList<Actor> newGeneratedSubordinates) {
        int flag = 0;
        for (ListIterator<Actor> iter = newGeneratedSubordinates.listIterator(); iter.hasNext();) {
            Actor acurr = iter.next();
            if (!subordinatesList.contains(acurr)) {
                subordinatesList.add(acurr);
                flag = -1;
            }
        }

        return flag;
    }
}
