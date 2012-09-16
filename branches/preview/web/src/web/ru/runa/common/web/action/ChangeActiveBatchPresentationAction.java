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

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.AuthorizationException;
import ru.runa.af.ExecutorOutOfDateException;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.presentation.Profile;
import ru.runa.af.service.ProfileService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.form.BatchPresentationForm;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 18.07.2005
 * 
 * @struts:action path="/changeActiveBatchPresentation" name="batchPresentationForm" validate="false"
 */
public class ChangeActiveBatchPresentationAction extends Action {
    private static final Log log = LogFactory.getLog(ChangeActiveBatchPresentationAction.class);

    public static final String ACTION_PATH = "/changeActiveBatchPresentation";

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        BatchPresentationForm batchPresentationForm = (BatchPresentationForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        try {
            ProfileService profileService = DelegateFactory.getInstance().getProfileService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            profileService.changeActiveBatchPresentation(subject, profile.getActorId(), batchPresentationForm.getBatchPresentationId(),
                    batchPresentationForm.getBatchPresentationName());
            profile.setActiveBatchPresentation(batchPresentationForm.getBatchPresentationId(), batchPresentationForm.getBatchPresentationName());
        } catch (BatchPresentationNotFoundException e) {
            log.error(e);
            ActionExceptionHelper.addException(errors, e);
        } catch (AuthenticationException e) {
            log.error(e);
            ActionExceptionHelper.addException(errors, e);
        } catch (AuthorizationException e) {
            log.error(e);
            ActionExceptionHelper.addException(errors, e);
        } catch (ExecutorOutOfDateException e) {
            log.error(e);
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }

        return new ActionForward(batchPresentationForm.getReturnAction(), true);
    }
}
