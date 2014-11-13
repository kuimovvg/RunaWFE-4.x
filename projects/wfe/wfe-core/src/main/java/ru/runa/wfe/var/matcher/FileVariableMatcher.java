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
        return TypeConversionUtil.isList(value) && TypeConversionUtil.getListSize(value) > 0
                && TypeConversionUtil.getListValue(value, 0) instanceof IFileVariable;
    }

}
