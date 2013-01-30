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
package ru.runa.wf.web.ftl.method;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;

import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import freemarker.template.TemplateModelException;

public class ChooseActorByRelationTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String actorVarName = getParameterAs(String.class, 0);
        String relationName = getParameterAs(String.class, 1);
        Executor relationParam = getParameterAs(Executor.class, 2);
        List<Actor> actors = getActors(relationName, relationParam);
        return createSelect(actorVarName, actors).toString();
    }

    /**
     * Load executors according to tag parameters to apply relations for.
     * 
     * @param executorService
     *            Executor delegate to load data.
     * @param param
     *            Tag parameter - actor code or executor name.
     * @return Executors from right part of relation.
     */
    private List<Executor> getExecutors(Executor param) {
        List<Executor> result = new ArrayList<Executor>();
        result.add(param);
        BatchPresentation batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        result.addAll(Delegates.getExecutorService().getExecutorGroups(user, param, batchPresentation, false));
        return result;
    }

    /**
     * Creates actors list for tag.
     * 
     * @param relationName
     *            Applied relation name.
     * @param relationParam
     *            Actor code or executor name to apply relation.
     * @return Actors list.
     */
    private List<Actor> getActors(String relationName, Executor relationParam) throws TemplateModelException {
        List<Executor> executorRightList = getExecutors(relationParam);
        List<RelationPair> relationPairList = Delegates.getRelationService().getExecutorsRelationPairsRight(user, relationName,
                executorRightList);
        HashSet<Actor> result = new HashSet<Actor>();
        for (RelationPair relationPair : relationPairList) {
            Executor executorLeft = relationPair.getLeft();
            try {
                Delegates.getExecutorService().getExecutor(user, executorLeft.getId());
                if (executorLeft instanceof Actor) {
                    result.add((Actor) executorLeft);
                } else if (executorLeft instanceof Group) {
                    result.addAll(Delegates.getExecutorService().getGroupActors(user, (Group) executorLeft));
                }
            } catch (AuthorizationException e) {
                // TODO may be filter executors in logic?
                // http://sourceforge.net/tracker/?func=detail&aid=3478716&group_id=125156&atid=701698
            }
        }
        return new ArrayList<Actor>(result);
    }

    /**
     * Creates select element for tag.
     * 
     * @param selectName
     *            Variable (select) name.
     * @param actors
     *            Actors, available at created select.
     * @return Select element for tag.
     */
    private Select createSelect(String selectName, List<Actor> actors) {
        Select select = new Select();
        select.setName(selectName);
        for (Actor actor : actors) {
            select.addElement(new Option("ID" + actor.getId()).addElement(actor.getFullName()));
        }
        return select;
    }

}
