package org.jbpm.ui.bsh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.ui.DesignerLogger;
import org.jbpm.ui.common.model.Variable;
import org.jbpm.ui.dialog.NumberInputDialog;
import org.jbpm.ui.dialog.UserInputDialog;
import org.jbpm.ui.resource.Messages;

public abstract class BSHTypeSupport {
    private static final String DEFAULT_FORMAT_NAME = "org.jbpm.web.formgen.format.DefaultFormat";

    private static final Map<String, BSHTypeSupport> TYPES_MAP = new HashMap<String, BSHTypeSupport>();
    static {
        TYPES_MAP.put(DEFAULT_FORMAT_NAME, new StringType());
        TYPES_MAP.put("ru.runa.wf.web.forms.format.StringFormat", new StringType());
        TYPES_MAP.put("ru.runa.wf.web.forms.format.BooleanFormat", new BooleanType());
        TYPES_MAP.put("org.jbpm.web.formgen.format.DoubleFormat", new NumberType());
        TYPES_MAP.put("ru.runa.wf.web.forms.format.LongFormat", new NumberType());
        TYPES_MAP.put("ru.runa.wf.web.forms.format.DateFormat", new DateType());
        TYPES_MAP.put("ru.runa.wf.web.forms.format.TimeFormat", new DateType());
        TYPES_MAP.put("ru.runa.wf.web.forms.format.DateTimeFormat", new DateType());
        TYPES_MAP.put("ru.runa.wf.web.forms.format.FileFormat", new DefaultType());
    }

    public static BSHTypeSupport getByFormat(String format) {
        if (format == null) {
            format = DEFAULT_FORMAT_NAME;
        }
        BSHTypeSupport typeSupport = TYPES_MAP.get(format);
        if (typeSupport == null) {
            DesignerLogger.logInfo("Not found type support for format: " + format + ", using default");
            typeSupport = TYPES_MAP.get(DEFAULT_FORMAT_NAME);
        }
        return typeSupport;
    }

    public boolean hasUserInputEditor() {
        return true;
    }

    public List<String> getPredefinedValues(Operation operation) {
        List<String> v = new ArrayList<String>();
        if (operation == Operation.EQ || operation == Operation.NOT_EQ) {
            v.add("null");
        }
        return v;
    }

    public UserInputDialog createUserInputDialog(String title, String initialValue) {
        return new UserInputDialog(title, initialValue);
    }

    abstract String wrap(Object value);

    public String unwrapValue(String value) {
        return value;
    }

    abstract List<Operation> getTypedOperations();

    private static class DefaultType extends BSHTypeSupport {

        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getName();
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public boolean hasUserInputEditor() {
            return false;
        }

        @Override
        List<Operation> getTypedOperations() {
            return null;
        }
    }

    static class StringType extends BSHTypeSupport {

        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getName();
            } else if (value instanceof String) {
                return "\"" + value + "\"";
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public String unwrapValue(String value) {
            return value.substring(1, value.length() - 1);
        }

        @Override
        List<Operation> getTypedOperations() {
            List<Operation> extOperations = new ArrayList<Operation>();
            extOperations.add(new Operation(Messages.getString("BSH.Operation.contains"), "contains") {

                @Override
                public String generateCode(Variable variable, Object lexem2) {
                    StringBuffer buffer = new StringBuffer("");
                    buffer.append(wrap(variable));
                    buffer.append(".contains(");
                    buffer.append(wrap(lexem2));
                    buffer.append(")");
                    return buffer.toString();
                }

            });
            return extOperations;
        }
    }

    private static class BooleanType extends BSHTypeSupport {

        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getName() + ".booleanValue()";
            } else if (value instanceof String) {
                return (String) value;
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public List<String> getPredefinedValues(Operation operation) {
            List<String> v = super.getPredefinedValues(operation);
            v.add("true");
            v.add("false");
            return v;
        }

        @Override
        public boolean hasUserInputEditor() {
            return false;
        }

        @Override
        List<Operation> getTypedOperations() {
            return null;
        }
    }

    private static class NumberType extends BSHTypeSupport {

        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getName() + ".doubleValue()";
            } else if (value instanceof String) {
                return (String) value;
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public UserInputDialog createUserInputDialog(String title, String initialValue) {
            return new NumberInputDialog(initialValue);
        }

        @Override
        List<Operation> getTypedOperations() {
            List<Operation> extOperations = new ArrayList<Operation>();
            extOperations.add(new Operation(Messages.getString("BSH.Operation.more"), ">"));
            extOperations.add(new Operation(Messages.getString("BSH.Operation.less"), "<"));
            extOperations.add(new Operation(Messages.getString("BSH.Operation.moreeq"), ">="));
            extOperations.add(new Operation(Messages.getString("BSH.Operation.lesseq"), "<="));
            return extOperations;
        }
    }

    private static class DateType extends BSHTypeSupport {

        @Override
        String wrap(Object value) {
            if (value instanceof Variable) {
                return ((Variable) value).getName() + ".getTime()";
            } else if (value instanceof String) {
                return (String) value;
            } else {
                throw new IllegalArgumentException("value class is " + value.getClass().getName());
            }
        }

        @Override
        public boolean hasUserInputEditor() {
            return false;
        }

        @Override
        List<Operation> getTypedOperations() {
            List<Operation> extOperations = new ArrayList<Operation>();
            extOperations.add(new Operation(Messages.getString("BSH.Operation.earlier"), "<"));
            extOperations.add(new Operation(Messages.getString("BSH.Operation.later"), ">"));
            extOperations.add(new Operation(Messages.getString("BSH.Operation.earliereq"), "<="));
            extOperations.add(new Operation(Messages.getString("BSH.Operation.latereq"), ">="));
            return extOperations;
        }
    }
}
