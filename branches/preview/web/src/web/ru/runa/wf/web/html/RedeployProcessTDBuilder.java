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

import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.BaseTDBuilder;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.web.Resources;

/**
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class RedeployProcessTDBuilder extends BaseTDBuilder {

    public RedeployProcessTDBuilder() {
        super(ProcessDefinitionPermission.REDEPLOY_DEFINITION);
    }

    public TD build(Object object, Env env) throws JspException {
        ProcessDefinition pd = (ProcessDefinition) object;
        ConcreteElement startLink;

        if (isEnabled(object, env)) {
            startLink = new A(Commons.getActionUrl(Resources.ACTION_MAPPING_REDEPLOY_PROCESS_DEFINITION, IdForm.ID_INPUT_NAME, String.valueOf(pd
                    .getNativeId()), env.getPageContext(), PortletUrl.Action), Messages.getMessage(Messages.LABEL_REDEPLOY_PROCESS_DEFINIION, env
                    .getPageContext()));
        } else {
            startLink = new StringElement();
        }
        TD td = new TD(startLink);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    public String getValue(Object object, Env env) {
        String result = Messages.getMessage(Messages.LABEL_REDEPLOY_PROCESS_DEFINIION, env.getPageContext());
        if (result == null) {
            result = "";
        }
        return result;
    }
}
