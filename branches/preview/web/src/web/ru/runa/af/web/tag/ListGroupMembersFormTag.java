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
package ru.runa.af.web.tag;

import java.util.List;

import javax.servlet.jsp.JspException;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.service.AuthorizationService;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.web.action.RemoveExecutorsFromGroupAction;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 23.08.2004
 * 
 * @jsp.tag name = "listGroupMembersForm" body-content = "JSP"
 */
public class ListGroupMembersFormTag extends ListExecutorsBaseFormTag {

    private static final long serialVersionUID = -2400457393576894819L;

    @Override
    protected Permission getPermission() {
        return GroupPermission.REMOVE_FROM_GROUP;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    @Override
    protected boolean isVisible() throws JspException {
        try {
            AuthorizationService authorizationService = DelegateFactory.getInstance().getAuthorizationService();
            return getExecutor() instanceof Group && authorizationService.isAllowed(getSubject(), GroupPermission.LIST_GROUP, getExecutor());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected List<? extends Executor> getExecutors() throws AuthorizationException, AuthenticationException, JspException, BatchPresentationNotFoundException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            return executorService.getGroupChildren(getSubject(), (Group) getExecutor(), getBatchPresentation(), false);
        } catch (ExecutorOutOfDateException e) {
            throw new JspException(e);
        }
    }

    @Override
    protected int getExecutorsCount() throws AuthorizationException, AuthenticationException, JspException, BatchPresentationNotFoundException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            return executorService.getGroupChildrenCount(getSubject(), (Group) getExecutor(), getBatchPresentation(), false);
        } catch (ExecutorOutOfDateException e) {
            throw new JspException(e);
        }
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_GROUP_MEMBERS, pageContext);
    }

    @Override
    public String getAction() {
        return RemoveExecutorsFromGroupAction.ACTION_PATH;
    }

    @Override
    protected Permission getExecutorsPermission() {
        return ExecutorPermission.READ;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER;
    }
}
