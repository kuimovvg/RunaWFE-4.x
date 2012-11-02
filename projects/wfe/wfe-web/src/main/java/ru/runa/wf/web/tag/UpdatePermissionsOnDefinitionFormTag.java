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

import java.util.List;

import javax.servlet.jsp.JspException;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Messages;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wf.web.action.UpdatePermissionOnProcessDefinitionAction;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.SystemExecutors;

/**
 * Created on 24.08.2004
 * 
 * @jsp.tag name = "updatePermissionsOnDefinitionForm" body-content = "JSP"
 */
public class UpdatePermissionsOnDefinitionFormTag extends ProcessDefinitionBaseFormTag {

    private static final long serialVersionUID = -3924515617058954059L;

    @Override
    protected void fillFormData(TD tdFormElement) throws JspException {
        try {
            WfDefinition defintion = getDefinition();
            PermissionTableBuilder tableBuilder = new PermissionTableBuilder(defintion, getSubject(), pageContext);
            Table table = tableBuilder.buildTable();
            table.addElement(tableBuilder.createTR(getProcessStarterExecutor(), getUnmodifiablePermissions(), false));
            tdFormElement.addElement(table);
        } catch (Exception e) {
            handleException(e);
        }
    }

    private Executor getProcessStarterExecutor() throws AuthorizationException, AuthenticationException, ExecutorDoesNotExistException {
        return DelegateFactory.getExecutorService().getActor(getSubject(), SystemExecutors.PROCESS_STARTER_NAME);
    }

    private List<Permission> getUnmodifiablePermissions() {
        List<Permission> result = new DefinitionPermission().getAllPermissions();
        result.remove(DefinitionPermission.CANCEL_STARTED_PROCESS);
        result.remove(DefinitionPermission.READ_STARTED_PROCESS);
        return result;
    }

    @Override
    protected Permission getPermission() {
        return Permission.UPDATE_PERMISSIONS;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_APPLY, pageContext);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_PERMISSION_OWNERS, pageContext);
    }

    @Override
    public String getAction() {
        return UpdatePermissionOnProcessDefinitionAction.ACTION_PATH;
    }

}
