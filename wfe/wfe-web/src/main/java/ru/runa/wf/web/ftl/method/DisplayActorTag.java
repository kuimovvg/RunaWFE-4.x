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

import ru.runa.service.af.ExecutorService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import freemarker.template.TemplateModelException;

public class DisplayActorTag extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        ExecutorService executorService = DelegateFactory.getExecutorService();
        String actorVarName = getParameterAs(String.class, 0);
        try {
            Long code;
            try {
                code = getVariableAs(Long.class, actorVarName, false);
            } catch (TemplateModelException e) {
                String var = getVariableAs(String.class, actorVarName, false);
                if (var.startsWith("G")) {
                    Long groupId = TypeConversionUtil.convertTo(var.substring(1), Long.class);
                    Group group = executorService.getGroup(subject, groupId);
                    return "<p style='color: blue;'>" + group.getName() + "</p>";
                } else {
                    throw e;
                }
            }
            Actor actor = executorService.getActorByCode(subject, code);
            String view = getParameterAs(String.class, 1);
            if ("fullname".equals(view)) {
                return actor.getFullName();
            } else if ("shortname".equals(view)) {
                return actor.getName();
            } else {
                throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
            }
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
    }

}
