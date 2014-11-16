package ru.runa.wfe.var.matcher;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.VariableTypeMatcher;
import ru.runa.wfe.var.file.IFileVariable;

public class FileVariableMatcher implements VariableTypeMatcher {

    @Override
    public boolean matches(Object value) {
        if (IFileVariable.class.isAssignableFrom(value.getClass())) {
            return true;
        }
        if (TypeConversionUtil.isList(value)) {
            // match empty list too in order to prevent filling in to
            // serializable converter in next steps
            int size = TypeConversionUtil.getListSize(value);
            return size == 0 || (size > 0 && TypeConversionUtil.getListValue(value, 0) instanceof IFileVariable);
        }
        return false;
    }

}
