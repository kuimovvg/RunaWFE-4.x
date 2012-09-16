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

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.delegate.DelegateFactory;
import ru.runa.wf.ProcessDefinitionFormException;
import ru.runa.wf.TaskDoesNotExistException;
import ru.runa.wf.form.Interaction;
import ru.runa.wf.service.DefinitionService;
import ru.runa.wf.web.html.WorkflowFormProcessingException;

import com.google.common.base.Charsets;

/**
 * Created on 11.05.2005
 * 
 */
public abstract class WFFormTag extends TitledFormTag {

    private static final long serialVersionUID = 1L;

    private static final String FORM_CSS = "form.css";

    private static final Log log = LogFactory.getLog(WFFormTag.class);

    public static final String FORM_NAME = "processForm";

    protected String getFormBuilderType() throws AuthorizationException, AuthenticationException, ProcessDefinitionFormException,
            TaskDoesNotExistException {
        return getInteraction().getType();
    }

    private boolean isButtonVisible = false;

    @Override
    @SuppressWarnings("unchecked")
    protected void fillFormElement(TD tdFormElement) throws JspException {
        isButtonVisible = false;
        try {
            Interaction interaction = getInteraction();
            String wfFormContent = buildForm(interaction);
            Map<String, String[]> userDefinedVariables = (Map<String, String[]>) pageContext.getRequest().getAttribute("UserDefinedVariables");
            if (userDefinedVariables != null) {
                Map<String, String> userErrors = (Map<String, String>) pageContext.getRequest().getAttribute("UserErrors");
                wfFormContent = HTMLFormConverter.fillForm(pageContext, wfFormContent, userDefinedVariables, userErrors);
            }
            setStyle(tdFormElement);
            if (interaction.isUseJSValidation()) {
                log.debug("Using javascript validation.");
                String commonValidationJsSrc = Commons.getUrl("/validation.js", pageContext, PortletUrl.Resource);
                tdFormElement.addElement(new StringElement("<script language=\"javascript\" src=\"".concat(commonValidationJsSrc).concat(
                        "\">var c=0;</script>")));

                XWorkJavascriptValidator javascriptValidator = new XWorkJavascriptValidator();
                String cacheKey = String.valueOf(getDefinitionId() + 37 * interaction.getStateName().hashCode());
                String javaScript = javascriptValidator.getCachedJavascript(cacheKey);
                if (javaScript != null) {
                    log.debug("JS found in cache.");
                } else {
                    javaScript = javascriptValidator.getJavascript(cacheKey, interaction.getValidationData());
                    log.debug("JS created.");
                }
                getForm().setOnSubmit("return validateForm_".concat(FORM_NAME).concat("();"));
                tdFormElement.addElement(new StringElement(javaScript));
            } else if ("xsn".equals(interaction.getType())) {
                log.debug("InfoPath powered form");
                Script script = new Script();
                script.setSrc(Commons.getUrl("/infopath.js", pageContext, PortletUrl.Resource));
                script.setLanguage("javascript");
                script.setType("text/javascript");
                tdFormElement.addElement(script);
                getForm().setOnSubmit("return try_submit()");
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
        } catch (WorkflowFormProcessingException e) {
            log.error("WorkflowFormProcessingException", e.getCause());
            UL ul = new UL();
            tdFormElement.addElement(ul);
            ul.setClass(Resources.CLASS_ERROR);
            String taskCanNotBeShownLocolizedMessage = Messages.getMessage(Messages.TASK_FORM_ERROR, pageContext);
            LI li = new LI(taskCanNotBeShownLocolizedMessage + " " + ActionExceptionHelper.getErrorMessage(e.getCause(), pageContext));
            ul.addElement(li);
        } catch (TaskDoesNotExistException e) {
            log.error(e.getMessage());
            P p = new P();
            tdFormElement.addElement(p);
            p.setClass(Resources.CLASS_ERROR);
            String message = ActionExceptionHelper.getErrorMessage(e, pageContext);
            p.addElement(message);
        } catch (Exception e) {
            handleException(e);
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

    private void setStyle(TD tdFormElement) {
        try {
            DefinitionService definitionService = DelegateFactory.getInstance().getDefinitionService();
            byte[] css = definitionService.getFile(getSubject(), getDefinitionId(), FORM_CSS);
            if (css != null) {
                StringBuffer styles = new StringBuffer("<style>");
                styles.append(new String(css, Charsets.UTF_8));
                styles.append("</style>");
                tdFormElement.addElement(new StringElement(styles.toString()));
            }
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
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

    abstract protected Interaction getInteraction() throws AuthorizationException, AuthenticationException,
            TaskDoesNotExistException;

    abstract protected String buildForm(Interaction interaction) throws AuthenticationException, WorkflowFormProcessingException, AuthorizationException,
            TaskDoesNotExistException;
}
