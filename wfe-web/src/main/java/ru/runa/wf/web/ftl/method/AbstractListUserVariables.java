package ru.runa.wf.web.ftl.method;

import java.util.List;

import ru.runa.wfe.commons.ftl.FreemarkerTag;
import ru.runa.wfe.var.ComplexVariable;

abstract class AbstractListUserVariables extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    protected List<ComplexVariable> variable;
    protected String variableName;
    protected String dectVariableName;
    protected DisplayMode displayMode;
    protected String sortField;

    @SuppressWarnings("unchecked")
    protected void initFields() {
        if (getClass().equals(DisplayListUserVariablesTag.class)) {
            variableName = getParameterAsString(0);
            displayMode = DisplayMode.fromString(getParameterAsString(1));
            sortField = getParameterAsString(2);
        } else if (getClass().equals(MultipleSelectFromListUserVariablesTag.class)) {
            variableName = getParameterAsString(1);
            variable = variableProvider.getValue(List.class, variableName);
            dectVariableName = getParameterAsString(0);
            displayMode = DisplayMode.fromString(getParameterAsString(2));
            sortField = getParameterAsString(3);
        }
    }

    @Override
    abstract protected Object executeTag() throws Exception;

    public enum DisplayMode {
        TWO_DIMENTIONAL_TABLE("two-dimentional"), MULTI_DIMENTIONAL_TABLE("multi-dimentional");

        private final String mode;

        private DisplayMode(String s) {
            mode = s;
        }

        public static final DisplayMode fromString(String md) {
            for (DisplayMode dm : DisplayMode.values()) {
                if (!dm.mode.equals(md)) {
                    continue;
                }
                return dm;
            }
            return null;
        }
    }

}
