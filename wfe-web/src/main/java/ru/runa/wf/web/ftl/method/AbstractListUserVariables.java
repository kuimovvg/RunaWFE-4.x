package ru.runa.wf.web.ftl.method;

import ru.runa.wfe.commons.ftl.FreemarkerTag;

public abstract class AbstractListUserVariables extends FreemarkerTag {
    private static final long serialVersionUID = 1L;

    protected String variableName;
    protected DisplayMode displayMode;
    protected String sortField;

    protected void initFields() {
        variableName = getParameterAsString(0);
        displayMode = DisplayMode.fromString(getParameterAsString(1));
        sortField = getParameterAsString(2);
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
