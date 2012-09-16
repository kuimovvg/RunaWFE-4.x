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
package ru.runa.wf.web.html;

import javax.servlet.jsp.JspException;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;

import ru.runa.af.Actor;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.web.Resources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.BaseTDBuilder;

/**
 * Builds table cell for {@link Executor}, executed action.
 */
public class SystemLogActorTDBuilder extends BaseTDBuilder {

    /**
     * Creates component to build table cell for {@link Executor}, executed action.
     */
    public SystemLogActorTDBuilder() {
        super(ExecutorPermission.READ, new SystemLogActorExtractor());
    }

    @Override
    public TD build(Object object, Env env) throws JspException {
        ConcreteElement element;
        Actor actor = (Actor) getExtractor().getIdentifiable(object, env);
        if (actor == null || !isEnabled(object, env)) {
            element = new StringElement(getValue(object, env));
        } else {
            String url = Commons.getActionUrl(Resources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, actor.getId(), env.getPageContext(),
                    PortletUrl.Action);
            element = new A(url, getValue(object, env));
        }
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(element);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        Actor actor = (Actor) getExtractor().getIdentifiable(object, env);
        if (actor == null) {
            return "";
        }
        String name = actor.getName();
        return name == null ? "" : name;
    }
}
