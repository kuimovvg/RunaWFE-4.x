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
package ru.runa.common.web.html;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.ExecutorNameConverter;
import ru.runa.af.web.form.UpdatePermissionsOnIdentifiableForm;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.IdsForm;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

import com.google.common.collect.Sets;

/**
 * Builds HTML Table of executors with their own permissions on given
 * identifiable.
 */
public class PermissionTableBuilder {
    private final Identifiable identifiable;

    private final User user;

    private final PageContext pageContext;

    private AuthorizationService authorizationService;

    private List<Permission> allowedPermission;

    /**
     * this flag will be set to true if subject does not have permission to
     * update permissions on identifiable .
     */
    private boolean isDisabled;

    public PermissionTableBuilder(Identifiable identifiable, User user, PageContext pageContext) throws JspException {
        this.identifiable = identifiable;
        this.user = user;
        this.pageContext = pageContext;
        try {
            authorizationService = Delegates.getAuthorizationService();
            allowedPermission = identifiable.getSecuredObjectType().getAllPermissions();
            isDisabled = !authorizationService.isAllowed(user, Permission.UPDATE_PERMISSIONS, identifiable);
        } catch (AuthorizationException e) {
            throw new JspException(e);
        } catch (AuthenticationException e) {
            throw new JspException(e);
        }
    }

    public Table buildTable() throws JspException {
        Table table = new Table();
        table.setClass(Resources.CLASS_PERMISSION_TABLE);
        table.addElement(createTableHeaderTR());
        BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
        List<Executor> executors = authorizationService.getExecutorsWithPermission(user, identifiable, batchPresentation, true);
        for (Executor executor : executors) {
            table.addElement(createTR(executor, new ArrayList<Permission>(), true));
        }
        return table;
    }

    private TR createTableHeaderTR() {
        TR tr = new TR();
        tr.addElement(new TH().setClass(Resources.CLASS_PERMISSION_TABLE_TH));
        tr.addElement(new TH(Messages.getMessage(Messages.LABEL_EXECUTOR_NAME, pageContext)).setClass(Resources.CLASS_PERMISSION_TABLE_TH));
        for (Permission permission : allowedPermission) {
            String permissioni18nName = Messages.getMessage(permission.getName(), pageContext);
            tr.addElement(new TH(permissioni18nName).setClass(Resources.CLASS_PERMISSION_TABLE_TH));
        }
        return tr;
    }

    public TR createTR(Executor executor, List<Permission> unmodifiablePermission, boolean isLink) throws JspException {
        TR tr = new TR();
        Input input = new Input(Input.CHECKBOX, IdsForm.IDS_INPUT_NAME, String.valueOf(executor.getId()));
        input.setChecked(true);
        input.setDisabled(isDisabled);
        tr.addElement(new TD(input).setClass(Resources.CLASS_PERMISSION_TABLE_TD));
        if (isLink) {
            String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, executor.getId(), pageContext,
                    PortletUrlType.Render);
            ConcreteElement tdElement = new A(url, ExecutorNameConverter.getName(executor, pageContext));
            tr.addElement(new TD(tdElement).setClass(Resources.CLASS_PERMISSION_TABLE_TD));
        } else {
            ConcreteElement tdElement = new StringElement(ExecutorNameConverter.getName(executor, pageContext));
            tr.addElement(new TD(tdElement).setClass(Resources.CLASS_PERMISSION_TABLE_TD));
        }
        boolean[] enabled = getEnabledPermissions(executor);
        for (int i = 0; i < allowedPermission.size(); i++) {
            Input checkbox = new Input(Input.CHECKBOX, UpdatePermissionsOnIdentifiableForm.EXECUTOR_INPUT_NAME_PREFIX + "(" + executor.getId() + ")."
                    + UpdatePermissionsOnIdentifiableForm.PERMISSION_INPUT_NAME_PREFIX + "(" + allowedPermission.get(i).getMask() + ")");
            checkbox.setChecked(enabled[i]);
            checkbox.setDisabled(isDisabled || unmodifiablePermission.contains(allowedPermission.get(i)));
            tr.addElement(new TD(checkbox).setClass(Resources.CLASS_PERMISSION_TABLE_TD));
        }
        return tr;
    }

    private boolean[] getEnabledPermissions(Executor performer) throws JspException {
        Set<Permission> executorPermissionsSet = Sets.newHashSet(authorizationService.getOwnPermissions(user, performer, identifiable));
        boolean[] enabled = new boolean[allowedPermission.size()];
        for (int i = 0; i < allowedPermission.size(); i++) {
            enabled[i] = executorPermissionsSet.contains(allowedPermission.get(i));
        }
        return enabled;
    }

}
