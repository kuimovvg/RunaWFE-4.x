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

import ru.runa.InternalApplicationException;
import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.Executor;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.ExecutorPermission;
import ru.runa.af.Group;
import ru.runa.af.GroupPermission;
import ru.runa.af.Permission;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.web.action.AddMembersToGroupAction;
import ru.runa.common.web.Messages;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 09.09.2004
 * 
 * @jsp.tag name = "listNotGroupMembersForm" body-content = "JSP"
 */
public class ListNotGroupMembersFormTag extends ListExecutorsBaseFormTag {

    private static final long serialVersionUID = 1770247337446619592L;

    @Override
    protected Permission getPermission() {
        return GroupPermission.ADD_TO_GROUP;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_ADD, pageContext);
    }

    @Override
    protected List<? extends Executor> getExecutors() throws JspException, AuthorizationException, AuthenticationException, BatchPresentationNotFoundException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            Executor executor = getExecutor();
            if (!(executor instanceof Group)) {
                throw new InternalApplicationException("Executor type missmatch, Group expected but Actor found.");
            }
            Group group = (Group) executor;
            return executorService.getGroupChildren(getSubject(), group, getBatchPresentation(), true);
        } catch (ExecutorOutOfDateException e) {
            throw new JspException(e);
        }
    }

    @Override
    protected int getExecutorsCount() throws JspException, AuthorizationException, AuthenticationException, BatchPresentationNotFoundException {
        try {
            ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
            Executor executor = getExecutor();
            if (!(executor instanceof Group)) {
                throw new InternalApplicationException("Executor type missmatch, Group expected but Actor found.");
            }
            Group group = (Group) executor;
            return executorService.getGroupChildrenCount(getSubject(), group, getBatchPresentation(), true);
        } catch (ExecutorOutOfDateException e) {
            throw new JspException(e);
        }
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_ADD_EXECUTORS_TO_GROUP, pageContext);
    }

    @Override
    public String getAction() {
        return AddMembersToGroupAction.ACTION_PATH;
    }

    @Override
    protected Permission getExecutorsPermission() {
        return ExecutorPermission.READ;
    }
}
