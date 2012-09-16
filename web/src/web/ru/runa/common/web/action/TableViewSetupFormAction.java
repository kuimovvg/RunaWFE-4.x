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

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;

import ru.runa.af.AuthenticationException;
import ru.runa.af.presentation.BatchPresentation;
import ru.runa.af.presentation.BatchPresentationNotFoundException;
import ru.runa.af.presentation.ClassPresentation;
import ru.runa.af.presentation.FieldDescriptor;
import ru.runa.af.presentation.FieldState;
import ru.runa.af.presentation.Profile;
import ru.runa.af.presentation.filter.FilterCriteria;
import ru.runa.af.presentation.filter.FilterFormatException;
import ru.runa.af.service.ProfileService;
import ru.runa.af.web.SubjectHttpSessionHelper;
import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.common.web.html.format.FilterFormatsFactory;
import ru.runa.commons.ArraysCommons;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 26.01.2005
 * 
 * @struts:action path="/tableViewSetup" name="tableViewSetupForm" validate="false" parameter = "dispatch"
 */
public class TableViewSetupFormAction extends LookupDispatchAction {

    public static final String ACTION_PATH = "/tableViewSetup";

    public static final String PARAMETER_NAME = "dispatch";

    @Override
    protected Map<String, String> getKeyMethodMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(Messages.BUTTON_APPLY, "apply");
        map.put(Messages.BUTTON_SAVE, "save");
        map.put(Messages.BUTTON_SAVE_AS, "saveAs");
        map.put(Messages.BUTTON_REMOVE, "delete");
        return map;
    }

    public ActionForward apply(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionMessages errors = new ActionMessages();
        TableViewSetupForm tableViewSetupForm = (TableViewSetupForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        try {
            BatchPresentation batchPresentation = getActiveBatchPresentation(profile, tableViewSetupForm.getBatchPresentationId());
            applyBatchPresentation(batchPresentation, tableViewSetupForm, errors);
        } catch (BatchPresentationNotFoundException e) {
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward(tableViewSetupForm.getReturnAction(), true);
    }

    private void applyBatchPresentation(BatchPresentation batchPresentation, TableViewSetupForm tableViewSetupForm, ActionMessages errors) {
        batchPresentation.setFieldsToDisplayIds(tableViewSetupForm.getDisplayPositionsIds());
        try {
            {
                FieldDescriptor[] fields = batchPresentation.getAllFields();
                int idx = 0;
                for (FieldDescriptor field : fields) {
                    if (field.displayName.startsWith("edit:") && field.fieldState == FieldState.ENABLED) {
                        break;
                    }
                    ++idx;
                }

                if (idx == batchPresentation.getAllFields().length) {
                    // No edit section
                    Map<Integer, FilterCriteria> result = FilterFormatsFactory.getParser().parse(batchPresentation,
                            tableViewSetupForm.getFieldsToFilterCriteriasMap());
                    for (int fieldIdx : batchPresentation.getFilteredFieldsMap().keySet()) {
                        if (fields[fieldIdx].fieldState != FieldState.ENABLED) {
                            result.put(fieldIdx, batchPresentation.getFilteredFieldsMap().get(fieldIdx));
                        }
                    }
                    batchPresentation.setFilteredFieldsMap(result);
                    batchPresentation.setFieldsToSort(tableViewSetupForm.getSortPositionsIds(), tableViewSetupForm.getSortingModes());
                    batchPresentation.setFieldsToGroup(tableViewSetupForm.getFieldsToGroupIds());
                } else {
                    int arrayPos = ArraysCommons.findPosition(tableViewSetupForm.getSortPositionsIds(), idx);
                    Map<Integer, String[]> m = tableViewSetupForm.getFieldsToFilterCriteriasMap();
                    m.remove(new Integer(idx));
                    Map<Integer, FilterCriteria> result = FilterFormatsFactory.getParser().parse(batchPresentation, m);
                    for (int fieldIdx : batchPresentation.getFilteredFieldsMap().keySet()) {
                        if (fields[fieldIdx].fieldState != FieldState.ENABLED) {
                            result.put(fieldIdx, batchPresentation.getFilteredFieldsMap().get(fieldIdx));
                        }
                    }
                    batchPresentation.setFilteredFieldsMap(result);
                    int[] groupFields = (arrayPos == -1) ? tableViewSetupForm.getSortPositionsIds() : ArraysCommons.remove(tableViewSetupForm
                            .getSortPositionsIds(), arrayPos);
                    boolean[] sortModes = (arrayPos == -1) ? tableViewSetupForm.getSortingModes() : ArraysCommons.remove(tableViewSetupForm
                            .getSortingModes(), arrayPos);
                    batchPresentation.setFieldsToSort(groupFields, sortModes);
                    arrayPos = ArraysCommons.findPosition(tableViewSetupForm.getFieldsToGroupIds(), idx);
                    groupFields = (arrayPos == -1) ? tableViewSetupForm.getFieldsToGroupIds() : ArraysCommons.remove(tableViewSetupForm
                            .getFieldsToGroupIds(), arrayPos);
                    batchPresentation.setFieldsToGroup(groupFields);
                }
            }
            batchPresentation.setPredefinedRangeSize(tableViewSetupForm.getViewSize());
            {
                FieldDescriptor[] fields = batchPresentation.getAllFields();
                int[] activeRemovable = tableViewSetupForm.getRemovableIds();
                int removeCount = 0;
                for (int i = fields.length - 1; i >= 0; --i) {
                    if (fields[i].displayName.startsWith(ClassPresentation.removable_prefix) && fields[i].fieldState == FieldState.ENABLED) {
                        if (ArraysCommons.findPosition(activeRemovable, i) == -1) {
                            batchPresentation.removeDynamicField(i - removeCount);
                        }
                    }
                }

                String[] editableValues = tableViewSetupForm.getEditableFieldsValues();
                int editIdx = 0;
                fields = batchPresentation.getAllFields();
                for (int idx = 0; idx < fields.length; ++idx) {
                    if (fields[idx].displayName.startsWith(ClassPresentation.editable_prefix) && fields[idx].fieldState == FieldState.ENABLED) {
                        if (editableValues[editIdx] != null && !editableValues[editIdx].equals("")) {
                            batchPresentation.addDynamicField(idx, editableValues[editIdx]);
                        }
                        ++editIdx;
                    }
                }
            }
        } catch (FilterFormatException e) {
            ActionExceptionHelper.addException(errors, e);
        }
    }

    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        apply(mapping, form, request, response);
        TableViewSetupForm tableViewSetupForm = (TableViewSetupForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        saveProfile(request, profile, tableViewSetupForm);
        return new ActionForward(tableViewSetupForm.getReturnAction(), true);
    }

    private void saveProfile(HttpServletRequest request, Profile profile, TableViewSetupForm tableViewSetupForm) throws AuthenticationException {
        ProfileService profileService = DelegateFactory.getInstance().getProfileService();
        Subject subject = SubjectHttpSessionHelper.getActorSubject(request.getSession());
        // comment following line for WFDEMO
        profileService.saveProfile(subject, profile);
        // Why need to reload profile from database? This action lead to incorrect
        // behaviour - filtering by variable working incorrect
        // profile = profileServiceDelegate.getProfile(subject);
        /* next 2 lines used to reloading profile from db */
        // profile.changeBlockVisibility(tableViewSetupForm.getBatchPresentationId());
        // ProfileHttpSessionHelper.setProfile(profile, request.getSession());
    }

    private static final String DEFAUL_VIEW_SETUP_NAME = "";

    public ActionForward saveAs(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        TableViewSetupForm tableViewSetupForm = (TableViewSetupForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        try {
            BatchPresentation presentation = getActiveBatchPresentation(profile, tableViewSetupForm.getBatchPresentationId());
            String batchName = tableViewSetupForm.getSaveAsBatchPresentationName();
            String newName = batchName != null ? batchName : DEFAUL_VIEW_SETUP_NAME;
            BatchPresentation batchPresentationClone = presentation.clone();
            batchPresentationClone.setBatchPresentationName(newName);
            applyBatchPresentation(batchPresentationClone, tableViewSetupForm, errors);
            profile.addBatchPresentation(batchPresentationClone);
            profile.setActiveBatchPresentation(batchPresentationClone.getBatchPresentationId(), batchPresentationClone.getBatchPresentationName());
            saveProfile(request, profile, tableViewSetupForm);// profile.getActiveBatchPresentation(tableViewSetupForm.getBatchPresentationId())
        } catch (BatchPresentationNotFoundException e) {
            log.error(e);
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward(tableViewSetupForm.getReturnAction(), true);
    }

    public ActionForward delete(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        ActionMessages errors = new ActionMessages();
        TableViewSetupForm tableViewSetupForm = (TableViewSetupForm) form;
        Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
        try {
            BatchPresentation presentation = getActiveBatchPresentation(profile, tableViewSetupForm.getBatchPresentationId());
            profile.deleteBatchPresentation(presentation);
            saveProfile(request, profile, tableViewSetupForm);
        } catch (BatchPresentationNotFoundException e) {
            log.error(e);
            ActionExceptionHelper.addException(errors, e);
        }
        if (!errors.isEmpty()) {
            saveErrors(request.getSession(), errors);
        }
        return new ActionForward(tableViewSetupForm.getReturnAction(), true);
    }

    private BatchPresentation getActiveBatchPresentation(Profile profile, String batchPresentationId) throws BatchPresentationNotFoundException {
        return profile.getActiveBatchPresentation(batchPresentationId);
    }
}
