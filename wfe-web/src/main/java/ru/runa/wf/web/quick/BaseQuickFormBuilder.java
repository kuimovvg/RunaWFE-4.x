package ru.runa.wf.web.quick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.common.web.Messages;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.wf.web.FormPresentationUtils;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.commons.ftl.FormHashModel;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.dto.QuickFormVariable;

import com.google.common.base.Charsets;

public abstract class BaseQuickFormBuilder {

    private static final String TEMPLATE_VARIABLE = "variables";
    private static final String TEMPLATE_NAME = "name";

    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_TAG = "tag";
    private static final String ATTRIBUTE_PARAM = "param";

    public String build(User user, PageContext pageContext, Interaction interaction, Long definitionId, IVariableProvider variableProvider) {

        if (interaction.hasForm()) {
            String formTemplate = new String(interaction.getFormData(), Charsets.UTF_8);

            List<QuickFormVariable> templateVariables = new ArrayList<QuickFormVariable>();
            Document document = XmlUtils.parseWithoutValidation(formTemplate);

            String templateName = document.getRootElement().attributeValue(TEMPLATE_NAME);

            List<Element> varElementsList = document.getRootElement().elements(TEMPLATE_VARIABLE);
            for (Element varElement : varElementsList) {
                String tag = varElement.elementText(ATTRIBUTE_TAG);
                QuickFormVariable quickFormVariable = new QuickFormVariable();
                quickFormVariable.setTagName(tag);
                quickFormVariable.setName(varElement.elementText(ATTRIBUTE_NAME));

                List<Element> paramElements = varElement.elements(ATTRIBUTE_PARAM);
                if (paramElements != null && paramElements.size() > 0) {
                    List<String> params = new ArrayList<String>();
                    for (Element paramElement : paramElements) {
                        params.add(paramElement.getText());
                    }
                    quickFormVariable.setParams(params.toArray(new String[0]));
                }

                templateVariables.add(quickFormVariable);
            }

            String out = quickTemplateProcess(user, pageContext, definitionId, templateName, templateVariables);
            if (out == null) {
                String message = "Template of form is not exist";
                if (pageContext != null) {
                    message = Messages.getMessage("template.form.not.exist.error", pageContext);
                }
                return message;
            }
            return ftlTemplateProcess(user, pageContext, definitionId, variableProvider, interaction, out);
        } else {
            String message = "Task form is not defined";
            if (pageContext != null) {
                message = Messages.getMessage("task.form.not.defined.error", pageContext);
            }
            return message;
        }
    }

    private String quickTemplateProcess(User user, PageContext pageContext, Long definitionId, String templateName,
            List<QuickFormVariable> templateVariables) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("variables", templateVariables);
        IVariableProvider variableProvider = new MapDelegableVariableProvider(map, null);
        FormHashModel model = new FormHashModel(user, variableProvider, new StrutsWebHelper(pageContext));
        byte[] bytes = Delegates.getDefinitionService().getProcessDefinitionFile(user, definitionId, templateName);

        if (bytes == null) {
            return null;
        }

        return FreemarkerProcessor.process(new String(bytes, Charsets.UTF_8), model);
    }

    private String ftlTemplateProcess(User user, PageContext pageContext, Long definitionId, IVariableProvider variableProvider,
            Interaction interaction, String template) {
        Map<String, Object> userDefinedVariables = FormSubmissionUtils.getUserFormInputVariables((HttpServletRequest) pageContext.getRequest(),
                interaction);
        if (userDefinedVariables != null) {
            variableProvider = new MapDelegableVariableProvider(userDefinedVariables, variableProvider);
        }
        FormHashModel model = new FormHashModel(user, variableProvider, new StrutsWebHelper(pageContext));

        String out = FreemarkerProcessor.process(template, model);

        return FormPresentationUtils.adjustForm(pageContext, definitionId, out, model.getVariableProvider(), interaction.getRequiredVariableNames());
    }
}
