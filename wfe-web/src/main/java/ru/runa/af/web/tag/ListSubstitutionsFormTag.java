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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import ru.runa.af.web.action.DeleteSubstitutionsAction;
import ru.runa.af.web.action.SwitchSubstitutionsPositionsAction;
import ru.runa.af.web.action.UpdateSubstitutionAction;
import ru.runa.af.web.form.SubstitutionForm;
import ru.runa.af.web.orgfunction.SubstitutionHelper;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.IdsForm;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.service.af.AuthorizationService;
import ru.runa.service.af.SubstitutionService;
import ru.runa.service.delegate.DelegateFactory;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.TerminatorSubstitution;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.ExecutorPermission;

/**
 * @jsp.tag name = "listSubstitutionsForm" body-content = "JSP"
 */
public class ListSubstitutionsFormTag extends UpdateExecutorBaseFormTag {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(ListSubstitutionsFormTag.class);

    @Override
    protected boolean isVisible() throws JspException {
        return getExecutor() instanceof Actor;
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.LABEL_SUBSTITUTORS, pageContext);
    }

    @Override
    protected void fillFormData(TD formTd) throws JspException {
        SubstitutionService substitutionService = DelegateFactory.getSubstitutionService();
        try {
            Actor actor = (Actor) getExecutor();
            Subject subject = getSubject();
            List<Substitution> substitutions = substitutionService.get(subject, actor.getId());
            AuthorizationService authorizationService = ru.runa.service.delegate.DelegateFactory.getAuthorizationService();
            boolean disabled = !authorizationService.isAllowed(subject, ExecutorPermission.UPDATE, actor);
            RowBuilder substitutionRowBuilder = new SubstitutionRowBuilder(substitutions, disabled);
            HeaderBuilder substitutionHeaderBuilder = new SubstitutionHeaderBuilder();
            TableBuilder tableBuilder = new TableBuilder();
            formTd.addElement(tableBuilder.build(substitutionHeaderBuilder, substitutionRowBuilder));
            formTd.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(actor.getId())));
        } catch (Exception e) {
            log.error("", e);
            formTd.addElement(e.getMessage());
        }
    }

    @Override
    public String getAction() {
        return DeleteSubstitutionsAction.ACTION_PATH;
    }

    @Override
    protected Permission getPermission() {
        return ActorPermission.UPDATE;
    }

    class SubstitutionHeaderBuilder implements HeaderBuilder {

        @Override
        public TR build() {
            TR tr = new TR();
            tr.addElement(new TH().setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage(Messages.LABEL_SWIMLANE_ORGFUNCTION, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage(Messages.LABEL_SUBSTITUTORS_CRITERIA, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage(Messages.LABEL_SUBSTITUTORS_ENABLED, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }
    }

    class SubstitutionRowBuilder implements RowBuilder {
        private final List<Substitution> substitutions;
        private int index;
        private final boolean disabled;

        public SubstitutionRowBuilder(List<Substitution> substitutions, boolean disabled) {
            this.substitutions = substitutions;
            this.disabled = disabled;
        }

        @Override
        public boolean hasNext() {
            return substitutions.size() > index;
        }

        @Override
        public TR buildNext() {
            TR tr = new TR();
            Substitution substitution = substitutions.get(index++);
            tr.addElement(buildSelectTD(substitution));
            tr.addElement(buildFunctionTD(substitution));
            tr.addElement(buildCriteriaTD(substitution));
            tr.addElement(buildEnabledTD(substitution));
            return tr;
        }

        private TD buildEnabledTD(Substitution substitution) {
            TD enabledTD = new TD();
            Input enabledInput = new Input(Input.CHECKBOX, SubstitutionForm.ENABLED_INPUT_NAME);
            enabledInput.setChecked(substitution.isEnabled());
            enabledInput.setDisabled(true);
            enabledTD.setClass(Resources.CLASS_LIST_TABLE_TD);
            enabledTD.addElement(enabledInput);

            enabledTD.addElement(Entities.NBSP);
            int position;
            if (index == substitutions.size()) {
                position = 0;
            } else {
                position = index;

                Map<String, Object> downParams = new HashMap<String, Object>();
                downParams.put(IdsForm.IDS_INPUT_NAME,
                        new String[] { String.valueOf(substitution.getId()), String.valueOf(substitutions.get(position).getId()) });
                downParams.put(IdsForm.ID_INPUT_NAME, substitution.getActorId());

                A moveDownHref = new A(Commons.getActionUrl(SwitchSubstitutionsPositionsAction.ACTION_PATH, downParams, pageContext,
                        PortletUrlType.Action));
                IMG moveDownIMG = new IMG(Commons.getUrl(Resources.SORT_DESC_IMAGE, pageContext, PortletUrlType.Resource), 0);
                moveDownIMG.setAlt(Resources.SORT_DESC_ALT);
                moveDownHref.addElement(moveDownIMG);

                enabledTD.addElement(moveDownHref);
            }
            return enabledTD;
        }

        private TD buildCriteriaTD(Substitution substitution) {
            TD criteriaTD = new TD();
            criteriaTD.setClass(Resources.CLASS_LIST_TABLE_TD);
            if (substitution.getCriteria() == null) {
                criteriaTD.addElement(Messages.getMessage(Messages.SUBSTITUTION_ALWAYS, pageContext));
            } else {
                criteriaTD.addElement(substitution.getCriteria().getName());
            }
            return criteriaTD;
        }

        private TD buildFunctionTD(Substitution substitution) {
            TD orgfunctionTD = new TD();
            orgfunctionTD.setClass(Resources.CLASS_LIST_TABLE_TD);
            String string;
            if (substitution instanceof TerminatorSubstitution) {
                string = Messages.getMessage("terminator.edit.title", pageContext);
            } else {
                try {
                    string = SubstitutionHelper.getUserFriendlyOrgFunction(getSubject(), pageContext, substitution.getSubstitutionOrgFunction());
                } catch (Exception e) {
                    log.error("Invalid orgfunction?", e);
                    string = "<span class='error'>" + substitution.getSubstitutionOrgFunction() + "</span>";
                }
            }
            if (disabled) {
                orgfunctionTD.addElement(string);
            } else {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(IdForm.ID_INPUT_NAME, substitution.getId());
                A editHref = new A(Commons.getActionUrl(UpdateSubstitutionAction.EDIT_ACTION, params, pageContext, PortletUrlType.Action));
                editHref.addElement(string);
                orgfunctionTD.addElement(editHref);
            }
            return orgfunctionTD;
        }

        private TD buildSelectTD(Substitution substitution) {
            TD checkboxTD = new TD();
            Input checkBoxInput = new Input(Input.CHECKBOX, IdsForm.IDS_INPUT_NAME, String.valueOf(substitution.getId()));
            checkBoxInput.setDisabled(disabled);
            checkboxTD.setClass(Resources.CLASS_LIST_TABLE_TD);
            checkboxTD.addElement(checkBoxInput);
            return checkboxTD;
        }
    }
}
