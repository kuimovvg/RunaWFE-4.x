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

import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;

import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.user.Actor;
import freemarker.template.TemplateModelException;

public class ChooseActorTag extends FreemarkerTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String actorVarName = getParameterAs(String.class, 0);
        String view = getParameterAs(String.class, 1);
        try {
            ExecutorService executorService = DelegateFactory.getExecutorService();
            BatchPresentation batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
            int[] sortIds = { 1 };
            boolean[] sortOrder = { true };
            batchPresentation.setFieldsToSort(sortIds, sortOrder);
            List<Actor> actors = executorService.getActors(subject, batchPresentation);

            if ("all".equals(view)) {
                Select select = new Select();
                select.setName(actorVarName);
                for (Actor actor : actors) {
                    Option option = new Option(String.valueOf(actor.getCode())).addElement(actor.getFullName());
                    select.addElement(option);
                }
                return select.toString();
            } else if ("raw".equals(view)) {
                return actors;
            } else {
                throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
            }
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

}
