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
package ru.runa.wf.web.ftl.tags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;

import ru.runa.af.Actor;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.Group;
import ru.runa.af.RelationPair;
import ru.runa.af.presentation.AFProfileStrategy;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationConsts;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.RelationService;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.web.ftl.FreemarkerTag;
import freemarker.template.TemplateModelException;

public class RelationResultTag extends FreemarkerTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String varName = getParameterAs(String.class, 0);
        String relationName = getParameterAs(String.class, 1);
        String relationParam = getParameterAs(String.class, 2);
        List<Actor> actors = getActors(relationName, relationParam);
        return getHtml(actors, varName);
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
    private List<Executor> getExecutors(ExecutorService executorService, String param) throws AuthorizationException, AuthenticationException,
            ExecutorOutOfDateException, TemplateModelException {
        Executor executor = null;
        if (executorService.isExecutorExist(subject, param)) {
            executor = executorService.getExecutor(subject, param);
        } else if (!param.toUpperCase().startsWith("G")) {
            executor = executorService.getActorByCode(subject, Long.parseLong(param));
        } else {
            executor = executorService.getExecutor(subject, Long.parseLong(param.substring(1)));
        }
        if (executor == null) {
            throw new TemplateModelException("Failed to load executor");
        }
        List<Executor> result = new ArrayList<Executor>();
        result.add(executor);
        BatchPresentation batchPresentation = AFProfileStrategy.EXECUTOR_DEAFAULT_BATCH_PRESENTATOIN_FACTORY.getDefaultBatchPresentation();
        batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
        result.addAll(executorService.getExecutorGroups(subject, executor, batchPresentation, false));
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
    private List<Actor> getActors(String relationName, String relationParam) throws TemplateModelException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            RelationService relationService = DelegateFactory.getInstance().getRelationService();
            List<Executor> executorRightList = getExecutors(executorService, relationParam);
            List<RelationPair> relationPairList = relationService.getExecutorsRelationPairsRight(subject, relationName, executorRightList);
            HashSet<Actor> result = new HashSet<Actor>();
            for (RelationPair relationPair : relationPairList) {
                Executor executorLeft = relationPair.getLeft();
                try {
                    executorService.getExecutor(subject, executorLeft.getId());
                    if (executorLeft instanceof Actor) {
                        result.add((Actor) executorLeft);
                    } else if (executorLeft instanceof Group) {
                        result.addAll(executorService.getGroupActors(subject, (Group) executorLeft));
                    }
                } catch (AuthorizationException e) {
                    // TODO may be filter executors in logic?
                    // http://sourceforge.net/tracker/?func=detail&aid=3478716&group_id=125156&atid=701698
                }
            }
            return new ArrayList<Actor>(result);
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
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
            Option option = new Option(String.valueOf(actor.getCode())).addElement(actor.getFullName());
            select.addElement(option);
        }
        return select;
    }

    /**
     * Creates html for tag.
     * 
     * @param actors
     *            Actors, available at tag.
     * @param varName
     *            Variable to save chosen actor.
     * @return Tag html.
     */
    private String getHtml(List<Actor> actors, String varName) throws TemplateModelException {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append(createSelect(varName, actors).toString());
        return htmlContent.toString();
    }
}
