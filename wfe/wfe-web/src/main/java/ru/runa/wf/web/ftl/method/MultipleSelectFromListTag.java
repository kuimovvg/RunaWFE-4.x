package ru.runa.wf.web.ftl.method;

import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.commons.ftl.FtlTagVariableHandler;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.ISelectable;

import com.google.common.collect.Lists;

import freemarker.template.TemplateModelException;

@SuppressWarnings("unchecked")
public class MultipleSelectFromListTag extends FreemarkerTag implements FtlTagVariableHandler {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() throws TemplateModelException {
        String variableName = getParameterAsString(0);
        List<Object> list = getParameterVariableValue(List.class, 1, null);
        if (list == null) {
            list = Lists.newArrayList();
        }
        if (list.size() > 0 && list.get(0) instanceof ISelectable) {
            registerVariableHandler(variableName);
        }
        List<Object> selectedValues = variableProvider.getValue(List.class, variableName);
        StringBuffer html = new StringBuffer();
        html.append("<span class=\"multipleSelectFromList\">");
        for (Object option : list) {
            String optionValue;
            String optionLabel;
            if (option instanceof ISelectable) {
                ISelectable selectable = (ISelectable) option;
                optionValue = selectable.getValue();
                optionLabel = selectable.getLabel();
            } else if (option instanceof Executor) {
                Executor executor = (Executor) option;
                optionValue = "ID" + executor.getId();
                optionLabel = executor.getLabel();
            } else {
                optionValue = String.valueOf(option);
                optionLabel = String.valueOf(option);
            }
            String id = variableName + "_" + optionValue;
            html.append("<input id=\"").append(id).append("\"");
            html.append(" type=\"checkbox\" value=\"").append(optionValue).append("\"");
            html.append(" name=\"").append(variableName).append("\"");
            if (selectedValues != null && selectedValues.contains(option)) {
                html.append(" checked=\"true\"");
            }
            html.append("style=\"width: 30px;\">");
            html.append("<label for=\"").append(id).append("\">");
            html.append(optionLabel);
            html.append("</label><br>");
        }
        html.append("</span>");
        return html;
    }

    @Override
    public Object handle(Object source) throws TemplateModelException {
        if (source instanceof List) {
            List<String> valuesList = (List<String>) source;
            List<ISelectable> list = getParameterVariableValueNotNull(List.class, 1);
            List<ISelectable> selectedOptions = Lists.newArrayListWithExpectedSize(valuesList.size());
            for (String selectedValue : valuesList) {
                for (ISelectable option : list) {
                    if (selectedValue.equals(option.getValue())) {
                        selectedOptions.add(option);
                        break;
                    }
                }
            }
            return selectedOptions;
        }
        return source;
    }

}
