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
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.html.BaseTDBuilder;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.ProcessDefinition;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.web.Resources;
import ru.runa.wf.web.action.StartDisabledImageProcessInstanceAction;
import ru.runa.wf.web.action.StartImageProcessInstanceAction;

/**
 * @author Gordienko_m
 * @author Vitaliy S
 */
public class StartProcessTDBuilder extends BaseTDBuilder {

    public StartProcessTDBuilder() {
        super(ProcessDefinitionPermission.START_PROCESS);
    }

    public TD build(Object object, Env env) throws JspException {
        ProcessDefinition pd = (ProcessDefinition) object;
        ConcreteElement startLink;

        String href;
        if (isEnabled(object, env)) {
            if (pd.hasStartImage()) {
                href = Commons.getActionUrl(StartImageProcessInstanceAction.ACTION_PATH, IdForm.ID_INPUT_NAME, String.valueOf(pd.getNativeId()), env
                        .getPageContext(), PortletUrl.Resource);
            } else {
                href = Commons.getUrl(Resources.START_INSTANCE_IMAGE, env.getPageContext(), PortletUrl.Resource);
            }
        } else {
            if (pd.hasDisabledImage()) {
                href = Commons.getActionUrl(StartDisabledImageProcessInstanceAction.ACTION_PATH, IdForm.ID_INPUT_NAME, String.valueOf(pd
                        .getNativeId()), env.getPageContext(), PortletUrl.Resource);
            } else {
                href = Commons.getUrl(Resources.START_INSTANCE_DISABLED_IMAGE, env.getPageContext(), PortletUrl.Resource);
            }
        }
        IMG startImg = new IMG(href);
        String startMessage = Messages.getMessage(Messages.LABEL_START_INSTANCE, env.getPageContext());
        startImg.setAlt(startMessage);
        startImg.setTitle(startMessage);
        startImg.setBorder(0);
        if (isEnabled(object, env)) {
            String url = new DefinitionUrlStrategy(env.getPageContext()).getUrl(Resources.ACTION_MAPPING_START_INSTANCE, pd);
            startLink = new A(url).addElement(startImg);
            if (ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_INSTANCE_PARAMETER)
                    || ConfirmationPopupHelper.getInstance().isEnabled(ConfirmationPopupHelper.START_INSTANCE_FORM_PARAMETER)) {
                DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
                try {
                    String actionParameter = null;
                    if (!(definitionService.getStartInteraction(env.getSubject(), pd.getNativeId()).hasFile() || definitionService.getOutputTransitionNames(
                            env.getSubject(), pd.getNativeId(), null).size() > 1)) {
                        actionParameter = ConfirmationPopupHelper.START_INSTANCE_FORM_PARAMETER;
                        startLink.addAttribute("onclick", ConfirmationPopupHelper.getInstance().getConfirmationPopupCodeHTML(actionParameter,
                                env.getPageContext()));
                    }
                } catch (Exception e) {
                    throw new JspException(e);
                }
            }
        } else {
            startLink = new StringElement().addElement(startImg);
        }
        TD td = new TD(startLink);
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    public String getValue(Object object, Env env) {
        ProcessDefinition pd = (ProcessDefinition) object;
        return String.valueOf(pd.getNativeId());
    }
}
