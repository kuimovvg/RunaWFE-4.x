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
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.web.action.RemoveExecutorFromGroupsAction;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 23.08.2004
 * 
 * @jsp.tag name = "listExecutorGroupsForm" body-content = "JSP"
 */
public class ListExecutorGroupsFormTag extends ListExecutorsBaseFormTag {

    private static final long serialVersionUID = -2141545567983138556L;

    @Override
    protected Permission getPermission() {
        return Permission.READ;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    @Override
    protected List<? extends Executor> getExecutors() throws AuthorizationException, AuthenticationException, JspException, BatchPresentationNotFoundException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            return executorService.getExecutorGroups(getSubject(), getExecutor(), getBatchPresentation(), false);
        } catch (ExecutorOutOfDateException e) {
            throw new JspException(e);
        }
    }

    @Override
    protected int getExecutorsCount() throws AuthorizationException, AuthenticationException, JspException, BatchPresentationNotFoundException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            return executorService.getExecutorGroupsCount(getSubject(), getExecutor(), getBatchPresentation(), false);
        } catch (ExecutorOutOfDateException e) {
            throw new JspException(e);
        }
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_EXECUTOR_GROUPS, pageContext);
    }

    @Override
    public String getAction() {
        return RemoveExecutorFromGroupsAction.ACTION_PATH;
    }

    @Override
    protected Permission getExecutorsPermission() {
        return GroupPermission.REMOVE_FROM_GROUP;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER;
    }
}
