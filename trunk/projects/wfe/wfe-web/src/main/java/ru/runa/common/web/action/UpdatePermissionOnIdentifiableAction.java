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
package ru.runa.common.web.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.af.web.form.UpdatePermissionsOnIdentifiableForm;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.user.Executor;

import com.google.common.collect.Lists;

abstract public class UpdatePermissionOnIdentifiableAction extends IdentifiableAction {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        UpdatePermissionsOnIdentifiableForm permissionsForm = (UpdatePermissionsOnIdentifiableForm) form;
        try {
            AuthorizationService authorizationService = DelegateFactory.getAuthorizationService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            Identifiable identifiable = getIdentifiable(subject, permissionsForm.getId(), errors);
            if (identifiable != null) {
                Long[] selectedIds = permissionsForm.getIds();
                List<Long> executorsIdList = new ArrayList<Long>(selectedIds.length);
                List<Collection<Permission>> permissionList = Lists.newArrayList();
                Permission noPermission = identifiable.getSecuredObjectType().getNoPermission();
                for (int i = 0; i < selectedIds.length; i++) {
                    executorsIdList.add(selectedIds[i]);
                    List<Permission> permissions = Lists.newArrayList();
                    for (Long mask : permissionsForm.getPermissionMasks(selectedIds[i])) {
                        permissions.add(noPermission.getPermission(mask));
                    }
                    permissionList.add(permissions);
                }
                BatchPresentation batchPresentation = BatchPresentationFactory.EXECUTORS.createDefault();
                batchPresentation.setRangeSize(BatchPresentationConsts.MAX_UNPAGED_REQUEST_SIZE);
                List<Executor> executors = authorizationService.getExecutorsWithPermission(subject, identifiable, batchPresentation, true);
                for (Executor executor : executors) {
                    if (!executorsIdList.contains(executor.getId())) {
                        executorsIdList.add(executor.getId());
                        permissionList.add(Permission.getNoPermissions());
                    }
                }

                authorizationService.setPermissions(subject, executorsIdList, permissionList, identifiable);
            }
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return getErrorForward(SubjectHttpSessionHelper.getActorSubject(request.getSession()), mapping, permissionsForm.getId());
        }
        return getSuccessForward(SubjectHttpSessionHelper.getActorSubject(request.getSession()), mapping, permissionsForm.getId());
    }

    @Override
    protected List<Permission> getIdentifiablePermissions() {
        return Permission.getNoPermissions();
    }

    public abstract ActionForward getErrorForward(ActionMapping mapping, Long identifiableId);

    public abstract ActionForward getSuccessForward(ActionMapping mapping, Long identifiableId);

    protected ActionForward getErrorForward(Subject subject, ActionMapping mapping, Long identifiableId) {
        return getErrorForward(mapping, identifiableId);
    }

    protected ActionForward getSuccessForward(Subject subject, ActionMapping mapping, Long identifiableId) {
        return getSuccessForward(mapping, identifiableId);
    }
}
