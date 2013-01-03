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

import java.util.Map;

import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.LI;
import org.apache.ecs.html.P;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.UL;
import org.apache.struts.Globals;
import org.apache.struts.taglib.html.Constants;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.action.BaseProcessFormAction;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.task.TaskDoesNotExistException;

import com.google.common.base.Charsets;

/**
 * Created on 11.05.2005
 * 
 */
public abstract class WFFormTag extends TitledFormTag {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(WFFormTag.class);

    public static final String FORM_NAME = "processForm";

    private boolean isButtonVisible = false;

    @Override
    @SuppressWarnings("unchecked")
    protected void fillFormElement(TD tdFormElement) throws JspException {
        isButtonVisible = false;
        try {
            Interaction interaction = getInteraction();
            String wfFormContent = buildForm(interaction);
            Map<String, String[]> userDefinedVariables = (Map<String, String[]>) pageContext.getRequest().getAttribute(
                    BaseProcessFormAction.USER_DEFINED_VARIABLES);
            if (userDefinedVariables != null) {
                Map<String, String> userErrors = (Map<String, String>) pageContext.getRequest().getAttribute(BaseProcessFormAction.USER_ERRORS);
                wfFormContent = HTMLFormConverter.fillForm(pageContext, wfFormContent, userDefinedVariables, userErrors);
            }
            if (interaction.getCssData() != null) {
                StringBuffer styles = new StringBuffer("<style>");
                styles.append(new String(interaction.getCssData(), Charsets.UTF_8));
                styles.append("</style>");
                tdFormElement.addElement(new StringElement(styles.toString()));
            }
            if (interaction.isUseJSValidation()) {
                log.debug("Using javascript validation.");
                String javaScript = XWorkJavascriptValidator.getJavascript(interaction.getValidationData());
                getForm().setOnSubmit("return validateForm_".concat(FORM_NAME).concat("();"));
                tdFormElement.addElement(new StringElement(javaScript));
            }
            if (interaction.getScriptData() != null) {
                Script script = new Script();
                script.setLanguage("javascript");
                script.setType("text/javascript");
                script.addElement(new StringElement(new String(interaction.getScriptData(), Charsets.UTF_8)));
                tdFormElement.addElement(script);
            }

            tdFormElement.addElement(new StringElement(wfFormContent));
            isButtonVisible = true;
        } catch (TaskDoesNotExistException e) {
            log.error(e.getMessage());
            P p = new P();
            tdFormElement.addElement(p);
            p.setClass(Resources.CLASS_ERROR);
            String message = ActionExceptionHelper.getErrorMessage(e, pageContext);
            p.addElement(message);
        } catch (Exception e) {
            log.error("task form error", e.getCause());
            UL ul = new UL();
            tdFormElement.addElement(ul);
            ul.setClass(Resources.CLASS_ERROR);
            ul.addElement(new LI(Messages.getMessage(Messages.TASK_FORM_ERROR, pageContext) + " "
                    + ActionExceptionHelper.getErrorMessage(e.getCause(), pageContext)));
        }
        getForm().setEncType(Form.ENC_UPLOAD);
        getForm().setAcceptCharset(Charsets.UTF_8.name());
        getForm().setName(FORM_NAME);
        getForm().setID(FORM_NAME);
        Input tokenInput = new Input();
        tokenInput.setType(Input.HIDDEN);
        tokenInput.addAttribute("name", Constants.TOKEN_KEY);
        tokenInput.addAttribute("value", pageContext.getSession().getAttribute(Globals.TRANSACTION_TOKEN_KEY));
        getForm().addElement(tokenInput);
    }

    @Override
    public boolean isFormButtonVisible() {
        return isButtonVisible;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_COMPLETE, pageContext);
    }

    abstract protected Long getDefinitionId() throws AuthorizationException, AuthenticationException;

    abstract protected Interaction getInteraction() throws AuthorizationException, AuthenticationException, TaskDoesNotExistException;

    abstract protected String buildForm(Interaction interaction) throws Exception;
}
