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

import ru.runa.af.web.ExecutorNameConverter;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Created on 24.07.2007
 * 
 * @author Konstantinov A.
 */
public class TaskOwnerTDBuilder implements TDBuilder {

    public TaskOwnerTDBuilder() {
    }

    @Override
    public TD build(Object object, Env env) throws JspException {
        WfTask wfTask = (WfTask) object;
        String actorName = ExecutorNameConverter.getName(wfTask.getOwner(), env.getPageContext());
        ConcreteElement link = new StringElement(actorName);
        String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, wfTask.getOwner().getId(),
                env.getPageContext(), PortletUrlType.Render);
        link = new A(url, link);
        TD td = new TD(link);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfTask wfTask = (WfTask) object;
        String result = ExecutorNameConverter.getName(wfTask.getOwner(), env.getPageContext());
        if (result == null) {
            result = "";
        }
        return result;
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
