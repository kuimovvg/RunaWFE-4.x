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

import java.util.List;

import javax.security.auth.Subject;

import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;

import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import freemarker.template.TemplateModelException;

public class GroupMembersTag extends FreemarkerTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String varName = getParameterAs(String.class, 0);
        String groupName = getParameterAs(String.class, 1);
        String view = getParameterAs(String.class, 2);

        List<Actor> actors = getActors(subject, groupName);
        if ("all".equals(view)) {
            return getHtml(actors, varName);
        } else if ("raw".equals(view)) {
            return actors;
        } else {
            throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
        }
    }

    private List<Actor> getActors(Subject subject, String groupName) throws TemplateModelException {
        try {
            ExecutorService executorService = DelegateFactory.getExecutorService();
            BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
            batchPresentation.setFieldsToSort(new int[] { 1 }, new boolean[] { true });
            batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
            Group group = executorService.getGroup(subject, groupName);
            return executorService.getGroupActors(subject, group);
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

    protected Select createSelect(String selectName, List<Actor> actors, Actor defaultSelectedActor) {
        Select select = new Select();
        select.setName(selectName);
        for (Actor actor : actors) {
            Option option = new Option(String.valueOf(actor.getCode())).addElement(actor.getFullName());
            select.addElement(option);
            if (defaultSelectedActor.equals(actor)) {
                option.setSelected(true);
            }
        }
        return select;
    }

    public String getHtml(List<Actor> actors, String varName) throws TemplateModelException {
        try {
            StringBuilder htmlContent = new StringBuilder();
            Actor defaultActor = DelegateFactory.getAuthenticationService().getActor(subject);
            htmlContent.append(createSelect(varName, actors, defaultActor).toString());
            return htmlContent.toString();
        } catch (AuthenticationException e) {
            throw new TemplateModelException(e);
        }
    }
}
