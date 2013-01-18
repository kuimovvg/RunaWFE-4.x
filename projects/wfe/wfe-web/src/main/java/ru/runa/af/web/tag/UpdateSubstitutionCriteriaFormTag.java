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

import javax.security.auth.Subject;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.ecs.Element;
import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.UpdateSubstitutionCriteriaAction;
import ru.runa.af.web.form.SubstitutionCriteriaForm;
import ru.runa.af.web.html.BaseDetailTableBuilder;
import ru.runa.af.web.orgfunction.FunctionDef;
import ru.runa.af.web.orgfunction.ParamDef;
import ru.runa.af.web.orgfunction.SubstitutionCriteriaDefinitions;
import ru.runa.af.web.orgfunction.SubstitutionHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.service.af.SubstitutionService;
import ru.runa.service.delegate.Delegates;
import ru.runa.wfe.os.ParamRenderer;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.ss.SubstitutionCriteria;

/**
 * Created on 14.08.2010
 * 
 * @jsp.tag name = "updateSubstitutionCriteriaForm" body-content = "JSP"
 */
public class UpdateSubstitutionCriteriaFormTag extends IdentifiableFormTag {
    private static final long serialVersionUID = 1L;
    private SubstitutionCriteria substitutionCriteria;

    @Override
    protected Identifiable getIdentifiable() throws JspException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillFormData(TD tdFormElement) throws JspException {
        try {
            StringBuffer paramsDiv = new StringBuffer("<div id='rh' style='display: none;'>");
            List<FunctionDef> functions = SubstitutionCriteriaDefinitions.getAll();
            int i = 0;
            for (FunctionDef functionDef : functions) {
                paramsDiv.append("<div id='").append(functionDef.getClassName()).append("'>");
                for (ParamDef paramDef : functionDef.getParams()) {
                    paramsDiv.append("<div>");
                    paramsDiv.append("<span>").append(paramDef.getMessage(pageContext)).append("</span>");
                    paramsDiv.append("<span>").append(createEditElement(paramDef.getRenderer(), getSubject(), pageContext, "", i, false))
                            .append("</span>");
                    paramsDiv.append("</div>");
                }
                paramsDiv.append("</div>");
                i++;
            }
            paramsDiv.append("</div>");
            tdFormElement.addElement(paramsDiv.toString());

            SubstitutionTableBuilder builder = new SubstitutionTableBuilder(pageContext);
            tdFormElement.addElement(builder.buildTable());
        } catch (Exception e) {
            tdFormElement.addElement(e.getMessage());
        }
    }

    @Override
    protected Permission getPermission() {
        return null;
    }

    @Override
    public String getFormButtonName() {
        String message = (substitutionCriteria != null) ? Messages.BUTTON_SAVE : Messages.BUTTON_ADD;
        return Messages.getMessage(message, pageContext);
    }

    @Override
    protected String getTitle() {
        SubstitutionService substitutionService = Delegates.getSubstitutionService();
        if (getIdentifiableId() != null) {
            substitutionCriteria = substitutionService.getSubstitutionCriteria(getSubject(), getIdentifiableId());
        }
        return Messages.getMessage("substitutioncriteria.edit.title", pageContext);
    }

    @Override
    public String getAction() {
        return UpdateSubstitutionCriteriaAction.UPDATE_ACTION;
    }

    class SubstitutionTableBuilder extends BaseDetailTableBuilder {

        private final PageContext pageContext;

        public SubstitutionTableBuilder(PageContext pageContext) {
            this.pageContext = pageContext;
        }

        public Table buildTable() throws AuthenticationException {
            Table table = new Table();
            table.setID("paramsTable");
            table.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE);
            boolean isEnabled = true;
            String criteriaName = "";
            if (substitutionCriteria != null) {
                criteriaName = substitutionCriteria.getName();
            }
            table.addElement(createTRWith2TD(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_NAME, pageContext),
                    SubstitutionCriteriaForm.NAME_INPUT_NAME, criteriaName, false));
            String criteriaType = "";
            if (substitutionCriteria != null) {
                criteriaType = SubstitutionHelper.injectFunction(substitutionCriteria.getClass().getName());
            }
            SubstitutionCriteriaForm form = (SubstitutionCriteriaForm) pageContext.getRequest().getAttribute(SubstitutionCriteriaForm.NAME);
            if (form != null) {
                criteriaType = form.getType();
            }
            Option[] typeOptions = getTypeOptions(criteriaType);
            if (criteriaType.length() == 0 && typeOptions.length > 0) {
                criteriaType = typeOptions[0].getValue();
            }
            table.addElement(createTRWithLabelAndSelect(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_TYPE, pageContext),
                    SubstitutionCriteriaForm.TYPE_INPUT_NAME, typeOptions, !isEnabled));
            if (criteriaType.length() > 0) {
                FunctionDef functionDef = SubstitutionCriteriaDefinitions.getByClassName(criteriaType);
                if (functionDef != null) {
                    for (int i = 0; i < functionDef.getParams().size(); i++) {
                        String value = "";
                        if (substitutionCriteria != null) {
                            value = SubstitutionHelper.injectParameter(substitutionCriteria.getConf(), i);
                        }
                        ParamDef paramDef = functionDef.getParams().get(i);
                        table.addElement(createParameterTR(i, paramDef.getMessage(pageContext),
                                createEditElement(paramDef.getRenderer(), getSubject(), pageContext, value, i, isEnabled)));
                    }
                }
            }
            return table;
        }

        private Option[] getTypeOptions(String selectedValue) throws AuthenticationException {
            List<FunctionDef> definitions = SubstitutionCriteriaDefinitions.getAll();
            Option[] options = new Option[definitions.size()];
            for (int i = 0; i < options.length; i++) {
                options[i] = new Option(definitions.get(i).getClassName());
                options[i].addElement(definitions.get(i).getMessage(pageContext));
            }
            for (Option option : options) {
                if (selectedValue.equals(option.getValue())) {
                    option.setSelected(true);
                    break;
                }
            }
            return options;
        }

    }

    private TR createParameterTR(int index, String label, Element element) {
        TR tr = new TR();
        tr.addAttribute("paramIndex", index);
        tr.addElement(new TD(label).setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(element).setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    private Element createEditElement(ParamRenderer renderer, Subject subject, PageContext pageContext, String value, int index, boolean enabled) {
        Span span = new Span();
        Input input = new Input(Input.TEXT, SubstitutionCriteriaForm.CONF_INPUT_NAME, value);
        input.setDisabled(!enabled);
        input.addAttribute("paramIndex", index);
        span.addElement(input);
        if (renderer.hasJSEditor()) {
            span.addElement(Entities.NBSP);
            String url = "javascript:editParameter('" + index + "','" + renderer.getClass().getName() + "');";
            A selectorHref = new A(url);
            selectorHref.addElement(Messages.getMessage("substitution.select", pageContext));
            span.addElement(selectorHref);
        }
        return span;
    }

}
