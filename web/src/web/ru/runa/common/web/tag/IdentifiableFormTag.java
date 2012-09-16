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

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Identifiable;
import ru.runa.af.Permission;
import ru.runa.af.service.AuthorizationService;
import ru.runa.common.web.form.IdForm;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 07.10.2004
 * 
 */
public abstract class IdentifiableFormTag extends TitledFormTag {

    private static final long serialVersionUID = 1L;
    private Long identifiableId;

    public void setIdentifiableId(Long identifiableId) {
        this.identifiableId = identifiableId;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getIdentifiableId() {
        return identifiableId;
    }

    protected abstract void fillFormData(final TD tdFormElement) throws JspException;

    /**
     * @return {@link Permission}that executor must have to update.
     * @throws JspException
     */
    protected abstract Permission getPermission() throws JspException;

    protected abstract Identifiable getIdentifiable() throws JspException;

    @Override
    protected boolean isFormButtonEnabled() throws JspException {
        if (getPermission() == null) {
            return true;
        }
        return isFormButtonEnabled(getIdentifiable(), getPermission());
    }

    @Override
    protected boolean isFormButtonEnabled(Identifiable identifiable, Permission permission) throws JspException {
        try {
            AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
            return (permission == null || authorizationService.isAllowed(getSubject(), permission, identifiable));
        } catch (AuthorizationException e) {
            throw new JspException(e);
        } catch (AuthenticationException e) {
            throw new JspException(e);
        }
    }

    @Override
    public final void fillFormElement(TD tdFormElement) throws JspException {
        fillFormData(tdFormElement);
        Input hiddenName = new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(getIdentifiableId()));
        tdFormElement.addElement(hiddenName);
    }
}
