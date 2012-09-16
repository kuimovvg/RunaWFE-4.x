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
package ru.runa.wf.web.tag;

import java.util.Arrays;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.xhtml.br;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Permission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.wf.ProcessDefinitionPermission;
import ru.runa.wf.web.ProcessTypesIterator;
import ru.runa.wf.web.action.RedeployProcessDefinitionAction;
import ru.runa.wf.web.html.DefinitionFileOperationsFormBuilder;

/**
 * Created on 18.08.2004
 * 
 * @jsp.tag name = "redeployDefinitionForm" body-content = "empty"
 */
public class RedeployDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    private static final long serialVersionUID = 5106903896165128752L;

    public static Select getTypeSelectElement(String[] definitionType, Subject subject, PageContext pageContext) throws AuthorizationException,
            AuthenticationException {
        ProcessTypesIterator iter = new ProcessTypesIterator(subject);
        String selectedIdx = definitionType == null ? "_default_type_" : null;
        Map<String, String> attr = (Map<String, String>) pageContext.getRequest().getAttribute("TypeAttributes");
        if (attr != null) {
            selectedIdx = attr.get("typeSel");
        }

        Select select = new Select("typeSel");
        {
            Option option = new Option();
            option.addElement(Messages.getMessage("batch_presentation.process_instance.no_type", pageContext));
            option.setValue("_default_type_");
            if ("_default_type_".equals(selectedIdx)) {
                option.setSelected(true);
            }
            select.addElement(option);
        }
        int idx = 0;
        while (iter.hasNext()) {
            String[] type = iter.next();

            StringBuilder typeBuild = new StringBuilder();
            for (int i = 1; i < type.length; ++i) {
                typeBuild.append("&nbsp;&nbsp;&nbsp;");
            }
            typeBuild.append(type[type.length - 1]);
            Option option = new Option();
            option.setValue(Integer.toString(idx));
            option.addElement(typeBuild.toString());
            if ((selectedIdx == null && Arrays.equals(type, definitionType)) || Integer.toString(idx).equals(selectedIdx)) {
                option.setSelected(true);
            }
            select.addElement(option);
            ++idx;
        }
        return select;
    }

    public static void fillTD(TD tdFormElement, Form form, String[] definitionType, Subject subject, PageContext pageContext) {
        DefinitionFileOperationsFormBuilder.displayTable(form, tdFormElement);

        tdFormElement.addElement(new br());
        tdFormElement.addElement(Messages.getMessage("batch_presentation.process_definition.process_type", pageContext) + ": ");

        try {
            Select select = getTypeSelectElement(definitionType, subject, pageContext);
            tdFormElement.addElement(select);
        } catch (AuthenticationException e) {
        } catch (AuthorizationException e) {
        }

        Map<String, String> attr = (Map<String, String>) pageContext.getRequest().getAttribute("TypeAttributes");
        String newType = "";
        if (attr != null) {
            newType = attr.get("type");
        }
        tdFormElement.addElement("  " + Messages.getMessage("batch_presentation.process_instance.create_new_process_type", pageContext) + ": ");
        Input inputType = new Input(Input.TEXT, "type", newType);
        tdFormElement.addElement(inputType);
    }

    protected void fillFormData(TD tdFormElement) {
        try {
            fillTD(tdFormElement, getForm(), getDefinition().getType(), getSubject(), pageContext);
        } catch (JspException e) {
        }
    }

    protected Permission getPermission() {
        return ProcessDefinitionPermission.REDEPLOY_DEFINITION;
    }

    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_REDEPLOY_DEFINITION, pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.TITLE_REDEPLOY_DEFINITION, pageContext);
    }

    public String getAction() {
        return RedeployProcessDefinitionAction.ACTION_PATH;
    }

    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REDEPLOY_PROCESS_DEFINITION_PARAMETER;
    }

    protected boolean isVisible() throws JspException {
        AuthorizationService authorizationService = ru.runa.delegate.DelegateFactory.getInstance()
                .getAuthorizationService();
        try {
            return authorizationService.isAllowed(getSubject(), ProcessDefinitionPermission.REDEPLOY_DEFINITION, getIdentifiable());
        } catch (AuthorizationException e) {
            return false;
        } catch (AuthenticationException e) {
            throw new JspException(e);
        }
    }
}
