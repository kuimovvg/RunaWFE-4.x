package ru.runa.gpd.handler.decision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.dialog.NumberInputDialog;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.wfe.var.format.ArrayListFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TimeFormat;

public abstract class BSHTypeSupport {
    private static final String DEFAULT_FORMAT_NAME = StringFormat.class.getName();
    private static final Map<String, BSHTypeSupport> TYPES_MAP = new HashMap<String, BSHTypeSupport>();
    static {
        TYPES_MAP.put(DEFAULT_FORMAT_NAME, new StringType());
        TYPES_MAP.put(StringFormat.class.getName(), new StringType());
        TYPES_MAP.put(BooleanFormat.class.getName(), new BooleanType());
        TYPES_MAP.put(DoubleFormat.class.getName(), new NumberType());
        TYPES_MAP.put(LongFormat.class.getName(), new NumberType());
        TYPES_MAP.put(DateFormat.class.getName(), new DateType());
        TYPES_MAP.put(TimeFormat.class.getName(), new DateType());
        TYPES_MAP.put(DateTimeFormat.class.getName(), new DateType());
        TYPES_MAP.put(FileFormat.class.getName(), new DefaultType());
        TYPES_MAP.put(ArrayListFormat.class.getName(), new DefaultType()); // TODO make mapping by java class
    }

    public static BSHTypeSupport getByFormat(String format) {
        if (format == null) {
            format = DEFAULT_FORMAT_NAME;
        }
        BSHTypeSupport typeSupport = TYPES_MAP.get(format);
        if (typeSupport == null) {
            PluginLogger.logInfo("Not found type support for format: " + format + ", using default");
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
            extOperations.add(new Operation(Localization.getString("BSH.Operation.contains"), "contains") {
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
            extOperations.add(new Operation(Localization.getString("BSH.Operation.more"), ">"));
            extOperations.add(new Operation(Localization.getString("BSH.Operation.less"), "<"));
            extOperations.add(new Operation(Localization.getString("BSH.Operation.moreeq"), ">="));
            extOperations.add(new Operation(Localization.getString("BSH.Operation.lesseq"), "<="));
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
            extOperations.add(new Operation(Localization.getString("BSH.Operation.earlier"), "<"));
            extOperations.add(new Operation(Localization.getString("BSH.Operation.later"), ">"));
            extOperations.add(new Operation(Localization.getString("BSH.Operation.earliereq"), "<="));
            extOperations.add(new Operation(Localization.getString("BSH.Operation.latereq"), ">="));
            return extOperations;
        }
    }
}
