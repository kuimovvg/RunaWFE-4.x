package ru.runa.wf.web.ftl.method;

import java.util.List;
import java.util.Map;

import ru.runa.wf.web.FormSubmissionUtils;

public class RkDemoRegisterInOutsTag extends EditLinkedListsTag {
    private static final long serialVersionUID = 1L;
    private boolean inInputEnabled = true;
    private boolean outInputEnabled = true;

    @Override
    protected void renderRow(StringBuffer html, List<String> variableNames, List<List<?>> lists, List<String> componentFormatClassNames, int row,
            boolean allowToChangeElements, boolean allowToDeleteElements) {
        int inVariableNameIndex = variableNames.indexOf("время реального входа");
        int outVariableNameIndex = variableNames.indexOf("время реального выхода");
        Object in = (lists.get(inVariableNameIndex).size() > row) ? lists.get(inVariableNameIndex).get(row) : null;
        Object out = (lists.get(outVariableNameIndex).size() > row) ? lists.get(outVariableNameIndex).get(row) : null;
        Map<String, String> errors = FormSubmissionUtils.getUserFormValidationErrors(webHelper.getRequest());
        inInputEnabled = in == null || errors.containsKey("время реального входа[" + row + "]");
        outInputEnabled = (out == null && in != null) || errors.containsKey("время реального выхода[" + row + "]");
        super.renderRow(html, variableNames, lists, componentFormatClassNames, row, allowToChangeElements, allowToDeleteElements);
    }

    @Override
    protected String getComponentInput(String variableName, String formatClassName, Object value, boolean enabled) {
        if (variableName.startsWith("время реального входа")) {
            enabled = inInputEnabled;
        } else if (variableName.startsWith("время реального выхода")) {
            enabled = outInputEnabled;
        } else {
            enabled = false;
        }
        return super.getComponentInput(variableName, formatClassName, value, enabled);
    }
}
