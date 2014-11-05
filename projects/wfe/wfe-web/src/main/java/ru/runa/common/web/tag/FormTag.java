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
package ru.runa.common.web.tag;

import java.util.List;
import java.util.Map;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Entities;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.wfe.commons.web.PortletUrlType;

/**
 * Provides attibutes action and method for sub classes. Created on 19.08.2004
 * 
 */
abstract public class FormTag extends VisibleTag {

    private static final long serialVersionUID = 1L;
    public static final String SUBMIT_BUTTON_NAME = "submitButton";
    public static final String MULTIPLE_SUBMIT_BUTTONS = "multipleSubmit";

    protected String action;

    protected String method = Form.POST;

    protected String buttonAlignment;

    public void setAction(String action) {
        this.action = action;
    }

    public void setMethod(String string) {
        method = string;
    }

    /**
     * @jsp.attribute required = "false"
     */
    public String getAction() {
        return action;
    }

    /**
     * @jsp.attribute required = "false"
     */
    public String getMethod() {
        return method;
    }

    /**
     * In this method descendants fill the form.
     * 
     * @param tdFormElement
     *            @ if any exception occurred
     */
    abstract protected void fillFormElement(final TD tdFormElement);

    /**
     * @return returns true if form button must be displayed
     */
    protected boolean isFormButtonVisible() {
        return true;
    }

    /**
     * @return returns true if form button must be displayed
     */
    protected boolean isFormButtonEnabled() {
        return true;
    }

    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_FORM, pageContext);
    }

    protected Map<String, Object> getFormButtonParam() {
        return null;
    }

    protected boolean isMultipleSubmit() {
        return false;
    }

    protected List<String> getFormButtonNames() {
        return null;
    }

    private Form form;

    protected Form getForm() {
        return form;
    }

    public boolean isConfirmationPopupEnabled() {
        return ConfirmationPopupHelper.getInstance().isEnabled(getConfirmationPopupParameter());
    }

    protected String getConfirmationPopupParameter() {
        return "";
    }

    protected boolean isCancelButtonEnabled() {
        return false;
    }

    protected String getCancelButtonAction() {
        return "";
    }

    @Override
    protected ConcreteElement getEndElement() {
        form = new Form();
        Table table = new Table();
        table.setClass(Resources.CLASS_BOX);
        form.addElement(table);
        TR formElementTR = new TR();
        table.addElement(formElementTR);
        TD formElementTD = new TD();
        formElementTD.setClass(Resources.CLASS_BOX_BODY);
        formElementTR.addElement(formElementTD);
        fillFormElement(formElementTD);
        if (isFormButtonVisible()) {
            form.setAction(Commons.getActionUrl(getAction(), getFormButtonParam(), pageContext, PortletUrlType.Action));
            form.setMethod(getMethod());
            TR tr = new TR();
            table.addElement(tr);
            TD td = new TD();
            tr.addElement(td);
            td.setClass(Resources.CLASS_BOX_BODY);
            if (buttonAlignment != null) {
                td.setAlign(buttonAlignment);
            }
            if (isMultipleSubmit()) {
                td.addElement(new Input(Input.HIDDEN, MULTIPLE_SUBMIT_BUTTONS, "true"));
                for (String buttonName : getFormButtonNames()) {
                    Input submitButton = new Input(Input.SUBMIT, SUBMIT_BUTTON_NAME, buttonName);
                    submitButton.setClass(Resources.CLASS_BUTTON);
                    try {
                        if (!isFormButtonEnabled()) {
                            submitButton.setDisabled(true);
                        }
                    } catch (Exception e) {
                        log.debug("isFormButtonEnabled", e);
                        submitButton.setDisabled(true);
                    }
                    td.addElement(submitButton);
                    td.addElement(Entities.NBSP);
                }
            } else {
                Input submitButton = new Input(Input.SUBMIT, SUBMIT_BUTTON_NAME, getFormButtonName());
                submitButton.setClass(Resources.CLASS_BUTTON);
                if (isConfirmationPopupEnabled()) {
                    submitButton.addAttribute("onclick",
                            ConfirmationPopupHelper.getInstance().getConfirmationPopupCodeHTML(getConfirmationPopupParameter(), pageContext));
                }
                if (!isFormButtonEnabled()) {
                    submitButton.setDisabled(true);
                }
                td.addElement(submitButton);
            }
            if (isCancelButtonEnabled()) {
                Input cancelButton = new Input(Input.BUTTON, SUBMIT_BUTTON_NAME, Messages.getMessage(Messages.BUTTON_CANCEL, pageContext));
                cancelButton.setClass(Resources.CLASS_BUTTON);
                cancelButton.addAttribute("onclick", "window.location='" + getCancelButtonAction() + "'");
                td.addElement(Entities.NBSP);
                td.addElement(cancelButton);
            }
        }
        return form;
    }

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }
}
