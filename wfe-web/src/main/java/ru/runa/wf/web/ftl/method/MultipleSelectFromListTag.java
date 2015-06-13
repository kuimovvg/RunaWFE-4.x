package ru.runa.wf.web.ftl.method;

import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.commons.ftl.FtlTagVariableSubmissionPostProcessor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.ComplexVariable;
import ru.runa.wfe.var.ISelectable;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.IFileVariable;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.UserTypeFormat;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class MultipleSelectFromListTag extends FreemarkerTag implements FtlTagVariableSubmissionPostProcessor {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object executeTag() {
        String variableName = getParameterAsString(0);
        List<Object> list = getParameterVariableValue(List.class, 1, null);
        if (list == null) {
            list = Lists.newArrayList();
        }
        if (list.size() > 0 && list.get(0) instanceof ISelectable) {
            registerVariablePostProcessor(variableName);
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
            } else if (option instanceof ComplexVariable) {
                ComplexVariable cvar = (ComplexVariable) option;
                UserTypeFormat formatter = new UserTypeFormat(cvar.getUserType());
                optionValue = formatter.formatJSON(cvar);
                optionValue = optionValue.replaceAll("\"", "&quot;");
                WfVariable variable = ViewUtil.createVariable(variableName, cvar.getUserType().getName(), formatter, cvar);
                String hid = cvar.getUserType().getAttributes().get(0) != null ? cvar.getUserType().getAttributes().get(0).getName() + " "
                        + cvar.get(cvar.getUserType().getAttributes().get(0).getName()) : cvar.getUserType().getName();
                optionLabel = variableName + " " + hid + "<br>"
                        + ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variable);
            } else if (option instanceof IFileVariable) {
                FileFormat formatter = new FileFormat();
                IFileVariable file = (IFileVariable) option;
                optionValue = formatter.formatJSON(file);
                optionValue = optionValue.replaceAll("\"", "&quot;");
                WfVariable variable = ViewUtil.createVariable(file.getName(), variableName, formatter, file);
                optionLabel = variableName + ": " + file.getName() + "<br>"
                        + ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), variable);
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
    public Object postProcessValue(Object source) {
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
