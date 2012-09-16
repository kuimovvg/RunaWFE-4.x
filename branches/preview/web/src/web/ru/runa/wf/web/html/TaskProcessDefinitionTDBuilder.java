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

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.wf.ProcessDefinitionDoesNotExistException;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.TaskStub;
import ru.runa.wf.web.Resources;

/**
 * Created on 09.03.2006
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class TaskProcessDefinitionTDBuilder implements TDBuilder {
    public TaskProcessDefinitionTDBuilder() {
    }

    public TD build(Object object, Env env) throws JspException {
        TaskStub task = (TaskStub) object;
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        try {
            String definitionName = getValue(object, env);
            if (env.hasProcessDefinitionPermission(ProcessDefinitionPermission.READ, task.getSuperProcessDefinitionId())) {
                String url = Commons.getActionUrl(Resources.ACTION_MAPPING_MANAGE_DEFINITION, IdForm.ID_INPUT_NAME, String.valueOf(task
                        .getSuperProcessDefinitionId()), env.getPageContext(), PortletUrl.Render);
                A definitionNameLink = new A(url, definitionName);
                td.addElement(definitionNameLink);
            } else {
                // this should never happend, since read permission required to get definition
                addDisabledDefinitionName(td, task.getProcessDefinitionName());
            }
            return td;
        } catch (AuthenticationException e) {
            throw new JspException(e);
        } catch (AuthorizationException e) {
            throw new JspException(e);
        } catch (ProcessDefinitionDoesNotExistException e) {
            throw new JspException(e);
        }
    }

    public String getValue(Object object, Env env) {
        String superProcessName = ((TaskStub) object).getSuperProcessDefinitionName();
        return superProcessName == null ? "" : superProcessName;
    }

    private TD addDisabledDefinitionName(TD td, String name) {
        ConcreteElement nameElement = new StringElement(name);
        td.addElement(nameElement);
        return td;
    }

    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
