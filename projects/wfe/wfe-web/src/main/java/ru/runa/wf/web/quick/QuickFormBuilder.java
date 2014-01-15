package ru.runa.wf.web.quick;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wf.web.ftl.FtlFormBuilder;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.dto.QuickFormVariable;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class QuickFormBuilder extends FtlFormBuilder {
    private static final String ELEMENT_TAGS = "tags";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ELEMENT_TAG = "tag";
    private static final String ELEMENT_PARAM = "param";

    @Override
    protected String buildForm(IVariableProvider variableProvider) {
        String quickForm = new String(interaction.getFormData(), Charsets.UTF_8);
        List<QuickFormVariable> templateVariables = new ArrayList<QuickFormVariable>();
        Document document = XmlUtils.parseWithoutValidation(quickForm);
        Element tagsElement = document.getRootElement().element(ELEMENT_TAGS);
        List<Element> varElementsList = tagsElement.elements(ELEMENT_TAG);
        for (Element varElement : varElementsList) {
            String tag = varElement.elementText(ATTRIBUTE_NAME);
            QuickFormVariable quickFormVariable = new QuickFormVariable();
            quickFormVariable.setTagName(tag);
            List<Element> paramElements = varElement.elements(ELEMENT_PARAM);
            if (paramElements != null && paramElements.size() > 0) {
                List<String> params = new ArrayList<String>();
                int index = 0;
                for (Element paramElement : paramElements) {
                    if (index == 0) {
                        quickFormVariable.setName(paramElement.getText());
                    } else {
                        params.add(paramElement.getText());
                    }
                    index++;
                }
                quickFormVariable.setParams(params.toArray(new String[0]));
            }
            templateVariables.add(quickFormVariable);
        }
        String template = processFormTemplate(interaction.getTemplateData(), templateVariables);
        return processFreemarkerTemplate(template, variableProvider);
    }

    private String processFormTemplate(byte[] templateData, List<QuickFormVariable> templateVariables) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("variables", templateVariables);
        IVariableProvider variableProvider = new MapDelegableVariableProvider(map, null);
        Preconditions.checkNotNull(templateData, "Template is required");
        return processFreemarkerTemplate(new String(templateData, Charsets.UTF_8), variableProvider);
    }

}
