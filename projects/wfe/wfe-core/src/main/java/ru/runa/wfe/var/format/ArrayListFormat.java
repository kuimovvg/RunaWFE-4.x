package ru.runa.wfe.var.format;

import java.util.Arrays;
import java.util.List;

public class ArrayListFormat implements VariableFormat<List<?>> {

    @Override
    public List<?> parse(String[] source) {
        return Arrays.asList(source);
    }

    @Override
    public String format(List<?> obj) {
        return obj.toString();
    }

}
