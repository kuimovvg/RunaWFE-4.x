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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.Actor;
import ru.runa.af.Permission;
import ru.runa.af.Substitution;
import ru.runa.af.SubstitutionCriteria;
import ru.runa.af.service.ExecutorService;
import ru.runa.af.service.SubstitutionService;
import ru.runa.af.web.action.DeleteSubstitutionCriteriasAction;
import ru.runa.af.web.action.UpdateSubstitutionCriteriaAction;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Commons.PortletUrl;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.SubstitutionCriteriasForm;
import ru.runa.delegate.DelegateFactory;

/**
 * Created on 24.04.2012
 *
 * @jsp.tag name = "listSubstitutionCriteriasForm" body-content = "JSP"
 */
public class ListSubstitutionCriteriasFormTag extends UpdateSystemBaseFormTag {
    private static final long serialVersionUID = 1L;
    private String substitutionCriteriaIDs;

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public String getSubstitutionCriteriaIDs() {
        return substitutionCriteriaIDs;
    }

    public void setSubstitutionCriteriaIDs(String substitutionCriteriaIDs) {
        this.substitutionCriteriaIDs = substitutionCriteriaIDs;
    }

    private static ArrayList<Long> arrayFromString(String string) {
        ArrayList<Long> result = new ArrayList<Long>();
        if (string == null || string.isEmpty())
            return result;
        String[] strings = string.replace("[", "").replace("]", "").split(", ");
        for (int i = 0; i < strings.length; i++) {
            result.add(Long.valueOf(strings[i]));
        }
        return result;
    }

    protected void fillFormData(TD tdFormElement) throws JspException {
        SubstitutionCriteriaTableBuilder tableBuilder = new SubstitutionCriteriaTableBuilder(pageContext);
        tdFormElement.addElement(tableBuilder.buildTable());
        tdFormElement.addElement(new Input(Input.HIDDEN, SubstitutionCriteriasForm.REMOVE_METHOD_INPUT_NAME,
                SubstitutionCriteriasForm.REMOVE_METHOD_CONFIRM));
        if (substitutionCriteriaIDs != null && !substitutionCriteriaIDs.isEmpty()) {
            String message = Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_USED_BY, pageContext) + ":<ul>";
            ArrayList<Long> ids = arrayFromString(substitutionCriteriaIDs);
            ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
            try {
                SubstitutionService substitutionService = DelegateFactory.getInstance().getSubstitutionService();
                for (Long id : ids) {
                    SubstitutionCriteria substitutionCriteria = substitutionService.getSubstitutionCriteria(getSubject(), id);
                    substitutions.addAll(substitutionService.getBySubstitutionCriteria(getSubject(), substitutionCriteria));
                }
            } catch (Exception e) {
                handleException(e);
            }
            for (Substitution substitution : substitutions) {
                ExecutorService executorService = DelegateFactory.getInstance().getExecutorService();
                try {
                    Actor actor = executorService.getActor(getSubject(), substitution.getActorId());
                    message += "<li>" + actor.getFullName() + " (" + actor.getName() + ")</li>";
                } catch (Exception e) {
                    handleException(e);
                }
            }
            message += "</ul>" + Messages.getMessage(Messages.CONF_POPUP_REMOVE_SUBSTITUTION_CRITERIA, pageContext);
            getForm().addAttribute("id", "substitutionCriteriasForm");
            tdFormElement.addElement("<script>" + "onload = function() {" + "openSubstitutionCriteriasConfirmPopup('" + "substitutionCriteriasForm"
                    + "', '" + message + "', '" + SubstitutionCriteriasForm.REMOVE_METHOD_ALL + "', '"
                    + Messages.getMessage(Messages.CONF_POPUP_SUBSTITUTION_CRITERIA_BUTTON_ALL, pageContext) + "', '"
                    + SubstitutionCriteriasForm.REMOVE_METHOD_ONLY + "', '"
                    + Messages.getMessage(Messages.CONF_POPUP_SUBSTITUTION_CRITERIA_BUTTON_ONLY, pageContext) + "', '"
                    + Messages.getMessage(Messages.CONF_POPUP_BUTTON_CANCEL, pageContext) + "');" + "}" + "</script>");
        }
    }

    protected Permission getPermission() {
        return Permission.UPDATE_PERMISSIONS;
    }

    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_SUBSTITUTION_CRITERIA, pageContext);
    }

    public String getAction() {
        return DeleteSubstitutionCriteriasAction.ACTION_PATH;
    }

    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_SUBSTITUTION_CRITERIA_PARAMETER;
    }

    private class SubstitutionCriteriaTableBuilder {
        private final PageContext pageContext;

        public SubstitutionCriteriaTableBuilder(PageContext pageContext) throws JspException {
            this.pageContext = pageContext;
        }

        public Table buildTable() throws JspException {
            Table table = new Table();
            table.setClass(Resources.CLASS_PERMISSION_TABLE);
            table.addElement(createTableHeaderTR());
            List<SubstitutionCriteria> substitutionCriterias = null;
            try {
                SubstitutionService substitutionService = DelegateFactory.getInstance().getSubstitutionService();
                substitutionCriterias = substitutionService.getSubstitutionCriteriaAll(getSubject());
            } catch (Exception e) {
                return null;
            }

            ArrayList<Long> ids = arrayFromString(substitutionCriteriaIDs);
            for (SubstitutionCriteria substitutionCriteria : substitutionCriterias) {
                table.addElement(createTR(substitutionCriteria, ids.contains(substitutionCriteria.getId())));
            }
            return table;
        }

        private TR createTableHeaderTR() {
            TR tr = new TR();
            tr.addElement(new TH().setClass(Resources.CLASS_LIST_TABLE_TH));
            tr
                    .addElement(new TH(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_NAME, pageContext))
                            .setClass(Resources.CLASS_LIST_TABLE_TH));
            tr
                    .addElement(new TH(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_TYPE, pageContext))
                            .setClass(Resources.CLASS_LIST_TABLE_TH));
            tr
                    .addElement(new TH(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_CONF, pageContext))
                            .setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }

        private TR createTR(SubstitutionCriteria substitutionCriteria, boolean enabled) throws JspException {
            TR tr = new TR();
            Input input = new Input(Input.CHECKBOX, SubstitutionCriteriasForm.IDS_INPUT_NAME, String.valueOf(substitutionCriteria.getId()));
            input.setChecked(enabled);
            tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
            {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(IdForm.ID_INPUT_NAME, String.valueOf(substitutionCriteria.getId()));
                A editHref = new A(Commons.getActionUrl(UpdateSubstitutionCriteriaAction.EDIT_ACTION, params, pageContext, PortletUrl.Action));
                editHref.addElement(substitutionCriteria.getName());
                tr.addElement(new TD(editHref).setClass(Resources.CLASS_LIST_TABLE_TD));
            }
            tr.addElement(new TD(Messages.getMessage(substitutionCriteria.displayType(), pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(substitutionCriteria.getConf()).setClass(Resources.CLASS_LIST_TABLE_TD));
            return tr;
        }
    }
}
