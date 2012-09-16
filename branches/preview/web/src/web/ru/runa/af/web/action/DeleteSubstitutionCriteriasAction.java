package ru.runa.af.web.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.AuthenticationException;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.service.SubstitutionService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.SubstitutionCriteriasForm;
import ru.runa.delegate.DelegateFactory;

import com.google.common.collect.Lists;

/**
 * Created on 14.08.2010
 * 
 * @struts:action path="/deleteSubstitutionCriterias" name="substitutionCriteriasForm" validate="false"
 * @struts.action-forward name="success" path="/manage_system.do"
 * @struts.action-forward name="failure" path="/manage_system.do"
 */
public class DeleteSubstitutionCriteriasAction extends Action {

    public static final String ACTION_PATH = "/deleteSubstitutionCriterias";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse responce)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        try {
            SubstitutionService substitutionService = DelegateFactory.getInstance().getSubstitutionService();
            Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
            ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
            Long ids[] = ((SubstitutionCriteriasForm) form).getIds();
            String method = ((SubstitutionCriteriasForm) form).getRemoveMethod();
            for (Long id : ids) {
                SubstitutionCriteria substitutionCriteria = substitutionService.getSubstitutionCriteria(subject, id);
                substitutions.addAll(substitutionService.getBySubstitutionCriteria(subject, substitutionCriteria));
            }

            if (SubstitutionCriteriasForm.REMOVE_METHOD_CONFIRM.equals(method) && !substitutions.isEmpty()) {
                return Commons.forward(mapping.findForward(Resources.FORWARD_FAILURE), "substitutionCriteriaIDs", Arrays.toString(ids));
            }

            if (SubstitutionCriteriasForm.REMOVE_METHOD_ALL.equals(method)) {
                List<Long> substitutionIds = Lists.newArrayList();
                for (Substitution substitution : substitutions) {
                    substitutionIds.add(substitution.getId());
                }
                substitutionService.delete(subject, substitutionIds);
            } else if (SubstitutionCriteriasForm.REMOVE_METHOD_ONLY.equals(method)) {
                for (Substitution substitution : substitutions) {
                    substitution.setCriteria(null);
                    substitutionService.store(subject, substitution);
                }
            }

            for (long id : ids) {
                substitutionService.deleteSubstitutionCriteria(subject, id);
            }
        } catch (Exception e) {
            ActionExceptionHelper.addException(errors, e);
        }

        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
            return mapping.findForward(ru.runa.common.web.Resources.FORWARD_FAILURE);
        }
        return mapping.findForward(ru.runa.common.web.Resources.FORWARD_SUCCESS);
    }

}
