package ru.runa.wf.web.ftl.method;

import java.util.List;

public class RkDemoDisplayLinkedListsForEnteredGuestsTag extends DisplayLinkedListsTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void renderRow(StringBuffer buffer, List<String> variableNames, List<List<?>> lists, List<String> componentFormatClassNames, int row) {
        int inVariableNameIndex = variableNames.indexOf("время реального входа");
        int outVariableNameIndex = variableNames.indexOf("время реального выхода");
        Object in = (lists.get(inVariableNameIndex).size() > row) ? lists.get(inVariableNameIndex).get(row) : null;
        Object out = (lists.get(outVariableNameIndex).size() > row) ? lists.get(outVariableNameIndex).get(row) : null;
        if (in != null && out == null) {
            super.renderRow(buffer, variableNames, lists, componentFormatClassNames, row);
        }
    }
}
